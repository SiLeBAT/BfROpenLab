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
package de.bund.bfr.knime.nls.fitting;

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
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
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
import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.nls.Function;
import de.bund.bfr.knime.nls.NlsUtils;
import de.bund.bfr.knime.nls.functionport.FunctionPortObject;
import de.bund.bfr.knime.nls.functionport.FunctionPortObjectSpec;
import de.bund.bfr.math.Integrator;
import de.bund.bfr.math.MathUtils;
import de.bund.bfr.math.ParameterOptimizer;

/**
 * This is the model implementation of DiffFunctionFitting.
 * 
 * 
 * @author Christian Thoens
 */
public class FittingNodeModel extends NodeModel implements
		ParameterOptimizer.ProgressListener {

	private static final PortType[] INPUT_TYPE = new PortType[] {
			FunctionPortObject.TYPE, BufferedDataTable.TYPE };
	private static final PortType[] DIFF_INPUT_TYPE = new PortType[] {
			FunctionPortObject.TYPE, BufferedDataTable.TYPE,
			BufferedDataTable.TYPE };

	private boolean isDiff;
	private FittingSettings set;
	private ExecutionContext currentExec;

	/**
	 * Constructor for the node model.
	 */
	protected FittingNodeModel(boolean isDiff, FittingSettings set) {
		super(isDiff ? DIFF_INPUT_TYPE : INPUT_TYPE, new PortType[] {
				BufferedDataTable.TYPE, BufferedDataTable.TYPE });
		this.isDiff = isDiff;
		this.set = set;
		currentExec = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec)
			throws Exception {
		currentExec = exec;

		Function function = ((FunctionPortObject) inObjects[0]).getFunction();
		Map<String, ParameterOptimizer> results;
		PortObjectSpec[] outSpec;

		if (isDiff) {
			results = doEstimation(function, (BufferedDataTable) inObjects[1],
					(BufferedDataTable) inObjects[2]);
			outSpec = configure(new PortObjectSpec[] { inObjects[0].getSpec(),
					inObjects[1].getSpec(), inObjects[2].getSpec() });
		} else {
			results = doEstimation(function, (BufferedDataTable) inObjects[1]);
			outSpec = configure(new PortObjectSpec[] { inObjects[0].getSpec(),
					inObjects[1].getSpec() });
		}

		DataTableSpec paramSpec = (DataTableSpec) outSpec[0];
		DataTableSpec covSpec = (DataTableSpec) outSpec[1];
		BufferedDataContainer paramContainer = exec
				.createDataContainer(paramSpec);
		BufferedDataContainer covContainer = exec.createDataContainer(covSpec);
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

		return new PortObject[] { paramContainer.getTable(),
				covContainer.getTable() };
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
	@SuppressWarnings("unchecked")
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		Function function = ((FunctionPortObjectSpec) inSpecs[0]).getFunction();
		DataTableSpec dataSpec = (DataTableSpec) inSpecs[1];
		List<String> dataStringColumns = KnimeUtils.getColumnNames(KnimeUtils
				.getColumns(dataSpec, StringValue.class));
		List<String> dataDoubleColumns = KnimeUtils.getColumnNames(KnimeUtils
				.getColumns(dataSpec, DoubleValue.class));

		if (!dataStringColumns.contains(NlsUtils.ID_COLUMN)) {
			throw new InvalidSettingsException(
					"Data Table must contain String Column named \""
							+ NlsUtils.ID_COLUMN + "\"");
		}

		if (isDiff) {
			DataTableSpec conditionSpec = (DataTableSpec) inSpecs[2];
			List<String> conditionStringColumns = KnimeUtils
					.getColumnNames(KnimeUtils.getColumns(conditionSpec,
							StringValue.class));
			List<String> conditionDoubleColumns = KnimeUtils
					.getColumnNames(KnimeUtils.getColumns(conditionSpec,
							DoubleValue.class));

			if (!dataDoubleColumns.contains(function.getTimeVariable())) {
				throw new InvalidSettingsException(
						"Data Table must contain Double Column named \""
								+ function.getTimeVariable() + "\"");
			}

			if (!dataDoubleColumns.contains(function.getDependentVariable())) {
				throw new InvalidSettingsException(
						"Data Table must contain Double Column named \""
								+ function.getDependentVariable() + "\"");
			}

			if (!conditionStringColumns.contains(NlsUtils.ID_COLUMN)) {
				throw new InvalidSettingsException(
						"Condition Table must contain String Column named \""
								+ NlsUtils.ID_COLUMN + "\"");
			}

			for (String var : function.getVariables()) {
				if (!var.equals(function.getDependentVariable())
						&& !conditionDoubleColumns.contains(var)) {
					throw new InvalidSettingsException(
							"Condition Table must contain Double Column named \""
									+ var + "\"");
				}
			}
		} else {
			for (String var : function.getVariables()) {
				if (!dataDoubleColumns.contains(var)) {
					throw new InvalidSettingsException(
							"Data Table must contain Double Column named \""
									+ var + "\"");
				}
			}
		}

		List<DataColumnSpec> specs1 = new ArrayList<>();
		List<DataColumnSpec> specs2 = new ArrayList<>();

		specs1.add(new DataColumnSpecCreator(NlsUtils.ID_COLUMN,
				StringCell.TYPE).createSpec());
		specs2.add(new DataColumnSpecCreator(NlsUtils.ID_COLUMN,
				StringCell.TYPE).createSpec());
		specs2.add(new DataColumnSpecCreator(NlsUtils.PARAM_COLUMN,
				StringCell.TYPE).createSpec());

		for (String param : function.getParameters()) {
			specs1.add(new DataColumnSpecCreator(param, DoubleCell.TYPE)
					.createSpec());
			specs2.add(new DataColumnSpecCreator(param, DoubleCell.TYPE)
					.createSpec());
		}

		specs1.add(new DataColumnSpecCreator(NlsUtils.SSE_COLUMN,
				DoubleCell.TYPE).createSpec());
		specs1.add(new DataColumnSpecCreator(NlsUtils.MSE_COLUMN,
				DoubleCell.TYPE).createSpec());
		specs1.add(new DataColumnSpecCreator(NlsUtils.RMSE_COLUMN,
				DoubleCell.TYPE).createSpec());
		specs1.add(new DataColumnSpecCreator(NlsUtils.R2_COLUMN,
				DoubleCell.TYPE).createSpec());
		specs1.add(new DataColumnSpecCreator(NlsUtils.AIC_COLUMN,
				DoubleCell.TYPE).createSpec());
		specs1.add(new DataColumnSpecCreator(NlsUtils.DOF_COLUMN, IntCell.TYPE)
				.createSpec());

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
			BufferedDataTable table) throws ParseException {
		if (function.getTimeVariable() != null) {
			return new LinkedHashMap<>();
		}

		DataTableSpec spec = table.getSpec();
		Map<String, List<Double>> targetValues = new LinkedHashMap<>();
		Map<String, Map<String, List<Double>>> argumentValues = new LinkedHashMap<>();

		for (DataRow row : table) {
			String id = IO.getString(row.getCell(spec
					.findColumnIndex(NlsUtils.ID_COLUMN)));
			Map<String, Double> values = new LinkedHashMap<>();

			for (String var : function.getVariables()) {
				values.put(var,
						IO.getDouble(row.getCell(spec.findColumnIndex(var))));
			}

			if (id == null || MathUtils.containsInvalidDouble(values.values())) {
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
			Map<String, Double> minValues = null;
			Map<String, Double> maxValues = null;

			if (set.isEnforceLimits()) {
				minValues = set.getMinStartValues();
				maxValues = set.getMaxStartValues();
			}

			Map<String, double[]> argumentArrays = new LinkedHashMap<>();

			for (Map.Entry<String, List<Double>> entry : argumentValues.get(id)
					.entrySet()) {
				argumentArrays.put(entry.getKey(),
						Doubles.toArray(entry.getValue()));
			}

			ParameterOptimizer optimizer = new ParameterOptimizer(function
					.getTerms().get(function.getDependentVariable()), function
					.getParameters().toArray(new String[0]), minValues,
					maxValues, Doubles.toArray(targetValues.get(id)),
					argumentArrays);

			optimizer.optimize(set.getnParameterSpace(), set.getnLevenberg(),
					set.isStopWhenSuccessful(), set.getMinStartValues(),
					set.getMaxStartValues());
			results.put(id, optimizer);
		}

		return results;
	}

	private Map<String, ParameterOptimizer> doEstimation(Function function,
			BufferedDataTable dataTable, BufferedDataTable conditionTable)
			throws ParseException {
		if (function.getTimeVariable() == null) {
			return new LinkedHashMap<>();
		}

		DataTableSpec dataSpec = dataTable.getSpec();
		DataTableSpec conditionSpec = conditionTable.getSpec();
		Map<String, List<Double>> timeValues = new LinkedHashMap<>();
		Map<String, List<Double>> targetValues = new LinkedHashMap<>();

		for (DataRow row : dataTable) {
			String id = IO.getString(row.getCell(dataSpec
					.findColumnIndex(NlsUtils.ID_COLUMN)));
			Double time = IO.getDouble(row.getCell(dataSpec
					.findColumnIndex(function.getTimeVariable())));
			Double target = IO.getDouble(row.getCell(dataSpec
					.findColumnIndex(function.getDependentVariable())));

			if (id == null || !MathUtils.isValidDouble(time)
					|| !MathUtils.isValidDouble(target)) {
				continue;
			}

			if (!timeValues.containsKey(id)) {
				timeValues.put(id, new ArrayList<Double>());
				targetValues.put(id, new ArrayList<Double>());
			}

			timeValues.get(id).add(time);
			targetValues.get(id).add(target);
		}

		Map<String, Map<String, List<Double>>> argumentValues = new LinkedHashMap<>();

		for (DataRow row : conditionTable) {
			String id = IO.getString(row.getCell(conditionSpec
					.findColumnIndex(NlsUtils.ID_COLUMN)));
			Map<String, Double> values = new LinkedHashMap<>();

			for (String var : function.getIndependentVariables()) {
				values.put(var, IO.getDouble(row.getCell(conditionSpec
						.findColumnIndex(var))));
			}

			if (id == null || MathUtils.containsInvalidDouble(values.values())) {
				continue;
			}

			if (!argumentValues.containsKey(id)) {
				argumentValues.put(id,
						new LinkedHashMap<String, List<Double>>());

				for (String indep : function.getIndependentVariables()) {
					argumentValues.get(id).put(indep, new ArrayList<Double>());
				}
			}

			for (String indep : function.getIndependentVariables()) {
				argumentValues.get(id).get(indep).add(values.get(indep));
			}
		}

		Map<String, ParameterOptimizer> results = new LinkedHashMap<>();

		for (String id : targetValues.keySet()) {
			Map<String, double[]> argumentArrays = new LinkedHashMap<>();

			for (Map.Entry<String, List<Double>> entry : argumentValues.get(id)
					.entrySet()) {
				argumentArrays.put(entry.getKey(),
						Doubles.toArray(entry.getValue()));
			}

			int n = function.getTerms().size();
			String[] terms = new String[n];
			String[] valueVariables = new String[n];
			Double[] initValues = new Double[n];
			String[] initParameters = new String[n];
			int i = 0;

			for (String var : function.getTerms().keySet()) {
				terms[i] = function.getTerms().get(var);
				valueVariables[i] = var;
				initValues[i] = function.getInitValues().get(var);
				initParameters[i] = function.getInitParameters().get(var);
				i++;
			}

			ParameterOptimizer optimizer = new ParameterOptimizer(terms,
					valueVariables, initValues, initParameters, function
							.getParameters().toArray(new String[0]),
					Doubles.toArray(timeValues.get(id)),
					Doubles.toArray(targetValues.get(id)),
					function.getDependentVariable(),
					function.getTimeVariable(), argumentArrays, new Integrator(
							Integrator.Type.RUNGE_KUTTA, 0.01, 0, 0, 0, 0));

			optimizer.addProgressListener(this);
			optimizer.optimize(set.getnParameterSpace(), set.getnLevenberg(),
					set.isStopWhenSuccessful(), set.getMinStartValues(),
					set.getMaxStartValues());
			results.put(id, optimizer);
		}

		return results;
	}

	@Override
	public void progressChanged(double progress) {
		if (currentExec != null) {
			currentExec.setProgress(progress);
		}
	}

}
