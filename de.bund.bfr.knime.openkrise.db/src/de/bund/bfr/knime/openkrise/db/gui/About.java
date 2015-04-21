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
 * Created by JFormDesigner on Thu Jul 01 14:24:49 CEST 2010
 */

package de.bund.bfr.knime.openkrise.db.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import de.bund.bfr.knime.openkrise.db.DBKernel;

/**
 * @author Armin Weiser
 */
public class About extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public About() {
		initComponents();
		label4.setText("Copyright \u00a9 2014 BfR");
		String ver = About.class.getPackage().getImplementationVersion();
		label2.setText("Version " + (ver == null ? DBKernel.softwareVersion : ver)); //"TP-100701");
		label4.setVisible(false);
	}

	private void okButtonActionPerformed(ActionEvent e) {
		this.dispose();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		ResourceBundle bundle = ResourceBundle.getBundle("de.bund.bfr.knime.openkrise.db.gui.PanelProps");
		DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		label1 = compFactory.createLabel("");
		label3 = new JLabel();
		label2 = new JLabel();
		label4 = new JLabel();
		buttonBar = new JPanel();
		okButton = new JButton();

		//======== this ========
		setTitle(bundle.getString("About.this.title"));
		setModal(true);
		setResizable(false);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG);
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new FormLayout(
					"default:grow",
					"fill:default:grow, 4*($lgap, default)"));

				//---- label1 ----
				label1.setIcon(new ImageIcon(getClass().getResource("/de/bund/bfr/knime/openkrise/db/gui/res/Database.gif")));
				label1.setPreferredSize(new Dimension(180, 170));
				label1.setMaximumSize(new Dimension(255, 240));
				label1.setMinimumSize(new Dimension(128, 120));
				label1.setHorizontalAlignment(SwingConstants.CENTER);
				contentPanel.add(label1, CC.xy(1, 1, CC.FILL, CC.FILL));

				//---- label3 ----
				label3.setText(bundle.getString("About.label3.text"));
				label3.setHorizontalAlignment(SwingConstants.LEFT);
				label3.setFont(new Font("Dotum", Font.BOLD, 20));
				contentPanel.add(label3, CC.xy(1, 3));

				//---- label2 ----
				label2.setText(bundle.getString("About.label2.text"));
				label2.setHorizontalAlignment(SwingConstants.LEFT);
				contentPanel.add(label2, CC.xy(1, 5));

				//---- label4 ----
				label4.setText(bundle.getString("About.label4.text"));
				label4.setHorizontalAlignment(SwingConstants.LEFT);
				contentPanel.add(label4, CC.xy(1, 9));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_PAD);
				buttonBar.setLayout(new FormLayout(
					"$glue, $button",
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
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		setSize(210, 310);
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JLabel label1;
	private JLabel label3;
	private JLabel label2;
	private JLabel label4;
	private JPanel buttonBar;
	private JButton okButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
