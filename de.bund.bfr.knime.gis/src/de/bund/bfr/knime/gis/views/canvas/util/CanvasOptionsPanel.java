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
package de.bund.bfr.knime.gis.views.canvas.util;

import java.awt.Component;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;

import com.google.common.primitives.Ints;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.Canvas;
import de.bund.bfr.knime.ui.Dialogs;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

public class CanvasOptionsPanel extends JScrollPane implements ActionListener, ItemListener {

	public static final Mode DEFAULT_MODE = Mode.TRANSFORMING;
	public static final boolean DEFAULT_SHOW_LEGEND = false;
	public static final boolean DEFAULT_JOIN_EDGES = false;
	public static final boolean DEFAULT_SKIP_EDGELESS_NODES = false;
	public static final boolean DEFAULT_SHOW_EDGES_IN_META_NODE = false;
	public static final int DEFAULT_FONT_SIZE = 12;
	public static final boolean DEFAULT_FONT_BOLD = false;
	public static final int DEFAULT_NODE_SIZE = 10;
	public static final Integer DEFAULT_NODE_MAX_SIZE = null;
	public static final int DEFAULT_EDGE_THICKNESS = 1;
	public static final Integer DEFAULT_EDGE_MAX_THICKNESS = null;
	public static final boolean DEFAULT_ARROW_IN_MIDDLE = false;
	public static final int DEFAULT_BORDER_ALPHA = 255;
	public static final boolean DEFAULT_AVOID_OVERLAY = false;

	private static final long serialVersionUID = 1L;

	private static final int[] TEXT_SIZES = { 10, 12, 14, 18, 24 };
	private static final int[] NODE_SIZES = { 4, 6, 10, 14, 20, 30 };
	private static final Integer[] NODE_MAX_SIZES = { null, 6, 10, 14, 20, 30, 40 };
	private static final int[] EDGE_THICKNESSES = { 1, 2, 3, 5, 10 };
	private static final Integer[] EDGE_MAX_THICKNESSES = { null, 2, 3, 5, 10, 20 };

	private JPanel panel;

	private JCheckBox transformingBox;
	private JCheckBox pickingBox;
	private JCheckBox showLegendBox;
	private JCheckBox joinEdgesBox;
	private JCheckBox skipEdgelessNodesBox;
	private JCheckBox showEdgesInMetaNodeBox;
	private int fontSize;
	private JComboBox<Integer> fontSizeBox;
	private JCheckBox fontBoldBox;
	private int nodeSize;
	private JComboBox<Integer> nodeSizeBox;
	private Integer nodeMaxSize;
	private JComboBox<Integer> nodeMaxSizeBox;
	private int edgeThickness;
	private JComboBox<Integer> edgeThicknessBox;
	private Integer edgeMaxThickness;
	private JComboBox<Integer> edgeMaxThicknessBox;
	private JCheckBox arrowInMiddleBox;
	private String label;
	private JTextField labelField;
	private JButton labelButton;
	private int borderAlpha;
	private JSlider borderAlphaSlider;
	private JButton borderAlphaButton;
	private JCheckBox avoidOverlayBox;

	private List<ChangeListener> listeners;

	public CanvasOptionsPanel(Canvas<?> owner, boolean allowEdges, boolean allowNodeResize, boolean allowPolygons,
			boolean allowGisLocations) {
		init();

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(getOptionPanel("Editing Mode", transformingBox, pickingBox));
		panel.add(Box.createHorizontalStrut(5));
		panel.add(getOptionPanel("Show Legend", showLegendBox));
		panel.add(Box.createHorizontalStrut(5));
		panel.add(getOptionPanel("Font", fontSizeBox, fontBoldBox));
		panel.add(getOptionPanel("Label", labelField, labelButton));

		if (allowEdges) {
			panel.add(Box.createHorizontalStrut(5));
			panel.add(getOptionPanel("Join " + owner.getNaming().Edges(), joinEdgesBox));
			panel.add(Box.createHorizontalStrut(5));
			panel.add(getOptionPanel("Arrow in Middle", arrowInMiddleBox));
		}

		if (allowEdges && allowNodeResize) {
			panel.add(Box.createHorizontalStrut(5));
			panel.add(getOptionPanel("Skip Unconnected " + owner.getNaming().Nodes(), skipEdgelessNodesBox));
			panel.add(Box.createHorizontalStrut(5));
			panel.add(getOptionPanel("Show " + owner.getNaming().Edges() + " in Meta " + owner.getNaming().Node(),
					showEdgesInMetaNodeBox));
		}

		if (allowNodeResize) {
			panel.add(Box.createHorizontalStrut(5));
			panel.add(getOptionPanel(owner.getNaming().Node() + " Size", new JLabel("Min:"), nodeSizeBox,
					Box.createHorizontalStrut(5), new JLabel("Max:"), nodeMaxSizeBox));
		}

		if (allowEdges) {
			panel.add(Box.createHorizontalStrut(5));
			panel.add(getOptionPanel(owner.getNaming().Edge() + " Thickness", new JLabel("Min:"), edgeThicknessBox,
					Box.createHorizontalStrut(5), new JLabel("Max:"), edgeMaxThicknessBox));
		}

		if (allowPolygons) {
			panel.add(Box.createHorizontalStrut(5));
			panel.add(getOptionPanel("Border Alpha", borderAlphaSlider, borderAlphaButton));
		}

		if (allowGisLocations) {
			panel.add(Box.createHorizontalStrut(5));
			panel.add(getOptionPanel("Avoid Overlay", avoidOverlayBox));
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
		return transformingBox.isSelected() ? Mode.TRANSFORMING : Mode.PICKING;
	}

	public void setEditingMode(Mode editingMode) {
		transformingBox.setSelected(editingMode == Mode.TRANSFORMING);
		pickingBox.setSelected(editingMode != Mode.TRANSFORMING);
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

	public boolean isShowEdgesInMetaNode() {
		return showEdgesInMetaNodeBox.isSelected();
	}

	public void setShowEdgesInMetaNode(boolean showEdgesInMetaNode) {
		showEdgesInMetaNodeBox.setSelected(showEdgesInMetaNode);
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

	public Integer getNodeMaxSize() {
		return nodeMaxSize;
	}

	public void setNodeMaxSize(Integer nodeMaxSize) {
		this.nodeMaxSize = nodeMaxSize;
		nodeMaxSizeBox.setSelectedItem(nodeMaxSize);
	}

	public int getEdgeThickness() {
		return edgeThickness;
	}

	public void setEdgeThickness(int edgeThickness) {
		this.edgeThickness = edgeThickness;
		edgeThicknessBox.setSelectedItem(edgeThickness);
	}

	public Integer getEdgeMaxThickness() {
		return edgeMaxThickness;
	}

	public void setEdgeMaxThickness(Integer edgeMaxThickness) {
		this.edgeMaxThickness = edgeMaxThickness;
		edgeMaxThicknessBox.setSelectedItem(edgeMaxThickness);
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

	public boolean isAvoidOverlay() {
		return avoidOverlayBox.isSelected();
	}

	public void setAvoidOverlay(boolean avoidOverlay) {
		avoidOverlayBox.setSelected(avoidOverlay);
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
		if (e.getSource() == transformingBox && e.getStateChange() == ItemEvent.SELECTED) {
			pickingBox.setSelected(false);

			for (ChangeListener l : listeners) {
				l.editingModeChanged();
			}
		} else if (e.getSource() == pickingBox && e.getStateChange() == ItemEvent.SELECTED) {
			transformingBox.setSelected(false);

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
		} else if (e.getSource() == showEdgesInMetaNodeBox) {
			for (ChangeListener l : listeners) {
				l.showEdgesInMetaNodeChanged();
			}
		} else if (e.getSource() == fontSizeBox && e.getStateChange() == ItemEvent.SELECTED) {
			Object size = fontSizeBox.getSelectedItem();

			if (size instanceof Integer) {
				fontSize = (Integer) size;

				for (ChangeListener l : listeners) {
					l.fontChanged();
				}
			} else {
				Dialogs.showErrorMessage(fontSizeBox, size + " is not a valid number", "Error");
				fontSizeBox.setSelectedItem(fontSize);
			}
		} else if (e.getSource() == fontBoldBox) {
			for (ChangeListener l : listeners) {
				l.fontChanged();
			}
		} else if (e.getSource() == nodeSizeBox && e.getStateChange() == ItemEvent.SELECTED) {
			Object size = nodeSizeBox.getSelectedItem();

			if (size instanceof Integer) {
				if (nodeMaxSize != null && (Integer) size > nodeMaxSize) {
					Dialogs.showErrorMessage(nodeSizeBox, "Value cannot be larger than max size " + nodeMaxSize,
							"Error");
					nodeSizeBox.setSelectedItem(nodeSize);
				} else {
					nodeSize = (Integer) size;

					for (ChangeListener l : listeners) {
						l.nodeSizeChanged();
					}
				}
			} else {
				Dialogs.showErrorMessage(nodeSizeBox, size + " is not a valid number", "Error");
				nodeSizeBox.setSelectedItem(nodeSize);
			}
		} else if (e.getSource() == nodeMaxSizeBox
				&& (e.getStateChange() == ItemEvent.SELECTED || nodeMaxSizeBox.getSelectedItem() == null)) {
			Object size = nodeMaxSizeBox.getSelectedItem();

			if (size instanceof Integer || size == null) {
				if (size != null && (Integer) size < nodeSize) {
					Dialogs.showErrorMessage(nodeMaxSizeBox, "Value cannot be smaller than min size " + nodeSize,
							"Error");
					nodeMaxSizeBox.setSelectedItem(nodeMaxSize);
				} else {
					nodeMaxSize = (Integer) size;

					for (ChangeListener l : listeners) {
						l.nodeSizeChanged();
					}
				}
			} else {
				Dialogs.showErrorMessage(nodeMaxSizeBox, size + " is not a valid number", "Error");
				nodeMaxSizeBox.setSelectedItem(nodeMaxSize);
			}
		} else if (e.getSource() == edgeThicknessBox && e.getStateChange() == ItemEvent.SELECTED) {
			Object size = edgeThicknessBox.getSelectedItem();

			if (size instanceof Integer) {
				if (edgeMaxThickness != null && (Integer) size > edgeMaxThickness) {
					Dialogs.showErrorMessage(edgeThicknessBox,
							"Value cannot be larger than max thickness " + edgeMaxThickness, "Error");
					edgeThicknessBox.setSelectedItem(edgeThickness);
				} else {
					edgeThickness = (Integer) size;

					for (ChangeListener l : listeners) {
						l.edgeThicknessChanged();
					}
				}
			} else {
				Dialogs.showErrorMessage(edgeThicknessBox, size + " is not a valid number", "Error");
				edgeThicknessBox.setSelectedItem(edgeThickness);
			}
		} else if (e.getSource() == edgeMaxThicknessBox
				&& (e.getStateChange() == ItemEvent.SELECTED || edgeMaxThicknessBox.getSelectedItem() == null)) {
			Object size = edgeMaxThicknessBox.getSelectedItem();

			if (size instanceof Integer || size == null) {
				if (size != null && (Integer) size < edgeThickness) {
					Dialogs.showErrorMessage(edgeMaxThicknessBox,
							"Value cannot be smaller than min thickness " + edgeThickness, "Error");
					edgeMaxThicknessBox.setSelectedItem(edgeMaxThickness);
				} else {
					edgeMaxThickness = (Integer) size;

					for (ChangeListener l : listeners) {
						l.edgeThicknessChanged();
					}
				}
			} else {
				Dialogs.showErrorMessage(edgeMaxThicknessBox, size + " is not a valid number", "Error");
				edgeMaxThicknessBox.setSelectedItem(edgeMaxThickness);
			}
		} else if (e.getSource() == arrowInMiddleBox) {
			for (ChangeListener l : listeners) {
				l.arrowInMiddleChanged();
			}
		} else if (e.getSource() == avoidOverlayBox) {
			for (ChangeListener l : listeners) {
				l.avoidOverlayChanged();
			}
		}
	}

	private void init() {
		listeners = new ArrayList<>();

		fontSize = DEFAULT_FONT_SIZE;
		nodeSize = DEFAULT_NODE_SIZE;
		nodeMaxSize = DEFAULT_NODE_MAX_SIZE;
		edgeThickness = DEFAULT_EDGE_THICKNESS;
		edgeMaxThickness = DEFAULT_EDGE_MAX_THICKNESS;
		borderAlpha = DEFAULT_BORDER_ALPHA;

		transformingBox = new JCheckBox("Transforming");
		transformingBox.setSelected(DEFAULT_MODE == Mode.TRANSFORMING);
		transformingBox.addItemListener(this);

		pickingBox = new JCheckBox("Picking");
		pickingBox.setSelected(DEFAULT_MODE != Mode.TRANSFORMING);
		pickingBox.addItemListener(this);

		showLegendBox = new JCheckBox("Activate");
		showLegendBox.setSelected(DEFAULT_SHOW_LEGEND);
		showLegendBox.addItemListener(this);

		joinEdgesBox = new JCheckBox("Activate");
		joinEdgesBox.setSelected(DEFAULT_JOIN_EDGES);
		joinEdgesBox.addItemListener(this);

		skipEdgelessNodesBox = new JCheckBox("Activate");
		skipEdgelessNodesBox.setSelected(DEFAULT_SKIP_EDGELESS_NODES);
		skipEdgelessNodesBox.addItemListener(this);

		showEdgesInMetaNodeBox = new JCheckBox("Activate");
		showEdgesInMetaNodeBox.setSelected(DEFAULT_SHOW_EDGES_IN_META_NODE);
		showEdgesInMetaNodeBox.addItemListener(this);

		fontSizeBox = new JComboBox<>(new Vector<>(Ints.asList(TEXT_SIZES)));
		fontSizeBox.setEditable(true);
		((JTextField) fontSizeBox.getEditor().getEditorComponent()).setColumns(3);
		fontSizeBox.setSelectedItem(fontSize);
		fontSizeBox.addItemListener(this);

		fontBoldBox = new JCheckBox("Bold");
		fontBoldBox.setSelected(DEFAULT_FONT_BOLD);
		fontBoldBox.addItemListener(this);

		nodeSizeBox = new JComboBox<>(new Vector<>(Ints.asList(NODE_SIZES)));
		nodeSizeBox.setEditable(true);
		((JTextField) nodeSizeBox.getEditor().getEditorComponent()).setColumns(3);
		nodeSizeBox.setSelectedItem(nodeSize);
		nodeSizeBox.addItemListener(this);

		nodeMaxSizeBox = new JComboBox<>(NODE_MAX_SIZES);
		nodeMaxSizeBox.setEditable(true);
		((JTextField) nodeMaxSizeBox.getEditor().getEditorComponent()).setColumns(3);
		nodeMaxSizeBox.setSelectedItem(nodeMaxSize);
		nodeMaxSizeBox.addItemListener(this);

		edgeThicknessBox = new JComboBox<>(new Vector<>(Ints.asList(EDGE_THICKNESSES)));
		edgeThicknessBox.setEditable(true);
		((JTextField) edgeThicknessBox.getEditor().getEditorComponent()).setColumns(3);
		edgeThicknessBox.setSelectedItem(edgeThickness);
		edgeThicknessBox.addItemListener(this);

		edgeMaxThicknessBox = new JComboBox<>(EDGE_MAX_THICKNESSES);
		edgeMaxThicknessBox.setEditable(true);
		((JTextField) edgeMaxThicknessBox.getEditor().getEditorComponent()).setColumns(3);
		edgeMaxThicknessBox.setSelectedItem(edgeMaxThickness);
		edgeMaxThicknessBox.addItemListener(this);

		arrowInMiddleBox = new JCheckBox("Activate");
		arrowInMiddleBox.setSelected(DEFAULT_ARROW_IN_MIDDLE);
		arrowInMiddleBox.addItemListener(this);

		label = null;
		labelField = new JTextField(label, 20);
		labelButton = new JButton("Apply");
		labelButton.addActionListener(this);

		borderAlphaSlider = new JSlider(0, 255, borderAlpha);
		borderAlphaSlider.setPreferredSize(new Dimension(100, borderAlphaSlider.getPreferredSize().height));
		borderAlphaButton = new JButton("Apply");
		borderAlphaButton.addActionListener(this);

		avoidOverlayBox = new JCheckBox("Activate");
		avoidOverlayBox.setSelected(DEFAULT_AVOID_OVERLAY);
		avoidOverlayBox.addItemListener(this);
	}

	private static JPanel getOptionPanel(String name, Component... components) {
		JPanel panel = new JPanel();
		TitledBorder border = BorderFactory.createTitledBorder(name);

		panel.setBorder(border);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		for (Component c : components) {
			panel.add(UI.createCenterPanel(c));
		}

		if (components.length == 1) {
			int titleWidth = border.getMinimumSize(components[0]).width;

			components[0].setPreferredSize(new Dimension(Math.max(components[0].getPreferredSize().width, titleWidth),
					components[0].getPreferredSize().height));
		}

		return panel;
	}

	public static interface ChangeListener {

		void editingModeChanged();

		void showLegendChanged();

		void joinEdgesChanged();

		void skipEdgelessNodesChanged();

		void showEdgesInMetaNodeChanged();

		void fontChanged();

		void nodeSizeChanged();

		void edgeThicknessChanged();

		void arrowInMiddleChanged();

		void labelChanged();

		void borderAlphaChanged();

		void avoidOverlayChanged();
	}
}
