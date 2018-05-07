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
package de.bund.bfr.knime.openkrise;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.NodeSettings;

public class FCL_DB_WriterSettings extends NodeSettings {

	private static final String CFG_CLEAR_DB = "ClearDB";

	private boolean clearDB;

	public FCL_DB_WriterSettings() {
		clearDB = false;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			clearDB = settings.getBoolean(CFG_CLEAR_DB);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addBoolean(CFG_CLEAR_DB, clearDB);
	}

	public boolean isClearDB() {
		return clearDB;
	}

	public void setClearDB(boolean clearDB) {
		this.clearDB = clearDB;
	}

}
