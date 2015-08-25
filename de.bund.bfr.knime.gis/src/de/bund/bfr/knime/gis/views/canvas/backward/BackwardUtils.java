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
package de.bund.bfr.knime.gis.views.canvas.backward;

import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Functions;
import com.google.common.collect.Maps;

public class BackwardUtils {

	private BackwardUtils() {
	}

	public static Map<String, Set<String>> toNewCollapseFormat(Map<String, Map<String, Point2D>> map) {
		Map<String, Set<String>> newMap = new LinkedHashMap<>();

		for (Map.Entry<String, Map<String, Point2D>> entry : map.entrySet()) {
			newMap.put(entry.getKey(), new LinkedHashSet<>(entry.getValue().keySet()));
		}

		return newMap;
	}

	public static Map<String, Map<String, Point2D>> toOldCollapseFormat(Map<String, Set<String>> map) {
		Map<String, Map<String, Point2D>> oldMap = new LinkedHashMap<>();

		for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
			oldMap.put(entry.getKey(),
					new LinkedHashMap<>(Maps.asMap(entry.getValue(), Functions.constant((Point2D) null))));
		}

		return oldMap;
	}
}
