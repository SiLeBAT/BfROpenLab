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
package de.uni_heidelberg.ifi.pvs.knime.octavescriptnode.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoManager;

import org.testng.annotations.Test;

/**
 * 
 * @author André-Patrick Bubel <code@andre-bubel.de>
 * 
 */
public class UndoRedoKeyListenerTest {

	public class TestUndoableEdit extends AbstractUndoableEdit {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4149225815122668598L;

	}

	@Test
	public void allConstructorTest() throws IOException {
		UndoManager undoManager = new UndoManager();

		undoManager.addEdit(new TestUndoableEdit());
		assertThat("Can undo", undoManager.canUndo(), equalTo(true));
		assertThat("Can redo", undoManager.canRedo(), equalTo(false));

		// TODO: Test simulated key presses, maybe a terstframework for SWING is
		// needed
		// UndoRedoKeyListener listener = new UndoRedoKeyListener(undoManager);
		// listener.keyTyped(new KeyEvent(null, 0, 0, KeyEvent.CTRL_DOWN_MASK,
		// KeyEvent.VK_Y, 'y'));

		// assertThat("Can undo", undoManager.canUndo(), equalTo(false));
		// assertThat("Can redo", undoManager.canRedo(), equalTo(true));
	}

}
