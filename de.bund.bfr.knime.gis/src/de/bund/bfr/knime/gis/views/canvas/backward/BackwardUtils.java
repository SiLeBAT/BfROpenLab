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
package de.bund.bfr.knime.gis.views.canvas.backward;

import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Functions;
import com.google.common.collect.Maps;

public class BackwardUtils {

	private BackwardUtils() {
	}

	public static Map<String, Set<String>> toNewCollapseFormat(Map<String, Map<String, Point2D>> map) {
		return map.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey(), e -> new LinkedHashSet<>(e.getValue().keySet())));
	}

	public static Map<String, Map<String, Point2D>> toOldCollapseFormat(Map<String, Set<String>> map) {
		return map.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(),
				e -> new LinkedHashMap<>(Maps.asMap(e.getValue(), Functions.constant((Point2D) null)))));
	}

	public static String toNewHighlightingFormat(String settings) {
		String oldValuePrefix = "<void property=\"type\"><string>";
		String oldValuePostfix = "</string></void>";
		String newValuePrefix = "<void property=\"type\"><object class=\"java.lang.Enum\" method=\"valueOf\"><class>de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition$Type</class><string>";
		String newValuePostfix = "</string></object></void>";

		String oldValueType = oldValuePrefix + "Value" + oldValuePostfix;
		String newValueType = newValuePrefix + "VALUE" + newValuePostfix;
		String oldLogValueType = oldValuePrefix + "Log Value" + oldValuePostfix;
		String newLogValueType = newValuePrefix + "LOG_VALUE" + newValuePostfix;

		return trimLineAndRemoveLineBreaks(settings).replace(oldValueType, newValueType).replace(oldLogValueType,
				newLogValueType);
	}

	private static String trimLineAndRemoveLineBreaks(String s) {
		return Stream.of(s.split("\n")).map(l -> l.trim()).collect(Collectors.joining());
	}
}
