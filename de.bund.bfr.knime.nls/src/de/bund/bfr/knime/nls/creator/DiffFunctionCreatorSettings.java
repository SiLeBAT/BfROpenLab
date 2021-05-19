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
package de.bund.bfr.knime.nls.creator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.google.common.collect.Lists;

import de.bund.bfr.knime.NodeSettings;
import de.bund.bfr.knime.nls.BackwardUtils;
import de.bund.bfr.knime.nls.NlsNodeSettings;

public class DiffFunctionCreatorSettings extends NlsNodeSettings {

	private static final String CFG_DEPENDENT_VARIABLES = "DependentVariables";
	private static final String CFG_TERMS = "Terms";
	private static final String CFG_INIT_VALUES = "InitValues";
	private static final String CFG_INDEPENDENT_VARIABLES = "IndependentVariables";
	private static final String CFG_DIFF_VARIABLE = "DiffVariable";

	private List<String> dependentVariables;
	private List<String> terms;
	private List<Double> initValues;
	private List<String> independentVariables;
	private String diffVariable;

	public DiffFunctionCreatorSettings() {
		dependentVariables = new ArrayList<>();
		terms = new ArrayList<>();
		initValues = new ArrayList<>();
		independentVariables = new ArrayList<>();
		diffVariable = null;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			dependentVariables = Lists.newArrayList(settings.getStringArray(CFG_DEPENDENT_VARIABLES));
		} catch (InvalidSettingsException e) {
			try {
				dependentVariables = BackwardUtils.stringToList(settings.getString(CFG_DEPENDENT_VARIABLES));
			} catch (InvalidSettingsException ex) {
			}
		}

		try {
			terms = Lists.newArrayList(settings.getStringArray(CFG_TERMS));
		} catch (InvalidSettingsException e) {
			try {
				terms = BackwardUtils.stringToList(settings.getString(CFG_TERMS));
			} catch (InvalidSettingsException ex) {
			}
		}

		try {
			initValues = DoubleStream.of(settings.getDoubleArray(CFG_INIT_VALUES)).mapToObj(NodeSettings::nanToNull)
					.collect(Collectors.toList());
		} catch (InvalidSettingsException e) {
			try {
				initValues = BackwardUtils.stringToDoubleList(settings.getString(CFG_INIT_VALUES));
			} catch (InvalidSettingsException ex) {
			}
		}

		try {
			independentVariables = Lists.newArrayList(settings.getStringArray(CFG_INDEPENDENT_VARIABLES));
		} catch (InvalidSettingsException e) {
			try {
				independentVariables = BackwardUtils.stringToList(settings.getString(CFG_INDEPENDENT_VARIABLES));
			} catch (InvalidSettingsException ex) {
			}
		}

		try {
			diffVariable = settings.getString(CFG_DIFF_VARIABLE);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addStringArray(CFG_DEPENDENT_VARIABLES, dependentVariables.toArray(new String[0]));
		settings.addStringArray(CFG_TERMS, terms.toArray(new String[0]));
		settings.addDoubleArray(CFG_INIT_VALUES, initValues.stream().mapToDouble(NodeSettings::nullToNan).toArray());
		settings.addStringArray(CFG_INDEPENDENT_VARIABLES, independentVariables.toArray(new String[0]));
		settings.addString(CFG_DIFF_VARIABLE, diffVariable);
	}

	public List<String> getDependentVariables() {
		return dependentVariables;
	}

	public void setDependentVariables(List<String> dependentVariables) {
		this.dependentVariables = dependentVariables;
	}

	public List<String> getTerms() {
		return terms;
	}

	public void setTerms(List<String> terms) {
		this.terms = terms;
	}

	public List<Double> getInitValues() {
		return initValues;
	}

	public void setInitValues(List<Double> initValues) {
		this.initValues = initValues;
	}

	public List<String> getIndependentVariables() {
		return independentVariables;
	}

	public void setIndependentVariables(List<String> independentVariables) {
		this.independentVariables = independentVariables;
	}

	public String getDiffVariable() {
		return diffVariable;
	}

	public void setDiffVariable(String diffVariable) {
		this.diffVariable = diffVariable;
	}
}
