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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;

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
import org.knime.core.node.port.PortObject;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvas;
import de.bund.bfr.knime.openkrise.MyDelivery;
import de.bund.bfr.knime.openkrise.TracingUtils;
import de.bund.bfr.knime.openkrise.views.canvas.ITracingCanvas;

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

		JPanel northPanel = new JPanel();

		northPanel.setLayout(new BorderLayout());
		northPanel.add(UI.createHorizontalPanel(resetWeightsButton,
				resetCrossButton, resetFilterButton, exportAsSvgBox),
				BorderLayout.WEST);
		northPanel.add(UI.createHorizontalPanel(switchButton),
				BorderLayout.EAST);

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
		deliveries = TracingUtils.getDeliveries((BufferedDataTable) input[2],
				edgeTable);
		shapeTable = (BufferedDataTable) input[3];
		set.loadSettings(settings);

		if (shapeTable == null) {
			set.setShowGis(false);
			switchButton.setEnabled(false);
		} else {
			switchButton.setEnabled(true);
		}

		exportAsSvgBox.setSelected(set.isExportAsSvg());
		resized = false;
		panel.addComponentListener(this);

		final String warning = updateCanvas();
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					Window window = SwingUtilities.getWindowAncestor(panel);

					if (window != null && window.isActive()) {
						JOptionPane.showMessageDialog(panel, warning,
								"Warning", JOptionPane.WARNING_MESSAGE);
						break;
					}

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		if (warning != null) {
			thread.start();
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
		}

		try {
			updateCanvas();
		} catch (NotConfigurableException ex) {
			ex.printStackTrace();
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
				nodeTable, edgeTable, shapeTable, deliveries, set);

		canvas = set.isShowGis() ? creator.createGisCanvas() : creator
				.createGraphCanvas();
		switchButton
				.setText("Switch to " + (set.isShowGis() ? "Graph" : "GIS"));

		String warning = null;

		if (!creator.getSkippedEdgeRows().isEmpty()) {
			warning = "Some rows from the delivery table could not be imported."
					+ " Execute the Tracing View for more information.";
		}

		panel.add(canvas.getComponent(), BorderLayout.CENTER);
		panel.revalidate();

		return warning;
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
