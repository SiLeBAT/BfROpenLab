/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
package org.hsh.bfr.db.gui.dbtable;

import java.util.HashSet;
import java.util.LinkedHashMap;

import javax.swing.JOptionPane;

import org.hsh.bfr.db.DBKernel;
import org.hsh.bfr.db.MyLogger;

import quick.dbtable.DatabaseChangeListener;

/**
 * @author Armin
 *
 */

class MyDataChangeListener extends DatabaseChangeListener {

	private MyDBTable table;
	
  public MyDataChangeListener(MyDBTable table) {
    this.table = table;
  }
  public boolean beforeDelete(int row) {
  	if (table.getActualTable().isReadOnly() || (table.getMyDBPanel() != null && table.getMyDBPanel().isMN())) return false; // table.getMyDBPanel() == null bedeutet, dass es sich hier um myDBTable2 handelt... da soll schon gelöscht werden können
  	if (table.getActualTable().getTablename().equals("Users")) {
  		// Achtung: es sollte immer mindestens ein Admin vorhanden sein. Daher: Löschung nicht zulassen!!!
  		if (table.getValueAt(row, 1) != null && table.getValueAt(row, 1).toString().length() > 0) {
    		if (DBKernel.getUsername().equals(table.getValueAt(row, 1).toString())) {
  		    JOptionPane.showMessageDialog(table, "Aktiver User kann nicht gelöscht werden!", "Löschen nicht möglich", JOptionPane.INFORMATION_MESSAGE);
  				return false;
    		}
    		/*
    		if (DBKernel.countUsers(true) == 1) {
    			int oldAccRight = ((Integer) table.getValueAt(row, 4)).intValue();
    			if (oldAccRight == Users.ADMIN) {
    				JOptionPane.showMessageDialog(table, "Mindestens ein User muss Admin Rechte haben!", "Löschen nicht möglich", JOptionPane.INFORMATION_MESSAGE);
    				return false;
    			}
    		}  	
    		*/		
  		}
  	}
    int retVal = JOptionPane.showConfirmDialog(table, "Sind Sie sicher, daß Sie die ausgewählte Zeile löschen möchten?", "Löschen bestätigen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    return retVal == JOptionPane.YES_OPTION;
  }

  public boolean beforeInsert(int row) {
  	if (table.getActualTable().isReadOnly()) return false;
    boolean result = true;
    return result;
  }
  public boolean beforeUpdate(int row) {
  	if (table.getActualTable().isReadOnly()) return false;
    boolean result = true;
    return result;
  }

  public void afterInsert(int row) {
	  table.getActualTable().doMNs();
	  if (table.getTable().getRowSorter() != null && table.getTable().getRowSorter().getSortKeys().size() > 0) {
		  row = table.getTable().convertRowIndexToView(row);
	  }
	  table.myRefresh(row);
  }
  public void afterUpdate(int row) {
	  table.getActualTable().doMNs();
	  row = checkThings(row);
	  table.myRefresh(row);
	  /*
	  Vector<String[]> plausibility = PlausibilityChecker.getPlausibilityRow(table, table.getActualTable(), row, null);
	  if (plausibility != null && plausibility.size() == 1) {
		  String[] res = plausibility.get(0);
		  if (res[0] != null && res[0].trim().length() > 0) {
			  InfoBox ib = new InfoBox(res[0], true, new Dimension(500, 150), null, true);
			  ib.setVisible(true);   
		  }
	  }
	  */
  }
  public void afterDelete(int row) {
	  row = checkThings(row);
	  //if (row > 0) table.myRefresh(row-1);
	  if (table.getRowCount() == 1) table.myRefresh(0);
	  else table.myRefresh(row-1);
  }  
  private int checkThings(int row) {
	  int result = row;
	  if (table.getTable().getRowSorter() != null && table.getTable().getRowSorter().getSortKeys().size() > 0) {
			result = table.getTable().convertRowIndexToView(row);
	  }
	  deleteFromCPM(row, result);
	  return result;
  }
  private void deleteFromCPM(int row, int convertedRowIndexToView) {
	  if (table.getMyCellPropertiesModel() instanceof MyCellPropertiesModel) {
		  LinkedHashMap<Integer, HashSet<Integer>> modifiedCells = ((MyCellPropertiesModel) table.getMyCellPropertiesModel()).getModifiedCellsColl();
		  if (modifiedCells != null) {
			  //System.err.println(row + "\t" + table.getRowCount() + "\t" + table.getTable().getRowCount() + "\t" + table.getTable().getModel().getRowCount());
			  if (convertedRowIndexToView < table.getTable().getModel().getRowCount()) {
				  try {
					  if (modifiedCells.containsKey(table.getValueAt(convertedRowIndexToView, 0))) {
						  modifiedCells.remove(table.getValueAt(convertedRowIndexToView, 0));
					  }				  
				  }
				  catch (Exception e) {MyLogger.handleException(e);}
			  }
		  }
	  }	  
  }
}
