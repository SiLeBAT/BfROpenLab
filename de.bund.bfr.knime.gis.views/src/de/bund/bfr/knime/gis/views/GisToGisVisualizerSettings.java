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

import de.bund.bfr.knime.gis.views.canvas.RegionCanvas;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;

public class GisToGisVisualizerSettings extends SimpleGraphVisualizerSettings {

	public static final int DEFAULT_GIS_BORDER_ALPHA = 255;
	public static final String DEFAULT_GIS_EDITING_MODE = RegionCanvas.PICKING_MODE;
	public static final Dimension DEFAULT_GIS_CANVAS_SIZE = new Dimension(400,
			600);

	private static final String CFG_SHAPE_COLUMN = "ShapeColumn";

	private static final String CFG_GIS_SCALE_X = "GisScaleX";
	private static final String CFG_GIS_SCALE_Y = "GisScaleY";
	private static final String CFG_GIS_TRANSLATION_X = "GisTranslationX";
	private static final String CFG_GIS_TRANSLATION_Y = "GisTranslationY";
	private static final String CFG_GIS_BORDER_ALPHA = "GisBorderAlpha";
	private static final String CFG_GIS_SELECTED_NODES = "GisSelectedNodes";
	private static final String CFG_GIS_SELECTED_EDGES = "GisSelectedEdges";
	private static final String CFG_GIS_NODE_HIGHLIGHT_CONDITIONS = "GisNodeHighlightConditions";
	private static final String CFG_GIS_EDGE_HIGHLIGHT_CONDITIONS = "GisEdgeHighlightConditions";
	private static final String CFG_GIS_EDITING_MODE = "GisEditingMode";
	private static final String CFG_GIS_CANVAS_SIZE = "GisCanvasSize";

	private String shapeColumn;

	private double gisScaleX;
	private double gisScaleY;
	private double gisTranslationX;
	private double gisTranslationY;
	private int gisBorderAlpha;
	private String gisEditingMode;
	private Dimension gisCanvasSize;
	private List<String> gisSelectedNodes;
	private List<String> gisSelectedEdges;
	private HighlightConditionList gisNodeHighlightConditions;
	private HighlightConditionList gisEdgeHighlightConditions;

	public GisToGisVisualizerSettings() {
		shapeColumn = null;

		gisScaleX = Double.NaN;
		gisScaleY = Double.NaN;
		gisTranslationX = Double.NaN;
		gisTranslationY = Double.NaN;
		gisBorderAlpha = DEFAULT_GIS_BORDER_ALPHA;
		gisSelectedNodes = new ArrayList<String>();
		gisSelectedEdges = new ArrayList<String>();
		gisNodeHighlightConditions = new HighlightConditionList();
		gisEdgeHighlightConditions = new HighlightConditionList();
		gisEditingMode = DEFAULT_GIS_EDITING_MODE;
		gisCanvasSize = DEFAULT_GIS_CANVAS_SIZE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		super.loadSettings(settings);

		try {
			shapeColumn = settings.getString(CFG_SHAPE_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			gisScaleX = settings.getDouble(CFG_GIS_SCALE_X);
		} catch (InvalidSettingsException e) {
		}

		try {
			gisScaleY = settings.getDouble(CFG_GIS_SCALE_Y);
		} catch (InvalidSettingsException e) {
		}

		try {
			gisTranslationX = settings.getDouble(CFG_GIS_TRANSLATION_X);
		} catch (InvalidSettingsException e) {
		}

		try {
			gisTranslationY = settings.getDouble(CFG_GIS_TRANSLATION_Y);
		} catch (InvalidSettingsException e) {
		}

		try {
			gisBorderAlpha = settings.getInt(CFG_GIS_BORDER_ALPHA);
		} catch (InvalidSettingsException e) {
		}

		try {
			gisSelectedNodes = (List<String>) SERIALIZER.fromXml(settings
					.getString(CFG_GIS_SELECTED_NODES));
		} catch (InvalidSettingsException e) {
		}

		try {
			gisSelectedEdges = (List<String>) SERIALIZER.fromXml(settings
					.getString(CFG_GIS_SELECTED_EDGES));
		} catch (InvalidSettingsException e) {
		}

		try {
			gisNodeHighlightConditions = (HighlightConditionList) SERIALIZER
					.fromXml(settings
							.getString(CFG_GIS_NODE_HIGHLIGHT_CONDITIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			gisEdgeHighlightConditions = (HighlightConditionList) SERIALIZER
					.fromXml(settings
							.getString(CFG_GIS_EDGE_HIGHLIGHT_CONDITIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			gisEditingMode = settings.getString(CFG_GIS_EDITING_MODE);
		} catch (InvalidSettingsException e) {
		}
		try {
			gisCanvasSize = (Dimension) SERIALIZER.fromXml(settings
					.getString(CFG_GIS_CANVAS_SIZE));
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		super.saveSettings(settings);

		settings.addString(CFG_SHAPE_COLUMN, shapeColumn);

		settings.addDouble(CFG_GIS_SCALE_X, gisScaleX);
		settings.addDouble(CFG_GIS_SCALE_Y, gisScaleY);
		settings.addDouble(CFG_GIS_TRANSLATION_X, gisTranslationX);
		settings.addDouble(CFG_GIS_TRANSLATION_Y, gisTranslationY);
		settings.addInt(CFG_GIS_BORDER_ALPHA, gisBorderAlpha);
		settings.addString(CFG_GIS_SELECTED_NODES,
				SERIALIZER.toXml(gisSelectedNodes));
		settings.addString(CFG_GIS_SELECTED_EDGES,
				SERIALIZER.toXml(gisSelectedEdges));
		settings.addString(CFG_GIS_NODE_HIGHLIGHT_CONDITIONS,
				SERIALIZER.toXml(gisNodeHighlightConditions));
		settings.addString(CFG_GIS_EDGE_HIGHLIGHT_CONDITIONS,
				SERIALIZER.toXml(gisEdgeHighlightConditions));
		settings.addString(CFG_GIS_EDITING_MODE, gisEditingMode);
		settings.addString(CFG_GIS_CANVAS_SIZE, SERIALIZER.toXml(gisCanvasSize));
	}

	public String getShapeColumn() {
		return shapeColumn;
	}

	public void setShapeColumn(String shapeColumn) {
		this.shapeColumn = shapeColumn;
	}

	public double getGisScaleX() {
		return gisScaleX;
	}

	public void setGisScaleX(double gisScaleX) {
		this.gisScaleX = gisScaleX;
	}

	public double getGisScaleY() {
		return gisScaleY;
	}

	public void setGisScaleY(double gisScaleY) {
		this.gisScaleY = gisScaleY;
	}

	public double getGisTranslationX() {
		return gisTranslationX;
	}

	public void setGisTranslationX(double gisTranslationX) {
		this.gisTranslationX = gisTranslationX;
	}

	public double getGisTranslationY() {
		return gisTranslationY;
	}

	public void setGisTranslationY(double gisTranslationY) {
		this.gisTranslationY = gisTranslationY;
	}

	public int getGisBorderAlpha() {
		return gisBorderAlpha;
	}

	public void setGisBorderAlpha(int gisBorderAlpha) {
		this.gisBorderAlpha = gisBorderAlpha;
	}

	public String getGisEditingMode() {
		return gisEditingMode;
	}

	public void setGisEditingMode(String gisEditingMode) {
		this.gisEditingMode = gisEditingMode;
	}

	public Dimension getGisCanvasSize() {
		return gisCanvasSize;
	}

	public void setGisCanvasSize(Dimension gisCanvasSize) {
		this.gisCanvasSize = gisCanvasSize;
	}

	public List<String> getGisSelectedNodes() {
		return gisSelectedNodes;
	}

	public void setGisSelectedNodes(List<String> gisSelectedNodes) {
		this.gisSelectedNodes = gisSelectedNodes;
	}

	public List<String> getGisSelectedEdges() {
		return gisSelectedEdges;
	}

	public void setGisSelectedEdges(List<String> gisSelectedEdges) {
		this.gisSelectedEdges = gisSelectedEdges;
	}

	public HighlightConditionList getGisNodeHighlightConditions() {
		return gisNodeHighlightConditions;
	}

	public void setGisNodeHighlightConditions(
			HighlightConditionList gisNodeHighlightConditions) {
		this.gisNodeHighlightConditions = gisNodeHighlightConditions;
	}

	public HighlightConditionList getGisEdgeHighlightConditions() {
		return gisEdgeHighlightConditions;
	}

	public void setGisEdgeHighlightConditions(
			HighlightConditionList gisEdgeHighlightConditions) {
		this.gisEdgeHighlightConditions = gisEdgeHighlightConditions;
	}

}
