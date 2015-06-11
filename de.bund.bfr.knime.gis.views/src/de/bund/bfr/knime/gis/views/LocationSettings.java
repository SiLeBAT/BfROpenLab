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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.gis.views.canvas.Canvas;

public class LocationSettings extends GisSettings {

	public enum GisType {
		SHAPEFILE("Shapefile"), MAPNIK("Mapnik"), CYCLE_MAP("Cycle Map"), BING_AERIAL("Bing Aerial"), MAPQUEST(
				"MapQuest"), MAPQUEST_AERIAL("MapQuest Aerial");

		private String name;

		private GisType(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		public static GisType[] valuesWithoutShapefile() {
			List<GisType> types = new ArrayList<>(Arrays.asList(values()));

			types.remove(SHAPEFILE);

			return types.toArray(new GisType[0]);
		}
	}

	private static final String CFG_GIS_TYPE = "GisType";
	private static final String CFG_NODE_LATITUDE_COLUMN = "NodeLatitudeColumn";
	private static final String CFG_NODE_LONGITUDE_COLUMN = "NodeLongitudeColumn";
	private static final String CFG_NODE_SIZE = "GisLocationSize";
	private static final String CFG_NODE_MAX_SIZE = "GisNodeMaxSize";
	private static final String CFG_EDGE_THICKNESS = "GisEdgeThickness";
	private static final String CFG_EDGE_MAX_THICKNESS = "GisEdgeMaxThickness";
	private static final String CFG_AVOID_OVERLAY = "AvoidOverlay";

	private GisType gisType;
	private String nodeLatitudeColumn;
	private String nodeLongitudeColumn;
	private int nodeSize;
	private Integer nodeMaxSize;
	private int edgeThickness;
	private Integer edgeMaxThickness;
	private boolean avoidOverlay;

	public LocationSettings() {
		gisType = GisType.SHAPEFILE;
		nodeLatitudeColumn = null;
		nodeLongitudeColumn = null;
		nodeSize = 4;
		nodeMaxSize = null;
		edgeThickness = 1;
		edgeMaxThickness = null;
		avoidOverlay = false;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		super.loadSettings(settings);

		try {
			gisType = GisType.valueOf(settings.getString(CFG_GIS_TYPE));
		} catch (InvalidSettingsException | IllegalArgumentException e) {
		}

		try {
			nodeLatitudeColumn = settings.getString(CFG_NODE_LATITUDE_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeLongitudeColumn = settings.getString(CFG_NODE_LONGITUDE_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeSize = settings.getInt(CFG_NODE_SIZE);
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeMaxSize = KnimeUtils.minusOneToNull(settings.getInt(CFG_NODE_MAX_SIZE));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeThickness = settings.getInt(CFG_EDGE_THICKNESS);
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeMaxThickness = KnimeUtils.minusOneToNull(settings.getInt(CFG_EDGE_MAX_THICKNESS));
		} catch (InvalidSettingsException e) {
		}

		try {
			avoidOverlay = settings.getBoolean(CFG_AVOID_OVERLAY);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		super.saveSettings(settings);
		settings.addString(CFG_GIS_TYPE, gisType.name());
		settings.addString(CFG_NODE_LATITUDE_COLUMN, nodeLatitudeColumn);
		settings.addString(CFG_NODE_LONGITUDE_COLUMN, nodeLongitudeColumn);
		settings.addInt(CFG_NODE_SIZE, nodeSize);
		settings.addInt(CFG_NODE_MAX_SIZE, KnimeUtils.nullToMinusOne(nodeMaxSize));
		settings.addInt(CFG_EDGE_THICKNESS, edgeThickness);
		settings.addInt(CFG_EDGE_MAX_THICKNESS, KnimeUtils.nullToMinusOne(edgeMaxThickness));
		settings.addBoolean(CFG_AVOID_OVERLAY, avoidOverlay);
	}

	@Override
	public void setFromCanvas(Canvas<?> canvas, boolean resized) {
		super.setFromCanvas(canvas, resized);
		nodeSize = canvas.getNodeSize();
		nodeMaxSize = canvas.getNodeMaxSize();
		edgeThickness = canvas.getEdgeThickness();
		edgeMaxThickness = canvas.getEdgeMaxThickness();
		avoidOverlay = canvas.isAvoidOverlay();
	}

	@Override
	public void setToCanvas(Canvas<?> canvas) {
		super.setToCanvas(canvas);
		canvas.setNodeSize(nodeSize);
		canvas.setNodeMaxSize(nodeMaxSize);
		canvas.setEdgeThickness(edgeThickness);
		canvas.setEdgeMaxThickness(edgeMaxThickness);
		canvas.setAvoidOverlay(avoidOverlay);
	}

	public GisType getGisType() {
		return gisType;
	}

	public void setGisType(GisType gisType) {
		this.gisType = gisType;
	}

	public String getNodeLatitudeColumn() {
		return nodeLatitudeColumn;
	}

	public void setNodeLatitudeColumn(String nodeLatitudeColumn) {
		this.nodeLatitudeColumn = nodeLatitudeColumn;
	}

	public String getNodeLongitudeColumn() {
		return nodeLongitudeColumn;
	}

	public void setNodeLongitudeColumn(String nodeLongitudeColumn) {
		this.nodeLongitudeColumn = nodeLongitudeColumn;
	}

	public int getNodeSize() {
		return nodeSize;
	}

	public void setNodeSize(int nodeSize) {
		this.nodeSize = nodeSize;
	}

	public Integer getNodeMaxSize() {
		return nodeMaxSize;
	}

	public void setNodeMaxSize(Integer nodeMaxSize) {
		this.nodeMaxSize = nodeMaxSize;
	}

	public int getEdgeThickness() {
		return edgeThickness;
	}

	public void setEdgeThickness(int edgeThickness) {
		this.edgeThickness = edgeThickness;
	}

	public Integer getEdgeMaxThickness() {
		return edgeMaxThickness;
	}

	public void setEdgeMaxThickness(Integer edgeMaxThickness) {
		this.edgeMaxThickness = edgeMaxThickness;
	}

	public boolean isAvoidOverlay() {
		return avoidOverlay;
	}

	public void setAvoidOverlay(boolean avoidOverlay) {
		this.avoidOverlay = avoidOverlay;
	}
}
