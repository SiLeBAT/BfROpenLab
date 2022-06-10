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
package de.bund.bfr.knime.openkrise;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.NodeSettings;

public class FCL_DB_WriterSettings extends NodeSettings {

	private static final String CFG_CLEAR_DB = "ClearDB";
	
	private static final String CFG_SID_DB = "SID";
	private static final String CFG_SNAME_DB = "SNAME";
	private static final String CFG_SADDRESS_DB = "SADDRESS";
	private static final String CFG_SCOUNTRY_DB = "SCOUNTRY";
	private static final String CFG_STOB_DB = "STOB";
	
	private static final String CFG_DID_DB = "DID";
	private static final String CFG_DNAME_DB = "DNAME";
	private static final String CFG_DEAN_DB = "DEAN";
	private static final String CFG_DFROM_DB = "DFROM";
	private static final String CFG_DTO_DB = "DTO";
	private static final String CFG_DLOT_DB = "DLOT";
	private static final String CFG_DBESTBEFORE_DB = "DBESTBEFORE";
	private static final String CFG_DDDD_DB = "DDDD";
	private static final String CFG_DDDM_DB = "DDDM";
	private static final String CFG_DDDY_DB = "DDDY";
	private static final String CFG_DAMOUNT_DB = "DAMOUNT";
	private static final String CFG_DCOMMENT_DB = "DCOMMENT";
	
	private static final String CFG_TFROM_DB = "TFROM";
	private static final String CFG_TTO_DB = "TTO";

	private boolean clearDB;
	
	private String dbSId = "ID";
	private String dbSName = "Name";
	private String dbSAddress = "Address";
	private String dbSCountry = "Country";
	private String dbSTOB = "type of business";
	
	private String dbDId = "ID";
	private String dbDFrom = "from";
	private String dbDTo = "to";
	private String dbDName = "Name";
	private String dbDEAN = "EAN";
	private String dbDLot = "Lot Number";
	private String dbDBestBefore = "BestBefore";
	private String dbDDDD = "Date Delivery - Day";
	private String dbDDDM = "Date Delivery - Month";
	private String dbDDDY = "Date Delivery - Year";
	private String dbDAmount = "Amount";
	private String dbDComment = "Comment";

	private String dbTFrom = "from";
	private String dbTTo = "to";

	public FCL_DB_WriterSettings() {
		clearDB = false;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			clearDB = settings.getBoolean(CFG_CLEAR_DB);
		} catch (InvalidSettingsException e) {}
		
		try {
			dbSId = settings.getString(CFG_SID_DB);
		} catch (InvalidSettingsException e) {}
		try {
			dbSName = settings.getString(CFG_SNAME_DB);
		} catch (InvalidSettingsException e) {}
		try {
			dbSAddress = settings.getString(CFG_SADDRESS_DB);
		} catch (InvalidSettingsException e) {}
		try {
			dbSCountry = settings.getString(CFG_SCOUNTRY_DB);
		} catch (InvalidSettingsException e) {}
		try {
			dbSTOB = settings.getString(CFG_STOB_DB);
		} catch (InvalidSettingsException e) {}
		
		try {
			dbDId = settings.getString(CFG_DID_DB);
		} catch (InvalidSettingsException e) {}
		try {
			dbDFrom = settings.getString(CFG_DFROM_DB);
		} catch (InvalidSettingsException e) {}
		try {
			dbDTo = settings.getString(CFG_DTO_DB);
		} catch (InvalidSettingsException e) {}
		try {
			dbDName = settings.getString(CFG_DNAME_DB);
		} catch (InvalidSettingsException e) {}
		try {
			dbDEAN = settings.getString(CFG_DEAN_DB);
		} catch (InvalidSettingsException e) {}
		try {
			dbDLot = settings.getString(CFG_DLOT_DB);
		} catch (InvalidSettingsException e) {}
		try {
			dbDBestBefore = settings.getString(CFG_DBESTBEFORE_DB);
		} catch (InvalidSettingsException e) {}
		try {
			dbDDDD = settings.getString(CFG_DDDD_DB);
		} catch (InvalidSettingsException e) {}
		try {
			dbDDDM = settings.getString(CFG_DDDM_DB);
		} catch (InvalidSettingsException e) {}
		try {
			dbDDDY = settings.getString(CFG_DDDY_DB);
		} catch (InvalidSettingsException e) {}
		try {
			dbDAmount = settings.getString(CFG_DAMOUNT_DB);
		} catch (InvalidSettingsException e) {}
		try {
			dbDComment = settings.getString(CFG_DCOMMENT_DB);
		} catch (InvalidSettingsException e) {}
		
		try {
			dbTFrom = settings.getString(CFG_TFROM_DB);
		} catch (InvalidSettingsException e) {}
		try {
			dbTTo = settings.getString(CFG_TTO_DB);
		} catch (InvalidSettingsException e) {}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addBoolean(CFG_CLEAR_DB, clearDB);
		
		settings.addString(CFG_SID_DB, dbSId);
		settings.addString(CFG_SNAME_DB, dbSName);
		settings.addString(CFG_SADDRESS_DB, dbSAddress);
		settings.addString(CFG_SCOUNTRY_DB, dbSCountry);
		settings.addString(CFG_STOB_DB, dbSTOB);
		
		settings.addString(CFG_DID_DB, dbDId);
		settings.addString(CFG_DFROM_DB, dbDFrom);
		settings.addString(CFG_DTO_DB, dbDTo);
		settings.addString(CFG_DNAME_DB, dbDName);
		settings.addString(CFG_DEAN_DB, dbDEAN);
		settings.addString(CFG_DLOT_DB, dbDLot);
		settings.addString(CFG_DBESTBEFORE_DB, dbDBestBefore);
		settings.addString(CFG_DDDD_DB, dbDDDD);
		settings.addString(CFG_DDDM_DB, dbDDDM);
		settings.addString(CFG_DDDY_DB, dbDDDY);
		settings.addString(CFG_DAMOUNT_DB, dbDAmount);
		settings.addString(CFG_DCOMMENT_DB, dbDComment);
		
		settings.addString(CFG_TFROM_DB, dbTFrom);
		settings.addString(CFG_TTO_DB, dbTTo);
	}

	public boolean isClearDB() {
		return clearDB;
	}

	public void setClearDB(boolean clearDB) {
		this.clearDB = clearDB;
	}

	public String getDbSId() {
		return dbSId;
	}

	public void setDbSId(String dbSId) {
		this.dbSId = dbSId;
	}

	public String getDbSName() {
		return dbSName;
	}

	public void setDbSName(String dbSName) {
		this.dbSName = dbSName;
	}

	public String getDbSAddress() {
		return dbSAddress;
	}

	public void setDbSAddress(String dbSAddress) {
		this.dbSAddress = dbSAddress;
	}

	public String getDbSCountry() {
		return dbSCountry;
	}

	public void setDbSCountry(String dbSCountry) {
		this.dbSCountry = dbSCountry;
	}

	public String getDbSTOB() {
		return dbSTOB;
	}

	public void setDbSTOB(String dbSTOB) {
		this.dbSTOB = dbSTOB;
	}

	public String getDbDId() {
		return dbDId;
	}

	public void setDbDId(String dbDId) {
		this.dbDId = dbDId;
	}

	public String getDbDFrom() {
		return dbDFrom;
	}

	public void setDbDFrom(String dbDFrom) {
		this.dbDFrom = dbDFrom;
	}

	public String getDbDTo() {
		return dbDTo;
	}

	public void setDbDTo(String dbDTo) {
		this.dbDTo = dbDTo;
	}

	public String getDbDName() {
		return dbDName;
	}

	public void setDbDName(String dbDName) {
		this.dbDName = dbDName;
	}

	public String getDbDEAN() {
		return dbDEAN;
	}

	public void setDbDEAN(String dbDEAN) {
		this.dbDEAN = dbDEAN;
	}

	public String getDbDLot() {
		return dbDLot;
	}

	public void setDbDLot(String dbDLot) {
		this.dbDLot = dbDLot;
	}

	public String getDbDBestBefore() {
		return dbDBestBefore;
	}

	public void setDbDBestBefore(String dbDBestBefore) {
		this.dbDBestBefore = dbDBestBefore;
	}

	public String getDbDDDD() {
		return dbDDDD;
	}

	public void setDbDDDD(String dbDDDD) {
		this.dbDDDD = dbDDDD;
	}

	public String getDbDDDM() {
		return dbDDDM;
	}

	public void setDbDDDM(String dbDDDM) {
		this.dbDDDM = dbDDDM;
	}

	public String getDbDDDY() {
		return dbDDDY;
	}

	public void setDbDDDY(String dbDDDY) {
		this.dbDDDY = dbDDDY;
	}

	public String getDbDAmount() {
		return dbDAmount;
	}

	public void setDbDAmount(String dbDAmount) {
		this.dbDAmount = dbDAmount;
	}

	public String getDbDComment() {
		return dbDComment;
	}

	public void setDbDComment(String dbDComment) {
		this.dbDComment = dbDComment;
	}

	public String getDbTTo() {
		return dbTTo;
	}

	public void setDbTTo(String dbTTo) {
		this.dbTTo = dbTTo;
	}

	public String getDbTFrom() {
		return dbTFrom;
	}

	public void setDbTFrom(String dbTFrom) {
		this.dbTFrom = dbTFrom;
	}

}
