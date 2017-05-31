package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

public class XlsLot {
	public static final String BLOCK = "Lotinformation";

	public static final String NUMBER = "Chargennummer";
	public static final String MHD = "MHD";// oder Verbrauchsdatum".replaceAll("\\s+","");;

	public void addField(String fieldname, int index) {
		if (fieldname != null) {
			String s = fieldname.replaceAll("\\s+","");
			if (s.equals(NUMBER)) lotCol = index;
			else if (s.startsWith(MHD)) mhdCol = index;
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
