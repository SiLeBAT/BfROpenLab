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
import java.awt.Component;
import java.awt.Insets;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;


public class TableRowHeaderRenderer extends DefaultTableCellRenderer { //  implements ListCellRenderer
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Border noFocusBorder, focusBorder;

    public TableRowHeaderRenderer() {
        setOpaque(true);
        setBorder(noFocusBorder);
    }

    public void updateUI() {
        super.updateUI();
        Border cell = UIManager.getBorder("TableHeader.cellBorder");
        Border focus = UIManager.getBorder("Table.focusCellHighlightBorder");

        focusBorder = new BorderUIResource.CompoundBorderUIResource(cell, focus);
 
        Insets i = focus.getBorderInsets(this);

        noFocusBorder = new BorderUIResource.CompoundBorderUIResource(cell, BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right));

        /* Alternatively, if focus shouldn't be supported:

        focusBorder = noFocusBorder = cell;
    
        */
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {

    	JLabel comp = (JLabel) super.getTableCellRendererComponent(table, value, selected, focused, row, column);

    	if (table != null) {
            comp.setBackground(table.getBackground());
            comp.setForeground(table.getForeground());
            comp.setFont(table.getFont());
            comp.setEnabled(true);
        }
        else {
        	comp.setBackground(UIManager.getColor("TableHeader.background"));
        	comp.setForeground(UIManager.getColor("TableHeader.foreground"));
        	comp.setFont(UIManager.getFont("TableHeader.font"));
        	comp.setEnabled(false); // true
        }
        
        if (focused) comp.setBorder(focusBorder);
        else comp.setBorder(noFocusBorder);

        comp.setText(value.toString());
        comp.setHorizontalAlignment(CENTER);
 
        return comp;
    }
}
