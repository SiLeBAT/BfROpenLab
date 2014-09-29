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
package de.bund.bfr.knime.gis.views.locationtolocationvisualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;

import de.bund.bfr.knime.gis.views.ViewUtils;
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

	public LocationToLocationVisualizerCanvasCreator(
			BufferedDataTable shapeTable, BufferedDataTable nodeTable,
			BufferedDataTable edgeTable,
			LocationToLocationVisualizerSettings set) {
		this.shapeTable = shapeTable;
		this.nodeTable = nodeTable;
		this.edgeTable = edgeTable;
		this.set = set;
	}

	public GraphCanvas createGraphCanvas() throws InvalidSettingsException {
		Map<String, Class<?>> nodeProperties = ViewUtils
				.getTableColumns(nodeTable.getSpec());
		Map<String, Class<?>> edgeProperties = ViewUtils
				.getTableColumns(edgeTable.getSpec());
		Map<String, GraphNode> nodes = ViewUtils.readGraphNodes(nodeTable,
				nodeProperties, set.getGraphSettings().getNodeIdColumn(), null);
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

		set.getGraphSettings().setToCanvas(canvas);

		return canvas;
	}

	public LocationCanvas createLocationCanvas()
			throws InvalidSettingsException {
		List<RegionNode> regionNodes = ViewUtils.readRegionNodes(shapeTable,
				set.getGisSettings().getShapeColumn());
		Map<String, Class<?>> nodeProperties = ViewUtils
				.getTableColumns(nodeTable.getSpec());
		Map<String, Class<?>> edgeProperties = ViewUtils
				.getTableColumns(edgeTable.getSpec());
		Map<String, LocationNode> nodes = ViewUtils.readLocationNodes(
				nodeTable, nodeProperties, set.getGraphSettings()
						.getNodeIdColumn(), set.getGisSettings()
						.getNodeLatitudeColumn(), set.getGisSettings()
						.getNodeLongitudeColumn());
		List<Edge<LocationNode>> edges = ViewUtils.readEdges(edgeTable,
				edgeProperties, nodes, null, set.getGraphSettings()
						.getEdgeFromColumn(), set.getGraphSettings()
						.getEdgeToColumn());
		String edgeIdProperty = ViewUtils.createNewIdProperty(edges,
				edgeProperties);
		LocationCanvas canvas = new LocationCanvas(new ArrayList<>(
				nodes.values()), edges, nodeProperties, edgeProperties, set
				.getGraphSettings().getNodeIdColumn(), edgeIdProperty, set
				.getGraphSettings().getEdgeFromColumn(), set.getGraphSettings()
				.getEdgeToColumn(), regionNodes);

		set.getGraphSettings().setToCanvas(canvas);
		set.getGisSettings().setToCanvas(canvas);

		return canvas;
	}
}
