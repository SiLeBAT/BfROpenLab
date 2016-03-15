/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataTable;

import com.google.common.primitives.Doubles;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.nls.chart.Plotable;

public class NlsUtils {

	public static final String ID_COLUMN = "ID";
	public static final String SSE_COLUMN = "SSE";
	public static final String MSE_COLUMN = "MSE";
	public static final String RMSE_COLUMN = "RMSE";
	public static final String R2_COLUMN = "R2";
	public static final String AIC_COLUMN = "AIC";
	public static final String DOF_COLUMN = "DOF";
	public static final String PARAM_COLUMN = "Param";

	public static final String SD_COLUMN = "Standard Deviation";
	public static final String LOG_LIKELIHOOD_COLUMN = "Log Likelihood";

	private NlsUtils() {
	}

	public static Map<String, Double> createZeroMap(Collection<String> keys) {
		Map<String, Double> map = new LinkedHashMap<>();

		for (String key : keys) {
			map.put(key, 0.0);
		}

		return map;
	}

	public static List<String> getIds(BufferedDataTable table) {
		List<String> ids = new ArrayList<>();

		for (DataRow row : table) {
			String id = IO.getString(row.getCell(table.getSpec().findColumnIndex(NlsUtils.ID_COLUMN)));

			if (id != null) {
				ids.add(id);
			}
		}

		return ids;
	}

	public static List<String> getQualityColumns(BufferedDataTable table, Function f) {
		List<String> columns = new ArrayList<>();

		for (DataColumnSpec spec : table.getSpec()) {
			if ((spec.getType() == DoubleCell.TYPE || spec.getType() == IntCell.TYPE)
					&& !f.getParameters().contains(spec.getName())) {
				columns.add(spec.getName());
			}
		}

		return columns;
	}

	public static Map<String, Double> getQualityValues(BufferedDataTable table, String id, List<String> columns) {
		Map<String, Double> values = new LinkedHashMap<>();
		DataTableSpec spec = table.getSpec();

		for (DataRow row : table) {
			if (id.equals(IO.getString(row.getCell(spec.findColumnIndex(NlsUtils.ID_COLUMN))))) {
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

	public static Map<String, Double> getParameters(BufferedDataTable table, String id, Function f) {
		Map<String, Double> params = new LinkedHashMap<>();
		DataTableSpec spec = table.getSpec();

		for (DataRow row : table) {
			if (id.equals(IO.getString(row.getCell(spec.findColumnIndex(NlsUtils.ID_COLUMN))))) {
				for (String param : f.getParameters()) {
					params.put(param, IO.getDouble(row.getCell(spec.findColumnIndex(param))));
				}

				break;
			}
		}

		return params;
	}

	public static Map<String, Map<String, Double>> getCovariances(BufferedDataTable table, String id, Function f) {
		Map<String, Map<String, Double>> covariances = new LinkedHashMap<>();
		DataTableSpec spec = table.getSpec();

		for (DataRow row : table) {
			if (id.equals(IO.getString(row.getCell(spec.findColumnIndex(NlsUtils.ID_COLUMN))))) {
				Map<String, Double> cov = new LinkedHashMap<>();
				String param1 = IO.getString(row.getCell(spec.findColumnIndex(NlsUtils.PARAM_COLUMN)));

				for (String param2 : f.getParameters()) {
					cov.put(param2, IO.getDouble(row.getCell(spec.findColumnIndex(param2))));
				}

				covariances.put(param1, cov);
			}
		}

		return covariances;
	}

	public static Map<String, double[]> getConditionValues(BufferedDataTable table, String id, Function f) {
		Map<String, List<Double>> values = new LinkedHashMap<>();
		DataTableSpec spec = table.getSpec();

		for (String var : f.getIndependentVariables()) {
			values.put(var, new ArrayList<>());
		}

		loop: for (DataRow row : table) {
			if (id.equals(IO.getString(row.getCell(spec.findColumnIndex(NlsUtils.ID_COLUMN))))) {
				Map<String, Double> v = new LinkedHashMap<>();

				for (String var : f.getIndependentVariables()) {
					Double value = IO.getDouble(row.getCell(spec.findColumnIndex(var)));

					if (value == null || !Double.isFinite(value)) {
						continue loop;
					}

					v.put(var, value);
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

	public static List<Map<String, Double>> getFixedVariables(BufferedDataTable table, String id, Function f,
			String indep) {
		List<Map<String, Double>> values = new ArrayList<>();
		DataTableSpec spec = table.getSpec();

		for (DataRow row : table) {
			if (id.equals(IO.getString(row.getCell(spec.findColumnIndex(NlsUtils.ID_COLUMN))))) {
				Map<String, Double> v = new LinkedHashMap<>();

				for (String var : f.getIndependentVariables()) {
					if (!var.equals(indep)) {
						v.put(var, IO.getDouble(row.getCell(spec.findColumnIndex(var))));
					}
				}

				if (!values.contains(v)) {
					values.add(v);
				}
			}
		}

		return values;
	}

	public static Map<String, double[]> getVariableValues(BufferedDataTable table, String id, Function f,
			Map<String, Double> fixed) {
		Map<String, List<Double>> values = new LinkedHashMap<>();
		DataTableSpec spec = table.getSpec();

		for (String var : f.getVariables()) {
			values.put(var, new ArrayList<>());
		}

		loop: for (DataRow row : table) {
			if (id.equals(IO.getString(row.getCell(spec.findColumnIndex(NlsUtils.ID_COLUMN))))) {
				Map<String, Double> v = new LinkedHashMap<>();

				for (String var : f.getVariables()) {
					Double value = IO.getDouble(row.getCell(spec.findColumnIndex(var)));

					if (value == null || !Double.isFinite(value)) {
						continue loop;
					}

					v.put(var, value);
				}

				for (Map.Entry<String, Double> entry : fixed.entrySet()) {
					if (!entry.getValue().equals(v.get(entry.getKey()))) {
						continue loop;
					}
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

	public static Map<String, double[]> getDiffVariableValues(BufferedDataTable table, String id, Function f) {
		Map<String, List<Double>> values = new LinkedHashMap<>();
		DataTableSpec spec = table.getSpec();

		values.put(f.getTimeVariable(), new ArrayList<>());
		values.put(f.getDependentVariable(), new ArrayList<>());

		for (DataRow row : table) {
			if (id.equals(IO.getString(row.getCell(spec.findColumnIndex(NlsUtils.ID_COLUMN))))) {
				Double time = IO.getDouble(row.getCell(spec.findColumnIndex(f.getTimeVariable())));
				Double target = IO.getDouble(row.getCell(spec.findColumnIndex(f.getDependentVariable())));

				if (time != null && target != null && Double.isFinite(time) && Double.isFinite(target)) {
					values.get(f.getTimeVariable()).add(time);
					values.get(f.getDependentVariable()).add(target);
				}
			}
		}

		Map<String, double[]> result = new LinkedHashMap<>();

		for (Map.Entry<String, List<Double>> entry : values.entrySet()) {
			result.put(entry.getKey(), Doubles.toArray(entry.getValue()));
		}

		return result;
	}

	public static Set<String> getVariables(Collection<Plotable> plotables) {
		Set<String> variables = new LinkedHashSet<>();

		for (Plotable plotable : plotables) {
			variables.addAll(plotable.getIndependentVariables().keySet());
		}

		return variables;
	}

	public static Set<String> getParameters(Collection<Plotable> plotables) {
		Set<String> parameters = new LinkedHashSet<>();

		for (Plotable plotable : plotables) {
			parameters.addAll(plotable.getParameters().keySet());
		}

		return parameters;
	}

	public static List<String> getOrderedVariables(Collection<Plotable> plotables) {
		return KnimeUtils.ORDERING.sortedCopy(plotables.stream().map(p -> p.getIndependentVariables().keySet())
				.flatMap(Set::stream).collect(Collectors.toCollection(LinkedHashSet::new)));
	}
}
