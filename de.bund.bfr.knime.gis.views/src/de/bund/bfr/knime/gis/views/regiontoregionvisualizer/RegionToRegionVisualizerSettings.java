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
package de.bund.bfr.knime.gis.views.regiontoregionvisualizer;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.gis.views.GisToGisVisualizerSettings;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;

public class RegionToRegionVisualizerSettings extends
		GisToGisVisualizerSettings {

	private static final String CFG_SHAPE_REGION_COLUMN = "ShapeRegionColumn";
	private static final String CFG_NODE_REGION_COLUMN = "NodeRegionColumn";
	
	private static final String CFG_GIS_NODE_HIGHLIGHT_CONDITIONS = "GisNodeHighlightConditions";
	private static final String CFG_GIS_EDGE_HIGHLIGHT_CONDITIONS = "GisEdgeHighlightConditions";

	private String shapeRegionColumn;
	private String nodeRegionColumn;
	
	private HighlightConditionList gisNodeHighlightConditions;
	private HighlightConditionList gisEdgeHighlightConditions;

	public RegionToRegionVisualizerSettings() {
		shapeRegionColumn = null;
		nodeRegionColumn = null;
		
		gisNodeHighlightConditions = new HighlightConditionList();
		gisEdgeHighlightConditions = new HighlightConditionList();
	}
	
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		super.loadSettings(settings);

		try {
			shapeRegionColumn = settings.getString(CFG_SHAPE_REGION_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeRegionColumn = settings.getString(CFG_NODE_REGION_COLUMN);
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
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		super.saveSettings(settings);
		settings.addString(CFG_SHAPE_REGION_COLUMN, shapeRegionColumn);
		settings.addString(CFG_NODE_REGION_COLUMN, nodeRegionColumn);
		
		settings.addString(CFG_GIS_NODE_HIGHLIGHT_CONDITIONS,
				SERIALIZER.toXml(gisNodeHighlightConditions));
		settings.addString(CFG_GIS_EDGE_HIGHLIGHT_CONDITIONS,
				SERIALIZER.toXml(gisEdgeHighlightConditions));
	}

	public String getShapeRegionColumn() {
		return shapeRegionColumn;
	}

	public void setShapeRegionColumn(String shapeRegionColumn) {
		this.shapeRegionColumn = shapeRegionColumn;
	}

	public String getNodeRegionColumn() {
		return nodeRegionColumn;
	}

	public void setNodeRegionColumn(String nodeRegionColumn) {
		this.nodeRegionColumn = nodeRegionColumn;
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
