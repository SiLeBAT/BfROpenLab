package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

public class XlsLot {
	public static String BLOCK(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Lotinformation";
		else if (lang.equals("en")) return "Lot Information";
		else return null;
	}
	public static String NUMBER(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Chargennummer";
		else if (lang.equals("en")) return "LotNumber";
		else return null;
	}
	public static String MHD(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "MHD";
		else if (lang.equals("en")) return "BestBefore";
		else return null;
	}

	public void addField(String fieldname, int index, String lang) {
		if (fieldname != null) {
			String s = fieldname.replaceAll("\\s+","");
			if (s.equals(NUMBER(lang))) lotCol = index;
			else if (s.startsWith(MHD(lang))) mhdCol = index;
		}
	}

	private int startCol = -1;
	private int endCol = -1;
	private int lotCol = -1, mhdCol = -1;
	
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
	public int getLotCol() {
		return lotCol;
	}
	public void setLotCol(int lotCol) {
		this.lotCol = lotCol;
	}
	public int getMhdCol() {
		return mhdCol;
	}
	public void setMhdCol(int mhdCol) {
		this.mhdCol = mhdCol;
	}
}
