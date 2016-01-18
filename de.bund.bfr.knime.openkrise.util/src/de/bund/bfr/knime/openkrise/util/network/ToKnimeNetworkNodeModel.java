/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.openkrise.util.network;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.RowKey;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.network.core.api.KPartiteGraph;
import org.knime.network.core.api.Partition;
import org.knime.network.core.api.PersistentObject;
import org.knime.network.core.core.EndObject;
import org.knime.network.core.core.GraphFactory;
import org.knime.network.core.core.GraphMetaData;
import org.knime.network.core.core.feature.StandardFeature;
import org.knime.network.core.knime.port.GraphPortObject;
import org.knime.network.core.knime.port.GraphPortObjectSpec;

import com.google.common.collect.Sets;

import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.TracingUtils;

/**
 * This is the model implementation of ToKnimeNetwork.
 * 
 *
 * @author Christian Thoens
 */
public class ToKnimeNetworkNodeModel extends NodeModel {

	private static final String NET_ID = "FoodChainLab-Network";
	private static final String NET_URI = "FoodChainLab-Network";

	/**
	 * Constructor for the node model.
	 */
	protected ToKnimeNetworkNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE },
				new PortType[] { GraphPortObject.TYPE });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		BufferedDataTable nodeTable = (BufferedDataTable) inObjects[0];
		BufferedDataTable edgeTable = (BufferedDataTable) inObjects[1];
		NodePropertySchema nodeSchema = new NodePropertySchema(TracingUtils.getTableColumns(nodeTable.getSpec()),
				TracingColumns.ID);
		EdgePropertySchema edgeSchema = new EdgePropertySchema(TracingUtils.getTableColumns(edgeTable.getSpec()),
				TracingColumns.ID, TracingColumns.FROM, TracingColumns.TO);
		Map<String, GraphNode> nodes = TracingUtils.readGraphNodes(nodeTable, nodeSchema);
		Set<RowKey> skippedEdgeRows = new LinkedHashSet<>();
		Map<String, Edge<GraphNode>> edges = CanvasUtils
				.getElementsById(TracingUtils.readEdges(edgeTable, edgeSchema, nodes, skippedEdgeRows));

		for (RowKey key : skippedEdgeRows) {
			setWarningMessage("Delivery Table: Row " + key.getString() + " skipped");
		}

		boolean idsUnique = Sets.intersection(nodes.keySet(), edges.keySet()).isEmpty();
		String nodeIdPrefix = "";
		String edgeIdPrefix = "";

		if (!idsUnique) {
			nodeIdPrefix = "Node:";
			edgeIdPrefix = "Edge:";
			setWarningMessage("Some stations and deliveries are using the same IDs. Therefore a prefix will be added.");
		}

		KPartiteGraph<PersistentObject, Partition> net = GraphFactory.createNet(NET_ID, NET_URI);

		net.defineFeature(StandardFeature.IS_TARGET);

		for (Edge<GraphNode> e : edges.values()) {
			PersistentObject from = net.createNode(nodeIdPrefix + e.getFrom().getId());
			PersistentObject to = net.createNode(nodeIdPrefix + e.getTo().getId());
			PersistentObject edge = net.createEdge(edgeIdPrefix + e.getId(), from, to);

			net.addFeature(new EndObject(to, edge), StandardFeature.IS_TARGET.getName(), Boolean.TRUE);
		}

		return new PortObject[] { new GraphPortObject<>(net) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		return new PortObjectSpec[] { new GraphPortObjectSpec(
				new GraphMetaData(new GraphMetaData(NET_ID, NET_URI), StandardFeature.IS_TARGET)) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}
}
