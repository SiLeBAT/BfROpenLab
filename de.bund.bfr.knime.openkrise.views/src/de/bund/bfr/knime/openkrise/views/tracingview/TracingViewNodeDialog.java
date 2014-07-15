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
	private TracingCanvas canvas;

	private boolean resized;

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

		resetWeightsButton = new JButton("Reset Case Weights");
		resetWeightsButton.addActionListener(this);
		resetCrossButton = new JButton("Reset Cross Contamination");
		resetCrossButton.addActionListener(this);
		resetFilterButton = new JButton("Reset Filters");
		resetFilterButton.addActionListener(this);
		exportAsSvgBox = new JCheckBox("Export As Svg");
		exportAsSvgBox.addActionListener(this);

		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(UI.createWestPanel(UI.createHorizontalPanel(
				resetWeightsButton, resetCrossButton, resetFilterButton,
				exportAsSvgBox)), BorderLayout.NORTH);
		panel.addComponentListener(this);

		addTab("Options", panel, false);
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

		exportAsSvgBox.setSelected(set.isExportAsSvg());
		updateGraphCanvas();
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
			set.getNodeFilter().clear();
			set.getEdgeFilter().clear();
			updateGraphCanvas();
		} else if (e.getSource() == exportAsSvgBox) {
			updateSettings();
		}
	}

	private void updateGraphCanvas() {
		if (canvas != null) {
			panel.remove(canvas);
		}

		TracingViewCanvasCreator creator = new TracingViewCanvasCreator(
				nodeTable, edgeTable, deliveries, set);

		canvas = creator.createGraphCanvas();

		if (canvas == null) {
			canvas = new TracingCanvas();
			canvas.setCanvasSize(TracingViewSettings.DEFAULT_GRAPH_CANVAS_SIZE);
		}

		panel.add(canvas, BorderLayout.CENTER);
		panel.revalidate();
	}

	private void updateSettings() {
		List<String> selectedGraphNodes = new ArrayList<>(
				canvas.getSelectedNodeIds());
		List<String> selectedGraphEdges = new ArrayList<>(
				canvas.getSelectedEdgeIds());

		Collections.sort(selectedGraphNodes);
		Collections.sort(selectedGraphEdges);

		set.setExportAsSvg(exportAsSvgBox.isSelected());

		set.setGraphShowLegend(canvas.isShowLegend());
		set.setGraphScaleX(canvas.getScaleX());
		set.setGraphScaleY(canvas.getScaleY());
		set.setGraphTranslationX(canvas.getTranslationX());
		set.setGraphTranslationY(canvas.getTranslationY());
		set.setGraphNodePositions(canvas.getNodePositions());
		set.setGraphNodeSize(canvas.getNodeSize());
		set.setGraphFontSize(canvas.getFontSize());
		set.setGraphFontBold(canvas.isFontBold());
		set.setJoinEdges(canvas.isJoinEdges());
		set.setSkipEdgelessNodes(canvas.isSkipEdgelessNodes());
		set.setCollapsedNodes(canvas.getCollapsedNodes());
		set.setGraphSelectedNodes(selectedGraphNodes);
		set.setGraphSelectedEdges(selectedGraphEdges);
		set.setGraphNodeHighlightConditions(canvas.getNodeHighlightConditions());
		set.setGraphEdgeHighlightConditions(canvas.getEdgeHighlightConditions());
		set.setGraphEditingMode(canvas.getEditingMode());
		set.setNodeWeights(canvas.getNodeWeights());
		set.setEdgeWeights(canvas.getEdgeWeights());
		set.setNodeCrossContaminations(canvas.getNodeCrossContaminations());
		set.setEdgeCrossContaminations(canvas.getEdgeCrossContaminations());
		set.setNodeFilter(canvas.getNodeFilter());
		set.setEdgeFilter(canvas.getEdgeFilter());
		set.setEnforeTemporalOrder(canvas.isEnforceTemporalOrder());
		set.setLabel(canvas.getLabel());

		if (resized) {
			set.setGraphCanvasSize(canvas.getCanvasSize());
		}
	}
}
