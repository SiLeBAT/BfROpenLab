/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

import com.google.common.primitives.Doubles;

public class DiffFunction implements FirstOrderDifferentialEquations {

	private Parser parser;
	private List<Node> functions;
	private List<String> dependentVariables;
	private String timeVariable;

	private Map<String, UnivariateFunction> variableFunctions;

	private int nFunctions;

	public DiffFunction(Parser parser, List<Node> functions, List<String> dependentVariables,
			Map<String, List<Double>> variableValues, String timeVariable, InterpolationFactory interpolator) {
		this.parser = parser;
		this.functions = functions;
		this.dependentVariables = dependentVariables;
		this.timeVariable = timeVariable;

		variableFunctions = new LinkedHashMap<>();
		variableValues.forEach((var, values) -> {
			if (!var.equals(timeVariable)) {
				variableFunctions.put(var, interpolator.createInterpolationFunction(
						Doubles.toArray(variableValues.get(timeVariable)), Doubles.toArray(values)));
			}
		});

		nFunctions = functions.size();
	}

	@Override
	public void computeDerivatives(double t, double[] y, double[] yDot)
			throws MaxCountExceededException, DimensionMismatchException {
		parser.setVarValue(timeVariable, t);
		variableFunctions.forEach((var, function) -> parser.setVarValue(var, function.value(t)));

		for (int i = 0; i < nFunctions; i++) {
			parser.setVarValue(dependentVariables.get(i), y[i]);
		}

		for (int i = 0; i < nFunctions; i++) {
			try {
				double value = parser.evaluate(functions.get(i));

				yDot[i] = Double.isFinite(value) ? value : Double.NaN;
			} catch (ParseException e) {
				e.printStackTrace();
				yDot[i] = Double.NaN;
			}
		}
	}

	@Override
	public int getDimension() {
		return nFunctions;
	}
}
