/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
import java.util.Collection;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import de.bund.bfr.jung.NamedShape;
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
	public NamedShape getShape() {
		return valueCondition.getShape();
	}

	@Override
	public <T extends Element> Map<T, Double> getValues(Collection<? extends T> elements) {
		Map<T, Double> logicalValues = logicalCondition.getValues(elements);
		Map<T, Double> values = new LinkedHashMap<>();
		Map<T, Double> zeroValues = new LinkedHashMap<>();

		for (T element : elements) {
			if (logicalValues.get(element) != 0.0) {
				values.put(element,
						CanvasUtils.toPositiveDouble(element.getProperties().get(valueCondition.getProperty())));
			} else {
				zeroValues.put(element, 0.0);
			}
		}

		if (values.isEmpty()) {
			return zeroValues;
		}

		double min = valueCondition.isZeroAsMinimum() ? 0.0 : Collections.min(values.values());

		if (min != 0.0) {
			for (Map.Entry<T, Double> entry : values.entrySet()) {
				entry.setValue(entry.getValue() - min);
			}
		}

		double max = Collections.max(values.values());

		if (max != 0.0) {
			for (Map.Entry<T, Double> entry : values.entrySet()) {
				entry.setValue(entry.getValue() / max);
			}
		}

		if (valueCondition.getType() == ValueHighlightCondition.Type.LOG_VALUE) {
			for (Map.Entry<T, Double> entry : values.entrySet()) {
				entry.setValue(Math.log10(entry.getValue() * 9.0 + 1.0));
			}
		}

		values.putAll(zeroValues);

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
	public LogicalValueHighlightCondition copy() {
		return new LogicalValueHighlightCondition(valueCondition.copy(), logicalCondition.copy());
	}

	@Override
	public int hashCode() {
		return Objects.hash(logicalCondition, valueCondition);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		LogicalValueHighlightCondition other = (LogicalValueHighlightCondition) obj;

		return Objects.equals(logicalCondition, other.logicalCondition)
				&& Objects.equals(valueCondition, other.valueCondition);
	}
}
