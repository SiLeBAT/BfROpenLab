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
/**
 * 
 */
package de.bund.bfr.knime.openkrise.db;

import java.util.LinkedHashMap;

/**
 * @author Armin
 *
 */
class Users {

	static int READ_ONLY = 0;	
	static int WRITE_ACCESS = 10;
	static int SUPER_WRITE_ACCESS = 20;
	static int ADMIN = 30;
	
	private int accRight;
	private String username;
  
	
  static LinkedHashMap<Object, String> getUserTypesHash() {
  	LinkedHashMap<Object, String> result = new LinkedHashMap<>();
  	result.put(READ_ONLY, "READ_ONLY");					
  	result.put(WRITE_ACCESS, "WRITE_ACCESS");					
  	result.put(SUPER_WRITE_ACCESS, "SUPER_WRITE_ACCESS");					
  	result.put(ADMIN, "ADMIN");					
  	return result;
  }
  
  public String getUsername() {
  	return username;
  }
  public int getAccessRight() {
  	return accRight;
  }
}
