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
package de.bund.bfr.knime.openkrise.out;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.NodeSettings;

public class TracingXmlOutNodeSettings extends NodeSettings {

	private static final String CFG_SERVER = "server";
	private static final String CFG_USER = "user";
	private static final String CFG_PASS = "path";

	private String server, user, pass;

	public TracingXmlOutNodeSettings() {
		server = null;
		user = null;
		pass = null;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			server = settings.getString(CFG_SERVER);
		} catch (InvalidSettingsException e) {
		}

		try {
			user = settings.getString(CFG_USER);
		} catch (InvalidSettingsException e) {
		}

		try {
			pass = settings.getString(CFG_PASS);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addString(CFG_SERVER, server);
		settings.addString(CFG_USER, user);
		settings.addString(CFG_PASS, pass);
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}
}
