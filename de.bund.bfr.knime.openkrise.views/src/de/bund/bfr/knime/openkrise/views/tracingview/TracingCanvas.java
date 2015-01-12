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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.GraphMouse;
import de.bund.bfr.knime.gis.views.canvas.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightConditionChecker;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightListDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SinglePropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition;
import de.bund.bfr.knime.openkrise.MyDelivery;
import de.bund.bfr.knime.openkrise.MyNewTracing;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.views.BfrLogo;
import de.bund.bfr.knime.openkrise.views.FoodChainLabLogo;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;

public class TracingCanvas extends GraphCanvas {

	private static final long serialVersionUID = 1L;

	private static boolean DEFAULT_ENFORCE_TEMPORAL_ORDER = false;
	private static boolean DEFAULT_SHOW_FORWARD = false;
	private static boolean DEFAULT_PERFORM_TRACING = true;

	private Map<Integer, MyDelivery> deliveries;
	private boolean performTracing;

	private JCheckBox enforceTemporalOrderBox;
	private JCheckBox showForwardBox;
	private String label;
	private JTextField labelField;
	private JButton labelButton;

	public TracingCanvas() {
		this(new ArrayList<GraphNode>(), new ArrayList<Edge<GraphNode>>(),
				new NodePropertySchema(), new EdgePropertySchema(),
				new LinkedHashMap<Integer, MyDelivery>());
	}

	public TracingCanvas(List<GraphNode> nodes, List<Edge<GraphNode>> edges,
			NodePropertySchema nodeProperties,
			EdgePropertySchema edgeProperties,
			Map<Integer, MyDelivery> deliveries) {
		super(nodes, edges, nodeProperties, edgeProperties, true);
		this.deliveries = deliveries;
		performTracing = DEFAULT_PERFORM_TRACING;

		updatePopupMenuAndOptionsPanel();
		viewer.prependPostRenderPaintable(new PostPaintable());
	}

	public Map<String, Double> getNodeWeights() {
		Map<String, Double> nodeWeights = new LinkedHashMap<>();

		for (GraphNode node : nodeSaveMap.values()) {
			nodeWeights.put(node.getId(),
					(Double) node.getProperties().get(TracingColumns.WEIGHT));
		}

		return nodeWeights;
	}

	public void setNodeWeights(Map<String, Double> nodeWeights) {
		for (GraphNode node : nodeSaveMap.values()) {
			if (nodeWeights.containsKey(node.getId())) {
				node.getProperties().put(TracingColumns.WEIGHT,
						nullToZero(nodeWeights.get(node.getId())));
			}
		}

		if (performTracing) {
			applyChanges();
		}
	}

	public Map<String, Double> getEdgeWeights() {
		Map<String, Double> edgeWeights = new LinkedHashMap<>();

		for (Edge<GraphNode> edge : edgeSaveMap.values()) {
			edgeWeights.put(edge.getId(),
					(Double) edge.getProperties().get(TracingColumns.WEIGHT));
		}

		return edgeWeights;
	}

	public void setEdgeWeights(Map<String, Double> edgeWeights) {
		for (Edge<GraphNode> edge : edgeSaveMap.values()) {
			if (edgeWeights.containsKey(edge.getId())) {
				edge.getProperties().put(TracingColumns.WEIGHT,
						nullToZero(edgeWeights.get(edge.getId())));
			}
		}

		if (performTracing) {
			applyChanges();
		}
	}

	public Map<String, Boolean> getNodeCrossContaminations() {
		Map<String, Boolean> nodeCrossContaminations = new LinkedHashMap<>();

		for (GraphNode node : nodeSaveMap.values()) {
			nodeCrossContaminations.put(node.getId(), (Boolean) node
					.getProperties().get(TracingColumns.CROSS_CONTAMINATION));
		}

		return nodeCrossContaminations;
	}

	public void setNodeCrossContaminations(
			Map<String, Boolean> nodeCrossContaminations) {
		for (GraphNode node : nodeSaveMap.values()) {
			if (nodeCrossContaminations.containsKey(node.getId())) {
				node.getProperties().put(TracingColumns.CROSS_CONTAMINATION,
						nullToFalse(nodeCrossContaminations.get(node.getId())));
			}
		}

		if (performTracing) {
			applyChanges();
		}
	}

	public Map<String, Boolean> getEdgeCrossContaminations() {
		Map<String, Boolean> edgeCrossContaminations = new LinkedHashMap<>();

		for (Edge<GraphNode> edge : edgeSaveMap.values()) {
			edgeCrossContaminations.put(edge.getId(), (Boolean) edge
					.getProperties().get(TracingColumns.CROSS_CONTAMINATION));
		}

		return edgeCrossContaminations;
	}

	public void setEdgeCrossContaminations(
			Map<String, Boolean> edgeCrossContaminations) {
		for (Edge<GraphNode> edge : edgeSaveMap.values()) {
			if (edgeCrossContaminations.containsKey(edge.getId())) {
				edge.getProperties().put(TracingColumns.CROSS_CONTAMINATION,
						nullToFalse(edgeCrossContaminations.get(edge.getId())));
			}
		}

		if (performTracing) {
			applyChanges();
		}
	}

	public Map<String, Boolean> getObservedNodes() {
		Map<String, Boolean> observedNodes = new LinkedHashMap<>();

		for (GraphNode node : nodeSaveMap.values()) {
			observedNodes
					.put(node.getId(),
							(Boolean) node.getProperties().get(
									TracingColumns.OBSERVED));
		}

		return observedNodes;
	}

	public void setObservedNodes(Map<String, Boolean> observedNodes) {
		for (GraphNode node : nodeSaveMap.values()) {
			if (observedNodes.containsKey(node.getId())) {
				node.getProperties().put(TracingColumns.OBSERVED,
						nullToFalse(observedNodes.get(node.getId())));
			}
		}

		if (performTracing) {
			applyChanges();
		}
	}

	public Map<String, Boolean> getObservedEdges() {
		Map<String, Boolean> observedEdges = new LinkedHashMap<>();

		for (Edge<GraphNode> edge : edgeSaveMap.values()) {
			observedEdges
					.put(edge.getId(),
							(Boolean) edge.getProperties().get(
									TracingColumns.OBSERVED));
		}

		return observedEdges;
	}

	public void setObservedEdges(Map<String, Boolean> observedEdges) {
		for (Edge<GraphNode> edge : edgeSaveMap.values()) {
			if (observedEdges.containsKey(edge.getId())) {
				edge.getProperties().put(TracingColumns.OBSERVED,
						nullToFalse(observedEdges.get(edge.getId())));
			}
		}

		if (performTracing) {
			applyChanges();
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
		labelField.setText(label != null ? label : "");
		viewer.repaint();
	}

	public boolean isPerformTracing() {
		return performTracing;
	}

	public void setPerformTracing(boolean performTracing) {
		this.performTracing = performTracing;

		if (performTracing) {
			applyChanges();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		if (e.getSource() == labelButton) {
			setLabel(labelField.getText());
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		super.itemStateChanged(e);

		if (e.getSource() == enforceTemporalOrderBox) {
			if (performTracing) {
				applyChanges();
			}
		} else if (e.getSource() == showForwardBox) {
			if (performTracing) {
				applyChanges();
			}
		}
	}

	@Override
	public void nodePropertiesItemClicked() {
		EditablePropertiesDialog dialog = EditablePropertiesDialog
				.createNodeDialog(this, getSelectedNodes(), nodeSchema, true);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			applyChanges();
		}
	}

	@Override
	public void edgePropertiesItemClicked() {
		if (isJoinEdges()) {
			super.edgePropertiesItemClicked();
		} else {
			EditablePropertiesDialog dialog = EditablePropertiesDialog
					.createEdgeDialog(this, getSelectedEdges(), edgeSchema,
							true);

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				applyChanges();
			}
		}
	}

	@Override
	public void edgeAllPropertiesItemClicked() {
		Set<Edge<GraphNode>> allPicked = new LinkedHashSet<>();

		for (Edge<GraphNode> p : getSelectedEdges()) {
			if (joinMap.containsKey(p)) {
				allPicked.addAll(joinMap.get(p));
			} else {
				allPicked.add(p);
			}
		}

		EditablePropertiesDialog dialog = EditablePropertiesDialog
				.createEdgeDialog(this, allPicked, edgeSchema, false);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			applyChanges();
		}
	}

	@Override
	public VisualizationImageServer<GraphNode, Edge<GraphNode>> getVisualizationServer(
			boolean toSvg) {
		VisualizationImageServer<GraphNode, Edge<GraphNode>> server = super
				.getVisualizationServer(toSvg);

		server.prependPostRenderPaintable(new PostPaintable());

		return server;
	}

	@Override
	protected GraphMouse<GraphNode, Edge<GraphNode>> createMouseModel(
			Mode editingMode) {
		return new GraphMouse<>(
				new PickingGraphMousePlugin<GraphNode, Edge<GraphNode>>() {

					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.getButton() == MouseEvent.BUTTON1
								&& e.getClickCount() == 2) {
							GraphNode node = viewer.getPickSupport()
									.getVertex(viewer.getGraphLayout(),
											e.getX(), e.getY());
							Edge<GraphNode> edge = viewer.getPickSupport()
									.getEdge(viewer.getGraphLayout(), e.getX(),
											e.getY());

							if (node != null) {
								EditableSinglePropertiesDialog dialog = new EditableSinglePropertiesDialog(
										e.getComponent(), node,
										nodeSchema.getMap());

								dialog.setVisible(true);

								if (dialog.isApproved()) {
									applyChanges();
								}
							} else if (edge != null) {
								if (!isJoinEdges()) {
									EditableSinglePropertiesDialog dialog = new EditableSinglePropertiesDialog(
											e.getComponent(), edge,
											edgeSchema.getMap());

									dialog.setVisible(true);

									if (dialog.isApproved()) {
										applyChanges();
									}
								} else {
									SinglePropertiesDialog dialog = new SinglePropertiesDialog(
											e.getComponent(), edge,
											edgeSchema.getMap());

									dialog.setVisible(true);
								}
							}
						}
					}
				}, editingMode);
	}

	@Override
	protected void applyChanges() {
		Set<String> selectedNodeIds = getSelectedNodeIds();
		Set<String> selectedEdgeIds = getSelectedEdgeIds();

		applyNodeCollapse();
		applyInvisibility();
		applyJoinEdgesAndSkipEdgeless();
		viewer.getGraphLayout().setGraph(CanvasUtils.createGraph(nodes, edges));
		applyHighlights();
		applyTracing();
		applyHighlights();

		setSelectedNodeIds(selectedNodeIds);
		setSelectedEdgeIds(selectedEdgeIds);
		viewer.repaint();
	}

	@Override
	protected HighlightListDialog openNodeHighlightDialog() {
		HighlightListDialog dialog = super.openNodeHighlightDialog();

		dialog.addChecker(new HighlightChecker());

		return dialog;
	}

	@Override
	protected HighlightListDialog openEdgeHighlightDialog() {
		HighlightListDialog dialog = super.openEdgeHighlightDialog();

		dialog.addChecker(new HighlightChecker());

		return dialog;
	}

	@Override
	protected void applyInvisibility() {
		if (!isShowForward()) {
			super.applyInvisibility();
			return;
		}

		MyNewTracing tracingWithCC = createTracing(edges, true);
		MyNewTracing tracingWithoutCC = createTracing(edges, false);
		Set<Edge<GraphNode>> removedEdges = new LinkedHashSet<>();

		CanvasUtils.removeInvisibleElements(nodes, nodeHighlightConditions);
		removedEdges.addAll(CanvasUtils.removeInvisibleElements(edges,
				edgeHighlightConditions));
		removedEdges.addAll(CanvasUtils.removeNodelessEdges(edges, nodes));

		Set<Integer> forwardEdges = new LinkedHashSet<>();

		for (Edge<GraphNode> edge : edges) {
			forwardEdges.addAll(tracingWithCC
					.getForwardDeliveries2(getIntegerId(edge)));
		}

		for (Edge<GraphNode> edge : edges) {
			forwardEdges.removeAll(tracingWithoutCC
					.getForwardDeliveries2(getIntegerId(edge)));
		}

		for (Edge<GraphNode> edge : removedEdges) {
			if (forwardEdges.contains(getIntegerId(edge))) {
				nodes.add(edge.getFrom());
				nodes.add(edge.getTo());
				edges.add(edge);
			}
		}
	}

	@Override
	protected void applyNameChanges() {
		super.applyNameChanges();
		updatePopupMenuAndOptionsPanel();
	}

	private void updatePopupMenuAndOptionsPanel() {
		enforceTemporalOrderBox = new JCheckBox("Activate");
		enforceTemporalOrderBox.setSelected(DEFAULT_ENFORCE_TEMPORAL_ORDER);
		enforceTemporalOrderBox.addItemListener(this);

		showForwardBox = new JCheckBox("Activate");
		showForwardBox.setSelected(DEFAULT_SHOW_FORWARD);
		showForwardBox.addItemListener(this);

		label = new String();
		labelField = new JTextField(label, 20);
		labelButton = new JButton("Apply");
		labelButton.addActionListener(this);

		getOptionsPanel().addOption("Enforce Temporal Order",
				enforceTemporalOrderBox);
		getOptionsPanel().addOption("Show Cross Contaminated " + edgesName,
				showForwardBox);
		getOptionsPanel().addOption("Label", labelField, labelButton);
	}

	private void applyTracing() {
		if (!performTracing) {
			return;
		}

		Set<Edge<GraphNode>> edges = new LinkedHashSet<>();

		if (!isJoinEdges()) {
			edges.addAll(this.edges);
		} else {
			for (Edge<GraphNode> edge : this.edges) {
				edges.addAll(joinMap.get(edge));
			}
		}

		MyNewTracing tracing = createTracing(edges, true);

		Set<Integer> backwardNodes = new LinkedHashSet<>();
		Set<Integer> forwardNodes = new LinkedHashSet<>();
		Set<Integer> backwardEdges = new LinkedHashSet<>();
		Set<Integer> forwardEdges = new LinkedHashSet<>();

		for (GraphNode node : nodes) {
			int id = getIntegerId(node, getCollapsedNodes());
			Boolean value = (Boolean) node.getProperties().get(
					TracingColumns.OBSERVED);

			if (value != null && value == true) {
				backwardNodes.addAll(tracing.getBackwardStations(id));
				forwardNodes.addAll(tracing.getForwardStations(id));
				backwardEdges.addAll(tracing.getBackwardDeliveries(id));
				forwardEdges.addAll(tracing.getForwardDeliveries(id));
			}
		}

		for (Edge<GraphNode> edge : edges) {
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

		for (GraphNode node : nodes) {
			int id = getIntegerId(node, getCollapsedNodes());

			node.getProperties().put(TracingColumns.SCORE,
					tracing.getStationScore(id));
			node.getProperties().put(TracingColumns.BACKWARD,
					backwardNodes.contains(id));
			node.getProperties().put(TracingColumns.FORWARD,
					forwardNodes.contains(id));
		}

		for (Edge<GraphNode> edge : edges) {
			int id = Integer.parseInt(edge.getId());

			edge.getProperties().put(TracingColumns.SCORE,
					tracing.getDeliveryScore(id));
			edge.getProperties().put(TracingColumns.BACKWARD,
					backwardEdges.contains(id));
			edge.getProperties().put(TracingColumns.FORWARD,
					forwardEdges.contains(id));
		}

		if (isJoinEdges()) {
			for (Edge<GraphNode> edge : edges) {
				edge.getProperties().put(TracingColumns.OBSERVED, null);
				edge.getProperties().put(TracingColumns.SCORE, null);
				edge.getProperties().put(TracingColumns.BACKWARD, null);
				edge.getProperties().put(TracingColumns.FORWARD, null);
			}
		}
	}

	private MyNewTracing createTracing(Set<Edge<GraphNode>> edges,
			boolean useCrossContamination) {
		HashMap<Integer, MyDelivery> activeDeliveries = new HashMap<>();

		for (Edge<GraphNode> id : edges) {
			activeDeliveries.put(getIntegerId(id),
					deliveries.get(getIntegerId(id)));
		}

		MyNewTracing tracing = new MyNewTracing(activeDeliveries,
				new LinkedHashMap<Integer, Double>(),
				new LinkedHashMap<Integer, Double>(),
				new LinkedHashSet<Integer>(), new LinkedHashSet<Integer>(), 0.0);

		for (String id : getCollapsedNodes().keySet()) {
			Set<String> nodeIdStrings = getCollapsedNodes().get(id);
			Set<Integer> nodeIds = new LinkedHashSet<>();

			for (String idString : nodeIdStrings) {
				nodeIds.add(Integer.parseInt(idString));
			}

			tracing.mergeStations(nodeIds, createId(nodeIdStrings));
		}

		for (GraphNode node : nodes) {
			int id = getIntegerId(node, getCollapsedNodes());
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

		for (Edge<GraphNode> edge : edges) {
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

		tracing.fillDeliveries(enforceTemporalOrderBox.isSelected());

		return tracing;
	}

	private static int getIntegerId(GraphNode node,
			Map<String, Set<String>> collapsedNodes) {
		if (collapsedNodes.containsKey(node.getId())) {
			return createId(collapsedNodes.get(node.getId()));
		} else {
			return Integer.parseInt(node.getId());
		}
	}

	private static int getIntegerId(Edge<GraphNode> edge) {
		return Integer.parseInt(edge.getId());
	}

	private static int createId(Collection<String> c) {
		return KnimeUtils.listToString(new ArrayList<>(c)).hashCode();
	}

	private static double nullToZero(Double value) {
		return value == null ? 0.0 : value;
	}

	private static boolean nullToFalse(Boolean value) {
		return value == null ? false : value;
	}

	private class HighlightChecker implements HighlightConditionChecker {

		@Override
		public String findError(HighlightCondition condition) {
			List<String> tracingColumns = Arrays.asList(TracingColumns.SCORE,
					TracingColumns.BACKWARD, TracingColumns.FORWARD);
			String error = "The following columns cannot be used with \"Invisible\" option:\n";

			for (String column : tracingColumns) {
				error += column + ", ";
			}

			error = error.substring(0, error.length() - 2);

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

	private class PostPaintable implements Paintable {

		@Override
		public boolean useTransform() {
			return false;
		}

		@Override
		public void paint(Graphics g) {
			paintLogo(g);
			paintLabel(g);
		}

		private void paintLogo(Graphics g) {
			int w = getCanvasSize().width;
			int h = getCanvasSize().height;

			Font font = new Font("Default", Font.BOLD, 20);

			int height = 28;
			int fontHeight = g.getFontMetrics(font).getHeight();
			int fontAscent = g.getFontMetrics(font).getAscent();
			int dFont = (height - fontHeight) / 2;
			int logoHeight = 18;
			int dLogo = (height - logoHeight) / 2;

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

			g.setColor(new Color(230, 230, 230));
			g.fillRect(w - sw1 - iw1 - sw2 - iw2 - 5 * dx, h - height, sw1
					+ iw1 + sw2 + iw2 + 5 * dx, height);
			g.setColor(Color.BLACK);
			g.drawRect(w - sw1 - iw1 - sw2 - iw2 - 5 * dx, h - height, sw1
					+ iw1 + sw2 + iw2 + 5 * dx, height);
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

		private void paintLabel(Graphics g) {
			if (label == null || label.isEmpty()) {
				return;
			}

			int w = getCanvasSize().width;
			Font font = new Font("Default", Font.BOLD, 10);
			int fontHeight = g.getFontMetrics(font).getHeight();
			int fontAscent = g.getFontMetrics(font).getAscent();
			int dy = 2;

			int dx = 5;
			int sw = (int) font.getStringBounds(label,
					((Graphics2D) g).getFontRenderContext()).getWidth();

			g.setColor(new Color(230, 230, 230));
			g.fillRect(w - sw - 2 * dx, -1, sw + 2 * dx, fontHeight + 2 * dy);
			g.setColor(Color.BLACK);
			g.drawRect(w - sw - 2 * dx, -1, sw + 2 * dx, fontHeight + 2 * dy);
			g.setFont(font);
			g.drawString(label, w - sw - dx, dy + fontAscent);
		}
	}
}
