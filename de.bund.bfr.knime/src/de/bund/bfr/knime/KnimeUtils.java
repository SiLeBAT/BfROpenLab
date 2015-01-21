/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class KnimeUtils {

	private KnimeUtils() {
	}

	public static String listToString(List<?> list) {
		return Joiner.on(",").useForNull("null").join(list);
	}

	public static List<String> stringToList(String s) {
		return Lists.newArrayList(Splitter.on(",").omitEmptyStrings().split(s));
	}

	public static List<Double> stringToDoubleList(String s) {
		List<String> list = stringToList(s);
		List<Double> doubleList = new ArrayList<>();

		for (String value : list) {
			try {
				doubleList.add(Double.parseDouble(value));
			} catch (NumberFormatException e) {
				doubleList.add(null);
			}
		}

		return doubleList;
	}

	public static File getFile(String fileName) throws FileNotFoundException {
		File file = new File(fileName);

		if (!file.exists()) {
			try {
				file = new File(new URI(fileName).getPath());
			} catch (URISyntaxException e1) {
			}
		}

		if (!file.exists()) {
			throw new FileNotFoundException(fileName);
		}

		return file;
	}

	public static List<DataColumnSpec> getColumns(DataTableSpec spec,
			DataType... types) {
		List<DataColumnSpec> columns = new ArrayList<>();

		for (DataColumnSpec column : spec) {
			for (DataType type : types) {
				if (column.getType().equals(type)) {
					columns.add(column);
					break;
				}
			}
		}

		return columns;
	}

	@SuppressWarnings("unchecked")
	public static List<DataColumnSpec> getColumns(DataTableSpec spec,
			Class<? extends DataValue>... types) {
		List<DataColumnSpec> columns = new ArrayList<>();

		for (DataColumnSpec column : spec) {
			for (Class<? extends DataValue> type : types) {
				if (column.getType().isCompatible(type)) {
					columns.add(column);
					break;
				}
			}
		}

		return columns;
	}

	public static List<String> getColumnNames(List<DataColumnSpec> columns) {
		List<String> names = new ArrayList<>();

		for (DataColumnSpec column : columns) {
			names.add(column.getName());
		}

		return names;
	}

	public static String createNewValue(String value, Collection<String> values) {
		if (!values.contains(value)) {
			return value;
		}

		for (int i = 2;; i++) {
			String newValue = value + "_" + i;

			if (!values.contains(newValue)) {
				return newValue;
			}
		}
	}
}
