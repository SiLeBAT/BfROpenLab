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
package de.bund.bfr.knime.gis.views.locationtolocationvisualizer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.BufferedDataTable;

import de.bund.bfr.knime.KnimeUtilities;
import de.bund.bfr.knime.gis.views.ViewUtilities;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvas;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;

public class LocationToLocationVisualizerCanvasCreator {

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;
	private LocationToLocationVisualizerSettings set;

	private Set<String> connectedNodes;

	public LocationToLocationVisualizerCanvasCreator(
			BufferedDataTable shapeTable, BufferedDataTable nodeTable,
			BufferedDataTable edgeTable,
			LocationToLocationVisualizerSettings set) {
		this.shapeTable = shapeTable;
		this.nodeTable = nodeTable;
		this.edgeTable = edgeTable;
		this.set = set;

		connectedNodes = ViewUtilities.getConnectedNodes(nodeTable,
				set.getNodeIdColumn(), edgeTable, set.getEdgeFromColumn(),
				set.getEdgeToColumn());
	}

	public GraphCanvas createGraphCanvas() {
		Map<String, Class<?>> nodeProperties = KnimeUtilities
				.getTableColumns(nodeTable.getSpec());
		Map<String, Class<?>> edgeProperties = KnimeUtilities
				.getTableColumns(edgeTable.getSpec());
		Map<String, GraphNode> nodes = ViewUtilities.readGraphNodes(nodeTable,
				nodeProperties, connectedNodes, set.getNodeIdColumn(), null,
				set.isSkipEdgelessNodes());

		if (nodes.isEmpty()) {
			return null;
		}

		List<Edge<GraphNode>> edges = ViewUtilities.readEdges(edgeTable,
				edgeProperties, nodes, null, set.getEdgeFromColumn(),
				set.getEdgeToColumn());
		String edgeIdProperty = ViewUtilities.createNewIdProperty(edges,
				edgeProperties);
		GraphCanvas canvas = new GraphCanvas(new ArrayList<GraphNode>(
				nodes.values()), edges, nodeProperties, edgeProperties,
				set.getNodeIdColumn(), edgeIdProperty, set.getEdgeFromColumn(),
				set.getEdgeToColumn());

		canvas.setAllowCollapse(false);
		canvas.setCanvasSize(set.getGraphCanvasSize());
		canvas.setEditingMode(set.getGraphEditingMode());
		canvas.setNodeSize(set.getGraphNodeSize());
		canvas.setLayoutType(set.getGraphLayout());
		canvas.setJoinEdges(set.isJoinEdges());
		canvas.setNodeHighlightConditions(set.getGraphNodeHighlightConditions());
		canvas.setEdgeHighlightConditions(set.getGraphEdgeHighlightConditions());
		canvas.setSelectedNodeIds(new LinkedHashSet<String>(set
				.getGraphSelectedNodes()));
		canvas.setSelectedEdgeIds(new LinkedHashSet<String>(set
				.getGraphSelectedEdges()));		

		if (!Double.isNaN(set.getGraphScaleX())
				&& !Double.isNaN(set.getGraphScaleY())
				&& !Double.isNaN(set.getGraphTranslationX())
				&& !Double.isNaN(set.getGraphTranslationY())) {
			canvas.setTransform(set.getGraphScaleX(), set.getGraphScaleY(),
					set.getGraphTranslationX(), set.getGraphTranslationY());
		}
		
		canvas.setNodePositions(set.getGraphNodePositions());

		return canvas;
	}

	public LocationCanvas createLocationCanvas() {
		List<RegionNode> regionNodes = ViewUtilities.readRegionNodes(
				shapeTable, set.getShapeColumn());
		Map<String, Class<?>> nodeProperties = KnimeUtilities
				.getTableColumns(nodeTable.getSpec());
		Map<String, Class<?>> edgeProperties = KnimeUtilities
				.getTableColumns(edgeTable.getSpec());
		Map<String, LocationNode> nodes = ViewUtilities.readLocationNodes(
				nodeTable, nodeProperties, connectedNodes,
				set.getNodeIdColumn(), set.getNodeLatitudeColumn(),
				set.getNodeLongitudeColumn(), set.isSkipEdgelessNodes());

		if (nodes.isEmpty()) {
			return null;
		}

		List<Edge<LocationNode>> edges = ViewUtilities.readEdges(edgeTable,
				edgeProperties, nodes, null, set.getEdgeFromColumn(),
				set.getEdgeToColumn());
		String edgeIdProperty = ViewUtilities.createNewIdProperty(edges,
				edgeProperties);
		LocationCanvas canvas = new LocationCanvas(new ArrayList<LocationNode>(
				nodes.values()), edges, nodeProperties, edgeProperties,
				set.getNodeIdColumn(), edgeIdProperty, set.getEdgeFromColumn(),
				set.getEdgeToColumn(), regionNodes);

		canvas.setCanvasSize(set.getGisCanvasSize());
		canvas.setEditingMode(set.getGisEditingMode());
		canvas.setBorderAlpha(set.getGisBorderAlpha());
		canvas.setLocationSize(set.getGisLocationSize());
		canvas.setJoinEdges(set.isJoinEdges());
		canvas.setNodeHighlightConditions(set.getGraphNodeHighlightConditions());
		canvas.setEdgeHighlightConditions(set.getGraphEdgeHighlightConditions());
		canvas.setSelectedNodeIds(new LinkedHashSet<String>(set
				.getGraphSelectedNodes()));
		canvas.setSelectedEdgeIds(new LinkedHashSet<String>(set
				.getGraphSelectedEdges()));

		if (!Double.isNaN(set.getGisScaleX())
				&& !Double.isNaN(set.getGisScaleY())
				&& !Double.isNaN(set.getGisTranslationX())
				&& !Double.isNaN(set.getGisTranslationY())) {
			canvas.setTransform(set.getGisScaleX(), set.getGisScaleY(),
					set.getGisTranslationX(), set.getGisTranslationY());
		}

		return canvas;
	}
}
