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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.function.Function;

public enum NamedShape {
	CIRCLE("Circle", s -> new Ellipse2D.Double(-s / 2, -s / 2, s, s)),

	SQUARE("Square", s -> new Rectangle2D.Double(-s / 2, -s / 2, s, s)),

	TRIANGLE("Triangle", r -> createPolygon(new double[] { 0, 0.433 * r, -0.433 * r },
			new double[] { -0.5 * r, 0.25 * r, 0.25 * r }, 3)),

	STAR("Star",
			r -> createPolygon(
					new double[] { 0, 0.150 * r, 0.391 * r, 0.337 * r, 0.487 * r, 0.271 * r, 0.217 * r, 0, -0.217 * r,
							-0.271 * r, -0.487 * r, -0.337 * r, -0.391 * r, -0.150 * r },
					new double[] { -0.5 * r, -0.312 * r, -0.312 * r, -0.077 * r, 0.111 * r, 0.216 * r, 0.450 * r,
							0.346 * r, 0.450 * r, 0.216 * r, 0.111 * r, -0.077 * r, -0.312 * r, -0.312 * r },
					14)),

	DIAMOND("Diamond",
			s -> createPolygon(new double[] { 0, s / 2, 0, -s / 2 }, new double[] { -s / 2, 0, s / 2, 0 }, 4));

	private String name;
	private Function<Double, Shape> shapeFunction;

	private NamedShape(String name, Function<Double, Shape> shapeFunction) {
		this.name = name;
		this.shapeFunction = shapeFunction;
	}

	public String getName() {
		return name;
	}

	public Shape getShape(double size) {
		return shapeFunction.apply(size);
	}

	@Override
	public String toString() {
		return name;
	}

	private static Shape createPolygon(double[] x, double[] y, int n) {
		Path2D path = new Path2D.Double();

		path.moveTo(x[0], y[0]);

		for (int i = 1; i < n; i++) {
			path.lineTo(x[i], y[i]);
		}

		path.closePath();

		return path;
	}
}
