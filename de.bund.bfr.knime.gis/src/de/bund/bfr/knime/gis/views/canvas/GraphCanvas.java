/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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
import java.util.LinkedHashSet;
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

import com.google.common.collect.Sets;

import de.bund.bfr.jung.JungUtils;
import de.bund.bfr.jung.layout.FRLayout;
import de.bund.bfr.jung.layout.Layout;
import de.bund.bfr.jung.layout.LayoutType;
import de.bund.bfr.knime.PointUtils;
import de.bund.bfr.knime.UI;
//import de.bund.bfr.knime.gis.views.canvas.Canvas.PostPaintable;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.util.CanvasOptionsPanel;
import de.bund.bfr.knime.gis.views.canvas.util.CanvasPopupMenu;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.Naming;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.Transform;
import de.bund.bfr.knime.ui.Dialogs;

/**
 * @author Christian Thoens
 */
public class GraphCanvas extends Canvas<GraphNode> {
	
	//private static Logger logger =  Logger.getLogger("de.bund.bfr");

	private static final long serialVersionUID = 1L;
	private static final boolean USE_FR_LAYOUT_TO_PLACE_NEW_NODES = true;

	public GraphCanvas(boolean allowCollapse, Naming naming) {
		this(new ArrayList<>(0), new ArrayList<>(0), new NodePropertySchema(), new EdgePropertySchema(), naming,
				allowCollapse);
	}

	public GraphCanvas(List<GraphNode> nodes, List<Edge<GraphNode>> edges, NodePropertySchema nodeSchema,
			EdgePropertySchema edgeSchema, Naming naming, boolean allowCollapse) {
		super(nodes, edges, nodeSchema, edgeSchema, naming);
		
		setPopupMenu(new CanvasPopupMenu(this, true, true, allowCollapse,true));
		setOptionsPanel(new CanvasOptionsPanel(this, true, true, false, false));
		viewer.getRenderContext().setVertexShapeTransformer(JungUtils.newNodeShapeTransformer(
				getOptionsPanel().getNodeSize(), getOptionsPanel().getNodeMaxSize(), null, null));
	
	}

	public void initLayout() {
	
		if (!this.getLayoutableNodes().isEmpty()) {
			applyLayout(LayoutType.ISOM_LAYOUT, this.getLayoutableNodes(), false);
		}
		
	}

	public Map<String, Point2D> getNodePositions() {
		updatePositionsOfCollapsedNodes();
		
		return getNodePositions(nodeSaveMap.values().stream().filter(n -> !collapsedNodes.containsKey(n.getId()))
				.collect(Collectors.toList()));
	}

	public void setNodePositions(Map<String, Point2D> nodePositions) {
		
		List<GraphNode> nodesWithoutPos = new ArrayList<>();

		for (GraphNode node :  nodeSaveMap.values()) {
			if (collapsedNodes.containsKey(node.getId())) {
				
				Point2D centerOfCollapsedNodes = PointUtils
						.getCenter(CanvasUtils.getElementsById(nodePositions, collapsedNodes.get(node.getId())));

				if (centerOfCollapsedNodes != null) {
					viewer.getGraphLayout().setLocation(node, centerOfCollapsedNodes);
				} else if (nodePositions.containsKey(node.getId())) {
					viewer.getGraphLayout().setLocation(node, nodePositions.get(node.getId()));
				} else {
					nodesWithoutPos.add(node);
				}
			} else {
				if (nodePositions.containsKey(node.getId())) {
					viewer.getGraphLayout().setLocation(node, nodePositions.get(node.getId()));
				} else {
					nodesWithoutPos.add(node);
				}
			}
		}
		
		if (!nodesWithoutPos.isEmpty()) {
			if (USE_FR_LAYOUT_TO_PLACE_NEW_NODES) {
				Layout<GraphNode, Edge<GraphNode>> layout = new FRLayout<>(viewer.getGraphLayout().getGraph(),
						viewer.getSize(), true);
				Map<GraphNode, Point2D> initialPositions = new LinkedHashMap<>();

				for (GraphNode node : nodeSaveMap.values()) {
					Point2D pos = viewer.getGraphLayout().transform(node);

					initialPositions.put(node, transform.apply(pos.getX(), pos.getY()));
					layout.setLocked(node, !nodesWithoutPos.contains(node));
				}

				Map<GraphNode, Point2D> newPositions = layout.getNodePositions(initialPositions, null);

				newPositions.keySet().retainAll(nodesWithoutPos);
				newPositions.forEach((node, pos) -> viewer.getGraphLayout().setLocation(node,
						transform.applyInverse(pos.getX(), pos.getY())));
				nodesWithoutPos.removeAll(newPositions.keySet());
			}

			Point2D upperLeft = transform.applyInverse(10, 10);
			Point2D upperRight = transform.applyInverse(viewer.getPreferredSize().width - 10, 10);

			for (int i = 0; i < nodesWithoutPos.size(); i++) {
				double x = upperLeft.getX()
						+ (double) i / (double) nodesWithoutPos.size() * (upperRight.getX() - upperLeft.getX());

				viewer.getGraphLayout().setLocation(nodesWithoutPos.get(i), new Point2D.Double(x, upperLeft.getY()));
			}
		}
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
		Set<GraphNode> nodesForLayout;

		if (!selectedNodes.isEmpty()) {
			switch (Dialogs.showYesNoCancelDialog(this,
					"Should the layout be applied on the selected " + naming.nodes() + " only?", "Confirm")) {
			case YES:
				nodesForLayout = selectedNodes;
				break;
			case NO:
				nodesForLayout = this.getLayoutableNodes();
				break;
			case CANCEL:
			default:
				return;
			}
		} else {
			nodesForLayout = this.getLayoutableNodes();
		}

		if (nodesForLayout.size() < 2) {
			Dialogs.showErrorMessage(this, "Layouts can only be applied on 2 or more " + naming.nodes() + ".");
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

//	@Override
//	public void editingModeChanged() {
//		super.editingModeChanged();
//	}

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
	
	protected Set<GraphNode> getLayoutableNodes() { return this.nodes; }
	
	protected Rectangle2D getLayoutBounds() { return new Rectangle2D.Double(0.0, 0.0, viewer.getSize().getWidth(), viewer.getSize().getHeight()); }

	protected void applyLayout(LayoutType layoutType, Set<GraphNode> nodesForLayout, boolean showProgressDialog, boolean signalLayoutProcessFinish) {
		
		Rectangle2D layoutBounds = this.getLayoutBounds();
		Dimension s =  layoutBounds.getBounds().getSize();  //viewer.getSize();
		
		Layout<GraphNode, Edge<GraphNode>> layout = 
				((layoutType==LayoutType.FR_LAYOUT && nodesForLayout != nodes)?
				new FRLayout<>(viewer.getGraphLayout().getGraph(), s,true):
				layoutType.create(viewer.getGraphLayout().getGraph(),s));
		
		
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

			layoutDialog.add(UI.createBorderPanel(new JLabel("Waiting for Layout Process")), BorderLayout.NORTH);
			layoutDialog.add(UI.createBorderPanel(progressBar), BorderLayout.CENTER);
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
						p -> SwingUtilities.invokeLater(() -> progressBar.setValue((int) Math.round(p * 100)))));
				SwingUtilities.invokeLater(() -> layoutDialog.setVisible(false));
			}).start();

			layoutDialog.setVisible(true);
		} else {
			layoutResult.putAll(layout.getNodePositions(initialPositions, null));
		}

		Set<GraphNode> nonCollapsedNodes = CanvasUtils.getElementsById(nodeSaveMap,
				Sets.difference(nodeSaveMap.keySet(), collapsedNodes.values().stream().flatMap(Set::stream)
						.collect(Collectors.toCollection(LinkedHashSet::new))));

		
		for (GraphNode node : nonCollapsedNodes) {
			if (layoutResult.containsKey(node)) {

				if(layout.isLocked(node)) {
					viewer.getGraphLayout().setLocation(node, layoutResult.get(node));
				} else {
					Point2D pos = layoutResult.get(node);
					viewer.getGraphLayout().setLocation(node, new Point2D.Double(pos.getX() + layoutBounds.getX(),pos.getY() + layoutBounds.getY()));
				}
			} else {
				Point2D pos = viewer.getGraphLayout().transform(node);
				
				viewer.getGraphLayout().setLocation(node, transform.apply(pos.getX(), pos.getY()));
			}
		}
		
		 
		if (layoutType == LayoutType.FR_LAYOUT) {
			if(this.getLayoutableNodes()==nodesForLayout) {
			  setTransform(CanvasUtils.getTransformForBounds(getCanvasSize(), PointUtils.getBounds(layoutResult.values()),
					  null));
			} else {
			  setTransform(Transform.IDENTITY_TRANSFORM);
			}
		} else {
			setTransform(Transform.IDENTITY_TRANSFORM);
		}

		if(signalLayoutProcessFinish) Stream.of(getListeners(CanvasListener.class)).forEach(l -> l.layoutProcessFinished(this));
	
	}
	
	protected void applyLayout(LayoutType layoutType, Set<GraphNode> nodesForLayout, boolean showProgressDialog) {
		this.applyLayout(layoutType, nodesForLayout, showProgressDialog, true);
	}

	private void updatePositionsOfCollapsedNodes() {
		collapsedNodes.forEach((metaId, containedIds) -> {
			Set<GraphNode> newNodes = CanvasUtils.getElementsById(nodeSaveMap, containedIds);
			Point2D oldCenter = PointUtils.getCenter(getNodePositions(newNodes).values());
			Point2D newCenter = viewer.getGraphLayout().transform(nodeSaveMap.get(metaId));
			Point2D diff = PointUtils.substractPoints(newCenter, oldCenter);

			if (diff.getX() * diff.getX() + diff.getY() * diff.getY() > 1e-10) {
				for (GraphNode newNode : newNodes) {
					viewer.getGraphLayout().setLocation(newNode,
							PointUtils.addPoints(viewer.getGraphLayout().transform(newNode), diff));
				}
			}
		});
	}

}
