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
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NotConfigurableException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.gis.geocode.GeocodingNodeModel;
import de.bund.bfr.knime.gis.shapecell.ShapeBlobCell;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.Naming;
import de.bund.bfr.knime.gis.views.canvas.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.PropertySchema;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition;

public class TracingUtils {

	public static final Naming NAMING = new Naming("Station", "Stations", "Delivery", "Deliveries");

	private TracingUtils() {
	}

	public static Map<String, Class<?>> getTableColumns(DataTableSpec spec) {
		Map<String, Class<?>> tableColumns = new LinkedHashMap<>();

		for (int i = 0; i < spec.getNumColumns(); i++) {
			DataColumnSpec cSpec = spec.getColumnSpec(i);

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

	public static Map<String, GraphNode> readGraphNodes(BufferedDataTable nodeTable, NodePropertySchema nodeSchema)
			throws NotConfigurableException {
		int idIndex = nodeTable.getSpec().findColumnIndex(TracingColumns.ID);

		if (idIndex == -1) {
			throw new NotConfigurableException("Station Table: Column \"" + TracingColumns.ID + "\" is missing");
		}

		Map<String, GraphNode> nodes = new LinkedHashMap<>();
		Set<String> ids = new LinkedHashSet<>();

		nodeSchema.getMap().put(TracingColumns.ID, String.class);

		for (DataRow row : nodeTable) {
			String id = IO.getToCleanString(row.getCell(idIndex));

			if (id == null) {
				throw new NotConfigurableException("Station Table: Missing value in " + TracingColumns.ID + " column");
			} else if (!ids.add(id)) {
				throw new NotConfigurableException(
						"Station Table: Duplicate value in " + TracingColumns.ID + " column: " + id);
			}

			Map<String, Object> properties = new LinkedHashMap<>();

			TracingUtils.addToProperties(properties, nodeSchema, nodeTable, row);
			properties.put(TracingColumns.ID, id);
			replaceNullsInInputProperties(properties, nodeSchema);
			nodes.put(id, new GraphNode(id, properties, null));
		}

		if (nodes.isEmpty()) {
			throw new NotConfigurableException("Station Table: No valid nodes contained in table");
		}

		return nodes;
	}

	public static Map<String, LocationNode> readLocationNodes(BufferedDataTable nodeTable,
			NodePropertySchema nodeSchema, Set<RowKey> invalidRows, boolean skipInvalid)
					throws NotConfigurableException {
		int idIndex = nodeTable.getSpec().findColumnIndex(TracingColumns.ID);
		int latIndex = nodeTable.getSpec().findColumnIndex(GeocodingNodeModel.LATITUDE_COLUMN);
		int lonIndex = nodeTable.getSpec().findColumnIndex(GeocodingNodeModel.LONGITUDE_COLUMN);

		if (idIndex == -1) {
			throw new NotConfigurableException("Station Table: Column \"" + TracingColumns.ID + "\" is missing");
		}

		if (latIndex == -1) {
			throw new NotConfigurableException(
					"Station Table: Column \"" + GeocodingNodeModel.LATITUDE_COLUMN + "\" is missing");
		}

		if (lonIndex == -1) {
			throw new NotConfigurableException(
					"Station Table: Column \"" + GeocodingNodeModel.LONGITUDE_COLUMN + "\" is missing");
		}

		Map<String, LocationNode> nodes = new LinkedHashMap<>();
		Set<String> ids = new LinkedHashSet<>();
		boolean hasCoordinates = false;

		nodeSchema.getMap().put(TracingColumns.ID, String.class);

		for (DataRow row : nodeTable) {
			String id = IO.getToCleanString(row.getCell(idIndex));
			Double lat = IO.getDouble(row.getCell(latIndex));
			Double lon = IO.getDouble(row.getCell(lonIndex));

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

			TracingUtils.addToProperties(properties, nodeSchema, nodeTable, row);
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
		int idIndex = edgeTable.getSpec().findColumnIndex(TracingColumns.ID);
		int fromIndex = edgeTable.getSpec().findColumnIndex(TracingColumns.FROM);
		int toIndex = edgeTable.getSpec().findColumnIndex(TracingColumns.TO);
		int deliveryDateIndex = edgeTable.getSpec().findColumnIndex(TracingColumns.DELIVERY_DEPARTURE);

		if (idIndex == -1) {
			throw new NotConfigurableException("Delivery Table: Column \"" + TracingColumns.ID + "\" is missing");
		}

		if (fromIndex == -1) {
			throw new NotConfigurableException("Delivery Table: Column \"" + TracingColumns.FROM + "\" is missing");
		}

		if (toIndex == -1) {
			throw new NotConfigurableException("Delivery Table: Column \"" + TracingColumns.TO + "\" is missing");
		}

		if (deliveryDateIndex == -1) {
			throw new NotConfigurableException(
					"Delivery Table: Column \"" + TracingColumns.DELIVERY_DEPARTURE + "\" is missing");
		} else if (edgeTable.getSpec().getColumnSpec(deliveryDateIndex).getType() != StringCell.TYPE) {
			throw new NotConfigurableException(
					"Delivery Table: Column \"" + TracingColumns.DELIVERY_DEPARTURE + "\" must be of type String");
		}

		List<Edge<V>> edges = new ArrayList<>();
		Set<String> ids = new LinkedHashSet<>();

		edgeSchema.getMap().put(TracingColumns.ID, String.class);
		edgeSchema.getMap().put(TracingColumns.FROM, String.class);
		edgeSchema.getMap().put(TracingColumns.TO, String.class);

		for (DataRow row : edgeTable) {
			String id = IO.getToCleanString(row.getCell(idIndex));

			if (id == null) {
				throw new NotConfigurableException("Delivery Table: Missing value in " + TracingColumns.ID + " column");
			} else if (!ids.add(id)) {
				throw new NotConfigurableException(
						"Delivery Table: Duplicate value in " + TracingColumns.ID + " column: " + id);
			}

			String from = IO.getToCleanString(row.getCell(fromIndex));
			String to = IO.getToCleanString(row.getCell(toIndex));
			V node1 = nodes.get(from);
			V node2 = nodes.get(to);

			if (node1 == null || node2 == null) {
				skippedRows.add(row.getKey());
				continue;
			}

			Map<String, Object> properties = new LinkedHashMap<>();

			TracingUtils.addToProperties(properties, edgeSchema, edgeTable, row);
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
		DataTableSpec dataSpec = tracingTable.getSpec();
		int idIndex = tracingTable.getSpec().findColumnIndex(TracingColumns.ID);
		int nextIndex = tracingTable.getSpec().findColumnIndex(TracingColumns.NEXT);

		if (idIndex == -1) {
			throw new NotConfigurableException("Delivery Relations Table: Column \"" + TracingColumns.ID
					+ "\" is missing. Try reexecuting the Supply Chain Reader"
					+ " or downloading an up-to-date workflow.");
		}

		if (nextIndex == -1) {
			throw new NotConfigurableException(
					"Delivery Relations Table: Column \"" + TracingColumns.NEXT + "\" is missing."
							+ " Try reexecuting the Supply Chain Reader" + " or downloading an up-to-date workflow.");
		}

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
			String id = IO.getToCleanString(row.getCell(dataSpec.findColumnIndex(TracingColumns.ID)));
			String next = IO.getToCleanString(row.getCell(dataSpec.findColumnIndex(TracingColumns.NEXT)));

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
			throw new NotConfigurableException("No Shape Column in table");
		}

		int shapeIndex = shapeTable.getSpec().findColumnIndex(shapeColumns.get(0));
		int index = 0;

		for (DataRow row : shapeTable) {
			Geometry shape = GisUtils.getShape(row.getCell(shapeIndex));

			if (!(shape instanceof MultiPolygon)) {
				skippedRows.add(row.getKey());
				continue;
			}

			nodes.add(new RegionNode(index + "", new LinkedHashMap<String, Object>(), (MultiPolygon) shape));
			index++;
		}

		if (nodes.isEmpty()) {
			throw new NotConfigurableException("No valid shapes contained in table");
		}

		return nodes;
	}

	private static void addToProperties(Map<String, Object> properties, PropertySchema schema, BufferedDataTable table,
			DataRow row) {
		for (String property : schema.getMap().keySet()) {
			Class<?> type = schema.getMap().get(property);
			int column = table.getSpec().findColumnIndex(property);

			if (column != -1) {
				DataCell cell = row.getCell(column);

				TracingUtils.addCellContentToMap(properties, property, type, cell);
			}
		}
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

	public static HighlightConditionList renameColumns(HighlightConditionList list, Set<String> columns) {
		HighlightConditionList newList = new HighlightConditionList(list);
		List<HighlightCondition> newL = new ArrayList<>();

		for (HighlightCondition c : list.getConditions()) {
			if (c instanceof AndOrHighlightCondition) {
				newL.add(renameColumn((AndOrHighlightCondition) c, columns));
			} else if (c instanceof ValueHighlightCondition) {
				newL.add(renameColumn((ValueHighlightCondition) c, columns));
			} else if (c instanceof LogicalValueHighlightCondition) {
				newL.add(new LogicalValueHighlightCondition(
						renameColumn(((LogicalValueHighlightCondition) c).getValueCondition(), columns),
						renameColumn(((LogicalValueHighlightCondition) c).getLogicalCondition(), columns)));
			}
		}

		newList.setConditions(newL);

		return newList;
	}

	private static AndOrHighlightCondition renameColumn(AndOrHighlightCondition c, Set<String> columns) {
		AndOrHighlightCondition newC = new AndOrHighlightCondition(c);
		List<List<LogicalHighlightCondition>> newL1 = new ArrayList<>();

		newC.setLabelProperty(renameColumn(c.getLabelProperty(), columns));

		for (List<LogicalHighlightCondition> l2 : c.getConditions()) {
			List<LogicalHighlightCondition> newL2 = new ArrayList<>();

			for (LogicalHighlightCondition l3 : l2) {
				LogicalHighlightCondition newL3 = new LogicalHighlightCondition(l3);

				newL3.setProperty(renameColumn(l3.getProperty(), columns));
				newL2.add(newL3);
			}

			newL1.add(newL2);
		}

		newC.setConditions(newL1);

		return newC;
	}

	private static ValueHighlightCondition renameColumn(ValueHighlightCondition c, Set<String> columns) {
		ValueHighlightCondition newC = new ValueHighlightCondition(c);

		newC.setProperty(renameColumn(c.getProperty(), columns));
		newC.setLabelProperty(renameColumn(c.getLabelProperty(), columns));

		return newC;
	}

	private static String renameColumn(String column, Set<String> columns) {
		if (column == null) {
			return null;
		}

		if (column.equals(TracingColumns.OLD_WEIGHT) && !columns.contains(TracingColumns.OLD_WEIGHT)) {
			return TracingColumns.WEIGHT;
		}

		if (column.equals(TracingColumns.OLD_OBSERVED) && !columns.contains(TracingColumns.OLD_OBSERVED)) {
			return TracingColumns.OBSERVED;
		}

		return column;
	}
}
