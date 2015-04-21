package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.sql.PreparedStatement;
import java.sql.Statement;

import de.bund.bfr.knime.openkrise.db.DBKernel;

public class MetaInfo {

	public String getReporter() {
		return reporter;
	}
	public void setReporter(String reporter) {
		this.reporter = reporter;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	private String reporter;
	private String date;
	private String remarks;
	private String filename;
	
	
	private String logMessages = "";
	
	public String getLogMessages() {
		return logMessages;
	}
	public Integer getID() throws Exception {
		Integer result = null;
		String s1 = DBKernel.delimitL("filename");
		String s2 = "'" + filename + "'";
		if (reporter != null && !reporter.isEmpty()) {
			s1 += "," + DBKernel.delimitL("reporter");
			s2 += ",'" + reporter + "'";
		}
		if (date != null && !date.isEmpty()) {
			s1 += "," + DBKernel.delimitL("date");
			s2 += ",'" + date + "'";
		}
		if (remarks != null && !remarks.isEmpty()) {
			s1 += "," + DBKernel.delimitL("remarks");
			s2 += ",'" + remarks + "'";
		}
		try {
			String sql = "INSERT INTO " + DBKernel.delimitL("ImportMetadata") + " (" + s1 + ") VALUES (" + s2 + ")";
			PreparedStatement ps = DBKernel.getDBConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			if (ps.executeUpdate() > 0) result = DBKernel.getLastInsertedID(ps);
		}
		catch (Exception e) {System.err.println(e.getMessage());}

		return result;
	}
}
