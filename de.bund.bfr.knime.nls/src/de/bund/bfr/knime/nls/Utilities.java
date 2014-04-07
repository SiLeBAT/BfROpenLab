/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
 ******************************************************************************/
package de.bund.bfr.knime.nls;

import java.util.Collections;
import java.util.List;

import de.bund.bfr.math.MathUtilities;
import de.bund.bfr.math.Transform;

public class Utilities {

	public static final String ID_COLUMN = "ID";
	public static final String SSE_COLUMN = "SSE";
	public static final String MSE_COLUMN = "MSE";
	public static final String RMSE_COLUMN = "RMSE";
	public static final String R2_COLUMN = "R2";
	public static final String AIC_COLUMN = "AIC";
	public static final String DOF_COLUMN = "DOF";
	public static final String PARAM_COLUMN = "Param";

	private Utilities() {
	}

	public static String getName(String attr, Transform transform) {
		if (transform == null || transform == Transform.NO_TRANSFORM) {
			return attr;
		} else {
			return transform + "(" + attr + ")";
		}
	}

	public static Function createFunction(String term,
			String dependentVariable, List<String> independentVariables) {
		List<String> parameters = MathUtilities.getSymbols(term);

		parameters.removeAll(independentVariables);
		Collections.sort(parameters);

		return new Function(term, dependentVariable, independentVariables,
				parameters);
	}

}
