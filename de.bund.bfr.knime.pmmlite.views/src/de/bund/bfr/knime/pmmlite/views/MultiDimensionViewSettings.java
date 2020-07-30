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
package de.bund.bfr.knime.pmmlite.views;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.chart.NamedShape;
import de.bund.bfr.knime.pmmlite.core.Plotable;
import de.bund.bfr.knime.pmmlite.core.XmlUtils;
import de.bund.bfr.knime.pmmlite.views.chart.ChartConfigPanel;
import de.bund.bfr.knime.pmmlite.views.chart.ChartCreator;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSamplePanel;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSelectionPanel;

public class MultiDimensionViewSettings extends ViewSettings {

	private static final String CFG_SELECTED_ID = "SelectedID";
	private static final String CFG_SELECTED_VALUES_X = "SelectedValuesX";
	private static final String CFG_COLOR_LISTS = "ColorLists2";
	private static final String CFG_SHAPE_LISTS = "ShapeLists2";

	protected String selectedID;
	protected Map<String, List<Boolean>> selectedValuesX;
	protected Map<String, List<Color>> colorLists;
	protected Map<String, List<NamedShape>> shapeLists;

	public MultiDimensionViewSettings() {
		selectedID = null;
		selectedValuesX = new LinkedHashMap<>();
		colorLists = new LinkedHashMap<>();
		shapeLists = new LinkedHashMap<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void load(NodeSettingsRO settings) {
		super.load(settings);

		try {
			selectedID = settings.getString(CFG_SELECTED_ID);
		} catch (InvalidSettingsException e) {
		}

		try {
			selectedValuesX = (Map<String, List<Boolean>>) XmlUtils.fromXml(settings.getString(CFG_SELECTED_VALUES_X));
		} catch (InvalidSettingsException e) {
		}

		try {
			colorLists = (Map<String, List<Color>>) XmlUtils.fromXml(settings.getString(CFG_COLOR_LISTS));
		} catch (InvalidSettingsException e) {
		}

		try {
			shapeLists = (Map<String, List<NamedShape>>) XmlUtils.fromXml(settings.getString(CFG_SHAPE_LISTS));
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void save(NodeSettingsWO settings) throws InvalidSettingsException {
		super.save(settings);
		settings.addString(CFG_SELECTED_ID, selectedID);
		settings.addString(CFG_SELECTED_VALUES_X, XmlUtils.toXml(selectedValuesX));
		settings.addString(CFG_COLOR_LISTS, XmlUtils.toXml(colorLists));
		settings.addString(CFG_SHAPE_LISTS, XmlUtils.toXml(shapeLists));
	}

	@Override
	public void setToPlotable(Plotable plotable) {
		Map<String, List<Double>> arguments = new LinkedHashMap<>();
		Map<String, List<Double>> possibleValues = plotable.getPossibleVariableValues();

		for (Map.Entry<String, List<Boolean>> entry : selectedValuesX.entrySet()) {
			List<Double> usedValues = new ArrayList<>();
			List<Double> valuesList = possibleValues.get(entry.getKey());

			if (!entry.getKey().equals(varX)) {
				for (int i = 0; i < entry.getValue().size(); i++) {
					if (entry.getValue().get(i)) {
						usedValues.add(valuesList.get(i));
					}
				}
			} else {
				usedValues.add(0.0);
			}

			arguments.put(entry.getKey(), usedValues);
		}

		plotable.getVariables().putAll(arguments);
	}

	@Override
	public void setToChartCreator(ChartCreator creator) {
		super.setToChartCreator(creator);
		creator.setColorLists(colorLists);
		creator.setShapeLists(shapeLists);
	}

	@Override
	public void setFromConfigPanel(ChartConfigPanel configPanel) {
		super.setFromConfigPanel(configPanel);
		selectedValuesX = configPanel.getSelectedValues();
	}

	@Override
	public void setToConfigPanel(ChartConfigPanel configPanel) {
		super.setToConfigPanel(configPanel);
		configPanel.setSelectedValues(selectedValuesX);
	}

	@Override
	public void setFromSelectionPanel(ChartSelectionPanel selectionPanel) {
		super.setFromSelectionPanel(selectionPanel);
		colorLists = selectionPanel.getColorLists();
		shapeLists = selectionPanel.getShapeLists();

		if (!selectionPanel.getSelectedIDs().isEmpty()) {
			selectedID = selectionPanel.getSelectedIDs().get(0);
		} else {
			selectedID = null;
		}
	}

	@Override
	public void setToSelectionPanel(ChartSelectionPanel selectionPanel) {
		super.setToSelectionPanel(selectionPanel);
		selectionPanel.setColorLists(colorLists);
		selectionPanel.setShapeLists(shapeLists);

		if (getSelectedID() != null) {
			selectionPanel.setSelectedIDs(Arrays.asList(selectedID));
		}
	}

	@Override
	public void setFromSamplePanel(ChartSamplePanel samplePanel) {
	}

	@Override
	public void setToSamplePanel(ChartSamplePanel samplePanel) {
	}

	public String getSelectedID() {
		return selectedID;
	}

	public void setSelectedID(String selectedID) {
		this.selectedID = selectedID;
	}
}
