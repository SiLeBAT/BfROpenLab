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
package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import de.bund.bfr.knime.openkrise.TracingUtils;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;

public class TracingGraphCanvas extends GraphCanvas implements TracingCanvas {

	private static final long serialVersionUID = 1L;

	private Tracing<GraphNode> tracing;

	private Map<Integer, MyDelivery> deliveries;

	public TracingGraphCanvas() {
		this(new ArrayList<GraphNode>(), new ArrayList<Edge<GraphNode>>(),
				new NodePropertySchema(), new EdgePropertySchema(),
				new LinkedHashMap<Integer, MyDelivery>());
	}

	public TracingGraphCanvas(List<GraphNode> nodes,
			List<Edge<GraphNode>> edges, NodePropertySchema nodeProperties,
			EdgePropertySchema edgeProperties,
			Map<Integer, MyDelivery> deliveries) {
		super(nodes, edges, nodeProperties, edgeProperties, true);
		this.deliveries = deliveries;
		tracing = new Tracing<>(this, nodeSaveMap, edgeSaveMap);
		viewer.prependPostRenderPaintable(new PostPaintable());
	}

	public Map<String, Double> getNodeWeights() {
		return tracing.getNodeWeights();
	}

	public void setNodeWeights(Map<String, Double> nodeWeights) {
		tracing.setNodeWeights(nodeWeights);
	}

	public Map<String, Double> getEdgeWeights() {
		return tracing.getEdgeWeights();
	}

	public void setEdgeWeights(Map<String, Double> edgeWeights) {
		tracing.setEdgeWeights(edgeWeights);
	}

	public Map<String, Boolean> getNodeCrossContaminations() {
		return tracing.getNodeCrossContaminations();
	}

	public void setNodeCrossContaminations(
			Map<String, Boolean> nodeCrossContaminations) {
		tracing.setNodeCrossContaminations(nodeCrossContaminations);
	}

	public Map<String, Boolean> getEdgeCrossContaminations() {
		return tracing.getEdgeCrossContaminations();
	}

	public void setEdgeCrossContaminations(
			Map<String, Boolean> edgeCrossContaminations) {
		tracing.setEdgeCrossContaminations(edgeCrossContaminations);
	}

	public Map<String, Boolean> getObservedNodes() {
		return tracing.getObservedNodes();
	}

	public void setObservedNodes(Map<String, Boolean> observedNodes) {
		tracing.setObservedNodes(observedNodes);
	}

	public Map<String, Boolean> getObservedEdges() {
		return tracing.getObservedEdges();
	}

	public void setObservedEdges(Map<String, Boolean> observedEdges) {
		tracing.setObservedEdges(observedEdges);
	}

	public boolean isEnforceTemporalOrder() {
		return tracing.isEnforceTemporalOrder();
	}

	public void setEnforceTemporalOrder(boolean enforceTemporalOrder) {
		tracing.setEnforceTemporalOrder(enforceTemporalOrder);
	}

	public boolean isShowForward() {
		return tracing.isShowForward();
	}

	public void setShowForward(boolean showForward) {
		tracing.setShowForward(showForward);
	}

	public boolean isPerformTracing() {
		return tracing.isPerformTracing();
	}

	public void setPerformTracing(boolean performTracing) {
		tracing.setPerformTracing(performTracing);
	}

	@Override
	public void nodePropertiesItemClicked() {
		EditablePropertiesDialog<GraphNode> dialog = EditablePropertiesDialog
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
			EditablePropertiesDialog<GraphNode> dialog = EditablePropertiesDialog
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

		EditablePropertiesDialog<GraphNode> dialog = EditablePropertiesDialog
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
	public void applyChanges() {
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
	protected String getNodeName() {
		return TracingUtils.NODE_NAME;
	}

	@Override
	protected String getEdgeName() {
		return TracingUtils.EDGE_NAME;
	}

	@Override
	protected String getNodesName() {
		return TracingUtils.NODES_NAME;
	}

	@Override
	protected String getEdgesName() {
		return TracingUtils.EDGES_NAME;
	}

	private void applyTracing() {
		if (!isPerformTracing()) {
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

		tracing.fillDeliveries(isEnforceTemporalOrder());

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
	}
}
