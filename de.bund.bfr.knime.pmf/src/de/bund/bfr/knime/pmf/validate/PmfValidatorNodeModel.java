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
 ******************************************************************************/
package de.bund.bfr.knime.pmf.validate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.numl.ConformityMessage;
import de.bund.bfr.pmf.PMFReader;

/**
 * This is the model implementation of PmfValidator.
 * 
 *
 * @author Christian Thoens
 */
public class PmfValidatorNodeModel extends NodeModel {

	protected static final String PMF_FILE = "PmfFile";

	private SettingsModelString pmfFile;

	/**
	 * Constructor for the node model.
	 */
	protected PmfValidatorNodeModel() {
		super(0, 1);
		pmfFile = new SettingsModelString(PMF_FILE, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		DataTableSpec spec = configure(null)[0];
		BufferedDataContainer container = exec.createDataContainer(spec);

		File file = KnimeUtils.getFile(pmfFile.getStringValue());
		PMFReader reader = new PMFReader();
		int index = 1;

		reader.setValidating(true);
		reader.read(file);

		for (ConformityMessage message : reader.getMessages()) {
			container.addRowToTable(new DefaultRow(String.valueOf(index++),
					new StringCell(message.toString())));
			exec.checkCanceled();
			exec.setProgress((double) index
					/ (double) reader.getMessages().size());
		}
		reader.getMessages().get(0).toString();

		container.close();

		return new BufferedDataTable[] { container.getTable() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		List<DataColumnSpec> columns = new ArrayList<>();

		columns.add(new DataColumnSpecCreator("Messages", StringCell.TYPE)
				.createSpec());

		return new DataTableSpec[] { new DataTableSpec(
				columns.toArray(new DataColumnSpec[0])) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		pmfFile.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		pmfFile.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		pmfFile.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

}
