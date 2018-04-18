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

public class ValueHighlightCondition implements HighlightCondition, Serializable {

	public static enum Type {
		VALUE("Value"), LOG_VALUE("Log Value");

		private String name;

		private Type(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private static final long serialVersionUID = 1L;

	private String property;
	private Type type;
	private boolean zeroAsMinimum;
	private String name;
	private boolean showInLegend;
	private Color color;
	private boolean invisible;
	private boolean useThickness;
	private String labelProperty;
	private NamedShape shape;

	public ValueHighlightCondition() {
		this(null, null, false, null, true, null, false, false, null, null);
	}

	public ValueHighlightCondition(String property, Type type, boolean zeroAsMinimum, String name, boolean showInLegend,
			Color color, boolean invisible, boolean useThickness, String labelProperty, NamedShape shape) {
		setProperty(property);
		setType(type);
		setZeroAsMinimum(zeroAsMinimum);
		setName(name);
		setShowInLegend(showInLegend);
		setColor(color);
		setInvisible(invisible);
		setUseThickness(useThickness);
		setLabelProperty(labelProperty);
		setShape(shape);
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public boolean isZeroAsMinimum() {
		return zeroAsMinimum;
	}

	public void setZeroAsMinimum(boolean zeroAsMinimum) {
		this.zeroAsMinimum = zeroAsMinimum;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean isShowInLegend() {
		return showInLegend;
	}

	public void setShowInLegend(boolean showInLegend) {
		this.showInLegend = showInLegend;
	}

	@Override
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public boolean isInvisible() {
		return invisible;
	}

	public void setInvisible(boolean invisible) {
		this.invisible = invisible;
	}

	@Override
	public boolean isUseThickness() {
		return useThickness;
	}

	public void setUseThickness(boolean useThickness) {
		this.useThickness = useThickness;
	}

	@Override
	public String getLabelProperty() {
		return labelProperty;
	}

	public void setLabelProperty(String labelProperty) {
		this.labelProperty = labelProperty;
	}

	@Override
	public NamedShape getShape() {
		return shape;
	}

	public void setShape(NamedShape shape) {
		this.shape = shape;
	}

	@Override
	public <T extends Element> Map<T, Double> getValues(Collection<? extends T> elements) {
		Map<T, Double> values = new LinkedHashMap<>();

		for (T element : elements) {
			values.put(element, CanvasUtils.toPositiveDouble(element.getProperties().get(property)));
		}

		if (values.isEmpty()) {
			return values;
		}

		double min = zeroAsMinimum ? 0.0 : Collections.min(values.values());

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

		if (type == Type.LOG_VALUE) {
			for (Map.Entry<T, Double> entry : values.entrySet()) {
				entry.setValue(Math.log10(entry.getValue() * 9.0 + 1.0));
			}
		}

		return values;
	}

	@Override
	public Point2D getValueRange(Collection<? extends Element> elements) {
		DoubleSummaryStatistics stats = elements.stream()
				.mapToDouble(e -> CanvasUtils.toPositiveDouble(e.getProperties().get(property))).summaryStatistics();
		double min = zeroAsMinimum || stats.getCount() == 0 ? 0.0 : stats.getMin();
		double max = stats.getCount() == 0 ? 1.0 : stats.getMax();

		return new Point2D.Double(min, max);
	}

	@Override
	public ValueHighlightCondition copy() {
		return new ValueHighlightCondition(property, type, zeroAsMinimum, name, showInLegend, color, invisible,
				useThickness, labelProperty, shape);
	}

	@Override
	public int hashCode() {
		return Objects.hash(property, type, zeroAsMinimum, name, showInLegend, color, invisible, useThickness,
				labelProperty, shape);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		ValueHighlightCondition other = (ValueHighlightCondition) obj;

		return Objects.equals(property, other.property) && type == other.type && zeroAsMinimum == other.zeroAsMinimum
				&& Objects.equals(name, other.name) && showInLegend == other.showInLegend
				&& Objects.equals(color, other.color) && invisible == other.invisible
				&& useThickness == other.useThickness && Objects.equals(labelProperty, other.labelProperty)
				&& shape == other.shape;
	}
}
