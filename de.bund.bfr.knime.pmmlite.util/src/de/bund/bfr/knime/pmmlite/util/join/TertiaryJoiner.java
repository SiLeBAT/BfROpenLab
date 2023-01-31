/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.util.join;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.knime.core.node.InvalidSettingsException;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.UnitException;
import de.bund.bfr.knime.pmmlite.core.XmlUtils;
import de.bund.bfr.knime.pmmlite.core.data.Condition;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.core.models.ModelsFactory;
import de.bund.bfr.knime.pmmlite.core.models.TertiaryModel;
import de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.Variable;
import de.bund.bfr.knime.pmmlite.core.port.IncompatibleObjectException;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;

public class TertiaryJoiner implements Joiner {

	private List<TertiaryModelFormula> formulas;
	private List<TimeSeries> data;

	private Set<String> modelIds;
	private Map<String, String> modelNames;
	private Map<String, String> modelFormulas;
	private Map<String, String> dependentVariables;
	private Map<String, String> timeVariables;
	private Map<String, List<String>> independentVariables;
	private List<String> independentParameters;

	private Map<String, Map<String, JComboBox<String>>> comboBoxes;

	public TertiaryJoiner(PmmPortObject in1, PmmPortObject in2) {
		this.formulas = in1.getData(TertiaryModelFormula.class);
		this.data = in2.getData(TimeSeries.class);

		readModelTable();
		readDataTable();
	}

	@SuppressWarnings("unchecked")
	@Override
	public JComponent createPanel(String assignments) {
		Map<String, Map<String, String>> assignmentsMap = (Map<String, Map<String, String>>) XmlUtils
				.fromXml(assignments);
		JPanel panel = new JPanel();
		JPanel topPanel = new JPanel();

		comboBoxes = new LinkedHashMap<>();
		panel.setLayout(new BorderLayout());
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

		for (String modelId : modelIds) {
			JPanel modelPanel = new JPanel();

			modelPanel.setBorder(BorderFactory.createTitledBorder(modelNames.get(modelId)));
			modelPanel.setLayout(new BorderLayout());
			modelPanel.setToolTipText(modelFormulas.get(modelId));
			modelPanel.add(getAssignmentPanel(modelId, assignments != null ? assignmentsMap.get(modelId) : null),
					BorderLayout.CENTER);
			topPanel.add(modelPanel);
		}

		panel.add(topPanel, BorderLayout.NORTH);

		return new JScrollPane(panel);
	}

	@Override
	public String getAssignments() {
		Map<String, Map<String, String>> assignmentsMap = new LinkedHashMap<>();

		for (Map.Entry<String, Map<String, JComboBox<String>>> entry : comboBoxes.entrySet()) {
			Map<String, String> modelAssignments = new LinkedHashMap<>();

			for (Map.Entry<String, JComboBox<String>> box : entry.getValue().entrySet()) {
				modelAssignments.put(box.getKey(), (String) box.getValue().getSelectedItem());
			}

			assignmentsMap.put(entry.getKey(), modelAssignments);
		}

		return XmlUtils.toXml(assignmentsMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public PmmPortObject getOutput(String assignments)
			throws JoinException, UnitException, IncompatibleObjectException {
		Map<String, Map<String, String>> assignmentsMap = (Map<String, Map<String, String>>) XmlUtils
				.fromXml(assignments);
		List<TertiaryModel> result = new ArrayList<>();
		Map<String, TertiaryModelFormula> formulasById = PmmUtils.getById(formulas);

		for (Map.Entry<String, Map<String, String>> entry : assignmentsMap.entrySet()) {
			Map<String, String> assign = entry.getValue();
			TertiaryModelFormula model = formulasById.get(entry.getKey());

			if (model == null) {
				continue;
			}

			TertiaryModel joined = ModelsFactory.eINSTANCE.createTertiaryModel();

			for (Variable indep : model.getIndepVars()) {
				if (assign.get(indep.getName()) == null) {
					throw new JoinException(indep.getName() + " is unassigned");
				}
			}

			Variable depVar = model.getDepVar();
			Variable timeVar = PmmUtils.getByName(model.getIndepVars()).get(model.getTimeVar());

			for (TimeSeries series : data) {
				PmmUtils.convertTo(0.0, series.getConcentrationUnit(), depVar.getUnit());
				PmmUtils.convertTo(0.0, series.getTimeUnit(), timeVar.getUnit());

				Map<String, Condition> conditionsByName = PmmUtils.getByName(series.getConditions());

				for (Variable var : model.getIndepVars()) {
					if (var == timeVar) {
						continue;
					}

					Condition cond = conditionsByName.get(assign.get(var.getName()));

					PmmUtils.convertTo(0.0, cond.getUnit(), var.getUnit());
				}

				joined.getData().add(series);
			}

			joined.setFormula(model);
			joined.getAssignments().putAll(assign);
			joined.setName(model.getName() + "_" + joined.getData().get(0).getName() + "_...");
			PmmUtils.setId(joined);
			result.add(joined);
		}

		return PmmPortObject.createListObject(result, PmmPortObjectSpec.TERTIARY_MODEL_TYPE);
	}

	@Override
	public String getErrorMessage() {
		for (Map<String, JComboBox<String>> boxMap : comboBoxes.values()) {
			for (Map.Entry<String, JComboBox<String>> entry : boxMap.entrySet()) {
				if (entry.getValue().getSelectedItem() == null) {
					return "Assignment for " + entry.getKey() + " is missing";
				}
			}
		}

		return null;
	}

	public static PmmPortObjectSpec getOutputType(PmmPortObjectSpec in1, PmmPortObjectSpec in2)
			throws InvalidSettingsException {
		if (in1 == PmmPortObjectSpec.TERTIARY_MODEL_FORMULA_TYPE && in2 == PmmPortObjectSpec.DATA_TYPE) {
			return PmmPortObjectSpec.TERTIARY_MODEL_TYPE;
		} else if (in2 == PmmPortObjectSpec.TERTIARY_MODEL_FORMULA_TYPE && in1 == PmmPortObjectSpec.DATA_TYPE) {
			throw new InvalidSettingsException("Please switch the ports!");
		} else {
			throw new InvalidSettingsException("Wrong input!");
		}
	}

	public static boolean isValidInput(PmmPortObjectSpec in1, PmmPortObjectSpec in2) {
		try {
			getOutputType(in1, in2);
		} catch (InvalidSettingsException e) {
			return false;
		}

		return true;
	}

	private void readModelTable() {
		modelIds = new LinkedHashSet<>();
		modelNames = new LinkedHashMap<>();
		modelFormulas = new LinkedHashMap<>();
		dependentVariables = new LinkedHashMap<>();
		timeVariables = new LinkedHashMap<>();
		independentVariables = new LinkedHashMap<>();

		for (TertiaryModelFormula formula : formulas) {
			modelIds.add(formula.getId());
			modelNames.put(formula.getId(), formula.getName());
			modelFormulas.put(formula.getId(), formula.getExpression());
			dependentVariables.put(formula.getId(), formula.getDepVar().getName());

			List<String> indeps = PmmUtils.getNames(formula.getIndepVars());

			indeps.remove(formula.getTimeVar());
			timeVariables.put(formula.getId(), formula.getTimeVar());
			independentVariables.put(formula.getId(), indeps);
		}
	}

	private void readDataTable() {
		independentParameters = new ArrayList<>();

		for (TimeSeries series : data) {
			for (Condition cond : series.getConditions()) {
				if (!independentParameters.contains(cond.getName())) {
					independentParameters.add(cond.getName());
				}
			}
		}
	}

	private JPanel getAssignmentPanel(String modelID, Map<String, String> assignment) {
		Map<String, JComboBox<String>> boxes = new LinkedHashMap<>();
		JPanel assignmentPanel = new JPanel();

		assignmentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		assignmentPanel.setLayout(new BoxLayout(assignmentPanel, BoxLayout.X_AXIS));

		JComboBox<String> depBox = new JComboBox<>(new String[] { PmmUtils.CONCENTRATION });

		depBox.setSelectedIndex(0);
		boxes.put(dependentVariables.get(modelID), depBox);
		assignmentPanel.add(new JLabel(dependentVariables.get(modelID) + ":"));
		assignmentPanel.add(Box.createHorizontalStrut(5));
		assignmentPanel.add(depBox);

		JComboBox<String> timeBox = new JComboBox<>(new String[] { PmmUtils.TIME });

		timeBox.setSelectedIndex(0);
		boxes.put(timeVariables.get(modelID), timeBox);
		assignmentPanel.add(Box.createHorizontalStrut(5));
		assignmentPanel.add(new JLabel(timeVariables.get(modelID) + ":"));
		assignmentPanel.add(Box.createHorizontalStrut(5));
		assignmentPanel.add(timeBox);

		for (String indepVar : independentVariables.get(modelID)) {
			JComboBox<String> indepBox = new JComboBox<>(new Vector<>(independentParameters));

			indepBox.setSelectedItem(assignment != null ? assignment.get(indepVar) : null);
			boxes.put(indepVar, indepBox);
			assignmentPanel.add(Box.createHorizontalStrut(5));
			assignmentPanel.add(new JLabel(indepVar + ":"));
			assignmentPanel.add(Box.createHorizontalStrut(5));
			assignmentPanel.add(indepBox);
		}

		comboBoxes.put(modelID, boxes);

		return UI.createWestPanel(UI.createCenterPanel(assignmentPanel));
	}
}
