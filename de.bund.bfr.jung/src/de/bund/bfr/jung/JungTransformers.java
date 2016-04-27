/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.jung;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.Pair;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.util.ArrowFactory;

public class JungTransformers {

	private JungTransformers() {
	}

	public static <V, E> Transformer<V, Paint> nodeFillTransformer(RenderContext<V, E> renderContext,
			Map<V, Paint> nodeColors) {
		return node -> {
			if (renderContext.getPickedVertexState().getPicked().contains(node)) {
				return Color.BLUE;
			} else if (nodeColors != null && nodeColors.containsKey(node)) {
				return nodeColors.get(node);
			} else {
				return Color.WHITE;
			}
		};
	}

	public static <V> Transformer<V, Shape> nodeShapeTransformer(int size, Integer maxSize,
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

	public static <V, E> Transformer<E, Paint> edgeDrawTransformer(RenderContext<V, E> renderContext,
			Map<E, Paint> edgeColors) {
		return edge -> {
			if (renderContext.getPickedEdgeState().getPicked().contains(edge)) {
				return Color.GREEN;
			} else if (edgeColors != null && edgeColors.containsKey(edge)) {
				return edgeColors.get(edge);
			} else {
				return Color.BLACK;
			}
		};
	}

	public static <V, E> Pair<Transformer<E, Stroke>, Transformer<Context<Graph<V, E>, E>, Shape>> edgeStrokeArrowTransformers(
			int thickness, Integer maxThickness, Map<E, Double> thicknessValues) {
		double denom = getDenominator(thicknessValues.values());
		Transformer<E, Stroke> strokeTransformer = edge -> {
			int max = maxThickness != null ? maxThickness : thickness + 10;
			Double factor = thicknessValues.get(edge);

			if (factor == null) {
				factor = 0.0;
			}

			return new BasicStroke((float) (thickness + (max - thickness) * factor / denom));
		};
		Transformer<Context<Graph<V, E>, E>, Shape> arrowTransformer = edge -> {
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
