/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import com.google.common.collect.Ordering;

import de.bund.bfr.knime.NoInternalsNodeModel;
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
public class DiffFunctionCreatorNodeModel extends NoInternalsNodeModel {

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
		return new PortObject[] { new FunctionPortObject(createFunction(set)) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		if (set.getTerms().isEmpty()) {
			throw new InvalidSettingsException("Function not specified");
		}

		return new PortObjectSpec[] { new FunctionPortObjectSpec(createFunction(set)) };
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

	private static Function createFunction(DiffFunctionCreatorSettings set) {
		Map<String, String> termsMap = new LinkedHashMap<>();
		Map<String, String> initParameterMap = new LinkedHashMap<>();
		Map<String, Double> initValueMap = new LinkedHashMap<>();

		for (int i = 0; i < set.getTerms().size(); i++) {
			termsMap.put(set.getDependentVariables().get(i), set.getTerms().get(i));

			if (set.getInitValues().get(i) != null) {
				initValueMap.put(set.getDependentVariables().get(i), set.getInitValues().get(i));
			} else {
				initParameterMap.put(set.getDependentVariables().get(i), set.getDependentVariables().get(i) + "_0");
			}
		}

		Set<String> parameters = MathUtils.getSymbols(set.getTerms());
		Set<String> indeps = new LinkedHashSet<>(set.getIndependentVariables());

		indeps.add(set.getDiffVariable());
		parameters.removeAll(indeps);
		parameters.removeAll(set.getDependentVariables());
		parameters.addAll(initParameterMap.values());

		return new Function(termsMap, set.getDependentVariables().get(0), Ordering.natural().sortedCopy(indeps),
				Ordering.natural().sortedCopy(parameters), set.getDiffVariable(), initParameterMap, initValueMap);
	}
}
