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
package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import com.google.common.base.Joiner;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.GisCanvas;
import de.bund.bfr.knime.gis.views.canvas.ICanvas;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightConditionChecker;
import de.bund.bfr.knime.gis.views.canvas.dialogs.PropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SinglePropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition;
import de.bund.bfr.knime.openkrise.MyDelivery;
import de.bund.bfr.knime.openkrise.MyNewTracing;
import de.bund.bfr.knime.openkrise.TracingColumns;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;

public class Tracing<V extends Node> implements ActionListener, ItemListener {

	private static boolean DEFAULT_PERFORM_TRACING = true;
	private static boolean DEFAULT_ENFORCE_TEMPORAL_ORDER = false;
	private static boolean DEFAULT_SHOW_FORWARD = false;

	private ITracingCanvas<V> canvas;
	private Map<String, V> nodeSaveMap;
	private Map<String, Edge<V>> edgeSaveMap;
	private Map<Edge<V>, Set<Edge<V>>> joinMap;
	private Map<Integer, MyDelivery> deliveries;

	private boolean performTracing;

	private JCheckBox enforceTemporalOrderBox;
	private JCheckBox showForwardBox;
	private JMenuItem defaultHighlightItem;

	public Tracing(ITracingCanvas<V> canvas, Map<String, V> nodeSaveMap,
			Map<String, Edge<V>> edgeSaveMap,
			Map<Edge<V>, Set<Edge<V>>> joinMap,
			Map<Integer, MyDelivery> deliveries) {
		this.canvas = canvas;
		this.nodeSaveMap = nodeSaveMap;
		this.edgeSaveMap = edgeSaveMap;
		this.joinMap = joinMap;
		this.deliveries = deliveries;

		performTracing = DEFAULT_PERFORM_TRACING;

		enforceTemporalOrderBox = new JCheckBox("Activate");
		enforceTemporalOrderBox.setSelected(DEFAULT_ENFORCE_TEMPORAL_ORDER);
		enforceTemporalOrderBox.addItemListener(this);

		showForwardBox = new JCheckBox("Activate");
		showForwardBox.setSelected(DEFAULT_SHOW_FORWARD);
		showForwardBox.addItemListener(this);

		defaultHighlightItem = new JMenuItem("Set default Highlighting");
		defaultHighlightItem.addActionListener(this);

		canvas.getOptionsPanel().addOption("Enforce Temporal Order",
				enforceTemporalOrderBox);
		canvas.getOptionsPanel().addOption(
				"Show Cross Contaminated " + canvas.getNaming().Edges(),
				showForwardBox);
		canvas.getPopupMenu().add(new JSeparator());
		canvas.getPopupMenu().add(defaultHighlightItem);
		canvas.getViewer()
				.prependPostRenderPaintable(new PostPaintable(canvas));
	}

	public Map<String, Double> getNodeWeights() {
		Map<String, Double> nodeWeights = new LinkedHashMap<>();

		for (V node : nodeSaveMap.values()) {
			nodeWeights.put(node.getId(),
					(Double) node.getProperties().get(TracingColumns.WEIGHT));
		}

		return nodeWeights;
	}

	public void setNodeWeights(Map<String, Double> nodeWeights) {
		for (V node : nodeSaveMap.values()) {
			if (nodeWeights.containsKey(node.getId())) {
				node.getProperties().put(TracingColumns.WEIGHT,
						nullToZero(nodeWeights.get(node.getId())));
			}
		}

		if (performTracing) {
			canvas.applyChanges();
		}
	}

	public Map<String, Double> getEdgeWeights() {
		Map<String, Double> edgeWeights = new LinkedHashMap<>();

		for (Edge<V> edge : edgeSaveMap.values()) {
			edgeWeights.put(edge.getId(),
					(Double) edge.getProperties().get(TracingColumns.WEIGHT));
		}

		return edgeWeights;
	}

	public void setEdgeWeights(Map<String, Double> edgeWeights) {
		for (Edge<V> edge : edgeSaveMap.values()) {
			if (edgeWeights.containsKey(edge.getId())) {
				edge.getProperties().put(TracingColumns.WEIGHT,
						nullToZero(edgeWeights.get(edge.getId())));
			}
		}

		if (performTracing) {
			canvas.applyChanges();
		}
	}

	public Map<String, Boolean> getNodeCrossContaminations() {
		Map<String, Boolean> nodeCrossContaminations = new LinkedHashMap<>();

		for (V node : nodeSaveMap.values()) {
			nodeCrossContaminations.put(node.getId(), (Boolean) node
					.getProperties().get(TracingColumns.CROSS_CONTAMINATION));
		}

		return nodeCrossContaminations;
	}

	public void setNodeCrossContaminations(
			Map<String, Boolean> nodeCrossContaminations) {
		for (V node : nodeSaveMap.values()) {
			if (nodeCrossContaminations.containsKey(node.getId())) {
				node.getProperties().put(TracingColumns.CROSS_CONTAMINATION,
						nullToFalse(nodeCrossContaminations.get(node.getId())));
			}
		}

		if (performTracing) {
			canvas.applyChanges();
		}
	}

	public Map<String, Boolean> getEdgeCrossContaminations() {
		Map<String, Boolean> edgeCrossContaminations = new LinkedHashMap<>();

		for (Edge<V> edge : edgeSaveMap.values()) {
			edgeCrossContaminations.put(edge.getId(), (Boolean) edge
					.getProperties().get(TracingColumns.CROSS_CONTAMINATION));
		}

		return edgeCrossContaminations;
	}

	public void setEdgeCrossContaminations(
			Map<String, Boolean> edgeCrossContaminations) {
		for (Edge<V> edge : edgeSaveMap.values()) {
			if (edgeCrossContaminations.containsKey(edge.getId())) {
				edge.getProperties().put(TracingColumns.CROSS_CONTAMINATION,
						nullToFalse(edgeCrossContaminations.get(edge.getId())));
			}
		}

		if (performTracing) {
			canvas.applyChanges();
		}
	}

	public Map<String, Boolean> getObservedNodes() {
		Map<String, Boolean> observedNodes = new LinkedHashMap<>();

		for (V node : nodeSaveMap.values()) {
			observedNodes
					.put(node.getId(),
							(Boolean) node.getProperties().get(
									TracingColumns.OBSERVED));
		}

		return observedNodes;
	}

	public void setObservedNodes(Map<String, Boolean> observedNodes) {
		for (V node : nodeSaveMap.values()) {
			if (observedNodes.containsKey(node.getId())) {
				node.getProperties().put(TracingColumns.OBSERVED,
						nullToFalse(observedNodes.get(node.getId())));
			}
		}

		if (performTracing) {
			canvas.applyChanges();
		}
	}

	public Map<String, Boolean> getObservedEdges() {
		Map<String, Boolean> observedEdges = new LinkedHashMap<>();

		for (Edge<V> edge : edgeSaveMap.values()) {
			observedEdges
					.put(edge.getId(),
							(Boolean) edge.getProperties().get(
									TracingColumns.OBSERVED));
		}

		return observedEdges;
	}

	public void setObservedEdges(Map<String, Boolean> observedEdges) {
		for (Edge<V> edge : edgeSaveMap.values()) {
			if (observedEdges.containsKey(edge.getId())) {
				edge.getProperties().put(TracingColumns.OBSERVED,
						nullToFalse(observedEdges.get(edge.getId())));
			}
		}

		if (performTracing) {
			canvas.applyChanges();
		}
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
		EditablePropertiesDialog<V> dialog = EditablePropertiesDialog
				.createNodeDialog(canvas, canvas.getSelectedNodes(),
						canvas.getNodeSchema(), true);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			canvas.applyChanges();
		}
	}

	public void edgePropertiesItemClicked() {
		if (canvas.isJoinEdges()) {
			PropertiesDialog<V> dialog = PropertiesDialog.createEdgeDialog(
					canvas, canvas.getSelectedEdges(), canvas.getEdgeSchema(),
					true);

			dialog.setVisible(true);
		} else {
			EditablePropertiesDialog<V> dialog = EditablePropertiesDialog
					.createEdgeDialog(canvas, canvas.getSelectedEdges(),
							canvas.getEdgeSchema(), true);

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				canvas.applyChanges();
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

		EditablePropertiesDialog<V> dialog = EditablePropertiesDialog
				.createEdgeDialog(canvas, allPicked, canvas.getEdgeSchema(),
						false);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			canvas.applyChanges();
		}
	}

	public void applyChanges() {
		Set<String> selectedNodeIds = canvas.getSelectedNodeIds();
		Set<String> selectedEdgeIds = canvas.getSelectedEdgeIds();

		canvas.applyNodeCollapse();
		canvas.applyInvisibility();
		canvas.applyJoinEdgesAndSkipEdgeless();
		applyTracing();
		canvas.applyHighlights();
		canvas.getViewer()
				.getGraphLayout()
				.setGraph(
						CanvasUtils.createGraph(canvas.getNodes(),
								canvas.getEdges()));

		canvas.setSelectedNodeIds(selectedNodeIds);
		canvas.setSelectedEdgeIds(selectedEdgeIds);
		canvas.getViewer().repaint();
	}

	public void applyInvisibility() {
		if (!isShowForward()) {
			CanvasUtils.removeInvisibleElements(canvas.getNodes(),
					canvas.getNodeHighlightConditions());
			CanvasUtils.removeInvisibleElements(canvas.getEdges(),
					canvas.getEdgeHighlightConditions());
			CanvasUtils.removeNodelessEdges(canvas.getEdges(),
					canvas.getNodes());
			return;
		}

		MyNewTracing tracingWithCC = createTracing(canvas.getEdges(), true);
		MyNewTracing tracingWithoutCC = createTracing(canvas.getEdges(), false);
		Set<Edge<V>> removedEdges = new LinkedHashSet<>();

		CanvasUtils.removeInvisibleElements(canvas.getNodes(),
				canvas.getNodeHighlightConditions());
		removedEdges.addAll(CanvasUtils.removeInvisibleElements(
				canvas.getEdges(), canvas.getEdgeHighlightConditions()));
		removedEdges.addAll(CanvasUtils.removeNodelessEdges(canvas.getEdges(),
				canvas.getNodes()));

		Set<Integer> forwardEdges = new LinkedHashSet<>();

		for (Edge<V> edge : canvas.getEdges()) {
			forwardEdges.addAll(tracingWithCC
					.getForwardDeliveries2(getIntegerId(edge)));
		}

		for (Edge<V> edge : canvas.getEdges()) {
			forwardEdges.removeAll(tracingWithoutCC
					.getForwardDeliveries2(getIntegerId(edge)));
		}

		for (Edge<V> edge : removedEdges) {
			if (forwardEdges.contains(getIntegerId(edge))) {
				canvas.getNodes().add(edge.getFrom());
				canvas.getNodes().add(edge.getTo());
				canvas.getEdges().add(edge);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == defaultHighlightItem) {
			if (JOptionPane.showConfirmDialog(canvas.getComponent(),
					"All current highlight conditions will be replaced "
							+ "by default hightlight conditions?",
					"Please Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				canvas.setNodeHighlightConditions(DefaultHighlighting
						.createNodeHighlighting());
				canvas.setEdgeHighlightConditions(DefaultHighlighting
						.createEdgeHighlighting());
			}
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == enforceTemporalOrderBox) {
			if (performTracing) {
				canvas.applyChanges();
			}
		} else if (e.getSource() == showForwardBox) {
			if (performTracing) {
				canvas.applyChanges();
			}
		}
	}

	private MyNewTracing createTracing(Set<Edge<V>> edges,
			boolean useCrossContamination) {
		HashMap<Integer, MyDelivery> activeDeliveries = new HashMap<>();

		for (Edge<V> id : edges) {
			activeDeliveries.put(getIntegerId(id),
					deliveries.get(getIntegerId(id)));
		}

		MyNewTracing tracing = new MyNewTracing(activeDeliveries,
				new LinkedHashMap<Integer, Double>(),
				new LinkedHashMap<Integer, Double>(),
				new LinkedHashSet<Integer>(), new LinkedHashSet<Integer>(), 0.0);

		for (String id : canvas.getCollapsedNodes().keySet()) {
			Set<String> nodeIdStrings = canvas.getCollapsedNodes().get(id);
			Set<Integer> nodeIds = new LinkedHashSet<>();

			for (String idString : nodeIdStrings) {
				nodeIds.add(Integer.parseInt(idString));
			}

			tracing.mergeStations(nodeIds, createId(nodeIdStrings));
		}

		for (V node : canvas.getNodes()) {
			int id = getIntegerId(node, canvas.getCollapsedNodes());
			Double caseValue = (Double) node.getProperties().get(
					TracingColumns.WEIGHT);
			Boolean contaminationValue = (Boolean) node.getProperties().get(
					TracingColumns.CROSS_CONTAMINATION);

			if (caseValue != null) {
				tracing.setCase(id, caseValue);
			} else {
				tracing.setCase(id, 0.0);
			}

			if (useCrossContamination) {
				if (contaminationValue != null) {
					tracing.setCrossContamination(id, contaminationValue);
				} else {
					tracing.setCrossContamination(id, false);
				}
			}
		}

		for (Edge<V> edge : edges) {
			int id = getIntegerId(edge);
			Double caseValue = (Double) edge.getProperties().get(
					TracingColumns.WEIGHT);
			Boolean contaminationValue = (Boolean) edge.getProperties().get(
					TracingColumns.CROSS_CONTAMINATION);

			if (caseValue != null) {
				tracing.setCaseDelivery(id, caseValue);
			} else {
				tracing.setCaseDelivery(id, 0.0);
			}

			if (useCrossContamination) {
				if (contaminationValue != null) {
					tracing.setCrossContaminationDelivery(id,
							contaminationValue);
				} else {
					tracing.setCrossContaminationDelivery(id, false);
				}
			}
		}

		tracing.fillDeliveries(isEnforceTemporalOrder());

		return tracing;
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

		MyNewTracing tracing = createTracing(edges, true);

		Set<Integer> backwardNodes = new LinkedHashSet<>();
		Set<Integer> forwardNodes = new LinkedHashSet<>();
		Set<Integer> backwardEdges = new LinkedHashSet<>();
		Set<Integer> forwardEdges = new LinkedHashSet<>();

		for (V node : canvas.getNodes()) {
			int id = getIntegerId(node, canvas.getCollapsedNodes());
			Boolean value = (Boolean) node.getProperties().get(
					TracingColumns.OBSERVED);

			if (value != null && value == true) {
				backwardNodes.addAll(tracing.getBackwardStations(id));
				forwardNodes.addAll(tracing.getForwardStations(id));
				backwardEdges.addAll(tracing.getBackwardDeliveries(id));
				forwardEdges.addAll(tracing.getForwardDeliveries(id));
			}
		}

		for (Edge<V> edge : canvas.getEdges()) {
			int id = getIntegerId(edge);
			Boolean value = (Boolean) edge.getProperties().get(
					TracingColumns.OBSERVED);

			if (value != null && value == true) {
				backwardNodes.addAll(tracing.getBackwardStations2(id));
				forwardNodes.addAll(tracing.getForwardStations2(id));
				backwardEdges.addAll(tracing.getBackwardDeliveries2(id));
				forwardEdges.addAll(tracing.getForwardDeliveries2(id));
			}
		}

		for (V node : canvas.getNodes()) {
			int id = getIntegerId(node, canvas.getCollapsedNodes());

			node.getProperties().put(TracingColumns.SCORE,
					tracing.getStationScore(id));
			node.getProperties().put(TracingColumns.BACKWARD,
					backwardNodes.contains(id));
			node.getProperties().put(TracingColumns.FORWARD,
					forwardNodes.contains(id));
		}

		for (Edge<V> edge : canvas.getEdges()) {
			int id = getIntegerId(edge);

			edge.getProperties().put(TracingColumns.SCORE,
					tracing.getDeliveryScore(id));
			edge.getProperties().put(TracingColumns.BACKWARD,
					backwardEdges.contains(id));
			edge.getProperties().put(TracingColumns.FORWARD,
					forwardEdges.contains(id));
		}

		if (canvas.isJoinEdges()) {
			for (Edge<V> edge : canvas.getEdges()) {
				edge.getProperties().put(TracingColumns.OBSERVED, null);
				edge.getProperties().put(TracingColumns.SCORE, null);
				edge.getProperties().put(TracingColumns.BACKWARD, null);
				edge.getProperties().put(TracingColumns.FORWARD, null);
			}
		}
	}

	private static double nullToZero(Double value) {
		return value == null ? 0.0 : value;
	}

	private static boolean nullToFalse(Boolean value) {
		return value == null ? false : value;
	}

	private static <V extends Node> int getIntegerId(V node,
			Map<String, Set<String>> collapsedNodes) {
		if (collapsedNodes.containsKey(node.getId())) {
			return createId(collapsedNodes.get(node.getId()));
		} else {
			return Integer.parseInt(node.getId());
		}
	}

	private static <V extends Node> int getIntegerId(Edge<V> edge) {
		return Integer.parseInt(edge.getId());
	}

	private static int createId(Collection<String> c) {
		return KnimeUtils.listToString(new ArrayList<>(c)).hashCode();
	}

	public static class HighlightChecker implements HighlightConditionChecker {

		@Override
		public String findError(HighlightCondition condition) {
			List<String> tracingColumns = Arrays.asList(TracingColumns.SCORE,
					TracingColumns.BACKWARD, TracingColumns.FORWARD);
			String error = "The following columns cannot be used with \"Invisible\" option:\n"
					+ Joiner.on(", ").join(tracingColumns);

			if (condition != null && condition.isInvisible()) {
				AndOrHighlightCondition logicalCondition = null;
				ValueHighlightCondition valueCondition = null;

				if (condition instanceof AndOrHighlightCondition) {
					logicalCondition = (AndOrHighlightCondition) condition;
				} else if (condition instanceof ValueHighlightCondition) {
					valueCondition = (ValueHighlightCondition) condition;
				} else if (condition instanceof LogicalValueHighlightCondition) {
					logicalCondition = ((LogicalValueHighlightCondition) condition)
							.getLogicalCondition();
					valueCondition = ((LogicalValueHighlightCondition) condition)
							.getValueCondition();
				}

				if (logicalCondition != null) {
					for (List<LogicalHighlightCondition> cc : logicalCondition
							.getConditions()) {
						for (LogicalHighlightCondition c : cc) {
							if (tracingColumns.contains(c.getProperty())) {
								return error;
							}
						}
					}
				}

				if (valueCondition != null) {
					if (tracingColumns.contains(valueCondition.getProperty())) {
						return error;
					}
				}
			}

			return null;
		}
	}

	public static class PostPaintable implements Paintable {

		public static final int HEIGHT = 28;

		private ICanvas<?> canvas;

		public PostPaintable(ICanvas<?> canvas) {
			this.canvas = canvas;
		}

		@Override
		public boolean useTransform() {
			return false;
		}

		@Override
		public void paint(Graphics g) {
			int w = canvas.getCanvasSize().width;
			int h = canvas.getCanvasSize().height;

			Font font = new Font("Default", Font.BOLD, 20);

			int fontHeight = g.getFontMetrics(font).getHeight();
			int fontAscent = g.getFontMetrics(font).getAscent();
			int dFont = (HEIGHT - fontHeight) / 2;
			int logoHeight = 18;
			int dLogo = (HEIGHT - logoHeight) / 2;

			int dx = 10;
			String s1 = "Created with";
			int sw1 = (int) font.getStringBounds(s1,
					((Graphics2D) g).getFontRenderContext()).getWidth();
			String s2 = "by";
			int sw2 = (int) font.getStringBounds(s2,
					((Graphics2D) g).getFontRenderContext()).getWidth();
			FoodChainLabLogo logo1 = new FoodChainLabLogo();
			int iw1 = logo1.getOrigWidth() * logoHeight / logo1.getOrigHeight();
			BfrLogo logo2 = new BfrLogo();
			int iw2 = logo2.getOrigWidth() * logoHeight / logo2.getOrigHeight();

			g.setColor(CanvasUtils.LEGEND_BACKGROUND);
			g.fillRect(w - sw1 - iw1 - sw2 - iw2 - 5 * dx, h - HEIGHT, sw1
					+ iw1 + sw2 + iw2 + 5 * dx, HEIGHT);
			g.setColor(Color.BLACK);
			g.drawRect(w - sw1 - iw1 - sw2 - iw2 - 5 * dx, h - HEIGHT, sw1
					+ iw1 + sw2 + iw2 + 5 * dx, HEIGHT);
			g.setFont(font);
			g.drawString(s1, w - sw1 - iw1 - sw2 - iw2 - 4 * dx, h - fontHeight
					- dFont + fontAscent);
			logo1.setDimension(new Dimension(iw1, logoHeight));
			logo1.paintIcon(null, g, w - iw1 - sw2 - iw2 - 3 * dx, h
					- logoHeight - dLogo);
			g.drawString(s2, w - sw2 - iw2 - 2 * dx, h - fontHeight - dFont
					+ fontAscent);
			logo2.setDimension(new Dimension(iw2, logoHeight));
			logo2.paintIcon(null, g, w - iw2 - dx, h - logoHeight - dLogo);
		}
	}

	public static class PickingPlugin<V extends Node> extends
			PickingGraphMousePlugin<V, Edge<V>> {

		private ICanvas<V> canvas;

		public PickingPlugin(ICanvas<V> canvas) {
			this.canvas = canvas;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			VisualizationViewer<V, Edge<V>> viewer = canvas.getViewer();

			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
				V node = viewer.getPickSupport().getVertex(
						viewer.getGraphLayout(), e.getX(), e.getY());
				Edge<V> edge = viewer.getPickSupport().getEdge(
						viewer.getGraphLayout(), e.getX(), e.getY());

				if (node != null) {
					EditableSinglePropertiesDialog dialog = new EditableSinglePropertiesDialog(
							e.getComponent(), node, canvas.getNodeSchema()
									.getMap());

					dialog.setVisible(true);

					if (dialog.isApproved()) {
						canvas.applyChanges();
					}
				} else if (edge != null) {
					if (!canvas.isJoinEdges()) {
						EditableSinglePropertiesDialog dialog = new EditableSinglePropertiesDialog(
								e.getComponent(), edge, canvas.getEdgeSchema()
										.getMap());

						dialog.setVisible(true);

						if (dialog.isApproved()) {
							canvas.applyChanges();
						}
					} else {
						SinglePropertiesDialog dialog = new SinglePropertiesDialog(
								e.getComponent(), edge, canvas.getEdgeSchema());

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
