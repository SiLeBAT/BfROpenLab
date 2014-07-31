/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
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
package de.bund.bfr.knime.gis.views.canvas;

public enum LayoutType {

	GRID_LAYOUT, CIRCLE_LAYOUT, FR_LAYOUT, FR_LAYOUT_2, ISOM_LAYOUT, KK_LAYOUT, SPRING_LAYOUT, SPRING_LAYOUT_2;

	@Override
	public String toString() {
		switch (this) {
		case GRID_LAYOUT:
			return "Grid Layout";
		case CIRCLE_LAYOUT:
			return "Circle Layout";
		case FR_LAYOUT:
			return "FR Layout";
		case FR_LAYOUT_2:
			return "FR Layout 2";
		case ISOM_LAYOUT:
			return "ISOM Layout";
		case KK_LAYOUT:
			return "KK Layout";
		case SPRING_LAYOUT:
			return "Spring Layout";
		case SPRING_LAYOUT_2:
			return "Spring Layout 2";
		}

		return super.toString();
	}
}
