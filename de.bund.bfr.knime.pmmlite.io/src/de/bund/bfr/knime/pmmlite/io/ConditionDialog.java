/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.io;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.ui.Dialogs;
import de.bund.bfr.knime.ui.DoubleTextField;
import de.bund.bfr.knime.ui.KnimeDialog;
import de.bund.bfr.knime.ui.StringTextField;

public class ConditionDialog extends KnimeDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	private boolean approved;
	private String conditionName;
	private Double conditionValue;

	private StringTextField nameField;
	private DoubleTextField valueField;
	private JButton okButton;
	private JButton cancelButton;

	public ConditionDialog(Component owner) {
		super(owner, "Specify Condition", DEFAULT_MODALITY_TYPE);

		approved = false;
		conditionName = null;
		conditionValue = null;

		nameField = new StringTextField(false, 16);
		valueField = new DoubleTextField(false, 16);
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new GridBagLayout());
		mainPanel.add(new JLabel("Name:"), UI.westConstraints(0, 0));
		mainPanel.add(nameField, UI.westConstraints(1, 0));
		mainPanel.add(new JLabel("Value:"), UI.westConstraints(0, 1));
		mainPanel.add(valueField, UI.westConstraints(1, 1));

		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);
		add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)), BorderLayout.SOUTH);

		pack();
		setResizable(false);
		setLocationRelativeTo(owner);
		getRootPane().setDefaultButton(okButton);
	}

	public boolean isApproved() {
		return approved;
	}

	public String getConditionName() {
		return conditionName;
	}

	public Double getConditionValue() {
		return conditionValue;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			if (!nameField.isValueValid()) {
				Dialogs.showErrorMessage(okButton, "Invalid Name");
			} else if (!valueField.isValueValid()) {
				Dialogs.showErrorMessage(okButton, "Invalid Value");
			} else {
				approved = true;
				conditionName = nameField.getValue();
				conditionValue = valueField.getValue();
				dispose();
			}
		} else if (e.getSource() == cancelButton) {
			approved = false;
			dispose();
		}
	}
}
