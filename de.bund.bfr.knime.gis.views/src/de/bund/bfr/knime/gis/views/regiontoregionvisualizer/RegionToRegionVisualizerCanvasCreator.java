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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;

import com.vividsolutions.jts.geom.MultiPolygon;

import de.bund.bfr.knime.gis.GisType;
import de.bund.bfr.knime.gis.views.ViewUtils;
import de.bund.bfr.knime.gis.views.canvas.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.GisCanvas;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.Naming;
import de.bund.bfr.knime.gis.views.canvas.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.RegionCanvas;
import de.bund.bfr.knime.gis.views.canvas.RegionOsmCanvas;
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

	public GisCanvas<RegionNode> createGisCanvas(GraphCanvas graphCanvas) throws InvalidSettingsException {
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
		GisCanvas<RegionNode> canvas;

		if (set.getGisSettings().getGisType() == GisType.SHAPEFILE) {
			canvas = new RegionCanvas(new ArrayList<>(nodes.values()), edges, nodeSchema, edgeSchema,
					Naming.DEFAULT_NAMING);
		} else {
			canvas = new RegionOsmCanvas(new ArrayList<>(nodes.values()), edges, nodeSchema, edgeSchema,
					Naming.DEFAULT_NAMING);
			((RegionOsmCanvas) canvas).setTileSource(set.getGisSettings().getGisType().getTileSource());
		}

		set.getGraphSettings().setToCanvas(canvas);
		set.getGisSettings().setToCanvas(canvas);
		canvas.setSelectedNodeIds(
				RegionToRegionUtils.getSelectedGisNodeIds(canvas.getNodes(), graphCanvas.getSelectedNodes()));
		canvas.setSelectedEdgeIds(RegionToRegionUtils.getSelectedGisEdgeIds(canvas.getEdges(),
				graphCanvas.getSelectedEdges(), graphCanvas.isJoinEdges()));

		return canvas;
	}

	public Set<String> getNonExistingRegions() {
		return nonExistingRegions;
	}
}
