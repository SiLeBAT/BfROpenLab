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
package de.bund.bfr.knime.nls.diffcreator;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.nls.NlsNodeSettings;

public class DiffFunctionCreatorSettings extends NlsNodeSettings {

	private static final String CFG_DEPENDENT_VARIABLES = "DependentVariables";
	private static final String CFG_TERMS = "Terms";
	private static final String CFG_INDEPENDENT_VARIABLES = "IndependentVariables";
	private static final String CFG_DIFF_VARIABLE = "DiffVariable";
	private static final String CFG_INITIAL_VALUES = "InitialValues";

	private List<String> dependentVariables;
	private List<String> terms;
	private List<String> independentVariables;
	private String diffVariable;
	private List<Double> initialValues;

	public DiffFunctionCreatorSettings() {
		dependentVariables = new ArrayList<>();
		terms = new ArrayList<>();
		independentVariables = new ArrayList<>();
		diffVariable = null;
		initialValues = new ArrayList<>();
	}
	
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			dependentVariables = KnimeUtils.stringToList(settings
					.getString(CFG_DEPENDENT_VARIABLES));
		} catch (InvalidSettingsException e) {
		}

		try {
			terms = KnimeUtils.stringToList(settings.getString(CFG_TERMS));
		} catch (InvalidSettingsException e) {
		}

		try {
			independentVariables = KnimeUtils.stringToList(settings
					.getString(CFG_INDEPENDENT_VARIABLES));
		} catch (InvalidSettingsException e) {
		}

		try {
			diffVariable = settings.getString(CFG_DIFF_VARIABLE);
		} catch (InvalidSettingsException e) {
		}

		try {
			initialValues = KnimeUtils.stringToDoubleList(settings
					.getString(CFG_INITIAL_VALUES));
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addString(CFG_DEPENDENT_VARIABLES,
				KnimeUtils.listToString(dependentVariables));
		settings.addString(CFG_TERMS, KnimeUtils.listToString(terms));
		settings.addString(CFG_INDEPENDENT_VARIABLES,
				KnimeUtils.listToString(independentVariables));
		settings.addString(CFG_DIFF_VARIABLE, diffVariable);
		settings.addString(CFG_INITIAL_VALUES,
				KnimeUtils.listToString(initialValues));
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

	public List<Double> getInitialValues() {
		return initialValues;
	}

	public void setInitialValues(List<Double> initialValues) {
		this.initialValues = initialValues;
	}
}
