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
package de.bund.bfr.knime.gis.views.canvas.highlighting;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.bund.bfr.knime.gis.views.canvas.element.Element;

public class LogicalValueHighlightCondition implements HighlightCondition,
		Serializable {

	private static final long serialVersionUID = 1L;

	private ValueHighlightCondition valueCondition;
	private AndOrHighlightCondition logicalCondition;

	public LogicalValueHighlightCondition() {
		this(new ValueHighlightCondition(), new AndOrHighlightCondition());
	}

	public LogicalValueHighlightCondition(
			ValueHighlightCondition valueCondition,
			AndOrHighlightCondition logicalCondition) {
		this.valueCondition = valueCondition;
		this.logicalCondition = logicalCondition;
	}

	public ValueHighlightCondition getValueCondition() {
		return valueCondition;
	}

	public void setValueCondition(ValueHighlightCondition valueCondition) {
		this.valueCondition = valueCondition;
	}

	public AndOrHighlightCondition getLogicalCondition() {
		return logicalCondition;
	}

	public void setLogicalCondition(AndOrHighlightCondition logicalCondition) {
		this.logicalCondition = logicalCondition;
	}

	@Override
	public Color getColor() {
		return valueCondition.getColor();
	}

	@Override
	public boolean isInvisible() {
		return valueCondition.isInvisible();
	}

	@Override
	public boolean isUseThickness() {
		return valueCondition.isUseThickness();
	}

	@Override
	public String getLabelProperty() {
		return valueCondition.getLabelProperty();
	}

	@Override
	public <T extends Element> Map<T, Double> getValues(Collection<T> elements) {
		String type = valueCondition.getType();

		valueCondition.setType(ValueHighlightCondition.VALUE_TYPE);

		Map<T, Double> valueValues = valueCondition.getValues(elements);
		Map<T, Double> logicalValues = logicalCondition.getValues(elements);

		valueCondition.setType(type);

		Map<T, Double> values = new LinkedHashMap<T, Double>();
		List<T> nonZeroElements = new ArrayList<T>();
		double min = 1.0;
		double max = 0.0;

		for (T element : elements) {
			if (logicalValues.get(element) != 0.0) {
				double value = valueValues.get(element);

				values.put(element, value);
				nonZeroElements.add(element);
				min = Math.min(min, value);
				max = Math.max(max, value);
			} else {
				values.put(element, 0.0);
			}
		}

		if (nonZeroElements.isEmpty()) {
			return values;
		}

		if (min != 0.0) {
			for (T element : nonZeroElements) {
				values.put(element, values.get(element) - min);
			}
		}

		if (max > min) {
			double diff = max - min;

			for (T element : nonZeroElements) {
				values.put(element, values.get(element) / diff);
			}
		}

		if (type.equals(ValueHighlightCondition.LOG_VALUE_TYPE)) {
			for (T element : nonZeroElements) {
				values.put(element, Math.log10(values.get(element) * 9.0 + 1.0));
			}
		}

		for (T element : nonZeroElements) {
			if (values.get(element) == 0.0) {
				values.put(element, Double.MIN_VALUE);
			}
		}

		return values;
	}

	@Override
	public String toString() {
		return "Logical Value Condition";
	}
}
