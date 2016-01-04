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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class VectorDiffFunctionJacobian implements MultivariateMatrixFunction {

	private VectorDiffFunction[] diffFunctions;
	private int nParams;
	private int nValues;

	public VectorDiffFunctionJacobian(String[] formulas, String[] dependentVariables, double[] initValues,
			String[] initParameters, String[] parameters, Map<String, double[]> variableValues, double[] timeValues,
			String dependentVariable, String timeVariable, IntegratorFactory integrator) throws ParseException {
		nParams = parameters.length;
		nValues = timeValues.length;
		diffFunctions = new VectorDiffFunction[nParams];

		for (int ip = 0; ip < nParams; ip++) {
			Parser parser = new Parser(
					Stream.concat(Stream.concat(Stream.of(dependentVariables), Stream.of(parameters)),
							variableValues.keySet().stream()).collect(Collectors.toSet()));
			List<Node> functions = new ArrayList<>();

			for (String f : formulas) {
				functions.add(parser.parse(f));
			}

			diffFunctions[ip] = new VectorDiffFunction(parser, functions.toArray(new Node[0]), dependentVariables,
					initValues, initParameters, parameters, variableValues, timeValues,
					Arrays.asList(dependentVariables).indexOf(dependentVariable), timeVariable, integrator);
		}
	}

	@Override
	public double[][] value(double[] point) throws IllegalArgumentException {
		return MathUtils.aproxJacobianParallel(diffFunctions, point, nParams, nValues);
	}
}
