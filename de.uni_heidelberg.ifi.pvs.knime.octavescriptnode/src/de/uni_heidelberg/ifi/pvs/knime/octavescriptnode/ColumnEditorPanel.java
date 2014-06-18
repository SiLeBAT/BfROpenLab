/*
 * OctaveScriptNode - A KNIME node that runs Octave scripts
 * Copyright (C) 2011 Andre-Patrick Bubel (pvs@andre-bubel.de) and
 *                    Parallel and Distributed Systems Group (PVS),
 *                    University of Heidelberg, Germany
 * Website: http://pvs.ifi.uni-heidelberg.de/
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
 */
package de.uni_heidelberg.ifi.pvs.knime.octavescriptnode;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * Extracted from org.knime.ext.jython.PythonFunctionNodeDialog
 * 
 * @author André-Patrick Bubel <code@andre-bubel.de>
 * 
 */
public class ColumnEditorPanel extends JPanel {
	private static NodeLogger logger = NodeLogger
			.getLogger(ColumnEditorPanel.class);

	private static final long serialVersionUID = 8264391036355837398L;

	private int counter = 1;
	private JCheckBox m_appendColsCB;
	private JTable table;

	public ColumnEditorPanel() {
		// construct the output column selection panel
		JPanel outputPanel = new JPanel();
		outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.Y_AXIS));
		JPanel outputButtonPanel = new JPanel();
		JPanel outputMainPanel = new JPanel(new BorderLayout());
		JPanel newtableCBPanel = new JPanel();
		m_appendColsCB = new JCheckBox("Append columns to input table spec");
		m_appendColsCB.setSelected(true);
		newtableCBPanel.add(m_appendColsCB, BorderLayout.WEST);
		JButton addButton = new JButton(new AbstractAction() {

			private static final long serialVersionUID = -1793900194193788523L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				((ScriptNodeOutputColumnsTableModel) table.getModel()).addRow(
						"script_output_" + counter, "Double");
				counter++;
			}
		});
		addButton.setText("Add Output Column");

		JButton removeButton = new JButton(new AbstractAction() {

			private static final long serialVersionUID = -5156473691690800775L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				int[] selectedRows = table.getSelectedRows();
				logger.debug("selectedRows = " + selectedRows);

				if (selectedRows.length == 0) {
					return;
				}

				for (int i = selectedRows.length - 1; i >= 0; i--) {
					logger.debug("   removal " + i + ": removing row "
							+ selectedRows[i]);
					((ScriptNodeOutputColumnsTableModel) table.getModel())
							.removeRow(selectedRows[i]);
				}
			}
		});
		removeButton.setText("Remove Output Column");

		outputButtonPanel.add(addButton);
		outputButtonPanel.add(removeButton);

		table = new JTable();
		table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

		table.setAutoscrolls(true);
		ScriptNodeOutputColumnsTableModel model = new ScriptNodeOutputColumnsTableModel();
		model.addColumn("Column name");
		model.addColumn("Column type");
		table.setModel(model);

		outputMainPanel.add(table.getTableHeader(), BorderLayout.PAGE_START);
		outputMainPanel.add(table, BorderLayout.CENTER);
		outputPanel.add(newtableCBPanel);
		outputPanel.add(outputButtonPanel);
		outputPanel.add(outputMainPanel);

		TableColumn typeColumn = table.getColumnModel().getColumn(1);
		JComboBox<String> typeSelector = new JComboBox<>();

		typeSelector.addItem("Double");
		typeSelector.addItem("Integer");
		typeSelector.addItem("String");

		typeColumn.setCellEditor(new DefaultCellEditor(typeSelector));

		this.add(outputPanel);
	}

	protected void loadSettingsFrom(final NodeSettingsRO settings,
			final DataTableSpec[] specs) {

		boolean appendCols = settings.getBoolean(
				OctaveScriptNodeModel.APPEND_COLS, true);
		m_appendColsCB.setSelected(appendCols);

		String[] dataTableColumnNames = settings.getStringArray(
				OctaveScriptNodeModel.COLUMN_NAMES, new String[0]);
		String[] dataTableColumnTypes = settings.getStringArray(
				OctaveScriptNodeModel.COLUMN_TYPES, new String[0]);

		((ScriptNodeOutputColumnsTableModel) table.getModel()).clearRows();

		if (dataTableColumnNames == null) {
			return;
		}

		for (int i = 0; i < dataTableColumnNames.length; i++) {
			((ScriptNodeOutputColumnsTableModel) table.getModel()).addRow(
					dataTableColumnNames[i], dataTableColumnTypes[i]);
		}
	}

	protected void saveSettingsTo(final NodeSettingsWO settings) {
		// work around a jtable cell value persistence problem
		// by explicitly stopping editing if a cell is currently in edit mode
		int editingRow = table.getEditingRow();
		int editingColumn = table.getEditingColumn();

		if (editingRow != -1 && editingColumn != -1) {
			TableCellEditor editor = table.getCellEditor(editingRow,
					editingColumn);
			editor.stopCellEditing();
		}

		// save the settings
		settings.addBoolean(OctaveScriptNodeModel.APPEND_COLS,
				m_appendColsCB.isSelected());
		String[] columnNames = ((ScriptNodeOutputColumnsTableModel) table
				.getModel()).getDataTableColumnNames();
		settings.addStringArray(OctaveScriptNodeModel.COLUMN_NAMES, columnNames);

		String[] columnTypes = ((ScriptNodeOutputColumnsTableModel) table
				.getModel()).getDataTableColumnTypes();
		settings.addStringArray(OctaveScriptNodeModel.COLUMN_TYPES, columnTypes);
	}

}
