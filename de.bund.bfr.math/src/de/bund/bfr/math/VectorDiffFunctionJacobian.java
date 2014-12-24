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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.lsmp.djep.djep.DJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class VectorDiffFunctionJacobian implements MultivariateMatrixFunction {

	private static final double EPSILON = 0.00001;
	private static final int MAX_THREADS = 8;

	private VectorDiffFunction[] diffFunctions;
	private int nParams;
	private int nValues;

	public VectorDiffFunctionJacobian(String[] formulas,
			String[] dependentVariables, Double[] initValues,
			String[] initParameters, String[] parameters,
			Map<String, double[]> variableValues, double[] timeValues,
			String dependentVariable, String timeVariable, Integrator integrator)
			throws ParseException {
		nParams = parameters.length;
		nValues = timeValues.length;

		Set<String> variables = new LinkedHashSet<>();

		variables.addAll(Arrays.asList(dependentVariables));
		variables.addAll(variableValues.keySet());
		variables.addAll(Arrays.asList(parameters));

		diffFunctions = new VectorDiffFunction[nParams];

		for (int i = 0; i < nParams; i++) {
			DJep[] parsers = new DJep[formulas.length];
			Node[] functions = new Node[formulas.length];

			for (int j = 0; j < formulas.length; j++) {
				parsers[j] = MathUtils.createParser(variables);
				functions[j] = parsers[j].parse(formulas[j]);
			}

			diffFunctions[i] = new VectorDiffFunction(parsers, functions,
					dependentVariables, initValues, initParameters, parameters,
					variableValues, timeValues, Arrays.asList(
							dependentVariables).indexOf(dependentVariable),
					timeVariable, integrator);
		}
	}

	@Override
	public double[][] value(double[] point) throws IllegalArgumentException {
		double[][] r = new double[nParams][nValues];
		ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

		for (int i = 0; i < nParams; i++) {
			executor.execute(new ParamDiffThread(point, i, r[i]));
		}

		executor.shutdown();

		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		double[][] result = new double[nValues][nParams];

		for (int i = 0; i < nParams; i++) {
			for (int j = 0; j < nValues; j++) {
				result[j][i] = r[i][j];
			}
		}

		return result;
	}

	private class ParamDiffThread implements Runnable {

		private double[] point;
		private int index;
		private double[] result;

		public ParamDiffThread(double[] point, int index, double[] result) {
			this.point = point;
			this.index = index;
			this.result = result;
		}

		@Override
		public void run() {
			double[] point = this.point.clone();

			point[index] = this.point[index] - EPSILON;

			double[] result1 = diffFunctions[index].value(point);

			point[index] = this.point[index] + EPSILON;

			double[] result2 = diffFunctions[index].value(point);

			for (int i = 0; i < result.length; i++) {
				result[i] = (result2[i] - result1[i]) / (2 * EPSILON);
			}
		}

	}
}
