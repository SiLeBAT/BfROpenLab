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
package de.bund.bfr.knime.pmmlite.util.join;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JButton;
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
import de.bund.bfr.knime.pmmlite.core.models.ModelsFactory;
import de.bund.bfr.knime.pmmlite.core.models.Parameter;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModel;
import de.bund.bfr.knime.pmmlite.core.models.SecondaryModel;
import de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.Variable;
import de.bund.bfr.knime.pmmlite.core.port.IncompatibleObjectException;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;

public class SecondaryJoiner implements Joiner, ActionListener {

	private List<SecondaryModelFormula> formulas;
	private List<PrimaryModel> data;

	private Set<String> modelIds;
	private Map<String, String> modelNames;
	private Map<String, String> modelFormulas;
	private Map<String, String> dependentVariables;
	private Map<String, List<String>> independentVariables;
	private List<String> dependentParameters;
	private List<String> independentParameters;

	private Map<String, JPanel> boxPanels;
	private Map<String, JPanel> buttonPanels;
	private Map<String, List<Map<String, JComboBox<String>>>> comboBoxes;
	private Map<String, List<JButton>> addButtons;
	private Map<String, List<JButton>> removeButtons;

	public SecondaryJoiner(PmmPortObject in1, PmmPortObject in2) {
		this.formulas = in1.getData(SecondaryModelFormula.class);
		this.data = in2.getData(PrimaryModel.class);

		readModelTable();
		readDataTable();
	}

	@SuppressWarnings("unchecked")
	@Override
	public JComponent createPanel(String assignments) {
		Map<String, List<Map<String, String>>> assignmentsMap = (Map<String, List<Map<String, String>>>) XmlUtils
				.fromXml(assignments);
		JPanel panel = new JPanel();
		JPanel topPanel = new JPanel();

		if (assignmentsMap != null && assignmentsMap.isEmpty()) {
			for (String modelID : modelIds) {
				List<Map<String, String>> list = new ArrayList<>();

				list.add(new LinkedHashMap<>(0));
				assignmentsMap.put(modelID, list);
			}
		}

		boxPanels = new LinkedHashMap<>();
		buttonPanels = new LinkedHashMap<>();
		comboBoxes = new LinkedHashMap<>();
		addButtons = new LinkedHashMap<>();
		removeButtons = new LinkedHashMap<>();
		panel.setLayout(new BorderLayout());
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

		for (String modelId : modelIds) {
			comboBoxes.put(modelId, new ArrayList<>());
			addButtons.put(modelId, new ArrayList<>());
			removeButtons.put(modelId, new ArrayList<>());
			boxPanels.put(modelId, new JPanel());
			boxPanels.get(modelId).setLayout(new GridLayout(0, 1));
			buttonPanels.put(modelId, new JPanel());
			buttonPanels.get(modelId).setLayout(new GridLayout(0, 1));

			int index = 0;

			if (assignmentsMap != null && assignmentsMap.get(modelId) != null) {
				for (Map<String, String> assignment : assignmentsMap.get(modelId)) {
					addAssignment(modelId, assignment, index);
					index++;
				}
			}

			JButton addButton = new JButton("+");

			addButton.addActionListener(this);
			addButtons.get(modelId).add(addButton);
			boxPanels.get(modelId).add(new JPanel());
			buttonPanels.get(modelId).add(UI.createWestPanel(UI.createBorderPanel(addButton)));

			JPanel modelPanel = new JPanel();

			modelPanel.setBorder(BorderFactory.createTitledBorder(modelNames.get(modelId)));
			modelPanel.setLayout(new BorderLayout());
			modelPanel.setToolTipText(modelFormulas.get(modelId));
			modelPanel.add(boxPanels.get(modelId), BorderLayout.CENTER);
			modelPanel.add(buttonPanels.get(modelId), BorderLayout.EAST);
			topPanel.add(modelPanel);
		}

		panel.add(topPanel, BorderLayout.NORTH);

		return new JScrollPane(panel);
	}

	@Override
	public String getAssignments() {
		Map<String, List<Map<String, String>>> assignmentsMap = new LinkedHashMap<>();

		for (Map.Entry<String, List<Map<String, JComboBox<String>>>> entry : comboBoxes.entrySet()) {
			List<Map<String, String>> modelAssignments = new ArrayList<>();

			for (Map<String, JComboBox<String>> boxes : entry.getValue()) {
				Map<String, String> assignment = new LinkedHashMap<>();

				for (Map.Entry<String, JComboBox<String>> box : boxes.entrySet()) {
					assignment.put(box.getKey(), (String) box.getValue().getSelectedItem());
				}

				modelAssignments.add(assignment);
			}

			assignmentsMap.put(entry.getKey(), modelAssignments);
		}

		return XmlUtils.toXml(assignmentsMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public PmmPortObject getOutput(String assignments)
			throws JoinException, UnitException, IncompatibleObjectException {
		Map<String, List<Map<String, String>>> assignmentsMap = (Map<String, List<Map<String, String>>>) XmlUtils
				.fromXml(assignments);
		List<SecondaryModel> result = new ArrayList<>();
		Map<String, SecondaryModelFormula> formulasById = PmmUtils.getById(formulas);

		for (Map.Entry<String, List<Map<String, String>>> entry : assignmentsMap.entrySet()) {
			for (Map<String, String> assignment : entry.getValue()) {
				SecondaryModelFormula model = formulasById.get(entry.getKey());

				if (model == null) {
					continue;
				}

				SecondaryModel joined = ModelsFactory.eINSTANCE.createSecondaryModel();
				String primModelName = null;

				for (Variable indep : model.getIndepVars()) {
					if (assignment.get(indep.getName()) == null) {
						throw new JoinException(indep.getName() + " is unassigned");
					}
				}

				for (Map.Entry<String, String> assign : assignment.entrySet()) {
					String s = assign.getValue();
					int i0 = s.indexOf(" ");
					int i1 = s.indexOf("(");
					int i2 = s.indexOf(")");

					if (i0 != -1 && i1 != -1 && i2 != -1) {
						primModelName = s.substring(i1 + 1, i2);
						assign.setValue(s.substring(0, i0));
						break;
					}
				}

				for (PrimaryModel primModel : data) {
					if (primModel.getFormula().getName().equals(primModelName)) {
						Map<String, Condition> conditionsByName = PmmUtils
								.getByName(primModel.getData().getConditions());

						for (Variable var : model.getIndepVars()) {
							Condition cond = conditionsByName.get(assignment.get(var.getName()));

							PmmUtils.convertTo(0.0, cond.getUnit(), var.getUnit());
						}

						joined.getData().add(primModel);
					}
				}

				if (joined.getData().isEmpty()) {
					throw new JoinException("No Primary Models");
				}

				joined.setFormula(model);
				joined.getAssignments().putAll(assignment);
				joined.setName(model.getName() + "_" + joined.getData().get(0).getName() + "_...");
				PmmUtils.setId(joined);
				result.add(joined);
			}
		}

		return PmmPortObject.createListObject(result, PmmPortObjectSpec.SECONDARY_MODEL_TYPE);
	}

	@Override
	public String getErrorMessage() {
		for (List<Map<String, JComboBox<String>>> boxMaps : comboBoxes.values()) {
			for (Map<String, JComboBox<String>> boxMap : boxMaps) {
				for (Map.Entry<String, JComboBox<String>> entry : boxMap.entrySet()) {
					if (entry.getValue().getSelectedItem() == null) {
						return "Assignment for " + entry.getKey() + " is missing";
					}
				}
			}
		}

		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton button = (JButton) e.getSource();

		for (Map.Entry<String, List<JButton>> entry : addButtons.entrySet()) {
			if (entry.getValue().contains(button)) {
				int index = entry.getValue().indexOf(button);

				addAssignment(entry.getKey(), new LinkedHashMap<>(), index);
				boxPanels.get(entry.getKey()).revalidate();
				buttonPanels.get(entry.getKey()).revalidate();
				return;
			}
		}

		for (Map.Entry<String, List<JButton>> entry : removeButtons.entrySet()) {
			if (entry.getValue().contains(button)) {
				int index = entry.getValue().indexOf(button);

				addButtons.get(entry.getKey()).remove(index);
				removeButtons.get(entry.getKey()).remove(index);
				comboBoxes.get(entry.getKey()).remove(index);
				boxPanels.get(entry.getKey()).remove(index);
				buttonPanels.get(entry.getKey()).remove(index);
				boxPanels.get(entry.getKey()).revalidate();
				buttonPanels.get(entry.getKey()).revalidate();
				return;
			}
		}
	}

	public static PmmPortObjectSpec getOutputType(PmmPortObjectSpec in1, PmmPortObjectSpec in2)
			throws InvalidSettingsException {
		if (in1 == PmmPortObjectSpec.SECONDARY_MODEL_FORMULA_TYPE && in2 == PmmPortObjectSpec.PRIMARY_MODEL_TYPE) {
			return PmmPortObjectSpec.SECONDARY_MODEL_TYPE;
		} else if (in2 == PmmPortObjectSpec.SECONDARY_MODEL_FORMULA_TYPE
				&& in1 == PmmPortObjectSpec.PRIMARY_MODEL_TYPE) {
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
		independentVariables = new LinkedHashMap<>();

		for (SecondaryModelFormula model : formulas) {
			modelIds.add(model.getId());
			modelNames.put(model.getId(), model.getName());
			modelFormulas.put(model.getId(), model.getExpression());
			dependentVariables.put(model.getId(), model.getDepVar().getName());
			independentVariables.put(model.getId(), PmmUtils.getNames(model.getIndepVars()));
		}
	}

	private void readDataTable() {
		dependentParameters = new ArrayList<>();
		independentParameters = new ArrayList<>();

		for (PrimaryModel primModel : data) {
			for (Parameter param : primModel.getFormula().getParams()) {
				String name = param.getName() + " (" + primModel.getFormula().getName() + ")";

				if (!dependentParameters.contains(name)) {
					dependentParameters.add(name);
				}
			}

			for (Condition cond : primModel.getData().getConditions()) {
				if (!independentParameters.contains(cond.getName())) {
					independentParameters.add(cond.getName());
				}
			}
		}
	}

	private void addAssignment(String modelID, Map<String, String> assignment, int index) {
		Map<String, JComboBox<String>> boxes = new LinkedHashMap<>();
		JPanel assignmentPanel = new JPanel();

		assignmentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		assignmentPanel.setLayout(new BoxLayout(assignmentPanel, BoxLayout.X_AXIS));

		JComboBox<String> depBox = new JComboBox<>(new Vector<>(dependentParameters));

		depBox.setSelectedItem(assignment.get(dependentVariables.get(modelID)));
		boxes.put(dependentVariables.get(modelID), depBox);
		assignmentPanel.add(new JLabel(dependentVariables.get(modelID) + ":"));
		assignmentPanel.add(Box.createHorizontalStrut(5));
		assignmentPanel.add(depBox);

		for (String indepVar : independentVariables.get(modelID)) {
			JComboBox<String> indepBox = new JComboBox<>(new Vector<>(independentParameters));

			indepBox.setSelectedItem(assignment.get(indepVar));
			boxes.put(indepVar, indepBox);
			assignmentPanel.add(Box.createHorizontalStrut(5));
			assignmentPanel.add(new JLabel(indepVar + ":"));
			assignmentPanel.add(Box.createHorizontalStrut(5));
			assignmentPanel.add(indepBox);
		}

		comboBoxes.get(modelID).add(index, boxes);
		boxPanels.get(modelID).add(UI.createWestPanel(UI.createCenterPanel(assignmentPanel)), index);

		JButton addButton = new JButton("+");
		JButton removeButton = new JButton("-");

		addButton.addActionListener(this);
		removeButton.addActionListener(this);
		addButtons.get(modelID).add(index, addButton);
		removeButtons.get(modelID).add(index, removeButton);
		buttonPanels.get(modelID).add(UI.createWestPanel(UI.createHorizontalPanel(addButton, removeButton)), index);
	}
}
