/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

public class XlsStruct {
	
	public static String getBACK_SHEETNAME(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Rückverfolgung";
		else if (lang.equals("en")) return "Backtracing";
		else return null;
	}
	public static String getFWD_SHEETNAME(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Vorwärtsverfolgung";
		else if (lang.equals("en")) return "Fwdtracing";
		else return null;
	}
	public static String getPROD_BACK_SHEETNAME(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Herstellung - Rückverfolgung";
		else if (lang.equals("en")) return "Production - Backtrace";
		else return null;
	}
	public static String getPROD_FWD_SHEETNAME(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Herstellung - Vorwärtsverfolgun";
		else if (lang.equals("en")) return "Production - Fwdtrace";
		else return null;
	}
	public static String getAiO_SHEETNAME(String lang) { // all in one
		if (lang == null) return null;
		if (lang.equals("de")) return "Daten";
		else if (lang.equals("en")) return "Data";
		else return null;
	}
	public static String getBACK_NEW_DATA_START(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Lieferant";
		else if (lang.equals("en")) return "Supplier";
		else return null;
	}
	public static String getFWD_NEW_DATA_START(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Empfänger";
		else if (lang.equals("en")) return "Recipient";
		else return null;
	}
	public static String getPROD_NEW_DATA_START(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Zeilennummer";
		else if (lang.equals("en")) return "Line Number";
		else return null;
	}
	public static String getOUT_SOURCE_KEY(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Quelle";
		else if (lang.equals("en")) return "Source";
		else return null;
	}
	public static String getOUT_SOURCE_VAL(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Zeile";
		else if (lang.equals("en")) return "Row";
		else return null;
	}
}
