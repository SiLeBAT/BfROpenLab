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
package de.bund.bfr.knime.openkrise.db.gui.dbtable.header;

import javax.swing.table.DefaultTableModel;

/**
 * @author Armin
 *
 */
public class MyTableRowModel extends DefaultTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int numRows;
	
	public MyTableRowModel(int numRows) {
		this.numRows = numRows;
	}

  public int getColumnCount() {
  	return 1;
  }
  public int getRowCount() {
  	return numRows;
  }

  public Object getValueAt(int row, int col) {
  	return row+1;
  }

  public boolean isCellEditable(int row, int col) {
    return false;
  }
}
