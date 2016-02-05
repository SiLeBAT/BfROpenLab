/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.google.common.collect.Ordering;

import de.bund.bfr.knime.NodeSettings;
import de.bund.bfr.knime.XmlConverter;
import de.bund.bfr.knime.gis.GisType;
import de.bund.bfr.knime.gis.views.canvas.backward.BackwardUtils;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.openkrise.views.Activator;
import de.bund.bfr.knime.openkrise.views.canvas.ITracingCanvas;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

public class TracingViewSettings extends NodeSettings {

	protected static final XmlConverter SERIALIZER = new XmlConverter(Activator.class.getClassLoader());

	private static final String CFG_SHOW_GIS = "ShowGis";
	private static final String CFG_GIS_TYPE = "GisType";
	private static final String CFG_EXPORT_AS_SVG = "ExportAsSvg";
	private static final String CFG_SKIP_EDGELESS_NODES = "SkipEdgelessNodes";
	private static final String CFG_SHOW_EDGES_IN_META_NODE = "ShowEdgesInMetaNode";
	private static final String CFG_JOIN_EDGES = "JoinEdges";
	private static final String CFG_ARROW_IN_MIDDLE = "ArrowInMiddle";
	private static final String CFG_SHOW_LEGEND = "GraphShowLegend";
	private static final String CFG_SELECTED_NODES = "GraphSelectedNodes";
	private static final String CFG_SELECTED_EDGES = "GraphSelectedEdges";
	private static final String CFG_EDITING_MODE = "GraphEditingMode2";
	private static final String CFG_CANVAS_SIZE = "GraphCanvasSize";
	private static final String CFG_NODE_HIGHLIGHT_CONDITIONS = "GraphNodeHighlightConditions";
	private static final String CFG_EDGE_HIGHLIGHT_CONDITIONS = "GraphEdgeHighlightConditions";
	private static final String CFG_COLLAPSED_NODES = "CollapsedNodes";
	private static final String CFG_LABEL = "Label";

	private static final String CFG_NODE_WEIGHTS = "CaseWeights";
	private static final String CFG_EDGE_WEIGHTS = "EdgeWeights";
	private static final String CFG_NODE_CROSS_CONTAMINATIONS = "CrossContaminations";
	private static final String CFG_EDGE_CROSS_CONTAMINATIONS = "EdgeCrossContaminations";
	private static final String CFG_NODE_KILL_CONTAMINATIONS = "NodeKillContaminations";
	private static final String CFG_EDGE_KILL_CONTAMINATIONS = "EdgeKillContaminations";
	private static final String CFG_OBSERVED_NODES = "Filter";
	private static final String CFG_OBSERVED_EDGES = "EdgeFilter";
	private static final String CFG_ENFORCE_TEMPORAL_ORDER = "EnforceTemporalOrder";
	private static final String CFG_SHOW_FORWARD = "ShowConnected";

	private boolean showGis;
	private GisType gisType;
	private boolean exportAsSvg;
	private boolean skipEdgelessNodes;
	private boolean showEdgesInMetaNode;
	private boolean joinEdges;
	private boolean arrowInMiddle;
	private boolean showLegend;
	private Mode editingMode;
	private Dimension canvasSize;
	private List<String> selectedNodes;
	private List<String> selectedEdges;
	private HighlightConditionList nodeHighlightConditions;
	private HighlightConditionList edgeHighlightConditions;
	private Map<String, Map<String, Point2D>> collapsedNodes;
	private String label;

	private Map<String, Double> nodeWeights;
	private Map<String, Double> edgeWeights;
	private Map<String, Boolean> nodeCrossContaminations;
	private Map<String, Boolean> edgeCrossContaminations;
	private Map<String, Boolean> nodeKillContaminations;
	private Map<String, Boolean> edgeKillContaminations;
	private Map<String, Boolean> observedNodes;
	private Map<String, Boolean> observedEdges;
	private boolean enforeTemporalOrder;
	private boolean showForward;

	private GraphSettings graphSettings;
	private GisSettings gisSettings;

	public TracingViewSettings() {
		showGis = false;
		gisType = GisType.SHAPEFILE;
		exportAsSvg = false;
		skipEdgelessNodes = false;
		showEdgesInMetaNode = false;
		joinEdges = false;
		arrowInMiddle = false;
		showLegend = false;
		editingMode = Mode.PICKING;
		canvasSize = null;
		selectedNodes = new ArrayList<>();
		selectedEdges = new ArrayList<>();
		nodeHighlightConditions = new HighlightConditionList();
		edgeHighlightConditions = new HighlightConditionList();
		collapsedNodes = new LinkedHashMap<>();
		label = null;

		nodeWeights = new LinkedHashMap<>();
		edgeWeights = new LinkedHashMap<>();
		nodeCrossContaminations = new LinkedHashMap<>();
		edgeCrossContaminations = new LinkedHashMap<>();
		nodeKillContaminations = new LinkedHashMap<>();
		edgeKillContaminations = new LinkedHashMap<>();
		observedNodes = new LinkedHashMap<>();
		observedEdges = new LinkedHashMap<>();
		enforeTemporalOrder = true;
		showForward = false;

		graphSettings = new GraphSettings();
		gisSettings = new GisSettings();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void loadSettings(NodeSettingsRO settings) {
		try {
			showGis = settings.getBoolean(CFG_SHOW_GIS);
		} catch (InvalidSettingsException e) {
		}

		try {
			gisType = GisType.valueOf(settings.getString(CFG_GIS_TYPE));
		} catch (InvalidSettingsException | IllegalArgumentException e) {
		}

		try {
			exportAsSvg = settings.getBoolean(CFG_EXPORT_AS_SVG);
		} catch (InvalidSettingsException e) {
		}

		try {
			skipEdgelessNodes = settings.getBoolean(CFG_SKIP_EDGELESS_NODES);
		} catch (InvalidSettingsException e) {
		}

		try {
			showEdgesInMetaNode = settings.getBoolean(CFG_SHOW_EDGES_IN_META_NODE);
		} catch (InvalidSettingsException e) {
		}

		try {
			joinEdges = settings.getBoolean(CFG_JOIN_EDGES);
		} catch (InvalidSettingsException e) {
		}

		try {
			arrowInMiddle = settings.getBoolean(CFG_ARROW_IN_MIDDLE);
		} catch (InvalidSettingsException e) {
		}

		try {
			showLegend = settings.getBoolean(CFG_SHOW_LEGEND);
		} catch (InvalidSettingsException e) {
		}

		try {
			editingMode = Mode.valueOf(settings.getString(CFG_EDITING_MODE));
		} catch (InvalidSettingsException e) {
		}

		try {
			canvasSize = (Dimension) SERIALIZER.fromXml(settings.getString(CFG_CANVAS_SIZE));
		} catch (InvalidSettingsException e) {
		}

		try {
			selectedNodes = (List<String>) SERIALIZER.fromXml(settings.getString(CFG_SELECTED_NODES));
		} catch (InvalidSettingsException e) {
		}

		try {
			selectedEdges = (List<String>) SERIALIZER.fromXml(settings.getString(CFG_SELECTED_EDGES));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeHighlightConditions = (HighlightConditionList) SERIALIZER
					.fromXml(BackwardUtils.toNewHighlightingFormat(settings.getString(CFG_NODE_HIGHLIGHT_CONDITIONS)));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeHighlightConditions = (HighlightConditionList) SERIALIZER
					.fromXml(BackwardUtils.toNewHighlightingFormat(settings.getString(CFG_EDGE_HIGHLIGHT_CONDITIONS)));
		} catch (InvalidSettingsException e) {
		}

		try {
			collapsedNodes = (Map<String, Map<String, Point2D>>) SERIALIZER
					.fromXml(settings.getString(CFG_COLLAPSED_NODES));
		} catch (InvalidSettingsException e) {
		}

		try {
			label = settings.getString(CFG_LABEL);
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeWeights = (Map<String, Double>) SERIALIZER.fromXml(settings.getString(CFG_NODE_WEIGHTS));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeWeights = (Map<String, Double>) SERIALIZER.fromXml(settings.getString(CFG_EDGE_WEIGHTS));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeCrossContaminations = (Map<String, Boolean>) SERIALIZER
					.fromXml(settings.getString(CFG_NODE_CROSS_CONTAMINATIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeCrossContaminations = (Map<String, Boolean>) SERIALIZER
					.fromXml(settings.getString(CFG_EDGE_CROSS_CONTAMINATIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeKillContaminations = (Map<String, Boolean>) SERIALIZER
					.fromXml(settings.getString(CFG_NODE_KILL_CONTAMINATIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeKillContaminations = (Map<String, Boolean>) SERIALIZER
					.fromXml(settings.getString(CFG_EDGE_KILL_CONTAMINATIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			observedNodes = (Map<String, Boolean>) SERIALIZER.fromXml(settings.getString(CFG_OBSERVED_NODES));
		} catch (InvalidSettingsException e) {
		}

		try {
			observedEdges = (Map<String, Boolean>) SERIALIZER.fromXml(settings.getString(CFG_OBSERVED_EDGES));
		} catch (InvalidSettingsException e) {
		}

		try {
			enforeTemporalOrder = settings.getBoolean(CFG_ENFORCE_TEMPORAL_ORDER);
		} catch (InvalidSettingsException e) {
		}

		try {
			showForward = settings.getBoolean(CFG_SHOW_FORWARD);
		} catch (InvalidSettingsException e) {
		}

		graphSettings.loadSettings(settings);
		gisSettings.loadSettings(settings);
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addBoolean(CFG_SHOW_GIS, showGis);
		settings.addString(CFG_GIS_TYPE, gisType.name());
		settings.addBoolean(CFG_EXPORT_AS_SVG, exportAsSvg);
		settings.addBoolean(CFG_SKIP_EDGELESS_NODES, skipEdgelessNodes);
		settings.addBoolean(CFG_SHOW_EDGES_IN_META_NODE, showEdgesInMetaNode);
		settings.addBoolean(CFG_JOIN_EDGES, joinEdges);
		settings.addBoolean(CFG_ARROW_IN_MIDDLE, arrowInMiddle);
		settings.addBoolean(CFG_SHOW_LEGEND, showLegend);
		settings.addString(CFG_EDITING_MODE, editingMode.name());
		settings.addString(CFG_CANVAS_SIZE, SERIALIZER.toXml(canvasSize));
		settings.addString(CFG_SELECTED_NODES, SERIALIZER.toXml(selectedNodes));
		settings.addString(CFG_SELECTED_EDGES, SERIALIZER.toXml(selectedEdges));
		settings.addString(CFG_NODE_HIGHLIGHT_CONDITIONS, SERIALIZER.toXml(nodeHighlightConditions));
		settings.addString(CFG_EDGE_HIGHLIGHT_CONDITIONS, SERIALIZER.toXml(edgeHighlightConditions));
		settings.addString(CFG_COLLAPSED_NODES, SERIALIZER.toXml(collapsedNodes));
		settings.addString(CFG_LABEL, label);

		settings.addString(CFG_NODE_WEIGHTS, SERIALIZER.toXml(nodeWeights));
		settings.addString(CFG_EDGE_WEIGHTS, SERIALIZER.toXml(edgeWeights));
		settings.addString(CFG_NODE_CROSS_CONTAMINATIONS, SERIALIZER.toXml(nodeCrossContaminations));
		settings.addString(CFG_EDGE_CROSS_CONTAMINATIONS, SERIALIZER.toXml(edgeCrossContaminations));
		settings.addString(CFG_NODE_KILL_CONTAMINATIONS, SERIALIZER.toXml(nodeKillContaminations));
		settings.addString(CFG_EDGE_KILL_CONTAMINATIONS, SERIALIZER.toXml(edgeKillContaminations));
		settings.addString(CFG_OBSERVED_NODES, SERIALIZER.toXml(observedNodes));
		settings.addString(CFG_OBSERVED_EDGES, SERIALIZER.toXml(observedEdges));
		settings.addBoolean(CFG_ENFORCE_TEMPORAL_ORDER, enforeTemporalOrder);
		settings.addBoolean(CFG_SHOW_FORWARD, showForward);

		graphSettings.saveSettings(settings);
		gisSettings.saveSettings(settings);
	}

	public void setFromCanvas(ITracingCanvas<?> canvas, boolean resized) {
		showLegend = canvas.isShowLegend();
		joinEdges = canvas.isJoinEdges();
		arrowInMiddle = canvas.isArrowInMiddle();
		skipEdgelessNodes = canvas.isSkipEdgelessNodes();
		showEdgesInMetaNode = canvas.isShowEdgesInMetaNode();
		label = canvas.getLabel();

		selectedNodes = Ordering.natural().sortedCopy(canvas.getSelectedNodeIds());
		selectedEdges = Ordering.natural().sortedCopy(canvas.getSelectedEdgeIds());
		nodeHighlightConditions = canvas.getNodeHighlightConditions();
		edgeHighlightConditions = canvas.getEdgeHighlightConditions();
		editingMode = canvas.getEditingMode();
		collapsedNodes = BackwardUtils.toOldCollapseFormat(canvas.getCollapsedNodes());

		if (resized || canvasSize == null) {
			canvasSize = canvas.getCanvasSize();
		}

		nodeWeights = canvas.getNodeWeights();
		edgeWeights = canvas.getEdgeWeights();
		nodeCrossContaminations = canvas.getNodeCrossContaminations();
		edgeCrossContaminations = canvas.getEdgeCrossContaminations();
		nodeKillContaminations = canvas.getNodeKillContaminations();
		edgeKillContaminations = canvas.getEdgeKillContaminations();
		observedNodes = canvas.getObservedNodes();
		observedEdges = canvas.getObservedEdges();
		enforeTemporalOrder = canvas.isEnforceTemporalOrder();
		showForward = canvas.isShowForward();
	}

	public void setToCanvas(ITracingCanvas<?> canvas) {
		canvas.setShowLegend(showLegend);
		canvas.setEditingMode(editingMode);
		canvas.setJoinEdges(joinEdges);
		canvas.setArrowInMiddle(arrowInMiddle);
		canvas.setLabel(label);
		canvas.setSkipEdgelessNodes(skipEdgelessNodes);
		canvas.setShowEdgesInMetaNode(showEdgesInMetaNode);
		canvas.setCollapsedNodes(BackwardUtils.toNewCollapseFormat(collapsedNodes));

		canvas.setNodeHighlightConditions(de.bund.bfr.knime.openkrise.BackwardUtils
				.renameColumns(nodeHighlightConditions, canvas.getNodeSchema().getMap().keySet()));
		canvas.setEdgeHighlightConditions(de.bund.bfr.knime.openkrise.BackwardUtils
				.renameColumns(edgeHighlightConditions, canvas.getEdgeSchema().getMap().keySet()));
		canvas.setSelectedNodeIds(new LinkedHashSet<>(selectedNodes));
		canvas.setSelectedEdgeIds(new LinkedHashSet<>(selectedEdges));

		if (canvasSize != null) {
			canvas.setCanvasSize(canvasSize);
		}

		canvas.setNodeWeights(nodeWeights);
		canvas.setEdgeWeights(edgeWeights);
		canvas.setNodeCrossContaminations(nodeCrossContaminations);
		canvas.setEdgeCrossContaminations(edgeCrossContaminations);
		canvas.setNodeKillContaminations(nodeKillContaminations);
		canvas.setEdgeKillContaminations(edgeKillContaminations);
		canvas.setObservedNodes(observedNodes);
		canvas.setObservedEdges(observedEdges);
		canvas.setEnforceTemporalOrder(enforeTemporalOrder);
		canvas.setShowForward(showForward);
	}

	public GraphSettings getGraphSettings() {
		return graphSettings;
	}

	public GisSettings getGisSettings() {
		return gisSettings;
	}

	public boolean isShowGis() {
		return showGis;
	}

	public void setShowGis(boolean showGis) {
		this.showGis = showGis;
	}

	public GisType getGisType() {
		return gisType;
	}

	public void setGisType(GisType gisType) {
		this.gisType = gisType;
	}

	public boolean isExportAsSvg() {
		return exportAsSvg;
	}

	public void setExportAsSvg(boolean exportAsSvg) {
		this.exportAsSvg = exportAsSvg;
	}

	public void clearWeights() {
		nodeWeights.clear();
		edgeWeights.clear();
	}

	public void clearCrossContamination() {
		nodeCrossContaminations.clear();
		edgeCrossContaminations.clear();
	}

	public void clearKillContamination() {
		nodeKillContaminations.clear();
		edgeKillContaminations.clear();
	}

	public void clearObserved() {
		observedNodes.clear();
		observedEdges.clear();
	}
}
