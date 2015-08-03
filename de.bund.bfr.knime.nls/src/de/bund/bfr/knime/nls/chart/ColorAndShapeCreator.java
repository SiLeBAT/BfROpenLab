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
import java.util.ArrayList;
import java.util.List;

public class ColorAndShapeCreator {

	public static final int SHAPE_SIZE = 6;
	public static final int SHAPE_DELTA = 3;

	public static final Color[] COLORS = new Color[] { new Color(255, 85, 85), new Color(85, 85, 255),
			new Color(85, 255, 85), new Color(255, 85, 255), new Color(85, 255, 255), new Color(255, 175, 175),
			new Color(128, 128, 128), new Color(192, 0, 0), new Color(0, 0, 192), new Color(0, 192, 0),
			new Color(192, 192, 0), new Color(192, 0, 192), new Color(0, 192, 192), new Color(64, 64, 64),
			new Color(255, 64, 64), new Color(64, 64, 255), new Color(64, 255, 64), new Color(255, 64, 255),
			new Color(64, 255, 255), new Color(192, 192, 192), new Color(128, 0, 0), new Color(0, 0, 128),
			new Color(0, 128, 0), new Color(128, 128, 0), new Color(128, 0, 128), new Color(0, 128, 128),
			new Color(255, 128, 128), new Color(128, 128, 255), new Color(128, 255, 128), new Color(255, 128, 255),
			new Color(128, 255, 255) };

	private List<Color> colorList;
	private List<NamedShape> shapeList;

	public ColorAndShapeCreator(int n) {
		colorList = new ArrayList<>();
		shapeList = new ArrayList<>();

		for (int i = 0; i < n; i++) {
			colorList.add(COLORS[i % COLORS.length]);
		}

		NamedShape[] shapes = NamedShape.values();

		for (int i = 0; i < n; i++) {
			shapeList.add(shapes[i % shapes.length]);
		}
	}

	public List<Color> getColorList() {
		return colorList;
	}

	public List<NamedShape> getShapeList() {
		return shapeList;
	}
}
