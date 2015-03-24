/*******************************************************************************
 * PMM-Lab © 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab © 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * Christian Thöns (BfR)
 * Matthias Filter (BfR)
 * Armin A. Weiser (BfR)
 * Alexander Falenski (BfR)
 * Jörgen Brandt (BfR)
 * Annemarie Käsbohrer (BfR)
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
/*
 * Created by JFormDesigner on Wed Dec 14 16:43:06 CET 2011
 */

package org.hsh.bfr.db.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.hsh.bfr.db.gui.dbtable.header.GuiMessages;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

/**
 * @author Armin Weiser
 */
public class PlausibleDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2854403990155736490L;
	public boolean okPressed = false;
	public PlausibleDialog(Frame owner) {
		super(owner);
		okPressed = false;
		initComponents();
	}
	
	private void okButtonActionPerformed(ActionEvent e) {
		okPressed = true;
		dispose();
	}

	private void cancelButtonActionPerformed(ActionEvent e) {
		okPressed = false;
		dispose();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		radioButton1 = new JRadioButton();
		passwordField1 = new JPasswordField();
		radioButton2 = new JRadioButton();
		radioButton3 = new JRadioButton();
		textField1 = new JTextField();
		radioButton4 = new JRadioButton();
		checkBox1 = new JCheckBox();
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();

		//======== this ========
		setTitle(GuiMessages.getString("Welche Datensaetze sollen einer Plausibilitaetspruefung unterzogen werden?"));
		setModal(true);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG);
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new FormLayout(
					"default, $lcgap, default:grow",
					"4*(default, $lgap), default"));

				//---- radioButton1 ----
				radioButton1.setText(GuiMessages.getString("Alle"));
				radioButton1.setSelected(true);
				contentPanel.add(radioButton1, CC.xy(1, 1));
				contentPanel.add(passwordField1, CC.xy(3, 1));

				//---- radioButton2 ----
				radioButton2.setText(GuiMessages.getString("nur sichtbare Tabelle"));
				contentPanel.add(radioButton2, CC.xy(1, 3));

				//---- radioButton3 ----
				radioButton3.setText(GuiMessages.getString("nur folgende IDs der sichtbaren Tabelle:"));
				contentPanel.add(radioButton3, CC.xy(1, 5));

				//---- textField1 ----
				textField1.setToolTipText("z.B. 23-28");
				contentPanel.add(textField1, CC.xy(3, 5));

				//---- radioButton4 ----
				radioButton4.setText(GuiMessages.getString("nur selektierer Eintrag in der sichtbaren Tabelle"));
				contentPanel.add(radioButton4, CC.xy(1, 7));

				//---- checkBox1 ----
				checkBox1.setText(GuiMessages.getString("nur Datensaetze des angemeldeten Benutzers anzeigen"));
				checkBox1.setSelected(true);
				contentPanel.add(checkBox1, CC.xywh(1, 9, 3, 1));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_PAD);
				buttonBar.setLayout(new FormLayout(
					"$glue, $button, $rgap, $button",
					"pref"));

				//---- okButton ----
				okButton.setText("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						okButtonActionPerformed(e);
					}
				});
				buttonBar.add(okButton, CC.xy(2, 1));

				//---- cancelButton ----
				cancelButton.setText("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cancelButtonActionPerformed(e);
					}
				});
				buttonBar.add(cancelButton, CC.xy(4, 1));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		setSize(480, 235);
		setLocationRelativeTo(getOwner());

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(radioButton1);
		buttonGroup1.add(radioButton2);
		buttonGroup1.add(radioButton3);
		buttonGroup1.add(radioButton4);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel dialogPane;
	private JPanel contentPanel;
	public JRadioButton radioButton1;
	private JPasswordField passwordField1;
	public JRadioButton radioButton2;
	public JRadioButton radioButton3;
	public JTextField textField1;
	public JRadioButton radioButton4;
	public JCheckBox checkBox1;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
