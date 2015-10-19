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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.util.PropertySchema;
import de.bund.bfr.knime.ui.KnimeDialog;

public class HighlightListDialog extends KnimeDialog implements ActionListener, MouseListener, WindowListener {

	private static final long serialVersionUID = 1L;

	private static final boolean DEFAULT_ALLOW_INVISIBLE = true;
	private static final boolean DEFAULT_ALLOW_THICKNESS = true;

	private JList<HighlightCondition> list;
	private JButton addButton;
	private JButton removeButton;
	private JCheckBox prioritizeBox;
	private JButton upButton;
	private JButton downButton;

	private JButton okButton;
	private JButton cancelButton;

	private PropertySchema schema;
	private boolean allowInvisible;
	private boolean allowThickness;
	private List<HighlightConditionChecker> checkers;
	private HighlightCondition autoAddCondition;

	private PropertySelectorCreator selectorCreator;

	private HighlightConditionList highlightConditions;
	private boolean approved;

	public HighlightListDialog(Component owner, PropertySchema schema, HighlightConditionList highlightConditions) {
		super(owner, "Highlight Condition List", DEFAULT_MODALITY_TYPE);
		addWindowListener(this);
		this.schema = schema;
		this.highlightConditions = new HighlightConditionList(new ArrayList<>(highlightConditions.getConditions()),
				highlightConditions.isPrioritizeColors());
		allowInvisible = DEFAULT_ALLOW_INVISIBLE;
		allowThickness = DEFAULT_ALLOW_THICKNESS;
		checkers = new ArrayList<>();
		autoAddCondition = null;
		approved = false;
		selectorCreator = new DefaultPropertySelectorCreator();

		list = new JList<>();
		list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new HighlightListCellRenderer());
		list.addMouseListener(this);
		updateList();
		addButton = new JButton("Add");
		addButton.addActionListener(this);
		removeButton = new JButton("Remove");
		removeButton.addActionListener(this);
		prioritizeBox = new JCheckBox("Prioritize Colors");
		prioritizeBox.setSelected(highlightConditions.isPrioritizeColors());
		prioritizeBox.addActionListener(this);
		upButton = new JButton("Up");
		upButton.addActionListener(this);
		downButton = new JButton("Down");
		downButton.addActionListener(this);
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		JPanel editPanel = new JPanel();

		editPanel.setLayout(new GridLayout(5, 1, 5, 5));
		editPanel.add(addButton);
		editPanel.add(removeButton);
		editPanel.add(prioritizeBox);
		editPanel.add(upButton);
		editPanel.add(downButton);

		JPanel eastPanel = new JPanel();

		eastPanel.setLayout(new BorderLayout());
		eastPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		eastPanel.add(editPanel, BorderLayout.NORTH);

		JPanel southPanel = new JPanel();

		southPanel.setLayout(new BorderLayout());
		southPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		southPanel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.CENTER);

		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		mainPanel.add(eastPanel, BorderLayout.EAST);
		mainPanel.add(southPanel, BorderLayout.SOUTH);

		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);
		add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)), BorderLayout.SOUTH);

		pack();
		UI.adjustDialog(this);
		setLocationRelativeTo(owner);
		getRootPane().setDefaultButton(okButton);
	}

	public PropertySelectorCreator getSelectorCreator() {
		return selectorCreator;
	}

	public void setSelectorCreator(PropertySelectorCreator selectorCreator) {
		this.selectorCreator = selectorCreator;
	}

	public boolean isAllowInvisible() {
		return allowInvisible;
	}

	public void setAllowInvisible(boolean allowInvisible) {
		this.allowInvisible = allowInvisible;
	}

	public boolean isAllowThickness() {
		return allowThickness;
	}

	public void setAllowThickness(boolean allowThickness) {
		this.allowThickness = allowThickness;
	}

	public void addChecker(HighlightConditionChecker checker) {
		checkers.add(checker);
	}

	public void removeChecker(HighlightConditionChecker checker) {
		checkers.remove(checker);
	}

	public HighlightCondition getAutoAddCondition() {
		return autoAddCondition;
	}

	public void setAutoAddCondition(HighlightCondition autoAddCondition) {
		this.autoAddCondition = autoAddCondition;
	}

	public boolean isApproved() {
		return approved;
	}

	public HighlightConditionList getHighlightConditions() {
		return highlightConditions;
	}

	public boolean isPrioritizeColors() {
		return prioritizeBox.isSelected();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			approved = true;
			dispose();
		} else if (e.getSource() == cancelButton) {
			approved = false;
			dispose();
		} else if (e.getSource() == addButton) {
			addCondition(null);
		} else if (e.getSource() == removeButton) {
			int i = list.getSelectedIndex();

			if (i != -1) {
				highlightConditions.getConditions().remove(i);
				updateList();
			}
		} else if (e.getSource() == prioritizeBox) {
			highlightConditions.setPrioritizeColors(prioritizeBox.isSelected());
		} else if (e.getSource() == upButton) {
			int i = list.getSelectedIndex();

			if (i > 0) {
				highlightConditions.getConditions().add(i - 1, highlightConditions.getConditions().remove(i));
				updateList();
				list.setSelectedIndex(i - 1);
			}
		} else if (e.getSource() == downButton) {
			int i = list.getSelectedIndex();

			if (i != -1 && i != list.getModel().getSize() - 1) {
				highlightConditions.getConditions().add(i + 1, highlightConditions.getConditions().remove(i));
				updateList();
				list.setSelectedIndex(i + 1);
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int i = list.getSelectedIndex();

		if (e.getClickCount() == 2 && i != -1) {
			HighlightDialog dialog = HighlightDialog.createHighlightDialog(this, schema, allowInvisible, allowThickness,
					highlightConditions.getConditions().get(i), checkers, selectorCreator);

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				highlightConditions.getConditions().set(i, dialog.getHighlightCondition());
				updateList();
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
		if (autoAddCondition != null) {
			addCondition(autoAddCondition);
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	private void addCondition(HighlightCondition condition) {
		HighlightDialog dialog = HighlightDialog.createHighlightDialog(this, schema, allowInvisible, allowThickness,
				condition, checkers, selectorCreator);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			highlightConditions.getConditions().add(dialog.getHighlightCondition());
			updateList();
		}
	}

	private void updateList() {
		list.setListData(highlightConditions.getConditions().toArray(new HighlightCondition[0]));
	}
}
