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
package de.bund.bfr.knime.gis.views;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;

public class SimpleGraphVisualizerSettings extends VisualizerSettings {

	public static final boolean DEFAULT_SKIP_EDGELESS_NODES = true;
	public static final boolean DEFAULT_JOIN_EDGES = true;
	
	public static final boolean DEFAULT_GRAPH_SHOW_LEGEND = false;	
	public static final int DEFAULT_GRAPH_NODE_SIZE = 10;
	public static final String DEFAULT_GRAPH_EDITING_MODE = GraphCanvas.PICKING_MODE;
	public static final Dimension DEFAULT_GRAPH_CANVAS_SIZE = new Dimension(
			400, 600);

	private static final String CFG_NODE_ID_COLUMN = "NodeIdColumn";
	private static final String CFG_SKIP_EDGELESS_NODES = "SkipEdgelessNodes";
	private static final String CFG_EDGE_FROM_COLUMN = "EdgeFromColumn";
	private static final String CFG_EDGE_TO_COLUMN = "EdgeToColumn";
	private static final String CFG_JOIN_EDGES = "JoinEdges";

	private static final String CFG_GRAPH_SHOW_LEGEND = "GraphShowLegend";
	private static final String CFG_GRAPH_SCALE_X = "GraphScaleX";
	private static final String CFG_GRAPH_SCALE_Y = "GraphScaleY";
	private static final String CFG_GRAPH_TRANSLATION_X = "GraphTranslationX";
	private static final String CFG_GRAPH_TRANSLATION_Y = "GraphTranslationY";
	private static final String CFG_GRAPH_NODE_POSITIONS = "GraphNodePositions";	
	private static final String CFG_GRAPH_NODE_SIZE = "GraphNodeSize";
	private static final String CFG_GRAPH_SELECTED_NODES = "GraphSelectedNodes";
	private static final String CFG_GRAPH_SELECTED_EDGES = "GraphSelectedEdges";
	private static final String CFG_GRAPH_EDITING_MODE = "GraphEditingMode";
	private static final String CFG_GRAPH_CANVAS_SIZE = "GraphCanvasSize";
	private static final String CFG_GRAPH_NODE_HIGHLIGHT_CONDITIONS = "GraphNodeHighlightConditions";
	private static final String CFG_GRAPH_EDGE_HIGHLIGHT_CONDITIONS = "GraphEdgeHighlightConditions";

	private String nodeIdColumn;
	private boolean skipEdgelessNodes;
	private String edgeFromColumn;
	private String edgeToColumn;
	private boolean joinEdges;

	private boolean graphShowLegend;
	private double graphScaleX;
	private double graphScaleY;
	private double graphTranslationX;
	private double graphTranslationY;
	private Map<String, Point2D> graphNodePositions;	
	private int graphNodeSize;
	private String graphEditingMode;
	private Dimension graphCanvasSize;
	private List<String> graphSelectedNodes;
	private List<String> graphSelectedEdges;
	private HighlightConditionList graphNodeHighlightConditions;
	private HighlightConditionList graphEdgeHighlightConditions;

	public SimpleGraphVisualizerSettings() {
		nodeIdColumn = null;
		skipEdgelessNodes = DEFAULT_SKIP_EDGELESS_NODES;
		edgeFromColumn = null;
		edgeToColumn = null;
		joinEdges = DEFAULT_JOIN_EDGES;

		graphShowLegend = DEFAULT_GRAPH_SHOW_LEGEND;
		graphScaleX = Double.NaN;
		graphScaleY = Double.NaN;
		graphTranslationX = Double.NaN;
		graphTranslationY = Double.NaN;
		graphNodePositions = new LinkedHashMap<String, Point2D>();		
		graphNodeSize = DEFAULT_GRAPH_NODE_SIZE;
		graphEditingMode = DEFAULT_GRAPH_EDITING_MODE;
		graphCanvasSize = DEFAULT_GRAPH_CANVAS_SIZE;
		graphSelectedNodes = new ArrayList<String>();
		graphSelectedEdges = new ArrayList<String>();
		graphNodeHighlightConditions = new HighlightConditionList();
		graphEdgeHighlightConditions = new HighlightConditionList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		super.loadSettings(settings);

		try {
			nodeIdColumn = settings.getString(CFG_NODE_ID_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			skipEdgelessNodes = settings.getBoolean(CFG_SKIP_EDGELESS_NODES);
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeFromColumn = settings.getString(CFG_EDGE_FROM_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeToColumn = settings.getString(CFG_EDGE_TO_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			joinEdges = settings.getBoolean(CFG_JOIN_EDGES);
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
			graphEditingMode = settings.getString(CFG_GRAPH_EDITING_MODE);
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
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		super.saveSettings(settings);
		settings.addString(CFG_NODE_ID_COLUMN, nodeIdColumn);
		settings.addBoolean(CFG_SKIP_EDGELESS_NODES, skipEdgelessNodes);
		settings.addString(CFG_EDGE_FROM_COLUMN, edgeFromColumn);
		settings.addString(CFG_EDGE_TO_COLUMN, edgeToColumn);
		settings.addBoolean(CFG_JOIN_EDGES, joinEdges);

		settings.addBoolean(CFG_GRAPH_SHOW_LEGEND, graphShowLegend);
		settings.addDouble(CFG_GRAPH_SCALE_X, graphScaleX);
		settings.addDouble(CFG_GRAPH_SCALE_Y, graphScaleY);
		settings.addDouble(CFG_GRAPH_TRANSLATION_X, graphTranslationX);
		settings.addDouble(CFG_GRAPH_TRANSLATION_Y, graphTranslationY);
		settings.addString(CFG_GRAPH_NODE_POSITIONS,
				SERIALIZER.toXml(graphNodePositions));		
		settings.addInt(CFG_GRAPH_NODE_SIZE, graphNodeSize);
		settings.addString(CFG_GRAPH_EDITING_MODE, graphEditingMode);
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
	}

	public String getNodeIdColumn() {
		return nodeIdColumn;
	}

	public void setNodeIdColumn(String nodeIdColumn) {
		this.nodeIdColumn = nodeIdColumn;
	}

	public boolean isSkipEdgelessNodes() {
		return skipEdgelessNodes;
	}

	public void setSkipEdgelessNodes(boolean skipEdgelessNodes) {
		this.skipEdgelessNodes = skipEdgelessNodes;
	}

	public String getEdgeFromColumn() {
		return edgeFromColumn;
	}

	public void setEdgeFromColumn(String edgeFromColumn) {
		this.edgeFromColumn = edgeFromColumn;
	}

	public String getEdgeToColumn() {
		return edgeToColumn;
	}

	public void setEdgeToColumn(String edgeToColumn) {
		this.edgeToColumn = edgeToColumn;
	}

	public boolean isJoinEdges() {
		return joinEdges;
	}

	public void setJoinEdges(boolean joinEdges) {
		this.joinEdges = joinEdges;
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

	public String getGraphEditingMode() {
		return graphEditingMode;
	}

	public void setGraphEditingMode(String graphEditingMode) {
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
}
