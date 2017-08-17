/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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
 * Created by JFormDesigner on Tue Dec 18 15:46:21 CET 2012
 */

package de.bund.bfr.knime.openkrise.ui.handlers;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.util.Locale;
import java.util.zip.CRC32;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.gui.Login;

/**
 * @author Armin Weiser
 */
public class SettingsDialog extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8433737081879655528L;

	public SettingsDialog() {
		initComponents();
		//this.setIconImage(Resources.getInstance().getDefaultIcon());
		this.username.setVisible(false);
		this.password.setVisible(false);
		this.readOnly.setVisible(false);
		this.button2.setVisible(false);
		this.label2.setVisible(false);
		this.label3.setVisible(false);
		this.label4.setVisible(false);
		fillFields();
	}

	private void fillFields() {
		String idbp = DBKernel.prefs.get("FC_LAB_SETTINGS_DB_PATH", DBKernel.prefs.get("PMM_LAB_SETTINGS_DB_PATH", DBKernel.getInternalDefaultDBPath()));
		dbPath.setText(idbp);
		CRC32 crc32 = new CRC32();
		crc32.update(idbp.getBytes());
		long crc32Out = crc32.getValue();
		username.setText(DBKernel.prefs.get("FC_LAB_SETTINGS_DB_USERNAME" + crc32Out, DBKernel.prefs.get("PMM_LAB_SETTINGS_DB_USERNAME" + crc32Out, "SA")));
		password.setText(DBKernel.prefs.get("FC_LAB_SETTINGS_DB_PASSWORD" + crc32Out, DBKernel.prefs.get("PMM_LAB_SETTINGS_DB_PASSWORD" + crc32Out, "")));
		readOnly.setSelected(DBKernel.prefs.getBoolean("FC_LAB_SETTINGS_DB_RO" + crc32Out, DBKernel.prefs.getBoolean("PMM_LAB_SETTINGS_DB_RO" + crc32Out, false)));
		username.setEnabled(false);
		password.setEnabled(false);
	}

	private void button1ActionPerformed(ActionEvent e) {
		Locale oldLocale = JComponent.getDefaultLocale();
		JComponent.setDefaultLocale(Locale.US);
		JFileChooser chooser = new JFileChooser();
		JComponent.setDefaultLocale(oldLocale);
		chooser.setCurrentDirectory(new java.io.File(dbPath.getText()));
		chooser.setDialogTitle("Choose folder of database");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			dbPath.setText(chooser.getSelectedFile().getAbsolutePath());
		} else {
			MyLogger.handleMessage("No Selection ");
		}

	}

	private void okButtonActionPerformed(ActionEvent e) {
		String dbt = dbPath.getText();
		boolean isServer = DBKernel.isHsqlServer(dbt);
		if (!isServer && !dbt.endsWith(System.getProperty("file.separator"))) {
			dbt += System.getProperty("file.separator");
		}
		if (hasChanged(dbt, username.getText(), String.valueOf(password.getPassword()), readOnly.isSelected())) {
			DBKernel.prefs.put("FC_LAB_SETTINGS_DB_PATH", dbt);
			CRC32 crc32 = new CRC32();
			crc32.update(dbt.getBytes());
			long crc32Out = crc32.getValue();
			DBKernel.prefs.put("FC_LAB_SETTINGS_DB_USERNAME" + crc32Out, username.getText());
			DBKernel.prefs.put("FC_LAB_SETTINGS_DB_PASSWORD" + crc32Out, String.valueOf(password.getPassword()));
			DBKernel.prefs.putBoolean("FC_LAB_SETTINGS_DB_RO" + crc32Out, readOnly.isSelected());
			DBKernel.prefs.prefsFlush();
			DBKernel.closeDBConnections(true);

			try {
				DBKernel.mainFrame = null;
				new Login(dbt, username.getText(), String.valueOf(password.getPassword()), readOnly.isSelected(), true);
				//Bfrdb db = new Bfrdb(dbt + (isServer ? "" : "DB"), username.getText(), String.valueOf(password.getPassword()));
				//Connection conn = db.getConnection();//DBKernel.getLocalConn(true);
				//DBKernel.setLocalConn(conn, dbt, username.getText(), String.valueOf(password.getPassword()));
				if (!isServer) DBKernel.getUP(dbt);
				Connection conn = DBKernel.getLocalConn(true);
				if (conn != null) {
					//DBKernel.createGui(conn);
					DBKernel.mainFrame.getMyList().getMyDBTable().initConn(conn);
					DBKernel.mainFrame.getMyList().getMyDBTable().setTable();
					//DBKernel.myList.setSelection("Matrices");
					//DBKernel.myList.setSelection(DBKernel.prefs.get("LAST_SELECTED_TABLE", "Versuchsbedingungen"));
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		username.setEnabled(false);
		password.setEnabled(false);
		this.dispose();
	}

	private boolean hasChanged(String dbt, String username, String password, boolean isRO) {
		CRC32 crc32 = new CRC32();
		crc32.update(dbt.getBytes());
		long crc32Out = crc32.getValue();
		return !DBKernel.prefs.get("FC_LAB_SETTINGS_DB_PATH", DBKernel.prefs.get("PMM_LAB_SETTINGS_DB_PATH", "")).equals(dbt) || !DBKernel.prefs.get("FC_LAB_SETTINGS_DB_USERNAME" + crc32Out, DBKernel.prefs.get("PMM_LAB_SETTINGS_DB_USERNAME" + crc32Out, "")).equals(username)
				|| !DBKernel.prefs.get("FC_LAB_SETTINGS_DB_PASSWORD" + crc32Out, DBKernel.prefs.get("PMM_LAB_SETTINGS_DB_PASSWORD" + crc32Out, "")).equals(password)
				|| DBKernel.prefs.getBoolean("FC_LAB_SETTINGS_DB_RO" + crc32Out, DBKernel.prefs.getBoolean("PMM_LAB_SETTINGS_DB_RO" + crc32Out, false)) != isRO;
	}

	private void cancelButtonActionPerformed(ActionEvent e) {
		username.setEnabled(false);
		password.setEnabled(false);
		this.dispose();
	}

	private void button2ActionPerformed(ActionEvent e) {
		username.setEnabled(true);
		password.setEnabled(true);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		label1 = new JLabel();
		dbPath = new JTextField();
		button1 = new JButton();
		label3 = new JLabel();
		username = new JTextField();
		label4 = new JLabel();
		password = new JPasswordField();
		button2 = new JButton();
		label2 = new JLabel();
		readOnly = new JCheckBox();
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();

		//======== this ========
		setTitle("Settings");
		setAlwaysOnTop(true);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG);
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new FormLayout("2*(default, $lcgap), default", "4*(default, $lgap), default"));

				//---- label1 ----
				label1.setText("DB Path:");
				contentPanel.add(label1, CC.xy(1, 1));

				//---- dbPath ----
				dbPath.setColumns(50);
				contentPanel.add(dbPath, CC.xy(3, 1));

				//---- button1 ----
				button1.setText("...");
				button1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						button1ActionPerformed(e);
					}
				});
				contentPanel.add(button1, CC.xy(5, 1));

				//---- label3 ----
				label3.setText("Username:");
				contentPanel.add(label3, CC.xy(1, 3));
				contentPanel.add(username, CC.xywh(3, 3, 3, 1));

				//---- label4 ----
				label4.setText("Password:");
				contentPanel.add(label4, CC.xy(1, 5));
				contentPanel.add(password, CC.xywh(3, 5, 3, 1));

				//---- button2 ----
				button2.setText("Change Username/password");
				button2.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						button2ActionPerformed(e);
					}
				});
				contentPanel.add(button2, CC.xy(3, 7));

				//---- label2 ----
				label2.setText("DB Read-only:");
				label2.setVisible(false);
				contentPanel.add(label2, CC.xy(1, 9));

				//---- readOnly ----
				readOnly.setVisible(false);
				contentPanel.add(readOnly, CC.xywh(3, 9, 3, 1));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_PAD);
				buttonBar.setLayout(new FormLayout("$glue, $button, $rgap, $button", "pref"));

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
	// Generated using JFormDesigner non-commercial license
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JLabel label1;
	private JTextField dbPath;
	private JButton button1;
	private JLabel label3;
	private JTextField username;
	private JLabel label4;
	private JPasswordField password;
	private JButton button2;
	private JLabel label2;
	private JCheckBox readOnly;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;

	// JFormDesigner - End of variables declaration  //GEN-END:variables

	class FolderFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			File f = new File(dir.getAbsolutePath() + File.separator + name);
			System.err.println(name + "\t" + (f.exists() && f.isDirectory()));
			return f.exists() && f.isDirectory();
		}
	}
}
