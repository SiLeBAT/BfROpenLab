/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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

import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public enum LabelPosition {
	CENTER("Center", Position.CNTR), BOTTOM_RIGHT("Bottom Right", Position.SE);

	private String name;
	private Position position;

	private LabelPosition(String name, Position position) {
		this.name = name;
		this.position = position;
	}

	public Position getPosition() {
		return position;
	}

	@Override
	public String toString() {
		return name;
	}
}
