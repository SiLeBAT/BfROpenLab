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
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;

import com.google.common.primitives.Ints;

import de.bund.bfr.knime.UI;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

public class CanvasOptionsPanel extends JScrollPane implements ActionListener,
		ItemListener {

	private static final long serialVersionUID = 1L;

	private static final Mode DEFAULT_MODE = Mode.TRANSFORMING;
	private static final boolean DEFAULT_SHOW_LEGEND = false;
	private static final boolean DEFAULT_JOIN_EDGES = false;
	private static final boolean DEFAULT_SKIP_EDGELESS_NODES = false;
	private static final int DEFAULT_FONT_SIZE = 12;
	private static final boolean DEFAULT_FONT_BOLD = false;
	private static final int DEFAULT_NODE_SIZE = 10;
	private static final boolean DEFAULT_ARROW_IN_MIDDLE = false;
	private static final int DEFAULT_BORDER_ALPHA = 255;

	private static final int[] TEXT_SIZES = { 10, 12, 14, 18, 24 };
	private static final int[] NODE_SIZES = { 4, 6, 10, 14, 20, 40 };

	private JPanel panel;

	private JComboBox<Mode> editingModeBox;
	private JCheckBox showLegendBox;
	private JCheckBox joinEdgesBox;
	private JCheckBox skipEdgelessNodesBox;
	private int fontSize;
	private JComboBox<Integer> fontSizeBox;
	private JCheckBox fontBoldBox;
	private int nodeSize;
	private JComboBox<Integer> nodeSizeBox;
	private JCheckBox arrowInMiddleBox;
	private String label;
	private JTextField labelField;
	private JButton labelButton;
	private int borderAlpha;
	private JSlider borderAlphaSlider;
	private JButton borderAlphaButton;

	private List<ChangeListener> listeners;

	public CanvasOptionsPanel(Canvas<?> owner, boolean allowEdges,
			boolean allowNodeResize, boolean allowPolygons) {
		init();

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(getOptionPanel("Editing Mode", editingModeBox));
		panel.add(Box.createHorizontalStrut(5));
		panel.add(getOptionPanel("Show Legend", showLegendBox));
		panel.add(Box.createHorizontalStrut(5));
		panel.add(getOptionPanel("Font", fontSizeBox, fontBoldBox));
		panel.add(getOptionPanel("Label", labelField, labelButton));

		if (allowEdges) {
			panel.add(Box.createHorizontalStrut(5));
			panel.add(getOptionPanel("Join " + owner.getEdgesName(),
					joinEdgesBox));
		}

		if (allowEdges && allowNodeResize) {
			panel.add(Box.createHorizontalStrut(5));
			panel.add(getOptionPanel(
					"Skip Unconnected " + owner.getNodesName(),
					skipEdgelessNodesBox));
		}

		if (allowNodeResize) {
			panel.add(Box.createHorizontalStrut(5));
			panel.add(getOptionPanel(owner.getNodeName() + " Size", nodeSizeBox));
		}

		if (allowEdges) {
			panel.add(Box.createHorizontalStrut(5));
			panel.add(getOptionPanel("Arrow in Middle", arrowInMiddleBox));
		}

		if (allowPolygons) {
			panel.add(Box.createHorizontalStrut(5));
			panel.add(getOptionPanel("Border Alpha", borderAlphaSlider,
					borderAlphaButton));
		}

		setViewportView(UI.createWestPanel(UI.createNorthPanel(panel)));
		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		setPreferredSize(getPreferredSize());
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}

	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		listeners.remove(listener);
	}

	public void addOption(String name, JComponent... components) {
		panel.add(Box.createHorizontalStrut(5));
		panel.add(getOptionPanel(name, components));
	}

	public Mode getEditingMode() {
		return (Mode) editingModeBox.getSelectedItem();
	}

	public void setEditingMode(Mode editingMode) {
		editingModeBox.setSelectedItem(editingMode);
	}

	public boolean isShowLegend() {
		return showLegendBox.isSelected();
	}

	public void setShowLegend(boolean showLegend) {
		showLegendBox.setSelected(showLegend);
	}

	public boolean isJoinEdges() {
		return joinEdgesBox.isSelected();
	}

	public void setJoinEdges(boolean joinEdges) {
		joinEdgesBox.setSelected(joinEdges);
	}

	public boolean isSkipEdgelessNodes() {
		return skipEdgelessNodesBox.isSelected();
	}

	public void setSkipEdgelessNodes(boolean skipEdgelessNodes) {
		skipEdgelessNodesBox.setSelected(skipEdgelessNodes);
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
		fontSizeBox.setSelectedItem(fontSize);
	}

	public boolean isFontBold() {
		return fontBoldBox.isSelected();
	}

	public void setFontBold(boolean fontBold) {
		fontBoldBox.setSelected(fontBold);
	}

	public int getNodeSize() {
		return nodeSize;
	}

	public void setNodeSize(int nodeSize) {
		this.nodeSize = nodeSize;
		nodeSizeBox.setSelectedItem(nodeSize);
	}

	public boolean isArrowInMiddle() {
		return arrowInMiddleBox.isSelected();
	}

	public void setArrowInMiddle(boolean arrowInMiddle) {
		arrowInMiddleBox.setSelected(arrowInMiddle);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
		labelField.setText(label != null ? label : "");
		labelButton.doClick();
	}

	public int getBorderAlpha() {
		return borderAlpha;
	}

	public void setBorderAlpha(int borderAlpha) {
		this.borderAlpha = borderAlpha;
		borderAlphaSlider.setValue(borderAlpha);
		borderAlphaButton.doClick();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == borderAlphaButton) {
			borderAlpha = borderAlphaSlider.getValue();

			for (ChangeListener l : listeners) {
				l.borderAlphaChanged();
			}
		} else if (e.getSource() == labelButton) {
			label = labelField.getText();

			for (ChangeListener l : listeners) {
				l.labelChanged();
			}
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == editingModeBox
				&& e.getStateChange() == ItemEvent.SELECTED) {
			for (ChangeListener l : listeners) {
				l.editingModeChanged();
			}
		} else if (e.getSource() == showLegendBox) {
			for (ChangeListener l : listeners) {
				l.showLegendChanged();
			}
		} else if (e.getSource() == joinEdgesBox) {
			for (ChangeListener l : listeners) {
				l.joinEdgesChanged();
			}
		} else if (e.getSource() == skipEdgelessNodesBox) {
			for (ChangeListener l : listeners) {
				l.skipEdgelessNodesChanged();
			}
		} else if (e.getSource() == fontSizeBox
				&& e.getStateChange() == ItemEvent.SELECTED) {
			Object size = fontSizeBox.getSelectedItem();

			if (size instanceof Integer) {
				fontSize = (Integer) size;

				for (ChangeListener l : listeners) {
					l.fontChanged();
				}
			} else {
				JOptionPane.showMessageDialog(fontSizeBox, size
						+ " is not a valid number", "Error",
						JOptionPane.ERROR_MESSAGE);
				fontSizeBox.setSelectedItem(fontSize);
			}
		} else if (e.getSource() == fontBoldBox) {
			for (ChangeListener l : listeners) {
				l.fontChanged();
			}
		} else if (e.getSource() == nodeSizeBox
				&& e.getStateChange() == ItemEvent.SELECTED) {
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
		} else if (e.getSource() == arrowInMiddleBox) {
			for (ChangeListener l : listeners) {
				l.arrowInMiddleChanged();
			}
		}
	}

	private void init() {
		listeners = new ArrayList<>();

		fontSize = DEFAULT_FONT_SIZE;
		nodeSize = DEFAULT_NODE_SIZE;
		borderAlpha = DEFAULT_BORDER_ALPHA;

		editingModeBox = new JComboBox<>(new Mode[] { Mode.TRANSFORMING,
				Mode.PICKING });
		editingModeBox.setSelectedItem(DEFAULT_MODE);
		editingModeBox.addItemListener(this);
		showLegendBox = new JCheckBox("Activate");
		showLegendBox.setSelected(DEFAULT_SHOW_LEGEND);
		showLegendBox.addItemListener(this);
		joinEdgesBox = new JCheckBox("Activate");
		joinEdgesBox.setSelected(DEFAULT_JOIN_EDGES);
		joinEdgesBox.addItemListener(this);
		skipEdgelessNodesBox = new JCheckBox("Activate");
		skipEdgelessNodesBox.setSelected(DEFAULT_SKIP_EDGELESS_NODES);
		skipEdgelessNodesBox.addItemListener(this);
		fontSizeBox = new JComboBox<>(new Vector<>(Ints.asList(TEXT_SIZES)));
		fontSizeBox.setEditable(true);
		((JTextField) fontSizeBox.getEditor().getEditorComponent())
				.setColumns(3);
		fontSizeBox.setSelectedItem(fontSize);
		fontSizeBox.addItemListener(this);
		fontBoldBox = new JCheckBox("Bold");
		fontBoldBox.setSelected(DEFAULT_FONT_BOLD);
		fontBoldBox.addItemListener(this);
		nodeSizeBox = new JComboBox<>(new Vector<>(Ints.asList(NODE_SIZES)));
		nodeSizeBox.setEditable(true);
		((JTextField) nodeSizeBox.getEditor().getEditorComponent())
				.setColumns(3);
		nodeSizeBox.setSelectedItem(nodeSize);
		nodeSizeBox.addItemListener(this);
		arrowInMiddleBox = new JCheckBox("Activate");
		arrowInMiddleBox.setSelected(DEFAULT_ARROW_IN_MIDDLE);
		arrowInMiddleBox.addItemListener(this);
		label = new String();
		labelField = new JTextField(label, 20);
		labelButton = new JButton("Apply");
		labelButton.addActionListener(this);
		borderAlphaSlider = new JSlider(0, 255, borderAlpha);
		borderAlphaSlider.setPreferredSize(new Dimension(100, borderAlphaSlider
				.getPreferredSize().height));
		borderAlphaButton = new JButton("Apply");
		borderAlphaButton.addActionListener(this);
	}

	private static JPanel getOptionPanel(String name, JComponent... components) {
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

		return panel;
	}

	public static interface ChangeListener {

		public void editingModeChanged();

		public void showLegendChanged();

		public void joinEdgesChanged();

		public void skipEdgelessNodesChanged();

		public void fontChanged();

		public void nodeSizeChanged();

		public void arrowInMiddleChanged();

		public void labelChanged();

		public void borderAlphaChanged();
	}
}
