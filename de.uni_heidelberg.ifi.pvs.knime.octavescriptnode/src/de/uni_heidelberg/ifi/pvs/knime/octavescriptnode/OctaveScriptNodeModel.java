/*
 * OctaveScriptNode - A KNIME node that runs Octave scripts
 * Copyright (C) 2011 Andre-Patrick Bubel (pvs@andre-bubel.de) and
 *                    Parallel and Distributed Systems Group (PVS),
 *                    University of Heidelberg, Germany
 * Website: http://pvs.ifi.uni-heidelberg.de/
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
 */
package de.uni_heidelberg.ifi.pvs.knime.octavescriptnode;

import java.io.File;
import java.io.IOException;

import org.knime.base.data.append.column.AppendedColumnTable;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.workflow.FlowVariable;

import dk.ange.octave.type.OctaveDouble;
import dk.ange.octave.type.OctaveObject;
import dk.ange.octave.type.OctaveString;
import dk.ange.octave.type.OctaveStruct;

/**
 * This is the model implementation of OctaveScriptNode.
 * 
 * @author André-Patrick Bubel <code@andre-bubel.de>
 * 
 */
public class OctaveScriptNodeModel extends NodeModel {

	// internal names of setting
	public static final String APPEND_COLS = "append_columns";
	public static final String COLUMN_NAMES = "new_column_names";
	public static final String COLUMN_TYPES = "new_column_types";

	// names of the in and out variable for the octave script
	public static final String IN_VARIABLE_NAME = "in";
	public static final String OUT_VARIABLE_NAME = "out";

	// in port ID
	private static final int IN_PORT = 0;

	// the logger instance
	private static final NodeLogger LOGGER = NodeLogger
			.getLogger(OctaveScriptNodeModel.class);

	// out port ID
	private static final int OUT_PORT = 0;

	// settings
	private boolean m_appendCols;
	private String[] m_columnNames;
	private String[] m_columnTypes;
	// the octave script that will be executed
	private String m_octaveCommand;

	private String m_latestOctaveError;
	private String m_latestOctaveOutput;

	public String getLatestOctaveError() {
		return m_latestOctaveError;
	}

	public String getLatestOctaveOutput() {
		return m_latestOctaveOutput;
	}

	/**
	 * Constructor for the node model.
	 */
	protected OctaveScriptNodeModel() {
		// one incoming port and one outgoing port
		super(1, 1);
	}

	/**
	 * Returns the current configured Octave script as entered into the
	 * configuration field
	 * 
	 * @return the configured Octave script
	 */
	public String getOctaveCommand() {
		return m_octaveCommand;
	}

	/**
	 * Add flow variables to the runner instance.
	 * 
	 * The variable name is checked for validity. Only alphanumerical characters
	 * are allowed. Invalid variables are discarded.
	 * 
	 * Overwriting already defined or global variables may be still possible.
	 * 
	 * (TODO: may be better placed in Runner)
	 * 
	 * @param runner
	 * @param runnerVariableName
	 * @throws NotConfigurableException
	 */
	private void addFlowVariablesToRunner(OctaveScriptRunner runner,
			String runnerVariableName) throws NotConfigurableException {
		OctaveStruct struct = new OctaveStruct();

		for (String variableName : getAvailableFlowVariables().keySet()) {
			if (variableName.matches("[a-zA-Z0-9]*")) {
				struct.set(variableName,
						flowVariableToOctaveObject(getAvailableFlowVariables()
								.get(variableName)));
			} else {
				LOGGER.warn("Variable " + variableName
						+ " contains non-alphanumerical characters");
			}
		}
		runner.addInVariable(runnerVariableName, struct);
	}

	/**
	 * Checks the result for the correct format
	 * 
	 * Throws exception if it is invalid
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void checkResult(OctaveObject result) throws Exception {
		if (!(result instanceof OctaveStruct))
			throw new InvalidSettingsException(
					"The out variable needs to be a nx1 cell with a nested struct, whose keys are the table column names");
	}

	private OctaveObject flowVariableToOctaveObject(FlowVariable flowVariable)
			throws NotConfigurableException {
		if (flowVariable.getType() == FlowVariable.Type.DOUBLE) {
			double[] d = { flowVariable.getDoubleValue() };
			return new OctaveDouble(d, 1, 1);
		} else if (flowVariable.getType() == FlowVariable.Type.INTEGER) {
			double[] d = { flowVariable.getIntValue() };
			return new OctaveDouble(d, 1, 1);
		} else if (flowVariable.getType() == FlowVariable.Type.STRING)
			return new OctaveString(flowVariable.getStringValue());
		else
			throw new NotConfigurableException(flowVariable.getType()
					.toString() + " type not supported");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		// append the property columns to the data table spec
		DataTableSpec newSpec = m_appendCols ? inSpecs[0] : new DataTableSpec();

		if (m_columnNames == null)
			return new DataTableSpec[] { newSpec };

		for (int i = 0; i < m_columnNames.length; i++) {
			DataType type = StringCell.TYPE;
			String columnType = m_columnTypes[i];

			if ("String".equals(columnType)) {
				type = StringCell.TYPE;
			} else if ("Integer".equals(columnType)) {
				type = IntCell.TYPE;
			} else if ("Double".equals(columnType)) {
				type = DoubleCell.TYPE;
			}
			DataColumnSpec newColumn = new DataColumnSpecCreator(
					m_columnNames[i], type).createSpec();

			newSpec = AppendedColumnTable.getTableSpec(newSpec, newColumn);
		}

		return new DataTableSpec[] { newSpec };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		DataTableSpec[] outSpecs = configure(new DataTableSpec[] { inData[IN_PORT]
				.getDataTableSpec() });

		exec.setProgress(0.0, "Convert the table to an Octave struct");
		OctaveStruct struct = (new KNIMETableToOctaveStructConverter(
				inData[IN_PORT])).convert();

		exec.setProgress(0.25, "Starting the Octave interpreter");
		OctaveScriptRunner runner = new OctaveScriptRunner(OUT_VARIABLE_NAME);
		runner.addInVariable(IN_VARIABLE_NAME, struct);
		addFlowVariablesToRunner(runner, "vars");

		exec.setProgress(0.50, "Running the Octave interpreter");
		OctaveObject result = runner.run(getOctaveCommand());

		exec.setProgress(0.75, "Convert Octave output to table");
		checkResult(result);
		OctaveStructToKNIMETableConverter converter = new OctaveStructToKNIMETableConverter(
				outSpecs[OUT_PORT], exec);

		BufferedDataTable[] resultTable = new BufferedDataTable[] { converter
				.convert((OctaveStruct) result) };

		exec.setProgress(1.0, "Finished");

		this.m_latestOctaveOutput = runner.getLastOutput();

		return resultTable;
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		// super.loadValidatedSettingsFrom(settings);
		m_octaveCommand = OctaveScriptNodeScriptEditorPanel
				.getExpressionFrom(settings);

		m_appendCols = settings.getBoolean(APPEND_COLS, true);
		m_columnNames = settings.getStringArray(COLUMN_NAMES);
		m_columnTypes = settings.getStringArray(COLUMN_TYPES);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// TODO Code executed on reset.
		// Models build during execute are cleared here.
		// Also data handled in load/saveInternals will be erased here.
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		// super.saveSettingsTo(settings);
		OctaveScriptNodeScriptEditorPanel.setExpressionTo(settings,
				m_octaveCommand);
		settings.addBoolean(APPEND_COLS, m_appendCols);
		settings.addStringArray(COLUMN_NAMES, m_columnNames);
		settings.addStringArray(COLUMN_TYPES, m_columnTypes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		settings.getStringArray(COLUMN_NAMES);
		settings.getStringArray(COLUMN_TYPES);
	}

}
