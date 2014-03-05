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
package de.bund.bfr.knime.gis.views.regiontoregionvisualizer;

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
import de.bund.bfr.knime.gis.views.canvas.RegionCanvas;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.gis.views.canvas.listener.SelectionListener;

/**
 * <code>NodeDialog</code> for the "RegionToRegionVisualizer" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Christian Thoens
 */
public class RegionToRegionVisualizerNodeDialog extends DataAwareNodeDialogPane
		implements ActionListener, ComponentListener {

	private JPanel panel;
	private JSplitPane splitPane;
	private GraphCanvas graphCanvas;
	private RegionCanvas gisCanvas;

	private boolean resized;

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;

	private RegionToRegionVisualizerSettings set;

	private SelectionListener<GraphNode> graphSelectionListener;
	private SelectionListener<RegionNode> gisSelectionListener;

	/**
	 * New pane for configuring the RegionToRegionVisualizer node.
	 */
	protected RegionToRegionVisualizerNodeDialog() {
		set = new RegionToRegionVisualizerSettings();

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
	public void actionPerformed(ActionEvent e) {
		RegionToRegionVisualizerInputDialog dialog = new RegionToRegionVisualizerInputDialog(
				(JButton) e.getSource(), shapeTable.getSpec(),
				nodeTable.getSpec(), edgeTable.getSpec(), set);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			updateSettings();
			updateSplitPane(true);
		}
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

	private void updateSplitPane(boolean showWarning) {
		if (splitPane != null) {
			panel.remove(splitPane);
		}

		RegionToRegionVisualizerCanvasCreator creator = new RegionToRegionVisualizerCanvasCreator(
				shapeTable, nodeTable, edgeTable, set);

		graphCanvas = creator.createGraphCanvas();
		gisCanvas = creator.createGISCanvas();

		if (graphCanvas != null && gisCanvas != null) {
			graphCanvas.addSelectionListener(graphSelectionListener);
			gisCanvas.addSelectionListener(gisSelectionListener);

			if (showWarning && !creator.getNonExistingRegions().isEmpty()) {
				JOptionPane.showMessageDialog(panel,
						"Some regions from the table are not contained"
								+ " in the shapefile", "Warning",
						JOptionPane.WARNING_MESSAGE);
			}
		} else {
			graphCanvas = new GraphCanvas();
			graphCanvas
					.setCanvasSize(SimpleGraphVisualizerSettings.DEFAULT_GRAPH_CANVAS_SIZE);
			graphCanvas
					.setLayoutType(SimpleGraphVisualizerSettings.DEFAULT_GRAPH_LAYOUT);
			graphCanvas.setAllowCollapse(false);
			gisCanvas = new RegionCanvas(true);
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
				Set<RegionNode> selectedGisNodes = new LinkedHashSet<RegionNode>();
				Map<String, RegionNode> gisNodesByRegion = new LinkedHashMap<String, RegionNode>();

				for (RegionNode gisNode : gisCanvas.getNodes()) {
					gisNodesByRegion.put(gisNode.getId(), gisNode);
				}

				for (GraphNode graphNode : selectedNodes) {
					RegionNode gisNode = gisNodesByRegion.get(graphNode
							.getRegion());

					if (gisNode != null) {
						selectedGisNodes.add(gisNode);
					}
				}

				gisCanvas.removeSelectionListener(gisSelectionListener);
				gisCanvas.setSelectedNodes(selectedGisNodes);
				gisCanvas.addSelectionListener(gisSelectionListener);
				gisCanvas.repaint();
			}

			@Override
			public void edgeSelectionChanged(Set<Edge<GraphNode>> selectedEdges) {
				Set<Edge<RegionNode>> selectedGisEdges = new LinkedHashSet<Edge<RegionNode>>();

				if (!set.isJoinEdges()) {
					Map<String, Edge<RegionNode>> gisEdgesById = new LinkedHashMap<String, Edge<RegionNode>>();

					for (Edge<RegionNode> gisEdge : gisCanvas.getEdges()) {
						gisEdgesById.put(gisEdge.getId(), gisEdge);
					}

					for (Edge<GraphNode> graphEdge : selectedEdges) {
						selectedGisEdges
								.add(gisEdgesById.get(graphEdge.getId()));
					}
				} else {
					Map<String, Map<String, Edge<RegionNode>>> gisEdgesByRegion = new LinkedHashMap<String, Map<String, Edge<RegionNode>>>();

					for (Edge<RegionNode> gisEdge : gisCanvas.getEdges()) {
						String fromRegion = gisEdge.getFrom().getId();
						String toRegion = gisEdge.getTo().getId();

						if (!gisEdgesByRegion.containsKey(fromRegion)) {
							gisEdgesByRegion
									.put(fromRegion,
											new LinkedHashMap<String, Edge<RegionNode>>());
						}

						gisEdgesByRegion.get(fromRegion).put(toRegion, gisEdge);
					}

					for (Edge<GraphNode> graphEdge : selectedEdges) {
						String fromRegion = graphEdge.getFrom().getRegion();
						String toRegion = graphEdge.getTo().getRegion();

						if (gisEdgesByRegion.containsKey(fromRegion)) {
							Edge<RegionNode> gisEdge = gisEdgesByRegion.get(
									fromRegion).get(toRegion);

							if (gisEdge != null) {
								selectedGisEdges.add(gisEdge);
							}
						}
					}
				}

				gisCanvas.removeSelectionListener(gisSelectionListener);
				gisCanvas.setSelectedEdges(selectedGisEdges);
				gisCanvas.addSelectionListener(gisSelectionListener);
				gisCanvas.repaint();
			}
		};

		gisSelectionListener = new SelectionListener<RegionNode>() {

			@Override
			public void nodeSelectionChanged(Set<RegionNode> selectedNodes) {
				Set<GraphNode> selectedGraphNodes = new LinkedHashSet<GraphNode>();
				Map<String, List<GraphNode>> graphNodesByRegion = new LinkedHashMap<String, List<GraphNode>>();

				for (GraphNode graphNode : graphCanvas.getNodes()) {
					if (!graphNodesByRegion.containsKey(graphNode.getRegion())) {
						graphNodesByRegion.put(graphNode.getRegion(),
								new ArrayList<GraphNode>());
					}

					graphNodesByRegion.get(graphNode.getRegion())
							.add(graphNode);
				}

				for (RegionNode gisNode : selectedNodes) {
					List<GraphNode> graphNodes = graphNodesByRegion.get(gisNode
							.getId());

					if (graphNodes != null) {
						selectedGraphNodes.addAll(graphNodes);
					}
				}

				graphCanvas.removeSelectionListener(graphSelectionListener);
				graphCanvas.setSelectedNodes(selectedGraphNodes);
				graphCanvas.addSelectionListener(graphSelectionListener);
			}

			@Override
			public void edgeSelectionChanged(Set<Edge<RegionNode>> selectedEdges) {
				Set<Edge<GraphNode>> selectedGraphEdges = new LinkedHashSet<Edge<GraphNode>>();

				if (!set.isJoinEdges()) {
					Map<String, Edge<GraphNode>> graphEdgesById = new LinkedHashMap<String, Edge<GraphNode>>();

					for (Edge<GraphNode> graphEdge : graphCanvas.getEdges()) {
						graphEdgesById.put(graphEdge.getId(), graphEdge);
					}

					for (Edge<RegionNode> gisEdge : selectedEdges) {
						selectedGraphEdges.add(graphEdgesById.get(gisEdge
								.getId()));
					}
				} else {
					Map<String, Map<String, List<Edge<GraphNode>>>> graphEdgesByRegion = new LinkedHashMap<String, Map<String, List<Edge<GraphNode>>>>();

					for (Edge<GraphNode> graphEdge : graphCanvas.getEdges()) {
						String fromRegion = graphEdge.getFrom().getRegion();
						String toRegion = graphEdge.getTo().getRegion();

						if (!graphEdgesByRegion.containsKey(fromRegion)) {
							graphEdgesByRegion
									.put(fromRegion,
											new LinkedHashMap<String, List<Edge<GraphNode>>>());
						}

						if (!graphEdgesByRegion.get(fromRegion).containsKey(
								toRegion)) {
							graphEdgesByRegion.get(fromRegion).put(toRegion,
									new ArrayList<Edge<GraphNode>>());
						}

						graphEdgesByRegion.get(fromRegion).get(toRegion)
								.add(graphEdge);
					}

					for (Edge<RegionNode> gisEdge : selectedEdges) {
						String fromRegion = gisEdge.getFrom().getId();
						String toRegion = gisEdge.getTo().getId();

						if (graphEdgesByRegion.containsKey(fromRegion)) {
							List<Edge<GraphNode>> graphEdges = graphEdgesByRegion
									.get(fromRegion).get(toRegion);

							if (graphEdges != null) {
								selectedGraphEdges.addAll(graphEdges);
							}
						}
					}
				}

				graphCanvas.removeSelectionListener(graphSelectionListener);
				graphCanvas.setSelectedEdges(selectedGraphEdges);
				graphCanvas.addSelectionListener(graphSelectionListener);
			}
		};
	}
}
