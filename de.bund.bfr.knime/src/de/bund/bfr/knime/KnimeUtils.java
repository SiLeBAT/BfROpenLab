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
package de.bund.bfr.knime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.data.xml.XMLCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.xml.sax.SAXException;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class KnimeUtils {

	private KnimeUtils() {
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
