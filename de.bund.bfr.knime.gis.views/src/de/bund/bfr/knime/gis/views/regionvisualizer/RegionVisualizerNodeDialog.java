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
package de.bund.bfr.knime.gis.views.regionvisualizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;

import de.bund.bfr.knime.gis.views.VisualizerNodeDialog;
import de.bund.bfr.knime.gis.views.canvas.GisCanvas;
import de.bund.bfr.knime.gis.views.canvas.Naming;
import de.bund.bfr.knime.gis.views.canvas.RegionCanvas;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;

/**
 * <code>NodeDialog</code> for the "RegionVisualizer" Node.
 * 
 * @author Christian Thoens
 */
public class RegionVisualizerNodeDialog extends VisualizerNodeDialog {

	private GisCanvas<RegionNode> canvas;

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;

	private RegionVisualizerSettings set;

	/**
	 * New pane for configuring the RegionVisualizer node.
	 */
	protected RegionVisualizerNodeDialog() {
		set = new RegionVisualizerSettings();
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input) throws NotConfigurableException {
		shapeTable = (BufferedDataTable) input[0];
		nodeTable = (BufferedDataTable) input[1];
		set.loadSettings(settings);
		updateGisCanvas(false);
		resized = set.getGisSettings().getCanvasSize() == null;
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		set.getGisSettings().setFromCanvas(canvas, resized);
		set.saveSettings(settings);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		RegionVisualizerInputDialog dialog = new RegionVisualizerInputDialog((JButton) e.getSource(),
				shapeTable.getSpec(), nodeTable.getSpec(), set);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			set.getGisSettings().setFromCanvas(canvas, resized);
			updateGisCanvas(true);
		}
	}

	private void updateGisCanvas(boolean showWarning) {
		if (canvas != null) {
			panel.remove(canvas);
		}

		RegionVisualizerCanvasCreator creator = new RegionVisualizerCanvasCreator(shapeTable, nodeTable, set);

		try {
			canvas = creator.createCanvas();

			if (showWarning && !creator.getNonExistingRegions().isEmpty()) {
				JOptionPane.showMessageDialog(panel,
						"Some regions from the table are not contained" + " in the shapefile", "Warning",
						JOptionPane.WARNING_MESSAGE);
			}
		} catch (InvalidSettingsException e) {
			canvas = new RegionCanvas(false, Naming.DEFAULT_NAMING);
			canvas.setCanvasSize(new Dimension(400, 600));

			if (showWarning) {
				JOptionPane.showMessageDialog(panel, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}

		panel.add(canvas, BorderLayout.CENTER);
		panel.revalidate();
	}
}
