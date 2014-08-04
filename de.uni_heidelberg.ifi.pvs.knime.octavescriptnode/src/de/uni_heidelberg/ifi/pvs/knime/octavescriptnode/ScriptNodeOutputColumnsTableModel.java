/*
 * OctaveScriptNode - A KNIME node that runs Octave scripts
 * Copyright (C) 2011 Andre-Patrick Bubel (pvs@andre-bubel.de) and
 *                    Parallel and Distributed Systems Group (PVS),
 *                    University of Heidelberg, Germany
 * Website: http://pvs.ifi.uni-heidelberg.de/
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
 */
package de.uni_heidelberg.ifi.pvs.knime.octavescriptnode;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.table.AbstractTableModel;

/**
 * Largely copied from org.knime.ext.jython.ScriptNodeOutputColumnsTableModel
 * 
 * @author André-Patrick Bubel <code@andre-bubel.de>
 * 
 */
public class ScriptNodeOutputColumnsTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2904071119465971558L;

	ArrayList<String> columnNames = new ArrayList<>();
	ArrayList<ArrayList<Object>> data = new ArrayList<>();

	public void addColumn(String columnName) {
		columnNames.add(columnName);
	}

	public void addRow(Object dataTableColumnName, Object dataTableColumnType) {
		ArrayList<Object> row = new ArrayList<>();
		row.add(dataTableColumnName);
		row.add(dataTableColumnType);

		data.add(row);

		int rowNum = data.size() - 1;
		fireTableRowsInserted(rowNum, rowNum);
	}

	public void clearRows() {
		data = new ArrayList<>();
	}

	@Override
	public int getColumnCount() {
		return columnNames.size();
	}

	@Override
	public String getColumnName(int col) {
		return columnNames.get(col).toString();
	}

	public String[] getDataTableColumnNames() {
		return getDataTableValues(0);
	}

	public String[] getDataTableColumnTypes() {
		return getDataTableValues(1);
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public Object getValueAt(int row, int col) {

		ArrayList<?> rowList = data.get(row);
		return rowList.get(col);
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return true;
	}

	public void removeRow(int row) {
		data.remove(row);
		fireTableRowsDeleted(row, row);
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		ArrayList<Object> rowList = data.get(row);
		rowList.set(col, value);
		fireTableCellUpdated(row, col);
	}

	private String[] getDataTableValues(int colIndex) {
		String[] dataTableColumnValues = new String[data.size()];

		Iterator<ArrayList<Object>> i = data.iterator();
		int rowNum = 0;
		while (i.hasNext()) {
			ArrayList<?> row = i.next();
			dataTableColumnValues[rowNum] = (String) row.get(colIndex);
			rowNum++;
		}
		return dataTableColumnValues;
	}
}
