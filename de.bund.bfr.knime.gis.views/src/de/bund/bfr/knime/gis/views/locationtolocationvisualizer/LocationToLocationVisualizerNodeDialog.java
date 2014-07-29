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
package de.bund.bfr.knime.gis.views.locationtolocationvisualizer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;

import de.bund.bfr.knime.KnimeUtilities;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.Canvas;
import de.bund.bfr.knime.gis.views.canvas.CanvasListener;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvas;

/**
 * <code>NodeDialog</code> for the "LocationToLocationVisualizer" Node.
 * 
 * @author Christian Thoens
 */
public class LocationToLocationVisualizerNodeDialog extends
		DataAwareNodeDialogPane implements ActionListener, ComponentListener,
		CanvasListener {

	private JPanel panel;
	private JSplitPane splitPane;
	private GraphCanvas graphCanvas;
	private LocationCanvas gisCanvas;

	private boolean resized;

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;

	private LocationToLocationVisualizerSettings set;

	/**
	 * New pane for configuring the LocationToLocationVisualizer node.
	 */
	protected LocationToLocationVisualizerNodeDialog() {
		set = new LocationToLocationVisualizerSettings();

		JButton inputButton = new JButton("Input");

		inputButton.addActionListener(this);

		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(UI.createWestPanel(UI.createEmptyBorderPanel(inputButton)),
				BorderLayout.NORTH);

		addTab("Options", panel, false);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input)
			throws NotConfigurableException {
		shapeTable = (BufferedDataTable) input[0];
		nodeTable = (BufferedDataTable) input[1];
		edgeTable = (BufferedDataTable) input[2];

		set.loadSettings(settings);

		if (input[3] != null) {
			try {
				set.loadFromXml(KnimeUtilities
						.tableToXml((BufferedDataTable) input[3]));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		updateSplitPane(false);
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
		LocationToLocationVisualizerInputDialog dialog = new LocationToLocationVisualizerInputDialog(
				(JButton) e.getSource(), shapeTable.getSpec(),
				nodeTable.getSpec(), edgeTable.getSpec(), set);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			updateSettings();
			updateSplitPane(true);
		}
	}

	@Override
	public void nodeSelectionChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setSelectedNodeIds(graphCanvas.getSelectedNodeIds());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setSelectedNodeIds(gisCanvas.getSelectedNodeIds());
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void edgeSelectionChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setSelectedEdgeIds(graphCanvas.getSelectedEdgeIds());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setSelectedEdgeIds(gisCanvas.getSelectedEdgeIds());
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void nodeHighlightingChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setNodeHighlightConditions(graphCanvas
					.getNodeHighlightConditions());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setNodeHighlightConditions(gisCanvas
					.getNodeHighlightConditions());
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void edgeHighlightingChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setEdgeHighlightConditions(graphCanvas
					.getEdgeHighlightConditions());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setEdgeHighlightConditions(gisCanvas
					.getEdgeHighlightConditions());
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void edgeJoinChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setJoinEdges(graphCanvas.isJoinEdges());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setJoinEdges(gisCanvas.isJoinEdges());
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void skipEdgelessChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setSkipEdgelessNodes(graphCanvas.isSkipEdgelessNodes());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setSkipEdgelessNodes(gisCanvas.isSkipEdgelessNodes());
			graphCanvas.addCanvasListener(this);
		}
	}

	private void updateSplitPane(boolean showWarning) {
		if (splitPane != null) {
			panel.remove(splitPane);
		}

		LocationToLocationVisualizerCanvasCreator creator = new LocationToLocationVisualizerCanvasCreator(
				shapeTable, nodeTable, edgeTable, set);

		graphCanvas = creator.createGraphCanvas();
		gisCanvas = creator.createLocationCanvas();

		if (graphCanvas != null && gisCanvas != null) {
			graphCanvas.addCanvasListener(this);
			gisCanvas.addCanvasListener(this);
		} else {
			graphCanvas = new GraphCanvas(false);
			graphCanvas.setCanvasSize(set.getGraphSettings().getCanvasSize());
			gisCanvas = new LocationCanvas(true);
			gisCanvas.setCanvasSize(set.getGisSettings().getCanvasSize());

			if (showWarning) {
				JOptionPane.showMessageDialog(panel,
						"Error reading nodes and edges", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}

		graphCanvas.addComponentListener(this);
		gisCanvas.addComponentListener(this);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphCanvas,
				gisCanvas);
		splitPane.setResizeWeight(0.5);
		panel.add(splitPane, BorderLayout.CENTER);
		panel.revalidate();
	}

	private void updateSettings() {
		List<String> selectedNodes = new ArrayList<>(
				graphCanvas.getSelectedNodeIds());
		List<String> selectedEdges = new ArrayList<>(
				graphCanvas.getSelectedEdgeIds());

		Collections.sort(selectedNodes);
		Collections.sort(selectedEdges);

		set.getGraphSettings().setShowLegend(graphCanvas.isShowLegend());
		set.getGraphSettings().setScaleX(graphCanvas.getScaleX());
		set.getGraphSettings().setScaleY(graphCanvas.getScaleY());
		set.getGraphSettings().setTranslationX(graphCanvas.getTranslationX());
		set.getGraphSettings().setTranslationY(graphCanvas.getTranslationY());
		set.getGraphSettings().setNodePositions(graphCanvas.getNodePositions());
		set.getGraphSettings().setNodeSize(graphCanvas.getNodeSize());
		set.getGraphSettings().setFontSize(graphCanvas.getFontSize());
		set.getGraphSettings().setFontBold(graphCanvas.isFontBold());
		set.getGraphSettings().setJoinEdges(graphCanvas.isJoinEdges());
		set.getGraphSettings().setSkipEdgelessNodes(
				graphCanvas.isSkipEdgelessNodes());
		set.getGraphSettings().setSelectedNodes(selectedNodes);
		set.getGraphSettings().setSelectedEdges(selectedEdges);
		set.getGraphSettings().setNodeHighlightConditions(
				graphCanvas.getNodeHighlightConditions());
		set.getGraphSettings().setEdgeHighlightConditions(
				graphCanvas.getEdgeHighlightConditions());
		set.getGraphSettings().setEditingMode(graphCanvas.getEditingMode());

		set.getGisSettings().setShowLegend(gisCanvas.isShowLegend());
		set.getGisSettings().setScaleX(gisCanvas.getScaleX());
		set.getGisSettings().setScaleY(gisCanvas.getScaleY());
		set.getGisSettings().setTranslationX(gisCanvas.getTranslationX());
		set.getGisSettings().setTranslationY(gisCanvas.getTranslationY());
		set.getGisSettings().setFontSize(gisCanvas.getFontSize());
		set.getGisSettings().setFontBold(gisCanvas.isFontBold());
		set.getGisSettings().setBorderAlpha(gisCanvas.getBorderAlpha());
		set.getGisSettings().setNodeSize(gisCanvas.getNodeSize());
		set.getGisSettings().setEditingMode(gisCanvas.getEditingMode());
		set.getGisSettings().setSelectedNodes(selectedNodes);
		set.getGisSettings().setNodeHighlightConditions(
				gisCanvas.getNodeHighlightConditions());

		if (resized) {
			set.getGraphSettings().setCanvasSize(graphCanvas.getCanvasSize());
			set.getGisSettings().setCanvasSize(gisCanvas.getCanvasSize());
		}
	}
}
