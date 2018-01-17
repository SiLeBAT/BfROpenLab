/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.io.IOException;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

public class SimSearchTableRowTransferHandler  extends TransferHandler {
  
    /**
	 * 
	 */
	private static final long serialVersionUID = 401344497399864471L;
	
	private SimSearchTable simSearchTable;
    private final DataFlavor dataFlavor;

    protected SimSearchTableRowTransferHandler(SimSearchTable simSearchTable) {
      super();
      this.dataFlavor = new ActivationDataFlavor(int[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of row indices.");
      this.simSearchTable = simSearchTable;
    }
    
    @Override protected Transferable createTransferable(JComponent c) {
      JTable table = (JTable) c;
      //SimSearch.SimSearchTableModel model = (SimSearch.SimSearchTableModel) table.getModel();
      //List<Integer> list = new ArrayList<>();
      int[] indices = table.getSelectedRows();
//      for (int i : indices) {
//        list.add(model.getDataVector().get(i));
//      }
      //Object[] transferedObjects = list.toArray();
      return new DataHandler(indices, dataFlavor.getMimeType());
      //return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
    }
    @Override public boolean canImport(TransferHandler.TransferSupport info) {
      SimSearchJTable table = (SimSearchJTable) info.getComponent();
      
      boolean isDropable = info.isDrop() && info.isDataFlavorSupported(dataFlavor);
      if(isDropable) {
        JTable.DropLocation dropLocation = (JTable.DropLocation) info.getDropLocation();
        if(!dropLocation.isInsertColumn()) {
          
          try {
            int[] rowsDragged = (int[]) info.getTransferable().getTransferData(this.dataFlavor);
            if(dropLocation.isInsertRow()) {
              //isDropable = this.simSearchTable.areRowsMovableTo((int[]) info.getTransferable().getTransferData(dataFlavor), dropLocation.getRow());
              isDropable = this.simSearchTable.isRowMoveValid(rowsDragged, dropLocation.getRow());
            } else {
              isDropable = this.simSearchTable.isMergeValid(rowsDragged, dropLocation.getRow());
            }
          } catch (UnsupportedFlavorException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            isDropable = false;
          } catch (IOException e) {
            // TODO Auto-generated catch block
            isDropable = false;
          }
        }
      }
      
      table.setCursor(isDropable ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
      //table.setCursor(isDropable ? DragSource.DefaultCopyDrop : DragSource.DefaultCopyNoDrop);
      return isDropable;
    }
    @Override public int getSourceActions(JComponent c) {
      return TransferHandler.MOVE;
      //return TransferHandler.COPY_OR_MOVE;
        //return DnDConstants.ACTION_MOVE;
      //return TransferHandler.COPY;
    }
    @Override public boolean importData(TransferHandler.TransferSupport info) {
      if (!canImport(info)) {
        return false;
      }
      //TransferHandler.DropLocation dropLocation = info.getDropLocation();
      if (!(info.getDropLocation() instanceof JTable.DropLocation)) {
        return false;
      }
      boolean dataImported = false;
      JTable.DropLocation dropLocation = (JTable.DropLocation) info.getDropLocation();
      if(dropLocation.isInsertRow()) {
        // move rows
      } else if(dropLocation.isInsertColumn()) {
        // do nothing 
      } else {
        int[] rowsToMerge;
        try {
          rowsToMerge = (int[]) info.getTransferable().getTransferData(this.dataFlavor);
          this.simSearchTable.mergeRows(rowsToMerge, dropLocation.getRow());
          dataImported = true;
        } catch (UnsupportedFlavorException e) {
          // TODO Auto-generated catch block
          //e.printStackTrace();
          e.printStackTrace();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      
      
//      JTable.DropLocation dl = (JTable.DropLocation) tdl;
//      JTable target = (JTable) info.getComponent();
////      System.out.println("getUserDropAction(): " + info.getUserDropAction());
////      System.out.println("getSourceDropActions(): " + info.getSourceDropActions());
////      System.out.println("getDropAction(): " + info.getDropAction());
//      System.out.println("getDropLocation(): " + info.getDropLocation());
////      if(info.getDropAction()==TransferHandler.MOVE) {
////        System.out.println("Move row");
////      } else if(info.getDropAction()==TransferHandler.COPY) {
////        System.out.println("Merge row");
////      } else {
////        System.out.println("Undecided operation");
////      }
      info.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      //target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      return dataImported;
//      return true;
//      DefaultTableModel model = (DefaultTableModel) target.getModel();
//      int index = dl.getRow();
//      int max = model.getRowCount();
//      if (index < 0 || index > max) {
//        index = max;
//      }
//      addIndex = index;
//      target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//      try {
//        Object[] values = (Object[]) info.getTransferable().getTransferData(localObjectFlavor);
//        addCount = values.length;
//        for (int i = 0; i < values.length; i++) {
//          int idx = index++;
//          model.insertRow(idx, (Vector) values[i]);
//          target.getSelectionModel().addSelectionInterval(idx, idx);
//        }
//        return true;
//      } catch (UnsupportedFlavorException | IOException ex) {
//        ex.printStackTrace();
//      }
//      return false;
    }
    @Override protected void exportDone(JComponent c, Transferable data, int action) {
      //System.out.println("Export done");
      //cleanup(c, action == TransferHandler.MOVE);
    }

//    //If the remove argument is true, the drop has been
//    //successful and it's time to remove the selected items
//    //from the list. If the remove argument is false, it
//    //was a Copy operation and the original list is left
//    //intact.
//    protected void cleanup(JComponent c, boolean remove) {
//      if (remove && indices != null) {
//        c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//        DefaultTableModel model = (DefaultTableModel)((JTable) c).getModel();
//        //If we are moving items around in the same list, we
//        //need to adjust the indices accordingly, since those
//        //after the insertion point have moved.
//        if (addCount > 0) {
//          for (int i = 0; i < indices.length; i++) {
//            if (indices[i] >= addIndex) {
//              indices[i] += addCount;
//            }
//          }
//        }
//        for (int i = indices.length - 1; i >= 0; i--) {
//          model.removeRow(indices[i]);
//        }
//      }
//      indices  = null;
//      addCount = 0;
//      addIndex = -1;
//    }
}