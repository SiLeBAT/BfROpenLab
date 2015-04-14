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
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.ui.ColumnComboBox;

public class RegionToRegionVisualizerInputDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	private ColumnComboBox shapeBox;
	private ColumnComboBox shapeRegionBox;
	private ColumnComboBox nodeIdBox;
	private ColumnComboBox nodeRegionBox;
	private ColumnComboBox edgeFromBox;
	private ColumnComboBox edgeToBox;
	private JCheckBox exportAsSvgBox;
	private JButton okButton;
	private JButton cancelButton;

	private boolean approved;
	private RegionToRegionVisualizerSettings set;

	public RegionToRegionVisualizerInputDialog(JComponent owner, DataTableSpec shapeSpec,
			DataTableSpec nodeSpec, DataTableSpec edgeSpec, RegionToRegionVisualizerSettings set) {
		super(SwingUtilities.getWindowAncestor(owner), "Input", DEFAULT_MODALITY_TYPE);
		this.set = set;
		approved = false;

		shapeBox = new ColumnComboBox(false, GisUtils.getShapeColumns(shapeSpec));
		shapeBox.setSelectedColumnName(set.getGisSettings().getShapeColumn());
		shapeRegionBox = new ColumnComboBox(false, KnimeUtils.getColumns(shapeSpec,
				StringCell.TYPE, IntCell.TYPE));
		shapeRegionBox.setSelectedColumnName(set.getGisSettings().getShapeRegionColumn());
		nodeIdBox = new ColumnComboBox(false, KnimeUtils.getColumns(nodeSpec, StringCell.TYPE,
				IntCell.TYPE));
		nodeIdBox.setSelectedColumnName(set.getGraphSettings().getNodeIdColumn());
		nodeRegionBox = new ColumnComboBox(false, KnimeUtils.getColumns(nodeSpec, StringCell.TYPE,
				IntCell.TYPE));
		nodeRegionBox.setSelectedColumnName(set.getGisSettings().getNodeRegionColumn());
		edgeFromBox = new ColumnComboBox(false, KnimeUtils.getColumns(edgeSpec, StringCell.TYPE,
				IntCell.TYPE));
		edgeFromBox.setSelectedColumnName(set.getGraphSettings().getEdgeFromColumn());
		edgeToBox = new ColumnComboBox(false, KnimeUtils.getColumns(edgeSpec, StringCell.TYPE,
				IntCell.TYPE));
		edgeToBox.setSelectedColumnName(set.getGraphSettings().getEdgeToColumn());
		exportAsSvgBox = new JCheckBox("Export As Svg");
		exportAsSvgBox.setSelected(set.isExportAsSvg());
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(UI.createOptionsPanel("Shape Table",
				Arrays.asList(new JLabel("Shape Column:"), new JLabel("Region ID Column:")),
				Arrays.asList(shapeBox, shapeRegionBox)));
		mainPanel.add(UI.createOptionsPanel("Node Table",
				Arrays.asList(new JLabel("Node ID column:"), new JLabel("Region ID column:")),
				Arrays.asList(nodeIdBox, nodeRegionBox)));
		mainPanel.add(UI.createOptionsPanel("Edge Table", Arrays.asList(new JLabel(
				"Source Node ID Column:"), new JLabel("Target Node ID Column:")), Arrays.asList(
				edgeFromBox, edgeToBox)));
		mainPanel.add(UI.createOptionsPanel("Miscellaneous", Arrays.asList(exportAsSvgBox),
				Arrays.asList(new JLabel())));

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
			DataColumnSpec shapeRegionColumn = shapeRegionBox.getSelectedColumn();
			DataColumnSpec nodeIdColumn = nodeIdBox.getSelectedColumn();
			DataColumnSpec nodeRegionColumn = nodeRegionBox.getSelectedColumn();
			DataColumnSpec edgeFromColumn = edgeFromBox.getSelectedColumn();
			DataColumnSpec edgeToColumn = edgeToBox.getSelectedColumn();

			if (shapeColumn == null || shapeRegionColumn == null || nodeIdColumn == null
					|| nodeRegionColumn == null || edgeFromColumn == null || edgeToColumn == null) {
				String error = "\"Shape\", all \"Region ID\" and all \"Node ID\""
						+ " columns must be selected";

				JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
			} else if (shapeRegionColumn.getType() != nodeRegionColumn.getType()) {
				String error = "All \"Region ID\" columns must have the same type";

				JOptionPane.showMessageDialog(this, error, "Type Error", JOptionPane.ERROR_MESSAGE);
			} else if (nodeIdColumn.getType() != edgeFromColumn.getType()
					|| nodeIdColumn.getType() != edgeToColumn.getType()) {
				String error = "All \"Node ID\" columns must have the same type";

				JOptionPane.showMessageDialog(this, error, "Type Error", JOptionPane.ERROR_MESSAGE);
			} else {
				approved = true;
				set.getGisSettings().setShapeColumn(shapeColumn.getName());
				set.getGisSettings().setShapeRegionColumn(shapeRegionColumn.getName());
				set.getGraphSettings().setNodeIdColumn(nodeIdColumn.getName());
				set.getGisSettings().setNodeRegionColumn(nodeRegionColumn.getName());
				set.getGraphSettings().setEdgeFromColumn(edgeFromColumn.getName());
				set.getGraphSettings().setEdgeToColumn(edgeToColumn.getName());
				set.setExportAsSvg(exportAsSvgBox.isSelected());
				dispose();
			}
		} else if (e.getSource() == cancelButton) {
			dispose();
		}
	}

}
