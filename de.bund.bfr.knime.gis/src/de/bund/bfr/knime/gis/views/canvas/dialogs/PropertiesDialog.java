/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.Canvas;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.Node;

public class PropertiesDialog<V extends Node> extends JDialog implements
		ActionListener {

	private static final long serialVersionUID = 1L;

	private static enum Type {
		NODE, EDGE
	}

	private Canvas<V> parent;
	private Type type;

	private PropertiesTable table;
	private JButton selectButton;
	private JButton okButton;

	private PropertiesDialog(Canvas<V> parent,
			Collection<? extends Element> elements,
			Map<String, Class<?>> properties, Type type,
			boolean allowViewSelection, Set<String> idColumns) {
		super(SwingUtilities.getWindowAncestor(parent), "Properties",
				DEFAULT_MODALITY_TYPE);
		this.parent = parent;
		this.type = type;

		table = new PropertiesTable(new ArrayList<>(elements), properties,
				idColumns);
		selectButton = new JButton("Select in View");
		selectButton.addActionListener(this);
		okButton = new JButton("OK");
		okButton.addActionListener(this);

		JScrollPane scrollPane = new JScrollPane(table);

		scrollPane.setPreferredSize(UI.getMaxDimension(
				scrollPane.getPreferredSize(), table.getPreferredSize()));

		JPanel bottomPanel = new JPanel();

		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(
				UI.createEmptyBorderPanel(new JLabel("Number of Elements: "
						+ elements.size())), BorderLayout.WEST);
		bottomPanel.add(UI.createHorizontalPanel(okButton), BorderLayout.EAST);

		setLayout(new BorderLayout());

		if (allowViewSelection) {
			add(UI.createHorizontalPanel(selectButton), BorderLayout.NORTH);
		}

		add(scrollPane, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(parent);
		UI.adjustDialog(this);
	}

	public static <V extends Node> PropertiesDialog<V> createNodeDialog(
			Canvas<V> parent, Collection<V> nodes,
			Map<String, Class<?>> properties, boolean allowViewSelection,
			Set<String> idColumns) {
		return new PropertiesDialog<>(parent, nodes, properties, Type.NODE,
				allowViewSelection, idColumns);
	}

	public static <V extends Node> PropertiesDialog<V> createEdgeDialog(
			Canvas<V> parent, Collection<Edge<V>> edges,
			Map<String, Class<?>> properties, boolean allowViewSelection,
			Set<String> idColumns) {
		return new PropertiesDialog<>(parent, edges, properties, Type.EDGE,
				allowViewSelection, idColumns);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == selectButton) {
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
		} else if (e.getSource() == okButton) {
			dispose();
		}
	}
}
