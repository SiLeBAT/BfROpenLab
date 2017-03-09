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
package de.bund.bfr.knime.openkrise;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.dialogs.PropertySelector;
import de.bund.bfr.knime.gis.views.canvas.util.PropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.PropertySchema.Type;
import de.bund.bfr.knime.ui.KnimeDialog;

public class PropertySelectionButton extends JButton implements PropertySelector {

	private static final long serialVersionUID = 1L;

	private String selectedProperty;

	private Map<String, List<String>> groupedProperties;

	public PropertySelectionButton(PropertySchema schema, String metaProperty) {
		super("Select Property");
		selectedProperty = null;
		groupedProperties = groupProperties(schema, metaProperty);
		addActionListener(e -> buttonPressed());

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
		return selectedProperty;
	}

	@Override
	public void setSelectedProperty(String selectedProperty) {
		String oldProperty = this.selectedProperty;

		this.selectedProperty = selectedProperty;
		setText(selectedProperty);

		if (!Objects.equals(oldProperty, selectedProperty)) {
			fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, oldProperty, ItemEvent.DESELECTED));
			fireItemStateChanged(
					new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, selectedProperty, ItemEvent.SELECTED));
		}
	}

	private void buttonPressed() {
		PropertySelectionDialog dialog = new PropertySelectionDialog();

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setSelectedProperty(dialog.getSelected());
		}
	}

	private static Map<String, List<String>> groupProperties(PropertySchema schema, String metaProperty) {
		Map<String, List<String>> result = new LinkedHashMap<>();

		if (schema.getType() == Type.NODE) {
			List<String> mainProperties = new ArrayList<>(TracingColumns.STATION_COLUMNS);

			if (metaProperty != null) {
				mainProperties.add(metaProperty);
			}

			result.put("Main", filterGroup(mainProperties.stream(), schema));
			result.put("Address", filterGroup(TracingColumns.ADDRESS_COLUMNS.stream(), schema));
		} else if (schema.getType() == Type.EDGE) {
			result.put("Main", filterGroup(TracingColumns.DELIVERY_COLUMNS.stream(), schema));
		}

		result.put("Tracing", filterGroup(
				Stream.concat(TracingColumns.IN_COLUMNS.stream(), TracingColumns.OUT_COLUMNS.stream()), schema));

		List<String> otherProperties = new ArrayList<>(schema.getMap().keySet());

		for (List<String> used : result.values()) {
			otherProperties.removeAll(used);
		}

		if (!otherProperties.isEmpty()) {
			result.put("Other", KnimeUtils.ORDERING.sortedCopy(otherProperties));
		}

		return result;
	}

	private static List<String> filterGroup(Stream<String> groupColumns, PropertySchema schema) {
		return groupColumns.filter(s -> schema.getMap().containsKey(s))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private class PropertySelectionDialog extends KnimeDialog {

		private static final long serialVersionUID = 1L;

		private boolean approved;
		private String selected;

		public PropertySelectionDialog() {
			super(PropertySelectionButton.this, "Select Property", DEFAULT_MODALITY_TYPE);
			approved = false;

			JButton cancelButton = new JButton("Cancel");

			cancelButton.addActionListener(e -> dispose());

			JPanel mainPanel = new JPanel();

			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));

			for (Map.Entry<String, List<String>> entry : groupedProperties.entrySet()) {
				JPanel panel = new JPanel();

				panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				panel.setLayout(new GridLayout(entry.getValue().size(), 1));

				for (String property : entry.getValue()) {
					JPanel p = new JPanel();
					JButton button = new JButton(property);

					if (property.equals(selectedProperty)) {
						p.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
					} else {
						p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
					}

					button.addActionListener(e -> selectButtonPressed(property));
					p.setLayout(new BorderLayout());
					p.add(button, BorderLayout.CENTER);
					panel.add(p);
				}

				mainPanel.add(UI.createTitledPanel(UI.createNorthPanel(panel), entry.getKey()));
			}

			setLayout(new BorderLayout());
			add(mainPanel, BorderLayout.CENTER);
			add(UI.createEastPanel(UI.createBorderPanel(cancelButton)), BorderLayout.SOUTH);

			pack();
			UI.adjustDialog(this);
			setLocationRelativeTo(PropertySelectionButton.this);
			getRootPane().setDefaultButton(cancelButton);
		}

		public boolean isApproved() {
			return approved;
		}

		public String getSelected() {
			return selected;
		}

		private void selectButtonPressed(String selected) {
			this.selected = selected;
			approved = true;
			dispose();
		}
	}
}
