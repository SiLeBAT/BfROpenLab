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
package de.bund.bfr.knime.gis.views.canvas.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;

public class HighlightSelectionDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JList<HighlightCondition> list;
	private JButton okButton;
	private JButton cancelButton;

	private List<HighlightCondition> highlightConditions;
	private boolean approved;

	public HighlightSelectionDialog(Component owner, List<HighlightCondition> highlightConditions) {
		super(SwingUtilities.getWindowAncestor(owner), "Highlight Conditions", DEFAULT_MODALITY_TYPE);
		this.highlightConditions = null;
		approved = false;

		list = new JList<>();
		list.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setCellRenderer(new HighlightListCellRenderer());
		list.setListData(new Vector<>(highlightConditions));

		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		JPanel southPanel = new JPanel();

		southPanel.setLayout(new BorderLayout());
		southPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		southPanel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.CENTER);

		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		mainPanel.add(southPanel, BorderLayout.SOUTH);

		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);
		add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)), BorderLayout.SOUTH);

		pack();
		UI.adjustDialog(this);
		setLocationRelativeTo(owner);
		getRootPane().setDefaultButton(okButton);
	}

	public boolean isApproved() {
		return approved;
	}

	public List<HighlightCondition> getHighlightConditions() {
		return highlightConditions;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			highlightConditions = list.getSelectedValuesList();
			approved = true;
			dispose();
		} else if (e.getSource() == cancelButton) {
			highlightConditions = null;
			approved = false;
			dispose();
		}
	}
}
