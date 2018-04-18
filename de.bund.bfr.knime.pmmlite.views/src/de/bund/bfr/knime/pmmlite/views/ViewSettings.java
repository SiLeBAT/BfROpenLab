/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.views;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.pmmlite.core.Plotable;
import de.bund.bfr.knime.pmmlite.core.PmmUnit;
import de.bund.bfr.knime.pmmlite.core.XmlUtils;
import de.bund.bfr.knime.pmmlite.views.chart.ChartConfigPanel;
import de.bund.bfr.knime.pmmlite.views.chart.ChartCreator;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSamplePanel;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSelectionPanel;
import de.bund.bfr.math.Transform;

public abstract class ViewSettings {

	private static final String CFG_FIRST_START = "FirstStart";
	private static final String CFG_VAR_X = "ParamX";
	private static final String CFG_VAR_Y = "ParamY";
	private static final String CFG_MANUAL_RANGE = "ManualRange";
	private static final String CFG_MIN_X = "MinX";
	private static final String CFG_MAX_X = "MaxX";
	private static final String CFG_MIN_Y = "MinY";
	private static final String CFG_MAX_Y = "MaxY";
	private static final String CFG_DRAW_LINES = "DrawLines";
	private static final String CFG_SHOW_LEGEND = "ShowLegend";
	private static final String CFG_DISPLAY_HIGHLIGHTED = "DisplayHighlighted";
	private static final String CFG_EXPORT_AS_SVG = "ExportAsSvg";
	private static final String CFG_SHOW_CONFIDENCE = "ShowConfidence";
	private static final String CFG_SHOW_PREDICTION = "ShowPrediction";
	private static final String CFG_EXPORT_WIDTH = "ExportWidth";
	private static final String CFG_EXPORT_HEIGHT = "ExportHeight";
	private static final String CFG_RESOLUTION = "Resolution";
	private static final String CFG_UNIT_X = "UnitX";
	private static final String CFG_UNIT_Y = "UnitY";
	private static final String CFG_TRANSFORM_X = "TransformX";
	private static final String CFG_TRANSFORM_Y = "TransformY";
	private static final String CFG_VISIBLE_COLUMNS = "VisibleColumns";
	private static final String CFG_COLUMN_WIDTHS = "ColumnWidths";
	private static final String CFG_FILTERS = "Filters";

	private static final boolean DEFAULT_FIRST_START = true;
	private static final boolean DEFAULT_MANUAL_RANGE = false;
	private static final double DEFAULT_MIN_X = 0.0;
	private static final double DEFAULT_MAX_X = 100.0;
	private static final double DEFAULT_MIN_Y = 0.0;
	private static final double DEFAULT_MAX_Y = 10.0;
	private static final boolean DEFAULT_DRAW_LINES = false;
	private static final boolean DEFAULT_SHOW_LEGEND = true;
	private static final boolean DEFAULT_DISPLAY_HIGHLIGHTED = false;
	private static final boolean DEFAULT_EXPORT_AS_SVG = false;
	private static final int DEFAULT_EXPORT_WIDTH = 640;
	private static final int DEFAULT_EXPORT_HEIGHT = 480;
	private static final int DEFAULT_RESOLUTION = 1000;
	private static final boolean DEFAULT_SHOW_CONFIDENCE = false;
	private static final boolean DEFAULT_SHOW_PREDICTION = false;
	private static final PmmUnit DEFAULT_UNIT = new PmmUnit.Builder().build();
	private static final Transform DEFAULT_TRANSFORM = Transform.NO_TRANSFORM;

	protected boolean firstStart;
	protected String varX;
	protected String varY;
	protected boolean manualRange;
	protected double minX;
	protected double maxX;
	protected double minY;
	protected double maxY;
	protected boolean drawLines;
	protected boolean showLegend;
	protected boolean displayHighlighted;
	protected boolean exportAsSvg;
	protected boolean showConfidence;
	protected boolean showPrediction;
	protected int exportWidth;
	protected int exportHeight;
	protected int resolution;
	protected PmmUnit unitX;
	protected PmmUnit unitY;
	protected Transform transformX;
	protected Transform transformY;
	protected Set<String> visibleColumns;
	protected Map<String, Integer> columnWidths;
	protected Map<String, String> filters;

	public ViewSettings() {
		firstStart = DEFAULT_FIRST_START;
		varX = null;
		varY = null;
		manualRange = DEFAULT_MANUAL_RANGE;
		minX = DEFAULT_MIN_X;
		maxX = DEFAULT_MAX_X;
		minY = DEFAULT_MIN_Y;
		maxY = DEFAULT_MAX_Y;
		drawLines = DEFAULT_DRAW_LINES;
		showLegend = DEFAULT_SHOW_LEGEND;
		displayHighlighted = DEFAULT_DISPLAY_HIGHLIGHTED;
		exportAsSvg = DEFAULT_EXPORT_AS_SVG;
		showConfidence = DEFAULT_SHOW_CONFIDENCE;
		showPrediction = DEFAULT_SHOW_PREDICTION;
		exportWidth = DEFAULT_EXPORT_WIDTH;
		exportHeight = DEFAULT_EXPORT_HEIGHT;
		resolution = DEFAULT_RESOLUTION;
		unitX = DEFAULT_UNIT;
		unitY = DEFAULT_UNIT;
		transformX = DEFAULT_TRANSFORM;
		transformY = DEFAULT_TRANSFORM;
		visibleColumns = new LinkedHashSet<>();
		columnWidths = new LinkedHashMap<>();
		filters = new LinkedHashMap<>();
	}

	@SuppressWarnings("unchecked")
	public void load(NodeSettingsRO settings) {
		try {
			firstStart = settings.getBoolean(CFG_FIRST_START);
		} catch (InvalidSettingsException e) {
		}

		try {
			varX = settings.getString(CFG_VAR_X);
		} catch (InvalidSettingsException e) {
		}

		try {
			varY = settings.getString(CFG_VAR_Y);
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
			displayHighlighted = settings.getBoolean(CFG_DISPLAY_HIGHLIGHTED);
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
			showPrediction = settings.getBoolean(CFG_SHOW_PREDICTION);
		} catch (InvalidSettingsException e) {
		}

		try {
			exportWidth = settings.getInt(CFG_EXPORT_WIDTH);
		} catch (InvalidSettingsException e) {
		}

		try {
			exportHeight = settings.getInt(CFG_EXPORT_HEIGHT);
		} catch (InvalidSettingsException e) {
		}

		try {
			resolution = settings.getInt(CFG_RESOLUTION);
		} catch (InvalidSettingsException e) {
		}

		try {
			unitX = (PmmUnit) new PmmUnit.ConversionDelegate().createFromString(settings.getString(CFG_UNIT_X));
		} catch (InvalidSettingsException e) {
		}

		try {
			unitY = (PmmUnit) new PmmUnit.ConversionDelegate().createFromString(settings.getString(CFG_UNIT_Y));
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
			visibleColumns = (Set<String>) XmlUtils.fromXml(settings.getString(CFG_VISIBLE_COLUMNS));
		} catch (InvalidSettingsException e) {
		}

		try {
			columnWidths = (Map<String, Integer>) XmlUtils.fromXml(settings.getString(CFG_COLUMN_WIDTHS));
		} catch (InvalidSettingsException e) {
		}

		try {
			filters = (Map<String, String>) XmlUtils.fromXml(settings.getString(CFG_FILTERS));
		} catch (InvalidSettingsException e) {
		}
	}

	public void save(NodeSettingsWO settings) throws InvalidSettingsException {
		if (displayHighlighted) {
			throw new InvalidSettingsException("Dialog cannot be saved with \"Display Highlighted Row\" enabled");
		}

		settings.addBoolean(CFG_FIRST_START, firstStart);
		settings.addString(CFG_VAR_X, varX);
		settings.addString(CFG_VAR_Y, varY);
		settings.addBoolean(CFG_MANUAL_RANGE, manualRange);
		settings.addDouble(CFG_MIN_X, minX);
		settings.addDouble(CFG_MAX_X, maxX);
		settings.addDouble(CFG_MIN_Y, minY);
		settings.addDouble(CFG_MAX_Y, maxY);
		settings.addBoolean(CFG_DRAW_LINES, drawLines);
		settings.addBoolean(CFG_SHOW_LEGEND, showLegend);
		settings.addBoolean(CFG_DISPLAY_HIGHLIGHTED, displayHighlighted);
		settings.addBoolean(CFG_EXPORT_AS_SVG, exportAsSvg);
		settings.addBoolean(CFG_SHOW_CONFIDENCE, showConfidence);
		settings.addBoolean(CFG_SHOW_PREDICTION, showPrediction);
		settings.addInt(CFG_EXPORT_WIDTH, exportWidth);
		settings.addInt(CFG_EXPORT_HEIGHT, exportHeight);
		settings.addInt(CFG_RESOLUTION, resolution);
		settings.addString(CFG_UNIT_X, new PmmUnit.ConversionDelegate().convertToString(unitX));
		settings.addString(CFG_UNIT_Y, new PmmUnit.ConversionDelegate().convertToString(unitY));
		settings.addString(CFG_TRANSFORM_X, transformX.name());
		settings.addString(CFG_TRANSFORM_Y, transformY.name());
		settings.addString(CFG_VISIBLE_COLUMNS, XmlUtils.toXml(visibleColumns));
		settings.addString(CFG_COLUMN_WIDTHS, XmlUtils.toXml(columnWidths));
		settings.addString(CFG_FILTERS, XmlUtils.toXml(filters));
	}

	public abstract void setToPlotable(Plotable plotable);

	public void setToChartCreator(ChartCreator creator) {
		creator.setVarX(new Plotable.Variable(varX, unitX, transformX));
		creator.setVarY(new Plotable.Variable(varY, unitY, transformY));
		creator.setManualRange(manualRange);
		creator.setMinX(minX);
		creator.setMaxX(maxX);
		creator.setMinY(minY);
		creator.setMaxY(maxY);
		creator.setDrawLines(drawLines);
		creator.setShowLegend(showLegend);
		creator.setShowConfidence(showConfidence);
		creator.setShowPrediction(showPrediction);
		creator.setResolution(resolution);
	}

	public void setFromConfigPanel(ChartConfigPanel configPanel) {
		varX = configPanel.getVarX();
		varY = configPanel.getVarY();
		manualRange = configPanel.isManualRange();
		minX = configPanel.getMinX();
		maxX = configPanel.getMaxX();
		minY = configPanel.getMinY();
		maxY = configPanel.getMaxY();
		drawLines = configPanel.isDrawLines();
		showLegend = configPanel.isShowLegend();
		displayHighlighted = configPanel.isDisplayHighlighted();
		exportAsSvg = configPanel.isExportAsSvg();
		showConfidence = configPanel.isShowConfidence();
		showPrediction = configPanel.isShowPrediction();
		exportWidth = configPanel.getExportWidth();
		exportHeight = configPanel.getExportHeight();
		resolution = configPanel.getResolution();
		unitX = configPanel.getUnitX();
		unitY = configPanel.getUnitY();
		transformX = configPanel.getTransformX();
		transformY = configPanel.getTransformY();
	}

	public void setToConfigPanel(ChartConfigPanel configPanel) {
		configPanel.setVarX(varX);
		configPanel.setVarY(varY);
		configPanel.setManualRange(manualRange);
		configPanel.setMinX(minX);
		configPanel.setMaxX(maxX);
		configPanel.setMinY(minY);
		configPanel.setMaxY(maxY);
		configPanel.setDrawLines(drawLines);
		configPanel.setShowLegend(showLegend);
		configPanel.setDisplayHighlighted(displayHighlighted);
		configPanel.setExportAsSvg(exportAsSvg);
		configPanel.setShowConfidence(showConfidence);
		configPanel.setShowPrediction(showPrediction);
		configPanel.setExportWidth(exportWidth);
		configPanel.setExportHeight(exportHeight);
		configPanel.setResolution(resolution);
		configPanel.setUnitX(unitX);
		configPanel.setUnitY(unitY);
		configPanel.setTransformX(transformX);
		configPanel.setTransformY(transformY);
	}

	public void setFromSelectionPanel(ChartSelectionPanel selectionPanel) {
		firstStart = false;
		visibleColumns = selectionPanel.getVisibleColumns();
		columnWidths = selectionPanel.getColumnWidths();
		filters = selectionPanel.getFilters();
	}

	public void setToSelectionPanel(ChartSelectionPanel selectionPanel) {
		if (!firstStart) {
			selectionPanel.setVisibleColumns(visibleColumns);
		}

		selectionPanel.setColumnWidths(columnWidths);
		selectionPanel.setFilters(filters);
	}

	public abstract void setFromSamplePanel(ChartSamplePanel samplePanel);

	public abstract void setToSamplePanel(ChartSamplePanel samplePanel);

	public boolean isFirstStart() {
		return firstStart;
	}

	public void setFirstStart(boolean firstStart) {
		this.firstStart = firstStart;
	}

	public boolean isExportAsSvg() {
		return exportAsSvg;
	}

	public void setExportAsSvg(boolean exportAsSvg) {
		this.exportAsSvg = exportAsSvg;
	}

	public int getExportWidth() {
		return exportWidth;
	}

	public void setExportWidth(int exportWidth) {
		this.exportWidth = exportWidth;
	}

	public int getExportHeight() {
		return exportHeight;
	}

	public void setExportHeight(int exportHeight) {
		this.exportHeight = exportHeight;
	}
}
