/*******************************************************************************
 * PMM-Lab © 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab © 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * Christian Thöns (BfR)
 * Matthias Filter (BfR)
 * Armin A. Weiser (BfR)
 * Alexander Falenski (BfR)
 * Jörgen Brandt (BfR)
 * Annemarie Käsbohrer (BfR)
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
/**
 * 
 */
package org.hsh.bfr.db;

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
