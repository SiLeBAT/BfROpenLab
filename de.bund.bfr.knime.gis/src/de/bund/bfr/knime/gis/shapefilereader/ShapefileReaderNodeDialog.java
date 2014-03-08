/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
package de.bund.bfr.knime.gis.shapefilereader;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import de.bund.bfr.knime.KnimeUtilities;
import de.bund.bfr.knime.UI;

public class ShapefileReaderNodeDialog extends NodeDialogPane implements
		ActionListener, DocumentListener {

	private ShapefileReaderSettings set;

	private JTextField fileField;
	private JButton fileButton;
	private JLabel systemLabel;
	private JTextField systemField;

	public ShapefileReaderNodeDialog() {
		set = new ShapefileReaderSettings();

		fileField = new JTextField();
		fileField.getDocument().addDocumentListener(this);
		fileButton = new JButton("Browse...");
		fileButton.addActionListener(this);
		systemLabel = new JLabel();
		systemField = new JTextField();

		JPanel innerFilePanel = new JPanel();

		innerFilePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		innerFilePanel.setLayout(new BorderLayout(5, 5));
		innerFilePanel.add(fileField, BorderLayout.CENTER);
		innerFilePanel.add(fileButton, BorderLayout.EAST);

		JPanel outerFilePanel = new JPanel();

		outerFilePanel.setBorder(BorderFactory.createTitledBorder("Shapefile"));
		outerFilePanel.setLayout(new BorderLayout());
		outerFilePanel.add(innerFilePanel, BorderLayout.CENTER);

		JPanel innerSystemPanel = new JPanel();

		innerSystemPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		innerSystemPanel.setLayout(new BorderLayout(5, 5));
		innerSystemPanel.add(systemLabel, BorderLayout.WEST);
		innerSystemPanel.add(systemField, BorderLayout.CENTER);

		JPanel outerSystemPanel = new JPanel();

		outerSystemPanel.setBorder(BorderFactory
				.createTitledBorder("Coordinate Reference System"));
		outerSystemPanel.setLayout(new BorderLayout());
		outerSystemPanel.add(innerSystemPanel, BorderLayout.CENTER);

		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(outerFilePanel);
		panel.add(outerSystemPanel);

		addTab("Options", UI.createNorthPanel(panel));
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings,
			DataTableSpec[] specs) throws NotConfigurableException {
		set.loadSettings(settings);
		fileField.setText(set.getFileName() != null ? set.getFileName() : "");
		systemField.setText(set.getSystemCode() != null ? set.getSystemCode()
				: "");
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		if (systemField.isEditable()) {
			set.setSystemCode(systemField.getText());
		} else {
			set.setSystemCode(null);
		}

		set.setFileName(fileField.getText());
		set.saveSettings(settings);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser;

		try {
			fileChooser = new JFileChooser(new File(fileField.getText()));
		} catch (Exception ex) {
			fileChooser = new JFileChooser();
		}

		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return "Shapefile (*.shp)";
			}

			@Override
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".shp");
			}
		});

		if (fileChooser.showOpenDialog(fileButton) == JFileChooser.APPROVE_OPTION) {
			fileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		updateSystemPanel();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		updateSystemPanel();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateSystemPanel();
	}

	private void updateSystemPanel() {
		try {
			File shpFile = KnimeUtilities.getFile(fileField.getText());

			try {
				KnimeUtilities.getFile(FilenameUtils.removeExtension(shpFile
						.getAbsolutePath()) + ".prj");
				systemLabel.setText("Code is read from Prj file");
				systemField.setEditable(false);
			} catch (FileNotFoundException e) {
				systemLabel
						.setText("Prj file not found: please enter code manually");
				systemField.setEditable(true);
			}
		} catch (FileNotFoundException e) {
			systemLabel.setText("Please enter filename first");
			systemField.setEditable(false);
		}

		((JPanel) systemLabel.getParent()).revalidate();
	}
}
