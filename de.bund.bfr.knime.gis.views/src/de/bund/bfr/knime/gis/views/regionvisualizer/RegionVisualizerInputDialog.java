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
package de.bund.bfr.knime.gis.views.regionvisualizer;

import java.awt.BorderLayout;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.GisType;
import de.bund.bfr.knime.gis.shapecell.ShapeBlobCell;
import de.bund.bfr.knime.ui.ColumnComboBox;
import de.bund.bfr.knime.ui.Dialogs;
import de.bund.bfr.knime.ui.KnimeDialog;

public class RegionVisualizerInputDialog extends KnimeDialog {

	private static final long serialVersionUID = 1L;

	private JComboBox<GisType> gisBox;
	private ColumnComboBox shapeBox;
	private ColumnComboBox shapeRegionBox;
	private ColumnComboBox nodeRegionBox;
	private JCheckBox exportAsSvgBox;

	private boolean approved;
	private RegionVisualizerSettings set;

	public RegionVisualizerInputDialog(JComponent owner, DataTableSpec shapeSpec, DataTableSpec nodeSpec,
			RegionVisualizerSettings set) {
		super(owner, "Input", DEFAULT_MODALITY_TYPE);
		this.set = set;
		approved = false;

		gisBox = new JComboBox<>(GisType.values());
		gisBox.setSelectedItem(set.getGisSettings().getGisType());
		shapeBox = new ColumnComboBox(false, IO.getColumns(shapeSpec, ShapeBlobCell.TYPE));
		shapeBox.setSelectedColumnName(set.getGisSettings().getShapeColumn());
		shapeRegionBox = new ColumnComboBox(false, IO.getColumns(shapeSpec, StringCell.TYPE, IntCell.TYPE));
		shapeRegionBox.setSelectedColumnName(set.getGisSettings().getShapeRegionColumn());
		nodeRegionBox = new ColumnComboBox(false, IO.getColumns(nodeSpec, StringCell.TYPE, IntCell.TYPE));
		nodeRegionBox.setSelectedColumnName(set.getGisSettings().getNodeRegionColumn());
		exportAsSvgBox = new JCheckBox("Export As Svg");
		exportAsSvgBox.setSelected(set.isExportAsSvg());

		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");

		okButton.addActionListener(e -> okButtonPressed());
		cancelButton.addActionListener(e -> dispose());

		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(UI.createOptionsPanel("Shape Table",
				Arrays.asList(new JLabel("GIS Type"), new JLabel("Shape Column:"), new JLabel("Region ID Column:")),
				Arrays.asList(gisBox, shapeBox, shapeRegionBox)));
		mainPanel.add(UI.createOptionsPanel("Node Table", Arrays.asList(new JLabel("Region ID Column:")),
				Arrays.asList(nodeRegionBox)));
		mainPanel.add(
				UI.createOptionsPanel("Miscellaneous", Arrays.asList(exportAsSvgBox), Arrays.asList(new JLabel())));

		setLayout(new BorderLayout());
		add(UI.createNorthPanel(mainPanel), BorderLayout.CENTER);
		add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)), BorderLayout.SOUTH);

		pack();
		UI.adjustDialog(this);
		setLocationRelativeTo(owner);
		getRootPane().setDefaultButton(okButton);
	}

	public boolean isApproved() {
		return approved;
	}

	private void okButtonPressed() {
		DataColumnSpec shapeColumn = shapeBox.getSelectedColumn();
		DataColumnSpec shapeRegionColumn = shapeRegionBox.getSelectedColumn();
		DataColumnSpec nodeRegionColumn = nodeRegionBox.getSelectedColumn();

		if (shapeColumn == null || shapeRegionColumn == null || nodeRegionColumn == null) {
			Dialogs.showErrorMessage(this, "\"Shape\" and all \"Region ID\" columns must be selected");
		} else if (shapeRegionColumn.getType() != nodeRegionColumn.getType()) {
			Dialogs.showErrorMessage(this, "All \"Region ID\" columns must have the same type");
		} else {
			approved = true;
			set.getGisSettings().setGisType((GisType) gisBox.getSelectedItem());
			set.getGisSettings().setShapeColumn(shapeColumn.getName());
			set.getGisSettings().setShapeRegionColumn(shapeRegionColumn.getName());
			set.getGisSettings().setNodeRegionColumn(nodeRegionColumn.getName());
			set.setExportAsSvg(exportAsSvgBox.isSelected());
			dispose();
		}
	}

}
