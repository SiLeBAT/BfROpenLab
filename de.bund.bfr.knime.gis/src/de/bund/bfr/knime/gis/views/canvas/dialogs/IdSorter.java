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
package de.bund.bfr.knime.gis.views.canvas.dialogs;

import java.util.Comparator;
import java.util.Set;

import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class IdSorter extends TableRowSorter<TableModel> {

	private Set<String> idColumns;

	public IdSorter(TableModel model, Set<String> idColumns) {
		super(model);
		this.idColumns = idColumns;
	}

	@Override
	public Comparator<?> getComparator(int column) {
		String name = getModel().getColumnName(column);

		if (idColumns.contains(name)) {
			return new IdComparator();
		}

		return super.getComparator(column);
	}

	private static class IdComparator implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			Integer i1 = null;
			Integer i2 = null;

			try {
				i1 = Integer.parseInt(o1);
			} catch (NumberFormatException e) {
			}

			try {
				i2 = Integer.parseInt(o2);
			} catch (NumberFormatException e) {
			}

			if (i1 != null && i2 != null) {
				return i1.compareTo(i2);
			} else if (i1 != null) {
				return -1;
			} else if (i2 != null) {
				return 1;
			} else {
				return o1.compareTo(o2);
			}
		}
	}
}
