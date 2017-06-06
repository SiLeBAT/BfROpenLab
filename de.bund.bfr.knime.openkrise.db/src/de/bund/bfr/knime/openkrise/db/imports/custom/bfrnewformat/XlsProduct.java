package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.util.HashMap;

public class XlsProduct {
	private HashMap<Integer, String> extraVals = new HashMap<>();	
	public HashMap<Integer, String> getExtraVals() {
		return extraVals;
	}

	public static String BLOCK_PRODUCT(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Produkt";
		else if (lang.equals("en")) return "Product";
		else return null;
	}
	public static String BLOCK_INGREDIENT(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Zutat";
		else if (lang.equals("en")) return "Ingredient";
		else return null;
	}
	public static String ITEM(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Bezeichnung";
		else if (lang.equals("en")) return "Name";
		else return null;
	}
	public static String EAN(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "EAN";
		else if (lang.equals("en")) return "EAN";
		else return null;
	}
	
	public void addField(String fieldname, int index, String lang) {
		if (fieldname != null) {
			String s = fieldname.replaceAll("\\s+","");
			if (s.equalsIgnoreCase(ITEM(lang))) nameCol = index;
			else if (s.equalsIgnoreCase(EAN(lang))) eanCol = index;
			else extraVals.put(index, s);
		}
	}

	private int startCol = -1;
	private int endCol = -1;
	private int nameCol = -1, eanCol = -1;

	public int getStartCol() {
		return startCol;
	}
	public void setStartCol(int startCol) {
		this.startCol = startCol;
	}
	public int getEndCol() {
		return endCol;
	}
	public void setEndCol(int endCol) {
		this.endCol = endCol;
	}
	public int getNameCol() {
		return nameCol;
	}
	public void setNameCol(int nameCol) {
		this.nameCol = nameCol;
	}
	public int getEanCol() {
		return eanCol;
	}
	public void setEanCol(int eanCol) {
		this.eanCol = eanCol;
	}
}
