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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.json.JsonValue;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.json.JSONCell;
import org.knime.core.data.json.JSONCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortType;
import de.bund.bfr.knime.NoInternalsNodeModel;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.TracingUtils;
import de.bund.bfr.knime.openkrise.common.Delivery;

/**
 * @author Christian Thoens
 */
public class ToJsonNodeModel extends NoInternalsNodeModel {
    //private static final String VERSION = "1.0.0"; 

	/**
	 * Constructor for the node model.
	 */
	protected ToJsonNodeModel() {
	  //super(4,1);
	  super(new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE,
          BufferedDataTable.TYPE_OPTIONAL },
          new PortType[] { BufferedDataTable.TYPE });
	  
	  //super(4,1);
	  //super()
	  // Todo make 4th inport optional
		
		//super(4, 1);
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		BufferedDataTable nodeTable = inData[0];
		BufferedDataTable edgeTable = inData[1];
		BufferedDataTable deliveryRelationTable = inData[2];
		BufferedDataTable settingsTable = inData[3];
		
//		NodePropertySchema nodeSchema = new NodePropertySchema(TracingUtils.getTableColumns(nodeTable.getSpec()),
//				TracingColumns.ID);
//		EdgePropertySchema edgeSchema = new EdgePropertySchema(TracingUtils.getTableColumns(edgeTable.getSpec()),
//				TracingColumns.ID, TracingColumns.FROM, TracingColumns.TO);
//		Map<RowKey, String> skippedDeliveryRows = new LinkedHashMap<>();
//		Map<RowKey, String> skippedDeliveryRelationsRows = new LinkedHashMap<>();
//
//		Map<String, GraphNode> nodes = TracingUtils.readGraphNodes(nodeTable, nodeSchema);
//		List<Edge<GraphNode>> edges = TracingUtils.readEdges(edgeTable, edgeSchema, nodes, skippedDeliveryRows);
//		Map<String, Delivery> deliveries = TracingUtils.readDeliveries(deliveryRelationTable, edges,
//				skippedDeliveryRelationsRows);
//
//		skippedDeliveryRows.forEach((key,
//				value) -> setWarningMessage("Deliveries Table: Row " + key.getString() + " skipped (" + value + ")"));
//		skippedDeliveryRelationsRows.forEach((key, value) -> setWarningMessage(
//				"Deliveries Relations Table: Row " + key.getString() + " skipped (" + value + ")"));
 
		JsonValue json = null;
		if(settingsTable!=null) json = Utils.extractJsonValueFromBufferedDataTable(settingsTable, JsonConstants.JSON_COLUMN_SETTINGS);
		
		//BufferedDataContainer container = exec.createDataContainer(configure(null)[0]);
		
		JsonConverter.JsonBuilder jsonBuilder = new JsonConverter.JsonBuilder();
		
//		jsonBuilder.setData(nodeSchema, edgeSchema, nodes, edges, deliveries);
//		jsonBuilder.setTracing(nodes, edges);
		
		jsonBuilder.setData(nodeTable, edgeTable, deliveryRelationTable); //, edgeSchema, nodes, edges, deliveries);
		jsonBuilder.setTracing(nodeTable, edgeTable);
        
		if(json!=null) jsonBuilder.merge(JsonConverter.convertFromJson(json));
		
		
		
//		JsonNodeFactory nodeFactory = JacksonUtils.nodeFactory();
//		ObjectNode rootNode = nodeFactory.objectNode();
//		ArrayNode stationColumnsNode = rootNode.putArray(JsonConstants.STATION_COLUMNS);
//		ArrayNode stationsNode = rootNode.putArray(JsonConstants.STATIONS);
//		ArrayNode deliveryColumnsNode = rootNode.putArray(JsonConstants.DELIVERY_COLUMNS);
//		ArrayNode deliveriesNode = rootNode.putArray(JsonConstants.DELIVERIES);
//		ArrayNode deliveryRelationsNode = rootNode.putArray(JsonConstants.DELIVERY_RELATIONS);
//
//		for (Map.Entry<String, Class<?>> entry: nodeSchema.getMap().entrySet()) {
//		  ObjectNode node = stationColumnsNode.addObject();
//		  node.put(JsonConstants.COLUMN_ID, entry.getKey());
//		  node.put(JsonConstants.COLUMN_TYPE, entry.getValue().getTypeName());
//		}
//		
//		for (Map.Entry<String, Class<?>> entry: edgeSchema.getMap().entrySet()) {
//          ObjectNode node = deliveryColumnsNode.addObject();
//          node.put(JsonConstants.COLUMN_ID, entry.getKey());
//          node.put(JsonConstants.COLUMN_TYPE, entry.getValue().getTypeName());
//        }
//		
//		for (GraphNode s : nodes.values()) {
//			ObjectNode node = stationsNode.addObject();
//
//			//node.put(JsonConstants.ID, createStationId(s.getId()));
//			node.put(JsonConstants.ID, s.getId());
//			//addIfNotNull(node, JsonConstants.NAME, s.getProperties().get(TracingColumns.NAME));
//
//			s.getProperties().forEach((name, value) -> {
//				//if (!JsonConstants.STATION_PROPERTIES.contains(name)) {
//					addIfNotNull(node, name, value);
//				//}
//			});
//		}
//
//		for (Edge<GraphNode> d : edges) {
//			ObjectNode node = deliveriesNode.addObject();
//
//			//node.put(JsonConstants.ID, createDeliveryId(d.getId()));
//			//node.put(JsonConstants.ID, d.getId());
//			//node.put(JsonConstants.SOURCE, createStationId(d.getFrom().getId()));
//			//node.put(JsonConstants.TARGET, createStationId(d.getTo().getId()));
//			//addIfNotNull(node, JsonConstants.NAME, d.getProperties().get(TracingColumns.NAME));
//			//addIfNotNull(node, JsonConstants.LOT, deliveries.get(d.getId()).getLot());
//
////			Integer year = deliveries.get(d.getId()).getDepartureYear();
////			Integer month = deliveries.get(d.getId()).getDepartureMonth();
////			Integer day = deliveries.get(d.getId()).getDepartureDay();
//
////			if (year != null && month != null && day != null) {
////				node.put(JsonConstants.DATE,
////						LocalDate.of(year, Month.of(month), day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
////			}
//
//			d.getProperties().forEach((name, value) -> {
////				if (!JsonConstants.DELIVERY_PROPERTIES.contains(name)) {
//					addIfNotNull(node, name, value);
////				}
//			});
//
//			for (String next : deliveries.get(d.getId()).getAllNextIds()) {
//				ObjectNode n = deliveryRelationsNode.addObject();
//				n.put(, v)
//				n.put(JsonConstants.SOURCE, createDeliveryId(d.getId()));
//				n.put(JsonConstants.TARGET, createDeliveryId(next));
//			}
//		}
//
//		if(configurationTable!=null) {
//		  rootNode.set(JsonConstants.SETTINGS,  JacksonConversions.getInstance().toJackson(Utils.extractJsonValueFromBufferedDataTable(configurationTable)));
//		}
		
//		container.addRowToTable(new DefaultRow(RowKey.createRowKey(0L),
//				JSONCellFactory.create(JacksonConversions.getInstance().toJSR353(rootNode))));
//		container.addRowToTable(new DefaultRow(RowKey.createRowKey(0L),
//          JSONCellFactory.create(jsonBuilder.build())));
//		container.close();
//
//		return new BufferedDataTable[] { container.getTable() };
		return new BufferedDataTable[] { Utils.convertJsonValueToTable(jsonBuilder.build(), exec) };
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
//		return new DataTableSpec[] {
//				new DataTableSpec(new DataColumnSpecCreator(JsonConstants.JSON_COLUMN, JSONCell.TYPE).createSpec()) };
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

//	private static void addIfNotNull(ObjectNode node, String fieldName, Object value) {
//		if (value != null) {
//			if (value instanceof Number) {
//				node.put(fieldName, ((Number) value).doubleValue());
//			} else if (value instanceof Boolean) {
//				node.put(fieldName, ((Boolean) value).booleanValue());
//			} else {
//				node.put(fieldName, value.toString());
//			}
//		}
//	}

//	// ToDo: remove these two functions
//	private static String createStationId(String id) {
//		return JsonConstants.STATION_ID_PREFIX + id;
//	}
//
//	private static String createDeliveryId(String id) {
//		return JsonConstants.DELIVERY_ID_PREFIX + id;
//	}

  @Override
  protected void saveSettingsTo(NodeSettingsWO settings) {
    // do nothing
    
  }

  @Override
  protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
    // do nothing
  }

  @Override
  protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
      throws InvalidSettingsException {
    // do nothing
    
  }
}
