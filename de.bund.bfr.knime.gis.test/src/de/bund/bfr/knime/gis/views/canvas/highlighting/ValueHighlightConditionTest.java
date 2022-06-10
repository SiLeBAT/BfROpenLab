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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

public class ValueHighlightConditionTest {

	private static final String PROPERTY = "property";
	private static final ValueHighlightCondition.Type TYPE = ValueHighlightCondition.Type.LOG_VALUE;
	private static final boolean ZERO_AS_MINIMUM = true;
	private static final String NAME = "name";
	private static final boolean SHOW_IN_LEGEND = false;
	private static final Color COLOR = Color.RED;
	private static final boolean INVISIBLE = true;
	private static final boolean USE_THICKNESS = true;
	private static final String LABEL_PROPERTY = "labelProperty";
	private static final NamedShape SHAPE = NamedShape.SQUARE;

	private ValueHighlightCondition valueCondition;
	private ValueHighlightCondition zeroAsMinLogValueCondition;
	private Element element5;
	private Element element10;

	@Before
	public void setUp() throws Exception {
		valueCondition = new ValueHighlightCondition(PROPERTY, ValueHighlightCondition.Type.VALUE, false, NAME,
				SHOW_IN_LEGEND, COLOR, INVISIBLE, USE_THICKNESS, LABEL_PROPERTY, SHAPE);
		zeroAsMinLogValueCondition = new ValueHighlightCondition(PROPERTY, ValueHighlightCondition.Type.LOG_VALUE, true,
				NAME, SHOW_IN_LEGEND, COLOR, INVISIBLE, USE_THICKNESS, LABEL_PROPERTY, SHAPE);
		element5 = new GraphNode(null, ImmutableMap.of(PROPERTY, 5.0));
		element10 = new GraphNode(null, ImmutableMap.of(PROPERTY, 10.0));
	}

	@Test
	public void testValueHighlightCondition() {
		ValueHighlightCondition c = new ValueHighlightCondition();

		assertNull(c.getProperty());
		assertNull(c.getType());
		assertFalse(c.isZeroAsMinimum());
		assertNull(c.getName());
		assertTrue(c.isShowInLegend());
		assertNull(c.getColor());
		assertFalse(c.isInvisible());
		assertFalse(c.isUseThickness());
		assertNull(c.getLabelProperty());
		assertNull(c.getShape());
	}

	@Test
	public void testValueHighlightConditionStringStringBooleanStringBooleanColorBooleanBooleanString() {
		ValueHighlightCondition c = new ValueHighlightCondition(PROPERTY, TYPE, ZERO_AS_MINIMUM, NAME, SHOW_IN_LEGEND,
				COLOR, INVISIBLE, USE_THICKNESS, LABEL_PROPERTY, SHAPE);

		assertEquals(PROPERTY, c.getProperty());
		assertEquals(TYPE, c.getType());
		assertEquals(ZERO_AS_MINIMUM, c.isZeroAsMinimum());
		assertEquals(NAME, c.getName());
		assertEquals(SHOW_IN_LEGEND, c.isShowInLegend());
		assertEquals(COLOR, c.getColor());
		assertEquals(INVISIBLE, c.isInvisible());
		assertEquals(USE_THICKNESS, c.isUseThickness());
		assertEquals(LABEL_PROPERTY, c.getLabelProperty());
		assertEquals(SHAPE, c.getShape());
	}

	@Test
	public void testGetValues() {
		Map<Element, Double> values1 = valueCondition.getValues(Arrays.asList(element5, element10));

		assertEquals(0.0, values1.get(element5), 0.0);
		assertEquals(1.0, values1.get(element10), 0.0);

		Map<Element, Double> values2 = zeroAsMinLogValueCondition.getValues(Arrays.asList(element5, element10));

		assertEquals(Math.log10(5.5), values2.get(element5), 0.0);
		assertEquals(1.0, values2.get(element10), 0.0);
	}

	@Test
	public void testGetValueRange() {
		assertEquals(new Point2D.Double(5.0, 10.0), valueCondition.getValueRange(Arrays.asList(element5, element10)));
		assertEquals(new Point2D.Double(0.0, 10.0),
				zeroAsMinLogValueCondition.getValueRange(Arrays.asList(element5, element10)));
	}

	@Test
	public void testCopy() {
		ValueHighlightCondition copy = valueCondition.copy();

		assertEquals(valueCondition, copy);

		copy.setShowInLegend(!copy.isShowInLegend());

		assertNotEquals(valueCondition, copy);
	}
}
