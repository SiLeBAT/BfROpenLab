/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.gis;

import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Functions;
import com.google.common.collect.Maps;

import de.bund.bfr.knime.gis.geocode.GeocodingSettings;

public class BackwardUtils {

	private BackwardUtils() {
	}

	public static final String OLD_LATITUDE_COLUMN = "GeocodingLatitude";
	public static final String OLD_LONGITUDE_COLUMN = "GeocodingLongitude";

	private static final String PROVIDER_MAPQUEST = "MapQuest";
	private static final String PROVIDER_GISGRAPHY = "Gisgraphy";
	private static final String PROVIDER_BKG = "Bundesamt für Kartographie und Geodäsie";

	private static final String MULTIPLE_DO_NOT_USE = "Do not use";
	private static final String MULTIPLE_USE_FIRST = "Use first";
	private static final String MULTIPLE_ASK_USER = "Ask User";

	public static Map<String, Set<String>> toNewCollapseFormat(Map<String, Map<String, Point2D>> map) {
		Map<String, Set<String>> result = new LinkedHashMap<>();

		map.forEach((key, value) -> result.put(key, new LinkedHashSet<>(value.keySet())));

		return result;
	}

	public static Map<String, Map<String, Point2D>> toOldCollapseFormat(Map<String, Set<String>> map) {
		Map<String, Map<String, Point2D>> result = new LinkedHashMap<>();

		map.forEach((key, value) -> result.put(key,
				new LinkedHashMap<>(Maps.asMap(value, Functions.constant((Point2D) null)))));

		return result;
	}

	public static GeocodingSettings.Provider toNewProviderFormat(String provider) {
		if (provider.equals(PROVIDER_MAPQUEST)) {
			return GeocodingSettings.Provider.MAPQUEST;
		} else if (provider.equals(PROVIDER_GISGRAPHY)) {
			return GeocodingSettings.Provider.GISGRAPHY;
		} else if (provider.equals(PROVIDER_BKG)) {
			return GeocodingSettings.Provider.BKG;
		}

		throw new RuntimeException("Should not happen");
	}

	public static GeocodingSettings.Multiple toNewMultipleFormat(String multiple) {
		if (multiple.equals(MULTIPLE_DO_NOT_USE)) {
			return GeocodingSettings.Multiple.DO_NOT_USE;
		} else if (multiple.equals(MULTIPLE_USE_FIRST)) {
			return GeocodingSettings.Multiple.USE_FIRST;
		} else if (multiple.equals(MULTIPLE_ASK_USER)) {
			return GeocodingSettings.Multiple.ASK_USER;
		}

		throw new RuntimeException("Should not happen");
	}

	public static String toNewHighlightingFormat(String settings) {
		if (settings == null) {
			return null;
		}

		String oldValuePrefix = "<void property=\"type\"><string>";
		String oldValuePostfix = "</string></void>";
		String newValuePrefix = "<void property=\"type\"><object class=\"java.lang.Enum\" method=\"valueOf\"><class>de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition$Type</class><string>";
		String newValuePostfix = "</string></object></void>";

		String oldValueType = oldValuePrefix + "Value" + oldValuePostfix;
		String newValueType = newValuePrefix + "VALUE" + newValuePostfix;
		String oldLogValueType = oldValuePrefix + "Log Value" + oldValuePostfix;
		String newLogValueType = newValuePrefix + "LOG_VALUE" + newValuePostfix;

		String oldLogicPrefix = "<void property=\"type\"><string>";
		String oldLogicPostfix = "</string></void>";
		String newLogicPrefix = "<void property=\"type\"><object class=\"java.lang.Enum\" method=\"valueOf\"><class>de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalHighlightCondition$Type</class><string>";
		String newLogicPostfix = "</string></object></void>";

		String oldEqualType = oldLogicPrefix + "==" + oldLogicPostfix;
		String newEqualType = newLogicPrefix + "EQUAL" + newLogicPostfix;
		String oldNotEqualType = oldLogicPrefix + "!=" + oldLogicPostfix;
		String newNotEqualType = newLogicPrefix + "NOT_EQUAL" + newLogicPostfix;
		String oldGreaterType = oldLogicPrefix + "&gt;" + oldLogicPostfix;
		String newGreaterType = newLogicPrefix + "GREATER" + newLogicPostfix;
		String oldLessType = oldLogicPrefix + "&lt;" + oldLogicPostfix;
		String newLessType = newLogicPrefix + "LESS" + newLogicPostfix;
		String oldRgxEqualType = oldLogicPrefix + "== (Regex)" + oldLogicPostfix;
		String newRgxEqualType = newLogicPrefix + "REGEX_EQUAL" + newLogicPostfix;
		String oldRgxNotEqualType = oldLogicPrefix + "!= (Regex)" + oldLogicPostfix;
		String newRgxNotEqualType = newLogicPrefix + "REGEX_NOT_EQUAL" + newLogicPostfix;
		String oldRgxEqualCaseType = oldLogicPrefix + "== (Regex Ignore Case)" + oldLogicPostfix;
		String newRgxEqualCaseType = newLogicPrefix + "REGEX_EQUAL_IGNORE_CASE" + newLogicPostfix;
		String oldRgxNotEqualCaseType = oldLogicPrefix + "!= (Regex Ignore Case)" + oldLogicPostfix;
		String newRgxNotEqualCaseType = newLogicPrefix + "REGEX_NOT_EQUAL_IGNORE_CASE" + newLogicPostfix;

		return trimLineAndRemoveLineBreaks(settings).replace(oldValueType, newValueType)
				.replace(oldLogValueType, newLogValueType).replace(oldEqualType, newEqualType)
				.replace(oldNotEqualType, newNotEqualType).replace(oldGreaterType, newGreaterType)
				.replace(oldLessType, newLessType).replace(oldRgxEqualType, newRgxEqualType)
				.replace(oldRgxNotEqualType, newRgxNotEqualType).replace(oldRgxEqualCaseType, newRgxEqualCaseType)
				.replace(oldRgxNotEqualCaseType, newRgxNotEqualCaseType);
	}

	private static String trimLineAndRemoveLineBreaks(String s) {
		return Stream.of(s.split("\n")).map(l -> l.trim()).collect(Collectors.joining());
	}
}
