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
package de.bund.bfr.knime.pmmlite.io.sbmlreader;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.SBMLDocument;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.NodeModelWithoutInternals;
import de.bund.bfr.knime.pmmlite.core.PmmUnit;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.SbmlUtils;
import de.bund.bfr.knime.pmmlite.core.data.DataFactory;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.core.models.ModelsFactory;
import de.bund.bfr.knime.pmmlite.core.models.Parameter;
import de.bund.bfr.knime.pmmlite.core.models.ParameterValue;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModel;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.Variable;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;
import de.bund.bfr.math.MathUtils;
import de.bund.bfr.pmfml.file.PrimaryModelWODataFile;
import de.bund.bfr.pmfml.sbml.Model1Annotation;
import de.bund.bfr.pmfml.sbml.ModelRule;
import de.bund.bfr.pmfml.sbml.PMFCompartment;
import de.bund.bfr.pmfml.sbml.PMFCompartmentImpl;
import de.bund.bfr.pmfml.sbml.PMFSpecies;
import de.bund.bfr.pmfml.sbml.PMFSpeciesImpl;
import de.bund.bfr.pmfml.sbml.PMFUnitDefinition;

/**
 * This is the model implementation of SbmlReader.
 * 
 * 
 * @author Christian Thoens
 */
public class SbmlReaderNodeModel extends NodeModelWithoutInternals {

	protected static final String CFG_IN_PATH = "InPath";

	private SettingsModelString inPath;

	/**
	 * Constructor for the node model.
	 */
	protected SbmlReaderNodeModel() {
		super(new PortType[] {}, new PortType[] { PmmPortObject.TYPE });
		inPath = new SettingsModelString(CFG_IN_PATH, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		List<File> files = Files.list(KnimeUtils.getFile(inPath.getStringValue()).toPath())
				.filter(f -> f.toString().endsWith(".pmfx")).map(f -> f.toFile()).collect(Collectors.toList());
		List<PrimaryModel> models = new ArrayList<>();

		for (File f : files) {
			SBMLDocument sbmlDoc = PrimaryModelWODataFile.readPMFX(f).get(0).getDoc();
			org.sbml.jsbml.Model sbmlModel = sbmlDoc.getModel();

			Model1Annotation modelAnnotation = new Model1Annotation(sbmlModel.getAnnotation());
			PMFCompartment compartment = new PMFCompartmentImpl(sbmlModel.getCompartment(0));
			PMFSpecies species = new PMFSpeciesImpl(sbmlModel.getSpecies(0));
			ModelRule rule = new ModelRule((AssignmentRule) sbmlModel.getRule(0));
			PMFUnitDefinition depUnit = new PMFUnitDefinition(sbmlModel.getUnitDefinition(species.getUnits()));
			PMFUnitDefinition indepUnit = new PMFUnitDefinition(
					sbmlModel.getParameter(SbmlUtils.TIME).getUnitsInstance());

			PrimaryModel model = ModelsFactory.eINSTANCE.createPrimaryModel();
			PrimaryModelFormula formula = ModelsFactory.eINSTANCE.createPrimaryModelFormula();
			TimeSeries data = DataFactory.eINSTANCE.createTimeSeries();
			Variable dep = ModelsFactory.eINSTANCE.createVariable();
			Variable indep = ModelsFactory.eINSTANCE.createVariable();

			data.setName(modelAnnotation.getCondID());
			data.setOrganism(species.getName());
			data.setMatrix(compartment.getName());
			dep.setName(PmmUtils.CONCENTRATION);
			dep.setUnit(new PmmUnit.Builder().fromPmfUnit(depUnit).build());
			indep.setName(PmmUtils.TIME);
			indep.setUnit(new PmmUnit.Builder().fromPmfUnit(indepUnit).build());
			formula.setName(rule.getFormulaName());
			formula.setExpression(MathUtils.replaceVariable(rule.getFormula(), SbmlUtils.TIME, PmmUtils.TIME));
			formula.setDepVar(dep);
			formula.setIndepVar(indep);
			model.setData(data);
			model.setFormula(formula);
			model.setName(sbmlModel.getId());
			model.getAssignments().put(PmmUtils.CONCENTRATION, PmmUtils.CONCENTRATION);
			model.getAssignments().put(PmmUtils.TIME, PmmUtils.TIME);

			for (org.sbml.jsbml.Parameter p : sbmlModel.getListOfParameters()) {
				if (p.isConstant()) {
					Parameter param = ModelsFactory.eINSTANCE.createParameter();
					ParameterValue value = ModelsFactory.eINSTANCE.createParameterValue();

					param.setName(p.getId());
					value.setValue(p.getValue());
					formula.getParams().add(param);
					model.getParamValues().put(param.getName(), value);
				}
			}

			PmmUtils.setId(data);
			PmmUtils.setId(formula);
			PmmUtils.setId(model);
			models.add(model);
		}

		return new PortObject[] { PmmPortObject.createListObject(models, PmmPortObjectSpec.PRIMARY_MODEL_TYPE) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		return new PortObjectSpec[] { PmmPortObjectSpec.PRIMARY_MODEL_TYPE };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		inPath.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		inPath.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		inPath.validateSettings(settings);
	}
}
