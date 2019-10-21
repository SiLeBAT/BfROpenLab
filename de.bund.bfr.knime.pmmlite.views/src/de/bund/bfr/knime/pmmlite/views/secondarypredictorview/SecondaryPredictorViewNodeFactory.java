/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.views.secondarypredictorview;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeModel;
import org.sbml.jsbml.text.parser.ParseException;

import de.bund.bfr.knime.pmmlite.core.Plotable;
import de.bund.bfr.knime.pmmlite.core.UnitException;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;
import de.bund.bfr.knime.pmmlite.views.SingleSelectionViewDialog;
import de.bund.bfr.knime.pmmlite.views.SingleSelectionViewModel;
import de.bund.bfr.knime.pmmlite.views.ViewFactory;
import de.bund.bfr.knime.pmmlite.views.ViewReader;
import de.bund.bfr.knime.ui.Dialogs;

/**
 * <code>NodeFactory</code> for the "SecondaryPredictorView" Node.
 * 
 * 
 * @author Christian Thoens
 */
public class SecondaryPredictorViewNodeFactory extends ViewFactory {

	@Override
	public NodeModel createNodeModel() {
		return new SingleSelectionViewModel() {

			@Override
			protected List<PmmPortObjectSpec> getCompatibleSpecs() {
				return Arrays.asList(PmmPortObjectSpec.SECONDARY_MODEL_TYPE);
			}

			@Override
			protected ViewReader createReader(PmmPortObject input) throws UnitException, ParseException {
				return new SecondaryPredictorViewReader(input);
			}
		};
	}

	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new SingleSelectionViewDialog() {

			@Override
			protected ViewReader createReader(PmmPortObject input) throws UnitException, ParseException {
				return new SecondaryPredictorViewReader(input);
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

				Plotable plotable = reader.getPlotables().get(configPanel.isDisplayHighlighted()
						? selectionPanel.getFocusedID() : selectionPanel.getSelectedID());
				Map<String, double[][]> points = new LinkedHashMap<>();

				if (plotable != null) {
					set.setToPlotable(plotable);

					Plotable.Variable varX = new Plotable.Variable(configPanel.getVarX(), configPanel.getUnitX(),
							configPanel.getTransformX());
					Plotable.Variable varY = new Plotable.Variable(configPanel.getVarY(), configPanel.getUnitY(),
							configPanel.getTransformY());

					try {
						points.put(configPanel.getVarY(), plotable.getFunctionSamplePoints(varX, varY,
								Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
					} catch (ParseException | UnitException e) {
						chartCreator.createEmptyChart();
						Dialogs.showErrorMessage(chartCreator, e.getMessage());
						return;
					}
				}

				samplePanel.setNameX(configPanel.getVarX());
				samplePanel.setNamesY(Arrays.asList(configPanel.getVarY()));
				samplePanel.setDataPoints(points);
			}
		};
	}
}
