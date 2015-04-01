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

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.TileController;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;

public abstract class OsmCanvas<V extends Node> extends Canvas<V> implements
		TileLoaderListener {

	private static final long serialVersionUID = 1L;

	private BufferedImage image;

	private TileSource tileSource;
	private TileController tileController;

	public OsmCanvas(List<V> nodes, List<Edge<V>> edges,
			NodePropertySchema nodeSchema, EdgePropertySchema edgeSchema,
			Naming naming) {
		super(nodes, edges, nodeSchema, edgeSchema, naming);
		image = null;

		tileSource = new OsmTileSource.Mapnik();
		tileController = new TileController(tileSource, new MemoryTileCache(),
				this);

		viewer.addPreRenderPaintable(new PrePaintable(false));
	}

	@Override
	public void resetLayoutItemClicked() {
		setTransform(Transform.IDENTITY_TRANSFORM);
	}

	@Override
	public void layoutItemClicked(LayoutType layoutType) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void borderAlphaChanged() {
		flushImage();
		viewer.repaint();
	}

	@Override
	public VisualizationImageServer<V, Edge<V>> getVisualizationServer(
			final boolean toSvg) {
		VisualizationImageServer<V, Edge<V>> server = super
				.getVisualizationServer(toSvg);

		server.addPreRenderPaintable(new PrePaintable(toSvg));

		return server;
	}

	@Override
	public void tileLoadingFinished(Tile tile, boolean success) {
		flushImage();
		viewer.repaint();
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
		return new ZoomingPaintable(this, 0, 2.0);
	}

	protected void flushImage() {
		if (image != null) {
			image.flush();
			image = null;
		}
	}

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
	}

	private void paintGisImage(Graphics g) {
		int width = getCanvasSize().width;
		int height = getCanvasSize().height;

		if (image == null || image.getWidth() != width
				|| image.getHeight() != height) {
			flushImage();
			image = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_ARGB);
			paintGis(image.getGraphics(), false);
		}

		g.drawImage(image, 0, 0, null);
	}

	protected class GisPickingPlugin extends PickingPlugin {

		@Override
		public void mouseDragged(MouseEvent e) {
			if (vertex == null) {
				super.mouseDragged(e);
			}
		}
	}

	private class PrePaintable implements Paintable {

		private boolean toSvg;

		public PrePaintable(boolean toSvg) {
			this.toSvg = toSvg;
		}

		@Override
		public boolean useTransform() {
			return false;
		}

		@Override
		public void paint(Graphics g) {
			if (toSvg) {
				paintGis(g, true);
			} else {
				paintGisImage(g);
			}
		}
	}
}
