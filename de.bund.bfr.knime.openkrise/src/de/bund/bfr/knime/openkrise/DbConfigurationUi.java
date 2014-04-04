/*******************************************************************************
 * PMM-Lab © 2012, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab © 2012, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * Joergen Brandt (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Christian Thoens (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
 * 
 * PMM-Lab is a project under development. Contributions are welcome.
 * 
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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.config.Config;

public class DbConfigurationUi extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 20120622;
	
	public static final String PARAM_FILENAME = "filename";
	public static final String PARAM_LOGIN = "login";
	public static final String PARAM_PASSWD = "passwd";
	public static final String PARAM_OVERRIDE = "override";

	private JCheckBox overrideBox;
	private JTextField connField;
	private JTextField loginField;
	private JPasswordField passwdField;
	private JButton chooseButton;
	private JButton applyButton;
	
	public DbConfigurationUi() { this( false ); }

	public DbConfigurationUi( boolean hasApplyButton ) {
		
		JPanel mainPanel, panel, panel2, panel0;
		
		mainPanel = new JPanel();
		mainPanel.setLayout( new BorderLayout() );
		// setPreferredSize( new Dimension( 330, 130 ) );
		
		panel0 = new JPanel();
		panel0.setLayout( new BorderLayout() );
		
		mainPanel.add( panel0, BorderLayout.SOUTH );
				
		panel = new JPanel();
		panel.setLayout( new GridLayout( 0, 1 ) );
		panel0.add( panel, BorderLayout.WEST );
		// panel.add( new JLabel() );
		panel.add( new JLabel( "Database : " ) );
		panel.add( new JLabel( "Login : " ) );
		panel.add( new JLabel( "Password : " ) );
		panel.add( new JLabel() );
		
		panel = new JPanel();
		panel.setLayout( new GridLayout( 0, 1 ) );
		panel0.add( panel, BorderLayout.CENTER );
		
		
		overrideBox = new JCheckBox( "Use external database" );
		overrideBox.addActionListener( this );
		mainPanel.add( overrideBox, BorderLayout.CENTER );
		
		panel2 = new JPanel();
		panel2.setLayout( new BorderLayout() );
		panel.add( panel2 );
		
		connField = new JTextField();
		connField.setEditable( false );
		panel2.add( connField, BorderLayout.CENTER );
		
		chooseButton = new JButton( "..." );
		chooseButton.addActionListener( this );
		panel2.add( chooseButton, BorderLayout.EAST );
		panel2.add( new JLabel( "jdbc:hsqldb:file:" ), BorderLayout.WEST );
		
		loginField = new JTextField();
		loginField.setEditable( false );
		panel.add( loginField );
		
		passwdField = new JPasswordField();
		passwdField.setEditable( false );
		panel.add( passwdField );
		
		if( hasApplyButton ) {
			
			panel2 = new JPanel();
			panel2.setLayout( new BoxLayout( panel2, BoxLayout.X_AXIS ) );
			panel.add( panel2 );
			
			applyButton = new JButton( "Apply" );
			panel2.add( Box.createHorizontalGlue() );
			panel2.add( applyButton );
		}
		else
			panel.add( new JLabel() );
		
		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.NORTH);
	}
	
	@Override
	public void actionPerformed( ActionEvent e ) {
		if( e.getSource() == overrideBox ) {
			connField.setEditable(overrideBox.isSelected());
			loginField.setEditable(overrideBox.isSelected());
			passwdField.setEditable(overrideBox.isSelected());
		}
		
		if (e.getSource() == chooseButton) {			
			if( overrideBox.isSelected() ) {				
			    JFileChooser chooser = new JFileChooser(); 
			    chooser.setCurrentDirectory(new java.io.File(connField.getText()));
			    chooser.setDialogTitle("Choose folder of database");
			    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    chooser.setAcceptAllFileFilterUsed(false);
			    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
			    	connField.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
			
		}
	}
	
	public boolean getOverride() {
		return overrideBox.isSelected();
	}
	
	public void setOverride( boolean override ) {
		
		if( override != overrideBox.isSelected() )
			overrideBox.doClick();
	}
	
	public void setFilename( String filename ) {
		connField.setText( filename );
	}
	
	public void setLogin( String login ) {
		loginField.setText( login );
	}
	
	public void setPasswd( String passwd ) {
		passwdField.setText( passwd );
	}
	
	public boolean isOverride() {return overrideBox.isSelected();}
	public String getFilename() {return connField.getText();}
	public String getLogin() {return loginField.getText();}
	public String getPasswd() {return String.valueOf(passwdField.getPassword());}
	
	public JButton getApplyButton() {return applyButton;}
	
    public void saveSettingsTo(Config c) {
    	c.addString(PARAM_FILENAME, connField.getText());
    	c.addString(PARAM_LOGIN, loginField.getText());
    	c.addString(PARAM_PASSWD, String.valueOf(passwdField.getPassword()));
    	c.addBoolean(PARAM_OVERRIDE, overrideBox.isSelected());
    }	
	public void setSettings(Config c) throws InvalidSettingsException {		
		connField.setText(c.getString(PARAM_FILENAME));
		loginField.setText(c.getString(PARAM_LOGIN));
		passwdField.setText(c.getString(PARAM_PASSWD));
		overrideBox.setSelected(c.getBoolean(PARAM_OVERRIDE));
		connField.setEditable(overrideBox.isSelected());
		loginField.setEditable(overrideBox.isSelected());
		passwdField.setEditable(overrideBox.isSelected());
	}
}
