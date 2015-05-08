/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
 * Created by JFormDesigner on Tue Feb 25 00:30:50 CET 2014
 */

package de.bund.bfr.knime.openkrise.db.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

/**
 * @author Armin Weiser
 */
public class PlausibleDialog4Krise extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4250627311147927549L;
	public boolean okPressed = false;
	public PlausibleDialog4Krise(Frame owner) {
		super(owner);
		okPressed = false;
		initComponents();
		selS.setVisible(false);
		label20.setVisible(false);
		gentle.setVisible(false);
		ld.setVisible(false);
		lb.setVisible(false);
		pi.setVisible(false);
		label6.setVisible(false);
		label14.setVisible(false);
		label18.setVisible(false);
		sv.setVisible(false);
		label19.setVisible(false);
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
		label1 = new JLabel();
		cs = new JCheckBox();
		label5 = new JLabel();
		sn = new JSpinner();
		lz = new JLabel();
		sz = new JSpinner();
		label7 = new JLabel();
		ss = new JSpinner();
		label8 = new JLabel();
		snum = new JSpinner();
		label9 = new JLabel();
		sc = new JSpinner();
		label19 = new JLabel();
		sv = new JSpinner();
		label2 = new JLabel();
		cp = new JCheckBox();
		label10 = new JLabel();
		ps = new JSpinner();
		label11 = new JLabel();
		pd = new JSpinner();
		label6 = new JLabel();
		pi = new JSpinner();
		label3 = new JLabel();
		cl = new JCheckBox();
		label12 = new JLabel();
		la = new JSpinner();
		label13 = new JLabel();
		ll = new JSpinner();
		label14 = new JLabel();
		lb = new JSpinner();
		label18 = new JLabel();
		ld = new JSpinner();
		label4 = new JLabel();
		cd = new JCheckBox();
		label15 = new JLabel();
		dl = new JSpinner();
		label16 = new JLabel();
		dd = new JSpinner();
		label17 = new JLabel();
		dr = new JSpinner();
		label20 = new JLabel();
		selS = new JCheckBox();
		gentle = new JCheckBox();
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();

		//======== this ========
		setTitle("SimSearch Options");
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
					"13*(default, $lcgap), default",
					"5*(default, $lgap), default"));
				((FormLayout)contentPanel.getLayout()).setColumnGroups(new int[][] {{5, 9, 13, 17, 21, 25}, {7, 11, 15, 19, 23, 27}});

				//---- label1 ----
				label1.setText("Station:");
				contentPanel.add(label1, CC.xy(1, 1));
				contentPanel.add(cs, CC.xy(3, 1));

				//---- label5 ----
				label5.setText("Name:");
				label5.setHorizontalAlignment(SwingConstants.RIGHT);
				contentPanel.add(label5, CC.xy(5, 1));

				//---- sn ----
				sn.setModel(new SpinnerNumberModel(3, 0, null, 1));
				contentPanel.add(sn, CC.xy(7, 1));

				//---- lz ----
				lz.setText("Zip:");
				lz.setHorizontalAlignment(SwingConstants.RIGHT);
				contentPanel.add(lz, CC.xy(9, 1));

				//---- sz ----
				sz.setModel(new SpinnerNumberModel(1, 0, null, 1));
				contentPanel.add(sz, CC.xy(11, 1));

				//---- label7 ----
				label7.setText("Street:");
				label7.setHorizontalAlignment(SwingConstants.RIGHT);
				contentPanel.add(label7, CC.xy(13, 1));

				//---- ss ----
				ss.setModel(new SpinnerNumberModel(3, 0, null, 1));
				contentPanel.add(ss, CC.xy(15, 1));

				//---- label8 ----
				label8.setText("Number:");
				label8.setHorizontalAlignment(SwingConstants.RIGHT);
				contentPanel.add(label8, CC.xy(17, 1));

				//---- snum ----
				snum.setModel(new SpinnerNumberModel(1, 0, null, 1));
				contentPanel.add(snum, CC.xy(19, 1));

				//---- label9 ----
				label9.setText("City:");
				label9.setHorizontalAlignment(SwingConstants.RIGHT);
				contentPanel.add(label9, CC.xy(21, 1));

				//---- sc ----
				sc.setModel(new SpinnerNumberModel(3, 0, null, 1));
				contentPanel.add(sc, CC.xy(23, 1));

				//---- label19 ----
				label19.setText("VATnumber:");
				contentPanel.add(label19, CC.xy(25, 1));

				//---- sv ----
				sv.setModel(new SpinnerNumberModel(0, 0, null, 1));
				contentPanel.add(sv, CC.xy(27, 1));

				//---- label2 ----
				label2.setText("Product:");
				contentPanel.add(label2, CC.xy(1, 3));
				contentPanel.add(cp, CC.xy(3, 3));

				//---- label10 ----
				label10.setText("Station:");
				label10.setHorizontalAlignment(SwingConstants.RIGHT);
				contentPanel.add(label10, CC.xy(5, 3));

				//---- ps ----
				ps.setModel(new SpinnerNumberModel(0, 0, 0, 1));
				ps.setEnabled(false);
				contentPanel.add(ps, CC.xy(7, 3));

				//---- label11 ----
				label11.setText("Name:");
				label11.setHorizontalAlignment(SwingConstants.RIGHT);
				contentPanel.add(label11, CC.xy(9, 3));

				//---- pd ----
				pd.setModel(new SpinnerNumberModel(3, 0, null, 1));
				contentPanel.add(pd, CC.xy(11, 3));

				//---- label6 ----
				label6.setText("Item number:");
				contentPanel.add(label6, CC.xy(13, 3));

				//---- pi ----
				pi.setModel(new SpinnerNumberModel(0, 0, null, 1));
				contentPanel.add(pi, CC.xy(15, 3));

				//---- label3 ----
				label3.setText("Lot:");
				contentPanel.add(label3, CC.xy(1, 5));
				contentPanel.add(cl, CC.xy(3, 5));

				//---- label12 ----
				label12.setText("Article:");
				label12.setHorizontalAlignment(SwingConstants.RIGHT);
				contentPanel.add(label12, CC.xy(5, 5));

				//---- la ----
				la.setModel(new SpinnerNumberModel(0, 0, 0, 1));
				la.setEnabled(false);
				contentPanel.add(la, CC.xy(7, 5));

				//---- label13 ----
				label13.setText("LotNo:");
				label13.setHorizontalAlignment(SwingConstants.RIGHT);
				contentPanel.add(label13, CC.xy(9, 5));

				//---- ll ----
				ll.setModel(new SpinnerNumberModel(1, 0, null, 1));
				contentPanel.add(ll, CC.xy(11, 5));

				//---- label14 ----
				label14.setText("BestBefore:");
				label14.setHorizontalAlignment(SwingConstants.RIGHT);
				contentPanel.add(label14, CC.xy(13, 5));

				//---- lb ----
				lb.setModel(new SpinnerNumberModel(0, 0, null, 1));
				contentPanel.add(lb, CC.xy(15, 5));

				//---- label18 ----
				label18.setText("DateManufactor:");
				contentPanel.add(label18, CC.xy(17, 5));

				//---- ld ----
				ld.setModel(new SpinnerNumberModel(0, 0, null, 1));
				contentPanel.add(ld, CC.xy(19, 5));

				//---- label4 ----
				label4.setText("Delivery:");
				contentPanel.add(label4, CC.xy(1, 7));
				contentPanel.add(cd, CC.xy(3, 7));

				//---- label15 ----
				label15.setText("Lot:");
				label15.setHorizontalAlignment(SwingConstants.RIGHT);
				contentPanel.add(label15, CC.xy(5, 7));

				//---- dl ----
				dl.setModel(new SpinnerNumberModel(0, 0, 0, 1));
				dl.setEnabled(false);
				contentPanel.add(dl, CC.xy(7, 7));

				//---- label16 ----
				label16.setText("DeliveryDate:");
				label16.setHorizontalAlignment(SwingConstants.RIGHT);
				contentPanel.add(label16, CC.xy(9, 7));

				//---- dd ----
				dd.setModel(new SpinnerNumberModel(0, 0, null, 1));
				contentPanel.add(dd, CC.xy(11, 7));

				//---- label17 ----
				label17.setText("Recipient:");
				label17.setHorizontalAlignment(SwingConstants.RIGHT);
				contentPanel.add(label17, CC.xy(13, 7));

				//---- dr ----
				dr.setModel(new SpinnerNumberModel(0, 0, 0, 1));
				dr.setEnabled(false);
				contentPanel.add(dr, CC.xy(15, 7));

				//---- label20 ----
				label20.setText("Check sel. S");
				contentPanel.add(label20, CC.xy(1, 9));
				contentPanel.add(selS, CC.xy(3, 9));

				//---- gentle ----
				gentle.setText("gentle");
				gentle.setSelected(true);
				gentle.setToolTipText("NULL values are not handled as different");
				contentPanel.add(gentle, CC.xy(1, 11));
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
	public JCheckBox cs;
	private JLabel label5;
	public JSpinner sn;
	private JLabel lz;
	public JSpinner sz;
	private JLabel label7;
	public JSpinner ss;
	private JLabel label8;
	public JSpinner snum;
	private JLabel label9;
	public JSpinner sc;
	private JLabel label19;
	public JSpinner sv;
	private JLabel label2;
	public JCheckBox cp;
	private JLabel label10;
	public JSpinner ps;
	private JLabel label11;
	public JSpinner pd;
	private JLabel label6;
	public JSpinner pi;
	private JLabel label3;
	public JCheckBox cl;
	private JLabel label12;
	public JSpinner la;
	private JLabel label13;
	public JSpinner ll;
	private JLabel label14;
	public JSpinner lb;
	private JLabel label18;
	public JSpinner ld;
	private JLabel label4;
	public JCheckBox cd;
	private JLabel label15;
	public JSpinner dl;
	private JLabel label16;
	public JSpinner dd;
	private JLabel label17;
	public JSpinner dr;
	private JLabel label20;
	public JCheckBox selS;
	public JCheckBox gentle;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
