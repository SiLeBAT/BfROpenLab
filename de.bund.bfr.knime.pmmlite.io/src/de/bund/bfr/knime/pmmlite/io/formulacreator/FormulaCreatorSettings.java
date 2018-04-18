/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.io.formulacreator;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.pmmlite.core.EmfUtils;
import de.bund.bfr.knime.pmmlite.core.models.ModelFormula;

public class FormulaCreatorSettings {

	public static enum FormulaType {
		PRIMARY_TYPE("Primary"), SECONDARY_TYPE("Secondary");

		private String name;

		private FormulaType(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private static final String CFG_MODEL_TYPE = "ModelType";
	private static final String CFG_FORMULA = "Formula";

	private static final FormulaType DEFAULT_MODEL_TYPE = FormulaType.PRIMARY_TYPE;

	private FormulaType modelType;
	private ModelFormula formula;

	public FormulaCreatorSettings() {
		modelType = DEFAULT_MODEL_TYPE;
		formula = null;
	}

	public void load(NodeSettingsRO settings) {
		try {
			modelType = FormulaType.valueOf(settings.getString(CFG_MODEL_TYPE));
		} catch (InvalidSettingsException e) {
		}

		try {
			formula = EmfUtils.fromXml(settings.getString(CFG_FORMULA), ModelFormula.class);
		} catch (InvalidSettingsException e) {
		}
	}

	public void save(NodeSettingsWO settings) {
		settings.addString(CFG_MODEL_TYPE, modelType.name());
		settings.addString(CFG_FORMULA, EmfUtils.toXml(formula));
	}

	public FormulaType getModelType() {
		return modelType;
	}

	public void setModelType(FormulaType modelType) {
		this.modelType = modelType;
	}

	public ModelFormula getFormula() {
		return formula;
	}

	public void setFormula(ModelFormula formula) {
		this.formula = formula;
	}

}
