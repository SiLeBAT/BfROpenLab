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
package de.bund.bfr.knime.nls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataTable;

import com.google.common.base.Functions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;

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
		return new LinkedHashMap<>(Maps.asMap(new LinkedHashSet<>(keys), Functions.constant(0.0)));
	}

	public static List<String> getIds(BufferedDataTable table) {
		if (table == null) {
			return Collections.emptyList();
		}

		Set<String> ids = new LinkedHashSet<>();

		for (DataRow row : table) {
			String id = IO.getString(row.getCell(table.getSpec().findColumnIndex(NlsUtils.ID_COLUMN)));

			if (id != null) {
				ids.add(id);
			}
		}

		return new ArrayList<>(ids);
	}

	public static List<String> getQualityColumns(BufferedDataTable table, Function f) {
		if (table == null) {
			return Collections.emptyList();
		}

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
		for (DataRow row : getRowsById(table, id)) {
			Map<String, Double> values = new LinkedHashMap<>();

			for (String column : columns) {
				DataCell cell = row.getCell(table.getSpec().findColumnIndex(column));

				if (IO.getDouble(cell) != null) {
					values.put(column, IO.getDouble(cell));
				} else if (IO.getInt(cell) != null) {
					values.put(column, IO.getInt(cell).doubleValue());
				} else {
					values.put(column, null);
				}
			}

			return values;
		}

		return new LinkedHashMap<>();
	}

	public static Map<String, Double> getParameters(BufferedDataTable table, String id, Function f) {
		for (DataRow row : getRowsById(table, id)) {
			Map<String, Double> params = new LinkedHashMap<>();

			for (String param : f.getParameters()) {
				params.put(param, IO.getDouble(row.getCell(table.getSpec().findColumnIndex(param))));
			}

			return params;
		}

		return createZeroMap(f.getParameters());
	}

	public static Map<String, Map<String, Double>> getCovariances(BufferedDataTable table, String id, Function f) {
		Map<String, Map<String, Double>> covariances = new LinkedHashMap<>();

		for (DataRow row : getRowsById(table, id)) {
			Map<String, Double> cov = new LinkedHashMap<>();

			for (String param : f.getParameters()) {
				cov.put(param, IO.getDouble(row.getCell(table.getSpec().findColumnIndex(param))));
			}

			covariances.put(IO.getString(row.getCell(table.getSpec().findColumnIndex(NlsUtils.PARAM_COLUMN))), cov);
		}

		return covariances;
	}

	public static Map<String, List<Double>> getConditionValues(BufferedDataTable table, String id, Function f) {
		ListMultimap<String, Double> values = ArrayListMultimap.create();

		for (DataRow row : getRowsById(table, id)) {
			Map<String, Double> newValues = new LinkedHashMap<>();

			for (String var : f.getIndependentVariables()) {
				newValues.put(var, IO.getDouble(row.getCell(table.getSpec().findColumnIndex(var))));
			}

			if (newValues.values().stream().allMatch(v -> v != null && Double.isFinite(v))) {
				newValues.forEach((var, value) -> values.put(var, value));
			}
		}

		return Multimaps.asMap(values);
	}

	public static List<Map<String, Double>> getFixedVariables(BufferedDataTable table, String id, Function f,
			String indep) {
		List<Map<String, Double>> values = new ArrayList<>();
		Iterable<String> fixedVariables = Iterables.filter(f.getIndependentVariables(), var -> !var.equals(indep));

		for (DataRow row : getRowsById(table, id)) {
			Map<String, Double> currentValues = new LinkedHashMap<>();

			for (String var : fixedVariables) {
				currentValues.put(var, IO.getDouble(row.getCell(table.getSpec().findColumnIndex(var))));
			}

			if (!values.contains(currentValues)) {
				values.add(currentValues);
			}
		}

		return values;
	}

	public static Map<String, List<Double>> getVariableValues(BufferedDataTable table, String id, Function f,
			Map<String, Double> fixed) {
		ListMultimap<String, Double> values = ArrayListMultimap.create();

		for (DataRow row : getRowsById(table, id)) {
			Map<String, Double> newValues = new LinkedHashMap<>();

			for (String var : f.getVariables()) {
				newValues.put(var, IO.getDouble(row.getCell(table.getSpec().findColumnIndex(var))));
			}

			if (newValues.values().stream().allMatch(v -> v != null && Double.isFinite(v))
					&& fixed.entrySet().stream().allMatch(e -> e.getValue().equals(newValues.get(e.getKey())))) {
				newValues.forEach((var, value) -> values.put(var, value));
			}
		}

		return Multimaps.asMap(values);
	}

	public static Map<String, List<Double>> getDiffVariableValues(BufferedDataTable table, String id, Function f) {
		ListMultimap<String, Double> values = ArrayListMultimap.create();

		for (DataRow row : getRowsById(table, id)) {
			Double time = IO.getDouble(row.getCell(table.getSpec().findColumnIndex(f.getTimeVariable())));
			Double target = IO.getDouble(row.getCell(table.getSpec().findColumnIndex(f.getDependentVariable())));

			if (time != null && target != null && Double.isFinite(time) && Double.isFinite(target)) {
				values.put(f.getTimeVariable(), time);
				values.put(f.getDependentVariable(), target);
			}

		}

		return Multimaps.asMap(values);
	}

	public static List<String> getSortedVariables(Collection<Plotable> plotables) {
		return KnimeUtils.ORDERING.sortedCopy(plotables.stream().map(p -> p.getIndependentVariables().keySet())
				.flatMap(Set::stream).collect(Collectors.toSet()));
	}

	public static List<String> getSortedParameters(Collection<Plotable> plotables) {
		return KnimeUtils.ORDERING.sortedCopy(plotables.stream().map(p -> p.getParameters().keySet())
				.flatMap(Set::stream).collect(Collectors.toSet()));
	}

	private static Iterable<DataRow> getRowsById(BufferedDataTable table, String id) {
		if (table == null) {
			return Collections.emptyList();
		}

		int idColumn = table.getSpec().findColumnIndex(NlsUtils.ID_COLUMN);

		if (idColumn == -1) {
			return Collections.emptyList();
		}

		return Iterables.filter(table, row -> id.equals(IO.getString(row.getCell(idColumn))));
	}
}
