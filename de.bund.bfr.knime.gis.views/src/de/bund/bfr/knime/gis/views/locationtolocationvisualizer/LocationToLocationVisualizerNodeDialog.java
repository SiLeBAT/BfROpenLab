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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;

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
			Set<LocationNode> selectedGisNodes = new LinkedHashSet<>();
			Map<String, LocationNode> gisNodesById = new LinkedHashMap<>();

			for (LocationNode gisNode : gisCanvas.getNodes()) {
				gisNodesById.put(gisNode.getId(), gisNode);
			}

			for (String graphNodeId : graphCanvas.getSelectedNodeIds()) {
				selectedGisNodes.add(gisNodesById.get(graphNodeId));
			}

			gisCanvas.removeCanvasListener(this);
			gisCanvas.setSelectedNodes(selectedGisNodes);
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			Set<GraphNode> selectedGraphNodes = new LinkedHashSet<>();
			Map<String, GraphNode> graphNodesById = new LinkedHashMap<>();

			for (GraphNode graphNode : graphCanvas.getNodes()) {
				graphNodesById.put(graphNode.getId(), graphNode);
			}

			for (String gisNodeId : gisCanvas.getSelectedNodeIds()) {
				selectedGraphNodes.add(graphNodesById.get(gisNodeId));
			}

			graphCanvas.removeCanvasListener(this);
			graphCanvas.setSelectedNodes(selectedGraphNodes);
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void edgeSelectionChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			Set<Edge<LocationNode>> selectedGisEdges = new LinkedHashSet<>();
			Map<String, Edge<LocationNode>> gisEdgesById = new LinkedHashMap<>();

			for (Edge<LocationNode> gisEdge : gisCanvas.getEdges()) {
				gisEdgesById.put(gisEdge.getId(), gisEdge);
			}

			for (String graphEdgeId : graphCanvas.getSelectedEdgeIds()) {
				selectedGisEdges.add(gisEdgesById.get(graphEdgeId));
			}

			gisCanvas.removeCanvasListener(this);
			gisCanvas.setSelectedEdges(selectedGisEdges);
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			Set<Edge<GraphNode>> selectedGraphEdges = new LinkedHashSet<>();
			Map<String, Edge<GraphNode>> graphEdgesById = new LinkedHashMap<>();

			for (Edge<GraphNode> graphEdge : graphCanvas.getEdges()) {
				graphEdgesById.put(graphEdge.getId(), graphEdge);
			}

			for (String gisEdgeId : gisCanvas.getSelectedEdgeIds()) {
				selectedGraphEdges.add(graphEdgesById.get(gisEdgeId));
			}

			graphCanvas.removeCanvasListener(this);
			graphCanvas.setSelectedEdges(selectedGraphEdges);
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
			graphCanvas.setCanvasSize(set.getGraphCanvasSize());
			gisCanvas = new LocationCanvas(true);
			gisCanvas.setCanvasSize(set.getGisCanvasSize());

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
		List<String> selectedGraphNodes = new ArrayList<>(
				graphCanvas.getSelectedNodeIds());
		List<String> selectedGraphEdges = new ArrayList<>(
				graphCanvas.getSelectedEdgeIds());

		Collections.sort(selectedGraphNodes);
		Collections.sort(selectedGraphEdges);

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
		set.setGraphSelectedNodes(selectedGraphNodes);
		set.setGraphSelectedEdges(selectedGraphEdges);
		set.setGraphNodeHighlightConditions(graphCanvas
				.getNodeHighlightConditions());
		set.setGraphEdgeHighlightConditions(graphCanvas
				.getEdgeHighlightConditions());
		set.setGraphEditingMode(graphCanvas.getEditingMode());
		set.setGisShowLegend(gisCanvas.isShowLegend());
		set.setGisScaleX(gisCanvas.getScaleX());
		set.setGisScaleY(gisCanvas.getScaleY());
		set.setGisTranslationX(gisCanvas.getTranslationX());
		set.setGisTranslationY(gisCanvas.getTranslationY());
		set.setGisFontSize(gisCanvas.getFontSize());
		set.setGisFontBold(gisCanvas.isFontBold());
		set.setGisBorderAlpha(gisCanvas.getBorderAlpha());
		set.setGisLocationSize(gisCanvas.getNodeSize());
		set.setGisEditingMode(gisCanvas.getEditingMode());

		if (resized) {
			set.setGraphCanvasSize(graphCanvas.getCanvasSize());
			set.setGisCanvasSize(gisCanvas.getCanvasSize());
		}
	}
}
