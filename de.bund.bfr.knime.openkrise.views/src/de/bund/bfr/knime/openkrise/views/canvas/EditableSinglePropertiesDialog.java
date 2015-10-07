/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.google.common.collect.Iterables;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.ui.KnimeDialog;

public class EditableSinglePropertiesDialog extends KnimeDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	private Element element;

	private JButton okButton;
	private JButton cancelButton;

	private JTextField caseField;
	private JCheckBox contaminationBox;
	private JCheckBox killBox;
	private JCheckBox observedBox;

	private boolean approved;

	public EditableSinglePropertiesDialog(Component parent, Element element, Map<String, Class<?>> properties) {
		super(SwingUtilities.getWindowAncestor(parent), "Properties", DEFAULT_MODALITY_TYPE);
		this.element = element;

		Map<String, Object> values = element.getProperties();
		double weight = values.get(TracingColumns.WEIGHT) != null ? (Double) values.get(TracingColumns.WEIGHT) : 0.0;
		boolean crossContamination = values.get(TracingColumns.CROSS_CONTAMINATION) != null
				? (Boolean) values.get(TracingColumns.CROSS_CONTAMINATION) : false;
		boolean killContamination = values.get(TracingColumns.KILL_CONTAMINATION) != null
				? (Boolean) values.get(TracingColumns.KILL_CONTAMINATION) : false;
		boolean observed = values.get(TracingColumns.OBSERVED) != null ? (Boolean) values.get(TracingColumns.OBSERVED)
				: false;

		caseField = new JTextField(8);
		caseField.setText(String.valueOf(weight));
		contaminationBox = new JCheckBox("", crossContamination);
		killBox = new JCheckBox("", killContamination);
		observedBox = new JCheckBox("", observed);

		JPanel inputPanel = UI.createOptionsPanel("Input",
				Arrays.asList(new JLabel(TracingColumns.WEIGHT + ":"),
						new JLabel(TracingColumns.CROSS_CONTAMINATION + ":"),
						new JLabel(TracingColumns.KILL_CONTAMINATION + ":"), new JLabel(TracingColumns.OBSERVED + ":")),
				Arrays.asList(caseField, contaminationBox, killBox, observedBox));
		List<JLabel> tracingLabels = new ArrayList<>();
		List<JTextField> tracingFields = new ArrayList<>();

		for (String column : TracingColumns.OUTPUT_COLUMNS) {
			tracingLabels.add(new JLabel(column + ":"));
			tracingFields.add(createField(values.get(column)));
		}

		JPanel tracingPanel = UI.createOptionsPanel("Tracing", tracingLabels, tracingFields);
		JPanel northPanel = new JPanel();

		northPanel.setLayout(new BorderLayout());
		northPanel.add(inputPanel, BorderLayout.CENTER);
		northPanel.add(tracingPanel, BorderLayout.SOUTH);

		Map<String, Class<?>> otherProperties = new LinkedHashMap<>(properties);

		for (String column : Iterables.concat(TracingColumns.INPUT_COLUMNS, TracingColumns.OUTPUT_COLUMNS)) {
			otherProperties.remove(column);
		}

		JPanel leftCenterPanel = new JPanel();
		JPanel rightCenterPanel = new JPanel();

		leftCenterPanel.setLayout(new GridLayout(otherProperties.size(), 1, 5, 5));
		rightCenterPanel.setLayout(new GridLayout(otherProperties.size(), 1, 5, 5));

		for (String property : otherProperties.keySet()) {
			Object value = element.getProperties().get(property);

			leftCenterPanel.add(new JLabel(property + ":"));
			rightCenterPanel.add(createField(value));
		}

		JPanel centerPanel = new JPanel();

		centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		centerPanel.setLayout(new BorderLayout(5, 5));
		centerPanel.add(leftCenterPanel, BorderLayout.WEST);
		centerPanel.add(rightCenterPanel, BorderLayout.CENTER);

		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		setLayout(new BorderLayout());
		add(northPanel, BorderLayout.NORTH);
		add(new JScrollPane(UI.createNorthPanel(centerPanel)), BorderLayout.CENTER);
		add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)), BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(parent);
		UI.adjustDialog(this, 0.5, 0.8);
		getRootPane().setDefaultButton(okButton);
	}

	public boolean isApproved() {
		return approved;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			if (caseField.getText().isEmpty()) {
				element.getProperties().put(TracingColumns.WEIGHT, 0.0);
			} else {
				try {
					element.getProperties().put(TracingColumns.WEIGHT, Double.parseDouble(caseField.getText()));
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(this, "Please enter valid number for " + TracingColumns.WEIGHT,
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}

			element.getProperties().put(TracingColumns.CROSS_CONTAMINATION, contaminationBox.isSelected());
			element.getProperties().put(TracingColumns.KILL_CONTAMINATION, killBox.isSelected());
			element.getProperties().put(TracingColumns.OBSERVED, observedBox.isSelected());
			approved = true;
			dispose();
		} else if (e.getSource() == cancelButton) {
			approved = false;
			dispose();
		}
	}

	private static JTextField createField(Object obj) {
		JTextField field = new JTextField(obj != null ? obj.toString() : "");

		field.setPreferredSize(new Dimension(field.getPreferredSize().width + 5, field.getPreferredSize().height));
		field.setEditable(false);

		return field;
	}
}
