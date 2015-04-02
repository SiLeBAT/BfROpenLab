/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
import java.util.List;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.TileController;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;

public abstract class OsmCanvas<V extends Node> extends GisCanvas<V> implements
		TileLoaderListener {

	private static final long serialVersionUID = 1L;

	private TileSource tileSource;
	private TileController tileController;

	private Integer lastZoom;
	private Coordinate lastTopLeft;
	private Coordinate lastBottomRight;

	public OsmCanvas(List<V> nodes, List<Edge<V>> edges,
			NodePropertySchema nodeSchema, EdgePropertySchema edgeSchema,
			Naming naming) {
		super(nodes, edges, nodeSchema, edgeSchema, naming);
		setTileSource(new OsmTileSource.Mapnik());
		viewer.addPostRenderPaintable(new PostPaintable());

		lastZoom = null;

	}

	public TileSource getTileSource() {
		return tileSource;
	}

	public void setTileSource(TileSource tileSource) {
		if (tileController != null) {
			tileController.cancelOutstandingJobs();
		}

		this.tileSource = tileSource;
		tileController = new TileController(tileSource, new MemoryTileCache(),
				this);
	}

	@Override
	public void borderAlphaChanged() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void resetLayoutItemClicked() {
		setTransform(Transform.IDENTITY_TRANSFORM);
	}

	@Override
	public void tileLoadingFinished(Tile tile, boolean success) {
		flushImage();
		viewer.repaint();
	}

	@Override
	public VisualizationImageServer<V, Edge<V>> getVisualizationServer(
			boolean toSvg) {
		VisualizationImageServer<V, Edge<V>> server = super
				.getVisualizationServer(toSvg);

		server.addPostRenderPaintable(new PostPaintable());

		return server;
	}

	@Override
	protected void applyTransform() {
		flushImage();
		viewer.repaint();
	}

	@Override
	protected GraphMouse<V, Edge<V>> createGraphMouse() {
		return new GraphMouse<>(new GisPickingPlugin(), 2.0);
	}

	@Override
	protected ZoomingPaintable createZoomingPaintable() {
		return new ZoomingPaintable(this, 2.0);
	}

	@Override
	protected void paintGis(Graphics g, boolean toSvg) {
		int w = getCanvasSize().width;
		int h = getCanvasSize().height;
		int tileSize = tileSource.getTileSize();
		int zoom = (int) Math.round(Math.log(transform.getScaleX())
				/ Math.log(2.0));
		double x = -transform.getTranslationX();
		double y = -transform.getTranslationY();
		int startX = Math.max(0, (int) x / tileSize);
		int startY = Math.max(0, (int) y / tileSize);
		int dx = (int) Math.round(startX * tileSize - x);
		int dy = (int) Math.round(startY * tileSize - y);
		int max = (int) Math.round(Math.pow(2.0, zoom)) - 1;
		int maxX = Math.min(max, startX + (w - dx) / tileSize);
		int maxY = Math.min(max, startY + (h - dy) / tileSize);

		for (int ix = startX; ix <= maxX; ix++) {
			for (int iy = startY; iy <= maxY; iy++) {
				Tile tile = tileController.getTile(ix, iy, zoom);

				tile.paint(g, (ix - startX) * tileSize + dx, (iy - startY)
						* tileSize + dy);
			}
		}

		int size = (int) (Math.pow(2.0, zoom) * tileSize);

		g.setColor(Color.BLACK);
		g.drawRect((int) transform.getTranslationX(),
				(int) transform.getTranslationY(), size, size);

		lastZoom = zoom;
		lastTopLeft = new Coordinate(
				OsmMercator.YToLat(startY * tileSize, zoom),
				OsmMercator.XToLon(startY * tileSize, zoom));
		lastBottomRight = new Coordinate(OsmMercator.YToLat((maxY + 1)
				* tileSize, zoom), OsmMercator.XToLon((maxX + 1) * tileSize,
				zoom));
	}

	private class PostPaintable implements Paintable {

		@Override
		public void paint(Graphics g) {
			String text = tileSource.getAttributionText(lastZoom, lastTopLeft,
					lastBottomRight);
			Image img = tileSource.getAttributionImage();
			int startY = 0;

			if (text != null) {
				Font font = new Font("Default", Font.PLAIN, 9);
				int w = (int) font.getStringBounds(text,
						((Graphics2D) g).getFontRenderContext()).getWidth();
				int h = g.getFontMetrics(font).getHeight();
				int d = 3;

				g.setColor(CanvasUtils.LEGEND_BACKGROUND);
				g.fillRect(-1, -1, w + 2 * d + 1, h + 1);
				g.setColor(Color.BLACK);
				g.drawRect(-1, -1, w + 2 * d + 1, h + 1);
				g.setFont(font);
				g.drawString(text, d, g.getFontMetrics(font).getAscent());
				startY += h;
			}

			if (img != null) {
				g.drawImage(img, 0, startY, null);
			}
		}

		@Override
		public boolean useTransform() {
			return false;
		}
	}
}
