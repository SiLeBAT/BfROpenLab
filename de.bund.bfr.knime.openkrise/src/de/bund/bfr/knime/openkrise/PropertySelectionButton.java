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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.dialogs.PropertySelector;
import de.bund.bfr.knime.gis.views.canvas.util.PropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.PropertySchema.Type;
import de.bund.bfr.knime.ui.KnimeDialog;

public class PropertySelectionButton extends JButton implements PropertySelector, ActionListener {

	private static final long serialVersionUID = 1L;

	private Map<String, List<String>> groupedProperties;

	public PropertySelectionButton(PropertySchema schema) {
		super("Select Property");
		groupedProperties = groupProperties(schema);
		addActionListener(this);

		int maxWidth = getPreferredSize().width;
		int maxHeight = getPreferredSize().height;

		for (String property : schema.getMap().keySet()) {
			Dimension d = new JButton(property).getPreferredSize();

			maxWidth = Math.max(maxWidth, d.width);
			maxHeight = Math.max(maxHeight, d.height);
		}

		setPreferredSize(new Dimension(maxWidth, maxHeight));
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public String getSelectedProperty() {
		return getText();
	}

	@Override
	public void setSelectedProperty(String property) {
		setText(property);
		fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, property, ItemEvent.SELECTED));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		PropertySelectionDialog dialog = new PropertySelectionDialog();

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setSelectedProperty(dialog.getSelected());
		}
	}

	private static Map<String, List<String>> groupProperties(PropertySchema schema) {
		Map<String, List<String>> result = new LinkedHashMap<>();

		if (schema.getType() == Type.NODE) {
			result.put("Main", createPropertyGroup(schema.getMap().keySet(), TracingColumns.STATION_COLUMNS));
			result.put("Address", createPropertyGroup(schema.getMap().keySet(), TracingColumns.ADDRESS_COLUMNS));
		} else if (schema.getType() == Type.EDGE) {
			result.put("Main", createPropertyGroup(schema.getMap().keySet(), TracingColumns.DELIVERY_COLUMNS));
		}

		result.put("Tracing", createPropertyGroup(schema.getMap().keySet(),
				Iterables.concat(TracingColumns.INPUT_COLUMNS, TracingColumns.OUTPUT_COLUMNS)));

		List<String> otherProperties = new ArrayList<>(schema.getMap().keySet());

		for (List<String> used : result.values()) {
			otherProperties.removeAll(used);
		}

		result.put("Other", otherProperties);

		return result;
	}

	private static List<String> createPropertyGroup(Set<String> properties, Iterable<String> groupColumns) {
		List<String> result = Lists.newArrayList(groupColumns);

		result.retainAll(properties);

		return result;
	}

	private class PropertySelectionDialog extends KnimeDialog implements ActionListener {

		private static final long serialVersionUID = 1L;

		private boolean approved;
		private String selected;

		private BiMap<String, JButton> selectButtons;
		private JButton cancelButton;

		public PropertySelectionDialog() {
			super(PropertySelectionButton.this, "Select Property", DEFAULT_MODALITY_TYPE);
			approved = false;
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(this);
			selectButtons = HashBiMap.create();

			JPanel mainPanel = new JPanel();

			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));

			for (Map.Entry<String, List<String>> entry : groupedProperties.entrySet()) {
				JPanel panel = new JPanel();

				panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				panel.setLayout(new GridLayout(entry.getValue().size(), 1));

				for (String property : entry.getValue()) {
					JButton button = new JButton(property);

					button.addActionListener(this);
					panel.add(button);
					selectButtons.put(property, button);
				}

				mainPanel.add(UI.createTitledPanel(UI.createNorthPanel(panel), entry.getKey()));
			}

			setLayout(new BorderLayout());
			add(mainPanel, BorderLayout.CENTER);
			add(UI.createEastPanel(UI.createHorizontalPanel(cancelButton)), BorderLayout.SOUTH);

			pack();
			setLocationRelativeTo(PropertySelectionButton.this);
			UI.adjustDialog(this);
			getRootPane().setDefaultButton(cancelButton);
		}

		public boolean isApproved() {
			return approved;
		}

		public String getSelected() {
			return selected;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == cancelButton) {
				dispose();
			} else if (selectButtons.containsValue(e.getSource())) {
				approved = true;
				selected = selectButtons.inverse().get(e.getSource());
				dispose();
			}
		}
	}
}
