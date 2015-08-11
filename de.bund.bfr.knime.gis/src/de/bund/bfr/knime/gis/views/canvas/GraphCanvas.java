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
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.jung.ChangeSupportLayout;
import de.bund.bfr.knime.gis.views.canvas.jung.GraphMouse;
import de.bund.bfr.knime.gis.views.canvas.jung.PickingMoveListener;
import de.bund.bfr.knime.gis.views.canvas.layout.CircleLayout;
import de.bund.bfr.knime.gis.views.canvas.layout.FRLayout;
import de.bund.bfr.knime.gis.views.canvas.layout.GridLayout;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;

/**
 * @author Christian Thoens
 */
public class GraphCanvas extends Canvas<GraphNode>implements PickingMoveListener {

	private static final long serialVersionUID = 1L;

	public GraphCanvas(boolean allowCollapse, Naming naming) {
		this(new ArrayList<GraphNode>(), new ArrayList<Edge<GraphNode>>(), new NodePropertySchema(),
				new EdgePropertySchema(), naming, allowCollapse);
	}

	@SuppressWarnings("unchecked")
	public GraphCanvas(List<GraphNode> nodes, List<Edge<GraphNode>> edges, NodePropertySchema nodeSchema,
			EdgePropertySchema edgeSchema, Naming naming, boolean allowCollapse) {
		super(nodes, edges, nodeSchema, edgeSchema, naming);

		setPopupMenu(new CanvasPopupMenu(this, true, true, allowCollapse));
		setOptionsPanel(new CanvasOptionsPanel(this, true, true, false, false));
		viewer.getRenderContext()
				.setVertexShapeTransformer(new NodeShapeTransformer<GraphNode>(getNodeSize(), getNodeMaxSize()));
		((GraphMouse<GraphNode, Edge<GraphNode>>) viewer.getGraphMouse()).addPickingMoveListener(this);
	}

	public void initLayout() {
		if (!nodes.isEmpty()) {
			applyLayout(LayoutType.ISOM_LAYOUT, null, true);
		}
	}

	public Map<String, Point2D> getNodePositions() {
		return getNodePositions(nodeSaveMap.values());
	}

	public void setNodePositions(Map<String, Point2D> nodePositions) {
		int n = 0;

		for (GraphNode node : nodeSaveMap.values()) {
			if (nodePositions.get(node.getId()) == null) {
				n++;
			}
		}

		Layout<GraphNode, Edge<GraphNode>> layout = new StaticLayout<>(viewer.getGraphLayout().getGraph());
		Point2D upperLeft = transform.applyInverse(0, 0);
		Point2D upperRight = transform.applyInverse(viewer.getPreferredSize().width, 0);
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
		viewer.setGraphLayout(new ChangeSupportLayout<>(layout));
	}

	@Override
	public void resetLayoutItemClicked() {
		Rectangle2D bounds = CanvasUtils.getBounds(getNodePositions(nodes).values());

		if (bounds != null) {
			setTransform(CanvasUtils.getTransformForBounds(getCanvasSize(), bounds, null));
		} else {
			super.resetLayoutItemClicked();
		}
	}

	@Override
	public void layoutItemClicked(LayoutType layoutType) {
		applyLayout(layoutType, getSelectedNodes(), false);
	}

	@Override
	public void collapseToNodeItemClicked() {
		updatePositionsOfCollapsedNodes();
		super.collapseToNodeItemClicked();
	}

	@Override
	public void expandFromNodeItemClicked() {
		updatePositionsOfCollapsedNodes();
		super.expandFromNodeItemClicked();
	}

	@Override
	public void clearCollapsedNodesItemClicked() {
		updatePositionsOfCollapsedNodes();
		super.clearCollapsedNodesItemClicked();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void editingModeChanged() {
		super.editingModeChanged();
		((GraphMouse<GraphNode, Edge<GraphNode>>) viewer.getGraphMouse()).addPickingMoveListener(this);
	}

	@Override
	public void borderAlphaChanged() {
	}

	@Override
	public void avoidOverlayChanged() {
	}

	@Override
	public void nodesMoved() {
		for (CanvasListener listener : canvasListeners) {
			listener.nodePositionsChanged(this);
		}
	}

	@Override
	protected void applyTransform() {
	}

	@Override
	protected GraphNode createMetaNode(String id, Collection<GraphNode> nodes) {
		Map<String, Object> properties = new LinkedHashMap<>();

		for (GraphNode node : nodes) {
			CanvasUtils.addMapToMap(properties, nodeSchema, node.getProperties());
		}

		properties.put(nodeSchema.getId(), id);
		properties.put(metaNodeProperty, true);
		properties.put(nodeSchema.getLatitude(), null);
		properties.put(nodeSchema.getLongitude(), null);

		GraphNode newNode = new GraphNode(id, properties, null);

		viewer.getGraphLayout().setLocation(newNode, CanvasUtils.getCenter(getNodePositions(nodes).values()));

		return newNode;
	}

	private void applyLayout(LayoutType layoutType, Set<GraphNode> selectedNodes, boolean avoidIterations) {
		if (selectedNodes == null) {
			selectedNodes = new LinkedHashSet<>();
		}

		Graph<GraphNode, Edge<GraphNode>> graph = viewer.getGraphLayout().getGraph();
		Layout<GraphNode, Edge<GraphNode>> layout = null;

		if (!selectedNodes.isEmpty() && layoutType == LayoutType.ISOM_LAYOUT) {
			if (JOptionPane.showConfirmDialog(this,
					layoutType + " cannot be applied on a subset of " + naming.nodes() + ". Apply " + layoutType
							+ " on all " + naming.nodes() + "?",
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
			throw new IllegalArgumentException("Unknown LayoutType: " + layoutType);
		}

		if (!selectedNodes.isEmpty()) {
			Point2D move = new Point2D.Double(transform.getTranslationX() / transform.getScaleX(),
					transform.getTranslationY() / transform.getScaleY());

			for (GraphNode node : nodes) {
				if (!selectedNodes.contains(node)) {
					layout.setLocation(node, CanvasUtils.addPoints(viewer.getGraphLayout().transform(node), move));
					layout.lock(node, true);
				}
			}

			layout.setSize(new Dimension((int) (viewer.getSize().width / transform.getScaleX()),
					(int) (viewer.getSize().height / transform.getScaleY())));

			for (GraphNode node : nodes) {
				if (!selectedNodes.contains(node)) {
					layout.setLocation(node, CanvasUtils.addPoints(viewer.getGraphLayout().transform(node), move));
					layout.lock(node, true);
				}
			}

			setTransform(new Transform(transform.getScaleX(), transform.getScaleY(), 0, 0));
		} else if (!nodes.isEmpty()) {
			layout.setSize(viewer.getSize());
			setTransform(Transform.IDENTITY_TRANSFORM);
		} else {
			return;
		}

		viewer.setGraphLayout(new ChangeSupportLayout<>(layout));

		if (layout instanceof IterativeContext && !avoidIterations) {
			new Thread(new LayoutThread((IterativeContext) layout, !selectedNodes.isEmpty() ? selectedNodes : nodes))
					.start();
		} else {
			setNodePositions(getNodePositions());
			nodesMoved();
		}
	}

	private void updatePositionsOfCollapsedNodes() {
		for (Map.Entry<String, Set<String>> entry : collapsedNodes.entrySet()) {
			Set<GraphNode> newNodes = CanvasUtils.getElementsById(nodeSaveMap, entry.getValue());
			Point2D oldCenter = CanvasUtils.getCenter(getNodePositions(newNodes).values());
			Point2D newCenter = viewer.getGraphLayout().transform(nodeSaveMap.get(entry.getKey()));
			Point2D diff = CanvasUtils.substractPoints(newCenter, oldCenter);

			for (GraphNode newNode : newNodes) {
				Point2D newPos = CanvasUtils.addPoints(viewer.getGraphLayout().transform(newNode), diff);

				viewer.getGraphLayout().setLocation(newNode, newPos);
			}
		}
	}

	private class LayoutThread implements Runnable {

		private IterativeContext layoutProcess;
		private Set<GraphNode> usedNodes;
		private Transform lastTransform;
		private boolean transformedByUser;

		public LayoutThread(IterativeContext layoutProcess, Set<GraphNode> usedNodes) {
			this.layoutProcess = layoutProcess;
			this.usedNodes = usedNodes;
			lastTransform = getTransform();
			transformedByUser = false;
		}

		@Override
		public void run() {
			while (true) {
				if (!transformedByUser) {
					Rectangle2D bounds = CanvasUtils.getBounds(getNodePositions(usedNodes).values());
					Transform newTransform = CanvasUtils.getTransformForBounds(getCanvasSize(), bounds, null);

					if (getTransform().equals(lastTransform)) {
						setTransform(newTransform);
						lastTransform = newTransform;
					} else {
						transformedByUser = true;
					}
				}

				if (layoutProcess.done()) {
					setNodePositions(getNodePositions());
					nodesMoved();
					break;
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
