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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightListDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SinglePropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.jung.GraphMouse;
import de.bund.bfr.knime.gis.views.canvas.transformer.InvisibleTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

/**
 * @author Christian Thoens
 */
public class RegionCanvas extends ShapefileCanvas<RegionNode> {

	private static final long serialVersionUID = 1L;

	public RegionCanvas(boolean allowEdges, Naming naming) {
		this(new ArrayList<RegionNode>(), new ArrayList<Edge<RegionNode>>(),
				new NodePropertySchema(), new EdgePropertySchema(), naming, allowEdges);
	}

	public RegionCanvas(List<RegionNode> nodes, NodePropertySchema nodeSchema, Naming naming) {
		this(nodes, new ArrayList<Edge<RegionNode>>(), nodeSchema, new EdgePropertySchema(),
				naming, false);
	}

	public RegionCanvas(List<RegionNode> nodes, List<Edge<RegionNode>> edges,
			NodePropertySchema nodeSchema, EdgePropertySchema edgeSchema, Naming naming) {
		this(nodes, edges, nodeSchema, edgeSchema, naming, true);
	}

	private RegionCanvas(List<RegionNode> nodes, List<Edge<RegionNode>> edges,
			NodePropertySchema nodeSchema, EdgePropertySchema edgeSchema, Naming naming,
			boolean allowEdges) {
		super(nodes, edges, nodeSchema, edgeSchema, naming);

		setPopupMenu(new CanvasPopupMenu(this, allowEdges, false, false));
		setOptionsPanel(new CanvasOptionsPanel(this, allowEdges, false, true, false));
		viewer.getRenderContext().setVertexShapeTransformer(
				new NodeShapeTransformer<RegionNode>(2, null));
		viewer.getRenderContext().setVertexDrawPaintTransformer(
				new InvisibleTransformer<RegionNode>());
		viewer.getRenderContext().setVertexFillPaintTransformer(
				new InvisibleTransformer<RegionNode>());
		viewer.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

		for (RegionNode node : this.nodes) {
			node.updatePolygon(GisUtils.latLonToViz(node.getPolygon()));
		}

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
	public void avoidOverlayChanged() {
	}

	@Override
	public void applyChanges() {
		flushImage();
		super.applyChanges();
	}

	@Override
	public void applyHighlights() {
		CanvasUtils.applyNodeLabels(viewer.getRenderContext(), nodes, nodeHighlightConditions);
		CanvasUtils.applyEdgeHighlights(viewer.getRenderContext(), edges, edgeHighlightConditions);
	}

	@Override
	protected GraphMouse<RegionNode, Edge<RegionNode>> createGraphMouse() {
		return new GraphMouse<>(new RegionPickingPlugin(), 2.0);
	}

	@Override
	protected void paintGis(Graphics g, boolean toSvg) {
		for (RegionNode node : viewer.getPickedVertexState().getPicked()) {
			g.setColor(Color.BLUE);
			((Graphics2D) g).fill(node.getTransformedPolygon());
		}

		List<Color> nodeColors = new ArrayList<>();
		Map<RegionNode, List<Double>> nodeAlphas = new LinkedHashMap<>();
		boolean prioritize = nodeHighlightConditions.isPrioritizeColors();

		for (RegionNode node : nodes) {
			nodeAlphas.put(node, new ArrayList<Double>());
		}

		for (HighlightCondition condition : nodeHighlightConditions.getConditions()) {
			Map<RegionNode, Double> values = condition.getValues(nodes);

			nodeColors.add(condition.getColor());

			for (RegionNode node : nodes) {
				List<Double> alphas = nodeAlphas.get(node);

				if (!prioritize || alphas.isEmpty() || Collections.max(alphas) == 0.0) {
					alphas.add(values.get(node));
				} else {
					alphas.add(0.0);
				}
			}
		}

		for (RegionNode node : nodes) {
			Paint color = CanvasUtils.mixColors(Color.WHITE, nodeColors, nodeAlphas.get(node),
					false);

			if (!color.equals(Color.WHITE) && !viewer.getPickedVertexState().isPicked(node)) {
				((Graphics2D) g).setPaint(color);
				((Graphics2D) g).fill(node.getTransformedPolygon());
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
	protected RegionNode createMetaNode(String id, Collection<RegionNode> nodes) {
		throw new UnsupportedOperationException();
	}

	private RegionNode getContainingNode(int x, int y) {
		Point2D p = transform.applyInverse(x, y);

		for (RegionNode node : getRegions()) {
			if (node.containsPoint(p)) {
				return node;
			}
		}

		return null;
	}

	protected class RegionPickingPlugin extends GisPickingPlugin {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
				RegionNode node = getContainingNode(e.getX(), e.getY());
				Edge<RegionNode> edge = viewer.getPickSupport().getEdge(viewer.getGraphLayout(),
						e.getX(), e.getY());

				if (edge != null) {
					SinglePropertiesDialog dialog = new SinglePropertiesDialog(e.getComponent(),
							edge, edgeSchema);

					dialog.setVisible(true);
				} else if (node != null) {
					SinglePropertiesDialog dialog = new SinglePropertiesDialog(e.getComponent(),
							node, nodeSchema);

					dialog.setVisible(true);
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			RegionNode node = getContainingNode(e.getX(), e.getY());
			Edge<RegionNode> edge = viewer.getPickSupport().getEdge(viewer.getGraphLayout(),
					e.getX(), e.getY());

			if (e.getButton() == MouseEvent.BUTTON1 && node != null && edge == null) {
				if (!e.isShiftDown()) {
					viewer.getPickedVertexState().clear();
				}

				if (e.isShiftDown() && viewer.getPickedVertexState().isPicked(node)) {
					viewer.getPickedVertexState().pick(node, false);
				} else {
					viewer.getPickedVertexState().pick(node, true);
					vertex = node;
				}
			} else {
				super.mousePressed(e);
			}
		}
	}
}
