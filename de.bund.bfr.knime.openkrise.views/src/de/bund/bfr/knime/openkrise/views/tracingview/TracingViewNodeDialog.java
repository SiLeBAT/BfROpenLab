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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;

import de.bund.bfr.knime.NodeDialogWarningThread;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.openkrise.views.canvas.ITracingCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.TracingGisCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.TracingGraphCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.TracingOsmCanvas;
import de.bund.bfr.knime.openkrise.views.tracingview.TracingViewSettings.GisType;

/**
 * <code>NodeDialog</code> for the "TracingVisualizer" Node.
 * 
 * @author Christian Thoens
 */
public class TracingViewNodeDialog extends DataAwareNodeDialogPane implements
		ActionListener, ItemListener, ComponentListener {

	private JPanel panel;
	private ITracingCanvas<?> canvas;

	private boolean resized;

	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;
	private BufferedDataTable tracingTable;
	private BufferedDataTable shapeTable;

	private TracingViewSettings set;

	private JButton resetWeightsButton;
	private JButton resetCrossButton;
	private JButton resetFilterButton;
	private JCheckBox exportAsSvgBox;
	private JButton switchButton;
	private JComboBox<TracingViewSettings.GisType> gisBox;

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
		switchButton = new JButton();
		switchButton.addActionListener(this);
		gisBox = new JComboBox<>(TracingViewSettings.GisType.values());
		gisBox.addItemListener(this);

		JPanel northPanel = new JPanel();

		northPanel.setLayout(new BorderLayout());
		northPanel.add(UI.createHorizontalPanel(resetWeightsButton,
				resetCrossButton, resetFilterButton, exportAsSvgBox),
				BorderLayout.WEST);
		northPanel.add(UI.createHorizontalPanel(switchButton, new JLabel(
				"GIS Type:"), gisBox), BorderLayout.EAST);

		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(northPanel, BorderLayout.NORTH);

		addTab("Options", panel, false);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input)
			throws NotConfigurableException {
		nodeTable = (BufferedDataTable) input[0];
		edgeTable = (BufferedDataTable) input[1];
		tracingTable = (BufferedDataTable) input[2];
		shapeTable = (BufferedDataTable) input[3];
		set.loadSettings(settings);

		gisBox.removeItemListener(this);

		if (shapeTable == null) {
			if (set.getGisType() == TracingViewSettings.GisType.SHAPEFILE) {
				set.setGisType(TracingViewSettings.GisType.MAPNIK);
			}

			gisBox.removeItem(TracingViewSettings.GisType.SHAPEFILE);
		}

		gisBox.setSelectedItem(set.getGisType());
		gisBox.addItemListener(this);
		gisBox.setEnabled(set.isShowGis());
		exportAsSvgBox.setSelected(set.isExportAsSvg());
		resized = set.getCanvasSize() == null;
		panel.addComponentListener(this);

		String warning = updateCanvas();

		if (warning != null) {
			new Thread(new NodeDialogWarningThread(panel, warning)).start();
		}
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		updateSettings();
		set.saveSettings(settings);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		updateSettings();

		if (e.getSource() == resetWeightsButton) {
			set.getNodeWeights().clear();
			set.getEdgeWeights().clear();
		} else if (e.getSource() == resetCrossButton) {
			set.getNodeCrossContaminations().clear();
			set.getEdgeCrossContaminations().clear();
		} else if (e.getSource() == resetFilterButton) {
			set.getObservedNodes().clear();
			set.getObservedEdges().clear();
		} else if (e.getSource() == switchButton) {
			set.setShowGis(!set.isShowGis());
			gisBox.setEnabled(set.isShowGis());
		}

		try {
			updateCanvas();
		} catch (NotConfigurableException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == gisBox && e.getStateChange() == ItemEvent.SELECTED) {
			updateSettings();
			set.setGisType((GisType) gisBox.getSelectedItem());

			try {
				updateCanvas();
			} catch (NotConfigurableException ex) {
				ex.printStackTrace();
			}
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

	private String updateCanvas() throws NotConfigurableException {
		if (canvas != null) {
			panel.remove(canvas.getComponent());
		}

		TracingViewCanvasCreator creator = new TracingViewCanvasCreator(
				nodeTable, edgeTable, tracingTable, shapeTable, set);

		canvas = set.isShowGis() ? creator.createGisCanvas() : creator
				.createGraphCanvas();
		switchButton
				.setText("Switch to " + (set.isShowGis() ? "Graph" : "GIS"));

		String warningTable = null;

		if (!creator.getSkippedEdgeRows().isEmpty()
				&& !creator.getSkippedTracingRows().isEmpty()) {
			warningTable = "the delivery table and the tracing table";
		} else if (!creator.getSkippedEdgeRows().isEmpty()) {
			warningTable = "the delivery table";
		} else if (!creator.getSkippedTracingRows().isEmpty()) {
			warningTable = "the tracing table";
		}

		String warning = null;

		if (warningTable != null) {
			warning = "Some rows from " + warningTable
					+ " could not be imported."
					+ " Execute the Tracing View for more information.";
		}

		panel.add(canvas.getComponent(), BorderLayout.CENTER);
		panel.revalidate();

		return warning;
	}

	private void updateSettings() {
		set.setExportAsSvg(exportAsSvgBox.isSelected());
		set.setFromCanvas(canvas, resized);

		if (canvas instanceof TracingGraphCanvas) {
			set.getGraphSettings().setFromCanvas((TracingGraphCanvas) canvas);
		} else if (canvas instanceof TracingGisCanvas) {
			set.getGisSettings().setFromCanvas((TracingGisCanvas) canvas);
		} else if (canvas instanceof TracingOsmCanvas) {
			set.getOsmSettings().setFromCanvas((TracingOsmCanvas) canvas);
		}
	}
}
