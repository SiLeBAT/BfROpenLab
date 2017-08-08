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
package de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren;

import java.util.Vector;

import javax.swing.table.TableModel;

import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.MyTable;
import quick.dbtable.Filter;

/**
 * @author Armin
 *
 */
public class MyStringFilter implements Filter {
	
	private MyTable myTable = null;
	private String findString = "";
	private String columnName = "";
	
	public MyStringFilter(MyTable myTable, String columnName, String findString) {
		this.myTable = myTable;
		this.findString = findString;
		this.columnName = columnName;
	}
	
     public int[] filter(TableModel tm) {
    	 int[] result = null;
    	 Vector<Integer> vv = new Vector<>();
    	 try {
    		 if (myTable == null || findString.trim().length() == 0) {
    			 result = getAllFilter(tm, null);
    		 }
    		 else {
        		 int numRows = tm.getRowCount();
    			 for (int row = 0; row < numRows; row++) {
     		  		 loopInternal(vv, row, tm, findString);
    			 }
            	 result = getAllFilter(tm, vv);
    		 }
    	 }
    	 catch (Exception e) {MyLogger.handleException(e); result = null;}
 		return result;
     }
     private void loopInternal(Vector<Integer> vv, int row, TableModel tm, String findString) {
		 for (int col = 0; col < myTable.getFieldNames().length; col++) {
			 if (myTable.getFieldNames()[col].equals(columnName)) {				 
	    	 		Object o = tm.getValueAt(row, col + 2);
	    	 		if (o != null && o.toString().equals(findString)) { // sonst Fehler in z.B. Methoden bei der Suchfunktion
	 					vv.addElement(new Integer(row));
	    	 		}
	    	 }
		 }
     }
     private int[] getAllFilter(TableModel tm, Vector<Integer> vv) {
    	 if (vv == null) {
        	 int numRows = tm.getRowCount();
        	 int arr[] = new int[numRows];
        	 for (int row = 0; row < numRows; row++) {
        		 arr[row] = row;
        	 }    		 
        	 return arr;
    	 }
    	 else {
             int arr[] = new int[vv.size()];
             for(int i=0; i< vv.size(); i++) {
               arr[i] = (vv.elementAt(i)).intValue();
             }          	 
             return arr;    		 
    	 }
     }
}
