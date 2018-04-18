/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
/**
 * 
 */
package de.bund.bfr.knime.openkrise.db.gui.dbtable.header;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.event.MouseInputAdapter;

/**
 * @author Armin
 * http://www.jroller.com/santhosh/entry/make_jtable_resiable_better_than
 */
public class TableRowHeaderResizer extends MouseInputAdapter { 
	
  private static Cursor resizeCursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR); 

  private int mouseYOffset, resizingRow; 
  private Cursor otherCursor = resizeCursor; 
  private JTable jTable;
  private JTable bigTable;
  
  public TableRowHeaderResizer(JTable rowHeader, JTable bigTable) { 
    jTable = rowHeader; 
    this.bigTable = bigTable; 
    jTable.addMouseListener(this); 
    jTable.addMouseMotionListener(this); 
  } 

  private int getResizingRow(Point p){ 
      return getResizingRow(p, jTable.rowAtPoint(p)); 
  } 

  private int getResizingRow(Point p, int row){ 
      if(row == -1){ 
          return -1; 
      } 
      int col = jTable.columnAtPoint(p); 
      if(col==-1) 
          return -1; 
      Rectangle r = jTable.getCellRect(row, col, true); 
      r.grow(0, -3); 
      if(r.contains(p)) 
          return -1; 

      int midPoint = r.y + r.height / 2; 
      int rowIndex = (p.y < midPoint) ? row - 1 : row; 

      return rowIndex; 
  } 

  public void mousePressed(MouseEvent e){ 
      Point p = e.getPoint(); 

      resizingRow = getResizingRow(p); 
      mouseYOffset = p.y - jTable.getRowHeight(resizingRow); 
  } 

  private void swapCursor(){ 
      Cursor tmp = jTable.getCursor(); 
      jTable.setCursor(otherCursor); 
      otherCursor = tmp; 
  } 

  public void mouseMoved(MouseEvent e){ 
      if ((getResizingRow(e.getPoint())>=0) != (jTable.getCursor() == resizeCursor)){ 
          swapCursor(); 
      } 
  } 

  public void mouseDragged(MouseEvent e){ 
      int mouseY = e.getY(); 

      if (resizingRow >= 0) { 
          int newHeight = mouseY - mouseYOffset; 
          if (newHeight > 0) {
          	jTable.setRowHeight(resizingRow, newHeight);
          	bigTable.setRowHeight(resizingRow, newHeight);
          }
      } 
  } 
} 
