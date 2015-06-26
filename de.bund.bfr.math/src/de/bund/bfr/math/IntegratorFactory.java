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
package de.bund.bfr.math;

import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.apache.commons.math3.ode.nonstiff.EulerIntegrator;
import org.apache.commons.math3.ode.nonstiff.GillIntegrator;
import org.apache.commons.math3.ode.nonstiff.MidpointIntegrator;
import org.apache.commons.math3.ode.nonstiff.ThreeEighthesIntegrator;

public class IntegratorFactory {

	public static enum Type {
		RUNGE_KUTTA("Classical Runge Kutta"), EULER("Euler"), GILL("Gill"), MIDPOINT("Midpoint"), THREE_EIGHTHES("3/8");

		private String name;

		private Type(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private Type type;
	private double step;

	public IntegratorFactory(Type type, double step) {
		this.type = type;
		this.step = step;
	}

	public FirstOrderIntegrator createIntegrator() {
		switch (type) {
		case EULER:
			return new EulerIntegrator(step);
		case GILL:
			return new GillIntegrator(step);
		case MIDPOINT:
			return new MidpointIntegrator(step);
		case RUNGE_KUTTA:
			return new ClassicalRungeKuttaIntegrator(step);
		case THREE_EIGHTHES:
			return new ThreeEighthesIntegrator(step);
		}

		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp = Double.doubleToLongBits(step);

		result = prime * result + type.hashCode();
		result = prime * result + (int) (temp ^ (temp >>> 32));

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IntegratorFactory)) {
			return false;
		}

		IntegratorFactory factory = (IntegratorFactory) obj;

		return type == factory.type && step == factory.step;
	}
}
