/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
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
package de.bund.bfr.knime.openkrise.views.tracingparameters;

import java.util.LinkedHashMap;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.openkrise.views.TracingSettings;

public class TracingParametersSettings extends TracingSettings {

	public static final boolean DEFAULT_ENFORCE_TEMPORAL_ORDER = false;

	private static final String CFG_CASE_WEIGHTS = "CaseWeights";
	private static final String CFG_CROSS_CONTAMINATIONS = "CrossContaminations";
	private static final String CFG_FILTER = "Filter";
	private static final String CFG_EDGE_FILTER = "EdgeFilter";
	private static final String CFG_WEIGHT_CONDITION = "WeightCondition";
	private static final String CFG_CONTAMINATION_CONDITION = "ContaminationCondition";
	private static final String CFG_FILTER_CONDITION = "FilterCondition";
	private static final String CFG_EDGE_FILTER_CONDITION = "EdgeFilterCondition";
	private static final String CFG_WEIGHT_CONDITION_VALUE = "WeightConditionValue";
	private static final String CFG_CONTAMINATION_CONDITION_VALUE = "ContaminationConditionValue";
	private static final String CFG_FILTER_CONDITION_VALUE = "FilterConditionValue";
	private static final String CFG_EDGE_FILTER_CONDITION_VALUE = "EdgeFilterConditionValue";
	private static final String CFG_ENFORCE_TEMPORAL_ORDER = "EnforceTemporalOrder";

	private Map<String, Double> caseWeights;
	private Map<String, Boolean> crossContaminations;
	private Map<String, Boolean> filter;
	private Map<String, Boolean> edgeFilter;
	private AndOrHighlightCondition weightCondition;
	private AndOrHighlightCondition contaminationCondition;
	private AndOrHighlightCondition filterCondition;
	private AndOrHighlightCondition edgeFilterCondition;
	private Double weightConditionValue;
	private Boolean contaminationConditionValue;
	private Boolean filterConditionValue;
	private Boolean edgeFilterConditionValue;
	private boolean enforeTemporalOrder;

	public TracingParametersSettings() {
		caseWeights = new LinkedHashMap<>();
		crossContaminations = new LinkedHashMap<>();
		filter = new LinkedHashMap<>();
		edgeFilter = new LinkedHashMap<>();
		weightCondition = null;
		contaminationCondition = null;
		filterCondition = null;
		edgeFilterCondition = null;
		weightConditionValue = Double.NaN;
		contaminationConditionValue = false;
		filterConditionValue = false;
		edgeFilterConditionValue = false;
		enforeTemporalOrder = DEFAULT_ENFORCE_TEMPORAL_ORDER;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			caseWeights = (Map<String, Double>) SERIALIZER.fromXml(settings
					.getString(CFG_CASE_WEIGHTS));
		} catch (InvalidSettingsException e) {
		}

		try {
			crossContaminations = (Map<String, Boolean>) SERIALIZER
					.fromXml(settings.getString(CFG_CROSS_CONTAMINATIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			filter = (Map<String, Boolean>) SERIALIZER.fromXml(settings
					.getString(CFG_FILTER));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeFilter = (Map<String, Boolean>) SERIALIZER.fromXml(settings
					.getString(CFG_EDGE_FILTER));
		} catch (InvalidSettingsException e) {
		}

		try {
			weightCondition = (AndOrHighlightCondition) SERIALIZER
					.fromXml(settings.getString(CFG_WEIGHT_CONDITION));
		} catch (InvalidSettingsException e) {
		}

		try {
			contaminationCondition = (AndOrHighlightCondition) SERIALIZER
					.fromXml(settings.getString(CFG_CONTAMINATION_CONDITION));
		} catch (InvalidSettingsException e) {
		}

		try {
			filterCondition = (AndOrHighlightCondition) SERIALIZER
					.fromXml(settings.getString(CFG_FILTER_CONDITION));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeFilterCondition = (AndOrHighlightCondition) SERIALIZER
					.fromXml(settings.getString(CFG_EDGE_FILTER_CONDITION));
		} catch (InvalidSettingsException e) {
		}

		try {
			weightConditionValue = settings
					.getDouble(CFG_WEIGHT_CONDITION_VALUE);
			weightConditionValue = Double.isNaN(weightConditionValue) ? null
					: weightConditionValue;
		} catch (InvalidSettingsException e) {
		}

		try {
			contaminationConditionValue = settings
					.getBoolean(CFG_CONTAMINATION_CONDITION_VALUE);
			contaminationConditionValue = !contaminationConditionValue ? null
					: contaminationConditionValue;
		} catch (InvalidSettingsException e) {
		}

		try {
			filterConditionValue = settings
					.getBoolean(CFG_FILTER_CONDITION_VALUE);
			filterConditionValue = !filterConditionValue ? null
					: filterConditionValue;
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeFilterConditionValue = settings
					.getBoolean(CFG_EDGE_FILTER_CONDITION_VALUE);
			edgeFilterConditionValue = !edgeFilterConditionValue ? null
					: edgeFilterConditionValue;
		} catch (InvalidSettingsException e) {
		}

		try {
			enforeTemporalOrder = settings
					.getBoolean(CFG_ENFORCE_TEMPORAL_ORDER);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addString(CFG_CASE_WEIGHTS, SERIALIZER.toXml(caseWeights));
		settings.addString(CFG_CROSS_CONTAMINATIONS,
				SERIALIZER.toXml(crossContaminations));
		settings.addString(CFG_FILTER, SERIALIZER.toXml(filter));
		settings.addString(CFG_EDGE_FILTER, SERIALIZER.toXml(edgeFilter));
		settings.addString(CFG_WEIGHT_CONDITION,
				SERIALIZER.toXml(weightCondition));
		settings.addString(CFG_CONTAMINATION_CONDITION,
				SERIALIZER.toXml(contaminationCondition));
		settings.addString(CFG_FILTER_CONDITION,
				SERIALIZER.toXml(filterCondition));
		settings.addString(CFG_EDGE_FILTER_CONDITION,
				SERIALIZER.toXml(edgeFilterCondition));
		settings.addDouble(CFG_WEIGHT_CONDITION_VALUE,
				weightConditionValue == null ? Double.NaN
						: weightConditionValue);
		settings.addBoolean(CFG_CONTAMINATION_CONDITION_VALUE,
				contaminationConditionValue == null ? false
						: contaminationConditionValue);
		settings.addBoolean(CFG_FILTER_CONDITION_VALUE,
				filterConditionValue == null ? false : filterConditionValue);
		settings.addBoolean(CFG_EDGE_FILTER_CONDITION_VALUE,
				edgeFilterConditionValue == null ? false
						: edgeFilterConditionValue);
		settings.addBoolean(CFG_ENFORCE_TEMPORAL_ORDER, enforeTemporalOrder);
	}

	public Map<String, Double> getCaseWeights() {
		return caseWeights;
	}

	public void setCaseWeights(Map<String, Double> caseWeights) {
		this.caseWeights = caseWeights;
	}

	public Map<String, Boolean> getCrossContaminations() {
		return crossContaminations;
	}

	public void setCrossContaminations(Map<String, Boolean> crossContaminations) {
		this.crossContaminations = crossContaminations;
	}

	public Map<String, Boolean> getFilter() {
		return filter;
	}

	public void setFilter(Map<String, Boolean> filter) {
		this.filter = filter;
	}

	public Map<String, Boolean> getEdgeFilter() {
		return edgeFilter;
	}

	public void setEdgeFilter(Map<String, Boolean> edgeFilter) {
		this.edgeFilter = edgeFilter;
	}

	public AndOrHighlightCondition getWeightCondition() {
		return weightCondition;
	}

	public void setWeightCondition(AndOrHighlightCondition weightCondition) {
		this.weightCondition = weightCondition;
	}

	public AndOrHighlightCondition getContaminationCondition() {
		return contaminationCondition;
	}

	public void setContaminationCondition(
			AndOrHighlightCondition contaminationCondition) {
		this.contaminationCondition = contaminationCondition;
	}

	public AndOrHighlightCondition getFilterCondition() {
		return filterCondition;
	}

	public void setFilterCondition(AndOrHighlightCondition filterCondition) {
		this.filterCondition = filterCondition;
	}

	public AndOrHighlightCondition getEdgeFilterCondition() {
		return edgeFilterCondition;
	}

	public void setEdgeFilterCondition(
			AndOrHighlightCondition edgeFilterCondition) {
		this.edgeFilterCondition = edgeFilterCondition;
	}

	public Double getWeightConditionValue() {
		return weightConditionValue;
	}

	public void setWeightConditionValue(Double weightConditionValue) {
		this.weightConditionValue = weightConditionValue;
	}

	public Boolean getContaminationConditionValue() {
		return contaminationConditionValue;
	}

	public void setContaminationConditionValue(
			Boolean contaminationConditionValue) {
		this.contaminationConditionValue = contaminationConditionValue;
	}

	public Boolean getFilterConditionValue() {
		return filterConditionValue;
	}

	public void setFilterConditionValue(Boolean filterConditionValue) {
		this.filterConditionValue = filterConditionValue;
	}

	public Boolean getEdgeFilterConditionValue() {
		return edgeFilterConditionValue;
	}

	public void setEdgeFilterConditionValue(Boolean edgeFilterConditionValue) {
		this.edgeFilterConditionValue = edgeFilterConditionValue;
	}

	public boolean isEnforeTemporalOrder() {
		return enforeTemporalOrder;
	}

	public void setEnforeTemporalOrder(boolean enforeTemporalOrder) {
		this.enforeTemporalOrder = enforeTemporalOrder;
	}
}
