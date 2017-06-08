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
