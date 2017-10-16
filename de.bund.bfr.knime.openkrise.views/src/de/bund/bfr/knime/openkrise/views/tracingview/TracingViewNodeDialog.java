/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
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

import de.bund.bfr.jung.LabelPosition;
import de.bund.bfr.jung.ZoomingPaintable;
import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.GisType;
import de.bund.bfr.knime.gis.views.canvas.CanvasListener;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.ICanvas;
import de.bund.bfr.knime.gis.views.canvas.IGisCanvas;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.util.ArrowHeadType;
import de.bund.bfr.knime.gis.views.canvas.util.Transform;
import de.bund.bfr.knime.openkrise.TracingUtils;
import de.bund.bfr.knime.openkrise.views.canvas.ExplosionCanvasListener;
import de.bund.bfr.knime.openkrise.views.canvas.ExplosionListener;
import de.bund.bfr.knime.openkrise.views.canvas.ExplosionTracingGraphCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.IExplosionCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.ITracingCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.TracingListener;
import de.bund.bfr.knime.ui.Dialogs;

import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;

/**
 * <code>NodeDialog</code> for the "TracingVisualizer" Node.
 * 
 * @author Christian Thoens
 */
public class TracingViewNodeDialog extends DataAwareNodeDialogPane implements ExplosionListener, CanvasListener, TracingListener {

	private JPanel panel;
	private ITracingCanvas<?> canvas;
//	private ExplosionViewLabel gobjExplosionViewLabel;

	private boolean resized;

	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;
	private BufferedDataTable tracingTable;
	private BufferedDataTable shapeTable;

	private TracingViewSettings set;
	private Deque<TracingChange> undoStack;
	private Deque<TracingChange> redoStack;
	//private Stack<ExplosionSettings> gobjExplosionStack;
	//private Stack<ViewPair> gobjViewStack;

	private Transform transform;
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

	private boolean joinEdges;
	private boolean skipEdgelessNodes;
	private boolean showEdgesInMetaNode;
	private ArrowHeadType arrowHeadType;
	private LabelPosition nodeLabelPosition;
	private boolean showLegend;
	private boolean enforeTemporalOrder;
	private boolean showForward;
	private boolean showDeliveriesWithoutDate;
	private GregorianCalendar showToDate;

	private int nodeSize;
	private Integer nodeMaxSize;
	private int edgeThickness;
	private Integer edgeMaxThickness;
	private int fontSize;
	private boolean fontBold;
	private String label;

	private int borderAlpha;
	private boolean avoidOverlay;

	private JButton undoButton;
	private JButton redoButton;
	private JButton resetWeightsButton;
	private JButton resetCrossButton;
	private JButton resetKillButton;
	private JButton resetObservedButton;
	private JCheckBox exportAsSvgBox;
	private JButton switchButton;
	private JComboBox<GisType> gisBox;
	private ItemListener gisBoxListener;

	private JScrollPane northScrollPane;
	
	private static Logger logger =  Logger.getLogger("de.bund.bfr");

	/**
	 * New pane for configuring the TracingVisualizer node.
	 */
	protected TracingViewNodeDialog() {
		this.initializeFileLogging();
		this.set = new TracingViewSettings();
		this.undoStack = new LinkedList<>();
		this.redoStack = new LinkedList<>();

		this.undoButton = new JButton("Undo");
		this.undoButton.addActionListener(e -> undoRedoPressed(true));
		this.redoButton = new JButton("Redo");
		this.redoButton.addActionListener(e -> undoRedoPressed(false));
		this.resetWeightsButton = new JButton("Reset Weights");
		this.resetWeightsButton.addActionListener(e -> resetPressed(resetWeightsButton));
		this.resetCrossButton = new JButton("Reset Cross Contamination");
		this.resetCrossButton.addActionListener(e -> resetPressed(resetCrossButton));
		this.resetKillButton = new JButton("Reset Kill Contamination");
		this.resetKillButton.addActionListener(e -> resetPressed(resetKillButton));
		this.resetObservedButton = new JButton("Reset Observed");
		this.resetObservedButton.addActionListener(e -> resetPressed(resetObservedButton));
		this.exportAsSvgBox = new JCheckBox("Export As Svg");
		this.switchButton = new JButton();
		this.switchButton.addActionListener(e -> switchPressed());
		this.gisBox = new JComboBox<>();
		this.gisBox.addItemListener(this.gisBoxListener = UI.newItemSelectListener(e -> gisTypeChanged()));

		JPanel northPanel = new JPanel();

		northPanel.setLayout(new BorderLayout());
		northPanel.add(UI.createHorizontalPanel(this.undoButton, this.redoButton, this.resetWeightsButton, this.resetCrossButton,
				this.resetKillButton, this.resetObservedButton, this.exportAsSvgBox), BorderLayout.WEST);
		northPanel.add(UI.createHorizontalPanel(this.switchButton, new JLabel("GIS Type:"), this.gisBox), BorderLayout.EAST);
		northScrollPane = new JScrollPane(northPanel);
		panel = UI.createNorthPanel(northScrollPane);

		this.addTab("Options", panel, false);
	}
	
	private void initializeFileLogging() {
		// File Handler erzeugen
		try {
			FileHandler file_handler = new FileHandler("C:\\Temp\\FCL.log");
			// Formatter erzeugen
			// SimpleFormatter klartext = new SimpleFormatter();
			MyFormatter klartext = new MyFormatter();
			file_handler.setFormatter(klartext);
			logger.addHandler(file_handler);
			logger.setLevel(Level.ALL);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	finally
		{}
	}
	
	private class MyFormatter extends Formatter {

		@Override
		public String format(LogRecord arg0) {
			// TODO Auto-generated method stub
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(arg0.getMillis());
			Date date = cal.getTime();
			
			return arg0.getLevel().getName() + "\t" + 
			      String.format("%04d%02d%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)) + 
			      String.format("%02d%02d%02d.%03d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND)) + 
			      "\t" + 
			      arg0.getSourceClassName() + "\t" +
			      arg0.getSourceMethodName() + "\t" + 
			      arg0.getMessage() + "\r\n";
		}
		
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input) throws NotConfigurableException {
		logger.finest("entered");
		this.nodeTable = (BufferedDataTable) input[0];
		this.edgeTable = (BufferedDataTable) input[1];
		this.tracingTable = (BufferedDataTable) input[2];
		this.shapeTable = (BufferedDataTable) input[3];
		this.set.loadSettings(settings);
		//this.set.getExplosionSettingsList().clearActiveExplosionSettings();

		this.undoButton.setEnabled(false);
		this.redoButton.setEnabled(false);
		this.undoStack.clear();
		this.redoStack.clear();
		//this.gobjExplosionStack.clear();
		//this.gobjViewStack.push(new ViewPair(this.set.getGraphSettings(),this.set.getGisSettings()));

		this.gisBox.removeItemListener(this.gisBoxListener);
		this.gisBox.removeAllItems();

		for (GisType type : GisType.values()) {
			if (this.shapeTable != null || type != GisType.SHAPEFILE) {
				this.gisBox.addItem(type);
			}
		}

		if (this.shapeTable == null && this.set.getGisType() == GisType.SHAPEFILE) {
			this.set.setGisType(GisType.MAPNIK);
		}

		this.gisBox.setSelectedItem(set.getGisType());
		this.gisBox.addItemListener(gisBoxListener);
		this.gisBox.setEnabled(set.isShowGis());
		this.exportAsSvgBox.setSelected(set.isExportAsSvg());
		this.resized = false;
		this.panel.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				if (SwingUtilities.getWindowAncestor(e.getComponent()).isActive()) {
					TracingViewNodeDialog.this.resized = true;
				}

				if (TracingViewNodeDialog.this.northScrollPane.getSize().width < TracingViewNodeDialog.this.northScrollPane.getPreferredSize().width) {
					if (TracingViewNodeDialog.this.northScrollPane
							.getHorizontalScrollBarPolicy() != ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS) {
						TracingViewNodeDialog.this.northScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
						TracingViewNodeDialog.this.northScrollPane.getParent().revalidate();
					}
				} else {
					if (TracingViewNodeDialog.this.northScrollPane
							.getHorizontalScrollBarPolicy() != ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {
						TracingViewNodeDialog.this.northScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
						TracingViewNodeDialog.this.northScrollPane.getParent().revalidate();
					}
				}
			}
		});

		this.createCanvas(false);
		this.updateStatusVariables();
		logger.finest("leaving");
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		logger.finest("entered");
		this.updateSettings();
		this.set.saveSettings(settings);
		logger.finest("leaving");
	}

	@Override
	public void transformChanged(ICanvas<?> source) {
		Transform newTransform = this.canvas.getTransform();

		if (this.changeOccured(new TracingChange.Builder().transform(this.transform, newTransform).build())) {
			this.transform = newTransform;
		}
	}

	@Override
	public void selectionChanged(ICanvas<?> source) {
		Set<String> newNodeSelection = this.canvas.getSelectedNodeIds();
		Set<String> newEdgeSelection = this.canvas.getSelectedEdgeIds();

		if (this.changeOccured(new TracingChange.Builder().selectedNodes(this.selectedNodes, newNodeSelection)
				.selectedEdges(this.selectedEdges, newEdgeSelection).build())) {
			this.selectedNodes = new LinkedHashSet<>(newNodeSelection);
			this.selectedEdges = new LinkedHashSet<>(newEdgeSelection);
		}
	}

	@Override
	public void nodeSelectionChanged(ICanvas<?> source) {
		Set<String> newSelection = this.canvas.getSelectedNodeIds();

		if (this.changeOccured(new TracingChange.Builder().selectedNodes(this.selectedNodes, newSelection).build())) {
			this.selectedNodes = new LinkedHashSet<>(newSelection);
		}
	}

	@Override
	public void edgeSelectionChanged(ICanvas<?> source) {
		Set<String> newSelection = this.canvas.getSelectedEdgeIds();

		if (this.changeOccured(new TracingChange.Builder().selectedEdges(this.selectedEdges, newSelection).build())) {
			this.selectedEdges = new LinkedHashSet<>(newSelection);
		}
	}

	@Override
	public void nodeHighlightingChanged(ICanvas<?> source) {
		HighlightConditionList newHighlighting = this.canvas.getNodeHighlightConditions();

		if (this.changeOccured(new TracingChange.Builder().nodeHighlighting(this.nodeHighlighting, newHighlighting).build())) {
			this.nodeHighlighting = newHighlighting.copy();
		}
	}

	@Override
	public void edgeHighlightingChanged(ICanvas<?> source) {
		HighlightConditionList newHighlighting = this.canvas.getEdgeHighlightConditions();

		if (this.changeOccured(new TracingChange.Builder().edgeHighlighting(this.edgeHighlighting, newHighlighting).build())) {
			this.edgeHighlighting = newHighlighting.copy();
		}
	}

	@Override
	public void highlightingChanged(ICanvas<?> source) {
		HighlightConditionList newNodeHighlighting = this.canvas.getNodeHighlightConditions();
		HighlightConditionList newEdgeHighlighting = this.canvas.getEdgeHighlightConditions();

		if (this.changeOccured(new TracingChange.Builder().nodeHighlighting(this.nodeHighlighting, newNodeHighlighting)
				.edgeHighlighting(this.edgeHighlighting, newEdgeHighlighting).build())) {
			this.nodeHighlighting = newNodeHighlighting.copy();
			this.edgeHighlighting = newEdgeHighlighting.copy();
		}
	}

	@Override
	public void layoutProcessFinished(ICanvas<?> source) {
		Map<String, Point2D> newPositions = ((GraphCanvas) this.canvas).getNodePositions();
		Transform newTransform = this.canvas.getTransform();

		if (this.changeOccured(new TracingChange.Builder().nodePositions(this.nodePositions, newPositions)
				.transform(this.transform, newTransform).build())) {
			this.nodePositions = new LinkedHashMap<>(newPositions);
			this.transform = newTransform;
		}
	}

	@Override
	public void nodePositionsChanged(ICanvas<?> source) {
		Map<String, Point2D> newPositions = ((GraphCanvas) this.canvas).getNodePositions();

		if (this.changeOccured(new TracingChange.Builder().nodePositions(this.nodePositions, newPositions).build())) {
			this.nodePositions = new LinkedHashMap<>(newPositions);
		}
	}

	@Override
	public void edgeJoinChanged(ICanvas<?> source) {
		boolean newEdgeJoin = this.canvas.getOptionsPanel().isJoinEdges();

		if (this.changeOccured(new TracingChange.Builder().joinEdges(this.joinEdges, newEdgeJoin).build())) {
			this.joinEdges = newEdgeJoin;
		}
	}

	@Override
	public void skipEdgelessChanged(ICanvas<?> source) {
		boolean newSkipEdgeless = this.canvas.getOptionsPanel().isSkipEdgelessNodes();

		if (this.changeOccured(new TracingChange.Builder().skipEdgelessNodes(this.skipEdgelessNodes, newSkipEdgeless).build())) {
			this.skipEdgelessNodes = newSkipEdgeless;
		}
	}

	@Override
	public void showEdgesInMetaNodeChanged(ICanvas<?> source) {
		boolean newShowEdgesInMeta = this.canvas.getOptionsPanel().isShowEdgesInMetaNode();

		if (this.changeOccured(
				new TracingChange.Builder().showEdgesInMetaNode(this.showEdgesInMetaNode, newShowEdgesInMeta).build())) {
			this.showEdgesInMetaNode = newShowEdgesInMeta;
		}
	}

	@Override
	public void arrowHeadTypeChanged(ICanvas<?> source) {
		ArrowHeadType newArrowHeadType = canvas.getOptionsPanel().getArrowHeadType();

		if (changeOccured(new TracingChange.Builder().arrowHeadType(arrowHeadType, newArrowHeadType).build())) {
			arrowHeadType = newArrowHeadType;
		}
	}

	@Override
	public void nodeLabelPositionChanged(ICanvas<?> source) {
		LabelPosition newNodeLabelPosition = canvas.getOptionsPanel().getNodeLabelPosition();

		if (changeOccured(
				new TracingChange.Builder().nodeLabelPosition(nodeLabelPosition, newNodeLabelPosition).build())) {
			nodeLabelPosition = newNodeLabelPosition;
		}
	}

	@Override
	public void showLegendChanged(ICanvas<?> source) {
		boolean newShowLegend = canvas.getOptionsPanel().isShowLegend();

		if (changeOccured(new TracingChange.Builder().showLegend(showLegend, newShowLegend).build())) {
			showLegend = newShowLegend;
		}
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
	public void nodeSizeChanged(ICanvas<?> source) {
		int newNodeSize = canvas.getOptionsPanel().getNodeSize();
		Integer newNodeMaxSize = canvas.getOptionsPanel().getNodeMaxSize();

		if (changeOccured(
				new TracingChange.Builder().nodeSize(nodeSize, newNodeSize, nodeMaxSize, newNodeMaxSize).build())) {
			nodeSize = newNodeSize;
			nodeMaxSize = newNodeMaxSize;
		}
	}

	@Override
	public void edgeThicknessChanged(ICanvas<?> source) {
		int newEdgeThickness = canvas.getOptionsPanel().getEdgeThickness();
		Integer newEdgeMaxThickness = canvas.getOptionsPanel().getEdgeMaxThickness();

		if (changeOccured(new TracingChange.Builder()
				.edgeThickness(edgeThickness, newEdgeThickness, edgeMaxThickness, newEdgeMaxThickness).build())) {
			edgeThickness = newEdgeThickness;
			edgeMaxThickness = newEdgeMaxThickness;
		}
	}

	@Override
	public void fontChanged(ICanvas<?> source) {
		int newFontSize = canvas.getOptionsPanel().getFontSize();
		boolean newFontBold = canvas.getOptionsPanel().isFontBold();

		if (changeOccured(new TracingChange.Builder().font(fontSize, newFontSize, fontBold, newFontBold).build())) {
			fontSize = newFontSize;
			fontBold = newFontBold;
		}
	}

	@Override
	public void labelChanged(ICanvas<?> source) {
		String newLabel = canvas.getOptionsPanel().getLabel();

		if (changeOccured(new TracingChange.Builder().label(label, newLabel).build())) {
			label = newLabel;
		}
	}

	@Override
	public void borderAlphaChanged(ICanvas<?> source) {
		int newBorderAlpha = canvas.getOptionsPanel().getBorderAlpha();

		if (changeOccured(new TracingChange.Builder().borderAlpha(borderAlpha, newBorderAlpha).build())) {
			borderAlpha = newBorderAlpha;
		}
	}

	@Override
	public void avoidOverlayChanged(ICanvas<?> source) {
		boolean newAvoidOverlay = canvas.getOptionsPanel().isAvoidOverlay();

		if (changeOccured(new TracingChange.Builder().avoidOverlay(avoidOverlay, newAvoidOverlay).build())) {
			avoidOverlay = newAvoidOverlay;
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

	@Override
	public void enforceTemporalOrderChanged(ITracingCanvas<?> source) {
		boolean newEnforceTemp = canvas.isEnforceTemporalOrder();

		if (changeOccured(
				new TracingChange.Builder().enforceTemporalOrder(enforeTemporalOrder, newEnforceTemp).build())) {
			enforeTemporalOrder = newEnforceTemp;
		}
	}

	@Override
	public void showForwardChanged(ITracingCanvas<?> source) {
		boolean newShowForward = canvas.isShowForward();

		if (changeOccured(new TracingChange.Builder().showForwardChanged(showForward, newShowForward).build())) {
			showForward = newShowForward;
		}
	}

	@Override
	public void dateSettingsChanged(ITracingCanvas<?> source) {
		boolean newShowDeliveriesWithoutDate = canvas.isShowDeliveriesWithoutDate();
		GregorianCalendar newShowToDate = canvas.getShowToDate();

		if (changeOccured(new TracingChange.Builder()
				.showWithoutDateChanged(showDeliveriesWithoutDate, newShowDeliveriesWithoutDate)
				.showToDateChanged(showToDate, newShowToDate).build())) {
			showDeliveriesWithoutDate = newShowDeliveriesWithoutDate;
			showToDate = newShowToDate;
		}
	}

	private void gisTypeChanged() {
		this.updateSettings();
		this.changeOccured(TracingChange.Builder.createViewChange(
				set.isShowGis(), set.isShowGis(), set.getGisType(), (GisType) gisBox.getSelectedItem()));
		set.setGisType((GisType) gisBox.getSelectedItem());
		this.updateCanvas();
	}

	private String createCanvas(boolean isUpdate) throws NotConfigurableException {
		logger.finest("entered");
		//if (this.gobjExplosionViewLabel!=null) this.removeLabelFromExplosionViewLabel();

		if (canvas != null) {
			if(canvas.getComponent().getParent()==panel) {
				panel.remove(canvas.getComponent());
			} 
//			else {
//			  panel.remove(canvas.getComponent().getParent().getParent());
//			}
			//panel.remove(canvas.getComponent());
		}
		

		TracingViewCanvasCreator creator = new TracingViewCanvasCreator(nodeTable, edgeTable, tracingTable, shapeTable,
				set);

		boolean bolIsGisAvailable = creator.hasGisCoordinates();
		if(!bolIsGisAvailable && set.isShowGis()) this.forceGraphView();
		
		
		if(this.set.getExplosionSettingsList().getActiveExplosionSettings()==null) {
		    canvas = ((set.isShowGis() && bolIsGisAvailable) ? creator.createGisCanvas() : creator.createGraphCanvas());
		} else {
			canvas = ((set.isShowGis() && bolIsGisAvailable) ? creator.createExplosionGisCanvas() : creator.createExplosionGraphCanvas());
		}
		canvas.addCanvasListener(this);
		canvas.addTracingListener(this);
		if(canvas instanceof IExplosionCanvas) ((IExplosionCanvas) canvas).addExplosionListener(this);
		switchButton.setText("Switch to " + ((canvas instanceof IGisCanvas) ? "Graph" : "GIS"));
		switchButton.setEnabled(bolIsGisAvailable);

		String warningTable = null;

		if (!creator.getSkippedDeliveryRows().isEmpty() && !creator.getSkippedDeliveryRelationRows().isEmpty()) {
			warningTable = "the deliveries table and the delivery relations table";
		} else if (!creator.getSkippedDeliveryRows().isEmpty()) {
			warningTable = "the deliveries table";
		} else if (!creator.getSkippedDeliveryRelationRows().isEmpty()) {
			warningTable = "the delivery relations table";
		}

		String warning = warningTable != null ? "Some rows from " + warningTable + " could not be imported."
				+ " Execute the Tracing View for more information." : null;
		boolean showLotBasedInfo = creator.isLotBased() && !isUpdate;

		if (warning != null && showLotBasedInfo) {
			KnimeUtils.runWhenDialogOpens(panel, () -> {
				Dialogs.showWarningMessage(panel, warning);
				Dialogs.showInfoMessage(panel, TracingUtils.LOT_BASED_INFO);
			});
		} else if (warning != null) {
			KnimeUtils.runWhenDialogOpens(panel, () -> Dialogs.showWarningMessage(panel, warning));
		} else if (showLotBasedInfo) {
			KnimeUtils.runWhenDialogOpens(panel, () -> Dialogs.showInfoMessage(panel, TracingUtils.LOT_BASED_INFO));
		}

//		if(false && this.isExplosionViewActive()) {
//			// explosion view
//			panel.add(new ExplosionCanvasContainerWithLabelAndCloseFeature(canvas, 
//					this.set.getExplosionSettingsList().getActiveExplosionSettings().getKey(), c -> this.closeExplosionViewRequested(c)),
//					BorderLayout.CENTER);
//		} else {
			panel.add(canvas.getComponent(), BorderLayout.CENTER);
//		}
//		JPanel objPanel = new JPanel();
//		JLayeredPane objPane = new JLayeredPane();
//		objPanel.add(objPane);
//		panel.add(objPanel, BorderLayout.CENTER);
	    //canvas.getComponent().setBounds(objPane.getBounds());
		//panel.add(canvas.getComponent(), BorderLayout.CENTER);
//		objPane.add(canvas.getComponent(), new Integer(0), 0);
		
//        if(this.isExplosionViewActive()) {
//			this.addLabelToExplosionView();
//        } else {
//        	  objPane.setLayout(new BorderLayout());
//        	  
//		}
//        objPane.revalidate();
		panel.revalidate();

		logger.finest("leaving");
		return warning;
	}
	
	private boolean isExplosionViewActive() { return this.set.getExplosionSettingsList().getActiveExplosionSettings()!=null; }
	
//	private void addLabelToExplosionView() {
//	  this.gobjExplosionViewLabel = new ExplosionViewLabel(this.set.getExplosionSettingsList().getActiveExplosionSettings().getKey());
//	}
	
//	private void removeLabelFromExplosionViewLabel() {
//		if(this.gobjExplosionViewLabel!=null) {
//			this.gobjExplosionViewLabel.remove();
//			this.gobjExplosionViewLabel = null;
//		}
//	}

	private void updateCanvas() {
		undoButton.setEnabled(false);
		redoButton.setEnabled(false);

		try {
			createCanvas(true);
			updateStatusVariables();
		} catch (NotConfigurableException ex) {
			ex.printStackTrace();
		}

		undoButton.setEnabled(!undoStack.isEmpty());
		redoButton.setEnabled(!redoStack.isEmpty());
	}

	private void updateGisBox() {
		gisBox.removeItemListener(gisBoxListener);
		gisBox.setEnabled(set.isShowGis());
		gisBox.setSelectedItem(set.getGisType());
		gisBox.addItemListener(gisBoxListener);
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
	
	private void forceGraphView() {
		
		Dialogs.showInfoMessage(this.getPanel(), "No GIS information available. Graph mode will be activated.");    
		
		if(!undoStack.isEmpty()) {
			
			TracingChange lastTracingChange = undoStack.pop();
			TracingChange newTracingChange = TracingChange.Builder.createViewChange(
					set.isShowGis(), false, set.getGisType(), set.getGisType());
			
			undoStack.push(newTracingChange);
			undoStack.push(lastTracingChange);
			
		}
		
		this.set.setShowGis(false);
	}

	private void updateStatusVariables() {
		transform = canvas.getTransform();
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

		joinEdges = canvas.getOptionsPanel().isJoinEdges();
		skipEdgelessNodes = canvas.getOptionsPanel().isSkipEdgelessNodes();
		showEdgesInMetaNode = canvas.getOptionsPanel().isShowEdgesInMetaNode();
		arrowHeadType = canvas.getOptionsPanel().getArrowHeadType();
		nodeLabelPosition = canvas.getOptionsPanel().getNodeLabelPosition();
		showLegend = canvas.getOptionsPanel().isShowLegend();
		enforeTemporalOrder = canvas.isEnforceTemporalOrder();
		showForward = canvas.isShowForward();
		showDeliveriesWithoutDate = canvas.isShowDeliveriesWithoutDate();
		showToDate = canvas.getShowToDate();

		nodeSize = canvas.getOptionsPanel().getNodeSize();
		nodeMaxSize = canvas.getOptionsPanel().getNodeMaxSize();
		edgeThickness = canvas.getOptionsPanel().getEdgeThickness();
		edgeMaxThickness = canvas.getOptionsPanel().getEdgeMaxThickness();
		fontSize = canvas.getOptionsPanel().getFontSize();
		fontBold = canvas.getOptionsPanel().isFontBold();
		label = canvas.getOptionsPanel().getLabel();

		borderAlpha = canvas.getOptionsPanel().getBorderAlpha();
		avoidOverlay = canvas.getOptionsPanel().isAvoidOverlay();
	}

	private void undoRedoPressed(boolean undo) {
		TracingChange change = (undo ? undoStack : redoStack).pop();

		if (undoStack.isEmpty()) {
			undoButton.setEnabled(false);
		}

		if (redoStack.isEmpty()) {
			redoButton.setEnabled(false);
		}

		if (change.isViewChange()) {
			updateSettings();

			if (undo) {
				change.undo(set);
			} else {
				change.redo(set);
			}

			updateGisBox();
			updateCanvas();
		} else {
			
			canvas.removeCanvasListener(this);
			canvas.removeTracingListener(this);
			if(canvas instanceof IExplosionCanvas) ((IExplosionCanvas) canvas).removeExplosionListener(this);

			if (undo) {
				change.undo(canvas);
			} else {
				change.redo(canvas);
			}
			if(canvas instanceof ExplosionTracingGraphCanvas) ((ExplosionTracingGraphCanvas) canvas).repositionBoundaryNodes();
			
			canvas.addCanvasListener(this);
			canvas.addTracingListener(this);
			if(canvas instanceof IExplosionCanvas) ((IExplosionCanvas) canvas).addExplosionListener(this);
		
			updateStatusVariables();
//			canvas.refreshPaintables();
		}

		(undo ? redoStack : undoStack).push(change);

		if (!undoStack.isEmpty()) {
			undoButton.setEnabled(true);
		}

		if (!redoStack.isEmpty()) {
			redoButton.setEnabled(true);
		}
	}

	private void resetPressed(JButton button) {
		if (Dialogs.showOkCancelDialog(canvas.getComponent(), "This cannot be made undone. Proceed?",
				button.getText()) == Dialogs.OkCancelResult.OK) {
			updateSettings();

			if (button == resetWeightsButton) {
				set.clearWeights();
			} else if (button == resetCrossButton) {
				set.clearCrossContamination();
			} else if (button == resetKillButton) {
				set.clearKillContamination();
			} else if (button == resetObservedButton) {
				set.clearObserved();
			}

			undoStack.clear();
			redoStack.clear();
			updateCanvas();
		}
	}

	private void switchPressed() {
		updateSettings();
		this.changeOccured(TracingChange.Builder.createViewChange(
				set.isShowGis(), !set.isShowGis(), set.getGisType(), set.getGisType()));
		set.setShowGis(!set.isShowGis());
		gisBox.setEnabled(set.isShowGis());
		updateCanvas();
	}

	private static Map<String, Set<String>> copy(Map<String, Set<String>> map) {
		Map<String, Set<String>> copy = new LinkedHashMap<>();

		map.forEach((key, value) -> copy.put(key, new LinkedHashSet<>(value)));

		return copy;
	}

	@Override
	public void openExplosionViewRequested(ICanvas<?> source, String strKey) { //, Set<String> containedNodes) {
		// TODO Auto-generated method stub
		updateSettings();
		
        ExplosionSettings objFromES = this.set.getExplosionSettingsList().getActiveExplosionSettings();
//		Set<String> objCurrentNodes = 
//				(this.set.getExplosionSettingsList().getActiveExplosionSettings()==null?
//				 null:
//				 this.set.getExplosionSettingsList().getActiveExplosionSettings().getContainedNodes());
		
		ExplosionSettings objToES = this.set.getExplosionSettingsList().setActiveExplosionSettings(strKey, this.collapsedNodes.get(strKey)); //, containedNodes);
		
		this.changeOccured(TracingChange.Builder.createViewChange(
				this.set.isShowGis(), this.set.isShowGis(), this.set.getGisType(),
				this.set.getGisType(), objFromES, objToES, TracingChange.ExplosionViewAction.Opened));
		
		updateCanvas();
	}
	
//	private void openExplosionView(String strKey, Set<String> explodedNodes) {
//		
//	}
	
	@Override
	public void closeExplosionViewRequested(IExplosionCanvas<?> source) {
		// TODO Auto-generated method stub
		ExplosionSettings objCloseES = this.set.getExplosionSettingsList().getActiveExplosionSettings();
		if(objCloseES==null) return; 
		
		updateSettings();
		
		this.set.getExplosionSettingsList().setActiveExplosionSettings(objCloseES, false);
		
		this.changeOccured(TracingChange.Builder.createViewChange(
				this.set.isShowGis(), this.set.isShowGis(), this.set.getGisType(),
				this.set.getGisType(), objCloseES, this.set.getExplosionSettingsList().getActiveExplosionSettings(),TracingChange.ExplosionViewAction.Closed));
		
	    updateCanvas();
	}
	
//	@Override
//	public void closeExplosionViewRequested() {
//		// TODO Auto-generated method stub
//		
//		ExplosionSettings objCloseES = this.set.getExplosionSettingsList().getActiveExplosionSettings();
//		if(objCloseES==null) return; 
//		
//		updateSettings();
//		
//		this.set.getExplosionSettingsList().setActiveExplosionSettings(objCloseES, false);
//		
//		this.changeOccured(TracingChange.Builder.createViewChange(
//				this.set.isShowGis(), this.set.isShowGis(), this.set.getGisType(),
//				this.set.getGisType(), objCloseES, this.set.getExplosionSettingsList().getActiveExplosionSettings(),TracingChange.ExplosionViewAction.Closed));
//		
//	    updateCanvas();
//	}
	
//	public void closeExplosionView() {
//		// TODO Auto-generated method stub
//		ExplosionSettings objCloseES = this.set.getExplosionSettingsList().getActiveExplosionSettings();
//		if(objCloseES==null) return; 
//		
//		updateSettings();
//		
//		this.set.getExplosionSettingsList().setActiveExplosionSettings(objCloseES, false);
//		
//		this.changeOccured(TracingChange.Builder.createViewChange(
//				this.set.isShowGis(), this.set.isShowGis(), this.set.getGisType(),
//				this.set.getGisType(), objCloseES, this.set.getExplosionSettingsList().getActiveExplosionSettings(),TracingChange.ExplosionViewAction.Closed));
//		
//	    updateCanvas();
//	}

@Override
public void nodeSubsetChanged(ICanvas<?> source) {
	// TODO Auto-generated method stub
	
}
	
//	private Set<String> getNodesInCurrentView() {
//		if(this.gobjViewStack.size()==1) {
//			//return this.
//		} else {
//			//return this.getCurrentExplosionSettings
//		}
//		return new HashSet<String>();
//	}
  private class ExplosionCanvasContainerWithLabelAndCloseFeature extends JPanel implements MouseListener, ComponentListener{
	private String gstrKey;
	private String gstrLabel;
	private JLabel gguiLabel;
	private ICanvas canvas;
	private Consumer<ICanvas<?>> consumer; 
	
	
	
	private class PostPaintable implements Paintable {
		
		PostPaintable() {}
		
		@Override
		public void paint(Graphics graphics) {
			// TODO Auto-generated method stub
			if(ExplosionCanvasContainerWithLabelAndCloseFeature.this.gguiLabel!=null) {
				Graphics2D g = (Graphics2D) graphics;
				//String strLabel = this.gstrKey + " Explosion View";
				// TODO Auto-generated method stub
				int w = ExplosionCanvasContainerWithLabelAndCloseFeature.this.canvas.getCanvasSize().width;
				Font font = ExplosionCanvasContainerWithLabelAndCloseFeature.this.gguiLabel.getFont(); // new Font("Default", Font.BOLD, 10);
				int fontHeight = g.getFontMetrics(font).getHeight();
				int fontAscent = g.getFontMetrics(font).getAscent();
				int dy = 2;
		
				int dx = 5;
				int sw = (int) font.getStringBounds(ExplosionCanvasContainerWithLabelAndCloseFeature.this.gstrLabel, g.getFontRenderContext()).getWidth();
				Color currentColor = g.getColor();
				Font currentFont = g.getFont();
		
				g.setColor(ZoomingPaintable.BACKGROUND);
				g.fillRect(w/2 - sw/2 - dx, -1, sw + 2 * dx, fontHeight + 2 * dy);
				g.setColor(Color.BLACK);
				g.drawRect(w/2 - sw/2 - dx, -1, sw + 2 * dx, fontHeight + 2 * dy);
				g.setFont(font);
				g.drawString(ExplosionCanvasContainerWithLabelAndCloseFeature.this.gstrLabel, w/2 - sw/2, dy + fontAscent);
		
				g.setColor(currentColor);
				g.setFont(currentFont);
				ExplosionCanvasContainerWithLabelAndCloseFeature.this.gguiLabel.repaint();
			}
		}

		@Override
		public boolean useTransform() {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
	
	ExplosionCanvasContainerWithLabelAndCloseFeature(ICanvas<?> canvas, String strKey, Consumer<ICanvas<?>> consumer) {
		this.gstrKey = strKey;
		this.gstrLabel = strKey + " Explosion View";
		this.canvas = canvas;
		this.consumer = consumer;
		//this.font = new Font("Default", Font.BOLD, 10);
		this.gguiLabel = new JLabel();
		this.gguiLabel.setFont(new Font("Default", Font.BOLD, 10));
		this.gguiLabel.setText("x");
		//this.gguiLabel.setFont(new Font("Default", Font.BOLD, 10));
		this.gguiLabel.setForeground(Color.BLACK);
		this.gguiLabel.addMouseListener(this);
		
		this.setLayout(new BorderLayout());
		JLayeredPane objPane = new JLayeredPane();
		
		objPane.add(canvas.getComponent(), new Integer(0), 0);
		objPane.add(this.gguiLabel, new Integer(1), 0);
		this.add(objPane, BorderLayout.CENTER);
	
		this.addComponentListener(this);
		canvas.getViewer().addPostRenderPaintable(new PostPaintable());
//		JLayeredPane objPane = (JLayeredPane) TracingViewNodeDialog.this.canvas.getComponent().getParent();
//		objPane.setBackground(Color.GREEN);
//		objPane.add(this.gguiLabel,1,0);
//		this.updateBounds();
		//TracingViewNodeDialog.this.panel.add(this.gguiLabel,TracingViewNodeDialog.this.panel.getComponentCount());
		//objPane.addComponentListener(this);
		//TracingViewNodeDialog.this.panel.addComponentListener(this);
		//TracingViewNodeDialog.this.canvas.getViewer().addPostRenderPaintable(this);
	}
	
//	public void remove() {
//		TracingViewNodeDialog.this.panel.remove(this.gguiLabel);
//	}
	
	private void updateBounds() {
		Rectangle rect = new Rectangle(this.getSize()); //this.getBounds(); 
		this.canvas.getComponent().setBounds(rect);
		Dimension d = this.canvas.getViewer().getSize();
		Point p = this.canvas.getViewer().getLocation();
		
		Graphics2D g = (Graphics2D) this.getGraphics();
		//Graphics2D g = (Graphics2D) TracingViewNodeDialog.this.getGraphics();
		//String strLabel = this.gstrKey + " Explosion View";
		// TODO Auto-generated method stub
		int w = d.width;  //TracingViewNodeDialog.this.panel.getWidth();//canvas.getCanvasSize().width;
		
		Font font = this.gguiLabel.getFont();
		int fontHeight = g.getFontMetrics(font).getHeight();
		//int fontAscent = g.getFontMetrics(font).getAscent();
		int dy = 2;

		int dx = 5;
		int sw = (int) font.getStringBounds(this.gstrLabel, g.getFontRenderContext()).getWidth();
		//Color currentColor = g.getColor();
		//Font currentFont = g.getFont();
		
		//this.gguiLabel.setLocation(w/2+sw/2+dx, -1);
		this.gguiLabel.setBounds(p.x + w/2 + sw/2 + 2*dx, p.y-1, fontHeight + 2 * dy, fontHeight + 2 * dy);

		//g.setColor(ZoomingPaintable.BACKGROUND);
		//g.fillRect(w - sw - 2 * dx, -1, sw + 2 * dx, fontHeight + 2 * dy);
		//g.setColor(Color.BLACK);
		//g.drawRect(w - sw - 2 * dx, -1, sw + 2 * dx, fontHeight + 2 * dy);
		//g.setFont(font);
		//g.drawString(strLabel, w - sw - dx, dy + fontAscent);

		//g.setColor(currentColor);
		//g.setFont(currentFont);
		//this.gguiLabel.repaint();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		//TracingViewNodeDialog.this.closeExplosionViewRequested(canvas);
		//function.apply(canvas);
		consumer.accept(canvas);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		this.gguiLabel.setForeground(Color.BLUE);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		this.gguiLabel.setForeground(Color.BLACK);
	}

	@Override
	public void componentResized(ComponentEvent e) {
		// TODO Auto-generated method stub
		this.updateBounds();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

  }

}
