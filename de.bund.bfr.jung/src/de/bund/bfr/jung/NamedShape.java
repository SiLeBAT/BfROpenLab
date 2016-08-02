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

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.function.Function;

public enum NamedShape {
	CIRCLE("Circle", s -> new Ellipse2D.Double(-s / 2, -s / 2, s, s)),

	SQUARE("Square", s -> new Rectangle2D.Double(-s / 2, -s / 2, s, s));

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
