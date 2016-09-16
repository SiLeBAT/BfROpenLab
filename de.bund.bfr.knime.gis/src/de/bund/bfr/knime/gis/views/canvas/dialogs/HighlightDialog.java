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
package de.bund.bfr.knime.gis.views.canvas.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import com.google.common.base.Strings;

import de.bund.bfr.jung.NamedShape;
import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.util.PropertySchema;
import de.bund.bfr.knime.ui.AutoSuggestField;
import de.bund.bfr.knime.ui.Dialogs;
import de.bund.bfr.knime.ui.KnimeDialog;

public class HighlightDialog extends KnimeDialog {

	private static final long serialVersionUID = 1L;

	private static Color BUTTON_BACKGROUND = new JButton().getBackground();

	private static enum Type {
		LOGICAL_CONDITION("Logical Condition"), APPLY_TO_ALL("Apply To All"), VALUE_CONDITION(
				"Value Condition"), LOGICAL_VALUE_CONDITION("Logical Value Condition");

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
	private Color color;
	private Color lastColor;

	private JComboBox<Type> conditionTypeBox;
	private JTextField nameField;
	private JCheckBox legendBox;
	private JButton colorButton;
	private JCheckBox colorBox;
	private JCheckBox invisibleBox;
	private JCheckBox thicknessBox;
	private JComboBox<String> labelBox;
	private JComboBox<NamedShape> shapeBox;
	private JComponent conditionPanel;
	private JButton okButton;
	private JButton cancelButton;

	private JComboBox<String> valuePropertyBox;
	private JComboBox<ValueHighlightCondition.Type> valueTypeBox;
	private JCheckBox zeroAsMinimumBox;

	private List<JComboBox<AndOr>> logicalAndOrBoxes;
	private List<PropertySelector> logicalPropertyBoxes;
	private List<JComboBox<LogicalHighlightCondition.Type>> logicalTypeBoxes;
	private List<AutoSuggestField> logicalValueFields;
	private List<JButton> logicalAddButtons;
	private List<JButton> logicalRemoveButtons;

	private PropertySchema schema;
	private boolean allowName;
	private boolean allowColor;
	private boolean allowInvisible;
	private boolean allowThickness;
	private boolean allowLabel;
	private boolean allowShape;
	private List<HighlightConditionChecker> checkers;

	private PropertySelectorCreator selectorCreator;

	private HighlightCondition condition;
	private boolean approved;

	public static HighlightDialog createFilterDialog(Component owner, PropertySchema schema,
			HighlightCondition condition, PropertySelectorCreator selectorCreator) {
		return new HighlightDialog(owner, schema, false, false, false, false, false, false, true, condition, null,
				selectorCreator);
	}

	public static HighlightDialog createHighlightDialog(Component owner, PropertySchema schema, boolean allowInvisible,
			boolean allowThickness, boolean allowShape, HighlightCondition condition,
			List<HighlightConditionChecker> checkers, PropertySelectorCreator selectorCreator) {
		return new HighlightDialog(owner, schema, true, true, allowInvisible, allowThickness, allowShape, true, false,
				condition, checkers, selectorCreator);
	}

	private HighlightDialog(Component owner, PropertySchema schema, boolean allowName, boolean allowColor,
			boolean allowInvisible, boolean allowThickness, boolean allowShape, boolean allowLabel,
			boolean onlyAllowLogical, HighlightCondition condition, List<HighlightConditionChecker> checkers,
			PropertySelectorCreator selectorCreator) {
		super(owner, "Highlight Condition", DEFAULT_MODALITY_TYPE);
		this.schema = schema;
		this.allowName = allowName;
		this.allowColor = allowColor;
		this.allowInvisible = allowInvisible;
		this.allowThickness = allowThickness;
		this.allowLabel = allowLabel;
		this.allowShape = allowShape;
		this.checkers = checkers;
		this.selectorCreator = selectorCreator;
		this.condition = null;
		approved = false;
		lastColor = Color.RED;

		HighlightCondition initCondition = condition != null ? condition : createAndOrCondition();

		if (initCondition instanceof AndOrHighlightCondition) {
			AndOrHighlightCondition c = (AndOrHighlightCondition) initCondition;

			if (c.getConditionCount() == 0 && !onlyAllowLogical) {
				type = Type.APPLY_TO_ALL;
				conditionPanel = createApplyToAllPanel();
			} else {
				type = Type.LOGICAL_CONDITION;
				conditionPanel = createLogicalPanel(c);
			}
		} else if (initCondition instanceof ValueHighlightCondition) {
			type = Type.VALUE_CONDITION;
			conditionPanel = createValuePanel((ValueHighlightCondition) initCondition);
		} else if (initCondition instanceof LogicalValueHighlightCondition) {
			type = Type.LOGICAL_VALUE_CONDITION;
			conditionPanel = createLogicalValuePanel((LogicalValueHighlightCondition) initCondition);
		}

		conditionTypeBox = new JComboBox<>(onlyAllowLogical ? new Type[] { Type.LOGICAL_CONDITION } : Type.values());
		conditionTypeBox.setSelectedItem(type);
		conditionTypeBox.addItemListener(UI.newItemSelectListener(e -> conditionTypeChanged()));
		nameField = new JTextField(20);
		nameField.setText(Strings.nullToEmpty(initCondition.getName()));
		nameField.getDocument().addDocumentListener(UI.newDocumentActionListener(e -> updateOptionsPanel()));
		legendBox = new JCheckBox("Show In Legend");
		legendBox.setSelected(initCondition.isShowInLegend());
		colorButton = new JButton("     ");
		colorButton.setContentAreaFilled(false);
		colorButton.setOpaque(true);
		colorButton.addActionListener(e -> colorButtonPressed());
		colorBox = new JCheckBox("Use Color");
		colorBox.setSelected(initCondition.getColor() != null);
		colorBox.addActionListener(e -> updateOptionsPanel());
		invisibleBox = new JCheckBox("Invisible");
		invisibleBox.setSelected(initCondition.isInvisible());
		invisibleBox.addActionListener(e -> updateOptionsPanel());
		thicknessBox = new JCheckBox("Adjust Thickness");
		thicknessBox.setSelected(initCondition.isUseThickness());
		labelBox = new JComboBox<>(schema.getMap().keySet().toArray(new String[0]));
		labelBox.insertItemAt(null, 0);
		labelBox.setSelectedItem(initCondition.getLabelProperty());
		shapeBox = new JComboBox<>(NamedShape.values());
		shapeBox.insertItemAt(null, 0);
		shapeBox.setSelectedItem(initCondition.getShape());
		shapeBox.addActionListener(e -> updateOptionsPanel());

		setNewColor(initCondition.getColor());

		JPanel optionsPanel = new JPanel();

		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.X_AXIS));
		optionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		if (!onlyAllowLogical) {
			optionsPanel.add(new JLabel("Type:"));
			optionsPanel.add(conditionTypeBox);
		}

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

		if (allowShape) {
			optionsPanel.add(Box.createHorizontalStrut(5));
			optionsPanel.add(new JLabel("Shape:"));
			optionsPanel.add(shapeBox);
		}

		okButton = new JButton("OK");
		okButton.addActionListener(e -> okButtonPressed());
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> dispose());

		JPanel bottomPanel = new JPanel();

		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(new JSeparator(), BorderLayout.NORTH);
		bottomPanel.add(UI.createHorizontalPanel(okButton, cancelButton), BorderLayout.EAST);

		setLayout(new BorderLayout());
		add(conditionPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);

		if (optionsPanel.getComponentCount() != 0) {
			JPanel upperPanel = new JPanel();

			upperPanel.setLayout(new BorderLayout());
			upperPanel.add(UI.createWestPanel(optionsPanel), BorderLayout.CENTER);
			upperPanel.add(new JSeparator(), BorderLayout.SOUTH);
			add(upperPanel, BorderLayout.NORTH);
		}

		updateOptionsPanel();

		pack();
		UI.adjustDialog(this);
		setLocationRelativeTo(owner);
		getRootPane().setDefaultButton(okButton);
	}

	public HighlightCondition getHighlightCondition() {
		return condition;
	}

	public boolean isApproved() {
		return approved;
	}

	private void logicalPropertyChanged(PropertySelector propertyBox) {
		AutoSuggestField valueField = logicalValueFields.get(logicalPropertyBoxes.indexOf(propertyBox));
		Set<String> possibleValues = schema.getPossibleValues().get(propertyBox.getSelectedProperty());

		valueField.setPossibleValues(possibleValues);
	}

	private void updateOptionsPanel() {
		boolean invisible = allowInvisible && invisibleBox.isSelected();

		colorBox.setEnabled(!invisible);
		thicknessBox.setEnabled(!invisible);
		labelBox.setEnabled(!invisible);
		shapeBox.setEnabled(!invisible);

		boolean colorSelected = allowColor && colorBox.isEnabled() && colorBox.isSelected();
		boolean shapeSelected = allowShape && shapeBox.isEnabled() && shapeBox.getSelectedItem() != null;

		setNewColor(colorSelected ? lastColor : null);
		colorButton.setEnabled(colorSelected);
		legendBox.setEnabled(!nameField.getText().isEmpty() && (colorSelected || shapeSelected));
	}

	private JComponent createLogicalPanel(AndOrHighlightCondition condition) {
		AndOrHighlightCondition initCondition = condition != null ? condition : createAndOrCondition();

		logicalAndOrBoxes = new ArrayList<>();
		logicalPropertyBoxes = new ArrayList<>();
		logicalTypeBoxes = new ArrayList<>();
		logicalValueFields = new ArrayList<>();
		logicalAddButtons = new ArrayList<>();
		logicalRemoveButtons = new ArrayList<>();

		JPanel logicalPanel = new JPanel();
		int row = 0;

		if (initCondition.getConditionCount() != 0) {
			logicalPanel.setLayout(new GridBagLayout());
			logicalPanel.add(new JLabel("Property"), UI.centerConstraints(1, row));
			logicalPanel.add(new JLabel("Operation"), UI.centerConstraints(2, row));
			logicalPanel.add(new JLabel("Value"), UI.centerConstraints(3, row));
			row++;
		}

		for (List<LogicalHighlightCondition> conds : initCondition.getConditions()) {
			boolean first = true;

			for (LogicalHighlightCondition cond : conds) {
				PropertySelector propertyBox = selectorCreator.createSelector(schema);
				JComboBox<LogicalHighlightCondition.Type> typeBox = new JComboBox<>(
						LogicalHighlightCondition.Type.values());
				AutoSuggestField valueField = new AutoSuggestField(30);
				Set<String> possibleValues = schema.getPossibleValues().get(cond.getProperty());
				JButton addButton = new JButton("Add");
				JButton removeButton = new JButton("Remove");

				propertyBox.setSelectedProperty(cond.getProperty());
				propertyBox.addItemListener(UI.newItemSelectListener(e -> logicalPropertyChanged(propertyBox)));
				typeBox.setSelectedItem(cond.getType());
				valueField.setPossibleValues(possibleValues);
				valueField.setSelectedItem(cond.getValue());

				addButton.addActionListener(e -> addRemoveButtonPressed(addButton));
				removeButton.addActionListener(e -> addRemoveButtonPressed(removeButton));

				if (row != 1) {
					JComboBox<AndOr> andOrBox = new JComboBox<>(AndOr.values());

					andOrBox.setSelectedItem(first ? AndOr.OR : AndOr.AND);
					logicalAndOrBoxes.add(andOrBox);
					logicalPanel.add(andOrBox, UI.centerConstraints(0, row));
				}

				logicalPropertyBoxes.add(propertyBox);
				logicalTypeBoxes.add(typeBox);
				logicalValueFields.add(valueField);
				logicalAddButtons.add(addButton);
				logicalRemoveButtons.add(removeButton);

				logicalPanel.add(propertyBox.getComponent(), UI.centerConstraints(1, row));
				logicalPanel.add(typeBox, UI.centerConstraints(2, row));
				logicalPanel.add(valueField, UI.fillConstraints(3, row));
				logicalPanel.add(addButton, UI.centerConstraints(4, row));
				logicalPanel.add(removeButton, UI.centerConstraints(5, row));

				row++;
				first = false;
			}
		}

		JButton addButton = new JButton("Add");

		addButton.addActionListener(e -> addRemoveButtonPressed(addButton));
		logicalAddButtons.add(addButton);
		logicalPanel.add(addButton, UI.centerConstraints(4, row));

		return new JScrollPane(UI.createNorthPanel(logicalPanel), ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	}

	private JComponent createApplyToAllPanel() {
		logicalAndOrBoxes = new ArrayList<>();
		logicalPropertyBoxes = new ArrayList<>();
		logicalTypeBoxes = new ArrayList<>();
		logicalValueFields = new ArrayList<>();
		logicalAddButtons = new ArrayList<>();
		logicalRemoveButtons = new ArrayList<>();

		return new JPanel();
	}

	private JComponent createValuePanel(ValueHighlightCondition condition) {
		List<String> numberProperties = new ArrayList<>();

		for (String property : schema.getMap().keySet()) {
			Class<?> type = schema.getMap().get(property);

			if (type == Integer.class || type == Double.class) {
				numberProperties.add(property);
			}
		}

		valuePropertyBox = new JComboBox<>(new Vector<>(numberProperties));
		valueTypeBox = new JComboBox<>(ValueHighlightCondition.Type.values());
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

	private JComponent createLogicalValuePanel(LogicalValueHighlightCondition condition) {
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
		boolean useThickness = allowThickness && thicknessBox.isEnabled() && thicknessBox.isSelected();
		String name = allowName ? Strings.emptyToNull(nameField.getText().trim()) : null;
		boolean showInLegend = (allowColor || allowShape) && legendBox.isEnabled() && legendBox.isSelected();
		Color color = allowColor && colorBox.isEnabled() && colorBox.isSelected() ? this.color : null;
		String labelProperty = allowLabel && labelBox.isEnabled() ? (String) labelBox.getSelectedItem() : null;
		NamedShape shape = allowShape && shapeBox.isEnabled() ? (NamedShape) shapeBox.getSelectedItem() : null;

		switch (type) {
		case LOGICAL_CONDITION:
		case APPLY_TO_ALL:
			return createLogicalCondition(name, showInLegend, color, invisible, useThickness, labelProperty, shape);
		case VALUE_CONDITION:
			return createValueCondition(name, showInLegend, color, invisible, useThickness, labelProperty, shape);
		case LOGICAL_VALUE_CONDITION:
			return new LogicalValueHighlightCondition(
					createValueCondition(name, showInLegend, color, invisible, useThickness, labelProperty, shape),
					createLogicalCondition(name, showInLegend, color, invisible, useThickness, labelProperty, shape));
		default:
			throw new RuntimeException("Unknown type of HighlightCondition: " + type);
		}
	}

	private AndOrHighlightCondition createLogicalCondition(String name, boolean showInLegend, Color color,
			boolean invisible, boolean useThickness, String labelProperty, NamedShape shape) {
		List<List<LogicalHighlightCondition>> conditions = new ArrayList<>();
		List<LogicalHighlightCondition> andList = new ArrayList<>();

		for (int i = 0; i < logicalPropertyBoxes.size(); i++) {
			if (i != 0) {
				AndOr operation = (AndOr) logicalAndOrBoxes.get(i - 1).getSelectedItem();

				if (operation == AndOr.OR) {
					conditions.add(andList);
					andList = new ArrayList<>();
				}
			}

			String property = logicalPropertyBoxes.get(i).getSelectedProperty();
			LogicalHighlightCondition.Type type = (LogicalHighlightCondition.Type) logicalTypeBoxes.get(i)
					.getSelectedItem();
			String value = (String) logicalValueFields.get(i).getSelectedItem();

			andList.add(new LogicalHighlightCondition(property, type, value));
		}

		conditions.add(andList);

		return new AndOrHighlightCondition(conditions, name, showInLegend, color, invisible, useThickness,
				labelProperty, shape);
	}

	private ValueHighlightCondition createValueCondition(String name, boolean showInLegend, Color color,
			boolean invisible, boolean useThickness, String labelProperty, NamedShape shape) {
		return new ValueHighlightCondition((String) valuePropertyBox.getSelectedItem(),
				(ValueHighlightCondition.Type) valueTypeBox.getSelectedItem(), zeroAsMinimumBox.isSelected(), name,
				showInLegend, color, invisible, useThickness, labelProperty, shape);
	}

	private void okButtonPressed() {
		condition = createCondition();

		Optional<String> error = KnimeUtils.nullToEmpty(checkers).stream().map(c -> c.findError(condition))
				.filter(Objects::nonNull).findFirst();

		if (error.isPresent()) {
			Dialogs.showErrorMessage(okButton, error.get());
		} else {
			approved = true;
			dispose();
		}
	}

	private void colorButtonPressed() {
		Color newColor = Dialogs.showColorChooser(colorButton, "Choose Color", color);

		if (newColor != null) {
			setNewColor(newColor);
		}
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
			logicalCondition = ((LogicalValueHighlightCondition) condition).getLogicalCondition();
		} else {
			return;
		}

		List<List<LogicalHighlightCondition>> conditions = logicalCondition.getConditions();
		LogicalHighlightCondition newCond = createLogicalCondition();
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
							conditions.add(i, new ArrayList<>());
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

			if (lastCond != null && conditions.size() != 1 && conditions.get(conditions.size() - 1).size() == 1) {
				conditions.add(new ArrayList<>());
				conditions.get(conditions.size() - 1).add(newCond);
			} else {
				conditions.get(conditions.size() - 1).add(newCond);
			}
		}

		remove(conditionPanel);

		if (condition instanceof AndOrHighlightCondition) {
			conditionPanel = createLogicalPanel(
					new AndOrHighlightCondition(conditions, null, false, null, false, false, null, null));
		} else if (condition instanceof LogicalValueHighlightCondition) {
			conditionPanel = createLogicalValuePanel(
					new LogicalValueHighlightCondition(((LogicalValueHighlightCondition) condition).getValueCondition(),
							new AndOrHighlightCondition(conditions, null, false, null, false, false, null, null)));
		}

		add(conditionPanel, BorderLayout.CENTER);
		pack();
	}

	private void conditionTypeChanged() {
		remove(conditionPanel);

		switch ((Type) conditionTypeBox.getSelectedItem()) {
		case LOGICAL_CONDITION:
			if (type == Type.LOGICAL_VALUE_CONDITION) {
				LogicalValueHighlightCondition c = (LogicalValueHighlightCondition) createCondition();

				conditionPanel = createLogicalPanel(c.getLogicalCondition());
			} else {
				conditionPanel = createLogicalPanel(null);
			}

			break;
		case APPLY_TO_ALL:
			conditionPanel = createApplyToAllPanel();
			break;
		case VALUE_CONDITION:
			if (type == Type.LOGICAL_VALUE_CONDITION) {
				LogicalValueHighlightCondition c = (LogicalValueHighlightCondition) createCondition();

				conditionPanel = createValuePanel(c.getValueCondition());
			} else {
				conditionPanel = createValuePanel(null);
			}

			break;
		case LOGICAL_VALUE_CONDITION:
			if (type == Type.LOGICAL_CONDITION) {
				AndOrHighlightCondition c = (AndOrHighlightCondition) createCondition();

				conditionPanel = createLogicalValuePanel(new LogicalValueHighlightCondition(null, c));
			} else if (type == Type.VALUE_CONDITION) {
				ValueHighlightCondition c = (ValueHighlightCondition) createCondition();

				conditionPanel = createLogicalValuePanel(new LogicalValueHighlightCondition(c, null));
			} else {
				conditionPanel = createLogicalValuePanel(null);
			}

			break;
		default:
			throw new RuntimeException("Unknown type of HighlightCondition: " + type);
		}

		type = (Type) conditionTypeBox.getSelectedItem();
		add(conditionPanel, BorderLayout.CENTER);
		pack();
	}

	private void setNewColor(Color c) {
		color = c;

		if (c != null) {
			lastColor = c;
		}

		if (c != null) {
			double alpha = c.getAlpha() / 255.0;
			double r = alpha * c.getRed() / 255.0 + (1 - alpha) * BUTTON_BACKGROUND.getRed() / 255.0;
			double g = alpha * c.getGreen() / 255.0 + (1 - alpha) * BUTTON_BACKGROUND.getGreen() / 255.0;
			double b = alpha * c.getBlue() / 255.0 + (1 - alpha) * BUTTON_BACKGROUND.getBlue() / 255.0;

			colorButton.setBackground(new Color((float) r, (float) g, (float) b));
		} else {
			colorButton.setBackground(BUTTON_BACKGROUND);
		}
	}

	private LogicalHighlightCondition createLogicalCondition() {
		return new LogicalHighlightCondition(schema.getMap().keySet().stream().findFirst().get(),
				LogicalHighlightCondition.Type.EQUAL, "");
	}

	private AndOrHighlightCondition createAndOrCondition() {
		return new AndOrHighlightCondition(createLogicalCondition(), null, true, Color.RED, false, false, null, null);
	}
}
