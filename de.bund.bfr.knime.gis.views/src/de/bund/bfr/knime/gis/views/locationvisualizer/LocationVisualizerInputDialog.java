/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.gis.views.locationvisualizer;

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
import org.knime.core.data.def.DoubleCell;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.GisType;
import de.bund.bfr.knime.gis.shapecell.ShapeBlobCell;
import de.bund.bfr.knime.ui.ColumnComboBox;
import de.bund.bfr.knime.ui.Dialogs;
import de.bund.bfr.knime.ui.KnimeDialog;

public class LocationVisualizerInputDialog extends KnimeDialog {

	private static final long serialVersionUID = 1L;

	private JComboBox<GisType> gisBox;
	private ColumnComboBox shapeBox;
	private ColumnComboBox nodeLatitudeBox;
	private ColumnComboBox nodeLongitudeBox;
	private JCheckBox exportAsSvgBox;

	private boolean approved;
	private LocationVisualizerSettings set;

	public LocationVisualizerInputDialog(JComponent owner, DataTableSpec shapeSpec, DataTableSpec nodeSpec,
			LocationVisualizerSettings set) {
		super(owner, "Input", DEFAULT_MODALITY_TYPE);
		this.set = set;
		approved = false;

		gisBox = new JComboBox<>(shapeSpec != null ? GisType.values() : GisType.valuesWithoutShapefile());
		gisBox.setSelectedItem(set.getGisSettings().getGisType());
		gisBox.addActionListener(e -> shapeBox.setEnabled((GisType) gisBox.getSelectedItem() == GisType.SHAPEFILE));
		shapeBox = new ColumnComboBox(false, shapeSpec != null ? IO.getColumns(shapeSpec, ShapeBlobCell.TYPE) : null);
		shapeBox.setSelectedColumnName(set.getGisSettings().getShapeColumn());
		shapeBox.setEnabled((GisType) gisBox.getSelectedItem() == GisType.SHAPEFILE);
		nodeLatitudeBox = new ColumnComboBox(false, IO.getColumns(nodeSpec, DoubleCell.TYPE));
		nodeLatitudeBox.setSelectedColumnName(set.getGisSettings().getNodeLatitudeColumn());
		nodeLongitudeBox = new ColumnComboBox(false, IO.getColumns(nodeSpec, DoubleCell.TYPE));
		nodeLongitudeBox.setSelectedColumnName(set.getGisSettings().getNodeLongitudeColumn());
		exportAsSvgBox = new JCheckBox("Export As Svg");
		exportAsSvgBox.setSelected(set.isExportAsSvg());

		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");

		okButton.addActionListener(e -> okButtonPressed());
		cancelButton.addActionListener(e -> dispose());

		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(UI.createOptionsPanel("GIS Options",
				Arrays.asList(new JLabel("GIS Type"), new JLabel("Shape Column:")), Arrays.asList(gisBox, shapeBox)));
		mainPanel.add(UI.createOptionsPanel("Node Table",
				Arrays.asList(new JLabel("Latitude Column:"), new JLabel("Longitude Column:")),
				Arrays.asList(nodeLatitudeBox, nodeLongitudeBox)));
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
		DataColumnSpec nodeLatitudeColumn = nodeLatitudeBox.getSelectedColumn();
		DataColumnSpec nodeLongitudeColumn = nodeLongitudeBox.getSelectedColumn();
		GisType gisType = (GisType) gisBox.getSelectedItem();

		if (gisType == GisType.SHAPEFILE
				&& (shapeColumn == null || nodeLatitudeColumn == null || nodeLongitudeColumn == null)) {
			Dialogs.showErrorMessage(this, "\"Shape\", \"Latitude\" and \"Longitude\" columns must be selected");
		} else if (gisType != GisType.SHAPEFILE && (nodeLatitudeColumn == null || nodeLongitudeColumn == null)) {
			Dialogs.showErrorMessage(this, "\"Latitude\" and \"Longitude\" columns must be selected");
		} else {
			approved = true;
			set.getGisSettings().setGisType(gisType);
			set.getGisSettings().setShapeColumn(shapeBox.getSelectedColumnName());
			set.getGisSettings().setNodeLatitudeColumn(nodeLatitudeBox.getSelectedColumnName());
			set.getGisSettings().setNodeLongitudeColumn(nodeLongitudeBox.getSelectedColumnName());
			set.setExportAsSvg(exportAsSvgBox.isSelected());
			dispose();
		}
	}
}
