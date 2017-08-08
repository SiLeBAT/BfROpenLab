/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.views;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.google.common.collect.Lists;

import de.bund.bfr.knime.chart.NamedShape;
import de.bund.bfr.knime.pmmlite.core.Plotable;
import de.bund.bfr.knime.pmmlite.core.XmlUtils;
import de.bund.bfr.knime.pmmlite.views.chart.ChartConfigPanel;
import de.bund.bfr.knime.pmmlite.views.chart.ChartCreator;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSamplePanel;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSelectionPanel;

public abstract class SingleDimensionViewSettings extends ViewSettings {

	private static final String CFG_VARIABLE_VALUES = "ParamXValues";
	private static final String CFG_COLORS = "Colors2";
	private static final String CFG_SHAPES = "Shapes2";
	private static final String CFG_SAMPLE_VALUES = "SampleValues";

	protected Map<String, Double> variableValues;
	protected Map<String, Color> colors;
	protected Map<String, NamedShape> shapes;
	protected List<Double> sampleValues;

	public SingleDimensionViewSettings() {
		variableValues = new LinkedHashMap<>();
		colors = new LinkedHashMap<>();
		shapes = new LinkedHashMap<>();
		sampleValues = new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void load(NodeSettingsRO settings) {
		super.load(settings);

		try {
			variableValues = (Map<String, Double>) XmlUtils.fromXml(settings.getString(CFG_VARIABLE_VALUES));
		} catch (InvalidSettingsException e) {
		}

		try {
			colors = (Map<String, Color>) XmlUtils.fromXml(settings.getString(CFG_COLORS));
		} catch (InvalidSettingsException e) {
		}

		try {
			shapes = (Map<String, NamedShape>) XmlUtils.fromXml(settings.getString(CFG_SHAPES));
		} catch (InvalidSettingsException e) {
		}

		try {
			sampleValues = (List<Double>) XmlUtils.fromXml(settings.getString(CFG_SAMPLE_VALUES));
		} catch (InvalidSettingsException e1) {
		}
	}

	@Override
	public void save(NodeSettingsWO settings) throws InvalidSettingsException {
		super.save(settings);
		settings.addString(CFG_VARIABLE_VALUES, XmlUtils.toXml(variableValues));
		settings.addString(CFG_COLORS, XmlUtils.toXml(colors));
		settings.addString(CFG_SHAPES, XmlUtils.toXml(shapes));
		settings.addString(CFG_SAMPLE_VALUES, XmlUtils.toXml(sampleValues));
	}

	@Override
	public void setToPlotable(Plotable plotable) {
		for (String arg : plotable.getVariables().keySet()) {
			plotable.getVariables().put(arg, Lists.newArrayList(variableValues.get(arg)));
		}

		for (String arg : plotable.getVariableParameters().keySet()) {
			plotable.getVariableParameters().put(arg, variableValues.get(arg));
		}

		plotable.getSamples().clear();
		plotable.getSamples().addAll(sampleValues);
	}

	@Override
	public void setToChartCreator(ChartCreator creator) {
		super.setToChartCreator(creator);
		creator.setColors(colors);
		creator.setShapes(shapes);
	}

	@Override
	public void setFromConfigPanel(ChartConfigPanel configPanel) {
		super.setFromConfigPanel(configPanel);
		variableValues = configPanel.getVariableValues();
	}

	@Override
	public void setToConfigPanel(ChartConfigPanel configPanel) {
		super.setToConfigPanel(configPanel);
		configPanel.setVariableValues(variableValues);
	}

	@Override
	public void setFromSelectionPanel(ChartSelectionPanel selectionPanel) {
		super.setFromSelectionPanel(selectionPanel);
		colors = selectionPanel.getColors();
		shapes = selectionPanel.getShapes();
	}

	@Override
	public void setToSelectionPanel(ChartSelectionPanel selectionPanel) {
		super.setToSelectionPanel(selectionPanel);
		selectionPanel.setColors(colors);
		selectionPanel.setShapes(shapes);
	}

	@Override
	public void setFromSamplePanel(ChartSamplePanel samplePanel) {
		sampleValues = samplePanel.getValuesX();
	}

	@Override
	public void setToSamplePanel(ChartSamplePanel samplePanel) {
		samplePanel.setValuesX(sampleValues);
	}
}
