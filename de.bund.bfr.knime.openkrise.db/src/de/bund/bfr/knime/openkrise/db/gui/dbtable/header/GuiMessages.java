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
package de.bund.bfr.knime.openkrise.db.gui.dbtable.header;

import java.util.ResourceBundle;

import de.bund.bfr.knime.openkrise.db.DBKernel;

public class GuiMessages {
	private static final String BUNDLE_NAME = "de.bund.bfr.knime.openkrise.db.gui.dbtable.header.guimessages_" + DBKernel.getLanguage(); //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private GuiMessages() {
		// empty
	}

	/**
	* @param key
	*            the key for the message
	* @return the string that matches the key
	*/
	public static String getString(final String key) {
		try {
			String res = RESOURCE_BUNDLE.getString(key);
			return res == null || res.isEmpty() ? key : res;
		}
		catch (final Exception e) {
			return key;//'!' + key + '!';
		}
	}
}
