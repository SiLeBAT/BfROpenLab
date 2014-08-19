/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
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
package de.bund.bfr.knime.nls.functionfitting;

import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.nls.NlsNodeSettings;
import de.bund.bfr.math.Integrator;

public class FunctionFittingSettings extends NlsNodeSettings {

	public static final boolean DEFAULT_EXPERT_SETTINGS = false;
	public static final int DEFAULT_N_PARAMETER_SPACE = 10000;
	public static final int DEFAULT_N_LEVENBERG = 10;
	public static final boolean DEFAULT_STOP_WHEN_SUCCESSFUL = false;
	public static final boolean DEFAULT_ENFORCE_LIMITS = false;

	public static final Integrator.Type DEFAULT_INTEGRATOR_TYPE = Integrator.Type.RUNGE_KUTTA;
	public static final double DEFAULT_MIN_STEP_SIZE = 0.01;
	public static final double DEFAULT_MAX_STEP_SIZE = 0.1;
	public static final double DEFAULT_ABS_TOLERANCE = 1e-6;
	public static final double DEFAULT_REL_TOLERANCE = 0.0;

	private static final String CFG_EXPERT_SETTINGS = "ExpertSettings";
	private static final String CFG_N_PARAMETER_SPACE = "NParameterSpace";
	private static final String CFG_N_LEVENBERG = "NLevenberg";
	private static final String CFG_STOP_WHEN_SUCCESSFUL = "StopWhenSuccessful";
	private static final String CFG_ENFORCE_LIMITS = "EnforceLimits";
	private static final String CFG_PARAMETER_GUESSES = "ParameterGuesses";

	private static final String CFG_INTEGRATOR_TYPE = "IntegratorType";
	private static final String CFG_MIN_STEP_SIZE = "MinStepSize";
	private static final String CFG_MAX_STEP_SIZE = "MaxStepSize";
	private static final String CFG_ABS_TOLERANCE = "AbsTolerance";
	private static final String CFG_REL_TOLERANCE = "RelTolerance";

	private boolean expertSettings;
	private int nParameterSpace;
	private int nLevenberg;
	private boolean stopWhenSuccessful;
	private boolean enforceLimits;
	private Map<String, Point2D.Double> parameterGuesses;

	private Integrator.Type integratorType;
	private double minStepSize;
	private double maxStepSize;
	private double absTolerance;
	private double relTolerance;

	public FunctionFittingSettings() {
		expertSettings = DEFAULT_EXPERT_SETTINGS;
		nParameterSpace = DEFAULT_N_PARAMETER_SPACE;
		nLevenberg = DEFAULT_N_LEVENBERG;
		stopWhenSuccessful = DEFAULT_STOP_WHEN_SUCCESSFUL;
		enforceLimits = DEFAULT_ENFORCE_LIMITS;
		parameterGuesses = new LinkedHashMap<>();
		integratorType = DEFAULT_INTEGRATOR_TYPE;
		minStepSize = DEFAULT_MIN_STEP_SIZE;
		maxStepSize = DEFAULT_MAX_STEP_SIZE;
		absTolerance = DEFAULT_ABS_TOLERANCE;
		relTolerance = DEFAULT_REL_TOLERANCE;
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
			parameterGuesses = (Map<String, Point2D.Double>) SERIALIZER
					.fromXml(settings.getString(CFG_PARAMETER_GUESSES));
		} catch (InvalidSettingsException e) {
		}

		try {
			integratorType = Integrator.Type.valueOf(settings
					.getString(CFG_INTEGRATOR_TYPE));
		} catch (InvalidSettingsException e) {
		}

		try {
			minStepSize = settings.getDouble(CFG_MIN_STEP_SIZE);
		} catch (InvalidSettingsException e) {
		}

		try {
			maxStepSize = settings.getDouble(CFG_MAX_STEP_SIZE);
		} catch (InvalidSettingsException e) {
		}

		try {
			absTolerance = settings.getDouble(CFG_ABS_TOLERANCE);
		} catch (InvalidSettingsException e) {
		}

		try {
			relTolerance = settings.getDouble(CFG_REL_TOLERANCE);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addBoolean(CFG_EXPERT_SETTINGS, expertSettings);
		settings.addInt(CFG_N_PARAMETER_SPACE, nParameterSpace);
		settings.addInt(CFG_N_LEVENBERG, nLevenberg);
		settings.addBoolean(CFG_STOP_WHEN_SUCCESSFUL, stopWhenSuccessful);
		settings.addBoolean(CFG_ENFORCE_LIMITS, enforceLimits);
		settings.addString(CFG_PARAMETER_GUESSES,
				SERIALIZER.toXml(parameterGuesses));

		settings.addString(CFG_INTEGRATOR_TYPE, integratorType.name());
		settings.addDouble(CFG_MIN_STEP_SIZE, minStepSize);
		settings.addDouble(CFG_MAX_STEP_SIZE, maxStepSize);
		settings.addDouble(CFG_ABS_TOLERANCE, absTolerance);
		settings.addDouble(CFG_REL_TOLERANCE, relTolerance);
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

	public Map<String, Point2D.Double> getParameterGuesses() {
		return parameterGuesses;
	}

	public void setParameterGuesses(Map<String, Point2D.Double> parameterGuesses) {
		this.parameterGuesses = parameterGuesses;
	}

	public Integrator.Type getIntegratorType() {
		return integratorType;
	}

	public void setIntegratorType(Integrator.Type integratorType) {
		this.integratorType = integratorType;
	}

	public double getMinStepSize() {
		return minStepSize;
	}

	public void setMinStepSize(double minStepSize) {
		this.minStepSize = minStepSize;
	}

	public double getMaxStepSize() {
		return maxStepSize;
	}

	public void setMaxStepSize(double maxStepSize) {
		this.maxStepSize = maxStepSize;
	}

	public double getAbsTolerance() {
		return absTolerance;
	}

	public void setAbsTolerance(double absTolerance) {
		this.absTolerance = absTolerance;
	}

	public double getRelTolerance() {
		return relTolerance;
	}

	public void setRelTolerance(double relTolerance) {
		this.relTolerance = relTolerance;
	}
}
