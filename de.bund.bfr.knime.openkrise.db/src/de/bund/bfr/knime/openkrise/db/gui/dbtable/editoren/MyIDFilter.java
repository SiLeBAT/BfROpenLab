/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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

import java.util.LinkedHashSet;
import java.util.Vector;

import javax.swing.table.TableModel;

import de.bund.bfr.knime.openkrise.db.MyLogger;
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
