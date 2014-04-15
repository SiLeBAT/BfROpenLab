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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.element.Element;

public class SingleElementPropertiesDialog extends JDialog implements
		ActionListener {

	private static final long serialVersionUID = 1L;

	public SingleElementPropertiesDialog(Component parent, Element element,
			Map<String, Class<?>> properties) {
		super(SwingUtilities.getWindowAncestor(parent), "Properties",
				DEFAULT_MODALITY_TYPE);

		JPanel centerPanel = new JPanel();
		JPanel leftCenterPanel = new JPanel();
		JPanel rightCenterPanel = new JPanel();

		leftCenterPanel.setLayout(new GridLayout(properties.size(), 1, 5, 5));
		rightCenterPanel.setLayout(new GridLayout(properties.size(), 1, 5, 5));

		for (String property : properties.keySet()) {
			JTextField field = new JTextField();
			Object value = element.getProperties().get(property);

			if (value != null) {
				field.setText(value.toString());
				field.setPreferredSize(new Dimension(
						field.getPreferredSize().width + 5, field
								.getPreferredSize().height));
			}

			field.setEditable(false);
			leftCenterPanel.add(new JLabel(property + ":"));
			rightCenterPanel.add(field);
		}

		centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		centerPanel.setLayout(new BorderLayout(5, 5));
		centerPanel.add(leftCenterPanel, BorderLayout.WEST);
		centerPanel.add(rightCenterPanel, BorderLayout.CENTER);

		JButton okButton = new JButton("OK");

		okButton.addActionListener(this);

		setLayout(new BorderLayout());
		add(new JScrollPane(UI.createNorthPanel(centerPanel)),
				BorderLayout.CENTER);
		add(UI.createEastPanel(UI.createHorizontalPanel(okButton)),
				BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(parent);
		UI.adjustDialog(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dispose();
	}
}
