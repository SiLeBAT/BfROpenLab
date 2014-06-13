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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang.ArrayUtils;

import de.bund.bfr.knime.UI;

import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

public class CanvasOptionsPanel implements ActionListener {

	private static final Mode DEFAULT_MODE = Mode.TRANSFORMING;
	private static final boolean DEFAULT_SHOW_LEGEND = false;
	private static final boolean DEFAULT_JOIN_EDGES = false;
	private static final boolean DEFAULT_SKIP_EDGELESS_NODES = false;
	private static final int DEFAULT_FONT_SIZE = 12;
	private static final boolean DEFAULT_FONT_BOLD = false;
	private static final int DEFAULT_NODE_SIZE = 10;
	private static final int DEFAULT_BORDER_ALPHA = 255;

	private static final int[] TEXT_SIZES = { 10, 12, 14, 18, 24 };
	private static final int[] NODE_SIZES = { 4, 6, 10, 14, 20, 40 };

	private Mode editingMode;
	private JComboBox<Mode> editingModeBox;
	private boolean showLegend;
	private JCheckBox showLegendBox;
	private boolean joinEdges;
	private JCheckBox joinEdgesBox;
	private boolean skipEdgelessNodes;
	private JCheckBox skipEdgelessNodesBox;
	private int fontSize;
	private JComboBox<Integer> fontSizeBox;
	private boolean fontBold;
	private JCheckBox fontBoldBox;
	private int nodeSize;
	private JComboBox<Integer> nodeSizeBox;
	private int borderAlpha;
	private JSlider borderAlphaSlider;
	private JButton borderAlphaButton;

	private List<ChangeListener> listeners;

	public CanvasOptionsPanel() {
		listeners = new ArrayList<ChangeListener>();

		editingMode = DEFAULT_MODE;
		showLegend = DEFAULT_SHOW_LEGEND;
		joinEdges = DEFAULT_JOIN_EDGES;
		skipEdgelessNodes = DEFAULT_SKIP_EDGELESS_NODES;
		fontSize = DEFAULT_FONT_SIZE;
		fontBold = DEFAULT_FONT_BOLD;
		nodeSize = DEFAULT_NODE_SIZE;
		borderAlpha = DEFAULT_BORDER_ALPHA;

		editingModeBox = new JComboBox<Mode>(new Mode[] { Mode.TRANSFORMING,
				Mode.PICKING });
		editingModeBox.setSelectedItem(editingMode);
		editingModeBox.addActionListener(this);
		showLegendBox = new JCheckBox("Activate");
		showLegendBox.setSelected(showLegend);
		showLegendBox.addActionListener(this);
		joinEdgesBox = new JCheckBox("Activate");
		joinEdgesBox.setSelected(joinEdges);
		joinEdgesBox.addActionListener(this);
		skipEdgelessNodesBox = new JCheckBox("Activate");
		skipEdgelessNodesBox.setSelected(skipEdgelessNodes);
		skipEdgelessNodesBox.addActionListener(this);
		fontSizeBox = new JComboBox<Integer>(ArrayUtils.toObject(TEXT_SIZES));
		fontSizeBox.setEditable(true);
		((JTextField) fontSizeBox.getEditor().getEditorComponent())
				.setColumns(3);
		fontSizeBox.setSelectedItem(fontSize);
		fontSizeBox.addActionListener(this);
		fontBoldBox = new JCheckBox("Bold");
		fontBoldBox.setSelected(fontBold);
		fontBoldBox.addActionListener(this);
		nodeSizeBox = new JComboBox<Integer>(ArrayUtils.toObject(NODE_SIZES));
		nodeSizeBox.setEditable(true);
		((JTextField) nodeSizeBox.getEditor().getEditorComponent())
				.setColumns(3);
		nodeSizeBox.setSelectedItem(nodeSize);
		nodeSizeBox.addActionListener(this);
		borderAlphaSlider = new JSlider(0, 255, borderAlpha);
		borderAlphaSlider.setPreferredSize(new Dimension(100, borderAlphaSlider
				.getPreferredSize().height));
		borderAlphaButton = new JButton("Apply");
		borderAlphaButton.addActionListener(this);
	}

	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		listeners.remove(listener);
	}

	public JComponent createPanel(boolean allowEdges, boolean allowNodes,
			boolean allowBorderAlpha) {
		JPanel optionsPanel = new JPanel();

		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.X_AXIS));
		optionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		addOptionsItem(optionsPanel, "Editing Mode", editingModeBox);
		addOptionsItem(optionsPanel, "Show Legend", showLegendBox);
		addOptionsItem(optionsPanel, "Font", fontSizeBox, fontBoldBox);

		if (allowEdges) {
			addOptionsItem(optionsPanel, "Join Edges", joinEdgesBox);
			addOptionsItem(optionsPanel, "Skip Edgeless Nodes",
					skipEdgelessNodesBox);
		}

		if (allowNodes) {
			addOptionsItem(optionsPanel, "Node Size", nodeSizeBox);
		}

		if (allowBorderAlpha) {
			addOptionsItem(optionsPanel, "Border Alpha", borderAlphaSlider,
					borderAlphaButton);
		}

		return optionsPanel;
	}

	public Mode getEditingMode() {
		return editingMode;
	}

	public boolean isShowLegend() {
		return showLegend;
	}

	public boolean isJoinEdges() {
		return joinEdges;
	}

	public boolean isSkipEdgelessNodes() {
		return skipEdgelessNodes;
	}

	public int getFontSize() {
		return fontSize;
	}

	public boolean isFontBold() {
		return fontBold;
	}

	public int getNodeSize() {
		return nodeSize;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == editingModeBox) {
			editingMode = (Mode) editingModeBox.getSelectedItem();

			for (ChangeListener l : listeners) {
				l.editingModeChanged();
			}
		} else if (e.getSource() == showLegendBox) {
			showLegend = showLegendBox.isSelected();

			for (ChangeListener l : listeners) {
				l.showLegendChanged();
			}
		} else if (e.getSource() == joinEdgesBox) {
			joinEdges = joinEdgesBox.isSelected();

			for (ChangeListener l : listeners) {
				l.joinEdgesChanged();
			}
		} else if (e.getSource() == skipEdgelessNodesBox) {
			skipEdgelessNodes = skipEdgelessNodesBox.isSelected();

			for (ChangeListener l : listeners) {
				l.skipEdgelessNodesChanged();
			}
		} else if (e.getSource() == fontSizeBox) {
			Object size = fontSizeBox.getSelectedItem();

			if (size instanceof Integer) {
				fontSize = (Integer) size;

				for (ChangeListener l : listeners) {
					l.fontSizeChanged();
				}
			} else {
				JOptionPane.showMessageDialog(fontSizeBox, size
						+ " is not a valid number", "Error",
						JOptionPane.ERROR_MESSAGE);
				fontSizeBox.setSelectedItem(fontSize);
			}
		} else if (e.getSource() == fontBoldBox) {
			fontBold = fontBoldBox.isSelected();

			for (ChangeListener l : listeners) {
				l.fontBoldChanged();
			}
		} else if (e.getSource() == nodeSizeBox) {
			Object size = nodeSizeBox.getSelectedItem();

			if (size instanceof Integer) {
				nodeSize = (Integer) size;

				for (ChangeListener l : listeners) {
					l.nodeSizeChanged();
				}
			} else {
				JOptionPane.showMessageDialog(nodeSizeBox, size
						+ " is not a valid number", "Error",
						JOptionPane.ERROR_MESSAGE);
				nodeSizeBox.setSelectedItem(nodeSize);
			}
		} else if (e.getSource() == borderAlphaButton) {
			borderAlpha = borderAlphaSlider.getValue();
			
			for (ChangeListener l : listeners) {
				l.borderAlphaChanged();
			}
		}
	}

	private void addOptionsItem(JPanel optionsPanel, String name,
			JComponent... components) {
		JPanel panel = new JPanel();
		TitledBorder border = BorderFactory.createTitledBorder(name);

		panel.setBorder(border);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		for (JComponent c : components) {
			panel.add(UI.createCenterPanel(c));
		}

		if (components.length == 1) {
			int titleWidth = border.getMinimumSize(components[0]).width;

			components[0].setPreferredSize(new Dimension(Math.max(
					components[0].getPreferredSize().width, titleWidth),
					components[0].getPreferredSize().height));
		}

		optionsPanel.add(panel);
		optionsPanel.add(Box.createHorizontalStrut(5));
	}

	public static interface ChangeListener {

		public void editingModeChanged();

		public void showLegendChanged();

		public void joinEdgesChanged();

		public void skipEdgelessNodesChanged();

		public void fontSizeChanged();

		public void fontBoldChanged();

		public void nodeSizeChanged();
		
		public void borderAlphaChanged();
	}
}
