/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
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
package de.bund.bfr.knime.openkrise.views;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.knime.base.data.xml.SvgCell;
import org.knime.base.data.xml.SvgImageContent;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.image.png.PNGImageContent;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.gis.views.canvas.Canvas;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtilities;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;

public class TracingUtilities {

	private TracingUtilities() {
	}

	public static ImagePortObject getImage(Canvas<?> canvas, boolean asSvg)
			throws IOException {
		if (asSvg) {
			return new ImagePortObject(new SvgImageContent(
					canvas.getSvgDocument(), true), new ImagePortObjectSpec(
					SvgCell.TYPE));
		} else {
			BufferedImage img = canvas.getImage();
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			ImageIO.write(img, "png", out);

			return new ImagePortObject(new PNGImageContent(out.toByteArray()),
					new ImagePortObjectSpec(PNGImageContent.TYPE));
		}
	}

	public static ImagePortObjectSpec getImageSpec(boolean asSvg) {
		if (asSvg) {
			return new ImagePortObjectSpec(SvgCell.TYPE);
		} else {
			return new ImagePortObjectSpec(PNGImageContent.TYPE);
		}
	}

	public static Set<Integer> getConnectedNodes(BufferedDataTable nodeTable,
			BufferedDataTable edgeTable) {
		Set<Integer> nodes = new LinkedHashSet<Integer>();
		Set<Integer> connectedNodes = new LinkedHashSet<Integer>();
		int nodeIdIndex = nodeTable.getSpec().findColumnIndex(
				TracingConstants.ID_COLUMN);
		int edgeFromIndex = edgeTable.getSpec().findColumnIndex(
				TracingConstants.FROM_COLUMN);
		int edgeToIndex = edgeTable.getSpec().findColumnIndex(
				TracingConstants.TO_COLUMN);

		if (nodeIdIndex != -1 && edgeFromIndex != -1 && edgeToIndex != -1) {
			for (DataRow row : nodeTable) {
				nodes.add(IO.getInt(row.getCell(nodeIdIndex)));
			}

			for (DataRow row : edgeTable) {
				int from = IO.getInt(row.getCell(edgeFromIndex));
				int to = IO.getInt(row.getCell(edgeToIndex));

				if (nodes.contains(from) && nodes.contains(to)) {
					connectedNodes.add(from);
					connectedNodes.add(to);
				}
			}
		}

		return connectedNodes;
	}

	public static Set<Integer> getSimpleSuppliers(BufferedDataTable nodeTable,
			BufferedDataTable edgeTable) {
		Map<Integer, Set<Integer>> customers = new LinkedHashMap<Integer, Set<Integer>>();
		Set<Integer> simpleSuppliers = new LinkedHashSet<Integer>();
		int nodeIdIndex = nodeTable.getSpec().findColumnIndex(
				TracingConstants.ID_COLUMN);
		int edgeFromIndex = edgeTable.getSpec().findColumnIndex(
				TracingConstants.FROM_COLUMN);
		int edgeToIndex = edgeTable.getSpec().findColumnIndex(
				TracingConstants.TO_COLUMN);

		if (nodeIdIndex != -1 && edgeFromIndex != -1 && edgeToIndex != -1) {
			for (DataRow row : nodeTable) {
				customers.put(IO.getInt(row.getCell(nodeIdIndex)),
						new LinkedHashSet<Integer>());
			}

			for (DataRow row : edgeTable) {
				int from = IO.getInt(row.getCell(edgeFromIndex));
				int to = IO.getInt(row.getCell(edgeToIndex));

				customers.remove(to);

				if (customers.containsKey(from)) {
					customers.get(from).add(to);
				}
			}

			for (int id : customers.keySet()) {
				if (customers.get(id).size() == 1) {
					simpleSuppliers.add(id);
				}
			}
		}

		return simpleSuppliers;
	}

	public static Map<Integer, GraphNode> readGraphNodes(
			BufferedDataTable nodeTable, Map<String, Class<?>> nodeProperties,
			Set<Integer> connectedNodes, boolean skipEdgelessNodes) {
		Map<Integer, GraphNode> nodes = new LinkedHashMap<Integer, GraphNode>();
		int nodeIdIndex = nodeTable.getSpec().findColumnIndex(
				TracingConstants.ID_COLUMN);

		if (nodeIdIndex == -1) {
			return nodes;
		}

		for (DataRow row : nodeTable) {
			int id = IO.getInt(row.getCell(nodeIdIndex));

			if (skipEdgelessNodes && !connectedNodes.contains(id)) {
				continue;
			}

			String region = null;
			Map<String, Object> properties = new LinkedHashMap<String, Object>();

			TracingUtilities.addToProperties(properties, nodeProperties,
					nodeTable, row);
			nodes.put(id, new GraphNode(id + "", properties, region));
		}

		return nodes;
	}

	public static List<Edge<GraphNode>> readEdges(BufferedDataTable edgeTable,
			Map<String, Class<?>> edgeProperties,
			Map<Integer, GraphNode> nodes, boolean joinEdges) {
		List<Edge<GraphNode>> edges = new ArrayList<Edge<GraphNode>>();
		int idIndex = edgeTable.getSpec().findColumnIndex(
				TracingConstants.ID_COLUMN);
		int fromIndex = edgeTable.getSpec().findColumnIndex(
				TracingConstants.FROM_COLUMN);
		int toIndex = edgeTable.getSpec().findColumnIndex(
				TracingConstants.TO_COLUMN);

		if (fromIndex == -1 || toIndex == -1) {
			return edges;
		}

		if (!joinEdges) {
			for (DataRow row : edgeTable) {
				int id = IO.getInt(row.getCell(idIndex));
				int from = IO.getInt(row.getCell(fromIndex));
				int to = IO.getInt(row.getCell(toIndex));
				GraphNode node1 = nodes.get(from);
				GraphNode node2 = nodes.get(to);

				if (node1 != null && node2 != null) {
					Map<String, Object> properties = new LinkedHashMap<String, Object>();

					TracingUtilities.addToProperties(properties,
							edgeProperties, edgeTable, row);
					edges.add(new Edge<GraphNode>(id + "", properties, node1,
							node2));
				}
			}
		} else {
			Map<Integer, Map<Integer, Map<String, Object>>> edgeMap = new LinkedHashMap<Integer, Map<Integer, Map<String, Object>>>();

			for (DataRow row : edgeTable) {
				int from = IO.getInt(row.getCell(fromIndex));
				int to = IO.getInt(row.getCell(toIndex));

				if (!edgeMap.containsKey(from)) {
					edgeMap.put(from,
							new LinkedHashMap<Integer, Map<String, Object>>());
				}

				if (!edgeMap.get(from).containsKey(to)) {
					edgeMap.get(from).put(to,
							new LinkedHashMap<String, Object>());
				}

				Map<String, Object> map = edgeMap.get(from).get(to);

				TracingUtilities.addToProperties(map, edgeProperties,
						edgeTable, row);
			}

			int edgeIndex = 0;

			for (int from : edgeMap.keySet()) {
				for (int to : edgeMap.get(from).keySet()) {
					GraphNode n1 = nodes.get(from);
					GraphNode n2 = nodes.get(to);

					if (n1 != null && n2 != null) {
						Map<String, Object> map = edgeMap.get(from).get(to);

						map.put(TracingConstants.ID_COLUMN, edgeIndex);
						edges.add(new Edge<GraphNode>(edgeIndex + "", edgeMap
								.get(from).get(to), n1, n2));
						edgeIndex++;
					}
				}
			}
		}

		return edges;
	}

	private static void addToProperties(Map<String, Object> properties,
			Map<String, Class<?>> propertyTypes, BufferedDataTable table,
			DataRow row) {
		for (String property : propertyTypes.keySet()) {
			Class<?> type = propertyTypes.get(property);
			int column = table.getSpec().findColumnIndex(property);

			if (column != -1) {
				DataCell cell = row.getCell(column);

				TracingUtilities.addCellContentToMap(properties, property,
						type, cell);
			}
		}
	}

	private static void addCellContentToMap(Map<String, Object> map,
			String property, Class<?> type, DataCell cell) {
		Object obj = null;

		if (type == String.class) {
			obj = IO.getCleanString(cell);
		} else if (type == Integer.class) {
			obj = IO.getInt(cell);
		} else if (type == Double.class) {
			obj = IO.getDouble(cell);
		} else if (type == Boolean.class) {
			obj = IO.getBoolean(cell);
		}

		CanvasUtilities.addObjectToMap(map, property, type, obj);
	}
}
