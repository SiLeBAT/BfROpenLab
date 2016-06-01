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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;

import de.bund.bfr.knime.Pair;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.util.ArrowFactory;

public class JungUtils {

	private JungUtils() {
	}

	public static <V, E> Transformer<V, Stroke> newNodeStrokeTransformer(RenderContext<V, E> renderContext,
			Set<V> metaNodes) {
		return node -> metaNodes.contains(node) || renderContext.getPickedVertexState().isPicked(node)
				? new BasicStroke(4.0f) : new BasicStroke(1.0f);
	}

	public static <V, E> Transformer<V, Paint> newNodeDrawTransformer(RenderContext<V, E> renderContext) {
		return node -> renderContext.getPickedVertexState().isPicked(node) ? Color.BLUE : Color.BLACK;
	}

	public static <V, E> Transformer<V, Paint> newNodeFillTransformer(RenderContext<V, E> renderContext,
			Map<V, Paint> nodeColors) {
		return node -> {
			Paint color = nodeColors != null && nodeColors.containsKey(node) ? nodeColors.get(node) : Color.WHITE;

			return renderContext.getPickedVertexState().isPicked(node) ? mixWithBlue(color) : color;
		};
	}

	public static <V> Transformer<V, Shape> newNodeShapeTransformer(int size, Integer maxSize,
			Map<V, Double> thicknessValues) {
		double denom = getDenominator(thicknessValues);
		int max = maxSize != null ? maxSize : size * 2;

		return node -> {
			Double factor = thicknessValues != null ? thicknessValues.get(node) : null;
			double s = factor != null ? size + (max - size) * factor / denom : size;

			return new Ellipse2D.Double(-s / 2, -s / 2, s, s);
		};
	}

	public static <V, E> Transformer<E, Paint> newEdgeDrawTransformer(RenderContext<V, E> renderContext,
			Map<E, Paint> edgeColors) {
		return edge -> {
			if (renderContext.getPickedEdgeState().isPicked(edge)) {
				return Color.GREEN;
			} else if (edgeColors != null && edgeColors.containsKey(edge)) {
				return edgeColors.get(edge);
			} else {
				return Color.BLACK;
			}
		};
	}

	public static <V, E> Pair<Transformer<E, Stroke>, Transformer<Context<Graph<V, E>, E>, Shape>> newEdgeStrokeArrowTransformers(
			int thickness, Integer maxThickness, Map<E, Double> thicknessValues) {
		double denom = getDenominator(thicknessValues);
		int max = maxThickness != null ? maxThickness : thickness + 10;

		Transformer<E, Stroke> strokeTransformer = edge -> {
			Double factor = thicknessValues != null ? thicknessValues.get(edge) : null;
			double width = factor != null ? thickness + (max - thickness) * factor / denom : thickness;

			return new BasicStroke((float) width);
		};
		Transformer<Context<Graph<V, E>, E>, Shape> arrowTransformer = edge -> {
			BasicStroke stroke = (BasicStroke) strokeTransformer.transform(edge.element);

			return ArrowFactory.getNotchedArrow(stroke.getLineWidth() + 8, 2 * stroke.getLineWidth() + 10, 4);
		};

		return new Pair<>(strokeTransformer, arrowTransformer);
	}

	private static double getDenominator(Map<?, Double> values) {
		if (values == null || values.isEmpty()) {
			return 1.0;
		}

		double max = Collections.max(values.values());

		return max != 0.0 ? max : 1.0;
	}

	static <V, E> Shape getTransformedEdgeShape(RenderContext<V, E> rc, Layout<V, E> layout, E e) {
		Graph<V, E> graph = layout.getGraph();
		edu.uci.ics.jung.graph.util.Pair<V> endpoints = graph.getEndpoints(e);
		V v1 = endpoints.getFirst();
		V v2 = endpoints.getSecond();

		if (!rc.getEdgeIncludePredicate().evaluate(Context.<Graph<V, E>, E> getInstance(graph, e))
				|| !rc.getVertexIncludePredicate().evaluate(Context.<Graph<V, E>, V> getInstance(graph, v1))
				|| !rc.getVertexIncludePredicate().evaluate(Context.<Graph<V, E>, V> getInstance(graph, v2))) {
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

	static Line2D getLineInMiddle(Shape edgeShape) {
		GeneralPath path = new GeneralPath(edgeShape);
		float[] seg = new float[6];
		List<Point2D> points = new ArrayList<>();

		for (PathIterator i = path.getPathIterator(null, 1); !i.isDone(); i.next()) {
			i.currentSegment(seg);
			points.add(new Point2D.Float(seg[0], seg[1]));
		}

		Point2D first = points.get(0);
		Point2D last = points.get(points.size() - 1);

		if (first.equals(last)) {
			Point2D minP = points.stream().min((p1, p2) -> Double.compare(p1.getY(), p2.getY())).get();

			return new Line2D.Float(minP, new Point2D.Float((float) (minP.getX() + 1.0), (float) minP.getY()));
		} else {
			for (int i = 0; i < points.size() - 1; i++) {
				Point2D p1 = points.get(i);
				Point2D p2 = points.get(i + 1);

				if (p2.distance(last) < p2.distance(first)) {
					Line2D ortho = getOrthogonal(new Line2D.Float(first, last));
					Point2D pp1 = getIntersection(new Line2D.Float(p1, p2), ortho);
					Point2D pp2 = new Point2D.Float((float) (pp1.getX() + last.getX() - first.getX()),
							(float) (pp1.getY() + last.getY() - first.getY()));

					return new Line2D.Float(pp1, pp2);
				}
			}
		}

		return null;
	}

	private static Point2D getIntersection(Line2D l1, Line2D l2) {
		float x1 = (float) l1.getX1();
		float x2 = (float) l1.getX2();
		float x3 = (float) l2.getX1();
		float x4 = (float) l2.getX2();
		float y1 = (float) l1.getY1();
		float y2 = (float) l1.getY2();
		float y3 = (float) l2.getY1();
		float y4 = (float) l2.getY2();
		float factor1 = x1 * y2 - y1 * x2;
		float factor2 = x3 * y4 - y3 * x4;
		float denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

		float x = (factor1 * (x3 - x4) - (x1 - x2) * factor2) / denom;
		float y = (factor1 * (y3 - y4) - (y1 - y2) * factor2) / denom;

		return new Point2D.Float(x, y);
	}

	private static Line2D getOrthogonal(Line2D l) {
		float x1 = (float) l.getX1();
		float x2 = (float) l.getX2();
		float y1 = (float) l.getY1();
		float y2 = (float) l.getY2();
		float dx = x2 - x1;
		float dy = y2 - y1;

		float nx1 = x1 + dx / 2;
		float ny1 = y1 + dy / 2;
		float nx2 = nx1 - dy;
		float ny2 = ny1 + dx;

		return new Line2D.Float(nx1, ny1, nx2, ny2);
	}

	private static Paint mixWithBlue(Paint paint) {
		if (paint instanceof Color) {
			Color c = (Color) paint;

			return new Color(c.getRed() / 2, c.getGreen() / 2, (c.getBlue() + 255) / 2, (c.getAlpha() + 255) / 2);
		} else if (paint instanceof TexturePaint) {
			BufferedImage texture = ((TexturePaint) paint).getImage();
			BufferedImage mixed = new BufferedImage(texture.getWidth(), texture.getHeight(), texture.getType());

			for (int x = 0; x < texture.getWidth(); x++) {
				for (int y = 0; y < texture.getHeight(); y++) {
					mixed.setRGB(x, y, ((Color) mixWithBlue(new Color(texture.getRGB(x, y)))).getRGB());
				}
			}

			return new TexturePaint(mixed, new Rectangle(mixed.getWidth(), mixed.getHeight()));
		} else {
			return paint;
		}
	}
}
