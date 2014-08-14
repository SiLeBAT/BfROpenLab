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
package de.bund.bfr.knime.nls;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Function implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<String, String> terms;
	private String dependentVariable;
	private List<String> independentVariables;
	private List<String> parameters;
	private String diffVariable;
	private Map<String, Double> initialValues;

	public Function() {
		this(new LinkedHashMap<String, String>(), null,
				new ArrayList<String>(), new ArrayList<String>());
	}

	public Function(Map<String, String> terms, String dependentVariable,
			List<String> independentVariables, List<String> parameters) {
		this(terms, dependentVariable, independentVariables, parameters, null,
				new LinkedHashMap<String, Double>());
	}

	public Function(Map<String, String> terms, String dependentVariable,
			List<String> independentVariables, List<String> parameters,
			String diffVariable, Map<String, Double> initialValues) {
		this.terms = terms;
		this.dependentVariable = dependentVariable;
		this.independentVariables = independentVariables;
		this.parameters = parameters;
		this.diffVariable = diffVariable;
		this.initialValues = initialValues;
	}

	public Map<String, String> getTerms() {
		return terms;
	}

	public String getDependentVariable() {
		return dependentVariable;
	}

	public List<String> getIndependentVariables() {
		return independentVariables;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public String getDiffVariable() {
		return diffVariable;
	}

	public Map<String, Double> getInitialValues() {
		return initialValues;
	}

	public List<String> getVariables() {
		List<String> names = new ArrayList<>();

		names.addAll(independentVariables);		
		names.add(dependentVariable);

		return names;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((dependentVariable == null) ? 0 : dependentVariable
						.hashCode());
		result = prime * result
				+ ((diffVariable == null) ? 0 : diffVariable.hashCode());
		result = prime
				* result
				+ ((independentVariables == null) ? 0 : independentVariables
						.hashCode());
		result = prime * result
				+ ((initialValues == null) ? 0 : initialValues.hashCode());
		result = prime * result
				+ ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((terms == null) ? 0 : terms.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Function other = (Function) obj;
		if (dependentVariable == null) {
			if (other.dependentVariable != null)
				return false;
		} else if (!dependentVariable.equals(other.dependentVariable))
			return false;
		if (diffVariable == null) {
			if (other.diffVariable != null)
				return false;
		} else if (!diffVariable.equals(other.diffVariable))
			return false;
		if (independentVariables == null) {
			if (other.independentVariables != null)
				return false;
		} else if (!independentVariables.equals(other.independentVariables))
			return false;
		if (initialValues == null) {
			if (other.initialValues != null)
				return false;
		} else if (!initialValues.equals(other.initialValues))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		if (terms == null) {
			if (other.terms != null)
				return false;
		} else if (!terms.equals(other.terms))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Function [terms=" + terms + ", dependentVariable="
				+ dependentVariable + ", independentVariables="
				+ independentVariables + ", parameters=" + parameters
				+ ", diffVariable=" + diffVariable + ", initialValues="
				+ initialValues + "]";
	}
}
