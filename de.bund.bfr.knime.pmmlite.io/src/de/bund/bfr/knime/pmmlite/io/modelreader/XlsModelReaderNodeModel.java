/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.io.modelreader;

import java.util.List;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import de.bund.bfr.knime.NoInternalsNodeModel;
import de.bund.bfr.knime.pmmlite.core.models.Model;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;
import de.bund.bfr.knime.pmmlite.io.XlsReader;

/**
 * This is the model implementation of XlsModelReader.
 * 
 * 
 * @author Christian Thoens
 */
public class XlsModelReaderNodeModel extends NoInternalsNodeModel {

	private XlsModelReaderSettings set;

	/**
	 * Constructor for the node model.
	 */
	protected XlsModelReaderNodeModel() {
		super(new PortType[] { PmmPortObject.TYPE }, new PortType[] { PmmPortObject.TYPE });
		set = new XlsModelReaderSettings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		PmmPortObject input = (PmmPortObject) inObjects[0];
		XlsReader xlsReader = new XlsReader();

		xlsReader.setFile(set.getFileName());
		xlsReader.setSheet(set.getSheetName());

		List<? extends Model> models = null;
		PmmPortObjectSpec spec = null;

		switch (input.getSpec()) {
		case PRIMARY_MODEL_FORMULA_TYPE:
			models = xlsReader.getPrimaryModels(input.getData(PrimaryModelFormula.class).get(0), set.getIdColumn(),
					set.getOrganismColumn(), set.getMatrixColumn(), set.getConditionColumns(), set.getUnits(),
					set.getParamColumns());
			spec = PmmPortObjectSpec.PRIMARY_MODEL_TYPE;
			break;
		case TERTIARY_MODEL_FORMULA_TYPE:
			models = xlsReader.getTertiaryModels(input.getData(TertiaryModelFormula.class).get(0), set.getIdColumn(),
					set.getOrganismColumn(), set.getMatrixColumn(), set.getParamColumns());
			spec = PmmPortObjectSpec.TERTIARY_MODEL_TYPE;
			break;
		case DATA_TYPE:
		case EMPTY_TYPE:
		case PRIMARY_MODEL_TYPE:
		case SECONDARY_MODEL_FORMULA_TYPE:
		case SECONDARY_MODEL_TYPE:
		case TERTIARY_MODEL_TYPE:
			throw new InvalidSettingsException("Unsupported input type: " + input.getSpec());
		default:
			throw new RuntimeException("Unknown input type: " + input.getSpec());
		}

		for (String warning : xlsReader.getWarnings()) {
			setWarningMessage(warning);
		}

		return new PortObject[] { PmmPortObject.createListObject(models, spec) };
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
}
