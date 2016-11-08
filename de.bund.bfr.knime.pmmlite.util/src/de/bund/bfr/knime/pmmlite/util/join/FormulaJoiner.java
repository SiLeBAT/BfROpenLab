/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import de.bund.bfr.knime.pmmlite.core.CombineUtils;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.UnitException;
import de.bund.bfr.knime.pmmlite.core.XmlUtils;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.TertiaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.port.IncompatibleObjectException;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;

public class FormulaJoiner implements Joiner {

	private List<PrimaryModelFormula> primaryFormulas;
	private List<SecondaryModelFormula> secondaryFormulas;

	private Map<String, Map<String, JComboBox<String>>> comboBoxes;

	public FormulaJoiner(PmmPortObject in1, PmmPortObject in2) {
		primaryFormulas = in1.getData(PrimaryModelFormula.class);
		secondaryFormulas = in2.getData(SecondaryModelFormula.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public JComponent createPanel(String assignments) {
		Map<String, Map<String, String>> assignmentsMap = (Map<String, Map<String, String>>) XmlUtils
				.fromXml(assignments);
		JPanel panel = new JPanel();

		comboBoxes = new LinkedHashMap<>();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		List<String> secNames = new ArrayList<>();

		secNames.add(null);
		secNames.addAll(PmmUtils.getNames(secondaryFormulas));

		for (PrimaryModelFormula primFormula : primaryFormulas) {
			Map<String, String> formulaAssignments = assignmentsMap != null ? assignmentsMap.get(primFormula.getId())
					: null;
			JPanel modelPanel = new JPanel();
			Map<String, JComboBox<String>> boxes = new LinkedHashMap<>();

			modelPanel.setBorder(BorderFactory.createTitledBorder(primFormula.getName()));
			modelPanel.setLayout(new BoxLayout(modelPanel, BoxLayout.X_AXIS));

			for (String param : PmmUtils.getNames(primFormula.getParams())) {
				JComboBox<String> box = new JComboBox<>(new Vector<>(secNames));

				box.setSelectedItem(formulaAssignments != null ? formulaAssignments.get(param) : null);

				modelPanel.add(Box.createHorizontalStrut(5));
				modelPanel.add(new JLabel(param + ":"));
				modelPanel.add(box);
				boxes.put(param, box);
			}

			modelPanel.remove(0);
			panel.add(modelPanel);
			comboBoxes.put(primFormula.getId(), boxes);
		}

		return new JScrollPane(UI.createNorthPanel(panel));
	}

	@Override
	public String getAssignments() {
		Map<String, Map<String, String>> assignmentsMap = new LinkedHashMap<>();

		for (Map.Entry<String, Map<String, JComboBox<String>>> entry : comboBoxes.entrySet()) {
			Map<String, String> modelAssignments = new LinkedHashMap<>();

			for (Map.Entry<String, JComboBox<String>> box : entry.getValue().entrySet()) {
				if (box.getValue().getSelectedItem() != null) {
					modelAssignments.put(box.getKey(), (String) box.getValue().getSelectedItem());
				}
			}

			assignmentsMap.put(entry.getKey(), modelAssignments);
		}

		return XmlUtils.toXml(assignmentsMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public PmmPortObject getOutput(String assignments) throws UnitException, IncompatibleObjectException {
		Map<String, Map<String, String>> assignmentsMap = (Map<String, Map<String, String>>) XmlUtils
				.fromXml(assignments);
		List<TertiaryModelFormula> result = new ArrayList<>();
		Map<String, PrimaryModelFormula> primFormulasById = PmmUtils.getById(primaryFormulas);
		Map<String, SecondaryModelFormula> secFormulasByName = PmmUtils.getByName(secondaryFormulas);

		loop: for (Map.Entry<String, Map<String, String>> entry : assignmentsMap.entrySet()) {
			PrimaryModelFormula primFormula = primFormulasById.get(entry.getKey());
			List<SecondaryModelFormula> secFormulas = new ArrayList<>();
			Map<String, String> assignedFormulas = new LinkedHashMap<>();

			if (primFormula == null) {
				continue loop;
			}

			for (Map.Entry<String, String> assign : entry.getValue().entrySet()) {
				SecondaryModelFormula secFormula = secFormulasByName.get(assign.getValue());

				if (secFormula == null) {
					continue loop;
				}

				secFormulas.add(secFormula);
				assignedFormulas.put(assign.getKey(), secFormula.getId());
			}

			result.add(CombineUtils.combine(primFormula, secFormulas, assignedFormulas, null));
		}

		return PmmPortObject.createListObject(result, PmmPortObjectSpec.TERTIARY_MODEL_FORMULA_TYPE);
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	public static PmmPortObjectSpec getOutputType(PmmPortObjectSpec in1, PmmPortObjectSpec in2)
			throws InvalidSettingsException {
		if (in1 == PmmPortObjectSpec.PRIMARY_MODEL_FORMULA_TYPE
				&& in2 == PmmPortObjectSpec.SECONDARY_MODEL_FORMULA_TYPE) {
			return PmmPortObjectSpec.TERTIARY_MODEL_FORMULA_TYPE;
		} else if (in2 == PmmPortObjectSpec.PRIMARY_MODEL_FORMULA_TYPE
				&& in1 == PmmPortObjectSpec.SECONDARY_MODEL_FORMULA_TYPE) {
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
}
