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
package de.bund.bfr.knime.pmmlite.io.formulacreator;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.common.Nameable;
import de.bund.bfr.knime.pmmlite.core.models.ModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.ModelsFactory;
import de.bund.bfr.knime.pmmlite.core.models.Parameter;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.SecondaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.Variable;
import de.bund.bfr.knime.pmmlite.io.DefaultModels;
import de.bund.bfr.knime.pmmlite.io.ParamTable;
import de.bund.bfr.knime.pmmlite.io.UnitTable;
import de.bund.bfr.knime.pmmlite.io.formulacreator.FormulaCreatorSettings.FormulaType;
import de.bund.bfr.knime.ui.Dialogs;
import de.bund.bfr.knime.ui.StringTextArea;
import de.bund.bfr.knime.ui.StringTextField;
import de.bund.bfr.knime.ui.TextInput;
import de.bund.bfr.knime.ui.TextListener;
import de.bund.bfr.math.MathUtils;
import de.bund.bfr.math.Transform;

/**
 * <code>NodeDialog</code> for the "FormulaCreator" Node.
 * 
 * @author Christian Thoens
 */
public class FormulaCreatorNodeDialog extends NodeDialogPane implements ItemListener, TextListener {

	private static final String NEW_FORMULA = "New Formula";

	private static Random random = new Random();

	private boolean nameChosen;

	private FormulaCreatorSettings set;
	private Map<String, Variable> usedIndeps;
	private Map<String, Parameter> usedParameters;
	private String primInitParam;

	private JPanel mainPanel;
	private JPanel modelPanel;

	private JComboBox<FormulaType> typeBox;
	private JComboBox<String> modelBox;
	private StringTextField depVarField;
	private StringTextArea formulaField;
	private JComboBox<String> primIndepBox;
	private JComboBox<String> primInitBox;
	private JComboBox<Transform> transformBox;
	private List<JCheckBox> secIndepBoxes;
	private UnitTable<Variable> unitTable;
	private ParamTable paramTable;

	/**
	 * New pane for configuring the FormulaCreator node.
	 */
	protected FormulaCreatorNodeDialog() {
		set = new FormulaCreatorSettings();
		secIndepBoxes = new ArrayList<>();
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		resetUsedIndepsAndParameters();
		updateModelPanel();
		addTab("Options", mainPanel);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
		set.load(settings);
		typeBox.setSelectedItem(set.getModelType());
		resetUsedIndepsAndParameters();
		updateModelPanel();
		nameChosen = false;
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		if (paramTable.isEditing()) {
			paramTable.getCellEditor().stopCellEditing();
		}

		if (unitTable.isEditing()) {
			unitTable.getCellEditor().stopCellEditing();
		}

		if (!depVarField.isValueValid()) {
			throw new InvalidSettingsException("Dependent Variable Missing");
		}

		if (set.getFormula() == null || set.getFormula().getExpression() == null) {
			throw new InvalidSettingsException("Formula Missing");
		}

		if (MathUtils.getSymbols(set.getFormula().getExpression()).isEmpty()) {
			throw new InvalidSettingsException("Formula Invalid");
		}

		List<String> paramNames = PmmUtils.getNames(set.getFormula().getParams());

		if (paramNames.contains(PmmUtils.TIME) || paramNames.contains(PmmUtils.CONCENTRATION)) {
			throw new InvalidSettingsException("\"" + PmmUtils.TIME + "\" and \"" + PmmUtils.CONCENTRATION
					+ "\" are not allowed as parameter names");
		}

		if (set.getModelType() == FormulaType.PRIMARY_TYPE) {
			PrimaryModelFormula primFormula = (PrimaryModelFormula) set.getFormula();

			if (primFormula.getIndepVar().getName().equals(PmmUtils.CONCENTRATION)) {
				throw new InvalidSettingsException(
						"Independent variable of primary model cannot be named \"" + PmmUtils.CONCENTRATION + "\"");
			}

			if (primFormula.getDepVar().getName().equals(PmmUtils.TIME)) {
				throw new InvalidSettingsException(
						"Dependent variable of primary model cannot be named \"" + PmmUtils.TIME + "\"");
			}
		} else if (set.getModelType() == FormulaType.SECONDARY_TYPE) {
			SecondaryModelFormula secFormula = (SecondaryModelFormula) set.getFormula();
			List<String> varNames = PmmUtils.getNames(secFormula.getIndepVars());

			if (varNames.contains(PmmUtils.TIME) || varNames.contains(PmmUtils.CONCENTRATION)) {
				throw new InvalidSettingsException("Independent variable of secondary model cannot be named\""
						+ PmmUtils.TIME + "\" or \"" + PmmUtils.CONCENTRATION + "\"");
			}

			if (secFormula.getDepVar().getName().equals(PmmUtils.TIME)
					|| secFormula.getDepVar().getName().equals(PmmUtils.CONCENTRATION)) {
				throw new InvalidSettingsException("Dependent variable of secondary model cannot be named\""
						+ PmmUtils.TIME + "\" or \"" + PmmUtils.CONCENTRATION + "\"");
			}
		}

		set.getFormula().setExpression(set.getFormula().getExpression().replaceAll("\\s", ""));

		if (!nameChosen) {
			set.setFormula(EcoreUtil.copy(set.getFormula()));

			String name = Strings.nullToEmpty(Dialogs.showInputDialog(mainPanel, "Choose Unique Formula Name:",
					"Formula", set.getFormula().getName())).trim();

			if (!name.isEmpty()) {
				set.getFormula().setName(name);
				PmmUtils.setId(set.getFormula());
			} else {
				set.getFormula().setName("Formula" + random.nextInt());
				PmmUtils.setId(set.getFormula());
			}

			nameChosen = true;
		}

		set.save(settings);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == typeBox && e.getStateChange() == ItemEvent.SELECTED) {
			set.setModelType((FormulaType) typeBox.getSelectedItem());
			resetUsedIndepsAndParameters();
			updateModelPanel();
		} else if (e.getSource() == modelBox && e.getStateChange() == ItemEvent.SELECTED) {
			if (set.getModelType() == FormulaType.PRIMARY_TYPE) {
				set.setFormula(PmmUtils.getByName(DefaultModels.getInstance().getPrimaryModels())
						.get(modelBox.getSelectedItem()));
			} else if (set.getModelType() == FormulaType.SECONDARY_TYPE) {
				set.setFormula(PmmUtils.getByName(DefaultModels.getInstance().getSecondaryModels())
						.get(modelBox.getSelectedItem()));
			}

			resetUsedIndepsAndParameters();
			updateModelPanel();
		} else if (e.getSource() == primIndepBox && e.getStateChange() == ItemEvent.SELECTED) {
			Variable indep = ((PrimaryModelFormula) set.getFormula()).getIndepVar();

			indep.setName((String) primIndepBox.getSelectedItem());
			usedIndeps = PmmUtils.getByName(Arrays.asList(indep));
			updatePrimaryFormula();
			updateModelPanel();
		} else if (e.getSource() == primInitBox && e.getStateChange() == ItemEvent.SELECTED) {
			((PrimaryModelFormula) set.getFormula()).setInitialParam((String) primInitBox.getSelectedItem());
		} else if (e.getSource() == transformBox && e.getStateChange() == ItemEvent.SELECTED) {
			((SecondaryModelFormula) set.getFormula()).setTransformation((Transform) transformBox.getSelectedItem());
			updateModelPanel();
		} else if (secIndepBoxes.contains(e.getSource())) {
			SecondaryModelFormula formula = (SecondaryModelFormula) set.getFormula();
			String name = ((JCheckBox) e.getSource()).getText();

			if (e.getStateChange() == ItemEvent.SELECTED) {
				Variable indep = ModelsFactory.eINSTANCE.createVariable();

				indep.setName(name);
				formula.getIndepVars().add(indep);
				usedIndeps.put(name, indep);
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				formula.getIndepVars().remove(PmmUtils.getByName(formula.getIndepVars()).get(name));
				usedIndeps.remove(name);
			}

			updateSecondaryFormula();
			updateModelPanel();
		}
	}

	@Override
	public void textChanged(TextInput source) {
		ModelFormula formula = set.getFormula();

		if (source == depVarField) {
			if (depVarField.getValue() != null) {
				Variable depVar = ModelsFactory.eINSTANCE.createVariable();

				depVar.setName(depVarField.getValue());
				formula.setDepVar(depVar);
			} else {
				formula.setDepVar(null);
			}
		} else if (source == formulaField) {
			set.getFormula().setExpression(formulaField.getValue());

			if (set.getModelType() == FormulaType.PRIMARY_TYPE) {
				updatePrimaryFormula();
			} else if (set.getModelType() == FormulaType.SECONDARY_TYPE) {
				updateSecondaryFormula();
			}

			updateModelPanel();
			formulaField.requestFocus();
		}
	}

	private void resetUsedIndepsAndParameters() {
		if (set.getModelType() == FormulaType.PRIMARY_TYPE && set.getFormula() instanceof PrimaryModelFormula) {
			PrimaryModelFormula formula = (PrimaryModelFormula) set.getFormula();

			usedIndeps = PmmUtils.getByName(Arrays.asList(formula.getIndepVar()));
			usedParameters = PmmUtils.getByName(formula.getParams());
			primInitParam = formula.getInitialParam();
		} else if (set.getModelType() == FormulaType.SECONDARY_TYPE
				&& set.getFormula() instanceof SecondaryModelFormula) {
			SecondaryModelFormula formula = (SecondaryModelFormula) set.getFormula();

			usedIndeps = PmmUtils.getByName(formula.getIndepVars());
			usedParameters = PmmUtils.getByName(formula.getParams());
			primInitParam = null;
		} else {
			usedIndeps = new LinkedHashMap<>();
			usedParameters = new LinkedHashMap<>();
			primInitParam = null;
		}
	}

	private void updateModelPanel() {
		typeBox = new JComboBox<>(FormulaType.values());
		typeBox.setSelectedItem(set.getModelType());
		typeBox.addItemListener(this);
		modelBox = new JComboBox<>();
		modelBox.addItem(NEW_FORMULA);
		modelBox.setSelectedItem(NEW_FORMULA);

		JPanel editPanel = null;

		if (set.getModelType() == FormulaType.PRIMARY_TYPE) {
			for (PrimaryModelFormula formula : DefaultModels.getInstance().getPrimaryModels()) {
				modelBox.addItem(formula.getName());

				if (EcoreUtil.equals(formula, set.getFormula())) {
					modelBox.setSelectedItem(formula.getName());
				}
			}

			editPanel = createPrimaryEditPanel();
		} else if (set.getModelType() == FormulaType.SECONDARY_TYPE) {
			for (SecondaryModelFormula formula : DefaultModels.getInstance().getSecondaryModels()) {
				modelBox.addItem(formula.getName());

				if (EcoreUtil.equals(formula, set.getFormula())) {
					modelBox.setSelectedItem(formula.getName());
				}
			}

			editPanel = createSecondaryEditPanel();
		}

		modelBox.addItemListener(this);

		if (modelPanel != null) {
			mainPanel.remove(modelPanel);
		}

		modelPanel = new JPanel();
		modelPanel.setLayout(new BoxLayout(modelPanel, BoxLayout.Y_AXIS));
		modelPanel.add(UI.createTitledPanel(
				UI.createWestPanel(
						UI.createHorizontalPanel(new JLabel("Type:"), typeBox, new JLabel("Formula:"), modelBox)),
				"Choose Formula"));
		modelPanel.add(editPanel);

		mainPanel.add(modelPanel, BorderLayout.NORTH);
		mainPanel.revalidate();
		mainPanel.repaint();
	}

	private JPanel createPrimaryEditPanel() {
		if (set.getFormula() == null) {
			set.setFormula(ModelsFactory.eINSTANCE.createPrimaryModelFormula());
		}

		PrimaryModelFormula formula = (PrimaryModelFormula) set.getFormula();

		depVarField = new StringTextField(false, 16);
		primIndepBox = new JComboBox<>(new Vector<>(MathUtils.getSymbols(formula.getExpression())));
		primInitBox = new JComboBox<>(new Vector<>(PmmUtils.getNames(formula.getParams())));
		unitTable = new UnitTable<>(PmmUtils.getVariables(formula));
		paramTable = new ParamTable(formula.getParams());

		if (formula.getDepVar() != null) {
			depVarField.setValue(formula.getDepVar().getName());
		}

		if (formula.getIndepVar() != null) {
			primIndepBox.setSelectedItem(formula.getIndepVar().getName());
		}

		if (formula.getInitialParam() != null) {
			primInitBox.setSelectedItem(formula.getInitialParam());
		}

		depVarField.addTextListener(this);
		primIndepBox.addItemListener(this);
		primInitBox.addItemListener(this);

		if (formulaField == null || !Objects.equals(formulaField.getValue(), set.getFormula().getExpression())) {
			formulaField = new StringTextArea(false, 3, 100);
			formulaField.setValue(formula.getExpression());
			formulaField.addTextListener(this);
		}

		JPanel formulaPanel = new JPanel();

		formulaPanel.setLayout(new BoxLayout(formulaPanel, BoxLayout.X_AXIS));
		formulaPanel.add(UI.createCenterPanel(depVarField));
		formulaPanel.add(new JLabel("="));
		formulaPanel.add(formulaField);

		JPanel editPanel = new JPanel();
		int row = 0;

		editPanel.setLayout(new GridBagLayout());
		editPanel.add(new JLabel("Formula:"), UI.westConstraints(0, row));
		editPanel.add(formulaPanel, UI.westConstraints(1, row));
		row++;
		editPanel.add(new JLabel("Independent Variable:"), UI.westConstraints(0, row));
		editPanel.add(primIndepBox, UI.westConstraints(1, row));
		row++;
		editPanel.add(new JLabel("Start Parameter:"), UI.westConstraints(0, row));
		editPanel.add(primInitBox, UI.westConstraints(1, row));
		row++;
		editPanel.add(new JLabel("Variable Units:"), UI.westConstraints(0, row));
		editPanel.add(UI.createTablePanel(unitTable), UI.westConstraints(1, row));
		row++;
		editPanel.add(new JLabel("Parameter Ranges:"), UI.westConstraints(0, row));
		editPanel.add(UI.createTablePanel(paramTable), UI.westConstraints(1, row));

		JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createTitledBorder("Edit Model"));
		panel.setLayout(new BorderLayout());
		panel.add(editPanel, BorderLayout.WEST);

		return panel;
	}

	private JPanel createSecondaryEditPanel() {
		if (set.getFormula() == null) {
			set.setFormula(ModelsFactory.eINSTANCE.createSecondaryModelFormula());
		}

		SecondaryModelFormula formula = (SecondaryModelFormula) set.getFormula();

		depVarField = new StringTextField(false, 16);
		transformBox = new JComboBox<>(Transform.values());
		transformBox.setSelectedItem(formula.getTransformation());
		unitTable = new UnitTable<>(PmmUtils.getVariables(formula));
		paramTable = new ParamTable(formula.getParams());

		if (formula.getDepVar() != null) {
			depVarField.setValue(formula.getDepVar().getName());
		}

		depVarField.addTextListener(this);
		transformBox.addItemListener(this);
		primIndepBox.addItemListener(this);

		if (formulaField == null || formulaField.getValue() == null
				|| !formulaField.getValue().equals(set.getFormula().getExpression())) {
			formulaField = new StringTextArea(false, 3, 100);
			formulaField.setValue(formula.getExpression());
			formulaField.addTextListener(this);
		}

		JPanel formulaPanel = new JPanel();

		formulaPanel.setLayout(new BoxLayout(formulaPanel, BoxLayout.X_AXIS));

		if (formula.getTransformation() != Transform.NO_TRANSFORM) {
			formulaPanel.add(new JLabel(formula.getTransformation().toString() + "("));
		}

		formulaPanel.add(UI.createCenterPanel(depVarField));

		if (formula.getTransformation() != Transform.NO_TRANSFORM) {
			formulaPanel.add(new JLabel(") ="));
		} else {
			formulaPanel.add(new JLabel("="));
		}

		formulaPanel.add(formulaField);

		JPanel editPanel = new JPanel();
		int row = 0;

		editPanel.setLayout(new GridBagLayout());
		editPanel.add(new JLabel("Formula:"), UI.westConstraints(0, row));
		editPanel.add(formulaPanel, UI.westConstraints(1, row));
		row++;
		editPanel.add(new JLabel("Transformation:"), UI.westConstraints(0, row));
		editPanel.add(transformBox, UI.westConstraints(1, row));
		row++;
		editPanel.add(new JLabel("Independent Variable:"), UI.westConstraints(0, row));
		editPanel.add(createIndepBoxPanel(), UI.westConstraints(1, row));
		row++;
		editPanel.add(new JLabel("Variable Units:"), UI.westConstraints(0, row));
		editPanel.add(UI.createTablePanel(unitTable), UI.westConstraints(1, row));
		row++;
		editPanel.add(new JLabel("Parameter Ranges:"), UI.westConstraints(0, row));
		editPanel.add(UI.createTablePanel(paramTable), UI.westConstraints(1, row));

		JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createTitledBorder("Edit Model"));
		panel.setLayout(new BorderLayout());
		panel.add(editPanel, BorderLayout.WEST);

		return panel;
	}

	private JPanel createIndepBoxPanel() {
		SecondaryModelFormula formula = (SecondaryModelFormula) set.getFormula();
		List<Nameable> elements = new ArrayList<>();

		elements.addAll(formula.getIndepVars());
		elements.addAll(formula.getParams());
		PmmUtils.sortByName(elements);

		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		secIndepBoxes.clear();

		for (Nameable element : elements) {
			JCheckBox box = new JCheckBox(element.getName());

			if (element instanceof Variable) {
				box.setSelected(true);
			} else {
				box.setSelected(false);
			}

			box.addItemListener(this);
			panel.add(box);
			secIndepBoxes.add(box);
		}

		return panel;
	}

	private void updatePrimaryFormula() {
		PrimaryModelFormula modelFormula = (PrimaryModelFormula) set.getFormula();
		Set<String> symbols = MathUtils.getSymbols(modelFormula.getExpression());

		if (modelFormula.getIndepVar() == null || !symbols.contains(modelFormula.getIndepVar().getName())) {
			Variable indep = null;

			if (!symbols.isEmpty()) {
				Variable first = Iterables.getFirst(usedIndeps.values(), null);

				if (first != null) {
					indep = first;
				} else {
					indep = ModelsFactory.eINSTANCE.createVariable();
					indep.setName(new ArrayList<>(symbols).get(0));
				}
			}

			modelFormula.setIndepVar(indep);
		}

		if (modelFormula.getIndepVar() != null) {
			symbols.remove(modelFormula.getIndepVar().getName());
		}

		if (modelFormula.getInitialParam() == null || !symbols.contains(modelFormula.getInitialParam())) {
			String init = null;

			if (!symbols.isEmpty()) {
				if (symbols.contains(primInitParam)) {
					init = primInitParam;
				} else {
					init = new ArrayList<>(symbols).get(0);
				}
			}

			modelFormula.setInitialParam(init);
		}

		List<Parameter> newParams = new ArrayList<>();

		for (Parameter param : modelFormula.getParams()) {
			if (symbols.contains(param.getName())) {
				newParams.add(param);
				symbols.remove(param.getName());
			}
		}

		for (String paramName : symbols) {
			Parameter param = usedParameters.get(paramName);

			if (param == null) {
				param = ModelsFactory.eINSTANCE.createParameter();
				param.setName(paramName);
				usedParameters.put(paramName, param);
			}

			newParams.add(param);
		}

		PmmUtils.sortByName(newParams);
		modelFormula.getParams().clear();
		modelFormula.getParams().addAll(newParams);
	}

	private void updateSecondaryFormula() {
		SecondaryModelFormula modelFormula = (SecondaryModelFormula) set.getFormula();
		Set<String> symbols = MathUtils.getSymbols(modelFormula.getExpression());
		List<Variable> newIndeps = new ArrayList<>();
		List<Parameter> newParams = new ArrayList<>();

		for (Variable indep : modelFormula.getIndepVars()) {
			if (symbols.contains(indep.getName())) {
				newIndeps.add(indep);
				symbols.remove(indep.getName());
			}
		}

		for (Map.Entry<String, Variable> entry : usedIndeps.entrySet()) {
			if (symbols.contains(entry.getKey())) {
				newIndeps.add(entry.getValue());
				symbols.remove(entry.getKey());
			}
		}

		for (Parameter param : modelFormula.getParams()) {
			if (symbols.contains(param.getName())) {
				newParams.add(param);
				symbols.remove(param.getName());
			}
		}

		for (String paramName : symbols) {
			Parameter param = usedParameters.get(paramName);

			if (param == null) {
				param = ModelsFactory.eINSTANCE.createParameter();
				param.setName(paramName);
				usedParameters.put(paramName, param);
			}

			newParams.add(param);
		}

		PmmUtils.sortByName(newIndeps);
		PmmUtils.sortByName(newParams);
		modelFormula.getIndepVars().clear();
		modelFormula.getIndepVars().addAll(newIndeps);
		modelFormula.getParams().clear();
		modelFormula.getParams().addAll(newParams);
	}
}
