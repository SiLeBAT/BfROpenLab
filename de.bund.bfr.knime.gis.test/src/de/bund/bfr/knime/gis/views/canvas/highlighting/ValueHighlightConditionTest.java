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

import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;

public class ValueHighlightConditionTest {

	private static final String PROPERTY = "property";
	private static final String TYPE = ValueHighlightCondition.LOG_VALUE_TYPE;
	private static final boolean ZERO_AS_MINIMUM = true;
	private static final String NAME = "name";
	private static final boolean SHOW_IN_LEGEND = false;
	private static final Color COLOR = Color.RED;
	private static final boolean INVISIBLE = true;
	private static final boolean USE_THICKNESS = true;
	private static final String LABEL_PROPERTY = "labelProperty";

	private ValueHighlightCondition valueCondition;
	private ValueHighlightCondition zeroAsMinLogValueCondition;
	private Element element5;
	private Element element10;

	@Before
	public void setUp() throws Exception {
		valueCondition = new ValueHighlightCondition(PROPERTY, ValueHighlightCondition.VALUE_TYPE, false, NAME,
				SHOW_IN_LEGEND, COLOR, INVISIBLE, USE_THICKNESS, LABEL_PROPERTY);
		zeroAsMinLogValueCondition = new ValueHighlightCondition(PROPERTY, ValueHighlightCondition.LOG_VALUE_TYPE, true,
				NAME, SHOW_IN_LEGEND, COLOR, INVISIBLE, USE_THICKNESS, LABEL_PROPERTY);
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
	}

	@Test
	public void testValueHighlightConditionStringStringBooleanStringBooleanColorBooleanBooleanString() {
		ValueHighlightCondition c = new ValueHighlightCondition(PROPERTY, TYPE, ZERO_AS_MINIMUM, NAME, SHOW_IN_LEGEND,
				COLOR, INVISIBLE, USE_THICKNESS, LABEL_PROPERTY);

		assertEquals(PROPERTY, c.getProperty());
		assertEquals(TYPE, c.getType());
		assertEquals(ZERO_AS_MINIMUM, c.isZeroAsMinimum());
		assertEquals(NAME, c.getName());
		assertEquals(SHOW_IN_LEGEND, c.isShowInLegend());
		assertEquals(COLOR, c.getColor());
		assertEquals(INVISIBLE, c.isInvisible());
		assertEquals(USE_THICKNESS, c.isUseThickness());
		assertEquals(LABEL_PROPERTY, c.getLabelProperty());
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
	public void testIsZeroAsMinimum() {
		// method automatically generated
	}

	@Test
	public void testSetZeroAsMinimum() {
		// method automatically generated
	}

	@Test
	public void testGetName() {
		// method automatically generated
	}

	@Test
	public void testSetName() {
		// method automatically generated
	}

	@Test
	public void testIsShowInLegend() {
		// method automatically generated
	}

	@Test
	public void testSetShowInLegend() {
		// method automatically generated
	}

	@Test
	public void testGetColor() {
		// method automatically generated
	}

	@Test
	public void testSetColor() {
		// method automatically generated
	}

	@Test
	public void testIsInvisible() {
		// method automatically generated
	}

	@Test
	public void testSetInvisible() {
		// method automatically generated
	}

	@Test
	public void testIsUseThickness() {
		// method automatically generated
	}

	@Test
	public void testSetUseThickness() {
		// method automatically generated
	}

	@Test
	public void testGetLabelProperty() {
		// method automatically generated
	}

	@Test
	public void testSetLabelProperty() {
		// method automatically generated
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
		ValueHighlightCondition c = new ValueHighlightCondition(PROPERTY, TYPE, ZERO_AS_MINIMUM, NAME, SHOW_IN_LEGEND,
				COLOR, INVISIBLE, USE_THICKNESS, LABEL_PROPERTY);
		ValueHighlightCondition copy = c.copy();

		assertEquals(c, copy);

		copy.setShowInLegend(!copy.isShowInLegend());

		assertNotEquals(c, copy);
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
