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
package de.bund.bfr.knime.nls.fitting;

import java.util.LinkedHashMap;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.nls.NlsNodeSettings;

public class FittingSettings extends NlsNodeSettings {

	private static final String CFG_EXPERT_SETTINGS = "ExpertSettings";
	private static final String CFG_N_PARAMETER_SPACE = "NParameterSpace";
	private static final String CFG_N_LEVENBERG = "NLevenberg";
	private static final String CFG_STOP_WHEN_SUCCESSFUL = "StopWhenSuccessful";
	private static final String CFG_ENFORCE_LIMITS = "EnforceLimits";
	private static final String CFG_MIN_START_VALUES = "MinStartValues";
	private static final String CFG_MAX_START_VALUES = "MaxStartValues";
	private static final String CFG_START_VALUES = "StartValues";

	private boolean expertSettings;
	private int nParameterSpace;
	private int nLevenberg;
	private boolean stopWhenSuccessful;
	private boolean enforceLimits;
	private Map<String, Double> minStartValues;
	private Map<String, Double> maxStartValues;
	private Map<String, Double> startValues;

	public FittingSettings() {
		expertSettings = false;
		setExpertParametersToDefault();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			expertSettings = settings.getBoolean(CFG_EXPERT_SETTINGS);
		} catch (InvalidSettingsException e) {
		}

		try {
			nParameterSpace = settings.getInt(CFG_N_PARAMETER_SPACE);
		} catch (InvalidSettingsException e) {
		}

		try {
			nLevenberg = settings.getInt(CFG_N_LEVENBERG);
		} catch (InvalidSettingsException e) {
		}

		try {
			stopWhenSuccessful = settings.getBoolean(CFG_STOP_WHEN_SUCCESSFUL);
		} catch (InvalidSettingsException e) {
		}

		try {
			enforceLimits = settings.getBoolean(CFG_ENFORCE_LIMITS);
		} catch (InvalidSettingsException e) {
		}

		try {
			minStartValues = (Map<String, Double>) SERIALIZER.fromXml(settings
					.getString(CFG_MIN_START_VALUES));
		} catch (InvalidSettingsException e) {
		}

		try {
			maxStartValues = (Map<String, Double>) SERIALIZER.fromXml(settings
					.getString(CFG_MAX_START_VALUES));
		} catch (InvalidSettingsException e) {
		}

		try {
			startValues = (Map<String, Double>) SERIALIZER.fromXml(settings
					.getString(CFG_START_VALUES));
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		if (!expertSettings) {
			setExpertParametersToDefault();
		}

		settings.addBoolean(CFG_EXPERT_SETTINGS, expertSettings);
		settings.addInt(CFG_N_PARAMETER_SPACE, nParameterSpace);
		settings.addInt(CFG_N_LEVENBERG, nLevenberg);
		settings.addBoolean(CFG_STOP_WHEN_SUCCESSFUL, stopWhenSuccessful);
		settings.addBoolean(CFG_ENFORCE_LIMITS, enforceLimits);
		settings.addString(CFG_MIN_START_VALUES,
				SERIALIZER.toXml(minStartValues));
		settings.addString(CFG_MAX_START_VALUES,
				SERIALIZER.toXml(maxStartValues));
		settings.addString(CFG_START_VALUES, SERIALIZER.toXml(startValues));
	}

	public boolean isExpertSettings() {
		return expertSettings;
	}

	public void setExpertSettings(boolean expertSettings) {
		this.expertSettings = expertSettings;
	}

	public int getnParameterSpace() {
		return nParameterSpace;
	}

	public void setnParameterSpace(int nParameterSpace) {
		this.nParameterSpace = nParameterSpace;
	}

	public int getnLevenberg() {
		return nLevenberg;
	}

	public void setnLevenberg(int nLevenberg) {
		this.nLevenberg = nLevenberg;
	}

	public boolean isStopWhenSuccessful() {
		return stopWhenSuccessful;
	}

	public void setStopWhenSuccessful(boolean stopWhenSuccessful) {
		this.stopWhenSuccessful = stopWhenSuccessful;
	}

	public boolean isEnforceLimits() {
		return enforceLimits;
	}

	public void setEnforceLimits(boolean enforceLimits) {
		this.enforceLimits = enforceLimits;
	}

	public Map<String, Double> getMinStartValues() {
		return minStartValues;
	}

	public void setMinStartValues(Map<String, Double> minStartValues) {
		this.minStartValues = minStartValues;
	}

	public Map<String, Double> getMaxStartValues() {
		return maxStartValues;
	}

	public void setMaxStartValues(Map<String, Double> maxStartValues) {
		this.maxStartValues = maxStartValues;
	}

	public Map<String, Double> getStartValues() {
		return startValues;
	}

	public void setStartValues(Map<String, Double> startValues) {
		this.startValues = startValues;
	}

	private void setExpertParametersToDefault() {
		nParameterSpace = 10000;
		nLevenberg = 10;
		stopWhenSuccessful = false;
		enforceLimits = false;
		minStartValues = new LinkedHashMap<>();
		maxStartValues = new LinkedHashMap<>();
		startValues = new LinkedHashMap<>();
	}
}
