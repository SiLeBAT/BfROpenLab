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
package de.bund.bfr.knime.pmmlite.core;

import java.util.List;

import org.sbml.jsbml.Unit;

import com.google.common.collect.Lists;

public class SbmlUtils {

	public static final String COMPARTMENT_MISSING = "CompartmentMissing";
	public static final String SPECIES_MISSING = "SpeciesMissing";
	public static final String TIME = "time";
	public static final String INITIAL = "initial";

	private SbmlUtils() {
	}

	@SuppressWarnings("deprecation")
	public static Unit.Kind[] getUnitKinds() {
		List<Unit.Kind> kinds = Lists.newArrayList(Unit.Kind.values());

		kinds.remove(Unit.Kind.LITER);
		kinds.remove(Unit.Kind.METER);
		kinds.remove(Unit.Kind.CELSIUS);
		kinds.remove(Unit.Kind.INVALID);

		return kinds.toArray(new Unit.Kind[0]);
	}
}
