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
package de.bund.bfr.knime.nls.predict;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.port.PortObject;
import org.nfunk.jep.ParseException;

import de.bund.bfr.knime.nls.NlsUtils;
import de.bund.bfr.knime.nls.ViewDialog;
import de.bund.bfr.knime.nls.ViewSettings;
import de.bund.bfr.knime.nls.chart.ChartAllPanel;
import de.bund.bfr.knime.nls.chart.ChartConfigPanel;
import de.bund.bfr.knime.nls.chart.ChartCreator;
import de.bund.bfr.knime.nls.chart.ChartSelectionPanel;
import de.bund.bfr.knime.nls.functionport.FunctionPortObject;

/**
 * <code>NodeDialog</code> for the "DiffFunctionPredictor" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Christian Thoens
 */
public class DiffFunctionPredictorNodeDialog extends ViewDialog {

	private DiffFunctionPredictorReader reader;

	private FunctionPortObject functionObject;
	private BufferedDataTable paramTable;
	private BufferedDataTable conditionTable;
	private BufferedDataTable covarianceTable;

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input) throws NotConfigurableException {
		set.loadSettings(settings);
		functionObject = (FunctionPortObject) input[0];
		paramTable = (BufferedDataTable) input[1];
		conditionTable = (BufferedDataTable) input[2];
		covarianceTable = (BufferedDataTable) input[3];
		reader = new DiffFunctionPredictorReader(functionObject, paramTable, conditionTable, covarianceTable);
		((JPanel) getTab("Options")).removeAll();
		((JPanel) getTab("Options")).add(createMainComponent());
	}

	@Override
	protected ViewSettings createSettings() {
		return new PredictorSettings();
	}

	private JComponent createMainComponent() {
		configPanel = new ChartConfigPanel(true, true, false, false, true);
		configPanel.init(reader.getDepVar(), NlsUtils.getOrderedVariables(reader.getPlotables().values()), null);
		selectionPanel = new ChartSelectionPanel(reader.getIds(), reader.getStringColumns(), reader.getDoubleColumns());
		chartCreator = new ChartCreator(reader.getPlotables(), reader.getLegend());
		chartCreator.setVarY(reader.getDepVar());

		set.setToConfigPanel(configPanel);
		set.setToSelectionPanel(selectionPanel);
		configPanel.addConfigListener(this);
		selectionPanel.addSelectionListener(this);
		chartCreator.addZoomListener(this);
		createChart();

		return new ChartAllPanel(chartCreator, selectionPanel, configPanel);
	}

	@Override
	protected void createChart() {
		set.setFromConfigPanel(configPanel);
		set.setFromSelectionPanel(selectionPanel);
		set.setToChartCreator(chartCreator);

		try {
			chartCreator.setChart(chartCreator.createChart());
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
