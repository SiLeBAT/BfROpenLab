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
import de.bund.bfr.knime.gis.views.canvas.dialogs.SinglePropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;

public class LocationCanvas extends GisCanvas<LocationNode> {

	private static final long serialVersionUID = 1L;

	private List<RegionNode> regions;
	private Map<String, Set<String>> collapsedNodes;

	private boolean allowEdges;

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
			String edgeToProperty, List<RegionNode> regions, boolean allowEdges) {
		super(nodes, edges, nodeProperties, edgeProperties, nodeIdProperty,
				edgeIdProperty, edgeFromProperty, edgeToProperty);
		this.allowEdges = allowEdges;
		this.regions = regions;
		collapsedNodes = new LinkedHashMap<>();

		updatePopupMenuAndOptionsPanel();
		viewer.getRenderContext().setVertexShapeTransformer(
				new NodeShapeTransformer<>(getNodeSize(),
						new LinkedHashMap<LocationNode, Double>()));
		viewer.getGraphLayout().setGraph(
				CanvasUtils.createGraph(this.nodes, this.edges));

		for (LocationNode node : this.nodes) {
			viewer.getGraphLayout().setLocation(node, node.getCenter());
		}
	}

	@Override
	public Collection<RegionNode> getRegions() {
		return regions;
	}

	@Override
	protected Map<String, Set<String>> getCollapseMap() {
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
						+ nodesName.toLowerCase() + " are already collapsed",
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		String newId = null;

		while (true) {
			newId = (String) JOptionPane.showInputDialog(this,
					"Specify ID for Meta " + nodeName, nodeName + " ID",
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
						+ nodesName.toLowerCase() + " are not collapsed",
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
		Map<Object, Set<LocationNode>> nodesByProperty = CanvasUtils
				.openCollapseByPropertyDialog(this, nodeProperties.keySet(),
						CanvasUtils.getElementIds(allNodes), nodeSaveMap);

		if (nodesByProperty.isEmpty()) {
			return;
		}

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
		viewer.getPickedVertexState().clear();
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

		setSelectedNodeIds(selectedNodeIds);
		setSelectedEdgeIds(selectedEdgeIds);
		viewer.repaint();
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
							LocationNode node = viewer.getPickSupport()
									.getVertex(viewer.getGraphLayout(),
											e.getX(), e.getY());
							Edge<LocationNode> edge = viewer.getPickSupport()
									.getEdge(viewer.getGraphLayout(), e.getX(),
											e.getY());

							if (node != null) {
								SinglePropertiesDialog dialog = new SinglePropertiesDialog(
										e.getComponent(), node, nodeProperties);

								dialog.setVisible(true);
							} else if (edge != null) {
								SinglePropertiesDialog dialog = new SinglePropertiesDialog(
										e.getComponent(), edge, edgeProperties);

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

		CanvasUtils.applyNodeCollapse(nodes, edges, allNodes, allEdges,
				nodeSaveMap, edgeSaveMap, edgeFromProperty, edgeToProperty,
				collapsedNodes, newMetaNodes);
	}

	private void applyInvisibility() {
		CanvasUtils.removeInvisibleElements(nodes, nodeHighlightConditions);
		CanvasUtils.removeInvisibleElements(edges, edgeHighlightConditions);
		CanvasUtils.removeNodelessEdges(edges, nodes);
	}

	private void applyJoinEdgesAndSkipEdgeless() {
		joinMap.clear();

		if (isJoinEdges()) {
			joinMap = CanvasUtils.joinEdges(edges, edgeProperties,
					edgeIdProperty, edgeFromProperty, edgeToProperty,
					CanvasUtils.getElementIds(allEdges));
			edges = new LinkedHashSet<>(joinMap.keySet());
		}

		if (isSkipEdgelessNodes()) {
			CanvasUtils.removeEdgelessNodes(nodes, edges);
		}
	}

	private void applyHighlights() {
		CanvasUtils.applyNodeHighlights(viewer, nodeHighlightConditions,
				getNodeSize());
		CanvasUtils.applyEdgeHighlights(viewer, edgeHighlightConditions);
	}

	private LocationNode createMetaNode(String id,
			Collection<LocationNode> nodes) {
		Map<String, Object> properties = new LinkedHashMap<>();

		for (LocationNode node : nodes) {
			CanvasUtils.addMapToMap(properties, nodeProperties,
					node.getProperties());
		}

		if (nodeIdProperty != null) {
			properties.put(nodeIdProperty, id);
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
