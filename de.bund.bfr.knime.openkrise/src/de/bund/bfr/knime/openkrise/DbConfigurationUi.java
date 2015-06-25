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
package de.bund.bfr.knime.openkrise;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.config.Config;

public class DbConfigurationUi extends JPanel implements ActionListener {

	private static final long serialVersionUID = 20120622;

	public static final String PARAM_FILENAME = "filename";
	public static final String PARAM_OVERRIDE = "override";

	private JCheckBox overrideBox;
	private JTextField connField;
	private JButton chooseButton;

	public DbConfigurationUi() {
		JPanel mainPanel, panel, panel2, panel0;

		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		panel0 = new JPanel();
		panel0.setLayout(new BorderLayout());

		mainPanel.add(panel0, BorderLayout.SOUTH);

		panel = new JPanel();
		panel.setLayout(new GridLayout(0, 1));
		panel0.add(panel, BorderLayout.WEST);

		panel.add(new JLabel("Database : "));

		panel = new JPanel();
		panel.setLayout(new GridLayout(0, 1));
		panel0.add(panel, BorderLayout.CENTER);

		overrideBox = new JCheckBox("Use external database");
		overrideBox.addActionListener(this);
		mainPanel.add(overrideBox, BorderLayout.CENTER);

		panel2 = new JPanel();
		panel2.setLayout(new BorderLayout());
		panel.add(panel2);

		connField = new JTextField();
		connField.setEditable(false);
		panel2.add(connField, BorderLayout.CENTER);

		chooseButton = new JButton("...");
		chooseButton.addActionListener(this);
		panel2.add(chooseButton, BorderLayout.EAST);
		panel2.add(new JLabel("jdbc:hsqldb:file:"), BorderLayout.WEST);

		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.NORTH);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == overrideBox) {
			connField.setEditable(overrideBox.isSelected());
		}

		if (e.getSource() == chooseButton) {
			if (overrideBox.isSelected()) {
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

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		overrideBox.setEnabled(enabled);
		connField.setEnabled(enabled);
		chooseButton.setEnabled(enabled);
	}

	public boolean getOverride() {
		return overrideBox.isSelected();
	}

	public void setOverride(boolean override) {

		if (override != overrideBox.isSelected())
			overrideBox.doClick();
	}

	public void setFilename(String filename) {
		if (filename != null) {
			if (filename.endsWith("/DB"))
				connField.setText(filename.substring(0, filename.length() - 3));
			else
				connField.setText(filename);
		}
	}

	public boolean isOverride() {
		return overrideBox.isSelected();
	}

	public String getFilename() {
		return connField.getText() + "/DB";
	}

	public void saveSettingsTo(Config c) {
		c.addString(PARAM_FILENAME, connField.getText());
		c.addBoolean(PARAM_OVERRIDE, overrideBox.isSelected());
	}

	public void setSettings(Config c) throws InvalidSettingsException {
		connField.setText(c.getString(PARAM_FILENAME));
		overrideBox.setSelected(c.getBoolean(PARAM_OVERRIDE));
		connField.setEditable(overrideBox.isSelected());
	}
}
