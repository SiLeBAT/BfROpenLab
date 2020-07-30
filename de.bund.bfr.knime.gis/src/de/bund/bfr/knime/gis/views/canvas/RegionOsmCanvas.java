/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.bund.bfr.jung.BetterPickingGraphMousePlugin;
import de.bund.bfr.jung.BetterVertexLabelRenderer;
import de.bund.bfr.jung.JungUtils;
import de.bund.bfr.jung.LabelPosition;
import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightListDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.gis.views.canvas.util.CanvasOptionsPanel;
import de.bund.bfr.knime.gis.views.canvas.util.CanvasPopupMenu;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.Naming;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;

public class RegionOsmCanvas extends OsmCanvas<RegionNode> {

	private static final long serialVersionUID = 1L;

	public RegionOsmCanvas(boolean allowEdges, Naming naming) {
		this(new ArrayList<>(0), new ArrayList<>(0), new NodePropertySchema(), new EdgePropertySchema(), naming,
				allowEdges);
	}

	public RegionOsmCanvas(List<RegionNode> nodes, NodePropertySchema nodeSchema, Naming naming) {
		this(nodes, new ArrayList<>(0), nodeSchema, new EdgePropertySchema(), naming, false);
	}

	public RegionOsmCanvas(List<RegionNode> nodes, List<Edge<RegionNode>> edges, NodePropertySchema nodeSchema,
			EdgePropertySchema edgeSchema, Naming naming) {
		this(nodes, edges, nodeSchema, edgeSchema, naming, true);
	}

	private RegionOsmCanvas(List<RegionNode> nodes, List<Edge<RegionNode>> edges, NodePropertySchema nodeSchema,
			EdgePropertySchema edgeSchema, Naming naming, boolean allowEdges) {
		super(nodes, edges, nodeSchema, edgeSchema, naming);

		setPopupMenu(new CanvasPopupMenu(this, allowEdges, false, false,true));
		setOptionsPanel(new CanvasOptionsPanel(this, allowEdges, false, true, false));
		viewer.getPickedVertexState().addItemListener(e -> {
			flushImage();
			viewer.repaint();
		});
		viewer.getRenderContext().setVertexShapeTransformer(JungUtils.newNodeShapeTransformer(2, null, null, null));
		viewer.getRenderContext().setVertexDrawPaintTransformer(node -> new Color(0, 0, 0, 0));
		viewer.getRenderContext().setVertexFillPaintTransformer(node -> new Color(0, 0, 0, 0));
		viewer.getRenderer().setVertexLabelRenderer(new BetterVertexLabelRenderer<>(LabelPosition.CENTER));

		for (RegionNode node : this.nodes) {
			node.updatePolygon(GisUtils.latLonToViz(node.getPolygon()));
		}

		for (RegionNode node : this.nodes) {
			viewer.getGraphLayout().setLocation(node, node.getCenter());
		}
	}

	@Override
	public void resetLayoutItemClicked() {
		Rectangle2D bounds = RegionCanvasUtils.getBounds(nodes);

		if (bounds != null) {
			setTransform(CanvasUtils.getTransformForBounds(getCanvasSize(), bounds, 2.0));
			transformFinished();
		} else {
			super.resetLayoutItemClicked();
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
		CanvasUtils.applyEdgeHighlights(viewer.getRenderContext(), edges, edgeHighlightConditions,
				getOptionsPanel().getEdgeThickness(), getOptionsPanel().getEdgeMaxThickness());
	}

	@Override
	protected void applyTransform() {
		flushImage();

		for (RegionNode node : nodes) {
			node.createTransformedPolygons(transform);
		}

		viewer.repaint();
	}

	@Override
	protected BetterPickingGraphMousePlugin<RegionNode, Edge<RegionNode>> createPickingPlugin() {
		return RegionCanvasUtils.createPickingPlugin(this);
	}

	@Override
	protected void paintGis(Graphics2D g, boolean toSvg, boolean onWhiteBackground) {
		super.paintGis(g, toSvg, onWhiteBackground);
		RegionCanvasUtils.paintRegions(g, nodes, getSelectedNodes(), nodeHighlightConditions);
	}

	@Override
	protected HighlightListDialog openNodeHighlightDialog() {
		HighlightListDialog dialog = super.openNodeHighlightDialog();

		dialog.setAllowInvisible(false);
		dialog.setAllowThickness(false);
		dialog.setAllowShape(false);

		return dialog;
	}

	@Override
	protected RegionNode createMetaNode(String id, Collection<RegionNode> nodes) {
		throw new UnsupportedOperationException();
	}
}
