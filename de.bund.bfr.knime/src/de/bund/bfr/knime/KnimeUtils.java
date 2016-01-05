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

import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.util.FileUtil;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import de.bund.bfr.knime.ui.Dialogs;

public class KnimeUtils {

	public static final Ordering<Object> OBJECT_ORDERING = Ordering.from((o1, o2) -> {
		if (o1 == o2) {
			return 0;
		} else if (o1 == null) {
			return -1;
		} else if (o2 == null) {
			return 1;
		} else if (o1 instanceof String && o2 instanceof String) {
			return ((String) o1).toLowerCase().compareTo(((String) o2).toLowerCase());
		} else if (o1 instanceof Integer && o2 instanceof Integer) {
			return ((Integer) o1).compareTo((Integer) o2);
		} else if (o1 instanceof Double && o2 instanceof Double) {
			return ((Double) o1).compareTo((Double) o2);
		} else if (o1 instanceof Boolean && o2 instanceof Boolean) {
			return ((Boolean) o1).compareTo((Boolean) o2);
		}

		return o1.toString().toLowerCase().compareTo(o2.toString().toLowerCase());
	});

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

	public static File getFile(String fileName) throws InvalidPathException, MalformedURLException {
		return FileUtil.getFileFromURL(FileUtil.toURL(fileName));
	}

	public static List<DataColumnSpec> getColumns(DataTableSpec spec, DataType... types) {
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
	public static List<DataColumnSpec> getColumns(DataTableSpec spec, Class<? extends DataValue>... types) {
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

	public static <T> List<T> nullToEmpty(List<T> list) {
		return list != null ? list : new ArrayList<>(0);
	}

	public static <T> Set<T> nullToEmpty(Set<T> set) {
		return set != null ? set : new LinkedHashSet<>(0);
	}

	public static <V, K> Map<V, K> nullToEmpty(Map<V, K> map) {
		return map != null ? map : new LinkedHashMap<>(0);
	}

	public static void showWarningWhenDialogOpens(Component c, String warning) {
		new Thread(() -> {
			while (true) {
				Window window = SwingUtilities.getWindowAncestor(c);

				if (window != null && window.isActive()) {
					Dialogs.showWarningMessage(c, warning, "Warning");
					break;
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public static void assertColumnNotMissing(DataTableSpec spec, String columnName) throws NotConfigurableException {
		assertColumnNotMissing(spec, columnName, null);
	}

	public static void assertColumnNotMissing(DataTableSpec spec, String columnName, String errorPrefix)
			throws NotConfigurableException {
		if (!spec.containsName(columnName)) {
			throw new NotConfigurableException(
					Strings.nullToEmpty(errorPrefix) + "Column \"" + columnName + "\" is missing");
		}
	}
}
