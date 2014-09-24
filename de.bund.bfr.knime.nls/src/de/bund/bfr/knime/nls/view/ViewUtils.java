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
package de.bund.bfr.knime.nls.view;

import java.util.ArrayList;
import java.util.Collection;
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
import de.bund.bfr.knime.nls.NlsUtils;
import de.bund.bfr.math.MathUtils;

public class ViewUtils {

	private ViewUtils() {
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
			String id = IO.getString(row.getCell(table.getSpec()
					.findColumnIndex(NlsUtils.ID_COLUMN)));

			if (id != null) {
				ids.add(id);
			}
		}

		return ids;
	}

	public static List<String> getQualityColumns(BufferedDataTable table,
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

	public static Map<String, Double> getQualityValues(BufferedDataTable table,
			String id, List<String> columns) {
		Map<String, Double> values = new LinkedHashMap<>();
		DataTableSpec spec = table.getSpec();

		for (DataRow row : table) {
			if (id.equals(IO.getString(row.getCell(spec
					.findColumnIndex(NlsUtils.ID_COLUMN))))) {
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

	public static Map<String, Double> getParameters(BufferedDataTable table,
			String id, Function f) {
		Map<String, Double> params = new LinkedHashMap<>();
		DataTableSpec spec = table.getSpec();

		for (DataRow row : table) {
			if (id.equals(IO.getString(row.getCell(spec
					.findColumnIndex(NlsUtils.ID_COLUMN))))) {
				for (String param : f.getParameters()) {
					params.put(param, IO.getDouble(row.getCell(spec
							.findColumnIndex(param))));
				}

				break;
			}
		}

		return params;
	}

	public static Map<String, Map<String, Double>> getCovariances(
			BufferedDataTable table, String id, Function f) {
		Map<String, Map<String, Double>> covariances = new LinkedHashMap<>();
		DataTableSpec spec = table.getSpec();

		for (DataRow row : table) {
			if (id.equals(IO.getString(row.getCell(spec
					.findColumnIndex(NlsUtils.ID_COLUMN))))) {
				Map<String, Double> cov = new LinkedHashMap<>();
				String param1 = IO.getString(row.getCell(spec
						.findColumnIndex(NlsUtils.PARAM_COLUMN)));

				for (String param2 : f.getParameters()) {
					cov.put(param2, IO.getDouble(row.getCell(spec
							.findColumnIndex(param2))));
				}

				covariances.put(param1, cov);
			}
		}

		return covariances;
	}

	public static Map<String, double[]> getConditionValues(
			BufferedDataTable table, String id, Function f) {
		Map<String, List<Double>> values = new LinkedHashMap<>();
		DataTableSpec spec = table.getSpec();

		for (String var : f.getIndependentVariables()) {
			values.put(var, new ArrayList<Double>());
		}

		for (DataRow row : table) {
			if (id.equals(IO.getString(row.getCell(spec
					.findColumnIndex(NlsUtils.ID_COLUMN))))) {
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

	public static List<Map<String, Double>> getFixedVariables(
			BufferedDataTable table, String id, Function f, String indep) {
		List<Map<String, Double>> values = new ArrayList<>();
		DataTableSpec spec = table.getSpec();

		for (DataRow row : table) {
			if (id.equals(IO.getString(row.getCell(spec
					.findColumnIndex(NlsUtils.ID_COLUMN))))) {
				Map<String, Double> v = new LinkedHashMap<>();

				for (String var : f.getIndependentVariables()) {
					if (!var.equals(indep)) {
						v.put(var, IO.getDouble(row.getCell(spec
								.findColumnIndex(var))));
					}
				}

				if (!values.contains(v)) {
					values.add(v);
				}
			}
		}

		return values;
	}

	public static Map<String, double[]> getVariableValues(
			BufferedDataTable table, String id, Function f,
			Map<String, Double> fixed) {
		Map<String, List<Double>> values = new LinkedHashMap<>();
		DataTableSpec spec = table.getSpec();

		for (String var : f.getVariables()) {
			values.put(var, new ArrayList<Double>());
		}

		loop: for (DataRow row : table) {
			if (id.equals(IO.getString(row.getCell(spec
					.findColumnIndex(NlsUtils.ID_COLUMN))))) {
				Map<String, Double> v = new LinkedHashMap<>();

				for (String var : f.getVariables()) {
					v.put(var, IO.getDouble(row.getCell(spec
							.findColumnIndex(var))));
				}

				for (String var : fixed.keySet()) {
					if (!fixed.get(var).equals(v.get(var))) {
						continue loop;
					}
				}

				if (MathUtils.containsInvalidDouble(v.values())) {
					continue;
				}

				for (String var : v.keySet()) {
					values.get(var).add(v.get(var));
				}
			}
		}

		Map<String, double[]> result = new LinkedHashMap<>();

		for (Map.Entry<String, List<Double>> entry : values.entrySet()) {
			result.put(entry.getKey(), Doubles.toArray(entry.getValue()));
		}

		return result;
	}

	public static Map<String, double[]> getDiffVariableValues(
			BufferedDataTable table, String id, Function f) {
		Map<String, List<Double>> values = new LinkedHashMap<>();
		DataTableSpec spec = table.getSpec();

		values.put(f.getTimeVariable(), new ArrayList<Double>());
		values.put(f.getDependentVariable(), new ArrayList<Double>());

		for (DataRow row : table) {
			if (id.equals(IO.getString(row.getCell(spec
					.findColumnIndex(NlsUtils.ID_COLUMN))))) {
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
}
