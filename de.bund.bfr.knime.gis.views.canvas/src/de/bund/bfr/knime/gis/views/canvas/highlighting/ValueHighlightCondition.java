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
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.bund.bfr.knime.gis.views.canvas.element.Element;

public class ValueHighlightCondition implements HighlightCondition,
		Serializable {

	public static final String VALUE_TYPE = "Value";
	public static final String LOG_VALUE_TYPE = "Log Value";
	public static final String[] TYPES = { VALUE_TYPE, LOG_VALUE_TYPE };

	private static final long serialVersionUID = 1L;

	private String property;
	private String type;
	private String name;
	private Color color;
	private boolean invisible;
	private boolean useThickness;
	private String labelProperty;

	public ValueHighlightCondition() {
		this(null, null, null, null, false, false, null);
	}

	public ValueHighlightCondition(String property, String type, String name,
			Color color, boolean invisible, boolean useThickness,
			String labelProperty) {
		setProperty(property);
		setType(type);
		setName(name);
		setColor(color);
		setInvisible(invisible);
		setUseThickness(useThickness);
		setLabelProperty(labelProperty);
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	public <T extends Element> Map<T, Double> getValues(Collection<T> elements) {
		Map<T, Double> values = new LinkedHashMap<T, Double>();

		for (T element : elements) {
			Object value = element.getProperties().get(property);

			if (value instanceof Number) {
				double doubleValue = ((Number) value).doubleValue();

				if (!Double.isNaN(doubleValue)
						&& !Double.isInfinite(doubleValue)
						&& doubleValue >= 0.0) {
					values.put(element, doubleValue);
				} else {
					values.put(element, 0.0);
				}
			} else {
				values.put(element, 0.0);
			}
		}

		double min = Collections.min(values.values());

		if (min != 0.0) {
			for (T element : elements) {
				values.put(element, values.get(element) - min);
			}
		}

		double max = Collections.max(values.values());

		if (max != 0.0) {
			for (T element : elements) {
				values.put(element, values.get(element) / max);
			}
		}

		if (type.equals(LOG_VALUE_TYPE)) {
			for (T element : elements) {
				values.put(element, Math.log10(values.get(element) * 9.0 + 1.0));
			}
		}

		for (T element : elements) {
			if (values.get(element) == 0.0) {
				values.put(element, Double.MIN_VALUE);
			}
		}

		return values;
	}

	@Override
	public Point2D getValueRange(Collection<? extends Element> elements) {
		List<Double> values = new ArrayList<Double>();

		for (Element element : elements) {
			Object value = element.getProperties().get(property);

			if (value instanceof Number) {
				double doubleValue = ((Number) value).doubleValue();

				if (!Double.isNaN(doubleValue)
						&& !Double.isInfinite(doubleValue)
						&& doubleValue >= 0.0) {
					values.add(doubleValue);
				} else {
					values.add(0.0);
				}
			} else {
				values.add(0.0);
			}
		}

		return new Point2D.Double(Collections.min(values),
				Collections.max(values));
	}

	@Override
	public String toString() {
		return getName() != null ? getName() : "Value Condition";
	}

}
