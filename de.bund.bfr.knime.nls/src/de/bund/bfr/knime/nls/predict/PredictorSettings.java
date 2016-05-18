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

import java.util.LinkedHashMap;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.nls.view.ViewSettings;

public class PredictorSettings extends ViewSettings {

	private static final String CFG_VARIABLE_VALUES = "VariableValues";
	private static final String CFG_MIN_VARIABLE_VALUES = "MinVariableValues";
	private static final String CFG_MAX_VARIABLE_VALUES = "MaxVariableValues";

	private Map<String, Double> variableValues;
	private Map<String, Double> minVariableValues;
	private Map<String, Double> maxVariableValues;

	public PredictorSettings() {
		variableValues = new LinkedHashMap<>();
		minVariableValues = new LinkedHashMap<>();
		maxVariableValues = new LinkedHashMap<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		super.loadSettings(settings);

		try {
			variableValues = (Map<String, Double>) SERIALIZER.fromXml(settings.getString(CFG_VARIABLE_VALUES));
		} catch (InvalidSettingsException e) {
		}

		try {
			minVariableValues = (Map<String, Double>) SERIALIZER.fromXml(settings.getString(CFG_MIN_VARIABLE_VALUES));
		} catch (InvalidSettingsException e) {
		}

		try {
			maxVariableValues = (Map<String, Double>) SERIALIZER.fromXml(settings.getString(CFG_MAX_VARIABLE_VALUES));
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		super.saveSettings(settings);

		settings.addString(CFG_VARIABLE_VALUES, SERIALIZER.toXml(variableValues));
		settings.addString(CFG_MIN_VARIABLE_VALUES, SERIALIZER.toXml(minVariableValues));
		settings.addString(CFG_MAX_VARIABLE_VALUES, SERIALIZER.toXml(maxVariableValues));
	}

	public Map<String, Double> getVariableValues() {
		return variableValues;
	}

	public void setVariableValues(Map<String, Double> variableValues) {
		this.variableValues = variableValues;
	}

	public Map<String, Double> getMinVariableValues() {
		return minVariableValues;
	}

	public void setMinVariableValues(Map<String, Double> minVariableValues) {
		this.minVariableValues = minVariableValues;
	}

	public Map<String, Double> getMaxVariableValues() {
		return maxVariableValues;
	}

	public void setMaxVariableValues(Map<String, Double> maxVariableValues) {
		this.maxVariableValues = maxVariableValues;
	}
}
