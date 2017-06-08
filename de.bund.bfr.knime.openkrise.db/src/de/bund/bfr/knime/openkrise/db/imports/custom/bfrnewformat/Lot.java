/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;

public class Lot {

	private static HashMap<String, Lot> gathereds = new HashMap<>();
	private boolean alreadyInDb = false;
	private HashMap<String, String> flexibles = new HashMap<>();
	public String getFlexible(String key) {
		if (flexibles.containsKey(key)) return flexibles.get(key);
		else return null;
	}
	private HashSet<String> inDeliveries = new HashSet<>();
	private List<Exception> exceptions = new ArrayList<>();
	private Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<Exception> getExceptions() {
		return exceptions;
	}

	public static void reset() {
		gathereds = new HashMap<>();
	}
	public HashSet<String> getInDeliveries() {
		return inDeliveries;
	}
	public void addFlexibleField(String key, String value) {
		flexibles.put(key, value);
	}
	private Product product;
	public Product getProduct() {
		return product;
	}
	public void setProduct(Product product) {
		this.product = product;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		if (product != null && product.getStation() != null) {
			String lotId = product.getStation().getId() + "_" + product.getName() + "_" + number;
			//System.err.println(lotId);
			gathereds.put(lotId, this);
		}
		this.number = number;
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
	private String number;
	private Double unitNumber;
	private String unitUnit;
	private Integer dbId;

	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}
	public Integer getDbId() {
		return dbId;
	}
	
	public Integer getID(Integer miDbId, MyDBI mydbi) throws Exception {
		//if (number == null) logWarnings += "Please, do always provide a lot number as this is most helpful!\n";//throw new Exception("Lot number not defined");
		String lotId = null;
		if (number != null && !number.isEmpty() && product != null && product.getStation() != null) {
			lotId = product.getStation().getId() + "_" + product.getName() + "_" + number;			
		}
		if (lotId != null && gathereds.get(lotId) != null && gathereds.get(lotId).getDbId() != null) dbId = gathereds.get(lotId).getDbId();
		if (alreadyInDb) { // if (dbId != null) {
			handleFlexibles(mydbi);
			return dbId;
		}
		Integer retId = getID(product,number,unitNumber,unitUnit, miDbId, mydbi);
		dbId = retId;
		alreadyInDb = true;
		if (lotId != null && gathereds.get(lotId) != null) gathereds.get(lotId).setDbId(dbId);
		
		if (retId != null) {
			handleFlexibles(mydbi);
		}
		return retId;
	}
	public void handleFlexibles(MyDBI mydbi) {
		// Further flexible cells
		if (dbId != null) {
			for (Entry<String, String> es : flexibles.entrySet()) {
				if (es.getValue() != null && !es.getValue().trim().isEmpty()) {
					String sql = "DELETE FROM " + MyDBI.delimitL("ExtraFields") +
							" WHERE " + MyDBI.delimitL("tablename") + "='Chargen'" +
							" AND " + MyDBI.delimitL("id") + "=" + dbId +
							" AND " + MyDBI.delimitL("attribute") + "='" + es.getKey() + "'";
					if (mydbi != null) mydbi.sendRequest(sql, false, false);
					else DBKernel.sendRequest(sql, false);
					
					sql = "INSERT INTO " + MyDBI.delimitL("ExtraFields") +
							" (" + MyDBI.delimitL("tablename") + "," + MyDBI.delimitL("id") + "," + MyDBI.delimitL("attribute") + "," + MyDBI.delimitL("value") +
							") VALUES ('Chargen'," + dbId + ",'" + es.getKey() + "','" + es.getValue() + "')";
					if (mydbi != null) mydbi.sendRequest(sql, false, false);
					else DBKernel.sendRequest(sql, false);
				}
			}		
		}
	}
	private Integer getID(Product product, String number, Double unitNumber, String unitUnit, Integer miDbId, MyDBI mydbi) throws Exception {
		Integer dbProdID = product.getID(miDbId, mydbi);
		//if (!product.getLogMessages().isEmpty()) logMessages += product.getLogMessages() + "\n";
		if (product.getExceptions().size() > 0) exceptions.addAll(product.getExceptions());
		if (dbProdID == null) {
			exceptions.add(new Exception("addendum: Product unknown..."));
			return null;
		}

		Integer result = null;
		String sql = "SELECT " + MyDBI.delimitL("ID") + " FROM " + MyDBI.delimitL("Chargen") +
				" WHERE " + MyDBI.delimitL("Artikel") + "=" + dbProdID + " AND " + MyDBI.delimitL("ChargenNr") + "='" + number + "'";
		String in = MyDBI.delimitL("Artikel") + "," + MyDBI.delimitL("ImportSources");
		String iv = dbProdID + ",';" + miDbId + ";'";
		if (number != null) {
			in += "," + MyDBI.delimitL("ChargenNr");
			iv += ",'" + number + "'";
		}
		if (unitNumber != null) {
			//sql += " AND " + MyDBI.delimitL("Menge") + "=" + unitNumber + "";
			in += "," + MyDBI.delimitL("Menge");
			String un = ("" + unitNumber).replace(",", ".");
			iv += "," + un;
		}
		if (unitUnit != null) {
			//sql += " AND UCASE(" + MyDBI.delimitL("Einheit") + ")='" + unitUnit.toUpperCase() + "'";
			in += "," + MyDBI.delimitL("Einheit");
			iv += ",'" + unitUnit + "'";
		}

		if (number != null) {
			ResultSet rs = (mydbi != null ? mydbi.getResultSet(sql, false) : DBKernel.getResultSet(sql, false));
			if (rs != null && rs.first()) {
				result = rs.getInt(1);
				rs.close();
			}
		}

		if (result != null) {
			sql = "UPDATE " + MyDBI.delimitL("Chargen") + " SET " + MyDBI.delimitL("ImportSources") + "=CASEWHEN(INSTR(';" + miDbId + ";'," + MyDBI.delimitL("ImportSources") + ")=0,CONCAT(" + MyDBI.delimitL("ImportSources") + ", '" + miDbId + ";'), " + MyDBI.delimitL("ImportSources") + ") WHERE " + MyDBI.delimitL("ID") + "=" + result;
			if (mydbi != null) mydbi.sendRequest(sql, false, false);
			else DBKernel.sendRequest(sql, false);
		}
		else if (!iv.isEmpty()) {
			sql = "INSERT INTO " + MyDBI.delimitL("Chargen") + " (" + MyDBI.delimitL("ID") + "," + in + ") VALUES (" + getDbId() + "," + iv + ")";
			@SuppressWarnings("resource")
			Connection conn = (mydbi != null ? mydbi.getConn() : DBKernel.getDBConnection());
			PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			try {
				if (ps.executeUpdate() > 0) {
					result = (mydbi != null ? mydbi.getLastInsertedID(ps) : DBKernel.getLastInsertedID(ps));
					//System.err.println(result);
				}
			}
			catch (SQLException e) {
				if (e.getMessage().startsWith("integrity constraint violation")) result = dbId; // Format_2017;//throw new Exception("Station ID is already assigned"); //  " + intId + "
				else throw e;
			}
		}

		return result;
	}	
	
	public Integer insertIntoDb(MyDBI mydbi) throws Exception {
		if (alreadyInDb) return dbId;
		//dbId = null;
		int productId = this.getProduct().insertIntoDb(mydbi);
		String in = MyDBI.delimitL("ID") + "," + MyDBI.delimitL("Serial") + "," + MyDBI.delimitL("Artikel");
		String iv = id + "," + id + "," + productId;
		String[] feldnames = new String[]{"ChargenNr"};
		String[] sFeldVals = new String[]{number};

		int i=0;
		for (;i<sFeldVals.length;i++) {
			//if (sFeldVals[i] != null) {
				in += "," + MyDBI.delimitL(feldnames[i]);
				iv += ",?";
			//}
		}
		String sql = "INSERT INTO " + MyDBI.delimitL("Chargen") + " (" + in + ") VALUES (" + iv + ")";
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
			if (e.getMessage().startsWith("integrity constraint violation")) {
				dbId = id;
				//throw new Exception("Station ID " + dbId + " is already assigned\n" + e.toString() + "\n" + sql);
			}
			else throw e;
		}
		handleFlexibles(mydbi);
		for (String inDel : inDeliveries) {
			Delivery.insertD2D(mydbi, inDel, dbId+"");
		}
		alreadyInDb = true;
		return dbId;
	}
}
