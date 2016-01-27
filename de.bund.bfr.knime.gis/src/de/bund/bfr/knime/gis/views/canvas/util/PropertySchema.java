/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.gis.views.canvas.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class PropertySchema {

	public static enum Type {
		NODE, EDGE
	}

	private Map<String, Class<?>> map;
	private Map<String, Set<String>> possibleValues;

	public PropertySchema(Map<String, Class<?>> map) {
		this.map = map;
		possibleValues = new LinkedHashMap<>();
	}

	public Map<String, Class<?>> getMap() {
		return map;
	}

	public Map<String, Set<String>> getPossibleValues() {
		return possibleValues;
	}

	public abstract Type getType();
}
