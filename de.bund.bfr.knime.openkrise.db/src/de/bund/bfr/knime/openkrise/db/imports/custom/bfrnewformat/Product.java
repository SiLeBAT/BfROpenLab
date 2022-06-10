/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;

public class Product {

	private List<Exception> exceptions = new ArrayList<>();
	private boolean alreadyInDb = false;
	private HashMap<String, String> flexibles = new HashMap<>();
	public String getFlexible(String key) {
		if (flexibles.containsKey(key)) return flexibles.get(key);
		else return null;
	}
	private Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void addFlexibleField(String key, String value) {
		flexibles.put(key, value);
	}

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
		if (alreadyInDb)  // dbId != null
			return dbId;
		Integer retId = station == null ? null : getID(station,new String[]{"Bezeichnung"}, new String[]{name}, miDbId, mydbi);
		dbId = retId;
		alreadyInDb = true;
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
				iv += ",'" + feldVals[i].replace("'", "''") + "'";
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
			sql = "INSERT INTO " + MyDBI.delimitL("Produktkatalog") + " (" + MyDBI.delimitL("ID") + "," + in + ") VALUES (" + getDbId() + "," + iv + ")";
			@SuppressWarnings("resource")
			Connection conn = (mydbi != null ? mydbi.getConn() : DBKernel.getDBConnection());
			PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			try {
				if (ps.executeUpdate() > 0) {
					result = (mydbi != null ? mydbi.getLastInsertedID(ps) : DBKernel.getLastInsertedID(ps));
					if (result != null) handleFlexibles(mydbi);
				}
			}
			catch (SQLException e) {
				if (e.getSQLState().equals("23505")) { // && e.getErrorCode() == -104   e.getMessage().startsWith("integrity constraint violation")) {
					result = dbId; // Format_2017;//throw new Exception("Station ID is already assigned"); //  " + intId + "
				}
				else throw e;
			}
		}

		return result;
	}
	public void handleFlexibles(MyDBI mydbi) {
		// Further flexible cells
		if (dbId != null) {
			for (Entry<String, String> es : flexibles.entrySet()) {
				if (es.getValue() != null && !es.getValue().trim().isEmpty()) {
					String sql = "DELETE FROM " + MyDBI.delimitL("ExtraFields") +
							" WHERE " + MyDBI.delimitL("tablename") + "='Produktkatalog'" +
							" AND " + MyDBI.delimitL("id") + "=" + dbId +
							" AND " + MyDBI.delimitL("attribute") + "='" + es.getKey() + "'";
					if (mydbi != null) mydbi.sendRequest(sql, false, false);
					else DBKernel.sendRequest(sql, false);
					
					sql = "INSERT INTO " + MyDBI.delimitL("ExtraFields") +
							" (" + MyDBI.delimitL("tablename") + "," + MyDBI.delimitL("id") + "," + MyDBI.delimitL("attribute") + "," + MyDBI.delimitL("value") +
							") VALUES ('Produktkatalog'," + dbId + ",'" + es.getKey() + "','" + es.getValue() + "')";
					if (mydbi != null) mydbi.sendRequest(sql, false, false);
					else DBKernel.sendRequest(sql, false);
				}
			}		
		}
	}
	public Integer insertIntoDb(MyDBI mydbi) throws Exception {
		if (alreadyInDb) return dbId;
		//dbId = null;
		int stationId = this.getStation().insertIntoDb(mydbi);
		String in = MyDBI.delimitL("ID") + "," + MyDBI.delimitL("Station") + "," + MyDBI.delimitL("Serial");
		String iv = id + "," + stationId + "," + id;
		String[] feldnames = new String[]{"Bezeichnung"};
		String[] sFeldVals = new String[]{name};

		int i=0;
		for (;i<sFeldVals.length;i++) {
			//if (sFeldVals[i] != null) {
				in += "," + MyDBI.delimitL(feldnames[i]);
				iv += ",?";
			//}
		}
		String sql = "INSERT INTO " + MyDBI.delimitL("Produktkatalog") + " (" + in + ") VALUES (" + iv + ")";
		@SuppressWarnings("resource")
		Connection conn = (mydbi != null ? mydbi.getConn() : DBKernel.getDBConnection());
		PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		for (int ii=0;ii<sFeldVals.length;ii++) {
			if (sFeldVals[ii] == null) ps.setNull(ii+1, Types.VARCHAR);	
			else ps.setString(ii+1, sFeldVals[ii]);
		}
		try {
			if (ps.executeUpdate() > 0) {
				dbId = (mydbi != null ? mydbi.getLastInsertedID(ps) : DBKernel.getLastInsertedID(ps));
			}
		}
		catch (SQLException e) {
			if (e.getSQLState().equals("23505")) { // && e.getErrorCode() == -104   e.getMessage().startsWith("integrity constraint violation")) {
				dbId = id;
			}//throw new Exception("Station ID " + dbId + " is already assigned\n" + e.toString() + "\n" + sql);
			else throw e;
		}
		handleFlexibles(mydbi);
		alreadyInDb = true;
		return dbId;
	}
}
