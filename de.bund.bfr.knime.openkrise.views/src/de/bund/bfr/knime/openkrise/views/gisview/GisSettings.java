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
package de.bund.bfr.knime.openkrise.views.gisview;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.NodeSettings;
import de.bund.bfr.knime.XmlConverter;
import de.bund.bfr.knime.gis.views.canvas.Canvas;
import de.bund.bfr.knime.gis.views.canvas.Transform;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.openkrise.TracingUtils;
import de.bund.bfr.knime.openkrise.views.Activator;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

public class GisSettings extends NodeSettings {

	protected static final XmlConverter SERIALIZER = new XmlConverter(
			Activator.class.getClassLoader());

	private static final String CFG_SHOW_LEGEND = "ShowLegend";
	private static final String CFG_SCALE_X = "ScaleX";
	private static final String CFG_SCALE_Y = "ScaleY";
	private static final String CFG_TRANSLATION_X = "TranslationX";
	private static final String CFG_TRANSLATION_Y = "TranslationY";
	private static final String CFG_NODE_SIZE = "GisLocationSize";
	private static final String CFG_FONT_SIZE = "TextSize";
	private static final String CFG_FONT_BOLD = "TextBold";
	private static final String CFG_BORDER_ALPHA = "BorderAlpha";
	private static final String CFG_EDITING_MODE = "EditingMode";
	private static final String CFG_CANVAS_SIZE = "CanvasSize";
	private static final String CFG_SELECTED_NODES = "SelectedNodes";
	private static final String CFG_NODE_HIGHLIGHT_CONDITIONS = "NodeHighlightConditions";

	private boolean showLegend;
	private Transform transform;
	private int nodeSize;
	private int fontSize;
	private boolean fontBold;
	private int borderAlpha;
	private Mode editingMode;
	private Dimension canvasSize;
	private List<String> selectedNodes;
	private HighlightConditionList nodeHighlightConditions;

	public GisSettings() {
		showLegend = false;
		transform = Transform.INVALID_TRANSFORM;
		nodeSize = 4;
		fontSize = 12;
		fontBold = false;
		borderAlpha = 255;
		editingMode = Mode.PICKING;
		selectedNodes = new ArrayList<>();
		nodeHighlightConditions = new HighlightConditionList();
		canvasSize = new Dimension(400, 600);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadSettings(NodeSettingsRO settings) {
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
			borderAlpha = settings.getInt(CFG_BORDER_ALPHA);
		} catch (InvalidSettingsException e) {
		}

		try {
			editingMode = Mode.valueOf(settings.getString(CFG_EDITING_MODE));
		} catch (InvalidSettingsException e) {
		}

		try {
			selectedNodes = (List<String>) SERIALIZER.fromXml(settings
					.getString(CFG_SELECTED_NODES));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeHighlightConditions = (HighlightConditionList) SERIALIZER
					.fromXml(settings.getString(CFG_NODE_HIGHLIGHT_CONDITIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			canvasSize = (Dimension) SERIALIZER.fromXml(settings
					.getString(CFG_CANVAS_SIZE));
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addBoolean(CFG_SHOW_LEGEND, showLegend);
		settings.addDouble(CFG_SCALE_X, transform.getScaleX());
		settings.addDouble(CFG_SCALE_Y, transform.getScaleY());
		settings.addDouble(CFG_TRANSLATION_X, transform.getTranslationX());
		settings.addDouble(CFG_TRANSLATION_Y, transform.getTranslationY());
		settings.addInt(CFG_NODE_SIZE, nodeSize);
		settings.addInt(CFG_FONT_SIZE, fontSize);
		settings.addBoolean(CFG_FONT_BOLD, fontBold);
		settings.addInt(CFG_BORDER_ALPHA, borderAlpha);
		settings.addString(CFG_EDITING_MODE, editingMode.name());
		settings.addString(CFG_SELECTED_NODES, SERIALIZER.toXml(selectedNodes));
		settings.addString(CFG_NODE_HIGHLIGHT_CONDITIONS,
				SERIALIZER.toXml(nodeHighlightConditions));
		settings.addString(CFG_CANVAS_SIZE, SERIALIZER.toXml(canvasSize));
	}

	public void setFromCanvas(Canvas<?> canvas, boolean resized) {
		selectedNodes = new ArrayList<>(canvas.getSelectedNodeIds());

		Collections.sort(selectedNodes);

		showLegend = canvas.isShowLegend();
		transform = canvas.getTransform();
		nodeSize = canvas.getNodeSize();
		fontSize = canvas.getFontSize();
		fontBold = canvas.isFontBold();
		borderAlpha = canvas.getBorderAlpha();
		editingMode = canvas.getEditingMode();
		nodeHighlightConditions = canvas.getNodeHighlightConditions();

		if (resized) {
			canvasSize = canvas.getCanvasSize();
		}
	}

	public void setToCanvas(Canvas<?> canvas,
			boolean applySelectionAndHighlighting) {
		canvas.setShowLegend(showLegend);
		canvas.setCanvasSize(canvasSize);
		canvas.setNodeSize(nodeSize);
		canvas.setFontSize(fontSize);
		canvas.setFontBold(fontBold);
		canvas.setBorderAlpha(borderAlpha);
		canvas.setEditingMode(editingMode);

		if (applySelectionAndHighlighting) {
			canvas.setNodeHighlightConditions(TracingUtils.renameColumns(
					nodeHighlightConditions, canvas.getNodeSchema().getMap()
							.keySet()));
			canvas.setSelectedNodeIds(new LinkedHashSet<>(selectedNodes));
		}

		if (transform.isValid()) {
			canvas.setTransform(transform);
		}
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

	public int getBorderAlpha() {
		return borderAlpha;
	}

	public void setBorderAlpha(int borderAlpha) {
		this.borderAlpha = borderAlpha;
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

	public HighlightConditionList getNodeHighlightConditions() {
		return nodeHighlightConditions;
	}

	public void setNodeHighlightConditions(
			HighlightConditionList nodeHighlightConditions) {
		this.nodeHighlightConditions = nodeHighlightConditions;
	}
}
