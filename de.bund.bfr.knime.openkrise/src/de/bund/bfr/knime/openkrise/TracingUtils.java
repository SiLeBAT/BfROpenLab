/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;

import de.bund.bfr.knime.IO;
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
import de.bund.bfr.knime.openkrise.common.Tracing;
import de.bund.bfr.knime.openkrise.common.TracingException;

public class TracingUtils {

	public static final Naming NAMING = new Naming("Station", "Stations", "Delivery", "Deliveries");
	public static final Naming LOT_NAMING = new Naming("Lot", "Lots", "Delivery", "Deliveries");

	public static final ImmutableSet<DataType> COMPATIBLE_COLUMNS_TYPES = ImmutableSet.of(StringCell.TYPE, IntCell.TYPE,
			DoubleCell.TYPE, BooleanCell.TYPE);

	public static final String LOT_BASED_INFO = "FoodChain-Lab assumes, that your network is lot-based (and not station-based),\n"
			+ "since the first input table contains the columns \"" + TracingColumns.STATION_ID + "\" and \""
			+ TracingColumns.LOT_NUMBER + "\"\nand the second input table contains the column \""
			+ TracingColumns.DELIVERY_ID + "\".";

	private TracingUtils() {
	}

	public static boolean isLotBased(NodePropertySchema nodeSchema, EdgePropertySchema edgeSchema) {
		return nodeSchema.getMap().containsKey(TracingColumns.STATION_ID)
				&& nodeSchema.getMap().containsKey(TracingColumns.LOT_NUMBER)
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

	public static Map<String, GraphNode> readGraphNodes(BufferedDataTable stationTable, NodePropertySchema nodeSchema)
			throws NotConfigurableException {
		assertColumnNotMissing(stationTable.getSpec(), TracingColumns.ID, "Station Table");

		Map<String, GraphNode> nodes = new LinkedHashMap<>();
		Set<String> ids = new LinkedHashSet<>();

		nodeSchema.getMap().put(TracingColumns.ID, String.class);

		for (DataRow row : stationTable) {
			String id = IO.getToCleanString(row.getCell(stationTable.getSpec().findColumnIndex(TracingColumns.ID)));

			if (id == null) {
				throw new NotConfigurableException("Station Table: Missing value in " + TracingColumns.ID + " column");
			} else if (!ids.add(id)) {
				throw new NotConfigurableException(
						"Station Table: Duplicate value in " + TracingColumns.ID + " column: " + id);
			}

			Map<String, Object> properties = new LinkedHashMap<>();

			TracingUtils.addToProperties(properties, nodeSchema, stationTable.getSpec(), row);
			properties.put(TracingColumns.ID, id);
			replaceNullsInInputProperties(properties, nodeSchema);
			nodes.put(id, new GraphNode(id, properties));
		}

		if (nodes.isEmpty()) {
			throw new NotConfigurableException("Station Table: No valid nodes contained in table");
		}

		return nodes;
	}

	public static Map<String, LocationNode> readLocationNodes(BufferedDataTable stationTable,
			NodePropertySchema nodeSchema, Map<RowKey, String> invalidRows, boolean skipInvalid)
			throws NotConfigurableException {
		DataTableSpec spec = stationTable.getSpec();
		String latColumn = GeocodingNodeModel.LATITUDE_COLUMN;
		String lonColumn = GeocodingNodeModel.LONGITUDE_COLUMN;

		assertColumnNotMissing(spec, TracingColumns.ID, "Station Table");

		if (spec.containsName(de.bund.bfr.knime.gis.BackwardUtils.OLD_LATITUDE_COLUMN)
				&& spec.containsName(de.bund.bfr.knime.gis.BackwardUtils.OLD_LONGITUDE_COLUMN)) {
			latColumn = de.bund.bfr.knime.gis.BackwardUtils.OLD_LATITUDE_COLUMN;
			lonColumn = de.bund.bfr.knime.gis.BackwardUtils.OLD_LONGITUDE_COLUMN;
		} else {
			assertColumnNotMissing(spec, latColumn, "Station Table");
			assertColumnNotMissing(spec, lonColumn, "Station Table");
		}

		Map<String, LocationNode> nodes = new LinkedHashMap<>();
		Set<String> ids = new LinkedHashSet<>();
		boolean hasCoordinates = false;

		nodeSchema.getMap().put(TracingColumns.ID, String.class);

		for (DataRow row : stationTable) {
			String id = IO.getToCleanString(row.getCell(spec.findColumnIndex(TracingColumns.ID)));
			Double lat = IO.getDouble(row.getCell(spec.findColumnIndex(latColumn)));
			Double lon = IO.getDouble(row.getCell(spec.findColumnIndex(lonColumn)));

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
				invalidRows.put(row.getKey(), "Invalid " + GeocodingNodeModel.LATITUDE_COLUMN + " and "
						+ GeocodingNodeModel.LONGITUDE_COLUMN);

				if (skipInvalid) {
					continue;
				}
			}

			Map<String, Object> properties = new LinkedHashMap<>();

			TracingUtils.addToProperties(properties, nodeSchema, stationTable.getSpec(), row);
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

	public static <V extends Node> List<Edge<V>> readEdges(BufferedDataTable deliveryTable,
			EdgePropertySchema edgeSchema, Map<String, V> nodes, Map<RowKey, String> skippedRows)
			throws NotConfigurableException {
		DataTableSpec spec = deliveryTable.getSpec();

		assertColumnNotMissing(spec, TracingColumns.ID, "Delivery Table");
		assertColumnNotMissing(spec, TracingColumns.FROM, "Delivery Table");
		assertColumnNotMissing(spec, TracingColumns.TO, "Delivery Table");

		if (spec.containsName(TracingColumns.DELIVERY_DEPARTURE)
				&& spec.getColumnSpec(TracingColumns.DELIVERY_DEPARTURE).getType() != StringCell.TYPE) {
			throw new NotConfigurableException(
					"Delivery Table: Column \"" + TracingColumns.DELIVERY_DEPARTURE + "\" must be of type String");
		}

		if (spec.containsName(TracingColumns.DELIVERY_ARRIVAL)
				&& spec.getColumnSpec(TracingColumns.DELIVERY_ARRIVAL).getType() != StringCell.TYPE) {
			throw new NotConfigurableException(
					"Delivery Table: Column \"" + TracingColumns.DELIVERY_ARRIVAL + "\" must be of type String");
		}

		List<Edge<V>> edges = new ArrayList<>();
		Set<String> ids = new LinkedHashSet<>();

		edgeSchema.getMap().put(TracingColumns.ID, String.class);
		edgeSchema.getMap().put(TracingColumns.FROM, String.class);
		edgeSchema.getMap().put(TracingColumns.TO, String.class);

		for (DataRow row : deliveryTable) {
			String id = IO.getToCleanString(row.getCell(spec.findColumnIndex(TracingColumns.ID)));

			if (id == null) {
				throw new NotConfigurableException("Delivery Table: Missing value in " + TracingColumns.ID + " column");
			} else if (!ids.add(id)) {
				throw new NotConfigurableException(
						"Delivery Table: Duplicate value in " + TracingColumns.ID + " column: " + id);
			}

			String from = IO.getToCleanString(row.getCell(spec.findColumnIndex(TracingColumns.FROM)));
			String to = IO.getToCleanString(row.getCell(spec.findColumnIndex(TracingColumns.TO)));
			V fromNode = nodes.get(from);
			V toNode = nodes.get(to);

			if (fromNode == null) {
				skippedRows.put(row.getKey(), "Station with " + TracingColumns.ID + " \"" + from + "\" does not exist");
				continue;
			} else if (toNode == null) {
				skippedRows.put(row.getKey(), "Station with " + TracingColumns.ID + " \"" + to + "\" does not exist");
				continue;
			}

			Map<String, Object> properties = new LinkedHashMap<>();

			TracingUtils.addToProperties(properties, edgeSchema, deliveryTable.getSpec(), row);
			properties.put(TracingColumns.ID, id);
			properties.put(TracingColumns.FROM, from);
			properties.put(TracingColumns.TO, to);
			replaceNullsInInputProperties(properties, edgeSchema);
			edges.add(new Edge<>(id, properties, fromNode, toNode));
		}

		return edges;
	}

	public static <V extends Node> Map<String, Delivery> readDeliveries(BufferedDataTable deliveryRelationsTable,
			Collection<Edge<V>> edges, Map<RowKey, String> skippedRows) throws NotConfigurableException {
		DataTableSpec spec = deliveryRelationsTable.getSpec();
		String fromColumn = TracingColumns.FROM;
		String toColumn = TracingColumns.TO;

		if (spec.containsName(BackwardUtils.OLD_FROM) && spec.containsName(BackwardUtils.OLD_TO)) {
			fromColumn = BackwardUtils.OLD_FROM;
			toColumn = BackwardUtils.OLD_TO;
		} else {
			assertColumnNotMissing(spec, TracingColumns.FROM, "Delivery Relations Table");
			assertColumnNotMissing(spec, TracingColumns.TO, "Delivery Relations Table");
		}

		Map<String, Delivery.Builder> builders = new LinkedHashMap<>();

		for (Edge<V> edge : edges) {
			String departureDate = (String) edge.getProperties().get(TracingColumns.DELIVERY_DEPARTURE);
			String arrivalDate = (String) edge.getProperties().get(TracingColumns.DELIVERY_ARRIVAL);
			String lot = edge.getProperties().containsKey(TracingColumns.LOT_NUMBER)
					? (String) edge.getProperties().get(TracingColumns.LOT_NUMBER)
					: (String) edge.getProperties().get(BackwardUtils.DELIVERY_CHARGENUM);

			builders.put(edge.getId(),
					new Delivery.Builder(edge.getId(), edge.getFrom().getId(), edge.getTo().getId())
							.departure(getYear(departureDate), getMonth(departureDate), getDay(departureDate))
							.arrival(getYear(arrivalDate), getMonth(arrivalDate), getDay(arrivalDate))
							.lotId((String) edge.getProperties().get(TracingColumns.LOT_ID)).lot(lot));
		}

		SetMultimap<String, String> previousDeliveries = LinkedHashMultimap.create();
		SetMultimap<String, String> nextDeliveries = LinkedHashMultimap.create();

		for (DataRow row : deliveryRelationsTable) {
			String from = IO.getToCleanString(row.getCell(spec.findColumnIndex(fromColumn)));
			String to = IO.getToCleanString(row.getCell(spec.findColumnIndex(toColumn)));

			if (!builders.containsKey(from)) {
				skippedRows.put(row.getKey(),
						"Delivery with " + TracingColumns.ID + " \"" + from + "\" does not exist");
				continue;
			} else if (!builders.containsKey(to)) {
				skippedRows.put(row.getKey(), "Delivery with " + TracingColumns.ID + " \"" + to + "\" does not exist");
				continue;
			}

			previousDeliveries.put(to, from);
			nextDeliveries.put(from, to);
		}

		Map<String, Delivery> deliveries = new LinkedHashMap<>();

		builders.forEach((id, builder) -> deliveries.put(id,
				builder.connectedDeliveries(previousDeliveries.get(id), nextDeliveries.get(id)).build()));

		try {
			new Tracing(deliveries.values()).check();
		} catch (TracingException e) {
			throw new NotConfigurableException(e.getMessage());
		}

		return deliveries;
	}

	public static List<RegionNode> readRegions(BufferedDataTable shapeTable, Map<RowKey, String> skippedRows)
			throws NotConfigurableException {
		List<RegionNode> nodes = new ArrayList<>();
		List<DataColumnSpec> shapeColumns = IO.getColumns(shapeTable.getSpec(), ShapeBlobCell.TYPE);

		if (shapeColumns.isEmpty()) {
			throw new NotConfigurableException("Shape Table: Shape Column missing");
		}

		int shapeIndex = shapeTable.getSpec().findColumnIndex(shapeColumns.get(0).getName());
		int index = 0;

		for (DataRow row : shapeTable) {
			Geometry shape = GisUtils.getShape(row.getCell(shapeIndex));

			if (!(shape instanceof MultiPolygon)) {
				skippedRows.put(row.getKey(), "No valid shape");
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

	public static void assertColumnNotMissing(DataTableSpec spec, String columnName, String tableName)
			throws NotConfigurableException {
		if (!spec.containsName(columnName)) {
			String prefix = tableName != null ? tableName + ": " : "";

			throw new NotConfigurableException(prefix + "Column \"" + columnName + "\" is missing");
		}
	}
}
