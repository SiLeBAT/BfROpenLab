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
package de.bund.bfr.knime.gis.views.locationvisualizer;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.gis.views.GisVisualizerSettings;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;

public class LocationVisualizerSettings extends
		GisVisualizerSettings<LocationNode> {

	public static final int DEFAULT_NODE_SIZE = 4;

	private static final String CFG_LATITUDE_COLUMN = "LatitudeColumn";
	private static final String CFG_LONGITUDE_COLUMN = "LongitudeColumn";
	private static final String CFG_NODE_SIZE = "LocationSize";

	private String latitudeColumn;
	private String longitudeColumn;
	private int nodeSize;

	public LocationVisualizerSettings() {
		latitudeColumn = null;
		longitudeColumn = null;
		nodeSize = DEFAULT_NODE_SIZE;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		super.loadSettings(settings);

		try {
			latitudeColumn = settings.getString(CFG_LATITUDE_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			longitudeColumn = settings.getString(CFG_LONGITUDE_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeSize = settings.getInt(CFG_NODE_SIZE);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		super.saveSettings(settings);
		settings.addString(CFG_LATITUDE_COLUMN, latitudeColumn);
		settings.addString(CFG_LONGITUDE_COLUMN, longitudeColumn);
		settings.addInt(CFG_NODE_SIZE, nodeSize);
	}

	public String getLatitudeColumn() {
		return latitudeColumn;
	}

	public void setLatitudeColumn(String latitudeColumn) {
		this.latitudeColumn = latitudeColumn;
	}

	public String getLongitudeColumn() {
		return longitudeColumn;
	}

	public void setLongitudeColumn(String longitudeColumn) {
		this.longitudeColumn = longitudeColumn;
	}

	public int getNodeSize() {
		return nodeSize;
	}

	public void setNodeSize(int nodeSize) {
		this.nodeSize = nodeSize;
	}
}
