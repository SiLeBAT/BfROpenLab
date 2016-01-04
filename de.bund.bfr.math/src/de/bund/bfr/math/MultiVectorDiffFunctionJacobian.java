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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class MultiVectorDiffFunctionJacobian implements MultivariateMatrixFunction {

	private MultiVectorDiffFunction[] diffFunctions;
	private int nParams;
	private int nValues;

	public MultiVectorDiffFunctionJacobian(String[] formulas, String[] dependentVariables, double[] initValues,
			List<String[]> initParameters, String[] parameters, Map<String, List<double[]>> variableValues,
			List<double[]> timeValues, String dependentVariable, String timeVariable, IntegratorFactory integrator)
					throws ParseException {
		nParams = parameters.length;
		nValues = 0;

		for (double[] t : timeValues) {
			nValues += t.length;
		}

		Set<String> variables = new LinkedHashSet<>();

		variables.addAll(Arrays.asList(dependentVariables));
		variables.addAll(variableValues.keySet());
		variables.addAll(Arrays.asList(parameters));

		diffFunctions = new MultiVectorDiffFunction[nParams];

		for (int i = 0; i < nParams; i++) {
			Parser parser = new Parser(variables);
			Node[] functions = new Node[formulas.length];

			for (int j = 0; j < formulas.length; j++) {
				functions[j] = parser.parse(formulas[j]);
			}

			diffFunctions[i] = new MultiVectorDiffFunction(parser, functions, dependentVariables, initValues,
					initParameters, parameters, variableValues, timeValues,
					Arrays.asList(dependentVariables).indexOf(dependentVariable), timeVariable, integrator);
		}
	}

	@Override
	public double[][] value(double[] point) throws IllegalArgumentException {
		return MathUtils.aproxJacobianParallel(diffFunctions, point, nParams, nValues);
	}
}
