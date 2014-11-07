/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
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
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition;

public class HighlightDialog extends JDialog implements ActionListener,
		DocumentListener {

	private static final long serialVersionUID = 1L;

	private static Color BUTTON_BACKGROUND = UIManager.getDefaults().getColor(
			"Button.background");

	private static enum Type {
		LOGICAL_CONDITION("Logical Condition"), VALUE_CONDITION(
				"Value Condition"), LOGICAL_VALUE_CONDITION(
				"Logical Value Condition");

		private String name;

		private Type(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private static enum AndOr {
		AND("And"), OR("Or");

		private String name;

		private AndOr(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private Type type;
	private Color lastColor;

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
	private JCheckBox zeroAsMinimumBox;

	private List<JComboBox<AndOr>> logicalAndOrBoxes;
	private List<JComboBox<String>> logicalPropertyBoxes;
	private List<JComboBox<String>> logicalTypeBoxes;
	private List<JTextField> logicalValueFields;
	private List<JButton> logicalAddButtons;
	private List<JButton> logicalRemoveButtons;

	private Map<String, Class<?>> properties;
	private boolean allowName;
	private boolean allowColor;
	private boolean allowInvisible;
	private boolean allowThickness;
	private boolean allowLabel;
	private List<HighlightConditionChecker> checkers;

	private HighlightCondition condition;
	private boolean approved;

	public HighlightDialog(Component parent, Map<String, Class<?>> properties,
			boolean allowName, boolean allowColor, boolean allowInvisible,
			boolean allowThickness, boolean allowLabel,
			boolean allowValueCondition, HighlightCondition condition,
			List<HighlightConditionChecker> checkers) {
		super(SwingUtilities.getWindowAncestor(parent), "Highlight Condition",
				DEFAULT_MODALITY_TYPE);
		this.properties = properties;
		this.allowName = allowName;
		this.allowColor = allowColor;
		this.allowInvisible = allowInvisible;
		this.allowThickness = allowThickness;
		this.allowLabel = allowLabel;
		this.checkers = checkers;
		this.condition = null;
		approved = false;
		lastColor = Color.RED;

		if (condition == null) {
			condition = new AndOrHighlightCondition(Arrays.asList(Arrays
					.asList(new LogicalHighlightCondition(properties.keySet()
							.toArray(new String[0])[0],
							LogicalHighlightCondition.EQUAL_TYPE, ""))), null,
					true, lastColor, false, false, null);
		}

		if (condition instanceof AndOrHighlightCondition) {
			type = Type.LOGICAL_CONDITION;
			conditionPanel = createLogicalPanel((AndOrHighlightCondition) condition);
		} else if (condition instanceof ValueHighlightCondition) {
			type = Type.VALUE_CONDITION;
			conditionPanel = createValuePanel((ValueHighlightCondition) condition);
		} else if (condition instanceof LogicalValueHighlightCondition) {
			type = Type.LOGICAL_VALUE_CONDITION;
			conditionPanel = createLogicalValuePanel((LogicalValueHighlightCondition) condition);
		}

		conditionTypeBox = new JComboBox<>(allowValueCondition ? Type.values()
				: new Type[] { Type.LOGICAL_CONDITION });
		conditionTypeBox.setSelectedItem(type);
		conditionTypeBox.addActionListener(this);
		nameField = new JTextField(20);
		nameField.setText(condition.getName() != null ? condition.getName()
				: "");
		nameField.getDocument().addDocumentListener(this);
		legendBox = new JCheckBox("Show In Legend");
		legendBox.setSelected(condition.isShowInLegend());
		colorButton = new JButton("     ");
		colorButton.setContentAreaFilled(false);
		colorButton.setOpaque(true);
		colorButton.setBackground(condition.getColor() != null ? condition
				.getColor() : BUTTON_BACKGROUND);
		colorButton.addActionListener(this);
		colorBox = new JCheckBox("Use Color");
		colorBox.setSelected(condition.getColor() != null);
		colorBox.addActionListener(this);
		invisibleBox = new JCheckBox("Invisible");
		invisibleBox.setSelected(condition.isInvisible());
		invisibleBox.addActionListener(this);
		thicknessBox = new JCheckBox("Adjust Thickness");
		thicknessBox.setSelected(condition.isUseThickness());
		labelBox = new JComboBox<>(properties.keySet().toArray(new String[0]));
		labelBox.insertItemAt("", 0);
		labelBox.setSelectedItem(condition.getLabelProperty() != null ? condition
				.getLabelProperty() : "");

		JPanel optionsPanel = new JPanel();

		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.X_AXIS));
		optionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		optionsPanel.add(new JLabel("Type:"));
		optionsPanel.add(conditionTypeBox);

		if (allowName) {
			optionsPanel.add(Box.createHorizontalStrut(5));
			optionsPanel.add(new JLabel("Name:"));
			optionsPanel.add(nameField);
		}

		if (allowColor) {
			optionsPanel.add(Box.createHorizontalStrut(5));
			optionsPanel.add(legendBox);
			optionsPanel.add(Box.createHorizontalStrut(5));
			optionsPanel.add(new JLabel("Color:"));
			optionsPanel.add(colorButton);
			optionsPanel.add(Box.createHorizontalStrut(5));
			optionsPanel.add(colorBox);
		}

		if (allowInvisible) {
			optionsPanel.add(Box.createHorizontalStrut(5));
			optionsPanel.add(invisibleBox);
		}

		if (allowThickness) {
			optionsPanel.add(Box.createHorizontalStrut(5));
			optionsPanel.add(thicknessBox);
		}

		if (allowLabel) {
			optionsPanel.add(Box.createHorizontalStrut(5));
			optionsPanel.add(new JLabel("Label:"));
			optionsPanel.add(labelBox);
		}

		JPanel upperPanel = new JPanel();

		upperPanel.setLayout(new BorderLayout());
		upperPanel.add(UI.createWestPanel(optionsPanel), BorderLayout.CENTER);
		upperPanel.add(new JSeparator(), BorderLayout.SOUTH);

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
		updateOptionsPanel();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			String error = null;

			condition = createCondition();

			if (checkers != null) {
				for (HighlightConditionChecker checker : checkers) {
					if (checker != null) {
						error = checker.findError(condition);
						break;
					}
				}
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
			updateOptionsPanel();
		} else if (e.getSource() == invisibleBox) {
			updateOptionsPanel();
		} else if (logicalAddButtons.contains(e.getSource())) {
			addRemoveButtonPressed((JButton) e.getSource());
		} else if (logicalRemoveButtons.contains(e.getSource())) {
			addRemoveButtonPressed((JButton) e.getSource());
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		updateOptionsPanel();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		updateOptionsPanel();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateOptionsPanel();
	}

	public HighlightCondition getHighlightCondition() {
		return condition;
	}

	public boolean isApproved() {
		return approved;
	}

	private void updateOptionsPanel() {
		if (colorButton.getBackground() != BUTTON_BACKGROUND) {
			lastColor = colorButton.getBackground();
		}

		if (allowInvisible && invisibleBox.isSelected()) {
			colorBox.setEnabled(false);
			thicknessBox.setEnabled(false);
			labelBox.setEnabled(false);
		} else {
			colorBox.setEnabled(true);
			thicknessBox.setEnabled(true);
			labelBox.setEnabled(true);
		}

		if (allowColor && colorBox.isEnabled() && colorBox.isSelected()) {
			colorButton.setBackground(lastColor);
			colorButton.setEnabled(true);
			legendBox.setEnabled(!nameField.getText().isEmpty());
		} else {
			colorButton.setBackground(BUTTON_BACKGROUND);
			colorButton.setEnabled(false);
			legendBox.setEnabled(false);
		}
	}

	private JComponent createLogicalPanel(AndOrHighlightCondition condition) {
		logicalAndOrBoxes = new ArrayList<>();
		logicalPropertyBoxes = new ArrayList<>();
		logicalTypeBoxes = new ArrayList<>();
		logicalValueFields = new ArrayList<>();
		logicalAddButtons = new ArrayList<>();
		logicalRemoveButtons = new ArrayList<>();

		JPanel logicalPanel = new JPanel();
		int row = 0;

		logicalPanel.setLayout(new GridBagLayout());
		logicalPanel.add(new JLabel("Property"), UI.centerConstraints(1, row));
		logicalPanel.add(new JLabel("Operation"), UI.centerConstraints(2, row));
		logicalPanel.add(new JLabel("Value"), UI.centerConstraints(3, row));
		row++;

		if (condition == null) {
			condition = new AndOrHighlightCondition(Arrays.asList(Arrays
					.asList(new LogicalHighlightCondition(properties.keySet()
							.toArray(new String[0])[0],
							LogicalHighlightCondition.EQUAL_TYPE, ""))), null,
					false, null, false, false, null);
		}

		for (int i = 0; i < condition.getConditions().size(); i++) {
			for (int j = 0; j < condition.getConditions().get(i).size(); j++) {
				LogicalHighlightCondition cond = condition.getConditions()
						.get(i).get(j);

				JComboBox<String> propertyBox = new JComboBox<>(new Vector<>(
						properties.keySet()));
				JComboBox<String> typeBox = new JComboBox<>(
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
					JComboBox<AndOr> andOrBox = new JComboBox<>(AndOr.values());

					if (j == 0) {
						andOrBox.setSelectedItem(AndOr.OR);
					} else {
						andOrBox.setSelectedItem(AndOr.AND);
					}

					logicalAndOrBoxes.add(andOrBox);
					logicalPanel.add(andOrBox, UI.centerConstraints(0, row));
				}

				logicalPropertyBoxes.add(propertyBox);
				logicalTypeBoxes.add(typeBox);
				logicalValueFields.add(valueField);
				logicalAddButtons.add(addButton);
				logicalRemoveButtons.add(removeButton);

				logicalPanel.add(propertyBox, UI.centerConstraints(1, row));
				logicalPanel.add(typeBox, UI.centerConstraints(2, row));
				logicalPanel.add(valueField, UI.fillConstraints(3, row));
				logicalPanel.add(addButton, UI.centerConstraints(4, row));
				logicalPanel.add(removeButton, UI.centerConstraints(5, row));

				row++;
			}
		}

		JButton addButton = new JButton("Add");

		addButton.addActionListener(this);
		logicalAddButtons.add(addButton);
		logicalPanel.add(addButton, UI.centerConstraints(4, row));

		return new JScrollPane(UI.createNorthPanel(logicalPanel),
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	}

	private JComponent createValuePanel(ValueHighlightCondition condition) {
		List<String> numberProperties = new ArrayList<>();

		for (String property : properties.keySet()) {
			Class<?> type = properties.get(property);

			if (type == Integer.class || type == Double.class) {
				numberProperties.add(property);
			}
		}

		valuePropertyBox = new JComboBox<>(new Vector<>(numberProperties));
		valueTypeBox = new JComboBox<>(ValueHighlightCondition.TYPES);
		zeroAsMinimumBox = new JCheckBox("Zero As Minimum");

		if (condition != null) {
			valuePropertyBox.setSelectedItem(condition.getProperty());
			valueTypeBox.setSelectedItem(condition.getType());
			zeroAsMinimumBox.setSelected(condition.isZeroAsMinimum());
		}

		JPanel valuePanel = new JPanel();

		valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.X_AXIS));
		valuePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		valuePanel.add(new JLabel("Property:"));
		valuePanel.add(valuePropertyBox);
		valuePanel.add(Box.createHorizontalStrut(5));
		valuePanel.add(new JLabel("Value Type:"));
		valuePanel.add(valueTypeBox);
		valuePanel.add(Box.createHorizontalStrut(5));
		valuePanel.add(zeroAsMinimumBox);

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
		Color color = null;
		String labelProperty = null;

		if (allowColor && colorBox.isEnabled() && colorBox.isSelected()) {
			color = colorButton.getBackground();
		}

		if (allowLabel && !labelBox.getSelectedItem().equals("")) {
			labelProperty = (String) labelBox.getSelectedItem();
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
		List<List<LogicalHighlightCondition>> conditions = new ArrayList<>();
		List<LogicalHighlightCondition> andList = new ArrayList<>();

		for (int i = 0; i < logicalPropertyBoxes.size(); i++) {
			if (i != 0) {
				AndOr operation = (AndOr) logicalAndOrBoxes.get(i - 1)
						.getSelectedItem();

				if (operation == AndOr.OR) {
					conditions.add(andList);
					andList = new ArrayList<>();
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
				(String) valueTypeBox.getSelectedItem(),
				zeroAsMinimumBox.isSelected(), name, showInLegend, color,
				invisible, useThickness, labelProperty);
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
				properties.keySet().toArray(new String[0])[0],
				LogicalHighlightCondition.EQUAL_TYPE, "");
		LogicalHighlightCondition lastCond = null;
		boolean done = false;
		int count = 0;

		loop: for (int i = 0; i < conditions.size(); i++) {
			for (int j = 0; j < conditions.get(i).size(); j++) {
				if (index == count) {
					if (addPressed) {
						if (lastCond != null) {
							newCond.setProperty(lastCond.getProperty());
							newCond.setType(lastCond.getType());
						}

						if (lastCond != null && j == 0 && i != 0) {
							conditions.add(i,
									new ArrayList<LogicalHighlightCondition>());
							conditions.get(i).add(newCond);
						} else {
							conditions.get(i).add(j, newCond);
						}
					} else {
						conditions.get(i).remove(j);

						if (conditions.get(i).isEmpty()) {
							conditions.remove(i);
						}
					}

					done = true;
					break loop;
				}

				lastCond = conditions.get(i).get(j);
				count++;
			}
		}

		if (addPressed && !done) {
			if (lastCond != null) {
				newCond.setProperty(lastCond.getProperty());
				newCond.setType(lastCond.getType());
			}

			if (lastCond != null && conditions.size() != 1
					&& conditions.get(conditions.size() - 1).size() == 1) {
				conditions.add(new ArrayList<LogicalHighlightCondition>());
				conditions.get(conditions.size() - 1).add(newCond);
			} else {
				conditions.get(conditions.size() - 1).add(newCond);
			}
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
}
