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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.gis.geocode.GeocodingNodeModel;
import de.bund.bfr.knime.gis.views.canvas.dialogs.ListFilterDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SinglePropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.layout.CircleLayout;
import de.bund.bfr.knime.gis.views.canvas.layout.FRLayout;
import de.bund.bfr.knime.gis.views.canvas.layout.GridLayout;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeStrokeTransformer;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;

/**
 * @author Christian Thoens
 */
public class GraphCanvas extends Canvas<GraphNode> {

	private static final long serialVersionUID = 1L;

	private List<GraphNode> allNodes;
	private List<Edge<GraphNode>> allEdges;
	private Set<GraphNode> nodes;
	private Set<Edge<GraphNode>> edges;
	private Map<Edge<GraphNode>, Set<Edge<GraphNode>>> joinMap;

	private Map<String, Map<String, Point2D>> collapsedNodes;
	private Map<String, GraphNode> nodeSaveMap;
	private Map<String, Edge<GraphNode>> edgeSaveMap;

	private boolean allowCollapse;

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
		this.allowCollapse = allowCollapse;
		this.nodes = new LinkedHashSet<>();
		this.edges = new LinkedHashSet<>();
		CanvasUtils.copyNodesAndEdges(nodes, edges, this.nodes, this.edges);

		allNodes = nodes;
		allEdges = edges;
		nodeSaveMap = CanvasUtils.getElementsById(this.nodes);
		edgeSaveMap = CanvasUtils.getElementsById(this.edges);
		joinMap = new LinkedHashMap<>();
		collapsedNodes = new LinkedHashMap<>();
		metaNodeProperty = KnimeUtils.createNewValue(IS_META_NODE
				+ getNodeName(), getNodeProperties().keySet());
		getNodeProperties().put(metaNodeProperty, Boolean.class);

		updatePopupMenuAndOptionsPanel();
		getViewer().getRenderContext().setVertexShapeTransformer(
				new NodeShapeTransformer<>(getNodeSize(),
						new LinkedHashMap<GraphNode, Double>()));
		getViewer().getRenderContext().setVertexStrokeTransformer(
				new NodeStrokeTransformer<GraphNode>(metaNodeProperty));
		getViewer().getGraphLayout().setGraph(
				CanvasUtils.createGraph(this.nodes, this.edges));
		applyLayout(LayoutType.FR_LAYOUT, null);
	}

	@Override
	public List<GraphNode> getAllNodes() {
		return allNodes;
	}

	@Override
	public List<Edge<GraphNode>> getAllEdges() {
		return allEdges;
	}

	@Override
	public Set<GraphNode> getNodes() {
		return nodes;
	}

	@Override
	public Set<Edge<GraphNode>> getEdges() {
		return edges;
	}

	@Override
	public Map<String, GraphNode> getNodeSaveMap() {
		return nodeSaveMap;
	}

	@Override
	public Map<String, Edge<GraphNode>> getEdgeSaveMap() {
		return edgeSaveMap;
	}

	@Override
	public Map<String, Set<String>> getCollapseMap() {
		Map<String, Set<String>> collapseMap = new LinkedHashMap<>();

		for (String id : collapsedNodes.keySet()) {
			collapseMap.put(id, collapsedNodes.get(id).keySet());
		}

		return collapseMap;
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
	public void collapseToNodeItemClicked() {
		Set<String> selectedIds = getSelectedNodeIds();

		for (String id : collapsedNodes.keySet()) {
			if (selectedIds.contains(id)) {
				JOptionPane.showMessageDialog(this, "Some of the selected "
						+ getNodesName().toLowerCase()
						+ " are already collapsed", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		String newId = null;

		while (true) {
			newId = (String) JOptionPane.showInputDialog(this,
					"Specify ID for Meta " + getNodeName(), getNodeName()
							+ " ID", JOptionPane.QUESTION_MESSAGE, null, null,
					"");

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

		Map<String, Point2D> absPos = getNodePositions(CanvasUtils
				.getElementsById(getViewer().getGraphLayout().getGraph()
						.getVertices(), selectedIds));
		Map<String, Point2D> relPos = new LinkedHashMap<>();
		Point2D center = CanvasUtils.getCenter(absPos.values());

		for (String id : absPos.keySet()) {
			relPos.put(id, CanvasUtils.substractPoints(absPos.get(id), center));
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
				JOptionPane.showMessageDialog(this, "Some of the selected "
						+ getNodesName().toLowerCase() + " are not collapsed",
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
						CanvasUtils.addPoints(removed.get(newId), center));
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

		Map<Object, Set<GraphNode>> nodesByProperty = new LinkedHashMap<>();

		for (String id : CanvasUtils.getElementIds(allNodes)) {
			GraphNode node = nodeSaveMap.get(id);
			Object value = node.getProperties().get(result);

			if (value == null) {
				continue;
			}

			if (!nodesByProperty.containsKey(value)) {
				nodesByProperty.put(value, new LinkedHashSet<GraphNode>());
			}

			nodesByProperty.get(value).add(node);
		}

		List<Object> propertyList = new ArrayList<>(nodesByProperty.keySet());

		Collections.sort(propertyList, new Comparator<Object>() {

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

		ListFilterDialog<Object> dialog = new ListFilterDialog<>(this,
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

		for (Object value : nodesByProperty.keySet()) {
			String newId = KnimeUtils.createNewValue(value.toString(),
					nodeSaveMap.keySet());
			Map<String, Point2D> absPos = getNodePositions(nodesByProperty
					.get(value));
			Map<String, Point2D> relPos = new LinkedHashMap<>();
			Point2D center = CanvasUtils.getCenter(absPos.values());

			for (String id : absPos.keySet()) {
				relPos.put(id,
						CanvasUtils.substractPoints(absPos.get(id), center));
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
						CanvasUtils.addPoints(removed.get(newId), center));
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
		applyInvisibility();
		applyJoinEdgesAndSkipEdgeless();
		getViewer().getGraphLayout().setGraph(
				CanvasUtils.createGraph(nodes, edges));
		applyHighlights();

		setSelectedNodeIds(selectedNodeIds);
		setSelectedEdgeIds(selectedEdgeIds);
		getViewer().repaint();
	}

	protected void applyNodeCollapse() {
		Map<String, GraphNode> newMetaNodes = new LinkedHashMap<>();

		for (String newId : collapsedNodes.keySet()) {
			if (!nodeSaveMap.containsKey(newId)) {
				Set<GraphNode> nodes = CanvasUtils.getElementsById(nodeSaveMap,
						collapsedNodes.get(newId).keySet());

				newMetaNodes.put(newId, createMetaNode(newId, nodes));
			}
		}

		CanvasUtils.applyNodeCollapse(this, newMetaNodes);
	}

	protected void applyInvisibility() {
		CanvasUtils
				.removeInvisibleElements(nodes, getNodeHighlightConditions());
		CanvasUtils
				.removeInvisibleElements(edges, getEdgeHighlightConditions());
		CanvasUtils.removeNodelessEdges(edges, nodes);
	}

	protected void applyJoinEdgesAndSkipEdgeless() {
		joinMap.clear();

		if (isJoinEdges()) {
			joinMap = CanvasUtils.joinEdges(edges, getEdgeProperties(),
					getEdgeIdProperty(), getEdgeFromProperty(),
					getEdgeToProperty(), CanvasUtils.getElementIds(allEdges));
			edges = new LinkedHashSet<>(joinMap.keySet());
		}

		if (isSkipEdgelessNodes()) {
			CanvasUtils.removeEdgelessNodes(nodes, edges);
		}
	}

	protected void applyHighlights() {
		CanvasUtils.applyNodeHighlights(getViewer(),
				getNodeHighlightConditions(), getNodeSize());
		CanvasUtils.applyEdgeHighlights(getViewer(),
				getEdgeHighlightConditions());
	}

	@Override
	protected void applyNameChanges() {
		updatePopupMenuAndOptionsPanel();
	}

	@Override
	protected Map<Edge<GraphNode>, Set<Edge<GraphNode>>> getJoinMap() {
		return joinMap;
	}

	private GraphNode createMetaNode(String id, Collection<GraphNode> nodes) {
		Map<String, Object> properties = new LinkedHashMap<>();

		for (GraphNode node : nodes) {
			CanvasUtils.addMapToMap(properties, getNodeProperties(),
					node.getProperties());
		}

		if (getNodeIdProperty() != null) {
			properties.put(getNodeIdProperty(), id);
		}

		properties.put(metaNodeProperty, true);

		if (properties.containsKey(GeocodingNodeModel.LATITUDE_COLUMN)) {
			properties.put(GeocodingNodeModel.LATITUDE_COLUMN, CanvasUtils
					.getMeanValue(nodes, GeocodingNodeModel.LATITUDE_COLUMN));
		}

		if (properties.containsKey(GeocodingNodeModel.LONGITUDE_COLUMN)) {
			properties.put(GeocodingNodeModel.LONGITUDE_COLUMN, CanvasUtils
					.getMeanValue(nodes, GeocodingNodeModel.LONGITUDE_COLUMN));
		}

		GraphNode newNode = new GraphNode(id, properties, null);

		getViewer().getGraphLayout().setLocation(newNode,
				CanvasUtils.getCenter(getNodePositions(nodes).values()));

		return newNode;
	}

	private void updatePopupMenuAndOptionsPanel() {
		setPopupMenu(new CanvasPopupMenu(this, true, true, allowCollapse));
		setOptionsPanel(new CanvasOptionsPanel(this, true, true, false));
	}

	private void applyLayout(LayoutType layoutType, Set<GraphNode> selectedNodes) {
		if (selectedNodes == null) {
			selectedNodes = new LinkedHashSet<>();
		}

		Graph<GraphNode, Edge<GraphNode>> graph = getViewer().getGraphLayout()
				.getGraph();
		Layout<GraphNode, Edge<GraphNode>> layout = null;

		if (!selectedNodes.isEmpty() && layoutType == LayoutType.ISOM_LAYOUT) {
			if (JOptionPane.showConfirmDialog(this, layoutType
					+ " cannot be applied on a subset of "
					+ getNodesName().toLowerCase() + ". Apply " + layoutType
					+ " on all " + getNodesName().toLowerCase() + "?",
					"Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				selectedNodes = new LinkedHashSet<>();
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
		case ISOM_LAYOUT:
			layout = new ISOMLayout<>(graph);
			break;
		case KK_LAYOUT:
			layout = new KKLayout<>(graph);
			break;
		default:
			throw new IllegalArgumentException("Illegal input");
		}

		if (!selectedNodes.isEmpty()) {
			Point2D move = new Point2D.Double(getTranslationX() / getScaleX(),
					getTranslationY() / getScaleY());

			for (GraphNode node : nodes) {
				if (!selectedNodes.contains(node)) {
					layout.setLocation(node, CanvasUtils.addPoints(getViewer()
							.getGraphLayout().transform(node), move));
					layout.lock(node, true);
				}
			}

			layout.setSize(new Dimension(
					(int) (getViewer().getSize().width / getScaleX()),
					(int) (getViewer().getSize().height / getScaleY())));

			for (GraphNode node : nodes) {
				if (!selectedNodes.contains(node)) {
					layout.setLocation(node, CanvasUtils.addPoints(getViewer()
							.getGraphLayout().transform(node), move));
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
}
