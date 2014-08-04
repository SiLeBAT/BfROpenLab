/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
 ******************************************************************************/
package de.bund.bfr.knime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public abstract class NodeSettings {

	public abstract void loadSettings(NodeSettingsRO settings);

	public abstract void saveSettings(NodeSettingsWO settings);

	public String toXml() throws IOException {
		org.knime.core.node.NodeSettings settings = new org.knime.core.node.NodeSettings(
				"ID");

		saveSettings(settings);

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		settings.saveToXML(out);

		return out.toString();
	}

	public void loadFromXml(String xml) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());

		loadSettings(org.knime.core.node.NodeSettings.loadFromXML(in));
	}
}
