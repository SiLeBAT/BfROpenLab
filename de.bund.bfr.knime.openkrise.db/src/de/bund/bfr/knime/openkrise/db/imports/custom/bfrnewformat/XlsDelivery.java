package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

public class XlsDelivery {
	public static final String BLOCK = "Lieferung";

	public static final String DELIVERY_DATE = "Lieferdatum";
	public static final String DAY = "Tag";
	public static final String MONTH = "Monat";
	public static final String YEAR = "Jahr";
	public static final String AMOUNT = "abgegebene Menge";//"abgegebene Menge\n(z.B. 4 Kartons a 10kg)".replaceAll("\\s+","");;
	public static final String COMMENT = "Kommentar";

	public void addField(String fieldname, int index) {
		if (fieldname != null) {
			String s = fieldname.replaceAll("\\s+","");
			if (s.equals(DELIVERY_DATE)) {
				dayCol = index;
				monthCol = index+1;
				yearCol = index+2;
			}
			else if (s.equals(DAY)) dayCol = index;
			else if (s.equals(MONTH)) monthCol = index;
			else if (s.equals(YEAR)) yearCol = index;
			else if (s.startsWith(AMOUNT)) amountCol = index;
			else if (s.equals(COMMENT)) commentCol = index;
		}
	}

	private int startCol = -1;
	private int endCol = -1;
	private int dayCol = -1, monthCol = -1, yearCol = -1, amountCol = -1, commentCol = -1, chargenLinkCol = -1;

	public int getChargenLinkCol() {
		return chargenLinkCol;
	}
	public void setChargenLinkCol(int chargenLinkCol) {
		this.chargenLinkCol = chargenLinkCol;
	}
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
	public int getDayCol() {
		return dayCol;
	}
	public void setDayCol(int dayCol) {
		this.dayCol = dayCol;
	}
	public int getMonthCol() {
		return monthCol;
	}
	public void setMonthCol(int monthCol) {
		this.monthCol = monthCol;
	}
	public int getYearCol() {
		return yearCol;
	}
	public void setYearCol(int yearCol) {
		this.yearCol = yearCol;
	}
	public int getAmountCol() {
		return amountCol;
	}
	public void setAmountCol(int amountCol) {
		this.amountCol = amountCol;
	}
	public int getCommentCol() {
		return commentCol;
	}
	public void setCommentCol(int commentCol) {
		this.commentCol = commentCol;
	}
}
