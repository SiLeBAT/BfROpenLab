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
package de.bund.bfr.knime.pmmlite.util.fitting;

import java.util.LinkedHashMap;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.pmmlite.core.XmlUtils;

public class ModelFittingSettings {

	public static enum FittingType {
		PRIMARY_FITTING("Primary Fitting"), SECONDARY_FITTING("Secondary Fitting"), TERTIARY_FITTING(
				"Tertiary Fitting");

		private String name;

		private FittingType(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private static final boolean DEFAULT_ENFORCE_LIMITS = false;
	private static final boolean DEFAULT_EXPERT_SETTINGS = false;
	private static final int DEFAULT_N_PARAMETER_SPACE = 10000;
	private static final int DEFAULT_N_LEVENBERG = 10;
	private static final boolean DEFAULT_STOP_WHEN_SUCCESSFUL = false;

	private static final String CFG_FITTING_TYPE = "FittingType";
	private static final String CFG_ENFORCE_LIMITS = "EnforceLimits";
	private static final String CFG_EXPERT_SETTINGS = "ExpertSettings";
	private static final String CFG_N_PARAMETER_SPACE = "NParameterSpace";
	private static final String CFG_N_LEVENBERG = "NLevenberg";
	private static final String CFG_STOP_WHEN_SUCCESSFUL = "StopWhenSuccessful";
	private static final String CFG_MIN_START_VALUES = "MinStartValues";
	private static final String CFG_MAX_START_VALUES = "MaxStartValues";

	private FittingType fittingType;
	private boolean enforceLimits;
	private boolean expertSettings;
	private int nParameterSpace;
	private int nLevenberg;
	private boolean stopWhenSuccessful;
	private Map<String, Map<String, Double>> minStartValues;
	private Map<String, Map<String, Double>> maxStartValues;

	public ModelFittingSettings() {
		fittingType = null;
		enforceLimits = DEFAULT_ENFORCE_LIMITS;
		expertSettings = DEFAULT_EXPERT_SETTINGS;
		setExpertParametersToDefault();
	}

	@SuppressWarnings("unchecked")
	public void loadSettings(NodeSettingsRO settings) {
		try {
			fittingType = FittingType.valueOf(settings.getString(CFG_FITTING_TYPE));
		} catch (NullPointerException e) {
			fittingType = null;
		} catch (InvalidSettingsException e) {
		}

		try {
			enforceLimits = settings.getBoolean(CFG_ENFORCE_LIMITS);
		} catch (InvalidSettingsException e) {
		}

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
			minStartValues = (Map<String, Map<String, Double>>) XmlUtils
					.fromXml(settings.getString(CFG_MIN_START_VALUES));
		} catch (InvalidSettingsException e) {
		}

		try {
			maxStartValues = (Map<String, Map<String, Double>>) XmlUtils
					.fromXml(settings.getString(CFG_MAX_START_VALUES));
		} catch (InvalidSettingsException e) {
		}
	}

	public void saveSettings(NodeSettingsWO settings) {
		if (!expertSettings) {
			setExpertParametersToDefault();
		}

		settings.addString(CFG_FITTING_TYPE, fittingType != null ? fittingType.name() : null);
		settings.addBoolean(CFG_ENFORCE_LIMITS, enforceLimits);
		settings.addBoolean(CFG_EXPERT_SETTINGS, expertSettings);
		settings.addInt(CFG_N_PARAMETER_SPACE, nParameterSpace);
		settings.addInt(CFG_N_LEVENBERG, nLevenberg);
		settings.addBoolean(CFG_STOP_WHEN_SUCCESSFUL, stopWhenSuccessful);
		settings.addString(CFG_MIN_START_VALUES, XmlUtils.toXml(minStartValues));
		settings.addString(CFG_MAX_START_VALUES, XmlUtils.toXml(maxStartValues));
	}

	public FittingType getFittingType() {
		return fittingType;
	}

	public void setFittingType(FittingType fittingType) {
		this.fittingType = fittingType;
	}

	public boolean isEnforceLimits() {
		return enforceLimits;
	}

	public void setEnforceLimits(boolean enforceLimits) {
		this.enforceLimits = enforceLimits;
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

	public Map<String, Map<String, Double>> getMinStartValues() {
		return minStartValues;
	}

	public void setMinStartValues(Map<String, Map<String, Double>> minStartValues) {
		this.minStartValues = minStartValues;
	}

	public Map<String, Map<String, Double>> getMaxStartValues() {
		return maxStartValues;
	}

	public void setMaxStartValues(Map<String, Map<String, Double>> maxStartValues) {
		this.maxStartValues = maxStartValues;
	}

	private void setExpertParametersToDefault() {
		nParameterSpace = DEFAULT_N_PARAMETER_SPACE;
		nLevenberg = DEFAULT_N_LEVENBERG;
		stopWhenSuccessful = DEFAULT_STOP_WHEN_SUCCESSFUL;
		minStartValues = new LinkedHashMap<>();
		maxStartValues = new LinkedHashMap<>();
	}
}
