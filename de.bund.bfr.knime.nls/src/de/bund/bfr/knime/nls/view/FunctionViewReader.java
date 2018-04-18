/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.nls.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.node.BufferedDataTable;

import de.bund.bfr.knime.nls.Function;
import de.bund.bfr.knime.nls.NlsUtils;
import de.bund.bfr.knime.nls.chart.ChartSelectionPanel;
import de.bund.bfr.knime.nls.chart.Plotable;
import de.bund.bfr.knime.nls.functionport.FunctionPortObject;

public class FunctionViewReader implements ViewReader {

	private List<String> ids;
	private String depVar;
	private Map<String, List<String>> stringColumns;
	private Map<String, List<Double>> doubleColumns;
	private Map<String, Plotable> plotables;
	private Map<String, String> legend;

	public FunctionViewReader(FunctionPortObject functionObject, BufferedDataTable paramTable,
			BufferedDataTable varTable, BufferedDataTable covarianceTable, String indep) {
		Function f = functionObject.getFunction();
		List<String> qualityColumns = NlsUtils.getQualityColumns(paramTable, f);
		String usedIndep = indep != null && f.getIndependentVariables().contains(indep) ? indep
				: f.getIndependentVariables().get(0);

		ids = new ArrayList<>();
		depVar = f.getDependentVariable();
		plotables = new LinkedHashMap<>();
		legend = new LinkedHashMap<>();
		doubleColumns = new LinkedHashMap<>();
		stringColumns = new LinkedHashMap<>();
		stringColumns.put(NlsUtils.ID_COLUMN, new ArrayList<>());
		stringColumns.put(ChartSelectionPanel.STATUS, new ArrayList<>());
		doubleColumns = new LinkedHashMap<>();

		for (String i : f.getIndependentVariables()) {
			if (!i.equals(usedIndep)) {
				doubleColumns.put(i, new ArrayList<>());
			}
		}

		for (String column : qualityColumns) {
			doubleColumns.put(column, new ArrayList<>());
		}

		for (String id : NlsUtils.getIds(paramTable != null ? paramTable : varTable)) {
			for (Map<String, Double> fixed : NlsUtils.getFixedVariables(varTable, id, f, usedIndep)) {
				String newId = fixed.isEmpty() ? id : id + " " + fixed.toString();
				Map<String, Double> qualityValues = NlsUtils.getQualityValues(paramTable, id, qualityColumns);

				ids.add(newId);
				legend.put(newId, newId);
				stringColumns.get(NlsUtils.ID_COLUMN).add(id);

				fixed.forEach((var, value) -> doubleColumns.get(var).add(value));
				qualityColumns.forEach(column -> doubleColumns.get(column).add(qualityValues.get(column)));

				Plotable plotable = new Plotable(Plotable.Type.DATA_FUNCTION);
				Map<String, Double> variables = new LinkedHashMap<>(fixed);

				variables.put(usedIndep, 0.0);

				plotable.setFunction(f.getTerms().get(f.getDependentVariable()));
				plotable.setDependentVariable(f.getDependentVariable());
				plotable.getIndependentVariables().putAll(variables);
				plotable.getValueLists().putAll(NlsUtils.getVariableValues(varTable, id, f, fixed));
				plotable.getParameters().putAll(NlsUtils.getParameters(paramTable, id, f));
				plotable.getCovariances().putAll(NlsUtils.getCovariances(covarianceTable, id, f));

				if (qualityValues.get(NlsUtils.MSE_COLUMN) != null) {
					plotable.setMse(qualityValues.get(NlsUtils.MSE_COLUMN));
				}

				if (qualityValues.get(NlsUtils.DOF_COLUMN) != null) {
					plotable.setDegreesOfFreedom(qualityValues.get(NlsUtils.DOF_COLUMN).intValue());
				}

				stringColumns.get(ChartSelectionPanel.STATUS).add(plotable.getStatus().toString());
				plotables.put(newId, plotable);
			}
		}
	}

	@Override
	public List<String> getIds() {
		return ids;
	}

	@Override
	public String getDepVar() {
		return depVar;
	}

	@Override
	public Map<String, List<String>> getStringColumns() {
		return stringColumns;
	}

	@Override
	public Map<String, List<Double>> getDoubleColumns() {
		return doubleColumns;
	}

	@Override
	public Map<String, Plotable> getPlotables() {
		return plotables;
	}

	@Override
	public Map<String, String> getLegend() {
		return legend;
	}
}
