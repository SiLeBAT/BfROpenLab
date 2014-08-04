/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Armin A. Weiser (BfR)
 * Christian Thoens (BfR)
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

	//private DbConfigurationUi dbui;
	private JCheckBox doAnonymize;

	protected MyKrisenInterfacesNodeDialog() {
		JPanel panel = new JPanel();
    	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));   	
    	
    	//dbui = new DbConfigurationUi();

    	doAnonymize = new JCheckBox(); doAnonymize.setText("Anonymize Data"); panel.add(doAnonymize);
    	
    	addTab("Tracing/Filtering", panel);
    	//addTab("Database connection", dbui);
    }
	
	@Override
	protected void saveSettingsTo( final NodeSettingsWO settings )
			throws InvalidSettingsException {
		/*
		settings.addString(MyKrisenInterfacesNodeModel.PARAM_FILENAME, dbui.getFilename());
		settings.addString(MyKrisenInterfacesNodeModel.PARAM_LOGIN, dbui.getLogin());
		settings.addString(MyKrisenInterfacesNodeModel.PARAM_PASSWD, dbui.getPasswd());
		settings.addBoolean(MyKrisenInterfacesNodeModel.PARAM_OVERRIDE, dbui.isOverride());
*/
		settings.addBoolean(MyKrisenInterfacesNodeModel.PARAM_ANONYMIZE, doAnonymize.isSelected());
		
	}

	@Override
	protected void loadSettingsFrom( final NodeSettingsRO settings, final PortObjectSpec[] specs )  {		
		try {
			/*
			dbui.setFilename(settings.getString( MyKrisenInterfacesNodeModel.PARAM_FILENAME));
			dbui.setLogin( settings.getString( MyKrisenInterfacesNodeModel.PARAM_LOGIN));
			dbui.setPasswd(settings.getString( MyKrisenInterfacesNodeModel.PARAM_PASSWD));
			dbui.setOverride(settings.getBoolean( MyKrisenInterfacesNodeModel.PARAM_OVERRIDE));
*/
			doAnonymize.setSelected(settings.getBoolean(MyKrisenInterfacesNodeModel.PARAM_ANONYMIZE));

		}
		catch( InvalidSettingsException ex ) {
			
			ex.printStackTrace( System.err );
		}
		
	}
}
