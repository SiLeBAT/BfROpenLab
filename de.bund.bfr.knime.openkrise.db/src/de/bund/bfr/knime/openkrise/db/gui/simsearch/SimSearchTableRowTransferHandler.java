/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db.gui.simsearch;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

public class SimSearchTableRowTransferHandler  extends TransferHandler {

	private static final long serialVersionUID = 401344497399864471L;

	private SimSearchTable simSearchTable;
	private final DataFlavor dataFlavor;


	protected SimSearchTableRowTransferHandler(SimSearchTable simSearchTable) {
		super();
		this.dataFlavor = new ActivationDataFlavor(int[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of row indices.");
		this.simSearchTable = simSearchTable;
	}

	@Override protected Transferable createTransferable(JComponent c) {
		if(c instanceof SimSearchJTable) {
			SimSearchJTable table = (SimSearchJTable) c;
			int[] indices = table.getSelectedRows();
			return new DataHandler(indices, dataFlavor.getMimeType());
		}
		return null;

	}

	@Override public boolean canImport(TransferHandler.TransferSupport info) {
		SimSearchJTable table = (SimSearchJTable) info.getComponent();

		boolean isDropable = info.isDrop() && info.isDataFlavorSupported(dataFlavor);
		if(isDropable) {
			JTable.DropLocation dropLocation = (JTable.DropLocation) info.getDropLocation();
			if(!dropLocation.isInsertColumn()) {

				int[] rowsDragged = table.getSelectedRows(); 

				if(dropLocation.isInsertRow()) {
					isDropable = this.simSearchTable.isRowMoveValid(rowsDragged, dropLocation.getRow());
				} else {
					isDropable = this.simSearchTable.isMergeValid(rowsDragged, dropLocation.getRow());
				}

			}
		}

		table.setCursor(isDropable ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);

		return isDropable;
	}

	@Override public int getSourceActions(JComponent c) {
		return TransferHandler.MOVE;
	}

	@Override public boolean importData(TransferHandler.TransferSupport info) {
		boolean dataImported = false;
		if(canImport(info)) { 

			if ((info.getDropLocation() instanceof JTable.DropLocation)) {


				JTable.DropLocation dropLocation = (JTable.DropLocation) info.getDropLocation();
				if(!dropLocation.isInsertColumn()) { 

					int[] rowsDragged = ((JTable) info.getComponent()).getSelectedRows();

					if(dropLocation.isInsertRow())  this.simSearchTable.moveRows(rowsDragged, dropLocation.getRow());
					else this.simSearchTable.mergeRows(rowsDragged, dropLocation.getRow());

					dataImported = true;

				}
			}
		}

		info.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

		return dataImported;

	}

	@Override protected void exportDone(JComponent c, Transferable data, int action) {
		//The cursor is reseted not directly in here because if the drag and drop is aborted in some cases 
		//(e.g. the mouse is released over a row that does not accept the drop) the direct mouse cursor reset 
		//is either ignored or set cursor is set again by the processing of an event still waiting in the event queue
		EventQueue.invokeLater(new Runnable() {
			@Override public void run() {
				c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				((SimSearchJTable) c).getPartnerTable().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});

	}

}