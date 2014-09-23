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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
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
import de.bund.bfr.knime.nls.FittingSettings;
import de.bund.bfr.knime.nls.Function;
import de.bund.bfr.knime.nls.NlsUtils;
import de.bund.bfr.knime.nls.functionport.FunctionPortObject;
import de.bund.bfr.knime.nls.functionport.FunctionPortObjectSpec;
import de.bund.bfr.math.MathUtils;
import de.bund.bfr.math.ParameterOptimizer;

/**
 * This is the model implementation of FunctionFitting.
 * 
 * 
 * @author Christian Thoens
 */
public class FunctionFittingNodeModel extends NodeModel implements
		ParameterOptimizer.ProgressListener {

	private FittingSettings set;

	private ExecutionContext currentExec;

	/**
	 * Constructor for the node model.
	 */
	protected FunctionFittingNodeModel() {
		super(
				new PortType[] { FunctionPortObject.TYPE,
						BufferedDataTable.TYPE }, new PortType[] {
						BufferedDataTable.TYPE, BufferedDataTable.TYPE });
		set = new FittingSettings();
		currentExec = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec)
			throws Exception {
		currentExec = exec;

		FunctionPortObject functionObject = (FunctionPortObject) inObjects[0];
		BufferedDataTable table = (BufferedDataTable) inObjects[1];
		Map<String, ParameterOptimizer> results = doEstimation(
				functionObject.getFunction(), table);
		PortObjectSpec[] outSpec = configure(new PortObjectSpec[] {
				functionObject.getSpec(), table.getSpec() });
		BufferedDataContainer paramContainer = exec
				.createDataContainer((DataTableSpec) outSpec[0]);
		BufferedDataContainer covContainer = exec
				.createDataContainer((DataTableSpec) outSpec[1]);

		NlsUtils.createFittingResultTable(paramContainer, covContainer, results,
				functionObject.getFunction());

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
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		Function function = ((FunctionPortObjectSpec) inSpecs[0]).getFunction();
		DataTableSpec spec = (DataTableSpec) inSpecs[1];
		List<String> variables = function.getVariables();
		List<String> parameters = function.getParameters();
		List<String> stringColumns = KnimeUtils.getColumnNames(KnimeUtils
				.getColumns(spec, StringValue.class));
		List<String> doubleColumns = KnimeUtils.getColumnNames(KnimeUtils
				.getColumns(spec, DoubleValue.class));

		if (!stringColumns.contains(NlsUtils.ID_COLUMN)) {
			throw new InvalidSettingsException(
					"Input Table must contain String Column named \""
							+ NlsUtils.ID_COLUMN + "\"");
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

		specs1.add(new DataColumnSpecCreator(NlsUtils.ID_COLUMN,
				StringCell.TYPE).createSpec());
		specs2.add(new DataColumnSpecCreator(NlsUtils.ID_COLUMN,
				StringCell.TYPE).createSpec());
		specs2.add(new DataColumnSpecCreator(NlsUtils.PARAM_COLUMN,
				StringCell.TYPE).createSpec());

		for (String param : parameters) {
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
			ParameterOptimizer optimizer;
			double[] targetArray = Doubles.toArray(targetValues.get(id));
			Map<String, double[]> argumentArrays = new LinkedHashMap<>();

			for (Map.Entry<String, List<Double>> entry : argumentValues.get(id)
					.entrySet()) {
				argumentArrays.put(entry.getKey(),
						Doubles.toArray(entry.getValue()));
			}

			if (function.getTimeVariable() != null) {
				continue;
			}

			optimizer = new ParameterOptimizer(function.getTerms().get(
					function.getDependentVariable()), function.getParameters()
					.toArray(new String[0]), set.getMinStartValues(),
					set.getMaxStartValues(), set.getMinStartValues(),
					set.getMaxStartValues(), targetArray, argumentArrays,
					set.isEnforceLimits());
			optimizer.optimize(set.getnParameterSpace(), set.getnLevenberg(),
					set.isStopWhenSuccessful());
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
