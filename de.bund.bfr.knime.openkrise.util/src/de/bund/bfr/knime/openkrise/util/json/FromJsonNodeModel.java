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
package de.bund.bfr.knime.openkrise.util.json;

import java.io.File;
import java.io.IOException;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.json.JSONValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NoSettingsNodeModel;

/**
 * @author Christian Thoens
 */
public class FromJsonNodeModel extends NoSettingsNodeModel {

	/**
	 * Constructor for the node model.
	 */
	protected FromJsonNodeModel() {
	  super(1, 4);
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
	    BufferedDataTable jsonInTable = inData[0];
	    JsonFormat json = JsonConverter.convertFromJson(Utils.extractJsonValueFromBufferedDataTable(jsonInTable, JsonConstants.JSON_COLUMN_SETTINGS));
	    
		BufferedDataTable stationTable = JsonConverter.createDataTable(json.data.stations, exec);
		BufferedDataTable deliveryTable = JsonConverter.createDataTable(json.data.deliveries, exec);
		BufferedDataTable deliveryRelationsTable = JsonConverter.createDataTable(json.data.deliveryRelations, exec);
		
		json.data = null;
		BufferedDataTable jsonOutTable = Utils.convertJsonValueToTable(JsonConverter.convertToJsonValue(json), exec);
		
		return new BufferedDataTable[] { stationTable, deliveryTable, deliveryRelationsTable, jsonOutTable };
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

}
