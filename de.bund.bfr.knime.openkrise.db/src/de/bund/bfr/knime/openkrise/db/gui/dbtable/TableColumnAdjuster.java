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
package de.bund.bfr.knime.openkrise.db.gui.dbtable;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/*
 *	Class to manage the widths of colunmns in a table.
 *
 *  Various properties control how the width of the column is calculated.
 *  Another property controls whether column width calculation should be dynamic.
 *  Finally, various Actions will be added to the table to allow the user
 *  to customize the functionality.
 *
 *  This class was designed to be used with tables that use an auto resize mode
 *  of AUTO_RESIZE_OFF. With all other modes you are constrained as the width
 *  of the columns must fit inside the table. So if you increase one column, one
 *  or more of the other columns must decrease. Because of this the resize mode
 *  of RESIZE_ALL_COLUMNS will work the best.
 */
class TableColumnAdjuster implements PropertyChangeListener, TableModelListener
{
	private JTable table;
	private int spacing;
	private boolean isColumnHeaderIncluded;
	private boolean isColumnDataIncluded;
	private boolean isOnlyAdjustLarger;
	private boolean isDynamicAdjustment;
	private Map<TableColumn, Integer> columnSizes = new HashMap<>();

	/*
	 *  Specify the table and use default spacing
	 */
	TableColumnAdjuster(JTable table)
	{
		this(table, 6);
	}

	/*
	 *  Specify the table and spacing
	 */
	private TableColumnAdjuster(JTable table, int spacing)
	{
		this.table = table;
		this.spacing = spacing;
		setColumnHeaderIncluded( true );
		setColumnDataIncluded( true );
		setOnlyAdjustLarger( true );
		setDynamicAdjustment( false );
		installActions();
	}

	/*
	 *  Adjust the widths of all the columns in the table
	 */
	void adjustColumns()
	{
		TableColumnModel tcm = table.getColumnModel();

		for (int i = 0; i < tcm.getColumnCount(); i++)
		{
			adjustColumn(i);
		}
	}

	/*
	 *  Adjust the width of the specified column in the table
	 */
	private void adjustColumn(final int column)
	{
		TableColumn tableColumn = table.getColumnModel().getColumn(column);

		if (! tableColumn.getResizable()) return;

		int columnHeaderWidth = getColumnHeaderWidth( column );
		int columnDataWidth   = getColumnDataWidth( column );
		int preferredWidth    = Math.max(columnHeaderWidth, columnDataWidth);

		updateTableColumn(column, preferredWidth);
	}

	/*
	 *  Calculated the width based on the column name
	 */
	private int getColumnHeaderWidth(int column)
	{
		if (! isColumnHeaderIncluded) return 0;

		TableColumn tableColumn = table.getColumnModel().getColumn(column);
		Object value = tableColumn.getHeaderValue();
		TableCellRenderer renderer = tableColumn.getHeaderRenderer();

		if (renderer == null)
		{
			renderer = table.getTableHeader().getDefaultRenderer();
		}

		Component c = renderer.getTableCellRendererComponent(table, value, false, false, -1, column);
		return c.getPreferredSize().width + 50;
	}

	/*
	 *  Calculate the width based on the widest cell renderer for the
	 *  given column.
	 */
	private int getColumnDataWidth(int column)
	{
		if (! isColumnDataIncluded) return 0;

		int preferredWidth = 0;
		int maxWidth = table.getColumnModel().getColumn(column).getMaxWidth();

		for (int row = 0; row < table.getRowCount(); row++)
		{
    		preferredWidth = Math.max(preferredWidth, getCellDataWidth(row, column));

			//  We've exceeded the maximum width, no need to check other rows

			if (preferredWidth >= maxWidth)
			    break;
		}

		return preferredWidth;
	}

	/*
	 *  Get the preferred width for the specified cell
	 */
	private int getCellDataWidth(int row, int column)
	{
		//  Inovke the renderer for the cell to calculate the preferred width

		TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
		Object value = table.getValueAt(row, column);
		Component c = cellRenderer.getTableCellRendererComponent(table, value, false, false, row, column);
		int width = c.getPreferredSize().width + table.getIntercellSpacing().width;

		return width;
	}

	/*
	 *  Update the TableColumn with the newly calculated width
	 */
	private void updateTableColumn(int column, int width)
	{
		final TableColumn tableColumn = table.getColumnModel().getColumn(column);

		if (! tableColumn.getResizable()) return;

		width += spacing;

		//  Don't shrink the column width

		if (isOnlyAdjustLarger)
		{
			width = Math.max(width, tableColumn.getPreferredWidth());
		}

		columnSizes.put(tableColumn, new Integer(tableColumn.getWidth()));
		table.getTableHeader().setResizingColumn(tableColumn);
		tableColumn.setWidth(width);
	}

	/*
	 *  Restore the widths of the columns in the table to its previous width
	 */
	private void restoreColumns()
	{
		TableColumnModel tcm = table.getColumnModel();

		for (int i = 0; i < tcm.getColumnCount(); i++)
		{
			restoreColumn(i);
		}
	}

	/*
	 *  Restore the width of the specified column to its previous width
	 */
	private void restoreColumn(int column)
	{
		TableColumn tableColumn = table.getColumnModel().getColumn(column);
		Integer width = columnSizes.get(tableColumn);

		if (width != null)
		{
			table.getTableHeader().setResizingColumn(tableColumn);
			tableColumn.setWidth( width.intValue() );
		}
	}

	/*
	 *	Indicates whether to include the header in the width calculation
	 */
	public void setColumnHeaderIncluded(boolean isColumnHeaderIncluded)
	{
		this.isColumnHeaderIncluded = isColumnHeaderIncluded;
	}

	/*
	 *	Indicates whether to include the model data in the width calculation
	 */
	public void setColumnDataIncluded(boolean isColumnDataIncluded)
	{
		this.isColumnDataIncluded = isColumnDataIncluded;
	}

	/*
	 *	Indicates whether columns can only be increased in size
	 */
	public void setOnlyAdjustLarger(boolean isOnlyAdjustLarger)
	{
		this.isOnlyAdjustLarger = isOnlyAdjustLarger;
	}

	/*
	 *  Indicate whether changes to the model should cause the width to be
	 *  dynamically recalculated.
	 */
	public void setDynamicAdjustment(boolean isDynamicAdjustment)
	{
		//  May need to add or remove the TableModelListener when changed

		if (this.isDynamicAdjustment != isDynamicAdjustment)
		{
			if (isDynamicAdjustment)
			{
				table.addPropertyChangeListener( this );
				table.getModel().addTableModelListener( this );
			}
			else
			{
				table.removePropertyChangeListener( this );
				table.getModel().removeTableModelListener( this );
			}
		}

		this.isDynamicAdjustment = isDynamicAdjustment;
	}
//
//  Implement the PropertyChangeListener
//
	public void propertyChange(PropertyChangeEvent e)
	{
		System.out.println("propertyChange");
		//  When the TableModel changes we need to update the listeners
		//  and column widths

		if ("model".equals(e.getPropertyName()))
		{
			TableModel model = (TableModel)e.getOldValue();
			model.removeTableModelListener( this );

			model = (TableModel)e.getNewValue();
			model.addTableModelListener( this );
			adjustColumns();
		}
	}
//
//  Implement the TableModelListener
//
	public void tableChanged(TableModelEvent e)
	{
		System.out.println("tableChanged");
		//  A cell has been updated

		if (e.getType() == TableModelEvent.UPDATE)
		{
			int column = table.convertColumnIndexToView(e.getColumn());

			//  Only need to worry about an increase in width for this cell

			if (isOnlyAdjustLarger)
			{
				int	row = e.getFirstRow();
				TableColumn tableColumn = table.getColumnModel().getColumn(column);

				if (tableColumn.getResizable())
				{
					int width =	getCellDataWidth(row, column);
					updateTableColumn(column, width);
				}
			}

			//	Could be an increase of decrease so check all rows

			else
			{
				adjustColumn( column );
			}
		}

		//  The update affected more than one column so adjust all columns

		else
		{
			adjustColumns();
		}
	}

	/*
	 *  Install Actions to give user control of certain functionality.
	 */
	private void installActions()
	{
		//installColumnAction(true,  true,  "adjustColumn",  KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK);// "control ADD");
		installColumnAction(false, true,  "adjustColumns", KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK);// "control shift ADD");
		//installColumnAction(true,  false, "restoreColumn",  "control SUBTRACT");
		//installColumnAction(false, false, "restoreColumns", "control shift SUBTRACT");

		//installToggleAction(true,  false, "toggleDynamic",  "control MULTIPLY");
		//installToggleAction(false, true,  "toggleLarger",   "control DIVIDE");
	}

	/*
	 *  Update the input and action maps with a new ColumnAction
	 */
	private void installColumnAction(boolean isSelectedColumn, boolean isAdjust, String key, int keyCode, int modifiers)
	{
		Action action = new ColumnAction(isSelectedColumn, isAdjust);
		KeyStroke ks = KeyStroke.getKeyStroke(keyCode, modifiers);
		table.getInputMap().put(ks, key);
		table.getActionMap().put(key, action);
	}

	/*
	 *  Update the input and action maps with new ToggleAction
	 */
	/*
	private void installToggleAction(
		boolean isToggleDynamic, boolean isToggleLarger, String key, String keyStroke)
	{
		Action action = new ToggleAction(isToggleDynamic, isToggleLarger);
		KeyStroke ks = KeyStroke.getKeyStroke( keyStroke );
		table.getInputMap().put(ks, key);
		table.getActionMap().put(key, action);
	}
*/
	
	/*
	 *  Action to adjust or restore the width of a single column or all columns
	 */
	private class ColumnAction extends AbstractAction
	{
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private boolean isSelectedColumn;
    	private boolean isAdjust;

		private ColumnAction(boolean isSelectedColumn, boolean isAdjust)
		{
			this.isSelectedColumn = isSelectedColumn;
			this.isAdjust = isAdjust;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			//  Handle selected column(s) width change actions

			if (isSelectedColumn)
			{
				int[] columns = table.getSelectedColumns();

				for (int i = 0; i < columns.length; i++)
				{
					if (isAdjust)
						adjustColumn(columns[i]);
					else
						restoreColumn(columns[i]);
				}
			}
			else
			{
				if (isAdjust)
					adjustColumns();
				else
					restoreColumns();
			}
		}
	}

	
}
