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
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.knime.base.data.xml.SvgCell;
import org.knime.base.data.xml.SvgImageContent;
import org.knime.core.data.image.png.PNGImageContent;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;
import org.knime.core.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.math.DoubleMath;
import com.google.common.primitives.Doubles;

import de.bund.bfr.knime.gis.views.canvas.dialogs.ListFilterDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.transformer.EdgeArrowTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.EdgeDrawTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.EdgeStrokeTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.LabelTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeFillTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationImageServer;

public class CanvasUtils {

	public static final Color LEGEND_BACKGROUND = new Color(230, 230, 230);

	private static final int NODE_TEXTURE_SIZE = 3;
	private static final int EDGE_TEXTURE_SIZE = 5;
	private static final Color[] COLORS = new Color[] { new Color(255, 85, 85), new Color(85, 85, 255),
			new Color(85, 255, 85), new Color(255, 85, 255), new Color(85, 255, 255), new Color(255, 175, 175),
			new Color(128, 128, 128), new Color(192, 0, 0), new Color(0, 0, 192), new Color(0, 192, 0),
			new Color(192, 192, 0), new Color(192, 0, 192), new Color(0, 192, 192), new Color(64, 64, 64),
			new Color(255, 64, 64), new Color(64, 64, 255), new Color(64, 255, 64), new Color(255, 64, 255),
			new Color(64, 255, 255), new Color(192, 192, 192), new Color(128, 0, 0), new Color(0, 0, 128),
			new Color(0, 128, 0), new Color(128, 128, 0), new Color(128, 0, 128), new Color(0, 128, 128),
			new Color(255, 128, 128), new Color(128, 128, 255), new Color(128, 255, 128), new Color(255, 128, 255),
			new Color(128, 255, 255) };

	private CanvasUtils() {
	}

	public static Rectangle2D getBounds(Collection<Point2D> positions) {
		Rectangle2D bounds = null;

		for (Point2D p : positions) {
			if (bounds == null) {
				bounds = new Rectangle2D.Double(p.getX(), p.getY(), 0, 0);
			} else {
				bounds.add(p);
			}
		}

		return bounds;
	}

	public static Transform getTransformForBounds(Dimension canvasSize, Rectangle2D bounds, Double zoomStep) {
		double widthRatio = canvasSize.width / bounds.getWidth();
		double heightRatio = canvasSize.height / bounds.getHeight();
		double canvasCenterX = canvasSize.width / 2.0;
		double canvasCenterY = canvasSize.height / 2.0;
		double centerX = bounds.getCenterX();
		double centerY = bounds.getCenterY();

		double scale = Math.min(widthRatio, heightRatio);

		if (zoomStep != null) {
			int zoom = (int) (Math.log(scale) / Math.log(2.0));

			scale = Math.pow(2.0, zoom);
		}

		double scaleX = scale;
		double scaleY = scale;
		double translationX = canvasCenterX - centerX * scaleX;
		double translationY = canvasCenterY - centerY * scaleY;

		return new Transform(scaleX, scaleY, translationX, translationY);
	}

	public static List<HighlightCondition> createCategorialHighlighting(Collection<? extends Element> elements,
			String property) {
		Set<Object> categories = new LinkedHashSet<>();

		for (Element element : elements) {
			Object value = element.getProperties().get(property);

			if (value != null) {
				categories.add(value);
			}
		}

		List<Object> categoryList = new ArrayList<>(categories);

		sortObjectList(categoryList);

		List<HighlightCondition> conditions = new ArrayList<>();
		int index = 0;

		for (Object category : categoryList) {
			Color color = COLORS[index % COLORS.length];
			LogicalHighlightCondition condition = new LogicalHighlightCondition(property,
					LogicalHighlightCondition.EQUAL_TYPE, category.toString());

			conditions.add(new AndOrHighlightCondition(condition, property + " = " + category, true, color, false,
					false, null));
			index++;
		}

		return conditions;
	}

	@SuppressWarnings("unchecked")
	public static <V extends Node> void copyNodesAndEdges(Collection<V> nodes, Collection<Edge<V>> edges,
			Collection<V> newNodes, Collection<Edge<V>> newEdges) {
		Map<String, V> nodesById = new LinkedHashMap<>();

		for (V node : nodes) {
			V newNode = (V) node.copy();

			nodesById.put(node.getId(), newNode);
			newNodes.add(newNode);
		}

		for (Edge<V> edge : edges) {
			newEdges.add(new Edge<>(edge.getId(), new LinkedHashMap<>(edge.getProperties()),
					nodesById.get(edge.getFrom().getId()), nodesById.get(edge.getTo().getId())));
		}
	}

	public static String openNewIdDialog(Component parent, Set<String> usedIds, String nodeName) {
		String newId = null;

		while (true) {
			newId = (String) JOptionPane.showInputDialog(parent, "Specify ID for Meta " + nodeName, nodeName + " ID",
					JOptionPane.QUESTION_MESSAGE, null, null, "");

			if (newId == null || !usedIds.contains(newId)) {
				break;
			}

			JOptionPane.showMessageDialog(parent, "ID already exists, please specify different ID", "Error",
					JOptionPane.ERROR_MESSAGE);
		}

		return newId;
	}

	public static <V extends Node> Map<Object, Set<V>> openCollapseByPropertyDialog(Component parent,
			Collection<String> nodeProperties, Collection<String> uncollapsedIds, Map<String, V> nodes) {
		String[] properties = nodeProperties.toArray(new String[0]);
		String result = (String) JOptionPane.showInputDialog(parent, "Select Property for Collapse?",
				"Collapse by Property", JOptionPane.QUESTION_MESSAGE, null, properties, properties[0]);

		if (result == null) {
			return new LinkedHashMap<>();
		}

		SetMultimap<Object, V> nodesByProperty = LinkedHashMultimap.create();

		for (String id : uncollapsedIds) {
			V node = nodes.get(id);
			Object value = node.getProperties().get(result);

			if (value != null) {
				nodesByProperty.put(value, node);
			}
		}

		List<Object> propertyList = new ArrayList<>(nodesByProperty.keySet());

		sortObjectList(propertyList);

		ListFilterDialog<Object> dialog = new ListFilterDialog<>(parent, propertyList);

		dialog.setVisible(true);

		if (!dialog.isApproved()) {
			return new LinkedHashMap<>();
		}

		nodesByProperty.keySet().retainAll(dialog.getFiltered());

		return Multimaps.asMap(nodesByProperty);
	}

	public static Point2D addPoints(Point2D p1, Point2D p2) {
		return new Point2D.Double(p1.getX() + p2.getX(), p1.getY() + p2.getY());
	}

	public static Point2D substractPoints(Point2D p1, Point2D p2) {
		return new Point2D.Double(p1.getX() - p2.getX(), p1.getY() - p2.getY());
	}

	public static Point2D getCenter(Collection<Point2D> points) {
		if (points.isEmpty()) {
			return null;
		}

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

	public static <V extends Node> Map<Edge<V>, Set<Edge<V>>> joinEdges(Collection<Edge<V>> edges,
			EdgePropertySchema properties, Set<String> usedIds) {
		SetMultimap<Pair<V, V>, Edge<V>> edgeMap = LinkedHashMultimap.create();

		for (Edge<V> edge : edges) {
			edgeMap.put(new Pair<>(edge.getFrom(), edge.getTo()), edge);
		}

		Map<Edge<V>, Set<Edge<V>>> joined = new LinkedHashMap<>();
		int index = 0;

		for (Map.Entry<Pair<V, V>, Set<Edge<V>>> entry : Multimaps.asMap(edgeMap).entrySet()) {
			V from = entry.getKey().getFirst();
			V to = entry.getKey().getSecond();
			Map<String, Object> prop = new LinkedHashMap<>();

			for (Edge<V> edge : entry.getValue()) {
				CanvasUtils.addMapToMap(prop, properties, edge.getProperties());
			}

			while (!usedIds.add(index + "")) {
				index++;
			}

			prop.put(properties.getId(), index + "");
			prop.put(properties.getFrom(), from.getId());
			prop.put(properties.getTo(), to.getId());
			joined.put(new Edge<>(index + "", prop, from, to), entry.getValue());
		}

		return joined;
	}

	public static void addMapToMap(Map<String, Object> map, PropertySchema schema, Map<String, Object> addMap) {
		for (String property : schema.getMap().keySet()) {
			addObjectToMap(map, property, schema.getMap().get(property), addMap.get(property));
		}
	}

	public static void addObjectToMap(Map<String, Object> map, String property, Class<?> type, Object obj) {
		if (type == String.class) {
			String value = (String) obj;

			if (map.containsKey(property)) {
				if (map.get(property) == null || !map.get(property).equals(value)) {
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
				if (map.get(property) == null || !map.get(property).equals(value)) {
					map.put(property, null);
				}
			} else {
				map.put(property, value);
			}
		}
	}

	public static <T extends Element> Set<T> getHighlightedElements(Collection<T> elements,
			List<HighlightCondition> highlightConditions) {
		Set<T> highlightedElements = new LinkedHashSet<>();

		for (HighlightCondition condition : highlightConditions) {
			for (Map.Entry<T, Double> entry : condition.getValues(elements).entrySet()) {
				if (entry.getValue() > 0.0) {
					highlightedElements.add(entry.getKey());
				}
			}
		}

		return highlightedElements;
	}

	public static Set<String> getElementIds(Collection<? extends Element> elements) {
		Set<String> ids = new LinkedHashSet<>();

		for (Element element : elements) {
			ids.add(element.getId());
		}

		return ids;
	}

	public static <T extends Element> Set<T> getElementsById(Collection<T> elements, Collection<String> ids) {
		Set<T> result = new LinkedHashSet<>();

		for (T element : elements) {
			if (ids.contains(element.getId())) {
				result.add(element);
			}
		}

		return result;
	}

	public static <T extends Element> Map<String, T> getElementsById(Collection<T> elements) {
		Map<String, T> result = new LinkedHashMap<>();

		for (T element : elements) {
			result.put(element.getId(), element);
		}

		return result;
	}

	public static <T> Set<T> getElementsById(Map<String, T> elements, Collection<String> ids) {
		Set<T> result = new LinkedHashSet<>();

		for (String id : ids) {
			if (elements.containsKey(id)) {
				result.add(elements.get(id));
			}
		}

		return result;
	}

	public static Map<String, Set<String>> getPossibleValues(Collection<? extends Element> elements) {
		SetMultimap<String, String> values = LinkedHashMultimap.create();

		for (Element e : elements) {
			for (Map.Entry<String, Object> entry : e.getProperties().entrySet()) {
				if (entry.getValue() instanceof Boolean) {
					values.putAll(entry.getKey(), Arrays.asList(Boolean.FALSE.toString(), Boolean.TRUE.toString()));
				} else if (entry.getValue() != null) {
					values.put(entry.getKey(), entry.getValue().toString());
				}
			}
		}

		return Multimaps.asMap(values);
	}

	public static Double getMeanValue(Collection<? extends Element> elements, String property) {
		List<Double> values = new ArrayList<>();

		for (Element element : elements) {
			Object o = element.getProperties().get(property);

			if (o instanceof Double) {
				values.add((Double) o);
			}
		}

		if (values.isEmpty()) {
			return null;
		}

		return DoubleMath.mean(Doubles.toArray(values));
	}

	public static <V extends Node> void applyNodeHighlights(RenderContext<V, Edge<V>> renderContext,
			Collection<V> nodes, HighlightConditionList nodeHighlightConditions, int nodeSize, Integer nodeMaxSize) {
		applyNodeHighlights(renderContext, nodes, nodeHighlightConditions, nodeSize, nodeMaxSize, false);
	}

	public static <V extends Node> void applyNodeLabels(RenderContext<V, Edge<V>> renderContext, Collection<V> nodes,
			HighlightConditionList nodeHighlightConditions) {
		applyNodeHighlights(renderContext, nodes, nodeHighlightConditions, 0, null, true);
	}

	public static <V extends Node> void applyEdgeHighlights(RenderContext<V, Edge<V>> renderContext,
			Collection<Edge<V>> edges, HighlightConditionList edgeHighlightConditions, int edgeThickness,
			Integer edgeMaxThickness) {
		List<Color> colors = new ArrayList<>();
		ListMultimap<Edge<V>, Double> alphaValues = ArrayListMultimap.create();
		Map<Edge<V>, Double> thicknessValues = new LinkedHashMap<>();
		SetMultimap<Edge<V>, String> labelLists = LinkedHashMultimap.create();
		boolean prioritize = edgeHighlightConditions.isPrioritizeColors();

		for (Edge<V> edge : edges) {
			thicknessValues.put(edge, 0.0);
		}

		for (HighlightCondition condition : edgeHighlightConditions.getConditions()) {
			if (condition.isInvisible()) {
				continue;
			}

			Map<Edge<V>, Double> values = condition.getValues(edges);

			if (condition.getColor() != null) {
				colors.add(condition.getColor());

				for (Edge<V> edge : edges) {
					List<Double> alphas = alphaValues.get(edge);

					if (!prioritize || alphas.isEmpty() || Collections.max(alphas) == 0.0) {
						alphas.add(values.get(edge));
					} else {
						alphas.add(0.0);
					}
				}
			}

			if (condition.isUseThickness()) {
				for (Edge<V> edge : edges) {
					thicknessValues.put(edge, thicknessValues.get(edge) + values.get(edge));
				}
			}

			if (condition.getLabelProperty() != null) {
				String property = condition.getLabelProperty();

				for (Edge<V> edge : edges) {
					if (values.get(edge) != 0.0 && edge.getProperties().get(property) != null) {
						labelLists.put(edge, edge.getProperties().get(property).toString());
					}
				}
			}
		}

		EdgeStrokeTransformer<Edge<V>> strokeTransformer = new EdgeStrokeTransformer<>(edgeThickness, edgeMaxThickness,
				thicknessValues);
		Map<Edge<V>, String> labels = new LinkedHashMap<>();

		for (Map.Entry<Edge<V>, Collection<String>> entry : labelLists.asMap().entrySet()) {
			labels.put(entry.getKey(), Joiner.on("/").join(entry.getValue()));
		}

		renderContext.setEdgeDrawPaintTransformer(
				new EdgeDrawTransformer<>(renderContext, Multimaps.asMap(alphaValues), colors));
		renderContext.setEdgeStrokeTransformer(strokeTransformer);
		renderContext.setEdgeArrowTransformer(new EdgeArrowTransformer<>(strokeTransformer));
		renderContext.setEdgeLabelTransformer(new LabelTransformer<>(labels));
	}

	public static Paint mixColors(Color backgroundColor, List<Color> colors, List<Double> alphas, boolean forEdges) {
		double rb = backgroundColor.getRed() / 255.0;
		double gb = backgroundColor.getGreen() / 255.0;
		double bb = backgroundColor.getBlue() / 255.0;
		double ab = backgroundColor.getAlpha() / 255.0;
		List<Color> cs = new ArrayList<>();

		for (int i = 0; i < colors.size(); i++) {
			double alpha = alphas.get(i);

			if (alpha > 0.0) {
				double r = colors.get(i).getRed() / 255.0 * alpha + rb * (1 - alpha);
				double g = colors.get(i).getGreen() / 255.0 * alpha + gb * (1 - alpha);
				double b = colors.get(i).getBlue() / 255.0 * alpha + bb * (1 - alpha);
				double a = colors.get(i).getAlpha() / 255.0 * alpha + ab * (1 - alpha);

				cs.add(new Color((float) r, (float) g, (float) b, (float) a));
			}
		}

		if (cs.isEmpty()) {
			return backgroundColor;
		} else if (cs.size() == 1) {
			return cs.get(0);
		}

		BufferedImage img;
		int size = cs.size() * (forEdges ? EDGE_TEXTURE_SIZE : NODE_TEXTURE_SIZE);

		if (forEdges) {
			img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					img.setRGB(i, j, cs.get((i / EDGE_TEXTURE_SIZE + j / EDGE_TEXTURE_SIZE) % cs.size()).getRGB());
				}
			}
		} else {
			img = new BufferedImage(size, 1, BufferedImage.TYPE_INT_ARGB);

			for (int i = 0; i < size; i++) {
				img.setRGB(i, 0, cs.get(i / NODE_TEXTURE_SIZE).getRGB());
			}
		}

		return new TexturePaint(img, new Rectangle(img.getWidth(), img.getHeight()));
	}

	public static void drawImageWithAlpha(Graphics g, BufferedImage img, int alpha) {
		float[] edgeScales = { 1f, 1f, 1f, alpha / 255.0f };
		float[] edgeOffsets = new float[4];

		((Graphics2D) g).drawImage(img, new RescaleOp(edgeScales, edgeOffsets, null), 0, 0);
	}

	public static <T extends Element> Set<T> removeInvisibleElements(Set<T> elements,
			HighlightConditionList highlightConditions) {
		Set<T> removed = new LinkedHashSet<>();

		for (HighlightCondition condition : highlightConditions.getConditions()) {
			if (!condition.isInvisible()) {
				continue;
			}

			Map<T, Double> values = condition.getValues(elements);

			for (Iterator<T> iterator = elements.iterator(); iterator.hasNext();) {
				T element = iterator.next();

				if (values.get(element) != 0.0) {
					removed.add(element);
					iterator.remove();
				}
			}
		}

		return removed;
	}

	public static <V extends Node> Set<Edge<V>> removeNodelessEdges(Set<Edge<V>> edges, Set<V> nodes) {
		Set<Edge<V>> removed = new LinkedHashSet<>();

		for (Iterator<Edge<V>> iterator = edges.iterator(); iterator.hasNext();) {
			Edge<V> edge = iterator.next();

			if (!nodes.contains(edge.getFrom()) || !nodes.contains(edge.getTo())) {
				removed.add(edge);
				iterator.remove();
			}
		}

		return removed;
	}

	public static <V extends Node> Set<V> removeEdgelessNodes(Set<V> nodes, Set<Edge<V>> edges) {
		Set<V> nodesWithEdges = new LinkedHashSet<>();

		for (Edge<V> edge : edges) {
			nodesWithEdges.add(edge.getFrom());
			nodesWithEdges.add(edge.getTo());
		}

		Set<V> removed = new LinkedHashSet<>();

		for (Iterator<V> iterator = nodes.iterator(); iterator.hasNext();) {
			V node = iterator.next();

			if (!nodesWithEdges.contains(node)) {
				removed.add(node);
				iterator.remove();
			}
		}

		return removed;
	}

	public static <V extends Node> Graph<V, Edge<V>> createGraph(Collection<V> nodes, Collection<Edge<V>> edges) {
		Graph<V, Edge<V>> graph = new DirectedSparseMultigraph<>();

		for (V node : nodes) {
			graph.addVertex(node);
		}

		for (Edge<V> edge : edges) {
			graph.addEdge(edge, edge.getFrom(), edge.getTo());
		}

		return graph;
	}

	public static BufferedImage getBufferedImage(ICanvas<?>... canvas) {
		int width = 0;
		int height = 0;

		for (ICanvas<?> c : canvas) {
			width += c.getCanvasSize().width;
			height = Math.max(height, c.getCanvasSize().height);
		}

		width = Math.max(width, 1);
		height = Math.max(height, 1);

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		int x = 0;

		for (ICanvas<?> c : canvas) {
			VisualizationImageServer<?, ?> server = c.getVisualizationServer(false);

			g.translate(x, 0);
			server.paint(g);
			x += c.getCanvasSize().width;
		}

		return img;
	}

	public static SVGDocument getSvgDocument(ICanvas<?>... canvas) {
		int width = 0;
		int height = 0;

		for (ICanvas<?> c : canvas) {
			width += c.getCanvasSize().width;
			height = Math.max(height, c.getCanvasSize().height);
		}

		width = Math.max(width, 1);
		height = Math.max(height, 1);

		SVGDOMImplementation domImpl = new SVGDOMImplementation();
		Document document = domImpl.createDocument(null, "svg", null);
		SVGGraphics2D g = new SVGGraphics2D(document);
		int x = 0;

		g.setSVGCanvasSize(new Dimension(width, height));

		for (ICanvas<?> c : canvas) {
			VisualizationImageServer<?, ?> server = c.getVisualizationServer(true);

			g.translate(x, 0);
			server.paint(g);
			x += c.getCanvasSize().width;
		}

		g.dispose();
		document.replaceChild(g.getRoot(), document.getDocumentElement());

		return (SVGDocument) document;
	}

	public static ImagePortObject getImage(boolean asSvg, ICanvas<?>... canvas) throws IOException {
		if (asSvg) {
			return new ImagePortObject(new SvgImageContent(CanvasUtils.getSvgDocument(canvas), true),
					new ImagePortObjectSpec(SvgCell.TYPE));
		} else {
			BufferedImage img = CanvasUtils.getBufferedImage(canvas);
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

	private static <V extends Node> void applyNodeHighlights(RenderContext<V, Edge<V>> renderContext,
			Collection<V> nodes, HighlightConditionList nodeHighlightConditions, int nodeSize, Integer nodeMaxSize,
			boolean labelsOnly) {
		List<Color> colors = new ArrayList<>();
		ListMultimap<V, Double> alphaValues = ArrayListMultimap.create();
		Map<V, Double> thicknessValues = new LinkedHashMap<>();
		SetMultimap<V, String> labelLists = LinkedHashMultimap.create();
		boolean prioritize = nodeHighlightConditions.isPrioritizeColors();

		if (!labelsOnly) {
			for (V node : nodes) {
				thicknessValues.put(node, 0.0);
			}
		}

		for (HighlightCondition condition : nodeHighlightConditions.getConditions()) {
			if (condition.isInvisible()) {
				continue;
			}

			Map<V, Double> values = condition.getValues(nodes);

			if (!labelsOnly && condition.isUseThickness()) {
				for (V node : nodes) {
					thicknessValues.put(node, thicknessValues.get(node) + values.get(node));
				}
			}

			if (!labelsOnly && condition.getColor() != null) {
				colors.add(condition.getColor());

				for (V node : nodes) {
					List<Double> alphas = alphaValues.get(node);

					if (!prioritize || alphas.isEmpty() || Collections.max(alphas) == 0.0) {
						alphas.add(values.get(node));
					} else {
						alphas.add(0.0);
					}
				}
			}

			if (condition.getLabelProperty() != null) {
				String property = condition.getLabelProperty();

				for (V node : nodes) {
					if (values.get(node) != 0.0 && node.getProperties().get(property) != null) {
						labelLists.put(node, node.getProperties().get(property).toString());
					}
				}
			}
		}

		Map<V, String> labels = new LinkedHashMap<>();

		for (Map.Entry<V, Collection<String>> entry : labelLists.asMap().entrySet()) {
			labels.put(entry.getKey(), Joiner.on("/").join(entry.getValue()));
		}

		if (!labelsOnly) {
			renderContext.setVertexShapeTransformer(new NodeShapeTransformer<>(nodeSize, nodeMaxSize, thicknessValues));
			renderContext.setVertexFillPaintTransformer(
					new NodeFillTransformer<>(renderContext, Multimaps.asMap(alphaValues), colors));
		}

		renderContext.setVertexLabelTransformer(new LabelTransformer<>(labels));
	}

	private static void sortObjectList(List<Object> list) {
		Collections.sort(list, new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) {
				if (o1 instanceof String && o2 instanceof String) {
					return ((String) o1).compareTo((String) o2);
				} else if (o1 instanceof Integer && o2 instanceof Integer) {
					return ((Integer) o1).compareTo((Integer) o2);
				} else if (o1 instanceof Double && o2 instanceof Double) {
					return ((Double) o1).compareTo((Double) o2);
				} else if (o1 instanceof Boolean && o2 instanceof Boolean) {
					return ((Boolean) o1).compareTo((Boolean) o2);
				}

				return o1.toString().compareTo(o2.toString());
			}
		});
	}
}
