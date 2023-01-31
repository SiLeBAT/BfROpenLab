/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.core;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.sbml.jsbml.Unit;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.ui.Dialogs;
import de.bund.bfr.knime.ui.DoubleTextField;
import de.bund.bfr.knime.ui.IntTextField;
import de.bund.bfr.knime.ui.KnimeDialog;
import de.bund.bfr.math.Transform;

public class UnitDialog extends KnimeDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	private boolean approved;
	private PmmUnit unit;

	private JComboBox<Transform> transformBox;
	private JButton okButton;
	private JButton cancelButton;

	private JPanel unitsPanel;
	private List<JComboBox<Unit.Kind>> kindBoxes;
	private List<DoubleTextField> multiplierFields;
	private List<IntTextField> scaleFields;
	private List<DoubleTextField> exponentFields;
	private List<JButton> addButtons;
	private List<JButton> removeButtons;

	public UnitDialog(Component owner, PmmUnit initialUnit) {
		super(owner, "Unit Selection", DEFAULT_MODALITY_TYPE);
		approved = false;
		unit = new PmmUnit.Builder().build();

		transformBox = new JComboBox<>(Transform.values());
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		kindBoxes = new ArrayList<>();
		multiplierFields = new ArrayList<>();
		scaleFields = new ArrayList<>();
		exponentFields = new ArrayList<>();
		addButtons = new ArrayList<>();
		removeButtons = new ArrayList<>();

		JButton addButton = new JButton("+");

		addButton.addActionListener(this);
		addButtons.add(addButton);

		if (initialUnit != null) {
			int index = 0;

			for (Unit u : initialUnit.getUnits()) {
				addUnit(index, u.getKind(), u.getMultiplier(), u.getScale(), u.getExponent());
				index++;
			}

			transformBox.setSelectedItem(initialUnit.getTransform());
		} else {
			addUnit(0, SbmlUtils.getUnitKinds()[0], 1.0, 0, 1.0);
		}

		setLayout(new BorderLayout());
		add(UI.createWestPanel(UI.createHorizontalPanel(new JLabel("Transform"), transformBox)), BorderLayout.NORTH);
		add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)), BorderLayout.SOUTH);
		updateUnitsPanel();

		pack();
		UI.adjustDialog(this);
		setLocationRelativeTo(owner);
		getRootPane().setDefaultButton(okButton);
	}

	public boolean isApproved() {
		return approved;
	}

	public PmmUnit getUnit() {
		return unit;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			PmmUnit.Builder builder = new PmmUnit.Builder().transform((Transform) transformBox.getSelectedItem());

			for (int i = 0; i < kindBoxes.size(); i++) {
				JComboBox<Unit.Kind> kindBox = kindBoxes.get(i);
				DoubleTextField multiplierField = multiplierFields.get(i);
				IntTextField scaleField = scaleFields.get(i);
				DoubleTextField exponentField = exponentFields.get(i);

				if (!multiplierField.isValueValid() || !scaleField.isValueValid() || !exponentField.isValueValid()) {
					Dialogs.showErrorMessage(this, "Invalid Settings");
					unit = new PmmUnit.Builder().build();
					return;
				}

				builder.addUnit((Unit.Kind) kindBox.getSelectedItem(), multiplierField.getValue(),
						scaleField.getValue(), exponentField.getValue());
			}

			unit = builder.build();
			approved = true;
			dispose();
		} else if (e.getSource() == cancelButton) {
			dispose();
		} else if (addButtons.contains(e.getSource())) {
			int i = addButtons.indexOf(e.getSource());

			addUnit(i, SbmlUtils.getUnitKinds()[0], 1.0, 0, 1.0);
			updateUnitsPanel();
			revalidate();
			pack();
		} else if (removeButtons.contains(e.getSource())) {
			int i = removeButtons.indexOf(e.getSource());

			kindBoxes.remove(i);
			multiplierFields.remove(i);
			scaleFields.remove(i);
			exponentFields.remove(i);
			addButtons.remove(i);
			removeButtons.remove(i);

			updateUnitsPanel();
			revalidate();
			repaint();
		}
	}

	private void updateUnitsPanel() {
		if (unitsPanel != null) {
			remove(unitsPanel);
		}

		int row = 0;

		JPanel panel = new JPanel();

		panel.setLayout(new GridBagLayout());
		panel.add(new JLabel("Kind"), UI.centerConstraints(0, row));
		panel.add(new JLabel("Multiplier"), UI.centerConstraints(1, row));
		panel.add(new JLabel("Scale"), UI.centerConstraints(2, row));
		panel.add(new JLabel("Exponent"), UI.centerConstraints(3, row));
		row++;

		for (int i = 0; i < kindBoxes.size(); i++) {
			panel.add(kindBoxes.get(i), UI.centerConstraints(0, row));
			panel.add(multiplierFields.get(i), UI.fillConstraints(1, row));
			panel.add(scaleFields.get(i), UI.fillConstraints(2, row));
			panel.add(exponentFields.get(i), UI.fillConstraints(3, row));
			panel.add(addButtons.get(i), UI.centerConstraints(4, row));
			panel.add(removeButtons.get(i), UI.centerConstraints(5, row));
			row++;
		}

		panel.add(addButtons.get(kindBoxes.size()), UI.centerConstraints(4, row));
		unitsPanel = UI.createNorthPanel(panel);
		add(unitsPanel, BorderLayout.CENTER);
	}

	private void addUnit(int index, Unit.Kind kind, Double multiplier, Integer scale, Double exponent) {
		JComboBox<Unit.Kind> kindBox = new JComboBox<>(SbmlUtils.getUnitKinds());
		DoubleTextField multiplierField = new DoubleTextField(false, 5);
		IntTextField scaleField = new IntTextField(false, 5);
		DoubleTextField exponentField = new DoubleTextField(false, 5);
		JButton addButton = new JButton("+");
		JButton removeButton = new JButton("-");

		kindBox.setSelectedItem(kind);
		multiplierField.setValue(multiplier);
		scaleField.setValue(scale);
		exponentField.setValue(exponent);
		addButton.addActionListener(this);
		removeButton.addActionListener(this);

		kindBoxes.add(index, kindBox);
		multiplierFields.add(index, multiplierField);
		scaleFields.add(index, scaleField);
		exponentFields.add(index, exponentField);
		addButtons.add(index, addButton);
		removeButtons.add(index, removeButton);
	}
}
