/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
import java.awt.Paint;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import de.bund.bfr.jung.BetterPickingGraphMousePlugin;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;

public class RegionCanvasUtils {

	private RegionCanvasUtils() {
	}

	public static BetterPickingGraphMousePlugin<RegionNode, Edge<RegionNode>> createPickingPlugin(
			GisCanvas<RegionNode> canvas) {
		return new BetterPickingGraphMousePlugin<RegionNode, Edge<RegionNode>>(false) {

			@Override
			protected RegionNode getPickedNode(MouseEvent e) {
				RegionNode node = super.getPickedNode(e);

				if (node != null) {
					return node;
				} else if (getPickedEdge(e) != null) {
					return null;
				}

				Point2D p = canvas.getTransform().applyInverse(e.getX(), e.getY());

				return canvas.getNodes().stream().filter(n -> n.containsPoint(p)).findAny().orElse(null);
			}
		};
	}

	public static Rectangle2D getBounds(Collection<RegionNode> nodes) {
		Rectangle2D bounds = null;

		for (RegionNode node : nodes) {
			bounds = bounds != null ? bounds.createUnion(node.getBoundingBox()) : node.getBoundingBox();
		}

		return bounds;
	}

	public static void paintRegions(Graphics2D g, Collection<RegionNode> nodes, Set<RegionNode> selectedNodes,
			HighlightConditionList nodeHighlightConditions) {
		Paint currentPaint = g.getPaint();

		g.setPaint(Color.BLUE);
		selectedNodes.forEach(n -> g.fill(n.getTransformedPolygon()));

		List<Color> nodeColors = new ArrayList<>();
		ListMultimap<RegionNode, Double> nodeAlphas = ArrayListMultimap.create();
		boolean prioritize = nodeHighlightConditions.isPrioritizeColors();

		for (HighlightCondition condition : nodeHighlightConditions.getConditions()) {
			Map<RegionNode, Double> values = condition.getValues(nodes);

			if (condition.getColor() != null) {
				nodeColors.add(condition.getColor());

				for (RegionNode node : nodes) {
					List<Double> alphas = nodeAlphas.get(node);

					if (!prioritize || alphas.isEmpty() || Collections.max(alphas) == 0.0) {
						alphas.add(values.get(node));
					} else {
						alphas.add(0.0);
					}
				}
			}
		}

		for (RegionNode node : nodes) {
			Paint color = CanvasUtils.mixColors(Color.WHITE, nodeColors, nodeAlphas.get(node), false);

			if (!color.equals(Color.WHITE) && !selectedNodes.contains(node)) {
				g.setPaint(color);
				g.fill(node.getTransformedPolygon());
			}
		}

		g.setPaint(currentPaint);
	}
}
