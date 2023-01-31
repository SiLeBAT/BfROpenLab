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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import de.bund.bfr.jung.NamedShape;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;

public class LogicalValueHighlightConditionTest {

	private static final String NAME = "name";
	private static final boolean SHOW_IN_LEGEND = false;
	private static final Color COLOR = Color.RED;
	private static final boolean INVISIBLE = true;
	private static final boolean USE_THICKNESS = true;
	private static final String LABEL_PROPERTY = "labelProperty";
	private static final NamedShape SHAPE = NamedShape.SQUARE;

	private static final String VALUE_PROPERTY = "valueProperty";
	private static final String PROPERTY = "property";
	private static final String VALUE_1 = "value1";
	private static final String VALUE_2 = "value2";

	private ValueHighlightCondition valueCondition;
	private AndOrHighlightCondition equal1Condition;
	private Element element1withValue1;
	private Element element1withValue2;
	private Element element2withValue3;
	private Element element2withValue4;

	@Before
	public void setUp() throws Exception {
		equal1Condition = new AndOrHighlightCondition(
				new LogicalHighlightCondition(PROPERTY, LogicalHighlightCondition.Type.EQUAL, VALUE_1), NAME,
				SHOW_IN_LEGEND, COLOR, INVISIBLE, USE_THICKNESS, LABEL_PROPERTY, SHAPE);
		valueCondition = new ValueHighlightCondition(VALUE_PROPERTY, ValueHighlightCondition.Type.VALUE, true, NAME,
				SHOW_IN_LEGEND, COLOR, INVISIBLE, USE_THICKNESS, LABEL_PROPERTY, SHAPE);
		element1withValue1 = new GraphNode(null, ImmutableMap.of(PROPERTY, VALUE_1, VALUE_PROPERTY, 1));
		element1withValue2 = new GraphNode(null, ImmutableMap.of(PROPERTY, VALUE_1, VALUE_PROPERTY, 2));
		element2withValue3 = new GraphNode(null, ImmutableMap.of(PROPERTY, VALUE_2, VALUE_PROPERTY, 3));
		element2withValue4 = new GraphNode(null, ImmutableMap.of(PROPERTY, VALUE_2, VALUE_PROPERTY, 4));
	}

	@Test
	public void testLogicalValueHighlightCondition() {
		LogicalValueHighlightCondition c = new LogicalValueHighlightCondition();

		assertEquals(new ValueHighlightCondition(), c.getValueCondition());
		assertEquals(new AndOrHighlightCondition(), c.getLogicalCondition());
	}

	@Test
	public void testLogicalValueHighlightConditionValueHighlightConditionAndOrHighlightCondition() {
		LogicalValueHighlightCondition c = new LogicalValueHighlightCondition(valueCondition, equal1Condition);

		assertEquals(valueCondition, c.getValueCondition());
		assertEquals(equal1Condition, c.getLogicalCondition());
	}

	@Test
	public void testGetValues() {
		LogicalValueHighlightCondition c = new LogicalValueHighlightCondition(valueCondition, equal1Condition);
		Map<Element, Double> values = c.getValues(
				Arrays.asList(element1withValue1, element1withValue2, element2withValue3, element2withValue4));

		assertEquals(0.5, values.get(element1withValue1), 0.0);
		assertEquals(1.0, values.get(element1withValue2), 0.0);
		assertEquals(0.0, values.get(element2withValue3), 0.0);
		assertEquals(0.0, values.get(element2withValue4), 0.0);
	}

	@Test
	public void testGetValueRange() {
		LogicalValueHighlightCondition c = new LogicalValueHighlightCondition(valueCondition, equal1Condition);
		Point2D range = c.getValueRange(
				Arrays.asList(element1withValue1, element1withValue2, element2withValue3, element2withValue4));

		assertEquals(new Point2D.Double(0.0, 2.0), range);
	}

	@Test
	public void testCopy() {
		LogicalValueHighlightCondition c = new LogicalValueHighlightCondition(valueCondition, equal1Condition);
		LogicalValueHighlightCondition copy = c.copy();

		assertEquals(c, copy);

		copy.getValueCondition().setShowInLegend(!copy.isShowInLegend());

		assertNotEquals(c, copy);
	}
}
