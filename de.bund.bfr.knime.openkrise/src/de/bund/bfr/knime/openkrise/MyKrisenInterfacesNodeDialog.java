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
package de.bund.bfr.knime.openkrise;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObjectSpec;

/**
 * <code>NodeDialog</code> for the "MyKrisenInterfaces" Node.
 * 
 * @author draaw
 */
public class MyKrisenInterfacesNodeDialog extends NodeDialogPane {

	private DbConfigurationUi dbui;
	private JCheckBox doAnonymize;

	// private JCheckBox randomGenerator;
	// private JSpinner randomNodes;
	// private JSpinner randomLinking;

	protected MyKrisenInterfacesNodeDialog() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		dbui = new DbConfigurationUi();

		doAnonymize = new JCheckBox();
		doAnonymize.setText("Anonymize Data");
		panel.add(doAnonymize);

		// randomGenerator = new JCheckBox();
		// randomGenerator.setText("Generate Random Data");
		// panel.add(randomGenerator);
		// randomGenerator.addItemListener(new ItemListener() {
		// public void itemStateChanged(ItemEvent e) {
		// randomNodes.setEnabled(randomGenerator.isSelected());
		// randomLinking.setEnabled(randomGenerator.isSelected());
		// doAnonymize.setEnabled(!randomGenerator.isSelected());
		// dbui.setEnabled(!randomGenerator.isSelected());
		// }
		// });
		// JPanel panelR = new JPanel();
		// panelR.setLayout(new FlowLayout());
		// randomNodes = new JSpinner(new SpinnerNumberModel(150, 0, 5000, 50));
		// randomNodes.setPreferredSize(new Dimension(150, 20));
		// panelR.add(randomNodes);
		// randomLinking = new JSpinner(new SpinnerNumberModel(3, 0, 50, 1));
		// randomLinking.setPreferredSize(new Dimension(150, 20));
		// panelR.add(randomLinking);
		// panel.add(panelR);

		addTab("Tracing/Filtering", panel);
		addTab("Database connection", dbui);
	}

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {

		settings.addString(MyKrisenInterfacesNodeModel.PARAM_FILENAME, dbui.getFilename());
		settings.addBoolean(MyKrisenInterfacesNodeModel.PARAM_OVERRIDE, dbui.isOverride());

		settings.addBoolean(MyKrisenInterfacesNodeModel.PARAM_ANONYMIZE, doAnonymize.isSelected());

		// settings.addBoolean(MyKrisenInterfacesNodeModel.PARAM_RANDOM,
		// randomGenerator.isSelected());
		// settings.addInt(MyKrisenInterfacesNodeModel.PARAM_RANDOMNODES,
		// (Integer) randomNodes.getValue());
		// settings.addInt(MyKrisenInterfacesNodeModel.PARAM_RANDOMLINKING,
		// (Integer) randomLinking.getValue());

	}

	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs) {
		try {

			dbui.setFilename(settings.getString(MyKrisenInterfacesNodeModel.PARAM_FILENAME));
			dbui.setOverride(settings.getBoolean(MyKrisenInterfacesNodeModel.PARAM_OVERRIDE));

			doAnonymize.setSelected(settings
					.getBoolean(MyKrisenInterfacesNodeModel.PARAM_ANONYMIZE));

			// if
			// (settings.containsKey(MyKrisenInterfacesNodeModel.PARAM_RANDOM))
			// randomGenerator.setSelected(settings.getBoolean(MyKrisenInterfacesNodeModel.PARAM_RANDOM));
			// if
			// (settings.containsKey(MyKrisenInterfacesNodeModel.PARAM_RANDOMNODES))
			// randomNodes.setValue(settings.getInt(MyKrisenInterfacesNodeModel.PARAM_RANDOMNODES));
			// if
			// (settings.containsKey(MyKrisenInterfacesNodeModel.PARAM_RANDOMLINKING))
			// randomLinking.setValue(settings.getInt(MyKrisenInterfacesNodeModel.PARAM_RANDOMLINKING));

		} catch (InvalidSettingsException ex) {

			ex.printStackTrace(System.err);
		}

	}
}
