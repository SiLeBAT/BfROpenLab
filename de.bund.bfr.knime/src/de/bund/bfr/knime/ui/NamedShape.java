/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.ui;

import static de.bund.bfr.knime.ui.ColorAndShapeUtils.SHAPE_DELTA;
import static de.bund.bfr.knime.ui.ColorAndShapeUtils.SHAPE_SIZE;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public enum NamedShape {
	SQUARE("Square", new Rectangle2D.Double(-SHAPE_DELTA, -SHAPE_DELTA, SHAPE_SIZE, SHAPE_SIZE)),

	CIRCLE("Circle", new Ellipse2D.Double(-SHAPE_DELTA, -SHAPE_DELTA, SHAPE_SIZE, SHAPE_SIZE)),

	TRIANGLE_1("Triangle1", new Polygon(new int[] { 0, SHAPE_DELTA, -SHAPE_DELTA },
			new int[] { -SHAPE_DELTA, SHAPE_DELTA, SHAPE_DELTA }, 3)),

	DIAMOND("Diamond", new Polygon(new int[] { 0, SHAPE_DELTA, 0, -SHAPE_DELTA },
			new int[] { -SHAPE_DELTA, 0, SHAPE_DELTA, 0 }, 4)),

	RECTANGLE_1("Rectangle1", new Rectangle2D.Double(-SHAPE_DELTA, -SHAPE_DELTA / 2, SHAPE_SIZE, SHAPE_SIZE / 2)),

	TRIANGLE_2("Triangle2", new Polygon(new int[] { -SHAPE_DELTA, SHAPE_DELTA, 0 },
			new int[] { -SHAPE_DELTA, -SHAPE_DELTA, SHAPE_DELTA }, 3)),

	ELLIPSE("Ellipse", new Ellipse2D.Double(-SHAPE_DELTA, -SHAPE_DELTA / 2, SHAPE_SIZE, SHAPE_SIZE / 2)),

	TRIANGLE_3("Triangle3", new Polygon(new int[] { -SHAPE_DELTA, SHAPE_DELTA, -SHAPE_DELTA },
			new int[] { -SHAPE_DELTA, 0, SHAPE_DELTA }, 3)),

	RECTANGLE_2("Rectangle2", new Rectangle2D.Double(-SHAPE_DELTA / 2, -SHAPE_DELTA, SHAPE_SIZE / 2, SHAPE_SIZE)),

	TRIANGLE_4("Triangle4", new Polygon(new int[] { -SHAPE_DELTA, SHAPE_DELTA, SHAPE_DELTA },
			new int[] { 0, -SHAPE_DELTA, SHAPE_DELTA }, 3));

	private String name;
	private Shape shape;

	private NamedShape(String name, Shape shape) {
		this.name = name;
		this.shape = shape;
	}

	public String getName() {
		return name;
	}

	public Shape getShape() {
		return shape;
	}

	@Override
	public String toString() {
		return name;
	}
}
