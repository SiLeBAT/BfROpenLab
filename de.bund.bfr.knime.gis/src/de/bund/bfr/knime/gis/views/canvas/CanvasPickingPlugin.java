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

import java.awt.event.MouseEvent;

import de.bund.bfr.jung.BetterPickingGraphMousePlugin;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SinglePropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class CanvasPickingPlugin<V extends Node> extends BetterPickingGraphMousePlugin<V, Edge<V>> {

	protected ICanvas<V> canvas;

	public CanvasPickingPlugin(ICanvas<V> canvas) {
		this.canvas = canvas;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			VisualizationViewer<V, Edge<V>> viewer = canvas.getViewer();
			V node = viewer.getPickSupport().getVertex(viewer.getGraphLayout(), e.getX(), e.getY());
			Edge<V> edge = viewer.getPickSupport().getEdge(viewer.getGraphLayout(), e.getX(), e.getY());

			if (node != null) {
				SinglePropertiesDialog dialog = new SinglePropertiesDialog(e.getComponent(), node,
						canvas.getNodeSchema());

				dialog.setVisible(true);
			} else if (edge != null) {
				SinglePropertiesDialog dialog = new SinglePropertiesDialog(e.getComponent(), edge,
						canvas.getEdgeSchema());

				dialog.setVisible(true);
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (vertex == null || !(canvas instanceof IGisCanvas)) {
			super.mouseDragged(e);
		}
	}
}
