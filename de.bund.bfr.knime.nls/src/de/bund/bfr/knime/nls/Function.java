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
import java.util.List;

public class Function implements Serializable {

	private static final long serialVersionUID = 1L;

	private String term;

	private String dependentVariable;

	private List<String> independentVariables;

	private List<String> parameters;

	public Function() {
		this(null, null, new ArrayList<String>(), new ArrayList<String>());
	}

	public Function(String term, String dependentVariable,
			List<String> independentVariables, List<String> parameters) {
		this.term = term;
		this.dependentVariable = dependentVariable;
		this.independentVariables = independentVariables;
		this.parameters = parameters;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String expression) {
		this.term = expression;
	}

	public String getDependentVariable() {
		return dependentVariable;
	}

	public void setDependentVariable(String dependentVariable) {
		this.dependentVariable = dependentVariable;
	}

	public List<String> getIndependentVariables() {
		return independentVariables;
	}

	public void setIndependentVariables(List<String> independentVariables) {
		this.independentVariables = independentVariables;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	public List<String> getVariables() {
		List<String> names = new ArrayList<String>();

		names.add(dependentVariable);
		names.addAll(independentVariables);

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
		result = prime
				* result
				+ ((independentVariables == null) ? 0 : independentVariables
						.hashCode());
		result = prime * result
				+ ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((term == null) ? 0 : term.hashCode());
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
		if (independentVariables == null) {
			if (other.independentVariables != null)
				return false;
		} else if (!independentVariables.equals(other.independentVariables))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		if (term == null) {
			if (other.term != null)
				return false;
		} else if (!term.equals(other.term))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Function [term=" + term + ", dependentVariable="
				+ dependentVariable + ", independentVariables="
				+ independentVariables + ", parameters=" + parameters + "]";
	}
}
