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

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import de.bund.bfr.knime.PointUtils;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.jung.ChangeSupportLayout;
import de.bund.bfr.knime.gis.views.canvas.jung.layout.Layout;
import de.bund.bfr.knime.gis.views.canvas.jung.layout.LayoutType;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;
import de.bund.bfr.knime.ui.Dialogs;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;

/**
 * @author Christian Thoens
 */
public class GraphCanvas extends Canvas<GraphNode> {

	private static final long serialVersionUID = 1L;

	public GraphCanvas(boolean allowCollapse, Naming naming) {
		this(new ArrayList<GraphNode>(0), new ArrayList<Edge<GraphNode>>(0), new NodePropertySchema(),
				new EdgePropertySchema(), naming, allowCollapse);
	}

	public GraphCanvas(List<GraphNode> nodes, List<Edge<GraphNode>> edges, NodePropertySchema nodeSchema,
			EdgePropertySchema edgeSchema, Naming naming, boolean allowCollapse) {
		super(nodes, edges, nodeSchema, edgeSchema, naming);

		setPopupMenu(new CanvasPopupMenu(this, true, true, allowCollapse));
		setOptionsPanel(new CanvasOptionsPanel(this, true, true, false, false));
		viewer.getRenderContext()
				.setVertexShapeTransformer(new NodeShapeTransformer<GraphNode>(getNodeSize(), getNodeMaxSize()));
	}

	public void initLayout() {
		if (!nodes.isEmpty()) {
			applyLayout(LayoutType.ISOM_LAYOUT, null);
		}
	}

	public Map<String, Point2D> getNodePositions() {
		return getNodePositions(nodeSaveMap.values());
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
		viewer.setGraphLayout(new ChangeSupportLayout<>(layout));
	}

	@Override
	public void resetLayoutItemClicked() {
		Rectangle2D bounds = PointUtils.getBounds(getNodePositions(nodes).values());

		if (bounds != null) {
			setTransform(CanvasUtils.getTransformForBounds(getCanvasSize(), bounds, null));
		} else {
			super.resetLayoutItemClicked();
		}
	}

	@Override
	public void layoutItemClicked(LayoutType layoutType) {
		applyLayout(layoutType, getSelectedNodes());
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
		Map<String, Object> properties = new LinkedHashMap<>();

		for (GraphNode node : nodes) {
			CanvasUtils.addMapToMap(properties, nodeSchema, node.getProperties());
		}

		properties.put(nodeSchema.getId(), id);
		properties.put(metaNodeProperty, true);
		properties.put(nodeSchema.getLatitude(), null);
		properties.put(nodeSchema.getLongitude(), null);

		GraphNode newNode = new GraphNode(id, properties, null);

		viewer.getGraphLayout().setLocation(newNode, PointUtils.getCenter(getNodePositions(nodes).values()));

		return newNode;
	}

	private void fireLayoutProcessFinished() {
		for (CanvasListener listener : canvasListeners) {
			listener.layoutProcessFinished(this);
		}
	}

	private void applyLayout(LayoutType layoutType, Set<GraphNode> selectedNodes) {
		if (selectedNodes != null && !selectedNodes.isEmpty()) {
			switch (Dialogs.showYesNoDialog(this,
					"Should the layout be applied on the selected " + naming.nodes() + " only?", "Confirm")) {
			case YES:
				// Do nothing
				break;
			case NO:
				selectedNodes = null;
				break;
			default:
				return;
			}
		}

		Set<GraphNode> nodesForLayout = selectedNodes != null && !selectedNodes.isEmpty() ? selectedNodes : nodes;

		if (nodesForLayout.size() < 2) {
			Dialogs.showErrorMessage(this, "Layouts can only be applied on 2 or more " + naming.nodes() + ".", "Error");
			return;
		}

		Graph<GraphNode, Edge<GraphNode>> graph = viewer.getGraphLayout().getGraph();
		Point2D move = new Point2D.Double(transform.getTranslationX() / transform.getScaleX(),
				transform.getTranslationY() / transform.getScaleY());
		final Layout<GraphNode, Edge<GraphNode>> layout = layoutType.create(graph,
				new Dimension((int) (viewer.getSize().width / transform.getScaleX()),
						(int) (viewer.getSize().height / transform.getScaleY())));
		final Map<GraphNode, Point2D> initialPositions = new LinkedHashMap<>();

		for (GraphNode node : nodes) {
			initialPositions.put(node, PointUtils.addPoints(viewer.getGraphLayout().transform(node), move));

			if (!nodesForLayout.contains(node)) {
				layout.setLocked(node, true);
			}
		}

		final Map<GraphNode, Point2D> layoutResult = new LinkedHashMap<>();

		if (selectedNodes == null) {
			layoutResult.putAll(layout.getNodePositions(initialPositions, null));
		} else {
			final JDialog layoutDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Layout Process",
					Dialog.DEFAULT_MODALITY_TYPE);
			final JProgressBar progressBar = new JProgressBar();

			Thread layoutThread = new Thread(new Runnable() {

				@Override
				public void run() {
					while (!layoutDialog.isVisible()) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
						}
					}

					layoutResult.putAll(layout.getNodePositions(initialPositions, new Layout.ProgressListener() {

						@Override
						public void progressChanged(double progress) {
							progressBar.setValue((int) Math.round(progress * 100));
						}
					}));

					layoutDialog.setVisible(false);
				}
			});

			layoutDialog.add(UI.createHorizontalPanel(new JLabel("Waiting for Layout Process")), BorderLayout.NORTH);
			layoutDialog.add(UI.createHorizontalPanel(progressBar), BorderLayout.CENTER);
			layoutDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			layoutDialog.pack();
			layoutDialog.setResizable(false);
			layoutDialog.setLocationRelativeTo(this);
			layoutThread.start();
			layoutDialog.setVisible(true);
		}

		Map<String, Point2D> newPositions = new LinkedHashMap<>();

		for (Map.Entry<GraphNode, Point2D> entry : layoutResult.entrySet()) {
			newPositions.put(entry.getKey().getId(), entry.getValue());
		}

		setNodePositions(newPositions);

		if (layoutType == LayoutType.FR_LAYOUT) {
			Rectangle2D bounds = PointUtils.getBounds(getNodePositions(nodes).values());

			setTransform(CanvasUtils.getTransformForBounds(getCanvasSize(), bounds, null));
		} else {
			setTransform(new Transform(transform.getScaleX(), transform.getScaleY(), 0, 0));
		}

		fireLayoutProcessFinished();
	}

	private void updatePositionsOfCollapsedNodes() {
		for (Map.Entry<String, Set<String>> entry : collapsedNodes.entrySet()) {
			Set<GraphNode> newNodes = CanvasUtils.getElementsById(nodeSaveMap, entry.getValue());
			Point2D oldCenter = PointUtils.getCenter(getNodePositions(newNodes).values());
			Point2D newCenter = viewer.getGraphLayout().transform(nodeSaveMap.get(entry.getKey()));
			Point2D diff = PointUtils.substractPoints(newCenter, oldCenter);

			for (GraphNode newNode : newNodes) {
				Point2D newPos = PointUtils.addPoints(viewer.getGraphLayout().transform(newNode), diff);

				viewer.getGraphLayout().setLocation(newNode, newPos);
			}
		}
	}
}
