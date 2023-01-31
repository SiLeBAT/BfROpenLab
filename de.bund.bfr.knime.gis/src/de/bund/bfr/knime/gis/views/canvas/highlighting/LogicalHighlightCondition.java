/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.google.common.primitives.Doubles;

import de.bund.bfr.knime.gis.views.canvas.element.Element;

public class LogicalHighlightCondition implements Serializable {

	public static enum Type {
		EQUAL("=="), NOT_EQUAL("!="), GREATER(">"), LESS("<"),

		REGEX_EQUAL("== (Regex)"), REGEX_NOT_EQUAL("!= (Regex)"),

		REGEX_EQUAL_IGNORE_CASE("== (Regex Ignore Case)"), REGEX_NOT_EQUAL_IGNORE_CASE("!= (Regex Ignore Case)");

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
	private String value;

	private transient Double doubleValue;
	private transient Boolean booleanValue;

	public LogicalHighlightCondition() {
		this(null, null, null);
	}

	public LogicalHighlightCondition(String property, Type type, String value) {
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

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public <T extends Element> Map<T, Double> getValues(Collection<? extends T> elements) {
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

		Map<T, Double> result = new LinkedHashMap<>();

		elements.forEach(e -> result.put(e, evaluate(e)));

		return result;
	}

	@Override
	public int hashCode() {
		return Objects.hash(property, type, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		LogicalHighlightCondition other = (LogicalHighlightCondition) obj;

		return Objects.equals(property, other.property) && type == other.type && Objects.equals(value, other.value);
	}

	private double evaluate(Element element) {
		Object nodeValue = element.getProperties().get(property);

		switch (type) {
		case EQUAL:
			return isEqual(nodeValue) ? 1.0 : 0.0;
		case NOT_EQUAL:
			return isEqual(nodeValue) ? 0.0 : 1.0;
		case GREATER:
			return isGreater(nodeValue) ? 1.0 : 0.0;
		case LESS:
			return isLess(nodeValue) ? 1.0 : 0.0;
		case REGEX_EQUAL:
			return isEqualRegex(nodeValue, false) ? 1.0 : 0.0;
		case REGEX_NOT_EQUAL:
			return isEqualRegex(nodeValue, false) ? 0.0 : 1.0;
		case REGEX_EQUAL_IGNORE_CASE:
			return isEqualRegex(nodeValue, true) ? 1.0 : 0.0;
		case REGEX_NOT_EQUAL_IGNORE_CASE:
			return isEqualRegex(nodeValue, true) ? 0.0 : 1.0;
		default:
			throw new RuntimeException("Unknown type of LogicalHighlightCondition: " + type);
		}
	}

	private boolean isEqual(Object nodeValue) {
		if (nodeValue instanceof Boolean && booleanValue != null) {
			return ((Boolean) nodeValue).booleanValue() == booleanValue;
		} else if (nodeValue instanceof Number && doubleValue != null) {
			return ((Number) nodeValue).doubleValue() == doubleValue;
		} else if (nodeValue instanceof String && value != null) {
			return ((String) nodeValue).equalsIgnoreCase(value);
		} else if (nodeValue == null || nodeValue.equals("")) {
			return Strings.isNullOrEmpty(value);
		} else {
			return false;
		}
	}

	private boolean isGreater(Object nodeValue) {
		if (nodeValue instanceof Number && doubleValue != null) {
			return ((Number) nodeValue).doubleValue() > doubleValue;
		} else if (nodeValue instanceof String && value != null ) {
          return ((String) nodeValue).compareToIgnoreCase(value) > 0;
        }

		return false;
	}

	private boolean isLess(Object nodeValue) {
		if (nodeValue instanceof Number && doubleValue != null) {
			return ((Number) nodeValue).doubleValue() < doubleValue;
		} else if (nodeValue instanceof String && value != null ) {
          return ((String) nodeValue).compareToIgnoreCase(value) < 0;
        }

		return false;
	}

	private boolean isEqualRegex(Object nodeValue, boolean ignoreCase) {
		if (nodeValue instanceof String && value != null) {
			int flags = ignoreCase ? Pattern.CASE_INSENSITIVE : 0;
			Matcher matcher = Pattern.compile(value, flags).matcher((String) nodeValue);

			return matcher.matches();
		} else if (nodeValue == null || nodeValue.equals("")) {
			return Strings.isNullOrEmpty(value);
		} else {
			return false;
		}
	}
}
