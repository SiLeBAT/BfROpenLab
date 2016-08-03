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

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.function.Function;

public enum NamedShape {
	CIRCLE("Circle", s -> new Ellipse2D.Double(-s / 2, -s / 2, s, s)),

	SQUARE("Square", s -> new Rectangle2D.Double(-s / 2, -s / 2, s, s)),

	STAR("Star", s -> {
		double r = s / 2;
		return new Polygon(
				new int[] { 0, (int) (0.3 * r), (int) (0.782 * r), (int) (0.675 * r), (int) (0.975 * r),
						(int) (0.541 * r), (int) (0.434 * r), 0, (int) (-0.434 * r), (int) (-0.541 * r),
						(int) (-0.975 * r), (int) (-0.675 * r), (int) (-0.782 * r), (int) (-0.3 * r) },
				new int[] { (int) -r, (int) (-0.623 * r), (int) (-0.623 * r), (int) (-0.154 * r), (int) (0.223 * r),
						(int) (0.431 * r), (int) (0.901 * r), (int) (0.692 * r), (int) (0.901 * r), (int) (0.431 * r),
						(int) (0.223 * r), (int) (-0.154 * r), (int) (-0.623 * r), (int) (-0.623 * r) },
				14);
	}),

	DIAMOND("Diamond", s -> {
		int r = (int) (s / 2);
		return new Polygon(new int[] { 0, r, 0, -r }, new int[] { -r, 0, r, 0 }, 4);
	});

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
}
