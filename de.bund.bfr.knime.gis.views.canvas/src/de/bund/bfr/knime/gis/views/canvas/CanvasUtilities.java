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
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.transformer.EdgeDrawTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.EdgeStrokeTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.LabelTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeFillTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class CanvasUtilities {

	private static final int TEXTURE_SIZE = 3;

	private CanvasUtilities() {
	}

	public static Point2D addPoints(Point2D p1, Point2D p2) {
		return new Point2D.Double(p1.getX() + p2.getX(), p1.getY() + p2.getY());
	}

	public static Point2D substractPoints(Point2D p1, Point2D p2) {
		return new Point2D.Double(p1.getX() - p2.getX(), p1.getY() - p2.getY());
	}

	public static Point2D getCenter(Collection<Point2D> points) {
		double x = 0.0;
		double y = 0.0;

		for (Point2D p : points) {
			if (p == null) {
				return null;
			}

			x += p.getX();
			y += p.getY();
		}

		x /= points.size();
		y /= points.size();

		return new Point2D.Double(x, y);
	}

	public static <V extends Node> Map<Edge<V>, Set<Edge<V>>> joinEdges(
			Collection<Edge<V>> edges, Map<String, Class<?>> properties,
			String idProperty, String fromProperty, String toProperty) {
		Map<V, Map<V, Set<Edge<V>>>> edgeMap = new LinkedHashMap<V, Map<V, Set<Edge<V>>>>();

		for (Edge<V> edge : edges) {
			V from = edge.getFrom();
			V to = edge.getTo();

			if (!edgeMap.containsKey(from)) {
				edgeMap.put(from, new LinkedHashMap<V, Set<Edge<V>>>());
			}

			if (!edgeMap.get(from).containsKey(to)) {
				edgeMap.get(from).put(to, new LinkedHashSet<Edge<V>>());
			}

			edgeMap.get(from).get(to).add(edge);
		}

		Map<Edge<V>, Set<Edge<V>>> joined = new LinkedHashMap<Edge<V>, Set<Edge<V>>>();
		int index = 0;

		for (V from : edgeMap.keySet()) {
			for (V to : edgeMap.get(from).keySet()) {
				Map<String, Object> prop = new LinkedHashMap<String, Object>();

				for (Edge<V> edge : edgeMap.get(from).get(to)) {
					CanvasUtilities.addMapToMap(prop, properties,
							edge.getProperties());
				}

				if (properties.get(idProperty) == String.class) {
					prop.put(idProperty, index + "");
				} else if (properties.get(idProperty) == Integer.class) {
					prop.put(idProperty, index);
				}

				if (properties.get(fromProperty) == String.class) {
					prop.put(fromProperty, from.getId());
				} else if (properties.get(fromProperty) == Integer.class) {
					prop.put(fromProperty, Integer.parseInt(from.getId()));
				}

				if (properties.get(toProperty) == String.class) {
					prop.put(toProperty, to.getId());
				} else if (properties.get(toProperty) == Integer.class) {
					prop.put(toProperty, Integer.parseInt(to.getId()));
				}

				joined.put(new Edge<V>(index + "", prop, from, to), edgeMap
						.get(from).get(to));
				index++;
			}
		}

		return joined;
	}

	public static void addMapToMap(Map<String, Object> map,
			Map<String, Class<?>> properties, Map<String, Object> addMap) {
		for (String property : properties.keySet()) {
			addObjectToMap(map, property, properties.get(property),
					addMap.get(property));
		}
	}

	public static void addObjectToMap(Map<String, Object> map, String property,
			Class<?> type, Object obj) {
		if (type == String.class) {
			String value = (String) obj;

			if (map.containsKey(property)) {
				if (map.get(property) == null
						|| !map.get(property).equals(value)) {
					map.put(property, null);
				}
			} else {
				map.put(property, value);
			}
		} else if (type == Integer.class) {
			Integer value = (Integer) obj;

			if (map.get(property) != null) {
				if (value != null) {
					map.put(property, (Integer) map.get(property) + value);
				}
			} else {
				map.put(property, value);
			}
		} else if (type == Double.class) {
			Double value = (Double) obj;

			if (map.get(property) != null) {
				if (value != null) {
					map.put(property, (Double) map.get(property) + value);
				}
			} else {
				map.put(property, value);
			}
		} else if (type == Boolean.class) {
			Boolean value = (Boolean) obj;

			if (map.containsKey(property)) {
				if (map.get(property) == null
						|| !map.get(property).equals(value)) {
					map.put(property, null);
				}
			} else {
				map.put(property, value);
			}
		}
	}

	public static <T extends Element> Set<T> getHighlightedElements(
			Collection<T> elements, List<HighlightCondition> highlightConditions) {
		Set<T> highlightedElements = new LinkedHashSet<T>();

		for (HighlightCondition condition : highlightConditions) {
			Map<T, Double> highlighted = condition.getValues(elements);

			for (T element : highlighted.keySet()) {
				if (highlighted.get(element) > 0.0) {
					highlightedElements.add(element);
				}
			}
		}

		return highlightedElements;
	}

	public static Set<String> getElementIds(
			Collection<? extends Element> elements) {
		Set<String> ids = new LinkedHashSet<String>();

		for (Element element : elements) {
			ids.add(element.getId());
		}

		return ids;
	}

	public static <T extends Element> Set<T> getElementsById(
			Collection<T> elements, Collection<String> ids) {
		Set<T> result = new LinkedHashSet<T>();

		for (T element : elements) {
			if (ids.contains(element.getId())) {
				result.add(element);
			}
		}

		return result;
	}

	public static <T extends Element> Map<String, T> getElementsById(
			Collection<T> elements) {
		Map<String, T> result = new LinkedHashMap<String, T>();

		for (T element : elements) {
			result.put(element.getId(), element);
		}

		return result;
	}

	public static String createNewProperty(String name,
			Map<String, Class<?>> properties) {
		if (!properties.containsKey(name)) {
			return name;
		}

		for (int i = 2;; i++) {
			String newName = name + "_" + i;

			if (!properties.containsKey(newName)) {
				return newName;
			}
		}
	}

	public static <V extends Node, E extends Edge<V>> boolean applyNodeHighlights(
			VisualizationViewer<V, E> viewer, Collection<V> nodes,
			Collection<E> edges, Collection<V> invisibleNodes,
			Collection<E> invisibleEdges,
			HighlightConditionList nodeHighlightConditions, int nodeSize,
			boolean nodesInvisibleDyDefault) {
		List<Color> colors = new ArrayList<Color>();
		Map<V, List<Double>> alphaValues = new LinkedHashMap<V, List<Double>>();
		Map<V, Double> thicknessValues = new LinkedHashMap<V, Double>();
		Map<V, String> labels = new LinkedHashMap<V, String>();
		boolean prioritize = nodeHighlightConditions.isPrioritizeColors();

		invisibleNodes.clear();

		if (nodesInvisibleDyDefault) {
			invisibleNodes.addAll(nodes);
		}

		for (V node : nodes) {
			alphaValues.put(node, new ArrayList<Double>());
			thicknessValues.put(node, 0.0);
		}

		for (HighlightCondition condition : nodeHighlightConditions
				.getConditions()) {
			Map<V, Double> values = condition.getValues(nodes);

			if (condition.isUseThickness()) {
				for (V node : nodes) {
					thicknessValues.put(node, thicknessValues.get(node)
							+ values.get(node));
				}
			}

			if (condition.isInvisible()) {
				for (V node : nodes) {
					if (values.get(node) != 0.0) {
						invisibleNodes.add(node);
					}
				}
			} else if (condition.getColor() != null) {
				colors.add(condition.getColor());

				for (V node : nodes) {
					List<Double> alphas = alphaValues.get(node);

					if (!prioritize || !containsNonZeroValue(alphas)) {
						alphas.add(values.get(node));
					} else {
						alphas.add(0.0);
					}

					if (nodesInvisibleDyDefault && values.get(node) != 0.0) {
						invisibleNodes.remove(node);
					}
				}
			}

			if (condition.getLabelProperty() != null) {
				String property = condition.getLabelProperty();

				for (V node : nodes) {
					if (values.get(node) != 0.0
							&& node.getProperties().get(property) != null) {
						String label = labels.get(node);
						String newLabel = node.getProperties().get(property)
								.toString();

						if (label != null) {
							labels.put(node, label + "/" + newLabel);
						} else {
							labels.put(node, newLabel);
						}
					}
				}
			}
		}

		Graph<V, E> graph = viewer.getGraphLayout().getGraph();
		boolean graphChanged = false;

		for (V node : nodes) {
			if (graph.containsVertex(node)) {
				if (invisibleNodes.contains(node)) {
					graph.removeVertex(node);
					graphChanged = true;
				}
			} else {
				if (!invisibleNodes.contains(node)) {
					graph.addVertex(node);
					graphChanged = true;
				}
			}
		}

		for (E edge : edges) {
			if (graph.containsEdge(edge)) {
				if (invisibleEdges.contains(edge)
						|| !graph.containsVertex(edge.getFrom())
						|| !graph.containsVertex(edge.getFrom())) {
					graph.removeEdge(edge);
					graphChanged = true;
				}
			} else {
				if (!invisibleEdges.contains(edge)
						&& graph.containsVertex(edge.getFrom())
						&& graph.containsVertex(edge.getTo())) {
					graph.addEdge(edge, edge.getFrom(), edge.getTo());
					graphChanged = true;
				}
			}
		}

		viewer.getRenderContext().setVertexShapeTransformer(
				new NodeShapeTransformer<V>(nodeSize, thicknessValues));
		viewer.getRenderContext().setVertexFillPaintTransformer(
				new NodeFillTransformer<V>(viewer, alphaValues, colors));
		viewer.getRenderContext().setVertexLabelTransformer(
				new LabelTransformer<V>(labels));
		viewer.repaint();

		return graphChanged;
	}

	public static <V extends Node, E extends Edge<V>> boolean applyEdgeHighlights(
			VisualizationViewer<V, E> viewer, Collection<E> edges,
			Collection<E> invisibleEdges,
			HighlightConditionList edgeHighlightConditions) {
		List<Color> colors = new ArrayList<Color>();
		Map<E, List<Double>> alphaValues = new LinkedHashMap<E, List<Double>>();
		Map<E, Double> thicknessValues = new LinkedHashMap<E, Double>();
		Map<E, String> labels = new LinkedHashMap<E, String>();
		boolean prioritize = edgeHighlightConditions.isPrioritizeColors();

		invisibleEdges.clear();

		for (E edge : edges) {
			alphaValues.put(edge, new ArrayList<Double>());
			thicknessValues.put(edge, 0.0);
		}

		for (HighlightCondition condition : edgeHighlightConditions
				.getConditions()) {
			Map<E, Double> values = condition.getValues(edges);

			if (condition.isInvisible()) {
				for (E edge : edges) {
					if (values.get(edge) != 0.0) {
						invisibleEdges.add(edge);
					}
				}
			} else if (condition.getColor() != null) {
				colors.add(condition.getColor());

				for (E edge : edges) {
					List<Double> alphas = alphaValues.get(edge);

					if (!prioritize || !containsNonZeroValue(alphas)) {
						alphas.add(values.get(edge));
					} else {
						alphas.add(0.0);
					}
				}
			}

			if (condition.isUseThickness()) {
				for (E edge : edges) {
					thicknessValues.put(edge, thicknessValues.get(edge)
							+ values.get(edge));
				}
			}

			if (condition.getLabelProperty() != null) {
				String property = condition.getLabelProperty();

				for (E edge : edges) {
					if (values.get(edge) != 0.0
							&& edge.getProperties().get(property) != null) {
						String label = labels.get(edge);
						String newLabel = edge.getProperties().get(property)
								.toString();

						if (label != null) {
							labels.put(edge, label + "/" + newLabel);
						} else {
							labels.put(edge, newLabel);
						}
					}
				}
			}
		}

		Graph<V, E> graph = viewer.getGraphLayout().getGraph();
		boolean graphChanged = false;

		for (E edge : edges) {
			if (graph.containsEdge(edge)) {
				if (invisibleEdges.contains(edge)) {
					graph.removeEdge(edge);
					graphChanged = true;
				}
			} else {
				if (!invisibleEdges.contains(edge)
						&& graph.containsVertex(edge.getFrom())
						&& graph.containsVertex(edge.getTo())) {
					graph.addEdge(edge, edge.getFrom(), edge.getTo());
					graphChanged = true;
				}
			}
		}

		viewer.getRenderContext().setEdgeDrawPaintTransformer(
				new EdgeDrawTransformer<E>(viewer, alphaValues, colors));
		viewer.getRenderContext().setEdgeStrokeTransformer(
				new EdgeStrokeTransformer<E>(thicknessValues));
		viewer.getRenderContext().setEdgeLabelTransformer(
				new LabelTransformer<E>(labels));
		viewer.repaint();

		return graphChanged;
	}

	public static boolean placeDialogAt(Dialog dialog, Point location) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int x = location.x;
		int y = location.y;

		if (x + dialog.getWidth() > screenSize.width) {
			x -= dialog.getWidth();
		}

		if (y + dialog.getHeight() > screenSize.height) {
			y -= dialog.getHeight();
		}

		if (x < 0 || y < 0) {
			return false;
		}

		dialog.setLocation(new Point(x, y));

		return true;
	}

	public static Paint mixColors(Color backgroundColor, List<Color> colors,
			List<Double> alphas) {
		double rb = backgroundColor.getRed() / 255.0;
		double gb = backgroundColor.getGreen() / 255.0;
		double bb = backgroundColor.getBlue() / 255.0;
		List<Color> cs = new ArrayList<Color>();

		for (int i = 0; i < colors.size(); i++) {
			double alpha = alphas.get(i);

			if (alpha > 0.0) {
				double r = colors.get(i).getRed() / 255.0 * alpha + rb
						* (1 - alpha);
				double g = colors.get(i).getGreen() / 255.0 * alpha + gb
						* (1 - alpha);
				double b = colors.get(i).getBlue() / 255.0 * alpha + bb
						* (1 - alpha);

				cs.add(new Color((float) r, (float) g, (float) b));
			}
		}

		if (cs.isEmpty()) {
			return backgroundColor;
		} else if (cs.size() == 1) {
			return cs.get(0);
		}

		BufferedImage img = new BufferedImage(cs.size() * TEXTURE_SIZE, 1,
				BufferedImage.TYPE_INT_RGB);

		for (int i = 0; i < cs.size() * TEXTURE_SIZE; i++) {
			img.setRGB(i, 0, cs.get(i / TEXTURE_SIZE).getRGB());
		}

		return new TexturePaint(img, new Rectangle(img.getWidth(),
				img.getHeight()));
	}

	public static void drawImageWithAlpha(Graphics g, BufferedImage img,
			int alpha) {
		float[] edgeScales = { 1f, 1f, 1f, alpha / 255.0f };
		float[] edgeOffsets = new float[4];

		((Graphics2D) g).drawImage(img, new RescaleOp(edgeScales, edgeOffsets,
				null), 0, 0);
	}

	private static boolean containsNonZeroValue(List<Double> list) {
		for (double value : list) {
			if (value != 0.0) {
				return true;
			}
		}

		return false;
	}
}
