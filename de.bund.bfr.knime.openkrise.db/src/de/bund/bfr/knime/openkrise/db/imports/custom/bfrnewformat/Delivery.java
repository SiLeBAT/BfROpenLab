package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map.Entry;

import de.bund.bfr.knime.openkrise.db.DBKernel;

public class Delivery {

	public static HashMap<String, Delivery> gathereds = new HashMap<>();
	private HashMap<String, String> flexibles = new HashMap<>();

	public void addFlexibleField(String key, String value) {
		flexibles.put(key, value);
	}
	private Lot lot;
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
		gathereds.put(id, this);
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
	private String targetLotId;
	public String getTargetLotId() {
		return targetLotId;
	}
	public void setTargetLotId(String targetLotId) {
		this.targetLotId = targetLotId;
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
		if (gathereds.get(id) != null && gathereds.get(id).getDbId() != null) dbId = gathereds.get(id).getDbId();
		if (dbId != null) return dbId;
		Integer retId = getID(lot,receiver,new String[]{"dd_day","dd_month","dd_year","ad_day","ad_month","ad_year","numPU","typePU","Serial"}, // "Charge","Empfänger",
				new Integer[]{departureDay,departureMonth,departureYear,arrivalDay,arrivalMonth,arrivalYear}, unitNumber, new String[]{unitUnit,id}, miDbId);
		dbId = retId;
		if (gathereds.get(id) != null) gathereds.get(id).setDbId(dbId);
		
		// Further flexible cells
		if (retId != null) {
			for (Entry<String, String> es : flexibles.entrySet()) {
				DBKernel.sendRequest("INSERT INTO " + DBKernel.delimitL("ExtraFields") +
						" (" + DBKernel.delimitL("tablename") + "," + DBKernel.delimitL("id") + "," + DBKernel.delimitL("attribute") + "," + DBKernel.delimitL("value") +
						") VALUES ('Lieferungen'," + retId + ",'" + es.getKey() + "','" + es.getValue() + "')", false);
			}
		}
		return retId;
	}
	private Integer getID(Lot lot, Station receiver, String[] feldnames, Integer[] iFeldVals, Double unitNumber, String[] sFeldVals, Integer miDbId) throws Exception {
		Integer dbRecID = receiver.getID(miDbId);
		if (!receiver.getLogMessages().isEmpty()) logMessages += receiver.getLogMessages() + "\n";
		if (dbRecID == null) {
			logMessages += "Receiver unknown...\n";
			return null;
		}
		Integer dbLotID = lot.getID(miDbId);
		if (!lot.getLogMessages().isEmpty()) logMessages += lot.getLogMessages() + "\n";
		if (dbLotID == null) {
			logMessages += "Lot unknown...\n";
			return null;
		}
		Integer result = null;
		String sql = "SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL("Lieferungen") +
				" WHERE " + DBKernel.delimitL("Charge") + "=" + dbLotID + " AND " + DBKernel.delimitL("Empfänger") + "=" + dbRecID;
		String in = DBKernel.delimitL("Charge") + "," + DBKernel.delimitL("Empfänger") + "," + DBKernel.delimitL("ImportSources");
		String iv = dbLotID + "," + dbRecID + ",';" + miDbId + ";'";
		String serialWhere = "";
		int i=0;
		for (;i<iFeldVals.length;i++) {
			if (iFeldVals[i] != null) {
				sql += " AND " + DBKernel.delimitL(feldnames[i]) + "=" + iFeldVals[i] + "";
				in += "," + DBKernel.delimitL(feldnames[i]);
				iv += "," + iFeldVals[i];
			}
		}
		if (unitNumber != null) {
			sql += " AND " + DBKernel.delimitL(feldnames[i]) + "=" + unitNumber + "";
			in += "," + DBKernel.delimitL(feldnames[i]);
			String un = ("" + unitNumber).replace(",", ".");
			iv += "," + un;
		}
		for (int j=0;j<sFeldVals.length;j++) {
			if (sFeldVals[j] != null) {
				//if (!feldnames[i+1+j].equals("Serial"))
				sql += " AND UCASE(" + DBKernel.delimitL(feldnames[i+1+j]) + ")='" + sFeldVals[j].toUpperCase() + "'";
				in += "," + DBKernel.delimitL(feldnames[i+1+j]);
				iv += ",'" + sFeldVals[j] + "'";
				if (feldnames[i+1+j].equalsIgnoreCase("Serial")) serialWhere = "UCASE(" + DBKernel.delimitL(feldnames[i+1+j]) + ")='" + sFeldVals[j].toUpperCase() + "'";
			}
		}

		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
			result = rs.getInt(1);
		}
		
		if (result != null) {
			DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Lieferungen") + " SET " + DBKernel.delimitL("ImportSources") + "=CASEWHEN(INSTR(';" + miDbId + ";'," + DBKernel.delimitL("ImportSources") + ")=0,CONCAT(" + DBKernel.delimitL("ImportSources") + ", '" + miDbId + ";'), " + DBKernel.delimitL("ImportSources") + ") WHERE " + DBKernel.delimitL("ID") + "=" + result, false);
		}
		else if (!iv.isEmpty()) {
			sql = "INSERT INTO " + DBKernel.delimitL("Lieferungen") + " (" + in + ") VALUES (" + iv + ")";
			PreparedStatement ps = DBKernel.getDBConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			if (ps.executeUpdate() > 0) {
				result = DBKernel.getLastInsertedID(ps);
				if (serialWhere.length() == 0) {
					DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Lieferungen") + " SET " + DBKernel.delimitL("Serial") + "=" + DBKernel.delimitL("ID") + " WHERE " + DBKernel.delimitL("ID") + "=" + result, false);
					serialWhere = "UCASE(" + DBKernel.delimitL("Serial") + ")='" + result + "'";
				}
				int numSameSerials = DBKernel.getRowCount("Lieferungen", serialWhere);
				if (numSameSerials > 1) {
					DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Lieferungen") + " SET " + DBKernel.delimitL("Serial") + "=CONCAT(" + DBKernel.delimitL("Serial") + ",'_" + result + "') WHERE " + DBKernel.delimitL("ID") + "=" + result, false);					
				}
			}
		}

		return result;
	}
}
