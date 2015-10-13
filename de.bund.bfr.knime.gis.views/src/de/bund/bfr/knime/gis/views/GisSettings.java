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
package de.bund.bfr.knime.gis.views;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.google.common.collect.Ordering;

import de.bund.bfr.knime.gis.GisType;
import de.bund.bfr.knime.gis.views.canvas.Canvas;
import de.bund.bfr.knime.gis.views.canvas.Transform;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

public class GisSettings extends Settings {

	private static final String CFG_GIS_TYPE = "GisType";
	private static final String CFG_SHAPE_COLUMN = "ShapeColumn";
	private static final String CFG_SHOW_LEGEND = "ShowLegend";
	private static final String CFG_SCALE_X = "ScaleX";
	private static final String CFG_SCALE_Y = "ScaleY";
	private static final String CFG_TRANSLATION_X = "TranslationX";
	private static final String CFG_TRANSLATION_Y = "TranslationY";
	private static final String CFG_FONT_SIZE = "TextSize";
	private static final String CFG_FONT_BOLD = "TextBold";
	private static final String CFG_BORDER_ALPHA = "BorderAlpha";
	private static final String CFG_EDITING_MODE = "EditingMode";
	private static final String CFG_CANVAS_SIZE = "CanvasSize";
	private static final String CFG_SELECTED_NODES = "SelectedNodes";
	private static final String CFG_NODE_HIGHLIGHT_CONDITIONS = "NodeHighlightConditions";
	private static final String CFG_LABEL = "Label";

	private GisType gisType;
	private String shapeColumn;
	private boolean showLegend;
	private Transform transform;
	private int fontSize;
	private boolean fontBold;
	private int borderAlpha;
	private Mode editingMode;
	private Dimension canvasSize;
	private List<String> selectedNodes;
	private HighlightConditionList nodeHighlightConditions;
	private String label;

	public GisSettings() {
		gisType = GisType.SHAPEFILE;
		shapeColumn = null;
		showLegend = false;
		transform = Transform.INVALID_TRANSFORM;
		fontSize = 12;
		fontBold = false;
		borderAlpha = 255;
		editingMode = Mode.PICKING;
		selectedNodes = new ArrayList<>();
		nodeHighlightConditions = new HighlightConditionList();
		canvasSize = null;
		label = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			gisType = GisType.valueOf(settings.getString(CFG_GIS_TYPE));
		} catch (InvalidSettingsException | IllegalArgumentException e) {
		}

		try {
			shapeColumn = settings.getString(CFG_SHAPE_COLUMN);
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
			selectedNodes = (List<String>) SERIALIZER.fromXml(settings.getString(CFG_SELECTED_NODES));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeHighlightConditions = (HighlightConditionList) SERIALIZER
					.fromXml(settings.getString(CFG_NODE_HIGHLIGHT_CONDITIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			canvasSize = (Dimension) SERIALIZER.fromXml(settings.getString(CFG_CANVAS_SIZE));
		} catch (InvalidSettingsException e) {
		}

		try {
			label = settings.getString(CFG_LABEL);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addString(CFG_GIS_TYPE, gisType.name());
		settings.addString(CFG_SHAPE_COLUMN, shapeColumn);
		settings.addBoolean(CFG_SHOW_LEGEND, showLegend);
		settings.addDouble(CFG_SCALE_X, transform.getScaleX());
		settings.addDouble(CFG_SCALE_Y, transform.getScaleY());
		settings.addDouble(CFG_TRANSLATION_X, transform.getTranslationX());
		settings.addDouble(CFG_TRANSLATION_Y, transform.getTranslationY());
		settings.addInt(CFG_FONT_SIZE, fontSize);
		settings.addBoolean(CFG_FONT_BOLD, fontBold);
		settings.addInt(CFG_BORDER_ALPHA, borderAlpha);
		settings.addString(CFG_EDITING_MODE, editingMode.name());
		settings.addString(CFG_SELECTED_NODES, SERIALIZER.toXml(selectedNodes));
		settings.addString(CFG_NODE_HIGHLIGHT_CONDITIONS, SERIALIZER.toXml(nodeHighlightConditions));
		settings.addString(CFG_CANVAS_SIZE, SERIALIZER.toXml(canvasSize));
		settings.addString(CFG_LABEL, label);
	}

	public void setFromCanvas(Canvas<?> canvas, boolean resized) {
		showLegend = canvas.isShowLegend();
		transform = canvas.getTransform();
		fontSize = canvas.getFontSize();
		fontBold = canvas.isFontBold();
		borderAlpha = canvas.getBorderAlpha();
		editingMode = canvas.getEditingMode();
		selectedNodes = Ordering.natural().sortedCopy(canvas.getSelectedNodeIds());
		nodeHighlightConditions = canvas.getNodeHighlightConditions();
		label = canvas.getLabel();

		if (resized || canvasSize == null) {
			canvasSize = canvas.getCanvasSize();
		}
	}

	public void setToCanvas(Canvas<?> canvas) {
		canvas.setShowLegend(showLegend);
		canvas.setFontSize(fontSize);
		canvas.setFontBold(fontBold);
		canvas.setBorderAlpha(borderAlpha);
		canvas.setEditingMode(editingMode);
		canvas.setNodeHighlightConditions(nodeHighlightConditions);
		canvas.setSelectedNodeIds(new LinkedHashSet<>(selectedNodes));
		canvas.setLabel(label);

		if (canvasSize != null) {
			canvas.setCanvasSize(canvasSize);
		}

		if (transform.isValid()) {
			canvas.setTransform(transform);
		}
	}

	public GisType getGisType() {
		return gisType;
	}

	public void setGisType(GisType gisType) {
		this.gisType = gisType;
	}

	public String getShapeColumn() {
		return shapeColumn;
	}

	public void setShapeColumn(String shapeColumn) {
		this.shapeColumn = shapeColumn;
	}
}
