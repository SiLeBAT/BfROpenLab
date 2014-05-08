/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
 ******************************************************************************/
package de.bund.bfr.knime.gis.views.canvas.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition;

public class HighlightDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static enum Type {
		LOGICAL_CONDITION, VALUE_CONDITION, LOGICAL_VALUE_CONDITION;

		@Override
		public String toString() {
			switch (this) {
			case LOGICAL_CONDITION:
				return "Logical Condition";
			case VALUE_CONDITION:
				return "Value Condition";
			case LOGICAL_VALUE_CONDITION:
				return "Logical Value Condition";
			}

			return super.toString();
		}
	}

	private static enum AndOr {
		AND, OR;

		@Override
		public String toString() {
			switch (this) {
			case AND:
				return "And";
			case OR:
				return "Or";
			}

			return super.toString();
		}
	}

	private Type type;

	private JComboBox<Type> conditionTypeBox;
	private JTextField nameField;
	private JCheckBox legendBox;
	private JButton colorButton;
	private JCheckBox colorBox;
	private JCheckBox invisibleBox;
	private JCheckBox thicknessBox;
	private JComboBox<String> labelBox;
	private JComponent conditionPanel;
	private JButton okButton;
	private JButton cancelButton;

	private JComboBox<String> valuePropertyBox;
	private JComboBox<String> valueTypeBox;

	private List<JComboBox<AndOr>> logicalAndOrBoxes;
	private List<JComboBox<String>> logicalPropertyBoxes;
	private List<JComboBox<String>> logicalTypeBoxes;
	private List<JTextField> logicalValueFields;
	private List<JButton> logicalAddButtons;
	private List<JButton> logicalRemoveButtons;

	private Map<String, Class<?>> nodeProperties;
	private boolean allowName;
	private boolean allowColor;
	private boolean allowInvisible;
	private boolean allowThickness;
	private boolean allowLabel;
	private HighlightConditionChecker checker;

	private HighlightCondition highlightCondition;
	private boolean approved;

	public HighlightDialog(Component parent,
			Map<String, Class<?>> nodeProperties, boolean allowName,
			boolean allowColor, boolean allowInvisible, boolean allowThickness,
			boolean allowLabel, boolean allowValueCondition,
			HighlightCondition initialCondition,
			HighlightConditionChecker checker) {
		super(SwingUtilities.getWindowAncestor(parent), "Highlight Condition",
				DEFAULT_MODALITY_TYPE);
		this.nodeProperties = nodeProperties;
		this.allowName = allowName;
		this.allowColor = allowColor;
		this.allowInvisible = allowInvisible;
		this.allowThickness = allowThickness;
		this.allowLabel = allowLabel;
		this.checker = checker;
		highlightCondition = null;
		approved = false;

		if (allowValueCondition) {
			conditionTypeBox = new JComboBox<Type>(Type.values());
		} else {
			conditionTypeBox = new JComboBox<Type>(
					new Type[] { Type.LOGICAL_CONDITION });
		}

		JPanel optionsPanel = new JPanel();

		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.X_AXIS));
		optionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		optionsPanel.add(new JLabel("Type:"));
		optionsPanel.add(conditionTypeBox);

		if (allowName) {
			nameField = new JTextField(20);
			optionsPanel.add(Box.createHorizontalStrut(5));
			optionsPanel.add(new JLabel("Name:"));
			optionsPanel.add(nameField);
		}

		if (allowColor) {
			legendBox = new JCheckBox("Show In Legend");
			legendBox.setSelected(true);
			colorButton = new JButton("     ");
			colorButton.setContentAreaFilled(false);
			colorButton.setOpaque(true);
			colorBox = new JCheckBox("Use Color");
			colorBox.setSelected(true);
			optionsPanel.add(Box.createHorizontalStrut(5));
			optionsPanel.add(legendBox);
			optionsPanel.add(Box.createHorizontalStrut(5));
			optionsPanel.add(new JLabel("Color:"));
			optionsPanel.add(colorButton);
			optionsPanel.add(Box.createHorizontalStrut(5));
			optionsPanel.add(colorBox);
		}

		if (allowInvisible) {
			invisibleBox = new JCheckBox("Invisible");
			invisibleBox.setSelected(false);
			optionsPanel.add(Box.createHorizontalStrut(5));
			optionsPanel.add(invisibleBox);
		}

		if (allowThickness) {
			thicknessBox = new JCheckBox("Adjust Thickness");
			thicknessBox.setSelected(false);
			optionsPanel.add(Box.createHorizontalStrut(5));
			optionsPanel.add(thicknessBox);
		}

		if (allowLabel) {
			List<String> choices = new ArrayList<String>();

			choices.add("");
			choices.addAll(nodeProperties.keySet());

			labelBox = new JComboBox<String>(choices.toArray(new String[0]));
			labelBox.setSelectedItem("");
			optionsPanel.add(Box.createHorizontalStrut(5));
			optionsPanel.add(new JLabel("Label:"));
			optionsPanel.add(labelBox);
		}

		JPanel upperPanel = new JPanel();

		upperPanel.setLayout(new BorderLayout());
		upperPanel.add(UI.createWestPanel(optionsPanel), BorderLayout.CENTER);
		upperPanel.add(new JSeparator(), BorderLayout.SOUTH);

		if (initialCondition != null) {
			if (initialCondition instanceof AndOrHighlightCondition) {
				type = Type.LOGICAL_CONDITION;
				conditionPanel = createLogicalPanel((AndOrHighlightCondition) initialCondition);
			} else if (initialCondition instanceof ValueHighlightCondition) {
				type = Type.VALUE_CONDITION;
				conditionPanel = createValuePanel((ValueHighlightCondition) initialCondition);
			} else if (initialCondition instanceof LogicalValueHighlightCondition) {
				type = Type.LOGICAL_VALUE_CONDITION;
				conditionPanel = createLogicalValuePanel((LogicalValueHighlightCondition) initialCondition);
			}

			conditionTypeBox.setSelectedItem(type);

			if (allowName && initialCondition.getName() != null) {
				nameField.setText(initialCondition.getName());
			}

			if (initialCondition.isInvisible()) {
				invisibleBox.setSelected(true);
				colorBox.setEnabled(false);
				colorButton.setEnabled(false);
				legendBox.setEnabled(false);
			}

			if (initialCondition.isUseThickness()) {
				thicknessBox.setSelected(true);
			}

			if (allowColor) {
				if (initialCondition.getColor() != null) {
					colorButton.setBackground(initialCondition.getColor());
					legendBox.setSelected(initialCondition.isShowInLegend());
				} else {
					colorBox.setSelected(false);
					colorButton.setEnabled(false);
					legendBox.setEnabled(false);
				}
			}

			if (initialCondition.getLabelProperty() != null) {
				labelBox.setSelectedItem(initialCondition.getLabelProperty());
			}
		} else {
			conditionTypeBox.setSelectedItem(Type.LOGICAL_CONDITION);
			type = Type.LOGICAL_CONDITION;
			conditionPanel = createLogicalPanel(null);

			if (allowColor) {
				colorButton.setBackground(Color.RED);
			}
		}

		conditionTypeBox.addActionListener(this);

		if (allowColor) {
			colorButton.addActionListener(this);
			colorBox.addActionListener(this);
		}

		if (allowInvisible) {
			invisibleBox.addActionListener(this);
		}

		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		JPanel bottomPanel = new JPanel();

		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(new JSeparator(), BorderLayout.NORTH);
		bottomPanel.add(UI.createHorizontalPanel(okButton, cancelButton),
				BorderLayout.EAST);

		setLayout(new BorderLayout());
		add(upperPanel, BorderLayout.NORTH);
		add(conditionPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(parent);
		UI.adjustDialog(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			String error = null;

			highlightCondition = createCondition();

			if (checker != null) {
				error = checker.findError(highlightCondition);
			}

			if (error == null) {
				approved = true;
				dispose();
			} else {
				JOptionPane.showMessageDialog(okButton, error, "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == cancelButton) {
			dispose();
		} else if (e.getSource() == conditionTypeBox) {
			if (!type.equals(conditionTypeBox.getSelectedItem())) {
				remove(conditionPanel);

				if (conditionTypeBox.getSelectedItem() == Type.LOGICAL_CONDITION) {
					if (type == Type.LOGICAL_VALUE_CONDITION) {
						LogicalValueHighlightCondition c = (LogicalValueHighlightCondition) createCondition();

						conditionPanel = createLogicalPanel(c
								.getLogicalCondition());
					} else {
						conditionPanel = createLogicalPanel(null);
					}
				} else if (conditionTypeBox.getSelectedItem() == Type.VALUE_CONDITION) {
					if (type == Type.LOGICAL_VALUE_CONDITION) {
						LogicalValueHighlightCondition c = (LogicalValueHighlightCondition) createCondition();

						conditionPanel = createValuePanel(c.getValueCondition());
					} else {
						conditionPanel = createValuePanel(null);
					}
				} else if (conditionTypeBox.getSelectedItem() == Type.LOGICAL_VALUE_CONDITION) {
					if (type == Type.LOGICAL_CONDITION) {
						AndOrHighlightCondition c = (AndOrHighlightCondition) createCondition();

						conditionPanel = createLogicalValuePanel(new LogicalValueHighlightCondition(
								null, c));
					} else if (type == Type.VALUE_CONDITION) {
						ValueHighlightCondition c = (ValueHighlightCondition) createCondition();

						conditionPanel = createLogicalValuePanel(new LogicalValueHighlightCondition(
								c, null));
					} else {
						conditionPanel = createLogicalValuePanel(null);
					}
				}

				type = (Type) conditionTypeBox.getSelectedItem();
				add(conditionPanel, BorderLayout.CENTER);
				pack();
			}
		} else if (e.getSource() == colorButton) {
			Color newColor = JColorChooser.showDialog(colorButton,
					"Choose Color", colorButton.getBackground());

			if (newColor != null) {
				colorButton.setBackground(newColor);
			}
		} else if (e.getSource() == colorBox) {
			if (!colorBox.isSelected()) {
				colorButton.setBackground(UIManager.getDefaults().getColor(
						"Button.background"));
				colorButton.setEnabled(false);
				legendBox.setEnabled(false);
			} else {
				colorButton.setBackground(Color.RED);
				colorButton.setEnabled(true);
				legendBox.setEnabled(true);
			}
		} else if (e.getSource() == invisibleBox) {
			if (allowColor) {
				if (invisibleBox.isSelected()) {
					colorBox.setEnabled(false);
					colorButton.setBackground(UIManager.getDefaults().getColor(
							"Button.background"));
					colorButton.setEnabled(false);
					legendBox.setEnabled(false);
				} else {
					colorBox.setEnabled(true);

					if (colorBox.isSelected()) {
						colorButton.setBackground(Color.RED);
						colorButton.setEnabled(true);
						legendBox.setEnabled(true);
					}
				}
			}
		} else if (logicalAddButtons.contains(e.getSource())) {
			addRemoveButtonPressed((JButton) e.getSource());
		} else if (logicalRemoveButtons.contains(e.getSource())) {
			addRemoveButtonPressed((JButton) e.getSource());
		}
	}

	public HighlightCondition getHighlightCondition() {
		return highlightCondition;
	}

	public boolean isApproved() {
		return approved;
	}

	@SuppressWarnings("unchecked")
	private JComponent createLogicalPanel(AndOrHighlightCondition condition) {
		logicalAndOrBoxes = new ArrayList<JComboBox<AndOr>>();
		logicalPropertyBoxes = new ArrayList<JComboBox<String>>();
		logicalTypeBoxes = new ArrayList<JComboBox<String>>();
		logicalValueFields = new ArrayList<JTextField>();
		logicalAddButtons = new ArrayList<JButton>();
		logicalRemoveButtons = new ArrayList<JButton>();

		JPanel logicalPanel = new JPanel();
		int row = 0;

		logicalPanel.setLayout(new GridBagLayout());
		logicalPanel.add(new JLabel("Property"), createConstraints(1, row));
		logicalPanel.add(new JLabel("Operation"), createConstraints(2, row));
		logicalPanel.add(new JLabel("Value"), createConstraints(3, row));
		row++;

		if (condition == null) {
			condition = new AndOrHighlightCondition(Arrays.asList(Arrays
					.asList(new LogicalHighlightCondition(nodeProperties
							.keySet().toArray(new String[0])[0],
							LogicalHighlightCondition.EQUAL_TYPE, ""))), null,
					false, null, false, false, null);
		}

		for (int i = 0; i < condition.getConditions().size(); i++) {
			for (int j = 0; j < condition.getConditions().get(i).size(); j++) {
				LogicalHighlightCondition cond = condition.getConditions()
						.get(i).get(j);

				JComboBox<String> propertyBox = new JComboBox<String>(
						nodeProperties.keySet().toArray(new String[0]));
				JComboBox<String> typeBox = new JComboBox<String>(
						LogicalHighlightCondition.TYPES);
				JTextField valueField = new JTextField(20);
				JButton addButton = new JButton("Add");
				JButton removeButton = new JButton("Remove");

				propertyBox.setSelectedItem(cond.getProperty());
				typeBox.setSelectedItem(cond.getType());
				valueField.setText(cond.getValue() + "");

				addButton.addActionListener(this);
				removeButton.addActionListener(this);

				if (row != 1) {
					JComboBox<AndOr> andOrBox = new JComboBox<AndOr>(
							AndOr.values());

					if (j == 0) {
						andOrBox.setSelectedItem(AndOr.OR);
					} else {
						andOrBox.setSelectedItem(AndOr.AND);
					}

					logicalAndOrBoxes.add(andOrBox);
					logicalPanel.add(andOrBox, createConstraints(0, row));
				}

				logicalPropertyBoxes.add(propertyBox);
				logicalTypeBoxes.add(typeBox);
				logicalValueFields.add(valueField);
				logicalAddButtons.add(addButton);
				logicalRemoveButtons.add(removeButton);

				logicalPanel.add(propertyBox, createConstraints(1, row));
				logicalPanel.add(typeBox, createConstraints(2, row));
				logicalPanel.add(valueField, createFillConstraints(3, row));
				logicalPanel.add(addButton, createConstraints(4, row));
				logicalPanel.add(removeButton, createConstraints(5, row));

				row++;
			}
		}

		JButton addButton = new JButton("Add");

		addButton.addActionListener(this);
		logicalAddButtons.add(addButton);
		logicalPanel.add(addButton, createConstraints(4, row));

		return new JScrollPane(UI.createNorthPanel(logicalPanel),
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	}

	private JComponent createValuePanel(ValueHighlightCondition condition) {
		List<String> numberProperties = new ArrayList<String>();

		for (String property : nodeProperties.keySet()) {
			Class<?> type = nodeProperties.get(property);

			if (type == Integer.class || type == Double.class) {
				numberProperties.add(property);
			}
		}

		valuePropertyBox = new JComboBox<String>(
				numberProperties.toArray(new String[0]));
		valueTypeBox = new JComboBox<String>(ValueHighlightCondition.TYPES);

		if (condition != null) {
			valuePropertyBox.setSelectedItem(condition.getProperty());
			valueTypeBox.setSelectedItem(condition.getType());
		}

		JPanel valuePanel = new JPanel();

		valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.X_AXIS));
		valuePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		valuePanel.add(new JLabel("Property:"));
		valuePanel.add(valuePropertyBox);
		valuePanel.add(Box.createHorizontalStrut(5));
		valuePanel.add(new JLabel("Value Type:"));
		valuePanel.add(valueTypeBox);

		return UI.createNorthPanel(UI.createWestPanel(valuePanel));
	}

	private JComponent createLogicalValuePanel(
			LogicalValueHighlightCondition condition) {
		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		if (condition != null) {
			panel.add(createValuePanel(condition.getValueCondition()));
			panel.add(new JSeparator(SwingConstants.HORIZONTAL));
			panel.add(createLogicalPanel(condition.getLogicalCondition()));
		} else {
			panel.add(createValuePanel(null));
			panel.add(new JSeparator(SwingConstants.HORIZONTAL));
			panel.add(createLogicalPanel(null));
		}

		return UI.createNorthPanel(panel);
	}

	private HighlightCondition createCondition() {
		boolean invisible = allowInvisible && invisibleBox.isSelected();
		boolean useThickness = allowThickness && thicknessBox.isSelected();
		String name = allowName && !nameField.getText().isEmpty() ? nameField
				.getText() : null;
		boolean showInLegend = allowColor && legendBox.isEnabled()
				&& legendBox.isSelected();
		Color color;
		String labelProperty;

		if (!allowColor || !colorBox.isEnabled() || !colorBox.isSelected()) {
			color = null;
		} else {
			color = colorButton.getBackground();
		}

		if (allowLabel && !labelBox.getSelectedItem().equals("")) {
			labelProperty = (String) labelBox.getSelectedItem();
		} else {
			labelProperty = null;
		}

		if (type == Type.LOGICAL_CONDITION) {
			return createLogicalCondition(name, showInLegend, color, invisible,
					useThickness, labelProperty);
		} else if (type == Type.VALUE_CONDITION) {
			return createValueCondition(name, showInLegend, color, invisible,
					useThickness, labelProperty);
		} else if (type == Type.LOGICAL_VALUE_CONDITION) {
			return new LogicalValueHighlightCondition(createValueCondition(
					name, showInLegend, color, invisible, useThickness,
					labelProperty), createLogicalCondition(name, showInLegend,
					color, invisible, useThickness, labelProperty));
		}

		return null;
	}

	private AndOrHighlightCondition createLogicalCondition(String name,
			boolean showInLegend, Color color, boolean invisible,
			boolean useThickness, String labelProperty) {
		List<List<LogicalHighlightCondition>> conditions = new ArrayList<List<LogicalHighlightCondition>>();
		List<LogicalHighlightCondition> andList = new ArrayList<LogicalHighlightCondition>();

		for (int i = 0; i < logicalPropertyBoxes.size(); i++) {
			if (i != 0) {
				AndOr operation = (AndOr) logicalAndOrBoxes.get(i - 1)
						.getSelectedItem();

				if (operation == AndOr.OR) {
					conditions.add(andList);
					andList = new ArrayList<LogicalHighlightCondition>();
				}
			}

			String property = (String) logicalPropertyBoxes.get(i)
					.getSelectedItem();
			String type = (String) logicalTypeBoxes.get(i).getSelectedItem();
			String value = logicalValueFields.get(i).getText();

			andList.add(new LogicalHighlightCondition(property, type, value));
		}

		conditions.add(andList);

		return new AndOrHighlightCondition(conditions, name, showInLegend,
				color, invisible, useThickness, labelProperty);
	}

	private ValueHighlightCondition createValueCondition(String name,
			boolean showInLegend, Color color, boolean invisible,
			boolean useThickness, String labelProperty) {
		return new ValueHighlightCondition(
				(String) valuePropertyBox.getSelectedItem(),
				(String) valueTypeBox.getSelectedItem(), name, showInLegend,
				color, invisible, useThickness, labelProperty);
	}

	private void addRemoveButtonPressed(JButton button) {
		boolean addPressed;
		int index;

		if (logicalAddButtons.contains(button)) {
			addPressed = true;
			index = logicalAddButtons.indexOf(button);
		} else {
			addPressed = false;
			index = logicalRemoveButtons.indexOf(button);
		}

		HighlightCondition condition = createCondition();
		AndOrHighlightCondition logicalCondition = null;

		if (condition instanceof AndOrHighlightCondition) {
			logicalCondition = (AndOrHighlightCondition) condition;
		} else if (condition instanceof LogicalValueHighlightCondition) {
			logicalCondition = ((LogicalValueHighlightCondition) condition)
					.getLogicalCondition();
		}

		List<List<LogicalHighlightCondition>> conditions = logicalCondition
				.getConditions();
		LogicalHighlightCondition newCond = new LogicalHighlightCondition(
				nodeProperties.keySet().toArray(new String[0])[0],
				LogicalHighlightCondition.EQUAL_TYPE, "");
		boolean done = false;
		int count = 0;

		for (int i = 0; i < conditions.size(); i++) {
			for (int j = 0; j < conditions.get(i).size(); j++) {
				if (index == count) {
					if (addPressed) {
						conditions.get(i).add(j, newCond);
					} else {
						conditions.get(i).remove(j);

						if (conditions.get(i).isEmpty()) {
							conditions.remove(i);
						}
					}

					done = true;
					break;
				}

				count++;
			}

			if (done) {
				break;
			}
		}

		if (addPressed && !done) {
			conditions.get(conditions.size() - 1).add(newCond);
		}

		remove(conditionPanel);

		if (condition instanceof AndOrHighlightCondition) {
			conditionPanel = createLogicalPanel(new AndOrHighlightCondition(
					conditions, null, false, null, false, false, null));
		} else if (condition instanceof LogicalValueHighlightCondition) {
			conditionPanel = createLogicalValuePanel(new LogicalValueHighlightCondition(
					((LogicalValueHighlightCondition) condition)
							.getValueCondition(),
					new AndOrHighlightCondition(conditions, null, false, null,
							false, false, null)));
		}

		add(conditionPanel, BorderLayout.CENTER);
		pack();
	}

	private static GridBagConstraints createConstraints(int x, int y) {
		return new GridBagConstraints(x, y, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						2, 2, 2, 2), 0, 0);
	}

	private static GridBagConstraints createFillConstraints(int x, int y) {
		return new GridBagConstraints(x, y, 1, 1, 1, 0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(2, 2, 2, 2), 0, 0);
	}
}
