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
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import de.bund.bfr.knime.KnimeUtilities;
import de.bund.bfr.knime.gis.views.canvas.dialogs.ListFilterDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.PropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SinglePropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.layout.CircleLayout;
import de.bund.bfr.knime.gis.views.canvas.layout.FRLayout;
import de.bund.bfr.knime.gis.views.canvas.layout.GridLayout;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeStrokeTransformer;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;

/**
 * @author Christian Thoens
 */
public class GraphCanvas extends Canvas<GraphNode> {

	private static final long serialVersionUID = 1L;

	private static final String IS_META_NODE = "IsMetaNode";

	private List<GraphNode> allNodes;
	private List<Edge<GraphNode>> allEdges;
	private Set<GraphNode> nodes;
	private Set<Edge<GraphNode>> edges;
	private Map<Edge<GraphNode>, Set<Edge<GraphNode>>> joinMap;

	private Map<String, Map<String, Point2D>> collapsedNodes;
	private Map<String, GraphNode> nodeSaveMap;
	private Map<String, Edge<GraphNode>> edgeSaveMap;

	private String metaNodeProperty;

	public GraphCanvas(boolean allowCollapse) {
		this(new ArrayList<GraphNode>(), new ArrayList<Edge<GraphNode>>(),
				new LinkedHashMap<String, Class<?>>(),
				new LinkedHashMap<String, Class<?>>(), null, null, null, null,
				allowCollapse);
	}

	public GraphCanvas(List<GraphNode> nodes, List<Edge<GraphNode>> edges,
			Map<String, Class<?>> nodeProperties,
			Map<String, Class<?>> edgeProperties, String nodeIdProperty,
			String edgeIdProperty, String edgeFromProperty,
			String edgeToProperty, boolean allowCollapse) {
		super(nodeProperties, edgeProperties, nodeIdProperty, edgeIdProperty,
				edgeFromProperty, edgeToProperty);
		this.nodes = new LinkedHashSet<>();
		this.edges = new LinkedHashSet<>();

		Map<String, GraphNode> nodesById = new LinkedHashMap<>();

		for (GraphNode node : nodes) {
			GraphNode newNode = new GraphNode(node.getId(),
					new LinkedHashMap<>(node.getProperties()), node.getRegion());

			nodesById.put(node.getId(), newNode);
			this.nodes.add(newNode);
		}

		for (Edge<GraphNode> edge : edges) {
			this.edges.add(new Edge<>(edge.getId(), new LinkedHashMap<>(edge
					.getProperties()), nodesById.get(edge.getFrom().getId()),
					nodesById.get(edge.getTo().getId())));
		}

		allNodes = nodes;
		allEdges = edges;
		nodeSaveMap = CanvasUtilities.getElementsById(this.nodes);
		edgeSaveMap = CanvasUtilities.getElementsById(this.edges);
		joinMap = new LinkedHashMap<>();
		collapsedNodes = new LinkedHashMap<>();
		metaNodeProperty = KnimeUtilities.createNewValue(IS_META_NODE,
				getNodeProperties().keySet());
		getNodeProperties().put(metaNodeProperty, Boolean.class);

		setPopupMenu(new CanvasPopupMenu(true, true, allowCollapse));
		setOptionsPanel(new CanvasOptionsPanel(true, true, false));
		getViewer().getRenderContext().setVertexShapeTransformer(
				new NodeShapeTransformer<>(getNodeSize(),
						new LinkedHashMap<GraphNode, Double>()));
		getViewer().getGraphLayout().setGraph(
				CanvasUtilities.createGraph(this.nodes, this.edges));
		applyLayout(LayoutType.FR_LAYOUT, null);
	}

	@Override
	public Set<GraphNode> getNodes() {
		return nodes;
	}

	@Override
	public Set<Edge<GraphNode>> getEdges() {
		return edges;
	}

	public Map<String, GraphNode> getNodeSaveMap() {
		return nodeSaveMap;
	}

	public Map<String, Edge<GraphNode>> getEdgeSaveMap() {
		return edgeSaveMap;
	}

	public Map<String, Point2D> getNodePositions() {
		return getNodePositions(nodeSaveMap.values());
	}

	public void setNodePositions(Map<String, Point2D> nodePositions) {
		if (nodePositions.isEmpty()) {
			return;
		}

		int n = 0;

		for (GraphNode node : nodeSaveMap.values()) {
			if (nodePositions.get(node.getId()) == null) {
				n++;
			}
		}

		Layout<GraphNode, Edge<GraphNode>> layout = new StaticLayout<>(
				getViewer().getGraphLayout().getGraph());
		Point2D upperLeft = toGraphCoordinates(0, 0);
		Point2D upperRight = toGraphCoordinates(
				getViewer().getPreferredSize().width, 0);
		double x1 = upperLeft.getX();
		double x2 = upperRight.getX();
		double y = upperLeft.getY();
		int i = 0;

		for (GraphNode node : nodeSaveMap.values()) {
			Point2D pos = nodePositions.get(node.getId());

			if (pos != null) {
				layout.setLocation(node, pos);
			} else {
				double x = x1 + (double) i / (double) n * (x2 - x1);

				layout.setLocation(node, new Point2D.Double(x, y));
				i++;
			}
		}

		layout.setSize(getViewer().getSize());
		getViewer().setGraphLayout(layout);
	}

	public Map<String, Map<String, Point2D>> getCollapsedNodes() {
		return collapsedNodes;
	}

	public void setCollapsedNodes(
			Map<String, Map<String, Point2D>> collapsedNodes) {
		this.collapsedNodes = collapsedNodes;
		applyChanges();
	}

	@Override
	public void resetLayoutItemClicked() {
		setTransform(1.0, 1.0, 0.0, 0.0);
	}

	@Override
	public void layoutItemClicked(LayoutType layoutType) {
		applyLayout(layoutType, getSelectedNodes());
	}

	@Override
	public void nodeAllPropertiesItemClicked() {
		Set<GraphNode> picked = new LinkedHashSet<>(getSelectedNodes());
		Set<GraphNode> pickedAll = new LinkedHashSet<>();

		picked.retainAll(nodes);

		for (GraphNode node : picked) {
			if (collapsedNodes.containsKey(node.getId())) {
				for (String id : collapsedNodes.get(node.getId()).keySet()) {
					pickedAll.add(nodeSaveMap.get(id));
				}
			} else {
				pickedAll.add(node);
			}
		}

		PropertiesDialog<GraphNode> dialog = PropertiesDialog.createNodeDialog(
				this, pickedAll, getNodeProperties(), false);

		dialog.setVisible(true);
	}

	@Override
	public void collapseToNodeItemClicked() {
		Set<String> selectedIds = getSelectedNodeIds();

		for (String id : collapsedNodes.keySet()) {
			if (selectedIds.contains(id)) {
				JOptionPane.showMessageDialog(this,
						"Some of the selected nodes are already collapsed",
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		String newId = null;

		while (true) {
			newId = (String) JOptionPane.showInputDialog(this,
					"Specify ID for Meta Node", "Node ID",
					JOptionPane.QUESTION_MESSAGE, null, null, "");

			if (newId == null) {
				return;
			} else if (nodeSaveMap.containsKey(newId)) {
				JOptionPane.showMessageDialog(this,
						"ID already exists, please specify different ID",
						"Error", JOptionPane.ERROR_MESSAGE);
			} else {
				break;
			}
		}

		Map<String, Point2D> absPos = getNodePositions(CanvasUtilities
				.getElementsById(getViewer().getGraphLayout().getGraph()
						.getVertices(), selectedIds));
		Map<String, Point2D> relPos = new LinkedHashMap<>();
		Point2D center = CanvasUtilities.getCenter(absPos.values());

		for (String id : absPos.keySet()) {
			relPos.put(id,
					CanvasUtilities.substractPoints(absPos.get(id), center));
		}

		collapsedNodes.put(newId, relPos);
		applyChanges();
		setSelectedNodeIds(new LinkedHashSet<>(Arrays.asList(newId)));
	}

	@Override
	public void expandFromNodeItemClicked() {
		Set<String> selectedIds = getSelectedNodeIds();

		for (String id : selectedIds) {
			if (!collapsedNodes.keySet().contains(id)) {
				JOptionPane.showMessageDialog(this,
						"Some of the selected nodes are not collapsed",
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		Set<String> newIds = new LinkedHashSet<>();

		for (String id : selectedIds) {
			Map<String, Point2D> removed = collapsedNodes.remove(id);
			Point2D center = getViewer().getGraphLayout().transform(
					nodeSaveMap.remove(id));

			newIds.addAll(removed.keySet());

			for (String newId : removed.keySet()) {
				getViewer().getGraphLayout().setLocation(
						nodeSaveMap.get(newId),
						CanvasUtilities.addPoints(removed.get(newId), center));
			}
		}

		applyChanges();
		setSelectedNodeIds(newIds);
	}

	@Override
	public void collapseByPropertyItemClicked() {
		String[] properties = getNodeProperties().keySet().toArray(
				new String[0]);
		String result = (String) JOptionPane.showInputDialog(this,
				"Select Property for Collapse?", "Collapse by Property",
				JOptionPane.QUESTION_MESSAGE, null, properties, properties[0]);

		if (result == null) {
			return;
		}

		Map<String, Set<GraphNode>> nodesByProperty = new LinkedHashMap<>();

		for (String id : CanvasUtilities.getElementIds(allNodes)) {
			GraphNode node = nodeSaveMap.get(id);
			Object value = node.getProperties().get(result);

			if (value == null) {
				continue;
			}

			String stringValue = value.toString();

			if (!nodesByProperty.containsKey(stringValue)) {
				nodesByProperty
						.put(stringValue, new LinkedHashSet<GraphNode>());
			}

			nodesByProperty.get(stringValue).add(node);
		}

		List<String> propertyList = new ArrayList<>(nodesByProperty.keySet());

		Collections.sort(propertyList);

		ListFilterDialog<String> dialog = new ListFilterDialog<>(this,
				propertyList);

		dialog.setVisible(true);

		if (!dialog.isApproved()) {
			return;
		}

		nodesByProperty.keySet().retainAll(dialog.getFiltered());

		for (String id : collapsedNodes.keySet()) {
			nodeSaveMap.remove(id);
		}

		collapsedNodes.clear();

		for (String value : nodesByProperty.keySet()) {
			String newId = KnimeUtilities.createNewValue(value,
					nodeSaveMap.keySet());
			Map<String, Point2D> absPos = getNodePositions(nodesByProperty
					.get(value));
			Map<String, Point2D> relPos = new LinkedHashMap<>();
			Point2D center = CanvasUtilities.getCenter(absPos.values());

			for (String id : absPos.keySet()) {
				relPos.put(id,
						CanvasUtilities.substractPoints(absPos.get(id), center));
			}

			collapsedNodes.put(newId, relPos);
		}

		applyChanges();
		setSelectedNodeIds(collapsedNodes.keySet());
	}

	@Override
	public void clearCollapsedNodesItemClicked() {
		for (String id : collapsedNodes.keySet()) {
			Map<String, Point2D> removed = collapsedNodes.get(id);
			Point2D center = getViewer().getGraphLayout().transform(
					nodeSaveMap.remove(id));

			for (String newId : removed.keySet()) {
				getViewer().getGraphLayout().setLocation(
						nodeSaveMap.get(newId),
						CanvasUtilities.addPoints(removed.get(newId), center));
			}
		}

		collapsedNodes.clear();
		applyChanges();
		getViewer().getPickedVertexState().clear();
	}

	@Override
	public void borderAlphaChanged() {
	}

	@Override
	protected void applyTransform() {
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
							GraphNode node = getViewer().getPickSupport()
									.getVertex(getViewer().getGraphLayout(),
											e.getX(), e.getY());
							Edge<GraphNode> edge = getViewer().getPickSupport()
									.getEdge(getViewer().getGraphLayout(),
											e.getX(), e.getY());

							if (node != null) {
								SinglePropertiesDialog dialog = new SinglePropertiesDialog(
										e.getComponent(), node,
										getNodeProperties());

								dialog.setVisible(true);
							} else if (edge != null) {
								SinglePropertiesDialog dialog = new SinglePropertiesDialog(
										e.getComponent(), edge,
										getEdgeProperties());

								dialog.setVisible(true);
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
		applyHighlights();

		setSelectedNodeIds(selectedNodeIds);
		setSelectedEdgeIds(selectedEdgeIds);
		getViewer().repaint();
	}

	protected void applyNodeCollapse() {
		nodes = new LinkedHashSet<>();
		edges = new LinkedHashSet<>();

		Map<String, String> collapseTo = new LinkedHashMap<>();

		for (String to : collapsedNodes.keySet()) {
			for (String from : collapsedNodes.get(to).keySet()) {
				collapseTo.put(from, to);
			}
		}

		Map<String, GraphNode> nodesById = new LinkedHashMap<>();

		for (String id : CanvasUtilities.getElementIds(allNodes)) {
			if (!collapseTo.keySet().contains(id)) {
				GraphNode newNode = nodeSaveMap.get(id);

				nodes.add(newNode);
				nodesById.put(id, newNode);
			}
		}

		Set<GraphNode> metaNodes = new LinkedHashSet<>();

		for (String newId : collapsedNodes.keySet()) {
			GraphNode newNode = nodeSaveMap.get(newId);

			if (newNode == null) {
				Set<GraphNode> nodes = CanvasUtilities.getElementsById(
						nodeSaveMap, collapsedNodes.get(newId).keySet());
				Point2D pos = CanvasUtilities.getCenter(getNodePositions(nodes)
						.values());

				newNode = combineNodes(newId, nodes);
				getViewer().getGraphLayout().setLocation(newNode, pos);
				nodeSaveMap.put(newId, newNode);
			}

			nodes.add(newNode);
			nodesById.put(newNode.getId(), newNode);
			metaNodes.add(newNode);
		}

		for (Edge<GraphNode> edge : allEdges) {
			GraphNode from = nodesById.get(edge.getFrom().getId());
			GraphNode to = nodesById.get(edge.getTo().getId());

			if (from == null) {
				from = nodesById.get(collapseTo.get(edge.getFrom().getId()));
			}

			if (to == null) {
				to = nodesById.get(collapseTo.get(edge.getTo().getId()));
			}

			if (from == to && metaNodes.contains(from)) {
				continue;
			}

			Edge<GraphNode> newEdge = edgeSaveMap.get(edge.getId());

			if (!newEdge.getFrom().equals(from) || !newEdge.getTo().equals(to)) {
				newEdge = new Edge<>(newEdge.getId(), newEdge.getProperties(),
						from, to);
				newEdge.getProperties()
						.put(getEdgeFromProperty(), from.getId());
				newEdge.getProperties().put(getEdgeToProperty(), to.getId());
				edgeSaveMap.put(newEdge.getId(), newEdge);
			}

			edges.add(newEdge);
		}

		removeInvisibleElements(nodes, edges);

		if (isJoinEdges()) {
			joinMap = CanvasUtilities.joinEdges(edges, getEdgeProperties(),
					getEdgeIdProperty(), getEdgeFromProperty(),
					getEdgeToProperty(),
					CanvasUtilities.getElementIds(allEdges));
			edges = joinMap.keySet();
		} else {
			joinMap = new LinkedHashMap<>();
		}

		if (isSkipEdgelessNodes()) {
			CanvasUtilities.removeEdgelessNodes(nodes, edges);
		}

		getViewer().getGraphLayout().setGraph(
				CanvasUtilities.createGraph(nodes, edges));
		getViewer().getRenderContext().setVertexStrokeTransformer(
				new NodeStrokeTransformer<>(metaNodes));
		getViewer().getPickedVertexState().clear();
	}

	protected void applyHighlights() {
		CanvasUtilities.applyNodeHighlights(getViewer(),
				getNodeHighlightConditions(), getNodeSize());
		CanvasUtilities.applyEdgeHighlights(getViewer(),
				getEdgeHighlightConditions());
	}

	protected void removeInvisibleElements(Set<GraphNode> nodes,
			Set<Edge<GraphNode>> edges) {
		CanvasUtilities.removeInvisibleElements(nodes,
				getNodeHighlightConditions());
		CanvasUtilities.removeInvisibleElements(edges,
				getEdgeHighlightConditions());
		CanvasUtilities.removeNodelessEdges(edges, nodes);
	}

	@Override
	protected Map<Edge<GraphNode>, Set<Edge<GraphNode>>> getJoinMap() {
		return joinMap;
	}

	private void applyLayout(LayoutType layoutType, Set<GraphNode> selectedNodes) {
		Graph<GraphNode, Edge<GraphNode>> graph = getViewer().getGraphLayout()
				.getGraph();
		Layout<GraphNode, Edge<GraphNode>> layout = null;
		boolean nodesSelected = selectedNodes != null
				&& !selectedNodes.isEmpty();

		if (nodesSelected && layoutType == LayoutType.ISOM_LAYOUT) {
			if (JOptionPane.showConfirmDialog(this, layoutType
					+ " cannot be applied on a subset of nodes. Apply "
					+ layoutType + " on all nodes?", "Confirm",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				nodesSelected = false;
			} else {
				return;
			}
		}

		switch (layoutType) {
		case GRID_LAYOUT:
			layout = new GridLayout<>(graph);
			break;
		case CIRCLE_LAYOUT:
			layout = new CircleLayout<>(graph);
			break;
		case FR_LAYOUT:
			layout = new FRLayout<>(graph);
			break;
		case FR_LAYOUT_2:
			layout = new FRLayout2<>(graph);
			break;
		case ISOM_LAYOUT:
			layout = new ISOMLayout<>(graph);
			break;
		case KK_LAYOUT:
			layout = new KKLayout<>(graph);
			break;
		case SPRING_LAYOUT:
			layout = new SpringLayout<>(graph);
			break;
		case SPRING_LAYOUT_2:
			layout = new SpringLayout2<>(graph);
			break;
		}

		if (nodesSelected) {
			Point2D move = new Point2D.Double(getTranslationX() / getScaleX(),
					getTranslationY() / getScaleY());

			for (GraphNode node : nodes) {
				if (!selectedNodes.contains(node)) {
					layout.setLocation(node, CanvasUtilities.addPoints(
							getViewer().getGraphLayout().transform(node), move));
					layout.lock(node, true);
				}
			}

			layout.setSize(new Dimension(
					(int) (getViewer().getSize().width / getScaleX()),
					(int) (getViewer().getSize().height / getScaleY())));

			for (GraphNode node : nodes) {
				if (!selectedNodes.contains(node)) {
					layout.setLocation(node, CanvasUtilities.addPoints(
							getViewer().getGraphLayout().transform(node), move));
					layout.lock(node, true);
				}
			}

			setTransform(getScaleX(), getScaleY(), 0.0, 0.0);
		} else {
			layout.setSize(getViewer().getSize());
			setTransform(1.0, 1.0, 0.0, 0.0);
		}

		getViewer().setGraphLayout(layout);
	}

	private Map<String, Point2D> getNodePositions(Collection<GraphNode> nodes) {
		Map<String, Point2D> map = new LinkedHashMap<>();
		Layout<GraphNode, Edge<GraphNode>> layout = getViewer()
				.getGraphLayout();

		for (GraphNode node : nodes) {
			Point2D pos = layout.transform(node);

			if (pos != null) {
				map.put(node.getId(), pos);
			}
		}

		return map;
	}

	private GraphNode combineNodes(String id, Collection<GraphNode> nodes) {
		Map<String, Object> properties = new LinkedHashMap<>();

		for (GraphNode node : nodes) {
			CanvasUtilities.addMapToMap(properties, getNodeProperties(),
					node.getProperties());
		}

		if (getNodeIdProperty() != null) {
			properties.put(getNodeIdProperty(), id);
		}

		properties.put(metaNodeProperty, true);

		return new GraphNode(id, properties, null);
	}
}
