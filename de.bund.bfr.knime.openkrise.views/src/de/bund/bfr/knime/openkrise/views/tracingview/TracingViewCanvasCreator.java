/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NotConfigurableException;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.gis.BackwardUtils;
import de.bund.bfr.knime.gis.GisType;
import de.bund.bfr.knime.gis.geocode.GeocodingNodeModel;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.TracingUtils;
import de.bund.bfr.knime.openkrise.common.Delivery;
import de.bund.bfr.knime.openkrise.views.canvas.ExplosionTracingGraphCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.ExplosionTracingOsmCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.ExplosionTracingShapefileCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.ITracingGisCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.TracingGraphCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.TracingOsmCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.TracingShapefileCanvas;

public class TracingViewCanvasCreator {
	
	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;
	private BufferedDataTable tracingTable;
	private BufferedDataTable shapeTable;
	private TracingViewSettings set;

	private NodePropertySchema nodeSchema;
	private EdgePropertySchema edgeSchema;

	private Map<RowKey, String> skippedDeliveryRows;
	private Map<RowKey, String> skippedDeliveryRelationRows;
	private Map<RowKey, String> skippedShapeRows;

	private boolean lotBased;

	public TracingViewCanvasCreator(BufferedDataTable nodeTable, BufferedDataTable edgeTable,
			BufferedDataTable tracingTable, BufferedDataTable shapeTable, TracingViewSettings set) {
		
		this.nodeTable = nodeTable;
		this.edgeTable = edgeTable;
		this.tracingTable = tracingTable;
		this.shapeTable = shapeTable;
		this.set = set;

		Map<String, Class<?>> nodeProperties = TracingUtils.getTableColumns(nodeTable.getSpec());
		Map<String, Class<?>> edgeProperties = TracingUtils.getTableColumns(edgeTable.getSpec());

		for (String c : TracingColumns.STATION_IN_OUT_COLUMNS) {
			nodeProperties.put(c, TracingColumns.IN_OUT_COLUMN_CLASSES.get(c));
		}

		for (String c : TracingColumns.DELIVERY_IN_OUT_COLUMNS) {
			edgeProperties.put(c, TracingColumns.IN_OUT_COLUMN_CLASSES.get(c));
		}

		nodeSchema = new NodePropertySchema(nodeProperties, TracingColumns.ID);
		edgeSchema = new EdgePropertySchema(edgeProperties, TracingColumns.ID, TracingColumns.FROM, TracingColumns.TO);
		nodeSchema.setLatitude(nodeProperties.containsKey(BackwardUtils.OLD_LATITUDE_COLUMN)
				? BackwardUtils.OLD_LATITUDE_COLUMN : GeocodingNodeModel.LATITUDE_COLUMN);
		nodeSchema.setLongitude(nodeProperties.containsKey(BackwardUtils.OLD_LONGITUDE_COLUMN)
				? BackwardUtils.OLD_LONGITUDE_COLUMN : GeocodingNodeModel.LONGITUDE_COLUMN);

		skippedDeliveryRows = new LinkedHashMap<>();
		skippedDeliveryRelationRows = new LinkedHashMap<>();
		skippedShapeRows = new LinkedHashMap<>();

		lotBased = TracingUtils.isLotBased(nodeSchema, edgeSchema);
		
	}

	public boolean hasGisCoordinates() {
		
		int latIndex = nodeTable.getSpec().findColumnIndex(GeocodingNodeModel.LATITUDE_COLUMN);
		int lonIndex = nodeTable.getSpec().findColumnIndex(GeocodingNodeModel.LONGITUDE_COLUMN);

		if (nodeTable.getSpec().containsName(BackwardUtils.OLD_LATITUDE_COLUMN)
				&& nodeTable.getSpec().containsName(BackwardUtils.OLD_LONGITUDE_COLUMN)) {
			latIndex = nodeTable.getSpec().findColumnIndex(BackwardUtils.OLD_LATITUDE_COLUMN);
			lonIndex = nodeTable.getSpec().findColumnIndex(BackwardUtils.OLD_LONGITUDE_COLUMN);
		}

		if (latIndex == -1 || lonIndex == -1) {
			return false;
		}

		int intIDIndex = nodeTable.getSpec().findColumnIndex(TracingColumns.ID);
		
		// if an explosion view is opened consider only the available GIS information for inner nodes
		Set<String> filterNodes = (this.set.getExplosionSettingsList().getActiveExplosionSettings()==null?null:this.set.getExplosionSettingsList().getActiveExplosionSettings().getContainedNodesIds());
	
		
		for (DataRow row : nodeTable) {
			if(filterNodes==null || filterNodes.contains(IO.getToCleanString(row.getCell(intIDIndex))))
			{
				Double lat = IO.getDouble(row.getCell(latIndex));
				Double lon = IO.getDouble(row.getCell(lonIndex));

				if (lat != null && lon != null) {
					return true;
				}
			}
		}

		return false;
	}

	public TracingGraphCanvas createGraphCanvas() throws NotConfigurableException {
        
		Map<String, GraphNode> nodes = TracingUtils.readGraphNodes(nodeTable, nodeSchema);
		List<Edge<GraphNode>> edges = TracingUtils.readEdges(edgeTable, edgeSchema, nodes, skippedDeliveryRows);
		Map<String, Delivery> deliveries = TracingUtils.readDeliveries(tracingTable, edges,
				skippedDeliveryRelationRows);
		TracingGraphCanvas canvas = new TracingGraphCanvas(new ArrayList<>(nodes.values()), edges, nodeSchema,
				edgeSchema, deliveries, lotBased);

		canvas.setPerformTracing(false);
		set.setToCanvas(canvas);
		set.getGraphSettings().setToCanvas(canvas);
		canvas.setPerformTracing(true);
		
		return canvas;
	}
	
	/*
	 * returns an explosion canvas for graph views
	 */
	public TracingGraphCanvas createExplosionGraphCanvas() throws NotConfigurableException {
		
		Map<String, GraphNode> nodes = TracingUtils.readGraphNodes(nodeTable, nodeSchema);
		List<Edge<GraphNode>> edges = TracingUtils.readEdges(edgeTable, edgeSchema, nodes, skippedDeliveryRows);
		Map<String, Delivery> deliveries = TracingUtils.readDeliveries(tracingTable, edges,
				skippedDeliveryRelationRows);
		
		TracingGraphCanvas canvas = new ExplosionTracingGraphCanvas(new ArrayList<>(nodes.values()), edges, nodeSchema,
				edgeSchema, deliveries, lotBased, 
				this.set.getExplosionSettingsList().getActiveExplosionSettings().getKey(),
				this.set.getExplosionSettingsList().getActiveExplosionSettings().getContainedNodesIds());

		canvas.setPerformTracing(false);
		set.setToCanvas(canvas);
		set.getGraphSettings().setToCanvas(canvas);
		canvas.setPerformTracing(true);

		return canvas;
	}

	public ITracingGisCanvas<?> createGisCanvas() throws NotConfigurableException {
		
		Map<String, LocationNode> nodes = TracingUtils.readLocationNodes(nodeTable, nodeSchema, new LinkedHashMap<>(),
				false);
		
		List<Edge<LocationNode>> edges = TracingUtils.readEdges(edgeTable, edgeSchema, nodes, skippedDeliveryRows);
		Map<String, Delivery> deliveries = TracingUtils.readDeliveries(tracingTable, edges,
				skippedDeliveryRelationRows);
		ITracingGisCanvas<?> canvas;

		if (set.getGisType() == GisType.SHAPEFILE) {
			canvas = new TracingShapefileCanvas(new ArrayList<>(nodes.values()), edges, nodeSchema, edgeSchema,
					TracingUtils.readRegions(shapeTable, skippedShapeRows), deliveries, lotBased);
		} else {
			canvas = new TracingOsmCanvas(new ArrayList<>(nodes.values()), edges, nodeSchema, edgeSchema, deliveries,
					lotBased);
			((TracingOsmCanvas) canvas).setTileSource(set.getGisType().getTileSource());
		}

		canvas.setPerformTracing(false);
		set.setToCanvas(canvas);
		set.getGisSettings().setToCanvas(canvas);
		canvas.setPerformTracing(true);

		return canvas;
	}
	
	/*
	 * returns an explosion canvas for GIS views
	 */
	public ITracingGisCanvas<?> createExplosionGisCanvas() throws NotConfigurableException {
		Map<String, LocationNode> nodes = TracingUtils.readLocationNodes(nodeTable, nodeSchema, new LinkedHashMap<>(),
				false);
		
		List<Edge<LocationNode>> edges = TracingUtils.readEdges(edgeTable, edgeSchema, nodes, skippedDeliveryRows);
		Map<String, Delivery> deliveries = TracingUtils.readDeliveries(tracingTable, edges,
				skippedDeliveryRelationRows);
		ITracingGisCanvas<?> canvas;

		if (set.getGisType() == GisType.SHAPEFILE) {
		
			canvas = new ExplosionTracingShapefileCanvas(new ArrayList<>(nodes.values()), edges, nodeSchema, edgeSchema,
					TracingUtils.readRegions(shapeTable, skippedShapeRows), deliveries, lotBased, 
					this.set.getExplosionSettingsList().getActiveExplosionSettings().getKey(),
					this.set.getExplosionSettingsList().getActiveExplosionSettings().getContainedNodesIds());
		} else {
			canvas = new ExplosionTracingOsmCanvas(new ArrayList<>(nodes.values()), edges, nodeSchema, edgeSchema, deliveries,
					lotBased,
					this.set.getExplosionSettingsList().getActiveExplosionSettings().getKey(),
					this.set.getExplosionSettingsList().getActiveExplosionSettings().getContainedNodesIds());
			((ExplosionTracingOsmCanvas) canvas).setTileSource(set.getGisType().getTileSource());
		}

		canvas.setPerformTracing(false);
		set.setToCanvas(canvas);
		set.getGisSettings().setToCanvas(canvas);
		canvas.setPerformTracing(true);

		return canvas;
	}

	
	public Map<RowKey, String> getSkippedDeliveryRows() {
		return skippedDeliveryRows;
	}

	public Map<RowKey, String> getSkippedDeliveryRelationRows() {
		return skippedDeliveryRelationRows;
	}

	public Map<RowKey, String> getSkippedShapeRows() {
		return skippedShapeRows;
	}

	public boolean isLotBased() {
		return lotBased;
	}
}
