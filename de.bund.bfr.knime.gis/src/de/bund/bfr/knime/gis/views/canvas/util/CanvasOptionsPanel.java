/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.EventListener;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;

import com.google.common.base.Strings;
import com.google.common.primitives.Ints;

import de.bund.bfr.jung.LabelPosition;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.Canvas;
import de.bund.bfr.knime.ui.Dialogs;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

public class CanvasOptionsPanel extends JScrollPane {

	public static final Mode DEFAULT_MODE = Mode.TRANSFORMING;
	public static final boolean DEFAULT_SHOW_LEGEND = false;
	public static final boolean DEFAULT_JOIN_EDGES = false;
	public static final boolean DEFAULT_SKIP_EDGELESS_NODES = false;
	public static final boolean DEFAULT_SHOW_EDGES_IN_META_NODE = false;
	public static final int DEFAULT_FONT_SIZE = 12;
	public static final boolean DEFAULT_FONT_BOLD = false;
	public static final int DEFAULT_NODE_SIZE = 10;
	public static final String DEFAULT_NODE_MAX_SIZE = "";
	public static final int DEFAULT_EDGE_THICKNESS = 1;
	public static final String DEFAULT_EDGE_MAX_THICKNESS = "";
	public static final ArrowHeadType DEFAULT_ARROW_HEAD_TYPE = ArrowHeadType.AT_TARGET;
	public static final LabelPosition DEFAULT_NODE_LABEL_POSITION = LabelPosition.BOTTOM_RIGHT;
	public static final int DEFAULT_BORDER_ALPHA = 255;
	public static final boolean DEFAULT_AVOID_OVERLAY = false;

	private static final long serialVersionUID = 1L;

	private static final int[] TEXT_SIZES = { 10, 12, 14, 18, 24 };
	private static final int[] NODE_SIZES = { 4, 6, 10, 14, 20, 30 };
	private static final String[] NODE_MAX_SIZES = { "", "6", "10", "14", "20", "30", "40" };
	private static final int[] EDGE_THICKNESSES = { 1, 2, 3, 5, 10 };
	private static final String[] EDGE_MAX_THICKNESSES = { "", "2", "3", "5", "10", "20" };

	private static final String SHOW_ADVANCED = "<html>Show Advanced<br>Options</html>";
	private static final String HIDE_ADVANCED = "<html>Hide Advanced<br>Options</html>";

	private JPanel panel;
	private JPanel standardPanel;
	private JPanel advancedPanel;
	private JButton advancedButton;

	private JCheckBox showLegendBox;
	private JCheckBox joinEdgesBox;
	private JCheckBox skipEdgelessNodesBox;
	private JCheckBox showEdgesInMetaNodeBox;
	private int fontSize;
	private JComboBox<Integer> fontSizeBox;
	private JCheckBox fontBoldBox;
	private int nodeSize;
	private JComboBox<Integer> nodeSizeBox;
	private String nodeMaxSize;
	private JComboBox<String> nodeMaxSizeBox;
	private int edgeThickness;
	private JComboBox<Integer> edgeThicknessBox;
	private String edgeMaxThickness;
	private JComboBox<String> edgeMaxThicknessBox;
	private JComboBox<ArrowHeadType> arrowHeadBox;
	private JComboBox<LabelPosition> nodeLabelPositionBox;
	private String label;
	private JTextField labelField;
	private JButton labelButton;
	private int borderAlpha;
	private JSlider borderAlphaSlider;
	private JButton borderAlphaButton;
	private JCheckBox avoidOverlayBox;

	public CanvasOptionsPanel(Canvas<?> owner, boolean allowEdges, boolean allowNodeResize, boolean allowPolygons,
			boolean allowGisLocations) {
		init();

		standardPanel = new JPanel();
		standardPanel.setLayout(new BoxLayout(standardPanel, BoxLayout.X_AXIS));
		advancedPanel = new JPanel();
		advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.X_AXIS));
		advancedButton = new JButton(SHOW_ADVANCED);
		advancedButton.addActionListener(e -> advancedButtonClicked());

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(standardPanel);
		panel.add(Box.createHorizontalStrut(5));
		panel.add(advancedButton);

		Naming naming = owner.getNaming();

//		addOption("Editing Mode",
//				"Current editing mode.\n" + "In \"Transforming\" mode the user can move the graph by using the mouse.\n"
//						+ "In \"Picking\" mode the user can select and unselect " + naming.nodes() + " and "
//						+ naming.edges() + ".",
//				false, transformingButton, pickingButton);
		addOption("Show Legend",
				"If checked, a legend with all highlight conditions, which have \"Show in Legend\" activated,\n"
						+ "is displayed in the lower left corner of the canvas.",
				false, showLegendBox);
		addOption("Label", "Small Label, that is displayed in the upper right corner of the graph image.", true,
				labelField, labelButton);

		if (allowEdges) {
			addOption("Join " + naming.Edges(),
					"If checked, " + naming.edges() + " with the same source and target are joined.", false,
					joinEdgesBox);
			addOption("Draw Arrow Head", "If and how arrows are drawn can be specified here.", false, arrowHeadBox);
		}

		if (allowEdges && allowNodeResize) {
			addOption("Skip Unconnected " + naming.Nodes(),
					"If checked, " + naming.nodes() + " without any connected" + naming.edges() + "are not shown.",
					false, skipEdgelessNodesBox);
			addOption("Show " + naming.Edges() + " in Meta " + naming.Node(),
					"If checked, " + naming.edges() + " from a meta " + naming.node() + " to itself are shown.", true,
					showEdgesInMetaNodeBox);
		}

		addOption(
				"Font", "Font Size and type (plain or bold) can be specified here.\n" + "This font is used for "
						+ naming.node() + "/" + naming.edge() + " labels and in the legend.",
				false, fontSizeBox, fontBoldBox);

		if (allowNodeResize) {
			addOption(naming.Node() + " Size",
					"Min/Max diameter of the " + naming.nodes()
							+ ".\nMin value is used, when no \"Value Condition\" or \"Logical Value Condition\" is defined for "
							+ naming.nodes() + ".\nOtherwise the " + naming.node() + " sizes are between min and max.",
					false, new JLabel("Min:"), nodeSizeBox, Box.createHorizontalStrut(5), new JLabel("Max:"),
					nodeMaxSizeBox);
			addOption(naming.Node() + " Label", "Position of the " + naming.nodes() + " labels.", false,
					nodeLabelPositionBox);
		}

		if (allowEdges) {
			addOption(naming.Edge() + " Thickness",
					"Min/Max thickness of the " + naming.edges()
							+ ".\nMin value is used, when no \"Value Condition\" or \"Logical Value Condition\" is defined for "
							+ naming.edges() + ".\nOtherwise the delivery thicknesses are between min and max.",
					false, new JLabel("Min:"), edgeThicknessBox, Box.createHorizontalStrut(5), new JLabel("Max:"),
					edgeMaxThicknessBox);
		}

		if (allowPolygons) {
			addOption("Border Alpha", "Alpha value used to draw the geographical borders.", true, borderAlphaSlider,
					borderAlphaButton);
		}

		if (allowGisLocations) {
			addOption("Avoid Overlay",
					"If checked, geographical " + naming.nodes()
							+ " that occlude each other are moved so that the user can see each " + naming.node() + ".",
					false, avoidOverlayBox);
		}

		setViewportView(UI.createWestPanel(UI.createNorthPanel(panel)));
		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				updateScrollbars();
			}
		});
	}

	public void addChangeListener(ChangeListener listener) {
		listenerList.add(ChangeListener.class, listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		listenerList.remove(ChangeListener.class, listener);
	}

	public void addOption(String name, String tooltip, boolean advanced, Component... components) {
		JPanel optionsPanel = getOptionPanel(name, components);

		UI.setTooltip(optionsPanel, tooltip);

		JPanel p = advanced ? advancedPanel : standardPanel;

		if (p.getComponentCount() > 0) {
			p.add(Box.createHorizontalStrut(5));
		}

		p.add(optionsPanel);
	}

//	public Mode getEditingMode() {
//		return transformingButton.isSelected() ? Mode.TRANSFORMING : Mode.PICKING;
//	}

//	public void setEditingMode(Mode editingMode) {
//		if (editingMode == Mode.TRANSFORMING) {
//			transformingButton.setSelected(true);
//		} else if (editingMode == Mode.PICKING) {
//			pickingButton.setSelected(true);
//		}
//	}

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
		nodeSizeBox.setSelectedItem(nodeSize);
	}

	public Integer getNodeMaxSize() {
		try {
			return Integer.parseInt(nodeMaxSize);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public void setNodeMaxSize(Integer nodeMaxSize) {
		nodeMaxSizeBox.setSelectedItem(nodeMaxSize != null ? nodeMaxSize.toString() : "");
	}

	public int getEdgeThickness() {
		return edgeThickness;
	}

	public void setEdgeThickness(int edgeThickness) {
		edgeThicknessBox.setSelectedItem(edgeThickness);
	}

	public Integer getEdgeMaxThickness() {
		try {
			return Integer.parseInt(edgeMaxThickness);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public void setEdgeMaxThickness(Integer edgeMaxThickness) {
		edgeMaxThicknessBox.setSelectedItem(edgeMaxThickness != null ? edgeMaxThickness.toString() : "");
	}

	public ArrowHeadType getArrowHeadType() {
		return (ArrowHeadType) arrowHeadBox.getSelectedItem();
	}

	public void setArrowHeadType(ArrowHeadType arrowHeadType) {
		arrowHeadBox.setSelectedItem(arrowHeadType);
	}

	public LabelPosition getNodeLabelPosition() {
		return (LabelPosition) nodeLabelPositionBox.getSelectedItem();
	}

	public void setNodeLabelPosition(LabelPosition nodeLabelPosition) {
		nodeLabelPositionBox.setSelectedItem(nodeLabelPosition);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		labelField.setText(Strings.nullToEmpty(label));
		labelButton.doClick();
	}

	public int getBorderAlpha() {
		return borderAlpha;
	}

	public void setBorderAlpha(int borderAlpha) {
		borderAlphaSlider.setValue(borderAlpha);
		borderAlphaButton.doClick();
	}

	public boolean isAvoidOverlay() {
		return avoidOverlayBox.isSelected();
	}

	public void setAvoidOverlay(boolean avoidOverlay) {
		avoidOverlayBox.setSelected(avoidOverlay);
	}

	private void init() {
		fontSize = DEFAULT_FONT_SIZE;
		nodeSize = DEFAULT_NODE_SIZE;
		nodeMaxSize = DEFAULT_NODE_MAX_SIZE;
		edgeThickness = DEFAULT_EDGE_THICKNESS;
		edgeMaxThickness = DEFAULT_EDGE_MAX_THICKNESS;
		borderAlpha = DEFAULT_BORDER_ALPHA;

		showLegendBox = new JCheckBox("Activate");
		showLegendBox.setSelected(DEFAULT_SHOW_LEGEND);
		showLegendBox.addItemListener(e -> call(ChangeListener::showLegendChanged));

		joinEdgesBox = new JCheckBox("Activate");
		joinEdgesBox.setSelected(DEFAULT_JOIN_EDGES);
		joinEdgesBox.addItemListener(e -> call(ChangeListener::joinEdgesChanged));

		skipEdgelessNodesBox = new JCheckBox("Activate");
		skipEdgelessNodesBox.setSelected(DEFAULT_SKIP_EDGELESS_NODES);
		skipEdgelessNodesBox.addItemListener(e -> call(ChangeListener::skipEdgelessNodesChanged));

		showEdgesInMetaNodeBox = new JCheckBox("Activate");
		showEdgesInMetaNodeBox.setSelected(DEFAULT_SHOW_EDGES_IN_META_NODE);
		showEdgesInMetaNodeBox.addItemListener(e -> call(ChangeListener::showEdgesInMetaNodeChanged));

		fontSizeBox = new JComboBox<>(new Vector<>(Ints.asList(TEXT_SIZES)));
		fontSizeBox.setEditable(true);
		((JTextField) fontSizeBox.getEditor().getEditorComponent()).setColumns(3);
		fontSizeBox.setSelectedItem(fontSize);
		fontSizeBox.addItemListener(UI.newItemSelectListener(e -> fontSizeChanged()));

		fontBoldBox = new JCheckBox("Bold");
		fontBoldBox.setSelected(DEFAULT_FONT_BOLD);
		fontBoldBox.addItemListener(e -> call(ChangeListener::fontChanged));

		nodeSizeBox = new JComboBox<>(new Vector<>(Ints.asList(NODE_SIZES)));
		nodeSizeBox.setEditable(true);
		((JTextField) nodeSizeBox.getEditor().getEditorComponent()).setColumns(3);
		nodeSizeBox.setSelectedItem(nodeSize);
		nodeSizeBox.addItemListener(UI.newItemSelectListener(e -> nodeSizeChanged()));

		nodeMaxSizeBox = new JComboBox<>(NODE_MAX_SIZES);
		nodeMaxSizeBox.setEditable(true);
		((JTextField) nodeMaxSizeBox.getEditor().getEditorComponent()).setColumns(3);
		nodeMaxSizeBox.setSelectedItem(nodeMaxSize);
		nodeMaxSizeBox.addItemListener(UI.newItemSelectListener(e -> nodeMaxSizeChanged()));

		edgeThicknessBox = new JComboBox<>(new Vector<>(Ints.asList(EDGE_THICKNESSES)));
		edgeThicknessBox.setEditable(true);
		((JTextField) edgeThicknessBox.getEditor().getEditorComponent()).setColumns(3);
		edgeThicknessBox.setSelectedItem(edgeThickness);
		edgeThicknessBox.addItemListener(UI.newItemSelectListener(e -> edgeThicknessChanged()));

		edgeMaxThicknessBox = new JComboBox<>(EDGE_MAX_THICKNESSES);
		edgeMaxThicknessBox.setEditable(true);
		((JTextField) edgeMaxThicknessBox.getEditor().getEditorComponent()).setColumns(3);
		edgeMaxThicknessBox.setSelectedItem(edgeMaxThickness);
		edgeMaxThicknessBox.addItemListener(UI.newItemSelectListener(e -> edgeMaxThicknessChanged()));

		arrowHeadBox = new JComboBox<>(ArrowHeadType.values());
		arrowHeadBox.setSelectedItem(DEFAULT_ARROW_HEAD_TYPE);
		arrowHeadBox.addItemListener(UI.newItemSelectListener(e -> call(ChangeListener::arrowHeadTypeChanged)));

		nodeLabelPositionBox = new JComboBox<>(LabelPosition.values());
		nodeLabelPositionBox.setSelectedItem(DEFAULT_NODE_LABEL_POSITION);
		nodeLabelPositionBox
				.addItemListener(UI.newItemSelectListener(e -> call(ChangeListener::nodeLabelPositionChanged)));

		label = null;
		labelField = new JTextField(label, 20);
		labelButton = new JButton("Apply");
		labelButton.addActionListener(e -> {
			label = Strings.emptyToNull(labelField.getText());
			call(ChangeListener::labelChanged);
		});

		borderAlphaSlider = new JSlider(0, 255, borderAlpha);
		borderAlphaSlider.setPreferredSize(new Dimension(100, borderAlphaSlider.getPreferredSize().height));
		borderAlphaButton = new JButton("Apply");
		borderAlphaButton.addActionListener(e -> {
			borderAlpha = borderAlphaSlider.getValue();
			call(ChangeListener::borderAlphaChanged);
		});

		avoidOverlayBox = new JCheckBox("Activate");
		avoidOverlayBox.setSelected(DEFAULT_AVOID_OVERLAY);
		avoidOverlayBox.addItemListener(e -> call(ChangeListener::avoidOverlayChanged));
	}

	private void updateScrollbars() {
		if (getSize().width < getPreferredSize().width) {
			if (getHorizontalScrollBarPolicy() != ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS) {
				setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
				getParent().revalidate();
			}
		} else {
			if (getHorizontalScrollBarPolicy() != ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {
				setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				getParent().revalidate();
			}
		}
	}

	private void advancedButtonClicked() {
		if (advancedButton.getText().equals(SHOW_ADVANCED)) {
			advancedButton.setText(HIDE_ADVANCED);
			panel.add(Box.createHorizontalStrut(5));
			panel.add(advancedPanel);
			panel.revalidate();
		} else if (advancedButton.getText().equals(HIDE_ADVANCED)) {
			advancedButton.setText(SHOW_ADVANCED);
			panel.remove(panel.getComponentCount() - 1);
			panel.remove(panel.getComponentCount() - 1);
			panel.revalidate();
		}

		updateScrollbars();
	}

	private void fontSizeChanged() {
		String sizeString = fontSizeBox.getSelectedItem().toString();

		try {
			int size = Integer.parseInt(sizeString);

			if (size < 1) {
				Dialogs.showErrorMessage(fontSizeBox, "Value cannot be smaller than 1");
				fontSizeBox.setSelectedItem(fontSize);
			} else {
				fontSize = size;
				Stream.of(getListeners(ChangeListener.class)).forEach(l -> l.fontChanged());
			}
		} catch (NumberFormatException e) {
			Dialogs.showErrorMessage(fontSizeBox, sizeString + " is not a valid number");
			fontSizeBox.setSelectedItem(fontSize);
		} catch (NullPointerException e) {
			Dialogs.showErrorMessage(fontSizeBox, "No value specified");
			fontSizeBox.setSelectedItem(fontSize);
		}
	}

	private void nodeSizeChanged() {
		String sizeString = nodeSizeBox.getSelectedItem().toString().trim();

		if (sizeString.isEmpty()) {
			Dialogs.showErrorMessage(nodeSizeBox, "No value specified");
			nodeSizeBox.setSelectedItem(nodeSize);
		} else {
			try {
				int size = Integer.parseInt(sizeString);
				Integer max = getNodeMaxSize();

				if (size < 1) {
					Dialogs.showErrorMessage(nodeSizeBox, "Value cannot be smaller than 1");
					nodeSizeBox.setSelectedItem(nodeSize);
				} else if (max != null && size > max) {
					Dialogs.showErrorMessage(nodeSizeBox, "Value cannot be larger than max size " + nodeMaxSize);
					nodeSizeBox.setSelectedItem(nodeSize);
				} else {
					nodeSize = size;
					call(ChangeListener::nodeSizeChanged);
				}
			} catch (NumberFormatException e) {
				Dialogs.showErrorMessage(nodeSizeBox, sizeString + " is not a valid number");
				nodeSizeBox.setSelectedItem(nodeSize);
			}
		}
	}

	private void nodeMaxSizeChanged() {
		String sizeString = nodeMaxSizeBox.getSelectedItem().toString().trim();

		if (sizeString.isEmpty()) {
			nodeMaxSize = "";
			call(ChangeListener::nodeSizeChanged);
		} else {
			try {
				int size = Integer.parseInt(sizeString);

				if (size < nodeSize) {
					Dialogs.showErrorMessage(nodeMaxSizeBox, "Value cannot be smaller than min size " + nodeSize);
					nodeMaxSizeBox.setSelectedItem(nodeMaxSize);
				} else {
					nodeMaxSize = String.valueOf(size);
					call(ChangeListener::nodeSizeChanged);
				}
			} catch (NumberFormatException e) {
				Dialogs.showErrorMessage(nodeMaxSizeBox, sizeString + " is not a valid number");
				nodeMaxSizeBox.setSelectedItem(nodeMaxSize);
			}
		}
	}

	private void edgeThicknessChanged() {
		String sizeString = edgeThicknessBox.getSelectedItem().toString().trim();

		if (sizeString.isEmpty()) {
			Dialogs.showErrorMessage(edgeThicknessBox, "No value specified");
			edgeThicknessBox.setSelectedItem(edgeThickness);
		} else {
			try {
				int size = Integer.parseInt(sizeString);
				Integer max = getEdgeMaxThickness();

				if (size < 1) {
					Dialogs.showErrorMessage(edgeThicknessBox, "Value cannot be smaller than 1");
					edgeThicknessBox.setSelectedItem(edgeThickness);
				} else if (max != null && size > max) {
					Dialogs.showErrorMessage(edgeThicknessBox,
							"Value cannot be larger than max size " + edgeMaxThickness);
					edgeThicknessBox.setSelectedItem(edgeThickness);
				} else {
					edgeThickness = size;
					call(ChangeListener::edgeThicknessChanged);
				}
			} catch (NumberFormatException e) {
				Dialogs.showErrorMessage(edgeThicknessBox, sizeString + " is not a valid number");
				edgeThicknessBox.setSelectedItem(edgeThickness);
			}
		}
	}

	private void edgeMaxThicknessChanged() {
		String sizeString = edgeMaxThicknessBox.getSelectedItem().toString().trim();

		if (sizeString.isEmpty()) {
			edgeMaxThickness = "";
			call(ChangeListener::edgeThicknessChanged);
		} else {
			try {
				int size = Integer.parseInt(sizeString);

				if (size < edgeThickness) {
					Dialogs.showErrorMessage(edgeMaxThicknessBox,
							"Value cannot be smaller than min size " + edgeThickness);
					edgeMaxThicknessBox.setSelectedItem(edgeMaxThickness);
				} else {
					edgeMaxThickness = String.valueOf(size);
					call(ChangeListener::edgeThicknessChanged);
				}
			} catch (NumberFormatException e) {
				Dialogs.showErrorMessage(edgeMaxThicknessBox, sizeString + " is not a valid number");
				edgeMaxThicknessBox.setSelectedItem(edgeMaxThickness);
			}
		}
	}

	private void call(Consumer<ChangeListener> action) {
		Stream.of(getListeners(ChangeListener.class)).forEach(action);
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

	public static interface ChangeListener extends EventListener {

		void editingModeChanged();

		void showLegendChanged();

		void joinEdgesChanged();

		void skipEdgelessNodesChanged();

		void showEdgesInMetaNodeChanged();

		void fontChanged();

		void nodeSizeChanged();

		void edgeThicknessChanged();

		void arrowHeadTypeChanged();

		void nodeLabelPositionChanged();

		void labelChanged();

		void borderAlphaChanged();

		void avoidOverlayChanged();
	}
}
