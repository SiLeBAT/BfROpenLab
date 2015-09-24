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
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import de.bund.bfr.knime.gis.views.canvas.jung.layout.LayoutType;

public class CanvasPopupMenu extends JPopupMenu implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JMenu nodeSelectionMenu;
	private JMenu edgeSelectionMenu;
	private JMenu nodeHighlightMenu;
	private JMenu edgeHighlightMenu;
	private JMenu layoutMenu;

	private JMenuItem resetLayoutItem;
	private JMenuItem saveAsItem;
	private Map<JMenuItem, LayoutType> layoutItems;

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
			for (JMenuItem item : layoutItems.keySet()) {
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

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == resetLayoutItem) {
			for (ClickListener l : listeners) {
				l.resetLayoutItemClicked();
			}
		} else if (e.getSource() == saveAsItem) {
			for (ClickListener l : listeners) {
				l.saveAsItemClicked();
			}
		} else if (layoutItems.containsKey(e.getSource())) {
			for (ClickListener l : listeners) {
				l.layoutItemClicked(layoutItems.get(e.getSource()));
			}
		} else if (e.getSource() == selectConnectionsItem) {
			for (ClickListener l : listeners) {
				l.selectConnectionsItemClicked();
			}
		} else if (e.getSource() == selectIncomingItem) {
			for (ClickListener l : listeners) {
				l.selectIncomingItemClicked();
			}
		} else if (e.getSource() == selectOutgoingItem) {
			for (ClickListener l : listeners) {
				l.selectOutgoingItemClicked();
			}
		} else if (e.getSource() == clearSelectedNodesItem) {
			for (ClickListener l : listeners) {
				l.clearSelectedNodesItemClicked();
			}
		} else if (e.getSource() == clearSelectedEdgesItem) {
			for (ClickListener l : listeners) {
				l.clearSelectedEdgesItemClicked();
			}
		} else if (e.getSource() == highlightSelectedNodesItem) {
			for (ClickListener l : listeners) {
				l.highlightSelectedNodesItemClicked();
			}
		} else if (e.getSource() == highlightSelectedEdgesItem) {
			for (ClickListener l : listeners) {
				l.highlightSelectedEdgesItemClicked();
			}
		} else if (e.getSource() == highlightNodesItem) {
			for (ClickListener l : listeners) {
				l.highlightNodesItemClicked();
			}
		} else if (e.getSource() == highlightEdgesItem) {
			for (ClickListener l : listeners) {
				l.highlightEdgesItemClicked();
			}
		} else if (e.getSource() == clearHighlightedNodesItem) {
			for (ClickListener l : listeners) {
				l.clearHighlightedNodesItemClicked();
			}
		} else if (e.getSource() == clearHighlightedEdgesItem) {
			for (ClickListener l : listeners) {
				l.clearHighlightedEdgesItemClicked();
			}
		} else if (e.getSource() == selectHighlightedNodesItem) {
			for (ClickListener l : listeners) {
				l.selectHighlightedNodesItemClicked();
			}
		} else if (e.getSource() == selectHighlightedEdgesItem) {
			for (ClickListener l : listeners) {
				l.selectHighlightedEdgesItemClicked();
			}
		} else if (e.getSource() == highlightNodeCategoriesItem) {
			for (ClickListener l : listeners) {
				l.highlightNodeCategoriesItemClicked();
			}
		} else if (e.getSource() == highlightEdgeCategoriesItem) {
			for (ClickListener l : listeners) {
				l.highlightEdgeCategoriesItemClicked();
			}
		} else if (e.getSource() == selectNodesItem) {
			for (ClickListener l : listeners) {
				l.selectNodesItemClicked();
			}
		} else if (e.getSource() == selectEdgesItem) {
			for (ClickListener l : listeners) {
				l.selectEdgesItemClicked();
			}
		} else if (e.getSource() == nodePropertiesItem) {
			for (ClickListener l : listeners) {
				l.nodePropertiesItemClicked();
			}
		} else if (e.getSource() == nodeAllPropertiesItem) {
			for (ClickListener l : listeners) {
				l.nodeAllPropertiesItemClicked();
			}
		} else if (e.getSource() == edgePropertiesItem) {
			for (ClickListener l : listeners) {
				l.edgePropertiesItemClicked();
			}
		} else if (e.getSource() == edgeAllPropertiesItem) {
			for (ClickListener l : listeners) {
				l.edgeAllPropertiesItemClicked();
			}
		} else if (e.getSource() == collapseToNodeItem) {
			for (ClickListener l : listeners) {
				l.collapseToNodeItemClicked();
			}
		} else if (e.getSource() == expandFromNodeItem) {
			for (ClickListener l : listeners) {
				l.expandFromNodeItemClicked();
			}
		} else if (e.getSource() == collapseByPropertyItem) {
			for (ClickListener l : listeners) {
				l.collapseByPropertyItemClicked();
			}
		} else if (e.getSource() == clearCollapsedNodesItem) {
			for (ClickListener l : listeners) {
				l.clearCollapsedNodesItemClicked();
			}
		}
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
		resetLayoutItem.addActionListener(this);
		saveAsItem = new JMenuItem("Save As ...");
		saveAsItem.addActionListener(this);

		clearSelectedNodesItem = new JMenuItem("Clear");
		clearSelectedNodesItem.addActionListener(this);
		nodePropertiesItem = new JMenuItem("Show Properties");
		nodePropertiesItem.addActionListener(this);
		nodeAllPropertiesItem = new JMenuItem("Show Uncollapsed Properties");
		nodeAllPropertiesItem.addActionListener(this);
		highlightSelectedNodesItem = new JMenuItem("Highlight Selected");
		highlightSelectedNodesItem.addActionListener(this);
		selectConnectionsItem = new JMenuItem("Select Connections");
		selectConnectionsItem.addActionListener(this);
		selectIncomingItem = new JMenuItem("Select Incoming");
		selectIncomingItem.addActionListener(this);
		selectOutgoingItem = new JMenuItem("Select Outgoing");
		selectOutgoingItem.addActionListener(this);

		clearSelectedEdgesItem = new JMenuItem("Clear");
		clearSelectedEdgesItem.addActionListener(this);
		edgePropertiesItem = new JMenuItem("Show Properties");
		edgePropertiesItem.addActionListener(this);
		edgeAllPropertiesItem = new JMenuItem("Show Unjoined Properties");
		edgeAllPropertiesItem.addActionListener(this);
		highlightSelectedEdgesItem = new JMenuItem("Highlight Selected");
		highlightSelectedEdgesItem.addActionListener(this);

		highlightNodesItem = new JMenuItem("Edit");
		highlightNodesItem.addActionListener(this);
		clearHighlightedNodesItem = new JMenuItem("Clear");
		clearHighlightedNodesItem.addActionListener(this);
		selectHighlightedNodesItem = new JMenuItem("Select Highlighted");
		selectHighlightedNodesItem.addActionListener(this);
		highlightNodeCategoriesItem = new JMenuItem("Add Category Highlighting");
		highlightNodeCategoriesItem.addActionListener(this);

		highlightEdgesItem = new JMenuItem("Edit");
		highlightEdgesItem.addActionListener(this);
		clearHighlightedEdgesItem = new JMenuItem("Clear");
		clearHighlightedEdgesItem.addActionListener(this);
		selectHighlightedEdgesItem = new JMenuItem("Select Highlighted");
		selectHighlightedEdgesItem.addActionListener(this);
		highlightEdgeCategoriesItem = new JMenuItem("Add Category Highlighting");
		highlightEdgeCategoriesItem.addActionListener(this);

		selectNodesItem = new JMenuItem("Select " + owner.getNaming().Nodes());
		selectNodesItem.addActionListener(this);
		selectEdgesItem = new JMenuItem("Select " + owner.getNaming().Edges());
		selectEdgesItem.addActionListener(this);

		collapseToNodeItem = new JMenuItem("Collapse to Meta " + owner.getNaming().Node());
		collapseToNodeItem.addActionListener(this);
		expandFromNodeItem = new JMenuItem("Expand from Meta " + owner.getNaming().Node());
		expandFromNodeItem.addActionListener(this);
		collapseByPropertyItem = new JMenuItem("Collapse by Property");
		collapseByPropertyItem.addActionListener(this);
		clearCollapsedNodesItem = new JMenuItem("Clear Collapsed " + owner.getNaming().Nodes());
		clearCollapsedNodesItem.addActionListener(this);

		layoutItems = new LinkedHashMap<>();

		for (LayoutType layoutType : LayoutType.values()) {
			JMenuItem item = new JMenuItem(layoutType.toString());

			item.addActionListener(this);
			layoutItems.put(item, layoutType);
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
