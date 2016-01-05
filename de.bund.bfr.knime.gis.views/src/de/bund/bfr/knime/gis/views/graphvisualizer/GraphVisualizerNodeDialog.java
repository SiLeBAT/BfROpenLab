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
package de.bund.bfr.knime.gis.views.graphvisualizer;

import java.awt.BorderLayout;
import java.awt.Dimension;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;

import de.bund.bfr.knime.gis.views.VisualizerNodeDialog;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.util.Naming;
import de.bund.bfr.knime.ui.Dialogs;

/**
 * <code>NodeDialog</code> for the "GraphVisualizer" Node.
 * 
 * @author Christian Thoens
 */
public class GraphVisualizerNodeDialog extends VisualizerNodeDialog {

	private GraphCanvas canvas;

	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;

	private GraphVisualizerSettings set;

	/**
	 * New pane for configuring the GraphVisualizer node.
	 */
	protected GraphVisualizerNodeDialog() {
		set = new GraphVisualizerSettings();
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input) throws NotConfigurableException {
		nodeTable = (BufferedDataTable) input[0];
		edgeTable = (BufferedDataTable) input[1];
		set.getGraphSettings().loadSettings(settings);

		updateCanvas(false);
		resized = false;
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		set.getGraphSettings().setFromCanvas(canvas, resized);
		set.getGraphSettings().saveSettings(settings);
	}

	@Override
	protected void inputButtonPressed() {
		GraphVisualizerInputDialog dialog = new GraphVisualizerInputDialog(inputButton, nodeTable.getSpec(),
				edgeTable.getSpec(), set);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			set.getGraphSettings().setFromCanvas(canvas, resized);
			updateCanvas(true);
		}
	}

	private void updateCanvas(boolean showWarning) {
		if (canvas != null) {
			panel.remove(canvas);
		}

		GraphVisualizerCanvasCreator creator = new GraphVisualizerCanvasCreator(nodeTable, edgeTable, set);

		try {
			canvas = creator.createGraphCanvas();
		} catch (NotConfigurableException e) {
			canvas = new GraphCanvas(true, Naming.DEFAULT_NAMING);
			canvas.setCanvasSize(new Dimension(400, 600));

			if (showWarning) {
				Dialogs.showErrorMessage(panel, e.getMessage(), "Error");
			}
		}

		panel.add(canvas, BorderLayout.CENTER);
		panel.revalidate();
	}
}
