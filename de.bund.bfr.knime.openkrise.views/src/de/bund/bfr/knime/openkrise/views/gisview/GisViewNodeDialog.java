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
package de.bund.bfr.knime.openkrise.views.gisview;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvas;
import de.bund.bfr.knime.openkrise.TracingUtils;
import de.bund.bfr.knime.ui.Dialogs;

/**
 * <code>NodeDialog</code> for the "GisView" Node.
 * 
 * @author Christian Thoens
 */
public class GisViewNodeDialog extends DataAwareNodeDialogPane {

	private JPanel panel;
	private LocationCanvas canvas;

	private ResizeListener listener;

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;

	private GisViewSettings set;

	private JCheckBox exportAsSvgBox;

	/**
	 * New pane for configuring the GisView node.
	 */
	protected GisViewNodeDialog() {
		set = new GisViewSettings();
		exportAsSvgBox = new JCheckBox("Export As Svg");

		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(UI.createWestPanel(UI.createHorizontalPanel(exportAsSvgBox)), BorderLayout.NORTH);

		addTab("Options", panel, false);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input) throws NotConfigurableException {
		shapeTable = (BufferedDataTable) input[0];
		nodeTable = (BufferedDataTable) input[1];
		set.loadSettings(settings);
		exportAsSvgBox.setSelected(set.isExportAsSvg());
		listener = new ResizeListener();
		panel.addComponentListener(listener);
		updateGisCanvas(false);
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		set.setExportAsSvg(exportAsSvgBox.isSelected());
		set.getGisSettings().setFromCanvas(canvas, listener.isResized());
		set.saveSettings(settings);
	}

	private void updateGisCanvas(boolean showWarning) {
		if (canvas != null) {
			panel.remove(canvas);
		}

		GisViewCanvasCreator creator = new GisViewCanvasCreator(shapeTable, nodeTable, set);

		try {
			canvas = creator.createCanvas();
		} catch (NotConfigurableException e) {
			canvas = new LocationCanvas(false, TracingUtils.NAMING);
			canvas.setCanvasSize(new Dimension(400, 600));

			if (showWarning) {
				Dialogs.showErrorMessage(panel, e.getMessage(), "Error");
			}
		}

		panel.add(canvas, BorderLayout.CENTER);
		panel.revalidate();
	}
}
