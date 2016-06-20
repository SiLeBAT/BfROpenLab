/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.openkrise;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NotConfigurableException;

import com.google.common.collect.ImmutableSet;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.gis.geocode.GeocodingNodeModel;
import de.bund.bfr.knime.gis.shapecell.ShapeBlobCell;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.Naming;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.PropertySchema;
import de.bund.bfr.knime.openkrise.common.Delivery;

public class TracingUtils {

	public static final Naming NAMING = new Naming("Station", "Stations", "Delivery", "Deliveries");
	public static final Naming LOT_NAMING = new Naming("Lot", "Lots", "Delivery", "Deliveries");

	public static final ImmutableSet<DataType> COMPATIBLE_COLUMNS_TYPES = ImmutableSet.of(StringCell.TYPE, IntCell.TYPE,
			DoubleCell.TYPE, BooleanCell.TYPE);

	private TracingUtils() {
	}

	public static boolean isLotBased(NodePropertySchema nodeSchema, EdgePropertySchema edgeSchema) {
		return nodeSchema.getMap().containsKey(TracingColumns.STATION_ID)
				&& nodeSchema.getMap().containsKey(TracingColumns.DELIVERY_LOTNUM)
				&& edgeSchema.getMap().containsKey(TracingColumns.DELIVERY_ID);
	}

	public static Map<String, Class<?>> getTableColumns(DataTableSpec spec) {
		Map<String, Class<?>> tableColumns = new LinkedHashMap<>();

		for (DataColumnSpec cSpec : spec) {
			if (cSpec.getType().equals(IntCell.TYPE)) {
				tableColumns.put(cSpec.getName(), Integer.class);
			} else if (cSpec.getType().equals(DoubleCell.TYPE)) {
				tableColumns.put(cSpec.getName(), Double.class);
			} else if (cSpec.getType().equals(BooleanCell.TYPE)) {
				tableColumns.put(cSpec.getName(), Boolean.class);
			} else {
				tableColumns.put(cSpec.getName(), String.class);
			}
		}

		return tableColumns;
	}

	public static DataColumnSpec toCompatibleColumn(DataColumnSpec spec) {
		if (COMPATIBLE_COLUMNS_TYPES.contains(spec.getType())) {
			return spec;
		}

		return new DataColumnSpecCreator(spec.getName(), StringCell.TYPE).createSpec();
	}

	public static Map<String, GraphNode> readGraphNodes(BufferedDataTable nodeTable, NodePropertySchema nodeSchema)
			throws NotConfigurableException {
		KnimeUtils.assertColumnNotMissing(nodeTable.getSpec(), TracingColumns.ID, "Station Table");

		Map<String, GraphNode> nodes = new LinkedHashMap<>();
		Set<String> ids = new LinkedHashSet<>();

		nodeSchema.getMap().put(TracingColumns.ID, String.class);

		for (DataRow row : nodeTable) {
			String id = IO.getToCleanString(row.getCell(nodeTable.getSpec().findColumnIndex(TracingColumns.ID)));

			if (id == null) {
				throw new NotConfigurableException("Station Table: Missing value in " + TracingColumns.ID + " column");
			} else if (!ids.add(id)) {
				throw new NotConfigurableException(
						"Station Table: Duplicate value in " + TracingColumns.ID + " column: " + id);
			}

			Map<String, Object> properties = new LinkedHashMap<>();

			TracingUtils.addToProperties(properties, nodeSchema, nodeTable.getSpec(), row);
			properties.put(TracingColumns.ID, id);
			replaceNullsInInputProperties(properties, nodeSchema);
			nodes.put(id, new GraphNode(id, properties));
		}

		if (nodes.isEmpty()) {
			throw new NotConfigurableException("Station Table: No valid nodes contained in table");
		}

		return nodes;
	}

	public static Map<String, LocationNode> readLocationNodes(BufferedDataTable nodeTable,
			NodePropertySchema nodeSchema, Set<RowKey> invalidRows, boolean skipInvalid)
			throws NotConfigurableException {
		DataTableSpec spec = nodeTable.getSpec();

		KnimeUtils.assertColumnNotMissing(spec, TracingColumns.ID, "Station Table");
		KnimeUtils.assertColumnNotMissing(spec, GeocodingNodeModel.LATITUDE_COLUMN, "Station Table");
		KnimeUtils.assertColumnNotMissing(spec, GeocodingNodeModel.LONGITUDE_COLUMN, "Station Table");

		Map<String, LocationNode> nodes = new LinkedHashMap<>();
		Set<String> ids = new LinkedHashSet<>();
		boolean hasCoordinates = false;

		nodeSchema.getMap().put(TracingColumns.ID, String.class);

		for (DataRow row : nodeTable) {
			String id = IO.getToCleanString(row.getCell(spec.findColumnIndex(TracingColumns.ID)));
			Double lat = IO.getDouble(row.getCell(spec.findColumnIndex(GeocodingNodeModel.LATITUDE_COLUMN)));
			Double lon = IO.getDouble(row.getCell(spec.findColumnIndex(GeocodingNodeModel.LONGITUDE_COLUMN)));

			if (id == null) {
				throw new NotConfigurableException("Station Table: Missing value in " + TracingColumns.ID + " column");
			} else if (!ids.add(id)) {
				throw new NotConfigurableException(
						"Station Table: Duplicate value in " + TracingColumns.ID + " column: " + id);
			}

			Point2D center = null;

			if (lat != null && lon != null) {
				center = new Point2D.Double(lat, lon);
				hasCoordinates = true;
			} else {
				invalidRows.add(row.getKey());

				if (skipInvalid) {
					continue;
				}
			}

			Map<String, Object> properties = new LinkedHashMap<>();

			TracingUtils.addToProperties(properties, nodeSchema, nodeTable.getSpec(), row);
			properties.put(TracingColumns.ID, id);
			replaceNullsInInputProperties(properties, nodeSchema);
			nodes.put(id, new LocationNode(id, properties, center));
		}

		if (nodes.isEmpty()) {
			throw new NotConfigurableException("Station Table: No valid nodes contained in table");
		}

		if (!hasCoordinates) {
			throw new NotConfigurableException("Station Table: No geographic coordinates contained in table");
		}

		return nodes;
	}

	public static <V extends Node> List<Edge<V>> readEdges(BufferedDataTable edgeTable, EdgePropertySchema edgeSchema,
			Map<String, V> nodes, Set<RowKey> skippedRows) throws NotConfigurableException {
		DataTableSpec spec = edgeTable.getSpec();

		KnimeUtils.assertColumnNotMissing(spec, TracingColumns.ID, "Delivery Table");
		KnimeUtils.assertColumnNotMissing(spec, TracingColumns.FROM, "Delivery Table");
		KnimeUtils.assertColumnNotMissing(spec, TracingColumns.TO, "Delivery Table");
		KnimeUtils.assertColumnNotMissing(spec, TracingColumns.DELIVERY_DEPARTURE, "Delivery Table");

		if (spec.getColumnSpec(TracingColumns.DELIVERY_DEPARTURE).getType() != StringCell.TYPE) {
			throw new NotConfigurableException(
					"Delivery Table: Column \"" + TracingColumns.DELIVERY_DEPARTURE + "\" must be of type String");
		}

		List<Edge<V>> edges = new ArrayList<>();
		Set<String> ids = new LinkedHashSet<>();

		edgeSchema.getMap().put(TracingColumns.ID, String.class);
		edgeSchema.getMap().put(TracingColumns.FROM, String.class);
		edgeSchema.getMap().put(TracingColumns.TO, String.class);

		for (DataRow row : edgeTable) {
			String id = IO.getToCleanString(row.getCell(spec.findColumnIndex(TracingColumns.ID)));

			if (id == null) {
				throw new NotConfigurableException("Delivery Table: Missing value in " + TracingColumns.ID + " column");
			} else if (!ids.add(id)) {
				throw new NotConfigurableException(
						"Delivery Table: Duplicate value in " + TracingColumns.ID + " column: " + id);
			}

			String from = IO.getToCleanString(row.getCell(spec.findColumnIndex(TracingColumns.FROM)));
			String to = IO.getToCleanString(row.getCell(spec.findColumnIndex(TracingColumns.TO)));
			V node1 = nodes.get(from);
			V node2 = nodes.get(to);

			if (node1 == null || node2 == null) {
				skippedRows.add(row.getKey());
				continue;
			}

			Map<String, Object> properties = new LinkedHashMap<>();

			TracingUtils.addToProperties(properties, edgeSchema, edgeTable.getSpec(), row);
			properties.put(TracingColumns.ID, id);
			properties.put(TracingColumns.FROM, from);
			properties.put(TracingColumns.TO, to);
			replaceNullsInInputProperties(properties, edgeSchema);
			edges.add(new Edge<>(id, properties, node1, node2));
		}

		return edges;
	}

	public static <V extends Node> Map<String, Delivery> readDeliveries(BufferedDataTable tracingTable,
			Collection<Edge<V>> edges, Set<RowKey> skippedRows) throws NotConfigurableException {
		DataTableSpec spec = tracingTable.getSpec();

		KnimeUtils.assertColumnNotMissing(spec, TracingColumns.ID, "Delivery Relations Table");
		KnimeUtils.assertColumnNotMissing(spec, TracingColumns.NEXT, "Delivery Relations Table");

		Map<String, Delivery> deliveries = new LinkedHashMap<>();

		for (Edge<V> edge : edges) {
			String departureDate = (String) edge.getProperties().get(TracingColumns.DELIVERY_DEPARTURE);
			String arrivalDate = (String) edge.getProperties().get(TracingColumns.DELIVERY_ARRIVAL);
			Delivery delivery = new Delivery(edge.getId(), edge.getFrom().getId(), edge.getTo().getId(),
					getDay(departureDate), getMonth(departureDate), getYear(departureDate), getDay(arrivalDate),
					getMonth(arrivalDate), getYear(arrivalDate));

			deliveries.put(edge.getId(), delivery);
		}

		for (DataRow row : tracingTable) {
			String id = IO.getToCleanString(row.getCell(spec.findColumnIndex(TracingColumns.ID)));
			String next = IO.getToCleanString(row.getCell(spec.findColumnIndex(TracingColumns.NEXT)));

			if (!deliveries.containsKey(id) || !deliveries.containsKey(next)) {
				skippedRows.add(row.getKey());
				continue;
			}

			deliveries.get(id).getAllNextIds().add(next);
			deliveries.get(next).getAllPreviousIds().add(id);
		}

		return deliveries;
	}

	public static List<RegionNode> readRegions(BufferedDataTable shapeTable, Set<RowKey> skippedRows)
			throws NotConfigurableException {
		List<RegionNode> nodes = new ArrayList<>();
		List<String> shapeColumns = KnimeUtils
				.getColumnNames(KnimeUtils.getColumns(shapeTable.getSpec(), ShapeBlobCell.TYPE));

		if (shapeColumns.isEmpty()) {
			throw new NotConfigurableException("Shape Table: Shape Column missing");
		}

		int shapeIndex = shapeTable.getSpec().findColumnIndex(shapeColumns.get(0));
		int index = 0;

		for (DataRow row : shapeTable) {
			Geometry shape = GisUtils.getShape(row.getCell(shapeIndex));

			if (!(shape instanceof MultiPolygon)) {
				skippedRows.add(row.getKey());
				continue;
			}

			nodes.add(new RegionNode(index + "", new LinkedHashMap<>(0), (MultiPolygon) shape));
			index++;
		}

		if (nodes.isEmpty()) {
			throw new NotConfigurableException("No valid shapes contained in table");
		}

		return nodes;
	}

	private static void addToProperties(Map<String, Object> properties, PropertySchema schema, DataTableSpec spec,
			DataRow row) {
		schema.getMap().forEach((property, type) -> {
			if (spec.containsName(property)) {
				TracingUtils.addCellContentToMap(properties, property, type,
						row.getCell(spec.findColumnIndex(property)));
			}
		});
	}

	private static void addCellContentToMap(Map<String, Object> map, String property, Class<?> type, DataCell cell) {
		Object obj = null;

		if (type == String.class) {
			obj = IO.getToCleanString(cell);
		} else if (type == Integer.class) {
			obj = IO.getInt(cell);
		} else if (type == Double.class) {
			obj = IO.getDouble(cell);
		} else if (type == Boolean.class) {
			obj = IO.getBoolean(cell);
		}

		CanvasUtils.addObjectToMap(map, property, type, obj);
	}

	private static void replaceNullsInInputProperties(Map<String, Object> properties, PropertySchema schema) {
		if (schema.getMap().containsKey(TracingColumns.WEIGHT) && properties.get(TracingColumns.WEIGHT) == null) {
			properties.put(TracingColumns.WEIGHT, 0.0);
		}

		if (schema.getMap().containsKey(TracingColumns.CROSS_CONTAMINATION)
				&& properties.get(TracingColumns.CROSS_CONTAMINATION) == null) {
			properties.put(TracingColumns.CROSS_CONTAMINATION, false);
		}

		if (schema.getMap().containsKey(TracingColumns.KILL_CONTAMINATION)
				&& properties.get(TracingColumns.KILL_CONTAMINATION) == null) {
			properties.put(TracingColumns.KILL_CONTAMINATION, false);
		}

		if (schema.getMap().containsKey(TracingColumns.OBSERVED) && properties.get(TracingColumns.OBSERVED) == null) {
			properties.put(TracingColumns.OBSERVED, false);
		}
	}

	private static Integer getYear(String date) {
		if (date == null) {
			return null;
		}

		String year = null;

		if (date.contains(".")) {
			year = date.substring(date.lastIndexOf('.') + 1);
		} else if (date.contains("-")) {
			year = date.substring(0, date.indexOf('-'));
		} else {
			year = date;
		}

		try {
			return Integer.parseInt(year);
		} catch (NumberFormatException | NullPointerException e) {
			return null;
		}
	}

	private static Integer getMonth(String date) {
		if (date == null) {
			return null;
		}

		String month = null;

		if (date.contains(".")) {
			int i1 = date.indexOf('.');
			int i2 = date.lastIndexOf('.');

			if (i1 == i2) {
				month = date.substring(0, i1);
			} else {
				month = date.substring(i1 + 1, i2);
			}
		} else if (date.contains("-")) {
			int i1 = date.indexOf('-');
			int i2 = date.lastIndexOf('-');

			if (i1 == i2) {
				month = date.substring(i1 + 1);
			} else {
				month = date.substring(i1 + 1, i2);
			}
		}

		try {
			return Integer.parseInt(month);
		} catch (NumberFormatException | NullPointerException e) {
			return null;
		}
	}

	private static Integer getDay(String date) {
		if (date == null) {
			return null;
		}

		String day = null;

		if (date.contains(".")) {
			day = date.substring(0, date.indexOf('.'));
		} else if (date.contains("-")) {
			day = date.substring(date.lastIndexOf('-') + 1);
		}

		try {
			return Integer.parseInt(day);
		} catch (NumberFormatException | NullPointerException e) {
			return null;
		}
	}
}
