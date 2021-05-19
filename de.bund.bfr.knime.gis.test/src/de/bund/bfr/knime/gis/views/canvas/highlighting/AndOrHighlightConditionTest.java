/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import de.bund.bfr.jung.NamedShape;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;

public class AndOrHighlightConditionTest {

	private static final String NAME = "name";
	private static final boolean SHOW_IN_LEGEND = false;
	private static final Color COLOR = Color.RED;
	private static final boolean INVISIBLE = true;
	private static final boolean USE_THICKNESS = true;
	private static final String LABEL_PROPERTY = "labelProperty";
	private static final NamedShape SHAPE = NamedShape.SQUARE;

	private static final String PROPERTY_1 = "property1";
	private static final String PROPERTY_2 = "property2";
	private static final String VALUE_1 = "value1";
	private static final String VALUE_2 = "value2";

	private LogicalHighlightCondition equal11Condition;
	private LogicalHighlightCondition equal12Condition;
	private LogicalHighlightCondition equal21Condition;
	private LogicalHighlightCondition equal22Condition;
	private Element element11;
	private Element element12;
	private Element element21;
	private Element element22;

	@Before
	public void setUp() throws Exception {
		equal11Condition = new LogicalHighlightCondition(PROPERTY_1, LogicalHighlightCondition.Type.EQUAL, VALUE_1);
		equal12Condition = new LogicalHighlightCondition(PROPERTY_1, LogicalHighlightCondition.Type.EQUAL, VALUE_2);
		equal21Condition = new LogicalHighlightCondition(PROPERTY_2, LogicalHighlightCondition.Type.EQUAL, VALUE_1);
		equal22Condition = new LogicalHighlightCondition(PROPERTY_2, LogicalHighlightCondition.Type.EQUAL, VALUE_2);
		element11 = new GraphNode(null, ImmutableMap.of(PROPERTY_1, VALUE_1, PROPERTY_2, VALUE_1));
		element12 = new GraphNode(null, ImmutableMap.of(PROPERTY_1, VALUE_1, PROPERTY_2, VALUE_2));
		element21 = new GraphNode(null, ImmutableMap.of(PROPERTY_1, VALUE_2, PROPERTY_2, VALUE_1));
		element22 = new GraphNode(null, ImmutableMap.of(PROPERTY_1, VALUE_2, PROPERTY_2, VALUE_2));
	}

	@Test
	public void testAndOrHighlightCondition() {
		AndOrHighlightCondition c = new AndOrHighlightCondition();

		assertEquals(Arrays.asList(Arrays.asList(new LogicalHighlightCondition())), c.getConditions());
		assertNull(c.getName());
		assertTrue(c.isShowInLegend());
		assertNull(c.getColor());
		assertFalse(c.isInvisible());
		assertFalse(c.isUseThickness());
		assertNull(c.getLabelProperty());
		assertNull(c.getShape());
	}

	@Test
	public void testAndOrHighlightConditionLogicalHighlightConditionStringBooleanColorBooleanBooleanString() {
		AndOrHighlightCondition c = new AndOrHighlightCondition(equal11Condition, NAME, SHOW_IN_LEGEND, COLOR,
				INVISIBLE, USE_THICKNESS, LABEL_PROPERTY, SHAPE);

		assertEquals(Arrays.asList(Arrays.asList(equal11Condition)), c.getConditions());
		assertEquals(NAME, c.getName());
		assertEquals(SHOW_IN_LEGEND, c.isShowInLegend());
		assertEquals(COLOR, c.getColor());
		assertEquals(INVISIBLE, c.isInvisible());
		assertEquals(USE_THICKNESS, c.isUseThickness());
		assertEquals(LABEL_PROPERTY, c.getLabelProperty());
		assertEquals(SHAPE, c.getShape());
	}

	@Test
	public void testAndOrHighlightConditionListOfListOfLogicalHighlightConditionStringBooleanColorBooleanBooleanString() {
		List<List<LogicalHighlightCondition>> conditions = Arrays.asList(
				Arrays.asList(equal11Condition, equal21Condition), Arrays.asList(equal12Condition, equal22Condition));
		AndOrHighlightCondition c = new AndOrHighlightCondition(conditions, NAME, SHOW_IN_LEGEND, COLOR, INVISIBLE,
				USE_THICKNESS, LABEL_PROPERTY, SHAPE);

		assertEquals(conditions, c.getConditions());
		assertEquals(NAME, c.getName());
		assertEquals(SHOW_IN_LEGEND, c.isShowInLegend());
		assertEquals(COLOR, c.getColor());
		assertEquals(INVISIBLE, c.isInvisible());
		assertEquals(USE_THICKNESS, c.isUseThickness());
		assertEquals(LABEL_PROPERTY, c.getLabelProperty());
		assertEquals(SHAPE, c.getShape());
	}

	@Test
	public void testGetConditionCount() {
		AndOrHighlightCondition c = new AndOrHighlightCondition(
				Arrays.asList(Arrays.asList(equal11Condition, equal21Condition), Arrays.asList(equal12Condition)), NAME,
				SHOW_IN_LEGEND, COLOR, INVISIBLE, USE_THICKNESS, LABEL_PROPERTY, SHAPE);

		assertEquals(3, c.getConditionCount());
	}

	@Test
	public void testGetValues() {
		AndOrHighlightCondition c11or22 = new AndOrHighlightCondition(
				Arrays.asList(Arrays.asList(equal11Condition, equal21Condition),
						Arrays.asList(equal12Condition, equal22Condition)),
				NAME, SHOW_IN_LEGEND, COLOR, INVISIBLE, USE_THICKNESS, LABEL_PROPERTY, SHAPE);
		Map<Element, Double> values = c11or22.getValues(Arrays.asList(element11, element12, element21, element22));

		assertEquals(1.0, values.get(element11), 0.0);
		assertEquals(0.0, values.get(element12), 0.0);
		assertEquals(0.0, values.get(element21), 0.0);
		assertEquals(1.0, values.get(element22), 0.0);
	}

	@Test
	public void testGetValueRange() {
		assertEquals(new Point2D.Double(0.0, 1.0), new AndOrHighlightCondition().getValueRange(null));
	}

	@Test
	public void testCopy() {
		AndOrHighlightCondition c = new AndOrHighlightCondition(equal11Condition, NAME, SHOW_IN_LEGEND, COLOR,
				INVISIBLE, USE_THICKNESS, LABEL_PROPERTY, SHAPE);
		AndOrHighlightCondition copy = c.copy();

		assertEquals(c, copy);

		copy.setShowInLegend(!copy.isShowInLegend());

		assertNotEquals(c, copy);
	}
}
