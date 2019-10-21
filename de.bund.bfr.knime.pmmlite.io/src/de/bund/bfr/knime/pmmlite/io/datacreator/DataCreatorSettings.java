/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.io.datacreator;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.pmmlite.core.EmfUtils;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;

public class DataCreatorSettings {

	private static final String CFG_TIME_SERIES = "TimeSeries";

	private TimeSeries timeSeries;

	public DataCreatorSettings() {
		timeSeries = null;
	}

	public void load(NodeSettingsRO settings) {
		try {
			timeSeries = EmfUtils.fromXml(settings.getString(CFG_TIME_SERIES), TimeSeries.class);
		} catch (InvalidSettingsException e) {
		}
	}

	public void save(NodeSettingsWO settings) {
		settings.addString(CFG_TIME_SERIES, EmfUtils.toXml(timeSeries));
	}

	public TimeSeries getTimeSeries() {
		return timeSeries;
	}

	public void setTimeSeries(TimeSeries timeSeries) {
		this.timeSeries = timeSeries;
	}
}
