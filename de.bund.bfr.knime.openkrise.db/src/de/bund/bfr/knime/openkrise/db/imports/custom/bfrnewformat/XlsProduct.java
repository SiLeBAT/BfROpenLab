package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

public class XlsProduct {
	public static final String BLOCK_PRODUCT = "Produkt";
	public static final String BLOCK_INGREDIENT = "Zutat";

	public static final String ITEM = "Bezeichnung";
	public static final String EAN = "EAN";
	
	public void addField(String fieldname, int index) {
		if (fieldname != null) {
			String s = fieldname.replaceAll("\\s+","");
			if (s.equals(ITEM)) nameCol = index;
			else if (s.equals(EAN)) eanCol = index;
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
