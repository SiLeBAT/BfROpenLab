/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
 ******************************************************************************/
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;

import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.openkrise.MyDelivery;
import de.bund.bfr.knime.openkrise.TracingConstants;
import de.bund.bfr.knime.openkrise.TracingUtils;

public class TracingViewCanvasCreator {

	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;
	private HashMap<Integer, MyDelivery> deliveries;
	private TracingViewSettings set;

	public TracingViewCanvasCreator(BufferedDataTable nodeTable,
			BufferedDataTable edgeTable, HashMap<Integer, MyDelivery> tracing,
			TracingViewSettings set) {
		this.nodeTable = nodeTable;
		this.edgeTable = edgeTable;
		this.deliveries = tracing;
		this.set = set;
	}

	public TracingCanvas createGraphCanvas() throws InvalidSettingsException {
		Map<String, Class<?>> nodeProperties = TracingUtils
				.getTableColumns(nodeTable.getSpec());
		Map<String, Class<?>> edgeProperties = TracingUtils
				.getTableColumns(edgeTable.getSpec());

		if (!nodeProperties.containsKey(TracingConstants.WEIGHT_COLUMN)) {
			nodeProperties.put(TracingConstants.WEIGHT_COLUMN, Double.class);
		}

		if (!nodeProperties
				.containsKey(TracingConstants.CROSS_CONTAMINATION_COLUMN)) {
			nodeProperties.put(TracingConstants.CROSS_CONTAMINATION_COLUMN,
					Boolean.class);
		}

		if (!nodeProperties.containsKey(TracingConstants.SCORE_COLUMN)) {
			nodeProperties.put(TracingConstants.SCORE_COLUMN, Double.class);
		}

		if (!nodeProperties.containsKey(TracingConstants.OBSERVED_COLUMN)) {
			nodeProperties.put(TracingConstants.OBSERVED_COLUMN, Boolean.class);
		}

		if (!nodeProperties.containsKey(TracingConstants.BACKWARD_COLUMN)) {
			nodeProperties.put(TracingConstants.BACKWARD_COLUMN, Boolean.class);
		}

		if (!nodeProperties.containsKey(TracingConstants.FORWARD_COLUMN)) {
			nodeProperties.put(TracingConstants.FORWARD_COLUMN, Boolean.class);
		}

		if (!edgeProperties.containsKey(TracingConstants.WEIGHT_COLUMN)) {
			edgeProperties.put(TracingConstants.WEIGHT_COLUMN, Double.class);
		}

		if (!edgeProperties
				.containsKey(TracingConstants.CROSS_CONTAMINATION_COLUMN)) {
			edgeProperties.put(TracingConstants.CROSS_CONTAMINATION_COLUMN,
					Boolean.class);
		}

		if (!edgeProperties.containsKey(TracingConstants.OBSERVED_COLUMN)) {
			edgeProperties.put(TracingConstants.OBSERVED_COLUMN, Boolean.class);
		}

		if (!edgeProperties.containsKey(TracingConstants.SCORE_COLUMN)) {
			edgeProperties.put(TracingConstants.SCORE_COLUMN, Double.class);
		}

		if (!edgeProperties.containsKey(TracingConstants.BACKWARD_COLUMN)) {
			edgeProperties.put(TracingConstants.BACKWARD_COLUMN, Boolean.class);
		}

		if (!edgeProperties.containsKey(TracingConstants.FORWARD_COLUMN)) {
			edgeProperties.put(TracingConstants.FORWARD_COLUMN, Boolean.class);
		}

		Map<String, GraphNode> nodes = TracingUtils.readGraphNodes(nodeTable,
				nodeProperties);
		List<Edge<GraphNode>> edges = TracingUtils.readEdges(edgeTable,
				edgeProperties, nodes);
		TracingCanvas canvas = new TracingCanvas(
				new ArrayList<>(nodes.values()), edges, nodeProperties,
				edgeProperties, deliveries);

		canvas.setNodeName(TracingConstants.NODE_NAME);
		canvas.setEdgeName(TracingConstants.EDGE_NAME);
		canvas.setNodesName(TracingConstants.NODES_NAME);
		canvas.setEdgesName(TracingConstants.EDGES_NAME);
		canvas.setPerformTracing(false);
		set.getGraphSettings().setToCanvas(canvas, nodeProperties,
				edgeProperties, false);
		canvas.setLabel(set.getLabel());
		canvas.setNodeWeights(set.getNodeWeights());
		canvas.setEdgeWeights(set.getEdgeWeights());
		canvas.setNodeCrossContaminations(set.getNodeCrossContaminations());
		canvas.setEdgeCrossContaminations(set.getEdgeCrossContaminations());
		canvas.setObservedNodes(set.getObservedNodes());
		canvas.setObservedEdges(set.getObservedEdges());
		canvas.setEnforceTemporalOrder(set.isEnforeTemporalOrder());
		canvas.setShowForward(set.isShowForward());
		canvas.setPerformTracing(true);
		canvas.setNodePositions(set.getGraphSettings().getNodePositions());

		return canvas;
	}
}
