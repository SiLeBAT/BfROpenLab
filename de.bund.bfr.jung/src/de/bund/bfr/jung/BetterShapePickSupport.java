/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;

public class BetterShapePickSupport<V, E> implements GraphElementAccessor<V, E> {

	private VisualizationServer<V, E> vv;

	public BetterShapePickSupport(VisualizationServer<V, E> vv) {
		this.vv = vv;
	}

	@Override
	public V getVertex(Layout<V, E> layout, double x, double y) {
		V closest = null;
		double minDistance = Double.MAX_VALUE;
		Point2D ip = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW,
				new Point2D.Double(x, y));

		for (V v : layout.getGraph().getVertices()) {
			if (!vv.getRenderContext().getVertexIncludePredicate()
					.evaluate(Context.<Graph<V, E>, V>getInstance(layout.getGraph(), v))) {
				continue;
			}

			Shape shape = vv.getRenderContext().getVertexShapeTransformer().transform(v);
			Point2D p = layout.transform(v);

			if (p == null) {
				continue;
			}

			p = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, p);

			double ox = ip.getX() - p.getX();
			double oy = ip.getY() - p.getY();

			if (shape.contains(ox, oy)) {
				if (vv.getPickedVertexState().isPicked(v)) {
					return v;
				}

				Rectangle2D bounds = shape.getBounds2D();
				double dx = bounds.getCenterX() - ox;
				double dy = bounds.getCenterY() - oy;
				double dist = dx * dx + dy * dy;

				if (dist < minDistance) {
					minDistance = dist;
					closest = v;
				}
			}
		}

		return closest;
	}

	@Override
	public Collection<V> getVertices(Layout<V, E> layout, Shape shape) {
		Set<V> pickedVertices = new HashSet<>();
		Shape iShape = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, shape);

		for (V v : layout.getGraph().getVertices()) {
			if (!vv.getRenderContext().getVertexIncludePredicate()
					.evaluate(Context.<Graph<V, E>, V>getInstance(layout.getGraph(), v))) {
				continue;
			}

			Point2D p = layout.transform(v);

			if (p == null) {
				continue;
			}

			p = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, p);

			if (iShape.contains(p)) {
				pickedVertices.add(v);
			}
		}

		return pickedVertices;
	}

	@Override
	public E getEdge(Layout<V, E> layout, double x, double y) {
		Point2D ip = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW,
				new Point2D.Double(x, y));
		E closest = null;
		float minDistance = Float.POSITIVE_INFINITY;

		for (E e : layout.getGraph().getEdges()) {
			Shape edgeShape = JungUtils.getTransformedEdgeShape(vv.getRenderContext(), layout, e);

			if (edgeShape == null) {
				continue;
			}

			float[] seg = new float[6];
			float lastX = Float.NaN;
			float lastY = Float.NaN;

			for (PathIterator i = new GeneralPath(edgeShape).getPathIterator(null, 1); !i.isDone(); i.next()) {
				i.currentSegment(seg);

				float newX = seg[0];
				float newY = seg[1];

				if (!Float.isNaN(lastX)) {
					float dist = getDistanceToLine(lastX, lastY, newX, newY, (float) ip.getX(), (float) ip.getY());

					if (dist < minDistance) {
						minDistance = dist;
						closest = e;
					}
				}

				lastX = newX;
				lastY = newY;
			}
		}

		return minDistance < 7 ? closest : null;
	}

	private static float getDistanceToLine(float x1, float y1, float x2, float y2, float x, float y) {
		float dx = (x2 - x1);
		float dy = (y2 - y1);
		float denom = dx * dx + dy * dy;
		float U = ((x - x1) * dx + (y - y1) * dy) / denom;

		float rx = x1 + U * dx;
		float ry = y1 + U * dy;
		float minx = Math.min(x1, x2);
		float maxx = Math.max(x1, x2);
		float miny = Math.min(y1, y2);
		float maxy = Math.max(y1, y2);

		if (rx < minx || rx > maxx || ry < miny || ry > maxy) {
			return Float.POSITIVE_INFINITY;
		}

		return (float) (Math.abs(dy * x - dx * y + x2 * y1 - y2 * x1) / Math.sqrt(denom));
	}
}
