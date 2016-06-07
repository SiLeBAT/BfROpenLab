/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import de.bund.bfr.jung.JungUtils;
import de.bund.bfr.jung.layout.Layout;
import de.bund.bfr.jung.layout.LayoutType;
import de.bund.bfr.knime.PointUtils;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.util.CanvasOptionsPanel;
import de.bund.bfr.knime.gis.views.canvas.util.CanvasPopupMenu;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.Naming;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.Transform;
import de.bund.bfr.knime.ui.Dialogs;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;

/**
 * @author Christian Thoens
 */
public class GraphCanvas extends Canvas<GraphNode> {

	private static final long serialVersionUID = 1L;

	public GraphCanvas(boolean allowCollapse, Naming naming) {
		this(new ArrayList<>(0), new ArrayList<>(0), new NodePropertySchema(), new EdgePropertySchema(), naming,
				allowCollapse);
	}

	public GraphCanvas(List<GraphNode> nodes, List<Edge<GraphNode>> edges, NodePropertySchema nodeSchema,
			EdgePropertySchema edgeSchema, Naming naming, boolean allowCollapse) {
		super(nodes, edges, nodeSchema, edgeSchema, naming);

		setPopupMenu(new CanvasPopupMenu(this, true, true, allowCollapse));
		setOptionsPanel(new CanvasOptionsPanel(this, true, true, false, false));
		viewer.getRenderContext()
				.setVertexShapeTransformer(JungUtils.newNodeShapeTransformer(getNodeSize(), getNodeMaxSize(), null));
	}

	public void initLayout() {
		if (!nodes.isEmpty()) {
			applyLayout(LayoutType.ISOM_LAYOUT, nodes, false);
		}
	}

	public Map<String, Point2D> getNodePositions() {
		updatePositionsOfCollapsedNodes();
		return getNodePositions(nodeSaveMap.values().stream().filter(n -> !collapsedNodes.containsKey(n.getId()))
				.collect(Collectors.toList()));
	}

	public void setNodePositions(Map<String, Point2D> nodePositions) {
		Map<String, Point2D> positions = new LinkedHashMap<>(nodePositions);
		int n = 0;

		for (GraphNode node : nodeSaveMap.values()) {
			if (positions.get(node.getId()) == null) {
				Set<String> containedNodes = collapsedNodes.get(node.getId());

				if (containedNodes != null) {
					Point2D center = PointUtils.getCenter(CanvasUtils.getElementsById(positions, containedNodes));

					if (center != null) {
						positions.put(node.getId(), center);
					} else {
						n++;
					}
				} else {
					n++;
				}
			}
		}

		StaticLayout<GraphNode, Edge<GraphNode>> layout = new StaticLayout<>(viewer.getGraphLayout().getGraph());
		Point2D upperLeft = transform.applyInverse(10, 10);
		Point2D upperRight = transform.applyInverse(viewer.getPreferredSize().width - 10, 10);
		double x1 = upperLeft.getX();
		double x2 = upperRight.getX();
		double y = upperLeft.getY();
		int i = 0;

		for (GraphNode node : nodeSaveMap.values()) {
			Point2D pos = positions.get(node.getId());

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
		Rectangle2D bounds = PointUtils.getBounds(getNodePositions(nodes).values());

		if (bounds != null) {
			setTransform(CanvasUtils.getTransformForBounds(getCanvasSize(), bounds, null));
			transformFinished();
		} else {
			super.resetLayoutItemClicked();
		}
	}

	@Override
	public void layoutItemClicked(LayoutType layoutType) {
		Set<GraphNode> selectedNodes = getSelectedNodes();
		Set<GraphNode> nodesForLayout = nodes;

		if (!selectedNodes.isEmpty()) {
			switch (Dialogs.showYesNoDialog(this,
					"Should the layout be applied on the selected " + naming.nodes() + " only?", "Confirm")) {
			case YES:
				nodesForLayout = selectedNodes;
				break;
			case NO:
				nodesForLayout = nodes;
				break;
			default:
				return;
			}
		}

		if (nodesForLayout.size() < 2) {
			Dialogs.showErrorMessage(this, "Layouts can only be applied on 2 or more " + naming.nodes() + ".", "Error");
			return;
		}

		applyLayout(layoutType, nodesForLayout, true);
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

	@Override
	public void editingModeChanged() {
		super.editingModeChanged();
	}

	@Override
	protected void applyTransform() {
	}

	@Override
	protected GraphNode createMetaNode(String id, Collection<GraphNode> nodes) {
		GraphNode newNode = new GraphNode(id,
				CanvasUtils.joinPropertiesOfNodes(nodes, nodeSchema, id, metaNodeProperty));

		viewer.getGraphLayout().setLocation(newNode, PointUtils.getCenter(getNodePositions(nodes).values()));

		return newNode;
	}

	private void applyLayout(LayoutType layoutType, Set<GraphNode> nodesForLayout, boolean showProgressDialog) {
		Layout<GraphNode, Edge<GraphNode>> layout = layoutType.create(viewer.getGraphLayout().getGraph(),
				viewer.getSize());
		Map<GraphNode, Point2D> initialPositions = new LinkedHashMap<>();

		for (GraphNode node : nodes) {
			Point2D pos = viewer.getGraphLayout().transform(node);

			initialPositions.put(node, transform.apply(pos.getX(), pos.getY()));
			layout.setLocked(node, !nodesForLayout.contains(node));
		}

		Map<GraphNode, Point2D> layoutResult = new LinkedHashMap<>();

		if (showProgressDialog) {
			JDialog layoutDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Layout Process",
					Dialog.DEFAULT_MODALITY_TYPE);
			JProgressBar progressBar = new JProgressBar();

			layoutDialog.add(UI.createHorizontalPanel(new JLabel("Waiting for Layout Process")), BorderLayout.NORTH);
			layoutDialog.add(UI.createHorizontalPanel(progressBar), BorderLayout.CENTER);
			layoutDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			layoutDialog.pack();
			layoutDialog.setResizable(false);
			layoutDialog.setLocationRelativeTo(this);

			new Thread(() -> {
				while (!layoutDialog.isVisible()) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
					}
				}

				layoutResult.putAll(layout.getNodePositions(initialPositions,
						p -> progressBar.setValue((int) Math.round(p * 100))));
				SwingUtilities.invokeLater(() -> layoutDialog.setVisible(false));
			}).start();

			layoutDialog.setVisible(true);
		} else {
			layoutResult.putAll(layout.getNodePositions(initialPositions, null));
		}

		layoutResult.forEach((node, pos) -> viewer.getGraphLayout().setLocation(node, pos));
		updatePositionsOfCollapsedNodes();

		if (layoutType == LayoutType.FR_LAYOUT) {
			Rectangle2D bounds = PointUtils.getBounds(getNodePositions(nodes).values());

			setTransform(CanvasUtils.getTransformForBounds(getCanvasSize(), bounds, null));
		} else {
			setTransform(Transform.IDENTITY_TRANSFORM);
		}

		Stream.of(getListeners(CanvasListener.class)).forEach(l -> l.layoutProcessFinished(this));
	}

	private void updatePositionsOfCollapsedNodes() {
		collapsedNodes.forEach((metaId, containedIds) -> {
			Set<GraphNode> newNodes = CanvasUtils.getElementsById(nodeSaveMap, containedIds);
			Point2D oldCenter = PointUtils.getCenter(getNodePositions(newNodes).values());
			Point2D newCenter = viewer.getGraphLayout().transform(nodeSaveMap.get(metaId));
			Point2D diff = PointUtils.substractPoints(newCenter, oldCenter);

			for (GraphNode newNode : newNodes) {
				viewer.getGraphLayout().setLocation(newNode,
						PointUtils.addPoints(viewer.getGraphLayout().transform(newNode), diff));
			}
		});
	}
}
