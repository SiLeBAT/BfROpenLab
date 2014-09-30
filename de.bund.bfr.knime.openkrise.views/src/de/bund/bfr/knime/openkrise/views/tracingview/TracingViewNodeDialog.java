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
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.openkrise.MyDelivery;
import de.bund.bfr.knime.openkrise.TracingUtils;
import de.bund.bfr.knime.openkrise.views.ResizeListener;

/**
 * <code>NodeDialog</code> for the "TracingVisualizer" Node.
 * 
 * @author Christian Thoens
 */
public class TracingViewNodeDialog extends DataAwareNodeDialogPane implements
		ActionListener {

	private JPanel panel;
	private TracingCanvas canvas;

	private ResizeListener listener;

	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;
	private HashMap<Integer, MyDelivery> deliveries;

	private TracingViewSettings set;

	private JButton resetWeightsButton;
	private JButton resetCrossButton;
	private JButton resetFilterButton;
	private JCheckBox exportAsSvgBox;

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

		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(UI.createWestPanel(UI.createHorizontalPanel(
				resetWeightsButton, resetCrossButton, resetFilterButton,
				exportAsSvgBox)), BorderLayout.NORTH);

		addTab("Options", panel, false);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input)
			throws NotConfigurableException {
		nodeTable = (BufferedDataTable) input[0];
		edgeTable = (BufferedDataTable) input[1];
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
		listener = new ResizeListener();
		panel.addComponentListener(listener);
		updateGraphCanvas();
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
			updateGraphCanvas();
		} else if (e.getSource() == resetCrossButton) {
			updateSettings();
			set.getNodeCrossContaminations().clear();
			set.getEdgeCrossContaminations().clear();
			updateGraphCanvas();
		} else if (e.getSource() == resetFilterButton) {
			updateSettings();
			set.getObservedNodes().clear();
			set.getObservedEdges().clear();
			updateGraphCanvas();
		}
	}

	private void updateGraphCanvas() {
		if (canvas != null) {
			panel.remove(canvas);
		}

		TracingViewCanvasCreator creator = new TracingViewCanvasCreator(
				nodeTable, edgeTable, deliveries, set);

		try {
			canvas = creator.createGraphCanvas();
		} catch (InvalidSettingsException e) {
			canvas = new TracingCanvas();
			canvas.setCanvasSize(set.getGraphSettings().getCanvasSize());
		}

		panel.add(canvas, BorderLayout.CENTER);
		panel.revalidate();
	}

	private void updateSettings() {
		set.getGraphSettings().setFromCanvas(canvas, listener.isResized());
		set.setNodeWeights(canvas.getNodeWeights());
		set.setEdgeWeights(canvas.getEdgeWeights());
		set.setNodeCrossContaminations(canvas.getNodeCrossContaminations());
		set.setEdgeCrossContaminations(canvas.getEdgeCrossContaminations());
		set.setObservedNodes(canvas.getObservedNodes());
		set.setObservedEdges(canvas.getObservedEdges());
		set.setEnforeTemporalOrder(canvas.isEnforceTemporalOrder());
		set.setShowForward(canvas.isShowForward());
		set.setLabel(canvas.getLabel());
	}
}
