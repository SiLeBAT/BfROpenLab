/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import de.bund.bfr.knime.UI;

public class ListFilterDialog<T> extends KnimeDialog {

	private static final long serialVersionUID = 1L;

	private List<T> elements;

	private boolean approved;
	private Set<T> filtered;

	private JCheckBox allBox;
	private JList<T> list;

	public ListFilterDialog(Component parent, List<T> elements) {
		super(parent, "Filter", DEFAULT_MODALITY_TYPE);
		this.elements = elements;

		allBox = new JCheckBox("Select All");
		allBox.setSelected(true);
		allBox.addActionListener(e -> list.setEnabled(!allBox.isSelected()));
		list = new JList<>(new Vector<>(elements));
		list.setEnabled(false);

		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");

		okButton.addActionListener(e -> okButtonPressed());
		cancelButton.addActionListener(e -> dispose());

		approved = false;
		filtered = null;

		setLayout(new BorderLayout());
		add(UI.createWestPanel(allBox), BorderLayout.NORTH);
		add(new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)), BorderLayout.SOUTH);

		pack();
		UI.adjustDialog(this);
		setLocationRelativeTo(parent);
		getRootPane().setDefaultButton(okButton);
	}

	public boolean isApproved() {
		return approved;
	}

	public Set<T> getFiltered() {
		return filtered;
	}

	private void okButtonPressed() {
		approved = true;
		filtered = new LinkedHashSet<>(allBox.isSelected() ? elements : list.getSelectedValuesList());
		dispose();
	}
}
