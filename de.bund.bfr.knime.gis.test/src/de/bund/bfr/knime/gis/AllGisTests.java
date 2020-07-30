/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.gis;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightConditionTest;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalHighlightConditionTest;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalValueHighlightConditionTest;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightConditionTest;

@RunWith(Suite.class)
@SuiteClasses({ LogicalHighlightConditionTest.class, AndOrHighlightConditionTest.class,
		ValueHighlightConditionTest.class, LogicalValueHighlightConditionTest.class })
public class AllGisTests {
}
