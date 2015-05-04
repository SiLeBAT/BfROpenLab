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
package de.bund.bfr.knime.gis.views.canvas.jung;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;

public class JungUtils {

	private JungUtils() {
	}

	public static <V, E> Shape getTransformedEdgeShape(RenderContext<V, E> rc, Layout<V, E> layout,
			E e) {
		Graph<V, E> graph = layout.getGraph();
		Pair<V> endpoints = graph.getEndpoints(e);
		V v1 = endpoints.getFirst();
		V v2 = endpoints.getSecond();

		if (!rc.getEdgeIncludePredicate().evaluate(Context.<Graph<V, E>, E> getInstance(graph, e))
				|| !rc.getVertexIncludePredicate().evaluate(
						Context.<Graph<V, E>, V> getInstance(graph, v1))
				|| !rc.getVertexIncludePredicate().evaluate(
						Context.<Graph<V, E>, V> getInstance(graph, v2))) {
			return null;
		}

		Point2D p1 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, layout.transform(v1));
		Point2D p2 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, layout.transform(v2));
		float x1 = (float) p1.getX();
		float y1 = (float) p1.getY();
		float x2 = (float) p2.getX();
		float y2 = (float) p2.getY();
		Shape edgeShape = rc.getEdgeShapeTransformer().transform(Context.getInstance(graph, e));
		AffineTransform edgeShapeTransform = AffineTransform.getTranslateInstance(x1, y1);

		if (v1.equals(v2)) {
			Rectangle2D bounds = rc.getVertexShapeTransformer().transform(v1).getBounds2D();

			edgeShapeTransform.scale(bounds.getWidth(), bounds.getHeight());
			edgeShapeTransform.translate(0, -edgeShape.getBounds2D().getWidth() / 2);
		} else {
			float dx = x2 - x1;
			float dy = y2 - y1;

			edgeShapeTransform.rotate(Math.atan2(dy, dx));
			edgeShapeTransform.scale(Math.sqrt(dx * dx + dy * dy), 1.0);
		}

		return edgeShapeTransform.createTransformedShape(edgeShape);
	}
}
