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
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.port.PortObject;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.GisToGisVisualizerSettings;
import de.bund.bfr.knime.gis.views.SimpleGraphVisualizerSettings;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvas;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.listener.HighlightListener;
import de.bund.bfr.knime.gis.views.canvas.listener.SelectionListener;

/**
 * <code>NodeDialog</code> for the "LocationToLocationVisualizer" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Christian Thoens
 */
public class LocationToLocationVisualizerNodeDialog extends
		DataAwareNodeDialogPane implements ActionListener, ComponentListener {

	private JPanel panel;
	private JSplitPane splitPane;
	private GraphCanvas graphCanvas;
	private LocationCanvas gisCanvas;

	private boolean resized;

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;

	private LocationToLocationVisualizerSettings set;

	private SelectionListener<GraphNode> graphSelectionListener;
	private SelectionListener<LocationNode> gisSelectionListener;
	private HighlightListener graphHighlightListener;
	private HighlightListener gisHighlightListener;

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
		panel.addComponentListener(this);

		addTab("Options", panel);
		createListeners();
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input)
			throws NotConfigurableException {
		shapeTable = (BufferedDataTable) input[0];
		nodeTable = (BufferedDataTable) input[1];
		edgeTable = (BufferedDataTable) input[2];
		set.loadSettings(settings);
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

	private void updateSplitPane(boolean showWarning) {
		if (splitPane != null) {
			panel.remove(splitPane);
		}

		LocationToLocationVisualizerCanvasCreator creator = new LocationToLocationVisualizerCanvasCreator(
				shapeTable, nodeTable, edgeTable, set);

		graphCanvas = creator.createGraphCanvas();
		gisCanvas = creator.createLocationCanvas();

		if (graphCanvas != null && gisCanvas != null) {
			graphCanvas.addSelectionListener(graphSelectionListener);
			graphCanvas.addHighlightListener(graphHighlightListener);
			gisCanvas.addSelectionListener(gisSelectionListener);
			gisCanvas.addHighlightListener(gisHighlightListener);
		} else {
			graphCanvas = new GraphCanvas(new ArrayList<GraphNode>(),
					new ArrayList<Edge<GraphNode>>(),
					new LinkedHashMap<String, Class<?>>(),
					new LinkedHashMap<String, Class<?>>(), null);
			graphCanvas
					.setCanvasSize(SimpleGraphVisualizerSettings.DEFAULT_GRAPH_CANVAS_SIZE);
			graphCanvas
					.setLayoutType(SimpleGraphVisualizerSettings.DEFAULT_GRAPH_LAYOUT);
			graphCanvas.setAllowCollapse(false);
			gisCanvas = new LocationCanvas(new ArrayList<LocationNode>(),
					new ArrayList<Edge<LocationNode>>(),
					new LinkedHashMap<String, Class<?>>(),
					new LinkedHashMap<String, Class<?>>(),
					new ArrayList<RegionNode>());
			gisCanvas
					.setCanvasSize(GisToGisVisualizerSettings.DEFAULT_GIS_CANVAS_SIZE);

			if (showWarning) {
				JOptionPane.showMessageDialog(panel,
						"Error reading nodes and edges", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphCanvas,
				gisCanvas);
		splitPane.setResizeWeight(0.5);
		panel.add(splitPane, BorderLayout.CENTER);
		panel.revalidate();
	}

	private void updateSettings() {
		List<String> selectedGraphNodes = new ArrayList<String>(
				graphCanvas.getSelectedNodeIds());
		List<String> selectedGraphEdges = new ArrayList<String>(
				graphCanvas.getSelectedEdgeIds());
		List<String> selectedGisNodes = new ArrayList<String>(
				gisCanvas.getSelectedNodeIds());
		List<String> selectedGisEdges = new ArrayList<String>(
				gisCanvas.getSelectedEdgeIds());

		Collections.sort(selectedGraphNodes);
		Collections.sort(selectedGraphEdges);
		Collections.sort(selectedGisNodes);
		Collections.sort(selectedGisEdges);

		set.setGraphScaleX(graphCanvas.getScaleX());
		set.setGraphScaleY(graphCanvas.getScaleY());
		set.setGraphTranslationX(graphCanvas.getTranslationX());
		set.setGraphTranslationY(graphCanvas.getTranslationY());
		set.setGraphNodePositions(graphCanvas.getNodePositions());
		set.setGraphLayout(graphCanvas.getLayoutType());
		set.setGraphNodeSize(graphCanvas.getNodeSize());
		set.setGraphSelectedNodes(selectedGraphNodes);
		set.setGraphSelectedEdges(selectedGraphEdges);
		set.setGraphNodeHighlightConditions(graphCanvas
				.getNodeHighlightConditions());
		set.setGraphEdgeHighlightConditions(graphCanvas
				.getEdgeHighlightConditions());
		set.setGraphEditingMode(graphCanvas.getEditingMode());
		set.setGisScaleX(gisCanvas.getScaleX());
		set.setGisScaleY(gisCanvas.getScaleY());
		set.setGisTranslationX(gisCanvas.getTranslationX());
		set.setGisTranslationY(gisCanvas.getTranslationY());
		set.setGisBorderAlpha(gisCanvas.getBorderAlpha());
		set.setGisLocationSize(gisCanvas.getLocationSize());
		set.setGisSelectedNodes(selectedGisNodes);
		set.setGisSelectedEdges(selectedGisEdges);
		set.setGisNodeHighlightConditions(gisCanvas
				.getNodeHighlightConditions());
		set.setGisEdgeHighlightConditions(gisCanvas
				.getEdgeHighlightConditions());
		set.setGisEditingMode(gisCanvas.getEditingMode());

		if (resized) {
			set.setGraphCanvasSize(graphCanvas.getCanvasSize());
			set.setGisCanvasSize(gisCanvas.getCanvasSize());
		}
	}

	private void createListeners() {
		graphSelectionListener = new SelectionListener<GraphNode>() {

			@Override
			public void nodeSelectionChanged(Set<GraphNode> selectedNodes) {
				Set<LocationNode> selectedGisNodes = new LinkedHashSet<LocationNode>();
				Map<String, LocationNode> gisNodesById = new LinkedHashMap<String, LocationNode>();

				for (LocationNode gisNode : gisCanvas.getNodes()) {
					gisNodesById.put(gisNode.getId(), gisNode);
				}

				for (GraphNode graphNode : selectedNodes) {
					selectedGisNodes.add(gisNodesById.get(graphNode.getId()));
				}

				gisCanvas.removeSelectionListener(gisSelectionListener);
				gisCanvas.setSelectedNodes(selectedGisNodes);
				gisCanvas.addSelectionListener(gisSelectionListener);
			}

			@Override
			public void edgeSelectionChanged(Set<Edge<GraphNode>> selectedEdges) {
				Set<Edge<LocationNode>> selectedGisEdges = new LinkedHashSet<Edge<LocationNode>>();
				Map<String, Edge<LocationNode>> gisEdgesById = new LinkedHashMap<String, Edge<LocationNode>>();

				for (Edge<LocationNode> gisEdge : gisCanvas.getEdges()) {
					gisEdgesById.put(gisEdge.getId(), gisEdge);
				}

				for (Edge<GraphNode> graphEdge : selectedEdges) {
					selectedGisEdges.add(gisEdgesById.get(graphEdge.getId()));
				}

				gisCanvas.removeSelectionListener(gisSelectionListener);
				gisCanvas.setSelectedEdges(selectedGisEdges);
				gisCanvas.addSelectionListener(gisSelectionListener);
			}
		};

		gisSelectionListener = new SelectionListener<LocationNode>() {

			@Override
			public void nodeSelectionChanged(Set<LocationNode> selectedNodes) {
				Set<GraphNode> selectedGraphNodes = new LinkedHashSet<GraphNode>();
				Map<String, GraphNode> graphNodesById = new LinkedHashMap<String, GraphNode>();

				for (GraphNode graphNode : graphCanvas.getNodes()) {
					graphNodesById.put(graphNode.getId(), graphNode);
				}

				for (LocationNode gisNode : selectedNodes) {
					selectedGraphNodes.add(graphNodesById.get(gisNode.getId()));
				}

				graphCanvas.removeSelectionListener(graphSelectionListener);
				graphCanvas.setSelectedNodes(selectedGraphNodes);
				graphCanvas.addSelectionListener(graphSelectionListener);
			}

			@Override
			public void edgeSelectionChanged(
					Set<Edge<LocationNode>> selectedEdges) {
				Set<Edge<GraphNode>> selectedGraphEdges = new LinkedHashSet<Edge<GraphNode>>();
				Map<String, Edge<GraphNode>> graphEdgesById = new LinkedHashMap<String, Edge<GraphNode>>();

				for (Edge<GraphNode> graphEdge : graphCanvas.getEdges()) {
					graphEdgesById.put(graphEdge.getId(), graphEdge);
				}

				for (Edge<LocationNode> gisEdge : selectedEdges) {
					selectedGraphEdges.add(graphEdgesById.get(gisEdge.getId()));
				}

				graphCanvas.removeSelectionListener(graphSelectionListener);
				graphCanvas.setSelectedEdges(selectedGraphEdges);
				graphCanvas.addSelectionListener(graphSelectionListener);
			}
		};

		graphHighlightListener = new HighlightListener() {

			@Override
			public void nodeHighlightingChanged(
					HighlightConditionList nodeHighlightConditions) {
				gisCanvas.removeHighlightListener(gisHighlightListener);
				gisCanvas.setNodeHighlightConditions(nodeHighlightConditions);
				gisCanvas.addHighlightListener(gisHighlightListener);
			}

			@Override
			public void edgeHighlightingChanged(
					HighlightConditionList edgeHighlightConditions) {
				gisCanvas.removeHighlightListener(gisHighlightListener);
				gisCanvas.setEdgeHighlightConditions(edgeHighlightConditions);
				gisCanvas.addHighlightListener(gisHighlightListener);
			}
		};

		gisHighlightListener = new HighlightListener() {

			@Override
			public void nodeHighlightingChanged(
					HighlightConditionList nodeHighlightConditions) {
				graphCanvas.removeHighlightListener(graphHighlightListener);
				graphCanvas.setNodeHighlightConditions(nodeHighlightConditions);
				graphCanvas.addHighlightListener(graphHighlightListener);
			}

			@Override
			public void edgeHighlightingChanged(
					HighlightConditionList edgeHighlightConditions) {
				graphCanvas.removeHighlightListener(graphHighlightListener);
				graphCanvas.setEdgeHighlightConditions(edgeHighlightConditions);
				graphCanvas.addHighlightListener(graphHighlightListener);
			}
		};
	}
}
