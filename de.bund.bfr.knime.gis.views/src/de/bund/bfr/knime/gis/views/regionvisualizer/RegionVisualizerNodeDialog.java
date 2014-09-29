/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
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
package de.bund.bfr.knime.gis.views.regionvisualizer;

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
import org.knime.core.node.port.PortObject;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.RegionCanvas;

/**
 * <code>NodeDialog</code> for the "RegionVisualizer" Node.
 * 
 * @author Christian Thoens
 */
public class RegionVisualizerNodeDialog extends DataAwareNodeDialogPane
		implements ActionListener, ComponentListener {

	private JPanel panel;
	private RegionCanvas canvas;

	private boolean resized;

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;

	private RegionVisualizerSettings set;

	/**
	 * New pane for configuring the RegionVisualizer node.
	 */
	protected RegionVisualizerNodeDialog() {
		set = new RegionVisualizerSettings();

		JButton inputButton = new JButton("Input");

		inputButton.addActionListener(this);

		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(UI.createWestPanel(UI.createEmptyBorderPanel(inputButton)),
				BorderLayout.NORTH);
		panel.addComponentListener(this);

		addTab("Options", panel, false);
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
		set.getGisSettings().setFromCanvas(canvas, resized);
		set.saveSettings(settings);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		RegionVisualizerInputDialog dialog = new RegionVisualizerInputDialog(
				(JButton) e.getSource(), shapeTable.getSpec(),
				nodeTable.getSpec(), set);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			set.getGisSettings().setFromCanvas(canvas, resized);
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
		if (canvas != null) {
			panel.remove(canvas);
		}

		RegionVisualizerCanvasCreator creator = new RegionVisualizerCanvasCreator(
				shapeTable, nodeTable, set);

		try {
			canvas = creator.createCanvas();

			if (showWarning && !creator.getNonExistingRegions().isEmpty()) {
				JOptionPane.showMessageDialog(panel,
						"Some regions from the table are not contained"
								+ " in the shapefile", "Warning",
						JOptionPane.WARNING_MESSAGE);
			}
		} catch (InvalidSettingsException e) {
			canvas = new RegionCanvas(false);
			canvas.setCanvasSize(set.getGisSettings().getCanvasSize());

			if (showWarning) {
				JOptionPane.showMessageDialog(panel, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}

		panel.add(canvas, BorderLayout.CENTER);
		panel.revalidate();
	}
}
