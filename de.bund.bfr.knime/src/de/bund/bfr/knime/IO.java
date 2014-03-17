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

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
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

	public static DataCell createCellFromObject(Object o) {
		if (o instanceof String) {
			return createCell((String) o);
		} else if (o instanceof Integer) {
			return createCell((Integer) o);
		} else if (o instanceof Double) {
			return createCell((Double) o);
		} else if (o instanceof Boolean) {
			return createCell((Boolean) o);
		}

		return DataType.getMissingCell();
	}

	public static String getToString(DataCell cell) {
		if (cell.isMissing()) {
			return null;
		}

		return cell.toString().trim();
	}

	public static String getToCleanString(DataCell cell) {
		return clean(getToString(cell));
	}

	public static String getString(DataCell cell) {
		if (cell instanceof StringCell) {
			return ((StringCell) cell).getStringValue();
		}

		return null;
	}

	public static String getCleanString(DataCell cell) {
		return clean(getString(cell));
	}

	public static Integer getInt(DataCell cell) {
		if (cell instanceof IntCell) {
			return ((IntCell) cell).getIntValue();
		}

		return null;
	}

	public static Double getDouble(DataCell cell) {
		if (cell instanceof DoubleCell) {
			return ((DoubleCell) cell).getDoubleValue();
		}

		return null;
	}

	public static Boolean getBoolean(DataCell cell) {
		if (cell instanceof BooleanCell) {
			return ((BooleanCell) cell).getBooleanValue();
		}

		return null;
	}

	public static Object getObject(DataCell cell) {
		String s = getString(cell);
		Integer i = getInt(cell);
		Double d = getDouble(cell);
		Boolean b = getBoolean(cell);

		if (s != null) {
			return s;
		} else if (i != null) {
			return i;
		} else if (d != null) {
			return d;
		} else if (b != null) {
			return b;
		}

		return null;
	}

	private static String clean(String s) {
		if (s == null) {
			return null;
		}

		return s.replace("\u00A0", " ").replace("\t", " ").replace("\n", " ")
				.trim();
	}
}
