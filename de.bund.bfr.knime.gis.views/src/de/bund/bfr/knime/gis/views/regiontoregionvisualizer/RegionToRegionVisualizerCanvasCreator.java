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
package de.bund.bfr.knime.gis.views.regiontoregionvisualizer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.BufferedDataTable;

import com.vividsolutions.jts.geom.MultiPolygon;

import de.bund.bfr.knime.gis.views.ViewUtils;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
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

		idToRegionMap = ViewUtils.getIdToRegionMap(nodeTable, set
				.getGraphSettings().getNodeIdColumn(), set.getGisSettings()
				.getNodeRegionColumn());
		nonExistingRegions = new LinkedHashSet<>();
	}

	public GraphCanvas createGraphCanvas() {
		Map<String, Class<?>> nodeProperties = ViewUtils
				.getTableColumns(nodeTable.getSpec());
		Map<String, Class<?>> edgeProperties = ViewUtils
				.getTableColumns(edgeTable.getSpec());
		Map<String, GraphNode> nodes = ViewUtils.readGraphNodes(nodeTable,
				nodeProperties, set.getGraphSettings().getNodeIdColumn(), set
						.getGisSettings().getNodeRegionColumn());

		if (nodes.isEmpty()) {
			return null;
		}

		List<Edge<GraphNode>> edges = ViewUtils.readEdges(edgeTable,
				edgeProperties, nodes, null, set.getGraphSettings()
						.getEdgeFromColumn(), set.getGraphSettings()
						.getEdgeToColumn());
		String edgeIdProperty = ViewUtils.createNewIdProperty(edges,
				edgeProperties);
		GraphCanvas canvas = new GraphCanvas(new ArrayList<>(nodes.values()),
				edges, nodeProperties, edgeProperties, set.getGraphSettings()
						.getNodeIdColumn(), edgeIdProperty, set
						.getGraphSettings().getEdgeFromColumn(), set
						.getGraphSettings().getEdgeToColumn(), false);

		canvas.setShowLegend(set.getGraphSettings().isShowLegend());
		canvas.setCanvasSize(set.getGraphSettings().getCanvasSize());
		canvas.setEditingMode(set.getGraphSettings().getEditingMode());
		canvas.setNodeSize(set.getGraphSettings().getNodeSize());
		canvas.setFontSize(set.getGraphSettings().getFontSize());
		canvas.setFontBold(set.getGraphSettings().isFontBold());
		canvas.setJoinEdges(set.getGraphSettings().isJoinEdges());
		canvas.setArrowInMiddle(set.getGraphSettings().isArrowInMiddle());
		canvas.setNodeHighlightConditions(set.getGraphSettings()
				.getNodeHighlightConditions());
		canvas.setEdgeHighlightConditions(set.getGraphSettings()
				.getEdgeHighlightConditions());
		canvas.setSkipEdgelessNodes(set.getGraphSettings()
				.isSkipEdgelessNodes());
		canvas.setSelectedNodeIds(new LinkedHashSet<>(set.getGraphSettings()
				.getSelectedNodes()));
		canvas.setSelectedEdgeIds(new LinkedHashSet<>(set.getGraphSettings()
				.getSelectedEdges()));

		if (!Double.isNaN(set.getGraphSettings().getScaleX())
				&& !Double.isNaN(set.getGraphSettings().getScaleY())
				&& !Double.isNaN(set.getGraphSettings().getTranslationX())
				&& !Double.isNaN(set.getGraphSettings().getTranslationY())) {
			canvas.setTransform(set.getGraphSettings().getScaleX(), set
					.getGraphSettings().getScaleY(), set.getGraphSettings()
					.getTranslationX(), set.getGraphSettings()
					.getTranslationY());
		}

		canvas.setNodePositions(set.getGraphSettings().getNodePositions());

		return canvas;
	}

	public RegionCanvas createGISCanvas(GraphCanvas graphCanvas) {
		Map<String, MultiPolygon> polygonMap = ViewUtils.readPolygons(
				shapeTable, set.getGisSettings().getShapeColumn(), set
						.getGisSettings().getShapeRegionColumn());
		Map<String, Class<?>> nodeProperties = ViewUtils
				.getTableColumns(nodeTable.getSpec());
		Map<String, Class<?>> edgeProperties = ViewUtils
				.getTableColumns(edgeTable.getSpec());
		Map<String, RegionNode> nodes = ViewUtils.readRegionNodes(nodeTable,
				nodeProperties, polygonMap, idToRegionMap, set
						.getGraphSettings().getNodeIdColumn(),
				nonExistingRegions);

		if (nodes.isEmpty()) {
			return null;
		}

		List<Edge<RegionNode>> edges = ViewUtils.readEdges(edgeTable,
				edgeProperties, nodes, idToRegionMap, set.getGraphSettings()
						.getEdgeFromColumn(), set.getGraphSettings()
						.getEdgeToColumn());
		String edgeIdProperty = ViewUtils.createNewIdProperty(edges,
				edgeProperties);
		RegionCanvas canvas = new RegionCanvas(new ArrayList<>(nodes.values()),
				edges, nodeProperties, edgeProperties, set.getGraphSettings()
						.getNodeIdColumn(), edgeIdProperty, set
						.getGraphSettings().getEdgeFromColumn(), set
						.getGraphSettings().getEdgeToColumn());

		canvas.setShowLegend(set.getGisSettings().isShowLegend());
		canvas.setCanvasSize(set.getGisSettings().getCanvasSize());
		canvas.setEditingMode(set.getGisSettings().getEditingMode());
		canvas.setFontSize(set.getGisSettings().getFontSize());
		canvas.setFontBold(set.getGisSettings().isFontBold());
		canvas.setBorderAlpha(set.getGisSettings().getBorderAlpha());
		canvas.setJoinEdges(set.getGraphSettings().isJoinEdges());
		canvas.setArrowInMiddle(set.getGraphSettings().isArrowInMiddle());
		canvas.setNodeHighlightConditions(set.getGisSettings()
				.getNodeHighlightConditions());
		canvas.setEdgeHighlightConditions(set.getGraphSettings()
				.getEdgeHighlightConditions());
		canvas.setSkipEdgelessNodes(set.getGraphSettings()
				.isSkipEdgelessNodes());
		canvas.setSelectedNodeIds(getSelectedGisNodeIds(canvas.getNodes(),
				graphCanvas.getSelectedNodes()));
		canvas.setSelectedEdgeIds(getSelectedGisEdgeIds(canvas.getEdges(),
				graphCanvas.getSelectedEdges(), set.getGraphSettings()
						.isJoinEdges()));

		if (!Double.isNaN(set.getGisSettings().getScaleX())
				&& !Double.isNaN(set.getGisSettings().getScaleY())
				&& !Double.isNaN(set.getGisSettings().getTranslationX())
				&& !Double.isNaN(set.getGisSettings().getTranslationY())) {
			canvas.setTransform(set.getGisSettings().getScaleX(), set
					.getGisSettings().getScaleY(), set.getGisSettings()
					.getTranslationX(), set.getGisSettings().getTranslationY());
		}

		return canvas;
	}

	public Set<String> getNonExistingRegions() {
		return nonExistingRegions;
	}

	public static Set<String> getSelectedGisNodeIds(Set<RegionNode> gisNodes,
			Set<GraphNode> selectedGraphNodes) {
		Set<String> selectedGisNodeIds = new LinkedHashSet<>();
		Map<String, RegionNode> gisNodesByRegion = new LinkedHashMap<>();

		for (RegionNode gisNode : gisNodes) {
			gisNodesByRegion.put(gisNode.getId(), gisNode);
		}

		for (GraphNode graphNode : selectedGraphNodes) {
			RegionNode gisNode = gisNodesByRegion.get(graphNode.getRegion());

			if (gisNode != null) {
				selectedGisNodeIds.add(gisNode.getId());
			}
		}

		return selectedGisNodeIds;
	}

	public static Set<String> getSelectedGisEdgeIds(
			Set<Edge<RegionNode>> gisEdges,
			Set<Edge<GraphNode>> graphSelectedEdges, boolean joinEdges) {
		Set<String> selectedGisEdgeIds = new LinkedHashSet<>();

		if (!joinEdges) {
			selectedGisEdgeIds.addAll(CanvasUtils
					.getElementIds(graphSelectedEdges));
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
						selectedGisEdgeIds.add(gisEdge.getId());
					}
				}
			}
		}

		return selectedGisEdgeIds;
	}
}
