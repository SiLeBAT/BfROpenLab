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
package de.bund.bfr.knime.nls.functionfitting;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.nfunk.jep.ParseException;

import com.google.common.primitives.Doubles;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.KnimeUtilities;
import de.bund.bfr.knime.nls.Function;
import de.bund.bfr.knime.nls.NlsConstants;
import de.bund.bfr.knime.nls.functionport.FunctionPortObject;
import de.bund.bfr.knime.nls.functionport.FunctionPortObjectSpec;
import de.bund.bfr.math.MathUtilities;
import de.bund.bfr.math.ParameterOptimizer;

/**
 * This is the model implementation of FunctionFitting.
 * 
 * 
 * @author Christian Thoens
 */
public class FunctionFittingNodeModel extends NodeModel {

	private FunctionFittingSettings set;

	/**
	 * Constructor for the node model.
	 */
	protected FunctionFittingNodeModel() {
		super(
				new PortType[] { FunctionPortObject.TYPE,
						BufferedDataTable.TYPE }, new PortType[] {
						BufferedDataTable.TYPE, BufferedDataTable.TYPE });
		set = new FunctionFittingSettings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec)
			throws Exception {
		FunctionPortObject functionObject = (FunctionPortObject) inObjects[0];
		BufferedDataTable table = (BufferedDataTable) inObjects[1];
		Map<String, ParameterOptimizer> values = doEstimation(
				functionObject.getFunction(), table, exec);
		PortObjectSpec[] outSpec = configure(new PortObjectSpec[] {
				functionObject.getSpec(), table.getSpec() });
		DataTableSpec outSpec1 = (DataTableSpec) outSpec[0];
		DataTableSpec outSpec2 = (DataTableSpec) outSpec[1];
		BufferedDataContainer container1 = exec.createDataContainer(outSpec1);
		BufferedDataContainer container2 = exec.createDataContainer(outSpec2);
		int i1 = 0;
		int i2 = 0;

		for (String id : values.keySet()) {
			ParameterOptimizer result = values.get(id);
			DataCell[] cells1 = new DataCell[outSpec1.getNumColumns()];

			for (String param1 : functionObject.getFunction().getParameters()) {
				cells1[outSpec1.findColumnIndex(param1)] = IO.createCell(result
						.getParameterValues().get(param1));

				DataCell[] cells2 = new DataCell[outSpec2.getNumColumns()];

				cells2[outSpec2.findColumnIndex(NlsConstants.ID_COLUMN)] = IO
						.createCell(id);
				cells2[outSpec2.findColumnIndex(NlsConstants.PARAM_COLUMN)] = IO
						.createCell(param1);

				for (String param2 : functionObject.getFunction()
						.getParameters()) {
					cells2[outSpec2.findColumnIndex(param2)] = IO
							.createCell(result.getCovariances().get(param1)
									.get(param2));
				}

				container2.addRowToTable(new DefaultRow(i2 + "", cells2));
				i2++;
			}

			cells1[outSpec1.findColumnIndex(NlsConstants.ID_COLUMN)] = IO
					.createCell(id);
			cells1[outSpec1.findColumnIndex(NlsConstants.SSE_COLUMN)] = IO
					.createCell(result.getSSE());
			cells1[outSpec1.findColumnIndex(NlsConstants.MSE_COLUMN)] = IO
					.createCell(result.getMSE());
			cells1[outSpec1.findColumnIndex(NlsConstants.RMSE_COLUMN)] = IO
					.createCell(result.getRMSE());
			cells1[outSpec1.findColumnIndex(NlsConstants.R2_COLUMN)] = IO
					.createCell(result.getR2());
			cells1[outSpec1.findColumnIndex(NlsConstants.AIC_COLUMN)] = IO
					.createCell(result.getAIC());
			cells1[outSpec1.findColumnIndex(NlsConstants.DOF_COLUMN)] = IO
					.createCell(result.getDOF());

			container1.addRowToTable(new DefaultRow(i1 + "", cells1));
			i1++;
		}

		container1.close();
		container2.close();

		return new PortObject[] { container1.getTable(), container2.getTable() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		Function function = ((FunctionPortObjectSpec) inSpecs[0]).getFunction();
		DataTableSpec spec = (DataTableSpec) inSpecs[1];
		List<String> variables = function.getVariables();
		List<String> parameters = function.getParameters();
		List<String> stringColumns = KnimeUtilities
				.getColumnNames(KnimeUtilities
						.getColumns(spec, StringCell.TYPE));
		List<String> doubleColumns = KnimeUtilities
				.getColumnNames(KnimeUtilities
						.getColumns(spec, DoubleCell.TYPE));

		if (!stringColumns.contains(NlsConstants.ID_COLUMN)) {
			throw new InvalidSettingsException(
					"Input Table must contain String Column named \""
							+ NlsConstants.ID_COLUMN + "\"");
		}

		for (String var : variables) {
			if (!doubleColumns.contains(var)) {
				throw new InvalidSettingsException(
						"Input Table must contain Double Column named \"" + var
								+ "\"");
			}
		}

		List<DataColumnSpec> specs1 = new ArrayList<>();
		List<DataColumnSpec> specs2 = new ArrayList<>();

		specs1.add(new DataColumnSpecCreator(NlsConstants.ID_COLUMN,
				StringCell.TYPE).createSpec());
		specs2.add(new DataColumnSpecCreator(NlsConstants.ID_COLUMN,
				StringCell.TYPE).createSpec());
		specs2.add(new DataColumnSpecCreator(NlsConstants.PARAM_COLUMN,
				StringCell.TYPE).createSpec());

		for (String param : parameters) {
			specs1.add(new DataColumnSpecCreator(param, DoubleCell.TYPE)
					.createSpec());
			specs2.add(new DataColumnSpecCreator(param, DoubleCell.TYPE)
					.createSpec());
		}

		specs1.add(new DataColumnSpecCreator(NlsConstants.SSE_COLUMN,
				DoubleCell.TYPE).createSpec());
		specs1.add(new DataColumnSpecCreator(NlsConstants.MSE_COLUMN,
				DoubleCell.TYPE).createSpec());
		specs1.add(new DataColumnSpecCreator(NlsConstants.RMSE_COLUMN,
				DoubleCell.TYPE).createSpec());
		specs1.add(new DataColumnSpecCreator(NlsConstants.R2_COLUMN,
				DoubleCell.TYPE).createSpec());
		specs1.add(new DataColumnSpecCreator(NlsConstants.AIC_COLUMN,
				DoubleCell.TYPE).createSpec());
		specs1.add(new DataColumnSpecCreator(NlsConstants.DOF_COLUMN,
				IntCell.TYPE).createSpec());

		return new PortObjectSpec[] {
				new DataTableSpec(specs1.toArray(new DataColumnSpec[0])),
				new DataTableSpec(specs2.toArray(new DataColumnSpec[0])) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		set.saveSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		set.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

	private Map<String, ParameterOptimizer> doEstimation(Function function,
			BufferedDataTable table, ExecutionContext exec)
			throws ParseException {
		Map<String, Point2D.Double> parameterGuesses;
		int nParameterSpace;
		int nLevenberg;
		boolean stopWhenSuccessful;

		if (set.isExpertSettings()) {
			parameterGuesses = set.getParameterGuesses();
			nParameterSpace = set.getnParameterSpace();
			nLevenberg = set.getnLevenberg();
			stopWhenSuccessful = set.isStopWhenSuccessful();
		} else {
			parameterGuesses = null;
			nParameterSpace = FunctionFittingSettings.DEFAULT_N_PARAMETER_SPACE;
			nLevenberg = FunctionFittingSettings.DEFAULT_N_LEVENBERG;
			stopWhenSuccessful = FunctionFittingSettings.DEFAULT_STOP_WHEN_SUCCESSFUL;
		}

		String formula = function.getTerm();
		List<String> parameters = new ArrayList<>();
		Map<String, Double> minParameterValues = new LinkedHashMap<>();
		Map<String, Double> maxParameterValues = new LinkedHashMap<>();

		for (String param : function.getParameters()) {
			Double min = null;
			Double max = null;

			if (parameterGuesses != null && parameterGuesses.containsKey(param)) {
				Point2D.Double range = parameterGuesses.get(param);

				if (!Double.isNaN(range.x)) {
					min = range.x;
				}

				if (!Double.isNaN(range.y)) {
					max = range.y;
				}
			}

			parameters.add(param);
			minParameterValues.put(param, min);
			maxParameterValues.put(param, max);
		}

		DataTableSpec spec = table.getSpec();
		Map<String, List<Double>> targetValues = new LinkedHashMap<>();
		Map<String, Map<String, List<Double>>> argumentValues = new LinkedHashMap<>();

		for (DataRow row : table) {
			String id = IO.getString(row.getCell(spec
					.findColumnIndex(NlsConstants.ID_COLUMN)));
			Map<String, Double> values = new LinkedHashMap<>();

			for (String var : function.getVariables()) {
				values.put(var,
						IO.getDouble(row.getCell(spec.findColumnIndex(var))));
			}

			if (id == null
					|| MathUtilities.containsInvalidDouble(values.values())) {
				continue;
			}

			if (!targetValues.containsKey(id)) {
				targetValues.put(id, new ArrayList<Double>());
				argumentValues.put(id,
						new LinkedHashMap<String, List<Double>>());

				for (String indep : function.getIndependentVariables()) {
					argumentValues.get(id).put(indep, new ArrayList<Double>());
				}
			}

			targetValues.get(id).add(
					values.get(function.getDependentVariable()));

			for (String indep : function.getIndependentVariables()) {
				argumentValues.get(id).get(indep).add(values.get(indep));
			}
		}

		Map<String, ParameterOptimizer> results = new LinkedHashMap<>();

		for (String id : targetValues.keySet()) {
			ParameterOptimizer optimizer;
			double[] targetArray = Doubles.toArray(targetValues.get(id));
			Map<String, double[]> argumentArrays = new LinkedHashMap<>();

			for (Map.Entry<String, List<Double>> entry : argumentValues.get(id)
					.entrySet()) {
				argumentArrays.put(entry.getKey(),
						Doubles.toArray(entry.getValue()));
			}

			if (function.getDiffVariable() != null) {
				optimizer = new ParameterOptimizer(formula, parameters,
						minParameterValues, maxParameterValues, targetArray,
						function.getDependentVariable(),
						function.getDiffVariable(), argumentArrays);
			} else {
				optimizer = new ParameterOptimizer(formula, parameters,
						minParameterValues, maxParameterValues,
						minParameterValues, maxParameterValues, targetArray,
						argumentArrays, set.isEnforceLimits());
			}

			optimizer.optimize(nParameterSpace, nLevenberg, stopWhenSuccessful);
			results.put(id, optimizer);
		}

		return results;
	}
}
