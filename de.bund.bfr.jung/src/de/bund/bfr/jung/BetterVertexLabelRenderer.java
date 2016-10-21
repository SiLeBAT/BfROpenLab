/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.renderers.Renderer;

public class BetterVertexLabelRenderer<V, E> implements Renderer.VertexLabel<V, E> {

	private LabelPosition position;

	public BetterVertexLabelRenderer(LabelPosition position) {
		this.position = position;
	}

	@Override
	public void labelVertex(RenderContext<V, E> rc, Layout<V, E> layout, V v, String label) {
		if (!rc.getVertexIncludePredicate().evaluate(Context.<Graph<V, E>, V>getInstance(layout.getGraph(), v))) {
			return;
		}

		Point2D vPos = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, layout.transform(v));
		AffineTransform transform = AffineTransform.getTranslateInstance(vPos.getX(), vPos.getY());
		Shape shape = transform.createTransformedShape(rc.getVertexShapeTransformer().transform(v));

		Component component = rc.getVertexLabelRenderer().<V>getVertexLabelRendererComponent(rc.getScreenDevice(),
				label, rc.getVertexFontTransformer().transform(v), rc.getPickedVertexState().isPicked(v), v);
		Dimension size = component.getPreferredSize();
		Point p = getAnchorPoint(shape.getBounds2D(), size);

		rc.getGraphicsContext().draw(component, rc.getRendererPane(), p.x, p.y, size.width, size.height, true);
	}

	@Override
	public Position getPosition() {
		throw new UnsupportedOperationException("");
	}

	@Override
	public void setPosition(Position position) {
		throw new UnsupportedOperationException("");
	}

	@Override
	public Positioner getPositioner() {
		throw new UnsupportedOperationException("");
	}

	@Override
	public void setPositioner(Positioner positioner) {
		throw new UnsupportedOperationException("");
	}

	private Point getAnchorPoint(Rectangle2D vertexBounds, Dimension labelSize) {
		switch (position.getPosition()) {
		case N:
			return new Point((int) vertexBounds.getCenterX() - labelSize.width / 2,
					(int) vertexBounds.getMinY() - labelSize.height);
		case NE:
			return new Point((int) vertexBounds.getMaxX(), (int) vertexBounds.getMinY() - labelSize.height);
		case E:
			return new Point((int) vertexBounds.getMaxX(), (int) vertexBounds.getCenterY() - labelSize.height / 2);
		case SE:
			return new Point((int) vertexBounds.getMaxX(), (int) vertexBounds.getMaxY());
		case S:
			return new Point((int) vertexBounds.getCenterX() - labelSize.width / 2, (int) vertexBounds.getMaxY());
		case SW:
			return new Point((int) vertexBounds.getMinX() - labelSize.width, (int) vertexBounds.getMaxY());
		case W:
			return new Point((int) vertexBounds.getMinX() - labelSize.width,
					(int) vertexBounds.getCenterY() - labelSize.height / 2);
		case NW:
			return new Point((int) vertexBounds.getMinX() - labelSize.width,
					(int) vertexBounds.getMinY() - labelSize.height);
		case CNTR:
			return new Point((int) vertexBounds.getCenterX() - labelSize.width / 2,
					(int) vertexBounds.getCenterY() - labelSize.height / 2);
		case AUTO:
			throw new IllegalArgumentException("Position \"" + position + "\" is not supported");
		default:
			throw new RuntimeException("Unknown Position: " + position);
		}
	}
}
