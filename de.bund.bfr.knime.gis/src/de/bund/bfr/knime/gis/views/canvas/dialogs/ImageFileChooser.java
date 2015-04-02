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
package de.bund.bfr.knime.gis.views.canvas.dialogs;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ImageFileChooser extends JFileChooser {

	public static enum Format {
		PNG_FORMAT, SVG_FORMAT
	}

	private static final long serialVersionUID = 1L;

	private FileFilter pngFilter;
	private FileFilter svgFilter;

	public ImageFileChooser() {
		pngFilter = new FileNameExtensionFilter(
				"Portable Network Graphics (*.png)", "png");
		svgFilter = new FileNameExtensionFilter("SVG Vector Graphic (*.svg)",
				"svg");

		setAcceptAllFileFilterUsed(false);
		addChoosableFileFilter(pngFilter);
		addChoosableFileFilter(svgFilter);
	}

	public File getImageFile() {
		File file = getSelectedFile();

		if (getFileFormat() == Format.PNG_FORMAT) {
			if (!file.getName().toLowerCase().endsWith(".png")) {
				file = new File(file.getAbsolutePath() + ".png");
			}
		} else if (getFileFormat() == Format.SVG_FORMAT) {
			if (!file.getName().toLowerCase().endsWith(".svg")) {
				file = new File(file.getAbsolutePath() + ".svg");
			}
		}

		return file;
	}

	public Format getFileFormat() {
		FileFilter filter = getFileFilter();

		if (filter == pngFilter) {
			return Format.PNG_FORMAT;
		} else if (filter == svgFilter) {
			return Format.SVG_FORMAT;
		}

		return null;
	}

	@Override
	public void approveSelection() {
		File f = getSelectedFile();
		String message = "The file " + f.getName()
				+ " already exists. Do you want to overwrite it?";

		if (f.exists()
				&& getDialogType() == SAVE_DIALOG
				&& JOptionPane.showConfirmDialog(this, message,
						"Existing file", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
			return;
		}

		super.approveSelection();
	}
}
