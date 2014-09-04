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

	private Map<String, DJep[]> parsers;
	private Map<String, Node[]> functions;
	private String[] dependentVariables;
	private String[] initParameters;
	private String[] parameters;
	private Map<String, double[]> variableValues;
	private String dependentVariable;
	private String timeVariable;
	private Integrator integrator;

	public VectorDiffFunctionJacobian(String[] formulas,
			String[] dependentVariables, String[] initParameters,
			String[] parameters, Map<String, double[]> variableValues,
			String dependentVariable, String timeVariable, Integrator integrator)
			throws ParseException {
		this.dependentVariables = dependentVariables;
		this.initParameters = initParameters;
		this.parameters = parameters;
		this.variableValues = variableValues;
		this.dependentVariable = dependentVariable;
		this.timeVariable = timeVariable;
		this.integrator = integrator;

		Set<String> variables = new LinkedHashSet<>();

		variables.addAll(Arrays.asList(dependentVariables));
		variables.addAll(variableValues.keySet());
		variables.addAll(Arrays.asList(parameters));

		parsers = new LinkedHashMap<>();
		functions = new LinkedHashMap<>();

		for (String param : parameters) {
			parsers.put(param, new DJep[formulas.length]);
			functions.put(param, new Node[formulas.length]);

			for (int i = 0; i < formulas.length; i++) {
				DJep parser = MathUtilities.createParser(variables);

				parsers.get(param)[i] = parser;
				functions.get(param)[i] = parser.parse(formulas[i]);
			}
		}
	}

	@Override
	public double[][] value(double[] point) throws IllegalArgumentException {
		int nParam = parameters.length;
		int nValue = variableValues.get(timeVariable).length;
		double[][] r = new double[nParam][nValue];
		ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

		for (int i = 0; i < nParam; i++) {
			executor.execute(new ParamDiffThread(point, i, r[i]));
		}

		executor.shutdown();

		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		double[][] result = new double[nValue][nParam];

		for (int i = 0; i < nParam; i++) {
			for (int j = 0; j < nValue; j++) {
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
			DJep[] parser = parsers.get(parameters[index]);
			Node[] function = functions.get(parameters[index]);
			double[] point = this.point.clone();

			point[index] = this.point[index] - EPSILON;

			double[] result1 = new VectorDiffFunction(parser, function,
					dependentVariables, initParameters, parameters,
					variableValues, dependentVariable, timeVariable, integrator)
					.value(point);

			point[index] = this.point[index] + EPSILON;

			double[] result2 = new VectorDiffFunction(parser, function,
					dependentVariables, initParameters, parameters,
					variableValues, dependentVariable, timeVariable, integrator)
					.value(point);

			for (int i = 0; i < result.length; i++) {
				result[i] = (result2[i] - result1[i]) / (2 * EPSILON);
			}
		}

	}
}
