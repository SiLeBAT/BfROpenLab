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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.openkrise.views.TracingConstants;

public class EditableSingleElementPropertiesDialog extends JDialog implements
		ActionListener {

	private static final long serialVersionUID = 1L;

	private Element element;

	private JButton okButton;
	private JButton cancelButton;

	private JTextField caseField;
	private JCheckBox contaminationBox;
	private JCheckBox filterBox;

	private boolean approved;

	public EditableSingleElementPropertiesDialog(Component parent,
			Element element, Map<String, Class<?>> properties) {
		super(SwingUtilities.getWindowAncestor(parent), "Properties",
				DEFAULT_MODALITY_TYPE);
		this.element = element;

		JPanel centerPanel = new JPanel();
		JPanel leftCenterPanel = new JPanel();
		JPanel rightCenterPanel = new JPanel();

		leftCenterPanel.setLayout(new GridLayout(properties.size(), 1, 5, 5));
		rightCenterPanel.setLayout(new GridLayout(properties.size(), 1, 5, 5));

		for (String property : properties.keySet()) {
			Object value = element.getProperties().get(property);

			leftCenterPanel.add(new JLabel(property + ":"));

			if (property.equals(TracingConstants.CASE_WEIGHT_COLUMN)) {
				caseField = new JTextField();

				if (value != null) {
					caseField.setText(value.toString());
				}

				rightCenterPanel.add(caseField);
			} else if (property
					.equals(TracingConstants.CROSS_CONTAMINATION_COLUMN)) {
				contaminationBox = new JCheckBox();

				if (value != null) {
					contaminationBox.setSelected((Boolean) value);
				} else {
					contaminationBox.setSelected(false);
				}

				rightCenterPanel.add(contaminationBox);
			} else if (property.equals(TracingConstants.FILTER_COLUMN)) {
				filterBox = new JCheckBox();

				if (value != null) {
					filterBox.setSelected((Boolean) value);
				} else {
					filterBox.setSelected(false);
				}

				rightCenterPanel.add(filterBox);
			} else {
				JTextField field = new JTextField();

				if (value != null) {
					field.setText(value.toString());
					field.setPreferredSize(new Dimension(field
							.getPreferredSize().width + 5, field
							.getPreferredSize().height));
				}

				field.setEditable(false);
				rightCenterPanel.add(field);
			}
		}

		centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		centerPanel.setLayout(new BorderLayout(5, 5));
		centerPanel.add(leftCenterPanel, BorderLayout.WEST);
		centerPanel.add(rightCenterPanel, BorderLayout.CENTER);

		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		setLayout(new BorderLayout());
		add(new JScrollPane(UI.createNorthPanel(centerPanel)),
				BorderLayout.CENTER);
		add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)),
				BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(parent);
		UI.adjustDialog(this);
	}

	public boolean isApproved() {
		return approved;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			if (caseField != null) {
				if (caseField.getText().isEmpty()) {
					element.getProperties().put(
							TracingConstants.CASE_WEIGHT_COLUMN, null);
				} else {
					try {
						element.getProperties().put(
								TracingConstants.CASE_WEIGHT_COLUMN,
								Double.parseDouble(caseField.getText()));
					} catch (NumberFormatException ex) {
						JOptionPane.showMessageDialog(this,
								"Please enter valid number for "
										+ TracingConstants.CASE_WEIGHT_COLUMN,
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}

			if (contaminationBox != null) {
				element.getProperties().put(
						TracingConstants.CROSS_CONTAMINATION_COLUMN,
						contaminationBox.isSelected());
			}

			if (filterBox != null) {
				element.getProperties().put(TracingConstants.FILTER_COLUMN,
						filterBox.isSelected());
			}

			approved = true;
			dispose();
		} else if (e.getSource() == cancelButton) {
			approved = false;
			dispose();
		}
	}
}
