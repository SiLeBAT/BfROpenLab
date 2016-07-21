/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.collections15.Transformer;
import org.knime.base.data.xml.SvgCell;
import org.knime.base.data.xml.SvgImageContent;
import org.knime.core.data.image.png.PNGImageContent;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;
import org.w3c.dom.svg.SVGDocument;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import de.bund.bfr.jung.BetterDirectedSparseMultigraph;
import de.bund.bfr.jung.BetterVisualizationViewer;
import de.bund.bfr.jung.JungUtils;
import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.Pair;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.PropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.Transform;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationImageServer;

public class CanvasUtils {

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

	public static double toPositiveDouble(Object value) {
		if (value instanceof Number) {
			double d = ((Number) value).doubleValue();

			return Double.isFinite(d) && d >= 0.0 ? d : 0.0;
		}

		return 0.0;
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
		Set<Object> categories = elements.stream().map(e -> e.getProperties().get(property)).filter(Objects::nonNull)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		List<HighlightCondition> conditions = new ArrayList<>();
		int index = 0;

		for (Object category : KnimeUtils.ORDERING.sortedCopy(categories)) {
			Color color = COLORS[index++ % COLORS.length];
			LogicalHighlightCondition condition = new LogicalHighlightCondition(property,
					LogicalHighlightCondition.Type.EQUAL, category.toString());

			conditions.add(new AndOrHighlightCondition(condition, property + " = " + category, true, color, false,
					false, null));
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

	public static <V extends Node> Map<Edge<V>, Set<Edge<V>>> joinEdges(Collection<Edge<V>> edges,
			EdgePropertySchema schema, Collection<Edge<V>> allEdges) {
		SetMultimap<Pair<V, V>, Edge<V>> edgeMap = LinkedHashMultimap.create();

		for (Edge<V> edge : edges) {
			edgeMap.put(new Pair<>(edge.getFrom(), edge.getTo()), edge);
		}

		Map<Edge<V>, Set<Edge<V>>> joined = new LinkedHashMap<>();
		Set<String> usedIds = CanvasUtils.getElementIds(allEdges);

		Multimaps.asMap(edgeMap).forEach((endpoints, edgesInBetween) -> {
			Map<String, Object> properties = new LinkedHashMap<>();

			for (Edge<V> edge : edgesInBetween) {
				CanvasUtils.addMapToMap(properties, schema, edge.getProperties());
			}

			V from = endpoints.getFirst();
			V to = endpoints.getSecond();
			String id = KnimeUtils.createNewValue(from.getId() + "->" + to.getId(), usedIds);

			usedIds.add(id);
			properties.put(schema.getId(), id);
			properties.put(schema.getFrom(), from.getId());
			properties.put(schema.getTo(), to.getId());
			joined.put(new Edge<>(id, properties, from, to), edgesInBetween);
		});

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

	public static Map<String, Object> joinPropertiesOfNodes(Collection<? extends Node> nodes, NodePropertySchema schema,
			String id, String metaNodeProperty) {
		Map<String, Object> properties = new LinkedHashMap<>();

		for (Node node : nodes) {
			addMapToMap(properties, schema, node.getProperties());
		}

		properties.put(schema.getId(), id);
		properties.put(metaNodeProperty, true);
		properties.put(schema.getLatitude(), null);
		properties.put(schema.getLongitude(), null);

		return properties;
	}

	public static <T extends Element> Set<T> getHighlightedElements(Collection<T> elements,
			List<HighlightCondition> highlightConditions) {
		Set<T> highlightedElements = new LinkedHashSet<>();

		for (HighlightCondition condition : highlightConditions) {
			condition.getValues(elements).entrySet().stream().filter(e -> e.getValue() != 0.0)
					.forEach(e -> highlightedElements.add(e.getKey()));
		}

		return highlightedElements;
	}

	public static Set<String> getElementIds(Collection<? extends Element> elements) {
		return elements.stream().map(e -> e.getId()).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public static <T extends Element> Set<T> getElementsById(Collection<T> elements, Set<String> ids) {
		return elements.stream().filter(e -> ids.contains(e.getId()))
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public static <T extends Element> Map<String, T> getElementsById(Collection<T> elements) {
		Map<String, T> result = new LinkedHashMap<>();

		elements.forEach(e -> result.put(e.getId(), e));

		return result;
	}

	public static <T> Set<T> getElementsById(Map<String, T> elements, Collection<String> ids) {
		return ids.stream().map(id -> elements.get(id)).filter(Objects::nonNull)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public static Map<String, Set<String>> getPossibleValues(Collection<? extends Element> elements) {
		SetMultimap<String, String> values = LinkedHashMultimap.create();

		for (Element e : elements) {
			e.getProperties().forEach((property, value) -> {
				if (value instanceof Boolean) {
					values.putAll(property, Arrays.asList(Boolean.FALSE.toString(), Boolean.TRUE.toString()));
				} else if (value != null) {
					values.put(property, value.toString());
				}
			});
		}

		return Multimaps.asMap(values);
	}

	public static AndOrHighlightCondition createIdHighlightCondition(Collection<String> ids, String idProperty) {
		List<List<LogicalHighlightCondition>> conditions = new ArrayList<>();

		for (String id : ids) {
			LogicalHighlightCondition c = new LogicalHighlightCondition(idProperty,
					LogicalHighlightCondition.Type.EQUAL, id);

			conditions.add(Arrays.asList(c));
		}

		return new AndOrHighlightCondition(conditions, null, false, Color.RED, false, false, null);
	}

	public static Set<String> getUsedProperties(HighlightCondition condition) {
		AndOrHighlightCondition logicalCondition = null;
		ValueHighlightCondition valueCondition = null;

		if (condition instanceof AndOrHighlightCondition) {
			logicalCondition = (AndOrHighlightCondition) condition;
		} else if (condition instanceof ValueHighlightCondition) {
			valueCondition = (ValueHighlightCondition) condition;
		} else if (condition instanceof LogicalValueHighlightCondition) {
			logicalCondition = ((LogicalValueHighlightCondition) condition).getLogicalCondition();
			valueCondition = ((LogicalValueHighlightCondition) condition).getValueCondition();
		}

		Set<String> properties = new LinkedHashSet<>();

		if (logicalCondition != null) {
			logicalCondition.getConditions().stream().flatMap(List::stream)
					.forEach(c -> properties.add(c.getProperty()));
		}

		if (valueCondition != null) {
			properties.add(valueCondition.getProperty());
		}

		return properties;
	}

	public static <V extends Node> void applyNodeHighlights(RenderContext<V, Edge<V>> renderContext,
			Collection<V> nodes, HighlightConditionList nodeHighlightConditions, int nodeSize, Integer nodeMaxSize,
			String metaNodeProperty) {
		HighlightResult<V> result = getResult(nodes, nodeHighlightConditions);
		Set<V> metaNodes = nodes.stream().filter(n -> Boolean.TRUE.equals(n.getProperties().get(metaNodeProperty)))
				.collect(Collectors.toCollection(LinkedHashSet::new));

		renderContext.setVertexShapeTransformer(
				JungUtils.newNodeShapeTransformer(nodeSize, nodeMaxSize, result.thicknessValues));
		renderContext.setVertexFillPaintTransformer(JungUtils.newNodeFillTransformer(renderContext, result.colors));
		renderContext.setVertexLabelTransformer(node -> result.labels.get(node));
		renderContext.setVertexStrokeTransformer(JungUtils.newNodeStrokeTransformer(renderContext, metaNodes));
	}

	public static <V extends Node> void applyNodeLabels(RenderContext<V, Edge<V>> renderContext, Collection<V> nodes,
			HighlightConditionList nodeHighlightConditions) {
		HighlightResult<V> result = getResult(nodes, nodeHighlightConditions);

		renderContext.setVertexLabelTransformer(node -> result.labels.get(node));
	}

	public static <V extends Node> void applyEdgeHighlights(RenderContext<V, Edge<V>> renderContext,
			Collection<Edge<V>> edges, HighlightConditionList edgeHighlightConditions, int edgeThickness,
			Integer edgeMaxThickness) {
		HighlightResult<Edge<V>> result = getResult(edges, edgeHighlightConditions);
		Pair<Transformer<Edge<V>, Stroke>, Transformer<Context<Graph<V, Edge<V>>, Edge<V>>, Shape>> strokeAndArrowTransformers = JungUtils
				.newEdgeStrokeArrowTransformers(edgeThickness, edgeMaxThickness, result.thicknessValues);

		renderContext.setEdgeFillPaintTransformer(JungUtils.newEdgeFillTransformer(renderContext, result.colors));
		renderContext.setEdgeStrokeTransformer(strokeAndArrowTransformers.getFirst());
		renderContext.setEdgeArrowTransformer(strokeAndArrowTransformers.getSecond());
		renderContext.setEdgeLabelTransformer(edge -> result.labels.get(edge));
	}

	public static Paint mixColors(Color backgroundColor, List<Color> colors, List<Double> alphas,
			boolean checkedInsteadOfStriped) {
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
		int size = cs.size() * (checkedInsteadOfStriped ? EDGE_TEXTURE_SIZE : NODE_TEXTURE_SIZE);

		if (checkedInsteadOfStriped) {
			img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

			for (int x = 0; x < size; x++) {
				for (int y = 0; y < size; y++) {
					img.setRGB(x, y, cs.get((x / EDGE_TEXTURE_SIZE + y / EDGE_TEXTURE_SIZE) % cs.size()).getRGB());
				}
			}
		} else {
			img = new BufferedImage(size, 1, BufferedImage.TYPE_INT_ARGB);

			for (int x = 0; x < size; x++) {
				img.setRGB(x, 0, cs.get(x / NODE_TEXTURE_SIZE).getRGB());
			}
		}

		return new TexturePaint(img, new Rectangle(img.getWidth(), img.getHeight()));
	}

	public static void drawImageWithAlpha(Graphics2D g, BufferedImage img, int alpha) {
		float[] edgeScales = { 1f, 1f, 1f, alpha / 255.0f };
		float[] edgeOffsets = new float[4];

		g.drawImage(img, new RescaleOp(edgeScales, edgeOffsets, null), 0, 0);
	}

	public static <T extends Element> Set<T> removeInvisibleElements(Set<T> elements,
			HighlightConditionList highlightConditions) {
		Set<T> removed = new LinkedHashSet<>();

		highlightConditions.getConditions().stream().filter(c -> c.isInvisible()).forEach(c -> {
			Set<T> toRemove = c.getValues(elements).entrySet().stream().filter(e -> e.getValue() != 0.0)
					.map(e -> e.getKey()).collect(Collectors.toCollection(LinkedHashSet::new));

			elements.removeAll(toRemove);
			removed.addAll(toRemove);
		});

		return removed;
	}

	public static <V extends Node> Set<Edge<V>> removeNodelessEdges(Set<Edge<V>> edges, Set<V> nodes) {
		Set<Edge<V>> removed = edges.stream().filter(e -> !nodes.contains(e.getFrom()) || !nodes.contains(e.getTo()))
				.collect(Collectors.toCollection(LinkedHashSet::new));

		edges.removeAll(removed);

		return removed;
	}

	public static <V extends Node> Set<V> removeEdgelessNodes(Set<V> nodes, Set<Edge<V>> edges) {
		Set<V> nodesWithEdges = new LinkedHashSet<>();

		for (Edge<V> edge : edges) {
			nodesWithEdges.add(edge.getFrom());
			nodesWithEdges.add(edge.getTo());
		}

		Set<V> removed = new LinkedHashSet<>(Sets.difference(nodes, nodesWithEdges));

		nodes.removeAll(removed);

		return removed;
	}

	public static <V extends Node> Graph<V, Edge<V>> createGraph(BetterVisualizationViewer<V, Edge<V>> owner,
			Collection<V> nodes, Collection<Edge<V>> edges) {
		Graph<V, Edge<V>> graph = new BetterDirectedSparseMultigraph<>(owner);

		nodes.forEach(n -> graph.addVertex(n));
		edges.forEach(e -> graph.addEdge(e, e.getFrom(), e.getTo()));

		return graph;
	}

	public static BufferedImage getBufferedImage(ICanvas<?>... canvas) {
		int width = Math.max(Stream.of(canvas).mapToInt(c -> c.getCanvasSize().width).sum(), 1);
		int height = Stream.of(canvas).mapToInt(c -> c.getCanvasSize().height).max().orElse(1);
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
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
		int width = Math.max(Stream.of(canvas).mapToInt(c -> c.getCanvasSize().width).sum(), 1);
		int height = Stream.of(canvas).mapToInt(c -> c.getCanvasSize().height).max().orElse(1);
		SVGDocument document = (SVGDocument) new SVGDOMImplementation().createDocument(null, "svg", null);
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

		return document;
	}

	public static ImagePortObject getImage(boolean asSvg, ICanvas<?>... canvas) throws IOException {
		if (asSvg) {
			return new ImagePortObject(new SvgImageContent(CanvasUtils.getSvgDocument(canvas)),
					new ImagePortObjectSpec(SvgCell.TYPE));
		} else {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				ImageIO.write(CanvasUtils.getBufferedImage(canvas), "png", out);

				return new ImagePortObject(new PNGImageContent(out.toByteArray()),
						new ImagePortObjectSpec(PNGImageContent.TYPE));
			}
		}
	}

	public static ImagePortObjectSpec getImageSpec(boolean asSvg) {
		return new ImagePortObjectSpec(asSvg ? SvgCell.TYPE : PNGImageContent.TYPE);
	}

	public static String toString(Object obj) {
		if (obj instanceof Number) {
			return NumberFormat.getInstance(Locale.US).format(obj);
		}

		return obj != null ? obj.toString() : null;
	}

	private static <E extends Element> HighlightResult<E> getResult(Collection<E> elements,
			HighlightConditionList highlightConditions) {
		List<Color> colorList = new ArrayList<>();
		ListMultimap<E, Double> alphaValues = ArrayListMultimap.create();
		Map<E, Double> thicknessValues = new LinkedHashMap<>();
		SetMultimap<E, String> labelLists = LinkedHashMultimap.create();

		elements.forEach(e -> thicknessValues.put(e, 0.0));

		for (HighlightCondition condition : highlightConditions.getConditions()) {
			if (condition.isInvisible()) {
				continue;
			}

			Map<E, Double> values = condition.getValues(elements);

			if (condition.getColor() != null) {
				colorList.add(condition.getColor());

				for (E e : elements) {
					List<Double> alphas = alphaValues.get(e);

					if (!highlightConditions.isPrioritizeColors() || alphas.isEmpty()
							|| Collections.max(alphas) == 0.0) {
						alphas.add(values.get(e));
					} else {
						alphas.add(0.0);
					}
				}
			}

			if (condition.isUseThickness()) {
				elements.forEach(e -> thicknessValues.put(e, thicknessValues.get(e) + values.get(e)));
			}

			if (condition.getLabelProperty() != null) {
				String property = condition.getLabelProperty();

				for (E e : elements) {
					if (values.get(e) != 0.0 && e.getProperties().get(property) != null) {
						labelLists.put(e, toString(e.getProperties().get(property)));
					}
				}
			}
		}

		Map<E, Paint> colors = new LinkedHashMap<>();
		Map<E, String> labels = new LinkedHashMap<>();

		Multimaps.asMap(alphaValues).forEach((e, alphas) -> colors.put(e, CanvasUtils
				.mixColors(e instanceof Edge ? Color.BLACK : Color.WHITE, colorList, alphas, e instanceof Edge)));
		Multimaps.asMap(labelLists).forEach((e, labelList) -> labels.put(e, Joiner.on("/").join(labelList)));

		HighlightResult<E> result = new HighlightResult<>();

		result.colors = colors;
		result.thicknessValues = thicknessValues;
		result.labels = labels;

		return result;
	}

	private static class HighlightResult<E extends Element> {

		private Map<E, Paint> colors;
		private Map<E, Double> thicknessValues;
		private Map<E, String> labels;
	}
}
