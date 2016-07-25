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
package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JPanel;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.ui.KnimeDialog;

public class IngredientAssignerDialog extends KnimeDialog {

	private static final long serialVersionUID = 1L;

	private boolean approved;

	public IngredientAssignerDialog(Component parent) {
		super(parent, "Assign Ingredients", DEFAULT_MODALITY_TYPE);

		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");

		okButton.addActionListener(e -> okButtonPressed());
		cancelButton.addActionListener(e -> dispose());

		setLayout(new BorderLayout());
		add(new JPanel(), BorderLayout.CENTER);
		add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)), BorderLayout.SOUTH);
		pack();
		UI.adjustDialog(this, 0.5, 0.8);
		setLocationRelativeTo(parent);
		getRootPane().setDefaultButton(okButton);

		approved = false;
	}

	public boolean isApproved() {
		return approved;
	}

	private void okButtonPressed() {
		approved = true;
		dispose();
	}
}
