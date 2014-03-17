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
package de.bund.bfr.knime.openkrise.views.tracingview2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.openkrise.views.tracingview.TracingViewSettings;

public class TracingView2Settings extends TracingViewSettings {

	public static final boolean DEFAULT_ENFORCE_TEMPORAL_ORDER = false;

	private static final String CFG_CASE_WEIGHTS = "CaseWeights";
	private static final String CFG_CROSS_CONTAMINATIONS = "CrossContaminations";
	private static final String CFG_FILTER = "Filter";
	private static final String CFG_ENFORCE_TEMPORAL_ORDER = "EnforceTemporalOrder";

	private ByteArrayOutputStream xmlBaos;
	
	private Map<String, Double> caseWeights;
	private Map<String, Boolean> crossContaminations;
	private Map<String, Boolean> filter;
	private boolean enforeTemporalOrder;

	public TracingView2Settings() {
		caseWeights = new LinkedHashMap<String, Double>();
		crossContaminations = new LinkedHashMap<String, Boolean>();
		filter = new LinkedHashMap<String, Boolean>();
		enforeTemporalOrder = DEFAULT_ENFORCE_TEMPORAL_ORDER;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		super.loadSettings(settings);
		try {
			xmlBaos = new ByteArrayOutputStream();
			settings.saveToXML(xmlBaos);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

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
			enforeTemporalOrder = settings
					.getBoolean(CFG_ENFORCE_TEMPORAL_ORDER);
		} catch (InvalidSettingsException e) {
		}
	}
	public String getXml() {
		return xmlBaos == null ? "" : xmlBaos.toString();
	}
	public void setXml(String xml) {
		ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());
		try {
			loadSettings(NodeSettings.loadFromXML(in));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		super.saveSettings(settings);

		settings.addString(CFG_CASE_WEIGHTS, SERIALIZER.toXml(caseWeights));
		settings.addString(CFG_CROSS_CONTAMINATIONS,
				SERIALIZER.toXml(crossContaminations));
		settings.addString(CFG_FILTER, SERIALIZER.toXml(filter));
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

	public boolean isEnforeTemporalOrder() {
		return enforeTemporalOrder;
	}

	public void setEnforeTemporalOrder(boolean enforeTemporalOrder) {
		this.enforeTemporalOrder = enforeTemporalOrder;
	}
}
