/*******************************************************************************
 * Copyright (c) 2013 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
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
package de.bund.bfr.knime.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;


public class FilePanel extends JPanel implements ActionListener, TextListener {

	public static final int OPEN_DIALOG = 1;
	public static final int SAVE_DIALOG = 2;

	private static final long serialVersionUID = 1L;

	private List<FileListener> listeners;

	private int dialogType;
	private boolean acceptAllFiles;
	private List<FileFilter> fileFilters;

	private JButton button;
	private StringTextField field;

	public FilePanel(String name, int dialogType) {
		this.dialogType = dialogType;
		acceptAllFiles = true;
		fileFilters = new ArrayList<FileFilter>();

		listeners = new ArrayList<FileListener>();

		button = new JButton("Browse...");
		button.addActionListener(this);
		field = new StringTextField(false, 0);
		field.addTextListener(this);

		JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout(5, 5));
		panel.add(field, BorderLayout.CENTER);
		panel.add(button, BorderLayout.EAST);

		setBorder(BorderFactory.createTitledBorder(name));
		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
	}

	public void addFileListener(FileListener listener) {
		listeners.add(listener);
	}

	public void removeFileListener(FileListener listener) {
		listeners.remove(listener);
	}

	public void setFileName(String fileName) {
		field.setValue(fileName);
	}

	public String getFileName() {
		String fileName = field.getValue();

		if (fileName != null) {
			fileName = fileName.trim();

			if (fileName.isEmpty()) {
				fileName = null;
			}
		}

		return fileName;
	}

	public boolean isAcceptAllFiles() {
		return acceptAllFiles;
	}

	public void setAcceptAllFiles(boolean acceptAllFiles) {
		this.acceptAllFiles = acceptAllFiles;
	}

	public void addFileFilter(StandardFileFilter filter) {
		fileFilters.add(filter);
	}

	public boolean isFileNameValid() {
		String fileName = getFileName();

		if (dialogType == OPEN_DIALOG) {
			// TODO test if file exists
			return fileName != null;
		} else if (dialogType == SAVE_DIALOG) {
			return fileName != null;
		}

		return false;
	}

	private void fireFileChanged() {
		for (FileListener listener : listeners) {
			listener.fileChanged(this);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser;

		try {
			fileChooser = new JFileChooser(new File(field.getValue()));
		} catch (Exception ex) {
			fileChooser = new JFileChooser();
		}

		fileChooser.setAcceptAllFileFilterUsed(acceptAllFiles);

		for (FileFilter filter : fileFilters) {
			fileChooser.addChoosableFileFilter(filter);
		}

		if (dialogType == OPEN_DIALOG) {
			if (fileChooser.showOpenDialog(button) == JFileChooser.APPROVE_OPTION) {
				field.setValue(fileChooser.getSelectedFile().getAbsolutePath());
			}
		} else if (dialogType == SAVE_DIALOG) {
			if (fileChooser.showSaveDialog(button) == JFileChooser.APPROVE_OPTION) {
				String fileName = fileChooser.getSelectedFile()
						.getAbsolutePath();

				if (!fileName.toLowerCase().endsWith(".csv")) {
					fileName += ".csv";
				}

				field.setValue(fileName);
			}
		}
	}

	@Override
	public void textChanged(Object source) {
		fireFileChanged();
	}

	public static interface FileListener {

		public void fileChanged(FilePanel source);
	}

}
