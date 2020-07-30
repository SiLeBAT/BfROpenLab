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
/*
 * Created by JFormDesigner on Tue Feb 19 09:47:43 CET 2013
 */

package de.bund.bfr.knime.openkrise.db.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import de.bund.bfr.knime.openkrise.db.DBKernel;

/**
 * @author Armin Weiser
 */
public class ChangeMasterPass extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1161242299677836250L;
	public ChangeMasterPass(Frame owner) {
		super(owner);
		initComponents();
		this.setIconImage(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Database.gif")).getImage());
		String sa = DBKernel.getTempSA(DBKernel.HSHDB_PATH);
		textField1.setText(sa);
		passwordField1.requestFocus();
	}

	private void okButtonActionPerformed(ActionEvent e) {
		String newSA = textField1.getText();
		String newPass = String.valueOf(passwordField1.getPassword());
		if (newPass.equals(String.valueOf(passwordField2.getPassword()))) {
			String ibText = "";
			String oldSA = DBKernel.getTempSA(DBKernel.HSHDB_PATH);
			boolean success = false;
			if (!newSA.equals(oldSA)) {
				if (DBKernel.sendRequest("CREATE USER " + DBKernel.delimitL(newSA) + " PASSWORD '" + newPass + "' ADMIN", false)) {
					if (DBKernel.getUsername().equals(DBKernel.getTempSA(DBKernel.HSHDB_PATH))) {
				  		DBKernel.closeDBConnections(false);
						if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) DBKernel.myDBi.establishDBConnection(newSA, newPass);
						else DBKernel.mainFrame.getMyList().getMyDBTable().initConn(newSA, newPass);
					}
					if (DBKernel.sendRequest("DROP USER " + DBKernel.delimitL(DBKernel.getTempSA(DBKernel.HSHDB_PATH)), false)) {
						if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) DBKernel.myDBi.setSA_P(newSA, newPass);
						else DBKernel.removeAdminInfo(DBKernel.HSHDB_PATH);
						success = true;
					}
				}
			}
			else if (DBKernel.sendRequest("ALTER USER " + DBKernel.delimitL(newSA) + " SET PASSWORD '" + newPass + "';", false)) {
				success = true;
			}
			if (success) {
				ibText = "Successful Change!";
				if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) DBKernel.myDBi.setSA_P(newSA, newPass);
			}
			else ibText = "Couldn't change password...";
			InfoBox ib = new InfoBox(this, ibText, true, new Dimension(300, 200), null, true);
			ib.setVisible(true);
			if (success) dispose();
		}
		else {
			InfoBox ib = new InfoBox(this, "Passwords don't match...", true, new Dimension(300, 200), null, true);
			ib.setVisible(true);
		}
	}

	private void cancelButtonActionPerformed(ActionEvent e) {
		dispose();
	}

	private void checkBox1ActionPerformed(ActionEvent e) {
		textField1.setEnabled(checkBox1.isSelected());
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		label1 = new JLabel();
		textField1 = new JTextField();
		checkBox1 = new JCheckBox();
		label2 = new JLabel();
		passwordField1 = new JPasswordField();
		label3 = new JLabel();
		passwordField2 = new JPasswordField();
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();

		//======== this ========
		setTitle("Change Master");
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG);
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new FormLayout(
					"default, $lcgap, default:grow, $lcgap, default",
					"2*(default, $lgap), default"));

				//---- label1 ----
				label1.setText("New Master Username:");
				contentPanel.add(label1, CC.xy(1, 1));

				//---- textField1 ----
				textField1.setColumns(20);
				textField1.setEnabled(false);
				contentPanel.add(textField1, CC.xy(3, 1));

				//---- checkBox1 ----
				checkBox1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						checkBox1ActionPerformed(e);
					}
				});
				contentPanel.add(checkBox1, CC.xy(5, 1));

				//---- label2 ----
				label2.setText("New Master Password:");
				contentPanel.add(label2, CC.xy(1, 3));
				contentPanel.add(passwordField1, CC.xywh(3, 3, 3, 1));

				//---- label3 ----
				label3.setText("Retype Password:");
				contentPanel.add(label3, CC.xy(1, 5));
				contentPanel.add(passwordField2, CC.xywh(3, 5, 3, 1));
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
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JLabel label1;
	private JTextField textField1;
	private JCheckBox checkBox1;
	private JLabel label2;
	private JPasswordField passwordField1;
	private JLabel label3;
	private JPasswordField passwordField2;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
