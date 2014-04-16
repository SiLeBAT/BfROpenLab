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
import org.knime.core.data.DoubleValue;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NotConfigurableException;

import dk.ange.octave.type.OctaveCell;
import dk.ange.octave.type.OctaveDouble;
import dk.ange.octave.type.OctaveObject;
import dk.ange.octave.type.OctaveString;
import dk.ange.octave.type.OctaveStruct;

public class KNIMETableToOctaveStructConverter {
	private final KNIMEColumnTable dataTable;

	public KNIMETableToOctaveStructConverter(BufferedDataTable dataTable) {
		this.dataTable = new KNIMEColumnTable(dataTable);
	}

	/**
	 * Converts a DataTable to a struct with the column names as the keys and
	 * cells containing the table rows
	 * 
	 * @return a {@link OctaveStruct}, that contains the data table columns
	 * @throws NotConfigurableException
	 */
	public OctaveStruct convert() throws NotConfigurableException {
		OctaveStruct result = new OctaveStruct();

		for (int x = 0; x < dataTable.getColumnCount(); x++) {
			Vector<DataCell> data = dataTable.getColumnData(x);
			OctaveCell newCell = new OctaveCell(dataTable.getRowCount(), 1);

			for (int y = 0; y < dataTable.getRowCount(); y++) {
				newCell.set(dataCellToOctaveObject(data.get(y)), y + 1, 1);
			}

			result.set(dataTable.getColumnName(x), newCell);
		}

		return result;
	}

	/**
	 * Returns true if cell contains a number (double or integer)
	 * 
	 * @param cell
	 * @return true if cell contains a number (double or integer)
	 */
	private boolean cellContainsNumber(DataCell cell) {
		return cell.getType() == IntCell.TYPE
				|| cell.getType() == DoubleCell.TYPE;
	}

	/**
	 * Returns true if cell contains a string
	 * 
	 * @param cell
	 * @return true if cell contains a string
	 */
	private boolean cellContainsString(DataCell cell) {
		return cell.getType() == StringCell.TYPE;
	}

	/**
	 * Converts a {@link DataCell} to a corresponding {@link OctaveObject}
	 * 
	 * Doubles and integers are converted to a double field, string cells will
	 * be converted to strings
	 * 
	 * @param cell
	 *            DataCell that will be converted
	 * @return a converted DataCell
	 * @throws NotConfigurableException
	 */
	private OctaveObject dataCellToOctaveObject(DataCell cell)
			throws NotConfigurableException {
		OctaveObject result;

		if (cellContainsString(cell)) {
			result = new OctaveString(cell.toString());
		} else if (cellContainsNumber(cell)) {
			DoubleValue value = (DoubleValue) cell;
			double[] d = { value.getDoubleValue() };

			result = new OctaveDouble(d, 1, 1);
		} else
			throw new NotConfigurableException(cell.getType().toString()
					+ " type not supported");

		return result;
	}

}
