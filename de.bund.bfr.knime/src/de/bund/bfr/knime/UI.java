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
package de.bund.bfr.knime;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class UI {

	private UI() {
	}

	public static final Border TABLE_CELL_BORDER = new EmptyBorder(1, 1, 1, 1);
	public static final Border TABLE_CELL_FOCUS_BORDER = UIManager.getBorder("Table.focusCellHighlightBorder");

	public static <V> void select(JComboBox<V> box, V item) {
		if (hasItem(box, item)) {
			box.setSelectedItem(item);
		} else {
			box.setSelectedItem(null);
		}
	}

	public static <V> boolean hasItem(JComboBox<V> box, V item) {
		for (int i = 0; i < box.getItemCount(); i++) {
			if (item == null) {
				if (box.getItemAt(i) == null) {
					return true;
				}
			} else {
				if (item.equals(box.getItemAt(i))) {
					return true;
				}
			}
		}

		return false;
	}

	public static void adjustDialog(JDialog dialog, double widthFraction, double heightFraction) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(dialog.getGraphicsConfiguration());
		int maxWidth = (int) ((screenSize.width - insets.left - insets.right) * widthFraction);
		int maxHeight = (int) ((screenSize.height - insets.top - insets.bottom) * heightFraction);

		dialog.setSize(Math.min(dialog.getWidth(), maxWidth), Math.min(dialog.getHeight(), maxHeight));

		int minX = insets.left;
		int minY = insets.top;
		int maxX = screenSize.width - insets.right - dialog.getWidth();
		int maxY = screenSize.height - insets.bottom - dialog.getHeight();

		dialog.setLocation(Math.max(dialog.getX(), minX), Math.max(dialog.getY(), minY));
		dialog.setLocation(Math.min(dialog.getX(), maxX), Math.min(dialog.getY(), maxY));
	}

	public static void adjustDialog(JDialog dialog) {
		adjustDialog(dialog, 1.0, 1.0);
	}

	public static Dimension getMaxDimension(Dimension d1, Dimension d2) {
		return new Dimension(Math.max(d1.width, d2.width), Math.max(d1.height, d2.height));
	}

	public static JPanel createTitledPanel(Component comp, String title) {
		JPanel p = new JPanel();

		p.setBorder(BorderFactory.createTitledBorder(title));
		p.setLayout(new BorderLayout());
		p.add(comp, BorderLayout.CENTER);

		return p;
	}

	public static JPanel createNorthPanel(Component component) {
		JPanel northPanel = new JPanel();

		northPanel.setLayout(new BorderLayout());
		northPanel.add(component, BorderLayout.NORTH);

		return northPanel;
	}

	public static JPanel createSouthPanel(Component component) {
		JPanel northPanel = new JPanel();

		northPanel.setLayout(new BorderLayout());
		northPanel.add(component, BorderLayout.SOUTH);

		return northPanel;
	}

	public static JPanel createWestPanel(Component component) {
		JPanel northPanel = new JPanel();

		northPanel.setLayout(new BorderLayout());
		northPanel.add(component, BorderLayout.WEST);

		return northPanel;
	}

	public static JPanel createEastPanel(Component component) {
		JPanel northPanel = new JPanel();

		northPanel.setLayout(new BorderLayout());
		northPanel.add(component, BorderLayout.EAST);

		return northPanel;
	}

	public static JPanel createHorizontalPanel(Component... components) {
		JPanel buttonPanel = new JPanel();

		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		for (Component button : components) {
			buttonPanel.add(Box.createHorizontalStrut(5));
			buttonPanel.add(button);
		}

		buttonPanel.remove(0);

		return buttonPanel;
	}

	public static JPanel createOptionsPanel(String name, List<? extends Component> leftComponents,
			List<? extends Component> rightComponents) {
		int n = leftComponents.size();

		JPanel leftPanel = new JPanel();
		JPanel rightPanel = new JPanel();

		leftPanel.setLayout(new GridLayout(n, 1, 5, 5));
		rightPanel.setLayout(new GridLayout(n, 1, 5, 5));

		for (int i = 0; i < n; i++) {
			leftPanel.add(leftComponents.get(i));
			rightPanel.add(rightComponents.get(i));
		}

		JPanel innerPanel = new JPanel();

		innerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		innerPanel.setLayout(new BorderLayout(5, 5));
		innerPanel.add(leftPanel, BorderLayout.WEST);
		innerPanel.add(rightPanel, BorderLayout.CENTER);

		if (name == null) {
			return innerPanel;
		}

		JPanel outerPanel = new JPanel();

		outerPanel.setBorder(BorderFactory.createTitledBorder(name));
		outerPanel.setLayout(new BorderLayout());
		outerPanel.add(innerPanel, BorderLayout.CENTER);

		return outerPanel;
	}

	public static JPanel createCenterPanel(Component comp) {
		JPanel p = new JPanel();

		p.setLayout(new GridBagLayout());
		p.add(comp, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		return p;
	}

	public static void packColumns(JTable table, int maxColumnWidth) {
		for (int c = 0; c < table.getColumnCount(); c++) {
			TableColumn col = table.getColumnModel().getColumn(c);

			if (col.getPreferredWidth() == 0) {
				continue;
			}

			TableCellRenderer renderer = col.getHeaderRenderer();
			Component comp = table.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(table,
					col.getHeaderValue(), false, false, 0, 0);
			int width = comp.getPreferredSize().width + 20;

			for (int r = 0; r < table.getRowCount(); r++) {
				renderer = table.getCellRenderer(r, c);
				comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, c), false, false, r, c);
				width = Math.max(width, comp.getPreferredSize().width);
			}

			col.setPreferredWidth(Math.min(width + 5, maxColumnWidth));
		}
	}

	public static void packColumns(JTable table) {
		packColumns(table, Integer.MAX_VALUE);
	}

	public static int findColumn(JTable table, String columnName) {
		for (int i = 0; i < table.getColumnCount(); i++) {
			if (table.getColumnName(i).equals(columnName)) {
				return i;
			}
		}

		return -1;
	}

	public static void setFontSize(Component c, int fontSize) {
		c.setFont(new Font(c.getFont().getName(), c.getFont().getStyle(), fontSize));
	}

	public static GridBagConstraints centerConstraints(int x, int y) {
		return new GridBagConstraints(x, y, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(2, 2, 2, 2), 0, 0);
	}

	public static GridBagConstraints westConstraints(int x, int y) {
		return westConstraints(x, y, 1, 1);
	}

	public static GridBagConstraints westConstraints(int x, int y, int w, int h) {
		return new GridBagConstraints(x, y, w, h, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(2, 2, 2, 2), 0, 0);
	}

	public static GridBagConstraints fillConstraints(int x, int y) {
		return new GridBagConstraints(x, y, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(2, 2, 2, 2), 0, 0);
	}

	public static JPanel createTablePanel(JTable table) {
		JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createLoweredBevelBorder());
		panel.setLayout(new BorderLayout());
		panel.add(table.getTableHeader(), BorderLayout.NORTH);
		panel.add(table, BorderLayout.CENTER);

		return panel;
	}

	public static void revalidatePanel(Container container) {
		while (container != null) {
			if (container instanceof JPanel) {
				container.revalidate();
				break;
			}

			container = container.getParent();
		}
	}

	public static class DoublePasteAdapter implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JTable table = (JTable) e.getSource();
			int startRow = table.getSelectedRows()[0];
			int startCol = table.getSelectedColumns()[0];
			String clipboardContent = null;

			try {
				clipboardContent = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(table)
						.getTransferData(DataFlavor.stringFlavor);
			} catch (IOException | UnsupportedFlavorException ex) {
				ex.printStackTrace();
				return;
			}

			String[] rows = clipboardContent.split("\n");

			for (int i = 0; i < rows.length; i++) {
				String[] cells = rows[i].split("\t");

				for (int j = 0; j < cells.length; j++) {
					int row = startRow + i;
					int col = startCol + j;

					if (row >= table.getRowCount() || col >= table.getColumnCount()
							|| !table.isCellEditable(row, col)) {
						continue;
					}

					try {
						table.setValueAt(Double.parseDouble(cells[j].replace(",", ".")), row, col);
					} catch (NumberFormatException ex) {
					}
				}
			}

			table.repaint();
		}
	}

	public static class StringPasteAdapter implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JTable table = (JTable) e.getSource();
			int startRow = table.getSelectedRows()[0];
			int startCol = table.getSelectedColumns()[0];
			String clipboardContent = null;

			try {
				clipboardContent = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(table)
						.getTransferData(DataFlavor.stringFlavor);
			} catch (IOException | UnsupportedFlavorException ex) {
				ex.printStackTrace();
				return;
			}

			String[] rows = clipboardContent.split("\n");

			for (int i = 0; i < rows.length; i++) {
				String[] cells = rows[i].split("\t");

				for (int j = 0; j < cells.length; j++) {
					int row = startRow + i;
					int col = startCol + j;

					if (row >= table.getRowCount() || col >= table.getColumnCount()
							|| !table.isCellEditable(row, col)) {
						continue;
					}

					table.setValueAt(cells[j], row, col);
				}
			}

			table.repaint();
		}
	}
}
