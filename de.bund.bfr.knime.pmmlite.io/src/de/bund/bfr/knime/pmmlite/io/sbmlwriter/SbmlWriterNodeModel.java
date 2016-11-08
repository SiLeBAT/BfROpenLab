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
package de.bund.bfr.knime.pmmlite.io.sbmlwriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.ext.comp.CompConstants;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.NodeModelWithoutInternals;
import de.bund.bfr.knime.pmmlite.core.AssignUtils;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.SbmlUtils;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.core.models.ModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.Parameter;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModel;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.Variable;
import de.bund.bfr.knime.pmmlite.core.models.VariableRange;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;
import de.bund.bfr.math.MathUtils;
import de.bund.bfr.pmfml.ModelClass;
import de.bund.bfr.pmfml.ModelType;
import de.bund.bfr.pmfml.file.PrimaryModelWODataFile;
import de.bund.bfr.pmfml.model.PrimaryModelWOData;
import de.bund.bfr.pmfml.sbml.LimitsConstraint;
import de.bund.bfr.pmfml.sbml.MetadataAnnotation;
import de.bund.bfr.pmfml.sbml.Model1Annotation;
import de.bund.bfr.pmfml.sbml.ModelRule;
import de.bund.bfr.pmfml.sbml.PMFUnitDefinition;
import de.bund.bfr.pmfml.sbml.Reference;
import de.bund.bfr.pmfml.sbml.SBMLFactory;

/**
 * This is the model implementation of SbmlWriter.
 * 
 * 
 * @author Christian Thoens
 */
public class SbmlWriterNodeModel extends NodeModelWithoutInternals {

	protected static final String CFG_OUT_PATH = "OutPath";

	private SettingsModelString outPath;

	/**
	 * Constructor for the node model.
	 */
	protected SbmlWriterNodeModel() {
		super(new PortType[] { PmmPortObject.TYPE }, new PortType[] {});
		outPath = new SettingsModelString(CFG_OUT_PATH, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		PmmPortObject input = (PmmPortObject) inObjects[0];

		for (PrimaryModel model : input.getData(PrimaryModel.class)) {
			AssignUtils.applyAssignmentsAndConversion(model);

			PrimaryModelFormula formula = model.getFormula();
			Map<String, PMFUnitDefinition> allUnits = getAllUnits(formula);
			Variable dep = formula.getDepVar();
			Variable indep = formula.getIndepVar();
			TimeSeries data = model.getData();

			SBMLDocument sbmlDocument = new SBMLDocument(3, 1);

			// TODO: Should be done in the pmf library
			sbmlDocument.enablePackage(CompConstants.shortLabel);
			sbmlDocument.addDeclaredNamespace("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			sbmlDocument.addDeclaredNamespace("xmlns:pmml", "http://www.dmg.org/PMML-4_2");
			sbmlDocument.addDeclaredNamespace("xmlns:pmf",
					"http://sourceforge.net/projects/microbialmodelingexchange/files/PMF-ML");
			sbmlDocument.addDeclaredNamespace("xmlns:dc", "http://purl.org/dc/elements/1.1");
			sbmlDocument.addDeclaredNamespace("xmlns:dcterms", "http://purl.org/dc/terms/");
			sbmlDocument.addDeclaredNamespace("xmlns:pmmlab",
					"http://sourceforge.net/projects/microbialmodelingexchange/files/PMF-ML");
			sbmlDocument.addDeclaredNamespace("xmlns:numl", "http://www.numl.org/numl/level1/version1");
			sbmlDocument.addDeclaredNamespace("xmlns:xlink", "http//www.w3.org/1999/xlink");

			sbmlDocument.setAnnotation(new MetadataAnnotation(SBMLFactory.createMetadata("Jim", "Gun", "jim@gun.com",
					"2016-01-01", "2016-01-01", ModelType.PRIMARY_MODEL_WODATA, "All rights", "jim-gun.com"))
							.getAnnotation());

			org.sbml.jsbml.Model sbmlModel = sbmlDocument.createModel(model.getId());

			for (PMFUnitDefinition unit : allUnits.values()) {
				sbmlModel.addUnitDefinition(unit.getUnitDefinition());
			}

			sbmlModel.setName(model.getName());
			sbmlModel
					.setAnnotation(new Model1Annotation(
							SBMLFactory.createUncertainties(null, null, null, model.getR2(), model.getRmse(),
									model.getSse(), model.getAic(), null, model.getDegreesOfFreedom()),
							new ArrayList<>(), data.getId()).getAnnotation());

			String compartment = data.getMatrix() != null ? data.getMatrix() : SbmlUtils.COMPARTMENT_MISSING;
			String species = data.getOrganism() != null ? data.getOrganism() : SbmlUtils.SPECIES_MISSING;

			sbmlModel.addCompartment(
					SBMLFactory.createPMFCompartment(PmmUtils.createId(compartment), compartment).getCompartment());
			// TODO: setTransformationName is ignored by pmf library
			sbmlModel.addSpecies(SBMLFactory.createPMFSpecies(PmmUtils.createId(compartment),
					PmmUtils.createId(species), species, allUnits.get(dep.getUnit().toString()).getId()).getSpecies());

			VariableRange indepRange = model.getVariableRanges().get(indep.getName());
			org.sbml.jsbml.Parameter sbmlIndep = sbmlModel.createParameter(SbmlUtils.TIME);
			sbmlIndep.setValue(0.0);
			sbmlIndep.setConstant(false);
			sbmlIndep.setUnits(allUnits.get(indep.getUnit().toString()).getId());
			sbmlModel.addConstraint(
					new LimitsConstraint(SbmlUtils.TIME, indepRange.getMin(), indepRange.getMax()).getConstraint());

			for (Parameter param : formula.getParams()) {
				org.sbml.jsbml.Parameter sbmlParam = sbmlModel.createParameter(param.getName());

				sbmlParam.setValue(model.getParamValues().get(param.getName()).getValue());
				sbmlParam.setConstant(true);
			}

			// TODO: pmmlabID should not be int
			sbmlModel.addRule(new ModelRule(PmmUtils.createId(species),
					MathUtils.replaceVariable(formula.getExpression(), PmmUtils.TIME, SbmlUtils.TIME),
					formula.getName(), ModelClass.UNKNOWN, 0, new Reference[0]).getRule());

			// TODO: SBML has no file ending
			PrimaryModelWODataFile.writePMFX(KnimeUtils.getFile(outPath.getStringValue()).getPath(), model.getId(),
					Arrays.asList(new PrimaryModelWOData(model.getName(), sbmlDocument)));
		}

		return new PortObject[] {};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		PmmPortObjectSpec spec = (PmmPortObjectSpec) inSpecs[0];

		if (spec != PmmPortObjectSpec.PRIMARY_MODEL_TYPE) {
			throw new InvalidSettingsException("Wrong input");
		}

		return new PortObjectSpec[] {};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		outPath.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		outPath.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		outPath.validateSettings(settings);
	}

	private static Map<String, PMFUnitDefinition> getAllUnits(ModelFormula formula) {
		Map<String, PMFUnitDefinition> units = new LinkedHashMap<>();
		PMFUnitDefinition depUnit = formula.getDepVar().getUnit().toPmfUnit();

		depUnit.setId("ID" + units.size());
		units.put(formula.getDepVar().getUnit().toString(), depUnit);

		for (Variable v : PmmUtils.getIndependentVariables(formula)) {
			if (!units.containsKey(v.getUnit().toString())) {
				PMFUnitDefinition pmfUnit = v.getUnit().toPmfUnit();

				pmfUnit.setId("ID" + units.size());
				units.put(v.getUnit().toString(), pmfUnit);
			}
		}

		return units;
	}
}
