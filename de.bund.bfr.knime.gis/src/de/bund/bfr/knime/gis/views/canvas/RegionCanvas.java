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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightListDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SinglePropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.transformer.InvisibleTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

/**
 * @author Christian Thoens
 */
public class RegionCanvas extends GisCanvas<RegionNode> {

	private static final long serialVersionUID = 1L;

	private boolean allowEdges;

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
		super(nodes, edges, nodeProperties, edgeProperties, nodeIdProperty,
				edgeIdProperty, edgeFromProperty, edgeToProperty);
		this.allowEdges = allowEdges;

		updatePopupMenuAndOptionsPanel();
		viewer.getRenderContext().setVertexShapeTransformer(
				new NodeShapeTransformer<>(2,
						new LinkedHashMap<RegionNode, Double>()));
		viewer.getRenderContext().setVertexDrawPaintTransformer(
				new InvisibleTransformer<RegionNode>());
		viewer.getRenderContext().setVertexFillPaintTransformer(
				new InvisibleTransformer<RegionNode>());
		viewer.getRenderer().getVertexLabelRenderer()
				.setPosition(Position.CNTR);
		viewer.getGraphLayout().setGraph(
				CanvasUtils.createGraph(this.nodes, this.edges));

		for (RegionNode node : this.nodes) {
			viewer.getGraphLayout().setLocation(node, node.getCenter());
		}
	}

	@Override
	public Collection<RegionNode> getRegions() {
		return nodes;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		super.itemStateChanged(e);

		if (e.getItem() instanceof RegionNode) {
			flushImage();
			viewer.repaint();
		}
	}

	@Override
	protected void applyChanges() {
		flushImage();
		super.applyChanges();
	}

	@Override
	protected void applyHighlights() {
		CanvasUtils.applyNodeLabels(viewer, nodeHighlightConditions);
		CanvasUtils.applyEdgeHighlights(viewer, edgeHighlightConditions);
	}

	@Override
	protected GraphMouse<RegionNode, Edge<RegionNode>> createMouseModel(
			Mode editingMode) {
		return new GraphMouse<>(
				new PickingGraphMousePlugin<RegionNode, Edge<RegionNode>>() {

					@Override
					public void mousePressed(MouseEvent e) {
						RegionNode node = getContainingNode(e.getX(), e.getY());
						Edge<RegionNode> edge = viewer.getPickSupport()
								.getEdge(viewer.getGraphLayout(), e.getX(),
										e.getY());

						if (e.getButton() == MouseEvent.BUTTON1 && node != null
								&& edge == null) {
							if (!e.isShiftDown()) {
								viewer.getPickedVertexState().clear();
							}

							if (e.isShiftDown()
									&& viewer.getPickedVertexState().isPicked(
											node)) {
								viewer.getPickedVertexState().pick(node, false);
							} else {
								viewer.getPickedVertexState().pick(node, true);
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
							Edge<RegionNode> edge = viewer.getPickSupport()
									.getEdge(viewer.getGraphLayout(), e.getX(),
											e.getY());

							if (edge != null) {
								SinglePropertiesDialog dialog = new SinglePropertiesDialog(
										e.getComponent(), edge, edgeProperties);

								dialog.setVisible(true);
							} else if (node != null) {
								SinglePropertiesDialog dialog = new SinglePropertiesDialog(
										e.getComponent(), node, nodeProperties);

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
	protected void paintGis(Graphics g, boolean toSvg) {
		for (RegionNode node : viewer.getPickedVertexState().getPicked()) {
			g.setColor(Color.BLUE);

			for (Polygon part : node.getTransformedPolygon()) {
				g.fillPolygon(part);
			}
		}

		List<Color> nodeColors = new ArrayList<>();
		Map<RegionNode, List<Double>> nodeAlphas = new LinkedHashMap<>();
		boolean prioritize = nodeHighlightConditions.isPrioritizeColors();

		for (RegionNode node : nodes) {
			nodeAlphas.put(node, new ArrayList<Double>());
		}

		for (HighlightCondition condition : nodeHighlightConditions
				.getConditions()) {
			Map<RegionNode, Double> values = condition.getValues(nodes);

			nodeColors.add(condition.getColor());

			for (RegionNode node : nodes) {
				List<Double> alphas = nodeAlphas.get(node);

				if (!prioritize || alphas.isEmpty()
						|| Collections.max(alphas) == 0.0) {
					alphas.add(values.get(node));
				} else {
					alphas.add(0.0);
				}
			}
		}

		for (RegionNode node : nodes) {
			Paint color = CanvasUtils.mixColors(Color.WHITE, nodeColors,
					nodeAlphas.get(node));

			if (!color.equals(Color.WHITE)
					&& !viewer.getPickedVertexState().isPicked(node)) {
				((Graphics2D) g).setPaint(color);

				for (Polygon part : node.getTransformedPolygon()) {
					g.fillPolygon(part);
				}
			}
		}

		super.paintGis(g, toSvg);
	}

	@Override
	protected HighlightListDialog openNodeHighlightDialog() {
		HighlightListDialog dialog = super.openNodeHighlightDialog();

		dialog.setAllowInvisible(false);
		dialog.setAllowThickness(false);

		return dialog;
	}

	@Override
	protected void applyNameChanges() {
		updatePopupMenuAndOptionsPanel();
	}

	@Override
	protected RegionNode createMetaNode(String id, Collection<RegionNode> nodes) {
		throw new UnsupportedOperationException();
	}

	private void updatePopupMenuAndOptionsPanel() {
		setPopupMenu(new CanvasPopupMenu(this, allowEdges, false, false));
		setOptionsPanel(new CanvasOptionsPanel(this, allowEdges, false, true));
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
}
