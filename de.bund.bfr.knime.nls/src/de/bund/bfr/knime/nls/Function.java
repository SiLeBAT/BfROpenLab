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
	private String timeVariable;
	private Map<String, String> initParameters;
	private Map<String, Double> initValues;

	public Function() {
		this(new LinkedHashMap<String, String>(), null,
				new ArrayList<String>(), new ArrayList<String>());
	}

	public Function(Map<String, String> terms, String dependentVariable,
			List<String> independentVariables, List<String> parameters) {
		this(terms, dependentVariable, independentVariables, parameters, null,
				new LinkedHashMap<String, String>(),
				new LinkedHashMap<String, Double>());
	}

	public Function(Map<String, String> terms, String dependentVariable,
			List<String> independentVariables, List<String> parameters,
			String timeVariable, Map<String, String> initParameters,
			Map<String, Double> initValues) {
		this.terms = terms;
		this.dependentVariable = dependentVariable;
		this.independentVariables = independentVariables;
		this.parameters = parameters;
		this.timeVariable = timeVariable;
		this.initParameters = initParameters;
		this.initValues = initValues;
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

	public String getTimeVariable() {
		return timeVariable;
	}

	public Map<String, String> getInitParameters() {
		return initParameters;
	}

	public Map<String, Double> getInitValues() {
		return initValues;
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
				+ ((timeVariable == null) ? 0 : timeVariable.hashCode());
		result = prime
				* result
				+ ((independentVariables == null) ? 0 : independentVariables
						.hashCode());
		result = prime * result
				+ ((initParameters == null) ? 0 : initParameters.hashCode());
		result = prime * result
				+ ((initValues == null) ? 0 : initValues.hashCode());
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
		if (timeVariable == null) {
			if (other.timeVariable != null)
				return false;
		} else if (!timeVariable.equals(other.timeVariable))
			return false;
		if (independentVariables == null) {
			if (other.independentVariables != null)
				return false;
		} else if (!independentVariables.equals(other.independentVariables))
			return false;
		if (initParameters == null) {
			if (other.initParameters != null)
				return false;
		} else if (!initParameters.equals(other.initParameters))
			return false;
		if (initValues == null) {
			if (other.initValues != null)
				return false;
		} else if (!initValues.equals(other.initValues))
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
				+ ", timeVariable=" + timeVariable + ", initParameters="
				+ initParameters + ", initValues=" + initValues + "]";
	}
}
