/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
public class MyKrisenInterfacesXmlNodeDialog extends NodeDialogPane {

	private static final String XML_HISTORY_ID = "SupplyChainReaderXmlHistory";

	private MyKrisenInterfacesXmlSettings set;

	private JCheckBox anonymizeBox, useBusstopBox;
	private FilesHistoryPanel xmlField;

	protected MyKrisenInterfacesXmlNodeDialog() {
		JPanel tracingPanel = new JPanel();

		xmlField = new FilesHistoryPanel(XML_HISTORY_ID, FilesHistoryPanel.LocationValidation.DirectoryInput);
		useBusstopBox = new JCheckBox("Use Busstop");
		useBusstopBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	xmlField.setEnabled(!useBusstopBox.isSelected());
            }
        });
		anonymizeBox = new JCheckBox("Anonymize Data");
		tracingPanel.setLayout(new BoxLayout(tracingPanel, BoxLayout.Y_AXIS));
		tracingPanel.add(UI.createWestPanel(UI.createBorderPanel(useBusstopBox)));
		tracingPanel.add(UI.createTitledPanel(xmlField, "Xml Path"));
		tracingPanel.add(UI.createWestPanel(UI.createBorderPanel(anonymizeBox)));

		addTab("Options", UI.createNorthPanel(tracingPanel));

		set = new MyKrisenInterfacesXmlSettings();
	}

	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs) {
		set.loadSettings(settings);
		useBusstopBox.setSelected(set.isBusstop());
		anonymizeBox.setSelected(set.isAnonymize());
		xmlField.setSelectedFile(set.getXmlPath());
		xmlField.setEnabled(!useBusstopBox.isSelected());
	}

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
		set.setBusstop(useBusstopBox.isSelected());
		set.setAnonymize(anonymizeBox.isSelected());
		set.setXmlPath(xmlField.getSelectedFile());
		set.saveSettings(settings);
	}
}
