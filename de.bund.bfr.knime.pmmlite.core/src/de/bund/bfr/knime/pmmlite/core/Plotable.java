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
package de.bund.bfr.knime.pmmlite.core;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.sbml.jsbml.text.parser.ParseException;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;

import de.bund.bfr.math.Evaluator;
import de.bund.bfr.math.MathUtils;
import de.bund.bfr.math.Transform;

public class Plotable {

	private static final int DEFAULT_FUNCTION_STEPS = 1000;

	public static enum Type {
		DATASET, DATASET_MANY, FUNCTION, BOTH, BOTH_MANY, FUNCTION_SAMPLE
	}

	public static enum Status {
		OK("Ok"), FAILED("Failed"), OUT_OF_LIMITS("Coeff. Out Of Limit"), NO_COVARIANCE(
				"No Cov. Matrix"), OUT_OF_LIMITS_AND_NO_COVARIANCE("Coeff. Out Of Limit + No Cov. Matrix");

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
	private String function;
	private String functionValue;

	private Map<String, List<Double>> valueLists;
	private Map<String, List<Double>> variables;
	private Map<String, Double> constants;
	private Map<String, Double> parameters;
	private Map<String, Double> variableParameters;

	private Map<String, Map<String, Double>> covariances;
	private Map<String, Double> minValues;
	private Map<String, Double> maxValues;
	private Map<String, PmmUnit> units;
	private List<Double> samples;

	private Double sse;
	private Double mse;
	private Double rmse;
	private Double r2;
	private Double aic;
	private Integer degreesOfFreedom;

	private Double globalMse;
	private Integer globalDegreesOfFreedom;

	public Plotable(Type type) {
		this.type = type;
		functionSteps = DEFAULT_FUNCTION_STEPS;
		function = null;
		functionValue = null;

		valueLists = new LinkedHashMap<>();
		variables = new LinkedHashMap<>();
		constants = new LinkedHashMap<>();
		parameters = new LinkedHashMap<>();
		variableParameters = new LinkedHashMap<>();

		minValues = new LinkedHashMap<>();
		maxValues = new LinkedHashMap<>();
		units = new LinkedHashMap<>();
		covariances = new LinkedHashMap<>();
		samples = new ArrayList<>();

		sse = null;
		mse = null;
		rmse = null;
		r2 = null;
		aic = null;
		degreesOfFreedom = null;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getFunctionSteps() {
		return functionSteps;
	}

	public void setFunctionSteps(int functionSteps) {
		this.functionSteps = functionSteps;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public String getFunctionValue() {
		return functionValue;
	}

	public void setFunctionValue(String functionValue) {
		this.functionValue = functionValue;
	}

	public Map<String, List<Double>> getValueLists() {
		return valueLists;
	}

	public Map<String, List<Double>> getVariables() {
		return variables;
	}

	public Map<String, Double> getConstants() {
		return constants;
	}

	public Map<String, Double> getParameters() {
		return parameters;
	}

	public Map<String, Double> getVariableParameters() {
		return variableParameters;
	}

	public Map<String, Map<String, Double>> getCovariances() {
		return covariances;
	}

	public Map<String, Double> getMinValues() {
		return minValues;
	}

	public Map<String, Double> getMaxValues() {
		return maxValues;
	}

	public Map<String, PmmUnit> getUnits() {
		return units;
	}

	public List<Double> getSamples() {
		return samples;
	}

	public Double getSse() {
		return sse;
	}

	public void setSse(Double sse) {
		this.sse = sse;
	}

	public Double getMse() {
		return mse;
	}

	public void setMse(Double mse) {
		this.mse = mse;
	}

	public Double getRmse() {
		return rmse;
	}

	public void setRmse(Double rmse) {
		this.rmse = rmse;
	}

	public Double getR2() {
		return r2;
	}

	public void setR2(Double r2) {
		this.r2 = r2;
	}

	public Double getAic() {
		return aic;
	}

	public void setAic(Double aic) {
		this.aic = aic;
	}

	public Integer getDegreesOfFreedom() {
		return degreesOfFreedom;
	}

	public void setDegreesOfFreedom(Integer degreesOfFreedom) {
		this.degreesOfFreedom = degreesOfFreedom;
	}

	public Double getGlobalMse() {
		return globalMse;
	}

	public void setGlobalMse(Double globalMse) {
		this.globalMse = globalMse;
	}

	public Integer getGlobalDegreesOfFreedom() {
		return globalDegreesOfFreedom;
	}

	public void setGlobalDegreesOfFreedom(Integer globalDegreesOfFreedom) {
		this.globalDegreesOfFreedom = globalDegreesOfFreedom;
	}

	public Set<String> getAllVariables() {
		Set<String> all = new LinkedHashSet<>();

		all.addAll(variables.keySet());
		all.addAll(variableParameters.keySet());

		return all;
	}

	public Map<String, Double> getAllParameters() {
		Map<String, Double> all = new LinkedHashMap<>();

		all.putAll(parameters);
		all.putAll(variableParameters);

		return all;
	}

	public Map<String, List<Double>> getPossibleVariableValues() {
		Map<String, List<Double>> vars = new LinkedHashMap<>();

		for (String var : variables.keySet()) {
			if (valueLists.get(var) != null) {
				Set<Double> values = new LinkedHashSet<>(valueLists.get(var));

				values.removeAll(Collections.singleton(null));
				vars.put(var, Ordering.natural().sortedCopy(values));
			} else {
				vars.put(var, new ArrayList<>(0));
			}
		}

		return vars;
	}

	public double[][] getPoints(Variable varX, Variable varY) throws UnitException {
		return getPoints(varX, varY, getStandardChoice());
	}

	public double[][] getPoints(Variable varX, Variable varY, Map<String, Integer> choice) throws UnitException {
		List<Double> xList = valueLists.get(varX.getName());
		List<Double> yList = valueLists.get(varY.getName());

		if (xList == null || yList == null) {
			return null;
		}

		List<Boolean> usedPoints = new ArrayList<>(Collections.nCopies(xList.size(), true));

		for (Map.Entry<String, List<Double>> entry : variables.entrySet()) {
			String var = entry.getKey();

			if (!var.equals(varX.getName()) && valueLists.containsKey(var)) {
				Double fixedValue = entry.getValue().get(choice.get(var));
				List<Double> values = valueLists.get(var);

				for (int i = 0; i < values.size(); i++) {
					if (!fixedValue.equals(values.get(i))) {
						usedPoints.set(i, false);
					}
				}
			}
		}

		if (!usedPoints.contains(true)) {
			return null;
		}

		List<Point2D.Double> points = new ArrayList<>(xList.size());

		for (int i = 0; i < xList.size(); i++) {
			double x = varX.to(MathUtils.nullToNan(xList.get(i)), units.get(varX.getName()));
			double y = varY.to(MathUtils.nullToNan(yList.get(i)), units.get(varY.getName()));

			if (usedPoints.get(i) && Double.isFinite(x) && Double.isFinite(y)) {
				points.add(new Point2D.Double(x, y));
			}
		}

		if (points.isEmpty()) {
			return null;
		}

		Collections.sort(points, (p1, p2) -> Double.compare(p1.x, p2.x));

		double[][] pointsArray = new double[2][points.size()];

		for (int i = 0; i < points.size(); i++) {
			pointsArray[0][i] = points.get(i).x;
			pointsArray[1][i] = points.get(i).y;
		}

		return pointsArray;
	}

	public double[][] getFunctionPoints(Variable varX, Variable varY, double minX, double maxX)
			throws ParseException, UnitException {
		return getFunctionPoints(varX, varY, minX, maxX, getStandardChoice());
	}

	public double[][] getFunctionPoints(Variable varX, Variable varY, double minX, double maxX,
			Map<String, Integer> choice) throws ParseException, UnitException {
		double[] xValues = new double[functionSteps];

		for (int i = 0; i < functionSteps; i++) {
			xValues[i] = minX + (double) i / (double) (functionSteps - 1) * (maxX - minX);
		}

		return getFunctionPoints(varX, varY, choice, xValues);
	}

	public double[][] getFunctionErrors(Variable varX, Variable varY, double minX, double maxX, boolean prediction)
			throws ParseException, UnitException {
		return getFunctionErrors(varX, varY, minX, maxX, prediction, getStandardChoice());
	}

	public double[][] getFunctionErrors(Variable varX, Variable varY, double minX, double maxX, boolean prediction,
			Map<String, Integer> choice) throws ParseException, UnitException {
		Map<String, Double> parserConstants = createParserConstants(varX.getName(), choice);

		if (function == null || parserConstants == null || covarianceMatrixMissing()) {
			return null;
		}

		double[] xs = new double[functionSteps];
		double[] convertedXs = new double[functionSteps];

		for (int i = 0; i < functionSteps; i++) {
			xs[i] = minX + (double) i / (double) (functionSteps - 1) * (maxX - minX);
			convertedXs[i] = varX.from(xs[i], units.get(varX.getName()));
		}

		double[] convertedYs = Evaluator.getFunctionErrors(parserConstants, function, varX.name, convertedXs,
				covariances, prediction ? globalMse : 0.0, globalDegreesOfFreedom);
		double[][] points = new double[2][functionSteps];
		boolean containsValidPoint = false;

		for (int i = 0; i < functionSteps; i++) {
			double y = varY.to(convertedYs[i], units.get(varY.getName()));

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

	public double[][] getFunctionSamplePoints(Variable varX, Variable varY, double minX, double maxX)
			throws ParseException, UnitException {
		return getFunctionSamplePoints(varX, varY, minX, maxX, getStandardChoice());
	}

	public double[][] getFunctionSamplePoints(Variable varX, Variable varY, double minX, double maxX,
			Map<String, Integer> choice) throws ParseException, UnitException {
		List<Double> samplesInRange = new ArrayList<>();

		for (Double x : samples) {
			if (x != null && x >= minX && x <= maxX) {
				samplesInRange.add(x);
			}
		}

		return getFunctionPoints(varX, varY, choice, Doubles.toArray(samplesInRange));
	}

	public Status getStatus() {
		if (!isPlotable()) {
			return Status.FAILED;
		}

		boolean outOfRange = isOutOfRange();
		boolean covMissing = covarianceMatrixMissing();

		if (outOfRange && covMissing) {
			return Status.OUT_OF_LIMITS_AND_NO_COVARIANCE;
		} else if (outOfRange) {
			return Status.OUT_OF_LIMITS;
		} else if (covMissing) {
			return Status.NO_COVARIANCE;
		}

		return Status.OK;
	}

	public List<Map<String, Integer>> getAllChoices(String varX) {
		List<Map<String, Integer>> choices = new ArrayList<>();
		List<String> varList = new ArrayList<>(variables.keySet());

		varList.remove(varX);

		List<Integer> choice = new ArrayList<>(Collections.nCopies(varList.size(), 0));
		boolean done = false;

		while (!done) {
			Map<String, Integer> map = new LinkedHashMap<>();

			for (int i = 0; i < varList.size(); i++) {
				map.put(varList.get(i), choice.get(i));
			}

			choices.add(map);

			for (int i = 0;; i++) {
				if (i >= varList.size()) {
					done = true;
					break;
				}

				choice.set(i, choice.get(i) + 1);

				if (choice.get(i) >= variables.get(varList.get(i)).size()) {
					choice.set(i, 0);
				} else {
					break;
				}
			}
		}

		return choices;
	}

	public int getNumberOfCombinations() {
		int nMax = 0;

		for (String varX : variables.keySet()) {
			int n = 1;

			for (String var : variables.keySet()) {
				if (!var.equals(varX) && valueLists.containsKey(var)) {
					n *= new LinkedHashSet<>(valueLists.get(var)).size();
				}
			}

			nMax = Math.max(nMax, n);
		}

		return nMax;
	}

	public void computeQualityMeasure(Variable varX, Variable varY, boolean local)
			throws ParseException, UnitException {
		if (getStatus() != Status.OK) {
			return;
		}

		List<Double> allTargetValues = new ArrayList<>();

		sse = 0.0;
		mse = null;
		rmse = null;
		r2 = null;
		aic = null;
		degreesOfFreedom = null;

		for (Map<String, Integer> choice : getAllChoices(varX.name)) {
			double[][] points = getPoints(varX, varY, choice);

			if (points == null) {
				continue;
			}

			double[][] functionPoints = getFunctionPoints(varX, varY, choice, points[0]);
			double[] targetValues = points[1];
			double[] values = functionPoints[1];

			for (int i = 0; i < targetValues.length; i++) {
				double diff = targetValues[i] - values[i];

				sse += diff * diff;
			}

			allTargetValues.addAll(Doubles.asList(targetValues));
		}

		if (local) {
			mse = sse / allTargetValues.size();
			rmse = Math.sqrt(mse);
		} else {
			int nParams = getAllParameters().size();

			degreesOfFreedom = allTargetValues.size() - nParams;
			r2 = MathUtils.getR2(sse, allTargetValues);
			aic = MathUtils.getAic(nParams, allTargetValues.size(), sse);

			if (degreesOfFreedom > 0) {
				mse = sse / degreesOfFreedom;
				rmse = Math.sqrt(mse);
			}

			globalMse = mse;
			globalDegreesOfFreedom = degreesOfFreedom;
		}
	}

	private double[][] getFunctionPoints(Variable varX, Variable varY, Map<String, Integer> choice, double[] xs)
			throws ParseException, UnitException {
		Map<String, Double> parserConstants = createParserConstants(varX.getName(), choice);

		if (function == null || parserConstants == null) {
			return null;
		}

		double[] convertedXs = new double[xs.length];

		for (int i = 0; i < xs.length; i++) {
			convertedXs[i] = varX.from(xs[i], units.get(varX.getName()));
		}

		double[] convertedYs = Evaluator.getFunctionPoints(parserConstants, function, varX.name, convertedXs);
		double[][] points = new double[2][xs.length];
		boolean containsValidPoint = false;

		for (int i = 0; i < xs.length; i++) {
			double y = varY.to(convertedYs[i], units.get(varY.getName()));

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

		System.arraycopy(xs, 0, points[0], 0, xs.length);

		return points;
	}

	private Map<String, Integer> getStandardChoice() {
		Map<String, Integer> choice = new LinkedHashMap<>();

		for (String var : variables.keySet()) {
			choice.put(var, 0);
		}

		return choice;
	}

	private boolean isPlotable() {
		if (type == Type.FUNCTION || type == Type.FUNCTION_SAMPLE) {
			return !parameters.values().contains(null);
		} else {
			List<String> variablesX = new ArrayList<>(valueLists.keySet());
			List<String> variablesY = new ArrayList<>();

			if (type == Type.DATASET || type == Type.DATASET_MANY) {
				variablesY = variablesX;
			} else if (type == Type.BOTH || type == Type.BOTH_MANY) {
				if (functionValue != null) {
					variablesY = Arrays.asList(functionValue);
				}
			}

			for (String varX : variablesX) {
				for (String varY : variablesY) {
					if (isPlotable(varX, varY)) {
						return true;
					}
				}
			}

			return false;
		}
	}

	private boolean isPlotable(String varX, String varY) {
		boolean dataSetPlotable;

		try {
			double[] xs = Doubles.toArray(valueLists.get(varX));
			double[] ys = Doubles.toArray(valueLists.get(varY));

			dataSetPlotable = xs.length == ys.length && xs.length != 0 && IntStream.rangeClosed(0, xs.length)
					.anyMatch(i -> Double.isFinite(xs[i]) && Double.isFinite(ys[i]));
		} catch (NullPointerException e) {
			dataSetPlotable = false;
		}

		boolean functionPlotable = function != null && functionValue.equals(varY) && variables.containsKey(varX)
				&& parameters.values().stream().allMatch(v -> v != null && Double.isFinite(v));

		switch (type) {
		case DATASET:
		case DATASET_MANY:
			return dataSetPlotable;
		case FUNCTION:
		case FUNCTION_SAMPLE:
			return functionPlotable;
		case BOTH:
		case BOTH_MANY:
			return dataSetPlotable && functionPlotable;
		default:
			throw new RuntimeException("Unknown type of Plotable: " + type);
		}
	}

	private boolean isOutOfRange() {
		Map<String, Double> params = getAllParameters();

		for (Map.Entry<String, Double> entry : params.entrySet()) {
			String param = entry.getKey();
			Double value = entry.getValue();

			if (value != null) {
				Double min = minValues.get(param);
				Double max = maxValues.get(param);

				if ((min != null && value < min) || (max != null && value > max)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean covarianceMatrixMissing() {
		Set<String> params = getAllParameters().keySet();

		for (String param : params) {
			Map<String, Double> paramCovs = covariances.get(param);

			if (paramCovs == null || params.stream().anyMatch(p -> paramCovs.get(p) == null)) {
				return true;
			}
		}

		return false;
	}

	private Map<String, Double> createParserConstants(String varX, Map<String, Integer> choice) {
		if (constants.values().contains(null) || parameters.values().contains(null)) {
			return null;
		}

		Map<String, Double> parserConstants = new LinkedHashMap<>();

		parserConstants.putAll(constants);
		parserConstants.putAll(parameters);
		parserConstants.putAll(variableParameters);

		variables.forEach((var, values) -> {
			if (!var.equals(varX)) {
				parserConstants.put(var, values.get(choice.get(var)));
			}
		});

		return parserConstants;
	}

	public static class Variable {

		private String name;
		private PmmUnit unit;
		private Transform transform;

		public Variable(String name, PmmUnit unit, Transform transform) {
			this.name = name;
			this.unit = unit;
			this.transform = transform;
		}

		public String getName() {
			return name;
		}

		public PmmUnit getUnit() {
			return unit;
		}

		public Transform getTransform() {
			return transform;
		}

		public String getDisplayString() {
			String s = transform.getName(name);

			return unit != null ? s + " [" + transform.getName(unit.toString()) + "]" : s;
		}

		public double to(double value, PmmUnit fromUnit) throws UnitException {
			return transform.to(PmmUtils.convertTo(value, fromUnit, unit));
		}

		public double from(double value, PmmUnit toUnit) throws UnitException {
			return PmmUtils.convertFrom(transform.from(value), unit, toUnit);
		}
	}
}
