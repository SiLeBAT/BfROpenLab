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

import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;

import de.bund.bfr.knime.IO;
import de.bund.bfr.math.ParameterOptimizer;

public class NlsUtils {

	public static final String ID_COLUMN = "ID";
	public static final String SSE_COLUMN = "SSE";
	public static final String MSE_COLUMN = "MSE";
	public static final String RMSE_COLUMN = "RMSE";
	public static final String R2_COLUMN = "R2";
	public static final String AIC_COLUMN = "AIC";
	public static final String DOF_COLUMN = "DOF";
	public static final String PARAM_COLUMN = "Param";

	private NlsUtils() {
	}

	public static void createFittingResultTable(
			BufferedDataContainer paramContainer,
			BufferedDataContainer covContainer,
			Map<String, ParameterOptimizer> results, Function function) {
		DataTableSpec paramSpec = paramContainer.getTableSpec();
		DataTableSpec covSpec = covContainer.getTableSpec();
		int iParam = 0;
		int iCov = 0;

		for (String id : results.keySet()) {
			ParameterOptimizer result = results.get(id);
			DataCell[] paramCells = new DataCell[paramSpec.getNumColumns()];

			for (String param1 : function.getParameters()) {
				paramCells[paramSpec.findColumnIndex(param1)] = IO
						.createCell(result.getParameterValues().get(param1));

				DataCell[] covCells = new DataCell[covSpec.getNumColumns()];

				covCells[covSpec.findColumnIndex(NlsUtils.ID_COLUMN)] = IO
						.createCell(id);
				covCells[covSpec.findColumnIndex(NlsUtils.PARAM_COLUMN)] = IO
						.createCell(param1);

				for (String param2 : function.getParameters()) {
					covCells[covSpec.findColumnIndex(param2)] = IO
							.createCell(result.getCovariances().get(param1)
									.get(param2));
				}

				covContainer.addRowToTable(new DefaultRow(String.valueOf(iCov),
						covCells));
				iCov++;
			}

			paramCells[paramSpec.findColumnIndex(NlsUtils.ID_COLUMN)] = IO
					.createCell(id);
			paramCells[paramSpec.findColumnIndex(NlsUtils.SSE_COLUMN)] = IO
					.createCell(result.getSSE());
			paramCells[paramSpec.findColumnIndex(NlsUtils.MSE_COLUMN)] = IO
					.createCell(result.getMSE());
			paramCells[paramSpec.findColumnIndex(NlsUtils.RMSE_COLUMN)] = IO
					.createCell(result.getRMSE());
			paramCells[paramSpec.findColumnIndex(NlsUtils.R2_COLUMN)] = IO
					.createCell(result.getR2());
			paramCells[paramSpec.findColumnIndex(NlsUtils.AIC_COLUMN)] = IO
					.createCell(result.getAIC());
			paramCells[paramSpec.findColumnIndex(NlsUtils.DOF_COLUMN)] = IO
					.createCell(result.getDOF());

			paramContainer.addRowToTable(new DefaultRow(String.valueOf(iParam)
					+ "", paramCells));
			iParam++;
		}

		paramContainer.close();
		covContainer.close();
	}
}
