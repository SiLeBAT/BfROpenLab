package de.bund.bfr.knime.openkrise.out;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.port.PortObjectSpec;

import de.bund.bfr.knime.UI;

/**
 * <code>NodeDialog</code> for the "TracingXmlOut" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author BfR
 */
public class TracingXmlOutNodeDialog extends NodeDialogPane {

	private TracingXmlOutNodeSettings set;

	private JTextField server;//, user;
	//private JPasswordField pass;

	protected TracingXmlOutNodeDialog() {
		JPanel tracingPanel = new JPanel();

		server = new JTextField();
		//user = new JTextField();
		//pass = new JPasswordField();
		tracingPanel.setLayout(new BoxLayout(tracingPanel, BoxLayout.Y_AXIS));
		tracingPanel.add(UI.createTitledPanel(server, "Server Address"));
		//tracingPanel.add(UI.createTitledPanel(user, "Username"));
		//tracingPanel.add(UI.createTitledPanel(pass, "Password"));

		addTab("Options", UI.createNorthPanel(tracingPanel));

		set = new TracingXmlOutNodeSettings();
	}

	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs) {
		set.loadSettings(settings);
		server.setText(set.getServer());
		//user.setText(set.getUser());
		//pass.setText(set.getPass());
	}

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
		set.setServer(server.getText());
		//set.setUser(user.getText());
		//set.setPass(new String(pass.getPassword()));
		set.saveSettings(settings);
	}
}

