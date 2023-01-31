/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

public class KnimeDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public KnimeDialog(Component parent, String title, Dialog.ModalityType modalityType) {
		super(parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent), title,
				modalityType);
		addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
				if (!Dialogs.hasKnimeDialogAncestor(KnimeDialog.this)) {
					Dialogs.setDialogButtonsEnabled(false);
				}
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				if (!Dialogs.hasKnimeDialogAncestor(KnimeDialog.this)) {
					Dialogs.setDialogButtonsEnabled(true);
				}
			}

			@Override
			public void windowClosed(WindowEvent e) {
				if (!Dialogs.hasKnimeDialogAncestor(KnimeDialog.this)) {
					Dialogs.setDialogButtonsEnabled(true);
				}
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
	}
}
