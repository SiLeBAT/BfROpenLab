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
package de.bund.bfr.knime.openkrise.views.tracingview2;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Collections;
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

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.openkrise.MyNewTracing;
import de.bund.bfr.knime.openkrise.views.tracingview.TracingViewInputDialog;
import de.bund.bfr.knime.openkrise.views.tracingview.TracingViewSettings;

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
public class TracingView2NodeDialog extends DataAwareNodeDialogPane implements
		ActionListener, ComponentListener {

	private JPanel panel;
	private TracingCanvas graphCanvas;

	private boolean resized;

	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;
	private MyNewTracing tracing;

	private TracingView2Settings set;

	private JButton inputButton;
	private JButton forgetConfigButton;
	private JCheckBox enforceTempBox;

	/**
	 * New pane for configuring the TracingVisualizer node.
	 */
	protected TracingView2NodeDialog() {
		set = new TracingView2Settings();

		inputButton = new JButton("Input");
		inputButton.addActionListener(this);
		forgetConfigButton = new JButton("Forget Tracing Config");
		forgetConfigButton.addActionListener(this);
		enforceTempBox = new JCheckBox("Enforce Temporal Order");
		enforceTempBox.addActionListener(this);

		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(UI.createWestPanel(UI.createButtonPanel(inputButton,
				forgetConfigButton, enforceTempBox)), BorderLayout.NORTH);
		panel.addComponentListener(this);

		addTab("Options", panel);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input)
			throws NotConfigurableException {
		nodeTable = (BufferedDataTable) input[0];
		edgeTable = (BufferedDataTable) input[1];
		tracing = TracingView2NodeModel
				.getTracing((BufferedDataTable) input[2]);
		set.loadSettings(settings);
		enforceTempBox.setSelected(set.isEnforeTemporalOrder());
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
		if (e.getSource() == inputButton) {
			TracingViewInputDialog dialog = new TracingViewInputDialog(
					(JButton) e.getSource(), set);

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				updateSettings();
				updateGraphCanvas(true);
			}
		} else if (e.getSource() == forgetConfigButton) {
			updateSettings();
			set.getCaseWeights().clear();
			set.getCrossContaminations().clear();
			set.getFilter().clear();
			updateGraphCanvas(false);
		} else if (e.getSource() == enforceTempBox) {
			updateSettings();
			set.setEnforeTemporalOrder(enforceTempBox.isSelected());
			updateGraphCanvas(false);
		}
	}

	private void updateGraphCanvas(boolean showWarning) {
		if (graphCanvas != null) {
			panel.remove(graphCanvas);
		}

		TracingView2CanvasCreator creator = new TracingView2CanvasCreator(
				nodeTable, edgeTable, tracing, set);

		graphCanvas = creator.createGraphCanvas();

		if (graphCanvas == null) {
			graphCanvas = new TracingCanvas();
			graphCanvas
					.setCanvasSize(TracingViewSettings.DEFAULT_GRAPH_CANVAS_SIZE);
			graphCanvas.setLayoutType(TracingViewSettings.DEFAULT_GRAPH_LAYOUT);
			graphCanvas.setAllowCollapse(true);

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
		List<String> selectedGraphNodes = new ArrayList<String>(
				graphCanvas.getSelectedNodeIds());
		List<String> selectedGraphEdges = new ArrayList<String>(
				graphCanvas.getSelectedEdgeIds());

		Collections.sort(selectedGraphNodes);
		Collections.sort(selectedGraphEdges);

		set.setGraphScaleX(graphCanvas.getScaleX());
		set.setGraphScaleY(graphCanvas.getScaleY());
		set.setGraphTranslationX(graphCanvas.getTranslationX());
		set.setGraphTranslationY(graphCanvas.getTranslationY());
		set.setGraphNodePositions(graphCanvas.getNodePositions());
		set.setGraphLayout(graphCanvas.getLayoutType());
		set.setGraphNodeSize(graphCanvas.getNodeSize());
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

		if (resized) {
			set.setGraphCanvasSize(graphCanvas.getCanvasSize());
		}
	}
}
