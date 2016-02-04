/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.google.common.collect.Sets;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.ICanvas;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.PropertySchema;
import de.bund.bfr.knime.ui.KnimeDialog;

public class PropertiesDialog<V extends Node> extends KnimeDialog {

	private static final long serialVersionUID = 1L;

	private static enum Type {
		NODE, EDGE
	}

	private ICanvas<V> parent;
	private Type type;

	private PropertiesTable table;

	private PropertiesDialog(ICanvas<V> parent, Collection<? extends Element> elements, PropertySchema schema,
			Type type, boolean allowViewSelection, Set<String> idColumns) {
		super(parent.getComponent(), "Properties", DEFAULT_MODALITY_TYPE);
		this.parent = parent;
		this.type = type;

		table = new PropertiesTable(new ArrayList<>(elements), schema, idColumns);

		JButton selectButton = new JButton("Select in View");
		JButton okButton = new JButton("OK");

		selectButton.addActionListener(e -> selectButtonPressed());
		okButton.addActionListener(e -> dispose());

		JScrollPane scrollPane = new JScrollPane(table);

		scrollPane.setPreferredSize(UI.getMaxDimension(scrollPane.getPreferredSize(), table.getPreferredSize()));

		JPanel bottomPanel = new JPanel();

		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(UI.createHorizontalPanel(new JLabel("Number of Elements: " + elements.size())),
				BorderLayout.WEST);
		bottomPanel.add(UI.createHorizontalPanel(okButton), BorderLayout.EAST);

		setLayout(new BorderLayout());

		if (allowViewSelection) {
			add(UI.createHorizontalPanel(selectButton), BorderLayout.NORTH);
		}

		add(scrollPane, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);

		pack();
		UI.adjustDialog(this);
		setLocationRelativeTo(parent.getComponent());
		getRootPane().setDefaultButton(okButton);
	}

	public static <V extends Node> PropertiesDialog<V> createNodeDialog(ICanvas<V> parent, Collection<V> nodes,
			NodePropertySchema schema, boolean allowViewSelection) {
		return new PropertiesDialog<>(parent, nodes, schema, Type.NODE, allowViewSelection,
				Sets.newHashSet(schema.getId()));
	}

	public static <V extends Node> PropertiesDialog<V> createEdgeDialog(ICanvas<V> parent, Collection<Edge<V>> edges,
			EdgePropertySchema schema, boolean allowViewSelection) {
		return new PropertiesDialog<>(parent, edges, schema, Type.EDGE, allowViewSelection,
				Sets.newHashSet(schema.getId(), schema.getFrom(), schema.getTo()));
	}

	@SuppressWarnings("unchecked")
	private void selectButtonPressed() {
		switch (type) {
		case NODE:
			Set<V> nodes = new LinkedHashSet<>();

			for (Element element : table.getSelectedElements()) {
				nodes.add((V) element);
			}

			parent.setSelectedNodes(nodes);
			break;
		case EDGE:
			Set<Edge<V>> edges = new LinkedHashSet<>();

			for (Element element : table.getSelectedElements()) {
				edges.add((Edge<V>) element);
			}

			parent.setSelectedEdges(edges);
			break;
		}
	}
}
