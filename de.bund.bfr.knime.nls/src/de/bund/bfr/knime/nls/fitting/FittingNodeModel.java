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
package de.bund.bfr.knime.nls.fitting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleConsumer;

import org.apache.commons.math3.util.Pair;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.primitives.Doubles;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.nls.Function;
import de.bund.bfr.knime.nls.NlsUtils;
import de.bund.bfr.knime.nls.functionport.FunctionPortObject;
import de.bund.bfr.knime.nls.functionport.FunctionPortObjectSpec;
import de.bund.bfr.math.IntegratorFactory;
import de.bund.bfr.math.InterpolationFactory;
import de.bund.bfr.math.LeastSquaresOptimization;
import de.bund.bfr.math.MultivariateOptimization;
import de.bund.bfr.math.Optimization;
import de.bund.bfr.math.OptimizationResult;

/**
 * This is the model implementation of DiffFunctionFitting.
 * 
 * 
 * @author Christian Thoens
 */
public class FittingNodeModel extends NodeModel {

	private static final PortType[] INPUT_TYPE = new PortType[] { FunctionPortObject.TYPE, BufferedDataTable.TYPE };
	private static final PortType[] DIFF_INPUT_TYPE = new PortType[] { FunctionPortObject.TYPE, BufferedDataTable.TYPE,
			BufferedDataTable.TYPE };

	private boolean isDiff;
	private FittingSettings set;

	private int numberOfFittings;
	private int currentFitting;

	private DoubleConsumer progressListener;

	/**
	 * Constructor for the node model.
	 */
	protected FittingNodeModel(boolean isDiff, FittingSettings set) {
		super(isDiff ? DIFF_INPUT_TYPE : INPUT_TYPE, new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE });
		this.isDiff = isDiff;
		this.set = set;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		progressListener = progress -> exec.setProgress((progress + currentFitting) / numberOfFittings);

		Function function = ((FunctionPortObject) inObjects[0]).getFunction();
		Map<String, OptimizationResult> results;
		PortObjectSpec[] outSpec;

		if (isDiff) {
			if (set.isFitAllAtOnce()) {
				results = doMultiDiffFitting(function, (BufferedDataTable) inObjects[1],
						(BufferedDataTable) inObjects[2]);
			} else {
				results = doDiffFitting(function, (BufferedDataTable) inObjects[1], (BufferedDataTable) inObjects[2]);
			}

			outSpec = configure(
					new PortObjectSpec[] { inObjects[0].getSpec(), inObjects[1].getSpec(), inObjects[2].getSpec() });
		} else {
			results = doFitting(function, (BufferedDataTable) inObjects[1]);
			outSpec = configure(new PortObjectSpec[] { inObjects[0].getSpec(), inObjects[1].getSpec() });
		}

		DataTableSpec paramSpec = (DataTableSpec) outSpec[0];
		DataTableSpec covSpec = (DataTableSpec) outSpec[1];
		BufferedDataContainer paramContainer = exec.createDataContainer(paramSpec);
		BufferedDataContainer covContainer = exec.createDataContainer(covSpec);
		int iParam = 0;
		int iCov = 0;

		for (Map.Entry<String, OptimizationResult> entry : results.entrySet()) {
			String id = entry.getKey();
			OptimizationResult result = entry.getValue();
			DataCell[] paramCells = new DataCell[paramSpec.getNumColumns()];

			if (result.getParameterValues().isEmpty()) {
				setWarningMessage("Fitting of data set with ID \"" + id + "\" failed");
			}

			for (String param1 : function.getParameters()) {
				paramCells[paramSpec.findColumnIndex(param1)] = IO.createCell(result.getParameterValues().get(param1));

				if (result instanceof LeastSquaresOptimization.Result) {
					DataCell[] covCells = new DataCell[covSpec.getNumColumns()];

					covCells[covSpec.findColumnIndex(NlsUtils.ID_COLUMN)] = IO.createCell(id);
					covCells[covSpec.findColumnIndex(NlsUtils.PARAM_COLUMN)] = IO.createCell(param1);

					for (String param2 : function.getParameters()) {
						covCells[covSpec.findColumnIndex(param2)] = IO
								.createCell(((LeastSquaresOptimization.Result) result).getCovariances()
										.get(new Pair<>(param1, param2)));
					}

					covContainer.addRowToTable(new DefaultRow(String.valueOf(iCov), covCells));
					iCov++;
				}
			}

			paramCells[paramSpec.findColumnIndex(NlsUtils.ID_COLUMN)] = IO.createCell(id);

			if (result instanceof LeastSquaresOptimization.Result) {
				paramCells[paramSpec.findColumnIndex(NlsUtils.SSE_COLUMN)] = IO
						.createCell(((LeastSquaresOptimization.Result) result).getSse());
				paramCells[paramSpec.findColumnIndex(NlsUtils.MSE_COLUMN)] = IO
						.createCell(((LeastSquaresOptimization.Result) result).getMse());
				paramCells[paramSpec.findColumnIndex(NlsUtils.RMSE_COLUMN)] = IO
						.createCell(((LeastSquaresOptimization.Result) result).getRmse());
				paramCells[paramSpec.findColumnIndex(NlsUtils.R2_COLUMN)] = IO
						.createCell(((LeastSquaresOptimization.Result) result).getR2());
				paramCells[paramSpec.findColumnIndex(NlsUtils.AIC_COLUMN)] = IO
						.createCell(((LeastSquaresOptimization.Result) result).getAic());
				paramCells[paramSpec.findColumnIndex(NlsUtils.DOF_COLUMN)] = IO
						.createCell(((LeastSquaresOptimization.Result) result).getDegreesOfFreedom());
				paramCells[paramSpec.findColumnIndex(NlsUtils.SD_COLUMN)] = DataType.getMissingCell();
				paramCells[paramSpec.findColumnIndex(NlsUtils.LOG_LIKELIHOOD_COLUMN)] = DataType.getMissingCell();
			} else if (result instanceof MultivariateOptimization.Result) {
				paramCells[paramSpec.findColumnIndex(NlsUtils.SSE_COLUMN)] = DataType.getMissingCell();
				paramCells[paramSpec.findColumnIndex(NlsUtils.MSE_COLUMN)] = DataType.getMissingCell();
				paramCells[paramSpec.findColumnIndex(NlsUtils.RMSE_COLUMN)] = DataType.getMissingCell();
				paramCells[paramSpec.findColumnIndex(NlsUtils.R2_COLUMN)] = DataType.getMissingCell();
				paramCells[paramSpec.findColumnIndex(NlsUtils.AIC_COLUMN)] = DataType.getMissingCell();
				paramCells[paramSpec.findColumnIndex(NlsUtils.DOF_COLUMN)] = DataType.getMissingCell();
				paramCells[paramSpec.findColumnIndex(NlsUtils.SD_COLUMN)] = IO
						.createCell(((MultivariateOptimization.Result) result).getSdValue());
				paramCells[paramSpec.findColumnIndex(NlsUtils.LOG_LIKELIHOOD_COLUMN)] = IO
						.createCell(((MultivariateOptimization.Result) result).getLogLikelihood());
			}

			paramContainer.addRowToTable(new DefaultRow(String.valueOf(iParam), paramCells));
			iParam++;
		}

		paramContainer.close();
		covContainer.close();

		return new PortObject[] { paramContainer.getTable(), covContainer.getTable() };
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
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		Function function = ((FunctionPortObjectSpec) inSpecs[0]).getFunction();
		DataTableSpec dataSpec = (DataTableSpec) inSpecs[1];
		List<String> dataStringColumns = KnimeUtils.getColumnNames(KnimeUtils.getColumns(dataSpec, StringValue.class));
		List<String> dataDoubleColumns = KnimeUtils.getColumnNames(KnimeUtils.getColumns(dataSpec, DoubleValue.class));

		if (!dataStringColumns.contains(NlsUtils.ID_COLUMN)) {
			throw new InvalidSettingsException(
					"Data Table must contain String Column named \"" + NlsUtils.ID_COLUMN + "\"");
		}

		if (isDiff) {
			DataTableSpec conditionSpec = (DataTableSpec) inSpecs[2];
			List<String> conditionStringColumns = KnimeUtils
					.getColumnNames(KnimeUtils.getColumns(conditionSpec, StringValue.class));
			List<String> conditionDoubleColumns = KnimeUtils
					.getColumnNames(KnimeUtils.getColumns(conditionSpec, DoubleValue.class));

			if (!dataDoubleColumns.contains(function.getTimeVariable())) {
				throw new InvalidSettingsException(
						"Data Table must contain Double Column named \"" + function.getTimeVariable() + "\"");
			}

			if (!dataDoubleColumns.contains(function.getDependentVariable())) {
				throw new InvalidSettingsException(
						"Data Table must contain Double Column named \"" + function.getDependentVariable() + "\"");
			}

			if (!conditionStringColumns.contains(NlsUtils.ID_COLUMN)) {
				throw new InvalidSettingsException(
						"Condition Table must contain String Column named \"" + NlsUtils.ID_COLUMN + "\"");
			}

			for (String var : function.getVariables()) {
				if (!var.equals(function.getDependentVariable()) && !conditionDoubleColumns.contains(var)) {
					throw new InvalidSettingsException(
							"Condition Table must contain Double Column named \"" + var + "\"");
				}
			}
		} else {
			for (String var : function.getVariables()) {
				if (!dataDoubleColumns.contains(var)) {
					throw new InvalidSettingsException("Data Table must contain Double Column named \"" + var + "\"");
				}
			}
		}

		List<DataColumnSpec> specs1 = new ArrayList<>();
		List<DataColumnSpec> specs2 = new ArrayList<>();

		specs1.add(new DataColumnSpecCreator(NlsUtils.ID_COLUMN, StringCell.TYPE).createSpec());
		specs2.add(new DataColumnSpecCreator(NlsUtils.ID_COLUMN, StringCell.TYPE).createSpec());
		specs2.add(new DataColumnSpecCreator(NlsUtils.PARAM_COLUMN, StringCell.TYPE).createSpec());

		for (String param : function.getParameters()) {
			specs1.add(new DataColumnSpecCreator(param, DoubleCell.TYPE).createSpec());
			specs2.add(new DataColumnSpecCreator(param, DoubleCell.TYPE).createSpec());
		}

		specs1.add(new DataColumnSpecCreator(NlsUtils.SSE_COLUMN, DoubleCell.TYPE).createSpec());
		specs1.add(new DataColumnSpecCreator(NlsUtils.MSE_COLUMN, DoubleCell.TYPE).createSpec());
		specs1.add(new DataColumnSpecCreator(NlsUtils.RMSE_COLUMN, DoubleCell.TYPE).createSpec());
		specs1.add(new DataColumnSpecCreator(NlsUtils.R2_COLUMN, DoubleCell.TYPE).createSpec());
		specs1.add(new DataColumnSpecCreator(NlsUtils.AIC_COLUMN, DoubleCell.TYPE).createSpec());
		specs1.add(new DataColumnSpecCreator(NlsUtils.DOF_COLUMN, IntCell.TYPE).createSpec());
		specs1.add(new DataColumnSpecCreator(NlsUtils.SD_COLUMN, DoubleCell.TYPE).createSpec());
		specs1.add(new DataColumnSpecCreator(NlsUtils.LOG_LIKELIHOOD_COLUMN, DoubleCell.TYPE).createSpec());

		return new PortObjectSpec[] { new DataTableSpec(specs1.toArray(new DataColumnSpec[0])),
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
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		set.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	private Map<String, OptimizationResult> doFitting(Function f, BufferedDataTable table) throws ParseException {
		if (f.getTimeVariable() != null) {
			return new LinkedHashMap<>();
		}

		ListMultimap<String, Double> targetValues = ArrayListMultimap.create();
		Map<String, ListMultimap<String, Double>> argumentValues = new LinkedHashMap<>();

		for (String indep : f.getIndependentVariables()) {
			argumentValues.put(indep, ArrayListMultimap.create());
		}

		loop: for (DataRow row : table) {
			String id = IO.getString(row.getCell(table.getSpec().findColumnIndex(NlsUtils.ID_COLUMN)));

			if (id == null) {
				continue loop;
			}

			Map<String, Double> values = new LinkedHashMap<>();

			for (String var : f.getVariables()) {
				Double value = IO.getDouble(row.getCell(table.getSpec().findColumnIndex(var)));

				if (value == null || !Double.isFinite(value)) {
					continue loop;
				}

				values.put(var, value);
			}

			targetValues.put(id, values.get(f.getDependentVariable()));

			for (String indep : f.getIndependentVariables()) {
				argumentValues.get(indep).put(id, values.get(indep));
			}
		}

		Map<String, OptimizationResult> results = new LinkedHashMap<>();
		List<String> ids = readIds(table);

		numberOfFittings = ids.size();
		currentFitting = 0;

		for (String id : ids) {
			Map<String, double[]> argumentArrays = new LinkedHashMap<>();

			for (String indep : f.getIndependentVariables()) {
				argumentArrays.put(indep, Doubles.toArray(argumentValues.get(indep).get(id)));
			}

			Optimization optimizer;

			if (set.getLevelOfDetection() != null) {
				optimizer = new MultivariateOptimization(f.getTerms().get(f.getDependentVariable()),
						f.getParameters().toArray(new String[0]), Doubles.toArray(targetValues.get(id)), argumentArrays,
						set.getLevelOfDetection());
			} else {
				optimizer = new LeastSquaresOptimization(f.getTerms().get(f.getDependentVariable()),
						f.getParameters().toArray(new String[0]), Doubles.toArray(targetValues.get(id)),
						argumentArrays);

				if (set.isEnforceLimits()) {
					((LeastSquaresOptimization) optimizer).getMinValues().putAll(set.getMinStartValues());
					((LeastSquaresOptimization) optimizer).getMaxValues().putAll(set.getMaxStartValues());
				}
			}

			if (!set.getStartValues().isEmpty()) {
				results.put(id,
						optimizer.optimize(set.getnParameterSpace(), set.getnLevenberg(), set.isStopWhenSuccessful(),
								set.getStartValues(), new LinkedHashMap<>(0), set.getMaxLevenbergIterations(),
								progressListener));
			} else {
				results.put(id,
						optimizer.optimize(set.getnParameterSpace(), set.getnLevenberg(), set.isStopWhenSuccessful(),
								set.getMinStartValues(), set.getMaxStartValues(), set.getMaxLevenbergIterations(),
								progressListener));
			}

			currentFitting++;
		}

		return results;
	}

	private Map<String, OptimizationResult> doDiffFitting(Function f, BufferedDataTable dataTable,
			BufferedDataTable conditionTable) throws ParseException {
		if (f.getTimeVariable() == null) {
			return new LinkedHashMap<>();
		}

		Pair<ListMultimap<String, Double>, ListMultimap<String, Double>> data = readDataTable(dataTable, f);
		ListMultimap<String, Double> timeValues = data.getFirst();
		ListMultimap<String, Double> targetValues = data.getSecond();
		Map<String, ListMultimap<String, Double>> argumentValues = readConditionTable(conditionTable, f);

		Map<String, OptimizationResult> results = new LinkedHashMap<>();
		List<String> ids = readIds(dataTable);

		numberOfFittings = ids.size();
		currentFitting = 0;

		for (String id : ids) {
			Map<String, double[]> argumentArrays = new LinkedHashMap<>();

			for (String indep : f.getIndependentVariables()) {
				argumentArrays.put(indep, Doubles.toArray(argumentValues.get(indep).get(id)));
			}

			List<String> valueVariables = new ArrayList<>(f.getTerms().keySet());
			List<String> terms = new ArrayList<>();
			List<Double> initValues = new ArrayList<>();
			List<String> initParameters = new ArrayList<>();

			for (String var : valueVariables) {
				terms.add(f.getTerms().get(var));
				initValues.add(f.getInitValues().containsKey(var) ? f.getInitValues().get(var) : Double.NaN);
				initParameters.add(f.getInitParameters().get(var));
			}

			LeastSquaresOptimization optimizer = new LeastSquaresOptimization(terms.toArray(new String[0]),
					valueVariables.toArray(new String[0]), Doubles.toArray(initValues),
					initParameters.toArray(new String[0]), f.getParameters().toArray(new String[0]),
					Doubles.toArray(timeValues.get(id)), Doubles.toArray(targetValues.get(id)),
					f.getDependentVariable(), f.getTimeVariable(), argumentArrays,
					new IntegratorFactory(IntegratorFactory.Type.RUNGE_KUTTA, set.getStepSize()),
					new InterpolationFactory(set.getInterpolator()));

			if (set.isEnforceLimits()) {
				optimizer.getMinValues().putAll(set.getMinStartValues());
				optimizer.getMaxValues().putAll(set.getMaxStartValues());
			}

			if (!set.getStartValues().isEmpty()) {
				results.put(id,
						optimizer.optimize(set.getnParameterSpace(), set.getnLevenberg(), set.isStopWhenSuccessful(),
								set.getStartValues(), new LinkedHashMap<>(), set.getMaxLevenbergIterations(),
								progressListener));
			} else {
				results.put(id,
						optimizer.optimize(set.getnParameterSpace(), set.getnLevenberg(), set.isStopWhenSuccessful(),
								set.getMinStartValues(), set.getMaxStartValues(), set.getMaxLevenbergIterations(),
								progressListener));
			}

			currentFitting++;
		}

		return results;
	}

	private Map<String, OptimizationResult> doMultiDiffFitting(Function f, BufferedDataTable dataTable,
			BufferedDataTable conditionTable) throws ParseException {
		if (f.getTimeVariable() == null) {
			return new LinkedHashMap<>();
		}

		List<String> ids = readIds(dataTable);
		Pair<ListMultimap<String, Double>, ListMultimap<String, Double>> data = readDataTable(dataTable, f);
		ListMultimap<String, Double> timeValues = data.getFirst();
		ListMultimap<String, Double> targetValues = data.getSecond();
		Map<String, ListMultimap<String, Double>> argumentValues = readConditionTable(conditionTable, f);

		List<String> valueVariables = new ArrayList<>(f.getTerms().keySet());
		List<String> terms = new ArrayList<>();
		List<Double> initValues = new ArrayList<>();

		for (String var : valueVariables) {
			terms.add(f.getTerms().get(var));
			initValues.add(f.getInitValues().containsKey(var) ? f.getInitValues().get(var) : Double.NaN);
		}

		List<double[]> timeArrays = new ArrayList<>();
		List<double[]> targetArrays = new ArrayList<>();
		Map<String, List<double[]>> variableArrays = new LinkedHashMap<>();
		List<String> parameters = new ArrayList<>(f.getParameters());
		List<String[]> initParameters = new ArrayList<>();

		for (String var : set.getInitValuesWithDifferentStart()) {
			if (f.getInitValues().get(var) == null) {
				parameters.remove(f.getInitParameters().get(var));
			}
		}

		for (String var : f.getIndependentVariables()) {
			variableArrays.put(var, new ArrayList<>());
		}

		for (int i = 0; i < ids.size(); i++) {
			String id = ids.get(i);

			timeArrays.add(Doubles.toArray(timeValues.get(id)));
			targetArrays.add(Doubles.toArray(targetValues.get(id)));

			for (String var : f.getIndependentVariables()) {
				variableArrays.get(var).add(Doubles.toArray(argumentValues.get(var).get(id)));
			}

			for (String var : set.getInitValuesWithDifferentStart()) {
				if (f.getInitValues().get(var) == null) {
					parameters.add(var + "_" + i);
				}
			}

			List<String> initParams = new ArrayList<>();

			for (String var : valueVariables) {
				if (set.getInitValuesWithDifferentStart().contains(var) && f.getInitValues().get(var) == null) {
					initParams.add(var + "_" + i);
				} else {
					initParams.add(f.getInitParameters().get(var));
				}
			}

			initParameters.add(initParams.toArray(new String[0]));
		}

		LeastSquaresOptimization optimizer = new LeastSquaresOptimization(terms.toArray(new String[0]),
				valueVariables.toArray(new String[0]), Doubles.toArray(initValues), initParameters,
				parameters.toArray(new String[0]), timeArrays, targetArrays, f.getDependentVariable(),
				f.getTimeVariable(), variableArrays,
				new IntegratorFactory(IntegratorFactory.Type.RUNGE_KUTTA, set.getStepSize()),
				new InterpolationFactory(set.getInterpolator()));

		if (set.isEnforceLimits()) {
			optimizer.getMinValues().putAll(set.getMinStartValues());
			optimizer.getMaxValues().putAll(set.getMaxStartValues());
		}

		numberOfFittings = 1;
		currentFitting = 0;

		LeastSquaresOptimization.Result result;

		if (!set.getStartValues().isEmpty()) {
			result = optimizer.optimize(set.getnParameterSpace(), set.getnLevenberg(), set.isStopWhenSuccessful(),
					set.getStartValues(), new LinkedHashMap<>(), set.getMaxLevenbergIterations(), progressListener);
		} else {
			result = optimizer.optimize(set.getnParameterSpace(), set.getnLevenberg(), set.isStopWhenSuccessful(),
					set.getMinStartValues(), set.getMaxStartValues(), set.getMaxLevenbergIterations(),
					progressListener);
		}

		Map<String, OptimizationResult> results = new LinkedHashMap<>();

		for (int i = 0; i < ids.size(); i++) {
			LeastSquaresOptimization.Result r = result.copy();

			for (String var : set.getInitValuesWithDifferentStart()) {
				if (f.getInitValues().get(var) == null) {
					String oldName = var + "_" + i;
					String newName = f.getInitParameters().get(var);

					r.getParameterValues().put(newName, r.getParameterValues().remove(oldName));
					r.getParameterStandardErrors().put(newName, r.getParameterStandardErrors().remove(oldName));
					r.getParameterTValues().put(newName, r.getParameterTValues().remove(oldName));
					r.getParameterPValues().put(newName, r.getParameterPValues().remove(oldName));

					Map<Pair<String, String>, Double> oldCovariances = new LinkedHashMap<>(r.getCovariances());

					r.getCovariances().clear();

					for (Map.Entry<Pair<String, String>, Double> entry : oldCovariances.entrySet()) {
						String param1 = entry.getKey().getFirst();
						String param2 = entry.getKey().getSecond();

						r.getCovariances().put(new Pair<>(param1.equals(oldName) ? newName : param1,
								param2.equals(oldName) ? newName : param2), entry.getValue());
					}
				}
			}

			results.put(ids.get(i), r);
		}

		return results;
	}

	private static List<String> readIds(BufferedDataTable table) {
		Set<String> ids = new LinkedHashSet<>();

		for (DataRow row : table) {
			String id = IO.getString(row.getCell(table.getSpec().findColumnIndex(NlsUtils.ID_COLUMN)));

			if (id != null) {
				ids.add(id);
			}
		}

		return new ArrayList<>(ids);
	}

	private static Pair<ListMultimap<String, Double>, ListMultimap<String, Double>> readDataTable(
			BufferedDataTable table, Function f) {
		ListMultimap<String, Double> timeValues = ArrayListMultimap.create();
		ListMultimap<String, Double> targetValues = ArrayListMultimap.create();

		for (DataRow row : table) {
			String id = IO.getString(row.getCell(table.getSpec().findColumnIndex(NlsUtils.ID_COLUMN)));
			Double time = IO.getDouble(row.getCell(table.getSpec().findColumnIndex(f.getTimeVariable())));
			Double target = IO.getDouble(row.getCell(table.getSpec().findColumnIndex(f.getDependentVariable())));

			if (id != null && time != null && target != null && Double.isFinite(time) && Double.isFinite(target)) {
				timeValues.put(id, time);
				targetValues.put(id, target);
			}
		}

		return new Pair<>(timeValues, targetValues);
	}

	private static Map<String, ListMultimap<String, Double>> readConditionTable(BufferedDataTable table, Function f) {
		Map<String, ListMultimap<String, Double>> argumentValues = new LinkedHashMap<>();

		for (String indep : f.getIndependentVariables()) {
			argumentValues.put(indep, ArrayListMultimap.create());
		}

		loop: for (DataRow row : table) {
			String id = IO.getString(row.getCell(table.getSpec().findColumnIndex(NlsUtils.ID_COLUMN)));

			if (id == null) {
				continue loop;
			}

			Map<String, Double> values = new LinkedHashMap<>();

			for (String var : f.getIndependentVariables()) {
				Double value = IO.getDouble(row.getCell(table.getSpec().findColumnIndex(var)));

				if (value == null || !Double.isFinite(value)) {
					continue loop;
				}

				values.put(var, value);
			}

			for (String indep : f.getIndependentVariables()) {
				argumentValues.get(indep).put(id, values.get(indep));
			}
		}

		return argumentValues;
	}
}
