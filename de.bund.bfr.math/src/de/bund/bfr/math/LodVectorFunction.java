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

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class LodVectorFunction implements MultivariateVectorFunctionWithJacobian {

	private String formula;
	private String[] parameters;
	private Map<String, double[]> variableValues;
	private double[] targetValues;
	private double levelOfDetection;
	private String sdParam;

	private int nParams;
	private int nValues;
	private Parser parser;
	private Node function;

	public LodVectorFunction(String formula, String[] parameters, Map<String, double[]> variableValues,
			double[] targetValues, double levelOfDetection, String sdParam) throws ParseException {
		this.formula = formula;
		this.parameters = parameters;
		this.variableValues = variableValues;
		this.targetValues = targetValues;
		this.levelOfDetection = levelOfDetection;
		this.sdParam = sdParam;

		nParams = parameters.length;
		nValues = targetValues.length;
		parser = new Parser(
				Stream.concat(Stream.of(parameters), variableValues.keySet().stream()).collect(Collectors.toSet()));
		function = parser.parse(formula);
	}

	@Override
	public double[] value(double[] point) throws IllegalArgumentException {
		double sd = Double.NaN;

		for (int ip = 0; ip < nParams; ip++) {
			if (parameters[ip].equals(sdParam)) {
				sd = point[ip];
			} else {
				parser.setVarValue(parameters[ip], point[ip]);
			}
		}

		double logLikelihood = 0.0;

		for (int iv = 0; iv < nValues; iv++) {
			for (Map.Entry<String, double[]> entry : variableValues.entrySet()) {
				parser.setVarValue(entry.getKey(), entry.getValue()[iv]);
			}

			try {
				double value = parser.evaluate(function);

				if (!Double.isFinite(value)) {
					return new double[] { Double.NaN };
				}

				NormalDistribution normDist = new NormalDistribution(value, sd);

				if (targetValues[iv] > levelOfDetection) {
					logLikelihood += Math.log(normDist.density(targetValues[iv]));
				} else {
					logLikelihood += Math.log(normDist.cumulativeProbability(levelOfDetection));
				}
			} catch (ParseException e) {
				e.printStackTrace();
				return new double[] { Double.NaN };
			}
		}

		return new double[] { logLikelihood };
	}

	@Override
	public MultivariateMatrixFunction createJacobian() {
		LodVectorFunction[] functions = new LodVectorFunction[nParams];

		for (int ip = 0; ip < nParams; ip++) {
			try {
				functions[ip] = new LodVectorFunction(formula, parameters, variableValues, targetValues,
						levelOfDetection, sdParam);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		return point -> MathUtils.aproxJacobianParallel(functions, point, nParams, 1);
	}
}
