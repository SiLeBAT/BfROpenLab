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

import java.util.Vector;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.node.BufferedDataTable;

/**
 * 
 * Wraps a KNIME {@link BufferedDataTable} so its data can be accessed by column
 * 
 * @author André-Patrick Bubel <code@andre-bubel.de>
 * 
 */
public class KNIMEColumnTable {
	private final Vector<Vector<DataCell>> columns;
	private final BufferedDataTable dataTable;

	/**
	 * Creates a representation of the dataTable, thats data can be accessed per
	 * column
	 * 
	 * @param dataTable
	 */
	public KNIMEColumnTable(final BufferedDataTable dataTable) {
		this.dataTable = dataTable;
		columns = dataTableToInternalTable();
	}

	/**
	 * Disable default constructor
	 */
	@SuppressWarnings("unused")
	private KNIMEColumnTable() {
		dataTable = null;
		columns = null;
	}

	/**
	 * Returns the number of columns of the table
	 * 
	 * @return number of columns in the table
	 */
	public int getColumnCount() {
		return dataTable.getSpec().getNumColumns();
	}

	/**
	 * Gets the table column at the given position
	 * 
	 * @param columnIndex
	 *            index column number to return
	 * @return the table column
	 */
	public Vector<DataCell> getColumnData(final int columnIndex) {
		// dataTable.getSpec().getColumnSpec(index);
		return columns.get(columnIndex);
	}

	/**
	 * Returns the name of the column at the given position
	 * 
	 * @param index
	 *            column number to query
	 * @return name of the column
	 */
	public String getColumnName(final int index) {
		return dataTable.getSpec().getColumnSpec(index).getName();
	}

	/**
	 * Returns the {@link DataType} of the column at the given position
	 * 
	 * @param index
	 *            column number to query
	 * @return type of the column
	 */
	public DataType getColumnType(final int index) {
		dataTable.getRowCount();
		return dataTable.getSpec().getColumnSpec(index).getType();
	}

	/**
	 * Returns the number of rows of the table
	 * 
	 * @return number of rows in the table
	 */
	public int getRowCount() {
		return dataTable.getRowCount();
	}

	/**
	 * Creates a nested Vector table with given number of columns and prepared
	 * internal Vectors for the row data
	 * 
	 * @param columnNumber
	 *            number of columns
	 * @param rowNumber
	 *            number of rows
	 * @return a nested Vector table with given number of columns and rows
	 */
	private Vector<Vector<DataCell>> createEmptyDataCellTable(
			final int columnNumber, final int rowNumber) {
		Vector<Vector<DataCell>> newTable = new Vector<>(columnNumber);

		int i = 0;
		while (i < columnNumber) {
			newTable.add(new Vector<DataCell>(rowNumber));
			i++;
		}

		return newTable;
	}

	/**
	 * Fills the internal temporary table with data from the dataTable
	 */
	private Vector<Vector<DataCell>> dataTableToInternalTable() {
		Vector<Vector<DataCell>> newTable = createEmptyDataCellTable(
				getColumnCount(), getRowCount());

		for (DataRow row : dataTable) {
			for (int columnIndex = 0; columnIndex < getColumnCount(); columnIndex++) {
				newTable.get(columnIndex).add(row.getCell(columnIndex));
			}
		}

		return newTable;
	}

}
