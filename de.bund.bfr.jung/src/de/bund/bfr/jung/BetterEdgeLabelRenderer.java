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

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

import com.google.common.base.Strings;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

public class BetterEdgeLabelRenderer<V, E> implements Renderer.EdgeLabel<V, E> {

	@Override
	public void labelEdge(RenderContext<V, E> rc, Layout<V, E> layout, E e, String label) {
		if (Strings.isNullOrEmpty(label)) {
			return;
		}

		Shape edgeShape = JungUtils.getTransformedEdgeShape(rc, layout, e);

		if (edgeShape == null) {
			return;
		}

		Line2D line = JungUtils.getLineInMiddle(edgeShape);

		GraphicsDecorator g = rc.getGraphicsContext();
		Font font = rc.getEdgeFontTransformer().transform(e);
		double width = font.getStringBounds(label, g.getFontRenderContext()).getWidth();
		AffineTransform old = g.getTransform();
		AffineTransform trans = new AffineTransform(old);
		double angle = Math.atan2(line.getY2() - line.getY1(), line.getX2() - line.getX1());

		if (angle < -Math.PI / 2) {
			angle += Math.PI;
		} else if (angle > Math.PI / 2) {
			angle -= Math.PI;
		}

		trans.translate(line.getX1(), line.getY1());
		trans.rotate(angle);
		g.setTransform(trans);
		g.setColor(rc.getPickedEdgeState().isPicked(e) ? Color.GREEN : Color.BLACK);
		g.setFont(rc.getEdgeFontTransformer().transform(e));
		g.drawString(label, (int) (-width / 2), 0);
		g.setTransform(old);
	}
}
