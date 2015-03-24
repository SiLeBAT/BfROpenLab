/*******************************************************************************
 * PMM-Lab � 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab � 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * Christian Th�ns (BfR)
 * Matthias Filter (BfR)
 * Armin A. Weiser (BfR)
 * Alexander Falenski (BfR)
 * J�rgen Brandt (BfR)
 * Annemarie K�sbohrer (BfR)
 * Bernd Appel (BfR)
 * 
 * PMM-Lab is a project under development. Contributions are welcome.
 * 
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
/*
// For Standalone-App compilation
package org.hsh.bfr.db;

import org.hsh.bfr.db.gui.Login;

public class MyPreferences {

	public java.util.prefs.Preferences prefsReg = java.util.prefs.Preferences.userNodeForPackage(Login.class);

	public void prefsFlush() {
	}

	public void put(String key, String value) {
		prefsReg.put(key, value);
	}

	public String get(String key, String defaultValue) {
		return prefsReg.get(key, defaultValue);
	}

	public void putBoolean(String key, boolean value) {
		prefsReg.putBoolean(key, value);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		return prefsReg.getBoolean(key, defaultValue);
	}
}
/*
Au�erdem:
- DBKernel, Zeile 1716 weg "bundle = FrameworkUtil.getBundle(DBKernel.class);"
- DBKernel.debug = false
- alle DBKernels weg in MyTrigger f�r Server.jar!!!
- zum debuggen auf der commandline app starten mit: 'java -Xms512m -Xmx768m -jar "C:\Dokumente und Einstellungen\Weiser\Lokale Einstellungen\Anwendungsdaten\SiLeBAT-DB\SiLeBAT_1.7.9.jar"'
*/

package org.hsh.bfr.db;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.hsh.bfr.db.gui.Login;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class MyPreferences {

	private java.util.prefs.Preferences prefsReg = java.util.prefs.Preferences.userNodeForPackage(Login.class);
	private Preferences preferences = InstanceScope.INSTANCE.getNode("org.hsh.bfr.db");
	private Preferences prefs = preferences.node("db");

	public void prefsFlush() {
		if (DBKernel.isKNIME) {
			try {
				preferences.flush();
			}
			catch (BackingStoreException e) {
				e.printStackTrace();
			}		  
		}
	}

	public void put(String key, String value) {
		if (DBKernel.isKNIME) prefs.put(key, value);
		else prefsReg.put(key, value);
	}

	public String get(String key, String defaultValue) {
		if (DBKernel.isKNIME) return prefs.get(key, defaultValue);
		else return prefsReg.get(key, defaultValue);
	}

	public void putBoolean(String key, boolean value) {
		if (DBKernel.isKNIME) prefs.putBoolean(key, value);
		else prefsReg.putBoolean(key, value);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		if (DBKernel.isKNIME) return prefs.getBoolean(key, defaultValue);
		else return prefsReg.getBoolean(key, defaultValue);
	}
}