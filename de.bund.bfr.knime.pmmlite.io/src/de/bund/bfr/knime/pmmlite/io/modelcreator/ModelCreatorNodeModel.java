/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.io.modelcreator;

import java.util.Map;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import de.bund.bfr.knime.NodeModelWithoutInternals;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit;
import de.bund.bfr.knime.pmmlite.core.data.Condition;
import de.bund.bfr.knime.pmmlite.core.data.DataFactory;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.core.models.Model;
import de.bund.bfr.knime.pmmlite.core.models.ModelsFactory;
import de.bund.bfr.knime.pmmlite.core.models.ParameterValue;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModel;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.TertiaryModel;
import de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.Variable;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;

/**
 * This is the model implementation of ModelCreator.
 * 
 *
 * @author Christian Thoens
 */
public class ModelCreatorNodeModel extends NodeModelWithoutInternals {

	private ModelCreatorSettings set;

	/**
	 * Constructor for the node model.
	 */
	protected ModelCreatorNodeModel() {
		super(new PortType[] { PmmPortObject.TYPE }, new PortType[] { PmmPortObject.TYPE });
		set = new ModelCreatorSettings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		PmmPortObject input = (PmmPortObject) inObjects[0];
		Model model = null;
		PmmPortObjectSpec spec = null;

		switch (input.getSpec()) {
		case PRIMARY_MODEL_FORMULA_TYPE:
			model = createModel(input.getData(PrimaryModelFormula.class).get(0));
			spec = PmmPortObjectSpec.PRIMARY_MODEL_TYPE;
			break;
		case TERTIARY_MODEL_FORMULA_TYPE:
			model = createModel(input.getData(TertiaryModelFormula.class).get(0));
			spec = PmmPortObjectSpec.TERTIARY_MODEL_TYPE;
			break;
		case DATA_TYPE:
		case EMPTY_TYPE:
		case PRIMARY_MODEL_TYPE:
		case SECONDARY_MODEL_FORMULA_TYPE:
		case SECONDARY_MODEL_TYPE:
		case TERTIARY_MODEL_TYPE:
			throw new InvalidSettingsException("Unsupported input type: " + spec);
		default:
			throw new RuntimeException("Unknown input type: " + spec);
		}

		return new PortObject[] { PmmPortObject.createObject(model, spec) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		PmmPortObjectSpec spec = (PmmPortObjectSpec) inSpecs[0];

		switch (spec) {
		case PRIMARY_MODEL_FORMULA_TYPE:
			return new PortObjectSpec[] { PmmPortObjectSpec.PRIMARY_MODEL_TYPE };
		case TERTIARY_MODEL_FORMULA_TYPE:
			return new PortObjectSpec[] { PmmPortObjectSpec.TERTIARY_MODEL_TYPE };
		case DATA_TYPE:
		case EMPTY_TYPE:
		case PRIMARY_MODEL_TYPE:
		case SECONDARY_MODEL_FORMULA_TYPE:
		case SECONDARY_MODEL_TYPE:
		case TERTIARY_MODEL_TYPE:
			throw new InvalidSettingsException("Unsupported input type: " + spec);
		default:
			throw new RuntimeException("Unknown input type: " + spec);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		set.save(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		set.load(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
	}

	private PrimaryModel createModel(PrimaryModelFormula formula) {
		TimeSeries series = DataFactory.eINSTANCE.createTimeSeries();

		series.setName(set.getId());
		series.setOrganism(set.getOrganism());
		series.setMatrix(set.getMatrix());

		Map<String, NameableWithUnit> unitsByName = PmmUtils.getByName(set.getUnits());

		for (String condName : set.getConditions().keySet()) {
			Condition condition = DataFactory.eINSTANCE.createCondition();

			condition.setName(PmmUtils.createMathSymbol(condName));
			condition.setValue(set.getConditions().get(condName));
			condition.setUnit(unitsByName.get(condName).getUnit());
			series.getConditions().add(condition);
		}

		PrimaryModel model = ModelsFactory.eINSTANCE.createPrimaryModel();

		model.setName(set.getId());
		model.setFormula(formula);
		model.setData(series);
		model.getAssignments().put(formula.getDepVar().getName(), PmmUtils.CONCENTRATION);
		model.getAssignments().put(formula.getIndepVar().getName(), PmmUtils.TIME);

		for (String paramName : set.getParameters().keySet()) {
			ParameterValue value = ModelsFactory.eINSTANCE.createParameterValue();

			value.setValue(set.getParameters().get(paramName));
			model.getParamValues().put(paramName, value);
		}

		PmmUtils.setId(series);
		PmmUtils.setId(model);

		return model;
	}

	private TertiaryModel createModel(TertiaryModelFormula formula) {
		TimeSeries series = DataFactory.eINSTANCE.createTimeSeries();

		series.setName(set.getId());
		series.setOrganism(set.getOrganism());
		series.setMatrix(set.getMatrix());

		TertiaryModel model = ModelsFactory.eINSTANCE.createTertiaryModel();

		model.setName(set.getId());
		model.setFormula(formula);
		model.getData().add(series);
		model.getAssignments().put(formula.getDepVar().getName(), PmmUtils.CONCENTRATION);
		model.getAssignments().put(formula.getTimeVar(), PmmUtils.TIME);

		for (Variable var : formula.getIndepVars()) {
			if (!var.getName().equals(formula.getTimeVar())) {
				model.getAssignments().put(var.getName(), var.getName());
			}
		}

		for (String paramName : set.getParameters().keySet()) {
			ParameterValue value = ModelsFactory.eINSTANCE.createParameterValue();

			value.setValue(set.getParameters().get(paramName));
			model.getParamValues().put(paramName, value);
		}

		PmmUtils.setId(series);
		PmmUtils.setId(model);

		return model;
	}
}
