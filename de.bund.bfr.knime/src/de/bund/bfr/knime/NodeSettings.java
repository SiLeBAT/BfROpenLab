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
package de.bund.bfr.knime;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class NodeSettings {

	public abstract void loadSettings(NodeSettingsRO settings);

	public abstract void saveSettings(NodeSettingsWO settings);

	protected static boolean nullToFalse(Boolean b) {
		return b != null ? b : false;
	}

	@SuppressFBWarnings(value = "NP")
	protected static Boolean falseToNull(boolean b) {
		return b ? b : null;
	}

	protected static double nullToNan(Double d) {
		return d != null ? d : Double.NaN;
	}

	protected static Double nanToNull(double d) {
		return !Double.isNaN(d) ? d : null;
	}

	protected static int nullToMinusOne(Integer i) {
		return i != null ? i : -1;
	}

	protected static Integer minusOneToNull(int i) {
		return i != -1 ? i : null;
	}
}
