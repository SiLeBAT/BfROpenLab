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
package de.bund.bfr.knime.pmmlite.io.combasereader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.util.compilers.UnitException;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Doubles;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.pmmlite.core.PmmUnit;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.data.Condition;
import de.bund.bfr.knime.pmmlite.core.data.DataFactory;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeriesPoint;
import de.bund.bfr.knime.pmmlite.core.models.ModelsFactory;
import de.bund.bfr.knime.pmmlite.core.models.ParameterValue;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModel;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula;
import de.bund.bfr.knime.pmmlite.io.DefaultModels;
import de.bund.bfr.math.Transform;

public class CombaseReader {

	private static final ImmutableMap<String, PmmUnit> unitMap = new ImmutableMap.Builder<String, PmmUnit>()
			.put(PmmUtils.TIME, new PmmUnit.Builder().addUnit(Unit.Kind.SECOND, 3600.0, 0, 1.0).build())
			.put(PmmUtils.CONCENTRATION,
					new PmmUnit.Builder().addUnit(Unit.Kind.ITEM).addUnit(Unit.Kind.GRAM, 1.0, 0, -1.0).build())
			.put("°C", new PmmUnit.Builder().transform(Transform.KELVIN_TO_CELSIUS).addUnit(Unit.Kind.KELVIN).build())
			.put("%", new PmmUnit.Builder().addUnit(Unit.Kind.ITEM).addUnit(Unit.Kind.ITEM, 1.0, 2, -1.0).build())
			.put("ppm", new PmmUnit.Builder().addUnit(Unit.Kind.ITEM).addUnit(Unit.Kind.ITEM, 1.0, 6, -1.0).build())
			.put("g/l", new PmmUnit.Builder().addUnit(Unit.Kind.GRAM).addUnit(Unit.Kind.LITRE, 1.0, 0, -1.0).build())
			.put("kGy", new PmmUnit.Builder().addUnit(Unit.Kind.GRAY, 1.0, 3, 1.0).build())
			.put("kGy/h",
					new PmmUnit.Builder().addUnit(Unit.Kind.GRAY, 1.0, 3, 1.0)
							.addUnit(Unit.Kind.SECOND, 3600.0, 0, -1.0).build())
			.put("MPA", new PmmUnit.Builder().addUnit(Unit.Kind.PASCAL, 1.0, 6, 1.0).build()).build();

	private boolean treatNotDetectedAsZero;

	private List<TimeSeries> data;
	private List<PrimaryModel> models;

	private transient TimeSeries currentData;
	private transient PrimaryModel currentModel;
	private transient boolean isCurrentlyReadingData;

	public CombaseReader(String fileName, boolean treatNotDetectedAsZero, ExecutionContext exec)
			throws IOException, CanceledExecutionException {
		this.treatNotDetectedAsZero = treatNotDetectedAsZero;
		data = new ArrayList<>();
		models = new ArrayList<>();

		File f = KnimeUtils.getFile(fileName);
		long lineCount;

		try (Stream<String> stream = Files.lines(f.toPath())) {
			lineCount = stream.count();
		}

		int index = 0;

		resetCurrentDataAndModel();

		try (Stream<String> stream = Files.lines(f.toPath())) {
			for (String line : (Iterable<String>) stream::iterator) {
				exec.checkCanceled();
				exec.setProgress((double) index++ / (double) lineCount);

				if (line.isEmpty()) {
					addCurrentDataAndModel();
					resetCurrentDataAndModel();
				} else {
					readLine(line);
				}
			}

			addCurrentDataAndModel();
		}
	}

	public List<TimeSeries> getData() {
		return data;
	}

	public List<PrimaryModel> getModels() {
		return models;
	}

	private void resetCurrentDataAndModel() {
		currentData = DataFactory.eINSTANCE.createTimeSeries();
		currentData.setTimeUnit(toPmmUnit(PmmUtils.TIME));
		currentData.setConcentrationUnit(toPmmUnit(PmmUtils.CONCENTRATION));
		currentModel = null;
		isCurrentlyReadingData = false;
	}

	private void addCurrentDataAndModel() {
		if (currentData != null && !currentData.getPoints().isEmpty()) {
			PmmUtils.setId(currentData);
			data.add(currentData);
		} else if (currentModel != null) {
			PmmUtils.setId(currentData);
			PmmUtils.setId(currentModel);
			models.add(currentModel);
		}
	}

	private void readLine(String line) {
		if (!line.contains(",")) {
			return;
		}

		String type = line.substring(0, line.indexOf(",")).replaceAll("\\(.+\\)", "").replace(":", "").trim();
		String data = line.substring(line.indexOf(",") + 1).trim();

		if (isCurrentlyReadingData) {
			if (!data.contains(",")) {
				return;
			}

			Double time = parse(data.substring(0, data.indexOf(",")).trim());
			Double concentration = parse(data.substring(data.indexOf(",") + 1).trim());

			if (time != null && concentration != null) {
				TimeSeriesPoint p = DataFactory.eINSTANCE.createTimeSeriesPoint();

				p.setTime(time);
				p.setConcentration(concentration);
				currentData.getPoints().add(p);
			}
		} else {
			switch (type) {
			case "ComBase ID":
				currentData.setName(data);
				break;
			case "Organism":
				currentData.setOrganism(data);
				break;
			case "Matrix":
				currentData.setMatrix(data);
				break;
			case "Temperature":
				addTemperatureCondition(currentData, data, "°C");
				break;
			case "pH":
				addPhCondition(currentData, data);
				break;
			case "Aw":
				addWaterActivityCondition(currentData, data);
				break;
			case "Conditions":
				currentData.getConditions().addAll(readConditions(data));
				break;
			case "Max.rate":
				currentModel = createLinearModel(data, currentData);
				break;
			case "Logcs":
				isCurrentlyReadingData = true;
				break;
			default:
				throw new RuntimeException("Unknown parameter in Combase file: " + type);
			}
		}
	}

	private Double parse(String expression) {
		String s = expression.trim();

		if (s.isEmpty() || s.equals("Not available")) {
			return null;
		}

		if (s.equals("No growth")) {
			return 0.0;
		}

		if (s.equals("N/D")) {
			if (treatNotDetectedAsZero) {
				return 0.0;
			} else {
				return null;
			}
		}

		try {
			return Double.parseDouble(s.replaceAll("[^-\\d\\.]", ""));
		} catch (NumberFormatException e) {
			return Doubles.tryParse(s);
		}
	}

	private List<Condition> readConditions(String condString) {
		List<Condition> result = new ArrayList<>();

		for (String s : condString.split(";")) {
			int valueSep = s.indexOf(':');
			String name = null;
			Double value = null;
			PmmUnit unit = new PmmUnit.Builder().build();

			if (valueSep != -1) {
				String valueString = s.substring(valueSep + 1).trim();

				name = s.substring(0, valueSep).trim();

				if (valueString.charAt(valueString.length() - 1) == ')') {
					int unitSep = valueString.lastIndexOf('(');
					String unitString = valueString.substring(unitSep + 1, valueString.length() - 1).trim();

					unit = toPmmUnit(unitString);
					valueString = valueString.replace(unitString, "").trim();
				}

				value = parse(valueString);
			} else {
				name = s;
				value = 1.0;
			}

			if (value != null) {
				Condition cond = DataFactory.eINSTANCE.createCondition();

				cond.setName(PmmUtils.createMathSymbol(name));
				cond.setValue(value);
				cond.setUnit(unit);
				result.add(cond);
			}
		}

		return result;
	}

	private void addTemperatureCondition(TimeSeries series, String s, String unitName) {
		Double value = parse(s);

		if (value != null) {
			PmmUnit unit = toPmmUnit(unitName);
			Condition cond = DataFactory.eINSTANCE.createCondition();

			cond.setName(PmmUtils.createMathSymbol("Temperature"));
			cond.setValue(value);
			cond.setUnit(unit);
			series.getConditions().add(cond);
		}
	}

	private void addPhCondition(TimeSeries series, String s) {
		Double value = parse(s);

		if (value != null) {
			Condition cond = DataFactory.eINSTANCE.createCondition();

			cond.setName(PmmUtils.createMathSymbol("pH"));
			cond.setValue(value);
			series.getConditions().add(cond);
		}
	}

	private void addWaterActivityCondition(TimeSeries series, String s) {
		Double value = parse(s);

		if (value != null) {
			Condition cond = DataFactory.eINSTANCE.createCondition();

			cond.setName(PmmUtils.createMathSymbol("Aw"));
			cond.setValue(value);
			series.getConditions().add(cond);
		}
	}

	private PrimaryModel createLinearModel(String s, TimeSeries series) {
		if (s.equals("Fit data")) {
			return null;
		}

		PrimaryModel model = ModelsFactory.eINSTANCE.createPrimaryModel();
		PrimaryModelFormula formula = DefaultModels.getInstance().getLinearModel();

		model.setFormula(formula);
		model.setData(series);
		model.getAssignments().put(formula.getDepVar().getName(), PmmUtils.CONCENTRATION);
		model.getAssignments().put(formula.getIndepVar().getName(), PmmUtils.TIME);

		ParameterValue y0Value = ModelsFactory.eINSTANCE.createParameterValue();
		ParameterValue mValue = ModelsFactory.eINSTANCE.createParameterValue();

		mValue.setValue(parse(s));
		model.getParamValues().put("y0", y0Value);
		model.getParamValues().put("m", mValue);

		return model;
	}

	private static PmmUnit toPmmUnit(String s) {
		if (!unitMap.containsKey(s)) {
			throw new UnitException("Invalid unit: " + s);
		}

		return unitMap.get(s);
	}
}
