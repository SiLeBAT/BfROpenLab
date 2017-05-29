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
package de.bund.bfr.knime.openkrise.util.json;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.json.JSONValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NoSettingsNodeModel;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import de.bund.bfr.knime.openkrise.TracingColumns;

/**
 * @author Christian Thoens
 */
public class FromJsonNodeModel extends NoSettingsNodeModel {

	/**
	 * Constructor for the node model.
	 */
	protected FromJsonNodeModel() {
		super(1, 1);
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		DataRow row = Iterables.getFirst(inData[0], null);
		DataCell cell = row.getCell(inData[0].getSpec().findColumnIndex(JsonConstants.JSON_COLUMN));

		if (cell.isMissing()) {
			throw new Exception("Cell in " + JsonConstants.JSON_COLUMN + " is missing");
		}

		JsonObject json = (JsonObject) ((JSONValue) cell).getJsonValue();
		JsonArray stations = json.getJsonArray(JsonConstants.STATIONS);
		Map<String, DataType> stationColumns = new LinkedHashMap<>();

		stationColumns.put(TracingColumns.ID, StringCell.TYPE);
		stationColumns.put(TracingColumns.NAME, StringCell.TYPE);

		for (JsonValue s : stations) {
			JsonObject station = (JsonObject) s;

			station.forEach((name, value) -> {
				if (!JsonConstants.STATION_PROPERTIES.contains(name)) {
					DataType type = toKnimeType(value.getValueType());

					if (type != null) {
						DataType oldType = stationColumns.get(name);

						stationColumns.put(name, oldType == null || oldType == type ? type : StringCell.TYPE);
					}
				}
			});
		}

		// DataTableSpec stationsSpec = toTableSpec(stationColumns);
		//
		// for (JsonValue s : stations) {
		// JsonObject station = (JsonObject) s;
		//
		// station.forEach((name, value) -> {
		// if (!JsonConstants.STATION_PROPERTIES.contains(name) &&
		// !fixedStationColumns.containsKey(name)) {
		// DataType type = toKnimeType(value.getValueType());
		//
		// if (type != null) {
		// DataType oldType = otherStationColumns.get(name);
		//
		// otherStationColumns.put(name, oldType == null || oldType == type ?
		// type : StringCell.TYPE);
		// }
		// }
		// });
		// }
		//
		// return new BufferedDataTable[] { container.getTable() };

		return null;
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		if (!inSpecs[0].containsName(JsonConstants.JSON_COLUMN)) {
			throw new InvalidSettingsException(JsonConstants.JSON_COLUMN + " is missing");
		} else if (!inSpecs[0].getColumnSpec(JsonConstants.JSON_COLUMN).getType().isCompatible(JSONValue.class)) {
			throw new InvalidSettingsException(JsonConstants.JSON_COLUMN + " must be of type JSON");
		}

		return null;
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	private static DataType toKnimeType(JsonValue.ValueType jsonType) {
		switch (jsonType) {
		case NUMBER:
			return DoubleCell.TYPE;
		case FALSE:
		case TRUE:
			return BooleanCell.TYPE;
		case STRING:
			return StringCell.TYPE;
		case ARRAY:
		case NULL:
		case OBJECT:
		default:
			return null;
		}
	}

	private static DataTableSpec toTableSpec(Map<String, DataType> columns) {
		List<DataColumnSpec> columnSpecs = new ArrayList<>();

		columns.forEach((name, type) -> columnSpecs.add(new DataColumnSpecCreator(name, type).createSpec()));

		return new DataTableSpec(columnSpecs.toArray(new DataColumnSpec[0]));
	}
}
