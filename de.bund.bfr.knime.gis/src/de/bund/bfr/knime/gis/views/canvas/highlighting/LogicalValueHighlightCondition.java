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

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Element;

public class LogicalValueHighlightCondition implements HighlightCondition, Serializable {

	private static final long serialVersionUID = 1L;

	private ValueHighlightCondition valueCondition;
	private AndOrHighlightCondition logicalCondition;

	public LogicalValueHighlightCondition() {
		this(new ValueHighlightCondition(), new AndOrHighlightCondition());
	}

	public LogicalValueHighlightCondition(ValueHighlightCondition valueCondition,
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
	public String getName() {
		return valueCondition.getName();
	}

	@Override
	public boolean isShowInLegend() {
		return valueCondition.isShowInLegend();
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
	public <T extends Element> Map<T, Double> getValues(Collection<? extends T> elements) {
		Map<T, Double> logicalValues = logicalCondition.getValues(elements);
		Map<T, Double> values = new LinkedHashMap<>();
		List<T> nonZeroElements = new ArrayList<>();
		double min = 1.0;
		double max = 0.0;

		for (T element : elements) {
			if (logicalValues.get(element) != 0.0) {
				double value = CanvasUtils.toPositiveDouble(element.getProperties().get(valueCondition.getProperty()));

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

		if (valueCondition.isZeroAsMinimum()) {
			min = 0.0;
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

		if (valueCondition.getType().equals(ValueHighlightCondition.LOG_VALUE_TYPE)) {
			for (T element : nonZeroElements) {
				values.put(element, Math.log10(values.get(element) * 9.0 + 1.0));
			}
		}

		return values;
	}

	@Override
	public Point2D getValueRange(Collection<? extends Element> elements) {
		Map<? extends Element, Double> logicalValues = logicalCondition.getValues(elements);
		DoubleSummaryStatistics stats = elements.stream().filter(e -> logicalValues.get(e) != 0.0)
				.mapToDouble(e -> CanvasUtils.toPositiveDouble(e.getProperties().get(valueCondition.getProperty())))
				.summaryStatistics();
		double min = valueCondition.isZeroAsMinimum() || stats.getCount() == 0 ? 0.0 : stats.getMin();
		double max = stats.getCount() == 0 ? 1.0 : stats.getMax();

		return new Point2D.Double(min, max);
	}

	@Override
	public HighlightCondition copy() {
		return new LogicalValueHighlightCondition(valueCondition.copy(), logicalCondition.copy());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + Objects.hashCode(logicalCondition);
		result = prime * result + Objects.hashCode(valueCondition);

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

		LogicalValueHighlightCondition c = (LogicalValueHighlightCondition) obj;

		return Objects.equals(valueCondition, c.valueCondition) && Objects.equals(logicalCondition, c.logicalCondition);
	}
}
