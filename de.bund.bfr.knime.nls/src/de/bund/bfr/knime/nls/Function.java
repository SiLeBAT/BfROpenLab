/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
		this(new LinkedHashMap<>(0), null, new ArrayList<>(0), new ArrayList<>(0));
	}

	public Function(Map<String, String> terms, String dependentVariable, List<String> independentVariables,
			List<String> parameters) {
		this(terms, dependentVariable, independentVariables, parameters, null, new LinkedHashMap<>(0),
				new LinkedHashMap<>(0));
	}

	public Function(Map<String, String> terms, String dependentVariable, List<String> independentVariables,
			List<String> parameters, String timeVariable, Map<String, String> initParameters,
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
}
