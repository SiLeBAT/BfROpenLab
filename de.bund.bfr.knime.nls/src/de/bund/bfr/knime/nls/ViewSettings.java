/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.nls;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.chart.NamedShape;
import de.bund.bfr.knime.nls.chart.ChartConfigPanel;
import de.bund.bfr.knime.nls.chart.ChartCreator;
import de.bund.bfr.knime.nls.chart.ChartSelectionPanel;
import de.bund.bfr.math.InterpolationFactory;
import de.bund.bfr.math.Transform;

public class ViewSettings extends NlsNodeSettings {

	private static final String CFG_MIN_TO_ZERO = "MinToZero";
	private static final String CFG_MANUAL_RANGE = "ManualRange";
	private static final String CFG_MIN_X = "MinX";
	private static final String CFG_MAX_X = "MaxX";
	private static final String CFG_MIN_Y = "MinY";
	private static final String CFG_MAX_Y = "MaxY";
	private static final String CFG_DRAW_LINES = "DrawLines";
	private static final String CFG_SHOW_LEGEND = "ShowLegend";
	private static final String CFG_EXPORT_AS_SVG = "ExportAsSvg";
	private static final String CFG_SHOW_CONFIDENCE = "ShowConfidence";
	private static final String CFG_RESOLUTION = "Resolution";
	private static final String CFG_INTERPOLATOR = "Interpolator";
	private static final String CFG_VAR_X = "CurrentParamX";
	private static final String CFG_TRANSFORM_X = "TransformX";
	private static final String CFG_TRANSFORM_Y = "TransformY";
	private static final String CFG_SELECT_ALL = "SelectAll";
	private static final String CFG_SELECTED_IDS = "SelectedIDs";
	private static final String CFG_COLORS = "Colors";
	private static final String CFG_SHAPES = "Shapes2";

	private boolean minToZero;
	private boolean manualRange;
	private double minX;
	private double maxX;
	private double minY;
	private double maxY;
	private boolean drawLines;
	private boolean showLegend;
	private boolean exportAsSvg;
	private boolean showConfidence;
	private int resolution;
	private InterpolationFactory.Type interpolator;
	private String varX;
	private Transform transformX;
	private Transform transformY;
	private boolean selectAll;
	private List<String> selectedIDs;
	private Map<String, Color> colors;
	private Map<String, NamedShape> shapes;

	public ViewSettings() {
		minToZero = false;
		manualRange = false;
		minX = 0.0;
		maxX = 10.0;
		minY = 0.0;
		maxY = 10.0;
		drawLines = false;
		showLegend = true;
		exportAsSvg = false;
		showConfidence = false;
		resolution = 1000;
		interpolator = InterpolationFactory.Type.STEP;
		varX = null;
		transformX = Transform.NO_TRANSFORM;
		transformY = Transform.NO_TRANSFORM;
		selectAll = true;
		selectedIDs = new ArrayList<>();
		colors = new LinkedHashMap<>();
		shapes = new LinkedHashMap<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			minToZero = settings.getBoolean(CFG_MIN_TO_ZERO);
		} catch (InvalidSettingsException e) {
		}

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
			resolution = settings.getInt(CFG_RESOLUTION);
		} catch (InvalidSettingsException e) {
		}

		try {
			interpolator = InterpolationFactory.Type.valueOf(settings.getString(CFG_INTERPOLATOR));
		} catch (InvalidSettingsException | IllegalArgumentException e) {
		}

		try {
			varX = settings.getString(CFG_VAR_X);
		} catch (InvalidSettingsException e) {
		}

		try {
			transformX = Transform.valueOf(settings.getString(CFG_TRANSFORM_X));
		} catch (InvalidSettingsException | IllegalArgumentException e) {
		}

		try {
			transformY = Transform.valueOf(settings.getString(CFG_TRANSFORM_Y));
		} catch (InvalidSettingsException | IllegalArgumentException e) {
		}

		try {
			selectAll = settings.getBoolean(CFG_SELECT_ALL);
		} catch (InvalidSettingsException e) {
		}

		try {
			selectedIDs = (List<String>) SERIALIZER.fromXml(settings.getString(CFG_SELECTED_IDS));
		} catch (InvalidSettingsException e) {
		}

		try {
			colors = (Map<String, Color>) SERIALIZER.fromXml(settings.getString(CFG_COLORS));
		} catch (InvalidSettingsException e) {
		}

		try {
			shapes = (Map<String, NamedShape>) SERIALIZER.fromXml(settings.getString(CFG_SHAPES));
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addBoolean(CFG_MIN_TO_ZERO, minToZero);
		settings.addBoolean(CFG_MANUAL_RANGE, manualRange);
		settings.addDouble(CFG_MIN_X, minX);
		settings.addDouble(CFG_MAX_X, maxX);
		settings.addDouble(CFG_MIN_Y, minY);
		settings.addDouble(CFG_MAX_Y, maxY);
		settings.addBoolean(CFG_DRAW_LINES, drawLines);
		settings.addBoolean(CFG_SHOW_LEGEND, showLegend);
		settings.addBoolean(CFG_EXPORT_AS_SVG, exportAsSvg);
		settings.addBoolean(CFG_SHOW_CONFIDENCE, showConfidence);
		settings.addInt(CFG_RESOLUTION, resolution);
		settings.addString(CFG_INTERPOLATOR, interpolator.name());
		settings.addString(CFG_VAR_X, varX);
		settings.addString(CFG_TRANSFORM_X, transformX.name());
		settings.addString(CFG_TRANSFORM_Y, transformY.name());
		settings.addBoolean(CFG_SELECT_ALL, selectAll);
		settings.addString(CFG_SELECTED_IDS, SERIALIZER.toXml(selectedIDs));
		settings.addString(CFG_COLORS, SERIALIZER.toXml(colors));
		settings.addString(CFG_SHAPES, SERIALIZER.toXml(shapes));

	}

	public void setToChartCreator(ChartCreator creator) {
		creator.setMinToZero(minToZero);
		creator.setManualRange(manualRange);
		creator.setMinX(minX);
		creator.setMaxX(maxX);
		creator.setMinY(minY);
		creator.setMaxY(maxY);
		creator.setDrawLines(drawLines);
		creator.setShowLegend(showLegend);
		creator.setShowConfidence(showConfidence);
		creator.setResolution(resolution);
		creator.setInterpolator(interpolator);
		creator.setVarX(varX);
		creator.setTransformX(transformX);
		creator.setTransformY(transformY);
		creator.setSelectAll(selectAll);
		creator.setSelectedIds(selectedIDs);
		creator.setColors(colors);
		creator.setShapes(shapes);
	}

	public void setFromConfigPanel(ChartConfigPanel configPanel) {
		minToZero = configPanel.isMinToZero();
		manualRange = configPanel.isManualRange();
		minX = configPanel.getMinX();
		maxX = configPanel.getMaxX();
		minY = configPanel.getMinY();
		maxY = configPanel.getMaxY();
		drawLines = configPanel.isDrawLines();
		showLegend = configPanel.isShowLegend();
		exportAsSvg = configPanel.isExportAsSvg();
		showConfidence = configPanel.isShowConfidence();
		resolution = configPanel.getResolution();
		interpolator = configPanel.getInterpolator();
		varX = configPanel.getVarX();
		transformX = configPanel.getTransformX();
		transformY = configPanel.getTransformY();
	}

	public void setToConfigPanel(ChartConfigPanel configPanel) {
		configPanel.setMinToZero(minToZero);
		configPanel.setManualRange(manualRange);
		configPanel.setMinX(minX);
		configPanel.setMaxX(maxX);
		configPanel.setMinY(minY);
		configPanel.setMaxY(maxY);
		configPanel.setDrawLines(drawLines);
		configPanel.setShowLegend(showLegend);
		configPanel.setExportAsSvg(exportAsSvg);
		configPanel.setShowConfidence(showConfidence);
		configPanel.setResolution(resolution);
		configPanel.setInterpolator(interpolator);
		configPanel.setVarX(varX);
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

	public boolean isExportAsSvg() {
		return exportAsSvg;
	}

	public void setExportAsSvg(boolean exportAsSvg) {
		this.exportAsSvg = exportAsSvg;
	}

	public String getVarX() {
		return varX;
	}

	public void setVarX(String varX) {
		this.varX = varX;
	}
}
