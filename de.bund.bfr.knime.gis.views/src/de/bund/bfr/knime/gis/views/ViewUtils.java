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
package de.bund.bfr.knime.gis.views;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

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

	private static MathTransform transform = createTransform();
	private static GeometryFactory factory = new GeometryFactory();

	private ViewUtils() {
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

	public static Map<String, String> getIdToRegionMap(
			BufferedDataTable nodeTable, String nodeIdColumn,
			String nodeRegionColumn) throws InvalidSettingsException {
		Map<String, String> idToRegionMap = new LinkedHashMap<>();
		int idIndex = nodeTable.getSpec().findColumnIndex(nodeIdColumn);
		int regionIndex = nodeTable.getSpec().findColumnIndex(nodeRegionColumn);

		if (idIndex == -1) {
			throw new InvalidSettingsException("Column \"" + nodeIdColumn
					+ "\" is missing");
		}

		if (regionIndex == -1) {
			throw new InvalidSettingsException("Column \"" + nodeRegionColumn
					+ "\" is missing");
		}

		for (DataRow row : nodeTable) {
			String id = IO.getToCleanString(row.getCell(idIndex));
			String region = IO.getToCleanString(row.getCell(regionIndex));

			if (id != null && region != null) {
				idToRegionMap.put(id, region);
			}
		}

		return idToRegionMap;
	}

	public static Map<String, MultiPolygon> readPolygons(
			BufferedDataTable shapeTable, String shapeColumn,
			String shapeRegionColumn) throws InvalidSettingsException {
		Map<String, MultiPolygon> polygonMap = new LinkedHashMap<>();
		int shapeIndex = shapeTable.getSpec().findColumnIndex(shapeColumn);
		int shapeRegionIndex = shapeTable.getSpec().findColumnIndex(
				shapeRegionColumn);

		if (shapeIndex == -1) {
			throw new InvalidSettingsException("Column \"" + shapeColumn
					+ "\" is missing");
		}

		if (shapeRegionIndex == -1) {
			throw new InvalidSettingsException("Column \"" + shapeRegionColumn
					+ "\" is missing");
		}

		for (DataRow row : shapeTable) {
			Geometry shape = GisUtils.getShape(row.getCell(shapeIndex));
			String region = IO.getToCleanString(row.getCell(shapeRegionIndex));

			if (region != null && shape instanceof MultiPolygon) {
				try {
					polygonMap.put(region,
							(MultiPolygon) JTS.transform(shape, transform));
				} catch (MismatchedDimensionException e) {
					e.printStackTrace();
				} catch (TransformException e) {
					e.printStackTrace();
				}
			}
		}

		return polygonMap;
	}

	public static Map<String, GraphNode> readGraphNodes(
			BufferedDataTable nodeTable, Map<String, Class<?>> nodeProperties,
			String nodeIdColumn, String nodeRegionColumn)
			throws InvalidSettingsException {
		Map<String, GraphNode> nodes = new LinkedHashMap<>();
		int nodeIdIndex = nodeTable.getSpec().findColumnIndex(nodeIdColumn);
		int nodeRegionIndex = nodeTable.getSpec().findColumnIndex(
				nodeRegionColumn);

		if (nodeIdIndex == -1) {
			throw new InvalidSettingsException("Column \"" + nodeIdColumn
					+ "\" is missing");
		}

		nodeProperties.put(nodeIdColumn, String.class);

		if (nodeRegionColumn != null) {
			nodeProperties.put(nodeRegionColumn, String.class);

			if (nodeRegionIndex == -1) {
				throw new InvalidSettingsException("Column \""
						+ nodeRegionColumn + "\" is missing");
			}
		}

		for (DataRow row : nodeTable) {
			String id = IO.getToCleanString(row.getCell(nodeIdIndex));
			String region = null;
			Map<String, Object> properties = new LinkedHashMap<>();

			if (nodeRegionIndex != -1) {
				region = IO.getToCleanString(row.getCell(nodeRegionIndex));
			}

			ViewUtils.addToProperties(properties, nodeProperties, nodeTable,
					row);
			properties.put(nodeIdColumn, id);
			properties.put(nodeRegionColumn, region);
			nodes.put(id, new GraphNode(id, properties, region));
		}

		return nodes;
	}

	public static List<RegionNode> readRegionNodes(
			BufferedDataTable shapeTable, String shapeColumn)
			throws InvalidSettingsException {
		List<RegionNode> nodes = new ArrayList<>();
		int shapeIndex = shapeTable.getSpec().findColumnIndex(shapeColumn);
		int index = 0;

		if (shapeIndex == -1) {
			throw new InvalidSettingsException("Column \"" + shapeColumn
					+ "\" is missing");
		}

		for (DataRow row : shapeTable) {
			Geometry shape = GisUtils.getShape(row.getCell(shapeIndex));

			if (shape instanceof MultiPolygon) {
				try {
					nodes.add(new RegionNode(index + "",
							new LinkedHashMap<String, Object>(),
							(MultiPolygon) JTS.transform(shape, transform)));
					index++;
				} catch (MismatchedDimensionException e) {
					e.printStackTrace();
				} catch (TransformException e) {
					e.printStackTrace();
				}
			}
		}

		return nodes;
	}

	public static Map<String, RegionNode> readRegionNodes(
			BufferedDataTable nodeTable, Map<String, Class<?>> nodeProperties,
			Map<String, MultiPolygon> polygonMap,
			Map<String, String> idToRegionMap, String nodeIdColumn,
			Set<String> nonExistingRegions) throws InvalidSettingsException {
		Map<String, RegionNode> nodes = new LinkedHashMap<>();
		Map<String, Map<String, Object>> nodeMap = new LinkedHashMap<>();
		int nodeIdIndex = nodeTable.getSpec().findColumnIndex(nodeIdColumn);

		if (nodeIdIndex == -1) {
			throw new InvalidSettingsException("Column \"" + nodeIdColumn
					+ "\" is missing");
		}

		nodeProperties.put(nodeIdColumn, String.class);

		for (DataRow row : nodeTable) {
			String id = IO.getToCleanString(row.getCell(nodeIdIndex));

			if (idToRegionMap != null) {
				id = idToRegionMap.get(id);
			}

			if (id != null) {
				if (!nodeMap.containsKey(id)) {
					nodeMap.put(id, new LinkedHashMap<String, Object>());
				}

				Map<String, Object> map = nodeMap.get(id);

				ViewUtils.addToProperties(map, nodeProperties, nodeTable, row);
				map.put(nodeIdColumn, id);
			}
		}

		for (String id : polygonMap.keySet()) {
			Map<String, Object> properties = nodeMap.get(id);

			if (properties == null) {
				properties = new LinkedHashMap<>();

				for (String property : nodeProperties.keySet()) {
					properties.put(property, null);
				}

				properties.put(nodeIdColumn, id);
			}

			nodes.put(id, new RegionNode(id, properties, polygonMap.get(id)));
		}

		for (String id : nodeMap.keySet()) {
			if (!polygonMap.containsKey(id)) {
				nonExistingRegions.add(id);
			}
		}

		return nodes;
	}

	public static Map<String, LocationNode> readLocationNodes(
			BufferedDataTable nodeTable, Map<String, Class<?>> nodeProperties,
			String nodeIdColumn, String latitudeColumn, String longitudeColumn)
			throws InvalidSettingsException {
		Map<String, LocationNode> nodes = new LinkedHashMap<>();
		int latIndex = nodeTable.getSpec().findColumnIndex(latitudeColumn);
		int lonIndex = nodeTable.getSpec().findColumnIndex(longitudeColumn);
		int nodeIdIndex = nodeTable.getSpec().findColumnIndex(nodeIdColumn);
		int locationIndex = 0;

		if (latIndex == -1) {
			throw new InvalidSettingsException("Column \"" + latitudeColumn
					+ "\" is missing");
		}

		if (lonIndex == -1) {
			throw new InvalidSettingsException("Column \"" + longitudeColumn
					+ "\" is missing");
		}

		if (nodeIdColumn != null) {
			nodeProperties.put(nodeIdColumn, String.class);

			if (nodeIdIndex == -1) {
				throw new InvalidSettingsException("Column \"" + nodeIdColumn
						+ "\" is missing");
			}
		}

		for (DataRow row : nodeTable) {
			String id;

			if (nodeIdIndex != -1) {
				id = IO.getToCleanString(row.getCell(nodeIdIndex));
			} else {
				id = locationIndex + "";
				locationIndex++;
			}

			Double lat = IO.getDouble(row.getCell(latIndex));
			Double lon = IO.getDouble(row.getCell(lonIndex));

			if (lat == null || lon == null) {
				continue;
			}

			Point p = null;

			try {
				p = (Point) JTS.transform(
						factory.createPoint(new Coordinate(lat, lon)),
						transform);
			} catch (MismatchedDimensionException e) {
				e.printStackTrace();
				continue;
			} catch (TransformException e) {
				e.printStackTrace();
				continue;
			}

			Map<String, Object> properties = new LinkedHashMap<>();

			ViewUtils.addToProperties(properties, nodeProperties, nodeTable,
					row);
			properties.put(nodeIdColumn, id);
			nodes.put(
					id,
					new LocationNode(id, properties, new Point2D.Double(p
							.getX(), p.getY())));
		}

		return nodes;
	}

	public static <V extends Node> List<Edge<V>> readEdges(
			BufferedDataTable edgeTable, Map<String, Class<?>> edgeProperties,
			Map<String, V> nodes, Map<String, String> idToRegionMap,
			String edgeFromColumn, String edgeToColumn)
			throws InvalidSettingsException {
		List<Edge<V>> edges = new ArrayList<>();
		int fromIndex = edgeTable.getSpec().findColumnIndex(edgeFromColumn);
		int toIndex = edgeTable.getSpec().findColumnIndex(edgeToColumn);

		if (fromIndex == -1) {
			throw new InvalidSettingsException("Column \"" + edgeFromColumn
					+ "\" is missing");
		}

		if (toIndex == -1) {
			throw new InvalidSettingsException("Column \"" + edgeToColumn
					+ "\" is missing");
		}

		edgeProperties.put(edgeFromColumn, String.class);
		edgeProperties.put(edgeToColumn, String.class);

		int edgeIndex = 0;

		for (DataRow row : edgeTable) {
			String from = IO.getToCleanString(row.getCell(fromIndex));
			String to = IO.getToCleanString(row.getCell(toIndex));

			if (idToRegionMap != null) {
				from = idToRegionMap.get(from);
				to = idToRegionMap.get(to);
			}

			V node1 = nodes.get(from);
			V node2 = nodes.get(to);

			if (node1 != null && node2 != null) {
				Map<String, Object> properties = new LinkedHashMap<>();

				ViewUtils.addToProperties(properties, edgeProperties,
						edgeTable, row);
				properties.put(edgeFromColumn, from);
				properties.put(edgeToColumn, to);
				edges.add(new Edge<>(edgeIndex + "", properties, node1, node2));
				edgeIndex++;
			}
		}

		return edges;
	}

	public static String createNewIdProperty(List<? extends Element> elements,
			Map<String, Class<?>> properties) {
		String name = KnimeUtils.createNewValue("ID", properties.keySet());

		properties.put(name, String.class);

		for (Element element : elements) {
			element.getProperties().put(name, element.getId());
		}

		return name;
	}

	private static void addToProperties(Map<String, Object> properties,
			Map<String, Class<?>> propertyTypes, BufferedDataTable table,
			DataRow row) {
		for (String property : propertyTypes.keySet()) {
			Class<?> type = propertyTypes.get(property);
			int column = table.getSpec().findColumnIndex(property);
			DataCell cell = row.getCell(column);

			ViewUtils.addCellContentToMap(properties, property, type, cell);
		}
	}

	private static void addCellContentToMap(Map<String, Object> map,
			String property, Class<?> type, DataCell cell) {
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

	private static MathTransform createTransform() {
		MathTransform transform = null;

		try {
			transform = CRS.findMathTransform(CRS.decode("EPSG:4326"),
					CRS.decode("EPSG:3857"), true);
		} catch (NoSuchAuthorityCodeException e) {
			e.printStackTrace();
		} catch (FactoryException e) {
			e.printStackTrace();
		}

		return transform;
	}
}
