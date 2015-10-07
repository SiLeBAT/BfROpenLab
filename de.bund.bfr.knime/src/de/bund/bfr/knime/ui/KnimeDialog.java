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

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class KnimeDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public KnimeDialog(Window owner, String title, Dialog.ModalityType modalityType) {
		super(owner, title, modalityType);
		addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
				if (!(getOwner() instanceof KnimeDialog)) {
					setDialogButtonsEnabled(false);
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
			}

			@Override
			public void windowClosed(WindowEvent e) {
				if (!(getOwner() instanceof KnimeDialog)) {
					setDialogButtonsEnabled(true);
				}
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
	}

	private static void setDialogButtonsEnabled(final boolean enabled) {
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
}
