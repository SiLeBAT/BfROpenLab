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

import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightListDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.gis.views.canvas.jung.BetterPickingGraphMousePlugin;
import de.bund.bfr.knime.gis.views.canvas.transformer.InvisibleTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;
import de.bund.bfr.knime.gis.views.canvas.util.CanvasOptionsPanel;
import de.bund.bfr.knime.gis.views.canvas.util.CanvasPopupMenu;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.Naming;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

/**
 * @author Christian Thoens
 */
public class RegionCanvas extends ShapefileCanvas<RegionNode>implements ItemListener {

	private static final long serialVersionUID = 1L;

	public RegionCanvas(boolean allowEdges, Naming naming) {
		this(new ArrayList<RegionNode>(0), new ArrayList<Edge<RegionNode>>(0), new NodePropertySchema(),
				new EdgePropertySchema(), naming, allowEdges);
	}

	public RegionCanvas(List<RegionNode> nodes, NodePropertySchema nodeSchema, Naming naming) {
		this(nodes, new ArrayList<Edge<RegionNode>>(0), nodeSchema, new EdgePropertySchema(), naming, false);
	}

	public RegionCanvas(List<RegionNode> nodes, List<Edge<RegionNode>> edges, NodePropertySchema nodeSchema,
			EdgePropertySchema edgeSchema, Naming naming) {
		this(nodes, edges, nodeSchema, edgeSchema, naming, true);
	}

	private RegionCanvas(List<RegionNode> nodes, List<Edge<RegionNode>> edges, NodePropertySchema nodeSchema,
			EdgePropertySchema edgeSchema, Naming naming, boolean allowEdges) {
		super(nodes, edges, nodeSchema, edgeSchema, naming);

		setPopupMenu(new CanvasPopupMenu(this, allowEdges, false, false));
		setOptionsPanel(new CanvasOptionsPanel(this, allowEdges, false, true, false));
		viewer.getPickedVertexState().addItemListener(this);
		viewer.getPickedEdgeState().addItemListener(this);
		viewer.getRenderContext().setVertexShapeTransformer(new NodeShapeTransformer<RegionNode>(2, null));
		viewer.getRenderContext().setVertexDrawPaintTransformer(new InvisibleTransformer<RegionNode>());
		viewer.getRenderContext().setVertexFillPaintTransformer(new InvisibleTransformer<RegionNode>());
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
		if (e.getItem() instanceof RegionNode) {
			flushImage();
			viewer.repaint();
		}
	}

	@Override
	public void applyChanges() {
		flushImage();
		super.applyChanges();
	}

	@Override
	public void applyHighlights() {
		CanvasUtils.applyNodeLabels(viewer.getRenderContext(), nodes, nodeHighlightConditions);
		CanvasUtils.applyEdgeHighlights(viewer.getRenderContext(), edges, edgeHighlightConditions, getEdgeThickness(),
				getEdgeMaxThickness());
	}

	@Override
	protected BetterPickingGraphMousePlugin<RegionNode, Edge<RegionNode>> createPickingPlugin() {
		return new RegionCanvasUtils.PickingPlugin<>(this);
	}

	@Override
	protected void paintGis(Graphics g, boolean toSvg, boolean onWhiteBackground) {
		RegionCanvasUtils.paintRegions(g, nodes, getSelectedNodes(), nodeHighlightConditions);
		super.paintGis(g, toSvg, false);
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
}
