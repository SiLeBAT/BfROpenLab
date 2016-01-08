/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.function.StepFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;

public class InterpolationFactory {

	public static enum Type {
		STEP("Step Function"), SPLINE("Spline");

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

	public InterpolationFactory(Type type) {
		this.type = type;
	}

	public UnivariateFunction createInterpolationFunction(double[] x, double[] y) {
		switch (type) {
		case STEP:
			return new StepFunction(x, y);
		case SPLINE:
			return new SplineInterpolator().interpolate(x, y);
		}

		return null;
	}
}
