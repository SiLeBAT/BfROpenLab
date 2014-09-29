/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.gis.views.canvas.Canvas;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

public class GraphSettings extends Settings {

	private static final String CFG_NODE_ID_COLUMN = "NodeIdColumn";
	private static final String CFG_EDGE_FROM_COLUMN = "EdgeFromColumn";
	private static final String CFG_EDGE_TO_COLUMN = "EdgeToColumn";
	private static final String CFG_SKIP_EDGELESS_NODES = "SkipEdgelessNodes";
	private static final String CFG_JOIN_EDGES = "JoinEdges";
	private static final String CFG_ARROW_IN_MIDDLE = "ArrowInMiddle";
	private static final String CFG_SHOW_LEGEND = "GraphShowLegend";
	private static final String CFG_SCALE_X = "GraphScaleX";
	private static final String CFG_SCALE_Y = "GraphScaleY";
	private static final String CFG_TRANSLATION_X = "GraphTranslationX";
	private static final String CFG_TRANSLATION_Y = "GraphTranslationY";
	private static final String CFG_NODE_POSITIONS = "GraphNodePositions";
	private static final String CFG_NODE_SIZE = "GraphNodeSize";
	private static final String CFG_FONT_SIZE = "GraphTextSize";
	private static final String CFG_FONT_BOLD = "GraphTextBold";
	private static final String CFG_SELECTED_NODES = "GraphSelectedNodes";
	private static final String CFG_SELECTED_EDGES = "GraphSelectedEdges";
	private static final String CFG_EDITING_MODE = "GraphEditingMode2";
	private static final String CFG_CANVAS_SIZE = "GraphCanvasSize";
	private static final String CFG_NODE_HIGHLIGHT_CONDITIONS = "GraphNodeHighlightConditions";
	private static final String CFG_EDGE_HIGHLIGHT_CONDITIONS = "GraphEdgeHighlightConditions";
	private static final String CFG_COLLAPSED_NODES = "CollapsedNodes";

	private static final boolean DEFAULT_SKIP_EDGELESS_NODES = true;
	private static final boolean DEFAULT_JOIN_EDGES = true;
	private static final boolean DEFAULT_ARROW_IN_MIDDLE = false;
	private static final boolean DEFAULT_SHOW_LEGEND = false;
	private static final int DEFAULT_NODE_SIZE = 10;
	private static final int DEFAULT_FONT_SIZE = 12;
	private static final boolean DEFAULT_FONT_BOLD = false;
	private static final Mode DEFAULT_EDITING_MODE = Mode.PICKING;
	private static final Dimension DEFAULT_CANVAS_SIZE = new Dimension(400, 600);

	private String nodeIdColumn;
	private String edgeFromColumn;
	private String edgeToColumn;
	private boolean skipEdgelessNodes;
	private boolean joinEdges;
	private boolean arrowInMiddle;
	private boolean showLegend;
	private double scaleX;
	private double scaleY;
	private double translationX;
	private double translationY;
	private Map<String, Point2D> nodePositions;
	private int nodeSize;
	private int fontSize;
	private boolean fontBold;
	private Mode editingMode;
	private Dimension canvasSize;
	private List<String> selectedNodes;
	private List<String> selectedEdges;
	private HighlightConditionList nodeHighlightConditions;
	private HighlightConditionList edgeHighlightConditions;
	private Map<String, Map<String, Point2D>> collapsedNodes;

	public GraphSettings() {
		nodeIdColumn = null;
		edgeFromColumn = null;
		edgeToColumn = null;
		skipEdgelessNodes = DEFAULT_SKIP_EDGELESS_NODES;
		joinEdges = DEFAULT_JOIN_EDGES;
		arrowInMiddle = DEFAULT_ARROW_IN_MIDDLE;
		showLegend = DEFAULT_SHOW_LEGEND;
		scaleX = Double.NaN;
		scaleY = Double.NaN;
		translationX = Double.NaN;
		translationY = Double.NaN;
		nodePositions = new LinkedHashMap<>();
		nodeSize = DEFAULT_NODE_SIZE;
		fontSize = DEFAULT_FONT_SIZE;
		fontBold = DEFAULT_FONT_BOLD;
		editingMode = DEFAULT_EDITING_MODE;
		canvasSize = DEFAULT_CANVAS_SIZE;
		selectedNodes = new ArrayList<>();
		selectedEdges = new ArrayList<>();
		nodeHighlightConditions = new HighlightConditionList();
		edgeHighlightConditions = new HighlightConditionList();
		collapsedNodes = new LinkedHashMap<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			nodeIdColumn = settings.getString(CFG_NODE_ID_COLUMN);
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
			scaleX = settings.getDouble(CFG_SCALE_X);
		} catch (InvalidSettingsException e) {
		}

		try {
			scaleY = settings.getDouble(CFG_SCALE_Y);
		} catch (InvalidSettingsException e) {
		}

		try {
			translationX = settings.getDouble(CFG_TRANSLATION_X);
		} catch (InvalidSettingsException e) {
		}

		try {
			translationY = settings.getDouble(CFG_TRANSLATION_Y);
		} catch (InvalidSettingsException e) {
		}

		try {
			nodePositions = (Map<String, Point2D>) SERIALIZER.fromXml(settings
					.getString(CFG_NODE_POSITIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeSize = settings.getInt(CFG_NODE_SIZE);
		} catch (InvalidSettingsException e) {
		}

		try {
			fontSize = settings.getInt(CFG_FONT_SIZE);
		} catch (InvalidSettingsException e) {
		}

		try {
			fontBold = settings.getBoolean(CFG_FONT_BOLD);
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
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addString(CFG_NODE_ID_COLUMN, nodeIdColumn);
		settings.addString(CFG_EDGE_FROM_COLUMN, edgeFromColumn);
		settings.addString(CFG_EDGE_TO_COLUMN, edgeToColumn);
		settings.addBoolean(CFG_SKIP_EDGELESS_NODES, skipEdgelessNodes);
		settings.addBoolean(CFG_JOIN_EDGES, joinEdges);
		settings.addBoolean(CFG_ARROW_IN_MIDDLE, arrowInMiddle);
		settings.addBoolean(CFG_SHOW_LEGEND, showLegend);
		settings.addDouble(CFG_SCALE_X, scaleX);
		settings.addDouble(CFG_SCALE_Y, scaleY);
		settings.addDouble(CFG_TRANSLATION_X, translationX);
		settings.addDouble(CFG_TRANSLATION_Y, translationY);
		settings.addString(CFG_NODE_POSITIONS, SERIALIZER.toXml(nodePositions));
		settings.addInt(CFG_NODE_SIZE, nodeSize);
		settings.addInt(CFG_FONT_SIZE, fontSize);
		settings.addBoolean(CFG_FONT_BOLD, fontBold);
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
	}

	public void setFromCanvas(Canvas<?> canvas, boolean resized) {
		selectedNodes = new ArrayList<>(canvas.getSelectedNodeIds());
		selectedEdges = new ArrayList<>(canvas.getSelectedEdgeIds());

		Collections.sort(selectedNodes);
		Collections.sort(selectedEdges);

		showLegend = canvas.isShowLegend();
		scaleX = canvas.getScaleX();
		scaleY = canvas.getScaleY();
		translationX = canvas.getTranslationX();
		translationY = canvas.getTranslationY();
		nodeSize = canvas.getNodeSize();
		fontSize = canvas.getFontSize();
		fontBold = canvas.isFontBold();
		joinEdges = canvas.isJoinEdges();
		arrowInMiddle = canvas.isArrowInMiddle();
		skipEdgelessNodes = canvas.isSkipEdgelessNodes();

		nodeHighlightConditions = canvas.getNodeHighlightConditions();
		edgeHighlightConditions = canvas.getEdgeHighlightConditions();
		editingMode = canvas.getEditingMode();

		if (resized) {
			canvasSize = canvas.getCanvasSize();
		}

		if (canvas instanceof GraphCanvas) {
			nodePositions = ((GraphCanvas) canvas).getNodePositions();
			collapsedNodes = ((GraphCanvas) canvas).getCollapsedNodes();
		}
	}

	public void setToCanvas(Canvas<?> canvas) {
		canvas.setShowLegend(showLegend);
		canvas.setCanvasSize(canvasSize);
		canvas.setEditingMode(editingMode);
		canvas.setNodeSize(nodeSize);
		canvas.setFontSize(fontSize);
		canvas.setFontBold(fontBold);
		canvas.setJoinEdges(joinEdges);
		canvas.setArrowInMiddle(arrowInMiddle);

		if (canvas instanceof GraphCanvas) {
			((GraphCanvas) canvas).setCollapsedNodes(collapsedNodes);
		}

		canvas.setNodeHighlightConditions(nodeHighlightConditions);
		canvas.setEdgeHighlightConditions(edgeHighlightConditions);
		canvas.setSkipEdgelessNodes(skipEdgelessNodes);
		canvas.setSelectedNodeIds(new LinkedHashSet<>(selectedNodes));
		canvas.setSelectedEdgeIds(new LinkedHashSet<>(selectedEdges));

		if (!Double.isNaN(scaleX) && !Double.isNaN(scaleY)
				&& !Double.isNaN(translationX) && !Double.isNaN(translationY)) {
			canvas.setTransform(scaleX, scaleY, translationX, translationY);
		}

		if (canvas instanceof GraphCanvas) {
			((GraphCanvas) canvas).setNodePositions(nodePositions);
		}
	}

	public String getNodeIdColumn() {
		return nodeIdColumn;
	}

	public void setNodeIdColumn(String nodeIdColumn) {
		this.nodeIdColumn = nodeIdColumn;
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

	public double getScaleX() {
		return scaleX;
	}

	public void setScaleX(double scaleX) {
		this.scaleX = scaleX;
	}

	public double getScaleY() {
		return scaleY;
	}

	public void setScaleY(double scaleY) {
		this.scaleY = scaleY;
	}

	public double getTranslationX() {
		return translationX;
	}

	public void setTranslationX(double translationX) {
		this.translationX = translationX;
	}

	public double getTranslationY() {
		return translationY;
	}

	public void setTranslationY(double translationY) {
		this.translationY = translationY;
	}

	public Map<String, Point2D> getNodePositions() {
		return nodePositions;
	}

	public void setNodePositions(Map<String, Point2D> nodePositions) {
		this.nodePositions = nodePositions;
	}

	public int getNodeSize() {
		return nodeSize;
	}

	public void setNodeSize(int nodeSize) {
		this.nodeSize = nodeSize;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public boolean isFontBold() {
		return fontBold;
	}

	public void setFontBold(boolean fontBold) {
		this.fontBold = fontBold;
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
}
