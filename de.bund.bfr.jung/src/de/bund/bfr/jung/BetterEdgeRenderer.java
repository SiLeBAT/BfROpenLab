/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.renderers.BasicEdgeRenderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

public class BetterEdgeRenderer<V, E> extends BasicEdgeRenderer<V, E> {

	@Override
	@SuppressWarnings("unchecked")
	public void paintEdge(RenderContext<V, E> rc, Layout<V, E> layout, E e) {
		GraphicsDecorator g = rc.getGraphicsContext();
		Graph<V, E> graph = layout.getGraph();

		if (!rc.getEdgeIncludePredicate().evaluate(Context.<Graph<V, E>, E>getInstance(graph, e))) {
			return;
		}

		Pair<V> endpoints = graph.getEndpoints(e);

		if (!rc.getVertexIncludePredicate().evaluate(Context.<Graph<V, E>, V>getInstance(graph, endpoints.getFirst()))
				|| !rc.getVertexIncludePredicate()
						.evaluate(Context.<Graph<V, E>, V>getInstance(graph, endpoints.getSecond()))) {
			return;
		}

		Point2D p1 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, layout.transform(endpoints.getFirst()));
		Point2D p2 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, layout.transform(endpoints.getSecond()));
		Shape edgeShape = rc.getEdgeShapeTransformer().transform(Context.<Graph<V, E>, E>getInstance(graph, e));
		Rectangle deviceRectangle = new Rectangle(rc.getScreenDevice().getSize());
		AffineTransform edgeTransform = AffineTransform.getTranslateInstance(p1.getX(), p1.getY());

		if (endpoints.getFirst().equals(endpoints.getSecond())) {
			Rectangle2D bounds = rc.getVertexShapeTransformer().transform(endpoints.getFirst()).getBounds2D();

			edgeTransform.scale(bounds.getWidth(), bounds.getHeight());
			edgeTransform.translate(0, -edgeShape.getBounds2D().getWidth() / 2);
		} else {
			double dx = p2.getX() - p1.getX();
			double dy = p2.getY() - p1.getY();

			edgeTransform.rotate(Math.atan2(dy, dx));
			edgeTransform.scale(Math.sqrt(dx * dx + dy * dy), 1.0);
		}

		edgeShape = edgeTransform.createTransformedShape(edgeShape);

		if (rc.getMultiLayerTransformer().getTransformer(Layer.VIEW).transform(edgeShape).intersects(deviceRectangle)) {
			Paint oldPaint = g.getPaint();
			Stroke oldStroke = g.getStroke();

			BasicStroke stroke = (BasicStroke) rc.getEdgeStrokeTransformer().transform(e);
			Paint drawPaint = rc.getEdgeDrawPaintTransformer().transform(e);

			if (drawPaint != null) {
				g.setStroke(new BasicStroke(stroke.getLineWidth() + 4, stroke.getEndCap(), stroke.getLineJoin(),
						stroke.getMiterLimit(), stroke.getDashArray(), stroke.getDashPhase()));
				g.setPaint(drawPaint);
				g.draw(edgeShape);
			}

			g.setStroke(stroke);
			g.setPaint(rc.getEdgeFillPaintTransformer().transform(e));
			g.draw(edgeShape);

			if (rc.getEdgeArrowPredicate().evaluate(Context.<Graph<V, E>, E>getInstance(graph, e))) {
				AffineTransform destVertexTransform = AffineTransform.getTranslateInstance(p2.getX(), p2.getY());
				Shape destVertexShape = destVertexTransform.createTransformedShape(
						rc.getVertexShapeTransformer().transform(graph.getEndpoints(e).getSecond()));

				if (rc.getMultiLayerTransformer().getTransformer(Layer.VIEW).transform(destVertexShape)
						.intersects(deviceRectangle)) {
					AffineTransform arrowTransform = edgeArrowRenderingSupport.getArrowTransform(rc, edgeShape,
							destVertexShape);
					Shape arrow = arrowTransform.createTransformedShape(
							rc.getEdgeArrowTransformer().transform(Context.<Graph<V, E>, E>getInstance(graph, e)));

					g.setStroke(rc.getEdgeArrowStrokeTransformer().transform(e));
					g.setPaint(rc.getArrowFillPaintTransformer().transform(e));
					g.fill(arrow);
					g.setPaint(rc.getArrowDrawPaintTransformer().transform(e));
					g.draw(arrow);
				}
			}

			g.setPaint(oldPaint);
			g.setStroke(oldStroke);
		}
	}
}
