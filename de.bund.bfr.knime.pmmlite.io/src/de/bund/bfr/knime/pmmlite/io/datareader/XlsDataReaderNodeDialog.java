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
package de.bund.bfr.knime.pmmlite.io.datareader;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.FilesHistoryPanel;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.common.NameableWithUnit;
import de.bund.bfr.knime.pmmlite.core.data.Condition;
import de.bund.bfr.knime.pmmlite.core.data.DataFactory;
import de.bund.bfr.knime.pmmlite.io.UnitTable;
import de.bund.bfr.knime.pmmlite.io.XlsException;
import de.bund.bfr.knime.pmmlite.io.XlsReader;
import de.bund.bfr.knime.ui.Dialogs;

/**
 * <code>NodeDialog</code> for the "XlsDataReader" Node.
 * 
 * @author Christian Thoens
 */
public class XlsDataReaderNodeDialog extends NodeDialogPane
		implements ActionListener, ItemListener, ChangeListener, CellEditorListener {

	private static final String FILE_HISTORY_ID = "XlsDataReaderFileHistory";

	private XlsDataReaderSettings set;
	private XlsReader xlsReader;

	private FilesHistoryPanel filePanel;
	private JComboBox<String> sheetBox;
	private JComboBox<String> idBox;
	private JComboBox<String> timeBox;
	private JComboBox<String> concentrationBox;
	private JComboBox<String> organismBox;
	private JComboBox<String> matrixBox;
	private JList<String> conditionsList;
	private List<String> conditions;
	private UnitTable<NameableWithUnit> unitTable;
	private JButton addButton;
	private JButton removeButton;

	/**
	 * New pane for configuring the XlsDataReader node.
	 */
	protected XlsDataReaderNodeDialog() {
		set = new XlsDataReaderSettings();
		xlsReader = new XlsReader();

		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(createFileSheetPanel(), BorderLayout.NORTH);
		mainPanel.add(UI.createNorthPanel(createConfigPanel()), BorderLayout.CENTER);

		addTab("Options", new JScrollPane(mainPanel));
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		set.load(settings);

		filePanel.addChangeListener(this);
		filePanel.setSelectedFile(set.getFileName());
		filePanel.removeChangeListener(this);

		fileNameChanged();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		if (unitTable.isEditing()) {
			unitTable.getCellEditor().stopCellEditing();
		}

		set.setIdColumn((String) idBox.getSelectedItem());
		set.setTimeColumn((String) timeBox.getSelectedItem());
		set.setConcentrationColumn((String) concentrationBox.getSelectedItem());
		set.setOrganismColumn((String) organismBox.getSelectedItem());
		set.setMatrixColumn((String) matrixBox.getSelectedItem());
		set.setConditionColumns(conditions);
		set.setUnits(unitTable.getElements());

		if (set.getFileName() == null) {
			throw new InvalidSettingsException("No file is specfied");
		}

		try {
			if (xlsReader.getColumns().isEmpty()) {
				throw new InvalidSettingsException("No Columns in specified file");
			}
		} catch (XlsException e) {
			throw new InvalidSettingsException("Specified file is invalid");
		}

		if (set.getSheetName() == null) {
			throw new InvalidSettingsException("No sheet is selected");
		}

		if (set.getIdColumn() == null) {
			throw new InvalidSettingsException("\"" + PmmUtils.DATA + "\" is unassigned");
		}

		if (set.getTimeColumn() == null) {
			throw new InvalidSettingsException("\"" + PmmUtils.TIME + "\" is unassigned");
		}

		if (set.getConcentrationColumn() == null) {
			throw new InvalidSettingsException("\"" + PmmUtils.CONCENTRATION + "\" is unassigned");
		}

		set.save(settings);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addButton) {
			List<String> values = new ArrayList<>();

			try {
				values.addAll(xlsReader.getSheets());
			} catch (XlsException ex) {
			}

			values.removeAll(conditions);

			String result = Dialogs.showInputDialog(addButton, "Choose Condition", "Condition Dialog", values);

			if (result != null) {
				conditions.add(result);
				conditionsList.setListData(new Vector<>(conditions));
				set.setConditionColumns(conditions);
				updateUnitTable();
			}
		} else if (e.getSource() == removeButton) {
			conditions.removeAll(conditionsList.getSelectedValuesList());
			conditionsList.setListData(new Vector<>(conditions));
			set.setConditionColumns(conditions);
			updateUnitTable();
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == sheetBox && e.getStateChange() == ItemEvent.SELECTED) {
			try {
				xlsReader.setSheet((String) sheetBox.getSelectedItem());
			} catch (XlsException ex) {
			}

			updateColumnBoxes();
			updateUnitTable();
		} else if (e.getSource() == idBox && e.getStateChange() == ItemEvent.SELECTED) {
			set.setIdColumn((String) idBox.getSelectedItem());
		} else if (e.getSource() == timeBox && e.getStateChange() == ItemEvent.SELECTED) {
			set.setTimeColumn((String) timeBox.getSelectedItem());
		} else if (e.getSource() == concentrationBox && e.getStateChange() == ItemEvent.SELECTED) {
			set.setConcentrationColumn((String) concentrationBox.getSelectedItem());
		} else if (e.getSource() == organismBox && e.getStateChange() == ItemEvent.SELECTED) {
			set.setOrganismColumn((String) organismBox.getSelectedItem());
		} else if (e.getSource() == matrixBox && e.getStateChange() == ItemEvent.SELECTED) {
			set.setMatrixColumn((String) matrixBox.getSelectedItem());
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		set.setFileName(filePanel.getSelectedFile());
		fileNameChanged();
	}

	@Override
	public void editingStopped(ChangeEvent e) {
		set.setUnits(unitTable.getElements());
	}

	@Override
	public void editingCanceled(ChangeEvent e) {
	}

	private JPanel createFileSheetPanel() {
		filePanel = new FilesHistoryPanel(FILE_HISTORY_ID, FilesHistoryPanel.LocationValidation.FileInput);
		filePanel.setSuffixes(".xls", ".xlsx");
		filePanel.addChangeListener(this);
		sheetBox = new JComboBox<>();
		sheetBox.addItemListener(this);

		JPanel fileSheetPanel = new JPanel();

		fileSheetPanel.setLayout(new BorderLayout());
		fileSheetPanel.add(UI.createTitledPanel(filePanel, "XLS File"), BorderLayout.CENTER);
		fileSheetPanel.add(UI.createTitledPanel(UI.createCenterPanel(sheetBox), "Sheet"), BorderLayout.EAST);

		return fileSheetPanel;
	}

	private JPanel createConfigPanel() {
		idBox = new JComboBox<>();
		timeBox = new JComboBox<>();
		concentrationBox = new JComboBox<>();
		organismBox = new JComboBox<>();
		matrixBox = new JComboBox<>();
		conditionsList = new JList<>();
		conditions = new ArrayList<>();
		unitTable = new UnitTable<>(new ArrayList<>(0));
		unitTable.getColumn(UnitTable.UNIT_COLUMN).getCellEditor().addCellEditorListener(this);
		addButton = new JButton("Add");
		addButton.addActionListener(this);
		removeButton = new JButton("Remove");
		removeButton.addActionListener(this);

		addColumnBoxListeners();

		JPanel mandatoryPanel = new JPanel();

		mandatoryPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		mandatoryPanel.setLayout(new BoxLayout(mandatoryPanel, BoxLayout.X_AXIS));
		mandatoryPanel.add(new JLabel(PmmUtils.DATA + ":"));
		mandatoryPanel.add(idBox);
		mandatoryPanel.add(Box.createHorizontalStrut(5));
		mandatoryPanel.add(new JLabel(PmmUtils.TIME + ":"));
		mandatoryPanel.add(timeBox);
		mandatoryPanel.add(Box.createHorizontalStrut(5));
		mandatoryPanel.add(new JLabel(PmmUtils.CONCENTRATION + ":"));
		mandatoryPanel.add(concentrationBox);

		JPanel optionalPanel = new JPanel();

		optionalPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		optionalPanel.setLayout(new BoxLayout(optionalPanel, BoxLayout.X_AXIS));
		optionalPanel.add(new JLabel(PmmUtils.ORGANISM + ":"));
		optionalPanel.add(organismBox);
		optionalPanel.add(Box.createHorizontalStrut(5));
		optionalPanel.add(new JLabel(PmmUtils.MATRIX_TYPE + ":"));
		optionalPanel.add(matrixBox);

		JPanel buttonPanel = new JPanel();

		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(addButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		buttonPanel.add(removeButton);

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

		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(UI.createTitledPanel(UI.createWestPanel(mandatoryPanel), "Mandatory Columns"));
		panel.add(UI.createTitledPanel(UI.createWestPanel(optionalPanel), "Optional Columns"));
		panel.add(splitPanel);

		return panel;
	}

	private void fileNameChanged() {
		sheetBox.removeItemListener(this);
		sheetBox.removeAllItems();

		try {
			xlsReader.setFile(set.getFileName());

			for (String sheet : xlsReader.getSheets()) {
				sheetBox.addItem(sheet);
			}

			UI.select(sheetBox, set.getSheetName());
			xlsReader.setSheet(set.getSheetName());
		} catch (NullPointerException | IOException | InvalidFormatException | XlsException e) {
		}

		sheetBox.addItemListener(this);

		updateColumnBoxes();
		updateUnitTable();
	}

	private void updateColumnBoxes() {
		List<String> columns = new ArrayList<>();

		try {
			columns.addAll(xlsReader.getColumns());
		} catch (XlsException e) {
		}

		removeColumnBoxListeners();

		idBox.removeAllItems();
		timeBox.removeAllItems();
		concentrationBox.removeAllItems();
		organismBox.removeAllItems();
		matrixBox.removeAllItems();

		organismBox.addItem(null);
		matrixBox.addItem(null);

		for (String column : columns) {
			idBox.addItem(column);
			timeBox.addItem(column);
			concentrationBox.addItem(column);
			organismBox.addItem(column);
			matrixBox.addItem(column);
		}

		UI.select(idBox, set.getIdColumn());
		UI.select(timeBox, set.getTimeColumn());
		UI.select(concentrationBox, set.getConcentrationColumn());
		UI.select(organismBox, set.getOrganismColumn());
		UI.select(matrixBox, set.getMatrixColumn());

		addColumnBoxListeners();

		conditions.clear();

		for (String cond : set.getConditionColumns()) {
			if (columns.contains(cond)) {
				conditions.add(cond);
			}
		}

		conditionsList.setListData(new Vector<>(conditions));
	}

	private void updateUnitTable() {
		Map<String, NameableWithUnit> elementsByName = PmmUtils.getByName(set.getUnits());
		List<NameableWithUnit> newElements = new ArrayList<>();
		List<String> variableNames = new ArrayList<>();

		variableNames.add(PmmUtils.TIME);
		variableNames.add(PmmUtils.CONCENTRATION);
		variableNames.addAll(conditions);

		for (String name : variableNames) {
			if (elementsByName.containsKey(name)) {
				newElements.add(elementsByName.get(name));
			} else {
				Condition element = DataFactory.eINSTANCE.createCondition();

				element.setName(name);
				newElements.add(element);
			}
		}

		unitTable.setElements(newElements);
	}

	private void addColumnBoxListeners() {
		idBox.addItemListener(this);
		timeBox.addItemListener(this);
		concentrationBox.addItemListener(this);
		organismBox.addItemListener(this);
		matrixBox.addItemListener(this);
	}

	private void removeColumnBoxListeners() {
		idBox.removeItemListener(this);
		timeBox.removeItemListener(this);
		concentrationBox.removeItemListener(this);
		organismBox.removeItemListener(this);
		matrixBox.removeItemListener(this);
	}
}
