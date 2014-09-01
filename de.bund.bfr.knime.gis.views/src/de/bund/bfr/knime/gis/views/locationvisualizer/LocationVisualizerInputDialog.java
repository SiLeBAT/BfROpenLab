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
package de.bund.bfr.knime.gis.views.locationvisualizer;

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
import org.knime.core.data.def.DoubleCell;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.ui.ColumnComboBox;

public class LocationVisualizerInputDialog extends JDialog implements
		ActionListener {

	private static final long serialVersionUID = 1L;

	private ColumnComboBox shapeBox;
	private ColumnComboBox nodeLatitudeBox;
	private ColumnComboBox nodeLongitudeBox;
	private JCheckBox exportAsSvgBox;

	private JButton okButton;
	private JButton cancelButton;

	private boolean approved;
	private LocationVisualizerSettings set;

	public LocationVisualizerInputDialog(JComponent owner,
			DataTableSpec shapeSpec, DataTableSpec nodeSpec,
			LocationVisualizerSettings set) {
		super(SwingUtilities.getWindowAncestor(owner), "Input",
				DEFAULT_MODALITY_TYPE);
		this.set = set;
		approved = false;

		shapeBox = new ColumnComboBox(false,
				GisUtils.getShapeColumns(shapeSpec));
		shapeBox.setSelectedColumnName(set.getGisSettings().getShapeColumn());
		nodeLatitudeBox = new ColumnComboBox(false, KnimeUtils.getColumns(
				nodeSpec, DoubleCell.TYPE));
		nodeLatitudeBox.setSelectedColumnName(set.getGisSettings()
				.getNodeLatitudeColumn());
		nodeLongitudeBox = new ColumnComboBox(false, KnimeUtils.getColumns(
				nodeSpec, DoubleCell.TYPE));
		nodeLongitudeBox.setSelectedColumnName(set.getGisSettings()
				.getNodeLongitudeColumn());
		exportAsSvgBox = new JCheckBox("Export As Svg");
		exportAsSvgBox.setSelected(set.isExportAsSvg());
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(UI.createOptionsPanel("Shape Table",
				Arrays.asList(new JLabel("Shape Column:")),
				Arrays.asList(shapeBox)));
		mainPanel.add(UI.createOptionsPanel("Node Table", Arrays
				.asList(new JLabel("Latitude Column:"), new JLabel(
						"Longitude Column:")), Arrays.asList(nodeLatitudeBox,
				nodeLongitudeBox)));
		mainPanel.add(UI.createOptionsPanel("Miscellaneous",
				Arrays.asList(exportAsSvgBox), Arrays.asList(new JLabel())));

		setLayout(new BorderLayout());
		add(UI.createNorthPanel(mainPanel), BorderLayout.CENTER);
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
			DataColumnSpec nodeLatitudeColumn = nodeLatitudeBox
					.getSelectedColumn();
			DataColumnSpec nodeLongitudeColumn = nodeLongitudeBox
					.getSelectedColumn();

			if (shapeColumn == null || nodeLatitudeColumn == null
					|| nodeLongitudeColumn == null) {
				String error = "\"Shape\", \"Latitude\" and \"Longitude\""
						+ " columns must be selected";

				JOptionPane.showMessageDialog(this, error, "Error",
						JOptionPane.ERROR_MESSAGE);
			} else {
				approved = true;
				set.getGisSettings().setShapeColumn(
						shapeBox.getSelectedColumnName());
				set.getGisSettings().setNodeLatitudeColumn(
						nodeLatitudeBox.getSelectedColumnName());
				set.getGisSettings().setNodeLongitudeColumn(
						nodeLongitudeBox.getSelectedColumnName());
				set.setExportAsSvg(exportAsSvgBox.isSelected());
				dispose();
			}
		} else if (e.getSource() == cancelButton) {
			dispose();
		}
	}

}
