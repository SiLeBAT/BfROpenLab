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
package de.bund.bfr.knime.gis.views.locationvisualizer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DoubleCell;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.gis.views.LocationSettings.GisType;
import de.bund.bfr.knime.ui.ColumnComboBox;

public class LocationVisualizerInputDialog extends JDialog implements ActionListener, ItemListener {

	private static final long serialVersionUID = 1L;

	private JComboBox<GisType> gisBox;
	private ColumnComboBox shapeBox;
	private ColumnComboBox nodeLatitudeBox;
	private ColumnComboBox nodeLongitudeBox;
	private JCheckBox exportAsSvgBox;

	private JButton okButton;
	private JButton cancelButton;

	private boolean approved;
	private LocationVisualizerSettings set;

	public LocationVisualizerInputDialog(JComponent owner, DataTableSpec shapeSpec, DataTableSpec nodeSpec,
			LocationVisualizerSettings set) {
		super(SwingUtilities.getWindowAncestor(owner), "Input", DEFAULT_MODALITY_TYPE);
		this.set = set;
		approved = false;

		gisBox = new JComboBox<>(shapeSpec != null ? GisType.values() : GisType.valuesWithoutShapefile());
		gisBox.setSelectedItem(set.getGisSettings().getGisType());
		gisBox.addItemListener(this);
		shapeBox = new ColumnComboBox(false, shapeSpec != null ? GisUtils.getShapeColumns(shapeSpec) : null);
		shapeBox.setSelectedColumnName(set.getGisSettings().getShapeColumn());
		shapeBox.setEnabled((GisType) gisBox.getSelectedItem() == GisType.SHAPEFILE);
		nodeLatitudeBox = new ColumnComboBox(false, KnimeUtils.getColumns(nodeSpec, DoubleCell.TYPE));
		nodeLatitudeBox.setSelectedColumnName(set.getGisSettings().getNodeLatitudeColumn());
		nodeLongitudeBox = new ColumnComboBox(false, KnimeUtils.getColumns(nodeSpec, DoubleCell.TYPE));
		nodeLongitudeBox.setSelectedColumnName(set.getGisSettings().getNodeLongitudeColumn());
		exportAsSvgBox = new JCheckBox("Export As Svg");
		exportAsSvgBox.setSelected(set.isExportAsSvg());
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

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
		setLocationRelativeTo(owner);
		pack();
		getRootPane().setDefaultButton(okButton);
	}

	public boolean isApproved() {
		return approved;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			DataColumnSpec shapeColumn = shapeBox.getSelectedColumn();
			DataColumnSpec nodeLatitudeColumn = nodeLatitudeBox.getSelectedColumn();
			DataColumnSpec nodeLongitudeColumn = nodeLongitudeBox.getSelectedColumn();
			GisType gisType = (GisType) gisBox.getSelectedItem();

			if (gisType == GisType.SHAPEFILE
					&& (shapeColumn == null || nodeLatitudeColumn == null || nodeLongitudeColumn == null)) {
				String error = "\"Shape\", \"Latitude\" and \"Longitude\"" + " columns must be selected";

				JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
			} else if (gisType != GisType.SHAPEFILE && (nodeLatitudeColumn == null || nodeLongitudeColumn == null)) {
				String error = "\"Latitude\" and \"Longitude\"" + " columns must be selected";

				JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
			} else {
				approved = true;
				set.getGisSettings().setGisType(gisType);
				set.getGisSettings().setShapeColumn(shapeBox.getSelectedColumnName());
				set.getGisSettings().setNodeLatitudeColumn(nodeLatitudeBox.getSelectedColumnName());
				set.getGisSettings().setNodeLongitudeColumn(nodeLongitudeBox.getSelectedColumnName());
				set.setExportAsSvg(exportAsSvgBox.isSelected());
				dispose();
			}
		} else if (e.getSource() == cancelButton) {
			dispose();
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == gisBox && e.getStateChange() == ItemEvent.SELECTED) {
			shapeBox.setEnabled((GisType) gisBox.getSelectedItem() == GisType.SHAPEFILE);
		}
	}
}
