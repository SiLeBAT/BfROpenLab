/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.node.BufferedDataTable;

import de.bund.bfr.knime.nls.Function;
import de.bund.bfr.knime.nls.NlsUtils;
import de.bund.bfr.knime.nls.chart.ChartSelectionPanel;
import de.bund.bfr.knime.nls.chart.Plotable;
import de.bund.bfr.knime.nls.functionport.FunctionPortObject;

public class DiffFunctionViewReader implements ViewReader {

	private List<String> ids;
	private String depVar;
	private Map<String, List<String>> stringColumns;
	private Map<String, List<Double>> doubleColumns;
	private Map<String, Plotable> plotables;
	private Map<String, String> legend;

	public DiffFunctionViewReader(FunctionPortObject functionObject, BufferedDataTable varTable,
			BufferedDataTable conditionTable) {
		this(functionObject, null, varTable, conditionTable, null);
	}

	public DiffFunctionViewReader(FunctionPortObject functionObject, BufferedDataTable paramTable,
			BufferedDataTable varTable, BufferedDataTable conditionTable, BufferedDataTable covarianceTable) {
		Function f = functionObject.getFunction();
		List<String> qualityColumns = NlsUtils.getQualityColumns(paramTable, f);

		ids = new ArrayList<>();
		depVar = f.getDependentVariable();
		plotables = new LinkedHashMap<>();
		legend = new LinkedHashMap<>();
		doubleColumns = new LinkedHashMap<>();
		stringColumns = new LinkedHashMap<>();
		stringColumns.put(NlsUtils.ID_COLUMN, new ArrayList<>());
		stringColumns.put(ChartSelectionPanel.STATUS, new ArrayList<>());
		doubleColumns = new LinkedHashMap<>();

		for (String column : qualityColumns) {
			doubleColumns.put(column, new ArrayList<>());
		}

		for (String id : NlsUtils.getIds(paramTable != null ? paramTable : varTable)) {
			Map<String, Double> qualityValues = NlsUtils.getQualityValues(paramTable, id, qualityColumns);

			ids.add(id);
			legend.put(id, id);
			stringColumns.get(NlsUtils.ID_COLUMN).add(id);

			for (String q : qualityColumns) {
				doubleColumns.get(q).add(qualityValues.get(q));
			}

			Plotable plotable = new Plotable(Plotable.Type.DATA_DIFF);

			plotable.getFunctions().putAll(f.getTerms());
			plotable.getInitValues().putAll(f.getInitValues());
			plotable.getInitParameters().putAll(f.getInitParameters());
			plotable.setDependentVariable(f.getDependentVariable());
			plotable.setDiffVariable(f.getTimeVariable());
			plotable.getIndependentVariables().putAll(NlsUtils.createZeroMap(Arrays.asList(f.getTimeVariable())));
			plotable.getValueLists().putAll(NlsUtils.getDiffVariableValues(varTable, id, f));
			plotable.getConditionLists().putAll(NlsUtils.getConditionValues(conditionTable, id, f));
			plotable.getParameters().putAll(NlsUtils.getParameters(paramTable, id, f));
			plotable.getCovariances().putAll(NlsUtils.getCovariances(covarianceTable, id, f));

			if (qualityValues.get(NlsUtils.MSE_COLUMN) != null) {
				plotable.setMse(qualityValues.get(NlsUtils.MSE_COLUMN));
			}

			if (qualityValues.get(NlsUtils.DOF_COLUMN) != null) {
				plotable.setDegreesOfFreedom(qualityValues.get(NlsUtils.DOF_COLUMN).intValue());
			}

			stringColumns.get(ChartSelectionPanel.STATUS).add(plotable.getStatus().toString());
			plotables.put(id, plotable);
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
