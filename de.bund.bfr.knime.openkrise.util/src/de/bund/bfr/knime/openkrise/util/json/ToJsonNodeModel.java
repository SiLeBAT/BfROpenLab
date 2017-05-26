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
import org.knime.core.data.json.JSONCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NoSettingsNodeModel;

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

		// TODO create JSON and write in one cell

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
}
