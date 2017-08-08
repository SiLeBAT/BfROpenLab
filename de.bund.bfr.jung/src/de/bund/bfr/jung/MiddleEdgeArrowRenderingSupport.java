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
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.renderers.EdgeArrowRenderingSupport;

public class MiddleEdgeArrowRenderingSupport<V, E> implements EdgeArrowRenderingSupport<V, E> {

	@Override
	public AffineTransform getArrowTransform(RenderContext<V, E> rc, Shape edgeShape, Shape vertexShape) {
		Line2D lineInMiddle = JungUtils.getLineInMiddle(edgeShape);

		return lineInMiddle != null ? getArrowTransform(rc, lineInMiddle, null) : new AffineTransform();
	}

	@Override
	public AffineTransform getArrowTransform(RenderContext<V, E> rc, Line2D edgeShape, Shape vertexShape) {
		float dx = (float) (edgeShape.getX2() - edgeShape.getX1());
		float dy = (float) (edgeShape.getY2() - edgeShape.getY1());
		AffineTransform at = AffineTransform.getTranslateInstance(edgeShape.getX1(), edgeShape.getY1());

		at.rotate(-Math.atan2(dx, dy) + Math.PI / 2);

		return at;
	}

	@Override
	public AffineTransform getReverseArrowTransform(RenderContext<V, E> rc, Shape edgeShape, Shape vertexShape) {
		throw new UnsupportedOperationException();
	}

	@Override
	public AffineTransform getReverseArrowTransform(RenderContext<V, E> rc, Shape edgeShape, Shape vertexShape,
			boolean passedGo) {
		throw new UnsupportedOperationException();
	}
}
