/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
 ******************************************************************************/
package de.bund.bfr.knime.gis.views.canvas.transformer;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.renderers.EdgeArrowRenderingSupport;

public class MiddleEdgeArrowRenderingSupport<V, E> implements
		EdgeArrowRenderingSupport<V, E> {

	@Override
	public AffineTransform getArrowTransform(RenderContext<V, E> rc,
			Shape edgeShape, Shape vertexShape) {
		GeneralPath path = new GeneralPath(edgeShape);
		float[] seg = new float[6];
		Point2D first = null;
		Point2D last = null;

		for (PathIterator i = path.getPathIterator(null, 1); !i.isDone(); i
				.next()) {
			i.currentSegment(seg);

			if (first == null) {
				first = new Point2D.Float(seg[0], seg[1]);
			}

			last = new Point2D.Float(seg[0], seg[1]);
		}

		Point2D p1 = null;
		Point2D p2 = null;

		for (PathIterator i = path.getPathIterator(null, 1); !i.isDone(); i
				.next()) {
			int ret = i.currentSegment(seg);

			if (ret == PathIterator.SEG_MOVETO) {
				p2 = new Point2D.Float(seg[0], seg[1]);
			} else if (ret == PathIterator.SEG_LINETO) {
				p1 = p2;
				p2 = new Point2D.Float(seg[0], seg[1]);

				if (p2.distance(last) < p2.distance(first)) {
					Line2D ortho = getOrthogonal(new Line2D.Float(first, last));
					Point2D pp1 = getIntersection(new Line2D.Float(p1, p2),
							ortho);
					Point2D pp2 = new Point2D.Float((float) (pp1.getX()
							+ p1.getX() - p2.getX()), (float) (pp1.getY()
							+ p1.getY() - p2.getY()));

					return getArrowTransform(rc, new Line2D.Float(pp1, pp2),
							null);
				}
			}
		}

		return new AffineTransform();
	}

	@Override
	public AffineTransform getArrowTransform(RenderContext<V, E> rc,
			Line2D edgeShape, Shape vertexShape) {
		float dx = (float) (edgeShape.getX1() - edgeShape.getX2());
		float dy = (float) (edgeShape.getY1() - edgeShape.getY2());
		AffineTransform at = AffineTransform.getTranslateInstance(
				edgeShape.getX1(), edgeShape.getY1());

		at.rotate(-Math.atan2(dx, dy) + Math.PI / 2);

		return at;
	}

	@Override
	public AffineTransform getReverseArrowTransform(RenderContext<V, E> rc,
			Shape edgeShape, Shape vertexShape) {
		throw new UnsupportedOperationException();
	}

	@Override
	public AffineTransform getReverseArrowTransform(RenderContext<V, E> rc,
			Shape edgeShape, Shape vertexShape, boolean passedGo) {
		throw new UnsupportedOperationException();
	}

	private Point2D getIntersection(Line2D l1, Line2D l2) {
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

	private Line2D getOrthogonal(Line2D l) {
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
}
