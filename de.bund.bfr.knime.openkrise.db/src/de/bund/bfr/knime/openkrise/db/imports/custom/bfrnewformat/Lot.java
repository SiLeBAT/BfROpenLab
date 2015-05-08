package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map.Entry;

import de.bund.bfr.knime.openkrise.db.DBKernel;

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
	private String number;
	private Double unitNumber;
	private String unitUnit;
	private Integer dbId;
	private String lookup;
	
	public String getLookup() {
		return lookup;
	}
	public void setLookup(String lookup) {
		this.lookup = lookup;
	}
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
		if (number == null) logMessages += "Please, do always provide a lot number as this is most helpful!";//throw new Exception("Lot number not defined");
		if (number != null && !number.isEmpty() && gathereds.get(number) != null && gathereds.get(number).getDbId() != null) dbId = gathereds.get(number).getDbId();
		if (dbId != null) return dbId;
		Integer retId = getID(product,number,unitNumber,unitUnit, miDbId);
		dbId = retId;
		if (number != null && !number.isEmpty() && gathereds.get(number) != null) gathereds.get(number).setDbId(dbId);
		if (retId != null) {
			// Further flexible cells
			for (Entry<String, String> es : flexibles.entrySet()) {
				DBKernel.sendRequest("INSERT INTO " + DBKernel.delimitL("ExtraFields") +
						" (" + DBKernel.delimitL("tablename") + "," + DBKernel.delimitL("id") + "," + DBKernel.delimitL("attribute") + "," + DBKernel.delimitL("value") +
						") VALUES ('Chargen'," + retId + ",'" + es.getKey() + "','" + es.getValue() + "')", false);
			}
		}
		return retId;
	}
	private Integer getID(Product product, String number, Double unitNumber, String unitUnit, Integer miDbId) throws Exception {
		Integer dbProdID = product.getID(miDbId);
		if (!product.getLogMessages().isEmpty()) logMessages += product.getLogMessages() + "\n";
		if (dbProdID == null) {
			logMessages += "Product unknown...\n";
			return null;
		}

		Integer result = null;
		String sql = "SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL("Chargen") +
				" WHERE " + DBKernel.delimitL("Artikel") + "=" + dbProdID + " AND " + DBKernel.delimitL("ChargenNr") + "='" + number + "'";
		String in = DBKernel.delimitL("Artikel") + "," + DBKernel.delimitL("ImportSources");
		String iv = dbProdID + ",';" + miDbId + ";'";
		if (number != null) {
			in += "," + DBKernel.delimitL("ChargenNr");
			iv += ",'" + number + "'";
		}
		if (unitNumber != null) {
			//sql += " AND " + DBKernel.delimitL("Menge") + "=" + unitNumber + "";
			in += "," + DBKernel.delimitL("Menge");
			String un = ("" + unitNumber).replace(",", ".");
			iv += "," + un;
		}
		if (unitUnit != null) {
			//sql += " AND UCASE(" + DBKernel.delimitL("Einheit") + ")='" + unitUnit.toUpperCase() + "'";
			in += "," + DBKernel.delimitL("Einheit");
			iv += ",'" + unitUnit + "'";
		}

		if (number != null) {
			ResultSet rs = DBKernel.getResultSet(sql, false);
			if (rs != null && rs.first()) {
				result = rs.getInt(1);
			}
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
