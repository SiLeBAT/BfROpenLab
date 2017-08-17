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
package de.bund.bfr.knime.pmmlite.io.datareader;

import java.util.List;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import de.bund.bfr.knime.NoInternalsNodeModel;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;
import de.bund.bfr.knime.pmmlite.io.XlsReader;

/**
 * This is the model implementation of XlsDataReader.
 * 
 * 
 * @author Christian Thoens
 */
public class XlsDataReaderNodeModel extends NoInternalsNodeModel {

	private XlsDataReaderSettings set;

	/**
	 * Constructor for the node model.
	 */
	protected XlsDataReaderNodeModel() {
		super(new PortType[] {}, new PortType[] { PmmPortObject.TYPE });
		set = new XlsDataReaderSettings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		XlsReader xlsReader = new XlsReader();

		xlsReader.setFile(set.getFileName());
		xlsReader.setSheet(set.getSheetName());

		List<TimeSeries> tuples = xlsReader.getTimeSeriesList(set.getIdColumn(), set.getTimeColumn(),
				set.getConcentrationColumn(), set.getOrganismColumn(), set.getMatrixColumn(), set.getConditionColumns(),
				set.getUnits());

		for (String warning : xlsReader.getWarnings()) {
			setWarningMessage(warning);
		}

		return new PortObject[] { PmmPortObject.createListObject(tuples, PmmPortObjectSpec.DATA_TYPE) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		return new PortObjectSpec[] { PmmPortObjectSpec.DATA_TYPE };
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
