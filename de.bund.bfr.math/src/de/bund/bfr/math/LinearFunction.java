/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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

import java.util.Arrays;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.util.MathArrays;

public class LinearFunction implements UnivariateFunction {

	private final double[] abscissa;
	private final double[] ordinate;

	public LinearFunction(double[] x, double[] y)
			throws NullArgumentException, NoDataException, DimensionMismatchException, NonMonotonicSequenceException {
		if (x == null || y == null) {
			throw new NullArgumentException();
		}
		if (x.length == 0 || y.length == 0) {
			throw new NoDataException();
		}
		if (y.length != x.length) {
			throw new DimensionMismatchException(y.length, x.length);
		}
		MathArrays.checkOrder(x);

		abscissa = MathArrays.copyOf(x);
		ordinate = MathArrays.copyOf(y);
	}

	@Override
	public double value(double x) {
		int index = Arrays.binarySearch(abscissa, x);
		double fx = 0;

		if (-index - 1 == abscissa.length) {
			// "x" is larger than the last value in "abscissa".
			fx = ordinate[-index - 2];
		} else if (index < -1) {
			// "x" is between "abscissa[-index-2]" and "abscissa[-index-1]".
			double alpha = (x - abscissa[-index - 2]) / (abscissa[-index - 1] - abscissa[-index - 2]);
			fx = alpha * ordinate[-index - 1] + (1 - alpha) * ordinate[-index - 2];
		} else if (index >= 0) {
			// "x" is exactly "abscissa[index]".
			fx = ordinate[index];
		} else {
			// Otherwise, "x" is smaller than the first value in "abscissa"
			// (hence the returned value should be "ordinate[0]").
			fx = ordinate[0];
		}

		return fx;
	}
}
