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

public class IO {

	private IO() {
	}

	public static DataCell createCell(String s) {
		if (s != null) {
			return new StringCell(s);
		}

		return DataType.getMissingCell();
	}

	public static DataCell createCell(Integer i) {
		if (i != null) {
			return new IntCell(i);
		}

		return DataType.getMissingCell();
	}

	public static DataCell createCell(Double d) {
		if (d != null) {
			return new DoubleCell(d);
		}

		return DataType.getMissingCell();
	}

	public static DataCell createCell(Boolean b) {
		if (b != null) {
			return BooleanCell.get(b);
		}

		return DataType.getMissingCell();
	}

	public static String getToString(DataCell cell) {
		if (cell.isMissing()) {
			return null;
		}

		return cell.toString();
	}

	public static String getToCleanString(DataCell cell) {
		return clean(getToString(cell));
	}

	public static String getString(DataCell cell) {
		if (cell instanceof StringValue) {
			return ((StringValue) cell).getStringValue();
		}

		return null;
	}

	public static String getCleanString(DataCell cell) {
		return clean(getString(cell));
	}

	public static Integer getInt(DataCell cell) {
		if (cell instanceof IntValue) {
			return ((IntValue) cell).getIntValue();
		}

		return null;
	}

	public static Double getDouble(DataCell cell) {
		if (cell instanceof DoubleValue) {
			return ((DoubleValue) cell).getDoubleValue();
		}

		return null;
	}

	public static Boolean getBoolean(DataCell cell) {
		if (cell instanceof BooleanValue) {
			return ((BooleanValue) cell).getBooleanValue();
		}

		return null;
	}

	private static String clean(String s) {
		if (s == null) {
			return null;
		}

		String cleaned = s.replaceAll("\\p{C}", "").replace("\u00A0", "")
				.replace("\t", " ").replace("\n", " ").trim();

		return cleaned.isEmpty() ? null : cleaned;
	}
}
