/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.gis.views.regiontoregionvisualizer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;

import com.vividsolutions.jts.geom.MultiPolygon;

import de.bund.bfr.knime.gis.views.ViewUtils;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.Naming;
import de.bund.bfr.knime.gis.views.canvas.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.RegionCanvas;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;

public class RegionToRegionVisualizerCanvasCreator {

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;
	private RegionToRegionVisualizerSettings set;

	private Set<String> nonExistingRegions;

	public RegionToRegionVisualizerCanvasCreator(BufferedDataTable shapeTable, BufferedDataTable nodeTable,
			BufferedDataTable edgeTable, RegionToRegionVisualizerSettings set) {
		this.shapeTable = shapeTable;
		this.nodeTable = nodeTable;
		this.edgeTable = edgeTable;
		this.set = set;

		nonExistingRegions = new LinkedHashSet<>();
	}

	public GraphCanvas createGraphCanvas() throws InvalidSettingsException {
		Map<String, Class<?>> nodeProperties = ViewUtils.getTableColumns(nodeTable.getSpec());
		Map<String, Class<?>> edgeProperties = ViewUtils.getTableColumns(edgeTable.getSpec());
		Map<String, GraphNode> nodes = ViewUtils.readGraphNodes(nodeTable, nodeProperties,
				set.getGraphSettings().getNodeIdColumn(), set.getGisSettings().getNodeRegionColumn());
		List<Edge<GraphNode>> edges = ViewUtils.readEdges(edgeTable, edgeProperties, nodes, null,
				set.getGraphSettings().getEdgeFromColumn(), set.getGraphSettings().getEdgeToColumn());
		String edgeIdProperty = ViewUtils.createNewIdProperty(edges, edgeProperties);
		NodePropertySchema nodeSchema = new NodePropertySchema(nodeProperties,
				set.getGraphSettings().getNodeIdColumn());
		EdgePropertySchema edgeSchema = new EdgePropertySchema(edgeProperties, edgeIdProperty,
				set.getGraphSettings().getEdgeFromColumn(), set.getGraphSettings().getEdgeToColumn());
		GraphCanvas canvas = new GraphCanvas(new ArrayList<>(nodes.values()), edges, nodeSchema, edgeSchema,
				Naming.DEFAULT_NAMING, false);

		set.getGraphSettings().setToCanvas(canvas);

		return canvas;
	}

	public RegionCanvas createGisCanvas(GraphCanvas graphCanvas) throws InvalidSettingsException {
		Map<String, String> idToRegionMap = ViewUtils.getIdToRegionMap(nodeTable,
				set.getGraphSettings().getNodeIdColumn(), set.getGisSettings().getNodeRegionColumn());
		Map<String, MultiPolygon> polygonMap = ViewUtils.readPolygons(shapeTable, set.getGisSettings().getShapeColumn(),
				set.getGisSettings().getShapeRegionColumn());
		Map<String, Class<?>> nodeProperties = ViewUtils.getTableColumns(nodeTable.getSpec());
		Map<String, Class<?>> edgeProperties = ViewUtils.getTableColumns(edgeTable.getSpec());
		Map<String, RegionNode> nodes = ViewUtils.readRegionNodes(nodeTable, nodeProperties, polygonMap, idToRegionMap,
				set.getGraphSettings().getNodeIdColumn(), nonExistingRegions);
		List<Edge<RegionNode>> edges = ViewUtils.readEdges(edgeTable, edgeProperties, nodes, idToRegionMap,
				set.getGraphSettings().getEdgeFromColumn(), set.getGraphSettings().getEdgeToColumn());
		String edgeIdProperty = ViewUtils.createNewIdProperty(edges, edgeProperties);
		NodePropertySchema nodeSchema = new NodePropertySchema(nodeProperties,
				set.getGraphSettings().getNodeIdColumn());
		EdgePropertySchema edgeSchema = new EdgePropertySchema(edgeProperties, edgeIdProperty,
				set.getGraphSettings().getEdgeFromColumn(), set.getGraphSettings().getEdgeToColumn());
		RegionCanvas canvas = new RegionCanvas(new ArrayList<>(nodes.values()), edges, nodeSchema, edgeSchema,
				Naming.DEFAULT_NAMING);

		set.getGraphSettings().setToCanvas(canvas);
		set.getGisSettings().setToCanvas(canvas);
		canvas.setSelectedNodeIds(getSelectedGisNodeIds(canvas.getNodes(), graphCanvas.getSelectedNodes()));
		canvas.setSelectedEdgeIds(getSelectedGisEdgeIds(canvas.getEdges(), graphCanvas.getSelectedEdges(),
				set.getGraphSettings().isJoinEdges()));

		return canvas;
	}

	public Set<String> getNonExistingRegions() {
		return nonExistingRegions;
	}

	public static Set<String> getSelectedGisNodeIds(Set<RegionNode> gisNodes, Set<GraphNode> selectedGraphNodes) {
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

	public static Set<String> getSelectedGisEdgeIds(Set<Edge<RegionNode>> gisEdges,
			Set<Edge<GraphNode>> graphSelectedEdges, boolean joinEdges) {
		Set<String> selectedGisEdgeIds = new LinkedHashSet<>();

		if (!joinEdges) {
			selectedGisEdgeIds.addAll(CanvasUtils.getElementIds(graphSelectedEdges));
		} else {
			Map<String, Map<String, Edge<RegionNode>>> gisEdgesByRegion = new LinkedHashMap<>();

			for (Edge<RegionNode> gisEdge : gisEdges) {
				String fromRegion = gisEdge.getFrom().getId();
				String toRegion = gisEdge.getTo().getId();

				if (!gisEdgesByRegion.containsKey(fromRegion)) {
					gisEdgesByRegion.put(fromRegion, new LinkedHashMap<String, Edge<RegionNode>>());
				}

				gisEdgesByRegion.get(fromRegion).put(toRegion, gisEdge);
			}

			for (Edge<GraphNode> graphEdge : graphSelectedEdges) {
				String fromRegion = graphEdge.getFrom().getRegion();
				String toRegion = graphEdge.getTo().getRegion();

				if (gisEdgesByRegion.containsKey(fromRegion)) {
					Edge<RegionNode> gisEdge = gisEdgesByRegion.get(fromRegion).get(toRegion);

					if (gisEdge != null) {
						selectedGisEdgeIds.add(gisEdge.getId());
					}
				}
			}
		}

		return selectedGisEdgeIds;
	}
}
