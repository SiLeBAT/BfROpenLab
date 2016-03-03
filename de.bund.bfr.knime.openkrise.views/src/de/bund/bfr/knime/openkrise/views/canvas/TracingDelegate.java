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
package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

import de.bund.bfr.knime.Pair;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.GisCanvas;
import de.bund.bfr.knime.gis.views.canvas.ICanvas;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightConditionChecker;
import de.bund.bfr.knime.gis.views.canvas.dialogs.PropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SinglePropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.jung.BetterPickingGraphMousePlugin;
import de.bund.bfr.knime.openkrise.Tracing;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.common.Delivery;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class TracingDelegate<V extends Node> {

	private static boolean DEFAULT_PERFORM_TRACING = true;
	private static boolean DEFAULT_ENFORCE_TEMPORAL_ORDER = false;
	private static boolean DEFAULT_SHOW_FORWARD = false;

	private ITracingCanvas<V> canvas;
	private Map<String, V> nodeSaveMap;
	private Map<String, Edge<V>> edgeSaveMap;
	private Map<Edge<V>, Set<Edge<V>>> joinMap;
	private Map<String, Delivery> deliveries;

	private List<TracingListener> listeners;
	private boolean performTracing;

	private JCheckBox enforceTemporalOrderBox;
	private JCheckBox showForwardBox;
	private DateSlider dateSlider;

	public TracingDelegate(ITracingCanvas<V> canvas, Map<String, V> nodeSaveMap, Map<String, Edge<V>> edgeSaveMap,
			Map<Edge<V>, Set<Edge<V>>> joinMap, Map<String, Delivery> deliveries) {
		this.canvas = canvas;
		this.nodeSaveMap = nodeSaveMap;
		this.edgeSaveMap = edgeSaveMap;
		this.joinMap = joinMap;
		this.deliveries = deliveries;

		listeners = new ArrayList<>();
		performTracing = DEFAULT_PERFORM_TRACING;

		enforceTemporalOrderBox = new JCheckBox("Activate");
		enforceTemporalOrderBox.setSelected(DEFAULT_ENFORCE_TEMPORAL_ORDER);
		enforceTemporalOrderBox.addItemListener(e -> {
			if (performTracing) {
				canvas.applyChanges();
			}

			listeners.forEach(l -> l.enforceTemporalOrderChanged(canvas));
		});

		showForwardBox = new JCheckBox("Activate");
		showForwardBox.setSelected(DEFAULT_SHOW_FORWARD);
		showForwardBox.addItemListener(e -> {
			if (performTracing) {
				canvas.applyChanges();
			}

			listeners.forEach(l -> l.showForwardChanged(canvas));
		});

		Pair<GregorianCalendar, GregorianCalendar> dateRange = getDateRange(deliveries.values());

		if (dateRange != null) {
			GregorianCalendar from = dateRange.getFirst();
			GregorianCalendar to = dateRange.getSecond();

			from.add(Calendar.DAY_OF_MONTH, -1);

			dateSlider = new DateSlider(from, to);
			dateSlider.addDateListener(e -> {
				applyChanges();
				listeners.forEach(l -> l.dateSettingsChanged(canvas));
			});
			canvas.getComponent().add(dateSlider, BorderLayout.NORTH);
		}

		JMenuItem defaultHighlightItem = new JMenuItem("Set default Highlighting");

		defaultHighlightItem
				.addActionListener(e -> canvas.setHighlightConditions(DefaultHighlighting.createNodeHighlighting(),
						DefaultHighlighting.createEdgeHighlighting()));

		canvas.getOptionsPanel().addOption("Enforce Temporal Order",
				"If checked, the " + canvas.getNaming().edge()
						+ " date is used for cross contamination.\nThat means a delivery can only contaminate "
						+ canvas.getNaming().edges() + " with a later date.",
				true, enforceTemporalOrderBox);
		canvas.getOptionsPanel().addOption("Show Cross Contaminated " + canvas.getNaming().Edges(),
				"If checked, all invisible " + canvas.getNaming().edges() + ", that can be reached from the visible "
						+ canvas.getNaming().edges()
						+ "\nvia the cross contamination specified by the user, are made visible.",
				true, showForwardBox);
		canvas.getPopupMenu().add(new JSeparator());
		canvas.getPopupMenu().add(defaultHighlightItem);
		canvas.getViewer().addPostRenderPaintable(new PostPaintable(canvas));
	}

	public void addTracingListener(TracingListener listener) {
		listeners.add(listener);
	}

	public void removeTracingListener(TracingListener listener) {
		listeners.remove(listener);
	}

	public List<TracingListener> getTracingListeners() {
		return listeners;
	}

	public Map<String, Double> getNodeWeights() {
		return getPropertyValues(nodeSaveMap.values(), TracingColumns.WEIGHT);
	}

	public void setNodeWeights(Map<String, Double> nodeWeights) {
		setDoublePropertyValues(nodeSaveMap.values(), TracingColumns.WEIGHT, nodeWeights);

		if (performTracing) {
			canvas.applyChanges();
		}

		listeners.forEach(l -> l.nodeWeightsChanged(canvas));
	}

	public Map<String, Double> getEdgeWeights() {
		return getPropertyValues(edgeSaveMap.values(), TracingColumns.WEIGHT);
	}

	public void setEdgeWeights(Map<String, Double> edgeWeights) {
		setDoublePropertyValues(edgeSaveMap.values(), TracingColumns.WEIGHT, edgeWeights);

		if (performTracing) {
			canvas.applyChanges();
		}

		listeners.forEach(l -> l.edgeWeightsChanged(canvas));
	}

	public Map<String, Boolean> getNodeCrossContaminations() {
		return getPropertyValues(nodeSaveMap.values(), TracingColumns.CROSS_CONTAMINATION);
	}

	public void setNodeCrossContaminations(Map<String, Boolean> nodeCrossContaminations) {
		setBooleanPropertyValues(nodeSaveMap.values(), TracingColumns.CROSS_CONTAMINATION, nodeCrossContaminations);

		if (performTracing) {
			canvas.applyChanges();
		}

		listeners.forEach(l -> l.nodeCrossContaminationsChanged(canvas));
	}

	public Map<String, Boolean> getEdgeCrossContaminations() {
		return getPropertyValues(edgeSaveMap.values(), TracingColumns.CROSS_CONTAMINATION);
	}

	public void setEdgeCrossContaminations(Map<String, Boolean> edgeCrossContaminations) {
		setBooleanPropertyValues(edgeSaveMap.values(), TracingColumns.CROSS_CONTAMINATION, edgeCrossContaminations);

		if (performTracing) {
			canvas.applyChanges();
		}

		listeners.forEach(l -> l.edgeCrossContaminationsChanged(canvas));
	}

	public Map<String, Boolean> getNodeKillContaminations() {
		return getPropertyValues(nodeSaveMap.values(), TracingColumns.KILL_CONTAMINATION);
	}

	public void setNodeKillContaminations(Map<String, Boolean> nodeKillContaminations) {
		setBooleanPropertyValues(nodeSaveMap.values(), TracingColumns.KILL_CONTAMINATION, nodeKillContaminations);

		if (performTracing) {
			canvas.applyChanges();
		}

		listeners.forEach(l -> l.nodeKillContaminationsChanged(canvas));
	}

	public Map<String, Boolean> getEdgeKillContaminations() {
		return getPropertyValues(edgeSaveMap.values(), TracingColumns.KILL_CONTAMINATION);
	}

	public void setEdgeKillContaminations(Map<String, Boolean> edgeKillContaminations) {
		setBooleanPropertyValues(edgeSaveMap.values(), TracingColumns.KILL_CONTAMINATION, edgeKillContaminations);

		if (performTracing) {
			canvas.applyChanges();
		}

		listeners.forEach(l -> l.edgeKillContaminationsChanged(canvas));
	}

	public Map<String, Boolean> getObservedNodes() {
		return getPropertyValues(nodeSaveMap.values(), TracingColumns.OBSERVED);
	}

	public void setObservedNodes(Map<String, Boolean> observedNodes) {
		setBooleanPropertyValues(nodeSaveMap.values(), TracingColumns.OBSERVED, observedNodes);

		if (performTracing) {
			canvas.applyChanges();
		}

		listeners.forEach(l -> l.observedNodesChanged(canvas));
	}

	public Map<String, Boolean> getObservedEdges() {
		return getPropertyValues(edgeSaveMap.values(), TracingColumns.OBSERVED);
	}

	public void setObservedEdges(Map<String, Boolean> observedEdges) {
		setBooleanPropertyValues(edgeSaveMap.values(), TracingColumns.OBSERVED, observedEdges);

		if (performTracing) {
			canvas.applyChanges();
		}

		listeners.forEach(l -> l.observedEdgesChanged(canvas));
	}

	public boolean isEnforceTemporalOrder() {
		return enforceTemporalOrderBox.isSelected();
	}

	public void setEnforceTemporalOrder(boolean enforceTemporalOrder) {
		enforceTemporalOrderBox.setSelected(enforceTemporalOrder);
	}

	public boolean isShowForward() {
		return showForwardBox.isSelected();
	}

	public void setShowForward(boolean showForward) {
		showForwardBox.setSelected(showForward);
	}

	public GregorianCalendar getShowToDate() {
		return dateSlider != null ? dateSlider.getShowToDate() : null;
	}

	public void setShowToDate(GregorianCalendar showToDate) {
		if (dateSlider != null) {
			dateSlider.setShowToDate(showToDate);
		}
	}

	public boolean isShowDeliveriesWithoutDate() {
		return dateSlider.isShowDeliveriesWithoutDate();
	}

	public void setShowDeliveriesWithoutDate(boolean showDeliveriesWithoutDate) {
		dateSlider.setShowDeliveriesWithoutDate(showDeliveriesWithoutDate);
	}

	public boolean isPerformTracing() {
		return performTracing;
	}

	public void setPerformTracing(boolean performTracing) {
		this.performTracing = performTracing;

		if (performTracing) {
			canvas.applyChanges();
		}
	}

	public void nodePropertiesItemClicked() {
		EditablePropertiesDialog<V> dialog = EditablePropertiesDialog.createNodeDialog(canvas,
				canvas.getSelectedNodes(), canvas.getNodeSchema(), true);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			canvas.applyChanges();
			listeners.forEach(l -> l.nodePropertiesChanged(canvas));
		}
	}

	public void edgePropertiesItemClicked() {
		if (canvas.isJoinEdges()) {
			PropertiesDialog<V> dialog = PropertiesDialog.createEdgeDialog(canvas, canvas.getSelectedEdges(),
					canvas.getEdgeSchema(), true);

			dialog.setVisible(true);
		} else {
			EditablePropertiesDialog<V> dialog = EditablePropertiesDialog.createEdgeDialog(canvas,
					canvas.getSelectedEdges(), canvas.getEdgeSchema(), true);

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				canvas.applyChanges();
				listeners.forEach(l -> l.edgePropertiesChanged(canvas));
			}
		}
	}

	public void edgeAllPropertiesItemClicked() {
		Set<Edge<V>> allPicked = new LinkedHashSet<>();

		for (Edge<V> p : canvas.getSelectedEdges()) {
			if (joinMap.containsKey(p)) {
				allPicked.addAll(joinMap.get(p));
			} else {
				allPicked.add(p);
			}
		}

		EditablePropertiesDialog<V> dialog = EditablePropertiesDialog.createEdgeDialog(canvas, allPicked,
				canvas.getEdgeSchema(), false);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			canvas.applyChanges();
			listeners.forEach(l -> l.edgePropertiesChanged(canvas));
		}
	}

	public void applyChanges() {
		Set<String> selectedNodeIds = canvas.getSelectedNodeIds();
		Set<String> selectedEdgeIds = canvas.getSelectedEdgeIds();

		canvas.resetNodesAndEdges();
		applyTimeWindow();
		canvas.applyNodeCollapse();
		applyInvisibility();
		canvas.applyJoinEdgesAndSkipEdgeless();
		applyTracing();
		canvas.applyShowEdgesInMetaNode();
		canvas.applyHighlights();
		canvas.getViewer().getGraphLayout()
				.setGraph(CanvasUtils.createGraph(canvas.getViewer(), canvas.getNodes(), canvas.getEdges()));

		canvas.setSelectedNodeIdsWithoutListener(selectedNodeIds);
		canvas.setSelectedEdgeIdsWithoutListener(selectedEdgeIds);
		canvas.getViewer().repaint();
	}

	private void applyTimeWindow() {
		if (dateSlider == null) {
			return;
		}

		GregorianCalendar to = dateSlider.getShowToDate();
		boolean showEdgesWithoutDate = dateSlider.isShowDeliveriesWithoutDate();

		if (to == null && showEdgesWithoutDate) {
			return;
		}

		Delivery afterDelivery = createDeliveryFromDate(to);

		for (Edge<V> edge : new LinkedHashSet<>(canvas.getEdges())) {
			Delivery d = deliveries.get(edge.getId());

			if ((d.getDepartureYear() == null && d.getArrivalYear() == null && !showEdgesWithoutDate)
					|| !d.isBefore(afterDelivery)) {
				canvas.getEdges().remove(edge);
			}
		}
	}

	private void applyInvisibility() {
		if (!isShowForward()) {
			canvas.applyInvisibility();
			return;
		}

		Tracing.Result tracingWithCC = createTracing(canvas.getEdges(), true);
		Tracing.Result tracingWithoutCC = createTracing(canvas.getEdges(), false);
		Set<Edge<V>> removedEdges = new LinkedHashSet<>();

		CanvasUtils.removeInvisibleElements(canvas.getNodes(), canvas.getNodeHighlightConditions());
		removedEdges
				.addAll(CanvasUtils.removeInvisibleElements(canvas.getEdges(), canvas.getEdgeHighlightConditions()));
		removedEdges.addAll(CanvasUtils.removeNodelessEdges(canvas.getEdges(), canvas.getNodes()));

		Set<String> forwardEdges = new LinkedHashSet<>();

		for (Edge<V> edge : canvas.getEdges()) {
			forwardEdges.addAll(tracingWithCC.getForwardDeliveriesByDelivery().get(edge.getId()));
		}

		for (Edge<V> edge : canvas.getEdges()) {
			forwardEdges.removeAll(tracingWithoutCC.getForwardDeliveriesByDelivery().get(edge.getId()));
		}

		for (Edge<V> edge : removedEdges) {
			if (forwardEdges.contains(edge.getId())) {
				canvas.getNodes().add(edge.getFrom());
				canvas.getNodes().add(edge.getTo());
				canvas.getEdges().add(edge);
			}
		}
	}

	private void applyTracing() {
		if (!isPerformTracing()) {
			return;
		}

		Set<Edge<V>> edges = new LinkedHashSet<>();

		if (!canvas.isJoinEdges()) {
			edges.addAll(canvas.getEdges());
		} else {
			for (Edge<V> edge : canvas.getEdges()) {
				edges.addAll(joinMap.get(edge));
			}
		}

		Tracing.Result tracing = createTracing(edges, true);

		Set<String> backwardNodes = new LinkedHashSet<>();
		Set<String> forwardNodes = new LinkedHashSet<>();
		Set<String> backwardEdges = new LinkedHashSet<>();
		Set<String> forwardEdges = new LinkedHashSet<>();

		for (V node : canvas.getNodes()) {
			Boolean value = (Boolean) node.getProperties().get(TracingColumns.OBSERVED);

			if (value != null && value == true) {
				backwardNodes.addAll(tracing.getBackwardStationsByStation().get(node.getId()));
				forwardNodes.addAll(tracing.getForwardStationsByStation().get(node.getId()));
				backwardEdges.addAll(tracing.getBackwardDeliveriesByStation().get(node.getId()));
				forwardEdges.addAll(tracing.getForwardDeliveriesByStation().get(node.getId()));
			}
		}

		for (Edge<V> edge : edges) {
			Boolean value = (Boolean) edge.getProperties().get(TracingColumns.OBSERVED);

			if (value != null && value == true) {
				backwardNodes.addAll(tracing.getBackwardStationsByDelivery().get(edge.getId()));
				forwardNodes.addAll(tracing.getForwardStationsByDelivery().get(edge.getId()));
				backwardEdges.addAll(tracing.getBackwardDeliveriesByDelivery().get(edge.getId()));
				forwardEdges.addAll(tracing.getForwardDeliveriesByDelivery().get(edge.getId()));
			}
		}

		for (V node : canvas.getNodes()) {
			node.getProperties().put(TracingColumns.SCORE, tracing.getStationScore(node.getId()));
			node.getProperties().put(TracingColumns.NORMALIZED_SCORE, tracing.getStationNormalizedScore(node.getId()));
			node.getProperties().put(TracingColumns.POSITIVE_SCORE, tracing.getStationPositiveScore(node.getId()));
			node.getProperties().put(TracingColumns.NEGATIVE_SCORE, tracing.getStationNegativeScore(node.getId()));
			node.getProperties().put(TracingColumns.BACKWARD, backwardNodes.contains(node.getId()));
			node.getProperties().put(TracingColumns.FORWARD, forwardNodes.contains(node.getId()));
		}

		for (Edge<V> edge : edges) {
			edge.getProperties().put(TracingColumns.SCORE, tracing.getDeliveryScore(edge.getId()));
			edge.getProperties().put(TracingColumns.NORMALIZED_SCORE, tracing.getDeliveryNormalizedScore(edge.getId()));
			edge.getProperties().put(TracingColumns.POSITIVE_SCORE, tracing.getDeliveryPositiveScore(edge.getId()));
			edge.getProperties().put(TracingColumns.NEGATIVE_SCORE, tracing.getDeliveryNegativeScore(edge.getId()));
			edge.getProperties().put(TracingColumns.BACKWARD, backwardEdges.contains(edge.getId()));
			edge.getProperties().put(TracingColumns.FORWARD, forwardEdges.contains(edge.getId()));
		}

		if (canvas.isJoinEdges()) {
			for (Edge<V> edge : canvas.getEdges()) {
				edge.getProperties().put(TracingColumns.SCORE, null);
				edge.getProperties().put(TracingColumns.NORMALIZED_SCORE, null);
				edge.getProperties().put(TracingColumns.POSITIVE_SCORE, null);
				edge.getProperties().put(TracingColumns.NEGATIVE_SCORE, null);
				edge.getProperties().put(TracingColumns.OBSERVED, false);
				edge.getProperties().put(TracingColumns.BACKWARD, false);
				edge.getProperties().put(TracingColumns.FORWARD, false);

				for (Edge<V> e : joinMap.get(edge)) {
					if ((Boolean) e.getProperties().get(TracingColumns.OBSERVED)) {
						edge.getProperties().put(TracingColumns.OBSERVED, true);
					}

					if ((Boolean) e.getProperties().get(TracingColumns.BACKWARD)) {
						edge.getProperties().put(TracingColumns.BACKWARD, true);
					}

					if ((Boolean) e.getProperties().get(TracingColumns.FORWARD)) {
						edge.getProperties().put(TracingColumns.FORWARD, true);
					}
				}
			}
		}
	}

	private Tracing.Result createTracing(Set<Edge<V>> edges, boolean useCrossContamination) {
		Map<String, Delivery> activeDeliveries = new LinkedHashMap<>();

		for (Edge<V> edge : edges) {
			activeDeliveries.put(edge.getId(), deliveries.get(edge.getId()));
		}

		Tracing tracing = new Tracing(activeDeliveries.values());

		for (Map.Entry<String, Set<String>> entry : canvas.getCollapsedNodes().entrySet()) {
			tracing.mergeStations(entry.getValue(), entry.getKey());
		}

		for (V node : canvas.getNodes()) {
			Double caseValue = (Double) node.getProperties().get(TracingColumns.WEIGHT);
			Boolean contaminationValue = (Boolean) node.getProperties().get(TracingColumns.CROSS_CONTAMINATION);
			Boolean killValue = (Boolean) node.getProperties().get(TracingColumns.KILL_CONTAMINATION);

			tracing.setStationWeight(node.getId(), caseValue != null ? caseValue : 0.0);
			tracing.setCrossContaminationOfStation(node.getId(),
					contaminationValue != null && useCrossContamination ? contaminationValue : false);
			tracing.setKillContaminationOfStation(node.getId(), killValue != null ? killValue : false);
		}

		for (Edge<V> edge : edges) {
			Double caseValue = (Double) edge.getProperties().get(TracingColumns.WEIGHT);
			Boolean contaminationValue = (Boolean) edge.getProperties().get(TracingColumns.CROSS_CONTAMINATION);
			Boolean killValue = (Boolean) edge.getProperties().get(TracingColumns.KILL_CONTAMINATION);

			tracing.setDeliveryWeight(edge.getId(), caseValue != null ? caseValue : 0.0);
			tracing.setCrossContaminationOfDelivery(edge.getId(),
					contaminationValue != null && useCrossContamination ? contaminationValue : false);
			tracing.setKillContaminationOfDelivery(edge.getId(), killValue != null ? killValue : false);
		}

		return tracing.getResult(isEnforceTemporalOrder());
	}

	private static Pair<GregorianCalendar, GregorianCalendar> getDateRange(Collection<Delivery> deliveries) {
		Optional<GregorianCalendar> from = deliveries.stream().map(d -> createDateFromDelivery(d, false))
				.filter(Objects::nonNull).min(GregorianCalendar::compareTo);
		Optional<GregorianCalendar> to = deliveries.stream().map(d -> createDateFromDelivery(d, true))
				.filter(Objects::nonNull).max(GregorianCalendar::compareTo);

		return from.isPresent() && to.isPresent() ? new Pair<>(from.get(), to.get()) : null;
	}

	private static Delivery createDeliveryFromDate(GregorianCalendar c) {
		return c != null
				? new Delivery(null, null, null, c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1,
						c.get(Calendar.YEAR), null, null, null)
				: new Delivery(null, null, null, null, null, null, null, null, null);
	}

	private static GregorianCalendar createDateFromDelivery(Delivery d, boolean arrival) {
		Integer year = arrival ? d.getArrivalYear() : d.getDepartureYear();
		Integer month = arrival ? d.getArrivalMonth() : d.getDepartureMonth();
		Integer day = arrival ? d.getArrivalDay() : d.getDepartureDay();

		if (year == null) {
			return null;
		}

		if (month == null) {
			month = arrival ? 12 : 1;
		}

		if (day == null) {
			day = arrival ? new GregorianCalendar(year, month - 1, 1).get(Calendar.DAY_OF_MONTH) : 1;
		}

		return new GregorianCalendar(year, month - 1, day);
	}

	@SuppressWarnings("unchecked")
	private static <T> Map<String, T> getPropertyValues(Collection<? extends Element> elements, String property) {
		Map<String, T> values = new LinkedHashMap<>();

		for (Element e : elements) {
			values.put(e.getId(), (T) e.getProperties().get(property));
		}

		return values;
	}

	private static void setDoublePropertyValues(Collection<? extends Element> elements, String property,
			Map<String, Double> values) {
		for (Element e : elements) {
			if (values.containsKey(e.getId())) {
				Double value = values.get(e.getId());

				e.getProperties().put(property, value != null ? value : 0.0);
			}
		}
	}

	private static void setBooleanPropertyValues(Collection<? extends Element> elements, String property,
			Map<String, Boolean> values) {
		for (Element e : elements) {
			if (values.containsKey(e.getId())) {
				Boolean value = values.get(e.getId());

				e.getProperties().put(property, value != null ? value : false);
			}
		}
	}

	public static class HighlightChecker implements HighlightConditionChecker {

		@Override
		public String findError(HighlightCondition condition) {
			if (condition != null && condition.isInvisible()
					&& !Sets.intersection(CanvasUtils.getUsedProperties(condition),
							new LinkedHashSet<>(TracingColumns.OUTPUT_COLUMNS)).isEmpty()) {
				return "The following columns cannot be used with \"Invisible\" option:\n"
						+ Joiner.on(", ").join(TracingColumns.OUTPUT_COLUMNS);
			}

			return null;
		}
	}

	public static class PostPaintable implements Paintable {

		private ICanvas<?> canvas;

		public PostPaintable(ICanvas<?> canvas) {
			this.canvas = canvas;
		}

		@Override
		public boolean useTransform() {
			return false;
		}

		@Override
		public void paint(Graphics graphics) {
			int w = canvas.getCanvasSize().width;
			int h = canvas.getCanvasSize().height;
			Font font = new Font("Default", Font.BOLD, 20);

			Graphics2D g = (Graphics2D) graphics;
			Color currentColor = g.getColor();
			Font currentFont = g.getFont();
			int height = 28;
			int fontHeight = g.getFontMetrics(font).getHeight();
			int fontAscent = g.getFontMetrics(font).getAscent();
			int dFont = (height - fontHeight) / 2;
			int logoHeight = 18;
			int dLogo = (height - logoHeight) / 2;

			int dx = 10;
			String s1 = "Created with";
			int sw1 = (int) font.getStringBounds(s1, g.getFontRenderContext()).getWidth();
			String s2 = "by";
			int sw2 = (int) font.getStringBounds(s2, g.getFontRenderContext()).getWidth();
			FoodChainLabLogo logo1 = new FoodChainLabLogo();
			int iw1 = logo1.getOrigWidth() * logoHeight / logo1.getOrigHeight();
			BfrLogo logo2 = new BfrLogo();
			int iw2 = logo2.getOrigWidth() * logoHeight / logo2.getOrigHeight();

			g.setColor(CanvasUtils.LEGEND_BACKGROUND);
			g.fillRect(w - sw1 - iw1 - sw2 - iw2 - 5 * dx, h - height, sw1 + iw1 + sw2 + iw2 + 5 * dx, height);
			g.setColor(Color.BLACK);
			g.drawRect(w - sw1 - iw1 - sw2 - iw2 - 5 * dx, h - height, sw1 + iw1 + sw2 + iw2 + 5 * dx, height);
			g.setFont(font);
			g.drawString(s1, w - sw1 - iw1 - sw2 - iw2 - 4 * dx, h - fontHeight - dFont + fontAscent);
			logo1.setDimension(new Dimension(iw1, logoHeight));
			logo1.paintIcon(null, g, w - iw1 - sw2 - iw2 - 3 * dx, h - logoHeight - dLogo);
			g.drawString(s2, w - sw2 - iw2 - 2 * dx, h - fontHeight - dFont + fontAscent);
			logo2.setDimension(new Dimension(iw2, logoHeight));
			logo2.paintIcon(null, g, w - iw2 - dx, h - logoHeight - dLogo);

			g.setColor(currentColor);
			g.setFont(currentFont);
		}
	}

	public static class PickingPlugin<V extends Node> extends BetterPickingGraphMousePlugin<V, Edge<V>> {

		private ITracingCanvas<V> canvas;

		public PickingPlugin(ITracingCanvas<V> canvas) {
			this.canvas = canvas;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			VisualizationViewer<V, Edge<V>> viewer = canvas.getViewer();

			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
				V node = viewer.getPickSupport().getVertex(viewer.getGraphLayout(), e.getX(), e.getY());
				Edge<V> edge = viewer.getPickSupport().getEdge(viewer.getGraphLayout(), e.getX(), e.getY());

				if (node != null) {
					EditableSinglePropertiesDialog dialog = new EditableSinglePropertiesDialog(e.getComponent(), node,
							canvas.getNodeSchema().getMap());

					dialog.setVisible(true);

					if (dialog.isApproved()) {
						canvas.applyChanges();
						canvas.getTracingListeners().forEach(l -> l.nodePropertiesChanged(canvas));
					}
				} else if (edge != null) {
					if (!canvas.isJoinEdges()) {
						EditableSinglePropertiesDialog dialog = new EditableSinglePropertiesDialog(e.getComponent(),
								edge, canvas.getEdgeSchema().getMap());

						dialog.setVisible(true);

						if (dialog.isApproved()) {
							canvas.applyChanges();
							canvas.getTracingListeners().forEach(l -> l.edgePropertiesChanged(canvas));
						}
					} else {
						SinglePropertiesDialog dialog = new SinglePropertiesDialog(e.getComponent(), edge,
								canvas.getEdgeSchema());

						dialog.setVisible(true);
					}
				}
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (vertex == null || !(canvas instanceof GisCanvas)) {
				super.mouseDragged(e);
			}
		}
	}
}
