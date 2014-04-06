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
package de.bund.bfr.knime.gis.views.canvas.dialogs;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import de.bund.bfr.knime.ui.StandardFileFilter;

public class ImageFileChooser extends JFileChooser {

	public static int NO_FORMAT = -1;
	public static int PNG_FORMAT = 0;
	public static int SVG_FORMAT = 1;

	private static final long serialVersionUID = 1L;

	private FileFilter pngFilter;
	private FileFilter svgFilter;

	public ImageFileChooser() {
		pngFilter = new StandardFileFilter(".png",
				"Portable Network Graphics (*.png)");
		svgFilter = new StandardFileFilter(".svg", "SVG Vector Graphic (*.svg)");

		setAcceptAllFileFilterUsed(false);
		addChoosableFileFilter(pngFilter);
		addChoosableFileFilter(svgFilter);
	}

	public File getImageFile() {
		File file = getSelectedFile();

		if (getFileFormat() == PNG_FORMAT) {
			if (!file.getName().toLowerCase().endsWith(".png")) {
				file = new File(file.getAbsolutePath() + ".png");
			}
		} else if (getFileFormat() == SVG_FORMAT) {
			if (!file.getName().toLowerCase().endsWith(".svg")) {
				file = new File(file.getAbsolutePath() + ".svg");
			}
		}

		return file;
	}

	public int getFileFormat() {
		FileFilter filter = getFileFilter();

		if (filter == pngFilter) {
			return PNG_FORMAT;
		} else if (filter == svgFilter) {
			return SVG_FORMAT;
		}

		return NO_FORMAT;
	}
}
