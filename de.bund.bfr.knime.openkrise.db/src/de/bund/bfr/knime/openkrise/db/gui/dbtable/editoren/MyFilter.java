/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JTextField;
import javax.swing.table.TableModel;

import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.MyTable;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;
import quick.dbtable.Filter;

/**
 * @author Armin
 *
 */
public class MyFilter implements Filter {
	
	private MyDBTable myDBTable1;
	private LinkedHashMap<Integer, Vector<String>> userIDs = null;
	private String findString = "";
	private JTextField textField1 = null;
	
	public MyFilter(MyDBTable myDBTable1, String findString, LinkedHashMap<Integer, Vector<String>> userIDs, JTextField textField1) {
		this.myDBTable1 = myDBTable1;
		this.findString = findString;
		this.userIDs = userIDs;
		this.textField1 = textField1;
	}
	
     public int[] filter(TableModel tm) {
    	 int[] result = null;
    	 Vector<Integer> rows2check = null;
    	 if (userIDs != null) {
        	 rows2check = new Vector<>();
		  	 for (int row = 0; row < tm.getRowCount(); row++) {
		  		Integer o = (Integer) tm.getValueAt(row, 1);
		  		if (userIDs.containsKey(o)) {
		  			rows2check.addElement(new Integer(row));
		  		}
			 }
    	 }
    	 Vector<Integer> vv = new Vector<>();
    	 try {
    		 if (findString.trim().length() == 0) {
    			 result = getAllFilter(tm, rows2check);
    		 }
    		 else {
    			 /*
    			 StringTokenizer tok = new StringTokenizer(findString);
    			 String[] findStrings = new String[tok.countTokens()];
    			 for (int i=0;tok.hasMoreTokens();i++) {
    				 findStrings[i] = tok.nextToken().toUpperCase();
    			 }
*/
    			 String theRealFindString = findString;
    			 HashSet<Integer> cols2Search = new HashSet<>();
				 if (theRealFindString.startsWith("[COLS:")) { // [COLS:#,#,#], e.g. [COLS:2]case
					 int endIndex = theRealFindString.indexOf("]");
					 if (endIndex > 0) {
						 try {
    						 String cols = theRealFindString.substring("[COLS:".length(), endIndex);
    						 HashSet<Integer> localCols2Search = new HashSet<>();
    						 StringTokenizer tok = new StringTokenizer(cols, ",");
    						 while (tok.hasMoreTokens()) {
    							 int col = Integer.parseInt(tok.nextToken());
    							 localCols2Search.add(col);
    						 }
    						 theRealFindString = theRealFindString.substring(endIndex + 1);
    						 cols2Search.addAll(localCols2Search);
						 }
						 catch (Exception e) {}
					 }
				 }
    			 String[] findStrings = theRealFindString.split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");
    			 for (int i=0;i<findStrings.length;i++) {
    				 if (!findStrings[i].trim().isEmpty()) {
    					 //parts[i] = parts[i].replace("%", "\\%");
    					 //parts[i] = parts[i].replace("_", "\\_");
    					 if (findStrings[i].startsWith("\"") && findStrings[i].endsWith("\"")) findStrings[i] = findStrings[i].substring(1, findStrings[i].length() - 1);
    					 findStrings[i] = findStrings[i].toUpperCase();
    				 }
    			 }

        		 int numRows = tm.getRowCount();
        		 MyTable[] foreignFields = myDBTable1.getActualTable().getForeignFields();
        		 String[] mnTable = myDBTable1.getActualTable().getMNTable();
        		 if (userIDs != null) {
            		 for (Integer row : rows2check) {
         		  		 loopInternal(vv, row.intValue(), mnTable, foreignFields, tm, findStrings, cols2Search);
            		 }        		 
        		 }
        		 else {
        			 for (int row = 0; row < numRows; row++) {
         		  		 loopInternal(vv, row, mnTable, foreignFields, tm, findStrings, cols2Search);
        			 }
        		 }
            	 result = getAllFilter(tm, vv);
    		 }
    	 }
    	 catch (Exception e) {MyLogger.handleException(e); result = null;}
 		if (textField1 != null && !textField1.hasFocus()) {
 			textField1.requestFocus();
 		}
 		return result;
     }
     private boolean loopInternal(Vector<Integer> vv, int row, String[] mnTable, MyTable[] foreignFields, TableModel tm, String[] findStrings, HashSet<Integer> cols2Search) {
		 String res = "";
		 for (int col = 0; col < tm.getColumnCount(); col++) {
			 if (cols2Search == null || cols2Search.size() == 0 || cols2Search.contains(col)) {
				 boolean isMN = (col > 0 && mnTable != null && mnTable.length > col-1 && mnTable[col-1] != null);
		    	 	if (myDBTable1.getColumn(col).getType() != java.sql.Types.BOOLEAN && !isMN) {
		    	 		Object o = tm.getValueAt(row, col+1);
		    	 		if (o != null) { // sonst Fehler in z.B. Methoden bei der Suchfunktion
		      	  			boolean lookIn = (col > 0 && foreignFields != null && foreignFields.length > col-1 && foreignFields[col-1] != null && myDBTable1.hashBox[col-1] != null);
		  		  	    	if (lookIn && myDBTable1.hashBox[col-1].get(o) != null) res += "\n" + myDBTable1.hashBox[col-1].get(o).toString();
		  		  	    	else res += "\n" + o.toString();
		  		  	    	//System.err.println(row + "\t" + col + "\t" + o + "\t" + res);
		    	 		}
		    	 	}
			 }
		 }
    	if (res != null && !res.isEmpty()) {
    		int i;
  	    	for (i=0;i<findStrings.length;i++) {
  	    		if (res.toUpperCase().indexOf(findStrings[i]) < 0) break;
  	    	}
  	    	if (i == findStrings.length) {
  	    		vv.addElement(new Integer(row));
  	    		return true;
  	    	}
    	}
    	return false;
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
