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
package de.bund.bfr.knime.nls.view;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;
import org.nfunk.jep.ParseException;

import de.bund.bfr.knime.nls.NlsUtils;
import de.bund.bfr.knime.nls.ViewDialog;
import de.bund.bfr.knime.nls.chart.ChartAllPanel;
import de.bund.bfr.knime.nls.chart.ChartConfigPanel;
import de.bund.bfr.knime.nls.chart.ChartCreator;
import de.bund.bfr.knime.nls.chart.ChartSelectionPanel;
import de.bund.bfr.knime.nls.functionport.FunctionPortObject;

/**
 * <code>NodeDialog</code> for the "DiffFunctionView" Node.
 * 
 * @author Christian Thoens
 */
public class DiffFunctionViewNodeDialog extends ViewDialog {

	private DiffFunctionViewReader reader;

	private FunctionPortObject functionObject;
	private BufferedDataTable paramTable;
	private BufferedDataTable varTable;
	private BufferedDataTable conditionTable;
	private BufferedDataTable covarianceTable;

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input) throws NotConfigurableException {
		set.loadSettings(settings);
		functionObject = (FunctionPortObject) input[0];
		paramTable = (BufferedDataTable) input[1];
		varTable = (BufferedDataTable) input[2];
		conditionTable = (BufferedDataTable) input[3];
		covarianceTable = (BufferedDataTable) input[4];
		reader = new DiffFunctionViewReader(functionObject, paramTable, varTable, conditionTable, covarianceTable);
		((JPanel) getTab("Options")).removeAll();
		((JPanel) getTab("Options")).add(createMainComponent());
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
