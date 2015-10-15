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

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import de.bund.bfr.knime.gis.views.canvas.dialogs.SinglePropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class RegionCanvasPickingPlugin extends CanvasPickingPlugin<RegionNode> {

	public RegionCanvasPickingPlugin(GisCanvas<RegionNode> canvas) {
		super(canvas);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			VisualizationViewer<RegionNode, Edge<RegionNode>> viewer = canvas.getViewer();
			RegionNode node = getContainingNode(e.getX(), e.getY());
			Edge<RegionNode> edge = viewer.getPickSupport().getEdge(viewer.getGraphLayout(), e.getX(), e.getY());

			if (edge != null) {
				SinglePropertiesDialog dialog = new SinglePropertiesDialog(e.getComponent(), edge,
						canvas.getEdgeSchema());

				dialog.setVisible(true);
			} else if (node != null) {
				SinglePropertiesDialog dialog = new SinglePropertiesDialog(e.getComponent(), node,
						canvas.getNodeSchema());

				dialog.setVisible(true);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		VisualizationViewer<RegionNode, Edge<RegionNode>> viewer = canvas.getViewer();
		RegionNode node = getContainingNode(e.getX(), e.getY());
		Edge<RegionNode> edge = viewer.getPickSupport().getEdge(viewer.getGraphLayout(), e.getX(), e.getY());

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

	private RegionNode getContainingNode(int x, int y) {
		Point2D p = canvas.getTransform().applyInverse(x, y);

		for (RegionNode node : canvas.getNodes()) {
			if (node.containsPoint(p)) {
				return node;
			}
		}

		return null;
	}
}
