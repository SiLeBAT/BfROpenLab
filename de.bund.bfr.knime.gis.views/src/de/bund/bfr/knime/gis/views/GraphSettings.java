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
package de.bund.bfr.knime.gis.views;

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

import de.bund.bfr.jung.LabelPosition;
import de.bund.bfr.knime.gis.BackwardUtils;
import de.bund.bfr.knime.gis.views.canvas.Canvas;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.util.ArrowHeadType;
import de.bund.bfr.knime.gis.views.canvas.util.Transform;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

public class GraphSettings extends Settings {

	private static final String CFG_NODE_ID_COLUMN = "NodeIdColumn";
	private static final String CFG_EDGE_FROM_COLUMN = "EdgeFromColumn";
	private static final String CFG_EDGE_TO_COLUMN = "EdgeToColumn";
	private static final String CFG_SKIP_EDGELESS_NODES = "SkipEdgelessNodes";
	private static final String CFG_SHOW_EDGES_IN_META_NODE = "ShowEdgesInMetaNode";
	private static final String CFG_JOIN_EDGES = "JoinEdges";
	private static final String CFG_HIDE_ARROW_HEAD = "HideArrowHead";
	private static final String CFG_ARROW_HEAD_IN_MIDDLE = "ArrowInMiddle";
	private static final String CFG_NODE_LABEL_POSITION = "NodeLabelPosition";
	private static final String CFG_SHOW_LEGEND = "GraphShowLegend";
	private static final String CFG_SCALE_X = "GraphScaleX";
	private static final String CFG_SCALE_Y = "GraphScaleY";
	private static final String CFG_TRANSLATION_X = "GraphTranslationX";
	private static final String CFG_TRANSLATION_Y = "GraphTranslationY";
	private static final String CFG_NODE_POSITIONS = "GraphNodePositions";
	private static final String CFG_NODE_SIZE = "GraphNodeSize";
	private static final String CFG_NODE_MAX_SIZE = "GraphNodeMaxSize";
	private static final String CFG_EDGE_THICKNESS = "GraphEdgeThickness";
	private static final String CFG_EDGE_MAX_THICKNESS = "GraphEdgeMaxThickness";
	private static final String CFG_FONT_SIZE = "GraphTextSize";
	private static final String CFG_FONT_BOLD = "GraphTextBold";
	private static final String CFG_SELECTED_NODES = "GraphSelectedNodes";
	private static final String CFG_SELECTED_EDGES = "GraphSelectedEdges";
	private static final String CFG_EDITING_MODE = "GraphEditingMode2";
	private static final String CFG_CANVAS_SIZE = "GraphCanvasSize";
	private static final String CFG_NODE_HIGHLIGHT_CONDITIONS = "GraphNodeHighlightConditions";
	private static final String CFG_EDGE_HIGHLIGHT_CONDITIONS = "GraphEdgeHighlightConditions";
	private static final String CFG_COLLAPSED_NODES = "CollapsedNodes";
	private static final String CFG_LABEL = "GraphLabel";

	private String nodeIdColumn;
	private String edgeFromColumn;
	private String edgeToColumn;
	private boolean skipEdgelessNodes;
	private boolean showEdgesInMetaNode;
	private boolean joinEdges;
	private boolean hideArrowHead;
	private boolean arrowHeadInMiddle;
	private LabelPosition nodeLabelPosition;
	private boolean showLegend;
	private Transform transform;
	private Map<String, Point2D> nodePositions;
	private int nodeSize;
	private Integer nodeMaxSize;
	private int edgeThickness;
	private Integer edgeMaxThickness;
	private int fontSize;
	private boolean fontBold;
	private Mode editingMode;
	private Dimension canvasSize;
	private List<String> selectedNodes;
	private List<String> selectedEdges;
	private HighlightConditionList nodeHighlightConditions;
	private HighlightConditionList edgeHighlightConditions;
	private Map<String, Map<String, Point2D>> collapsedNodes;
	private String label;

	public GraphSettings() {
		nodeIdColumn = null;
		edgeFromColumn = null;
		edgeToColumn = null;
		skipEdgelessNodes = false;
		showEdgesInMetaNode = false;
		joinEdges = false;
		hideArrowHead = false;
		arrowHeadInMiddle = false;
		nodeLabelPosition = LabelPosition.BOTTOM_RIGHT;
		showLegend = false;
		transform = Transform.INVALID_TRANSFORM;
		nodePositions = null;
		nodeSize = 10;
		nodeMaxSize = null;
		edgeThickness = 1;
		edgeMaxThickness = null;
		fontSize = 12;
		fontBold = false;
		editingMode = Mode.PICKING;
		canvasSize = null;
		selectedNodes = new ArrayList<>();
		selectedEdges = new ArrayList<>();
		nodeHighlightConditions = new HighlightConditionList();
		edgeHighlightConditions = new HighlightConditionList();
		collapsedNodes = new LinkedHashMap<>();
		label = null;
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
			showEdgesInMetaNode = settings.getBoolean(CFG_SHOW_EDGES_IN_META_NODE);
		} catch (InvalidSettingsException e) {
		}

		try {
			joinEdges = settings.getBoolean(CFG_JOIN_EDGES);
		} catch (InvalidSettingsException e) {
		}

		try {
			hideArrowHead = settings.getBoolean(CFG_HIDE_ARROW_HEAD);
		} catch (InvalidSettingsException e) {
		}

		try {
			arrowHeadInMiddle = settings.getBoolean(CFG_ARROW_HEAD_IN_MIDDLE);
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeLabelPosition = LabelPosition.valueOf(settings.getString(CFG_NODE_LABEL_POSITION));
		} catch (InvalidSettingsException e) {
		}

		try {
			showLegend = settings.getBoolean(CFG_SHOW_LEGEND);
		} catch (InvalidSettingsException e) {
		}

		try {
			transform = new Transform(settings.getDouble(CFG_SCALE_X), settings.getDouble(CFG_SCALE_Y),
					settings.getDouble(CFG_TRANSLATION_X), settings.getDouble(CFG_TRANSLATION_Y));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodePositions = (Map<String, Point2D>) SERIALIZER.fromXml(settings.getString(CFG_NODE_POSITIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeSize = settings.getInt(CFG_NODE_SIZE);
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeMaxSize = minusOneToNull(settings.getInt(CFG_NODE_MAX_SIZE));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeThickness = settings.getInt(CFG_EDGE_THICKNESS);
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeMaxThickness = minusOneToNull(settings.getInt(CFG_EDGE_MAX_THICKNESS));
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
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addString(CFG_NODE_ID_COLUMN, nodeIdColumn);
		settings.addString(CFG_EDGE_FROM_COLUMN, edgeFromColumn);
		settings.addString(CFG_EDGE_TO_COLUMN, edgeToColumn);
		settings.addBoolean(CFG_SKIP_EDGELESS_NODES, skipEdgelessNodes);
		settings.addBoolean(CFG_SHOW_EDGES_IN_META_NODE, showEdgesInMetaNode);
		settings.addBoolean(CFG_JOIN_EDGES, joinEdges);
		settings.addBoolean(CFG_HIDE_ARROW_HEAD, hideArrowHead);
		settings.addBoolean(CFG_ARROW_HEAD_IN_MIDDLE, arrowHeadInMiddle);
		settings.addString(CFG_NODE_LABEL_POSITION, nodeLabelPosition.name());
		settings.addBoolean(CFG_SHOW_LEGEND, showLegend);
		settings.addDouble(CFG_SCALE_X, transform.getScaleX());
		settings.addDouble(CFG_SCALE_Y, transform.getScaleY());
		settings.addDouble(CFG_TRANSLATION_X, transform.getTranslationX());
		settings.addDouble(CFG_TRANSLATION_Y, transform.getTranslationY());
		settings.addString(CFG_NODE_POSITIONS, SERIALIZER.toXml(nodePositions));
		settings.addInt(CFG_NODE_SIZE, nodeSize);
		settings.addInt(CFG_NODE_MAX_SIZE, nullToMinusOne(nodeMaxSize));
		settings.addInt(CFG_EDGE_THICKNESS, edgeThickness);
		settings.addInt(CFG_EDGE_MAX_THICKNESS, nullToMinusOne(edgeMaxThickness));
		settings.addInt(CFG_FONT_SIZE, fontSize);
		settings.addBoolean(CFG_FONT_BOLD, fontBold);
		settings.addString(CFG_EDITING_MODE, editingMode.name());
		settings.addString(CFG_CANVAS_SIZE, SERIALIZER.toXml(canvasSize));
		settings.addString(CFG_SELECTED_NODES, SERIALIZER.toXml(selectedNodes));
		settings.addString(CFG_SELECTED_EDGES, SERIALIZER.toXml(selectedEdges));
		settings.addString(CFG_NODE_HIGHLIGHT_CONDITIONS, SERIALIZER.toXml(nodeHighlightConditions));
		settings.addString(CFG_EDGE_HIGHLIGHT_CONDITIONS, SERIALIZER.toXml(edgeHighlightConditions));
		settings.addString(CFG_COLLAPSED_NODES, SERIALIZER.toXml(collapsedNodes));
		settings.addString(CFG_LABEL, label);
	}

	public void setFromCanvas(Canvas<?> canvas, boolean resized) {
		showLegend = canvas.getOptionsPanel().isShowLegend();
		transform = canvas.getTransform();
		nodeSize = canvas.getOptionsPanel().getNodeSize();
		nodeMaxSize = canvas.getOptionsPanel().getNodeMaxSize();
		edgeThickness = canvas.getOptionsPanel().getEdgeThickness();
		edgeMaxThickness = canvas.getOptionsPanel().getEdgeMaxThickness();
		fontSize = canvas.getOptionsPanel().getFontSize();
		fontBold = canvas.getOptionsPanel().isFontBold();
		joinEdges = canvas.getOptionsPanel().isJoinEdges();
		hideArrowHead = canvas.getOptionsPanel().getArrowHeadType() == ArrowHeadType.HIDE;
		arrowHeadInMiddle = canvas.getOptionsPanel().getArrowHeadType() == ArrowHeadType.IN_MIDDLE;
		nodeLabelPosition = canvas.getOptionsPanel().getNodeLabelPosition();
		skipEdgelessNodes = canvas.getOptionsPanel().isSkipEdgelessNodes();
		showEdgesInMetaNode = canvas.getOptionsPanel().isShowEdgesInMetaNode();
		label = canvas.getOptionsPanel().getLabel();

		selectedNodes = Ordering.natural().sortedCopy(canvas.getSelectedNodeIds());
		selectedEdges = Ordering.natural().sortedCopy(canvas.getSelectedEdgeIds());
		nodeHighlightConditions = canvas.getNodeHighlightConditions();
		edgeHighlightConditions = canvas.getEdgeHighlightConditions();
//		editingMode = canvas.getOptionsPanel().getEditingMode();
		collapsedNodes = BackwardUtils.toOldCollapseFormat(canvas.getCollapsedNodes());

		if (resized || canvasSize == null) {
			canvasSize = canvas.getCanvasSize();
		}

		if (canvas instanceof GraphCanvas) {
			nodePositions = ((GraphCanvas) canvas).getNodePositions();
		}
	}

	public void setToCanvas(Canvas<?> canvas) {
		canvas.getOptionsPanel().setShowLegend(showLegend);
//		canvas.getOptionsPanel().setEditingMode(editingMode);
		canvas.getOptionsPanel().setNodeSize(nodeSize);
		canvas.getOptionsPanel().setNodeMaxSize(nodeMaxSize);
		canvas.getOptionsPanel().setEdgeThickness(edgeThickness);
		canvas.getOptionsPanel().setEdgeMaxThickness(edgeMaxThickness);
		canvas.getOptionsPanel().setFontSize(fontSize);
		canvas.getOptionsPanel().setFontBold(fontBold);
		canvas.getOptionsPanel().setJoinEdges(joinEdges);
		canvas.getOptionsPanel().setArrowHeadType(hideArrowHead ? ArrowHeadType.HIDE
				: (arrowHeadInMiddle ? ArrowHeadType.IN_MIDDLE : ArrowHeadType.AT_TARGET));
		canvas.getOptionsPanel().setNodeLabelPosition(nodeLabelPosition);
		canvas.getOptionsPanel().setLabel(label);
		canvas.getOptionsPanel().setSkipEdgelessNodes(skipEdgelessNodes);
		canvas.getOptionsPanel().setShowEdgesInMetaNode(showEdgesInMetaNode);
		canvas.setCollapsedNodes(BackwardUtils.toNewCollapseFormat(collapsedNodes));

		canvas.setNodeHighlightConditions(nodeHighlightConditions);
		canvas.setEdgeHighlightConditions(edgeHighlightConditions);
		canvas.setSelectedNodeIds(new LinkedHashSet<>(selectedNodes));
		canvas.setSelectedEdgeIds(new LinkedHashSet<>(selectedEdges));

		if (canvasSize != null) {
			canvas.setCanvasSize(canvasSize);
		}

		if (transform.isValid()) {
			canvas.setTransform(transform);
		}

		if (canvas instanceof GraphCanvas) {
			if (nodePositions != null) {
				((GraphCanvas) canvas).setNodePositions(nodePositions);
			} else {
				((GraphCanvas) canvas).initLayout();
			}
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
}
