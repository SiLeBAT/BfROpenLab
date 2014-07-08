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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.openkrise.views.TracingConstants;

public class EditablePropertiesDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static enum Type {
		NODE, EDGE
	}

	private TracingCanvas parent;
	private Type type;

	private EditablePropertiesTable table;
	private JButton okButton;
	private JButton cancelButton;

	private JButton selectButton;
	private JButton weightButton;
	private JButton contaminationButton;
	private JButton filterButton;

	private boolean approved;

	private EditablePropertiesDialog(TracingCanvas parent,
			Collection<? extends Element> elements,
			Map<String, Class<?>> properties, Type type,
			boolean allowViewSelection) {
		super(SwingUtilities.getWindowAncestor(parent),
				type == Type.NODE ? "Node Properties" : "Edge Properties",
				DEFAULT_MODALITY_TYPE);
		this.parent = parent;
		this.type = type;

		table = new EditablePropertiesTable(elements, properties);
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		selectButton = new JButton("Select in View");
		selectButton.addActionListener(this);
		weightButton = new JButton("Set All "
				+ TracingConstants.CASE_WEIGHT_COLUMN);
		weightButton.addActionListener(this);
		contaminationButton = new JButton("Set All "
				+ TracingConstants.CROSS_CONTAMINATION_COLUMN);
		contaminationButton.addActionListener(this);
		filterButton = new JButton("Set All " + TracingConstants.FILTER_COLUMN);
		filterButton.addActionListener(this);

		JScrollPane scrollPane = new JScrollPane(table);

		scrollPane.setPreferredSize(UI.getMaxDimension(
				scrollPane.getPreferredSize(), table.getPreferredSize()));

		JPanel southPanel = new JPanel();

		southPanel.setLayout(new BorderLayout());
		southPanel.add(
				UI.createEmptyBorderPanel(new JLabel("Number of Elements: "
						+ elements.size())), BorderLayout.WEST);
		southPanel.add(UI.createHorizontalPanel(okButton, cancelButton),
				BorderLayout.EAST);

		List<JButton> buttons = new ArrayList<>();

		if (allowViewSelection) {
			buttons.add(selectButton);
		}

		if (properties.containsKey(TracingConstants.CASE_WEIGHT_COLUMN)) {
			buttons.add(weightButton);
		}

		if (properties.containsKey(TracingConstants.CROSS_CONTAMINATION_COLUMN)) {
			buttons.add(contaminationButton);
		}

		if (properties.containsKey(TracingConstants.FILTER_COLUMN)) {
			buttons.add(filterButton);
		}

		setLayout(new BorderLayout());
		add(UI.createWestPanel(UI.createHorizontalPanel(buttons
				.toArray(new JButton[0]))), BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		add(southPanel, BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(parent);
		UI.adjustDialog(this);
	}

	public static EditablePropertiesDialog createNodeDialog(
			TracingCanvas parent, Collection<GraphNode> nodes,
			Map<String, Class<?>> properties, boolean allowViewSelection) {
		return new EditablePropertiesDialog(parent, nodes, properties,
				Type.NODE, allowViewSelection);
	}

	public static <V extends Node> EditablePropertiesDialog createEdgeDialog(
			TracingCanvas parent, Collection<Edge<GraphNode>> edges,
			Map<String, Class<?>> properties, boolean allowViewSelection) {
		return new EditablePropertiesDialog(parent, edges, properties,
				Type.EDGE, allowViewSelection);
	}

	public boolean isApproved() {
		return approved;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			approved = true;
			table.updateElements();
			dispose();
		} else if (e.getSource() == cancelButton) {
			approved = false;
			dispose();
		} else if (e.getSource() == selectButton) {
			switch (type) {
			case NODE:
				Set<GraphNode> nodes = new LinkedHashSet<>();

				for (Element element : table.getSelectedElements()) {
					nodes.add((GraphNode) element);
				}

				parent.setSelectedNodes(nodes);
				break;
			case EDGE:
				Set<Edge<GraphNode>> edges = new LinkedHashSet<>();

				for (Element element : table.getSelectedElements()) {
					edges.add((Edge<GraphNode>) element);
				}

				parent.setSelectedEdges(edges);
				break;
			}
		} else if (e.getSource() == weightButton) {
			Object result = JOptionPane.showInputDialog(this,
					"Set All Values to?", TracingConstants.CASE_WEIGHT_COLUMN,
					JOptionPane.QUESTION_MESSAGE, null, null, 1.0);
			Double value = null;

			try {
				value = Double.parseDouble(result.toString());
			} catch (NumberFormatException ex) {
			} catch (NullPointerException ex) {
			}

			if (value != null) {
				setAllValuesTo(TracingConstants.CASE_WEIGHT_COLUMN, value);
			}
		} else if (e.getSource() == contaminationButton) {
			Object result = JOptionPane.showInputDialog(this,
					"Set All Values to?",
					TracingConstants.CROSS_CONTAMINATION_COLUMN,
					JOptionPane.QUESTION_MESSAGE, null, new Boolean[] {
							Boolean.TRUE, Boolean.FALSE }, Boolean.TRUE);

			if (result != null) {
				setAllValuesTo(TracingConstants.CROSS_CONTAMINATION_COLUMN,
						result);
			}
		} else if (e.getSource() == filterButton) {
			Object result = JOptionPane.showInputDialog(this,
					"Set All Values to?", TracingConstants.FILTER_COLUMN,
					JOptionPane.QUESTION_MESSAGE, null, new Boolean[] {
							Boolean.TRUE, Boolean.FALSE }, Boolean.TRUE);

			if (result != null) {
				setAllValuesTo(TracingConstants.FILTER_COLUMN, result);
			}
		}
	}

	private void setAllValuesTo(String column, Object value) {
		int columnIndex = UI.findColumn(table, column);

		for (int row = 0; row < table.getRowCount(); row++) {
			if (table.isCellEditable(row, columnIndex)) {
				table.setValueAt(value, row, columnIndex);
			}
		}
	}
}
