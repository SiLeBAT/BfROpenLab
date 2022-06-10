/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleConsumer;

import org.apache.commons.math3.util.Pair;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.sbml.jsbml.text.parser.ParseException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.NoInternalsNodeModel;
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
public class FittingNodeModel extends NoInternalsNodeModel {

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
						(BufferedDataTable) inObjects[2], exec);
			} else {
				results = doDiffFitting(function, (BufferedDataTable) inObjects[1], (BufferedDataTable) inObjects[2],
						exec);
			}

			outSpec = configure(
					new PortObjectSpec[] { inObjects[0].getSpec(), inObjects[1].getSpec(), inObjects[2].getSpec() });
		} else {
			results = doFitting(function, (BufferedDataTable) inObjects[1], exec);
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
	@SuppressWarnings("unchecked")
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		Function function = ((FunctionPortObjectSpec) inSpecs[0]).getFunction();
		DataTableSpec dataSpec = (DataTableSpec) inSpecs[1];
		List<String> dataDoubleColumns = IO.getColumnNames(dataSpec, DoubleValue.class);

		if (!IO.getColumnNames(dataSpec, StringCell.TYPE).contains(NlsUtils.ID_COLUMN)) {
			throw new InvalidSettingsException(
					"Data Table must contain String Column named \"" + NlsUtils.ID_COLUMN + "\"");
		}

		if (isDiff) {
			DataTableSpec conditionSpec = (DataTableSpec) inSpecs[2];
			List<String> conditionDoubleColumns = IO.getColumnNames(conditionSpec, DoubleValue.class);

			if (!dataDoubleColumns.contains(function.getTimeVariable())) {
				throw new InvalidSettingsException(
						"Data Table must contain Double Column named \"" + function.getTimeVariable() + "\"");
			}

			if (!dataDoubleColumns.contains(function.getDependentVariable())) {
				throw new InvalidSettingsException(
						"Data Table must contain Double Column named \"" + function.getDependentVariable() + "\"");
			}

			if (!IO.getColumnNames(conditionSpec, StringCell.TYPE).contains(NlsUtils.ID_COLUMN)) {
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

	private Map<String, OptimizationResult> doFitting(Function f, BufferedDataTable table, ExecutionContext exec)
			throws ParseException, CanceledExecutionException {
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
		List<String> ids = NlsUtils.getIds(table);

		numberOfFittings = ids.size();
		currentFitting = 0;

		for (String id : ids) {
			NodeLogger.getLogger(FittingNodeModel.class).debug("Started fitting of data set with id: \"" + id + "\"");

			Map<String, List<Double>> argumentLists = new LinkedHashMap<>();

			for (String indep : f.getIndependentVariables()) {
				argumentLists.put(indep, argumentValues.get(indep).get(id));
			}

			Optimization optimizer;

			if (set.getLevelOfDetection() != null) {
				optimizer = MultivariateOptimization.createLodOptimizer(f.getTerms().get(f.getDependentVariable()),
						f.getParameters(), targetValues.get(id), argumentLists, set.getLevelOfDetection());
			} else {
				optimizer = LeastSquaresOptimization.createVectorOptimizer(f.getTerms().get(f.getDependentVariable()),
						f.getParameters(), targetValues.get(id), argumentLists);

				if (set.isEnforceLimits()) {
					((LeastSquaresOptimization) optimizer).getMinValues().putAll(set.getMinStartValues());
					((LeastSquaresOptimization) optimizer).getMaxValues().putAll(set.getMaxStartValues());
				}
			}

			if (!set.getStartValues().isEmpty()) {
				results.put(id,
						optimizer.optimize(set.getnParameterSpace(), set.getnLevenberg(), set.isStopWhenSuccessful(),
								set.getStartValues(), new LinkedHashMap<>(0), set.getMaxLevenbergIterations(),
								progressListener, exec));
			} else {
				results.put(id,
						optimizer.optimize(set.getnParameterSpace(), set.getnLevenberg(), set.isStopWhenSuccessful(),
								set.getMinStartValues(), set.getMaxStartValues(), set.getMaxLevenbergIterations(),
								progressListener, exec));
			}

			currentFitting++;
			NodeLogger.getLogger(FittingNodeModel.class).debug("Finished fitting of data set with id: \"" + id + "\"");
		}

		return results;
	}

	private Map<String, OptimizationResult> doDiffFitting(Function f, BufferedDataTable dataTable,
			BufferedDataTable conditionTable, ExecutionContext exec) throws ParseException, CanceledExecutionException {
		if (f.getTimeVariable() == null) {
			return new LinkedHashMap<>();
		}

		Map<String, OptimizationResult> results = new LinkedHashMap<>();
		List<String> ids = NlsUtils.getIds(dataTable);

		numberOfFittings = ids.size();
		currentFitting = 0;

		for (String id : ids) {
			NodeLogger.getLogger(FittingNodeModel.class).debug("Started fitting of data set with id: \"" + id + "\"");
			Map<String, List<Double>> variableValues = NlsUtils.getDiffVariableValues(dataTable, id, f);
			Map<String, List<Double>> argumentValues = NlsUtils.getConditionValues(conditionTable, id, f);
			List<String> valueVariables = new ArrayList<>(f.getTerms().keySet());
			List<String> terms = new ArrayList<>();
			List<Double> initValues = new ArrayList<>();
			List<String> initParameters = new ArrayList<>();

			for (String var : valueVariables) {
				terms.add(f.getTerms().get(var));
				initValues.add(f.getInitValues().get(var));
				initParameters.add(f.getInitParameters().get(var));
			}

			LeastSquaresOptimization optimizer = LeastSquaresOptimization.createVectorDiffOptimizer(terms,
					valueVariables, initValues, initParameters, f.getParameters(),
					variableValues.get(f.getTimeVariable()), variableValues.get(f.getDependentVariable()),
					f.getDependentVariable(), f.getTimeVariable(), argumentValues,
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
								progressListener, exec));
			} else {
				results.put(id,
						optimizer.optimize(set.getnParameterSpace(), set.getnLevenberg(), set.isStopWhenSuccessful(),
								set.getMinStartValues(), set.getMaxStartValues(), set.getMaxLevenbergIterations(),
								progressListener, exec));
			}

			currentFitting++;
			NodeLogger.getLogger(FittingNodeModel.class).debug("Finished fitting of data set with id: \"" + id + "\"");
		}

		return results;
	}

	private Map<String, OptimizationResult> doMultiDiffFitting(Function f, BufferedDataTable dataTable,
			BufferedDataTable conditionTable, ExecutionContext exec) throws ParseException, CanceledExecutionException {
		if (f.getTimeVariable() == null) {
			return new LinkedHashMap<>();
		}

		NodeLogger.getLogger(FittingNodeModel.class).debug("Started fitting of all data sets");

		List<String> ids = NlsUtils.getIds(dataTable);
		List<String> valueVariables = new ArrayList<>(f.getTerms().keySet());
		List<String> terms = new ArrayList<>();
		List<Double> initValues = new ArrayList<>();

		for (String var : valueVariables) {
			terms.add(f.getTerms().get(var));
			initValues.add(f.getInitValues().get(var));
		}

		List<List<Double>> timeLists = new ArrayList<>();
		List<List<Double>> targetLists = new ArrayList<>();
		List<Map<String, List<Double>>> variableLists = new ArrayList<>();
		List<String> parameters = new ArrayList<>(f.getParameters());
		List<List<String>> initParameters = new ArrayList<>();

		for (String var : set.getInitValuesWithDifferentStart()) {
			if (f.getInitValues().get(var) == null) {
				parameters.remove(f.getInitParameters().get(var));
			}
		}

		for (int i = 0; i < ids.size(); i++) {
			String id = ids.get(i);
			Map<String, List<Double>> variableValues = NlsUtils.getDiffVariableValues(dataTable, id, f);
			Map<String, List<Double>> argumentValues = NlsUtils.getConditionValues(conditionTable, id, f);

			timeLists.add(variableValues.get(f.getTimeVariable()));
			targetLists.add(variableValues.get(f.getDependentVariable()));
			variableLists.add(argumentValues);

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

			initParameters.add(initParams);
		}

		LeastSquaresOptimization optimizer = LeastSquaresOptimization.createMultiVectorDiffOptimizer(terms,
				valueVariables, initValues, initParameters, parameters, timeLists, targetLists,
				f.getDependentVariable(), f.getTimeVariable(), variableLists,
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
					set.getStartValues(), new LinkedHashMap<>(), set.getMaxLevenbergIterations(), progressListener,
					exec);
		} else {
			result = optimizer.optimize(set.getnParameterSpace(), set.getnLevenberg(), set.isStopWhenSuccessful(),
					set.getMinStartValues(), set.getMaxStartValues(), set.getMaxLevenbergIterations(), progressListener,
					exec);
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

					Map<Pair<String, String>, Double> newCovariances = new LinkedHashMap<>();

					r.getCovariances().forEach((params, cov) -> {
						String param1 = params.getFirst().equals(oldName) ? newName : params.getFirst();
						String param2 = params.getSecond().equals(oldName) ? newName : params.getSecond();

						newCovariances.put(new Pair<>(param1, param2), cov);
					});

					r.getCovariances().clear();
					r.getCovariances().putAll(newCovariances);
				}
			}

			results.put(ids.get(i), r);
		}

		NodeLogger.getLogger(FittingNodeModel.class).debug("Finished fitting of all data sets");

		return results;
	}
}
