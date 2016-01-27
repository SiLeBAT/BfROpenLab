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
package de.bund.bfr.knime.gis.views.canvas.util;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import de.bund.bfr.knime.gis.views.canvas.Canvas;
import de.bund.bfr.knime.gis.views.canvas.jung.layout.LayoutType;

public class CanvasPopupMenu extends JPopupMenu {

	private static final long serialVersionUID = 1L;

	private JMenu nodeSelectionMenu;
	private JMenu edgeSelectionMenu;
	private JMenu nodeHighlightMenu;
	private JMenu edgeHighlightMenu;
	private JMenu layoutMenu;

	private JMenuItem resetLayoutItem;
	private JMenuItem saveAsItem;
	private List<JMenuItem> layoutItems;

	private JMenuItem selectConnectionsItem;
	private JMenuItem selectIncomingItem;
	private JMenuItem selectOutgoingItem;
	private JMenuItem clearSelectedNodesItem;
	private JMenuItem clearSelectedEdgesItem;
	private JMenuItem highlightSelectedNodesItem;
	private JMenuItem highlightSelectedEdgesItem;
	private JMenuItem highlightNodesItem;
	private JMenuItem highlightEdgesItem;
	private JMenuItem clearHighlightedNodesItem;
	private JMenuItem clearHighlightedEdgesItem;
	private JMenuItem selectHighlightedNodesItem;
	private JMenuItem selectHighlightedEdgesItem;
	private JMenuItem highlightNodeCategoriesItem;
	private JMenuItem highlightEdgeCategoriesItem;
	private JMenuItem selectNodesItem;
	private JMenuItem selectEdgesItem;
	private JMenuItem nodePropertiesItem;
	private JMenuItem nodeAllPropertiesItem;
	private JMenuItem edgePropertiesItem;
	private JMenuItem edgeAllPropertiesItem;

	private JMenuItem collapseToNodeItem;
	private JMenuItem expandFromNodeItem;
	private JMenuItem collapseByPropertyItem;
	private JMenuItem clearCollapsedNodesItem;

	private List<ClickListener> listeners;

	public CanvasPopupMenu(Canvas<?> owner, boolean allowEdges, boolean allowLayout, boolean allowCollapse) {
		init(owner);

		add(resetLayoutItem);
		add(saveAsItem);

		if (allowLayout) {
			for (JMenuItem item : layoutItems) {
				layoutMenu.add(item);
			}

			add(layoutMenu);
		}

		if (allowEdges) {
			nodeSelectionMenu.add(nodePropertiesItem);
			nodeSelectionMenu.add(clearSelectedNodesItem);
			nodeSelectionMenu.add(highlightSelectedNodesItem);
			nodeSelectionMenu.add(new JSeparator());
			nodeSelectionMenu.add(selectConnectionsItem);
			nodeSelectionMenu.add(selectIncomingItem);
			nodeSelectionMenu.add(selectOutgoingItem);

			if (allowCollapse) {
				nodeSelectionMenu.add(new JSeparator());
				nodeSelectionMenu.add(collapseToNodeItem);
				nodeSelectionMenu.add(expandFromNodeItem);
				nodeSelectionMenu.add(nodeAllPropertiesItem);
			}

			edgeSelectionMenu.add(edgePropertiesItem);
			edgeSelectionMenu.add(edgeAllPropertiesItem);
			edgeSelectionMenu.add(clearSelectedEdgesItem);
			edgeSelectionMenu.add(highlightSelectedEdgesItem);

			add(new JSeparator());
			add(nodeSelectionMenu);
			add(edgeSelectionMenu);

			nodeHighlightMenu.add(highlightNodesItem);
			nodeHighlightMenu.add(clearHighlightedNodesItem);
			nodeHighlightMenu.add(selectHighlightedNodesItem);
			nodeHighlightMenu.add(highlightNodeCategoriesItem);

			edgeHighlightMenu.add(highlightEdgesItem);
			edgeHighlightMenu.add(clearHighlightedEdgesItem);
			edgeHighlightMenu.add(selectHighlightedEdgesItem);
			edgeHighlightMenu.add(highlightEdgeCategoriesItem);

			add(nodeHighlightMenu);
			add(edgeHighlightMenu);
			add(selectNodesItem);
			add(selectEdgesItem);
		} else {
			nodeSelectionMenu.add(nodePropertiesItem);
			nodeSelectionMenu.add(clearSelectedNodesItem);
			nodeSelectionMenu.add(highlightSelectedNodesItem);

			add(new JSeparator());
			add(nodeSelectionMenu);

			nodeHighlightMenu.add(highlightNodesItem);
			nodeHighlightMenu.add(clearHighlightedNodesItem);
			nodeHighlightMenu.add(selectHighlightedNodesItem);
			nodeHighlightMenu.add(highlightNodeCategoriesItem);

			add(nodeHighlightMenu);
			add(selectNodesItem);
		}

		if (allowCollapse) {
			add(new JSeparator());
			add(collapseByPropertyItem);
			add(clearCollapsedNodesItem);
		}
	}

	public void addClickListener(ClickListener listener) {
		listeners.add(listener);
	}

	public void removeClickListener(ClickListener listener) {
		listeners.remove(listener);
	}

	public void setNodeSelectionEnabled(boolean enabled) {
		nodeSelectionMenu.setEnabled(enabled);
	}

	public void setEdgeSelectionEnabled(boolean enabled) {
		edgeSelectionMenu.setEnabled(enabled);
	}

	private void init(Canvas<?> owner) {
		listeners = new ArrayList<>();

		nodeSelectionMenu = new JMenu(owner.getNaming().Node() + " Selection");
		nodeSelectionMenu.setEnabled(false);
		edgeSelectionMenu = new JMenu(owner.getNaming().Edge() + " Selection");
		edgeSelectionMenu.setEnabled(false);
		nodeHighlightMenu = new JMenu(owner.getNaming().Node() + " Highlighting");
		edgeHighlightMenu = new JMenu(owner.getNaming().Edge() + " Highlighting");
		layoutMenu = new JMenu("Apply Layout");

		resetLayoutItem = new JMenuItem("Reset Layout");
		resetLayoutItem.addActionListener(e -> listeners.forEach(l -> l.resetLayoutItemClicked()));
		saveAsItem = new JMenuItem("Save As ...");
		saveAsItem.addActionListener(e -> listeners.forEach(l -> l.saveAsItemClicked()));

		clearSelectedNodesItem = new JMenuItem("Clear");
		clearSelectedNodesItem.addActionListener(e -> listeners.forEach(l -> l.clearSelectedNodesItemClicked()));
		nodePropertiesItem = new JMenuItem("Show Properties");
		nodePropertiesItem.addActionListener(e -> listeners.forEach(l -> l.nodePropertiesItemClicked()));
		nodeAllPropertiesItem = new JMenuItem("Show Uncollapsed Properties");
		nodeAllPropertiesItem.addActionListener(e -> listeners.forEach(l -> l.nodeAllPropertiesItemClicked()));
		highlightSelectedNodesItem = new JMenuItem("Highlight Selected");
		highlightSelectedNodesItem
				.addActionListener(e -> listeners.forEach(l -> l.highlightSelectedNodesItemClicked()));
		selectConnectionsItem = new JMenuItem("Select Connections");
		selectConnectionsItem.addActionListener(e -> listeners.forEach(l -> l.selectConnectionsItemClicked()));
		selectIncomingItem = new JMenuItem("Select Incoming");
		selectIncomingItem.addActionListener(e -> listeners.forEach(l -> l.selectIncomingItemClicked()));
		selectOutgoingItem = new JMenuItem("Select Outgoing");
		selectOutgoingItem.addActionListener(e -> listeners.forEach(l -> l.selectOutgoingItemClicked()));

		clearSelectedEdgesItem = new JMenuItem("Clear");
		clearSelectedEdgesItem.addActionListener(e -> listeners.forEach(l -> l.clearSelectedEdgesItemClicked()));
		edgePropertiesItem = new JMenuItem("Show Properties");
		edgePropertiesItem.addActionListener(e -> listeners.forEach(l -> l.edgePropertiesItemClicked()));
		edgeAllPropertiesItem = new JMenuItem("Show Unjoined Properties");
		edgeAllPropertiesItem.addActionListener(e -> listeners.forEach(l -> l.edgeAllPropertiesItemClicked()));
		highlightSelectedEdgesItem = new JMenuItem("Highlight Selected");
		highlightSelectedEdgesItem
				.addActionListener(e -> listeners.forEach(l -> l.highlightSelectedEdgesItemClicked()));

		highlightNodesItem = new JMenuItem("Edit");
		highlightNodesItem.addActionListener(e -> listeners.forEach(l -> l.highlightNodesItemClicked()));
		clearHighlightedNodesItem = new JMenuItem("Clear");
		clearHighlightedNodesItem.addActionListener(e -> listeners.forEach(l -> l.clearHighlightedNodesItemClicked()));
		selectHighlightedNodesItem = new JMenuItem("Select Highlighted");
		selectHighlightedNodesItem
				.addActionListener(e -> listeners.forEach(l -> l.selectHighlightedNodesItemClicked()));
		highlightNodeCategoriesItem = new JMenuItem("Add Category Highlighting");
		highlightNodeCategoriesItem
				.addActionListener(e -> listeners.forEach(l -> l.highlightNodeCategoriesItemClicked()));

		highlightEdgesItem = new JMenuItem("Edit");
		highlightEdgesItem.addActionListener(e -> listeners.forEach(l -> l.highlightEdgesItemClicked()));
		clearHighlightedEdgesItem = new JMenuItem("Clear");
		clearHighlightedEdgesItem.addActionListener(e -> listeners.forEach(l -> l.clearHighlightedEdgesItemClicked()));
		selectHighlightedEdgesItem = new JMenuItem("Select Highlighted");
		selectHighlightedEdgesItem
				.addActionListener(e -> listeners.forEach(l -> l.selectHighlightedEdgesItemClicked()));
		highlightEdgeCategoriesItem = new JMenuItem("Add Category Highlighting");
		highlightEdgeCategoriesItem
				.addActionListener(e -> listeners.forEach(l -> l.highlightEdgeCategoriesItemClicked()));

		selectNodesItem = new JMenuItem("Set Selected " + owner.getNaming().Nodes());
		selectNodesItem.addActionListener(e -> listeners.forEach(l -> l.selectNodesItemClicked()));
		selectEdgesItem = new JMenuItem("Set Selected " + owner.getNaming().Edges());
		selectEdgesItem.addActionListener(e -> listeners.forEach(l -> l.selectEdgesItemClicked()));

		collapseToNodeItem = new JMenuItem("Collapse to Meta " + owner.getNaming().Node());
		collapseToNodeItem.addActionListener(e -> listeners.forEach(l -> l.collapseToNodeItemClicked()));
		expandFromNodeItem = new JMenuItem("Expand from Meta " + owner.getNaming().Node());
		expandFromNodeItem.addActionListener(e -> listeners.forEach(l -> l.expandFromNodeItemClicked()));
		collapseByPropertyItem = new JMenuItem("Collapse by Property");
		collapseByPropertyItem.addActionListener(e -> listeners.forEach(l -> l.collapseByPropertyItemClicked()));
		clearCollapsedNodesItem = new JMenuItem("Clear Collapsed " + owner.getNaming().Nodes());
		clearCollapsedNodesItem.addActionListener(e -> listeners.forEach(l -> l.clearCollapsedNodesItemClicked()));

		layoutItems = new ArrayList<>();

		for (LayoutType layoutType : LayoutType.values()) {
			JMenuItem item = new JMenuItem(layoutType.toString());

			item.addActionListener(e -> listeners.forEach(l -> l.layoutItemClicked(layoutType)));
			layoutItems.add(item);
		}
	}

	public static interface ClickListener {

		void resetLayoutItemClicked();

		void saveAsItemClicked();

		void layoutItemClicked(LayoutType layoutType);

		void selectConnectionsItemClicked();

		void selectIncomingItemClicked();

		void selectOutgoingItemClicked();

		void clearSelectedNodesItemClicked();

		void clearSelectedEdgesItemClicked();

		void highlightSelectedNodesItemClicked();

		void highlightSelectedEdgesItemClicked();

		void highlightNodesItemClicked();

		void highlightEdgesItemClicked();

		void clearHighlightedNodesItemClicked();

		void clearHighlightedEdgesItemClicked();

		void selectHighlightedNodesItemClicked();

		void selectHighlightedEdgesItemClicked();

		void highlightNodeCategoriesItemClicked();

		void highlightEdgeCategoriesItemClicked();

		void selectNodesItemClicked();

		void selectEdgesItemClicked();

		void nodePropertiesItemClicked();

		void nodeAllPropertiesItemClicked();

		void edgePropertiesItemClicked();

		void edgeAllPropertiesItemClicked();

		void collapseToNodeItemClicked();

		void expandFromNodeItemClicked();

		void collapseByPropertyItemClicked();

		void clearCollapsedNodesItemClicked();
	}
}
