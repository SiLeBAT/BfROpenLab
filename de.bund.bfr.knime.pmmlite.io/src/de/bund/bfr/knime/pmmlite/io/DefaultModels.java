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
package de.bund.bfr.knime.pmmlite.io;

import java.util.ArrayList;
import java.util.List;

import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.models.ModelsFactory;
import de.bund.bfr.knime.pmmlite.core.models.Parameter;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.Variable;

public class DefaultModels {

	private static DefaultModels instance = null;

	private List<PrimaryModelFormula> primaryModels;
	private List<SecondaryModelFormula> secondaryModels;
	private PrimaryModelFormula linearModel;

	private DefaultModels() {
		linearModel = createLinearModel();

		primaryModels = new ArrayList<>();
		primaryModels.add(createBaranyiModel());
		primaryModels.add(createGompertzModel());
		primaryModels.add(linearModel);

		secondaryModels = new ArrayList<>();
		secondaryModels.add(createTwoVarQuadraticModel());
		secondaryModels.add(createTransformedSquareRootModel());
	}

	public static DefaultModels getInstance() {
		if (instance == null) {
			instance = new DefaultModels();
		}

		return instance;
	}

	public List<PrimaryModelFormula> getPrimaryModels() {
		return primaryModels;
	}

	public List<SecondaryModelFormula> getSecondaryModels() {
		return secondaryModels;
	}

	public PrimaryModelFormula getLinearModel() {
		return linearModel;
	}

	private static PrimaryModelFormula createBaranyiModel() {
		String formula = "y0+muMax*t" + "+ln(exp(-muMax*t)+exp(-muMax*lag)-exp(-muMax*(t+lag)))"
				+ "-ln(1+(exp(muMax*(t-lag))-exp(-muMax*lag))/(exp(yMax-y0)))";
		Variable y = ModelsFactory.eINSTANCE.createVariable();
		Variable t = ModelsFactory.eINSTANCE.createVariable();
		Parameter y0 = ModelsFactory.eINSTANCE.createParameter();
		Parameter yMax = ModelsFactory.eINSTANCE.createParameter();
		Parameter muMax = ModelsFactory.eINSTANCE.createParameter();
		Parameter lag = ModelsFactory.eINSTANCE.createParameter();

		y.setName("y");
		t.setName("t");
		y0.setName("y0");
		y0.setMin(0.0);
		y0.setMax(23.0);
		yMax.setName("yMax");
		yMax.setMin(0.0);
		yMax.setMax(23.0);
		muMax.setName("muMax");
		muMax.setMin(0.0);
		muMax.setMax(10.0);
		lag.setName("lag");
		lag.setMin(0.0);
		lag.setMax(100.0);

		PrimaryModelFormula model = ModelsFactory.eINSTANCE.createPrimaryModelFormula();
		List<Parameter> params = new ArrayList<>();

		params.add(y0);
		params.add(yMax);
		params.add(muMax);
		params.add(lag);
		PmmUtils.sortByName(params);

		model.setName("Baranyi");
		model.setExpression(formula);
		model.setDepVar(y);
		model.setIndepVar(t);
		model.getParams().addAll(params);
		model.setInitialParam(y0.getName());
		PmmUtils.setId(model);

		return model;
	}

	private static PrimaryModelFormula createGompertzModel() {
		String formula = "(yMax-y0)" + "*exp(-exp((muMax*exp(1)*(lag-t))/(yMax-y0)+1))";
		Variable y = ModelsFactory.eINSTANCE.createVariable();
		Variable t = ModelsFactory.eINSTANCE.createVariable();
		Parameter y0 = ModelsFactory.eINSTANCE.createParameter();
		Parameter yMax = ModelsFactory.eINSTANCE.createParameter();
		Parameter muMax = ModelsFactory.eINSTANCE.createParameter();
		Parameter lag = ModelsFactory.eINSTANCE.createParameter();

		y.setName("y");
		t.setName("t");
		y0.setName("y0");
		y0.setMin(0.0);
		y0.setMax(23.0);
		yMax.setName("yMax");
		yMax.setMin(0.0);
		yMax.setMax(23.0);
		muMax.setName("muMax");
		muMax.setMin(0.0);
		muMax.setMax(10.0);
		lag.setName("lag");
		lag.setMin(0.0);
		lag.setMax(100.0);

		PrimaryModelFormula model = ModelsFactory.eINSTANCE.createPrimaryModelFormula();
		List<Parameter> params = new ArrayList<>();

		params.add(y0);
		params.add(yMax);
		params.add(muMax);
		params.add(lag);
		PmmUtils.sortByName(params);

		model.setName("Gompertz");
		model.setExpression(formula);
		model.setDepVar(y);
		model.setIndepVar(t);
		model.getParams().addAll(params);
		model.setInitialParam(y0.getName());
		PmmUtils.setId(model);

		return model;
	}

	private static PrimaryModelFormula createLinearModel() {
		String formula = "y0+m*t";
		Variable y = ModelsFactory.eINSTANCE.createVariable();
		Variable t = ModelsFactory.eINSTANCE.createVariable();
		Parameter y0 = ModelsFactory.eINSTANCE.createParameter();
		Parameter m = ModelsFactory.eINSTANCE.createParameter();

		y.setName("y");
		t.setName("t");
		y0.setName("y0");
		y0.setMin(0.0);
		y0.setMax(23.0);
		m.setName("m");
		m.setMin(0.0);
		m.setMax(10.0);

		PrimaryModelFormula model = ModelsFactory.eINSTANCE.createPrimaryModelFormula();
		List<Parameter> params = new ArrayList<>();

		params.add(y0);
		params.add(m);
		PmmUtils.sortByName(params);

		model.setName("Linear Model");
		model.setExpression(formula);
		model.setDepVar(y);
		model.setIndepVar(t);
		model.getParams().addAll(params);
		model.setInitialParam(y0.getName());
		PmmUtils.setId(model);

		return model;
	}

	private static SecondaryModelFormula createTwoVarQuadraticModel() {
		String formula = "a*x1^2+b*x1*x2+c*x2^2+d*x1+e*x2+f";
		Variable y = ModelsFactory.eINSTANCE.createVariable();
		Variable x1 = ModelsFactory.eINSTANCE.createVariable();
		Variable x2 = ModelsFactory.eINSTANCE.createVariable();
		Parameter a = ModelsFactory.eINSTANCE.createParameter();
		Parameter b = ModelsFactory.eINSTANCE.createParameter();
		Parameter c = ModelsFactory.eINSTANCE.createParameter();
		Parameter d = ModelsFactory.eINSTANCE.createParameter();
		Parameter e = ModelsFactory.eINSTANCE.createParameter();
		Parameter f = ModelsFactory.eINSTANCE.createParameter();

		y.setName("y");
		x1.setName("x1");
		x2.setName("x2");
		a.setName("a");
		b.setName("b");
		c.setName("c");
		d.setName("d");
		e.setName("e");
		f.setName("f");

		SecondaryModelFormula model = ModelsFactory.eINSTANCE.createSecondaryModelFormula();
		List<Variable> indeps = new ArrayList<>();
		List<Parameter> params = new ArrayList<>();

		indeps.add(x1);
		indeps.add(x2);
		params.add(a);
		params.add(b);
		params.add(c);
		params.add(d);
		params.add(e);
		params.add(f);
		PmmUtils.sortByName(indeps);
		PmmUtils.sortByName(params);

		model.setName("2 Var Quadratic");
		model.setExpression(formula);
		model.setDepVar(y);
		model.getIndepVars().addAll(indeps);
		model.getParams().addAll(params);
		PmmUtils.setId(model);

		return model;
	}

	private static SecondaryModelFormula createTransformedSquareRootModel() {
		String formula = "(b*(T-Tmin))^2";
		Variable muMax = ModelsFactory.eINSTANCE.createVariable();
		Variable T = ModelsFactory.eINSTANCE.createVariable();
		Parameter b = ModelsFactory.eINSTANCE.createParameter();
		Parameter Tmin = ModelsFactory.eINSTANCE.createParameter();

		muMax.setName("muMax");
		T.setName("T");
		b.setName("b");
		Tmin.setName("Tmin");

		SecondaryModelFormula model = ModelsFactory.eINSTANCE.createSecondaryModelFormula();
		List<Variable> indeps = new ArrayList<>();
		List<Parameter> params = new ArrayList<>();

		indeps.add(T);
		params.add(b);
		params.add(Tmin);
		PmmUtils.sortByName(indeps);
		PmmUtils.sortByName(params);

		model.setName("Transformed Square Root Model");
		model.setExpression(formula);
		model.setDepVar(muMax);
		model.getIndepVars().addAll(indeps);
		model.getParams().addAll(params);
		PmmUtils.setId(model);

		return model;
	}
}
