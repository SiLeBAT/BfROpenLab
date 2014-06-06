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

public class CanvasPopupMenu implements ActionListener {

	private JMenu nodeSelectionMenu;
	private JMenu edgeSelectionMenu;
	private JMenu nodeHighlightMenu;
	private JMenu edgeHighlightMenu;
	private JMenu layoutMenu;

	private JMenuItem resetLayoutItem;
	private JMenuItem saveAsItem;
	private JMenuItem selectConnectingItem;
	private JMenuItem clearSelectNodesItem;
	private JMenuItem clearSelectEdgesItem;
	private JMenuItem highlightNodesItem;
	private JMenuItem clearHighlightNodesItem;
	private JMenuItem selectHighlightedNodesItem;
	private JMenuItem selectNodesItem;
	private JMenuItem highlightSelectedNodesItem;
	private JMenuItem highlightEdgesItem;
	private JMenuItem clearHighlightEdgesItem;
	private JMenuItem selectHighlightedEdgesItem;
	private JMenuItem selectEdgesItem;
	private JMenuItem highlightSelectedEdgesItem;
	private JMenuItem nodePropertiesItem;
	private JMenuItem edgePropertiesItem;
	private JMenuItem edgeAllPropertiesItem;
	private JMenuItem collapseToNodeItem;
	private JMenuItem expandFromNodeItem;
	private JMenuItem collapseByPropertyItem;
	private JMenuItem clearCollapsedNodesItem;
	private Map<JMenuItem, LayoutType> layoutItems;

	private List<ClickListener> listeners;

	public CanvasPopupMenu() {
		listeners = new ArrayList<ClickListener>();

		nodeSelectionMenu = new JMenu("Node Selection");
		nodeSelectionMenu.setEnabled(false);
		edgeSelectionMenu = new JMenu("Edge Selection");
		edgeSelectionMenu.setEnabled(false);
		nodeHighlightMenu = new JMenu("Node Highlighting");
		edgeHighlightMenu = new JMenu("Edge Highlighting");
		layoutMenu = new JMenu("Apply Layout");

		resetLayoutItem = new JMenuItem("Reset Layout");
		resetLayoutItem.addActionListener(this);
		saveAsItem = new JMenuItem("Save As ...");
		saveAsItem.addActionListener(this);

		clearSelectNodesItem = new JMenuItem("Clear");
		clearSelectNodesItem.addActionListener(this);
		nodePropertiesItem = new JMenuItem("Show Properties");
		nodePropertiesItem.addActionListener(this);
		selectConnectingItem = new JMenuItem("Select Connections");
		selectConnectingItem.addActionListener(this);
		highlightSelectedNodesItem = new JMenuItem("Highlight Selected");
		highlightSelectedNodesItem.addActionListener(this);

		clearSelectEdgesItem = new JMenuItem("Clear");
		clearSelectEdgesItem.addActionListener(this);
		edgePropertiesItem = new JMenuItem("Show Properties");
		edgePropertiesItem.addActionListener(this);
		edgeAllPropertiesItem = new JMenuItem("Show All Properties");
		edgeAllPropertiesItem.addActionListener(this);
		highlightSelectedEdgesItem = new JMenuItem("Highlight Selected");
		highlightSelectedEdgesItem.addActionListener(this);

		highlightNodesItem = new JMenuItem("Edit");
		highlightNodesItem.addActionListener(this);
		clearHighlightNodesItem = new JMenuItem("Clear");
		clearHighlightNodesItem.addActionListener(this);
		selectHighlightedNodesItem = new JMenuItem("Select Highlighted");
		selectHighlightedNodesItem.addActionListener(this);
		selectNodesItem = new JMenuItem("Select");
		selectNodesItem.addActionListener(this);

		highlightEdgesItem = new JMenuItem("Edit");
		highlightEdgesItem.addActionListener(this);
		clearHighlightEdgesItem = new JMenuItem("Clear");
		clearHighlightEdgesItem.addActionListener(this);
		selectHighlightedEdgesItem = new JMenuItem("Select Highlighted");
		selectHighlightedEdgesItem.addActionListener(this);
		selectEdgesItem = new JMenuItem("Select");
		selectEdgesItem.addActionListener(this);

		collapseToNodeItem = new JMenuItem("Collapse to Meta Node");
		collapseToNodeItem.addActionListener(this);
		expandFromNodeItem = new JMenuItem("Expand from Meta Node");
		expandFromNodeItem.addActionListener(this);
		collapseByPropertyItem = new JMenuItem("Collapse by Property");
		collapseByPropertyItem.addActionListener(this);
		clearCollapsedNodesItem = new JMenuItem("Clear Collapsed Nodes");
		clearCollapsedNodesItem.addActionListener(this);

		layoutItems = new LinkedHashMap<JMenuItem, LayoutType>();

		for (LayoutType layoutType : LayoutType.values()) {
			JMenuItem item = new JMenuItem(layoutType.toString());

			item.addActionListener(this);
			layoutItems.put(item, layoutType);
		}
	}

	public void addClickListener(ClickListener listener) {
		listeners.add(listener);
	}

	public void removeClickListener(ClickListener listener) {
		listeners.remove(listener);
	}

	public JPopupMenu createMenu(boolean allowEdges, boolean allowHighlighting,
			boolean allowLayout, boolean allowCollapse) {
		JPopupMenu popup = new JPopupMenu();

		popup.add(resetLayoutItem);
		popup.add(saveAsItem);

		if (allowLayout) {
			for (JMenuItem item : layoutItems.keySet()) {
				layoutMenu.add(item);
			}

			popup.add(layoutMenu);
		}

		if (allowEdges) {
			nodeSelectionMenu.add(nodePropertiesItem);
			nodeSelectionMenu.add(clearSelectNodesItem);
			nodeSelectionMenu.add(selectConnectingItem);
			nodeSelectionMenu.add(highlightSelectedNodesItem);

			if (allowCollapse) {
				nodeSelectionMenu.add(new JSeparator());
				nodeSelectionMenu.add(collapseToNodeItem);
				nodeSelectionMenu.add(expandFromNodeItem);
			}

			edgeSelectionMenu.add(edgePropertiesItem);
			edgeSelectionMenu.add(edgeAllPropertiesItem);
			edgeSelectionMenu.add(clearSelectEdgesItem);
			edgeSelectionMenu.add(highlightSelectedEdgesItem);

			popup.add(new JSeparator());
			popup.add(nodeSelectionMenu);
			popup.add(edgeSelectionMenu);

			if (allowHighlighting) {
				nodeHighlightMenu.add(highlightNodesItem);
				nodeHighlightMenu.add(clearHighlightNodesItem);
				nodeHighlightMenu.add(selectHighlightedNodesItem);
				nodeHighlightMenu.add(selectNodesItem);

				edgeHighlightMenu.add(highlightEdgesItem);
				edgeHighlightMenu.add(clearHighlightEdgesItem);
				edgeHighlightMenu.add(selectHighlightedEdgesItem);
				edgeHighlightMenu.add(selectEdgesItem);

				popup.add(nodeHighlightMenu);
				popup.add(edgeHighlightMenu);
			}
		} else {
			nodeSelectionMenu.add(nodePropertiesItem);
			nodeSelectionMenu.add(clearSelectNodesItem);
			nodeSelectionMenu.add(highlightSelectedNodesItem);

			popup.add(new JSeparator());
			popup.add(nodeSelectionMenu);

			if (allowHighlighting) {
				nodeHighlightMenu.add(highlightNodesItem);
				nodeHighlightMenu.add(clearHighlightNodesItem);
				nodeHighlightMenu.add(selectHighlightedNodesItem);
				nodeHighlightMenu.add(selectNodesItem);

				popup.add(nodeHighlightMenu);
			}
		}

		if (allowCollapse) {
			popup.add(new JSeparator());
			popup.add(collapseByPropertyItem);
			popup.add(clearCollapsedNodesItem);
		}

		return popup;
	}

	public void setNodeSelectionEnabled(boolean enabled) {
		nodeSelectionMenu.setEnabled(enabled);
	}

	public void setEdgeSelectionEnabled(boolean enabled) {
		edgeSelectionMenu.setEnabled(enabled);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == saveAsItem) {
			for (ClickListener l : listeners) {
				l.saveAsItemClicked();
			}
		} else if (e.getSource() == selectConnectingItem) {
			for (ClickListener l : listeners) {
				l.selectConnectingItemClicked();
			}
		} else if (e.getSource() == selectHighlightedNodesItem) {
			for (ClickListener l : listeners) {
				l.selectHighlightedNodesItemClicked();
			}
		} else if (e.getSource() == selectHighlightedEdgesItem) {
			for (ClickListener l : listeners) {
				l.selectHighlightedEdgesItemClicked();
			}
		} else if (e.getSource() == selectNodesItem) {
			for (ClickListener l : listeners) {
				l.selectNodesItemClicked();
			}
		} else if (e.getSource() == selectEdgesItem) {
			for (ClickListener l : listeners) {
				l.selectEdgesItemClicked();
			}
		} else if (e.getSource() == resetLayoutItem) {
			for (ClickListener l : listeners) {
				l.resetLayoutItemClicked();
			}
		} else if (e.getSource() == clearSelectNodesItem) {
			for (ClickListener l : listeners) {
				l.clearSelectNodesItemClicked();
			}
		} else if (e.getSource() == clearSelectEdgesItem) {
			for (ClickListener l : listeners) {
				l.clearSelectEdgesItemClicked();
			}
		} else if (e.getSource() == highlightNodesItem) {
			for (ClickListener l : listeners) {
				l.highlightNodesItemClicked();
			}
		} else if (e.getSource() == clearHighlightNodesItem) {
			for (ClickListener l : listeners) {
				l.clearHighlightNodesItemClicked();
			}
		} else if (e.getSource() == highlightEdgesItem) {
			for (ClickListener l : listeners) {
				l.highlightEdgesItemClicked();
			}
		} else if (e.getSource() == clearHighlightEdgesItem) {
			for (ClickListener l : listeners) {
				l.clearHighlightEdgesItemClicked();
			}
		} else if (e.getSource() == nodePropertiesItem) {
			for (ClickListener l : listeners) {
				l.nodePropertiesItemClicked();
			}
		} else if (e.getSource() == edgePropertiesItem) {
			for (ClickListener l : listeners) {
				l.edgePropertiesItemClicked();
			}
		} else if (e.getSource() == edgeAllPropertiesItem) {
			for (ClickListener l : listeners) {
				l.edgeAllPropertiesItemClicked();
			}
		} else if (e.getSource() == highlightSelectedNodesItem) {
			for (ClickListener l : listeners) {
				l.highlightSelectedNodesItemClicked();
			}
		} else if (e.getSource() == highlightSelectedEdgesItem) {
			for (ClickListener l : listeners) {
				l.highlightSelectedEdgesItemClicked();
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
		} else if (layoutItems.containsKey(e.getSource())) {
			for (ClickListener l : listeners) {
				l.layoutItemClicked(layoutItems.get(e.getSource()));
			}
		}
	}

	public static interface ClickListener {

		public void saveAsItemClicked();

		public void layoutItemClicked(LayoutType layoutType);

		public void clearCollapsedNodesItemClicked();

		public void collapseByPropertyItemClicked();

		public void expandFromNodeItemClicked();

		public void collapseToNodeItemClicked();

		public void highlightSelectedEdgesItemClicked();

		public void highlightSelectedNodesItemClicked();

		public void edgeAllPropertiesItemClicked();

		public void edgePropertiesItemClicked();

		public void nodePropertiesItemClicked();

		public void clearHighlightEdgesItemClicked();

		public void highlightEdgesItemClicked();

		public void clearHighlightNodesItemClicked();

		public void highlightNodesItemClicked();

		public void clearSelectEdgesItemClicked();

		public void clearSelectNodesItemClicked();

		public void resetLayoutItemClicked();

		public void selectEdgesItemClicked();

		public void selectNodesItemClicked();

		public void selectHighlightedEdgesItemClicked();

		public void selectHighlightedNodesItemClicked();

		public void selectConnectingItemClicked();

	}
}
