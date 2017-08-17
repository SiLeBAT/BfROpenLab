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
package de.bund.bfr.knime.pmmlite.io.modelcreator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit;
import de.bund.bfr.knime.pmmlite.core.data.Condition;
import de.bund.bfr.knime.pmmlite.core.data.DataFactory;
import de.bund.bfr.knime.pmmlite.core.models.ModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.Parameter;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.io.ConditionDialog;
import de.bund.bfr.knime.pmmlite.io.ConditionListCellRenderer;
import de.bund.bfr.knime.pmmlite.io.UnitTable;
import de.bund.bfr.knime.ui.DoubleTextField;
import de.bund.bfr.knime.ui.StringTextField;

/**
 * <code>NodeDialog</code> for the "ModelCreator" Node.
 * 
 * @author Christian Thoens
 */
public class ModelCreatorNodeDialog extends DataAwareNodeDialogPane implements ActionListener, CellEditorListener {

	private ModelCreatorSettings set;

	private StringTextField idField;
	private StringTextField organismField;
	private StringTextField matrixField;
	private BiMap<String, DoubleTextField> paramFields;
	private JList<Condition> conditionsList;
	private List<Condition> conditions;
	private UnitTable<NameableWithUnit> unitTable;
	private JButton addButton;
	private JButton removeButton;

	private JPanel mainPanel;

	/**
	 * New pane for configuring the ModelCreator node.
	 */
	protected ModelCreatorNodeDialog() {
		set = new ModelCreatorSettings();

		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		addTab("Options", new JScrollPane(mainPanel));
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input) throws NotConfigurableException {
		PmmPortObject in = (PmmPortObject) input[0];

		set.load(settings);
		mainPanel.removeAll();
		mainPanel.add(UI.createNorthPanel(createConfigPanel(in.getData(ModelFormula.class).get(0))),
				BorderLayout.CENTER);
		mainPanel.revalidate();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		if (unitTable.isEditing()) {
			unitTable.getCellEditor().stopCellEditing();
		}

		if (!idField.isValueValid()) {
			throw new InvalidSettingsException("\"" + PmmUtils.DATA + "\" is unassigned");
		}

		set.setId(idField.getValue());
		set.setOrganism(organismField.getValue());
		set.setMatrix(matrixField.getValue());
		set.setUnits(unitTable.getElements());
		set.setConditions(conditionsToMap(conditions));
		set.getParameters().clear();

		for (Map.Entry<String, DoubleTextField> entry : paramFields.entrySet()) {
			set.getParameters().put(entry.getKey(), entry.getValue().getValue());
		}

		set.save(settings);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addButton) {
			ConditionDialog dialog = new ConditionDialog(addButton);

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				Condition cond = DataFactory.eINSTANCE.createCondition();

				cond.setName(PmmUtils.createMathSymbol(dialog.getConditionName()));
				cond.setValue(dialog.getConditionValue());
				conditions.add(cond);
				conditionsList.setListData(new Vector<>(conditions));
				set.setConditions(conditionsToMap(conditions));
				updateUnitTable();
			}
		} else if (e.getSource() == removeButton) {
			conditions.removeAll(conditionsList.getSelectedValuesList());
			conditionsList.setListData(new Vector<>(conditions));
			set.setConditions(conditionsToMap(conditions));
			updateUnitTable();
		}
	}

	@Override
	public void editingStopped(ChangeEvent e) {
		set.setUnits(unitTable.getElements());
	}

	@Override
	public void editingCanceled(ChangeEvent e) {
	}

	private JPanel createConfigPanel(ModelFormula formula) {
		idField = new StringTextField(false, 20);
		idField.setValue(set.getId());
		organismField = new StringTextField(true, 20);
		organismField.setValue(set.getOrganism());
		matrixField = new StringTextField(true, 20);
		matrixField.setValue(set.getMatrix());
		paramFields = HashBiMap.create();
		conditions = mapToConditions(set.getConditions());
		conditionsList = new JList<>(new Vector<>(conditions));
		conditionsList.setCellRenderer(new ConditionListCellRenderer());
		unitTable = new UnitTable<>(new ArrayList<>(0));
		unitTable.getColumn(UnitTable.UNIT_COLUMN).getCellEditor().addCellEditorListener(this);
		updateUnitTable();
		addButton = new JButton("Add");
		addButton.addActionListener(this);
		removeButton = new JButton("Remove");
		removeButton.addActionListener(this);

		JPanel mandatoryPanel = new JPanel();

		mandatoryPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		mandatoryPanel.setLayout(new BoxLayout(mandatoryPanel, BoxLayout.X_AXIS));
		mandatoryPanel.add(new JLabel(PmmUtils.DATA + ":"));
		mandatoryPanel.add(idField);

		JPanel optionalPanel = new JPanel();

		optionalPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		optionalPanel.setLayout(new BoxLayout(optionalPanel, BoxLayout.X_AXIS));
		optionalPanel.add(new JLabel(PmmUtils.ORGANISM + ":"));
		optionalPanel.add(organismField);
		optionalPanel.add(Box.createHorizontalStrut(5));
		optionalPanel.add(new JLabel(PmmUtils.MATRIX_TYPE + ":"));
		optionalPanel.add(matrixField);

		JPanel paramPanel = new JPanel();

		paramPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.X_AXIS));

		for (Parameter param : formula.getParams()) {
			DoubleTextField field = new DoubleTextField(false, 5);

			field.setValue(set.getParameters().get(param.getName()));
			paramPanel.add(Box.createHorizontalStrut(5));
			paramPanel.add(new JLabel(param.getName() + ":"));
			paramPanel.add(field);
			paramFields.put(param.getName(), field);
		}

		JPanel buttonPanel = new JPanel();

		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(addButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		buttonPanel.add(removeButton);

		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(UI.createTitledPanel(UI.createWestPanel(mandatoryPanel), "Mandatory Columns"));
		panel.add(UI.createTitledPanel(UI.createWestPanel(optionalPanel), "Optional Columns"));
		panel.add(UI.createTitledPanel(UI.createWestPanel(paramPanel), "Parameter Columns"));

		if (formula instanceof PrimaryModelFormula) {
			JPanel conditionsPanel = new JPanel();

			conditionsPanel.setBorder(BorderFactory.createTitledBorder("Conditions"));
			conditionsPanel.setLayout(new BorderLayout());
			conditionsPanel.add(buttonPanel, BorderLayout.NORTH);
			conditionsPanel.add(new JScrollPane(conditionsList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

			JPanel unitPanel = new JPanel();

			unitPanel.setBorder(BorderFactory.createTitledBorder("Units"));
			unitPanel.setLayout(new BorderLayout());
			unitPanel.add(UI.createTablePanel(unitTable), BorderLayout.CENTER);

			JPanel splitPanel = new JPanel();

			splitPanel.setLayout(new BorderLayout());
			splitPanel.add(conditionsPanel, BorderLayout.WEST);
			splitPanel.add(unitPanel, BorderLayout.CENTER);

			panel.add(splitPanel);
		}

		return panel;
	}

	private void updateUnitTable() {
		Map<String, NameableWithUnit> elementsByName = PmmUtils.getByName(set.getUnits());
		List<NameableWithUnit> newElements = new ArrayList<>();

		for (Condition cond : conditions) {
			if (elementsByName.containsKey(cond.getName())) {
				newElements.add(elementsByName.get(cond.getName()));
			} else {
				Condition element = DataFactory.eINSTANCE.createCondition();

				element.setName(cond.getName());
				newElements.add(element);
			}
		}

		unitTable.setElements(newElements);
	}

	private static Map<String, Double> conditionsToMap(List<Condition> conditions) {
		Map<String, Double> map = new LinkedHashMap<>();

		for (Condition cond : conditions) {
			map.put(cond.getName(), cond.getValue());
		}

		return map;
	}

	private static List<Condition> mapToConditions(Map<String, Double> map) {
		List<Condition> conditions = new ArrayList<>();

		for (Map.Entry<String, Double> entry : map.entrySet()) {
			Condition cond = DataFactory.eINSTANCE.createCondition();

			cond.setName(entry.getKey());
			cond.setValue(entry.getValue());

			conditions.add(cond);
		}

		return conditions;
	}
}
