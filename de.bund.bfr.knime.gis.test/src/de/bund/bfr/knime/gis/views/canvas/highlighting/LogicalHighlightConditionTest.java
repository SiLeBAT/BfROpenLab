/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;

public class LogicalHighlightConditionTest {

	@Test
	public void testLogicalHighlightCondition() {
		LogicalHighlightCondition c = new LogicalHighlightCondition();

		assertNull(c.getProperty());
		assertNull(c.getType());
		assertNull(c.getValue());
	}

	@Test
	public void testLogicalHighlightConditionStringStringString() {
		final String PROPERTY = "property";
		final LogicalHighlightCondition.Type TYPE = LogicalHighlightCondition.Type.EQUAL;
		final String VALUE = "value";
		LogicalHighlightCondition c = new LogicalHighlightCondition(PROPERTY, TYPE, VALUE);

		assertEquals(PROPERTY, c.getProperty());
		assertEquals(TYPE, c.getType());
		assertEquals(VALUE, c.getValue());
	}

	@Test
	public void testGetValues() {
		final String PROPERTY = "property";
		final String VALUE_1 = "value1";
		final String VALUE_2 = "value2";

		LogicalHighlightCondition value1Condition = new LogicalHighlightCondition(PROPERTY,
				LogicalHighlightCondition.Type.EQUAL, VALUE_1);
		Element value1Element = new GraphNode(null, ImmutableMap.of(PROPERTY, VALUE_1));
		Element value2Element = new GraphNode(null, ImmutableMap.of(PROPERTY, VALUE_2));
		Map<Element, Double> value1Values = value1Condition.getValues(Arrays.asList(value1Element, value2Element));

		assertEquals(1.0, value1Values.get(value1Element), 0.0);
		assertEquals(0.0, value1Values.get(value2Element), 0.0);

		LogicalHighlightCondition greaterCondition = new LogicalHighlightCondition(PROPERTY,
				LogicalHighlightCondition.Type.GREATER, "1");
		LogicalHighlightCondition lessCondition = new LogicalHighlightCondition(PROPERTY,
				LogicalHighlightCondition.Type.LESS, "1");
		Element greaterElement = new GraphNode(null, ImmutableMap.of(PROPERTY, 2));
		Element equalElement = new GraphNode(null, ImmutableMap.of(PROPERTY, 1));
		Element lessElement = new GraphNode(null, ImmutableMap.of(PROPERTY, 0));
		Map<Element, Double> greaterValues = greaterCondition
				.getValues(Arrays.asList(greaterElement, equalElement, lessElement));
		Map<Element, Double> lessValues = lessCondition
				.getValues(Arrays.asList(greaterElement, equalElement, lessElement));

		assertEquals(1.0, greaterValues.get(greaterElement), 0.0);
		assertEquals(0.0, greaterValues.get(equalElement), 0.0);
		assertEquals(0.0, greaterValues.get(lessElement), 0.0);
		assertEquals(0.0, lessValues.get(greaterElement), 0.0);
		assertEquals(0.0, lessValues.get(equalElement), 0.0);
		assertEquals(1.0, lessValues.get(lessElement), 0.0);

		LogicalHighlightCondition regex1Condition = new LogicalHighlightCondition(PROPERTY,
				LogicalHighlightCondition.Type.REGEX_EQUAL, ".*1");
		Map<Element, Double> regex1Values = regex1Condition.getValues(Arrays.asList(value1Element, value2Element));

		assertEquals(1.0, regex1Values.get(value1Element), 0.0);
		assertEquals(0.0, regex1Values.get(value2Element), 0.0);

		LogicalHighlightCondition regexCaseCondition = new LogicalHighlightCondition(PROPERTY,
				LogicalHighlightCondition.Type.REGEX_EQUAL, "Value.*");
		LogicalHighlightCondition regexNoCaseCondition = new LogicalHighlightCondition(PROPERTY,
				LogicalHighlightCondition.Type.REGEX_EQUAL_IGNORE_CASE, "Value.*");
		Map<Element, Double> regexCaseValues = regexCaseCondition
				.getValues(Arrays.asList(value1Element, value2Element));
		Map<Element, Double> regexNoCaseValues = regexNoCaseCondition
				.getValues(Arrays.asList(value1Element, value2Element));

		assertEquals(0.0, regexCaseValues.get(value1Element), 0.0);
		assertEquals(0.0, regexCaseValues.get(value2Element), 0.0);
		assertEquals(1.0, regexNoCaseValues.get(value1Element), 0.0);
		assertEquals(1.0, regexNoCaseValues.get(value2Element), 0.0);
	}
}
