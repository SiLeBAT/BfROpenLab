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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.data.xml.XMLCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.xml.sax.SAXException;

public class KnimeUtilities {

	private KnimeUtilities() {
	}

	public static DataTableSpec getXmlSpec() {
		return new DataTableSpec(
				new DataColumnSpec[] { new DataColumnSpecCreator("XML",
						XMLCell.TYPE).createSpec() });
	}

	public static BufferedDataTable xmlToTable(String xml, ExecutionContext exec)
			throws IOException, ParserConfigurationException, SAXException,
			XMLStreamException {
		BufferedDataContainer container = exec
				.createDataContainer(getXmlSpec());

		container
				.addRowToTable(new DefaultRow("0", XMLCellFactory.create(xml)));
		container.close();

		return container.getTable();
	}

	public static String tableToXml(BufferedDataTable table) {
		for (DataRow row : table) {
			return ((StringValue) row.getCell(0)).getStringValue();
		}

		return null;
	}

	public static String listToString(List<String> list) {
		String result = "";

		for (String s : list) {
			result += s + ",";
		}

		if (!result.isEmpty()) {
			result = result.substring(0, result.length() - 1);
		}

		return result;
	}

	public static List<String> stringToList(String s) {
		return new ArrayList<String>(Arrays.asList(s.split(",")));
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

			if (cSpec.getType().equals(StringCell.TYPE)) {
				tableColumns.put(cSpec.getName(), String.class);
			} else if (cSpec.getType().equals(IntCell.TYPE)) {
				tableColumns.put(cSpec.getName(), Integer.class);
			} else if (cSpec.getType().equals(DoubleCell.TYPE)) {
				tableColumns.put(cSpec.getName(), Double.class);
			} else if (cSpec.getType().equals(BooleanCell.TYPE)) {
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
			if (column.getType().equals(StringCell.TYPE)) {
				columns.add(column);
			}
		}

		return columns.toArray(new DataColumnSpec[0]);
	}

	public static DataColumnSpec[] getDoubleColumns(DataTableSpec spec) {
		List<DataColumnSpec> columns = new ArrayList<DataColumnSpec>();

		for (DataColumnSpec column : spec) {
			if (column.getType().equals(DoubleCell.TYPE)) {
				columns.add(column);
			}
		}

		return columns.toArray(new DataColumnSpec[0]);
	}

	public static DataColumnSpec[] getStringIntColumns(DataTableSpec spec) {
		List<DataColumnSpec> columns = new ArrayList<DataColumnSpec>();

		for (DataColumnSpec column : spec) {
			if (column.getType().equals(StringCell.TYPE)
					|| column.getType().equals(IntCell.TYPE)) {
				columns.add(column);
			}
		}

		return columns.toArray(new DataColumnSpec[0]);
	}

	public static List<String> getColumnNames(DataColumnSpec[] columns) {
		List<String> names = new ArrayList<String>();

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

	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}

		return true;
	}
}
