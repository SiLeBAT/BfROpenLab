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
package de.bund.bfr.knime.pmmlite.util.join;

import java.io.File;
import java.io.IOException;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;

/**
 * This is the model implementation of PmmJoiner.
 * 
 * 
 * @author Christian Thoens
 */
public class JoinerNodeModel extends NodeModel {

	private JoinerSettings set;

	/**
	 * Constructor for the node model.
	 */
	protected JoinerNodeModel() {
		super(new PortType[] { PmmPortObject.TYPE, PmmPortObject.TYPE }, new PortType[] { PmmPortObject.TYPE });
		set = new JoinerSettings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		PmmPortObject in1 = (PmmPortObject) inObjects[0];
		PmmPortObject in2 = (PmmPortObject) inObjects[1];

		switch (set.getJoinType()) {
		case PRIMARY_JOIN:
			return new PortObject[] { new PrimaryJoiner(in1, in2).getOutput(set.getAssignments()) };
		case SECONDARY_JOIN:
			return new PortObject[] { new SecondaryJoiner(in1, in2).getOutput(set.getAssignments()) };
		case TERTIARY_JOIN:
			return new PortObject[] { new TertiaryJoiner(in1, in2).getOutput(set.getAssignments()) };
		case FORMULA_JOIN:
			return new PortObject[] { new FormulaJoiner(in1, in2).getOutput(set.getAssignments()) };
		default:
			throw new RuntimeException("Unknown join type: " + set.getJoinType());
		}
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
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		if (set.getJoinType() == null) {
			throw new InvalidSettingsException("Node has to be configured");
		}

		PmmPortObjectSpec in1 = (PmmPortObjectSpec) inSpecs[0];
		PmmPortObjectSpec in2 = (PmmPortObjectSpec) inSpecs[1];

		switch (set.getJoinType()) {
		case PRIMARY_JOIN:
			return new PortObjectSpec[] { PrimaryJoiner.getOutputType(in1, in2) };
		case SECONDARY_JOIN:
			return new PortObjectSpec[] { SecondaryJoiner.getOutputType(in1, in2) };
		case TERTIARY_JOIN:
			return new PortObjectSpec[] { TertiaryJoiner.getOutputType(in1, in2) };
		case FORMULA_JOIN:
			return new PortObjectSpec[] { FormulaJoiner.getOutputType(in1, in2) };
		default:
			throw new RuntimeException("Unknown join type: " + set.getJoinType());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		set.saveSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		set.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

}
