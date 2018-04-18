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
package de.bund.bfr.knime.pmmlite.io.datacreator;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.pmmlite.core.DataTable;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit;
import de.bund.bfr.knime.pmmlite.core.data.Condition;
import de.bund.bfr.knime.pmmlite.core.data.DataFactory;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.io.ConditionDialog;
import de.bund.bfr.knime.pmmlite.io.UnitTable;
import de.bund.bfr.knime.ui.StringTextField;

/**
 * <code>NodeDialog</code> for the "DataCreator" Node.
 * 
 * @author Christian Thoens
 */
public class DataCreatorNodeDialog extends NodeDialogPane implements ActionListener, CellEditorListener {

	private static final int ROW_COUNT = 1000;

	private DataCreatorSettings set;

	private DataTable table;
	private JButton addButton;
	private JButton removeButton;
	private JList<String> conditionsList;
	private UnitTable<NameableWithUnit> unitTable;
	private StringTextField organismField;
	private StringTextField matrixField;

	/**
	 * New pane for configuring the DataCreator node.
	 */
	protected DataCreatorNodeDialog() {
		set = new DataCreatorSettings();
		table = new DataTable(ROW_COUNT, true, true, PmmUtils.TIME, Arrays.asList(PmmUtils.CONCENTRATION));
		table.addCellEditorListener(this);
		addButton = new JButton("Add");
		addButton.addActionListener(this);
		removeButton = new JButton("Remove");
		removeButton.addActionListener(this);
		conditionsList = new JList<>();
		unitTable = new UnitTable<>(new ArrayList<>(0));
		unitTable.getColumn(UnitTable.UNIT_COLUMN).getCellEditor().addCellEditorListener(this);
		organismField = new StringTextField(true, 10);
		matrixField = new StringTextField(true, 10);

		JPanel conditionPanel = new JPanel();

		conditionPanel.setBorder(BorderFactory.createTitledBorder("Conditions"));
		conditionPanel.setLayout(new BorderLayout());
		conditionPanel.add(UI.createWestPanel(UI.createHorizontalPanel(addButton, removeButton)), BorderLayout.NORTH);
		conditionPanel.add(new JScrollPane(conditionsList), BorderLayout.CENTER);

		JPanel optionalPanel = UI.createHorizontalPanel(new JLabel("Organism:"), organismField, new JLabel("Matrix:"),
				matrixField);

		JPanel southPanel = new JPanel();

		southPanel.setLayout(new BorderLayout());
		southPanel.add(conditionPanel, BorderLayout.WEST);
		southPanel.add(UI.createTitledPanel(UI.createTablePanel(unitTable), "Units"), BorderLayout.CENTER);
		southPanel.add(UI.createTitledPanel(optionalPanel, "Optional Values"), BorderLayout.SOUTH);

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		panel.add(table, BorderLayout.CENTER);
		panel.add(southPanel, BorderLayout.SOUTH);

		addTab("Options", panel);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		set.load(settings);
		updateTableAndConditions();
		updateUnitTable();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		table.stopEditing();
		set.getTimeSeries().setOrganism(organismField.getValue());
		set.getTimeSeries().setMatrix(matrixField.getValue());
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

				set.getTimeSeries().getConditions().add(cond);
				conditionsList.setListData(new Vector<>(getConditionsAsStrings()));
				updateUnitTable();
			}
		} else if (e.getSource() == removeButton) {
			int[] indices = conditionsList.getSelectedIndices();

			Arrays.sort(indices);

			for (int i = indices.length - 1; i >= 0; i--) {
				set.getTimeSeries().getConditions().remove(indices[i]);
			}

			conditionsList.setListData(new Vector<>(getConditionsAsStrings()));
			updateUnitTable();
		}
	}

	@Override
	public void editingStopped(ChangeEvent e) {
		if (e.getSource() == unitTable.getColumn(UnitTable.UNIT_COLUMN).getCellEditor()) {
			Map<String, NameableWithUnit> elementsByName = PmmUtils.getByName(unitTable.getElements());

			for (Condition cond : set.getTimeSeries().getConditions()) {
				cond.setUnit(elementsByName.get(cond.getName()).getUnit());
			}

			set.getTimeSeries().setTimeUnit(elementsByName.get(PmmUtils.TIME).getUnit());
			set.getTimeSeries().setConcentrationUnit(elementsByName.get(PmmUtils.CONCENTRATION).getUnit());
		} else {
			set.getTimeSeries().getPoints().clear();
			set.getTimeSeries().getPoints().addAll(table.getData());
		}
	}

	@Override
	public void editingCanceled(ChangeEvent e) {
	}

	private void updateTableAndConditions() {
		if (set.getTimeSeries() == null) {
			set.setTimeSeries(DataFactory.eINSTANCE.createTimeSeries());
			set.getTimeSeries().setName("generated");
		}

		TimeSeries data = set.getTimeSeries();

		table.setData(data.getPoints());
		conditionsList.setListData(new Vector<>(getConditionsAsStrings()));
	}

	private void updateUnitTable() {
		Map<String, NameableWithUnit> elementsByName = PmmUtils.getByName(unitTable.getElements());
		List<NameableWithUnit> newElements = new ArrayList<>();

		if (elementsByName.containsKey(PmmUtils.TIME)) {
			newElements.add(elementsByName.get(PmmUtils.TIME));
		} else {
			Condition time = DataFactory.eINSTANCE.createCondition();

			time.setName(PmmUtils.TIME);
			time.setUnit(set.getTimeSeries().getTimeUnit());
			newElements.add(time);
		}

		if (elementsByName.containsKey(PmmUtils.CONCENTRATION)) {
			newElements.add(elementsByName.get(PmmUtils.CONCENTRATION));
		} else {
			Condition concentration = DataFactory.eINSTANCE.createCondition();

			concentration.setName(PmmUtils.CONCENTRATION);
			concentration.setUnit(set.getTimeSeries().getConcentrationUnit());
			newElements.add(concentration);
		}

		for (Condition cond : set.getTimeSeries().getConditions()) {
			if (elementsByName.containsKey(cond.getName())) {
				newElements.add(elementsByName.get(cond.getName()));
			} else {
				newElements.add(EcoreUtil.copy(cond));
			}
		}

		unitTable.setElements(newElements);
	}

	private List<String> getConditionsAsStrings() {
		List<String> conds = new ArrayList<>();

		for (Condition cond : set.getTimeSeries().getConditions()) {
			conds.add(cond.getName() + " = " + cond.getValue());
		}

		return conds;
	}
}
