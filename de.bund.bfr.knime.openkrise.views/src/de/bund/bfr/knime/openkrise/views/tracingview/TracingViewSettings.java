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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.openkrise.views.TracingSettings;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

public class TracingViewSettings extends TracingSettings {

	public static final boolean DEFAULT_SKIP_EDGELESS_NODES = true;
	public static final boolean DEFAULT_JOIN_EDGES = true;
	public static final boolean DEFAULT_EXPORT_AS_SVG = false;
	public static final boolean DEFAULT_ENFORCE_TEMPORAL_ORDER = false;

	public static final boolean DEFAULT_GRAPH_SHOW_LEGEND = false;
	public static final int DEFAULT_GRAPH_NODE_SIZE = 10;
	public static final int DEFAULT_GRAPH_TEXT_SIZE = 12;
	public static final Mode DEFAULT_GRAPH_EDITING_MODE = Mode.PICKING;
	public static final Dimension DEFAULT_GRAPH_CANVAS_SIZE = new Dimension(
			400, 600);

	private static final String CFG_SKIP_EDGELESS_NODES = "SkipEdgelessNodes";
	private static final String CFG_JOIN_EDGES = "JoinEdges";
	private static final String CFG_EXPORT_AS_SVG = "ExportAsSvg";
	private static final String CFG_CASE_WEIGHTS = "CaseWeights";
	private static final String CFG_CROSS_CONTAMINATIONS = "CrossContaminations";
	private static final String CFG_FILTER = "Filter";
	private static final String CFG_EDGE_FILTER = "EdgeFilter";
	private static final String CFG_ENFORCE_TEMPORAL_ORDER = "EnforceTemporalOrder";

	private static final String CFG_GRAPH_SHOW_LEGEND = "GraphShowLegend";
	private static final String CFG_GRAPH_SCALE_X = "GraphScaleX";
	private static final String CFG_GRAPH_SCALE_Y = "GraphScaleY";
	private static final String CFG_GRAPH_TRANSLATION_X = "GraphTranslationX";
	private static final String CFG_GRAPH_TRANSLATION_Y = "GraphTranslationY";
	private static final String CFG_GRAPH_NODE_POSITIONS = "GraphNodePositions";
	private static final String CFG_GRAPH_NODE_SIZE = "GraphNodeSize";
	private static final String CFG_GRAPH_TEXT_SIZE = "GraphTextSize";
	private static final String CFG_GRAPH_SELECTED_NODES = "GraphSelectedNodes";
	private static final String CFG_GRAPH_SELECTED_EDGES = "GraphSelectedEdges";
	private static final String CFG_GRAPH_EDITING_MODE = "GraphEditingMode2";
	private static final String CFG_GRAPH_CANVAS_SIZE = "GraphCanvasSize";
	private static final String CFG_GRAPH_NODE_HIGHLIGHT_CONDITIONS = "GraphNodeHighlightConditions";
	private static final String CFG_GRAPH_EDGE_HIGHLIGHT_CONDITIONS = "GraphEdgeHighlightConditions";
	private static final String CFG_COLLAPSED_NODES = "CollapsedNodes";

	private boolean skipEdgelessNodes;
	private boolean joinEdges;
	private boolean exportAsSvg;
	private Map<String, Double> caseWeights;
	private Map<String, Boolean> crossContaminations;
	private Map<String, Boolean> filter;
	private Map<String, Boolean> edgeFilter;
	private boolean enforeTemporalOrder;

	private boolean graphShowLegend;
	private double graphScaleX;
	private double graphScaleY;
	private double graphTranslationX;
	private double graphTranslationY;
	private Map<String, Point2D> graphNodePositions;
	private int graphNodeSize;
	private int graphTextSize;
	private Mode graphEditingMode;
	private Dimension graphCanvasSize;
	private List<String> graphSelectedNodes;
	private List<String> graphSelectedEdges;
	private HighlightConditionList graphNodeHighlightConditions;
	private HighlightConditionList graphEdgeHighlightConditions;
	private Map<String, Map<String, Point2D>> collapsedNodes;

	public TracingViewSettings() {
		skipEdgelessNodes = DEFAULT_SKIP_EDGELESS_NODES;
		joinEdges = DEFAULT_JOIN_EDGES;
		exportAsSvg = DEFAULT_EXPORT_AS_SVG;
		caseWeights = new LinkedHashMap<String, Double>();
		crossContaminations = new LinkedHashMap<String, Boolean>();
		filter = new LinkedHashMap<String, Boolean>();
		edgeFilter = new LinkedHashMap<String, Boolean>();
		enforeTemporalOrder = DEFAULT_ENFORCE_TEMPORAL_ORDER;

		graphShowLegend = DEFAULT_GRAPH_SHOW_LEGEND;
		graphScaleX = Double.NaN;
		graphScaleY = Double.NaN;
		graphTranslationX = Double.NaN;
		graphTranslationY = Double.NaN;
		graphNodePositions = new LinkedHashMap<String, Point2D>();
		graphNodeSize = DEFAULT_GRAPH_NODE_SIZE;
		graphTextSize = DEFAULT_GRAPH_TEXT_SIZE;
		graphEditingMode = DEFAULT_GRAPH_EDITING_MODE;
		graphCanvasSize = DEFAULT_GRAPH_CANVAS_SIZE;
		graphSelectedNodes = new ArrayList<String>();
		graphSelectedEdges = new ArrayList<String>();
		graphNodeHighlightConditions = new HighlightConditionList();
		graphEdgeHighlightConditions = new HighlightConditionList();
		collapsedNodes = new LinkedHashMap<String, Map<String, Point2D>>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			skipEdgelessNodes = settings.getBoolean(CFG_SKIP_EDGELESS_NODES);
		} catch (InvalidSettingsException e) {
		}

		try {
			joinEdges = settings.getBoolean(CFG_JOIN_EDGES);
		} catch (InvalidSettingsException e) {
		}

		try {
			exportAsSvg = settings.getBoolean(CFG_EXPORT_AS_SVG);
		} catch (InvalidSettingsException e) {
		}

		try {
			caseWeights = (Map<String, Double>) SERIALIZER.fromXml(settings
					.getString(CFG_CASE_WEIGHTS));
		} catch (InvalidSettingsException e) {
		}

		try {
			crossContaminations = (Map<String, Boolean>) SERIALIZER
					.fromXml(settings.getString(CFG_CROSS_CONTAMINATIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			filter = (Map<String, Boolean>) SERIALIZER.fromXml(settings
					.getString(CFG_FILTER));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeFilter = (Map<String, Boolean>) SERIALIZER.fromXml(settings
					.getString(CFG_EDGE_FILTER));
		} catch (InvalidSettingsException e) {
		}

		try {
			enforeTemporalOrder = settings
					.getBoolean(CFG_ENFORCE_TEMPORAL_ORDER);
		} catch (InvalidSettingsException e) {
		}

		try {
			graphShowLegend = settings.getBoolean(CFG_GRAPH_SHOW_LEGEND);
		} catch (InvalidSettingsException e) {
		}

		try {
			graphScaleX = settings.getDouble(CFG_GRAPH_SCALE_X);
		} catch (InvalidSettingsException e) {
		}

		try {
			graphScaleY = settings.getDouble(CFG_GRAPH_SCALE_Y);
		} catch (InvalidSettingsException e) {
		}

		try {
			graphTranslationX = settings.getDouble(CFG_GRAPH_TRANSLATION_X);
		} catch (InvalidSettingsException e) {
		}

		try {
			graphTranslationY = settings.getDouble(CFG_GRAPH_TRANSLATION_Y);
		} catch (InvalidSettingsException e) {
		}

		try {
			graphNodePositions = (Map<String, Point2D>) SERIALIZER
					.fromXml(settings.getString(CFG_GRAPH_NODE_POSITIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			graphNodeSize = settings.getInt(CFG_GRAPH_NODE_SIZE);
		} catch (InvalidSettingsException e) {
		}

		try {
			graphTextSize = settings.getInt(CFG_GRAPH_TEXT_SIZE);
		} catch (InvalidSettingsException e) {
		}

		try {
			graphEditingMode = Mode.valueOf(settings
					.getString(CFG_GRAPH_EDITING_MODE));
		} catch (InvalidSettingsException e) {
		}

		try {
			graphCanvasSize = (Dimension) SERIALIZER.fromXml(settings
					.getString(CFG_GRAPH_CANVAS_SIZE));
		} catch (InvalidSettingsException e) {
		}

		try {
			graphSelectedNodes = (List<String>) SERIALIZER.fromXml(settings
					.getString(CFG_GRAPH_SELECTED_NODES));
		} catch (InvalidSettingsException e) {
		}

		try {
			graphSelectedEdges = (List<String>) SERIALIZER.fromXml(settings
					.getString(CFG_GRAPH_SELECTED_EDGES));
		} catch (InvalidSettingsException e) {
		}

		try {
			graphNodeHighlightConditions = (HighlightConditionList) SERIALIZER
					.fromXml(settings
							.getString(CFG_GRAPH_NODE_HIGHLIGHT_CONDITIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			graphEdgeHighlightConditions = (HighlightConditionList) SERIALIZER
					.fromXml(settings
							.getString(CFG_GRAPH_EDGE_HIGHLIGHT_CONDITIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			collapsedNodes = (Map<String, Map<String, Point2D>>) SERIALIZER
					.fromXml(settings.getString(CFG_COLLAPSED_NODES));
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addBoolean(CFG_SKIP_EDGELESS_NODES, skipEdgelessNodes);
		settings.addBoolean(CFG_JOIN_EDGES, joinEdges);
		settings.addBoolean(CFG_EXPORT_AS_SVG, exportAsSvg);
		settings.addString(CFG_CASE_WEIGHTS, SERIALIZER.toXml(caseWeights));
		settings.addString(CFG_CROSS_CONTAMINATIONS,
				SERIALIZER.toXml(crossContaminations));
		settings.addString(CFG_FILTER, SERIALIZER.toXml(filter));
		settings.addString(CFG_EDGE_FILTER, SERIALIZER.toXml(edgeFilter));
		settings.addBoolean(CFG_ENFORCE_TEMPORAL_ORDER, enforeTemporalOrder);

		settings.addBoolean(CFG_GRAPH_SHOW_LEGEND, graphShowLegend);
		settings.addDouble(CFG_GRAPH_SCALE_X, graphScaleX);
		settings.addDouble(CFG_GRAPH_SCALE_Y, graphScaleY);
		settings.addDouble(CFG_GRAPH_TRANSLATION_X, graphTranslationX);
		settings.addDouble(CFG_GRAPH_TRANSLATION_Y, graphTranslationY);
		settings.addString(CFG_GRAPH_NODE_POSITIONS,
				SERIALIZER.toXml(graphNodePositions));
		settings.addInt(CFG_GRAPH_NODE_SIZE, graphNodeSize);
		settings.addInt(CFG_GRAPH_TEXT_SIZE, graphTextSize);
		settings.addString(CFG_GRAPH_EDITING_MODE, graphEditingMode.name());
		settings.addString(CFG_GRAPH_CANVAS_SIZE,
				SERIALIZER.toXml(graphCanvasSize));
		settings.addString(CFG_GRAPH_SELECTED_NODES,
				SERIALIZER.toXml(graphSelectedNodes));
		settings.addString(CFG_GRAPH_SELECTED_EDGES,
				SERIALIZER.toXml(graphSelectedEdges));
		settings.addString(CFG_GRAPH_NODE_HIGHLIGHT_CONDITIONS,
				SERIALIZER.toXml(graphNodeHighlightConditions));
		settings.addString(CFG_GRAPH_EDGE_HIGHLIGHT_CONDITIONS,
				SERIALIZER.toXml(graphEdgeHighlightConditions));
		settings.addString(CFG_COLLAPSED_NODES,
				SERIALIZER.toXml(collapsedNodes));
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

	public boolean isExportAsSvg() {
		return exportAsSvg;
	}

	public void setExportAsSvg(boolean exportAsSvg) {
		this.exportAsSvg = exportAsSvg;
	}

	public Map<String, Double> getCaseWeights() {
		return caseWeights;
	}

	public void setCaseWeights(Map<String, Double> caseWeights) {
		this.caseWeights = caseWeights;
	}

	public Map<String, Boolean> getCrossContaminations() {
		return crossContaminations;
	}

	public void setCrossContaminations(Map<String, Boolean> crossContaminations) {
		this.crossContaminations = crossContaminations;
	}

	public Map<String, Boolean> getFilter() {
		return filter;
	}

	public void setFilter(Map<String, Boolean> filter) {
		this.filter = filter;
	}

	public Map<String, Boolean> getEdgeFilter() {
		return edgeFilter;
	}

	public void setEdgeFilter(Map<String, Boolean> edgeFilter) {
		this.edgeFilter = edgeFilter;
	}

	public boolean isEnforeTemporalOrder() {
		return enforeTemporalOrder;
	}

	public void setEnforeTemporalOrder(boolean enforeTemporalOrder) {
		this.enforeTemporalOrder = enforeTemporalOrder;
	}

	public boolean isGraphShowLegend() {
		return graphShowLegend;
	}

	public void setGraphShowLegend(boolean graphShowLegend) {
		this.graphShowLegend = graphShowLegend;
	}

	public double getGraphScaleX() {
		return graphScaleX;
	}

	public void setGraphScaleX(double graphScaleX) {
		this.graphScaleX = graphScaleX;
	}

	public double getGraphScaleY() {
		return graphScaleY;
	}

	public void setGraphScaleY(double graphScaleY) {
		this.graphScaleY = graphScaleY;
	}

	public double getGraphTranslationX() {
		return graphTranslationX;
	}

	public void setGraphTranslationX(double graphTranslationX) {
		this.graphTranslationX = graphTranslationX;
	}

	public double getGraphTranslationY() {
		return graphTranslationY;
	}

	public void setGraphTranslationY(double graphTranslationY) {
		this.graphTranslationY = graphTranslationY;
	}

	public Map<String, Point2D> getGraphNodePositions() {
		return graphNodePositions;
	}

	public void setGraphNodePositions(Map<String, Point2D> graphNodePositions) {
		this.graphNodePositions = graphNodePositions;
	}

	public int getGraphNodeSize() {
		return graphNodeSize;
	}

	public void setGraphNodeSize(int graphNodeSize) {
		this.graphNodeSize = graphNodeSize;
	}

	public int getGraphTextSize() {
		return graphTextSize;
	}

	public void setGraphTextSize(int graphTextSize) {
		this.graphTextSize = graphTextSize;
	}

	public Mode getGraphEditingMode() {
		return graphEditingMode;
	}

	public void setGraphEditingMode(Mode graphEditingMode) {
		this.graphEditingMode = graphEditingMode;
	}

	public Dimension getGraphCanvasSize() {
		return graphCanvasSize;
	}

	public void setGraphCanvasSize(Dimension graphCanvasSize) {
		this.graphCanvasSize = graphCanvasSize;
	}

	public List<String> getGraphSelectedNodes() {
		return graphSelectedNodes;
	}

	public void setGraphSelectedNodes(List<String> graphSelectedNodes) {
		this.graphSelectedNodes = graphSelectedNodes;
	}

	public List<String> getGraphSelectedEdges() {
		return graphSelectedEdges;
	}

	public void setGraphSelectedEdges(List<String> graphSelectedEdges) {
		this.graphSelectedEdges = graphSelectedEdges;
	}

	public HighlightConditionList getGraphNodeHighlightConditions() {
		return graphNodeHighlightConditions;
	}

	public void setGraphNodeHighlightConditions(
			HighlightConditionList graphNodeHighlightConditions) {
		this.graphNodeHighlightConditions = graphNodeHighlightConditions;
	}

	public HighlightConditionList getGraphEdgeHighlightConditions() {
		return graphEdgeHighlightConditions;
	}

	public void setGraphEdgeHighlightConditions(
			HighlightConditionList graphEdgeHighlightConditions) {
		this.graphEdgeHighlightConditions = graphEdgeHighlightConditions;
	}

	public Map<String, Map<String, Point2D>> getCollapsedNodes() {
		return collapsedNodes;
	}

	public void setCollapsedNodes(
			Map<String, Map<String, Point2D>> collapsedNodes) {
		this.collapsedNodes = collapsedNodes;
	}

}
