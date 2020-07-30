/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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

import java.util.LinkedHashMap;
import java.util.Map;

import org.knime.core.data.RowKey;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
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
import org.knime.network.core.knime.node.AbstractGraphNodeModel;
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
public class ToKnimeNetworkNodeModel extends AbstractGraphNodeModel {

	protected static final String CFG_ADD_PREFIX = "AddPrefix";

	private static final String NET_ID = "FoodChainLab-Network";
	private static final String NET_URI = "FoodChainLab-Network";

	private SettingsModelBoolean addPrefix;

	/**
	 * Constructor for the node model.
	 */
	protected ToKnimeNetworkNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE },
				new PortType[] { GraphPortObject.TYPE });
		addPrefix = new SettingsModelBoolean(CFG_ADD_PREFIX, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] executeInternal(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		BufferedDataTable nodeTable = (BufferedDataTable) inObjects[0];
		BufferedDataTable edgeTable = (BufferedDataTable) inObjects[1];
		NodePropertySchema nodeSchema = new NodePropertySchema(TracingUtils.getTableColumns(nodeTable.getSpec()),
				TracingColumns.ID);
		EdgePropertySchema edgeSchema = new EdgePropertySchema(TracingUtils.getTableColumns(edgeTable.getSpec()),
				TracingColumns.ID, TracingColumns.FROM, TracingColumns.TO);
		Map<String, GraphNode> nodes = TracingUtils.readGraphNodes(nodeTable, nodeSchema);
		Map<RowKey, String> skippedDeliveryRows = new LinkedHashMap<>();
		Map<String, Edge<GraphNode>> edges = CanvasUtils
				.getElementsById(TracingUtils.readEdges(edgeTable, edgeSchema, nodes, skippedDeliveryRows));

		skippedDeliveryRows.forEach((key,
				value) -> setWarningMessage("Deliveries Table: Row " + key.getString() + " skipped (" + value + ")"));

		if (!addPrefix.getBooleanValue() && !Sets.intersection(nodes.keySet(), edges.keySet()).isEmpty()) {
			throw new Exception(
					"Some stations and deliveries are using the same IDs. Therefore you must enable \"Add Prefix to Node and Edge IDs\".");
		}

		KPartiteGraph<PersistentObject, Partition> net = GraphFactory.createNet(NET_ID, NET_URI);
		String nodeIdPrefix = addPrefix.getBooleanValue() ? "Node:" : "";
		String edgeIdPrefix = addPrefix.getBooleanValue() ? "Edge:" : "";

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
	protected void resetInternal() {
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
		addPrefix.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		addPrefix.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		addPrefix.validateSettings(settings);
	}
}
