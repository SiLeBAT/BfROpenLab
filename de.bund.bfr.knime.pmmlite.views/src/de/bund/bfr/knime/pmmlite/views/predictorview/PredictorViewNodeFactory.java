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
package de.bund.bfr.knime.pmmlite.views.predictorview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeModel;
import org.sbml.jsbml.text.parser.ParseException;

import de.bund.bfr.knime.pmmlite.core.Plotable;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.UnitException;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;
import de.bund.bfr.knime.pmmlite.views.MultiSelectionViewDialog;
import de.bund.bfr.knime.pmmlite.views.MultiSelectionViewModel;
import de.bund.bfr.knime.pmmlite.views.ViewFactory;
import de.bund.bfr.knime.pmmlite.views.ViewReader;
import de.bund.bfr.knime.ui.Dialogs;

/**
 * <code>NodeFactory</code> for the "PredictorView" Node.
 * 
 * 
 * @author Christian Thoens
 */
public class PredictorViewNodeFactory extends ViewFactory {

	@Override
	public NodeModel createNodeModel() {
		return new MultiSelectionViewModel(false) {

			@Override
			protected List<PmmPortObjectSpec> getCompatibleSpecs() {
				return Arrays.asList(PmmPortObjectSpec.PRIMARY_MODEL_TYPE, PmmPortObjectSpec.TERTIARY_MODEL_TYPE);
			}

			@Override
			protected ViewReader createReader(PmmPortObject input) throws UnitException, ParseException {
				return new PredictorViewReader(input);
			}
		};
	}

	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new MultiSelectionViewDialog() {

			@Override
			protected ViewReader createReader(PmmPortObject input) throws UnitException, ParseException {
				return new PredictorViewReader(input);
			}

			@Override
			protected boolean showSamplePanel() {
				return true;
			}

			@Override
			protected boolean allowInterval() {
				return true;
			}

			@Override
			protected void updateChart() {
				super.updateChart();

				Map<String, double[][]> points = new LinkedHashMap<>();
				Plotable.Variable varX = new Plotable.Variable(PmmUtils.TIME, configPanel.getUnitX(),
						configPanel.getTransformX());
				Plotable.Variable varY = new Plotable.Variable(PmmUtils.CONCENTRATION, configPanel.getUnitY(),
						configPanel.getTransformY());

				for (String id : configPanel.isDisplayHighlighted() ? selectionPanel.getFocusedIDs()
						: selectionPanel.getSelectedIDs()) {
					Plotable plotable = reader.getPlotables().get(id);
					String shortId = ((PredictorViewReader) reader).getShortIds().get(id);

					if (plotable != null) {
						try {
							points.put(shortId, plotable.getFunctionSamplePoints(varX, varY, Double.NEGATIVE_INFINITY,
									Double.POSITIVE_INFINITY));
						} catch (ParseException | UnitException e) {
							chartCreator.createEmptyChart();
							Dialogs.showErrorMessage(chartCreator, e.getMessage());
							return;
						}
					}
				}

				samplePanel.setNamesY(new ArrayList<>(points.keySet()));
				samplePanel.setDataPoints(points);
			}
		};
	}
}
