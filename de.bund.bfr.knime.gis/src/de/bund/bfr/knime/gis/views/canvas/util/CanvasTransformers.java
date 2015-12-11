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
package de.bund.bfr.knime.gis.views.canvas.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.Pair;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.util.ArrowFactory;

public class CanvasTransformers {

	private CanvasTransformers() {
	}

	public static <V extends Node> Transformer<V, Paint> nodeFillTransformer(RenderContext<V, Edge<V>> renderContext,
			Map<V, List<Double>> alphaValues, List<Color> colors) {
		Map<V, Paint> nodeColors = new LinkedHashMap<>();

		if (alphaValues != null && colors != null) {
			for (Map.Entry<V, List<Double>> entry : alphaValues.entrySet()) {
				nodeColors.put(entry.getKey(), CanvasUtils.mixColors(Color.WHITE, colors, entry.getValue(), false));
			}
		}

		return node -> {
			if (renderContext.getPickedVertexState().getPicked().contains(node)) {
				return Color.BLUE;
			} else if (nodeColors.containsKey(node)) {
				return nodeColors.get(node);
			} else {
				return Color.WHITE;
			}
		};
	}

	public static <V extends Node> Transformer<V, Shape> nodeShapeTransformer(int size, Integer maxSize,
			Map<V, Double> thicknessValues) {
		Map<V, Double> nonNullThicknessValues = KnimeUtils.nullToEmpty(thicknessValues);
		double denom = getDenominator(nonNullThicknessValues.values());

		return node -> {
			int max = maxSize != null ? maxSize : size * 2;
			Double factor = nonNullThicknessValues.get(node);

			if (factor == null) {
				factor = 0.0;
			}

			double s = size + (max - size) * factor / denom;

			return new Ellipse2D.Double(-s / 2, -s / 2, s, s);
		};
	}

	public static <V extends Node> Transformer<V, Stroke> nodeStrokeTransformer(String metaNodeProperty) {
		return node -> {
			Boolean isMetaNode = (Boolean) node.getProperties().get(metaNodeProperty);

			return isMetaNode != null && isMetaNode ? new BasicStroke(4.0f) : new BasicStroke(1.0f);
		};
	}

	public static <V extends Node> Transformer<Edge<V>, Paint> edgeDrawTransformer(
			RenderContext<V, Edge<V>> renderContext, Map<Edge<V>, List<Double>> alphaValues, List<Color> colors) {
		Map<Edge<V>, Paint> edgeColors = new LinkedHashMap<>();

		if (alphaValues != null && colors != null) {
			for (Map.Entry<Edge<V>, List<Double>> entry : alphaValues.entrySet()) {
				edgeColors.put(entry.getKey(), CanvasUtils.mixColors(Color.BLACK, colors, entry.getValue(), true));
			}
		}

		return edge -> {
			if (renderContext.getPickedEdgeState().getPicked().contains(edge)) {
				return Color.GREEN;
			} else if (edgeColors.containsKey(edge)) {
				return edgeColors.get(edge);
			} else {
				return Color.BLACK;
			}
		};
	}

	public static <V extends Node> Pair<Transformer<Edge<V>, Stroke>, Transformer<Context<Graph<V, Edge<V>>, Edge<V>>, Shape>> edgeStrokeArrowTransformers(
			int thickness, Integer maxThickness, Map<Edge<V>, Double> thicknessValues) {
		double denom = getDenominator(thicknessValues.values());
		Transformer<Edge<V>, Stroke> strokeTransformer = edge -> {
			int max = maxThickness != null ? maxThickness : thickness + 10;
			Double factor = thicknessValues.get(edge);

			if (factor == null) {
				factor = 0.0;
			}

			return new BasicStroke((float) (thickness + (max - thickness) * factor / denom));
		};
		Transformer<Context<Graph<V, Edge<V>>, Edge<V>>, Shape> arrowTransformer = edge -> {
			BasicStroke stroke = (BasicStroke) strokeTransformer.transform(edge.element);

			return ArrowFactory.getNotchedArrow(stroke.getLineWidth() + 8, 2 * stroke.getLineWidth() + 10, 4);
		};

		return new Pair<>(strokeTransformer, arrowTransformer);
	}

	private static double getDenominator(Collection<Double> values) {
		if (values.isEmpty()) {
			return 1.0;
		}

		double max = Collections.max(values);

		return max != 0.0 ? max : 1.0;
	}
}
