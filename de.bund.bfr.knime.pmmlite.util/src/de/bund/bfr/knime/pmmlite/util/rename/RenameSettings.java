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
package de.bund.bfr.knime.pmmlite.util.rename;

import java.util.LinkedHashMap;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.pmmlite.core.XmlUtils;

public class RenameSettings {

	private static final String CFG_RENAMINGS = "Renamings";

	private Map<String, String> renamings;

	public RenameSettings() {
		renamings = new LinkedHashMap<>();
	}

	@SuppressWarnings("unchecked")
	public void loadSettings(NodeSettingsRO settings) {
		try {
			renamings = (Map<String, String>) XmlUtils.fromXml(settings.getString(CFG_RENAMINGS));
		} catch (InvalidSettingsException e) {
		}
	}

	public void saveSettings(NodeSettingsWO settings) {
		settings.addString(CFG_RENAMINGS, XmlUtils.toXml(renamings));
	}

	public Map<String, String> getRenamings() {
		return renamings;
	}

	public void setRenamings(Map<String, String> renamings) {
		this.renamings = renamings;
	}
}
