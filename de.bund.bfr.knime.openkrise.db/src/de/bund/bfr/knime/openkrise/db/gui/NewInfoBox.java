/*******************************************************************************
 * Copyright (c) 2014-2025 German Federal Institute for Risk Assessment (BfR)
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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.text.html.HTMLEditorKit;

import de.bund.bfr.knime.UI;

public class NewInfoBox extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final String[] DEFAULT_OPTIONS = new String[] {"OK"};
	private int result = -1;

	public static void show(Frame owner, String title, String text) {
		show(owner, title, text, DEFAULT_OPTIONS, 0);
	}
	
	public static int show(Frame owner, String title, String text, String[] options, int defaultOptionIndex) {
		NewInfoBox infoBox = new NewInfoBox(owner, title, text, options, defaultOptionIndex);
		return infoBox.result;
	}

	private NewInfoBox(Frame owner, String title, String text, String[] options, int defaultOptionIndex) {
		super(owner, title, true);

		JEditorPane infoTextArea = new JEditorPane();
		HTMLEditorKit kit = new HTMLEditorKit();

		kit.getStyleSheet().addRule("* { font-family:monospace; font-size:10px; }");
		kit.getStyleSheet().addRule("#warning { color:orange; }");
		kit.getStyleSheet().addRule("#error { color:red; }");
		kit.getStyleSheet().addRule("h1 { font-size:12px; }");
		kit.getStyleSheet().addRule("h2 { font-size:11px; }");
		kit.getStyleSheet().addRule("h1, h2 { margin-top:0px; margin-bottom:0px; }");

		infoTextArea.setContentType("text/html");
		infoTextArea.setEditorKit(kit);
		infoTextArea.setText(text);
		infoTextArea.setCaretPosition(0);
		infoTextArea.setEditable(false);

		List<JButton> optionButtons = new ArrayList<>();
		
		for (int i = 0; i < options.length; i++) {
			JButton optionButton = new JButton(options[i]);
			final int optionButtonResult = i;
			optionButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					result = optionButtonResult;
					dispose();
				}
			});
			optionButtons.add(optionButton);
		}

		setLayout(new BorderLayout());
		if (options.length > 1) this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		add(new JScrollPane(infoTextArea), BorderLayout.CENTER);
		add(UI.createEastPanel(UI.createHorizontalPanel(optionButtons.toArray(new JButton[0]))), BorderLayout.SOUTH);
		pack();
		adjustDialog(this, 0.8, 0.8);
		setLocationRelativeTo(owner);
		getRootPane().setDefaultButton(optionButtons.get(defaultOptionIndex));
		setVisible(true);
	}

	private static void adjustDialog(JDialog dialog, double widthFraction, double heightFraction) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(dialog.getGraphicsConfiguration());
		int maxWidth = (int) ((screenSize.width - insets.left - insets.right) * widthFraction);
		int maxHeight = (int) ((screenSize.height - insets.top - insets.bottom) * heightFraction);

		dialog.setSize(Math.min(dialog.getWidth(), maxWidth), Math.min(dialog.getHeight(), maxHeight));

		int minX = insets.left;
		int minY = insets.top;
		int maxX = screenSize.width - insets.right - dialog.getWidth();
		int maxY = screenSize.height - insets.bottom - dialog.getHeight();

		dialog.setLocation(Math.max(dialog.getX(), minX), Math.max(dialog.getY(), minY));
		dialog.setLocation(Math.min(dialog.getX(), maxX), Math.min(dialog.getY(), maxY));
	}
}
