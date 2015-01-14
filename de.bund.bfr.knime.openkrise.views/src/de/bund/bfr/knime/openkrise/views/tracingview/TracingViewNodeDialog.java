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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvas;
import de.bund.bfr.knime.openkrise.MyDelivery;
import de.bund.bfr.knime.openkrise.TracingUtils;
import de.bund.bfr.knime.openkrise.views.canvas.ITracingCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.TracingGisCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.TracingGraphCanvas;

/**
 * <code>NodeDialog</code> for the "TracingVisualizer" Node.
 * 
 * @author Christian Thoens
 */
public class TracingViewNodeDialog extends DataAwareNodeDialogPane implements
		ActionListener, ComponentListener {

	private JPanel panel;
	private ITracingCanvas<?> canvas;

	private boolean resized;

	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;
	private BufferedDataTable shapeTable;
	private HashMap<Integer, MyDelivery> deliveries;

	private TracingViewSettings set;

	private JButton resetWeightsButton;
	private JButton resetCrossButton;
	private JButton resetFilterButton;
	private JCheckBox exportAsSvgBox;
	private JButton switchButton;

	private boolean showGis;

	/**
	 * New pane for configuring the TracingVisualizer node.
	 */
	protected TracingViewNodeDialog() {
		set = new TracingViewSettings();

		resetWeightsButton = new JButton("Reset Weights");
		resetWeightsButton.addActionListener(this);
		resetCrossButton = new JButton("Reset Cross Contamination");
		resetCrossButton.addActionListener(this);
		resetFilterButton = new JButton("Reset Observed");
		resetFilterButton.addActionListener(this);
		exportAsSvgBox = new JCheckBox("Export As Svg");
		switchButton = new JButton("Switch to Graph/GIS");
		switchButton.addActionListener(this);

		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(UI.createWestPanel(UI.createHorizontalPanel(
				resetWeightsButton, resetCrossButton, resetFilterButton,
				exportAsSvgBox, switchButton)), BorderLayout.NORTH);

		addTab("Options", panel, false);
		showGis = false;
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input)
			throws NotConfigurableException {
		nodeTable = (BufferedDataTable) input[0];
		edgeTable = (BufferedDataTable) input[1];
		shapeTable = (BufferedDataTable) input[4];
		deliveries = TracingUtils.getDeliveries((BufferedDataTable) input[2],
				edgeTable);

		set.loadSettings(settings);

		if (input[3] != null) {
			try {
				set.loadFromXml(KnimeUtils
						.tableToXml((BufferedDataTable) input[3]));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		exportAsSvgBox.setSelected(set.isExportAsSvg());
		resized = false;
		panel.addComponentListener(this);
		updateCanvas();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		updateSettings();
		set.saveSettings(settings);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == resetWeightsButton) {
			updateSettings();
			set.getNodeWeights().clear();
			set.getEdgeWeights().clear();
			updateCanvas();
		} else if (e.getSource() == resetCrossButton) {
			updateSettings();
			set.getNodeCrossContaminations().clear();
			set.getEdgeCrossContaminations().clear();
			updateCanvas();
		} else if (e.getSource() == resetFilterButton) {
			updateSettings();
			set.getObservedNodes().clear();
			set.getObservedEdges().clear();
			updateCanvas();
		} else if (e.getSource() == switchButton) {
			updateSettings();
			showGis = !showGis;
			updateCanvas();
		}
	}

	@Override
	public void componentResized(ComponentEvent e) {
		if (SwingUtilities.getWindowAncestor(e.getComponent()).isActive()) {
			resized = true;
		}
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	private void updateCanvas() {
		if (canvas != null) {
			panel.remove(canvas.getComponent());
		}

		TracingViewCanvasCreator creator = new TracingViewCanvasCreator(
				nodeTable, edgeTable, shapeTable, deliveries, set);

		try {
			canvas = showGis ? creator.createGisCanvas() : creator
					.createGraphCanvas();
		} catch (InvalidSettingsException e) {
			canvas = showGis ? new TracingGisCanvas()
					: new TracingGraphCanvas();
			canvas.setCanvasSize(set.getCanvasSize());
		}

		panel.add(canvas.getComponent(), BorderLayout.CENTER);
		panel.revalidate();
	}

	private void updateSettings() {
		set.setExportAsSvg(exportAsSvgBox.isSelected());
		set.setFromCanvas(canvas, resized);

		if (canvas instanceof GraphCanvas) {
			set.getGraphSettings().setFromCanvas((GraphCanvas) canvas);
		} else if (canvas instanceof LocationCanvas) {
			set.getGisSettings().setFromCanvas((LocationCanvas) canvas);
		}
	}
}
