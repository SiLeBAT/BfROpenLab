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

import org.knime.core.data.BooleanValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;

import com.google.common.base.Strings;

public class IO {

	private IO() {
	}

	public static DataCell createCell(String s) {
		return s != null ? new StringCell(s) : DataType.getMissingCell();
	}

	public static DataCell createCell(Integer i) {
		return i != null ? new IntCell(i) : DataType.getMissingCell();
	}

	public static DataCell createCell(Double d) {
		return d != null ? new DoubleCell(d) : DataType.getMissingCell();
	}

	public static DataCell createCell(Boolean b) {
		return b != null ? BooleanCell.get(b) : DataType.getMissingCell();
	}

	public static String getToString(DataCell cell) {
		return !cell.isMissing() ? cell.toString() : null;
	}

	public static String getToCleanString(DataCell cell) {
		return clean(getToString(cell));
	}

	public static String getString(DataCell cell) {
		return cell instanceof StringValue ? ((StringValue) cell).getStringValue() : null;
	}

	public static String getCleanString(DataCell cell) {
		return clean(getString(cell));
	}

	public static Integer getInt(DataCell cell) {
		return cell instanceof IntValue ? ((IntValue) cell).getIntValue() : null;
	}

	public static Double getDouble(DataCell cell) {
		return cell instanceof DoubleValue ? ((DoubleValue) cell).getDoubleValue() : null;
	}

	public static Boolean getBoolean(DataCell cell) {
		return cell instanceof BooleanValue ? ((BooleanValue) cell).getBooleanValue() : null;
	}

	private static String clean(String s) {
		return s != null
				? Strings.emptyToNull(
						s.replaceAll("\\p{C}", "").replace("\u00A0", "").replace("\t", " ").replace("\n", " ").trim())
				: null;
	}
}
