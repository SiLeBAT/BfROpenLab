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
package de.bund.bfr.knime.openkrise;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.NodeSettings;

public class MyKrisenInterfacesXmlSettings extends NodeSettings {

	private static final String CFG_ANONYMIZE = "anonymize";
	private static final String CFG_XML_PATH = "xmlPath";

	private boolean anonymize;
	private String xmlPath;

	public MyKrisenInterfacesXmlSettings() {
		anonymize = false;
		xmlPath = null;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			anonymize = settings.getBoolean(CFG_ANONYMIZE);
		} catch (InvalidSettingsException e) {
		}

		try {
			xmlPath = settings.getString(CFG_XML_PATH);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addBoolean(CFG_ANONYMIZE, anonymize);
		settings.addString(CFG_XML_PATH, xmlPath);
	}

	public boolean isAnonymize() {
		return anonymize;
	}

	public void setAnonymize(boolean anonymize) {
		this.anonymize = anonymize;
	}

	public String getXmlPath() {
		return xmlPath;
	}

	public void setXmlPath(String xmlPath) {
		this.xmlPath = xmlPath;
	}
}
