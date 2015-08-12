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
import java.awt.geom.Point2D;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
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
import de.bund.bfr.knime.gis.views.canvas.CanvasListener;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.ICanvas;
import de.bund.bfr.knime.gis.views.canvas.IGisCanvas;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.openkrise.views.canvas.ITracingCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.TracingChange;
import de.bund.bfr.knime.openkrise.views.canvas.TracingListener;
import de.bund.bfr.knime.openkrise.views.tracingview.TracingViewSettings.GisType;

/**
 * <code>NodeDialog</code> for the "TracingVisualizer" Node.
 * 
 * @author Christian Thoens
 */
public class TracingViewNodeDialog extends DataAwareNodeDialogPane
		implements ActionListener, ItemListener, ComponentListener, CanvasListener, TracingListener {

	private JPanel panel;
	private ITracingCanvas<?> canvas;

	private boolean resized;

	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;
	private BufferedDataTable tracingTable;
	private BufferedDataTable shapeTable;

	private TracingViewSettings set;
	private Deque<TracingChange> undoStack;
	private Deque<TracingChange> redoStack;

	private Set<String> selectedNodes;
	private Set<String> selectedEdges;
	private HighlightConditionList nodeHighlighting;
	private HighlightConditionList edgeHighlighting;
	private Map<String, Point2D> nodePositions;
	private Map<String, Set<String>> collapsedNodes;
	private Map<String, Double> nodeWeights;
	private Map<String, Double> edgeWeights;
	private Map<String, Boolean> nodeCrossContaminations;
	private Map<String, Boolean> edgeCrossContaminations;
	private Map<String, Boolean> nodeKillContaminations;
	private Map<String, Boolean> edgeKillContaminations;
	private Map<String, Boolean> observedNodes;
	private Map<String, Boolean> observedEdges;

	private JButton undoButton;
	private JButton redoButton;
	private JButton resetWeightsButton;
	private JButton resetCrossButton;
	private JButton resetFilterButton;
	private JCheckBox exportAsSvgBox;
	private JButton switchButton;
	private JComboBox<GisType> gisBox;

	private JScrollPane northScrollPane;

	/**
	 * New pane for configuring the TracingVisualizer node.
	 */
	protected TracingViewNodeDialog() {
		set = new TracingViewSettings();
		undoStack = new LinkedList<>();
		redoStack = new LinkedList<>();

		selectedNodes = null;
		selectedEdges = null;
		nodeHighlighting = null;
		edgeHighlighting = null;
		nodePositions = null;
		collapsedNodes = null;
		nodeWeights = null;
		edgeWeights = null;
		nodeCrossContaminations = null;
		edgeCrossContaminations = null;
		nodeKillContaminations = null;
		edgeKillContaminations = null;
		observedNodes = null;
		observedEdges = null;

		undoButton = new JButton("Undo");
		undoButton.addActionListener(this);
		redoButton = new JButton("Redo");
		redoButton.addActionListener(this);
		resetWeightsButton = new JButton("Reset Weights");
		resetWeightsButton.addActionListener(this);
		resetCrossButton = new JButton("Reset Cross Contamination");
		resetCrossButton.addActionListener(this);
		resetFilterButton = new JButton("Reset Observed");
		resetFilterButton.addActionListener(this);
		exportAsSvgBox = new JCheckBox("Export As Svg");
		switchButton = new JButton();
		switchButton.addActionListener(this);
		gisBox = new JComboBox<>();
		gisBox.addItemListener(this);

		JPanel northPanel = new JPanel();

		northPanel.setLayout(new BorderLayout());
		northPanel.add(UI.createHorizontalPanel(undoButton, redoButton, resetWeightsButton, resetCrossButton,
				resetFilterButton, exportAsSvgBox), BorderLayout.WEST);
		northPanel.add(UI.createHorizontalPanel(switchButton, new JLabel("GIS Type:"), gisBox), BorderLayout.EAST);
		northScrollPane = new JScrollPane(northPanel);
		panel = UI.createNorthPanel(northScrollPane);

		addTab("Options", panel, false);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input) throws NotConfigurableException {
		nodeTable = (BufferedDataTable) input[0];
		edgeTable = (BufferedDataTable) input[1];
		tracingTable = (BufferedDataTable) input[2];
		shapeTable = (BufferedDataTable) input[3];
		set.loadSettings(settings);

		undoButton.setEnabled(false);
		redoButton.setEnabled(false);
		undoStack.clear();
		redoStack.clear();

		gisBox.removeItemListener(this);
		gisBox.removeAllItems();

		for (GisType type : TracingViewSettings.GisType.values()) {
			if (shapeTable != null || type != GisType.SHAPEFILE) {
				gisBox.addItem(type);
			}
		}

		if (shapeTable == null && set.getGisType() == GisType.SHAPEFILE) {
			set.setGisType(GisType.MAPNIK);
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

		updateStatusVariables();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		updateSettings();
		set.saveSettings(settings);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		updateSettings();

		if (e.getSource() == undoButton) {
			TracingChange change = undoStack.pop();

			undoButton.setEnabled(!undoStack.isEmpty());

			canvas.removeCanvasListener(this);
			canvas.removeTracingListener(this);
			change.undo(canvas);
			canvas.addCanvasListener(this);
			canvas.addTracingListener(this);

			if (!(canvas instanceof GraphCanvas)) {
				set.getGraphSettings()
						.setNodePositions(change.undoRedoNodePositions(set.getGraphSettings().getNodePositions()));
			}

			updateStatusVariables();
			redoStack.push(change);
			redoButton.setEnabled(true);
		} else if (e.getSource() == redoButton) {
			TracingChange change = redoStack.pop();

			redoButton.setEnabled(!redoStack.isEmpty());

			canvas.removeCanvasListener(this);
			canvas.removeTracingListener(this);
			change.redo(canvas);
			canvas.addCanvasListener(this);
			canvas.addTracingListener(this);

			if (!(canvas instanceof GraphCanvas)) {
				set.getGraphSettings()
						.setNodePositions(change.undoRedoNodePositions(set.getGraphSettings().getNodePositions()));
			}

			updateStatusVariables();
			undoStack.push(change);
			undoButton.setEnabled(true);
		} else {
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

		if (northScrollPane.getSize().width < northScrollPane.getPreferredSize().width) {
			northScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			northScrollPane.getParent().revalidate();
		} else {
			northScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			northScrollPane.getParent().revalidate();
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

	@Override
	public void selectionChanged(ICanvas<?> source) {
		Set<String> newNodeSelection = canvas.getSelectedNodeIds();
		Set<String> newEdgeSelection = canvas.getSelectedEdgeIds();

		if (changeOccured(new TracingChange.Builder().selectedNodes(selectedNodes, newNodeSelection)
				.selectedEdges(selectedEdges, newEdgeSelection).build())) {
			selectedNodes = new LinkedHashSet<>(newNodeSelection);
			selectedEdges = new LinkedHashSet<>(newEdgeSelection);
		}
	}

	@Override
	public void nodeSelectionChanged(ICanvas<?> source) {
		Set<String> newSelection = canvas.getSelectedNodeIds();

		if (changeOccured(new TracingChange.Builder().selectedNodes(selectedNodes, newSelection).build())) {
			selectedNodes = new LinkedHashSet<>(newSelection);
		}
	}

	@Override
	public void edgeSelectionChanged(ICanvas<?> source) {
		Set<String> newSelection = canvas.getSelectedEdgeIds();

		if (changeOccured(new TracingChange.Builder().selectedEdges(selectedEdges, newSelection).build())) {
			selectedEdges = new LinkedHashSet<>(newSelection);
		}
	}

	@Override
	public void nodeHighlightingChanged(ICanvas<?> source) {
		HighlightConditionList newHighlighting = canvas.getNodeHighlightConditions();

		if (changeOccured(new TracingChange.Builder().nodeHighlighting(nodeHighlighting, newHighlighting).build())) {
			nodeHighlighting = newHighlighting.copy();
		}
	}

	@Override
	public void edgeHighlightingChanged(ICanvas<?> source) {
		HighlightConditionList newHighlighting = canvas.getEdgeHighlightConditions();

		if (changeOccured(new TracingChange.Builder().edgeHighlighting(edgeHighlighting, newHighlighting).build())) {
			edgeHighlighting = newHighlighting.copy();
		}
	}

	@Override
	public void nodePositionsChanged(ICanvas<?> source) {
		Map<String, Point2D> newPositions = ((GraphCanvas) canvas).getNodePositions();

		if (changeOccured(new TracingChange.Builder().nodePositions(nodePositions, newPositions).build())) {
			nodePositions = new LinkedHashMap<>(newPositions);
		}
	}

	@Override
	public void edgeJoinChanged(ICanvas<?> source) {
	}

	@Override
	public void skipEdgelessChanged(ICanvas<?> source) {
	}

	@Override
	public void showEdgesInMetaNodeChanged(ICanvas<?> source) {
	}

	@Override
	public void collapsedNodesChanged(ICanvas<?> source) {
		Map<String, Set<String>> newCollapsed = canvas.getCollapsedNodes();

		if (changeOccured(new TracingChange.Builder().collapsedNodes(collapsedNodes, newCollapsed).build())) {
			collapsedNodes = copy(newCollapsed);
		}
	}

	@Override
	public void collapsedNodesAndPickingChanged(ICanvas<?> source) {
		Set<String> newSelection = canvas.getSelectedNodeIds();
		Map<String, Set<String>> newCollapsed = canvas.getCollapsedNodes();

		if (changeOccured(new TracingChange.Builder().selectedNodes(selectedNodes, newSelection)
				.collapsedNodes(collapsedNodes, newCollapsed).build())) {
			selectedNodes = new LinkedHashSet<>(newSelection);
			collapsedNodes = copy(newCollapsed);
		}
	}

	@Override
	public void nodePropertiesChanged(ITracingCanvas<?> source) {
		Map<String, Double> newWeights = canvas.getNodeWeights();
		Map<String, Boolean> newCrossContaminations = canvas.getNodeCrossContaminations();
		Map<String, Boolean> newKillContaminations = canvas.getNodeKillContaminations();
		Map<String, Boolean> newObserved = canvas.getObservedNodes();

		if (changeOccured(new TracingChange.Builder().nodeWeights(nodeWeights, newWeights)
				.nodeCrossContaminations(nodeCrossContaminations, newCrossContaminations)
				.nodeKillContaminations(nodeKillContaminations, newKillContaminations)
				.observedNodes(observedNodes, newObserved).build())) {
			nodeWeights = new LinkedHashMap<>(newWeights);
			nodeCrossContaminations = new LinkedHashMap<>(newCrossContaminations);
			nodeKillContaminations = new LinkedHashMap<>(newKillContaminations);
			observedNodes = new LinkedHashMap<>(newObserved);
		}
	}

	@Override
	public void edgePropertiesChanged(ITracingCanvas<?> source) {
		Map<String, Double> newWeights = canvas.getEdgeWeights();
		Map<String, Boolean> newCrossContaminations = canvas.getEdgeCrossContaminations();
		Map<String, Boolean> newKillContaminations = canvas.getEdgeKillContaminations();
		Map<String, Boolean> newObserved = canvas.getObservedEdges();

		if (changeOccured(new TracingChange.Builder().edgeWeights(edgeWeights, newWeights)
				.edgeCrossContaminations(edgeCrossContaminations, newCrossContaminations)
				.edgeKillContaminations(edgeKillContaminations, newKillContaminations)
				.observedEdges(observedEdges, newObserved).build())) {
			edgeWeights = new LinkedHashMap<>(newWeights);
			edgeCrossContaminations = new LinkedHashMap<>(newCrossContaminations);
			edgeKillContaminations = new LinkedHashMap<>(newKillContaminations);
			observedEdges = new LinkedHashMap<>(newObserved);
		}
	}

	@Override
	public void nodeWeightsChanged(ITracingCanvas<?> source) {
		Map<String, Double> newWeights = canvas.getNodeWeights();

		if (changeOccured(new TracingChange.Builder().nodeWeights(nodeWeights, newWeights).build())) {
			nodeWeights = new LinkedHashMap<>(newWeights);
		}
	}

	@Override
	public void edgeWeightsChanged(ITracingCanvas<?> source) {
		Map<String, Double> newWeights = canvas.getEdgeWeights();

		if (changeOccured(new TracingChange.Builder().edgeWeights(edgeWeights, newWeights).build())) {
			edgeWeights = new LinkedHashMap<>(newWeights);
		}
	}

	@Override
	public void nodeCrossContaminationsChanged(ITracingCanvas<?> source) {
		Map<String, Boolean> newCrossContaminations = canvas.getNodeCrossContaminations();

		if (changeOccured(new TracingChange.Builder()
				.nodeCrossContaminations(nodeCrossContaminations, newCrossContaminations).build())) {
			nodeCrossContaminations = new LinkedHashMap<>(newCrossContaminations);
		}
	}

	@Override
	public void edgeCrossContaminationsChanged(ITracingCanvas<?> source) {
		Map<String, Boolean> newCrossContaminations = canvas.getEdgeCrossContaminations();

		if (changeOccured(new TracingChange.Builder()
				.edgeCrossContaminations(edgeCrossContaminations, newCrossContaminations).build())) {
			edgeCrossContaminations = new LinkedHashMap<>(newCrossContaminations);
		}
	}

	@Override
	public void nodeKillContaminationsChanged(ITracingCanvas<?> source) {
		Map<String, Boolean> newKillContaminations = canvas.getNodeKillContaminations();

		if (changeOccured(new TracingChange.Builder()
				.nodeKillContaminations(nodeKillContaminations, newKillContaminations).build())) {
			nodeKillContaminations = new LinkedHashMap<>(newKillContaminations);
		}
	}

	@Override
	public void edgeKillContaminationsChanged(ITracingCanvas<?> source) {
		Map<String, Boolean> newKillContaminations = canvas.getEdgeKillContaminations();

		if (changeOccured(new TracingChange.Builder()
				.edgeKillContaminations(edgeKillContaminations, newKillContaminations).build())) {
			edgeKillContaminations = new LinkedHashMap<>(newKillContaminations);
		}
	}

	@Override
	public void observedNodesChanged(ITracingCanvas<?> source) {
		Map<String, Boolean> newObserved = canvas.getObservedNodes();

		if (changeOccured(new TracingChange.Builder().observedNodes(observedNodes, newObserved).build())) {
			observedNodes = new LinkedHashMap<>(newObserved);
		}
	}

	@Override
	public void observedEdgesChanged(ITracingCanvas<?> source) {
		Map<String, Boolean> newObserved = canvas.getObservedEdges();

		if (changeOccured(new TracingChange.Builder().observedEdges(observedEdges, newObserved).build())) {
			observedEdges = new LinkedHashMap<>(newObserved);
		}
	}

	private String updateCanvas() throws NotConfigurableException {
		if (canvas != null) {
			panel.remove(canvas.getComponent());
		}

		TracingViewCanvasCreator creator = new TracingViewCanvasCreator(nodeTable, edgeTable, tracingTable, shapeTable,
				set);

		canvas = set.isShowGis() ? creator.createGisCanvas() : creator.createGraphCanvas();
		canvas.addCanvasListener(this);
		canvas.addTracingListener(this);
		switchButton.setText("Switch to " + (set.isShowGis() ? "Graph" : "GIS"));
		switchButton.setEnabled(creator.hasGisCoordinates());

		String warningTable = null;

		if (!creator.getSkippedEdgeRows().isEmpty() && !creator.getSkippedTracingRows().isEmpty()) {
			warningTable = "the delivery table and the tracing table";
		} else if (!creator.getSkippedEdgeRows().isEmpty()) {
			warningTable = "the delivery table";
		} else if (!creator.getSkippedTracingRows().isEmpty()) {
			warningTable = "the tracing table";
		}

		String warning = null;

		if (warningTable != null) {
			warning = "Some rows from " + warningTable + " could not be imported."
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
		} else if (canvas instanceof IGisCanvas) {
			set.getGisSettings().setFromCanvas((IGisCanvas<?>) canvas);
		}
	}

	private boolean changeOccured(TracingChange change) {
		if (change.isIdentity()) {
			return false;
		}

		undoStack.push(change);
		undoButton.setEnabled(true);
		redoStack.clear();
		redoButton.setEnabled(false);

		return true;
	}

	private void updateStatusVariables() {
		selectedNodes = new LinkedHashSet<>(canvas.getSelectedNodeIds());
		selectedEdges = new LinkedHashSet<>(canvas.getSelectedEdgeIds());
		nodeHighlighting = canvas.getNodeHighlightConditions().copy();
		edgeHighlighting = canvas.getEdgeHighlightConditions().copy();
		collapsedNodes = copy(canvas.getCollapsedNodes());

		if (canvas instanceof GraphCanvas) {
			nodePositions = new LinkedHashMap<>(((GraphCanvas) canvas).getNodePositions());
		}

		nodeWeights = new LinkedHashMap<>(canvas.getNodeWeights());
		edgeWeights = new LinkedHashMap<>(canvas.getEdgeWeights());
		nodeCrossContaminations = new LinkedHashMap<>(canvas.getNodeCrossContaminations());
		edgeCrossContaminations = new LinkedHashMap<>(canvas.getEdgeCrossContaminations());
		nodeKillContaminations = new LinkedHashMap<>(canvas.getNodeKillContaminations());
		edgeKillContaminations = new LinkedHashMap<>(canvas.getEdgeKillContaminations());
		observedNodes = new LinkedHashMap<>(canvas.getObservedNodes());
		observedEdges = new LinkedHashMap<>(canvas.getObservedEdges());
	}

	private static Map<String, Set<String>> copy(Map<String, Set<String>> map) {
		Map<String, Set<String>> copy = new LinkedHashMap<>();

		for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
			copy.put(entry.getKey(), new LinkedHashSet<>(entry.getValue()));
		}

		return copy;
	}
}
