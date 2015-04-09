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
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;

import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;

public abstract class ShapefileCanvas<V extends Node> extends GisCanvas<V> {

	private static final long serialVersionUID = 1L;

	private boolean regionsTransformed;

	public ShapefileCanvas(List<V> nodes, List<Edge<V>> edges,
			NodePropertySchema nodeSchema, EdgePropertySchema edgeSchema,
			Naming naming) {
		super(nodes, edges, nodeSchema, edgeSchema, naming);
		regionsTransformed = false;
	}

	public abstract Collection<RegionNode> getRegions();

	@Override
	public void resetLayoutItemClicked() {
		Rectangle2D bounds = CanvasUtils.getRegionBounds(getRegions());

		if (bounds != null) {
			zoomTo(bounds, null, true);
		} else {
			super.resetLayoutItemClicked();
		}
	}

	@Override
	public void borderAlphaChanged() {
		flushImage();
		viewer.repaint();
	}

	@Override
	protected void applyTransform() {
		flushImage();
		regionsTransformed = false;
		viewer.repaint();
	}

	@Override
	protected GraphMouse<V, Edge<V>> createGraphMouse() {
		return new GraphMouse<>(new GisPickingPlugin(), 1.1);
	}

	@Override
	protected void paintGis(Graphics g, boolean toSvg) {
		if (!regionsTransformed) {
			for (RegionNode node : getRegions()) {
				node.setTransform(transform);
			}

			regionsTransformed = true;
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
}
