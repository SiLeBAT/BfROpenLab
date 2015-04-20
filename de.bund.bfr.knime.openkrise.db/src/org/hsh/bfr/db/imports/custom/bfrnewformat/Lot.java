package org.hsh.bfr.db.imports.custom.bfrnewformat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map.Entry;

import org.hsh.bfr.db.DBKernel;

public class Lot {

	private static HashMap<String, Lot> gathereds = new HashMap<>();
	private HashMap<String, String> flexibles = new HashMap<>();

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
		gathereds.put(number, this);
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
	public Integer getProductionDay() {
		return productionDay;
	}
	public void setProductionDay(Integer productionDay) {
		this.productionDay = productionDay;
	}
	public Integer getProductionMonth() {
		return productionMonth;
	}
	public void setProductionMonth(Integer productionMonth) {
		this.productionMonth = productionMonth;
	}
	public Integer getProductionYear() {
		return productionYear;
	}
	public void setProductionYear(Integer productionYear) {
		this.productionYear = productionYear;
	}
	public Integer getExpiryDay() {
		return expiryDay;
	}
	public void setExpiryDay(Integer expiryDay) {
		this.expiryDay = expiryDay;
	}
	public Integer getExpiryMonth() {
		return expiryMonth;
	}
	public void setExpiryMonth(Integer expiryMonth) {
		this.expiryMonth = expiryMonth;
	}
	public Integer getExpiryYear() {
		return expiryYear;
	}
	public void setExpiryYear(Integer expiryYear) {
		this.expiryYear = expiryYear;
	}
	public String getSampling() {
		return sampling;
	}
	public void setSampling(String sampling) {
		this.sampling = sampling;
	}
	private String number;
	private Double unitNumber;
	private String unitUnit;
	private Integer productionDay;
	private Integer productionMonth;
	private Integer productionYear;
	private Integer expiryDay;
	private Integer expiryMonth;
	private Integer expiryYear;
	private String sampling; // -> flexible table
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
		if (number != null && !number.isEmpty() && gathereds.get(number) != null && gathereds.get(number).getDbId() != null) dbId = gathereds.get(number).getDbId();
		if (dbId != null) return dbId;
		Integer retId = getID(product,new String[]{"pd_day","pd_month","pd_year","MHD_day","MHD_month","MHD_year","Menge","ChargenNr","Einheit"},
				new Integer[]{productionDay,productionMonth,productionYear,expiryDay,expiryMonth,expiryYear}, unitNumber, new String[]{number,unitUnit}, miDbId);
		dbId = retId;
		if (number != null && !number.isEmpty() && gathereds.get(number) != null) gathereds.get(number).setDbId(dbId);
		if (retId != null) {
			if (sampling != null && !sampling.isEmpty()) DBKernel.sendRequest("INSERT INTO " + DBKernel.delimitL("ExtraFields") +
					" (" + DBKernel.delimitL("tablename") + "," + DBKernel.delimitL("id") + "," + DBKernel.delimitL("attribute") + "," + DBKernel.delimitL("value") +
					") VALUES ('Chargen'," + retId + ",'Sampling','" + sampling + "')", false);

			// Further flexible cells
			for (Entry<String, String> es : flexibles.entrySet()) {
				DBKernel.sendRequest("INSERT INTO " + DBKernel.delimitL("ExtraFields") +
						" (" + DBKernel.delimitL("tablename") + "," + DBKernel.delimitL("id") + "," + DBKernel.delimitL("attribute") + "," + DBKernel.delimitL("value") +
						") VALUES ('Chargen'," + retId + ",'" + es.getKey() + "','" + es.getValue() + "')", false);
			}
		}
		return retId;
	}
	private Integer getID(Product product, String[] feldnames, Integer[] iFeldVals, Double unitNumber, String[] sFeldVals, Integer miDbId) throws Exception {
		Integer dbProdID = product.getID(miDbId);
		if (!product.getLogMessages().isEmpty()) logMessages += product.getLogMessages() + "\n";
		if (dbProdID == null) {
			logMessages += "Product unknown...\n";
			return null;
		}

		Integer result = null;
		String sql = "SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL("Chargen") +
			" WHERE " + DBKernel.delimitL("Artikel") + "=" + dbProdID;
		String in = DBKernel.delimitL("Artikel") + "," + DBKernel.delimitL("ImportSources");
		String iv = dbProdID + ",';" + miDbId + ";'";
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
				sql += " AND UCASE(" + DBKernel.delimitL(feldnames[i+1+j]) + ")='" + sFeldVals[j].toUpperCase() + "'";
				in += "," + DBKernel.delimitL(feldnames[i+1+j]);
				iv += ",'" + sFeldVals[j] + "'";
			}
		}

		ResultSet rs = DBKernel.getResultSet(sql, false);

		if (rs != null && rs.first()) {
			result = rs.getInt(1);
		}

		if (result != null) {
			DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Chargen") + " SET " + DBKernel.delimitL("ImportSources") + "=CASEWHEN(INSTR(';" + miDbId + ";'," + DBKernel.delimitL("ImportSources") + ")=0,CONCAT(" + DBKernel.delimitL("ImportSources") + ", '" + miDbId + ";'), " + DBKernel.delimitL("ImportSources") + ") WHERE " + DBKernel.delimitL("ID") + "=" + result, false);
		}
		else if (!iv.isEmpty()) {
			sql = "INSERT INTO " + DBKernel.delimitL("Chargen") + " (" + in + ") VALUES (" + iv + ")";
			PreparedStatement ps = DBKernel.getDBConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			if (ps.executeUpdate() > 0) result = DBKernel.getLastInsertedID(ps);
		}


		return result;
	}	
}
