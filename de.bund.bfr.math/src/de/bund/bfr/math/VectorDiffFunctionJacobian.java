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
import java.util.LinkedHashMap;
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

	private Map<String, VectorDiffFunction> diffFunctions;
	private String[] parameters;
	private double[] timeValues;

	public VectorDiffFunctionJacobian(String[] formulas,
			String[] dependentVariables, Double[] initValues,
			String[] initParameters, String[] parameters,
			Map<String, double[]> variableValues, double[] timeValues,
			String dependentVariable, String timeVariable, Integrator integrator)
			throws ParseException {
		this.parameters = parameters;
		this.timeValues = timeValues;

		Set<String> variables = new LinkedHashSet<>();

		variables.addAll(Arrays.asList(dependentVariables));
		variables.addAll(variableValues.keySet());
		variables.addAll(Arrays.asList(parameters));

		diffFunctions = new LinkedHashMap<>();

		for (String param : parameters) {
			DJep[] parsers = new DJep[formulas.length];
			Node[] functions = new Node[formulas.length];

			for (int i = 0; i < formulas.length; i++) {
				parsers[i] = MathUtils.createParser(variables);
				functions[i] = parsers[i].parse(formulas[i]);
			}

			diffFunctions.put(param, new VectorDiffFunction(parsers, functions,
					dependentVariables, initValues, initParameters, parameters,
					variableValues, timeValues, dependentVariable,
					timeVariable, integrator));
		}
	}

	@Override
	public double[][] value(double[] point) throws IllegalArgumentException {
		double[][] r = new double[parameters.length][timeValues.length];
		ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

		for (int i = 0; i < parameters.length; i++) {
			executor.execute(new ParamDiffThread(point, i, r[i]));
		}

		executor.shutdown();

		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		double[][] result = new double[timeValues.length][parameters.length];

		for (int i = 0; i < parameters.length; i++) {
			for (int j = 0; j < timeValues.length; j++) {
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
			VectorDiffFunction diffFunction = diffFunctions
					.get(parameters[index]);
			double[] point = this.point.clone();

			point[index] = this.point[index] - EPSILON;

			double[] result1 = diffFunction.value(point);

			point[index] = this.point[index] + EPSILON;

			double[] result2 = diffFunction.value(point);

			for (int i = 0; i < result.length; i++) {
				result[i] = (result2[i] - result1[i]) / (2 * EPSILON);
			}
		}

	}
}
