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
package de.bund.bfr.knime.openkrise.views.gisgraphview;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.openkrise.views.gisview.GisSettings;
import de.bund.bfr.knime.openkrise.views.gisview.ViewSettings;

public class GisGraphViewSettings extends ViewSettings {

	private GraphSettings graphSettings;
	private GisSettings gisSettings;

	public GisGraphViewSettings() {
		graphSettings = new GraphSettings();
		gisSettings = new GisSettings();
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

	public GisSettings getGisSettings() {
		return gisSettings;
	}
}
