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

import com.google.common.math.DoubleMath;
import com.google.common.primitives.Doubles;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.gis.geocode.GeocodingNodeModel;
import de.bund.bfr.knime.gis.views.canvas.dialogs.ListFilterDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SinglePropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;

public class LocationCanvas extends GisCanvas<LocationNode> {

	private static final long serialVersionUID = 1L;

	private List<LocationNode> allNodes;
	private List<Edge<LocationNode>> allEdges;
	private Set<LocationNode> nodes;
	private Set<Edge<LocationNode>> edges;
	private Map<Edge<LocationNode>, Set<Edge<LocationNode>>> joinMap;

	private Map<String, Set<String>> collapsedNodes;
	private Map<String, LocationNode> nodeSaveMap;
	private Map<String, Edge<LocationNode>> edgeSaveMap;

	private boolean allowEdges;

	private String metaNodeProperty;

	public LocationCanvas(boolean allowEdges) {
		this(new ArrayList<LocationNode>(),
				new ArrayList<Edge<LocationNode>>(),
				new LinkedHashMap<String, Class<?>>(),
				new LinkedHashMap<String, Class<?>>(), null, null, null, null,
				new ArrayList<RegionNode>(), allowEdges);
	}

	public LocationCanvas(List<LocationNode> nodes,
			Map<String, Class<?>> nodeProperties, String nodeIdProperty,
			List<RegionNode> regions) {
		this(nodes, new ArrayList<Edge<LocationNode>>(), nodeProperties,
				new LinkedHashMap<String, Class<?>>(), nodeIdProperty, null,
				null, null, regions, false);
	}

	public LocationCanvas(List<LocationNode> nodes,
			List<Edge<LocationNode>> edges,
			Map<String, Class<?>> nodeProperties,
			Map<String, Class<?>> edgeProperties, String nodeIdProperty,
			String edgeIdProperty, String edgeFromProperty,
			String edgeToProperty, List<RegionNode> regions) {
		this(nodes, edges, nodeProperties, edgeProperties, nodeIdProperty,
				edgeIdProperty, edgeFromProperty, edgeToProperty, regions, true);
	}

	private LocationCanvas(List<LocationNode> nodes,
			List<Edge<LocationNode>> edges,
			Map<String, Class<?>> nodeProperties,
			Map<String, Class<?>> edgeProperties, String nodeIdProperty,
			String edgeIdProperty, String edgeFromProperty,
			String edgeToProperty, List<RegionNode> regionNodes,
			boolean allowEdges) {
		super(regionNodes, nodeProperties, edgeProperties, nodeIdProperty,
				edgeIdProperty, edgeFromProperty, edgeToProperty);
		this.allowEdges = allowEdges;
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
						new LinkedHashMap<LocationNode, Double>()));
		getViewer().getGraphLayout().setGraph(
				CanvasUtils.createGraph(this.nodes, this.edges));

		for (LocationNode node : this.nodes) {
			getViewer().getGraphLayout().setLocation(node, node.getCenter());
		}
	}

	@Override
	public Set<LocationNode> getNodes() {
		return nodes;
	}

	@Override
	public Set<Edge<LocationNode>> getEdges() {
		return edges;
	}

	@Override
	public List<LocationNode> getAllNodes() {
		return allNodes;
	}

	@Override
	public List<Edge<LocationNode>> getAllEdges() {
		return allEdges;
	}

	@Override
	public Map<String, LocationNode> getNodeSaveMap() {
		return nodeSaveMap;
	}

	@Override
	public Map<String, Edge<LocationNode>> getEdgeSaveMap() {
		return edgeSaveMap;
	}

	@Override
	public Map<String, Set<String>> getCollapseMap() {
		return collapsedNodes;
	}

	public Map<String, Set<String>> getCollapsedNodes() {
		return collapsedNodes;
	}

	public void setCollapsedNodes(Map<String, Set<String>> collapsedNodes) {
		this.collapsedNodes = collapsedNodes;
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

		collapsedNodes.put(newId, selectedIds);
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
			newIds.addAll(collapsedNodes.remove(id));
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

		Map<Object, Set<LocationNode>> nodesByProperty = new LinkedHashMap<>();

		for (String id : CanvasUtils.getElementIds(allNodes)) {
			LocationNode node = nodeSaveMap.get(id);
			Object value = node.getProperties().get(result);

			if (value == null) {
				continue;
			}

			if (!nodesByProperty.containsKey(value)) {
				nodesByProperty.put(value, new LinkedHashSet<LocationNode>());
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

			collapsedNodes.put(newId,
					CanvasUtils.getElementIds(nodesByProperty.get(value)));
		}

		applyChanges();
		setSelectedNodeIds(collapsedNodes.keySet());
	}

	@Override
	public void clearCollapsedNodesItemClicked() {
		collapsedNodes.clear();
		applyChanges();
		getViewer().getPickedVertexState().clear();
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

	@Override
	protected GraphMouse<LocationNode, Edge<LocationNode>> createMouseModel(
			Mode editingMode) {
		return new GraphMouse<>(
				new PickingGraphMousePlugin<LocationNode, Edge<LocationNode>>() {

					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.getButton() == MouseEvent.BUTTON1
								&& e.getClickCount() == 2) {
							LocationNode node = getViewer().getPickSupport()
									.getVertex(getViewer().getGraphLayout(),
											e.getX(), e.getY());
							Edge<LocationNode> edge = getViewer()
									.getPickSupport().getEdge(
											getViewer().getGraphLayout(),
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

					@Override
					public void mouseDragged(MouseEvent e) {
						if (vertex == null) {
							super.mouseDragged(e);
						}
					}
				}, editingMode);
	}

	@Override
	protected Map<Edge<LocationNode>, Set<Edge<LocationNode>>> getJoinMap() {
		return joinMap;
	}

	@Override
	protected void applyNameChanges() {
		updatePopupMenuAndOptionsPanel();
	}

	private void applyNodeCollapse() {
		Map<String, LocationNode> newMetaNodes = new LinkedHashMap<>();

		for (String newId : collapsedNodes.keySet()) {
			if (!nodeSaveMap.containsKey(newId)) {
				Set<LocationNode> nodes = CanvasUtils.getElementsById(
						nodeSaveMap, collapsedNodes.get(newId));

				newMetaNodes.put(newId, createMetaNode(newId, nodes));
			}
		}

		CanvasUtils.applyNodeCollapse(this, newMetaNodes);
	}

	private void applyInvisibility() {
		CanvasUtils
				.removeInvisibleElements(nodes, getNodeHighlightConditions());
		CanvasUtils
				.removeInvisibleElements(edges, getEdgeHighlightConditions());
		CanvasUtils.removeNodelessEdges(edges, nodes);
	}

	private void applyJoinEdgesAndSkipEdgeless() {
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

	private void applyHighlights() {
		CanvasUtils.applyNodeHighlights(getViewer(),
				getNodeHighlightConditions(), getNodeSize());
		CanvasUtils.applyEdgeHighlights(getViewer(),
				getEdgeHighlightConditions());
	}

	private LocationNode createMetaNode(String id,
			Collection<LocationNode> nodes) {
		Map<String, Object> properties = new LinkedHashMap<>();

		for (LocationNode node : nodes) {
			CanvasUtils.addMapToMap(properties, getNodeProperties(),
					node.getProperties());
		}

		if (getNodeIdProperty() != null) {
			properties.put(getNodeIdProperty(), id);
		}

		properties.put(metaNodeProperty, true);
		properties.put(GeocodingNodeModel.LATITUDE_COLUMN, CanvasUtils
				.getMeanValue(nodes, GeocodingNodeModel.LATITUDE_COLUMN));
		properties.put(GeocodingNodeModel.LONGITUDE_COLUMN, CanvasUtils
				.getMeanValue(nodes, GeocodingNodeModel.LONGITUDE_COLUMN));

		List<Double> xList = new ArrayList<Double>();
		List<Double> yList = new ArrayList<Double>();

		for (LocationNode node : nodes) {
			xList.add(node.getCenter().x);
			yList.add(node.getCenter().y);
		}

		double x = DoubleMath.mean(Doubles.toArray(xList));
		double y = DoubleMath.mean(Doubles.toArray(yList));

		return new LocationNode(id, properties, new Point2D.Double(x, y));
	}

	private void updatePopupMenuAndOptionsPanel() {
		setPopupMenu(new CanvasPopupMenu(this, allowEdges, false, false));
		setOptionsPanel(new CanvasOptionsPanel(this, allowEdges, true, true));
	}
}
