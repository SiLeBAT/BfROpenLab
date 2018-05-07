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
package de.bund.bfr.knime.openkrise;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObjectSpec;

import de.bund.bfr.knime.UI;

/**
 * <code>NodeDialog</code> for the "MyKrisenInterfaces" Node.
 * 
 * @author draaw
 */
public class FCL_DB_WriterNodeDialog extends NodeDialogPane {

	private FCL_DB_WriterSettings set;

	private JCheckBox clearDB;

	protected FCL_DB_WriterNodeDialog() {
		JPanel dbPanel = new JPanel();

		clearDB = new JCheckBox("Clear DB before writing");
		dbPanel.setLayout(new BoxLayout(dbPanel, BoxLayout.Y_AXIS));
		dbPanel.add(UI.createWestPanel(UI.createBorderPanel(clearDB)));

		addTab("Database", UI.createNorthPanel(dbPanel));

		set = new FCL_DB_WriterSettings();
	}

	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs) {
		set.loadSettings(settings);
		clearDB.setSelected(set.isClearDB());
	}

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
		set.setClearDB(clearDB.isSelected());
		set.saveSettings(settings);
	}
}
