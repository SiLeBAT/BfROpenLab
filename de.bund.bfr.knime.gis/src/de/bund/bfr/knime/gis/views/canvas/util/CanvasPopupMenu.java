/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
import java.util.EventListener;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import de.bund.bfr.jung.layout.LayoutType;
import de.bund.bfr.knime.gis.views.canvas.Canvas;

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
	private JMenuItem collapseSimpleChainsItem;
	
	private JMenuItem openExplosionViewItem;

	public CanvasPopupMenu(Canvas<?> owner, boolean allowEdges, boolean allowLayout, boolean allowCollapse, boolean allowOpenExplosionView) {
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

			nodeSelectionMenu.add(new JSeparator());
			
			if (allowCollapse) {
				nodeSelectionMenu.add(collapseToNodeItem);
				nodeSelectionMenu.add(expandFromNodeItem);
			}
			
			// in the previous version the following menu entry was dependent on allowCollapse,
			// this dependency was removed
			nodeSelectionMenu.add(nodeAllPropertiesItem); 

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
			add(collapseSimpleChainsItem);
		}
		if (allowOpenExplosionView) {
			nodeSelectionMenu.add(new JSeparator());
			nodeSelectionMenu.add(this.openExplosionViewItem);
		}
	}

	public void addClickListener(ClickListener listener) {
		listenerList.add(ClickListener.class, listener);
	}

	public void removeClickListener(ClickListener listener) {
		listenerList.remove(ClickListener.class, listener);
	}

	public void setNodeSelectionEnabled(boolean enabled) {
		nodeSelectionMenu.setEnabled(enabled);
	}

	public void setEdgeSelectionEnabled(boolean enabled) {
		edgeSelectionMenu.setEnabled(enabled);
	}
	
	public void setOpenExplosionViewEnabled(boolean enabled) {
		this.openExplosionViewItem.setEnabled(enabled);
	}

	private void init(Canvas<?> owner) {
		nodeSelectionMenu = new JMenu(owner.getNaming().Node() + " Selection");
		nodeSelectionMenu.setEnabled(false);
		edgeSelectionMenu = new JMenu(owner.getNaming().Edge() + " Selection");
		edgeSelectionMenu.setEnabled(false);
		nodeHighlightMenu = new JMenu(owner.getNaming().Node() + " Highlighting");
		edgeHighlightMenu = new JMenu(owner.getNaming().Edge() + " Highlighting");
		layoutMenu = new JMenu("Apply Layout");

		resetLayoutItem = createItem("Reset Layout", ClickListener::resetLayoutItemClicked);
		saveAsItem = createItem("Save As ...", ClickListener::saveAsItemClicked);

		clearSelectedNodesItem = createItem("Clear", ClickListener::clearSelectedNodesItemClicked);
		nodePropertiesItem = createItem("Show Properties", ClickListener::nodePropertiesItemClicked);
		nodeAllPropertiesItem = createItem("Show Uncollapsed Properties", ClickListener::nodeAllPropertiesItemClicked);
		highlightSelectedNodesItem = createItem("Highlight Selected", ClickListener::highlightSelectedNodesItemClicked);
		selectConnectionsItem = createItem("Select Connections", ClickListener::selectConnectionsItemClicked);
		selectIncomingItem = createItem("Select Incoming", ClickListener::selectIncomingItemClicked);
		selectOutgoingItem = createItem("Select Outgoing", ClickListener::selectOutgoingItemClicked);

		clearSelectedEdgesItem = createItem("Clear", ClickListener::clearSelectedEdgesItemClicked);
		edgePropertiesItem = createItem("Show Properties", ClickListener::edgePropertiesItemClicked);
		edgeAllPropertiesItem = createItem("Show Unjoined Properties", ClickListener::edgeAllPropertiesItemClicked);
		highlightSelectedEdgesItem = createItem("Highlight Selected", ClickListener::highlightSelectedEdgesItemClicked);

		highlightNodesItem = createItem("Edit", ClickListener::highlightNodesItemClicked);
		clearHighlightedNodesItem = createItem("Clear", ClickListener::clearHighlightedNodesItemClicked);
		selectHighlightedNodesItem = createItem("Select Highlighted", ClickListener::selectHighlightedNodesItemClicked);
		highlightNodeCategoriesItem = createItem("Add Category Highlighting",
				ClickListener::highlightNodeCategoriesItemClicked);

		highlightEdgesItem = createItem("Edit", ClickListener::highlightEdgesItemClicked);
		clearHighlightedEdgesItem = createItem("Clear", ClickListener::clearHighlightedEdgesItemClicked);
		selectHighlightedEdgesItem = createItem("Select Highlighted", ClickListener::selectHighlightedEdgesItemClicked);
		highlightEdgeCategoriesItem = createItem("Add Category Highlighting",
				ClickListener::highlightEdgeCategoriesItemClicked);

		selectNodesItem = createItem("Set Selected " + owner.getNaming().Nodes(),
				ClickListener::selectNodesItemClicked);
		selectEdgesItem = createItem("Set Selected " + owner.getNaming().Edges(),
				ClickListener::selectEdgesItemClicked);

		collapseToNodeItem = createItem("Collapse to Meta " + owner.getNaming().Node(),
				ClickListener::collapseToNodeItemClicked);
		expandFromNodeItem = createItem("Expand from Meta " + owner.getNaming().Node(),
				ClickListener::expandFromNodeItemClicked);
		collapseByPropertyItem = createItem("Collapse by Property", ClickListener::collapseByPropertyItemClicked);
		collapseSimpleChainsItem = createItem("Collapse Simple Chains", ClickListener::collapseSimpleChainsItemClicked);
		
		this.openExplosionViewItem = createItem("Show Contained Nodes", ClickListener::openExplosionViewItemClicked);
		
		layoutItems = new ArrayList<>();

		for (LayoutType layoutType : LayoutType.values()) {
			layoutItems.add(createItem(layoutType.toString(), l -> l.layoutItemClicked(layoutType)));
		}
	}

	private JMenuItem createItem(String text, Consumer<ClickListener> action) {
		JMenuItem item = new JMenuItem(text);

		item.addActionListener(e -> Stream.of(getListeners(ClickListener.class)).forEach(action));

		return item;
	}

	public static interface ClickListener extends EventListener {

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
		
		void collapseSimpleChainsItemClicked();
		
		void openExplosionViewItemClicked();

	}
}
