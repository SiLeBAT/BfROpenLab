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
package org.hsh.bfr.db.gui.dbtable.editoren;

import java.util.LinkedHashSet;
import java.util.Vector;

import javax.swing.table.TableModel;

import org.hsh.bfr.db.MyLogger;

import quick.dbtable.Filter;

/**
 * @author Armin
 *
 */
public class MyIDFilter implements Filter {
	
	private LinkedHashSet<Integer> filterIDs;
	
	public MyIDFilter(LinkedHashSet<Integer> filterIDs) {
		this.filterIDs = filterIDs;
	}
	
     public int[] filter(TableModel tm) {
    	 int[] result = null;
    	 Vector<Integer> vv = new Vector<>();
    	 try {
    		 int numRows = tm.getRowCount();
    		 for (int row = 0; row < numRows; row++) {
    			 Object o = tm.getValueAt(row, 1);
    			 if (o instanceof Integer) {
    				 if (filterIDs.contains(o)) {
    					 vv.add(row);
    				 }
    			 }
    		 }
    	 }
    	 catch (Exception e) {MyLogger.handleException(e); result = null;}
		 if (vv != null) {
			 result = new int[vv.size()];
             for (int i=0; i< vv.size(); i++) {
            	 result[i] = (vv.elementAt(i)).intValue();
             }          	 
		 }
 		return result;
     }
}
