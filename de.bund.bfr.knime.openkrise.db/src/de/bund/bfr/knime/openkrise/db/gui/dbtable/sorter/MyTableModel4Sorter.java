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
package de.bund.bfr.knime.openkrise.db.gui.dbtable.sorter;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;

/**
 * @author Armin
 *
 */
public class MyTableModel4Sorter extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MyDBTable myDB;
	private JTable table;
	private Object[][] o;
	
	public MyTableModel4Sorter(MyDBTable myDB) {
		this.table = myDB.getTable();
		this.myDB = myDB;
	}
	
	public void initArray() {
		o = myDB.getDataArray();
	}
	public int getColumnCount() {
		return table.getModel().getColumnCount() + 1;
	}

	public int getRowCount() {
		return table.getModel().getRowCount();
	}

	public Object getValueAt(int row, int col) {
		//System.out.println(table.getValueAt(0, col-1) + "\t" + o.length + "\t" + myDB.getDataArray().length);
		//if (o.length == 0) o = myDB.getDataArray();
		if (o == null || row >= o.length) return null;
		return o[row][col-1];//table.getValueAt(row, col-1);
	}
/*
  public String getColumnName(int col) {
    return table.getColumnName(col-1);
	}
	public Class getColumnClass(int col) {
	    return table.getColumnClass(col-1);
	}
	*/	
}
