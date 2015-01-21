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
package de.bund.bfr.knime.nls.chart;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ColorAndShapeCreator {

	public static final int SHAPE_SIZE = 6;
	public static final int SHAPE_DELTA = 3;

	public static final Color[] COLORS = new Color[] { new Color(255, 85, 85),
			new Color(85, 85, 255), new Color(85, 255, 85),
			new Color(255, 85, 255), new Color(85, 255, 255),
			new Color(255, 175, 175), new Color(128, 128, 128),
			new Color(192, 0, 0), new Color(0, 0, 192), new Color(0, 192, 0),
			new Color(192, 192, 0), new Color(192, 0, 192),
			new Color(0, 192, 192), new Color(64, 64, 64),
			new Color(255, 64, 64), new Color(64, 64, 255),
			new Color(64, 255, 64), new Color(255, 64, 255),
			new Color(64, 255, 255), new Color(192, 192, 192),
			new Color(128, 0, 0), new Color(0, 0, 128), new Color(0, 128, 0),
			new Color(128, 128, 0), new Color(128, 0, 128),
			new Color(0, 128, 128), new Color(255, 128, 128),
			new Color(128, 128, 255), new Color(128, 255, 128),
			new Color(255, 128, 255), new Color(128, 255, 255) };

	public static final Shape[] SHAPES = new Shape[] {
			new Rectangle2D.Double(-SHAPE_DELTA, -SHAPE_DELTA, SHAPE_SIZE,
					SHAPE_SIZE),
			new Ellipse2D.Double(-SHAPE_DELTA, -SHAPE_DELTA, SHAPE_SIZE,
					SHAPE_SIZE),
			new Polygon(new int[] { 0, SHAPE_DELTA, -SHAPE_DELTA }, new int[] {
					-SHAPE_DELTA, SHAPE_DELTA, SHAPE_DELTA }, 3),
			new Polygon(new int[] { 0, SHAPE_DELTA, 0, -SHAPE_DELTA },
					new int[] { -SHAPE_DELTA, 0, SHAPE_DELTA, 0 }, 4),
			new Rectangle2D.Double(-SHAPE_DELTA, -SHAPE_DELTA / 2, SHAPE_SIZE,
					SHAPE_SIZE / 2),
			new Polygon(new int[] { -SHAPE_DELTA, SHAPE_DELTA, 0 }, new int[] {
					-SHAPE_DELTA, -SHAPE_DELTA, SHAPE_DELTA }, 3),
			new Ellipse2D.Double(-SHAPE_DELTA, -SHAPE_DELTA / 2, SHAPE_SIZE,
					SHAPE_SIZE / 2),
			new Polygon(new int[] { -SHAPE_DELTA, SHAPE_DELTA, -SHAPE_DELTA },
					new int[] { -SHAPE_DELTA, 0, SHAPE_DELTA }, 3),
			new Rectangle2D.Double(-SHAPE_DELTA / 2, -SHAPE_DELTA,
					SHAPE_SIZE / 2, SHAPE_SIZE),
			new Polygon(new int[] { -SHAPE_DELTA, SHAPE_DELTA, SHAPE_DELTA },
					new int[] { 0, -SHAPE_DELTA, SHAPE_DELTA }, 3) };

	public static final String[] SHAPE_NAMES = { "Square", "Circle",
			"Triangle1", "Diamond", "Rectangle1", "Triangle2", "Ellipse",
			"Triangle3", "Rectangle2", "Triangle4" };

	private List<Color> colorList;
	private List<Shape> shapeList;
	private List<String> shapeNameList;
	private Map<String, Shape> shapeByNameMap;
	private Map<Shape, String> nameByShapeMap;

	public ColorAndShapeCreator(int n) {
		colorList = new ArrayList<>();
		shapeList = new ArrayList<>();
		shapeNameList = new ArrayList<>();
		shapeByNameMap = new LinkedHashMap<>();
		nameByShapeMap = new LinkedHashMap<>();

		for (int i = 0; i < n; i++) {
			colorList.add(COLORS[i % COLORS.length]);
		}

		for (int i = 0; i < n; i++) {
			shapeList.add(SHAPES[i % SHAPES.length]);
			shapeNameList.add(SHAPE_NAMES[i % SHAPE_NAMES.length]);
		}

		for (int i = 0; i < SHAPES.length; i++) {
			shapeByNameMap.put(SHAPE_NAMES[i], SHAPES[i]);
			nameByShapeMap.put(SHAPES[i], SHAPE_NAMES[i]);
		}
	}

	public List<Color> getColorList() {
		return colorList;
	}

	public List<Shape> getShapeList() {
		return shapeList;
	}

	public List<String> getShapeNameList() {
		return shapeNameList;
	}

	public Map<String, Shape> getShapeByNameMap() {
		return shapeByNameMap;
	}

	public Map<Shape, String> getNameByShapeMap() {
		return nameByShapeMap;
	}
}
