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

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.NodeSettings;
import de.bund.bfr.knime.XmlConverter;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.openkrise.TracingUtils;
import de.bund.bfr.knime.openkrise.views.Activator;
import de.bund.bfr.knime.openkrise.views.canvas.ITracingCanvas;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

public class TracingViewSettings extends NodeSettings {

	protected static final XmlConverter SERIALIZER = new XmlConverter(
			Activator.class.getClassLoader());

	private static final String CFG_SHOW_GIS = "ShowGis";
	private static final String CFG_EXPORT_AS_SVG = "ExportAsSvg";
	private static final String CFG_SKIP_EDGELESS_NODES = "SkipEdgelessNodes";
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
	private static final String CFG_OBSERVED_NODES = "Filter";
	private static final String CFG_OBSERVED_EDGES = "EdgeFilter";
	private static final String CFG_ENFORCE_TEMPORAL_ORDER = "EnforceTemporalOrder";
	private static final String CFG_SHOW_FORWARD = "ShowConnected";

	private boolean showGis;
	private boolean exportAsSvg;
	private boolean skipEdgelessNodes;
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
	private Map<String, Boolean> observedNodes;
	private Map<String, Boolean> observedEdges;
	private boolean enforeTemporalOrder;
	private boolean showForward;

	private GraphSettings graphSettings;
	private GisSettings gisSettings;

	public TracingViewSettings() {
		showGis = false;
		exportAsSvg = false;
		skipEdgelessNodes = true;
		joinEdges = true;
		arrowInMiddle = false;
		showLegend = false;
		editingMode = Mode.PICKING;
		canvasSize = new Dimension(400, 600);
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
		observedNodes = new LinkedHashMap<>();
		observedEdges = new LinkedHashMap<>();
		enforeTemporalOrder = false;
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
			exportAsSvg = settings.getBoolean(CFG_EXPORT_AS_SVG);
		} catch (InvalidSettingsException e) {
		}

		try {
			skipEdgelessNodes = settings.getBoolean(CFG_SKIP_EDGELESS_NODES);
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
			canvasSize = (Dimension) SERIALIZER.fromXml(settings
					.getString(CFG_CANVAS_SIZE));
		} catch (InvalidSettingsException e) {
		}

		try {
			selectedNodes = (List<String>) SERIALIZER.fromXml(settings
					.getString(CFG_SELECTED_NODES));
		} catch (InvalidSettingsException e) {
		}

		try {
			selectedEdges = (List<String>) SERIALIZER.fromXml(settings
					.getString(CFG_SELECTED_EDGES));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeHighlightConditions = (HighlightConditionList) SERIALIZER
					.fromXml(settings.getString(CFG_NODE_HIGHLIGHT_CONDITIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeHighlightConditions = (HighlightConditionList) SERIALIZER
					.fromXml(settings.getString(CFG_EDGE_HIGHLIGHT_CONDITIONS));
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
			nodeWeights = (Map<String, Double>) SERIALIZER.fromXml(settings
					.getString(CFG_NODE_WEIGHTS));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeWeights = (Map<String, Double>) SERIALIZER.fromXml(settings
					.getString(CFG_EDGE_WEIGHTS));
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
			observedNodes = (Map<String, Boolean>) SERIALIZER.fromXml(settings
					.getString(CFG_OBSERVED_NODES));
		} catch (InvalidSettingsException e) {
		}

		try {
			observedEdges = (Map<String, Boolean>) SERIALIZER.fromXml(settings
					.getString(CFG_OBSERVED_EDGES));
		} catch (InvalidSettingsException e) {
		}

		try {
			enforeTemporalOrder = settings
					.getBoolean(CFG_ENFORCE_TEMPORAL_ORDER);
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
		settings.addBoolean(CFG_EXPORT_AS_SVG, exportAsSvg);
		settings.addBoolean(CFG_SKIP_EDGELESS_NODES, skipEdgelessNodes);
		settings.addBoolean(CFG_JOIN_EDGES, joinEdges);
		settings.addBoolean(CFG_ARROW_IN_MIDDLE, arrowInMiddle);
		settings.addBoolean(CFG_SHOW_LEGEND, showLegend);
		settings.addString(CFG_EDITING_MODE, editingMode.name());
		settings.addString(CFG_CANVAS_SIZE, SERIALIZER.toXml(canvasSize));
		settings.addString(CFG_SELECTED_NODES, SERIALIZER.toXml(selectedNodes));
		settings.addString(CFG_SELECTED_EDGES, SERIALIZER.toXml(selectedEdges));
		settings.addString(CFG_NODE_HIGHLIGHT_CONDITIONS,
				SERIALIZER.toXml(nodeHighlightConditions));
		settings.addString(CFG_EDGE_HIGHLIGHT_CONDITIONS,
				SERIALIZER.toXml(edgeHighlightConditions));
		settings.addString(CFG_COLLAPSED_NODES,
				SERIALIZER.toXml(collapsedNodes));
		settings.addString(CFG_LABEL, label);

		settings.addString(CFG_NODE_WEIGHTS, SERIALIZER.toXml(nodeWeights));
		settings.addString(CFG_EDGE_WEIGHTS, SERIALIZER.toXml(edgeWeights));
		settings.addString(CFG_NODE_CROSS_CONTAMINATIONS,
				SERIALIZER.toXml(nodeCrossContaminations));
		settings.addString(CFG_EDGE_CROSS_CONTAMINATIONS,
				SERIALIZER.toXml(edgeCrossContaminations));
		settings.addString(CFG_OBSERVED_NODES, SERIALIZER.toXml(observedNodes));
		settings.addString(CFG_OBSERVED_EDGES, SERIALIZER.toXml(observedEdges));
		settings.addBoolean(CFG_ENFORCE_TEMPORAL_ORDER, enforeTemporalOrder);
		settings.addBoolean(CFG_SHOW_FORWARD, showForward);

		graphSettings.saveSettings(settings);
		gisSettings.saveSettings(settings);
	}

	public void setFromCanvas(ITracingCanvas<?> canvas, boolean resized) {
		selectedNodes = new ArrayList<>(canvas.getSelectedNodeIds());
		selectedEdges = new ArrayList<>(canvas.getSelectedEdgeIds());

		Collections.sort(selectedNodes);
		Collections.sort(selectedEdges);

		showLegend = canvas.isShowLegend();
		joinEdges = canvas.isJoinEdges();
		arrowInMiddle = canvas.isArrowInMiddle();
		skipEdgelessNodes = canvas.isSkipEdgelessNodes();
		label = canvas.getLabel();

		nodeHighlightConditions = canvas.getNodeHighlightConditions();
		edgeHighlightConditions = canvas.getEdgeHighlightConditions();
		editingMode = canvas.getEditingMode();

		if (resized) {
			canvasSize = canvas.getCanvasSize();
		}

		collapsedNodes = new LinkedHashMap<>();

		Map<String, Set<String>> collapsed = canvas.getCollapsedNodes();

		for (String id1 : collapsed.keySet()) {
			collapsedNodes.put(id1, new LinkedHashMap<String, Point2D>());

			for (String id2 : collapsed.get(id1)) {
				collapsedNodes.get(id1).put(id2, null);
			}
		}

		nodeWeights = canvas.getNodeWeights();
		edgeWeights = canvas.getEdgeWeights();
		nodeCrossContaminations = canvas.getNodeCrossContaminations();
		edgeCrossContaminations = canvas.getEdgeCrossContaminations();
		observedNodes = canvas.getObservedNodes();
		observedEdges = canvas.getObservedEdges();
		enforeTemporalOrder = canvas.isEnforceTemporalOrder();
		showForward = canvas.isShowForward();
	}

	public void setToCanvas(ITracingCanvas<?> canvas) {
		canvas.setShowLegend(showLegend);
		canvas.setCanvasSize(canvasSize);
		canvas.setEditingMode(editingMode);
		canvas.setJoinEdges(joinEdges);
		canvas.setArrowInMiddle(arrowInMiddle);
		canvas.setLabel(label);

		Map<String, Set<String>> collapsed = new LinkedHashMap<>();

		for (String id : collapsedNodes.keySet()) {
			collapsed.put(id, collapsedNodes.get(id).keySet());
		}

		canvas.setCollapsedNodes(collapsed);

		canvas.setNodeHighlightConditions(TracingUtils.renameColumns(
				nodeHighlightConditions, canvas.getNodeSchema().getMap()
						.keySet()));
		canvas.setEdgeHighlightConditions(TracingUtils.renameColumns(
				edgeHighlightConditions, canvas.getEdgeSchema().getMap()
						.keySet()));
		canvas.setSkipEdgelessNodes(skipEdgelessNodes);
		canvas.setSelectedNodeIds(new LinkedHashSet<>(selectedNodes));
		canvas.setSelectedEdgeIds(new LinkedHashSet<>(selectedEdges));

		canvas.setNodeWeights(nodeWeights);
		canvas.setEdgeWeights(edgeWeights);
		canvas.setNodeCrossContaminations(nodeCrossContaminations);
		canvas.setEdgeCrossContaminations(edgeCrossContaminations);
		canvas.setObservedNodes(observedNodes);
		canvas.setObservedEdges(observedEdges);
		canvas.setEnforceTemporalOrder(enforeTemporalOrder);
		canvas.setShowForward(showForward);
	}

	public boolean isShowGis() {
		return showGis;
	}

	public void setShowGis(boolean showGis) {
		this.showGis = showGis;
	}

	public boolean isExportAsSvg() {
		return exportAsSvg;
	}

	public void setExportAsSvg(boolean exportAsSvg) {
		this.exportAsSvg = exportAsSvg;
	}

	public boolean isSkipEdgelessNodes() {
		return skipEdgelessNodes;
	}

	public void setSkipEdgelessNodes(boolean skipEdgelessNodes) {
		this.skipEdgelessNodes = skipEdgelessNodes;
	}

	public boolean isJoinEdges() {
		return joinEdges;
	}

	public void setJoinEdges(boolean joinEdges) {
		this.joinEdges = joinEdges;
	}

	public boolean isArrowInMiddle() {
		return arrowInMiddle;
	}

	public void setArrowInMiddle(boolean arrowInMiddle) {
		this.arrowInMiddle = arrowInMiddle;
	}

	public boolean isShowLegend() {
		return showLegend;
	}

	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
	}

	public Mode getEditingMode() {
		return editingMode;
	}

	public void setEditingMode(Mode editingMode) {
		this.editingMode = editingMode;
	}

	public Dimension getCanvasSize() {
		return canvasSize;
	}

	public void setCanvasSize(Dimension canvasSize) {
		this.canvasSize = canvasSize;
	}

	public List<String> getSelectedNodes() {
		return selectedNodes;
	}

	public void setSelectedNodes(List<String> selectedNodes) {
		this.selectedNodes = selectedNodes;
	}

	public List<String> getSelectedEdges() {
		return selectedEdges;
	}

	public void setSelectedEdges(List<String> selectedEdges) {
		this.selectedEdges = selectedEdges;
	}

	public HighlightConditionList getNodeHighlightConditions() {
		return nodeHighlightConditions;
	}

	public void setNodeHighlightConditions(
			HighlightConditionList nodeHighlightConditions) {
		this.nodeHighlightConditions = nodeHighlightConditions;
	}

	public HighlightConditionList getEdgeHighlightConditions() {
		return edgeHighlightConditions;
	}

	public void setEdgeHighlightConditions(
			HighlightConditionList edgeHighlightConditions) {
		this.edgeHighlightConditions = edgeHighlightConditions;
	}

	public Map<String, Map<String, Point2D>> getCollapsedNodes() {
		return collapsedNodes;
	}

	public void setCollapsedNodes(
			Map<String, Map<String, Point2D>> collapsedNodes) {
		this.collapsedNodes = collapsedNodes;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Map<String, Double> getNodeWeights() {
		return nodeWeights;
	}

	public void setNodeWeights(Map<String, Double> nodeWeights) {
		this.nodeWeights = nodeWeights;
	}

	public Map<String, Double> getEdgeWeights() {
		return edgeWeights;
	}

	public void setEdgeWeights(Map<String, Double> edgeWeights) {
		this.edgeWeights = edgeWeights;
	}

	public Map<String, Boolean> getNodeCrossContaminations() {
		return nodeCrossContaminations;
	}

	public void setNodeCrossContaminations(
			Map<String, Boolean> nodeCrossContaminations) {
		this.nodeCrossContaminations = nodeCrossContaminations;
	}

	public Map<String, Boolean> getEdgeCrossContaminations() {
		return edgeCrossContaminations;
	}

	public void setEdgeCrossContaminations(
			Map<String, Boolean> edgeCrossContaminations) {
		this.edgeCrossContaminations = edgeCrossContaminations;
	}

	public Map<String, Boolean> getObservedNodes() {
		return observedNodes;
	}

	public void setObservedNodes(Map<String, Boolean> observedNodes) {
		this.observedNodes = observedNodes;
	}

	public Map<String, Boolean> getObservedEdges() {
		return observedEdges;
	}

	public void setObservedEdges(Map<String, Boolean> observedEdges) {
		this.observedEdges = observedEdges;
	}

	public boolean isEnforeTemporalOrder() {
		return enforeTemporalOrder;
	}

	public void setEnforeTemporalOrder(boolean enforeTemporalOrder) {
		this.enforeTemporalOrder = enforeTemporalOrder;
	}

	public boolean isShowForward() {
		return showForward;
	}

	public void setShowForward(boolean showForward) {
		this.showForward = showForward;
	}

	public GraphSettings getGraphSettings() {
		return graphSettings;
	}

	public void setGraphSettings(GraphSettings graphSettings) {
		this.graphSettings = graphSettings;
	}

	public GisSettings getGisSettings() {
		return gisSettings;
	}

	public void setGisSettings(GisSettings gisSettings) {
		this.gisSettings = gisSettings;
	}
}
