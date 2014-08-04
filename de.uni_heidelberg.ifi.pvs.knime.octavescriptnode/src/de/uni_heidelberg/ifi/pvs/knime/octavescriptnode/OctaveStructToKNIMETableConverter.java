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

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import dk.ange.octave.type.OctaveCell;
import dk.ange.octave.type.OctaveDouble;
import dk.ange.octave.type.OctaveObject;
import dk.ange.octave.type.OctaveString;
import dk.ange.octave.type.OctaveStruct;

/**
 * 
 * @author André-Patrick Bubel <code@andre-bubel.de>
 * 
 */
public class OctaveStructToKNIMETableConverter {
	private final ExecutionContext context;
	private final DataTableSpec spec;

	public OctaveStructToKNIMETableConverter(final DataTableSpec spec,
			final ExecutionContext context) {
		this.spec = spec;
		this.context = context;
	}

	@SuppressWarnings("unused")
	private OctaveStructToKNIMETableConverter() {
		this.spec = null;
		this.context = null;
	}

	public BufferedDataTable convert(OctaveStruct struct) throws ParseException {
		BufferedDataContainer newContainer = context.createDataContainer(spec);

		int rowCount = getRowCount(struct);
		for (int rowID = 0; rowID < rowCount; rowID++) {
			newContainer.addRowToTable(getRow(struct, rowID));
		}

		newContainer.close();

		return newContainer.getTable();
	}

	/**
	 * @param rowID
	 * @param cellData
	 * @return
	 */
	private static OctaveObject getCellArrayData(OctaveCell cell, int rowID) {
		return cell.get(rowID + 1);
	}

	/**
	 * @param octaveResult
	 * @param columnName
	 * @return
	 * @throws ParseException
	 */
	private static OctaveCell getOctaveCellArrayFromStruct(
			final OctaveStruct octaveResult, String columnName)
			throws ParseException {
		OctaveCell cellData = null;
		try {
			cellData = (OctaveCell) octaveResult.get(columnName);
		} catch (ClassCastException e) {
			throw new ParseException(
					"Struct needs to contain cells that hold the column data, but other data type found in column "
							+ columnName, e);
		}

		if (cellData == null)
			throw new ParseException("Column " + columnName
					+ " in struct doesn't contain data");
		return cellData;
	}

	private DataRow getRow(final OctaveStruct octaveResult, int rowID)
			throws ParseException {
		DataCell[] cells = new DataCell[spec.getNumColumns()];

		for (int i = 0; i < spec.getNumColumns(); i++) {
			String columnName = spec.getColumnSpec(i).getName();

			OctaveCell octaveCell = getOctaveCellArrayFromStruct(octaveResult,
					columnName);

			OctaveObject cellData = null;

			try {
				cellData = getCellArrayData(octaveCell, rowID);
			} catch (IndexOutOfBoundsException e) {
				// cell didn't exist, use empty cell as output
				cells[i] = DataType.getMissingCell();
				continue;
			}

			if (cellData instanceof OctaveDouble) {
				double value = ((OctaveDouble) cellData).get(1, 1);
				if (spec.getColumnSpec(columnName).getType() == DoubleCell.TYPE) {
					cells[i] = new DoubleCell(value);
				} else if (spec.getColumnSpec(columnName).getType() == IntCell.TYPE) {
					cells[i] = new IntCell((int) value);
				} else
					throw new ParseException(
							"Numerical value in octave cell but the column type is "
									+ spec.getColumnSpec(columnName).getType()
											.toString());
			} else if (cellData instanceof OctaveString) {
				cells[i] = new StringCell(((OctaveString) cellData).getString());
			} else
				throw new ParseException("Octave datatype "
						+ cellData.getClass().getName() + " in column "
						+ columnName + " not supported");
		}

		return new DefaultRow("row_" + rowID, cells);
	}

	private static int getRowCount(OctaveStruct struct) throws ParseException {
		int maxRow = 0;

		for (String columnName : struct.getData().keySet()) {
			OctaveCell cell = getOctaveCellArrayFromStruct(struct, columnName);

			// if current column has more entries, than use that
			if (cell.size(1) > maxRow) {
				maxRow = cell.size(1);
			}
		}
		return maxRow;
	}

}
