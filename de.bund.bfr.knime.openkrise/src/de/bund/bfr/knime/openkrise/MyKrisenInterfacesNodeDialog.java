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

    	doAnonymize = new JCheckBox(); doAnonymize.setText("Anonymize?"); panel.add(doAnonymize);
    	
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