/*******************************************************************************
 * Copyright (c) 2014-2025 German Federal Institute for Risk Assessment (BfR)
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
 *******************************************************************************/package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat.SheetUtils.InvalidCellValueException;

/***
 * This class provides functions for converting exceptions and warnings to html
 */
public class ReportUtils {
	
	static class WarningSource {
		final String sheet;
		final String cellAddress;

		WarningSource(Cell cell) {
			this.sheet = cell.getSheet().getSheetName();
			this.cellAddress = cell.getAddress().toString();
		}
		
		WarningSource(Sheet sheet) {
			this.sheet = sheet.getSheetName();
			this.cellAddress = null;
		}
	}
	
	static String exceptions2Html_v1(List<Exception> exceptions) {
		StringBuilder builder = new StringBuilder();
		LinkedHashMap<String, LinkedHashMap<String, List<String>>> map = new LinkedHashMap<>();
		for (Exception e : exceptions) {
			String msg = e.getMessage();
			if (msg == null) continue;
			if (e instanceof InvalidCellValueException) {
				InvalidCellValueException invCellValueEx = (InvalidCellValueException) e;
				LinkedHashMap<String, List<String>> sheetMap = map.get(msg);
				if (sheetMap == null) {
					sheetMap = new LinkedHashMap<>();
					map.put(msg, sheetMap);
				}
				List<String> sheetAddresses = sheetMap.get(invCellValueEx.sheet);
				if (sheetAddresses == null) {
					sheetAddresses = new ArrayList<>();
					sheetMap.put(invCellValueEx.sheet, sheetAddresses);
				}
				sheetAddresses.add(invCellValueEx.address);
			}
			else {
				map.put(msg, null);
				MyLogger.handleException(e);	
			}
		}
		
		for (String msg : map.keySet()) {
			List<String> lines = new ArrayList<>();
			lines.add(msg);
			LinkedHashMap<String, List<String>> sheetMap = map.get(msg);
			if (sheetMap != null) {
				for (String sheet : sheetMap.keySet()) {
					List<String> addresses = sheetMap.get(sheet);
					if (addresses.size() == 1) lines.add("Sheet: " + sheet + ", cell: " + addresses.get(0));
					else if (addresses.size() <= 11) lines.add("Sheet: " + sheet + ", cells: " + String.join(", ", addresses));
					else lines.add("Sheet: " + sheet + ", cells: " + String.join(", ", addresses.subList(0, 10)) + " ... (" + (addresses.size() - 10) + " others).");
				}
			}
			builder.append("<li>" + String.join("<br>", lines.stream().map(line -> StringEscapeUtils.escapeHtml4(line)).toList()) +  "</li>");
		}
		return builder.toString();
	}
	
	private static String getFormatedAddressListString(List<String> addresses, String addressNameSingular, String addressNamePlural) {
		final int LIMIT = 11;
		if (addresses == null || addresses.size() == 0) return "";
		
		if (addresses.size() == 1) return addressNameSingular + " " + addresses.get(0);
		else if (addresses.size() <= LIMIT) return addressNamePlural + " " + String.join(", ", addresses);
		
		return addressNamePlural + " " + String.join(", ", addresses.subList(0, 10)) + " ... (" + (addresses.size() - 10) + " others)";
	}
	
	private static String getFormatedAddressListString(List<String> cellAddresses, List<Integer> rowAddresses) {
		String cellAddressString = getFormatedAddressListString(cellAddresses, "cell", "cells");
		String rowAddressString = getFormatedAddressListString(rowAddresses.stream().map(Number::toString).toList(), "row", "rows");
		
		String result = "";
		
		if (cellAddressString != null) result = cellAddressString;
		if (!result.isEmpty() && 
			rowAddressString != null && !rowAddressString.isEmpty()
		) result += " and ";
		if (rowAddressString != null) result += rowAddressString;
		
		return result; 
	}

	private static String msgExceptions2Addresses(List<InvalidCellValueException> msgExceptions) {
		List<String> cells = new ArrayList<>();
		List<Integer> rows =  new ArrayList<>();
		for (InvalidCellValueException ex : msgExceptions) {
			if (ex == null) continue;
			if (ex.address != null) cells.add(ex.address);
			else if (ex.rowNum != null) rows.add(ex.rowNum); 
		}
		if (cells.size() == 0 && rows.size() == 0) return "";
		
		return getFormatedAddressListString(cells, rows);
	}
	
	private static String sheetExceptions2Html(LinkedHashMap<String, List<InvalidCellValueException>> sheetExceptions) {
		if (sheetExceptions == null) return "";
		
		List<String> lines = new ArrayList<>();
		for (Map.Entry<String, List<InvalidCellValueException>> entry : sheetExceptions.entrySet()) {
			String msg = entry.getKey();
			String addresses = msgExceptions2Addresses(sheetExceptions.get(msg));
			if (addresses != null && !addresses.isEmpty()) {
				if (msg.endsWith(".") || msg.endsWith("!")) msg += " [" + addresses + "]";
				else msg += " in " + addresses + ".";
			}
			lines.add(msg);
		}
		if (lines.size() == 0) return "";
		lines = lines.stream().map(line -> StringEscapeUtils.escapeHtml4(line)).toList();
		return "<ul><li>" + String.join("</li><li>", lines) +  "</li></ul>";
	}
	
	
	static String exceptions2Html_new(List<Exception> exceptions) {
		LinkedHashMap<String, LinkedHashMap<String, List<InvalidCellValueException>>> sheet2Exceptions = new LinkedHashMap<>();
		
		for (Exception e : exceptions) {
			String msg = e.getMessage();
			if (msg == null) continue;
			
			String sheet = e instanceof InvalidCellValueException ? ((InvalidCellValueException) e).sheet : null;
			LinkedHashMap<String, List<InvalidCellValueException>> sheetExceptions = sheet2Exceptions.get(sheet);
			
			if (sheetExceptions == null) {
				sheetExceptions = new LinkedHashMap<>();
				sheet2Exceptions.put(sheet, sheetExceptions);
			}
			List<InvalidCellValueException> msgExceptions = sheetExceptions.get(e.getMessage());
			if (msgExceptions == null) {
				msgExceptions = new ArrayList<>();
				sheetExceptions.put(msg, msgExceptions);
			}
			msgExceptions.add(e instanceof InvalidCellValueException ? (InvalidCellValueException) e : null);
		}
		
		List<String> lines = new ArrayList<>();
		for (Map.Entry<String, LinkedHashMap<String, List<InvalidCellValueException>>> entry : sheet2Exceptions.entrySet()) {
			String sheet = entry.getKey();
			if (sheet == null) continue;
			String html = sheetExceptions2Html(sheet2Exceptions.get(sheet));
			if (html != null && !html.isEmpty()) lines.add("<h2>Sheet '" + StringEscapeUtils.escapeHtml4(sheet) +"':</h2>" + html);
		}
		
		String html = sheetExceptions2Html(sheet2Exceptions.get(null));
		if (html != null && !html.isEmpty()) lines.add((sheet2Exceptions.size() == 1 ? "" : "<h2>Other issues:</h2>") + html);
		
		if (lines.size() == 0) return "";
		return String.join("", lines);
	}
	
	static String exceptions2Html(List<Exception> exceptions, String filePath) {
		if (exceptions == null || exceptions.size() == 0) return "";
		
		String html = (exceptions.size() == 0 || exceptions.stream().anyMatch(ex -> ex instanceof SheetUtils.InvalidCellValueException)) ?
			exceptions2Html_new(exceptions) : exceptions2Html_v1(exceptions);
		
		if (html == null || html.isEmpty()) html = "<ul><li>some undefined problems occurred - contact the support team</li></ul>";
		
		return "<h1 id=\"error\">Error in file '" + StringEscapeUtils.escapeHtml4(filePath) + "'</h1>" + html;
	}
		
	private static LinkedHashSet<String> cloneToLinkedHashSet(Collection<String> value) {
		if (value == null) return null;
		LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();
		linkedHashSet.addAll(value);
		return linkedHashSet;
	}
	
	private static Map<String, Set<String>> getNewWarnings(Map<String, Set<String>> currentWarnings, Map<String, Set<String>> oldWarnings) {
		if (oldWarnings == null || oldWarnings.isEmpty()) return currentWarnings;
		if (currentWarnings == null || currentWarnings.isEmpty()) return currentWarnings;
		
		Map<String, Set<String>> newWarnings = new LinkedHashMap<>();
		
		for (String warning : currentWarnings.keySet()) {
			Set<String> currentSources = currentWarnings.get(warning);
			Set<String> oldSources = oldWarnings.get(warning);
			if (!oldWarnings.containsKey(warning)) {
				newWarnings.put(warning, cloneToLinkedHashSet(currentSources));
				continue;
			} else if (oldSources == null) {
				if (currentSources == null || currentSources.isEmpty()) continue;
				newWarnings.put(warning, cloneToLinkedHashSet(currentSources));
				continue;
			} else if (currentSources == null) {
				continue;
			}
			
			List<String> filteredSources = currentSources.stream().filter(s -> (s != null && !oldSources.contains(s))).toList();
			
			if (!filteredSources.isEmpty()) {
				newWarnings.put(warning, cloneToLinkedHashSet(filteredSources));
			}
		}	
		return newWarnings;
	}
	
	private static Map<String, Set<String>> aggregateFileRelatedWarnings(Map<String, List<WarningSource>> warnings) {
		Map<String, Set<String>> aggregatedWarnings = new LinkedHashMap<>();
		
		if (warnings == null || warnings.isEmpty()) return aggregatedWarnings;
		
		for (String warning : warnings.keySet()) {
			List<WarningSource> sources = warnings.get(warning);
			if (sources == null || sources.isEmpty()) continue;
			
			Map<String, Set<String>> sheet2CellAddresses= new LinkedHashMap<>();
			for (WarningSource source : sources) {
				Set<String> cellAddresses = sheet2CellAddresses.get(source.sheet);
				if (cellAddresses == null) {
					cellAddresses = new LinkedHashSet<>();
					sheet2CellAddresses.put(source.sheet, cellAddresses);
				}
				if (source.cellAddress != null) cellAddresses.add(source.cellAddress);
			}
			
			Set<String> aggregatedSources = new LinkedHashSet<>();
			for (String sheet : sheet2CellAddresses.keySet()) {
				String addressString = getFormatedAddressListString(sheet2CellAddresses.get(sheet).stream().collect(Collectors.toList()), "cell", "cells");
				String aggregatedSource = "In sheet '" + sheet + "'";
				if (addressString != null && !addressString.isEmpty()) aggregatedSource += " in " + addressString;
				aggregatedSource += ".";
				aggregatedSources.add(aggregatedSource);
			}
			if (!aggregatedSources.isEmpty()) aggregatedWarnings.put(warning, aggregatedSources);
		}	
		return aggregatedWarnings;
	}
	
	/***
	 * This methods is different to Map.putAll because it merges the values instead of simply replacing them
	 * @param mapToAddTo
	 * @param mapToAdd
	 */
	private static void putAll(Map<String, Set<String>> mapToAddTo, Map<String, Set<String>> mapToAdd) {
		if (mapToAddTo == null || mapToAdd == null) return;
		
		for (String key : mapToAdd.keySet()) {
			Set<String> valuesToAdd = mapToAdd.get(key);
			if (valuesToAdd == null) {
				if (!mapToAddTo.containsKey(key)) mapToAddTo.put(key, null);
				continue;
			}
			Set<String> valuesToAddTo = mapToAddTo.get(key);
			if (valuesToAddTo == null) {
				valuesToAddTo = new LinkedHashSet<>();
				mapToAddTo.put(key, valuesToAddTo);
			}
			for (String valueToAdd : valuesToAdd) valuesToAddTo.add(valueToAdd);
		}
	}
		
	private static Map<String, Set<String>> mergeWarnings(Map<String, Set<String>> warningsToAddTo, Map<String, Set<String>> warningsToAdd) {
		if (warningsToAdd == null || warningsToAdd.isEmpty()) return warningsToAddTo;
		
		Map<String, Set<String>> mergedWarnings = new LinkedHashMap<>();
		if (warningsToAddTo != null) putAll(mergedWarnings, warningsToAddTo);
		putAll(mergedWarnings, warningsToAdd);
		return mergedWarnings;
	}
	
	static String warns2Html(String filePath, Map<String, List<WarningSource>> fileRelatedWarnings, Map<String, Set<String>> dbStateWarnings, Map<String, Set<String>> oldDBStateWarnings) {
		if (dbStateWarnings == null) dbStateWarnings = new LinkedHashMap<>();
		if (fileRelatedWarnings == null) fileRelatedWarnings = new LinkedHashMap<>();
		if (dbStateWarnings.isEmpty() && fileRelatedWarnings.isEmpty()) return "";
			
		Map<String, Set<String>> newDBStateWarnings = getNewWarnings(dbStateWarnings, oldDBStateWarnings);
		Map<String, Set<String>> aggregatedFileRelatedWarnings = aggregateFileRelatedWarnings(fileRelatedWarnings);
		Map<String, Set<String>> mergedWarnings = mergeWarnings(aggregatedFileRelatedWarnings, newDBStateWarnings);
		
		if (mergedWarnings == null || mergedWarnings.isEmpty()) return "";

		StringBuilder htmlBuilder = new StringBuilder();
		for (String warning : mergedWarnings.keySet()) {
			if (warning == null || warning.isEmpty()) continue;
			
			String sectionTitle = "<h2>" + StringEscapeUtils.escapeHtml4(warning) + "</h2>";
			String sectionContent = "";
			Set<String> sources = mergedWarnings.get(warning);
			if (sources != null) {
				sectionContent = String.join("</li><li>", sources.stream().filter(s -> s != null).map(s -> StringEscapeUtils.escapeHtml4(s)).collect(Collectors.toList()));
				if (!sectionContent.isEmpty()) sectionContent = "<li>" + sectionContent + "</li>";
			}
			htmlBuilder.append(sectionTitle + sectionContent);
		}
		if (htmlBuilder.isEmpty()) return "";
		
		String htmlTitle = "<h1 id=\"warning\">Warnings for import file '" + StringEscapeUtils.escapeHtml4(filePath) + "'<h1>";
		return htmlTitle + htmlBuilder.toString();
	}
}
