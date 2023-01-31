/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db.gui.dbtable;

import java.util.HashSet;
import java.util.LinkedHashMap;

import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.MyTable;
import quick.dbtable.DBTableCellListener;

/**
 * @author Armin
 *
 */
class MyTableCellListener implements DBTableCellListener {
	
	private MyDBTable theTable = null;
	private MyTable myT = null;
	
	MyTableCellListener(MyDBTable theTable) {
		this.theTable = theTable;
		myT = theTable.getActualTable();
	}
	
	public Object cellValueChanged(int row, int col, Object oldValue, Object newValue) {
		//System.out.println(row + "\t" + col + "\t" + oldValue + "\t" + newValue);
		try {
			if (theTable.getMyCellPropertiesModel() instanceof MyCellPropertiesModel) {
				if (theTable.getColumn(col-1).getColumnName().equals("Geprueft") && oldValue == null && newValue != null && 
						newValue instanceof Boolean && ((Boolean)newValue) == false) {
					;
				}
				else if (oldValue == null || newValue == null || !String.valueOf(newValue).equals(String.valueOf(oldValue))) {
					if (oldValue == null && newValue != null && newValue.toString().length() == 0) {
						;
					}
					else {
						//System.out.println(oldValue + "\t" + newValue);
						//if (oldValue != null && newValue != null) System.out.println("gleuich" + String.valueOf(newValue).equals(String.valueOf(oldValue)));
						LinkedHashMap<Integer, HashSet<Integer>> modifiedCells = ((MyCellPropertiesModel) theTable.getMyCellPropertiesModel()).getModifiedCellsColl();
						if (modifiedCells != null) {
							HashSet<Integer> vec;
							int newRow = theTable.getTable().convertRowIndexToView(row);
							Object o = theTable.getValueAt(newRow, 0);
							if (o != null && o instanceof Integer) {
								Integer id = (Integer) o;
								if (modifiedCells.containsKey(id)) vec = modifiedCells.get(id);
								else vec = new HashSet<>();
								vec.add(col-1);
								modifiedCells.put(id, vec);
							}							
						}						
					}
				}
			}
			if (col - 2 >= myT.getFieldNames().length) return newValue;
			return newValue;
		}
		catch (Exception e) {MyLogger.handleException(e); return null;}
	}
}
