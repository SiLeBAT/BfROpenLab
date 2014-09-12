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
package de.bund.bfr.knime.nls.difffitting;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.nls.FittingSettings;
import de.bund.bfr.math.Integrator;

public class DiffFunctionFittingSettings extends FittingSettings {

	private static final Integrator.Type DEFAULT_INTEGRATOR_TYPE = Integrator.Type.RUNGE_KUTTA;
	private static final double DEFAULT_STEP_SIZE = 0.01;
	private static final double DEFAULT_MIN_STEP_SIZE = 0.0;
	private static final double DEFAULT_MAX_STEP_SIZE = 0.1;
	private static final double DEFAULT_ABS_TOLERANCE = 1e-6;
	private static final double DEFAULT_REL_TOLERANCE = 0.0;

	private static final String CFG_INTEGRATOR_TYPE = "IntegratorType";
	private static final String CFG_STEP_SIZE = "StepSize";
	private static final String CFG_MIN_STEP_SIZE = "MinStepSize";
	private static final String CFG_MAX_STEP_SIZE = "MaxStepSize";
	private static final String CFG_ABS_TOLERANCE = "AbsTolerance";
	private static final String CFG_REL_TOLERANCE = "RelTolerance";

	private Integrator.Type integratorType;
	private double stepSize;
	private double minStepSize;
	private double maxStepSize;
	private double absTolerance;
	private double relTolerance;

	public DiffFunctionFittingSettings() {
		setExpertParametersToDefault();
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		super.loadSettings(settings);

		try {
			integratorType = Integrator.Type.valueOf(settings
					.getString(CFG_INTEGRATOR_TYPE));
		} catch (InvalidSettingsException e) {
		}

		try {
			stepSize = settings.getDouble(CFG_STEP_SIZE);
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
		super.saveSettings(settings);
		
		if (!isExpertSettings()) {
			setExpertParametersToDefault();
		}

		settings.addString(CFG_INTEGRATOR_TYPE, integratorType.name());
		settings.addDouble(CFG_STEP_SIZE, stepSize);
		settings.addDouble(CFG_MIN_STEP_SIZE, minStepSize);
		settings.addDouble(CFG_MAX_STEP_SIZE, maxStepSize);
		settings.addDouble(CFG_ABS_TOLERANCE, absTolerance);
		settings.addDouble(CFG_REL_TOLERANCE, relTolerance);
	}

	public Integrator.Type getIntegratorType() {
		return integratorType;
	}

	public void setIntegratorType(Integrator.Type integratorType) {
		this.integratorType = integratorType;
	}

	public double getStepSize() {
		return stepSize;
	}

	public void setStepSize(double stepSize) {
		this.stepSize = stepSize;
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
	
	private void setExpertParametersToDefault() {
		integratorType = DEFAULT_INTEGRATOR_TYPE;
		stepSize = DEFAULT_STEP_SIZE;
		minStepSize = DEFAULT_MIN_STEP_SIZE;
		maxStepSize = DEFAULT_MAX_STEP_SIZE;
		absTolerance = DEFAULT_ABS_TOLERANCE;
		relTolerance = DEFAULT_REL_TOLERANCE;
	}
}
