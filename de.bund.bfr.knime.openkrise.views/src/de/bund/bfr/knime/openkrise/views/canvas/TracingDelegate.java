/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

import de.bund.bfr.jung.ZoomingPaintable;
import de.bund.bfr.knime.Pair;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.ICanvas;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightConditionChecker;
import de.bund.bfr.knime.gis.views.canvas.dialogs.PropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SinglePropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.common.Delivery;
import de.bund.bfr.knime.openkrise.common.Tracing;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;

import java.util.logging.Logger;

public class TracingDelegate<V extends Node> {

	private static Logger logger =  Logger.getLogger("de.bund.bfr");
	
	private static boolean DEFAULT_PERFORM_TRACING = true;
	private static boolean DEFAULT_ENFORCE_TEMPORAL_ORDER = false;
	private static boolean DEFAULT_SHOW_FORWARD = false;

	private ITracingCanvas<V> canvas;
	private Map<String, V> nodeSaveMap;
	private Map<String, Edge<V>> edgeSaveMap;
	private Map<Edge<V>, Set<Edge<V>>> joinMap;
	private Map<String, Delivery> deliveries;

	private boolean performTracing;

	private JCheckBox enforceTemporalOrderBox;
	private JCheckBox showForwardBox;
	private DateSlider dateSlider;

	public TracingDelegate(ITracingCanvas<V> canvas, Map<String, V> nodeSaveMap, Map<String, Edge<V>> edgeSaveMap,
			Map<Edge<V>, Set<Edge<V>>> joinMap, Map<String, Delivery> deliveries) {
		logger.finest("entered");
		this.canvas = canvas;
		this.nodeSaveMap = nodeSaveMap;
		this.edgeSaveMap = edgeSaveMap;
		this.joinMap = joinMap;
		this.deliveries = deliveries;

		performTracing = DEFAULT_PERFORM_TRACING;

		enforceTemporalOrderBox = new JCheckBox("Activate");
		enforceTemporalOrderBox.setSelected(DEFAULT_ENFORCE_TEMPORAL_ORDER);
		enforceTemporalOrderBox.addItemListener(e -> {
			if (performTracing) {
				canvas.applyChanges();
			}

			call(l -> l.enforceTemporalOrderChanged(canvas));
		});

		showForwardBox = new JCheckBox("Activate");
		showForwardBox.setSelected(DEFAULT_SHOW_FORWARD);
		showForwardBox.addItemListener(e -> {
			if (performTracing) {
				canvas.applyChanges();
			}

			call(l -> l.showForwardChanged(canvas));
		});

		Pair<GregorianCalendar, GregorianCalendar> dateRange = getDateRange(deliveries.values());

		if (dateRange != null) {
			GregorianCalendar from = dateRange.getFirst();
			GregorianCalendar to = dateRange.getSecond();

			from.add(Calendar.DAY_OF_MONTH, -1);

			dateSlider = new DateSlider(from, to);
			
			dateSlider.addDateListener(e -> {
				applyChanges();
				call(l -> l.dateSettingsChanged(canvas));
			});
			
			JScrollPane pane = new JScrollPane(dateSlider, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

			canvas.getComponent().add(pane, BorderLayout.NORTH);
			pane.addComponentListener(new ComponentAdapter() {

				@Override
				public void componentResized(ComponentEvent e) {
					if (pane.getSize().width < pane.getPreferredSize().width) {
						if (pane.getHorizontalScrollBarPolicy() != ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS) {
							pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
							pane.getParent().revalidate();
						}
					} else {
						if (pane.getHorizontalScrollBarPolicy() != ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {
							pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
							pane.getParent().revalidate();
						}
					}
				}
			});
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
		
		logger.finest("leaving");
	}

	public Map<String, Double> getNodeWeights() {
		return getPropertyValues(nodeSaveMap.values(), TracingColumns.WEIGHT);
	}

	public void setNodeWeights(Map<String, Double> nodeWeights) {
		logger.finest("entered");
		setDoublePropertyValues(nodeSaveMap.values(), TracingColumns.WEIGHT, nodeWeights);

		if (performTracing) {
			canvas.applyChanges();
		}

		call(l -> l.nodeWeightsChanged(canvas));
		logger.finest("leaving");
	}

	public Map<String, Double> getEdgeWeights() {
		return getPropertyValues(edgeSaveMap.values(), TracingColumns.WEIGHT);
	}

	public void setEdgeWeights(Map<String, Double> edgeWeights) {
		logger.finest("entered");
		setDoublePropertyValues(edgeSaveMap.values(), TracingColumns.WEIGHT, edgeWeights);

		if (performTracing) {
			canvas.applyChanges();
		}

		call(l -> l.edgeWeightsChanged(canvas));
		logger.finest("leaving");
	}

	public Map<String, Boolean> getNodeCrossContaminations() {
		return getPropertyValues(nodeSaveMap.values(), TracingColumns.CROSS_CONTAMINATION);
	}

	public void setNodeCrossContaminations(Map<String, Boolean> nodeCrossContaminations) {
		logger.finest("entered");
		setBooleanPropertyValues(nodeSaveMap.values(), TracingColumns.CROSS_CONTAMINATION, nodeCrossContaminations);

		if (performTracing) {
			canvas.applyChanges();
		}

		call(l -> l.nodeCrossContaminationsChanged(canvas));
		logger.finest("leaving");
	}

	public Map<String, Boolean> getEdgeCrossContaminations() {
		return getPropertyValues(edgeSaveMap.values(), TracingColumns.CROSS_CONTAMINATION);
	}

	public void setEdgeCrossContaminations(Map<String, Boolean> edgeCrossContaminations) {
		logger.finest("entered");
		setBooleanPropertyValues(edgeSaveMap.values(), TracingColumns.CROSS_CONTAMINATION, edgeCrossContaminations);

		if (performTracing) {
			canvas.applyChanges();
		}

		call(l -> l.edgeCrossContaminationsChanged(canvas));
		logger.finest("leaving");
	}

	public Map<String, Boolean> getNodeKillContaminations() {
		return getPropertyValues(nodeSaveMap.values(), TracingColumns.KILL_CONTAMINATION);
	}

	public void setNodeKillContaminations(Map<String, Boolean> nodeKillContaminations) {
		logger.finest("entered");
		setBooleanPropertyValues(nodeSaveMap.values(), TracingColumns.KILL_CONTAMINATION, nodeKillContaminations);

		if (performTracing) {
			canvas.applyChanges();
		}

		call(l -> l.nodeKillContaminationsChanged(canvas));
		logger.finest("leaving");
	}

	public Map<String, Boolean> getEdgeKillContaminations() {
		return getPropertyValues(edgeSaveMap.values(), TracingColumns.KILL_CONTAMINATION);
	}

	public void setEdgeKillContaminations(Map<String, Boolean> edgeKillContaminations) {
		logger.finest("entered");
		setBooleanPropertyValues(edgeSaveMap.values(), TracingColumns.KILL_CONTAMINATION, edgeKillContaminations);

		if (performTracing) {
			canvas.applyChanges();
		}

		call(l -> l.edgeKillContaminationsChanged(canvas));
		logger.finest("leaving");
	}

	public Map<String, Boolean> getObservedNodes() {
		return getPropertyValues(nodeSaveMap.values(), TracingColumns.OBSERVED);
	}

	public void setObservedNodes(Map<String, Boolean> observedNodes) {
		logger.finest("entered");
		setBooleanPropertyValues(nodeSaveMap.values(), TracingColumns.OBSERVED, observedNodes);

		if (performTracing) {
			canvas.applyChanges();
		}

		call(l -> l.observedNodesChanged(canvas));
		logger.finest("leaving");
	}

	public Map<String, Boolean> getObservedEdges() {
		return getPropertyValues(edgeSaveMap.values(), TracingColumns.OBSERVED);
	}

	public void setObservedEdges(Map<String, Boolean> observedEdges) {
		logger.finest("entered");
		setBooleanPropertyValues(edgeSaveMap.values(), TracingColumns.OBSERVED, observedEdges);

		if (performTracing) {
			canvas.applyChanges();
		}

		call(l -> l.observedEdgesChanged(canvas));
		logger.finest("leaving");
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
		return dateSlider != null ? dateSlider.isShowDeliveriesWithoutDate() : true;
	}

	public void setShowDeliveriesWithoutDate(boolean showDeliveriesWithoutDate) {
		if (dateSlider != null) {
			dateSlider.setShowDeliveriesWithoutDate(showDeliveriesWithoutDate);
		}
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
			call(l -> l.nodePropertiesChanged(canvas));
		}
	}

	public void edgePropertiesItemClicked() {
		if (canvas.getOptionsPanel().isJoinEdges()) {
			PropertiesDialog<V> dialog = PropertiesDialog.createEdgeDialog(canvas, canvas.getSelectedEdges(),
					canvas.getEdgeSchema(), true);

			dialog.setVisible(true);
		} else {
			EditablePropertiesDialog<V> dialog = EditablePropertiesDialog.createEdgeDialog(canvas,
					canvas.getSelectedEdges(), canvas.getEdgeSchema(), true);

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				canvas.applyChanges();
				call(l -> l.edgePropertiesChanged(canvas));
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
			call(l -> l.edgePropertiesChanged(canvas));
		}
	}

	protected void applyChanges(boolean ignoreTimeWindow) {
		logger.finest("entered");
		
		Set<String> selectedNodeIds = canvas.getSelectedNodeIds();
		Set<String> selectedEdgeIds = canvas.getSelectedEdgeIds();

		canvas.resetNodesAndEdges();
		if(!ignoreTimeWindow) applyTimeWindow();
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
//		canvas.getViewer().repaint();
		logger.finest("leaving");
	}
	
	public void applyChanges() {
		this.applyChanges(false);
		canvas.getViewer().repaint();
		
//		Set<String> selectedNodeIds = canvas.getSelectedNodeIds();
//		Set<String> selectedEdgeIds = canvas.getSelectedEdgeIds();
//
//		canvas.resetNodesAndEdges();
//		applyTimeWindow();
//		canvas.applyNodeCollapse();
//		applyInvisibility();
//		canvas.applyJoinEdgesAndSkipEdgeless();
//		applyTracing();
//		canvas.applyShowEdgesInMetaNode();
//		canvas.applyHighlights();
//		canvas.getViewer().getGraphLayout()
//				.setGraph(CanvasUtils.createGraph(canvas.getViewer(), canvas.getNodes(), canvas.getEdges()));
//
//		canvas.setSelectedNodeIdsWithoutListener(selectedNodeIds);
//		canvas.setSelectedEdgeIdsWithoutListener(selectedEdgeIds);
//		canvas.getViewer().repaint();
//		logger.finest("leaving");
	}

	public void doubleClickedOn(Object obj) {
		if (obj instanceof Node) {
			EditableSinglePropertiesDialog dialog = new EditableSinglePropertiesDialog(canvas.getViewer(),
					(Element) obj, canvas.getNodeSchema().getMap(), EditableSinglePropertiesDialog.Type.NODE);

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				canvas.applyChanges();
				Stream.of(canvas.getComponent().getListeners(TracingListener.class))
						.forEach(l -> l.nodePropertiesChanged(canvas));
			}
		} else if (obj instanceof Edge) {
			if (!canvas.getOptionsPanel().isJoinEdges()) {
				EditableSinglePropertiesDialog dialog = new EditableSinglePropertiesDialog(canvas.getViewer(),
						(Element) obj, canvas.getEdgeSchema().getMap(), EditableSinglePropertiesDialog.Type.EDGE);

				dialog.setVisible(true);

				if (dialog.isApproved()) {
					canvas.applyChanges();
					Stream.of(canvas.getComponent().getListeners(TracingListener.class))
							.forEach(l -> l.edgePropertiesChanged(canvas));
				}
			} else {
				SinglePropertiesDialog dialog = new SinglePropertiesDialog(canvas.getViewer(), (Element) obj,
						canvas.getEdgeSchema());

				dialog.setVisible(true);
			}
		}
	}

	private void applyTimeWindow() {
		logger.finest("entered");
		if (dateSlider == null) {
			logger.finest("leaving dataSlider is null");
			return;
		}

		GregorianCalendar to = dateSlider.getShowToDate();
		boolean showEdgesWithoutDate = dateSlider.isShowDeliveriesWithoutDate();

		if (to == null && showEdgesWithoutDate) {
			logger.finest("leaving to == null && showEdgesWithoutDate");
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
		logger.finest("leaving");
	}

	private void applyInvisibility() {
		logger.finest("entered");
		if (!isShowForward()) {
			canvas.applyInvisibility();
			logger.finest("leaving !isShowForward()");
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
		logger.finest("leaving");
	}

	private void applyTracing() {
		logger.finest("entered");
		if (!isPerformTracing()) {
			logger.finest("leaving !isPerformTracing()");
			return;
		}

		Set<Edge<V>> edges = new LinkedHashSet<>();

		if (!canvas.getOptionsPanel().isJoinEdges()) {
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
			if (Boolean.TRUE.equals(node.getProperties().get(TracingColumns.OBSERVED))) {
				backwardNodes.addAll(tracing.getBackwardStationsByStation().get(node.getId()));
				forwardNodes.addAll(tracing.getForwardStationsByStation().get(node.getId()));
				backwardEdges.addAll(tracing.getBackwardDeliveriesByStation().get(node.getId()));
				forwardEdges.addAll(tracing.getForwardDeliveriesByStation().get(node.getId()));
			}
		}

		for (Edge<V> edge : edges) {
			if (Boolean.TRUE.equals(edge.getProperties().get(TracingColumns.OBSERVED))) {
				backwardNodes.addAll(tracing.getBackwardStationsByDelivery().get(edge.getId()));
				forwardNodes.addAll(tracing.getForwardStationsByDelivery().get(edge.getId()));
				backwardEdges.addAll(tracing.getBackwardDeliveriesByDelivery().get(edge.getId()));
				forwardEdges.addAll(tracing.getForwardDeliveriesByDelivery().get(edge.getId()));
			}
		}

		for (V node : canvas.getNodes()) {
			node.getProperties().put(TracingColumns.SCORE, tracing.getStationScore(node.getId()));
			node.getProperties().put(TracingColumns.MAX_LOT_SCORE, tracing.getMaxLotScore(node.getId()));
			node.getProperties().put(TracingColumns.NORMALIZED_SCORE, tracing.getStationNormalizedScore(node.getId()));
			node.getProperties().put(TracingColumns.POSITIVE_SCORE, tracing.getStationPositiveScore(node.getId()));
			node.getProperties().put(TracingColumns.NEGATIVE_SCORE, tracing.getStationNegativeScore(node.getId()));
			node.getProperties().put(TracingColumns.BACKWARD, backwardNodes.contains(node.getId()));
			node.getProperties().put(TracingColumns.FORWARD, forwardNodes.contains(node.getId()));
		}

		for (Edge<V> edge : edges) {
			edge.getProperties().put(TracingColumns.SCORE, tracing.getDeliveryScore(edge.getId()));
			edge.getProperties().put(TracingColumns.LOT_SCORE, tracing.getLotScore(edge.getId()));
			edge.getProperties().put(TracingColumns.NORMALIZED_SCORE, tracing.getDeliveryNormalizedScore(edge.getId()));
			edge.getProperties().put(TracingColumns.POSITIVE_SCORE, tracing.getDeliveryPositiveScore(edge.getId()));
			edge.getProperties().put(TracingColumns.NEGATIVE_SCORE, tracing.getDeliveryNegativeScore(edge.getId()));
			edge.getProperties().put(TracingColumns.BACKWARD, backwardEdges.contains(edge.getId()));
			edge.getProperties().put(TracingColumns.FORWARD, forwardEdges.contains(edge.getId()));
		}

		if (canvas.getOptionsPanel().isJoinEdges()) {
			for (Edge<V> edge : canvas.getEdges()) {
				edge.getProperties().put(TracingColumns.SCORE, null);
				edge.getProperties().put(TracingColumns.NORMALIZED_SCORE, null);
				edge.getProperties().put(TracingColumns.POSITIVE_SCORE, null);
				edge.getProperties().put(TracingColumns.NEGATIVE_SCORE, null);
				edge.getProperties().put(TracingColumns.LOT_SCORE, null);
				edge.getProperties().put(TracingColumns.OBSERVED, false);
				edge.getProperties().put(TracingColumns.BACKWARD, false);
				edge.getProperties().put(TracingColumns.FORWARD, false);

				for (Edge<V> e : joinMap.get(edge)) {
					if (Boolean.TRUE.equals(e.getProperties().get(TracingColumns.OBSERVED))) {
						edge.getProperties().put(TracingColumns.OBSERVED, true);
					}

					if (Boolean.TRUE.equals(e.getProperties().get(TracingColumns.BACKWARD))) {
						edge.getProperties().put(TracingColumns.BACKWARD, true);
					}

					if (Boolean.TRUE.equals(e.getProperties().get(TracingColumns.FORWARD))) {
						edge.getProperties().put(TracingColumns.FORWARD, true);
					}
				}
			}
		}
		logger.finest("leaving");
	}

	private Tracing.Result createTracing(Set<Edge<V>> edges, boolean useCrossContamination) {
		Map<String, Delivery> activeDeliveries = new LinkedHashMap<>();

		for (Edge<V> edge : edges) {
			activeDeliveries.put(edge.getId(), deliveries.get(edge.getId()));
		}

		Tracing tracing = new Tracing(activeDeliveries.values());

		canvas.getCollapsedNodes().forEach((metaId, containedIds) -> tracing.mergeStations(containedIds, metaId));

		for (V node : canvas.getNodes()) {
			tracing.setStationWeight(node.getId(), node.getProperties().get(TracingColumns.WEIGHT) instanceof Double
					? (Double) node.getProperties().get(TracingColumns.WEIGHT) : 0.0);
			tracing.setCrossContaminationOfStation(node.getId(), useCrossContamination
					? Boolean.TRUE.equals(node.getProperties().get(TracingColumns.CROSS_CONTAMINATION)) : false);
			tracing.setKillContaminationOfStation(node.getId(),
					Boolean.TRUE.equals(node.getProperties().get(TracingColumns.KILL_CONTAMINATION)));
		}

		for (Edge<V> edge : edges) {
			tracing.setDeliveryWeight(edge.getId(), edge.getProperties().get(TracingColumns.WEIGHT) instanceof Double
					? (Double) edge.getProperties().get(TracingColumns.WEIGHT) : 0.0);
			tracing.setCrossContaminationOfDelivery(edge.getId(), useCrossContamination
					? Boolean.TRUE.equals(edge.getProperties().get(TracingColumns.CROSS_CONTAMINATION)) : false);
			tracing.setKillContaminationOfDelivery(edge.getId(),
					Boolean.TRUE.equals(edge.getProperties().get(TracingColumns.KILL_CONTAMINATION)));
		}

		return tracing.getResult(isEnforceTemporalOrder());
	}

	private void call(Consumer<TracingListener> action) {
		Stream.of(canvas.getComponent().getListeners(TracingListener.class)).forEach(action);
	}

	private static Pair<GregorianCalendar, GregorianCalendar> getDateRange(Collection<Delivery> deliveries) {
		Optional<GregorianCalendar> from = deliveries.stream().map(d -> createDateFromDelivery(d, false))
				.filter(Objects::nonNull).min(GregorianCalendar::compareTo);
		Optional<GregorianCalendar> to = deliveries.stream().map(d -> createDateFromDelivery(d, true))
				.filter(Objects::nonNull).max(GregorianCalendar::compareTo);

		return from.isPresent() && to.isPresent() ? new Pair<>(from.get(), to.get()) : null;
	}

	private static Delivery createDeliveryFromDate(GregorianCalendar c) {
		return c != null ? new Delivery.Builder("", "", "")
				.departure(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)).build()
				: new Delivery.Builder("", "", "").build();
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
							new LinkedHashSet<>(TracingColumns.OUT_COLUMNS)).isEmpty()) {
				return "The following columns cannot be used with \"Invisible\" option:\n"
						+ Joiner.on(", ").join(TracingColumns.OUT_COLUMNS);
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
			
			// Additional Logos
			int swSlash = (int) font.getStringBounds("/", g.getFontRenderContext()).getWidth();
			int iwTotal = 0;
			
			List<Icon> logoArr = new ArrayList<>();
			List<Integer> logoLeft = new ArrayList<>();
			List<Integer> logoTop = new ArrayList<>();
			/*
			IdvLogo idv = new IdvLogo();
			int iw3 = idv.getOrigWidth() * logoHeight / idv.getOrigHeight();
			logoLeft.add(11); logoTop.add(0);
			idv.setDimension(new Dimension(iw3, logoHeight));
			logoArr.add(idv);
			iwTotal += iw3;
			*/
			/*
			CgiLogo cgi = new CgiLogo();
			iw3 = cgi.getOrigWidth() * logoHeight / cgi.getOrigHeight();
			logoLeft.add(0); logoTop.add(-8);
			cgi.setDimension(new Dimension(iw3, logoHeight));
			logoArr.add(cgi);
			iwTotal += iw3;
			*/
			
			g.setColor(ZoomingPaintable.BACKGROUND);
			
			g.fillRect(w - sw1 - iw1 - sw2 - iw2 - swSlash*logoArr.size() - iwTotal - (5+2*logoArr.size()) * dx, h - height, sw1 + iw1 + sw2 + iw2 + swSlash*logoArr.size() + iwTotal + (5+2*logoArr.size()) * dx, height);
			g.setColor(Color.BLACK);
			g.drawRect(w - sw1 - iw1 - sw2 - iw2 - swSlash*logoArr.size() - iwTotal - (5+2*logoArr.size()) * dx, h - height, sw1 + iw1 + sw2 + iw2 + swSlash*logoArr.size() + iwTotal + (5+2*logoArr.size()) * dx - 1, height - 1);
			g.setFont(font);
			
			g.drawString(s1, w - sw1 - iw1 - sw2 - iw2 - swSlash*logoArr.size() - iwTotal - (4+2*logoArr.size()) * dx, h - fontHeight - dFont + fontAscent);
			logo1.setDimension(new Dimension(iw1, logoHeight));
			logo1.paintIcon(null, g, w - iw1 - sw2 - iw2 - swSlash*logoArr.size() - iwTotal - (3+2*logoArr.size()) * dx, h - logoHeight - dLogo);
			
			int ddx = 0, diwTotal = 0, dSlash = 0;
			int ii = 0;
			for (Icon i : logoArr) {
				g.drawString("/", w - sw2 - iw2 - swSlash*logoArr.size() + dSlash - iwTotal + diwTotal - (2+2*logoArr.size()) * dx + ddx, h - fontHeight - dFont + fontAscent - 2);
				i.paintIcon(null, g, w - sw2 - iw2 - swSlash*logoArr.size() + dSlash - iwTotal + diwTotal - (1+2*logoArr.size()) * dx + ddx + logoLeft.get(ii), h - logoHeight - dLogo + logoTop.get(ii));
				ddx += 2*dx;
				diwTotal += i.getIconWidth();
				dSlash += swSlash;
				ii++;
			}
			
			g.drawString(s2, w - sw2 - iw2 - 2 * dx, h - fontHeight - dFont + fontAscent);
			logo2.setDimension(new Dimension(iw2, logoHeight));
			logo2.paintIcon(null, g, w - iw2 - dx, h - logoHeight - dLogo);
			

			g.setColor(currentColor);
			g.setFont(currentFont);
		}
	}
}
