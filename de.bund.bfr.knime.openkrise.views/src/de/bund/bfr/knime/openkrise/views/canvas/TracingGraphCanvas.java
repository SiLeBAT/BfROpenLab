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

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
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
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightListDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SinglePropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.openkrise.MyDelivery;
import de.bund.bfr.knime.openkrise.MyNewTracing;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.TracingUtils;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;

public class TracingGraphCanvas extends GraphCanvas implements
		TracingCanvas<GraphNode> {

	private static final long serialVersionUID = 1L;

	private Tracing<GraphNode> tracing;

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
		tracing = new Tracing<>(this, nodeSaveMap, edgeSaveMap, deliveries);
		viewer.prependPostRenderPaintable(new Tracing.PostPaintable(this));
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

		server.prependPostRenderPaintable(new Tracing.PostPaintable(this));

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

		dialog.addChecker(new Tracing.HighlightChecker());

		return dialog;
	}

	@Override
	protected HighlightListDialog openEdgeHighlightDialog() {
		HighlightListDialog dialog = super.openEdgeHighlightDialog();

		dialog.addChecker(new Tracing.HighlightChecker());

		return dialog;
	}

	@Override
	protected void applyInvisibility() {
		if (!isShowForward()) {
			super.applyInvisibility();
			return;
		}

		MyNewTracing tracingWithCC = tracing.createTracing(edges, true);
		MyNewTracing tracingWithoutCC = tracing.createTracing(edges, false);
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

		MyNewTracing tracing = this.tracing.createTracing(edges, true);

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
}
