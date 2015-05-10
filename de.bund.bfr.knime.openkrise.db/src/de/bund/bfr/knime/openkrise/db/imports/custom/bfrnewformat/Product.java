package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import de.bund.bfr.knime.openkrise.db.DBKernel;

public class Product {

	private Station station;
	public Station getStation() {
		return station;
	}
	public void setStation(Station station) {
		this.station = station;
	}
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	private Integer dbId;
	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}
	public Integer getDbId() {
		return dbId;
	}

	private String logMessages = "";
	
	public String getLogMessages() {
		return logMessages;
	}
	public Integer getID(Integer miDbId) throws Exception {
		if (dbId != null) return dbId;
		Integer retId = getID(station,new String[]{"Bezeichnung"}, new String[]{name}, miDbId);
		dbId = retId;
		return retId;
	}
	private Integer getID(Station station, String[] feldnames, String[] feldVals, Integer miDbId) throws Exception {
		Integer dbStatID = station.getID(miDbId);
		if (!station.getLogMessages().isEmpty()) logMessages += station.getLogMessages() + "\n";
		if (dbStatID == null) {
			logMessages += "Station unknown...\n";
			return null;
		}

		Integer result = null;
		String sql = "SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL("Produktkatalog") +
				" WHERE " + DBKernel.delimitL("Station") + "=" + dbStatID;
		String in = DBKernel.delimitL("Station") + "," + DBKernel.delimitL("ImportSources");
		String iv = dbStatID + ",';" + miDbId + ";'";
		for (int i=0;i<feldnames.length;i++) {
			if (feldVals[i] != null) {
				sql += " AND UCASE(" + DBKernel.delimitL(feldnames[i]) + ")='" + feldVals[i].toUpperCase() + "'";
				in += "," + DBKernel.delimitL(feldnames[i]);
				iv += ",'" + feldVals[i] + "'";
			}
		}

		ResultSet rs = DBKernel.getResultSet(sql, false);

		if (rs != null && rs.first()) {
			result = rs.getInt(1);
		}

		if (result != null) {
			DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Produktkatalog") + " SET " + DBKernel.delimitL("ImportSources") + "=CASEWHEN(INSTR(';" + miDbId + ";'," + DBKernel.delimitL("ImportSources") + ")=0,CONCAT(" + DBKernel.delimitL("ImportSources") + ", '" + miDbId + ";'), " + DBKernel.delimitL("ImportSources") + ") WHERE " + DBKernel.delimitL("ID") + "=" + result, false);
		}
		else if (!iv.isEmpty()) {
			sql = "INSERT INTO " + DBKernel.delimitL("Produktkatalog") + " (" + in + ") VALUES (" + iv + ")";
			PreparedStatement ps = DBKernel.getDBConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			if (ps.executeUpdate() > 0) result = DBKernel.getLastInsertedID(ps);
		}

		return result;
	}
}
