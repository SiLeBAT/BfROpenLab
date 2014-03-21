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
package de.bund.bfr.knime.gis.views.regiontoregionvisualizer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;

import de.bund.bfr.knime.ColumnComboBox;
import de.bund.bfr.knime.KnimeUtilities;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.GisUtilities;

public class RegionToRegionVisualizerInputDialog extends JDialog implements
		ActionListener {

	private static final long serialVersionUID = 1L;

	private ColumnComboBox shapeBox;
	private ColumnComboBox shapeRegionBox;
	private ColumnComboBox nodeIdBox;
	private ColumnComboBox nodeRegionBox;
	private JCheckBox skipEdgelessNodesBox;
	private ColumnComboBox edgeFromBox;
	private ColumnComboBox edgeToBox;
	private JCheckBox exportAsSvgBox;
	private JButton okButton;
	private JButton cancelButton;

	private boolean approved;
	private RegionToRegionVisualizerSettings set;

	@SuppressWarnings("unchecked")
	public RegionToRegionVisualizerInputDialog(JComponent owner,
			DataTableSpec shapeSpec, DataTableSpec nodeSpec,
			DataTableSpec edgeSpec, RegionToRegionVisualizerSettings set) {
		super(SwingUtilities.getWindowAncestor(owner), "Input",
				DEFAULT_MODALITY_TYPE);
		this.set = set;
		approved = false;

		shapeBox = new ColumnComboBox(false,
				GisUtilities.getShapeColumns(shapeSpec));
		shapeBox.setSelectedColumnName(set.getShapeColumn());
		shapeRegionBox = new ColumnComboBox(false,
				KnimeUtilities.getStringIntColumns(shapeSpec));
		shapeRegionBox.setSelectedColumnName(set.getShapeRegionColumn());
		nodeIdBox = new ColumnComboBox(false,
				KnimeUtilities.getStringIntColumns(nodeSpec));
		nodeIdBox.setSelectedColumnName(set.getNodeIdColumn());
		nodeRegionBox = new ColumnComboBox(false,
				KnimeUtilities.getStringIntColumns(nodeSpec));
		nodeRegionBox.setSelectedColumnName(set.getNodeRegionColumn());
		skipEdgelessNodesBox = new JCheckBox("Skip Nodes without Edges");
		skipEdgelessNodesBox.setSelected(set.isSkipEdgelessNodes());
		edgeFromBox = new ColumnComboBox(false,
				KnimeUtilities.getStringIntColumns(edgeSpec));
		edgeFromBox.setSelectedColumnName(set.getEdgeFromColumn());
		edgeToBox = new ColumnComboBox(false,
				KnimeUtilities.getStringIntColumns(edgeSpec));
		edgeToBox.setSelectedColumnName(set.getEdgeToColumn());
		exportAsSvgBox = new JCheckBox("Export As Svg");
		exportAsSvgBox.setSelected(set.isExportAsSvg());
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(UI.createOptionsPanel("Shape Table", Arrays.asList(
				new JLabel("Shape Column:"), new JLabel("Region ID Column:")),
				Arrays.asList(shapeBox, shapeRegionBox)));
		mainPanel.add(UI.createOptionsPanel("Node Table", Arrays.asList(
				new JLabel("Node ID column:"), new JLabel("Region ID column:"),
				new JLabel()), Arrays.asList(nodeIdBox, nodeRegionBox,
				skipEdgelessNodesBox)));
		mainPanel.add(UI.createOptionsPanel("Edge Table", Arrays.asList(
				new JLabel("Source Node ID Column:"), new JLabel(
						"Target Node ID Column:")), Arrays.asList(edgeFromBox,
				edgeToBox)));
		mainPanel.add(UI.createOptionsPanel("Miscellaneous",
				Arrays.asList(exportAsSvgBox), Arrays.asList(new JLabel())));

		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);
		add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)),
				BorderLayout.SOUTH);
		setLocationRelativeTo(owner);
		pack();
	}

	public boolean isApproved() {
		return approved;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			DataColumnSpec shapeColumn = shapeBox.getSelectedColumn();
			DataColumnSpec shapeRegionColumn = shapeRegionBox
					.getSelectedColumn();
			DataColumnSpec nodeIdColumn = nodeIdBox.getSelectedColumn();
			DataColumnSpec nodeRegionColumn = nodeRegionBox.getSelectedColumn();
			DataColumnSpec edgeFromColumn = edgeFromBox.getSelectedColumn();
			DataColumnSpec edgeToColumn = edgeToBox.getSelectedColumn();

			if (shapeColumn == null || shapeRegionColumn == null
					|| nodeIdColumn == null || nodeRegionColumn == null
					|| edgeFromColumn == null || edgeToColumn == null) {
				String error = "\"Shape\", all \"Region ID\" and all \"Node ID\""
						+ " columns must be selected";

				JOptionPane.showMessageDialog(this, error, "Error",
						JOptionPane.ERROR_MESSAGE);
			} else if (shapeRegionColumn.getType() != nodeRegionColumn
					.getType()) {
				String error = "All \"Region ID\" columns must have the same type";

				JOptionPane.showMessageDialog(this, error, "Type Error",
						JOptionPane.ERROR_MESSAGE);
			} else if (nodeIdColumn.getType() != edgeFromColumn.getType()
					|| nodeIdColumn.getType() != edgeToColumn.getType()) {
				String error = "All \"Node ID\" columns must have the same type";

				JOptionPane.showMessageDialog(this, error, "Type Error",
						JOptionPane.ERROR_MESSAGE);
			} else {
				approved = true;
				set.setShapeColumn(shapeColumn.getName());
				set.setShapeRegionColumn(shapeRegionColumn.getName());
				set.setNodeIdColumn(nodeIdColumn.getName());
				set.setNodeRegionColumn(nodeRegionColumn.getName());
				set.setSkipEdgelessNodes(skipEdgelessNodesBox.isSelected());
				set.setEdgeFromColumn(edgeFromColumn.getName());
				set.setEdgeToColumn(edgeToColumn.getName());
				set.setExportAsSvg(exportAsSvgBox.isSelected());
				dispose();
			}
		} else if (e.getSource() == cancelButton) {
			dispose();
		}
	}

}
