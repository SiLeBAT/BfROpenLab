/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.gis.views.locationvisualizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;

import de.bund.bfr.knime.gis.GisType;
import de.bund.bfr.knime.gis.views.VisualizerNodeDialog;
import de.bund.bfr.knime.gis.views.canvas.GisCanvas;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvas;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.util.Naming;
import de.bund.bfr.knime.ui.Dialogs;

/**
 * <code>NodeDialog</code> for the "LocationVisualizer" Node.
 * 
 * @author Christian Thoens
 */
public class LocationVisualizerNodeDialog extends VisualizerNodeDialog {

	private GisCanvas<LocationNode> canvas;

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;

	private LocationVisualizerSettings set;

	/**
	 * New pane for configuring the LocationVisualizer node.
	 */
	protected LocationVisualizerNodeDialog() {
		set = new LocationVisualizerSettings();
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input) throws NotConfigurableException {
		shapeTable = (BufferedDataTable) input[0];
		nodeTable = (BufferedDataTable) input[1];
		set.loadSettings(settings);

		if (shapeTable == null && set.getGisSettings().getGisType() == GisType.SHAPEFILE) {
			set.getGisSettings().setGisType(GisType.MAPNIK);
		}

		updateGisCanvas(false);
		resized = false;
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		set.getGisSettings().setFromCanvas(canvas, resized);
		set.saveSettings(settings);
	}

	@Override
	protected void inputButtonPressed() {
		LocationVisualizerInputDialog dialog = new LocationVisualizerInputDialog(inputButton,
				shapeTable != null ? shapeTable.getSpec() : null, nodeTable.getSpec(), set);

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

		LocationVisualizerCanvasCreator creator = new LocationVisualizerCanvasCreator(shapeTable, nodeTable, set);

		try {
			canvas = creator.createCanvas();
		} catch (NotConfigurableException e) {
			canvas = new LocationCanvas(false, Naming.DEFAULT_NAMING);
			canvas.setCanvasSize(new Dimension(400, 600));

			if (showWarning) {
				Dialogs.showErrorMessage(panel, e.getMessage());
			}
		}

		panel.add(canvas, BorderLayout.CENTER);
		panel.revalidate();
	}
}
