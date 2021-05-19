/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.io.modelcreator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.pmmlite.core.EmfUtils;
import de.bund.bfr.knime.pmmlite.core.XmlUtils;
import de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit;

public class ModelCreatorSettings {

	private static final String CFG_ID = "Id";
	private static final String CFG_ORGANISM = "Organism";
	private static final String CFG_MATRIX = "Matrix";
	private static final String CFG_CONDITIONS = "Conditions";
	private static final String CFG_UNITS = "Units";
	private static final String CFG_PARAMETERS = "Parameters";

	private String id;
	private String organism;
	private String matrix;
	private Map<String, Double> conditions;
	private List<NameableWithUnit> units;
	private Map<String, Double> parameters;

	public ModelCreatorSettings() {
		id = null;
		organism = null;
		matrix = null;
		conditions = new LinkedHashMap<>();
		units = new ArrayList<>();
		parameters = new LinkedHashMap<>();
	}

	@SuppressWarnings("unchecked")
	public void load(NodeSettingsRO settings) {
		try {
			id = settings.getString(CFG_ID);
		} catch (InvalidSettingsException e) {
		}

		try {
			organism = settings.getString(CFG_ORGANISM);
		} catch (InvalidSettingsException e) {
		}

		try {
			matrix = settings.getString(CFG_MATRIX);
		} catch (InvalidSettingsException e) {
		}

		try {
			conditions = (Map<String, Double>) XmlUtils.fromXml(settings.getString(CFG_CONDITIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			units = EmfUtils.listFromXml(settings.getString(CFG_UNITS), NameableWithUnit.class);
		} catch (InvalidSettingsException e) {
		}

		try {
			parameters = (Map<String, Double>) XmlUtils.fromXml(settings.getString(CFG_PARAMETERS));
		} catch (InvalidSettingsException e) {
		}
	}

	public void save(NodeSettingsWO settings) {
		settings.addString(CFG_ID, id);
		settings.addString(CFG_ORGANISM, organism);
		settings.addString(CFG_MATRIX, matrix);
		settings.addString(CFG_CONDITIONS, XmlUtils.toXml(conditions));
		settings.addString(CFG_UNITS, EmfUtils.listToXml(units));
		settings.addString(CFG_PARAMETERS, XmlUtils.toXml(parameters));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrganism() {
		return organism;
	}

	public void setOrganism(String organism) {
		this.organism = organism;
	}

	public String getMatrix() {
		return matrix;
	}

	public void setMatrix(String matrix) {
		this.matrix = matrix;
	}

	public Map<String, Double> getConditions() {
		return conditions;
	}

	public void setConditions(Map<String, Double> conditions) {
		this.conditions = conditions;
	}

	public List<NameableWithUnit> getUnits() {
		return units;
	}

	public void setUnits(List<NameableWithUnit> units) {
		this.units = units;
	}

	public Map<String, Double> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Double> parameters) {
		this.parameters = parameters;
	}
}
