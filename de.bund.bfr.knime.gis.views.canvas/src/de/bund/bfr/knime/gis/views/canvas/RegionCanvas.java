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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightListDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SingleElementPropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.transformer.InvisibleTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;

/**
 * @author Christian Thoens
 */
public class RegionCanvas extends GisCanvas<RegionNode> {

	private static final long serialVersionUID = 1L;

	private List<RegionNode> allNodes;
	private List<Edge<RegionNode>> allEdges;
	private Set<RegionNode> nodes;
	private Set<Edge<RegionNode>> edges;
	private Set<RegionNode> invisibleNodes;
	private Set<Edge<RegionNode>> invisibleEdges;

	public RegionCanvas(boolean allowEdges) {
		this(new ArrayList<RegionNode>(), new ArrayList<Edge<RegionNode>>(),
				new LinkedHashMap<String, Class<?>>(),
				new LinkedHashMap<String, Class<?>>(), null, null, null, null,
				allowEdges);
	}

	public RegionCanvas(List<RegionNode> nodes,
			Map<String, Class<?>> nodeProperties, String nodeIdProperty) {
		this(nodes, new ArrayList<Edge<RegionNode>>(), nodeProperties,
				new LinkedHashMap<String, Class<?>>(), nodeIdProperty, null,
				null, null, false);
	}

	public RegionCanvas(List<RegionNode> nodes, List<Edge<RegionNode>> edges,
			Map<String, Class<?>> nodeProperties,
			Map<String, Class<?>> edgeProperties, String nodeIdProperty,
			String edgeIdProperty, String edgeFromProperty,
			String edgeToProperty) {
		this(nodes, edges, nodeProperties, edgeProperties, nodeIdProperty,
				edgeIdProperty, edgeFromProperty, edgeToProperty, true);
	}

	private RegionCanvas(List<RegionNode> nodes, List<Edge<RegionNode>> edges,
			Map<String, Class<?>> nodeProperties,
			Map<String, Class<?>> edgeProperties, String nodeIdProperty,
			String edgeIdProperty, String edgeFromProperty,
			String edgeToProperty, boolean allowEdges) {
		super(nodes, nodeProperties, edgeProperties, nodeIdProperty,
				edgeIdProperty, edgeFromProperty, edgeToProperty);
		this.allNodes = nodes;
		this.allEdges = edges;
		this.nodes = new LinkedHashSet<RegionNode>(allNodes);
		this.edges = new LinkedHashSet<Edge<RegionNode>>(allEdges);
		setAllowEdges(allowEdges);
		invisibleNodes = new LinkedHashSet<RegionNode>();
		invisibleEdges = new LinkedHashSet<Edge<RegionNode>>();

		getViewer().getRenderContext().setVertexShapeTransformer(
				new NodeShapeTransformer<RegionNode>(2,
						new LinkedHashMap<RegionNode, Double>()));
		getViewer().getRenderContext().setVertexDrawPaintTransformer(
				new InvisibleTransformer<RegionNode>());
		getViewer().getRenderContext().setVertexFillPaintTransformer(
				new InvisibleTransformer<RegionNode>());
		createGraph();
	}

	public Set<RegionNode> getNodes() {
		return nodes;
	}

	public Set<Edge<RegionNode>> getEdges() {
		return edges;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		super.itemStateChanged(e);

		if (e.getItem() instanceof RegionNode) {
			flushImage();
			getViewer().repaint();
		}
	}

	@Override
	protected HighlightListDialog openNodeHighlightDialog() {
		return new HighlightListDialog(this, getNodeProperties(), false, false,
				false, getNodeHighlightConditions(), null);
	}

	@Override
	protected HighlightListDialog openEdgeHighlightDialog() {
		return new HighlightListDialog(this, getEdgeProperties(), true, true,
				true, getEdgeHighlightConditions(), null);
	}

	@Override
	protected boolean applyHighlights() {
		flushImage();
		getViewer().repaint();

		invisibleNodes.clear();
		boolean changed1 = CanvasUtilities.applyEdgeHighlights(getViewer(),
				edges, invisibleEdges, getEdgeHighlightConditions());
		boolean changed2 = CanvasUtilities.applyEdgelessNodes(getViewer(),
				nodes, edges, invisibleNodes, invisibleEdges,
				isSkipEdgelessNodes());

		return changed1 || changed2;
	}

	@Override
	protected void applyEdgeJoin() {
		Set<String> selectedEdgeIds = getSelectedEdgeIds();

		if (isJoinEdges()) {
			edges = CanvasUtilities.joinEdges(allEdges, getEdgeProperties(),
					getEdgeIdProperty(), getEdgeFromProperty(),
					getEdgeToProperty(),
					CanvasUtilities.getElementIds(allEdges)).keySet();
		} else {
			edges = new LinkedHashSet<Edge<RegionNode>>(allEdges);
		}

		createGraph();
		applyHighlights();
		setSelectedEdgeIds(selectedEdgeIds);
	}

	@Override
	protected GraphMouse<RegionNode, Edge<RegionNode>> createMouseModel() {
		return new GraphMouse<RegionNode, Edge<RegionNode>>(
				new PickingGraphMousePlugin<RegionNode, Edge<RegionNode>>() {

					@Override
					public void mousePressed(MouseEvent e) {
						RegionNode node = getContainingNode(e.getX(), e.getY());
						Edge<RegionNode> edge = getViewer().getPickSupport()
								.getEdge(getViewer().getGraphLayout(),
										e.getX(), e.getY());

						if (e.getButton() == MouseEvent.BUTTON1 && node != null
								&& edge == null) {
							if (!e.isShiftDown()) {
								getViewer().getPickedVertexState().clear();
							}

							if (e.isShiftDown()
									&& getViewer().getPickedVertexState()
											.isPicked(node)) {
								getViewer().getPickedVertexState().pick(node,
										false);
							} else {
								getViewer().getPickedVertexState().pick(node,
										true);
								vertex = node;
							}
						} else {
							super.mousePressed(e);
						}
					}

					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.getButton() == MouseEvent.BUTTON1
								&& e.getClickCount() == 2) {
							RegionNode node = getContainingNode(e.getX(),
									e.getY());
							Edge<RegionNode> edge = getViewer()
									.getPickSupport().getEdge(
											getViewer().getGraphLayout(),
											e.getX(), e.getY());

							if (edge != null) {
								SingleElementPropertiesDialog dialog = new SingleElementPropertiesDialog(
										e.getComponent(), edge,
										getEdgeProperties());

								dialog.setVisible(true);
							} else if (node != null) {
								SingleElementPropertiesDialog dialog = new SingleElementPropertiesDialog(
										e.getComponent(), node,
										getNodeProperties());

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
				});
	}

	@Override
	protected void paintGis(Graphics g, int width, int height, boolean toSvg) {
		paintBackground(g, width, height);
		paintRegions(g);
		paintRegionBorders(g, width, height, toSvg);
	}

	private void paintRegions(Graphics g) {
		for (RegionNode node : getViewer().getPickedVertexState().getPicked()) {
			g.setColor(Color.BLUE);

			for (Polygon part : node.getTransformedPolygon()) {
				g.fillPolygon(part);
			}
		}

		List<Color> nodeColors = new ArrayList<Color>();
		Map<RegionNode, List<Double>> nodeAlphas = new LinkedHashMap<RegionNode, List<Double>>();

		for (RegionNode node : nodes) {
			nodeAlphas.put(node, new ArrayList<Double>());
		}

		for (HighlightCondition condition : getNodeHighlightConditions()
				.getConditions()) {
			Map<RegionNode, Double> values = condition.getValues(nodes);

			nodeColors.add(condition.getColor());

			for (RegionNode node : nodes) {
				nodeAlphas.get(node).add(values.get(node));
			}
		}

		for (RegionNode node : nodes) {
			Paint color = CanvasUtilities.mixColors(Color.WHITE, nodeColors,
					nodeAlphas.get(node));

			if (!color.equals(Color.WHITE)
					&& !getViewer().getPickedVertexState().isPicked(node)) {
				((Graphics2D) g).setPaint(color);

				for (Polygon part : node.getTransformedPolygon()) {
					g.fillPolygon(part);
				}
			}
		}
	}

	private RegionNode getContainingNode(int x, int y) {
		Point2D p = toGraphCoordinates(x, y);

		for (RegionNode node : getRegions()) {
			if (node.containsPoint(p)) {
				return node;
			}
		}

		return null;
	}

	private void createGraph() {
		Layout<RegionNode, Edge<RegionNode>> layout = getViewer()
				.getGraphLayout();
		Graph<RegionNode, Edge<RegionNode>> graph = new DirectedSparseMultigraph<RegionNode, Edge<RegionNode>>();

		for (RegionNode node : nodes) {
			if (!invisibleNodes.contains(node)) {
				graph.addVertex(node);
				layout.setLocation(node, node.getCenter());
			}
		}

		for (Edge<RegionNode> edge : edges) {
			if (!invisibleEdges.contains(edge)
					&& !invisibleNodes.contains(edge.getFrom())
					&& !invisibleNodes.contains(edge.getTo())) {
				graph.addEdge(edge, edge.getFrom(), edge.getTo());
			}
		}

		layout.setGraph(graph);
	}
}
