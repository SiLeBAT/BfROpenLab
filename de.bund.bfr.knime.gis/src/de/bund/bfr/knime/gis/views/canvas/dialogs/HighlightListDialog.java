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
package de.bund.bfr.knime.gis.views.canvas.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

public class HighlightListDialog extends KnimeDialog {

	private static final long serialVersionUID = 1L;

	private static final boolean DEFAULT_ALLOW_INVISIBLE = true;
	private static final boolean DEFAULT_ALLOW_THICKNESS = true;
	private static final boolean DEFAULT_ALLOW_SHAPE = false;

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
	private boolean allowShape;
	private List<HighlightConditionChecker> checkers;
	private HighlightCondition autoAddCondition;

	private PropertySelectorCreator selectorCreator;

	private HighlightConditionList highlightConditions;
	private boolean approved;

	public HighlightListDialog(Component owner, PropertySchema schema, HighlightConditionList highlightConditions) {
		super(owner, "Highlight Condition List", DEFAULT_MODALITY_TYPE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowOpened(WindowEvent e) {
				if (autoAddCondition != null) {
					addCondition(autoAddCondition);
				}
			}
		});

		this.schema = schema;
		this.highlightConditions = new HighlightConditionList(new ArrayList<>(highlightConditions.getConditions()),
				highlightConditions.isPrioritizeColors());
		allowInvisible = DEFAULT_ALLOW_INVISIBLE;
		allowThickness = DEFAULT_ALLOW_THICKNESS;
		allowShape = DEFAULT_ALLOW_SHAPE;
		checkers = new ArrayList<>();
		autoAddCondition = null;
		approved = false;
		selectorCreator = new DefaultPropertySelectorCreator();

		list = new JList<>();
		list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new HighlightListCellRenderer());
		list.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				clickedOnList(e);
			}
		});

		updateList();
		addButton = new JButton("Add");
		addButton.addActionListener(e -> addCondition(null));
		removeButton = new JButton("Remove");
		removeButton.addActionListener(e -> removePressed());
		prioritizeBox = new JCheckBox("Prioritize Colors");
		prioritizeBox.setSelected(this.highlightConditions.isPrioritizeColors());
		prioritizeBox.addActionListener(e -> this.highlightConditions.setPrioritizeColors(prioritizeBox.isSelected()));
		upButton = new JButton("Up");
		upButton.addActionListener(e -> upPressed());
		downButton = new JButton("Down");
		downButton.addActionListener(e -> downPressed());
		okButton = new JButton("OK");
		okButton.addActionListener(e -> {
			approved = true;
			dispose();
		});
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> dispose());

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

		JScrollPane centerPanel = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		centerPanel.setPreferredSize(new Dimension(centerPanel.getPreferredSize().width,
				list.getPreferredSize().height + centerPanel.getInsets().top + centerPanel.getInsets().bottom));

		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(centerPanel, BorderLayout.CENTER);
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

	public boolean isAllowShape() {
		return allowShape;
	}

	public void setAllowShape(boolean allowShape) {
		this.allowShape = allowShape;
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

	private void clickedOnList(MouseEvent e) {
		int i = list.getSelectedIndex();

		if (e.getClickCount() == 2 && i != -1) {
			HighlightDialog dialog = HighlightDialog.createHighlightDialog(this, schema, allowInvisible, allowThickness,
					allowShape, highlightConditions.getConditions().get(i), checkers, selectorCreator);

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				highlightConditions.getConditions().set(i, dialog.getHighlightCondition());
				updateList();
			}
		}
	}

	private void removePressed() {
		int i = list.getSelectedIndex();

		if (i != -1) {
			highlightConditions.getConditions().remove(i);
			updateList();
		}
	}

	private void upPressed() {
		int i = list.getSelectedIndex();

		if (i > 0) {
			highlightConditions.getConditions().add(i - 1, highlightConditions.getConditions().remove(i));
			updateList();
			list.setSelectedIndex(i - 1);
		}
	}

	private void downPressed() {
		int i = list.getSelectedIndex();

		if (i != -1 && i != list.getModel().getSize() - 1) {
			highlightConditions.getConditions().add(i + 1, highlightConditions.getConditions().remove(i));
			updateList();
			list.setSelectedIndex(i + 1);
		}
	}

	private void addCondition(HighlightCondition condition) {
		HighlightDialog dialog = HighlightDialog.createHighlightDialog(this, schema, allowInvisible, allowThickness,
				allowShape, condition, checkers, selectorCreator);

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
