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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.element.Element;

public class PropertiesDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	public PropertiesDialog(JComponent parent,
			Collection<? extends Element> elements,
			Map<String, Class<?>> properties) {
		super(SwingUtilities.getWindowAncestor(parent), "Properties",
				DEFAULT_MODALITY_TYPE);

		JScrollPane scrollPane = new JScrollPane(new PropertiesTable(elements,
				properties));
		JLabel numberLabel = new JLabel("Number of Elements: "
				+ elements.size());
		JButton okButton = new JButton("OK");

		okButton.addActionListener(this);

		JPanel bottomPanel = new JPanel();

		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(UI.createEmptyBorderPanel(numberLabel),
				BorderLayout.WEST);
		bottomPanel.add(UI.createButtonPanel(okButton), BorderLayout.CENTER);

		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dispose();
	}
}
