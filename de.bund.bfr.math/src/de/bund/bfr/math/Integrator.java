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
package de.bund.bfr.math;

import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince54Integrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;

public class Integrator {

	public static enum Type {
		RUNGE_KUTTA, DORMAND_PRINCE_54, DORMAND_PRINCE_853;

		@Override
		public String toString() {
			switch (this) {
			case RUNGE_KUTTA:
				return "Classical Runge Kutta";
			case DORMAND_PRINCE_54:
				return "Dormand Prince 54";
			case DORMAND_PRINCE_853:
				return "Dormand Prince 853";
			}

			return super.toString();
		}
	}

	private Type type;
	private double minStep;
	private double maxStep;
	private double absTolerance;
	private double relTolerance;

	public Integrator(Type type, double minStep, double maxStep,
			double absTolerance, double relTolerance) {
		this.type = type;
		this.minStep = minStep;
		this.maxStep = maxStep;
		this.absTolerance = absTolerance;
		this.relTolerance = relTolerance;
	}

	public FirstOrderIntegrator createIntegrator() {
		switch (type) {
		case RUNGE_KUTTA:
			return new ClassicalRungeKuttaIntegrator(minStep);
		case DORMAND_PRINCE_54:
			return new DormandPrince54Integrator(minStep, maxStep,
					absTolerance, relTolerance);
		case DORMAND_PRINCE_853:
			return new DormandPrince853Integrator(minStep, maxStep,
					absTolerance, relTolerance);
		}

		return null;
	}

	@Override
	public String toString() {
		return "Integrator [type=" + type + ", minStep=" + minStep
				+ ", maxStep=" + maxStep + ", absTolerance=" + absTolerance
				+ ", relTolerance=" + relTolerance + "]";
	}
}
