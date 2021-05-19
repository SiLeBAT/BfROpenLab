/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.function.StepFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm;
import org.apache.commons.math3.exception.DimensionMismatchException;

import com.google.common.primitives.Doubles;

public class InterpolationFactory {

	public static enum Type {
		STEP("Step Function"), LINEAR("Linear"), SPLINE("Spline"), LAGRANGE("Lagrange Polynomial");

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

	public UnivariateFunction createInterpolationFunction(List<Double> x, List<Double> y) {
		if (y.size() != x.size()) {
			throw new DimensionMismatchException(y.size(), x.size());
		}

		int n = x.size();
		List<Double> nonNullX = new ArrayList<>(n);
		List<Double> nonNullY = new ArrayList<>(n);

		for (int i = 0; i < n; i++) {
			if (x.get(i) != null && y.get(i) != null) {
				nonNullX.add(x.get(i));
				nonNullY.add(y.get(i));
			}
		}

		switch (type) {
		case STEP:
			return new StepFunction(Doubles.toArray(nonNullX), Doubles.toArray(nonNullY));
		case LINEAR:
			return new LinearFunction(Doubles.toArray(nonNullX), Doubles.toArray(nonNullY));
		case SPLINE:
			return new SplineInterpolator().interpolate(Doubles.toArray(nonNullX), Doubles.toArray(nonNullY));
		case LAGRANGE:
			return new PolynomialFunctionLagrangeForm(Doubles.toArray(nonNullX), Doubles.toArray(nonNullY));
		default:
			throw new RuntimeException("Unknown type of InterpolationFactory: " + type);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		InterpolationFactory other = (InterpolationFactory) obj;

		return type == other.type;
	}
}
