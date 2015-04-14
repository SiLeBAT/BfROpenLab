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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NotConfigurableException;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOpenAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.gis.geocode.GeocodingNodeModel;
import de.bund.bfr.knime.gis.views.canvas.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.GisCanvas;
import de.bund.bfr.knime.gis.views.canvas.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.openkrise.MyDelivery;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.TracingUtils;
import de.bund.bfr.knime.openkrise.views.canvas.ITracingCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.TracingGisCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.TracingGraphCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.TracingOsmCanvas;
import de.bund.bfr.knime.openkrise.views.tracingview.TracingViewSettings.GisType;

public class TracingViewCanvasCreator {

	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;
	private BufferedDataTable tracingTable;
	private BufferedDataTable shapeTable;
	private TracingViewSettings set;

	private NodePropertySchema nodeSchema;
	private EdgePropertySchema edgeSchema;

	private Set<RowKey> skippedEdgeRows;
	private Set<RowKey> skippedTracingRows;
	private Set<RowKey> skippedShapeRows;

	public TracingViewCanvasCreator(BufferedDataTable nodeTable,
			BufferedDataTable edgeTable, BufferedDataTable tracingTable,
			BufferedDataTable shapeTable, TracingViewSettings set) {
		this.nodeTable = nodeTable;
		this.edgeTable = edgeTable;
		this.tracingTable = tracingTable;
		this.shapeTable = shapeTable;
		this.set = set;

		Map<String, Class<?>> nodeProperties = TracingUtils
				.getTableColumns(nodeTable.getSpec());
		Map<String, Class<?>> edgeProperties = TracingUtils
				.getTableColumns(edgeTable.getSpec());

		if (!nodeProperties.containsKey(TracingColumns.WEIGHT)) {
			nodeProperties.put(TracingColumns.WEIGHT, Double.class);
		}

		if (!nodeProperties.containsKey(TracingColumns.CROSS_CONTAMINATION)) {
			nodeProperties.put(TracingColumns.CROSS_CONTAMINATION,
					Boolean.class);
		}

		if (!nodeProperties.containsKey(TracingColumns.SCORE)) {
			nodeProperties.put(TracingColumns.SCORE, Double.class);
		}

		if (!nodeProperties.containsKey(TracingColumns.OBSERVED)) {
			nodeProperties.put(TracingColumns.OBSERVED, Boolean.class);
		}

		if (!nodeProperties.containsKey(TracingColumns.BACKWARD)) {
			nodeProperties.put(TracingColumns.BACKWARD, Boolean.class);
		}

		if (!nodeProperties.containsKey(TracingColumns.FORWARD)) {
			nodeProperties.put(TracingColumns.FORWARD, Boolean.class);
		}

		if (!edgeProperties.containsKey(TracingColumns.WEIGHT)) {
			edgeProperties.put(TracingColumns.WEIGHT, Double.class);
		}

		if (!edgeProperties.containsKey(TracingColumns.CROSS_CONTAMINATION)) {
			edgeProperties.put(TracingColumns.CROSS_CONTAMINATION,
					Boolean.class);
		}

		if (!edgeProperties.containsKey(TracingColumns.OBSERVED)) {
			edgeProperties.put(TracingColumns.OBSERVED, Boolean.class);
		}

		if (!edgeProperties.containsKey(TracingColumns.SCORE)) {
			edgeProperties.put(TracingColumns.SCORE, Double.class);
		}

		if (!edgeProperties.containsKey(TracingColumns.BACKWARD)) {
			edgeProperties.put(TracingColumns.BACKWARD, Boolean.class);
		}

		if (!edgeProperties.containsKey(TracingColumns.FORWARD)) {
			edgeProperties.put(TracingColumns.FORWARD, Boolean.class);
		}

		nodeSchema = new NodePropertySchema(nodeProperties, TracingColumns.ID);
		edgeSchema = new EdgePropertySchema(edgeProperties, TracingColumns.ID,
				TracingColumns.FROM, TracingColumns.TO);
		nodeSchema.setLatitude(GeocodingNodeModel.LATITUDE_COLUMN);
		nodeSchema.setLongitude(GeocodingNodeModel.LONGITUDE_COLUMN);

		skippedEdgeRows = new LinkedHashSet<>();
		skippedTracingRows = new LinkedHashSet<>();
		skippedShapeRows = new LinkedHashSet<>();
	}

	public boolean hasGisCoordinates() {
		int latIndex = nodeTable.getSpec().findColumnIndex(
				GeocodingNodeModel.LATITUDE_COLUMN);
		int lonIndex = nodeTable.getSpec().findColumnIndex(
				GeocodingNodeModel.LONGITUDE_COLUMN);

		if (latIndex == -1 || lonIndex == -1) {
			return false;
		}

		for (DataRow row : nodeTable) {
			Double lat = IO.getDouble(row.getCell(latIndex));
			Double lon = IO.getDouble(row.getCell(lonIndex));

			if (lat != null && lon != null) {
				return true;
			}
		}

		return false;
	}

	public TracingGraphCanvas createGraphCanvas()
			throws NotConfigurableException {
		Map<String, GraphNode> nodes = TracingUtils.readGraphNodes(nodeTable,
				nodeSchema);
		List<Edge<GraphNode>> edges = TracingUtils.readEdges(edgeTable,
				edgeSchema, nodes, skippedEdgeRows);
		HashMap<Integer, MyDelivery> deliveries = TracingUtils.readDeliveries(
				tracingTable, edges, skippedTracingRows);
		TracingGraphCanvas canvas = new TracingGraphCanvas(new ArrayList<>(
				nodes.values()), edges, nodeSchema, edgeSchema, deliveries);

		canvas.setPerformTracing(false);
		set.setToCanvas(canvas);
		set.getGraphSettings().setToCanvas(canvas);
		canvas.setPerformTracing(true);

		return canvas;
	}

	public ITracingCanvas<?> createGisCanvas() throws NotConfigurableException {
		Set<RowKey> invalidRows = new LinkedHashSet<>();
		Map<String, LocationNode> nodes = TracingUtils.readLocationNodes(
				nodeTable, nodeSchema, invalidRows, false);
		List<Edge<LocationNode>> edges = TracingUtils.readEdges(edgeTable,
				edgeSchema, nodes, skippedEdgeRows);
		HashMap<Integer, MyDelivery> deliveries = TracingUtils.readDeliveries(
				tracingTable, edges, skippedTracingRows);
		ITracingCanvas<?> canvas;

		if (set.getGisType() == GisType.SHAPEFILE) {
			List<RegionNode> regions = TracingUtils.readRegions(shapeTable,
					skippedShapeRows);

			canvas = new TracingGisCanvas(new ArrayList<>(nodes.values()),
					edges, nodeSchema, edgeSchema, regions, deliveries);
		} else {
			TileSource tileSource;

			switch (set.getGisType()) {
			case MAPNIK:
				tileSource = new OsmTileSource.Mapnik();
				break;
			case CYCLE_MAP:
				tileSource = new OsmTileSource.CycleMap();
				break;
			case BING_AERIAL:
				tileSource = new BingAerialTileSource();
				break;
			case MAPQUEST:
				tileSource = new MapQuestOsmTileSource();
				break;
			case MAPQUEST_AERIAL:
				tileSource = new MapQuestOpenAerialTileSource();
				break;
			default:
				throw new IllegalArgumentException();
			}

			canvas = new TracingOsmCanvas(new ArrayList<>(nodes.values()),
					edges, nodeSchema, edgeSchema, deliveries);
			((TracingOsmCanvas) canvas).setTileSource(tileSource);
		}

		canvas.setPerformTracing(false);
		set.setToCanvas(canvas);
		set.getGisSettings().setToCanvas((GisCanvas<?>) canvas);
		canvas.setPerformTracing(true);

		return canvas;
	}

	public Set<RowKey> getSkippedEdgeRows() {
		return skippedEdgeRows;
	}

	public Set<RowKey> getSkippedTracingRows() {
		return skippedTracingRows;
	}

	public Set<RowKey> getSkippedShapeRows() {
		return skippedShapeRows;
	}
}
