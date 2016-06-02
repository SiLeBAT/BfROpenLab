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
	protected void drawSimpleEdge(RenderContext<V, E> rc, Layout<V, E> layout, E e) {
		GraphicsDecorator g = rc.getGraphicsContext();
		Graph<V, E> graph = layout.getGraph();
		Pair<V> endpoints = graph.getEndpoints(e);
		Point2D p1 = layout.transform(endpoints.getFirst());
		Point2D p2 = layout.transform(endpoints.getSecond());
		p1 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p1);
		p2 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p2);
		float x1 = (float) p1.getX();
		float y1 = (float) p1.getY();
		float x2 = (float) p2.getX();
		float y2 = (float) p2.getY();
		Shape edgeShape = rc.getEdgeShapeTransformer().transform(Context.<Graph<V, E>, E> getInstance(graph, e));
		Rectangle deviceRectangle = new Rectangle(rc.getScreenDevice().getSize());
		AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

		if (endpoints.getFirst().equals(endpoints.getSecond())) {
			Rectangle2D bounds = rc.getVertexShapeTransformer().transform(endpoints.getFirst()).getBounds2D();

			xform.scale(bounds.getWidth(), bounds.getHeight());
			xform.translate(0, -edgeShape.getBounds2D().getWidth() / 2);
		} else {
			float dx = x2 - x1;
			float dy = y2 - y1;

			xform.rotate(Math.atan2(dy, dx));
			xform.scale(Math.sqrt(dx * dx + dy * dy), 1.0);
		}

		edgeShape = xform.createTransformedShape(edgeShape);

		if (rc.getMultiLayerTransformer().getTransformer(Layer.VIEW).transform(edgeShape).intersects(deviceRectangle)) {
			Paint oldPaint = g.getPaint();
			BasicStroke stroke = (BasicStroke) rc.getEdgeStrokeTransformer().transform(e);
			Paint draw_paint = rc.getEdgeDrawPaintTransformer().transform(e);
			Paint fill_paint = rc.getEdgeFillPaintTransformer().transform(e);

			if (draw_paint != null) {
				g.setStroke(new BasicStroke(stroke.getLineWidth() + 4, stroke.getEndCap(), stroke.getLineJoin(),
						stroke.getMiterLimit(), stroke.getDashArray(), stroke.getDashPhase()));
				g.setPaint(draw_paint);
				g.draw(edgeShape);
			}

			if (fill_paint != null) {
				g.setStroke(stroke);
				g.setPaint(fill_paint);
				g.draw(edgeShape);
			}

			if (rc.getEdgeArrowPredicate().evaluate(Context.<Graph<V, E>, E> getInstance(graph, e))) {
				Stroke new_stroke = rc.getEdgeArrowStrokeTransformer().transform(e);
				Stroke old_stroke = g.getStroke();

				if (new_stroke != null) {
					g.setStroke(new_stroke);
				}

				Shape destVertexShape = rc.getVertexShapeTransformer().transform(graph.getEndpoints(e).getSecond());
				AffineTransform xf = AffineTransform.getTranslateInstance(x2, y2);
				destVertexShape = xf.createTransformedShape(destVertexShape);

				if (rc.getMultiLayerTransformer().getTransformer(Layer.VIEW).transform(destVertexShape)
						.intersects(deviceRectangle)) {
					AffineTransform at = edgeArrowRenderingSupport.getArrowTransform(rc, edgeShape, destVertexShape);
					Shape arrow = rc.getEdgeArrowTransformer()
							.transform(Context.<Graph<V, E>, E> getInstance(graph, e));

					arrow = at.createTransformedShape(arrow);
					g.setPaint(rc.getArrowFillPaintTransformer().transform(e));
					g.fill(arrow);
					g.setPaint(rc.getArrowDrawPaintTransformer().transform(e));
					g.draw(arrow);
				}

				if (new_stroke != null) {
					g.setStroke(old_stroke);
				}
			}

			g.setPaint(oldPaint);
		}
	}
}
