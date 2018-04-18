/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

import org.sbml.jsbml.text.parser.ParseException;

import de.bund.bfr.math.Evaluator;
import de.bund.bfr.math.IntegratorFactory;
import de.bund.bfr.math.InterpolationFactory;
import de.bund.bfr.math.Transform;

public class Plotable {

	private static final int DEFAULT_FUNCTION_STEPS = 1000;

	public static enum Type {
		DATA, FUNCTION, DATA_FUNCTION, DIFF, DATA_DIFF
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
	private InterpolationFactory.Type interpolator;

	private Map<String, List<Double>> valueLists;
	private Map<String, List<Double>> conditionLists;

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
		interpolator = InterpolationFactory.Type.STEP;

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

	public InterpolationFactory.Type getInterpolator() {
		return interpolator;
	}

	public void setInterpolator(InterpolationFactory.Type interpolator) {
		this.interpolator = interpolator;
	}

	public Map<String, List<Double>> getValueLists() {
		return valueLists;
	}

	public Map<String, List<Double>> getConditionLists() {
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

	public double[][] getDataPoints(String paramX, String paramY, Transform transformX, Transform transformY) {
		List<Double> xList = valueLists.get(paramX);
		List<Double> yList = valueLists.get(paramY);

		if (xList == null || yList == null) {
			return null;
		}

		List<Point2D.Double> points = new ArrayList<>(xList.size());

		for (int i = 0; i < xList.size(); i++) {
			double x = transformX.to(xList.get(i));
			double y = transformY.to(yList.get(i));

			if (Double.isFinite(x) && Double.isFinite(y)) {
				points.add(new Point2D.Double(x, y));
			}
		}

		Collections.sort(points, (p1, p2) -> Double.compare(p1.x, p2.x));

		double[][] pointsArray = new double[2][points.size()];

		for (int i = 0; i < points.size(); i++) {
			pointsArray[0][i] = points.get(i).x;
			pointsArray[1][i] = points.get(i).y;
		}

		return pointsArray;
	}

	public double[][] getFunctionPoints(String varX, Transform transformX, Transform transformY, double minX,
			double maxX) throws ParseException {
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

		double[] convertedYs = Evaluator.getFunctionPoints(parserConstants, function, varX, convertedXs);
		double[][] points = new double[2][functionSteps];
		boolean containsValidPoint = false;

		for (int i = 0; i < functionSteps; i++) {
			double y = transformY.to(convertedYs[i]);

			if (Double.isFinite(y)) {
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

	public double[][] getFunctionErrors(String varX, Transform transformX, Transform transformY, double minX,
			double maxX, boolean prediction) throws ParseException {
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

		double[] convertedYs = Evaluator.getFunctionErrors(parserConstants, function, varX, convertedXs, covariances,
				prediction ? mse : 0.0, degreesOfFreedom);
		double[][] points = new double[2][functionSteps];
		boolean containsValidPoint = false;

		for (int i = 0; i < functionSteps; i++) {
			double y = transformY.to(convertedYs[i]);

			if (Double.isFinite(y)) {
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

	public double[][] getDiffPoints(String varX, Transform transformX, Transform transformY, double minX, double maxX)
			throws ParseException {
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

		IntegratorFactory integrator = new IntegratorFactory(IntegratorFactory.Type.RUNGE_KUTTA, stepSize / 10.0);
		InterpolationFactory interpolator = new InterpolationFactory(this.interpolator);
		double[] convertedYs = Evaluator.getDiffPoints(parserConstants, functions, initValues, initParameters,
				conditionLists, dependentVariable, independentVariables, varX, convertedXs, integrator, interpolator);
		double[][] points = new double[2][functionSteps];
		boolean containsValidPoint = false;

		for (int i = 0; i < functionSteps; i++) {
			double y = transformY.to(convertedYs[i]);

			if (Double.isFinite(y)) {
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

	public double[][] getDiffErrors(String varX, Transform transformX, Transform transformY, double minX, double maxX,
			boolean prediction) throws ParseException {
		Map<String, Double> parserConstants = createParserConstants(varX);

		if (parserConstants == null || functions.isEmpty() || !varX.equals(diffVariable) || covarianceMatrixMissing()) {
			return null;
		}

		double[] xs = new double[functionSteps];
		double[] convertedXs = new double[functionSteps];
		double stepSize = (maxX - minX) / (functionSteps - 1);

		for (int i = 0; i < functionSteps; i++) {
			xs[i] = minX + i * stepSize;
			convertedXs[i] = transformX.from(xs[i]);
		}

		IntegratorFactory integrator = new IntegratorFactory(IntegratorFactory.Type.RUNGE_KUTTA, stepSize / 10.0);
		InterpolationFactory interpolator = new InterpolationFactory(this.interpolator);
		double[] convertedYs = Evaluator.getDiffErrors(parserConstants, functions, initValues, initParameters,
				conditionLists, dependentVariable, independentVariables, varX, convertedXs, integrator, interpolator,
				covariances, prediction ? mse : 0.0, degreesOfFreedom);

		if (convertedYs == null) {
			return null;
		}

		double[][] points = new double[2][functionSteps];
		boolean containsValidPoint = false;

		for (int i = 0; i < functionSteps; i++) {
			double y = transformY.to(convertedYs[i]);

			if (Double.isFinite(y)) {
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

	public boolean isDataType() {
		return Arrays.asList(Type.DATA, Type.DATA_FUNCTION, Type.DATA_DIFF).contains(type);
	}

	public boolean isParamType() {
		return Arrays.asList(Type.FUNCTION, Type.DATA_FUNCTION, Type.DIFF, Type.DATA_DIFF).contains(type);
	}

	private boolean isPlotable() {
		IntPredicate containsDataAtIndex = i -> valueLists.values().stream().map(list -> list.get(i))
				.allMatch(v -> v != null && Double.isFinite(v));
		boolean dataPlotable = !valueLists.isEmpty() && IntStream
				.range(0, valueLists.values().stream().findAny().get().size()).anyMatch(containsDataAtIndex);
		boolean paramPlotable = !parameters.values().contains(null);

		if (isDataType() && isParamType()) {
			return dataPlotable && paramPlotable;
		} else if (isDataType()) {
			return dataPlotable;
		} else if (isParamType()) {
			return paramPlotable;
		} else {
			return true;
		}
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
		if (constants.values().contains(null) || parameters.values().contains(null)) {
			return null;
		}

		Map<String, Double> parserConstants = new LinkedHashMap<>();

		parserConstants.putAll(constants);
		parserConstants.putAll(parameters);

		if (type != Type.DATA_DIFF && type != Type.DIFF) {
			parserConstants.putAll(independentVariables);
			parserConstants.remove(varX);
		}

		return parserConstants;
	}
}
