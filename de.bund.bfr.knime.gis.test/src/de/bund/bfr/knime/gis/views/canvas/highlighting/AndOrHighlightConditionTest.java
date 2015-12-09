/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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

import static org.junit.Assert.*;

import java.awt.geom.Point2D;

import org.junit.Test;

public class AndOrHighlightConditionTest {

	@Test
	public void testAndOrHighlightCondition() {
		AndOrHighlightCondition c = new AndOrHighlightCondition();

		assertEquals(0, c.getConditionCount());
		assertNull(c.getName());
		assertTrue(c.isShowInLegend());
		assertNull(c.getColor());
		assertFalse(c.isInvisible());
		assertFalse(c.isUseThickness());
		assertNull(c.getLabelProperty());
	}

	@Test
	public void testAndOrHighlightConditionLogicalHighlightConditionStringBooleanColorBooleanBooleanString() {
		fail("Not yet implemented");
	}

	@Test
	public void testAndOrHighlightConditionListOfListOfLogicalHighlightConditionStringBooleanColorBooleanBooleanString() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetConditions() {
		// method automatically generated
	}

	@Test
	public void testSetConditions() {
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
	public void testGetConditionCount() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetValues() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetValueRange() {
		assertEquals(new Point2D.Double(0.0, 1.0), new AndOrHighlightCondition().getValueRange(null));
	}

	@Test
	public void testCopy() {
		fail("Not yet implemented");
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
