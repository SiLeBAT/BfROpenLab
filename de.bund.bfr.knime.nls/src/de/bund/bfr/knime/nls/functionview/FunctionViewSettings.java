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
package de.bund.bfr.knime.nls.functionview;

import java.awt.Color;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.nls.NlsNodeSettings;
import de.bund.bfr.knime.nls.chart.ChartConfigPanel;
import de.bund.bfr.knime.nls.chart.ChartCreator;
import de.bund.bfr.knime.nls.chart.ChartSelectionPanel;
import de.bund.bfr.knime.nls.chart.ChartUtilities;

public class FunctionViewSettings extends NlsNodeSettings {

	public static final boolean DEFAULT_MANUAL_RANGE = false;
	public static final double DEFAULT_MIN_X = 0.0;
	public static final double DEFAULT_MAX_X = 10.0;
	public static final double DEFAULT_MIN_Y = 0.0;
	public static final double DEFAULT_MAX_Y = 10.0;
	public static final boolean DEFAULT_DRAW_LINES = false;
	public static final boolean DEFAULT_SHOW_LEGEND = true;
	public static final boolean DEFAULT_EXPORT_AS_SVG = false;
	public static final boolean DEFAULT_SHOW_CONFIDENCE = false;
	public static final String DEFAULT_TRANSFORM = ChartUtilities.NO_TRANSFORM;
	public static final boolean DEFAULT_SELECT_ALL = false;

	private static final String CFG_MANUAL_RANGE = "ManualRange";
	private static final String CFG_MIN_X = "MinX";
	private static final String CFG_MAX_X = "MaxX";
	private static final String CFG_MIN_Y = "MinY";
	private static final String CFG_MAX_Y = "MaxY";
	private static final String CFG_DRAW_LINES = "DrawLines";
	private static final String CFG_SHOW_LEGEND = "ShowLegend";
	private static final String CFG_EXPORT_AS_SVG = "ExportAsSvg";
	private static final String CFG_SHOW_CONFIDENCE = "ShowConfidence";
	private static final String CFG_CURRENT_PARAM_X = "CurrentParamX";
	private static final String CFG_TRANSFORM_X = "TransformX";
	private static final String CFG_TRANSFORM_Y = "TransformY";
	private static final String CFG_SELECT_ALL = "SelectAll";
	private static final String CFG_SELECTED_IDS = "SelectedIDs";
	private static final String CFG_COLORS = "Colors";
	private static final String CFG_SHAPES = "Shapes";

	private boolean manualRange;
	private double minX;
	private double maxX;
	private double minY;
	private double maxY;
	private boolean drawLines;
	private boolean showLegend;
	private boolean exportAsSvg;
	private boolean showConfidence;
	private String currentParamX;
	private String transformX;
	private String transformY;
	private boolean selectAll;
	private List<String> selectedIDs;
	private Map<String, Color> colors;
	private Map<String, Shape> shapes;

	public FunctionViewSettings() {
		manualRange = DEFAULT_MANUAL_RANGE;
		minX = DEFAULT_MIN_X;
		maxX = DEFAULT_MAX_X;
		minY = DEFAULT_MIN_Y;
		maxY = DEFAULT_MAX_Y;
		drawLines = DEFAULT_DRAW_LINES;
		showLegend = DEFAULT_SHOW_LEGEND;
		exportAsSvg = DEFAULT_EXPORT_AS_SVG;
		showConfidence = DEFAULT_SHOW_CONFIDENCE;
		currentParamX = null;
		transformX = DEFAULT_TRANSFORM;
		transformY = DEFAULT_TRANSFORM;
		selectAll = DEFAULT_SELECT_ALL;
		selectedIDs = new ArrayList<String>();
		colors = new LinkedHashMap<String, Color>();
		shapes = new LinkedHashMap<String, Shape>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			manualRange = settings.getBoolean(CFG_MANUAL_RANGE);
		} catch (InvalidSettingsException e) {
		}

		try {
			minX = settings.getDouble(CFG_MIN_X);
		} catch (InvalidSettingsException e) {
		}

		try {
			maxX = settings.getDouble(CFG_MAX_X);
		} catch (InvalidSettingsException e) {
		}

		try {
			minY = settings.getDouble(CFG_MIN_Y);
		} catch (InvalidSettingsException e) {
		}

		try {
			maxY = settings.getDouble(CFG_MAX_Y);
		} catch (InvalidSettingsException e) {
		}

		try {
			drawLines = settings.getBoolean(CFG_DRAW_LINES);
		} catch (InvalidSettingsException e) {
		}

		try {
			showLegend = settings.getBoolean(CFG_SHOW_LEGEND);
		} catch (InvalidSettingsException e) {
		}

		try {
			exportAsSvg = settings.getBoolean(CFG_EXPORT_AS_SVG);
		} catch (InvalidSettingsException e) {
		}

		try {
			showConfidence = settings.getBoolean(CFG_SHOW_CONFIDENCE);
		} catch (InvalidSettingsException e) {
		}

		try {
			currentParamX = settings.getString(CFG_CURRENT_PARAM_X);
		} catch (InvalidSettingsException e) {
		}

		try {
			transformX = settings.getString(CFG_TRANSFORM_X);
		} catch (InvalidSettingsException e) {
		}

		try {
			transformY = settings.getString(CFG_TRANSFORM_Y);
		} catch (InvalidSettingsException e) {
		}

		try {
			selectAll = settings.getBoolean(CFG_SELECT_ALL);
		} catch (InvalidSettingsException e) {
		}

		try {
			selectedIDs = (List<String>) SERIALIZER.fromXml(settings
					.getString(CFG_SELECTED_IDS));
		} catch (InvalidSettingsException e) {
		}

		try {
			colors = (Map<String, Color>) SERIALIZER.fromXml(settings
					.getString(CFG_COLORS));
		} catch (InvalidSettingsException e) {
		}

		try {
			shapes = (Map<String, Shape>) SERIALIZER.fromXml(settings
					.getString(CFG_SHAPES));
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addBoolean(CFG_MANUAL_RANGE, manualRange);
		settings.addDouble(CFG_MIN_X, minX);
		settings.addDouble(CFG_MAX_X, maxX);
		settings.addDouble(CFG_MIN_Y, minY);
		settings.addDouble(CFG_MAX_Y, maxY);
		settings.addBoolean(CFG_DRAW_LINES, drawLines);
		settings.addBoolean(CFG_SHOW_LEGEND, showLegend);
		settings.addBoolean(CFG_EXPORT_AS_SVG, exportAsSvg);
		settings.addBoolean(CFG_SHOW_CONFIDENCE, showConfidence);
		settings.addString(CFG_CURRENT_PARAM_X, currentParamX);
		settings.addString(CFG_TRANSFORM_X, transformX);
		settings.addString(CFG_TRANSFORM_Y, transformY);
		settings.addBoolean(CFG_SELECT_ALL, selectAll);
		settings.addString(CFG_SELECTED_IDS, SERIALIZER.toXml(selectedIDs));
		settings.addString(CFG_COLORS, SERIALIZER.toXml(colors));
		settings.addString(CFG_SHAPES, SERIALIZER.toXml(shapes));

	}

	public void setToChartCreator(ChartCreator creator) {
		creator.setManualRange(manualRange);
		creator.setMinX(minX);
		creator.setMaxX(maxX);
		creator.setMinY(minY);
		creator.setMaxY(maxY);
		creator.setDrawLines(drawLines);
		creator.setShowLegend(showLegend);
		creator.setShowConfidence(showConfidence);
		creator.setParamX(currentParamX);
		creator.setTransformX(transformX);
		creator.setTransformY(transformY);
		creator.setSelectAll(selectAll);
		creator.setSelectedIds(selectedIDs);
		creator.setColors(colors);
		creator.setShapes(shapes);
	}

	public void setFromConfigPanel(ChartConfigPanel configPanel) {
		manualRange = configPanel.isManualRange();
		minX = configPanel.getMinX();
		maxX = configPanel.getMaxX();
		minY = configPanel.getMinY();
		maxY = configPanel.getMaxY();
		drawLines = configPanel.isDrawLines();
		showLegend = configPanel.isShowLegend();
		exportAsSvg = configPanel.isExportAsSvg();
		showConfidence = configPanel.isShowConfidence();
		currentParamX = configPanel.getParamX();
		transformX = configPanel.getTransformX();
		transformY = configPanel.getTransformY();
	}

	public void setToConfigPanel(ChartConfigPanel configPanel) {
		configPanel.setManualRange(manualRange);
		configPanel.setMinX(minX);
		configPanel.setMaxX(maxX);
		configPanel.setMinY(minY);
		configPanel.setMaxY(maxY);
		configPanel.setDrawLines(drawLines);
		configPanel.setShowLegend(showLegend);
		configPanel.setExportAsSvg(exportAsSvg);
		configPanel.setShowConfidence(showConfidence);
		configPanel.setParamX(currentParamX);
		configPanel.setTransformX(transformX);
		configPanel.setTransformY(transformY);
	}

	public void setFromSelectionPanel(ChartSelectionPanel selectionPanel) {
		selectAll = selectionPanel.isSelectAll();
		selectedIDs = selectionPanel.getSelectedIds();
		colors = selectionPanel.getColors();
		shapes = selectionPanel.getShapes();
	}

	public void setToSelectionPanel(ChartSelectionPanel selectionPanel) {
		selectionPanel.setSelectAll(selectAll);
		selectionPanel.setColors(colors);
		selectionPanel.setShapes(shapes);
		selectionPanel.setSelectedIds(selectedIDs);
	}

	public boolean isManualRange() {
		return manualRange;
	}

	public void setManualRange(boolean manualRange) {
		this.manualRange = manualRange;
	}

	public double getMinX() {
		return minX;
	}

	public void setMinX(double minX) {
		this.minX = minX;
	}

	public double getMaxX() {
		return maxX;
	}

	public void setMaxX(double maxX) {
		this.maxX = maxX;
	}

	public double getMinY() {
		return minY;
	}

	public void setMinY(double minY) {
		this.minY = minY;
	}

	public double getMaxY() {
		return maxY;
	}

	public void setMaxY(double maxY) {
		this.maxY = maxY;
	}

	public boolean isDrawLines() {
		return drawLines;
	}

	public void setDrawLines(boolean drawLines) {
		this.drawLines = drawLines;
	}

	public boolean isShowLegend() {
		return showLegend;
	}

	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
	}

	public boolean isExportAsSvg() {
		return exportAsSvg;
	}

	public void setExportAsSvg(boolean exportAsSvg) {
		this.exportAsSvg = exportAsSvg;
	}

	public boolean isShowConfidence() {
		return showConfidence;
	}

	public void setShowConfidence(boolean showConfidence) {
		this.showConfidence = showConfidence;
	}

	public String getCurrentParamX() {
		return currentParamX;
	}

	public void setCurrentParamX(String currentParamX) {
		this.currentParamX = currentParamX;
	}

	public String getTransformX() {
		return transformX;
	}

	public void setTransformX(String transformX) {
		this.transformX = transformX;
	}

	public String getTransformY() {
		return transformY;
	}

	public void setTransformY(String transformY) {
		this.transformY = transformY;
	}

	public boolean isSelectAll() {
		return selectAll;
	}

	public void setSelectAll(boolean selectAll) {
		this.selectAll = selectAll;
	}

	public List<String> getSelectedIDs() {
		return selectedIDs;
	}

	public void setSelectedIDs(List<String> selectedIDs) {
		this.selectedIDs = selectedIDs;
	}

	public Map<String, Color> getColors() {
		return colors;
	}

	public void setColors(Map<String, Color> colors) {
		this.colors = colors;
	}

	public Map<String, Shape> getShapes() {
		return shapes;
	}

	public void setShapes(Map<String, Shape> shapes) {
		this.shapes = shapes;
	}

}
