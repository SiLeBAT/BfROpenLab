/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
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
package de.bund.bfr.knime.gis.views.locationvisualizer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.port.PortObject;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.GisVisualizerSettings;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvas;

/**
 * <code>NodeDialog</code> for the "LocationVisualizer" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Christian Thoens
 */
public class LocationVisualizerNodeDialog extends DataAwareNodeDialogPane
		implements ActionListener, ComponentListener {

	private JPanel panel;
	private LocationCanvas gisCanvas;

	private boolean resized;

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;

	private LocationVisualizerSettings set;

	/**
	 * New pane for configuring the LocationVisualizer node.
	 */
	protected LocationVisualizerNodeDialog() {
		set = new LocationVisualizerSettings();

		JButton inputButton = new JButton("Input");

		inputButton.addActionListener(this);

		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(UI.createWestPanel(UI.createEmptyBorderPanel(inputButton)),
				BorderLayout.NORTH);
		panel.addComponentListener(this);

		addTab("Options", panel);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input)
			throws NotConfigurableException {
		shapeTable = (BufferedDataTable) input[0];
		nodeTable = (BufferedDataTable) input[1];
		set.loadSettings(settings);
		updateGisCanvas(false);
		resized = false;
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		updateSettings();
		set.saveSettings(settings);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		LocationVisualizerInputDialog dialog = new LocationVisualizerInputDialog(
				(JButton) e.getSource(), shapeTable.getSpec(),
				nodeTable.getSpec(), set);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			updateSettings();
			updateGisCanvas(true);
		}
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		if (SwingUtilities.getWindowAncestor(panel).isActive()) {
			resized = true;
		}
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	private void updateGisCanvas(boolean showWarning) {
		if (gisCanvas != null) {
			panel.remove(gisCanvas);
		}

		LocationVisualizerCanvasCreator creator = new LocationVisualizerCanvasCreator(
				shapeTable, nodeTable, set);

		gisCanvas = creator.createCanvas();

		if (gisCanvas == null) {
			gisCanvas = new LocationCanvas(false);
			gisCanvas.setCanvasSize(GisVisualizerSettings.DEFAULT_CANVAS_SIZE);

			if (showWarning) {
				JOptionPane.showMessageDialog(panel,
						"Error reading nodes and edges", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}

		panel.add(gisCanvas, BorderLayout.CENTER);
		panel.revalidate();
	}

	private void updateSettings() {
		set.setShowLegend(gisCanvas.isShowLegend());
		set.setScaleX(gisCanvas.getScaleX());
		set.setScaleY(gisCanvas.getScaleY());
		set.setTranslationX(gisCanvas.getTranslationX());
		set.setTranslationY(gisCanvas.getTranslationY());
		set.setBorderAlpha(gisCanvas.getBorderAlpha());
		set.setLocationSize(gisCanvas.getLocationSize());
		set.setNodeHighlightConditions(gisCanvas.getNodeHighlightConditions());

		if (resized) {
			set.setCanvasSize(gisCanvas.getCanvasSize());
		}
	}

}
