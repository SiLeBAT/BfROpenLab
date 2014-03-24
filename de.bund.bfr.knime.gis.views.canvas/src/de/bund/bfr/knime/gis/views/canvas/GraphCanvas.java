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

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightListDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SingleElementPropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeStrokeTransformer;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;

/**
 * @author Christian Thoens
 */
public class GraphCanvas extends Canvas<GraphNode> {

	public static final String CIRCLE_LAYOUT = "Circle Layout";
	public static final String FR_LAYOUT = "FR Layout";
	public static final String FR_LAYOUT_2 = "FR Layout 2";
	public static final String ISOM_LAYOUT = "ISOM Layout";
	public static final String KK_LAYOUT = "KK Layout";
	public static final String SPRING_LAYOUT = "Spring Layout";
	public static final String SPRING_LAYOUT_2 = "Spring Layout 2";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_LAYOUT = FR_LAYOUT;
	private static final int DEFAULT_NODESIZE = 10;
	private static final String IS_META_NODE = "IsMetaNode";

	private String layoutType;
	private int nodeSize;

	private List<GraphNode> allNodes;
	private List<Edge<GraphNode>> allEdges;
	private Set<GraphNode> nodes;
	private Set<Edge<GraphNode>> edges;
	private Set<GraphNode> invisibleNodes;
	private Set<Edge<GraphNode>> invisibleEdges;
	private Map<Edge<GraphNode>, Set<Edge<GraphNode>>> joinMap;

	private Map<String, Map<String, Point2D>> collapsedNodes;

	private JComboBox<String> layoutBox;
	private JButton layoutButton;
	private JTextField nodeSizeField;
	private JButton nodeSizeButton;

	private String metaNodeProperty;
	private Random random;

	public GraphCanvas() {
		this(new ArrayList<GraphNode>(), new ArrayList<Edge<GraphNode>>(),
				new LinkedHashMap<String, Class<?>>(),
				new LinkedHashMap<String, Class<?>>(), null, null, null, null);
	}

	public GraphCanvas(List<GraphNode> nodes, List<Edge<GraphNode>> edges,
			Map<String, Class<?>> nodeProperties,
			Map<String, Class<?>> edgeProperties, String nodeIdProperty,
			String edgeIdProperty, String edgeFromProperty,
			String edgeToProperty) {
		super(nodeProperties, edgeProperties, nodeIdProperty, edgeIdProperty,
				edgeFromProperty, edgeToProperty);
		setAllowEdges(true);
		this.allNodes = nodes;
		this.allEdges = edges;
		this.nodes = new LinkedHashSet<GraphNode>(allNodes);
		this.edges = new LinkedHashSet<Edge<GraphNode>>(allEdges);
		layoutType = DEFAULT_LAYOUT;
		nodeSize = DEFAULT_NODESIZE;
		invisibleNodes = new LinkedHashSet<GraphNode>();
		invisibleEdges = new LinkedHashSet<Edge<GraphNode>>();
		joinMap = new LinkedHashMap<Edge<GraphNode>, Set<Edge<GraphNode>>>();
		collapsedNodes = new LinkedHashMap<String, Map<String, Point2D>>();
		metaNodeProperty = CanvasUtilities.createNewProperty(IS_META_NODE,
				getNodeProperties());
		getNodeProperties().put(metaNodeProperty, Boolean.class);
		random = new Random();

		layoutBox = new JComboBox<String>(new String[] { CIRCLE_LAYOUT,
				FR_LAYOUT, FR_LAYOUT_2, ISOM_LAYOUT, KK_LAYOUT, SPRING_LAYOUT,
				SPRING_LAYOUT_2 });
		layoutBox.setSelectedItem(layoutType);
		layoutButton = new JButton("Apply");
		layoutButton.addActionListener(this);
		addOptionsItem("Layout", layoutBox, layoutButton);
		nodeSizeField = new JTextField("" + nodeSize, 5);
		nodeSizeButton = new JButton("Apply");
		nodeSizeButton.addActionListener(this);
		addOptionsItem("Node Size", nodeSizeField, nodeSizeButton);

		getViewer().getRenderContext().setVertexShapeTransformer(
				new NodeShapeTransformer<GraphNode>(nodeSize,
						new LinkedHashMap<GraphNode, Double>()));
		applyLayout();
	}

	public Set<GraphNode> getNodes() {
		return nodes;
	}

	public Set<Edge<GraphNode>> getEdges() {
		return edges;
	}

	public List<GraphNode> getAllNodes() {
		return allNodes;
	}

	public List<Edge<GraphNode>> getAllEdges() {
		return allEdges;
	}

	public Collection<GraphNode> getVisibleNodes() {
		return getViewer().getGraphLayout().getGraph().getVertices();
	}

	public Collection<Edge<GraphNode>> getVisibleEdges() {
		return getViewer().getGraphLayout().getGraph().getEdges();
	}

	public Map<Edge<GraphNode>, Set<Edge<GraphNode>>> getJoinMap() {
		return joinMap;
	}

	public Map<String, Point2D> getNodePositions() {
		Map<String, Point2D> map = new LinkedHashMap<String, Point2D>();
		Layout<GraphNode, Edge<GraphNode>> layout = getViewer()
				.getGraphLayout();

		for (GraphNode node : nodes) {
			Point2D pos = layout.transform(node);

			if (pos != null) {
				if (collapsedNodes.containsKey(node.getId())) {
					Map<String, Point2D> absPos = new LinkedHashMap<String, Point2D>();
					Map<String, Point2D> relPos = collapsedNodes.get(node
							.getId());

					for (String id : relPos.keySet()) {
						absPos.put(id,
								CanvasUtilities.addPoints(relPos.get(id), pos));
					}

					map.putAll(absPos);
				} else {
					map.put(node.getId(), pos);
				}
			}
		}

		return map;
	}

	public void setNodePositions(Map<String, Point2D> nodePositions) {
		if (nodePositions.isEmpty()) {
			return;
		}

		Graph<GraphNode, Edge<GraphNode>> graph = createGraph();
		Layout<GraphNode, Edge<GraphNode>> layout = new StaticLayout<GraphNode, Edge<GraphNode>>(
				graph);
		Map<String, GraphNode> nodesById = new LinkedHashMap<String, GraphNode>();

		for (GraphNode node : nodes) {
			nodesById.put(node.getId(), node);
		}

		for (String id : nodePositions.keySet()) {
			layout.setLocation(nodesById.get(id), nodePositions.get(id));
		}

		layout.setSize(getViewer().getSize());
		setTransform(1.0, 1.0, 0.0, 0.0);
		getViewer().setGraphLayout(layout);
	}

	public String getLayoutType() {
		return layoutType;
	}

	public void setLayoutType(String layoutType) {
		this.layoutType = layoutType;
		applyLayout();
	}

	public Map<String, Map<String, Point2D>> getCollapsedNodes() {
		return collapsedNodes;
	}

	public void setCollapsedNodes(
			Map<String, Map<String, Point2D>> collapsedNodes) {
		this.collapsedNodes = collapsedNodes;
		applyNodeCollapse();
	}

	public int getNodeSize() {
		return nodeSize;
	}

	public void setNodeSize(int nodeSize) {
		this.nodeSize = nodeSize;
		nodeSizeField.setText(nodeSize + "");
		applyHighlights();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		if (e.getSource() == layoutButton) {
			layoutType = (String) layoutBox.getSelectedItem();
			applyLayout();
		} else if (e.getSource() == nodeSizeButton) {
			try {
				nodeSize = Integer.parseInt(nodeSizeField.getText());
				applyHighlights();
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this,
						"Node Size must be Integer Value", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	protected void resetLayout() {
		setTransform(1.0, 1.0, 0.0, 0.0);
	}

	@Override
	protected HighlightListDialog openNodeHighlightDialog() {
		return new HighlightListDialog(this, getNodeProperties(), true, true,
				true, getNodeHighlightConditions());
	}

	@Override
	protected HighlightListDialog openEdgeHighlightDialog() {
		return new HighlightListDialog(this, getEdgeProperties(), true, true,
				true, getEdgeHighlightConditions());
	}

	@Override
	protected boolean applyHighlights() {
		boolean changed1 = CanvasUtilities.applyNodeHighlights(getViewer(),
				nodes, edges, invisibleNodes, invisibleEdges,
				getNodeHighlightConditions(), nodeSize, false);
		boolean changed2 = CanvasUtilities.applyEdgeHighlights(getViewer(),
				edges, invisibleEdges, getEdgeHighlightConditions());

		return changed1 || changed2;
	}

	@Override
	protected void applyTransform() {
	}

	@Override
	protected void collapseToNode() {
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
		Set<String> allIds = new LinkedHashSet<String>();

		allIds.addAll(CanvasUtilities.getElementIds(allNodes));
		allIds.addAll(collapsedNodes.keySet());

		while (newId == null) {
			String id = random.nextInt() + "";

			if (!allIds.contains(id)) {
				newId = id;
			}
		}

		Map<String, Point2D> absPos = getNodePositions(CanvasUtilities
				.getElementsById(getViewer().getGraphLayout().getGraph()
						.getVertices(), selectedIds));
		Map<String, Point2D> relPos = new LinkedHashMap<String, Point2D>();
		Point2D center = CanvasUtilities.getCenter(absPos.values());

		for (String id : absPos.keySet()) {
			relPos.put(id,
					CanvasUtilities.substractPoints(absPos.get(id), center));
		}

		collapsedNodes.put(newId, relPos);
		applyNodeCollapse();
		setSelectedNodeIds(new LinkedHashSet<String>(Arrays.asList(newId)));
	}

	@Override
	protected void expandFromNode() {
		Set<String> selectedIds = getSelectedNodeIds();

		for (String id : selectedIds) {
			if (!collapsedNodes.keySet().contains(id)) {
				JOptionPane.showMessageDialog(this,
						"Some of the selected nodes are not collapsed",
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		Set<String> newIds = new LinkedHashSet<String>();
		Set<GraphNode> oldAndNewNodes = new LinkedHashSet<GraphNode>();

		oldAndNewNodes.addAll(allNodes);
		oldAndNewNodes.addAll(nodes);

		Map<String, GraphNode> nodesById = CanvasUtilities
				.getElementsById(oldAndNewNodes);

		for (String id : selectedIds) {
			Map<String, Point2D> removed = collapsedNodes.remove(id);
			Point2D center = getViewer().getGraphLayout().transform(
					nodesById.get(id));

			newIds.addAll(removed.keySet());

			for (String newId : removed.keySet()) {
				getViewer().getGraphLayout().setLocation(nodesById.get(newId),
						CanvasUtilities.addPoints(removed.get(newId), center));
			}
		}

		applyNodeCollapse();
		setSelectedNodeIds(newIds);
	}

	@Override
	protected GraphMouse<GraphNode, Edge<GraphNode>> createMouseModel() {
		return new GraphMouse<GraphNode, Edge<GraphNode>>(
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
								SingleElementPropertiesDialog dialog = new SingleElementPropertiesDialog(
										e.getComponent(), node,
										getNodeProperties());

								CanvasUtilities.placeDialogAt(dialog,
										e.getLocationOnScreen());
								dialog.setVisible(true);
							} else if (edge != null) {
								SingleElementPropertiesDialog dialog = new SingleElementPropertiesDialog(
										e.getComponent(), edge,
										getEdgeProperties());

								CanvasUtilities.placeDialogAt(dialog,
										e.getLocationOnScreen());
								dialog.setVisible(true);
							}
						}
					}
				});
	}

	protected void applyNodeCollapse() {
		Set<String> selectedNodeIds = getSelectedNodeIds();
		Set<String> selectedEdgeIds = getSelectedEdgeIds();
		Map<String, GraphNode> oldNodesById = CanvasUtilities
				.getElementsById(nodes);
		Map<String, Edge<GraphNode>> oldEdgesById = CanvasUtilities
				.getElementsById(edges);

		nodes = new LinkedHashSet<GraphNode>();
		edges = new LinkedHashSet<Edge<GraphNode>>();

		Map<String, String> collapseTo = new LinkedHashMap<String, String>();

		for (String to : collapsedNodes.keySet()) {
			for (String from : collapsedNodes.get(to).keySet()) {
				collapseTo.put(from, to);
			}
		}

		Map<String, GraphNode> nodesById = new LinkedHashMap<String, GraphNode>();

		for (GraphNode node : allNodes) {
			if (!collapseTo.keySet().contains(node.getId())) {
				GraphNode newNode = oldNodesById.get(node.getId());

				if (newNode == null) {
					newNode = new GraphNode(node.getId(),
							new LinkedHashMap<String, Object>(
									node.getProperties()), node.getRegion());
				}

				nodes.add(newNode);
				nodesById.put(node.getId(), newNode);
			}
		}

		Set<GraphNode> metaNodes = new LinkedHashSet<GraphNode>();

		for (String newId : collapsedNodes.keySet()) {
			GraphNode newNode = oldNodesById.get(newId);

			if (newNode == null) {
				Set<GraphNode> nodes = CanvasUtilities.getElementsById(
						allNodes, collapsedNodes.get(newId).keySet());
				Point2D pos = CanvasUtilities.getCenter(getNodePositions(nodes)
						.values());

				newNode = combineNodes(newId, nodes);
				getViewer().getGraphLayout().setLocation(newNode, pos);
			}

			nodes.add(newNode);
			nodesById.put(newNode.getId(), newNode);
			metaNodes.add(newNode);
		}

		for (Edge<GraphNode> edge : allEdges) {
			GraphNode from = nodesById.get(edge.getFrom().getId());
			GraphNode to = nodesById.get(edge.getTo().getId());

			if (from == null || to == null) {
				Edge<GraphNode> newEdge = oldEdgesById.get(edge.getId());

				if (from == null) {
					from = nodesById
							.get(collapseTo.get(edge.getFrom().getId()));
				}

				if (to == null) {
					to = nodesById.get(collapseTo.get(edge.getTo().getId()));
				}

				if (newEdge == null) {
					newEdge = new Edge<GraphNode>(edge.getId(),
							new LinkedHashMap<String, Object>(
									edge.getProperties()), from, to);
				} else if (!newEdge.getFrom().equals(from)
						|| !newEdge.getTo().equals(to)) {
					newEdge = new Edge<GraphNode>(newEdge.getId(),
							newEdge.getProperties(), from, to);
				}

				if (!newEdge.getFrom().equals(newEdge.getTo())) {
					edges.add(newEdge);
				}
			} else {
				edges.add(new Edge<GraphNode>(
						edge.getId(),
						new LinkedHashMap<String, Object>(edge.getProperties()),
						from, to));
			}
		}

		if (isJoinEdges()) {
			joinMap = CanvasUtilities.joinEdges(edges, getEdgeProperties(),
					getEdgeIdProperty(), getEdgeFromProperty(),
					getEdgeToProperty(),
					CanvasUtilities.getElementIds(allEdges));
			edges = joinMap.keySet();
		} else {
			joinMap = new LinkedHashMap<Edge<GraphNode>, Set<Edge<GraphNode>>>();
		}

		invisibleNodes.clear();
		invisibleEdges.clear();
		getViewer().getGraphLayout().setGraph(createGraph());
		getViewer().getRenderContext().setVertexStrokeTransformer(
				new NodeStrokeTransformer<GraphNode>(metaNodes));
		getViewer().getPickedVertexState().clear();
		applyHighlights();
		setSelectedNodeIds(selectedNodeIds);
		setSelectedEdgeIds(selectedEdgeIds);
	}

	@Override
	protected void applyEdgeJoin() {
		applyNodeCollapse();
	}

	private void applyLayout() {
		Graph<GraphNode, Edge<GraphNode>> graph = createGraph();
		Layout<GraphNode, Edge<GraphNode>> layout = null;

		if (layoutType.equals(CIRCLE_LAYOUT)) {
			layout = new CircleLayout<GraphNode, Edge<GraphNode>>(graph);
		} else if (layoutType.equals(FR_LAYOUT)) {
			layout = new FRLayout<GraphNode, Edge<GraphNode>>(graph);
		} else if (layoutType.equals(FR_LAYOUT_2)) {
			layout = new FRLayout2<GraphNode, Edge<GraphNode>>(graph);
		} else if (layoutType.equals(ISOM_LAYOUT)) {
			layout = new ISOMLayout<GraphNode, Edge<GraphNode>>(graph);
		} else if (layoutType.equals(KK_LAYOUT)) {
			layout = new KKLayout<GraphNode, Edge<GraphNode>>(graph);
		} else if (layoutType.equals(SPRING_LAYOUT)) {
			layout = new SpringLayout<GraphNode, Edge<GraphNode>>(graph);
		} else if (layoutType.equals(SPRING_LAYOUT_2)) {
			layout = new SpringLayout2<GraphNode, Edge<GraphNode>>(graph);
		}

		layout.setSize(getViewer().getSize());
		setTransform(1.0, 1.0, 0.0, 0.0);
		getViewer().setGraphLayout(layout);
	}

	private Map<String, Point2D> getNodePositions(Collection<GraphNode> nodes) {
		Map<String, Point2D> map = new LinkedHashMap<String, Point2D>();
		Layout<GraphNode, Edge<GraphNode>> layout = getViewer()
				.getGraphLayout();

		for (GraphNode node : nodes) {
			map.put(node.getId(), layout.transform(node));
		}

		return map;
	}

	private GraphNode combineNodes(String id, Collection<GraphNode> nodes) {
		Map<String, Object> properties = new LinkedHashMap<String, Object>();

		for (GraphNode node : nodes) {
			CanvasUtilities.addMapToMap(properties, getNodeProperties(),
					node.getProperties());
		}

		if (getNodeIdProperty() != null) {
			Class<?> type = getNodeProperties().get(getNodeIdProperty());

			if (type == String.class) {
				properties.put(getNodeIdProperty(), id);
			} else if (type == Integer.class) {
				properties.put(getNodeIdProperty(), Integer.parseInt(id));
			}
		}

		properties.put(metaNodeProperty, true);

		return new GraphNode(id, properties, null);
	}

	private Graph<GraphNode, Edge<GraphNode>> createGraph() {
		Graph<GraphNode, Edge<GraphNode>> graph = new DirectedSparseMultigraph<GraphNode, Edge<GraphNode>>();

		for (GraphNode node : nodes) {
			if (!invisibleNodes.contains(node)) {
				graph.addVertex(node);
			}
		}

		for (Edge<GraphNode> edge : edges) {
			if (!invisibleEdges.contains(edge)
					&& !invisibleNodes.contains(edge.getFrom())
					&& !invisibleNodes.contains(edge.getTo())) {
				graph.addEdge(edge, edge.getFrom(), edge.getTo());
			}
		}

		return graph;
	}
}
