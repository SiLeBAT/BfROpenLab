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
	private static final String CFG_ENFORCE_TEMPORAL_ORDER = "EnforceTemporalOrder";

	private Map<Integer, Double> caseWeights;
	private Map<Integer, Boolean> crossContaminations;
	private Map<Integer, Boolean> filter;
	private Map<Integer, Boolean> edgeFilter;
	private AndOrHighlightCondition weightCondition;
	private AndOrHighlightCondition contaminationCondition;
	private AndOrHighlightCondition filterCondition;
	private AndOrHighlightCondition edgeFilterCondition;
	private boolean enforeTemporalOrder;

	public TracingParametersSettings() {
		caseWeights = new LinkedHashMap<Integer, Double>();
		crossContaminations = new LinkedHashMap<Integer, Boolean>();
		filter = new LinkedHashMap<Integer, Boolean>();
		edgeFilter = new LinkedHashMap<Integer, Boolean>();
		weightCondition = null;
		contaminationCondition = null;
		filterCondition = null;
		edgeFilterCondition = null;
		enforeTemporalOrder = DEFAULT_ENFORCE_TEMPORAL_ORDER;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			caseWeights = (Map<Integer, Double>) SERIALIZER.fromXml(settings
					.getString(CFG_CASE_WEIGHTS));
		} catch (InvalidSettingsException e) {
		}

		try {
			crossContaminations = (Map<Integer, Boolean>) SERIALIZER
					.fromXml(settings.getString(CFG_CROSS_CONTAMINATIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			filter = (Map<Integer, Boolean>) SERIALIZER.fromXml(settings
					.getString(CFG_FILTER));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeFilter = (Map<Integer, Boolean>) SERIALIZER.fromXml(settings
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
		settings.addBoolean(CFG_ENFORCE_TEMPORAL_ORDER, enforeTemporalOrder);
	}

	public Map<Integer, Double> getCaseWeights() {
		return caseWeights;
	}

	public void setCaseWeights(Map<Integer, Double> caseWeights) {
		this.caseWeights = caseWeights;
	}

	public Map<Integer, Boolean> getCrossContaminations() {
		return crossContaminations;
	}

	public void setCrossContaminations(Map<Integer, Boolean> crossContaminations) {
		this.crossContaminations = crossContaminations;
	}

	public Map<Integer, Boolean> getFilter() {
		return filter;
	}

	public void setFilter(Map<Integer, Boolean> filter) {
		this.filter = filter;
	}

	public Map<Integer, Boolean> getEdgeFilter() {
		return edgeFilter;
	}

	public void setEdgeFilter(Map<Integer, Boolean> edgeFilter) {
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

	public void setEdgeFilterCondition(AndOrHighlightCondition edgeFilterCondition) {
		this.edgeFilterCondition = edgeFilterCondition;
	}

	public boolean isEnforeTemporalOrder() {
		return enforeTemporalOrder;
	}

	public void setEnforeTemporalOrder(boolean enforeTemporalOrder) {
		this.enforeTemporalOrder = enforeTemporalOrder;
	}
}
