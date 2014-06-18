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

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	public static final String[] TYPES = { EQUAL_TYPE, NOT_EQUAL_TYPE,
			GREATER_TYPE, LESS_TYPE, REGEX_EQUAL_TYPE, REGEX_NOT_EQUAL_TYPE,
			REGEX_EQUAL_IGNORE_CASE_TYPE, REGEX_NOT_EQUAL_IGNORE_CASE_TYPE };

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

	public <T extends Element> Map<T, Double> getValues(Collection<T> elements) {
		Map<T, Double> values = new LinkedHashMap<>();

		try {
			doubleValue = Double.parseDouble(value);
		} catch (Exception e) {
			doubleValue = null;
		}

		if (value.equalsIgnoreCase("true") || value.equals("1")) {
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

	private boolean isEqual(Object nodeValue) {
		if (nodeValue instanceof Boolean && booleanValue != null) {
			return ((Boolean) nodeValue).booleanValue() == booleanValue;
		} else if (nodeValue instanceof Number && doubleValue != null) {
			return ((Number) nodeValue).doubleValue() == doubleValue;
		} else if (nodeValue instanceof String && value != null) {
			return ((String) nodeValue).equalsIgnoreCase(value);
		} else if (nodeValue == null) {
			return value == null || value.isEmpty();
		} else {
			return false;
		}
	}

	private boolean isGreater(Object nodeValue) {
		if (nodeValue instanceof Number && doubleValue != null) {
			return ((Number) nodeValue).doubleValue() > doubleValue;
		} else {
			return false;
		}
	}

	private boolean isLess(Object nodeValue) {
		if (nodeValue instanceof Number && doubleValue != null) {
			return ((Number) nodeValue).doubleValue() < doubleValue;
		} else {
			return false;
		}
	}

	private boolean isEqualRegex(Object nodeValue, boolean ignoreCase) {
		if (nodeValue instanceof String && value != null) {
			int flags = ignoreCase ? Pattern.CASE_INSENSITIVE : 0;
			Matcher matcher = Pattern.compile(value, flags).matcher(
					(String) nodeValue);

			return matcher.matches();
		} else if (nodeValue == null) {
			return value == null || value.isEmpty();
		} else {
			return false;
		}
	}

}
