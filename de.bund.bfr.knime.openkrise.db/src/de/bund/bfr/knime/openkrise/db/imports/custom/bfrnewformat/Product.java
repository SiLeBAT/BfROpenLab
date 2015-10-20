package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;

public class Product {

	private List<Exception> exceptions = new ArrayList<>();

	public List<Exception> getExceptions() {
		return exceptions;
	}
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

	public Integer getID(Integer miDbId, MyDBI mydbi) throws Exception {
		if (dbId != null) return dbId;
		Integer retId = station == null ? null : getID(station,new String[]{"Bezeichnung"}, new String[]{name}, miDbId, mydbi);
		dbId = retId;
		return retId;
	}
	private Integer getID(Station station, String[] feldnames, String[] feldVals, Integer miDbId, MyDBI mydbi) throws Exception {
		Integer dbStatID = station.getID(miDbId, mydbi);
		if (station.getExceptions().size() > 0) exceptions.addAll(station.getExceptions());
		if (dbStatID == null) {
			exceptions.add(new Exception("addendum: Station unknown..."));
			return null;
		}

		Integer result = null;
		String sql = "SELECT " + MyDBI.delimitL("ID") + " FROM " + MyDBI.delimitL("Produktkatalog") +
				" WHERE " + MyDBI.delimitL("Station") + "=" + dbStatID;
		String in = MyDBI.delimitL("Station") + "," + MyDBI.delimitL("ImportSources");
		String iv = dbStatID + ",';" + miDbId + ";'";
		for (int i=0;i<feldnames.length;i++) {
			if (feldVals[i] != null) {
				sql += " AND UCASE(" + MyDBI.delimitL(feldnames[i]) + ")='" + feldVals[i].toUpperCase() + "'";
				in += "," + MyDBI.delimitL(feldnames[i]);
				iv += ",'" + feldVals[i] + "'";
			}
		}

		ResultSet rs = (mydbi != null ? mydbi.getResultSet(sql, false) : DBKernel.getResultSet(sql, false));

		if (rs != null && rs.first()) {
			result = rs.getInt(1);
			rs.close();
		}

		if (result != null) {
			sql = "UPDATE " + MyDBI.delimitL("Produktkatalog") + " SET " + MyDBI.delimitL("ImportSources") + "=CASEWHEN(INSTR(';" + miDbId + ";'," + MyDBI.delimitL("ImportSources") + ")=0,CONCAT(" + MyDBI.delimitL("ImportSources") + ", '" + miDbId + ";'), " + MyDBI.delimitL("ImportSources") + ") WHERE " + MyDBI.delimitL("ID") + "=" + result;
			if (mydbi != null) mydbi.sendRequest(sql, false, false);
			else DBKernel.sendRequest(sql, false);
		}
		else if (!iv.isEmpty()) {
			sql = "INSERT INTO " + MyDBI.delimitL("Produktkatalog") + " (" + in + ") VALUES (" + iv + ")";
			@SuppressWarnings("resource")
			Connection conn = (mydbi != null ? mydbi.getConn() : DBKernel.getDBConnection());
			PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			if (ps.executeUpdate() > 0) {
				result = (mydbi != null ? mydbi.getLastInsertedID(ps) : DBKernel.getLastInsertedID(ps));
			}
		}

		return result;
	}
}
