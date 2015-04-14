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
package de.bund.bfr.knime.nls.chart;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.nfunk.jep.ParseException;

import de.bund.bfr.math.Evaluator;
import de.bund.bfr.math.IntegratorFactory;
import de.bund.bfr.math.MathUtils;
import de.bund.bfr.math.Transform;

public class Plotable {

	private static final int DEFAULT_FUNCTION_STEPS = 1000;

	public static enum Type {
		DATA, FUNCTION, DATA_FUNCTION, DATA_DIFF
	}

	public static enum Status {
		OK("Ok"), FAILED("Failed"), NO_COVARIANCE("No Cov. Matrix");

		private String name;

		private Status(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private Type type;
	private int functionSteps;

	private Map<String, double[]> valueLists;
	private Map<String, double[]> conditionLists;

	private String function;
	private Map<String, Double> constants;
	private Map<String, Double> parameters;
	private Map<String, Map<String, Double>> covariances;
	private Map<String, Double> independentVariables;
	private Map<String, Double> minValues;
	private Map<String, Double> maxValues;

	private String dependentVariable;
	private String diffVariable;
	private Map<String, String> functions;
	private Map<String, Double> initValues;
	private Map<String, String> initParameters;

	private Double mse;
	private Integer degreesOfFreedom;

	public Plotable(Type type) {
		this.type = type;
		functionSteps = DEFAULT_FUNCTION_STEPS;

		valueLists = new LinkedHashMap<>();
		conditionLists = new LinkedHashMap<>();

		function = null;
		constants = new LinkedHashMap<>();
		parameters = new LinkedHashMap<>();
		covariances = new LinkedHashMap<>();
		independentVariables = new LinkedHashMap<>();
		minValues = new LinkedHashMap<>();
		maxValues = new LinkedHashMap<>();

		dependentVariable = null;
		diffVariable = null;
		functions = new LinkedHashMap<>();
		initValues = new LinkedHashMap<>();
		initParameters = new LinkedHashMap<>();

		mse = null;
		degreesOfFreedom = null;
	}

	public Type getType() {
		return type;
	}

	public int getFunctionSteps() {
		return functionSteps;
	}

	public void setFunctionSteps(int functionSteps) {
		this.functionSteps = functionSteps;
	}

	public Map<String, double[]> getValueLists() {
		return valueLists;
	}

	public Map<String, double[]> getConditionLists() {
		return conditionLists;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public Map<String, Double> getConstants() {
		return constants;
	}

	public Map<String, Double> getParameters() {
		return parameters;
	}

	public Map<String, Map<String, Double>> getCovariances() {
		return covariances;
	}

	public Map<String, Double> getIndependentVariables() {
		return independentVariables;
	}

	public Map<String, Double> getMinValues() {
		return minValues;
	}

	public Map<String, Double> getMaxValues() {
		return maxValues;
	}

	public String getDependentVariable() {
		return dependentVariable;
	}

	public void setDependentVariable(String dependentVariable) {
		this.dependentVariable = dependentVariable;
	}

	public String getDiffVariable() {
		return diffVariable;
	}

	public void setDiffVariable(String diffVariable) {
		this.diffVariable = diffVariable;
	}

	public Map<String, String> getFunctions() {
		return functions;
	}

	public Map<String, Double> getInitValues() {
		return initValues;
	}

	public Map<String, String> getInitParameters() {
		return initParameters;
	}

	public Double getMse() {
		return mse;
	}

	public void setMse(Double mse) {
		this.mse = mse;
	}

	public Integer getDegreesOfFreedom() {
		return degreesOfFreedom;
	}

	public void setDegreesOfFreedom(Integer degreesOfFreedom) {
		this.degreesOfFreedom = degreesOfFreedom;
	}

	public double[][] getDataPoints(String paramX, String paramY, Transform transformX,
			Transform transformY) {
		double[] xList = valueLists.get(paramX);
		double[] yList = valueLists.get(paramY);

		if (xList == null || yList == null) {
			return null;
		}

		List<Point2D.Double> points = new ArrayList<>(xList.length);

		for (int i = 0; i < xList.length; i++) {
			Double x = transformX.to(xList[i]);
			Double y = transformY.to(yList[i]);

			if (MathUtils.isValidDouble(x) && MathUtils.isValidDouble(y)) {
				points.add(new Point2D.Double(x, y));
			}
		}

		Collections.sort(points, new Comparator<Point2D.Double>() {

			@Override
			public int compare(Point2D.Double p1, Point2D.Double p2) {
				return Double.compare(p1.x, p2.x);
			}
		});

		double[][] pointsArray = new double[2][points.size()];

		for (int i = 0; i < points.size(); i++) {
			pointsArray[0][i] = points.get(i).x;
			pointsArray[1][i] = points.get(i).y;
		}

		return pointsArray;
	}

	public double[][] getFunctionPoints(String varX, Transform transformX, Transform transformY,
			double minX, double maxX) throws ParseException {
		Map<String, Double> parserConstants = createParserConstants(varX);

		if (function == null || parserConstants == null) {
			return null;
		}

		double[] xs = new double[functionSteps];
		double[] convertedXs = new double[functionSteps];

		for (int i = 0; i < functionSteps; i++) {
			xs[i] = minX + (double) i / (double) (functionSteps - 1) * (maxX - minX);
			convertedXs[i] = transformX.from(xs[i]);
		}

		double[] convertedYs = Evaluator.getFunctionPoints(parserConstants, function, varX,
				convertedXs);

		if (convertedYs == null) {
			return null;
		}

		double[][] points = new double[2][functionSteps];
		boolean containsValidPoint = false;

		for (int i = 0; i < functionSteps; i++) {
			Double y = transformY.to(convertedYs[i]);

			if (MathUtils.isValidDouble(y)) {
				points[1][i] = y;
				containsValidPoint = true;
			} else {
				points[1][i] = Double.NaN;
			}
		}

		if (!containsValidPoint) {
			return null;
		}

		System.arraycopy(xs, 0, points[0], 0, functionSteps);

		return points;
	}

	public double[][] getFunctionErrors(String varX, Transform transformX, Transform transformY,
			double minX, double maxX, boolean prediction) throws ParseException {
		Map<String, Double> parserConstants = createParserConstants(varX);

		if (function == null || parserConstants == null || covarianceMatrixMissing()) {
			return null;
		}

		double[] xs = new double[functionSteps];
		double[] convertedXs = new double[functionSteps];

		for (int i = 0; i < functionSteps; i++) {
			xs[i] = minX + (double) i / (double) (functionSteps - 1) * (maxX - minX);
			convertedXs[i] = transformX.from(xs[i]);
		}

		double[] convertedYs = Evaluator.getFunctionErrors(parserConstants, function, varX,
				convertedXs, covariances, prediction ? mse : 0.0, degreesOfFreedom);

		if (convertedYs == null) {
			return null;
		}

		double[][] points = new double[2][functionSteps];
		boolean containsValidPoint = false;

		for (int i = 0; i < functionSteps; i++) {
			Double y = transformY.to(convertedYs[i]);

			if (MathUtils.isValidDouble(y)) {
				points[1][i] = y;
				containsValidPoint = true;
			} else {
				points[1][i] = Double.NaN;
			}
		}

		if (!containsValidPoint) {
			return null;
		}

		System.arraycopy(xs, 0, points[0], 0, functionSteps);

		return points;
	}

	public double[][] getDiffPoints(String varX, Transform transformX, Transform transformY,
			double minX, double maxX) throws ParseException {
		Map<String, Double> parserConstants = createParserConstants(varX);

		if (parserConstants == null || functions.isEmpty() || !varX.equals(diffVariable)) {
			return null;
		}

		double[] xs = new double[functionSteps];
		double[] convertedXs = new double[functionSteps];
		double stepSize = (maxX - minX) / (functionSteps - 1);

		for (int i = 0; i < functionSteps; i++) {
			xs[i] = minX + i * stepSize;
			convertedXs[i] = transformX.from(xs[i]);
		}

		IntegratorFactory integrator = new IntegratorFactory(IntegratorFactory.Type.RUNGE_KUTTA,
				stepSize / 10.0);
		double[] convertedYs = Evaluator.getDiffPoints(parserConstants, functions, initValues,
				initParameters, conditionLists, dependentVariable, independentVariables, varX,
				convertedXs, integrator);

		if (convertedYs == null) {
			return null;
		}

		double[][] points = new double[2][functionSteps];
		boolean containsValidPoint = false;

		for (int i = 0; i < functionSteps; i++) {
			Double y = transformY.to(convertedYs[i]);

			if (MathUtils.isValidDouble(y)) {
				points[1][i] = y;
				containsValidPoint = true;
			} else {
				points[1][i] = Double.NaN;
			}
		}

		if (!containsValidPoint) {
			return null;
		}

		System.arraycopy(xs, 0, points[0], 0, functionSteps);

		return points;
	}

	public double[][] getDiffErrors(String varX, Transform transformX, Transform transformY,
			double minX, double maxX, boolean prediction) throws ParseException {
		Map<String, Double> parserConstants = createParserConstants(varX);

		if (parserConstants == null || functions.isEmpty() || !varX.equals(diffVariable)
				|| covarianceMatrixMissing()) {
			return null;
		}

		double[] xs = new double[functionSteps];
		double[] convertedXs = new double[functionSteps];
		double stepSize = (maxX - minX) / (functionSteps - 1);

		for (int i = 0; i < functionSteps; i++) {
			xs[i] = minX + i * stepSize;
			convertedXs[i] = transformX.from(xs[i]);
		}

		IntegratorFactory integrator = new IntegratorFactory(IntegratorFactory.Type.RUNGE_KUTTA,
				stepSize / 10.0);
		double[] convertedYs = Evaluator.getDiffErrors(parserConstants, functions, initValues,
				initParameters, conditionLists, dependentVariable, independentVariables, varX,
				convertedXs, integrator, covariances, prediction ? mse : 0.0, degreesOfFreedom);

		if (convertedYs == null) {
			return null;
		}

		double[][] points = new double[2][functionSteps];
		boolean containsValidPoint = false;

		for (int i = 0; i < functionSteps; i++) {
			Double y = transformY.to(convertedYs[i]);

			if (MathUtils.isValidDouble(y)) {
				points[1][i] = y;
				containsValidPoint = true;
			} else {
				points[1][i] = Double.NaN;
			}
		}

		if (!containsValidPoint) {
			return null;
		}

		System.arraycopy(xs, 0, points[0], 0, functionSteps);

		return points;
	}

	public Status getStatus() {
		if (!isPlotable()) {
			return Status.FAILED;
		} else if (covarianceMatrixMissing()) {
			return Status.NO_COVARIANCE;
		}

		return Status.OK;
	}

	private boolean isPlotable() {
		List<Type> typesWithParams = Arrays.asList(Type.FUNCTION, Type.DATA_FUNCTION,
				Type.DATA_DIFF);
		List<Type> typesWithData = Arrays.asList(Type.DATA, Type.DATA_FUNCTION, Type.DATA_DIFF);

		if (typesWithParams.contains(type) && parameters.values().contains(null)) {
			return false;
		}

		if (typesWithData.contains(type)) {
			if (valueLists.isEmpty()) {
				return false;
			}

			int n = valueLists.get(new ArrayList<>(valueLists.keySet()).get(0)).length;
			boolean containsData = false;

			for (int i = 0; i < n; i++) {
				boolean containsNull = false;

				for (String var : valueLists.keySet()) {
					if (!MathUtils.isValidDouble(valueLists.get(var)[i])) {
						containsNull = true;
						break;
					}
				}

				if (!containsNull) {
					containsData = true;
					break;
				}
			}

			if (!containsData) {
				return false;
			}
		}

		return true;
	}

	private boolean covarianceMatrixMissing() {
		for (String param : parameters.keySet()) {
			if (covariances.get(param) == null) {
				return true;
			}

			for (String param2 : parameters.keySet()) {
				if (covariances.get(param).get(param2) == null) {
					return true;
				}
			}
		}

		return false;
	}

	private Map<String, Double> createParserConstants(String varX) {
		Map<String, Double> parserConstants = new LinkedHashMap<>();

		for (String constant : constants.keySet()) {
			if (constants.get(constant) == null) {
				return null;
			}

			parserConstants.put(constant, constants.get(constant));
		}

		for (String param : parameters.keySet()) {
			if (parameters.get(param) == null) {
				return null;
			}

			parserConstants.put(param, parameters.get(param));
		}

		if (type != Type.DATA_DIFF) {
			for (String param : independentVariables.keySet()) {
				if (!param.equals(varX)) {
					parserConstants.put(param, independentVariables.get(param));
				}
			}
		}

		return parserConstants;
	}
}
