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
package de.bund.bfr.knime.gis.views.canvas.highlighting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HighlightConditionList implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<HighlightCondition> conditions;
	private boolean prioritizeColors;

	public HighlightConditionList() {
		this(new ArrayList<HighlightCondition>(), false);
	}

	public HighlightConditionList(List<HighlightCondition> conditions, boolean prioritizeColors) {
		this.conditions = conditions;
		this.prioritizeColors = prioritizeColors;
	}

	public List<HighlightCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<HighlightCondition> conditions) {
		this.conditions = conditions;
	}

	public boolean isPrioritizeColors() {
		return prioritizeColors;
	}

	public void setPrioritizeColors(boolean prioritizeColors) {
		this.prioritizeColors = prioritizeColors;
	}

	public HighlightConditionList copy() {
		List<HighlightCondition> conditionsCopy = new ArrayList<>();

		for (HighlightCondition condition : conditions) {
			conditionsCopy.add(condition.copy());
		}

		return new HighlightConditionList(conditionsCopy, prioritizeColors);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + Objects.hashCode(conditions);
		result = prime * result + (prioritizeColors ? 1231 : 1237);

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}

		HighlightConditionList l = (HighlightConditionList) obj;

		return Objects.equals(conditions, l.conditions) && prioritizeColors == l.prioritizeColors;
	}
}
