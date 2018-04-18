/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;

public class Delivery {

	public static HashMap<String, Delivery> gathereds = new HashMap<>();
	private boolean alreadyInDb = false;
	private HashMap<String, String> flexibles = new HashMap<>();
	public String getFlexible(String key) {
		if (flexibles.containsKey(key)) return flexibles.get(key);
		else return null;
	}
	private List<Exception> exceptions = new ArrayList<>();
	private boolean newlyGeneratedID = false;

	public boolean isNewlyGeneratedID() {
		return newlyGeneratedID;
	}
	public void setNewlyGeneratedID(boolean newlyGeneratedID) {
		this.newlyGeneratedID = newlyGeneratedID;
	}
	public List<Exception> getExceptions() {
		return exceptions;
	}
	public static void reset() {
		gathereds = new HashMap<>();
	}
	public void addFlexibleField(String key, String value) {
		flexibles.put(key, value);
	}
	private Lot lot;
	private String comment;
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Lot getLot() {
		return lot;
	}
	public void setLot(Lot lot) {
		this.lot = lot;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		if (id != null) {
			gathereds.put(id, this);
		}
		this.id = id;
	}
	public Integer getArrivalDay() {
		return arrivalDay;
	}
	public void setArrivalDay(Integer arrivalDay) {
		this.arrivalDay = arrivalDay;
	}
	public Integer getArrivalMonth() {
		return arrivalMonth;
	}
	public void setArrivalMonth(Integer arrivalMonth) {
		this.arrivalMonth = arrivalMonth;
	}
	public Integer getArrivalYear() {
		return arrivalYear;
	}
	public void setArrivalYear(Integer arrivalYear) {
		this.arrivalYear = arrivalYear;
	}
	public Double getUnitNumber() {
		return unitNumber;
	}
	public void setUnitNumber(Double unitNumber) {
		this.unitNumber = unitNumber;
	}
	public String getUnitUnit() {
		return unitUnit;
	}
	public void setUnitUnit(String unitUnit) {
		this.unitUnit = unitUnit;
	}
	public Station getReceiver() {
		return receiver;
	}
	public void setReceiver(Station receiver) {
		this.receiver = receiver;
	}
	private String id;
	private Integer arrivalDay;
	private Integer arrivalMonth;
	private Integer arrivalYear;
	private Integer departureDay;
	public Integer getDepartureDay() {
		return departureDay;
	}
	public void setDepartureDay(Integer departureDay) {
		this.departureDay = departureDay;
	}
	public Integer getDepartureMonth() {
		return departureMonth;
	}
	public void setDepartureMonth(Integer departureMonth) {
		this.departureMonth = departureMonth;
	}
	public Integer getDepartureYear() {
		return departureYear;
	}
	public void setDepartureYear(Integer departureYear) {
		this.departureYear = departureYear;
	}
	private Integer departureMonth;
	private Integer departureYear;
	private Double unitNumber;
	private String unitUnit;
	private Station receiver;
	private HashSet<String> targetLotIds = new HashSet<>();
	public HashSet<String> getTargetLotIds() {
		return targetLotIds;
	}
	public void addTargetLotId(String targetLotId) {
		targetLotIds.add(targetLotId);
	}

	private Integer dbId;
	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}
	public Integer getDbId() {
		return dbId;
	}
	
	public Integer getID(Integer miDbId, boolean dataMayhaveChanged, MyDBI mydbi) throws Exception {
		if (id != null && gathereds.get(id) != null && gathereds.get(id).getDbId() != null) dbId = gathereds.get(id).getDbId();
		if (alreadyInDb) { // if (dbId != null) {
			handleFlexibles(mydbi);
			return dbId;
		}
		Integer retId = getID(lot,receiver,new String[]{"dd_day","dd_month","dd_year","ad_day","ad_month","ad_year","numPU","typePU","Serial"}, // "Charge","Empfänger",
				new Integer[]{departureDay,departureMonth,departureYear,arrivalDay,arrivalMonth,arrivalYear}, unitNumber, new String[]{unitUnit,id}, miDbId, dataMayhaveChanged, mydbi);
		dbId = retId;
		alreadyInDb = true;
		if (id != null && gathereds.get(id) != null) gathereds.get(id).setDbId(dbId);
		
		if (retId != null) {
			handleFlexibles(mydbi);
		}
		return retId;
	}
	public void handleFlexibles(MyDBI mydbi) {
		// Further flexible cells		
		for (Entry<String, String> es : flexibles.entrySet()) {
			if (es.getValue() != null && !es.getValue().trim().isEmpty()) {
				String sql = "DELETE FROM " + MyDBI.delimitL("ExtraFields") +
						" WHERE " + MyDBI.delimitL("tablename") + "='Lieferungen'" +
						" AND " + MyDBI.delimitL("id") + "=" + dbId +
						" AND " + MyDBI.delimitL("attribute") + "='" + es.getKey() + "'";
				if (mydbi != null) mydbi.sendRequest(sql, false, false);
				else DBKernel.sendRequest(sql, false);
				sql = "INSERT INTO " + MyDBI.delimitL("ExtraFields") +
						" (" + MyDBI.delimitL("tablename") + "," + MyDBI.delimitL("id") + "," + MyDBI.delimitL("attribute") + "," + MyDBI.delimitL("value") +
						") VALUES ('Lieferungen'," + dbId + ",'" + es.getKey() + "','" + es.getValue() + "')";
				if (mydbi != null) mydbi.sendRequest(sql, false, false);
				else DBKernel.sendRequest(sql, false);
			}
		}
	}

	private Integer getID(Lot lot, Station receiver, String[] feldnames, Integer[] iFeldVals, Double unitNumber, String[] sFeldVals, Integer miDbId, boolean dataMayhaveChanged, MyDBI mydbi) throws Exception {
		Integer dbRecID = receiver.getID(miDbId, mydbi);
		//if (!receiver.getLogMessages().isEmpty()) logMessages += receiver.getLogMessages() + "\n";
		if (receiver.getExceptions().size() > 0) exceptions.addAll(receiver.getExceptions());
		if (dbRecID == null) {
			exceptions.add(new Exception("addendum: Receiver unknown..."));
			return null;
		}
		Integer result = null;
		if (dataMayhaveChanged && id != null && !id.isEmpty() && lot != null && lot.getProduct() != null && lot.getProduct().getStation() != null) {
			String sql = "SELECT " + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("ID") +
					"," + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") +
					"," + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("ID") +
					" FROM " + MyDBI.delimitL("Lieferungen") +
					" LEFT JOIN " + MyDBI.delimitL("Chargen") +
					" ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Charge") +
					" LEFT JOIN " + MyDBI.delimitL("Produktkatalog") +
					" ON " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("Artikel") +
					" WHERE " + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Empfänger") + "=" + dbRecID +
					" AND UCASE(" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Serial") + ")='" + id.toUpperCase() + "'" +
					" AND " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Station") + "=" + lot.getProduct().getStation().getID(miDbId, mydbi);
			ResultSet rs = (mydbi != null ? mydbi.getResultSet(sql, false) : DBKernel.getResultSet(sql, false));
			if (rs != null && rs.first()) {
				lot.getProduct().setDbId(rs.getInt("Produktkatalog.ID"));
				if (lot.getProduct().getName() != null && !lot.getProduct().getName().isEmpty()) {
					sql = "UPDATE " + MyDBI.delimitL("Produktkatalog") + " SET " + MyDBI.delimitL("Bezeichnung") + " = '" + lot.getProduct().getName() + "' WHERE " + MyDBI.delimitL("ID") + "=" + rs.getInt("Produktkatalog.ID");
					if (mydbi != null) mydbi.sendRequest(sql, true, false);
					else DBKernel.sendRequest(sql, true);
					sql = "UPDATE " + MyDBI.delimitL("Produktkatalog") + " SET " + MyDBI.delimitL("ImportSources") + "=CASEWHEN(INSTR(';" + miDbId + ";'," + MyDBI.delimitL("ImportSources") + ")=0,CONCAT(" + MyDBI.delimitL("ImportSources") + ", '" + miDbId + ";'), " + MyDBI.delimitL("ImportSources") + ") WHERE " + MyDBI.delimitL("ID") + "=" + rs.getInt("Produktkatalog.ID");
					if (mydbi != null) mydbi.sendRequest(sql, false, false);
					else DBKernel.sendRequest(sql, false);
				}

				lot.setDbId(rs.getInt("Chargen.ID"));
				if (lot.getNumber() != null && !lot.getNumber().isEmpty()) {
					sql = "UPDATE " + MyDBI.delimitL("Chargen") + " SET " + MyDBI.delimitL("ChargenNr") + " = '" + lot.getNumber() + "' WHERE " + MyDBI.delimitL("ID") + "=" + rs.getInt("Chargen.ID");
					if (mydbi != null) mydbi.sendRequest(sql, true, false);
					else DBKernel.sendRequest(sql, true);
					if (lot.getUnitNumber() != null) {
						sql = "UPDATE " + MyDBI.delimitL("Chargen") + " SET " + MyDBI.delimitL("Menge") + " = " + lot.getUnitNumber() + " WHERE " + MyDBI.delimitL("ID") + "=" + rs.getInt("Chargen.ID");
						if (mydbi != null) mydbi.sendRequest(sql, true, false);
						else DBKernel.sendRequest(sql, true);
					}
					if (lot.getUnitUnit() != null) {						
						sql = "UPDATE " + MyDBI.delimitL("Chargen") + " SET " + MyDBI.delimitL("Einheit") + " = '" + lot.getUnitUnit() + "' WHERE " + MyDBI.delimitL("ID") + "=" + rs.getInt("Chargen.ID");
						if (mydbi != null) mydbi.sendRequest(sql, true, false);
						else DBKernel.sendRequest(sql, true);
					}
					sql = "UPDATE " + MyDBI.delimitL("Chargen") + " SET " + MyDBI.delimitL("ImportSources") + "=CASEWHEN(INSTR(';" + miDbId + ";'," + MyDBI.delimitL("ImportSources") + ")=0,CONCAT(" + MyDBI.delimitL("ImportSources") + ", '" + miDbId + ";'), " + MyDBI.delimitL("ImportSources") + ") WHERE " + MyDBI.delimitL("ID") + "=" + rs.getInt("Chargen.ID");
					if (mydbi != null) mydbi.sendRequest(sql, false, false);
					else DBKernel.sendRequest(sql, false);
					lot.handleFlexibles(mydbi);
				}

				result = rs.getInt("Lieferungen.ID");
				boolean updated = false;
				if (getArrivalDay() != null) {
					sql = "UPDATE " + MyDBI.delimitL("Lieferungen") + " SET " + MyDBI.delimitL("ad_day") + " = " + getArrivalDay() + " WHERE " + MyDBI.delimitL("ID") + "=" + result;
					if (mydbi != null) mydbi.sendRequest(sql, true, false);
					else DBKernel.sendRequest(sql, true);
					updated = true;
				}
				if (getArrivalMonth() != null) {
					sql = "UPDATE " + MyDBI.delimitL("Lieferungen") + " SET " + MyDBI.delimitL("ad_month") + " = " + getArrivalMonth() + " WHERE " + MyDBI.delimitL("ID") + "=" + result;
					if (mydbi != null) mydbi.sendRequest(sql, true, false);
					else DBKernel.sendRequest(sql, true);
					updated = true;
				}
				if (getArrivalYear() != null) {
					sql = "UPDATE " + MyDBI.delimitL("Lieferungen") + " SET " + MyDBI.delimitL("ad_year") + " = " + getArrivalYear() + " WHERE " + MyDBI.delimitL("ID") + "=" + result;
					if (mydbi != null) mydbi.sendRequest(sql, true, false);
					else DBKernel.sendRequest(sql, true);
					updated = true;
				}
				if (getDepartureDay() != null) {
					sql = "UPDATE " + MyDBI.delimitL("Lieferungen") + " SET " + MyDBI.delimitL("dd_day") + " = " + getDepartureDay() + " WHERE " + MyDBI.delimitL("ID") + "=" + result;
					if (mydbi != null) mydbi.sendRequest(sql, true, false);
					else DBKernel.sendRequest(sql, true);
					updated = true;
				}
				if (getDepartureMonth() != null) {
					sql = "UPDATE " + MyDBI.delimitL("Lieferungen") + " SET " + MyDBI.delimitL("dd_month") + " = " + getDepartureMonth() + " WHERE " + MyDBI.delimitL("ID") + "=" + result;
					if (mydbi != null) mydbi.sendRequest(sql, true, false);
					else DBKernel.sendRequest(sql, true);
					updated = true;
				}
				if (getDepartureYear() != null) {
					sql = "UPDATE " + MyDBI.delimitL("Lieferungen") + " SET " + MyDBI.delimitL("dd_year") + " = " + getDepartureYear() + " WHERE " + MyDBI.delimitL("ID") + "=" + result;
					if (mydbi != null) mydbi.sendRequest(sql, true, false);
					else DBKernel.sendRequest(sql, true);
					updated = true;
				}
				if (getUnitNumber() != null) {
					sql = "UPDATE " + MyDBI.delimitL("Lieferungen") + " SET " + MyDBI.delimitL("numPU") + " = " + getUnitNumber() + " WHERE " + MyDBI.delimitL("ID") + "=" + result;
					if (mydbi != null) mydbi.sendRequest(sql, true, false);
					else DBKernel.sendRequest(sql, true);
					updated = true;
				}
				if (getUnitUnit() != null) {
					sql = "UPDATE " + MyDBI.delimitL("Lieferungen") + " SET " + MyDBI.delimitL("typePU") + " = '" + getUnitUnit() + "' WHERE " + MyDBI.delimitL("ID") + "=" + result;
					if (mydbi != null) mydbi.sendRequest(sql, true, false);
					else DBKernel.sendRequest(sql, true);
					updated = true;
				}
				if (updated) {
					sql = "UPDATE " + MyDBI.delimitL("Lieferungen") + " SET " + MyDBI.delimitL("ImportSources") + "=CASEWHEN(INSTR(';" + miDbId + ";'," + MyDBI.delimitL("ImportSources") + ")=0,CONCAT(" + MyDBI.delimitL("ImportSources") + ", '" + miDbId + ";'), " + MyDBI.delimitL("ImportSources") + ") WHERE " + MyDBI.delimitL("ID") + "=" + result;
					if (mydbi != null) mydbi.sendRequest(sql, false, false);
					else DBKernel.sendRequest(sql, false);
				}
				if (rs.next()) exceptions.add(new Exception("Delivery Id seems to be defined more than once in the database. Please provide only unique Ids! (Id: '" + id + "')"));
				rs.close();
			}	
			if (result != null) return result;
		}
		Integer dbLotID = lot.getID(miDbId, mydbi);
		//if (!lot.getLogMessages().isEmpty()) logMessages += lot.getLogMessages() + "\n";
		if (lot.getExceptions().size() > 0) exceptions.addAll(lot.getExceptions());
		if (dbLotID == null) {
			exceptions.add(new Exception("addendum: Lot unknown..."));
			return null;
		}
		String sql = "SELECT " + MyDBI.delimitL("ID") + " FROM " + MyDBI.delimitL("Lieferungen") +
				" WHERE " + MyDBI.delimitL("Empfänger") + "=" + dbRecID;
		sql += " AND " + MyDBI.delimitL("Charge") + "=" + dbLotID; 
		String in = MyDBI.delimitL("Charge") + "," + MyDBI.delimitL("Empfänger") + "," + MyDBI.delimitL("ImportSources");
		String iv = dbLotID + "," + dbRecID + ",';" + miDbId + ";'";
		String serialWhere = "";
		int i=0;
		for (;i<iFeldVals.length;i++) {
			if (iFeldVals[i] != null) {
				sql += " AND " + MyDBI.delimitL(feldnames[i]) + "=" + iFeldVals[i] + "";
				in += "," + MyDBI.delimitL(feldnames[i]);
				iv += "," + iFeldVals[i];
			}
		}
		if (unitNumber != null) {
			sql += " AND " + MyDBI.delimitL(feldnames[i]) + "=" + unitNumber + "";
			in += "," + MyDBI.delimitL(feldnames[i]);
			String un = ("" + unitNumber).replace(",", ".");
			iv += "," + un;
		}
		for (int j=0;j<sFeldVals.length;j++) {
			if (sFeldVals[j] != null) {
				//if (!feldnames[i+1+j].equals("Serial"))
				sql += " AND UCASE(" + MyDBI.delimitL(feldnames[i+1+j]) + ")='" + sFeldVals[j].toUpperCase() + "'";
				in += "," + MyDBI.delimitL(feldnames[i+1+j]);
				iv += ",'" + sFeldVals[j] + "'";
				if (feldnames[i+1+j].equalsIgnoreCase("Serial")) serialWhere = "UCASE(" + MyDBI.delimitL(feldnames[i+1+j]) + ")='" + sFeldVals[j].toUpperCase() + "'";
			}
		}
		/*
		int intId = 0;
		try {
			intId = Integer.parseInt(sFeldVals[sFeldVals.length-1]);
			int numIdsPresent = (mydbi != null ? mydbi.getRowCount("Lieferungen", " WHERE " + MyDBI.delimitL("ID") + "=" + intId) : DBKernel.getRowCount("Lieferungen", " WHERE " + MyDBI.delimitL("ID") + "=" + intId));
			if (numIdsPresent == 0) {
				in += "," + MyDBI.delimitL("ID");
				iv += "," + intId;
			}
		}
		catch (Exception e) {}
*/
		ResultSet rs = (mydbi != null ? mydbi.getResultSet(sql, false) : DBKernel.getResultSet(sql, false));
		if (rs != null && rs.first()) {
			result = rs.getInt(1);
			rs.close();
		}
		
		if (result != null) {
			sql = "UPDATE " + MyDBI.delimitL("Lieferungen") + " SET " + MyDBI.delimitL("ImportSources") + "=CASEWHEN(INSTR(';" + miDbId + ";'," + MyDBI.delimitL("ImportSources") + ")=0,CONCAT(" + MyDBI.delimitL("ImportSources") + ", '" + miDbId + ";'), " + MyDBI.delimitL("ImportSources") + ") WHERE " + MyDBI.delimitL("ID") + "=" + result;
			if (mydbi != null) mydbi.sendRequest(sql, false, false);
			else DBKernel.sendRequest(sql, false);
		}
		else if (!iv.isEmpty()) {
			sql = "INSERT INTO " + MyDBI.delimitL("Lieferungen") + " (" + MyDBI.delimitL("ID") + "," + in + ") VALUES (" + getDbId() + "," + iv + ")";
			@SuppressWarnings("resource")
			Connection conn = (mydbi != null ? mydbi.getConn() : DBKernel.getDBConnection());
			PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			try {
				if (ps.executeUpdate() > 0) {
					result = (mydbi != null ? mydbi.getLastInsertedID(ps) : DBKernel.getLastInsertedID(ps));
					if (serialWhere.length() == 0) {
						sql = "UPDATE " + MyDBI.delimitL("Lieferungen") + " SET " + MyDBI.delimitL("Serial") + "=" + MyDBI.delimitL("ID") + " WHERE " + MyDBI.delimitL("ID") + "=" + result;
						if (mydbi != null) mydbi.sendRequest(sql, false, false);
						else DBKernel.sendRequest(sql, false);
						serialWhere = "UCASE(" + MyDBI.delimitL("Serial") + ")='" + result + "'";
					}
					int numSameSerials = (mydbi != null ? mydbi.getRowCount("Lieferungen", " WHERE " + serialWhere) : DBKernel.getRowCount("Lieferungen", " WHERE " + serialWhere));
					if (numSameSerials > 1) {
						sql = "UPDATE " + MyDBI.delimitL("Lieferungen") + " SET " + MyDBI.delimitL("Serial") + "=CONCAT(" + MyDBI.delimitL("Serial") + ",'_" + result + "') WHERE " + MyDBI.delimitL("ID") + "=" + result;
						if (mydbi != null) mydbi.sendRequest(sql, false, false);
						else DBKernel.sendRequest(sql, false);
					}
				}
			}
			catch (SQLException e) {
				if (e.getMessage().startsWith("integrity constraint violation")) result = dbId; // Format_2017;//throw new Exception("Delivery ID is already assigned\n" + e.toString() + "\n" + sql); //  " + intId + "
				else throw e;
			}
		}

		return result;
	}
	
	public void mergeLot(Integer oldLotDbId, Integer newLotDbId, MyDBI mydbi) {
		if (oldLotDbId != null && newLotDbId != null) {
			String sql = "UPDATE " + MyDBI.delimitL("Lieferungen") + " SET " + MyDBI.delimitL("Charge") + "=" + newLotDbId.intValue() + " WHERE " + MyDBI.delimitL("Charge") + "=" + oldLotDbId.intValue();
			if (mydbi != null) mydbi.sendRequest(sql, false, false);
			else DBKernel.sendRequest(sql, false);
			sql = "UPDATE " + MyDBI.delimitL("ChargenVerbindungen") + " SET " + MyDBI.delimitL("Produkt") + "=" + newLotDbId.intValue() + " WHERE " + MyDBI.delimitL("Produkt") + "=" + oldLotDbId.intValue();
			if (mydbi != null) mydbi.sendRequest(sql, false, false);
			else DBKernel.sendRequest(sql, false);
			sql = "DELETE FROM " + MyDBI.delimitL("Chargen") + " WHERE " + MyDBI.delimitL("ID") + "=" + oldLotDbId.intValue();
			if (mydbi != null) mydbi.sendRequest(sql, false, false);
			else DBKernel.sendRequest(sql, false);
		}
	}
	
	public Integer insertIntoDb(MyDBI mydbi) throws Exception {
		if (alreadyInDb) return dbId;
		//dbId = null;
		int lotId = this.getLot().insertIntoDb(mydbi);
		int recId = this.getReceiver().insertIntoDb(mydbi);
		String in = MyDBI.delimitL("ID") + "," + MyDBI.delimitL("Serial") + "," + MyDBI.delimitL("Charge") + "," + MyDBI.delimitL("Empfänger");
		String iv = Integer.parseInt(id) + "," + Integer.parseInt(id) + "," + lotId + "," + recId;
		String[] feldnames = new String[]{"ad_day","ad_month","ad_year"};
		Integer[] iFeldVals = new Integer[]{arrivalDay,arrivalMonth,arrivalYear};

		int i=0;
		for (;i<iFeldVals.length;i++) {
			if (iFeldVals[i] != null) {
				in += "," + MyDBI.delimitL(feldnames[i]);
				iv += "," + iFeldVals[i];
			}
		}
		if (comment != null) {
              in += "," + MyDBI.delimitL("Kommentar");
              iv += ",?";//"," + "'" + comment + "'";
		}
	      String sql = "INSERT INTO " + MyDBI.delimitL("Lieferungen") + " (" + in + ") VALUES (" + iv + ")";
	      @SuppressWarnings("resource")
	      Connection conn = (mydbi != null ? mydbi.getConn() : DBKernel.getDBConnection());
	      PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	      if (comment != null) ps.setString(1, comment);
	      //boolean newlyInserted = false;
	      try {
	              if (ps.executeUpdate() > 0) {
	                      dbId = (mydbi != null ? mydbi.getLastInsertedID(ps) : DBKernel.getLastInsertedID(ps));
	                      //newlyInserted = true;
	              }
	      }
	      catch (SQLException e) {
              if (e.getMessage().startsWith("integrity constraint violation")) {
                      dbId = Integer.parseInt(id);
                      //throw new Exception("Delivery ID " + dbId + " is already assigned\n" + e.toString() + "\n" + sql);
              }
              else {
                      System.err.println(sql);
                      throw e;
              }
	      }
	      handleFlexibles(mydbi);
		for (String tLotId : targetLotIds) {
			insertD2D(mydbi, dbId+"", tLotId);
		}
		alreadyInDb = true;
		return dbId;
	}
	public static void insertD2D(MyDBI mydbi, String delId, String lotId) throws Exception {
		String sql = "INSERT INTO " + MyDBI.delimitL("ChargenVerbindungen") +
				" (" + MyDBI.delimitL("Zutat") + "," + MyDBI.delimitL("Produkt") + ") VALUES (" + delId + "," + lotId + ")";
		//DBKernel.sendRequest(sql, false);
		@SuppressWarnings("resource")
		Connection conn = (mydbi != null ? mydbi.getConn() : DBKernel.getDBConnection());
		PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		try {
			if (ps.executeUpdate() > 0) {
				if (mydbi != null) mydbi.getLastInsertedID(ps);
				else DBKernel.getLastInsertedID(ps);
			}
		}
		catch (SQLException e) {
			//if (e.getMessage().startsWith("integrity constraint violation")) {
				//throw new Exception("Delivery ID " + dbId + " is already assigned\n" + e.toString() + "\n" + sql);
			//}
			//else 
				throw e;
		}
	}
}
