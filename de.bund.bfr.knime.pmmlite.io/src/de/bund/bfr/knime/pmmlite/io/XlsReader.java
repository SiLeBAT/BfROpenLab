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
package de.bund.bfr.knime.pmmlite.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.google.common.base.Strings;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit;
import de.bund.bfr.knime.pmmlite.core.data.Condition;
import de.bund.bfr.knime.pmmlite.core.data.DataFactory;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeriesPoint;
import de.bund.bfr.knime.pmmlite.core.models.ModelsFactory;
import de.bund.bfr.knime.pmmlite.core.models.ParameterValue;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModel;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.TertiaryModel;
import de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.Variable;

public class XlsReader {

	private Workbook wb;
	private Sheet s;
	private List<String> warnings;

	public XlsReader() {
		wb = null;
		s = null;
		warnings = new ArrayList<>();
	}

	public void setFile(String fileName) throws InvalidFormatException, IOException {
		wb = null;

		try (InputStream in = new FileInputStream(KnimeUtils.getFile(fileName))) {
			wb = WorkbookFactory.create(in);
		}
	}

	public void setSheet(String sheetName) throws XlsException {
		if (wb == null) {
			throw new XlsException("No file specified");
		}

		s = wb.getSheet(sheetName);

		if (s == null) {
			throw new XlsException("File does not contain sheet \"" + sheetName + "\"");
		}
	}

	public List<TimeSeries> getTimeSeriesList(String idColumn, String timeColumn, String concentrationColumn,
			String organismColumn, String matrixColumn, List<String> conditionColumns, List<NameableWithUnit> units)
			throws XlsException {
		if (s == null) {
			throw new XlsException("No sheet specified");
		}

		warnings.clear();

		Map<String, NameableWithUnit> unitsByName = PmmUtils.getByName(units);
		List<TimeSeries> list = new ArrayList<>();
		Map<String, Integer> columns = getColumns(s);
		TimeSeries series = null;
		String lastId = null;

		for (int i = s.getFirstRowNum() + 1; i <= s.getLastRowNum(); i++) {
			Row row = s.getRow(i);

			String id = getData(getCell(row, columns, idColumn));
			String time = getData(getCell(row, columns, timeColumn));
			String concentration = getData(getCell(row, columns, concentrationColumn));
			String organism = getData(getCell(row, columns, organismColumn));
			String matrix = getData(getCell(row, columns, matrixColumn));

			if (id != null && !id.equals(lastId)) {
				if (series != null) {
					list.add(series);
				}

				lastId = id;
				series = DataFactory.eINSTANCE.createTimeSeries();
				series.setName(id);
				series.setTimeUnit(unitsByName.get(PmmUtils.TIME).getUnit());
				series.setConcentrationUnit(unitsByName.get(PmmUtils.CONCENTRATION).getUnit());
				series.setOrganism(organism);
				series.setMatrix(matrix);
				PmmUtils.setId(series);

				for (String column : conditionColumns) {
					Condition condition = DataFactory.eINSTANCE.createCondition();
					String s = getData(getCell(row, columns, column));

					try {
						condition.setValue(Double.parseDouble(s));
					} catch (NumberFormatException e) {
						warnings.add(column + " value in row " + (i + 1) + " is not valid (" + s + ")");
					} catch (NullPointerException e) {
					}

					condition.setName(PmmUtils.createMathSymbol(column));
					condition.setUnit(unitsByName.get(column).getUnit());
					series.getConditions().add(condition);
				}
			}

			if (series != null) {
				Double timeValue = null;
				Double concentrationValue = null;

				try {
					timeValue = Double.parseDouble(time);
				} catch (NumberFormatException e) {
					warnings.add(timeColumn + " value in row " + (i + 1) + " is not valid (" + time + ")");
				} catch (NullPointerException e) {
				}

				try {
					concentrationValue = Double.parseDouble(concentration);
				} catch (NumberFormatException e) {
					warnings.add(
							concentrationColumn + " value in row " + (i + 1) + " is not valid (" + concentration + ")");
				} catch (NullPointerException e) {
				}

				if (timeValue != null && concentrationValue != null) {
					TimeSeriesPoint point = DataFactory.eINSTANCE.createTimeSeriesPoint();

					point.setTime(timeValue);
					point.setConcentration(concentrationValue);
					series.getPoints().add(point);
				}
			}
		}

		if (series != null) {
			list.add(series);
		}

		return list;
	}

	public List<PrimaryModel> getPrimaryModels(PrimaryModelFormula formula, String idColumn, String organismColumn,
			String matrixColumn, List<String> conditionColumns, List<NameableWithUnit> units,
			Map<String, String> paramColumns) throws XlsException {
		if (s == null) {
			throw new XlsException("No sheet specified");
		}

		warnings.clear();

		Map<String, NameableWithUnit> unitsByName = PmmUtils.getByName(units);
		List<PrimaryModel> list = new ArrayList<>();
		Map<String, Integer> columns = getColumns(s);

		for (int i = s.getFirstRowNum() + 1; i <= s.getLastRowNum(); i++) {
			Row row = s.getRow(i);

			String id = getData(getCell(row, columns, idColumn));
			String organism = getData(getCell(row, columns, organismColumn));
			String matrix = getData(getCell(row, columns, matrixColumn));

			TimeSeries series = DataFactory.eINSTANCE.createTimeSeries();

			series.setName(id);
			series.setOrganism(organism);
			series.setMatrix(matrix);

			for (String column : conditionColumns) {
				Condition condition = DataFactory.eINSTANCE.createCondition();
				String s = getData(getCell(row, columns, column));

				try {
					condition.setValue(Double.parseDouble(s));
				} catch (NumberFormatException e) {
					warnings.add(column + " value in row " + (i + 1) + " is not valid (" + s + ")");
				} catch (NullPointerException e) {
				}

				condition.setName(PmmUtils.createMathSymbol(column));
				condition.setUnit(unitsByName.get(column).getUnit());
				series.getConditions().add(condition);
			}

			PrimaryModel model = ModelsFactory.eINSTANCE.createPrimaryModel();

			model.setName(id);
			model.setFormula(formula);
			model.setData(series);
			model.getAssignments().put(formula.getDepVar().getName(), PmmUtils.CONCENTRATION);
			model.getAssignments().put(formula.getIndepVar().getName(), PmmUtils.TIME);

			for (Map.Entry<String, String> entry : paramColumns.entrySet()) {
				ParameterValue value = ModelsFactory.eINSTANCE.createParameterValue();
				String s = getData(getCell(row, columns, entry.getValue()));

				try {
					value.setValue(Double.parseDouble(s));
				} catch (NumberFormatException e) {
					warnings.add(entry.getValue() + " value in row " + (i + 1) + " is not valid (" + s + ")");
				} catch (NullPointerException e) {
				}

				model.getParamValues().put(entry.getKey(), value);
			}

			PmmUtils.setId(series);
			PmmUtils.setId(model);
			list.add(model);
		}

		return list;
	}

	public List<TertiaryModel> getTertiaryModels(TertiaryModelFormula formula, String idColumn, String organismColumn,
			String matrixColumn, Map<String, String> paramColumns) throws XlsException {
		if (s == null) {
			throw new XlsException("No sheet specified");
		}

		warnings.clear();

		List<TertiaryModel> list = new ArrayList<>();
		Map<String, Integer> columns = getColumns(s);

		for (int i = s.getFirstRowNum() + 1; i <= s.getLastRowNum(); i++) {
			Row row = s.getRow(i);

			String id = getData(getCell(row, columns, idColumn));
			String organism = getData(getCell(row, columns, organismColumn));
			String matrix = getData(getCell(row, columns, matrixColumn));

			TimeSeries series = DataFactory.eINSTANCE.createTimeSeries();

			series.setName(id);
			series.setOrganism(organism);
			series.setMatrix(matrix);

			TertiaryModel model = ModelsFactory.eINSTANCE.createTertiaryModel();

			model.setName(id);
			model.setFormula(formula);
			model.getData().add(series);
			model.getAssignments().put(formula.getDepVar().getName(), PmmUtils.CONCENTRATION);
			model.getAssignments().put(formula.getTimeVar(), PmmUtils.TIME);

			for (Variable var : formula.getIndepVars()) {
				if (!var.getName().equals(formula.getTimeVar())) {
					model.getAssignments().put(var.getName(), var.getName());
				}
			}

			for (Map.Entry<String, String> entry : paramColumns.entrySet()) {
				ParameterValue value = ModelsFactory.eINSTANCE.createParameterValue();
				String s = getData(getCell(row, columns, entry.getValue()));

				try {
					value.setValue(Double.parseDouble(s));
				} catch (NumberFormatException e) {
					warnings.add(entry.getValue() + " value in row " + (i + 1) + " is not valid (" + s + ")");
				} catch (NullPointerException e) {
				}

				model.getParamValues().put(entry.getKey(), value);
			}

			PmmUtils.setId(series);
			PmmUtils.setId(model);
			list.add(model);
		}

		return list;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public List<String> getSheets() throws XlsException {
		if (wb == null) {
			throw new XlsException("No file specified");
		}

		List<String> sheets = new ArrayList<>();

		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			sheets.add(wb.getSheetName(i));
		}

		return sheets;
	}

	public List<String> getColumns() throws XlsException {
		if (s == null) {
			throw new XlsException("No sheet specified");
		}

		return new ArrayList<>(getColumns(s).keySet());
	}

	private Map<String, Integer> getColumns(Sheet sheet) {
		Map<String, Integer> columns = new LinkedHashMap<>();
		Row firstRow = sheet.getRow(sheet.getFirstRowNum());

		if (firstRow == null) {
			return columns;
		}

		for (int i = firstRow.getFirstCellNum(); i <= firstRow.getLastCellNum(); i++) {
			String name = getData(firstRow.getCell(i));

			if (name != null) {
				columns.put(name, i);
			}
		}

		return columns;
	}

	private String getData(Cell cell) {
		if (cell == null) {
			return null;
		}

		if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
			CellValue value = wb.getCreationHelper().createFormulaEvaluator().evaluate(cell);

			switch (value.getCellType()) {
			case Cell.CELL_TYPE_BOOLEAN:
				return String.valueOf(value.getBooleanValue());
			case Cell.CELL_TYPE_NUMERIC:
				return String.valueOf(value.getNumberValue());
			case Cell.CELL_TYPE_STRING:
				return Strings.emptyToNull(Strings.nullToEmpty(value.getStringValue()).trim());
			default:
				return null;
			}
		} else {
			return Strings.emptyToNull(cell.toString().trim());
		}
	}

	private static Cell getCell(Row row, Map<String, Integer> columns, String column) {
		if (row == null || !columns.containsKey(column)) {
			return null;
		}

		return row.getCell(columns.get(column));
	}
}
