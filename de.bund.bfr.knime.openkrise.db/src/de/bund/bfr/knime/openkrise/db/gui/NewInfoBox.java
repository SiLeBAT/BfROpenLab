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
/**
 * 
 */
package de.bund.bfr.knime.openkrise.db.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import de.bund.bfr.knime.openkrise.db.DBKernel;

public class NewInfoBox extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel dialogPane;
	private JEditorPane infoTextArea;
	private JPanel buttonBar;
	private JButton okButton;

	private String inhalt;

	public static void show(String title, String inhalt, Dimension dim) {
		new NewInfoBox(title, inhalt, dim);
	}

	public NewInfoBox(String title, String inhalt, Dimension dim) {
		super(DBKernel.mainFrame, title, true);

		Point loc = DBKernel.mainFrame.getLocation();
		Dimension siz = DBKernel.mainFrame.getSize();

		this.inhalt = inhalt;
		this.setResizable(false);
		this.setSize(dim);
		this.setLocation(loc.x + (siz.width - this.getWidth()) / 2, loc.y + (siz.height - this.getHeight()) / 2);
		initComponents();
		infoTextArea.setEditable(false);
		setVisible(true);
	}

	private void initComponents() {
		infoTextArea = new JEditorPane();
		infoTextArea.setContentType("text/html");

		HTMLEditorKit kit = new HTMLEditorKit();

		kit.getStyleSheet().addRule("* { font-family:monospace; font-size:10px; }");
		kit.getStyleSheet().addRule("#warning { color:orange; }");
		kit.getStyleSheet().addRule("#error { color:red; }");
		kit.getStyleSheet().addRule("h1 { font-size:12px; }");
		kit.getStyleSheet().addRule("h2 { font-size:11px; }");
		kit.getStyleSheet().addRule("h1, h2 { margin-top:0px; margin-bottom:0px; }");

		infoTextArea.setEditorKit(kit);
		infoTextArea.setText(inhalt);
		infoTextArea.setCaretPosition(0);

		CellConstraints cc = new CellConstraints();

		okButton = new JButton();
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		okButton.setText("OK");

		buttonBar = new JPanel();
		buttonBar.add(okButton, cc.xy(2, 1));
		buttonBar.setBorder(Borders.BUTTON_BAR_PAD);
		buttonBar.setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.GLUE_COLSPEC, FormSpecs.BUTTON_COLSPEC },
				RowSpec.decodeSpecs("pref")));

		dialogPane = new JPanel();
		dialogPane.setBorder(Borders.DIALOG);
		dialogPane.setLayout(new BorderLayout());
		dialogPane.add(new JScrollPane(infoTextArea), BorderLayout.CENTER);
		dialogPane.add(buttonBar, BorderLayout.SOUTH);

		Container contentPane = getContentPane();

		contentPane.setLayout(new BorderLayout());
		contentPane.add(dialogPane, BorderLayout.CENTER);
	}
}
