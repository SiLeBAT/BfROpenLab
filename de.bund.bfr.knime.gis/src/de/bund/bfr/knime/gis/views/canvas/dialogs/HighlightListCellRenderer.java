/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.gis.views.canvas.dialogs;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition;

public class HighlightListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		Color color = ((HighlightCondition) value).getColor();

		setBorder(BorderFactory.createMatteBorder(0, 20, 0, 0, color != null ? color : Color.WHITE));
		setText(" " + getText((HighlightCondition) value));

		return this;
	}

	private static String getText(HighlightCondition condition) {
		if (condition.getName() != null) {
			return condition.getName();
		} else if (condition instanceof AndOrHighlightCondition) {
			return ((AndOrHighlightCondition) condition).getConditionCount() == 0 ? "Apply To All"
					: "Logical Condition";
		} else if (condition instanceof ValueHighlightCondition) {
			return "Value Condition";
		} else if (condition instanceof LogicalValueHighlightCondition) {
			return "Logical Value Condition";
		}

		throw new IllegalArgumentException("Unknown HighlightCondition");
	}
}
