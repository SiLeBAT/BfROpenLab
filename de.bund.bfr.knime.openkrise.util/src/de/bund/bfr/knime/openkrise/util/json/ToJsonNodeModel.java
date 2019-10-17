/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
import javax.json.JsonValue;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortType;
import de.bund.bfr.knime.NoInternalsNodeModel;

/**
 * @author Christian Thoens
 */
public class ToJsonNodeModel extends NoInternalsNodeModel {
    
	/**
	 * Constructor for the node model.
	 */
	protected ToJsonNodeModel() {

	  super(new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE,
          BufferedDataTable.TYPE_OPTIONAL },
          new PortType[] { BufferedDataTable.TYPE });
	  
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		BufferedDataTable nodeTable = inData[0];
		BufferedDataTable edgeTable = inData[1];
		BufferedDataTable deliveryRelationTable = inData[2];
		BufferedDataTable settingsTable = inData[3];
 
		JsonValue json = null;
		if(settingsTable!=null) json = Utils.extractJsonValueFromBufferedDataTable(settingsTable, JsonConstants.JSON_COLUMN_SETTINGS);
		
		JsonConverter.JsonBuilder jsonBuilder = new JsonConverter.JsonBuilder();
				
		jsonBuilder.setData(nodeTable, edgeTable, deliveryRelationTable); 
		jsonBuilder.setTracing(nodeTable, edgeTable);
        
		if(json!=null) jsonBuilder.merge(JsonConverter.convertFromJson(json));
		
		return new BufferedDataTable[] { Utils.convertJsonValueToTable(jsonBuilder.build(), exec) };
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
	  return new DataTableSpec[] {Utils.createJSONTableSpec()};
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}


  @Override
  protected void saveSettingsTo(NodeSettingsWO settings) {
    
  }

  @Override
  protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {

  }

  @Override
  protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
      throws InvalidSettingsException {
    
  }
}
