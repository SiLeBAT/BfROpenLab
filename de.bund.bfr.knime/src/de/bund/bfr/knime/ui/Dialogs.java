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
package de.bund.bfr.knime.ui;

import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class Dialogs {

	public static enum Result {
		YES, NO, OK, CANCEL
	}

	public static void showErrorMessage(Component parent, String message, String title) {
		showMessage(parent, message, title, true);
	}

	public static void showWarningMessage(Component parent, String message, String title) {
		showMessage(parent, message, title, false);
	}

	private static void showMessage(Component parent, String message, String title, boolean error) {
		Window owner = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
		Locale oldLocale = JComponent.getDefaultLocale();

		if (!(owner instanceof KnimeDialog) && !hasKnimeDialogAncestor(owner)) {
			setDialogButtonsEnabled(false);
		}

		JComponent.setDefaultLocale(Locale.US);
		JOptionPane.showMessageDialog(parent, message, title,
				error ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE);
		JComponent.setDefaultLocale(oldLocale);

		if (!(owner instanceof KnimeDialog) && !hasKnimeDialogAncestor(owner)) {
			setDialogButtonsEnabled(true);
		}
	}

	public static Result showYesNoDialog(Component parent, String message, String title) {
		return showConfirmDialog(parent, message, title, true);
	}

	public static Result showOkCancelDialog(Component parent, String message, String title) {
		return showConfirmDialog(parent, message, title, false);
	}

	private static Result showConfirmDialog(Component parent, String message, String title, boolean yesNo) {
		Window owner = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
		Locale oldLocale = JComponent.getDefaultLocale();

		if (!(owner instanceof KnimeDialog) && !hasKnimeDialogAncestor(owner)) {
			setDialogButtonsEnabled(false);
		}

		JComponent.setDefaultLocale(Locale.US);
		int result = JOptionPane.showConfirmDialog(parent, message, title,
				yesNo ? JOptionPane.YES_NO_CANCEL_OPTION : JOptionPane.OK_CANCEL_OPTION);
		JComponent.setDefaultLocale(oldLocale);

		if (!(owner instanceof KnimeDialog) && !hasKnimeDialogAncestor(owner)) {
			setDialogButtonsEnabled(true);
		}

		switch (result) {
		case JOptionPane.YES_OPTION:
			return yesNo ? Result.YES : Result.OK;
		case JOptionPane.NO_OPTION:
			return Result.NO;
		}

		return Result.CANCEL;
	}

	public static boolean hasKnimeDialogAncestor(Window w) {
		Window owner = w.getOwner();

		if (owner instanceof KnimeDialog) {
			return true;
		} else if (owner == null) {
			return false;
		}

		return hasKnimeDialogAncestor(owner);
	}

	public static void setDialogButtonsEnabled(final boolean enabled) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				for (Shell dialog : shell.getShells()) {
					if (dialog.isVisible()) {
						for (Button b : getButtons(dialog)) {
							b.setEnabled(enabled);
						}
					}
				}
			}
		});
	}

	private static List<Button> getButtons(Composite panel) {
		List<Button> buttons = new ArrayList<>();

		for (Control c : panel.getChildren()) {
			if (c instanceof Button) {
				buttons.add((Button) c);
			} else if (c instanceof Composite) {
				buttons.addAll(getButtons((Composite) c));
			}
		}

		return buttons;
	}

	public static File showImageFileChooser(Component parent) {
		Window owner = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
		Locale oldLocale = JComponent.getDefaultLocale();

		if (!(owner instanceof KnimeDialog) && !hasKnimeDialogAncestor(owner)) {
			setDialogButtonsEnabled(false);
		}

		JComponent.setDefaultLocale(Locale.US);
		ImageFileChooser chooser = new ImageFileChooser();
		File result = null;

		if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			result = chooser.getImageFile();
		}

		JComponent.setDefaultLocale(oldLocale);

		if (!(owner instanceof KnimeDialog) && !hasKnimeDialogAncestor(owner)) {
			setDialogButtonsEnabled(true);
		}

		return result;
	}

	private static class ImageFileChooser extends JFileChooser {

		private static final long serialVersionUID = 1L;

		private static final FileFilter PNG_FILTER = new FileNameExtensionFilter("Portable Network Graphics (*.png)",
				"png");
		private static final FileFilter SVG_FILTER = new FileNameExtensionFilter("SVG Vector Graphic (*.svg)", "svg");

		public ImageFileChooser() {
			setAcceptAllFileFilterUsed(false);
			addChoosableFileFilter(PNG_FILTER);
			addChoosableFileFilter(SVG_FILTER);
		}

		public File getImageFile() {
			File file = getSelectedFile();

			if (getFileFilter() == PNG_FILTER) {
				if (!file.getName().toLowerCase().endsWith(".png")) {
					file = new File(file.getAbsolutePath() + ".png");
				}
			} else if (getFileFilter() == SVG_FILTER) {
				if (!file.getName().toLowerCase().endsWith(".svg")) {
					file = new File(file.getAbsolutePath() + ".svg");
				}
			}

			return file;
		}

		@Override
		public void approveSelection() {
			File f = getSelectedFile();

			if (f.exists() && getDialogType() == SAVE_DIALOG
					&& JOptionPane.showConfirmDialog(this,
							"The file " + f.getName() + " already exists. Do you want to overwrite it?", "Confirm",
							JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
				return;
			}

			super.approveSelection();
		}
	}
}
