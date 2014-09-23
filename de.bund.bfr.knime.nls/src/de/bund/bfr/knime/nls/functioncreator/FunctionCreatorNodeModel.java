/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
package de.bund.bfr.knime.nls.functioncreator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

import de.bund.bfr.knime.nls.Function;
import de.bund.bfr.knime.nls.functionport.FunctionPortObject;
import de.bund.bfr.knime.nls.functionport.FunctionPortObjectSpec;
import de.bund.bfr.math.MathUtils;

/**
 * This is the model implementation of FunctionCreator.
 * 
 * 
 * @author Christian Thoens
 */
public class FunctionCreatorNodeModel extends NodeModel {

	private FunctionCreatorSettings set;

	/**
	 * Constructor for the node model.
	 */
	protected FunctionCreatorNodeModel() {
		super(new PortType[] {}, new PortType[] { FunctionPortObject.TYPE });
		set = new FunctionCreatorSettings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec)
			throws Exception {
		return new PortObject[] { new FunctionPortObject(createFunction(
				set.getTerm(), set.getDependentVariable(),
				set.getIndependentVariables())) };
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
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		if (set.getTerm() == null || set.getDependentVariable() == null
				|| set.getIndependentVariables().isEmpty()) {
			throw new InvalidSettingsException("Function not specified");
		}

		return new PortObjectSpec[] { new FunctionPortObjectSpec(
				createFunction(set.getTerm(), set.getDependentVariable(),
						set.getIndependentVariables())) };
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
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		set.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
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

	private static Function createFunction(String term,
			String dependentVariable, List<String> independentVariables) {
		Map<String, String> terms = new LinkedHashMap<>();

		terms.put(dependentVariable, term);

		List<String> parameters = new ArrayList<>(MathUtils.getSymbols(term));

		parameters.removeAll(independentVariables);
		Collections.sort(parameters);
		Collections.sort(independentVariables);

		return new Function(terms, dependentVariable, independentVariables,
				parameters);
	}

}
