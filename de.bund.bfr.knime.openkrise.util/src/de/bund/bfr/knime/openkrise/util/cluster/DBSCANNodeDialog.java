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
package de.bund.bfr.knime.openkrise.util.cluster;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightDialog;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.TracingPropertySelectorCreator;
import de.bund.bfr.knime.openkrise.TracingUtils;
import de.bund.bfr.knime.ui.DoubleTextField;
import de.bund.bfr.knime.ui.IntTextField;

/**
 * <code>NodeDialog</code> for the "DBSCAN" Node.
 * 
 * @author BfR
 */
public class DBSCANNodeDialog extends DataAwareNodeDialogPane {

	private DBSCANNSettings set;
	private NodePropertySchema schema;

	private JComboBox<String> modelBox;
	private JButton filterButton;
	private JButton removeFilterButton;
	private IntTextField minPointsField;
	private DoubleTextField maxDistField;
	private IntTextField numClustersField;

	private JPanel panel;

	/**
	 * New pane for configuring the DBSCAN node.
	 */
	public DBSCANNodeDialog() {
		set = new DBSCANNSettings();

		modelBox = new JComboBox<>(new Vector<>(DBSCANNSettings.MODEL_CHOICES));
		modelBox.addActionListener(e -> updatePanel());
		filterButton = new JButton("Set Filter");
		filterButton.addActionListener(e -> filterButtonPressed());
		removeFilterButton = new JButton("Remove Filter");
		removeFilterButton.setEnabled(false);
		removeFilterButton.addActionListener(e -> removeFilterButtonPressed());
		minPointsField = new IntTextField(false, 5);
		minPointsField.setMinValue(1);
		maxDistField = new DoubleTextField(false, 5);
		maxDistField.setMinValue(0);
		numClustersField = new IntTextField(false, 5);
		numClustersField.setMinValue(1);

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		addTab("Options", UI.createNorthPanel(panel));
	}

	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings, final BufferedDataTable[] input)
			throws NotConfigurableException {
		schema = new NodePropertySchema(TracingUtils.getTableColumns(input[0].getSpec()), TracingColumns.ID);
		schema.getPossibleValues().putAll(CanvasUtils.getPossibleValues(
				TracingUtils.readLocationNodes(input[0], schema, new LinkedHashMap<>(), false).values()));

		set.loadSettings(settings);
		modelBox.setSelectedItem(set.getModel());
		minPointsField.setValue(set.getMinPoints());
		maxDistField.setValue(set.getMaxDistance());
		numClustersField.setValue(set.getNumClusters());
		removeFilterButton.setEnabled(set.getFilter() != null);

		updatePanel();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		set.setModel((String) modelBox.getSelectedItem());

		if (set.getModel().equals(DBSCANNSettings.MODEL_DBSCAN)) {
			if (!minPointsField.isValueValid()) {
				throw new InvalidSettingsException("Invalid: Min Number of Points per Cluster");
			}

			if (!maxDistField.isValueValid()) {
				throw new InvalidSettingsException("Invalid: Max Neighborhood Distance (km)");
			}

			set.setMinPoints(minPointsField.getValue());
			set.setMaxDistance(maxDistField.getValue());
		} else if (set.getModel().equals(DBSCANNSettings.MODEL_K_MEANS)) {
			if (!numClustersField.isValueValid()) {
				throw new InvalidSettingsException("Invalid: Number of Clusters");
			}

			set.setNumClusters(numClustersField.getValue());
		}

		set.saveSettings(settings);
	}

	private void updatePanel() {
		String model = (String) modelBox.getSelectedItem();

		panel.removeAll();
		panel.add(UI.createOptionsPanel("Options", Arrays.asList(new JLabel("Cluster Algorithm:"), filterButton),
				Arrays.asList(modelBox, UI.createWestPanel(removeFilterButton))));

		if (model.equals(DBSCANNSettings.MODEL_DBSCAN)) {
			panel.add(UI.createOptionsPanel("Algorithm Options",
					Arrays.asList(new JLabel("Min Number of Points per Cluster:"),
							new JLabel("Max Neighborhood Distance (km):")),
					Arrays.asList(minPointsField, maxDistField)));
		} else if (model.equals(DBSCANNSettings.MODEL_K_MEANS)) {
			panel.add(UI.createOptionsPanel("Algorithm Options", Arrays.asList(new JLabel("Number of Clusters")),
					Arrays.asList(numClustersField)));
		}

		panel.revalidate();
	}

	private void filterButtonPressed() {
		HighlightDialog dialog = HighlightDialog.createFilterDialog(filterButton, schema, set.getFilter(),
				new TracingPropertySelectorCreator());

		dialog.setLocationRelativeTo(filterButton);
		dialog.setVisible(true);

		if (dialog.isApproved()) {
			set.setFilter((AndOrHighlightCondition) dialog.getHighlightCondition());
			removeFilterButton.setEnabled(true);
		}
	}

	private void removeFilterButtonPressed() {
		set.setFilter(null);
		removeFilterButton.setEnabled(false);
	}
}
