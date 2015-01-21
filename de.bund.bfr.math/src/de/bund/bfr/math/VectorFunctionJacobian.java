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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.lsmp.djep.djep.DJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

import com.google.common.collect.Sets;

public class VectorFunctionJacobian implements MultivariateMatrixFunction {

	private static final double EPSILON = 0.00001;

	private DJep parser;
	private Node function;
	private String[] parameters;
	private Map<String, Node> derivatives;
	private List<Map<String, double[]>> variableValues;
	private int dimension;

	public VectorFunctionJacobian(String formula, String[] parameters,
			Map<String, double[]> variableValues) throws ParseException {
		this.parameters = parameters;
		this.variableValues = createArgumentVariationList(variableValues);

		parser = MathUtils.createParser(Sets.union(
				new LinkedHashSet<>(Arrays.asList(parameters)),
				variableValues.keySet()));
		function = parser.parse(formula);
		derivatives = new LinkedHashMap<>();

		for (String param : parameters) {
			derivatives.put(param, parser.differentiate(function, param));
		}

		for (double[] values : variableValues.values()) {
			dimension = values.length;
			break;
		}
	}

	@Override
	public double[][] value(double[] point) throws IllegalArgumentException {
		double[][] retValue = new double[dimension][parameters.length];

		try {
			Map<String, Double> paramValues = new LinkedHashMap<>();

			for (int i = 0; i < parameters.length; i++) {
				paramValues.put(parameters[i], point[i]);
			}

			for (int i = 0; i < dimension; i++) {
				for (int j = 0; j < parameters.length; j++) {
					retValue[i][j] = evalWithSingularityCheck(parameters[j],
							paramValues, i);
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return retValue;
	}

	private double evalWithSingularityCheck(String param,
			Map<String, Double> paramValues, int index) throws ParseException {
		for (Map.Entry<String, Double> entry : paramValues.entrySet()) {
			parser.setVarValue(entry.getKey(), entry.getValue());
		}

		for (Map<String, double[]> argValues : variableValues) {
			for (Map.Entry<String, double[]> entry : argValues.entrySet()) {
				parser.setVarValue(entry.getKey(), entry.getValue()[index]);
			}

			Object number = parser.evaluate(derivatives.get(param));

			if (MathUtils.isValidDouble(number)) {
				return (Double) number;
			}
		}

		for (Map<String, double[]> argValues : variableValues) {
			for (Map.Entry<String, double[]> entry : argValues.entrySet()) {
				parser.setVarValue(entry.getKey(), entry.getValue()[index]);
			}

			parser.setVarValue(param, paramValues.get(param) - EPSILON);

			Object number1 = parser.evaluate(function);

			parser.setVarValue(param, paramValues.get(param) + EPSILON);

			Object number2 = parser.evaluate(function);

			if (MathUtils.isValidDouble(number1)
					&& MathUtils.isValidDouble(number2)) {
				return ((Double) number2 - (Double) number1) / (2 * EPSILON);
			}
		}

		return Double.NaN;
	}

	private static List<Map<String, double[]>> createArgumentVariationList(
			Map<String, double[]> argumentValues) {
		int n = argumentValues.size();
		boolean done = false;
		List<int[]> variationList = new ArrayList<>();
		int[] variation = new int[n];

		Arrays.fill(variation, -1);

		while (!done) {
			variationList.add(variation.clone());

			for (int i = 0;; i++) {
				if (i >= n) {
					done = true;
					break;
				}

				variation[i]++;

				if (variation[i] > 1) {
					variation[i] = -1;
				} else {
					break;
				}
			}
		}

		Collections.sort(variationList, new Comparator<int[]>() {

			@Override
			public int compare(int[] l1, int[] l2) {
				int n1 = 0;
				int n2 = 0;

				for (int i : l1) {
					if (i == 0) {
						n1++;
					}
				}

				for (int i : l2) {
					if (i == 0) {
						n2++;
					}
				}

				if (n1 < n2) {
					return 1;
				} else if (n1 > n2) {
					return -1;
				} else {
					return 0;
				}
			}
		});

		List<Map<String, double[]>> argumentVariationList = new ArrayList<>();

		for (int[] changeList : variationList) {
			Map<String, double[]> newArgumentValues = new LinkedHashMap<>();
			int index = 0;

			for (String arg : argumentValues.keySet()) {
				double[] oldValues = argumentValues.get(arg);
				double[] newValues = new double[oldValues.length];
				double d = changeList[index] * EPSILON;

				for (int i = 0; i < oldValues.length; i++) {
					newValues[i] = oldValues[i] + d;
				}

				newArgumentValues.put(arg, newValues);
				index++;
			}

			argumentVariationList.add(newArgumentValues);
		}

		return argumentVariationList;
	}
}
