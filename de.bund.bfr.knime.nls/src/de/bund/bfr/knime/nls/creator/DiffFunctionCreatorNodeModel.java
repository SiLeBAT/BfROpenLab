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
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.nls.creator;

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
 * This is the model implementation of DiffFunctionCreator.
 * 
 * 
 * @author Christian Thoens
 */
public class DiffFunctionCreatorNodeModel extends NodeModel {

	private DiffFunctionCreatorSettings set;

	/**
	 * Constructor for the node model.
	 */
	protected DiffFunctionCreatorNodeModel() {
		super(new PortType[] {}, new PortType[] { FunctionPortObject.TYPE });
		set = new DiffFunctionCreatorSettings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		return new PortObject[] { new FunctionPortObject(createFunction(set.getTerms(), set.getDependentVariables(),
				set.getInitValues(), set.getIndependentVariables(), set.getDiffVariable())) };
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
		if (set.getTerms().isEmpty()) {
			throw new InvalidSettingsException("Function not specified");
		}

		return new PortObjectSpec[] {
				new FunctionPortObjectSpec(createFunction(set.getTerms(), set.getDependentVariables(),
						set.getInitValues(), set.getIndependentVariables(), set.getDiffVariable())) };
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

	private static Function createFunction(List<String> terms, List<String> dependentVariables, List<Double> initValues,
			List<String> independentVariables, String diffVariable) {
		Map<String, String> termsMap = new LinkedHashMap<>();
		Map<String, String> initParameterMap = new LinkedHashMap<>();
		Map<String, Double> initValueMap = new LinkedHashMap<>();

		for (int i = 0; i < terms.size(); i++) {
			termsMap.put(dependentVariables.get(i), terms.get(i));

			if (initValues.get(i) != null) {
				initValueMap.put(dependentVariables.get(i), initValues.get(i));
			} else {
				initParameterMap.put(dependentVariables.get(i), dependentVariables.get(i) + "_0");
			}
		}

		List<String> parameters = new ArrayList<>(MathUtils.getSymbols(terms));
		List<String> indeps = new ArrayList<>(independentVariables);

		indeps.add(diffVariable);
		parameters.removeAll(indeps);
		parameters.removeAll(dependentVariables);
		parameters.addAll(initParameterMap.values());

		Collections.sort(parameters);
		Collections.sort(indeps);

		return new Function(termsMap, dependentVariables.get(0), indeps, parameters, diffVariable, initParameterMap,
				initValueMap);
	}
}
