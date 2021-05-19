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
package de.bund.bfr.knime.pmmlite.io.datareader;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.pmmlite.core.EmfUtils;
import de.bund.bfr.knime.pmmlite.core.XmlUtils;
import de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit;

public class XlsDataReaderSettings {

	private static final String CFG_FILE_NAME = "FileName";
	private static final String CFG_SHEET_NAME = "SheetName";
	private static final String CFG_ID_COLUMN = "IdColumn";
	private static final String CFG_TIME_COLUMN = "TimeColumn";
	private static final String CFG_CONCENTRATION_COLUMN = "ConcentrationColumn";
	private static final String CFG_ORGANISM_COLUMN = "OrganismColumn";
	private static final String CFG_MATRIX_COLUMN = "MatrixColumn";
	private static final String CFG_CONDITION_COLUMNS = "ConditionColumns";
	private static final String CFG_UNITS = "Units";

	private String fileName;
	private String sheetName;
	private String idColumn;
	private String timeColumn;
	private String concentrationColumn;
	private String organismColumn;
	private String matrixColumn;
	private List<String> conditionColumns;
	private List<NameableWithUnit> units;

	public XlsDataReaderSettings() {
		fileName = null;
		sheetName = null;
		idColumn = null;
		timeColumn = null;
		concentrationColumn = null;
		organismColumn = null;
		matrixColumn = null;
		conditionColumns = new ArrayList<>();
		units = new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	public void load(NodeSettingsRO settings) {
		try {
			fileName = settings.getString(CFG_FILE_NAME);
		} catch (InvalidSettingsException e) {
		}

		try {
			sheetName = settings.getString(CFG_SHEET_NAME);
		} catch (InvalidSettingsException e) {
		}

		try {
			idColumn = settings.getString(CFG_ID_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			timeColumn = settings.getString(CFG_TIME_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			concentrationColumn = settings.getString(CFG_CONCENTRATION_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			organismColumn = settings.getString(CFG_ORGANISM_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			matrixColumn = settings.getString(CFG_MATRIX_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			conditionColumns = (List<String>) XmlUtils.fromXml(settings.getString(CFG_CONDITION_COLUMNS));
		} catch (InvalidSettingsException e) {
		}

		try {
			units = EmfUtils.listFromXml(settings.getString(CFG_UNITS), NameableWithUnit.class);
		} catch (InvalidSettingsException e) {
		}
	}

	public void save(NodeSettingsWO settings) {
		settings.addString(CFG_FILE_NAME, fileName);
		settings.addString(CFG_SHEET_NAME, sheetName);
		settings.addString(CFG_ID_COLUMN, idColumn);
		settings.addString(CFG_TIME_COLUMN, timeColumn);
		settings.addString(CFG_CONCENTRATION_COLUMN, concentrationColumn);
		settings.addString(CFG_ORGANISM_COLUMN, organismColumn);
		settings.addString(CFG_MATRIX_COLUMN, matrixColumn);
		settings.addString(CFG_CONDITION_COLUMNS, XmlUtils.toXml(conditionColumns));
		settings.addString(CFG_UNITS, EmfUtils.listToXml(units));
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	public String getIdColumn() {
		return idColumn;
	}

	public void setIdColumn(String idColumn) {
		this.idColumn = idColumn;
	}

	public String getTimeColumn() {
		return timeColumn;
	}

	public void setTimeColumn(String timeColumn) {
		this.timeColumn = timeColumn;
	}

	public String getConcentrationColumn() {
		return concentrationColumn;
	}

	public void setConcentrationColumn(String concentrationColumn) {
		this.concentrationColumn = concentrationColumn;
	}

	public String getOrganismColumn() {
		return organismColumn;
	}

	public void setOrganismColumn(String organismColumn) {
		this.organismColumn = organismColumn;
	}

	public String getMatrixColumn() {
		return matrixColumn;
	}

	public void setMatrixColumn(String matrixColumn) {
		this.matrixColumn = matrixColumn;
	}

	public List<String> getConditionColumns() {
		return conditionColumns;
	}

	public void setConditionColumns(List<String> conditionColumns) {
		this.conditionColumns = conditionColumns;
	}

	public List<NameableWithUnit> getUnits() {
		return units;
	}

	public void setUnits(List<NameableWithUnit> units) {
		this.units = units;
	}
}
