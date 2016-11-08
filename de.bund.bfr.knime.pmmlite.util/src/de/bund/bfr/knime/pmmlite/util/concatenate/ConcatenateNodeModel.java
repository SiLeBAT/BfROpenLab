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
package de.bund.bfr.knime.pmmlite.util.concatenate;

import java.util.List;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import de.bund.bfr.knime.NodeModelWithoutInternals;
import de.bund.bfr.knime.pmmlite.core.common.Identifiable;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;

/**
 * This is the model implementation of Concatenate.
 * 
 * 
 * @author Christian Thoens
 */
public class ConcatenateNodeModel extends NodeModelWithoutInternals {

	/**
	 * Constructor for the node model.
	 */
	protected ConcatenateNodeModel() {
		super(new PortType[] { PmmPortObject.TYPE, PmmPortObject.TYPE_OPTIONAL, PmmPortObject.TYPE_OPTIONAL,
				PmmPortObject.TYPE_OPTIONAL }, new PortType[] { PmmPortObject.TYPE });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		PmmPortObject firstInput = (PmmPortObject) inObjects[0];
		List<Identifiable> data = firstInput.getData(Identifiable.class);

		for (int i = 1; i <= 3; i++) {
			PmmPortObject input = (PmmPortObject) inObjects[i];

			if (input != null) {
				data.addAll(input.getData(Identifiable.class));
			}
		}

		return new PortObject[] { PmmPortObject.createListObject(data, firstInput.getSpec()) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		PmmPortObjectSpec firstSpec = (PmmPortObjectSpec) inSpecs[0];

		for (int i = 1; i <= 3; i++) {
			PmmPortObjectSpec spec = (PmmPortObjectSpec) inSpecs[i];

			if (spec != null && spec != firstSpec) {
				throw new InvalidSettingsException("Specs do no match: \"" + firstSpec + "\" and \"" + spec + "\"");
			}
		}

		return new PortObjectSpec[] { firstSpec };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
	}
}
