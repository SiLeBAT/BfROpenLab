/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
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
package de.bund.bfr.knime.openkrise.views.tracingview2;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.BufferedDataTable;

import de.bund.bfr.knime.KnimeUtilities;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.openkrise.MyNewTracing;
import de.bund.bfr.knime.openkrise.views.TracingConstants;
import de.bund.bfr.knime.openkrise.views.TracingUtilities;

public class TracingView2CanvasCreator {

	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;
	private MyNewTracing tracing;
	private TracingView2Settings set;

	private Set<Integer> connectedNodes;
	private Set<Integer> simpleSuppliers;

	public TracingView2CanvasCreator(BufferedDataTable nodeTable,
			BufferedDataTable edgeTable, MyNewTracing tracing,
			TracingView2Settings set) {
		this.nodeTable = nodeTable;
		this.edgeTable = edgeTable;
		this.tracing = tracing;
		this.set = set;

		connectedNodes = TracingUtilities.getConnectedNodes(nodeTable,
				edgeTable);
		simpleSuppliers = TracingUtilities.getSimpleSuppliers(nodeTable,
				edgeTable);
	}

	public TracingCanvas createGraphCanvas() {
		Map<String, Class<?>> nodeProperties = KnimeUtilities
				.getTableColumns(nodeTable.getSpec());
		Map<String, Class<?>> edgeProperties = KnimeUtilities
				.getTableColumns(edgeTable.getSpec());

		if (!nodeProperties.containsKey(TracingConstants.CASE_WEIGHT_COLUMN)) {
			nodeProperties.put(TracingConstants.CASE_WEIGHT_COLUMN,
					Double.class);
		}

		if (!nodeProperties
				.containsKey(TracingConstants.CROSS_CONTAMINATION_COLUMN)) {
			nodeProperties.put(TracingConstants.CROSS_CONTAMINATION_COLUMN,
					Boolean.class);
		}

		if (!nodeProperties.containsKey(TracingConstants.SCORE_COLUMN)) {
			nodeProperties.put(TracingConstants.SCORE_COLUMN, Double.class);
		}

		if (!nodeProperties.containsKey(TracingConstants.FILTER_COLUMN)) {
			nodeProperties.put(TracingConstants.FILTER_COLUMN, Boolean.class);
		}

		if (!nodeProperties.containsKey(TracingConstants.BACKWARD_COLUMN)) {
			nodeProperties.put(TracingConstants.BACKWARD_COLUMN, Boolean.class);
		}

		if (!nodeProperties.containsKey(TracingConstants.FORWARD_COLUMN)) {
			nodeProperties.put(TracingConstants.FORWARD_COLUMN, Boolean.class);
		}

		if (!nodeProperties
				.containsKey(TracingConstants.SIMPLE_SUPPLIER_COLUMN)) {
			nodeProperties.put(TracingConstants.SIMPLE_SUPPLIER_COLUMN,
					Boolean.class);
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

		Map<Integer, GraphNode> nodes = TracingUtilities.readGraphNodes(
				nodeTable, nodeProperties, connectedNodes,
				set.isSkipEdgelessNodes());

		if (nodes.isEmpty()) {
			return null;
		}

		for (GraphNode node : nodes.values()) {
			node.getProperties().put(TracingConstants.SIMPLE_SUPPLIER_COLUMN,
					simpleSuppliers.contains(Integer.parseInt(node.getId())));
		}

		List<Edge<GraphNode>> edges = TracingUtilities.readEdges(edgeTable,
				edgeProperties, nodes, set.isJoinEdges());
		TracingCanvas canvas = new TracingCanvas(new ArrayList<GraphNode>(
				nodes.values()), edges, nodeProperties, edgeProperties,
				tracing, set.isJoinEdges(), set.isEnforeTemporalOrder());

		canvas.setCanvasSize(set.getGraphCanvasSize());
		canvas.setLayoutType(set.getGraphLayout());
		canvas.setNodePositions(set.getGraphNodePositions());
		canvas.setAllowCollapse(true);
		canvas.setEditingMode(set.getGraphEditingMode());
		canvas.setNodeSize(set.getGraphNodeSize());
		canvas.setCollapsedNodes(set.getCollapsedNodes());
		canvas.setSelectedNodeIds(new LinkedHashSet<String>(set
				.getGraphSelectedNodes()));
		canvas.setSelectedEdgeIds(new LinkedHashSet<String>(set
				.getGraphSelectedEdges()));
		canvas.setNodeHighlightConditions(set.getGraphNodeHighlightConditions());
		canvas.setEdgeHighlightConditions(set.getGraphEdgeHighlightConditions());
		canvas.setCaseWeights(set.getCaseWeights());
		canvas.setCrossContaminations(set.getCrossContaminations());
		canvas.setFilter(set.getFilter());

		if (!Double.isNaN(set.getGraphScaleX())
				&& !Double.isNaN(set.getGraphScaleY())
				&& !Double.isNaN(set.getGraphTranslationX())
				&& !Double.isNaN(set.getGraphTranslationY())) {
			canvas.setTransform(set.getGraphScaleX(), set.getGraphScaleY(),
					set.getGraphTranslationX(), set.getGraphTranslationY());
		}

		return canvas;
	}
}
