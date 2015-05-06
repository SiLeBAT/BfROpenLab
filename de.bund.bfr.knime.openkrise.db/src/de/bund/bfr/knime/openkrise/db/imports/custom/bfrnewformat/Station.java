package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map.Entry;

import de.bund.bfr.knime.openkrise.db.DBKernel;

public class Station {

	private static HashMap<String, Station> gathereds = new HashMap<>();
	private HashMap<String, String> flexibles = new HashMap<>();

	public void addFlexibleField(String key, String value) {
		flexibles.put(key, value);
	}
	
	private String id;
	public String getId() {
		return id;
	}
	public Station setId(String id) {
		this.id = id;
		gathereds.put(id, this);
		return this;
	}
	public String getName() {
		return name;
	}
	public Station setName(String name) {
		this.name = name;
		return this;
	}
	public String getStreet() {
		return street;
	}
	public Station setStreet(String street) {
		this.street = street;
		return this;
	}
	public String getNumber() {
		return number;
	}
	public Station setNumber(String number) {
		this.number = number;
		return this;
	}
	public String getZip() {
		return zip;
	}
	public Station setZip(String zip) {
		this.zip = zip;
		return this;
	}
	public String getCity() {
		return city;
	}
	public Station setCity(String city) {
		this.city = city;
		return this;
	}
	public String getDistrict() {
		return district;
	}
	public Station setDistrict(String district) {
		this.district = district;
		return this;
	}
	public String getState() {
		return state;
	}
	public Station setState(String state) {
		this.state = state;
		return this;
	}
	public String getCountry() {
		return country;
	}
	public Station setCountry(String country) {
		this.country = country;
		return this;
	}
	public String getTypeOfBusiness() {
		return typeOfBusiness;
	}
	public Station setTypeOfBusiness(String typeOfBusiness) {
		this.typeOfBusiness = typeOfBusiness;
		return this;
	}
	private String name;
	private String street;
	private String number;
	private String zip;
	private String city;
	private String district;
	private String state;
	private String country;
	private String typeOfBusiness;
	
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
		if (gathereds.get(id).getDbId() != null) dbId = gathereds.get(id).getDbId();
		if (dbId != null) return dbId;
		Integer retId = getID(new String[]{"Name","Strasse","Hausnummer","PLZ","Ort","District","Bundesland","Land","Betriebsart","Serial"},
				new String[]{name,street,number,zip,city,district,state,country,typeOfBusiness,id}, miDbId);
		dbId = retId;
		gathereds.get(id).setDbId(dbId);
		
		// Further flexible cells
		if (retId != null) {
			for (Entry<String, String> es : flexibles.entrySet()) {
				DBKernel.sendRequest("INSERT INTO " + DBKernel.delimitL("ExtraFields") +
						" (" + DBKernel.delimitL("tablename") + "," + DBKernel.delimitL("id") + "," + DBKernel.delimitL("attribute") + "," + DBKernel.delimitL("value") +
						") VALUES ('Station'," + retId + ",'" + es.getKey() + "','" + es.getValue() + "')", false);
			}
		}
		return retId;
	}
	private Integer getID(String[] feldnames, String[] feldVals, Integer miDbId) throws Exception {
		Integer result = null;
		String sql = "SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL("Station") + " WHERE TRUE ";
		String in = DBKernel.delimitL("ImportSources");
		String iv =  "';" + miDbId + ";'";
		String serialWhere = "";
		for (int i=0;i<feldnames.length;i++) {
			if (feldVals[i] != null) {
				sql += " AND UCASE(" + DBKernel.delimitL(feldnames[i]) + ")='" + feldVals[i].toUpperCase() + "'";
				in += "," + DBKernel.delimitL(feldnames[i]);
				iv += ",'" + feldVals[i] + "'";
				if (feldnames[i].equalsIgnoreCase("Serial")) serialWhere = "UCASE(" + DBKernel.delimitL(feldnames[i]) + ")='" + feldVals[i].toUpperCase() + "'";
			}
		}
		int intId = 0;
		try {
			intId = Integer.parseInt(feldVals[feldVals.length-1]);
			int numIdsPresent = DBKernel.getRowCount("Station", " WHERE " + DBKernel.delimitL("ID") + "=" + intId);
			if (numIdsPresent == 0) {
				in += "," + DBKernel.delimitL("ID");
				iv += "," + intId;
			}
		}
		catch (Exception e) {}

		ResultSet rs = DBKernel.getResultSet(sql, false);

		if (rs != null && rs.first()) {
			result = rs.getInt(1);
		}

		if (result != null) {
			sql = "UPDATE " + DBKernel.delimitL("Station") + " SET " + DBKernel.delimitL("ImportSources") + "=CASEWHEN(INSTR(';" + miDbId + ";'," + DBKernel.delimitL("ImportSources") + ")=0,CONCAT(" + DBKernel.delimitL("ImportSources") + ", '" + miDbId + ";'), " + DBKernel.delimitL("ImportSources") + ") WHERE " + DBKernel.delimitL("ID") + "=" + result;
			DBKernel.sendRequest(sql, false);
		}
		else if (!iv.isEmpty()) {
			sql = "INSERT INTO " + DBKernel.delimitL("Station") + " (" + in + ") VALUES (" + iv + ")";
			PreparedStatement ps = DBKernel.getDBConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			//System.err.println(in + "\t" + iv);
			try {
				if (ps.executeUpdate() > 0) {
					result = DBKernel.getLastInsertedID(ps);
					if (serialWhere.length() == 0) {
						DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Station") + " SET " + DBKernel.delimitL("Serial") + "=" + DBKernel.delimitL("ID") + " WHERE " + DBKernel.delimitL("ID") + "=" + result, false);
						serialWhere = "UCASE(" + DBKernel.delimitL("Serial") + ")='" + result + "'";
					}
					int numSameSerials = DBKernel.getRowCount("Station", " WHERE " + serialWhere);
					if (numSameSerials > 1) {
						DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Station") + " SET " + DBKernel.delimitL("Serial") + "=CONCAT(" + DBKernel.delimitL("Serial") + ",'_" + result + "') WHERE " + DBKernel.delimitL("ID") + "=" + result, false);					
					}
					/*
					while (true) {
						int numSameSerials = DBKernel.getRowCount("Station", serialWhere);
						if (numSameSerials == 1) break;
						DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Station") + " SET " + DBKernel.delimitL("Serial") + "=CONCAT(" + DBKernel.delimitL("Serial") + ",'_','" + numSameSerials + "') WHERE " + DBKernel.delimitL("ID") + "=" + result, false);
						serialWhere = serialWhere.substring(0, serialWhere.lastIndexOf("'")) + "_" + numSameSerials + "'";
					}
					*/
				}
			}
			catch (SQLException e) {
				if (e.getMessage().startsWith("integrity constraint violation")) throw new Exception("Station ID " + intId + " ist bereits vergeben");
				else throw e;
			}
		}

		return result;
	}
}
