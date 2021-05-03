/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.FeatureAdapter;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.TileController;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import com.google.common.base.Strings;

import de.bund.bfr.jung.BetterGraphMouse;
import de.bund.bfr.jung.ZoomingPaintable;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.Naming;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;

public abstract class OsmCanvas<V extends Node> extends GisCanvas<V> implements TileLoaderListener {

	private static final long serialVersionUID = 1L;
	
	private static final String HTTP_HEADER_USER_AGENT_ATTR = "user-agent";
	private static final String HTTP_HEADER_USER_AGENT_VALUE = "BfROpenLab"; 
	
	private TileController tileController;

	private int lastZoom;
	private Coordinate lastTopLeft;
	private Coordinate lastBottomRight;

	public OsmCanvas(List<V> nodes, List<Edge<V>> edges, NodePropertySchema nodeSchema, EdgePropertySchema edgeSchema,
			Naming naming) {
		super(nodes, edges, nodeSchema, edgeSchema, naming);
		
		tileController = new TileController(new OsmTileSource.Mapnik(), new MemoryTileCache(), this);
		OsmTileLoader tileLoader = (OsmTileLoader) tileController.getTileLoader();
		tileLoader.headers.put(HTTP_HEADER_USER_AGENT_ATTR, HTTP_HEADER_USER_AGENT_VALUE);
		lastZoom = -1;
		lastTopLeft = null;
		lastBottomRight = null;
		viewer.addPostRenderPaintable(new PostPaintable());
	}

	public TileSource getTileSource() {
		return tileController.getTileSource();
	}

	public void setTileSource(TileSource tileSource) {
		tileController.cancelOutstandingJobs();
		tileController.setTileSource(tileSource);
	}

	public void loadAllTiles() {
		int tileSize = tileController.getTileSource().getTileSize();
		int maxTiles = (getCanvasSize().width / tileSize + 2) * (getCanvasSize().height / tileSize + 2);
		MemoryTileCache tileCache = (MemoryTileCache) tileController.getTileCache();

		tileCache.setCacheSize(Math.max(tileCache.getCacheSize(), maxTiles));
		getTiles(true);
	}

	@Override
	public void tileLoadingFinished(Tile tile, boolean success) {
        flushImage();
		viewer.repaint();
	}

	@Override
	public VisualizationImageServer<V, Edge<V>> getVisualizationServer(boolean toSvg) {
		VisualizationImageServer<V, Edge<V>> server = super.getVisualizationServer(toSvg);

		server.addPostRenderPaintable(new PostPaintable());

		return server;
	}

	@Override
	protected void applyTransform() {
		flushImage();
		viewer.repaint();
	}

	@Override
	protected void paintGis(Graphics2D g, boolean toSvg, boolean onWhiteBackground) {
		getTiles(false).forEach((pos, tile) -> tile.paint(g, pos.x, pos.y));

		int size = (int) (Math.pow(2.0, lastZoom) * tileController.getTileSource().getTileSize());
		Color currentColor = g.getColor();

		g.setColor(Color.BLACK);
		g.drawRect((int) transform.getTranslationX(), (int) transform.getTranslationY(), size, size);
		g.setColor(currentColor);
	}

	private Map<Point, Tile> getTiles(boolean waitForLoading) {
		int w = getCanvasSize().width;
		int h = getCanvasSize().height;
		int tileSize = tileController.getTileSource().getTileSize();
		int zoom = (int) Math.round(Math.log(transform.getScaleX()) / Math.log(2.0));
		double x = -transform.getTranslationX();
		double y = -transform.getTranslationY();
		int startX = Math.max(0, (int) x / tileSize);
		int startY = Math.max(0, (int) y / tileSize);
		int dx = (int) Math.round(startX * tileSize - x);
		int dy = (int) Math.round(startY * tileSize - y);
		int max = (int) Math.round(Math.pow(2.0, zoom)) - 1;
		int maxX = Math.min(max, startX + (w - dx) / tileSize);
		int maxY = Math.min(max, startY + (h - dy) / tileSize);

		lastZoom = zoom;
		lastTopLeft = new Coordinate(new OsmMercator().yToLat(startY * tileSize, zoom),
				new OsmMercator().xToLon(startY * tileSize, zoom));
		lastBottomRight = new Coordinate(new OsmMercator().yToLat((maxY + 1) * tileSize, zoom),
				new OsmMercator().xToLon((maxX + 1) * tileSize, zoom));

		Map<Point, Tile> tiles = new LinkedHashMap<>();

		if (zoom < 0) return tiles;
		
		for (int ix = startX; ix <= maxX; ix++) {
			for (int iy = startY; iy <= maxY; iy++) {
				Tile tile = tileController.getTile(ix, iy, zoom);

				if (waitForLoading) {
					while (!tile.isLoaded()) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
						}
					}
				}

				tiles.put(new Point((ix - startX) * tileSize + dx, (iy - startY) * tileSize + dy), tile);
			}
		}
		
		return tiles;
	}

	private class PostPaintable implements Paintable, MouseMotionListener, MouseListener {

		private Rectangle textRect;
		private Rectangle imgRect;

		private boolean textFocused;
		private boolean imgFocused;

		public PostPaintable() {
			textRect = null;
			imgRect = null;
			textFocused = false;
			imgFocused = false;

			getViewer().addMouseMotionListener(this);
			getViewer().addMouseListener(this);
		}

		@Override
		public void paint(Graphics graphics) {
			textRect = null;
			imgRect = null;

			if (lastZoom < 0) {
				return;
			}

			String text = tileController.getTileSource().getAttributionText(lastZoom, lastTopLeft, lastBottomRight);
			Image img = tileController.getTileSource().getAttributionImage();
			int startY = 0;
			Graphics2D g = (Graphics2D) graphics;
			Color currentColor = g.getColor();
			Font currentFont = g.getFont();

			if (!Strings.isNullOrEmpty(text)) {
				Font font = new Font("Default", Font.PLAIN, 9);
				int w = (int) font.getStringBounds(text, g.getFontRenderContext()).getWidth();
				int h = g.getFontMetrics(font).getHeight();
				int d = 3;

				g.setColor(ZoomingPaintable.BACKGROUND);
				g.fillRect(0, 0, w + 2 * d, h);
				g.setColor(Color.BLACK);
				g.drawRect(0, 0, w + 2 * d, h);
				g.setColor(textFocused ? Color.BLUE : Color.BLACK);
				g.setFont(font);
				g.drawString(text, d, g.getFontMetrics(font).getAscent());

				startY += h + d;
				textRect = new Rectangle(0, 0, w + 2 * d, h);
			}

			if (img != null) {
				g.drawImage(img, 0, startY, null);
				imgRect = new Rectangle(0, startY, img.getWidth(null), img.getHeight(null));
			}

			g.setColor(currentColor);
			g.setFont(currentFont);
		}

		@Override
		public boolean useTransform() {
			return false;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			boolean newTextFocused = textRect != null && textRect.contains(e.getPoint());
			boolean newImgFocused = imgRect != null && imgRect.contains(e.getPoint());
			boolean changed = newTextFocused != textFocused || newImgFocused != imgFocused;

			textFocused = newTextFocused;
			imgFocused = newImgFocused;

			if (changed) {
				BetterGraphMouse<?, ?> graphMouse = (BetterGraphMouse<?, ?>) getViewer().getGraphMouse();

				graphMouse.setPickingDeactivated(textFocused || imgFocused);
				paint(getViewer().getGraphics());
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			String textLink = tileController.getTileSource().getAttributionLinkURL();
			String imgLink = tileController.getTileSource().getAttributionImageURL();

			if (e.getButton() == MouseEvent.BUTTON1) {
				if (textFocused && textLink != null) {
					FeatureAdapter.openLink(textLink);
				} else if (imgFocused && imgLink != null) {
					FeatureAdapter.openLink(imgLink);
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
	}
}
