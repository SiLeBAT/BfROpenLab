/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.views;

import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;
import org.sbml.jsbml.text.parser.ParseException;

import de.bund.bfr.knime.NoInternalsNodeModel;
import de.bund.bfr.knime.chart.ChartUtils;
import de.bund.bfr.knime.pmmlite.core.UnitException;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;

public abstract class ViewModel extends NoInternalsNodeModel {

	protected boolean isSelectionNode;
	protected ViewSettings set;

	protected ViewModel(boolean isSelectionNode) {
		super(new PortType[] { PmmPortObject.TYPE },
				isSelectionNode ? new PortType[] { PmmPortObject.TYPE, ImagePortObject.TYPE }
						: new PortType[] { ImagePortObject.TYPE });
		this.isSelectionNode = isSelectionNode;
		set = createSettings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		if (!getCompatibleSpecs().contains(inSpecs[0])) {
			throw new InvalidSettingsException("Wrong input");
		}

		return isSelectionNode ? new PortObjectSpec[] { inSpecs[0], ChartUtils.getImageSpec(set.isExportAsSvg()) }
				: new PortObjectSpec[] { ChartUtils.getImageSpec(set.isExportAsSvg()) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		try {
			set.save(settings);
		} catch (InvalidSettingsException e) {
			e.printStackTrace();
		}
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

	protected abstract List<PmmPortObjectSpec> getCompatibleSpecs();

	protected abstract ViewSettings createSettings();

	protected abstract ViewReader createReader(PmmPortObject input) throws UnitException, ParseException;
}
