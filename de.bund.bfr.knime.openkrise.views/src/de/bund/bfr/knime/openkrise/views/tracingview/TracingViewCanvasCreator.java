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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.BufferedDataTable;

import de.bund.bfr.knime.KnimeUtilities;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.openkrise.views.TracingConstants;
import de.bund.bfr.knime.openkrise.views.TracingUtilities;

public class TracingViewCanvasCreator {

	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;
	private TracingViewSettings set;

	private Set<Integer> connectedNodes;

	public TracingViewCanvasCreator(BufferedDataTable nodeTable,
			BufferedDataTable edgeTable, TracingViewSettings set) {
		this.nodeTable = nodeTable;
		this.edgeTable = edgeTable;
		this.set = set;

		connectedNodes = TracingUtilities.getConnectedNodes(nodeTable,
				edgeTable);
	}

	public GraphCanvas createGraphCanvas() {
		Map<String, Class<?>> nodeProperties = KnimeUtilities
				.getTableColumns(nodeTable.getSpec());
		Map<String, Class<?>> edgeProperties = KnimeUtilities
				.getTableColumns(edgeTable.getSpec());
		Map<Integer, GraphNode> nodes = TracingUtilities.readGraphNodes(
				nodeTable, nodeProperties, connectedNodes,
				set.isSkipEdgelessNodes());

		if (nodes.isEmpty()) {
			return null;
		}

		List<Edge<GraphNode>> edges = TracingUtilities.readEdges(edgeTable,
				edgeProperties, nodes, set.isJoinEdges());
		GraphCanvas canvas = new GraphCanvas(new ArrayList<GraphNode>(
				nodes.values()), edges, nodeProperties, edgeProperties,
				TracingConstants.ID_COLUMN, TracingConstants.ID_COLUMN,
				TracingConstants.FROM_COLUMN, TracingConstants.TO_COLUMN);

		canvas.setCanvasSize(set.getGraphCanvasSize());
		canvas.setLayoutType(set.getGraphLayout());
		canvas.setNodePositions(set.getGraphNodePositions());
		canvas.setAllowCollapse(true);
		canvas.setEditingMode(set.getGraphEditingMode());
		canvas.setNodeSize(set.getGraphNodeSize());
		canvas.setJoinEdges(set.isJoinEdges());
		canvas.setCollapsedNodes(set.getCollapsedNodes());
		canvas.setSelectedNodeIds(new LinkedHashSet<String>(set
				.getGraphSelectedNodes()));
		canvas.setSelectedEdgeIds(new LinkedHashSet<String>(set
				.getGraphSelectedEdges()));
		canvas.setNodeHighlightConditions(set.getGraphNodeHighlightConditions());
		canvas.setEdgeHighlightConditions(set.getGraphEdgeHighlightConditions());

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
