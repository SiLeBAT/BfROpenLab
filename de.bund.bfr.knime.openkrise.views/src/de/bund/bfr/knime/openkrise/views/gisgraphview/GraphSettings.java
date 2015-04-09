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
package de.bund.bfr.knime.openkrise.views.gisgraphview;

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
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.ICanvas;
import de.bund.bfr.knime.gis.views.canvas.Transform;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.openkrise.TracingUtils;
import de.bund.bfr.knime.openkrise.views.Activator;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

public class GraphSettings extends NodeSettings {

	protected static final XmlConverter SERIALIZER = new XmlConverter(
			Activator.class.getClassLoader());

	private static final String CFG_SKIP_EDGELESS_NODES = "SkipEdgelessNodes";
	private static final String CFG_SHOW_EDGES_IN_META_NODE = "ShowEdgesInMetaNode";
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
	private static final String CFG_LABEL = "Label";

	private boolean skipEdgelessNodes;
	private boolean showEdgesInMetaNode;
	private boolean joinEdges;
	private boolean arrowInMiddle;
	private boolean showLegend;
	private Transform transform;
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
	private String label;

	public GraphSettings() {
		skipEdgelessNodes = true;
		showEdgesInMetaNode = false;
		joinEdges = true;
		arrowInMiddle = false;
		showLegend = false;
		transform = Transform.INVALID_TRANSFORM;
		nodePositions = new LinkedHashMap<>();
		nodeSize = 10;
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
			skipEdgelessNodes = settings.getBoolean(CFG_SKIP_EDGELESS_NODES);
		} catch (InvalidSettingsException e) {
		}

		try {
			showEdgesInMetaNode = settings
					.getBoolean(CFG_SHOW_EDGES_IN_META_NODE);
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
			transform = new Transform(settings.getDouble(CFG_SCALE_X),
					settings.getDouble(CFG_SCALE_Y),
					settings.getDouble(CFG_TRANSLATION_X),
					settings.getDouble(CFG_TRANSLATION_Y));
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

		try {
			label = settings.getString(CFG_LABEL);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addBoolean(CFG_SKIP_EDGELESS_NODES, skipEdgelessNodes);
		settings.addBoolean(CFG_SHOW_EDGES_IN_META_NODE, showEdgesInMetaNode);
		settings.addBoolean(CFG_JOIN_EDGES, joinEdges);
		settings.addBoolean(CFG_ARROW_IN_MIDDLE, arrowInMiddle);
		settings.addBoolean(CFG_SHOW_LEGEND, showLegend);
		settings.addDouble(CFG_SCALE_X, transform.getScaleX());
		settings.addDouble(CFG_SCALE_Y, transform.getScaleY());
		settings.addDouble(CFG_TRANSLATION_X, transform.getTranslationX());
		settings.addDouble(CFG_TRANSLATION_Y, transform.getTranslationY());
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
		settings.addString(CFG_LABEL, label);
	}

	public void setFromCanvas(ICanvas<?> canvas, boolean resized) {
		selectedNodes = new ArrayList<>(canvas.getSelectedNodeIds());
		selectedEdges = new ArrayList<>(canvas.getSelectedEdgeIds());

		Collections.sort(selectedNodes);
		Collections.sort(selectedEdges);

		showLegend = canvas.isShowLegend();
		transform = canvas.getTransform();
		nodeSize = canvas.getNodeSize();
		fontSize = canvas.getFontSize();
		fontBold = canvas.isFontBold();
		joinEdges = canvas.isJoinEdges();
		arrowInMiddle = canvas.isArrowInMiddle();
		skipEdgelessNodes = canvas.isSkipEdgelessNodes();
		showEdgesInMetaNode = canvas.isShowEdgesInMetaNode();
		label = canvas.getLabel();

		nodeHighlightConditions = canvas.getNodeHighlightConditions();
		edgeHighlightConditions = canvas.getEdgeHighlightConditions();
		editingMode = canvas.getEditingMode();

		if (resized) {
			canvasSize = canvas.getCanvasSize();
		}

		collapsedNodes = new LinkedHashMap<>();

		for (Map.Entry<String, Set<String>> entry : canvas.getCollapsedNodes()
				.entrySet()) {
			Map<String, Point2D> ids = new LinkedHashMap<>();

			for (String id : entry.getValue()) {
				ids.put(id, null);
			}

			collapsedNodes.put(entry.getKey(), ids);
		}

		if (canvas instanceof GraphCanvas) {
			nodePositions = ((GraphCanvas) canvas).getNodePositions();
		}
	}

	public void setToCanvas(ICanvas<?> canvas, boolean applyNodePosition) {
		canvas.setShowLegend(showLegend);
		canvas.setEditingMode(editingMode);
		canvas.setNodeSize(nodeSize);
		canvas.setFontSize(fontSize);
		canvas.setFontBold(fontBold);
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
		canvas.setShowEdgesInMetaNode(showEdgesInMetaNode);
		canvas.setSelectedNodeIds(new LinkedHashSet<>(selectedNodes));
		canvas.setSelectedEdgeIds(new LinkedHashSet<>(selectedEdges));

		if (canvasSize != null) {
			canvas.setCanvasSize(canvasSize);
		} else {
			canvas.setCanvasSize(new Dimension(400, 600));
		}

		if (transform.isValid()) {
			canvas.setTransform(transform);
		}

		if (applyNodePosition && canvas instanceof GraphCanvas) {
			((GraphCanvas) canvas).setNodePositions(nodePositions);
		}
	}

	public boolean isSkipEdgelessNodes() {
		return skipEdgelessNodes;
	}

	public void setSkipEdgelessNodes(boolean skipEdgelessNodes) {
		this.skipEdgelessNodes = skipEdgelessNodes;
	}

	public boolean isShowEdgesInMetaNode() {
		return showEdgesInMetaNode;
	}

	public void setShowEdgesInMetaNode(boolean showEdgesInMetaNode) {
		this.showEdgesInMetaNode = showEdgesInMetaNode;
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

	public Transform getTransform() {
		return transform;
	}

	public void setTransform(Transform transform) {
		this.transform = transform;
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
