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
package de.bund.bfr.knime.gis.views.regiontoregionvisualizer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.BufferedDataTable;

import com.vividsolutions.jts.geom.MultiPolygon;

import de.bund.bfr.knime.KnimeUtilities;
import de.bund.bfr.knime.gis.views.ViewUtilities;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.RegionCanvas;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;

public class RegionToRegionVisualizerCanvasCreator {

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;
	private RegionToRegionVisualizerSettings set;

	private Map<String, String> idToRegionMap;
	private Set<String> nonExistingRegions;

	public RegionToRegionVisualizerCanvasCreator(BufferedDataTable shapeTable,
			BufferedDataTable nodeTable, BufferedDataTable edgeTable,
			RegionToRegionVisualizerSettings set) {
		this.shapeTable = shapeTable;
		this.nodeTable = nodeTable;
		this.edgeTable = edgeTable;
		this.set = set;

		idToRegionMap = ViewUtilities.getIdToRegionMap(nodeTable,
				set.getNodeIdColumn(), set.getNodeRegionColumn());
		nonExistingRegions = new LinkedHashSet<>();
	}

	public GraphCanvas createGraphCanvas() {
		Map<String, Class<?>> nodeProperties = KnimeUtilities
				.getTableColumns(nodeTable.getSpec());
		Map<String, Class<?>> edgeProperties = KnimeUtilities
				.getTableColumns(edgeTable.getSpec());
		Map<String, GraphNode> nodes = ViewUtilities.readGraphNodes(nodeTable,
				nodeProperties, set.getNodeIdColumn(),
				set.getNodeRegionColumn());

		if (nodes.isEmpty()) {
			return null;
		}

		List<Edge<GraphNode>> edges = ViewUtilities.readEdges(edgeTable,
				edgeProperties, nodes, null, set.getEdgeFromColumn(),
				set.getEdgeToColumn());
		String edgeIdProperty = ViewUtilities.createNewIdProperty(edges,
				edgeProperties);
		GraphCanvas canvas = new GraphCanvas(new ArrayList<>(nodes.values()),
				edges, nodeProperties, edgeProperties, set.getNodeIdColumn(),
				edgeIdProperty, set.getEdgeFromColumn(), set.getEdgeToColumn(),
				false);

		canvas.setShowLegend(set.isGraphShowLegend());
		canvas.setCanvasSize(set.getGraphCanvasSize());
		canvas.setEditingMode(set.getGraphEditingMode());
		canvas.setNodeSize(set.getGraphNodeSize());
		canvas.setFontSize(set.getGraphFontSize());
		canvas.setFontBold(set.isGraphFontBold());
		canvas.setJoinEdges(set.isJoinEdges());
		canvas.setNodeHighlightConditions(set.getGraphNodeHighlightConditions());
		canvas.setEdgeHighlightConditions(set.getGraphEdgeHighlightConditions());
		canvas.setSkipEdgelessNodes(set.isSkipEdgelessNodes());
		canvas.setSelectedNodeIds(new LinkedHashSet<>(set
				.getGraphSelectedNodes()));
		canvas.setSelectedEdgeIds(new LinkedHashSet<>(set
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

	public RegionCanvas createGISCanvas(GraphCanvas graphCanvas) {
		Map<String, MultiPolygon> polygonMap = ViewUtilities.readPolygons(
				shapeTable, set.getShapeColumn(), set.getShapeRegionColumn());
		Map<String, Class<?>> nodeProperties = KnimeUtilities
				.getTableColumns(nodeTable.getSpec());
		Map<String, Class<?>> edgeProperties = KnimeUtilities
				.getTableColumns(edgeTable.getSpec());
		Map<String, RegionNode> nodes = ViewUtilities.readRegionNodes(
				nodeTable, nodeProperties, polygonMap, idToRegionMap,
				set.getNodeIdColumn(), nonExistingRegions);

		if (nodes.isEmpty()) {
			return null;
		}

		List<Edge<RegionNode>> edges = ViewUtilities.readEdges(edgeTable,
				edgeProperties, nodes, idToRegionMap, set.getEdgeFromColumn(),
				set.getEdgeToColumn());
		String edgeIdProperty = ViewUtilities.createNewIdProperty(edges,
				edgeProperties);
		RegionCanvas canvas = new RegionCanvas(new ArrayList<>(nodes.values()),
				edges, nodeProperties, edgeProperties, set.getNodeIdColumn(),
				edgeIdProperty, set.getEdgeFromColumn(), set.getEdgeToColumn());

		canvas.setShowLegend(set.isGisShowLegend());
		canvas.setCanvasSize(set.getGisCanvasSize());
		canvas.setEditingMode(set.getGisEditingMode());
		canvas.setFontSize(set.getGisFontSize());
		canvas.setFontBold(set.isGisFontBold());
		canvas.setBorderAlpha(set.getGisBorderAlpha());
		canvas.setJoinEdges(set.isJoinEdges());
		canvas.setNodeHighlightConditions(set.getGisNodeHighlightConditions());
		canvas.setEdgeHighlightConditions(set.getGisEdgeHighlightConditions());
		canvas.setSkipEdgelessNodes(set.isSkipEdgelessNodes());
		canvas.setSelectedNodes(getSelectedGisNodes(canvas.getNodes(),
				graphCanvas.getSelectedNodes()));
		canvas.setSelectedEdges(getSelectedGisEdges(canvas.getEdges(),
				graphCanvas.getSelectedEdges(), set.isJoinEdges()));

		if (!Double.isNaN(set.getGisScaleX())
				&& !Double.isNaN(set.getGisScaleY())
				&& !Double.isNaN(set.getGisTranslationX())
				&& !Double.isNaN(set.getGisTranslationY())) {
			canvas.setTransform(set.getGisScaleX(), set.getGisScaleY(),
					set.getGisTranslationX(), set.getGisTranslationY());
		}

		return canvas;
	}

	public Set<String> getNonExistingRegions() {
		return nonExistingRegions;
	}

	public static Set<RegionNode> getSelectedGisNodes(Set<RegionNode> gisNodes,
			Set<GraphNode> selectedGraphNodes) {
		Set<RegionNode> selectedGisNodes = new LinkedHashSet<>();
		Map<String, RegionNode> gisNodesByRegion = new LinkedHashMap<>();

		for (RegionNode gisNode : gisNodes) {
			gisNodesByRegion.put(gisNode.getId(), gisNode);
		}

		for (GraphNode graphNode : selectedGraphNodes) {
			RegionNode gisNode = gisNodesByRegion.get(graphNode.getRegion());

			if (gisNode != null) {
				selectedGisNodes.add(gisNode);
			}
		}

		return selectedGisNodes;
	}

	public static Set<Edge<RegionNode>> getSelectedGisEdges(
			Set<Edge<RegionNode>> gisEdges,
			Set<Edge<GraphNode>> graphSelectedEdges, boolean joinEdges) {
		Set<Edge<RegionNode>> selectedGisEdges = new LinkedHashSet<>();

		if (!joinEdges) {
			Map<String, Edge<RegionNode>> gisEdgesById = new LinkedHashMap<>();

			for (Edge<RegionNode> gisEdge : gisEdges) {
				gisEdgesById.put(gisEdge.getId(), gisEdge);
			}

			for (Edge<GraphNode> graphEdge : graphSelectedEdges) {
				selectedGisEdges.add(gisEdgesById.get(graphEdge.getId()));
			}
		} else {
			Map<String, Map<String, Edge<RegionNode>>> gisEdgesByRegion = new LinkedHashMap<>();

			for (Edge<RegionNode> gisEdge : gisEdges) {
				String fromRegion = gisEdge.getFrom().getId();
				String toRegion = gisEdge.getTo().getId();

				if (!gisEdgesByRegion.containsKey(fromRegion)) {
					gisEdgesByRegion.put(fromRegion,
							new LinkedHashMap<String, Edge<RegionNode>>());
				}

				gisEdgesByRegion.get(fromRegion).put(toRegion, gisEdge);
			}

			for (Edge<GraphNode> graphEdge : graphSelectedEdges) {
				String fromRegion = graphEdge.getFrom().getRegion();
				String toRegion = graphEdge.getTo().getRegion();

				if (gisEdgesByRegion.containsKey(fromRegion)) {
					Edge<RegionNode> gisEdge = gisEdgesByRegion.get(fromRegion)
							.get(toRegion);

					if (gisEdge != null) {
						selectedGisEdges.add(gisEdge);
					}
				}
			}
		}

		return selectedGisEdges;
	}
}
