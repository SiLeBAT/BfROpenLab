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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import de.bund.bfr.knime.gis.views.canvas.dialogs.SinglePropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.layout.CircleLayout;
import de.bund.bfr.knime.gis.views.canvas.layout.FRLayout;
import de.bund.bfr.knime.gis.views.canvas.layout.GridLayout;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;
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

	private boolean allowCollapse;

	public GraphCanvas(boolean allowCollapse) {
		this(new ArrayList<GraphNode>(), new ArrayList<Edge<GraphNode>>(),
				new NodePropertySchema(), new EdgePropertySchema(),
				allowCollapse);
	}

	public GraphCanvas(List<GraphNode> nodes, List<Edge<GraphNode>> edges,
			NodePropertySchema nodeSchema, EdgePropertySchema edgeSchema,
			boolean allowCollapse) {
		super(nodes, edges, nodeSchema, edgeSchema);
		this.allowCollapse = allowCollapse;

		updatePopupMenuAndOptionsPanel();
		viewer.getRenderContext().setVertexShapeTransformer(
				new NodeShapeTransformer<>(getNodeSize(),
						new LinkedHashMap<GraphNode, Double>()));
		viewer.getGraphLayout().setGraph(
				CanvasUtils.createGraph(this.nodes, this.edges));
		applyLayout(LayoutType.FR_LAYOUT, null);
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

		Layout<GraphNode, Edge<GraphNode>> layout = new StaticLayout<>(viewer
				.getGraphLayout().getGraph());
		Point2D upperLeft = toGraphCoordinates(0, 0);
		Point2D upperRight = toGraphCoordinates(
				viewer.getPreferredSize().width, 0);
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

		layout.setSize(viewer.getSize());
		viewer.setGraphLayout(layout);
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
	public void expandFromNodeItemClicked() {
		applyNewMetaNodePositions(getSelectedNodeIds());
		super.expandFromNodeItemClicked();
	}

	@Override
	public void clearCollapsedNodesItemClicked() {
		applyNewMetaNodePositions(collapsedNodes.keySet());
		super.clearCollapsedNodesItemClicked();
	}

	@Override
	public void borderAlphaChanged() {
		throw new UnsupportedOperationException();
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
							GraphNode node = viewer.getPickSupport()
									.getVertex(viewer.getGraphLayout(),
											e.getX(), e.getY());
							Edge<GraphNode> edge = viewer.getPickSupport()
									.getEdge(viewer.getGraphLayout(), e.getX(),
											e.getY());

							if (node != null) {
								SinglePropertiesDialog dialog = new SinglePropertiesDialog(
										e.getComponent(), node,
										nodeSchema.getMap());

								dialog.setVisible(true);
							} else if (edge != null) {
								SinglePropertiesDialog dialog = new SinglePropertiesDialog(
										e.getComponent(), edge,
										edgeSchema.getMap());

								dialog.setVisible(true);
							}
						}
					}
				}, editingMode);
	}

	@Override
	protected void applyNameChanges() {
		updatePopupMenuAndOptionsPanel();
	}

	@Override
	protected GraphNode createMetaNode(String id, Collection<GraphNode> nodes) {
		Map<String, Object> properties = new LinkedHashMap<>();

		for (GraphNode node : nodes) {
			CanvasUtils.addMapToMap(properties, nodeSchema.getMap(),
					node.getProperties());
		}

		properties.put(nodeSchema.getId(), id);
		properties.put(metaNodeProperty, true);

		if (nodeSchema.getLatitude() != null) {
			properties.put(nodeSchema.getLatitude(),
					CanvasUtils.getMeanValue(nodes, nodeSchema.getLatitude()));
		}

		if (nodeSchema.getLongitude() != null) {
			properties.put(nodeSchema.getLongitude(),
					CanvasUtils.getMeanValue(nodes, nodeSchema.getLongitude()));
		}

		GraphNode newNode = new GraphNode(id, properties, null);

		viewer.getGraphLayout().setLocation(newNode,
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

		Graph<GraphNode, Edge<GraphNode>> graph = viewer.getGraphLayout()
				.getGraph();
		Layout<GraphNode, Edge<GraphNode>> layout = null;

		if (!selectedNodes.isEmpty() && layoutType == LayoutType.ISOM_LAYOUT) {
			if (JOptionPane.showConfirmDialog(this,
					layoutType + " cannot be applied on a subset of "
							+ nodesName.toLowerCase() + ". Apply " + layoutType
							+ " on all " + nodesName.toLowerCase() + "?",
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
		}

		if (!selectedNodes.isEmpty()) {
			Point2D move = new Point2D.Double(translationX / scaleX,
					translationY / scaleY);

			for (GraphNode node : nodes) {
				if (!selectedNodes.contains(node)) {
					layout.setLocation(node, CanvasUtils.addPoints(viewer
							.getGraphLayout().transform(node), move));
					layout.lock(node, true);
				}
			}

			layout.setSize(new Dimension(
					(int) (viewer.getSize().width / scaleX), (int) (viewer
							.getSize().height / scaleY)));

			for (GraphNode node : nodes) {
				if (!selectedNodes.contains(node)) {
					layout.setLocation(node, CanvasUtils.addPoints(viewer
							.getGraphLayout().transform(node), move));
					layout.lock(node, true);
				}
			}

			setTransform(scaleX, scaleY, 0.0, 0.0);
		} else {
			layout.setSize(viewer.getSize());
			setTransform(1.0, 1.0, 0.0, 0.0);
		}

		viewer.setGraphLayout(layout);
	}

	private Map<String, Point2D> getNodePositions(Collection<GraphNode> nodes) {
		Map<String, Point2D> map = new LinkedHashMap<>();
		Layout<GraphNode, Edge<GraphNode>> layout = viewer.getGraphLayout();

		for (GraphNode node : nodes) {
			Point2D pos = layout.transform(node);

			if (pos != null) {
				map.put(node.getId(), pos);
			}
		}

		return map;
	}

	private void applyNewMetaNodePositions(Collection<String> metaIds) {
		for (String id : metaIds) {
			if (!collapsedNodes.containsKey(id)) {
				continue;
			}

			Set<GraphNode> newNodes = CanvasUtils.getElementsById(nodeSaveMap,
					collapsedNodes.get(id));
			Point2D oldCenter = CanvasUtils
					.getCenter(getNodePositions(newNodes).values());
			Point2D newCenter = viewer.getGraphLayout().transform(
					nodeSaveMap.get(id));
			Point2D diff = CanvasUtils.substractPoints(newCenter, oldCenter);

			for (GraphNode newNode : newNodes) {
				Point2D newPos = CanvasUtils.addPoints(viewer.getGraphLayout()
						.transform(newNode), diff);

				viewer.getGraphLayout().setLocation(newNode, newPos);
			}
		}
	}
}
