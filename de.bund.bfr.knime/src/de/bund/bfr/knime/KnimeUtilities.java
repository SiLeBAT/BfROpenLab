/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
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
package de.bund.bfr.knime;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;

public class KnimeUtilities {

	private KnimeUtilities() {
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

	public static Map<String, Class<?>> getTableColumns(DataTableSpec spec) {
		Map<String, Class<?>> tableColumns = new LinkedHashMap<String, Class<?>>();

		for (int i = 0; i < spec.getNumColumns(); i++) {
			DataColumnSpec cSpec = spec.getColumnSpec(i);

			if (cSpec.getType() == StringCell.TYPE) {
				tableColumns.put(cSpec.getName(), String.class);
			} else if (cSpec.getType() == IntCell.TYPE) {
				tableColumns.put(cSpec.getName(), Integer.class);
			} else if (cSpec.getType() == DoubleCell.TYPE) {
				tableColumns.put(cSpec.getName(), Double.class);
			} else if (cSpec.getType() == BooleanCell.TYPE) {
				tableColumns.put(cSpec.getName(), Boolean.class);
			}
		}

		return tableColumns;
	}

	public static DataColumnSpec[] getAllColumns(DataTableSpec spec) {
		List<DataColumnSpec> columns = new ArrayList<DataColumnSpec>();

		for (DataColumnSpec column : spec) {
			columns.add(column);
		}

		return columns.toArray(new DataColumnSpec[0]);
	}

	public static DataColumnSpec[] getStringColumns(DataTableSpec spec) {
		List<DataColumnSpec> columns = new ArrayList<DataColumnSpec>();

		for (DataColumnSpec column : spec) {
			if (column.getType() == StringCell.TYPE) {
				columns.add(column);
			}
		}

		return columns.toArray(new DataColumnSpec[0]);
	}

	public static DataColumnSpec[] getDoubleColumns(DataTableSpec spec) {
		List<DataColumnSpec> columns = new ArrayList<DataColumnSpec>();

		for (DataColumnSpec column : spec) {
			if (column.getType() == DoubleCell.TYPE) {
				columns.add(column);
			}
		}

		return columns.toArray(new DataColumnSpec[0]);
	}

	public static DataColumnSpec[] getStringIntColumns(DataTableSpec spec) {
		List<DataColumnSpec> columns = new ArrayList<DataColumnSpec>();

		for (DataColumnSpec column : spec) {
			if (column.getType() == StringCell.TYPE
					|| column.getType() == IntCell.TYPE) {
				columns.add(column);
			}
		}

		return columns.toArray(new DataColumnSpec[0]);
	}
}
