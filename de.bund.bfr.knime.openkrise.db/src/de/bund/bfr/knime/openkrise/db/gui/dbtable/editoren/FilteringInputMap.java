/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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
/**
 * 
 */
package de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren;

import javax.swing.InputMap;
import javax.swing.KeyStroke;

/**
 * @author Armin
 *
 */
class FilteringInputMap extends InputMap {

	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private KeyStroke[] disableKeys;

	 FilteringInputMap(InputMap parent, KeyStroke[] disableKeys) {
		 super();
		 setParent(parent);
		 this.disableKeys = disableKeys;
	 }

	 public Object get(KeyStroke keyStroke) {
		 if (disableKeys != null) {
			 for(int i=0; i<disableKeys.length; i++) {
				 if(keyStroke.equals(disableKeys[i])) {
					 return null;
				 }
			 }			 
		 }
		 return super.get(keyStroke);
	 }

}
