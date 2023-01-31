/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.gis.views.locationvisualizer;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.gis.views.LocationSettings;
import de.bund.bfr.knime.gis.views.ViewSettings;

public class LocationVisualizerSettings extends ViewSettings {

	private LocationSettings gisSettings;

	public LocationVisualizerSettings() {
		gisSettings = new LocationSettings();
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		super.loadSettings(settings);
		gisSettings.loadSettings(settings);
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		super.saveSettings(settings);
		gisSettings.saveSettings(settings);
	}

	public LocationSettings getGisSettings() {
		return gisSettings;
	}
}
