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
package de.bund.bfr.knime.openkrise;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.ui.KnimeDialog;

public class PropertySelectionDialog extends KnimeDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	private boolean approved;

	private BiMap<String, JButton> selectButtons;
	private JButton cancelButton;

	public PropertySelectionDialog(Component parent) {
		super(parent, "Select Property", DEFAULT_MODALITY_TYPE);
		approved = false;

		selectButtons = HashBiMap.create();

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		setLayout(new BorderLayout());
		add(UI.createEastPanel(UI.createHorizontalPanel(cancelButton)), BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(parent);
		UI.adjustDialog(this, 0.5, 0.8);
		getRootPane().setDefaultButton(cancelButton);
	}

	public boolean isApproved() {
		return approved;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancelButton) {
			dispose();
		} else if (selectButtons.containsValue(e.getSource())) {
		}
	}
}
