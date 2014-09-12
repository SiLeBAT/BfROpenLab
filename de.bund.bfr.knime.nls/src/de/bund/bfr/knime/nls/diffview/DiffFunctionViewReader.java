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
package de.bund.bfr.knime.nls.diffview;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataTable;

import com.google.common.primitives.Doubles;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.nls.Function;
import de.bund.bfr.knime.nls.NlsConstants;
import de.bund.bfr.knime.nls.chart.ChartUtilities;
import de.bund.bfr.knime.nls.chart.Plotable;
import de.bund.bfr.knime.nls.functionport.FunctionPortObject;
import de.bund.bfr.math.MathUtils;

public class DiffFunctionViewReader {

	private List<String> ids;
	private String depVar;
	private Map<String, List<String>> stringColumns;
	private Map<String, List<Double>> doubleColumns;
	private Map<String, Plotable> plotables;
	private Map<String, String> legend;

	public DiffFunctionViewReader(FunctionPortObject functionObject,
			BufferedDataTable paramTable, BufferedDataTable varTable,
			BufferedDataTable conditionTable,
			BufferedDataTable covarianceTable, String indep) {
		Function f = functionObject.getFunction();
		List<String> qualityColumns = getQualityColumns(paramTable, f);

		if (indep == null) {
			indep = f.getIndependentVariables().get(0);
		}

		ids = new ArrayList<>();
		depVar = f.getDependentVariable();
		plotables = new LinkedHashMap<>();
		legend = new LinkedHashMap<>();
		doubleColumns = new LinkedHashMap<>();
		stringColumns = new LinkedHashMap<>();
		stringColumns.put(NlsConstants.ID_COLUMN, new ArrayList<String>());
		stringColumns.put(ChartUtilities.STATUS, new ArrayList<String>());
		doubleColumns = new LinkedHashMap<>();

		if (f.getTimeVariable() == null) {
			for (String i : f.getIndependentVariables()) {
				if (!i.equals(indep)) {
					doubleColumns.put(i, new ArrayList<Double>());
				}
			}
		}

		for (String column : qualityColumns) {
			doubleColumns.put(column, new ArrayList<Double>());
		}

		for (String id : getIds(paramTable)) {
			if (f.getTimeVariable() == null) {
				continue;
			}

			Map<String, Double> qualityValues = getQualityValues(paramTable,
					id, qualityColumns);

			ids.add(id);
			legend.put(id, id);
			stringColumns.get(NlsConstants.ID_COLUMN).add(id);

			for (String q : qualityColumns) {
				doubleColumns.get(q).add(qualityValues.get(q));
			}

			Plotable plotable = new Plotable(Plotable.Type.DATA_DIFF);

			plotable.setFunctions(f.getTerms());
			plotable.setInitValues(f.getInitValues());
			plotable.setInitParameters(f.getInitParameters());
			plotable.setDependentVariable(f.getDependentVariable());
			plotable.setDiffVariable(f.getTimeVariable());
			plotable.setParameters(getParameters(paramTable, id, f));
			plotable.setIndependentVariables(getVariables(f));
			plotable.setMinVariables(new LinkedHashMap<String, Double>());
			plotable.setMaxVariables(new LinkedHashMap<String, Double>());
			plotable.setValueLists(getVariableValues(varTable, id, f));
			plotable.setConditionLists(getConditionValues(conditionTable, id, f));

			if (covarianceTable != null) {
				plotable.setCovariances(getCovariances(covarianceTable, id, f));
			}

			if (qualityValues.get(NlsConstants.DOF_COLUMN) != null) {
				plotable.setDegreesOfFreedom(qualityValues.get(
						NlsConstants.DOF_COLUMN).intValue());
			}

			stringColumns.get(ChartUtilities.STATUS).add(
					plotable.getStatus().toString());
			plotables.put(id, plotable);
		}
	}

	public List<String> getIds() {
		return ids;
	}

	public String getDepVar() {
		return depVar;
	}

	public Map<String, List<String>> getStringColumns() {
		return stringColumns;
	}

	public Map<String, List<Double>> getDoubleColumns() {
		return doubleColumns;
	}

	public Map<String, Plotable> getPlotables() {
		return plotables;
	}

	public Map<String, String> getLegend() {
		return legend;
	}

	private static List<String> getIds(BufferedDataTable table) {
		List<String> ids = new ArrayList<>();

		for (DataRow row : table) {
			String id = IO.getString(row.getCell(table.getSpec()
					.findColumnIndex(NlsConstants.ID_COLUMN)));

			if (id != null) {
				ids.add(id);
			}
		}

		return ids;
	}

	private static List<String> getQualityColumns(BufferedDataTable table,
			Function f) {
		List<String> columns = new ArrayList<>();

		for (DataColumnSpec spec : table.getSpec()) {
			if ((spec.getType() == DoubleCell.TYPE || spec.getType() == IntCell.TYPE)
					&& !f.getParameters().contains(spec.getName())) {
				columns.add(spec.getName());
			}
		}

		return columns;
	}

	private static Map<String, Double> getQualityValues(
			BufferedDataTable table, String id, List<String> columns) {
		Map<String, Double> values = new LinkedHashMap<>();
		DataTableSpec spec = table.getSpec();

		for (DataRow row : table) {
			if (id.equals(IO.getString(row.getCell(spec
					.findColumnIndex(NlsConstants.ID_COLUMN))))) {
				for (String column : columns) {
					DataCell cell = row.getCell(spec.findColumnIndex(column));

					if (IO.getDouble(cell) != null) {
						values.put(column, IO.getDouble(cell));
					} else if (IO.getInt(cell) != null) {
						values.put(column, IO.getInt(cell).doubleValue());
					} else {
						values.put(column, null);
					}
				}

				break;
			}
		}

		return values;
	}

	private static Map<String, Double> getParameters(BufferedDataTable table,
			String id, Function f) {
		Map<String, Double> params = new LinkedHashMap<>();
		DataTableSpec spec = table.getSpec();

		for (DataRow row : table) {
			if (id.equals(IO.getString(row.getCell(spec
					.findColumnIndex(NlsConstants.ID_COLUMN))))) {
				for (String param : f.getParameters()) {
					params.put(param, IO.getDouble(row.getCell(spec
							.findColumnIndex(param))));
				}

				break;
			}
		}

		return params;
	}

	private static Map<String, Map<String, Double>> getCovariances(
			BufferedDataTable table, String id, Function f) {
		Map<String, Map<String, Double>> covariances = new LinkedHashMap<>();
		DataTableSpec spec = table.getSpec();

		for (DataRow row : table) {
			if (id.equals(IO.getString(row.getCell(spec
					.findColumnIndex(NlsConstants.ID_COLUMN))))) {
				Map<String, Double> cov = new LinkedHashMap<>();
				String param1 = IO.getString(row.getCell(spec
						.findColumnIndex(NlsConstants.PARAM_COLUMN)));

				for (String param2 : f.getParameters()) {
					cov.put(param2, IO.getDouble(row.getCell(spec
							.findColumnIndex(param2))));
				}

				covariances.put(param1, cov);
			}
		}

		return covariances;
	}

	private static Map<String, Double> getVariables(Function f) {
		Map<String, Double> vars = new LinkedHashMap<>();

		vars.put(f.getTimeVariable(), 0.0);

		return vars;
	}

	private static Map<String, double[]> getVariableValues(
			BufferedDataTable table, String id, Function f) {
		Map<String, List<Double>> values = new LinkedHashMap<>();
		DataTableSpec spec = table.getSpec();

		values.put(f.getTimeVariable(), new ArrayList<Double>());
		values.put(f.getDependentVariable(), new ArrayList<Double>());

		for (DataRow row : table) {
			if (id.equals(IO.getString(row.getCell(spec
					.findColumnIndex(NlsConstants.ID_COLUMN))))) {
				Double time = IO.getDouble(row.getCell(spec.findColumnIndex(f
						.getTimeVariable())));
				Double target = IO.getDouble(row.getCell(spec.findColumnIndex(f
						.getDependentVariable())));

				if (!MathUtils.isValidDouble(time)
						|| !MathUtils.isValidDouble(target)) {
					continue;
				}

				values.get(f.getTimeVariable()).add(time);
				values.get(f.getDependentVariable()).add(target);
			}
		}

		Map<String, double[]> result = new LinkedHashMap<>();

		for (Map.Entry<String, List<Double>> entry : values.entrySet()) {
			result.put(entry.getKey(), Doubles.toArray(entry.getValue()));
		}

		return result;
	}

	private static Map<String, double[]> getConditionValues(
			BufferedDataTable table, String id, Function f) {
		Map<String, List<Double>> values = new LinkedHashMap<>();
		DataTableSpec spec = table.getSpec();

		for (String var : f.getIndependentVariables()) {
			values.put(var, new ArrayList<Double>());
		}

		for (DataRow row : table) {
			if (id.equals(IO.getString(row.getCell(spec
					.findColumnIndex(NlsConstants.ID_COLUMN))))) {
				Map<String, Double> v = new LinkedHashMap<>();

				for (String var : f.getIndependentVariables()) {
					v.put(var, IO.getDouble(row.getCell(spec
							.findColumnIndex(var))));
				}

				if (MathUtils.containsInvalidDouble(v.values())) {
					continue;
				}

				for (Map.Entry<String, Double> entry : v.entrySet()) {
					values.get(entry.getKey()).add(entry.getValue());
				}
			}
		}

		Map<String, double[]> result = new LinkedHashMap<>();

		for (Map.Entry<String, List<Double>> entry : values.entrySet()) {
			result.put(entry.getKey(), Doubles.toArray(entry.getValue()));
		}

		return result;
	}
}
