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
/**
 * 
 */
package de.bund.bfr.knime.openkrise.db.gui.dbtable.header;

/**
 * @author Armin
 *
 */
import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;

public class MyTableHeaderCellRenderer extends DefaultTableCellRenderer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Color myBgColor;
	private String tooltip;
	//private MyDBTable dbTable;
  private Icon ascIcon = UIManager.getIcon("Table.ascendingSortIcon");
  private Icon descIcon = UIManager.getIcon("Table.descendingSortIcon");
	
  public MyTableHeaderCellRenderer(MyDBTable dbTable, Color bgColor, String tooltip) {
    super();
    //this.dbTable = dbTable;
    this.myBgColor = bgColor;
    this.tooltip = (tooltip == null ? null : GuiMessages.getString(tooltip).replaceAll("\n", "<BR>"));
  }

  @Override
public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
  	
  	JLabel comp = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

  	comp.setBackground(myBgColor);
    comp.setHorizontalAlignment(JLabel.CENTER);
    
    if (tooltip == null) {
		comp.setToolTipText(GuiMessages.getString(value.toString()));
	}
    else {
    	comp.setToolTipText("<HTML>"+tooltip+"</HTML>");
	}
    
    comp.setIcon(null);
    /*
    if (dbTable != null) {
    	Vector<Integer> v = dbTable.getSortColumns();
    	if (v != null && v.size() > 0 && v.get(0) == column + 1) {
    		comp.setIcon(dbTable.isSortAscending() ? ascIcon : descIcon);
    		comp.repaint();
    	}
    }
    */
    if (table != null && table.getRowSorter() != null) { //  && myBgColor != dbTable.getTable().getTableHeader().getBackground()
    	if (table.getRowSorter().getSortKeys().size() > 0) {
    		SortKey sk = table.getRowSorter().getSortKeys().get(0);
    		int sortCol = sk.getColumn();
    		//System.out.println(comp + "\tgetSortColumns\t" + sortCol);
	      	if (column + 1 == sortCol) {
	      		sk.getSortOrder();
				comp.setIcon(sk.getSortOrder() == SortOrder.ASCENDING ? ascIcon : descIcon);
	      	}    		
    	}
    }
    //comp.setText("W");
    comp.setText(GuiMessages.getString(comp.getText()));
    return comp;
  }

}
