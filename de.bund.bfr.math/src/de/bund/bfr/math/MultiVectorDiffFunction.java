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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class MultiVectorDiffFunction implements ValueAndJacobianFunction {

	private String[] formulas;
	private String[] dependentVariables;
	private double[] initValues;
	private List<String[]> initParameters;
	private String[] parameters;
	private Map<String, List<double[]>> variableValues;
	private List<double[]> timeValues;
	private String dependentVariable;
	private String timeVariable;
	private IntegratorFactory integrator;
	private InterpolationFactory interpolator;

	private int nParams;
	private int nValues;
	private int nTerms;
	private int dependentIndex;
	private Parser parser;
	private Node[] functions;

	public MultiVectorDiffFunction(String[] formulas, String[] dependentVariables, double[] initValues,
			List<String[]> initParameters, String[] parameters, Map<String, List<double[]>> variableValues,
			List<double[]> timeValues, String dependentVariable, String timeVariable, IntegratorFactory integrator,
			InterpolationFactory interpolator) throws ParseException {
		this.formulas = formulas;
		this.dependentVariables = dependentVariables;
		this.initValues = initValues;
		this.initParameters = initParameters;
		this.parameters = parameters;
		this.variableValues = variableValues;
		this.timeValues = timeValues;
		this.dependentVariable = dependentVariable;
		this.timeVariable = timeVariable;
		this.integrator = integrator;
		this.interpolator = interpolator;

		nParams = parameters.length;
		nValues = timeValues.stream().mapToInt(t -> t.length).sum();
		nTerms = formulas.length;
		dependentIndex = Arrays.asList(dependentVariables).indexOf(dependentVariable);
		parser = new Parser(Stream.concat(Stream.concat(Stream.of(dependentVariables), Stream.of(parameters)),
				variableValues.keySet().stream()).collect(Collectors.toSet()));
		functions = new Node[nTerms];

		for (int it = 0; it < nTerms; it++) {
			functions[it] = parser.parse(formulas[it]);
		}
	}

	@Override
	public double[] value(double[] point) throws IllegalArgumentException {
		for (int ip = 0; ip < nParams; ip++) {
			if (!Arrays.asList(initParameters).contains(parameters[ip])) {
				parser.setVarValue(parameters[ip], point[ip]);
			}
		}

		FirstOrderIntegrator integratorInstance = integrator.createIntegrator();
		double[] result = new double[nValues];
		int index = 0;

		for (int i = 0; i < timeValues.size(); i++) {
			Map<String, double[]> vv = new LinkedHashMap<>();

			for (Map.Entry<String, List<double[]>> entry : variableValues.entrySet()) {
				vv.put(entry.getKey(), entry.getValue().get(i));
			}

			DiffFunction f = new DiffFunction(parser, functions, dependentVariables, vv, timeVariable, interpolator);
			double[] values = new double[nTerms];

			for (int it = 0; it < nTerms; it++) {
				if (Double.isFinite(initValues[it])) {
					values[it] = initValues[it];
				} else {
					values[it] = point[Arrays.asList(parameters).indexOf(initParameters.get(i)[it])];
				}
			}

			result[index++] = values[dependentIndex];

			for (int j = 1; j < timeValues.get(i).length; j++) {
				integratorInstance.integrate(f, timeValues.get(i)[j - 1], values, timeValues.get(i)[j], values);
				result[index++] = values[dependentIndex];
			}
		}

		return result;
	}

	@Override
	public MultivariateMatrixFunction createJacobian() {
		MultiVectorDiffFunction[] diffFunctions = new MultiVectorDiffFunction[nParams];

		for (int ip = 0; ip < nParams; ip++) {
			try {
				diffFunctions[ip] = new MultiVectorDiffFunction(formulas, dependentVariables, initValues,
						initParameters, parameters, variableValues, timeValues, dependentVariable, timeVariable,
						integrator, interpolator);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		return point -> MathUtils.aproxJacobianParallel(diffFunctions, point, nParams, nValues);
	}
}
