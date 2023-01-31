/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.util.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import de.bund.bfr.knime.NoInternalsNodeModel;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.common.Identifiable;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;

/**
 * This is the model implementation of Filter.
 * 
 * 
 * @author Christian Thoens
 */
public class FilterNodeModel extends NoInternalsNodeModel {

	/**
	 * Constructor for the node model.
	 */
	protected FilterNodeModel() {
		super(new PortType[] { PmmPortObject.TYPE, BufferedDataTable.TYPE }, new PortType[] { PmmPortObject.TYPE });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		PmmPortObject input = (PmmPortObject) inObjects[0];
		BufferedDataTable refTable = (BufferedDataTable) inObjects[1];
		Map<String, Identifiable> inDataById = PmmUtils.getById(input.getData(Identifiable.class));
		List<Identifiable> outData = new ArrayList<>();

		for (DataRow row : refTable) {
			Identifiable obj = inDataById.get(row.getKey().getString());

			if (obj != null) {
				outData.add(obj);
			} else {
				setWarningMessage("No object with ID \"" + row.getKey().getString() + "\" found");
			}
		}

		return new PortObject[] { PmmPortObject.createListObject(outData, input.getSpec()) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		return new PortObjectSpec[] { inSpecs[0] };
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
