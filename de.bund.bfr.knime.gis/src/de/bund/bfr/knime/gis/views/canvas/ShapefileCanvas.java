/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;

import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.Naming;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;

public abstract class ShapefileCanvas<V extends Node> extends GisCanvas<V> {

	private static final long serialVersionUID = 1L;

	public ShapefileCanvas(List<V> nodes, List<Edge<V>> edges, NodePropertySchema nodeSchema,
			EdgePropertySchema edgeSchema, Naming naming) {
		super(nodes, edges, nodeSchema, edgeSchema, naming);
	}

	public abstract Collection<RegionNode> getRegions();

	@Override
	public void resetLayoutItemClicked() {
		Rectangle2D bounds = RegionCanvasUtils.getBounds(getRegions());

		if (bounds != null) {
			setTransform(CanvasUtils.getTransformForBounds(getCanvasSize(), bounds, 2.0));
			transformFinished();
		} else {
			super.resetLayoutItemClicked();
		}
	}

	@Override
	public void borderAlphaChanged() {
		flushImage();
		viewer.repaint();
		super.borderAlphaChanged();
	}

	@Override
	protected void applyTransform() {
		flushImage();

		for (RegionNode node : getRegions()) {
			node.createTransformedPolygons(transform);
		}

		viewer.repaint();
	}

	@Override
	protected void paintGis(Graphics2D g, boolean toSvg, boolean onWhiteBackground) {
		int borderAlpha = getOptionsPanel().getBorderAlpha();
		Color currentColor = g.getColor();

		if (onWhiteBackground) {
			g.setColor(new Color(255 - borderAlpha, 255 - borderAlpha, 255 - borderAlpha));

			for (RegionNode node : getRegions()) {
				g.draw(node.getTransformedPolygon());
			}
		} else if (toSvg || borderAlpha == 255) {
			g.setColor(new Color(0, 0, 0, borderAlpha));

			for (RegionNode node : getRegions()) {
				g.draw(node.getTransformedPolygon());
			}
		} else {
			BufferedImage borderImage = new BufferedImage(getCanvasSize().width, getCanvasSize().height,
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D borderGraphics = borderImage.createGraphics();

			borderGraphics.setColor(Color.BLACK);

			for (RegionNode node : getRegions()) {
				borderGraphics.draw(node.getTransformedPolygon());
			}

			CanvasUtils.drawImageWithAlpha(g, borderImage, borderAlpha);
			borderImage.flush();
		}

		g.setColor(currentColor);
	}
}
