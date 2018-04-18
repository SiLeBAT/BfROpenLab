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
package de.bund.bfr.knime.pmmlite.views.chart;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.ui.KnimeDialog;

class SelectTableDialog extends KnimeDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	private List<String> visualizationColumns;
	private List<String> miscellaneousColumns;
	private List<String> qualityColumns;
	private List<String> conditionColumns;
	private List<String> parameterColumns;

	private boolean approved;
	private Set<String> columnSelection;

	private Map<String, JCheckBox> columnBoxes;
	private Map<String, JCheckBox> propertyBoxes;
	private JCheckBox visualizationBox;
	private JCheckBox miscellaneousBox;
	private JCheckBox qualityBox;
	private JCheckBox conditionsBox;
	private JCheckBox parametersBox;
	private JCheckBox propertyBox;

	private JButton okButton;
	private JButton cancelButton;

	public SelectTableDialog(Component owner, List<String> visualizationColumns, List<String> miscellaneousColumns,
			List<String> qualityColumns, List<String> conditionColumns, List<String> parameterColumns,
			List<String> properties, Set<String> initialColumnSelection) {
		super(owner, "Column Selection", DEFAULT_MODALITY_TYPE);

		this.visualizationColumns = visualizationColumns;
		this.miscellaneousColumns = miscellaneousColumns;
		this.qualityColumns = qualityColumns;
		this.conditionColumns = conditionColumns;
		this.parameterColumns = parameterColumns;

		approved = false;
		columnSelection = null;
		columnBoxes = new LinkedHashMap<>();
		propertyBoxes = new LinkedHashMap<>();

		JPanel visualizationPanel = new JPanel();

		visualizationPanel.setLayout(new GridLayout(visualizationColumns.size() + 1, 1, 5, 5));
		visualizationBox = new JCheckBox("All");
		visualizationBox.addActionListener(this);
		visualizationPanel.add(visualizationBox);

		for (String column : visualizationColumns) {
			JCheckBox box = new JCheckBox(column);

			box.setSelected(initialColumnSelection.contains(column));
			box.addActionListener(this);
			columnBoxes.put(column, box);
			visualizationPanel.add(box);
		}

		JPanel miscellaneousPanel = new JPanel();

		miscellaneousPanel.setLayout(new GridLayout(miscellaneousColumns.size() + 1, 1, 5, 5));
		miscellaneousBox = new JCheckBox("All");
		miscellaneousBox.addActionListener(this);
		miscellaneousPanel.add(miscellaneousBox);

		for (String column : miscellaneousColumns) {
			JCheckBox box = new JCheckBox(column);

			box.setSelected(initialColumnSelection.contains(column));
			box.addActionListener(this);
			columnBoxes.put(column, box);
			miscellaneousPanel.add(box);
		}

		JPanel qualityPanel = new JPanel();

		qualityPanel.setLayout(new GridLayout(qualityColumns.size() + 1, 1, 5, 5));
		qualityBox = new JCheckBox("All");
		qualityBox.addActionListener(this);
		qualityPanel.add(qualityBox);

		for (String column : qualityColumns) {
			JCheckBox box = new JCheckBox(column);

			box.setSelected(initialColumnSelection.contains(column));
			box.addActionListener(this);
			columnBoxes.put(column, box);
			qualityPanel.add(box);
		}

		JPanel conditionsPanel = new JPanel();

		conditionsPanel.setLayout(new GridLayout(conditionColumns.size() + 1, 1, 5, 5));
		conditionsBox = new JCheckBox("All");
		conditionsBox.addActionListener(this);
		conditionsPanel.add(conditionsBox);

		for (String column : conditionColumns) {
			JCheckBox box = new JCheckBox(column);

			box.setSelected(initialColumnSelection.contains(column));
			box.addActionListener(this);
			columnBoxes.put(column, box);
			conditionsPanel.add(box);
		}

		JPanel parameterPanel = new JPanel();
		int max = Math.max(parameterColumns.size(), properties.size());

		parametersBox = new JCheckBox("All");
		parametersBox.addActionListener(this);
		propertyBox = new JCheckBox("All");
		propertyBox.addActionListener(this);

		if (!parameterColumns.isEmpty()) {
			parameterPanel.setLayout(new GridLayout(max + 1, 2, 5, 5));
			parameterPanel.add(parametersBox);
		} else {
			parameterPanel.setLayout(new GridLayout(max + 1, 1, 5, 5));
		}

		parameterPanel.add(propertyBox);

		for (int i = 0; i < max; i++) {
			if (i < parameterColumns.size() && !parameterColumns.isEmpty()) {
				String column = parameterColumns.get(i);
				JCheckBox box = new JCheckBox(column);

				box.setSelected(initialColumnSelection.contains(column));
				box.addActionListener(this);
				columnBoxes.put(column, box);
				parameterPanel.add(box);
			} else {
				parameterPanel.add(new JLabel());
			}

			if (i < properties.size()) {
				String property = properties.get(i);
				JCheckBox box = new JCheckBox(property);

				box.setSelected(initialColumnSelection.contains(property));
				box.addActionListener(this);
				propertyBoxes.put(property, box);
				parameterPanel.add(box);
			} else {
				parameterPanel.add(new JLabel());
			}
		}

		updateCheckBoxes();

		JPanel centerPanel = new JPanel();

		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
		centerPanel.add(createTitledScrollPane(visualizationPanel, "Visualization"));

		if (!miscellaneousColumns.isEmpty()) {
			centerPanel.add(createTitledScrollPane(miscellaneousPanel, "Miscellaneous"));
		}

		if (!qualityColumns.isEmpty()) {
			centerPanel.add(createTitledScrollPane(qualityPanel, "Quality Criteria"));
		}

		if (!conditionColumns.isEmpty()) {
			centerPanel.add(createTitledScrollPane(conditionsPanel, "Conditions"));
		}

		if (!parameterColumns.isEmpty() || !properties.isEmpty()) {
			centerPanel.add(createTitledScrollPane(parameterPanel, "Parameters"));
		}

		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		setLayout(new BorderLayout());
		add(centerPanel, BorderLayout.CENTER);
		add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)), BorderLayout.SOUTH);

		pack();
		UI.adjustDialog(this);
		setLocationRelativeTo(owner);
		getRootPane().setDefaultButton(okButton);
	}

	public boolean isApproved() {
		return approved;
	}

	public Set<String> getColumnSelection() {
		return columnSelection;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			approved = true;
			columnSelection = new LinkedHashSet<>();

			for (Map.Entry<String, JCheckBox> entry : columnBoxes.entrySet()) {
				if (entry.getValue().isSelected()) {
					columnSelection.add(entry.getKey());
				}
			}

			for (Map.Entry<String, JCheckBox> entry : propertyBoxes.entrySet()) {
				if (entry.getValue().isSelected()) {
					columnSelection.add(entry.getKey());
				}
			}

			dispose();
		} else if (e.getSource() == cancelButton) {
			dispose();
		} else if (e.getSource() == visualizationBox) {
			setColumnsTo(visualizationColumns, visualizationBox.isSelected());
		} else if (e.getSource() == miscellaneousBox) {
			setColumnsTo(miscellaneousColumns, miscellaneousBox.isSelected());
		} else if (e.getSource() == qualityBox) {
			setColumnsTo(qualityColumns, qualityBox.isSelected());
		} else if (e.getSource() == conditionsBox) {
			setColumnsTo(conditionColumns, conditionsBox.isSelected());
		} else if (e.getSource() == parametersBox) {
			setColumnsTo(parameterColumns, parametersBox.isSelected());
		} else if (e.getSource() == propertyBox) {
			for (JCheckBox box : propertyBoxes.values()) {
				box.setSelected(propertyBox.isSelected());
			}
		} else {
			updateCheckBoxes();
		}
	}

	private void updateCheckBoxes() {
		Predicate<String> columnSelected = c -> columnBoxes.get(c).isSelected();

		visualizationBox.setSelected(visualizationColumns.stream().allMatch(columnSelected));
		miscellaneousBox.setSelected(miscellaneousColumns.stream().allMatch(columnSelected));
		qualityBox.setSelected(qualityColumns.stream().allMatch(columnSelected));
		conditionsBox.setSelected(conditionColumns.stream().allMatch(columnSelected));
		parametersBox.setSelected(parameterColumns.stream().allMatch(columnSelected));
		propertyBox.setSelected(propertyBoxes.values().stream().allMatch(b -> b.isSelected()));
	}

	private void setColumnsTo(Collection<String> columns, boolean value) {
		columns.forEach(c -> columnBoxes.get(c).setSelected(value));
	}

	private static JComponent createTitledScrollPane(JComponent comp, String name) {
		JPanel panel = UI.createNorthPanel(comp);

		panel.setBorder(BorderFactory.createTitledBorder(name));
		panel.setPreferredSize(
				new Dimension(Math.max(panel.getPreferredSize().width, 100), panel.getPreferredSize().height));

		return new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	}
}
