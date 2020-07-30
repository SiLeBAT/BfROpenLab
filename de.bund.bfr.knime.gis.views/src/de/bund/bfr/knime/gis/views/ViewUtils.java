/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.gis.views;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NotConfigurableException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;

public class ViewUtils {

	private ViewUtils() {
	}

	public static Map<String, Class<?>> getTableColumns(DataTableSpec spec) {
		Map<String, Class<?>> tableColumns = new LinkedHashMap<>();

		for (DataColumnSpec column : spec) {
			if (column.getType().equals(IntCell.TYPE)) {
				tableColumns.put(column.getName(), Integer.class);
			} else if (column.getType().equals(DoubleCell.TYPE)) {
				tableColumns.put(column.getName(), Double.class);
			} else if (column.getType().equals(BooleanCell.TYPE)) {
				tableColumns.put(column.getName(), Boolean.class);
			} else {
				tableColumns.put(column.getName(), String.class);
			}
		}

		return tableColumns;
	}

	public static Map<String, String> getIdToRegionMap(BufferedDataTable nodeTable, String nodeIdColumn,
			String nodeRegionColumn) throws NotConfigurableException {
		DataTableSpec spec = nodeTable.getSpec();

		assertColumnNotMissing(spec, nodeIdColumn, "Node Table");
		assertColumnNotMissing(spec, nodeRegionColumn, "Node Table");

		Map<String, String> idToRegionMap = new LinkedHashMap<>();

		for (DataRow row : nodeTable) {
			String id = IO.getToCleanString(row.getCell(spec.findColumnIndex(nodeIdColumn)));
			String region = IO.getToCleanString(row.getCell(spec.findColumnIndex(nodeRegionColumn)));

			if (id != null && region != null) {
				idToRegionMap.put(id, region);
			}
		}

		return idToRegionMap;
	}

	public static Map<String, MultiPolygon> readPolygons(BufferedDataTable shapeTable, String shapeColumn,
			String shapeRegionColumn) throws NotConfigurableException {
		DataTableSpec spec = shapeTable.getSpec();

		assertColumnNotMissing(spec, shapeColumn, "Shape Table");
		assertColumnNotMissing(spec, shapeRegionColumn, "Shape Table");

		Map<String, MultiPolygon> polygonMap = new LinkedHashMap<>();

		for (DataRow row : shapeTable) {
			Geometry shape = GisUtils.getShape(row.getCell(spec.findColumnIndex(shapeColumn)));
			String region = IO.getToCleanString(row.getCell(spec.findColumnIndex(shapeRegionColumn)));

			if (region != null && shape instanceof MultiPolygon) {
				polygonMap.put(region, (MultiPolygon) shape);
			}
		}

		return polygonMap;
	}

	public static Map<String, GraphNode> readGraphNodes(BufferedDataTable nodeTable,
			Map<String, Class<?>> nodeProperties, String nodeIdColumn, String nodeRegionColumn)
			throws NotConfigurableException {
		DataTableSpec spec = nodeTable.getSpec();

		assertColumnNotMissing(spec, nodeIdColumn, "Node Table");
		nodeProperties.put(nodeIdColumn, String.class);

		if (nodeRegionColumn != null) {
			nodeProperties.put(nodeRegionColumn, String.class);
			assertColumnNotMissing(spec, nodeRegionColumn, "Node Table");
		}

		Map<String, GraphNode> nodes = new LinkedHashMap<>();

		for (DataRow row : nodeTable) {
			String id = IO.getToCleanString(row.getCell(spec.findColumnIndex(nodeIdColumn)));
			String region = null;
			Map<String, Object> properties = new LinkedHashMap<>();

			if (nodeRegionColumn != null) {
				region = IO.getToCleanString(row.getCell(spec.findColumnIndex(nodeRegionColumn)));
			}

			ViewUtils.addToProperties(properties, nodeProperties, nodeTable.getSpec(), row);
			properties.put(nodeIdColumn, id);
			properties.put(nodeRegionColumn, region);
			nodes.put(id, new GraphNode(id, properties, region));
		}

		return nodes;
	}

	public static List<RegionNode> readRegionNodes(BufferedDataTable shapeTable, String shapeColumn)
			throws NotConfigurableException {
		assertColumnNotMissing(shapeTable.getSpec(), shapeColumn, "Shape Table");

		List<RegionNode> nodes = new ArrayList<>();
		int index = 0;

		for (DataRow row : shapeTable) {
			Geometry shape = GisUtils.getShape(row.getCell(shapeTable.getSpec().findColumnIndex(shapeColumn)));

			if (shape instanceof MultiPolygon) {
				nodes.add(new RegionNode(String.valueOf(index++), new LinkedHashMap<>(0), (MultiPolygon) shape));
			}
		}

		return nodes;
	}

	public static Map<String, RegionNode> readRegionNodes(BufferedDataTable nodeTable,
			Map<String, Class<?>> nodeProperties, Map<String, MultiPolygon> polygonMap,
			Map<String, String> idToRegionMap, String nodeIdColumn, Set<String> nonExistingRegions)
			throws NotConfigurableException {
		assertColumnNotMissing(nodeTable.getSpec(), nodeIdColumn, "Node Table");
		nodeProperties.put(nodeIdColumn, String.class);

		Map<String, Map<String, Object>> nodeMap = new LinkedHashMap<>();

		for (DataRow row : nodeTable) {
			String id = IO.getToCleanString(row.getCell(nodeTable.getSpec().findColumnIndex(nodeIdColumn)));

			if (idToRegionMap != null) {
				id = idToRegionMap.get(id);
			}

			if (id != null) {
				if (!nodeMap.containsKey(id)) {
					nodeMap.put(id, new LinkedHashMap<>());
				}

				Map<String, Object> map = nodeMap.get(id);

				ViewUtils.addToProperties(map, nodeProperties, nodeTable.getSpec(), row);
				map.put(nodeIdColumn, id);
			}
		}

		Map<String, RegionNode> nodes = new LinkedHashMap<>();

		polygonMap.forEach((id, polygon) -> {
			Map<String, Object> properties = nodeMap.get(id);

			if (properties == null) {
				properties = new LinkedHashMap<>();

				for (String property : nodeProperties.keySet()) {
					properties.put(property, null);
				}

				properties.put(nodeIdColumn, id);
			}

			nodes.put(id, new RegionNode(id, properties, polygon));
		});

		for (String id : nodeMap.keySet()) {
			if (!polygonMap.containsKey(id)) {
				nonExistingRegions.add(id);
			}
		}

		return nodes;
	}

	public static Map<String, LocationNode> readLocationNodes(BufferedDataTable nodeTable,
			Map<String, Class<?>> nodeProperties, String nodeIdColumn, String latitudeColumn, String longitudeColumn)
			throws NotConfigurableException {
		DataTableSpec spec = nodeTable.getSpec();

		assertColumnNotMissing(spec, latitudeColumn, "Node Table");
		assertColumnNotMissing(spec, longitudeColumn, "Node Table");

		if (nodeIdColumn != null) {
			assertColumnNotMissing(spec, nodeIdColumn, "Node Table");
		}

		Map<String, LocationNode> nodes = new LinkedHashMap<>();
		int index = 0;

		for (DataRow row : nodeTable) {
			String id;

			if (nodeIdColumn != null) {
				id = IO.getToCleanString(row.getCell(spec.findColumnIndex(nodeIdColumn)));
			} else {
				id = String.valueOf(index++);
			}

			Double lat = IO.getDouble(row.getCell(spec.findColumnIndex(latitudeColumn)));
			Double lon = IO.getDouble(row.getCell(spec.findColumnIndex(longitudeColumn)));

			if (lat == null || lon == null) {
				continue;
			}

			Map<String, Object> properties = new LinkedHashMap<>();

			ViewUtils.addToProperties(properties, nodeProperties, nodeTable.getSpec(), row);
			properties.put(nodeIdColumn, id);
			nodes.put(id, new LocationNode(id, properties, new Point2D.Double(lat, lon)));
		}

		return nodes;
	}

	public static <V extends Node> List<Edge<V>> readEdges(BufferedDataTable edgeTable,
			Map<String, Class<?>> edgeProperties, Map<String, V> nodes, Map<String, String> idToRegionMap,
			String edgeFromColumn, String edgeToColumn) throws NotConfigurableException {
		DataTableSpec spec = edgeTable.getSpec();

		assertColumnNotMissing(spec, edgeFromColumn, "Edge Table");
		assertColumnNotMissing(spec, edgeToColumn, "Edge Table");
		edgeProperties.put(edgeFromColumn, String.class);
		edgeProperties.put(edgeToColumn, String.class);

		List<Edge<V>> edges = new ArrayList<>();
		int index = 0;

		for (DataRow row : edgeTable) {
			String from = IO.getToCleanString(row.getCell(spec.findColumnIndex(edgeFromColumn)));
			String to = IO.getToCleanString(row.getCell(spec.findColumnIndex(edgeToColumn)));

			if (idToRegionMap != null) {
				from = idToRegionMap.get(from);
				to = idToRegionMap.get(to);
			}

			V node1 = nodes.get(from);
			V node2 = nodes.get(to);

			if (node1 != null && node2 != null) {
				Map<String, Object> properties = new LinkedHashMap<>();

				ViewUtils.addToProperties(properties, edgeProperties, edgeTable.getSpec(), row);
				properties.put(edgeFromColumn, from);
				properties.put(edgeToColumn, to);
				edges.add(new Edge<>(String.valueOf(index++), properties, node1, node2));
			}
		}

		return edges;
	}

	public static String createNewIdProperty(List<? extends Element> elements, Map<String, Class<?>> properties) {
		String name = KnimeUtils.createNewValue("ID", properties.keySet());

		properties.put(name, String.class);

		for (Element element : elements) {
			element.getProperties().put(name, element.getId());
		}

		return name;
	}

	private static void addToProperties(Map<String, Object> properties, Map<String, Class<?>> propertyTypes,
			DataTableSpec spec, DataRow row) {
		propertyTypes.forEach((property, type) -> ViewUtils.addCellContentToMap(properties, property, type,
				row.getCell(spec.findColumnIndex(property))));
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

	private static void assertColumnNotMissing(DataTableSpec spec, String columnName, String tableName)
			throws NotConfigurableException {
		if (!spec.containsName(columnName)) {
			throw new NotConfigurableException(tableName + ": Column \"" + columnName + "\" is missing");
		}
	}
}
