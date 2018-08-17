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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.json.JSONValue;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NoSettingsNodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortType;
import de.bund.bfr.knime.NoInternalsNodeModel;
import de.bund.bfr.knime.openkrise.TracingColumns;

/**
 * @author Christian Thoens
 */
public class FromJsonNodeModel extends NoSettingsNodeModel {
//public class FromJsonNodeModel extends NoInternalsNodeModel {

	/**
	 * Constructor for the node model.
	 */
	protected FromJsonNodeModel() {
	  super(1, 4);
//	  super(new PortType[] { BufferedDataTable.TYPE}, 
//	      new PortType[] {BufferedDataTable.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE,
//          BufferedDataTable.TYPE_OPTIONAL });
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
	    BufferedDataTable jsonInTable = inData[0];
	    JsonFormat json = JsonConverter.convertFromJson(Utils.extractJsonValueFromBufferedDataTable(jsonInTable, JsonConstants.JSON_COLUMN_SETTINGS));
	    
//		DataRow row = Iterables.getFirst(inData[0], null);
//		DataCell cell = row.getCell(inData[0].getSpec().findColumnIndex(JsonConstants.JSON_COLUMN));

//		if (cell.isMissing()) {
//			throw new Exception("Cell in " + JsonConstants.JSON_COLUMN + " is missing");
//		}
//
//		JsonObject json = (JsonObject) ((JSONValue) cell).getJsonValue();
//
//		if (!json.containsKey(JsonConstants.ELEMENTS)) {
//			throw new Exception("Wrong json format. Must be exported from FoodChain-Lab Web App");
//		}

//		JsonObject elements = (JsonObject) json.get(JsonConstants.ELEMENTS);
//		JsonArray stations = elements.getJsonArray(JsonConstants.STATIONS);
//		JsonArray deliveries = elements.getJsonArray(JsonConstants.DELIVERIES);
		
//		Map<String, DataType> stationColumns = new LinkedHashMap<>();
//        Map<String, DataType> deliveryColumns = new LinkedHashMap<>();
//		//Map<String, DataType> stationColumns = new HashMap<>();
//		for(JsonFormat.Data.ColumnSpec columnSpec : json.data.stationColumns) stationColumns.put(columnSpec.id, JsonConverter.convertToDataType(columnSpec.type));
//		for(JsonFormat.Data.ColumnSpec columnSpec : json.data.deliveryColumns) deliveryColumns.put(columnSpec.id, JsonConverter.convertToDataType(columnSpec.type));
		
		
		
//		Map<String, DataType> stationColumns = new LinkedHashMap<>();
//		Map<String, DataType> deliveryColumns = new LinkedHashMap<>();
//
//		
//		
//		for (JsonValue s : stations) {
//			for (JsonValue prop : ((JsonObject) s).getJsonArray(JsonConstants.PROPERTIES)) {
//				stationColumns.put(((JsonObject) prop).getString(JsonConstants.NAME), StringCell.TYPE);
//			}
//		}
//
//		for (JsonValue d : deliveries) {
//			for (JsonValue prop : ((JsonObject) d).getJsonArray(JsonConstants.PROPERTIES)) {
//				deliveryColumns.put(((JsonObject) prop).getString(JsonConstants.NAME), StringCell.TYPE);
//			}
//		}
//
//		stationColumns.put(TracingColumns.ID, StringCell.TYPE);
//		stationColumns.put(TracingColumns.NAME, StringCell.TYPE);
//		deliveryColumns.put(TracingColumns.ID, StringCell.TYPE);
//		deliveryColumns.put(TracingColumns.NAME, StringCell.TYPE);
//		deliveryColumns.put(TracingColumns.FROM, StringCell.TYPE);
//		deliveryColumns.put(TracingColumns.TO, StringCell.TYPE);

//		DataTableSpec stationsSpec = toTableSpec(stationColumns);
//		DataTableSpec deliveriesSpec = toTableSpec(deliveryColumns);
////		DataTableSpec deliveryRelationsSpec = new DataTableSpec(
////				new DataColumnSpecCreator(TracingColumns.FROM, StringCell.TYPE).createSpec(),
////				new DataColumnSpecCreator(TracingColumns.TO, StringCell.TYPE).createSpec());
//		DataTableSpec deliveryRelationsSpec = new DataTableSpec(
//          new DataColumnSpecCreator(TracingColumns.ID, StringCell.TYPE).createSpec(),
//          new DataColumnSpecCreator(TracingColumns.NEXT, StringCell.TYPE).createSpec());
//		
//		BufferedDataContainer stationsContainer = exec.createDataContainer(stationsSpec);
//		BufferedDataContainer deliveriesContainer = exec.createDataContainer(deliveriesSpec);
//		BufferedDataContainer deliveryRelationsContainer = exec.createDataContainer(deliveryRelationsSpec);
//		
//		long rowIndex = 0;
//		for (JsonFormat.Data.Property[] properties : json.data.stations) {
//          DataCell[] cells = new DataCell[stationsSpec.getNumColumns()];
//
//          Arrays.fill(cells, DataType.getMissingCell());
//          
//          for(JsonFormat.Data.Property property : properties) {
//            int columnIndex = stationsSpec.findColumnIndex(property.id);
//            
//            if(property.value!=null) cells[columnIndex] = JsonConverter.createDataCell(property.value, stationsSpec.getColumnSpec(columnIndex).getType().getCellClass());
//          }
//          
//          stationsContainer.addRowToTable(new DefaultRow( RowKey.createRowKey(rowIndex++),cells));
//		}
//		
//		rowIndex = 0;
//		for (JsonFormat.Data.Property[] properties : json.data.deliveries) {
//          DataCell[] cells = new DataCell[deliveriesSpec.getNumColumns()];
//
//          Arrays.fill(cells, DataType.getMissingCell());
//          
//          for(JsonFormat.Data.Property property : properties) {
//            int columnIndex = stationsSpec.findColumnIndex(property.id);
//            
//            try {
//              if(property.value!=null) cells[columnIndex] = JsonConverter.createDataCell(property.value, deliveriesSpec.getColumnSpec(columnIndex).getType().getCellClass());
//            } catch(Exception e) {
//              e.printStackTrace();
//            }
//          }
//          
//          deliveriesContainer.addRowToTable(new DefaultRow( RowKey.createRowKey(rowIndex++),cells));
//        }
//		
//		rowIndex = 0;
//		for(JsonFormat.Data.DeliveryRelation deliveryRelation : json.data.deliveryRelations) 
//		   deliveryRelationsContainer.addRowToTable(new DefaultRow(
//              RowKey.createRowKey(rowIndex++),new StringCell(deliveryRelation.fromId),new StringCell(deliveryRelation.toId)));
//		
		
//		long stationsIndex = 0;
//		long deliveriesIndex = 0;
//		long deliveryRelationsIndex = 0;
//
//		for (JsonValue s : stations) {
//			DataCell[] cells = new DataCell[stationsSpec.getNumColumns()];
//
//			Arrays.fill(cells, DataType.getMissingCell());
//
//			JsonObject station = (JsonObject) s;
//
//			for (JsonValue prop : station.getJsonArray(JsonConstants.PROPERTIES)) {
//				String name = ((JsonObject) prop).getString(JsonConstants.NAME);
//				JsonValue value = ((JsonObject) prop).get(JsonConstants.VALUE);
//
//				if (stationsSpec.containsName(name)) {
//					cells[stationsSpec.findColumnIndex(name)] = value instanceof JsonString
//							? new StringCell(((JsonString) value).getString()) : DataType.getMissingCell();
//				}
//			}
//
//			cells[stationsSpec.findColumnIndex(TracingColumns.ID)] = new StringCell(
//					readStationId(station.getString(JsonConstants.ID)));
//			cells[stationsSpec.findColumnIndex(TracingColumns.NAME)] = new StringCell(
//					station.getString(JsonConstants.NAME));
//
//			stationsContainer.addRowToTable(new DefaultRow(RowKey.createRowKey(stationsIndex++), cells));
//
//			JsonValue connections = station.get(JsonConstants.CONNECTIONS);
//
//			if (connections instanceof JsonArray) {
//				for (JsonValue c : (JsonArray) connections) {
//					if (c instanceof JsonObject) {
//						deliveryRelationsContainer.addRowToTable(new DefaultRow(
//								RowKey.createRowKey(deliveryRelationsIndex++),
//								new StringCell(readDeliveryId(((JsonObject) c).getString(JsonConstants.SOURCE))),
//								new StringCell(readDeliveryId(((JsonObject) c).getString(JsonConstants.TARGET)))));
//					}
//				}
//			}
//		}
//
//		for (JsonValue d : deliveries) {
//			DataCell[] cells = new DataCell[deliveriesSpec.getNumColumns()];
//
//			Arrays.fill(cells, DataType.getMissingCell());
//
//			JsonObject delivery = (JsonObject) d;
//
//			for (JsonValue prop : delivery.getJsonArray(JsonConstants.PROPERTIES)) {
//				String name = ((JsonObject) prop).getString(JsonConstants.NAME);
//				JsonValue value = ((JsonObject) prop).get(JsonConstants.VALUE);
//
//				if (deliveriesSpec.containsName(name)) {
//					cells[deliveriesSpec.findColumnIndex(name)] = value instanceof JsonString
//							? new StringCell(((JsonString) value).getString()) : DataType.getMissingCell();
//				}
//			}
//
//			cells[deliveriesSpec.findColumnIndex(TracingColumns.ID)] = new StringCell(
//					readDeliveryId(delivery.getString(JsonConstants.ID)));
//			cells[deliveriesSpec.findColumnIndex(TracingColumns.NAME)] = new StringCell(
//					delivery.getString(JsonConstants.NAME));
//			cells[deliveriesSpec.findColumnIndex(TracingColumns.FROM)] = new StringCell(
//					readStationId(delivery.getString(JsonConstants.SOURCE)));
//			cells[deliveriesSpec.findColumnIndex(TracingColumns.TO)] = new StringCell(
//					readStationId(delivery.getString(JsonConstants.TARGET)));
//
//			deliveriesContainer.addRowToTable(new DefaultRow(RowKey.createRowKey(deliveriesIndex++), cells));
//		}

//		stationsContainer.close();
//		deliveriesContainer.close();
//		deliveryRelationsContainer.close();
//		
//		return new BufferedDataTable[] { stationsContainer.getTable(), deliveriesContainer.getTable(),
//            deliveryRelationsContainer.getTable(), null };

	   
	    
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

//	private static DataTableSpec toTableSpec(Map<String, DataType> columns) {
//		List<DataColumnSpec> columnSpecs = new ArrayList<>();
//
//		columns.forEach((name, type) -> columnSpecs.add(new DataColumnSpecCreator(name, type).createSpec()));
//
//		return new DataTableSpec(columnSpecs.toArray(new DataColumnSpec[0]));
//	}

//  @Override
//  protected void saveSettingsTo(NodeSettingsWO settings) {
//    // do nothing
//    
//  }
//
//  @Override
//  protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
//    // do nothing
//    
//  }
//
//  @Override
//  protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
//      throws InvalidSettingsException {
//    //  do nothing
//    
//  }

//	private static String readStationId(String id) throws Exception {
//		if (id.startsWith(JsonConstants.STATION_ID_PREFIX)) {
//			return id.substring(JsonConstants.STATION_ID_PREFIX.length());
//		} else {
//			throw new Exception("Invalid id " + id);
//		}
//	}
//
//	private static String readDeliveryId(String id) throws Exception {
//		if (id.startsWith(JsonConstants.DELIVERY_ID_PREFIX)) {
//			return id.substring(JsonConstants.DELIVERY_ID_PREFIX.length());
//		} else {
//			throw new Exception("Invalid id " + id);
//		}
//	}
}
