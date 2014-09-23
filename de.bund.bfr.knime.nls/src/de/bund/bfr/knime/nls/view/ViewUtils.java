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

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.nls.Function;
import de.bund.bfr.knime.nls.NlsUtils;

public class ViewUtils {

	private ViewUtils() {
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
}
