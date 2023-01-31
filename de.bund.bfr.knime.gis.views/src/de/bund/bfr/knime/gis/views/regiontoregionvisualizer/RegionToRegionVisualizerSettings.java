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
package de.bund.bfr.knime.gis.views.regiontoregionvisualizer;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.gis.views.GraphSettings;
import de.bund.bfr.knime.gis.views.RegionSettings;
import de.bund.bfr.knime.gis.views.ViewSettings;

public class RegionToRegionVisualizerSettings extends ViewSettings {

	private GraphSettings graphSettings;
	private RegionSettings gisSettings;

	public RegionToRegionVisualizerSettings() {
		graphSettings = new GraphSettings();
		gisSettings = new RegionSettings();
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		super.loadSettings(settings);
		graphSettings.loadSettings(settings);
		gisSettings.loadSettings(settings);
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		super.saveSettings(settings);
		graphSettings.saveSettings(settings);
		gisSettings.saveSettings(settings);
	}

	public GraphSettings getGraphSettings() {
		return graphSettings;
	}

	public RegionSettings getGisSettings() {
		return gisSettings;
	}
}
