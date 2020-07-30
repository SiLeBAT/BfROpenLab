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
package de.bund.bfr.knime.nls.fitting;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.nls.NlsNodeSettings;
import de.bund.bfr.math.InterpolationFactory;

public class FittingSettings extends NlsNodeSettings {

	private static final String CFG_LEVEL_OF_DETECTION = "LevelOfDetection";
	private static final String CFG_FIT_ALL_AT_ONCE = "FitAllAtOnce";
	private static final String CFG_INIT_VALUES_WITH_DIFFERENT_START = "InitValuesWithDifferentStart";
	private static final String CFG_EXPERT_SETTINGS = "ExpertSettings";
	private static final String CFG_N_PARAMETER_SPACE = "NParameterSpace";
	private static final String CFG_N_LEVENBERG = "NLevenberg";
	private static final String CFG_STOP_WHEN_SUCCESSFUL = "StopWhenSuccessful";
	private static final String CFG_MAX_LEVENBERG_ITERATIONS = "MaxLevenbergIterations";
	private static final String CFG_ENFORCE_LIMITS = "EnforceLimits";
	private static final String CFG_MIN_START_VALUES = "MinStartValues";
	private static final String CFG_MAX_START_VALUES = "MaxStartValues";
	private static final String CFG_START_VALUES = "StartValues";
	private static final String CFG_STEP_SIZE = "StepSize";
	private static final String CFG_INTERPOLATOR = "Interpolator";

	private Double levelOfDetection;
	private boolean fitAllAtOnce;
	private Set<String> initValuesWithDifferentStart;
	private boolean expertSettings;
	private int nParameterSpace;
	private int nLevenberg;
	private boolean stopWhenSuccessful;
	private int maxLevenbergIterations;
	private boolean enforceLimits;
	private Map<String, Double> minStartValues;
	private Map<String, Double> maxStartValues;
	private Map<String, Double> startValues;
	private double stepSize;
	private InterpolationFactory.Type interpolator;

	public FittingSettings() {
		levelOfDetection = null;
		fitAllAtOnce = false;
		initValuesWithDifferentStart = new LinkedHashSet<>();
		expertSettings = false;
		setExpertParametersToDefault();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			levelOfDetection = nanToNull(settings.getDouble(CFG_LEVEL_OF_DETECTION));
		} catch (InvalidSettingsException e) {
		}

		try {
			fitAllAtOnce = settings.getBoolean(CFG_FIT_ALL_AT_ONCE);
		} catch (InvalidSettingsException e) {
		}

		try {
			initValuesWithDifferentStart = (Set<String>) SERIALIZER
					.fromXml(settings.getString(CFG_INIT_VALUES_WITH_DIFFERENT_START));
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
			maxLevenbergIterations = settings.getInt(CFG_MAX_LEVENBERG_ITERATIONS);
		} catch (InvalidSettingsException e) {
		}

		try {
			enforceLimits = settings.getBoolean(CFG_ENFORCE_LIMITS);
		} catch (InvalidSettingsException e) {
		}

		try {
			minStartValues = (Map<String, Double>) SERIALIZER.fromXml(settings.getString(CFG_MIN_START_VALUES));
		} catch (InvalidSettingsException e) {
		}

		try {
			maxStartValues = (Map<String, Double>) SERIALIZER.fromXml(settings.getString(CFG_MAX_START_VALUES));
		} catch (InvalidSettingsException e) {
		}

		try {
			startValues = (Map<String, Double>) SERIALIZER.fromXml(settings.getString(CFG_START_VALUES));
		} catch (InvalidSettingsException e) {
		}

		try {
			stepSize = settings.getDouble(CFG_STEP_SIZE);
		} catch (InvalidSettingsException e) {
		}

		try {
			interpolator = InterpolationFactory.Type.valueOf(settings.getString(CFG_INTERPOLATOR));
		} catch (InvalidSettingsException | IllegalArgumentException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		if (!expertSettings) {
			setExpertParametersToDefault();
		}

		settings.addDouble(CFG_LEVEL_OF_DETECTION, nullToNan(levelOfDetection));
		settings.addBoolean(CFG_FIT_ALL_AT_ONCE, fitAllAtOnce);
		settings.addString(CFG_INIT_VALUES_WITH_DIFFERENT_START, SERIALIZER.toXml(initValuesWithDifferentStart));
		settings.addBoolean(CFG_EXPERT_SETTINGS, expertSettings);
		settings.addInt(CFG_N_PARAMETER_SPACE, nParameterSpace);
		settings.addInt(CFG_N_LEVENBERG, nLevenberg);
		settings.addBoolean(CFG_STOP_WHEN_SUCCESSFUL, stopWhenSuccessful);
		settings.addInt(CFG_MAX_LEVENBERG_ITERATIONS, maxLevenbergIterations);
		settings.addBoolean(CFG_ENFORCE_LIMITS, enforceLimits);
		settings.addString(CFG_MIN_START_VALUES, SERIALIZER.toXml(minStartValues));
		settings.addString(CFG_MAX_START_VALUES, SERIALIZER.toXml(maxStartValues));
		settings.addString(CFG_START_VALUES, SERIALIZER.toXml(startValues));
		settings.addDouble(CFG_STEP_SIZE, stepSize);
		settings.addString(CFG_INTERPOLATOR, interpolator.name());
	}

	public Double getLevelOfDetection() {
		return levelOfDetection;
	}

	public void setLevelOfDetection(Double levelOfDetection) {
		this.levelOfDetection = levelOfDetection;
	}

	public boolean isFitAllAtOnce() {
		return fitAllAtOnce;
	}

	public void setFitAllAtOnce(boolean fitAllAtOnce) {
		this.fitAllAtOnce = fitAllAtOnce;
	}

	public Set<String> getInitValuesWithDifferentStart() {
		return initValuesWithDifferentStart;
	}

	public void setInitValuesWithDifferentStart(Set<String> initValuesWithDifferentStart) {
		this.initValuesWithDifferentStart = initValuesWithDifferentStart;
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

	public int getMaxLevenbergIterations() {
		return maxLevenbergIterations;
	}

	public void setMaxLevenbergIterations(int maxLevenbergIterations) {
		this.maxLevenbergIterations = maxLevenbergIterations;
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

	public double getStepSize() {
		return stepSize;
	}

	public void setStepSize(double stepSize) {
		this.stepSize = stepSize;
	}

	public InterpolationFactory.Type getInterpolator() {
		return interpolator;
	}

	public void setInterpolator(InterpolationFactory.Type interpolator) {
		this.interpolator = interpolator;
	}

	private void setExpertParametersToDefault() {
		nParameterSpace = 10000;
		nLevenberg = 10;
		stopWhenSuccessful = false;
		maxLevenbergIterations = 100;
		enforceLimits = false;
		minStartValues = new LinkedHashMap<>();
		maxStartValues = new LinkedHashMap<>();
		startValues = new LinkedHashMap<>();
		stepSize = 0.01;
		interpolator = InterpolationFactory.Type.STEP;
	}
}
