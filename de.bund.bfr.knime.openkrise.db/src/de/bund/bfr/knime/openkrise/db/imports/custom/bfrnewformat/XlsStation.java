package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

public class XlsStation {
	public static final String BLOCK_RECIPIENT = "Empfänger";
	public static final String BLOCK_SUPPLIER = "Lieferant";

	public static final String NAME = "Name";
	public static final String ADDRESS = "Adresse";//\n(Straße Hausnummer, PLZ Ort)".replaceAll("\\s+","");
	public static final String COUNTRY = "Land";
	public static final String TOB = "Betriebsart";
	
	public void addField(String fieldname, int index) {
		if (fieldname != null) {
			String s = fieldname.replaceAll("\\s+","");
			if (s.equals(NAME)) nameCol = index;
			else if (s.startsWith(ADDRESS)) addressCol = index;
			else if (s.equals(COUNTRY)) countryCol = index;
			else if (s.equals(TOB)) tobCol = index;
		}
	}
	private int startCol = -1;
	private int endCol = -1;
	private int nameCol = -1, addressCol = -1, countryCol = -1, tobCol = -1;
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
	public int getAddressCol() {
		return addressCol;
	}
	public void setAddressCol(int addressCol) {
		this.addressCol = addressCol;
	}
	public int getCountryCol() {
		return countryCol;
	}
	public void setCountryCol(int countryCol) {
		this.countryCol = countryCol;
	}
	public int getTobCol() {
		return tobCol;
	}
	public void setTobCol(int tobCol) {
		this.tobCol = tobCol;
	}
}
