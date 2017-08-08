/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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

	private static final String CFG_BUSSTOP = "busstop";
	private static final String CFG_ANONYMIZE = "anonymize";
	private static final String CFG_XML_PATH = "xmlPath";
	private static final String CFG_SERVER = "server";
	private static final String CFG_USER = "user";
	private static final String CFG_PASS = "path";
	private static final String CFG_ENVIRONMENT = "environment";
	private static final String CFG_CASE = "case";

	private boolean anonymize, busstop;
	private String xmlPath, server, user, pass, caseNumber, environment;

	public MyKrisenInterfacesXmlSettings() {
		busstop = false;
		anonymize = false;
		xmlPath = null;
		server = null;
		user = null;
		pass = null;
		caseNumber = null;
		environment = null;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			busstop = settings.getBoolean(CFG_BUSSTOP);
		} catch (InvalidSettingsException e) {
		}

		try {
			anonymize = settings.getBoolean(CFG_ANONYMIZE);
		} catch (InvalidSettingsException e) {
		}

		try {
			xmlPath = settings.getString(CFG_XML_PATH);
		} catch (InvalidSettingsException e) {
		}

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
		try {
			environment = settings.getString(CFG_ENVIRONMENT);
		} catch (InvalidSettingsException e) {
		}
		try {
			caseNumber = settings.getString(CFG_CASE);
		} catch (InvalidSettingsException e) {
		}

	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addBoolean(CFG_BUSSTOP, busstop);
		settings.addBoolean(CFG_ANONYMIZE, anonymize);
		settings.addString(CFG_XML_PATH, xmlPath);
		settings.addString(CFG_SERVER, server);
		settings.addString(CFG_USER, user);
		settings.addString(CFG_PASS, pass);
		settings.addString(CFG_ENVIRONMENT, environment);
		settings.addString(CFG_CASE, caseNumber);
	}

	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		String error = "";
		String s;
		boolean b = settings == null ? busstop : settings.getBoolean(CFG_BUSSTOP);
		if (b) {
			s = settings == null ? server : settings.getString(CFG_SERVER);
			if (s == null || s.trim().isEmpty()) error += "Server address not defined\n";
			s = settings == null ? environment : settings.getString(CFG_ENVIRONMENT);
			if (s == null || s.trim().isEmpty()) error += "Client ID not defined\n";
		}
		else {
			s = settings == null ? xmlPath : settings.getString(CFG_XML_PATH);
			if (s == null || s.trim().isEmpty()) error += "Xml path not defined\n";
		}
		if (!error.trim().isEmpty()) throw new InvalidSettingsException(error.trim());
	}

	public boolean isAnonymize() {
		return anonymize;
	}

	public void setAnonymize(boolean anonymize) {
		this.anonymize = anonymize;
	}

	public boolean isBusstop() {
		return busstop;
	}

	public void setBusstop(boolean busstop) {
		this.busstop = busstop;
	}

	public String getXmlPath() {
		return xmlPath;
	}

	public void setXmlPath(String xmlPath) {
		this.xmlPath = xmlPath;
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

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getCaseNumber() {
		return caseNumber;
	}

	public void setCaseNumber(String caseNumber) {
		this.caseNumber = caseNumber;
	}
}
