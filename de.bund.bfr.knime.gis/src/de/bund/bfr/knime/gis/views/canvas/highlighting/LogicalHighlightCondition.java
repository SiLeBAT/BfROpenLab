/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Doubles;

import de.bund.bfr.knime.gis.views.canvas.element.Element;

public class LogicalHighlightCondition implements Serializable {

	public static final String EQUAL_TYPE = "==";
	public static final String NOT_EQUAL_TYPE = "!=";
	public static final String GREATER_TYPE = ">";
	public static final String LESS_TYPE = "<";
	public static final String REGEX_EQUAL_TYPE = "== (Regex)";
	public static final String REGEX_NOT_EQUAL_TYPE = "!= (Regex)";
	public static final String REGEX_EQUAL_IGNORE_CASE_TYPE = "== (Regex Ignore Case)";
	public static final String REGEX_NOT_EQUAL_IGNORE_CASE_TYPE = "!= (Regex Ignore Case)";
	public static final ImmutableList<String> TYPES = ImmutableList.of(EQUAL_TYPE, NOT_EQUAL_TYPE, GREATER_TYPE,
			LESS_TYPE, REGEX_EQUAL_TYPE, REGEX_NOT_EQUAL_TYPE, REGEX_EQUAL_IGNORE_CASE_TYPE,
			REGEX_NOT_EQUAL_IGNORE_CASE_TYPE);

	private static final long serialVersionUID = 1L;

	private String property;
	private String type;
	private String value;

	private transient Double doubleValue;
	private transient Boolean booleanValue;

	public LogicalHighlightCondition() {
		this(null, null, null);
	}

	public LogicalHighlightCondition(String property, String type, String value) {
		setProperty(property);
		setType(type);
		setValue(value);
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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public <T extends Element> Map<T, Double> getValues(Collection<? extends T> elements) {
		Map<T, Double> values = new LinkedHashMap<>();

		doubleValue = value != null ? Doubles.tryParse(value) : null;

		if (value == null) {
			booleanValue = null;
		} else if (value.equalsIgnoreCase("true") || value.equals("1")) {
			booleanValue = true;
		} else if (value.equalsIgnoreCase("false") || value.equals("0")) {
			booleanValue = false;
		} else {
			booleanValue = null;
		}

		for (T element : elements) {
			Object nodeValue = element.getProperties().get(property);

			if (type.equals(EQUAL_TYPE)) {
				values.put(element, isEqual(nodeValue) ? 1.0 : 0.0);
			} else if (type.equals(NOT_EQUAL_TYPE)) {
				values.put(element, isEqual(nodeValue) ? 0.0 : 1.0);
			} else if (type.equals(GREATER_TYPE)) {
				values.put(element, isGreater(nodeValue) ? 1.0 : 0.0);
			} else if (type.equals(LESS_TYPE)) {
				values.put(element, isLess(nodeValue) ? 1.0 : 0.0);
			} else if (type.equals(REGEX_EQUAL_TYPE)) {
				values.put(element, isEqualRegex(nodeValue, false) ? 1.0 : 0.0);
			} else if (type.equals(REGEX_NOT_EQUAL_TYPE)) {
				values.put(element, isEqualRegex(nodeValue, false) ? 0.0 : 1.0);
			} else if (type.equals(REGEX_EQUAL_IGNORE_CASE_TYPE)) {
				values.put(element, isEqualRegex(nodeValue, true) ? 1.0 : 0.0);
			} else if (type.equals(REGEX_NOT_EQUAL_IGNORE_CASE_TYPE)) {
				values.put(element, isEqualRegex(nodeValue, true) ? 0.0 : 1.0);
			}
		}

		return values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LogicalHighlightCondition other = (LogicalHighlightCondition) obj;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	private boolean isEqual(Object nodeValue) {
		if (nodeValue instanceof Boolean && booleanValue != null) {
			return ((Boolean) nodeValue).booleanValue() == booleanValue;
		} else if (nodeValue instanceof Number && doubleValue != null) {
			return ((Number) nodeValue).doubleValue() == doubleValue;
		} else if (nodeValue instanceof String && value != null) {
			return ((String) nodeValue).equalsIgnoreCase(value);
		} else if (nodeValue == null || (nodeValue instanceof String && ((String) nodeValue).isEmpty())) {
			return Strings.isNullOrEmpty(value);
		} else {
			return false;
		}
	}

	private boolean isGreater(Object nodeValue) {
		if (nodeValue instanceof Number && doubleValue != null) {
			return ((Number) nodeValue).doubleValue() > doubleValue;
		}

		return false;
	}

	private boolean isLess(Object nodeValue) {
		if (nodeValue instanceof Number && doubleValue != null) {
			return ((Number) nodeValue).doubleValue() < doubleValue;
		}

		return false;
	}

	private boolean isEqualRegex(Object nodeValue, boolean ignoreCase) {
		if (nodeValue instanceof String && value != null) {
			int flags = ignoreCase ? Pattern.CASE_INSENSITIVE : 0;
			Matcher matcher = Pattern.compile(value, flags).matcher((String) nodeValue);

			return matcher.matches();
		} else if (nodeValue == null) {
			return Strings.isNullOrEmpty(value);
		} else {
			return false;
		}
	}

}
