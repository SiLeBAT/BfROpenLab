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
package de.bund.bfr.knime.gis.views.locationtolocationvisualizer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.gis.views.GisToGisVisualizerSettings;

public class LocationToLocationVisualizerSettings extends
		GisToGisVisualizerSettings {

	public static final int DEFAULT_GIS_LOCATION_SIZE = 4;

	private static final String CFG_NODE_LATITUDE_COLUMN = "NodeLatitudeColumn";
	private static final String CFG_NODE_LONGITUDE_COLUMN = "NodeLongitudeColumn";
	private static final String CFG_GIS_LOCATION_SIZE = "GisLocationSize";

	private ByteArrayOutputStream xmlBaos;
	
	private String nodeLatitudeColumn;
	private String nodeLongitudeColumn;
	private int gisLocationSize;

	public LocationToLocationVisualizerSettings() {
		nodeLatitudeColumn = null;
		nodeLongitudeColumn = null;
		gisLocationSize = DEFAULT_GIS_LOCATION_SIZE;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		super.loadSettings(settings);
		try {
			xmlBaos = new ByteArrayOutputStream();
			settings.saveToXML(xmlBaos);
		} catch (IOException e1) {
			e1.printStackTrace();
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
			gisLocationSize = settings.getInt(CFG_GIS_LOCATION_SIZE);
		} catch (InvalidSettingsException e) {
		}
	}
	public String getXml() {
		return xmlBaos == null ? "" : xmlBaos.toString();
	}
	public void setXml(String xml) {
		ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());
		try {
			loadSettings(NodeSettings.loadFromXML(in));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		super.saveSettings(settings);
		settings.addString(CFG_NODE_LATITUDE_COLUMN, nodeLatitudeColumn);
		settings.addString(CFG_NODE_LONGITUDE_COLUMN, nodeLongitudeColumn);
		settings.addInt(CFG_GIS_LOCATION_SIZE, gisLocationSize);
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

	public int getGisLocationSize() {
		return gisLocationSize;
	}

	public void setGisLocationSize(int gisLocationSize) {
		this.gisLocationSize = gisLocationSize;
	}
}
