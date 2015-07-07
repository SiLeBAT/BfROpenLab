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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.FilesHistoryPanel;

import de.bund.bfr.knime.UI;

/**
 * <code>NodeDialog</code> for the "MyKrisenInterfaces" Node.
 * 
 * @author draaw
 */
public class MyKrisenInterfacesNodeDialog extends NodeDialogPane implements ItemListener {

	private static final String FILE_HISTORY_ID = "SupplyChainReaderFileHistory";

	private JCheckBox anonymizeBox;

	private JCheckBox overrideBox;
	private FilesHistoryPanel connField;

	protected MyKrisenInterfacesNodeDialog() {
		JPanel tracingPanel = new JPanel();

		anonymizeBox = new JCheckBox("Anonymize Data");
		tracingPanel.setLayout(new BoxLayout(tracingPanel, BoxLayout.Y_AXIS));
		tracingPanel.add(UI.createWestPanel(UI.createHorizontalPanel(anonymizeBox)));

		JPanel dbPanel = new JPanel();

		overrideBox = new JCheckBox("Use External Database");
		overrideBox.addItemListener(this);
		connField = new FilesHistoryPanel(FILE_HISTORY_ID, FilesHistoryPanel.LocationValidation.DirectoryInput);
		dbPanel.setLayout(new BoxLayout(dbPanel, BoxLayout.Y_AXIS));
		dbPanel.add(UI.createWestPanel(UI.createHorizontalPanel(overrideBox)));
		dbPanel.add(UI.createTitledPanel(connField, "Database Path"));

		addTab("Options", UI.createNorthPanel(tracingPanel));
		addTab("Database Connection", UI.createNorthPanel(dbPanel));
	}

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
		settings.addBoolean(MyKrisenInterfacesNodeModel.PARAM_ANONYMIZE, anonymizeBox.isSelected());
		settings.addString(MyKrisenInterfacesNodeModel.PARAM_FILENAME, connField.getSelectedFile());
		settings.addBoolean(MyKrisenInterfacesNodeModel.PARAM_OVERRIDE, overrideBox.isSelected());
	}

	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs) {
		try {
			anonymizeBox.setSelected(settings.getBoolean(MyKrisenInterfacesNodeModel.PARAM_ANONYMIZE));
			overrideBox.setSelected(settings.getBoolean(MyKrisenInterfacesNodeModel.PARAM_OVERRIDE));
			connField.setSelectedFile(removeNameOfDB(settings.getString(MyKrisenInterfacesNodeModel.PARAM_FILENAME)));
			connField.setEnabled(overrideBox.isSelected());
		} catch (InvalidSettingsException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		connField.setEnabled(overrideBox.isSelected());
	}

	private static String removeNameOfDB(String path) {
		return path.endsWith("\\DB") || path.endsWith("/DB") ? path.substring(0, path.length() - 3) : path;
	}
}
