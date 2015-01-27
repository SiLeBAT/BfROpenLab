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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;

public abstract class GisCanvas<V extends Node> extends Canvas<V> {

	private static final long serialVersionUID = 1L;

	private BufferedImage image;
	private Map<String, Paint> regionFillPaints;

	public GisCanvas(List<V> nodes, List<Edge<V>> edges,
			NodePropertySchema nodeSchema, EdgePropertySchema edgeSchema,
			Naming naming) {
		super(nodes, edges, nodeSchema, edgeSchema, naming);
		image = null;
		regionFillPaints = new LinkedHashMap<>();

		viewer.addPreRenderPaintable(new PrePaintable(false));
	}

	public abstract Collection<RegionNode> getRegions();

	public Map<String, Paint> getRegionFillPaints() {
		return regionFillPaints;
	}

	public void setRegionFillPaints(Map<String, Paint> regionFillPaints) {
		this.regionFillPaints = regionFillPaints;
	}

	@Override
	public void setCanvasSize(Dimension canvasSize) {
		super.setCanvasSize(canvasSize);
		computeTransform(canvasSize);
	}

	@Override
	public void resetLayoutItemClicked() {
		computeTransform(viewer.getSize());
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
	protected void applyTransform() {
		flushImage();
		computeTransformedShapes();
		viewer.repaint();
	}

	protected void flushImage() {
		if (image != null) {
			image.flush();
			image = null;
		}
	}

	protected void paintGis(Graphics g, boolean toSvg) {
		for (RegionNode node : getRegions()) {
			Paint paint = regionFillPaints.get(node.getId());

			if (paint == null) {
				continue;
			}

			((Graphics2D) g).setPaint(paint);

			for (Polygon part : node.getTransformedPolygon()) {
				g.fillPolygon(part);
			}
		}

		if (!toSvg) {
			BufferedImage borderImage = new BufferedImage(
					getCanvasSize().width, getCanvasSize().height,
					BufferedImage.TYPE_INT_ARGB);
			Graphics borderGraphics = borderImage.getGraphics();

			borderGraphics.setColor(Color.BLACK);

			for (RegionNode node : getRegions()) {
				for (Polygon part : node.getTransformedPolygon()) {
					borderGraphics.drawPolygon(part);
				}
			}

			CanvasUtils.drawImageWithAlpha(g, borderImage, getBorderAlpha());
		} else {
			g.setColor(new Color(0, 0, 0, getBorderAlpha()));

			for (RegionNode node : getRegions()) {
				for (Polygon part : node.getTransformedPolygon()) {
					g.drawPolygon(part);
				}
			}
		}
	}

	private void computeTransform(Dimension canvasSize) {
		Rectangle2D polygonsBounds = getPolygonsBounds();

		if (polygonsBounds != null) {
			double widthRatio = canvasSize.width / polygonsBounds.getWidth();
			double heightRatio = canvasSize.height / polygonsBounds.getHeight();
			double canvasCenterX = canvasSize.width / 2.0;
			double canvasCenterY = canvasSize.height / 2.0;
			double polygonCenterX = polygonsBounds.getCenterX();
			double polygonCenterY = polygonsBounds.getCenterY();

			double scaleX = Math.min(widthRatio, heightRatio);
			double scaleY = -scaleX;
			double translationX = canvasCenterX - polygonCenterX * scaleX;
			double translationY = canvasCenterY - polygonCenterY * scaleY;

			setTransform(new Transform(scaleX, scaleY, translationX,
					translationY));
		} else {
			setTransform(Transform.IDENTITY_TRANSFORM);
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

	private void computeTransformedShapes() {
		for (RegionNode node : getRegions()) {
			node.setTransform(transform);
		}
	}

	private Rectangle2D getPolygonsBounds() {
		Rectangle2D bounds = null;

		for (RegionNode node : getRegions()) {
			if (bounds == null) {
				bounds = node.getBoundingBox();
			} else {
				bounds = bounds.createUnion(node.getBoundingBox());
			}
		}

		return bounds;
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
