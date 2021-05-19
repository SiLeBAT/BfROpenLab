/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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

import java.util.List;
import java.util.Map;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.text.parser.ParseException;

public class LodFunction implements MultivariateFunction {

	private List<String> parameters;
	private Map<String, List<Double>> variableValues;
	private List<Double> targetValues;
	private double levelOfDetection;
	private String sdParam;

	private int nParams;
	private int nValues;
	private Parser parser;
	private ASTNode function;

	public LodFunction(String formula, List<String> parameters, Map<String, List<Double>> variableValues,
			List<Double> targetValues, double levelOfDetection, String sdParam) throws ParseException {
		this.parameters = parameters;
		this.variableValues = variableValues;
		this.targetValues = targetValues;
		this.levelOfDetection = levelOfDetection;
		this.sdParam = sdParam;

		nParams = parameters.size();
		nValues = targetValues.size();
		parser = new Parser();
		function = parser.parse(formula);
	}

	@Override
	public double value(double[] point) {
		double sd = Double.NaN;

		for (int ip = 0; ip < nParams; ip++) {
			if (parameters.get(ip).equals(sdParam)) {
				sd = Math.abs(point[ip]);
			} else {
				parser.setVarValue(parameters.get(ip), point[ip]);
			}
		}

		if (sd == 0.0) {
			return Double.NaN;
		}

		double logLikelihood = 0.0;

		for (int iv = 0; iv < nValues; iv++) {
			for (Map.Entry<String, List<Double>> entry : variableValues.entrySet()) {
				parser.setVarValue(entry.getKey(), entry.getValue().get(iv));
			}

			try {
				double value = parser.evaluate(function);

				if (!Double.isFinite(value)) {
					return Double.NaN;
				}

				NormalDistribution normDist = new NormalDistribution(value, sd);

				logLikelihood += targetValues.get(iv) > levelOfDetection
						? Math.log(normDist.density(targetValues.get(iv)))
						: Math.log(normDist.cumulativeProbability(levelOfDetection));
			} catch (ParseException e) {
				e.printStackTrace();
				return Double.NaN;
			}
		}

		return logLikelihood;
	}
}
