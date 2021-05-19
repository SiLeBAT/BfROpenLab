/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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

public class MyKrisenInterfacesSettings extends NodeSettings {

	private static final String CFG_LOT_BASED = "LotBased";
	private static final String CFG_ENSURE_BACKWARD_COMPATIBILITY = "EnsureBackwardCompatibility";
	private static final String CFG_ANONYMIZE = "anonymize";
	private static final String CFG_USE_EXTERNAL_DB = "override";
	private static final String CFG_DB_PATH = "filename";

	private boolean lotBased;
	private boolean ensureBackwardCompatibility;
	private boolean anonymize;
	private boolean useExternalDb;
	private String dbPath;

	public MyKrisenInterfacesSettings() {
		lotBased = false;
		ensureBackwardCompatibility = false;
		anonymize = false;
		useExternalDb = false;
		dbPath = null;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			lotBased = settings.getBoolean(CFG_LOT_BASED);
		} catch (InvalidSettingsException e) {
		}

		try {
			ensureBackwardCompatibility = settings.getBoolean(CFG_ENSURE_BACKWARD_COMPATIBILITY);
		} catch (InvalidSettingsException e) {
		}

		try {
			anonymize = settings.getBoolean(CFG_ANONYMIZE);
		} catch (InvalidSettingsException e) {
		}

		try {
			useExternalDb = settings.getBoolean(CFG_USE_EXTERNAL_DB);
		} catch (InvalidSettingsException e) {
		}

		try {
			dbPath = settings.getString(CFG_DB_PATH);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addBoolean(CFG_LOT_BASED, lotBased);
		settings.addBoolean(CFG_ENSURE_BACKWARD_COMPATIBILITY, ensureBackwardCompatibility);
		settings.addBoolean(CFG_ANONYMIZE, anonymize);
		settings.addBoolean(CFG_USE_EXTERNAL_DB, useExternalDb);
		settings.addString(CFG_DB_PATH, dbPath);
	}

	public boolean isLotBased() {
		return lotBased;
	}

	public void setLotBased(boolean lotBased) {
		this.lotBased = lotBased;
	}

	public boolean isEnsureBackwardCompatibility() {
		return ensureBackwardCompatibility;
	}

	public void setEnsureBackwardCompatibility(boolean ensureBackwardCompatibility) {
		this.ensureBackwardCompatibility = ensureBackwardCompatibility;
	}

	public boolean isAnonymize() {
		return anonymize;
	}

	public void setAnonymize(boolean anonymize) {
		this.anonymize = anonymize;
	}

	public boolean isUseExternalDb() {
		return useExternalDb;
	}

	public void setUseExternalDb(boolean useExternalDb) {
		this.useExternalDb = useExternalDb;
	}

	public String getDbPath() {
		return dbPath;
	}

	public void setDbPath(String dbPath) {
		this.dbPath = dbPath;
	}
}
