/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.data.DataTableSpec;
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
	DataTableSpec nodeSpec = null;
	DataTableSpec edgeSpec = null;
	DataTableSpec tracingSpec = null;
	JComboBox<String> dbSId = new JComboBox<>();
	JComboBox<String> dbSName = new JComboBox<>();
	JComboBox<String> dbSAddress = new JComboBox<>();
	JComboBox<String> dbSCountry = new JComboBox<>();
	JComboBox<String> dbSTOB = new JComboBox<>();
	JComboBox<String>[] sCombos = new JComboBox[]{dbSId, dbSName, dbSAddress, dbSCountry, dbSTOB};
	
	JComboBox<String> dbDId = new JComboBox<>();
	JComboBox<String> dbDFrom = new JComboBox<>();
	JComboBox<String> dbDTo = new JComboBox<>();
	JComboBox<String> dbDName = new JComboBox<>();
	JComboBox<String> dbDEAN = new JComboBox<>();
	JComboBox<String> dbDLot = new JComboBox<>();
	JComboBox<String> dbDBestBefore = new JComboBox<>();
	JComboBox<String> dbDDDD = new JComboBox<>();
	JComboBox<String> dbDDDM = new JComboBox<>();
	JComboBox<String> dbDDDY = new JComboBox<>();
	JComboBox<String> dbDAmount = new JComboBox<>();
	JComboBox<String> dbDComment = new JComboBox<>();
	JComboBox<String>[] dCombos = new JComboBox[]{dbDId, dbDFrom, dbDTo, dbDName, dbDEAN, dbDLot, dbDBestBefore, dbDDDD, dbDDDM, dbDDDY, dbDAmount, dbDComment};
	
	JComboBox<String> dbTFrom = new JComboBox<>();
	JComboBox<String> dbTTo = new JComboBox<>();
	JComboBox<String>[] tCombos = new JComboBox[]{dbTFrom, dbTTo};
	protected FCL_DB_WriterNodeDialog() {
		JPanel dbPanel = new JPanel();

		clearDB = new JCheckBox("Clear DB before writing");
		dbPanel.setLayout(new BoxLayout(dbPanel, BoxLayout.Y_AXIS));
		dbPanel.add(UI.createNorthPanel(UI.createBorderPanel(clearDB)));
		
	    JPanel sPanel = new JPanel();
	    sPanel.setLayout(new GridBagLayout());
	    GridBagConstraints c = new GridBagConstraints();
	    c.fill = GridBagConstraints.HORIZONTAL;
		
	    int y = 0;
	    JLabel label = new JLabel("Stations", JLabel.HORIZONTAL);
	    label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	    c.gridx = 0; c.gridy = y; c.gridwidth = 2;
	    sPanel.add(label, c);
	    
	    c.fill = GridBagConstraints.HORIZONTAL; c.gridwidth = 1;
	    y++; c.gridx = 0; c.gridy = y; sPanel.add(new JLabel("Id: ", JLabel.RIGHT), c);		
	    c.gridx = 1; c.gridy = y; sPanel.add(dbSId, c);		
	    y++; c.gridx = 0; c.gridy = y; sPanel.add(new JLabel("Name: ", JLabel.RIGHT), c);		
	    c.gridx = 1; c.gridy = y; sPanel.add(dbSName, c);		
	    y++; c.gridx = 0; c.gridy = y; sPanel.add(new JLabel("Address: ", JLabel.RIGHT), c);		
	    c.gridx = 1; c.gridy = y; sPanel.add(dbSAddress, c);		
	    y++; c.gridx = 0; c.gridy = y; sPanel.add(new JLabel("Country: ", JLabel.RIGHT), c);		
	    c.gridx = 1; c.gridy = y; sPanel.add(dbSCountry, c);		
	    y++; c.gridx = 0; c.gridy = y; sPanel.add(new JLabel("Type Of Business: ", JLabel.RIGHT), c);		
	    c.gridx = 1; c.gridy = y; sPanel.add(dbSTOB, c);		
	    
	    label = new JLabel("Deliveries", JLabel.HORIZONTAL);
	    label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	    y++; c.gridx = 0; c.gridy = y; c.gridwidth = 2;
	    sPanel.add(label, c);

	    c.fill = GridBagConstraints.HORIZONTAL; c.gridwidth = 1;
	    y++; c.gridx = 0; c.gridy = y; sPanel.add(new JLabel("Id: ", JLabel.RIGHT), c);		
	    c.gridx = 1; c.gridy = y; sPanel.add(dbDId, c);		
	    y++; c.gridx = 0; c.gridy = y; sPanel.add(new JLabel("from: ", JLabel.RIGHT), c);		
	    c.gridx = 1; c.gridy = y; sPanel.add(dbDFrom, c);		
	    y++; c.gridx = 0; c.gridy = y; sPanel.add(new JLabel("to: ", JLabel.RIGHT), c);		
	    c.gridx = 1; c.gridy = y; sPanel.add(dbDTo, c);		
	    y++; c.gridx = 0; c.gridy = y; sPanel.add(new JLabel("Name: ", JLabel.RIGHT), c);		
	    c.gridx = 1; c.gridy = y; sPanel.add(dbDName, c);		
	    y++; c.gridx = 0; c.gridy = y; sPanel.add(new JLabel("EAN: ", JLabel.RIGHT), c);		
	    c.gridx = 1; c.gridy = y; sPanel.add(dbDEAN, c);		
	    y++; c.gridx = 0; c.gridy = y; sPanel.add(new JLabel("Lot Number: ", JLabel.RIGHT), c);		
	    c.gridx = 1; c.gridy = y; sPanel.add(dbDLot, c);		
	    y++; c.gridx = 0; c.gridy = y; sPanel.add(new JLabel("Best Before Date: ", JLabel.RIGHT), c);		
	    c.gridx = 1; c.gridy = y; sPanel.add(dbDBestBefore, c);		
	    y++; c.gridx = 0; c.gridy = y; sPanel.add(new JLabel("Date Delivery - Day: ", JLabel.RIGHT), c);		
	    c.gridx = 1; c.gridy = y; sPanel.add(dbDDDD, c);		
	    y++; c.gridx = 0; c.gridy = y; sPanel.add(new JLabel("Date Delivery - Month: ", JLabel.RIGHT), c);		
	    c.gridx = 1; c.gridy = y; sPanel.add(dbDDDM, c);		
	    y++; c.gridx = 0; c.gridy = y; sPanel.add(new JLabel("Date Delivery - Year: ", JLabel.RIGHT), c);		
	    c.gridx = 1; c.gridy = y; sPanel.add(dbDDDY, c);		
	    y++; c.gridx = 0; c.gridy = y; sPanel.add(new JLabel("Amount: ", JLabel.RIGHT), c);		
	    c.gridx = 1; c.gridy = y; sPanel.add(dbDAmount, c);		
	    y++; c.gridx = 0; c.gridy = y; sPanel.add(new JLabel("Comment: ", JLabel.RIGHT), c);		
	    c.gridx = 1; c.gridy = y; sPanel.add(dbDComment, c);		

	    
	    label = new JLabel("Tracing - Relations", JLabel.HORIZONTAL);
	    label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	    y++; c.gridx = 0; c.gridy = y; c.gridwidth = 2;
	    sPanel.add(label, c);

	    c.fill = GridBagConstraints.HORIZONTAL; c.gridwidth = 1;
	    y++; c.gridx = 0; c.gridy = y; sPanel.add(new JLabel("from: ", JLabel.RIGHT), c);		
	    c.gridx = 1; c.gridy = y; sPanel.add(dbTFrom, c);		
	    y++; c.gridx = 0; c.gridy = y; sPanel.add(new JLabel("to: ", JLabel.RIGHT), c);		
	    c.gridx = 1; c.gridy = y; sPanel.add(dbTTo, c);		

	    dbPanel.add(sPanel);
		addTab("Database", UI.createNorthPanel(dbPanel));

		set = new FCL_DB_WriterSettings();
	}

	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs) {
		set.loadSettings(settings);
		clearDB.setSelected(set.isClearDB());

		nodeSpec = (DataTableSpec) specs[0];
		edgeSpec = (DataTableSpec) specs[1];
		tracingSpec = (DataTableSpec) specs[2];
		if (nodeSpec != null && edgeSpec != null && tracingSpec != null) {
			for (JComboBox<String> combo : sCombos) {
				combo.removeAllItems();
				combo.addItem("");
				for (String col : nodeSpec.getColumnNames()) combo.addItem(col);
			}
			for (JComboBox<String> combo : dCombos) {
				combo.removeAllItems();
				combo.addItem("");
				for (String col : edgeSpec.getColumnNames()) combo.addItem(col);
			}
			for (JComboBox<String> combo : tCombos) {
				combo.removeAllItems();
				combo.addItem("");
				for (String col : tracingSpec.getColumnNames()) combo.addItem(col);
			}
		}
		
		dbSId.setSelectedItem(set.getDbSId());
		dbSName.setSelectedItem(set.getDbSName());
		dbSAddress.setSelectedItem(set.getDbSAddress());
		dbSCountry.setSelectedItem(set.getDbSCountry());
		dbSTOB.setSelectedItem(set.getDbSTOB());

		dbDId.setSelectedItem(set.getDbDId());
		dbDName.setSelectedItem(set.getDbDName());
		dbDFrom.setSelectedItem(set.getDbDFrom());
		dbDTo.setSelectedItem(set.getDbDTo());
		dbDEAN.setSelectedItem(set.getDbDEAN());
		dbDLot.setSelectedItem(set.getDbDLot());
		dbDBestBefore.setSelectedItem(set.getDbDBestBefore());
		dbDDDD.setSelectedItem(set.getDbDDDD());
		dbDDDM.setSelectedItem(set.getDbDDDM());
		dbDDDY.setSelectedItem(set.getDbDDDY());
		dbDAmount.setSelectedItem(set.getDbDAmount());
		dbDComment.setSelectedItem(set.getDbDComment());
		
		dbTFrom.setSelectedItem(set.getDbTFrom());
		dbTTo.setSelectedItem(set.getDbTTo());
	}

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
		set.setClearDB(clearDB.isSelected());
		
		set.setDbSId((String) dbSId.getSelectedItem());
		set.setDbSName((String) dbSName.getSelectedItem());
		set.setDbSAddress((String) dbSAddress.getSelectedItem());
		set.setDbSCountry((String) dbSCountry.getSelectedItem());
		set.setDbSTOB((String) dbSTOB.getSelectedItem());
		
		set.setDbDId((String) dbDId.getSelectedItem());
		set.setDbDName((String) dbDName.getSelectedItem());
		set.setDbDFrom((String) dbDFrom.getSelectedItem());
		set.setDbDTo((String) dbDTo.getSelectedItem());
		set.setDbDEAN((String) dbDEAN.getSelectedItem());
		set.setDbDLot((String) dbDLot.getSelectedItem());
		set.setDbDBestBefore((String) dbDBestBefore.getSelectedItem());
		set.setDbDDDD((String) dbDDDD.getSelectedItem());
		set.setDbDDDM((String) dbDDDM.getSelectedItem());
		set.setDbDDDY((String) dbDDDY.getSelectedItem());
		set.setDbDAmount((String) dbDAmount.getSelectedItem());
		set.setDbDComment((String) dbDComment.getSelectedItem());
				
		set.setDbTFrom((String) dbTFrom.getSelectedItem());
		set.setDbTTo((String) dbTTo.getSelectedItem());

		set.saveSettings(settings);
	}
}
