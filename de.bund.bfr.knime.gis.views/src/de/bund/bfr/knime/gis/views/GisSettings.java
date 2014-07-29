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
import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

public class GisSettings extends Settings {

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

	private static final boolean DEFAULT_SHOW_LEGEND = false;
	private static final int DEFAULT_FONT_SIZE = 12;
	private static final boolean DEFAULT_FONT_BOLD = false;
	private static final int DEFAULT_BORDER_ALPHA = 255;
	private static final Mode DEFAULT_EDITING_MODE = Mode.PICKING;
	private static final Dimension DEFAULT_CANVAS_SIZE = new Dimension(400, 600);

	private String shapeColumn;
	private boolean showLegend;
	private double scaleX;
	private double scaleY;
	private double translationX;
	private double translationY;
	private int fontSize;
	private boolean fontBold;
	private int borderAlpha;
	private Mode editingMode;
	private Dimension canvasSize;
	private List<String> selectedNodes;
	private HighlightConditionList nodeHighlightConditions;

	public GisSettings() {
		shapeColumn = null;
		showLegend = DEFAULT_SHOW_LEGEND;
		scaleX = Double.NaN;
		scaleY = Double.NaN;
		translationX = Double.NaN;
		translationY = Double.NaN;
		fontSize = DEFAULT_FONT_SIZE;
		fontBold = DEFAULT_FONT_BOLD;
		borderAlpha = DEFAULT_BORDER_ALPHA;
		editingMode = DEFAULT_EDITING_MODE;
		selectedNodes = new ArrayList<>();
		nodeHighlightConditions = new HighlightConditionList();
		canvasSize = DEFAULT_CANVAS_SIZE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			shapeColumn = settings.getString(CFG_SHAPE_COLUMN);
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
		settings.addString(CFG_SHAPE_COLUMN, shapeColumn);
		settings.addBoolean(CFG_SHOW_LEGEND, showLegend);
		settings.addDouble(CFG_SCALE_X, scaleX);
		settings.addDouble(CFG_SCALE_Y, scaleY);
		settings.addDouble(CFG_TRANSLATION_X, translationX);
		settings.addDouble(CFG_TRANSLATION_Y, translationY);
		settings.addInt(CFG_FONT_SIZE, fontSize);
		settings.addBoolean(CFG_FONT_BOLD, fontBold);
		settings.addInt(CFG_BORDER_ALPHA, borderAlpha);
		settings.addString(CFG_EDITING_MODE, editingMode.name());
		settings.addString(CFG_SELECTED_NODES, SERIALIZER.toXml(selectedNodes));
		settings.addString(CFG_NODE_HIGHLIGHT_CONDITIONS,
				SERIALIZER.toXml(nodeHighlightConditions));
		settings.addString(CFG_CANVAS_SIZE, SERIALIZER.toXml(canvasSize));
	}

	public String getShapeColumn() {
		return shapeColumn;
	}

	public void setShapeColumn(String shapeColumn) {
		this.shapeColumn = shapeColumn;
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
