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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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

import de.bund.bfr.knime.KnimeUtilities;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.openkrise.MyDelivery;

/**
 * <code>NodeDialog</code> for the "TracingVisualizer" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Christian Thoens
 */
public class TracingViewNodeDialog extends DataAwareNodeDialogPane implements
		ActionListener, ComponentListener {

	private JPanel panel;
	private TracingCanvas graphCanvas;

	private boolean resized;

	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;
	private HashMap<Integer, MyDelivery> deliveries;

	private TracingViewSettings set;

	private JButton resetWeightsButton;
	private JButton resetCrossButton;
	private JButton resetFilterButton;
	private JCheckBox enforceTempBox;
	private JCheckBox exportAsSvgBox;

	/**
	 * New pane for configuring the TracingVisualizer node.
	 */
	protected TracingViewNodeDialog() {
		set = new TracingViewSettings();

		resetWeightsButton = new JButton("Reset Case Weights");
		resetWeightsButton.addActionListener(this);
		resetCrossButton = new JButton("Reset Cross Contamination");
		resetCrossButton.addActionListener(this);
		resetFilterButton = new JButton("Reset Filters");
		resetFilterButton.addActionListener(this);
		enforceTempBox = new JCheckBox("Enforce Temporal Order");
		enforceTempBox.addActionListener(this);
		exportAsSvgBox = new JCheckBox("Export As Svg");
		exportAsSvgBox.addActionListener(this);

		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(UI.createWestPanel(UI.createHorizontalPanel(
				resetWeightsButton, resetCrossButton, resetFilterButton,
				enforceTempBox, exportAsSvgBox)), BorderLayout.NORTH);
		panel.addComponentListener(this);

		addTab("Options", panel);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input)
			throws NotConfigurableException {
		nodeTable = (BufferedDataTable) input[0];
		edgeTable = (BufferedDataTable) input[1];
		deliveries = TracingViewNodeModel
				.getDeliveries((BufferedDataTable) input[2]);

		set.loadSettings(settings);

		if (input[3] != null) {
			try {
				set.loadFromXml(KnimeUtilities
						.tableToXml((BufferedDataTable) input[3]));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		enforceTempBox.setSelected(set.isEnforeTemporalOrder());
		exportAsSvgBox.setSelected(set.isExportAsSvg());
		updateGraphCanvas(false);
		resized = false;
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		updateSettings();
		set.saveSettings(settings);
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

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == resetWeightsButton) {
			updateSettings();
			set.getCaseWeights().clear();
			updateGraphCanvas(false);
		} else if (e.getSource() == resetCrossButton) {
			updateSettings();
			set.getCrossContaminations().clear();
			updateGraphCanvas(false);
		} else if (e.getSource() == resetFilterButton) {
			updateSettings();
			set.getFilter().clear();
			set.getEdgeFilter().clear();
			updateGraphCanvas(false);
		} else if (e.getSource() == enforceTempBox) {
			updateSettings();
			updateGraphCanvas(false);
		} else if (e.getSource() == exportAsSvgBox) {
			updateSettings();
		}
	}

	private void updateGraphCanvas(boolean showWarning) {
		if (graphCanvas != null) {
			panel.remove(graphCanvas);
		}

		TracingViewCanvasCreator creator = new TracingViewCanvasCreator(
				nodeTable, edgeTable, deliveries, set);

		graphCanvas = creator.createGraphCanvas();

		if (graphCanvas == null) {
			graphCanvas = new TracingCanvas();
			graphCanvas
					.setCanvasSize(TracingViewSettings.DEFAULT_GRAPH_CANVAS_SIZE);

			if (showWarning) {
				JOptionPane.showMessageDialog(panel,
						"Error reading nodes and edges", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}

		panel.add(graphCanvas, BorderLayout.CENTER);
		panel.revalidate();
	}

	private void updateSettings() {
		List<String> selectedGraphNodes = new ArrayList<>(
				graphCanvas.getSelectedNodeIds());
		List<String> selectedGraphEdges = new ArrayList<>(
				graphCanvas.getSelectedEdgeIds());

		Collections.sort(selectedGraphNodes);
		Collections.sort(selectedGraphEdges);

		set.setEnforeTemporalOrder(enforceTempBox.isSelected());
		set.setExportAsSvg(exportAsSvgBox.isSelected());

		set.setGraphShowLegend(graphCanvas.isShowLegend());
		set.setGraphScaleX(graphCanvas.getScaleX());
		set.setGraphScaleY(graphCanvas.getScaleY());
		set.setGraphTranslationX(graphCanvas.getTranslationX());
		set.setGraphTranslationY(graphCanvas.getTranslationY());
		set.setGraphNodePositions(graphCanvas.getNodePositions());
		set.setGraphNodeSize(graphCanvas.getNodeSize());
		set.setGraphFontSize(graphCanvas.getFontSize());
		set.setGraphFontBold(graphCanvas.isFontBold());
		set.setJoinEdges(graphCanvas.isJoinEdges());
		set.setSkipEdgelessNodes(graphCanvas.isSkipEdgelessNodes());
		set.setCollapsedNodes(graphCanvas.getCollapsedNodes());
		set.setGraphSelectedNodes(selectedGraphNodes);
		set.setGraphSelectedEdges(selectedGraphEdges);
		set.setGraphNodeHighlightConditions(graphCanvas
				.getNodeHighlightConditions());
		set.setGraphEdgeHighlightConditions(graphCanvas
				.getEdgeHighlightConditions());
		set.setGraphEditingMode(graphCanvas.getEditingMode());
		set.setCaseWeights(graphCanvas.getCaseWeights());
		set.setCrossContaminations(graphCanvas.getCrossContaminations());
		set.setFilter(graphCanvas.getFilter());
		set.setEdgeFilter(graphCanvas.getEdgeFilter());

		if (resized) {
			set.setGraphCanvasSize(graphCanvas.getCanvasSize());
		}
	}
}
