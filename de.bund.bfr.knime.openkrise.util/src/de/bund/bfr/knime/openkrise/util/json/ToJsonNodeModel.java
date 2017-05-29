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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.json.JSONCell;
import org.knime.core.data.json.JSONCellFactory;
import org.knime.core.data.json.JacksonConversions;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NoSettingsNodeModel;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JacksonUtils;

import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.TracingUtils;
import de.bund.bfr.knime.openkrise.common.Delivery;

/**
 * This is the model implementation of ToKnimeNetwork.
 * 
 *
 * @author Christian Thoens
 */
public class ToJsonNodeModel extends NoSettingsNodeModel {

	private static final String STATIONS = "stations";
	private static final String DELIVERIES = "deliveries";
	private static final String DELIVERY_RELATIONS = "deliveriesRelations";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String SOURCE = "source";
	private static final String TARGET = "target";
	private static final String LOT = "lot";

	/**
	 * Constructor for the node model.
	 */
	protected ToJsonNodeModel() {
		super(3, 1);
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		BufferedDataTable nodeTable = inData[0];
		BufferedDataTable edgeTable = inData[1];
		BufferedDataTable tracingTable = inData[2];
		NodePropertySchema nodeSchema = new NodePropertySchema(TracingUtils.getTableColumns(nodeTable.getSpec()),
				TracingColumns.ID);
		EdgePropertySchema edgeSchema = new EdgePropertySchema(TracingUtils.getTableColumns(edgeTable.getSpec()),
				TracingColumns.ID, TracingColumns.FROM, TracingColumns.TO);
		Map<RowKey, String> skippedDeliveryRows = new LinkedHashMap<>();
		Map<RowKey, String> skippedDeliveryRelationsRows = new LinkedHashMap<>();

		Map<String, GraphNode> nodes = TracingUtils.readGraphNodes(nodeTable, nodeSchema);
		List<Edge<GraphNode>> edges = TracingUtils.readEdges(edgeTable, edgeSchema, nodes, skippedDeliveryRows);
		Map<String, Delivery> deliveries = TracingUtils.readDeliveries(tracingTable, edges,
				skippedDeliveryRelationsRows);

		skippedDeliveryRows.forEach((key,
				value) -> setWarningMessage("Deliveries Table: Row " + key.getString() + " skipped (" + value + ")"));
		skippedDeliveryRelationsRows.forEach((key, value) -> setWarningMessage(
				"Deliveries Relations Table: Row " + key.getString() + " skipped (" + value + ")"));

		BufferedDataContainer container = exec.createDataContainer(configure(null)[0]);
		JsonNodeFactory nodeFactory = JacksonUtils.nodeFactory();
		ObjectNode rootNode = nodeFactory.objectNode();
		ArrayNode stationsNode = rootNode.putArray(STATIONS);
		ArrayNode deliveriesNode = rootNode.putArray(DELIVERIES);
		ArrayNode deliveryRelationsNode = rootNode.putArray(DELIVERY_RELATIONS);

		for (GraphNode s : nodes.values()) {
			ObjectNode node = stationsNode.addObject();

			node.put(ID, createStationId(s.getId()));
			addIfNotNull(node, NAME, (String) s.getProperties().get(TracingColumns.NAME));
		}

		for (Edge<GraphNode> d : edges) {
			ObjectNode node = deliveriesNode.addObject();
			Delivery dd = deliveries.get(d.getId());

			node.put(ID, createDeliveryId(dd.getId()));
			node.put(SOURCE, createStationId(dd.getSupplierId()));
			node.put(TARGET, createStationId(dd.getRecipientId()));
			addIfNotNull(node, NAME, (String) d.getProperties().get(TracingColumns.NAME));
			addIfNotNull(node, LOT, dd.getLot());

			for (String next : dd.getAllNextIds()) {
				ObjectNode n = deliveryRelationsNode.addObject();

				n.put(SOURCE, createDeliveryId(dd.getId()));
				n.put(TARGET, createDeliveryId(next));
			}
		}

		container.addRowToTable(new DefaultRow(RowKey.createRowKey(0L),
				JSONCellFactory.create(JacksonConversions.getInstance().toJSR353(rootNode))));
		container.close();

		return new BufferedDataTable[] { container.getTable() };
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		return new DataTableSpec[] { new DataTableSpec(new DataColumnSpecCreator("JSON", JSONCell.TYPE).createSpec()) };
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	private static void addIfNotNull(ObjectNode node, String fieldName, String value) {
		if (value != null) {
			node.put(fieldName, value);
		}
	}

	private static String createStationId(String id) {
		return "S" + id;
	}

	private static String createDeliveryId(String id) {
		return "D" + id;
	}
}
