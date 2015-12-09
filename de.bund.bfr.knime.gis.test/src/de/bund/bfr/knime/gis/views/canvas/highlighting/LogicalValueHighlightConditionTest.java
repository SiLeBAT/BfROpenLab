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

import org.junit.Test;

public class LogicalValueHighlightConditionTest {

	@Test
	public void testLogicalValueHighlightCondition() {
		LogicalValueHighlightCondition c = new LogicalValueHighlightCondition();

		assertEquals(new AndOrHighlightCondition(), c.getLogicalCondition());
		assertEquals(new ValueHighlightCondition(), c.getValueCondition());
	}

	@Test
	public void testLogicalValueHighlightConditionValueHighlightConditionAndOrHighlightCondition() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetValueCondition() {
		// method automatically generated
	}

	@Test
	public void testSetValueCondition() {
		// method automatically generated
	}

	@Test
	public void testGetLogicalCondition() {
		// method automatically generated
	}

	@Test
	public void testSetLogicalCondition() {
		// method automatically generated
	}

	@Test
	public void testGetName() {
		// method automatically generated
	}

	@Test
	public void testIsShowInLegend() {
		// method automatically generated
	}

	@Test
	public void testGetColor() {
		// method automatically generated
	}

	@Test
	public void testIsInvisible() {
		// method automatically generated
	}

	@Test
	public void testIsUseThickness() {
		// method automatically generated
	}

	@Test
	public void testGetLabelProperty() {
		// method automatically generated
	}

	@Test
	public void testGetValues() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetValueRange() {
		fail("Not yet implemented");
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
