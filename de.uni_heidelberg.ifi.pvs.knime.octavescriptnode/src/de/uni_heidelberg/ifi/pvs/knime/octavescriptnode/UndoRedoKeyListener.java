/*
 * OctaveScriptNode - A KNIME node that runs Octave scripts
 * Copyright (C) 2011 Andre-Patrick Bubel (pvs@andre-bubel.de) and
 *                    Parallel and Distributed Systems Group (PVS),
 *                    University of Heidelberg, Germany
 * Website: http://pvs.ifi.uni-heidelberg.de/
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
 */
package de.uni_heidelberg.ifi.pvs.knime.octavescriptnode;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.undo.UndoManager;

/**
 * A KeyListener for AWT KeyEvents, that invoke undo and redo commands on a
 * SWING UndoManager
 */
public final class UndoRedoKeyListener implements KeyListener {
	private UndoManager undoManager;

	public UndoRedoKeyListener(UndoManager undoManager) {
		this.undoManager = undoManager;
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
			if (undoManager.canUndo())
				undoManager.undo();
		} else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Y) {
			if (undoManager.canRedo())
				undoManager.redo();
		}
	}
}