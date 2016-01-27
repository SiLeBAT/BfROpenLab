/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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

import java.net.MalformedURLException;
import java.nio.file.InvalidPathException;

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
public class MyKrisenInterfacesNodeDialog extends NodeDialogPane {

	private static final String FILE_HISTORY_ID = "SupplyChainReaderFileHistory";

	private MyKrisenInterfacesSettings set;

	private JCheckBox backwardBox;
	private JCheckBox anonymizeBox;
	private JCheckBox dbBox;
	private FilesHistoryPanel dbField;

	protected MyKrisenInterfacesNodeDialog() {
		JPanel tracingPanel = new JPanel();

		backwardBox = new JCheckBox("Ensure Backward Compatibility");
		anonymizeBox = new JCheckBox("Anonymize Data");
		tracingPanel.setLayout(new BoxLayout(tracingPanel, BoxLayout.Y_AXIS));
		tracingPanel.add(UI.createWestPanel(UI.createHorizontalPanel(backwardBox)));
		tracingPanel.add(UI.createWestPanel(UI.createHorizontalPanel(anonymizeBox)));

		JPanel dbPanel = new JPanel();

		dbBox = new JCheckBox("Use External Database");
		dbBox.addActionListener(e -> dbField.setEnabled(dbBox.isSelected()));
		dbField = new FilesHistoryPanel(FILE_HISTORY_ID, FilesHistoryPanel.LocationValidation.DirectoryInput);
		dbPanel.setLayout(new BoxLayout(dbPanel, BoxLayout.Y_AXIS));
		dbPanel.add(UI.createWestPanel(UI.createHorizontalPanel(dbBox)));
		dbPanel.add(UI.createTitledPanel(dbField, "Database Path"));

		addTab("Options", UI.createNorthPanel(tracingPanel));
		addTab("Database Connection", UI.createNorthPanel(dbPanel));

		set = new MyKrisenInterfacesSettings();
	}

	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs) {
		set.loadSettings(settings);
		backwardBox.setSelected(set.isEnsureBackwardCompatibility());
		anonymizeBox.setSelected(set.isAnonymize());
		dbBox.setSelected(set.isUseExternalDb());
		dbField.setEnabled(dbBox.isSelected());

		try {
			dbField.setSelectedFile(MyKrisenInterfacesNodeModel.removeNameOfDB(set.getDbPath()));
		} catch (InvalidPathException | MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
		set.setEnsureBackwardCompatibility(backwardBox.isSelected());
		set.setAnonymize(anonymizeBox.isSelected());
		set.setUseExternalDb(dbBox.isSelected());
		set.setDbPath(dbField.getSelectedFile());
		set.saveSettings(settings);
	}
}
