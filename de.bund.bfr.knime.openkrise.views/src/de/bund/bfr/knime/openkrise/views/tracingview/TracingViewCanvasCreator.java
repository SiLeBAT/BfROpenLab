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

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.RowKey;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NotConfigurableException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import de.bund.bfr.knime.gis.geocode.GeocodingNodeModel;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.openkrise.MyDelivery;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.TracingUtils;
import de.bund.bfr.knime.openkrise.views.canvas.TracingGisCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.TracingGraphCanvas;

public class TracingViewCanvasCreator {

	private static final String INVALID = "invalid";
	private static final double BORDER = 0.1;

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

	public TracingGraphCanvas createGraphCanvas()
			throws NotConfigurableException {
		Map<String, GraphNode> nodes = TracingUtils.readGraphNodes(nodeTable,
				nodeSchema, shapeTable != null, new LinkedHashSet<RowKey>());
		List<Edge<GraphNode>> edges = TracingUtils.readEdges(edgeTable,
				edgeSchema, nodes, skippedEdgeRows);
		HashMap<Integer, MyDelivery> deliveries = TracingUtils.getDeliveries(
				tracingTable, edges, skippedTracingRows);
		TracingGraphCanvas canvas = new TracingGraphCanvas(new ArrayList<>(
				nodes.values()), edges, nodeSchema, edgeSchema, deliveries);

		canvas.setPerformTracing(false);
		set.setToCanvas(canvas);
		set.getGraphSettings().setToCanvas(canvas);
		canvas.setPerformTracing(true);

		return canvas;
	}

	public TracingGisCanvas createGisCanvas() throws NotConfigurableException {
		Set<RowKey> invalidRows = new LinkedHashSet<>();
		Map<String, LocationNode> nodes = TracingUtils.readLocationNodes(
				nodeTable, nodeSchema, invalidRows, false);
		List<RegionNode> regions = TracingUtils.readRegions(shapeTable,
				skippedShapeRows);

		if (!invalidRows.isEmpty()) {
			Rectangle2D bounds = getBounds(nodes.values());
			double d = Math.max(bounds.getWidth(), bounds.getHeight()) * BORDER;

			if (d == 0.0) {
				d = 1.0;
			}

			Point2D p = new Point2D.Double(bounds.getMinX() - 2 * d,
					bounds.getMaxY() + 2 * d);
			Map<String, LocationNode> invalidNodes = new LinkedHashMap<>();

			for (LocationNode node : nodes.values()) {
				if (node.getCenter() == null) {
					invalidNodes.put(node.getId(),
							new LocationNode(node.getId(),
									node.getProperties(), p));
				}
			}

			nodes.putAll(invalidNodes);
			regions.add(new RegionNode(INVALID,
					new LinkedHashMap<String, Object>(), createSquare(
							bounds.getMinX() - 3 * d, bounds.getMaxY() + d,
							2 * d, 2 * d)));
		}

		List<Edge<LocationNode>> edges = TracingUtils.readEdges(edgeTable,
				edgeSchema, nodes, skippedEdgeRows);
		HashMap<Integer, MyDelivery> deliveries = TracingUtils.getDeliveries(
				tracingTable, edges, skippedTracingRows);
		TracingGisCanvas canvas = new TracingGisCanvas(new ArrayList<>(
				nodes.values()), edges, nodeSchema, edgeSchema, regions,
				deliveries);

		canvas.getRegionFillPaints().put(
				INVALID,
				CanvasUtils.mixColors(Color.WHITE,
						Arrays.asList(Color.RED, Color.WHITE),
						Arrays.asList(1.0, 1.0)));
		canvas.setPerformTracing(false);
		set.setToCanvas(canvas);
		set.getGisSettings().setToCanvas(canvas);
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

	private static Rectangle2D getBounds(Collection<LocationNode> nodes) {
		Rectangle2D bounds = null;

		for (LocationNode node : nodes) {
			if (node.getCenter() == null) {
				continue;
			}

			Rectangle2D r = new Rectangle2D.Double(node.getCenter().getX(),
					node.getCenter().getY(), 0, 0);

			if (bounds == null) {
				bounds = r;
			} else {
				bounds = bounds.createUnion(r);
			}
		}

		return bounds;
	}

	private static MultiPolygon createSquare(double x, double y, double w,
			double h) {
		GeometryFactory factory = new GeometryFactory();
		List<Coordinate> coordinates = new ArrayList<>();

		coordinates.add(new Coordinate(x, y));
		coordinates.add(new Coordinate(x + w, y));
		coordinates.add(new Coordinate(x + w, y + h));
		coordinates.add(new Coordinate(x, y + h));
		coordinates.add(new Coordinate(x, y));

		Polygon p = new Polygon(new LinearRing(new CoordinateArraySequence(
				coordinates.toArray(new Coordinate[0])), factory), null,
				factory);

		return new MultiPolygon(new Polygon[] { p }, factory);
	}
}
