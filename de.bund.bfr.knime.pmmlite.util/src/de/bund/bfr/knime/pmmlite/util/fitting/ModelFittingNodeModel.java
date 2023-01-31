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
package de.bund.bfr.knime.pmmlite.util.fitting;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import de.bund.bfr.knime.NoInternalsNodeModel;
import de.bund.bfr.knime.pmmlite.core.CombineUtils;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.models.Model;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModel;
import de.bund.bfr.knime.pmmlite.core.models.SecondaryModel;
import de.bund.bfr.knime.pmmlite.core.models.TertiaryModel;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;

/**
 * This is the model implementation of ModelFitting.
 * 
 * 
 * @author Christian Thoens
 */
public class ModelFittingNodeModel extends NoInternalsNodeModel {

	private static final int MAX_THREADS = 8;

	private ModelFittingSettings set;

	/**
	 * Constructor for the node model.
	 */
	protected ModelFittingNodeModel() {
		super(new PortType[] { PmmPortObject.TYPE }, new PortType[] { PmmPortObject.TYPE, PmmPortObject.TYPE });
		set = new ModelFittingSettings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		PmmPortObject in = (PmmPortObject) inObjects[0];

		switch (set.getFittingType()) {
		case PRIMARY_FITTING:
			List<PrimaryModel> prim = in.getData(PrimaryModel.class);

			doEstimation(prim, exec);
			return new PortObject[] { PmmPortObject.createListObject(prim, PmmPortObjectSpec.PRIMARY_MODEL_TYPE),
					PmmPortObject.createEmptyObject() };
		case SECONDARY_FITTING:
			List<SecondaryModel> sec = in.getData(SecondaryModel.class);

			doEstimation(sec, exec);
			return new PortObject[] { PmmPortObject.createListObject(sec, PmmPortObjectSpec.SECONDARY_MODEL_TYPE),
					PmmPortObject.createListObject(CombineUtils.combine(sec), PmmPortObjectSpec.TERTIARY_MODEL_TYPE) };
		case TERTIARY_FITTING:
			List<TertiaryModel> tert = in.getSpec() == PmmPortObjectSpec.SECONDARY_MODEL_TYPE
					? CombineUtils.combine(in.getData(SecondaryModel.class)) : in.getData(TertiaryModel.class);

			doEstimation(tert, exec);
			return new PortObject[] { PmmPortObject.createListObject(tert, PmmPortObjectSpec.TERTIARY_MODEL_TYPE),
					PmmPortObject.createEmptyObject() };
		default:
			throw new RuntimeException("Unknown fitting type: " + set.getFittingType());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		if (set.getFittingType() == null) {
			throw new InvalidSettingsException("Node has to be configured");
		}

		PmmPortObjectSpec in = (PmmPortObjectSpec) inSpecs[0];
		PmmPortObjectSpec out1 = null;
		PmmPortObjectSpec out2 = null;

		switch (set.getFittingType()) {
		case PRIMARY_FITTING:
			if (in != PmmPortObjectSpec.PRIMARY_MODEL_TYPE) {
				throw new InvalidSettingsException("Wrong Input");
			}

			out1 = PmmPortObjectSpec.PRIMARY_MODEL_TYPE;
			out2 = PmmPortObjectSpec.EMPTY_TYPE;
			break;
		case SECONDARY_FITTING:
			if (in != PmmPortObjectSpec.SECONDARY_MODEL_TYPE) {
				throw new InvalidSettingsException("Wrong Input");
			}

			out1 = PmmPortObjectSpec.SECONDARY_MODEL_TYPE;
			out2 = PmmPortObjectSpec.TERTIARY_MODEL_TYPE;
			break;
		case TERTIARY_FITTING:
			if (in != PmmPortObjectSpec.SECONDARY_MODEL_TYPE && in != PmmPortObjectSpec.TERTIARY_MODEL_TYPE) {
				throw new InvalidSettingsException("Wrong Input");
			}

			out1 = PmmPortObjectSpec.TERTIARY_MODEL_TYPE;
			out2 = PmmPortObjectSpec.EMPTY_TYPE;
			break;
		default:
			if (in != PmmPortObjectSpec.PRIMARY_MODEL_TYPE && in != PmmPortObjectSpec.SECONDARY_MODEL_TYPE
					&& in != PmmPortObjectSpec.TERTIARY_MODEL_TYPE) {
				throw new InvalidSettingsException("Wrong Input");
			}

			break;
		}

		return new PortObjectSpec[] { out1, out2 };
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

	private void doEstimation(List<? extends Model> dataModels, ExecutionContext exec)
			throws CanceledExecutionException, InterruptedException {
		int n = dataModels.size();
		AtomicInteger finishedThreads = new AtomicInteger(0);
		ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

		for (Model dataModel : dataModels) {
			Map<String, Double> minStartValues;
			Map<String, Double> maxStartValues;

			if (set.isExpertSettings()) {
				minStartValues = set.getMinStartValues().get(dataModel.getFormula().getId());
				maxStartValues = set.getMaxStartValues().get(dataModel.getFormula().getId());
			} else {
				minStartValues = PmmUtils.getMinValues(dataModel.getFormula().getParams());
				maxStartValues = PmmUtils.getMaxValues(dataModel.getFormula().getParams());
			}

			if (dataModel instanceof PrimaryModel) {
				executor.execute(new PrimaryEstimationThread((PrimaryModel) dataModel, minStartValues, maxStartValues,
						set.isEnforceLimits(), set.getnParameterSpace(), set.getnLevenberg(),
						set.isStopWhenSuccessful(), finishedThreads));
			} else if (dataModel instanceof SecondaryModel) {
				executor.execute(new SecondaryEstimationThread((SecondaryModel) dataModel, minStartValues,
						maxStartValues, set.isEnforceLimits(), set.getnParameterSpace(), set.getnLevenberg(),
						set.isStopWhenSuccessful(), finishedThreads));
			} else if (dataModel instanceof TertiaryModel) {
				executor.execute(new TertiaryEstimationThread((TertiaryModel) dataModel, minStartValues, maxStartValues,
						set.isEnforceLimits(), set.getnParameterSpace(), set.getnLevenberg(),
						set.isStopWhenSuccessful(), finishedThreads));
			}
		}

		executor.shutdown();

		while (!executor.isTerminated()) {
			exec.checkCanceled();
			exec.setProgress((double) finishedThreads.get() / (double) n, "");
			Thread.sleep(100);
		}
	}

}
