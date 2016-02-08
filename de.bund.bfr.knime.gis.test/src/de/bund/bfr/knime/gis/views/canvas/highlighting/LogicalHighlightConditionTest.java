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
	public void testGetProperty() {
		// method automatically generated
	}

	@Test
	public void testSetProperty() {
		// method automatically generated
	}

	@Test
	public void testGetType() {
		// method automatically generated
	}

	@Test
	public void testSetType() {
		// method automatically generated
	}

	@Test
	public void testGetValue() {
		// method automatically generated
	}

	@Test
	public void testSetValue() {
		// method automatically generated
	}

	@Test
	public void testGetValues() {
		final String PROPERTY = "property";
		final String VALUE = "value1";
		final String OTHER_VALUE = "value2";
		LogicalHighlightCondition equalCondition = new LogicalHighlightCondition(PROPERTY,
				LogicalHighlightCondition.Type.EQUAL, VALUE);
		Element valueElement = new GraphNode(null, ImmutableMap.of(PROPERTY, VALUE));
		Element otherValueElement = new GraphNode(null, ImmutableMap.of(PROPERTY, OTHER_VALUE));
		Map<Element, Double> values = equalCondition.getValues(Arrays.asList(valueElement, otherValueElement));

		assertEquals(1.0, values.get(valueElement), 0.0);
		assertEquals(0.0, values.get(otherValueElement), 0.0);

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
	}

	@Test
	public void testHashCode() {
		// method automatically generated
	}

	@Test
	public void testEqualsObject() {
		// method automatically generated
	}
}
