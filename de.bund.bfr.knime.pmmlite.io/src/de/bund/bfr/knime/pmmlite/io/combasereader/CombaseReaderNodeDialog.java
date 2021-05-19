/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.io.combasereader;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.FilesHistoryPanel;

import de.bund.bfr.knime.UI;

/**
 * <code>NodeDialog</code> for the "CombaseReader" Node.
 * 
 * @author Christian Thoens
 */
public class CombaseReaderNodeDialog extends NodeDialogPane {

	private static final String FILE_HISTORY_ID = "CombaseReaderFileHistory";

	private CombaseReaderSettings set;

	private FilesHistoryPanel filePanel;
	private JCheckBox ndBox;

	/**
	 * New pane for configuring the CombaseReader node.
	 */
	protected CombaseReaderNodeDialog() {
		set = new CombaseReaderSettings();
		filePanel = new FilesHistoryPanel(FILE_HISTORY_ID, FilesHistoryPanel.LocationValidation.FileInput);
		filePanel.setSuffixes(".csv");
		ndBox = new JCheckBox("Treat \"N/D\" as zero");

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		panel.add(UI.createTitledPanel(filePanel, "Combase CSV File"), BorderLayout.NORTH);
		panel.add(UI.createTitledPanel(UI.createWestPanel(ndBox), "Options"), BorderLayout.CENTER);
		addTab("Options", UI.createNorthPanel(panel));
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		set.load(settings);
		filePanel.setSelectedFile(set.getFileName());
		ndBox.setSelected(set.isTreatNotDetectedAsZero());
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		if (filePanel.getSelectedFile() == null) {
			throw new InvalidSettingsException("No file is specfied");
		}

		set.setFileName(filePanel.getSelectedFile());
		set.setTreatNotDetectedAsZero(ndBox.isSelected());
		set.save(settings);
	}
}
