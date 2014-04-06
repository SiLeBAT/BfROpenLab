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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;

import de.bund.bfr.math.MathUtilities;

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

	public static String getName(String attr, String transform) {
		if (transform == null || transform.equals("")) {
			return attr;
		} else {
			return transform + "(" + attr + ")";
		}
	}

	public static List<String> getStringColumns(DataTableSpec spec) {
		List<String> columns = new ArrayList<String>();

		for (int i = 0; i < spec.getNumColumns(); i++) {
			DataColumnSpec cSpec = spec.getColumnSpec(i);

			if (cSpec.getType() == StringCell.TYPE) {
				columns.add(cSpec.getName());
			}
		}

		return columns;
	}

	public static List<String> getDoubleColumns(DataTableSpec spec) {
		List<String> columns = new ArrayList<String>();

		for (int i = 0; i < spec.getNumColumns(); i++) {
			DataColumnSpec cSpec = spec.getColumnSpec(i);

			if (cSpec.getType() == DoubleCell.TYPE) {
				columns.add(cSpec.getName());
			}
		}

		return columns;
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
