/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
 ******************************************************************************/
package de.bund.bfr.knime;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class UI {

	private UI() {
	}

	public static void select(JComboBox<?> box, Object item) {
		box.setSelectedItem(item);

		if (box.getSelectedItem() != null
				&& !box.getSelectedItem().equals(item)) {
			box.setSelectedItem(null);
		}
	}

	public static void adjustDialog(JDialog dialog, double widthFraction,
			double heightFraction) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(
				dialog.getGraphicsConfiguration());
		int maxWidth = (int) ((screenSize.width - insets.left - insets.right) * widthFraction);
		int maxHeight = (int) ((screenSize.height - insets.top - insets.bottom) * heightFraction);

		dialog.setSize(Math.min(dialog.getWidth(), maxWidth),
				Math.min(dialog.getHeight(), maxHeight));

		int minX = insets.left;
		int minY = insets.top;
		int maxX = screenSize.width - insets.right - dialog.getWidth();
		int maxY = screenSize.height - insets.bottom - dialog.getHeight();

		dialog.setLocation(Math.max(dialog.getX(), minX),
				Math.max(dialog.getY(), minY));
		dialog.setLocation(Math.min(dialog.getX(), maxX),
				Math.min(dialog.getY(), maxY));
	}

	public static void adjustDialog(JDialog dialog) {
		adjustDialog(dialog, 1.0, 1.0);
	}

	public static Dimension getMaxDimension(Dimension d1, Dimension d2) {
		return new Dimension(Math.max(d1.width, d2.width), Math.max(d1.height,
				d2.height));
	}

	public static JPanel createTitledPanel(JComponent comp, String title) {
		JPanel p = new JPanel();

		p.setBorder(BorderFactory.createTitledBorder(title));
		p.setLayout(new BorderLayout());
		p.add(comp, BorderLayout.CENTER);

		return p;
	}

	public static JPanel createNorthPanel(JComponent component) {
		JPanel northPanel = new JPanel();

		northPanel.setLayout(new BorderLayout());
		northPanel.add(component, BorderLayout.NORTH);

		return northPanel;
	}

	public static JPanel createSouthPanel(JComponent component) {
		JPanel northPanel = new JPanel();

		northPanel.setLayout(new BorderLayout());
		northPanel.add(component, BorderLayout.SOUTH);

		return northPanel;
	}

	public static JPanel createWestPanel(JComponent component) {
		JPanel northPanel = new JPanel();

		northPanel.setLayout(new BorderLayout());
		northPanel.add(component, BorderLayout.WEST);

		return northPanel;
	}

	public static JPanel createEastPanel(JComponent component) {
		JPanel northPanel = new JPanel();

		northPanel.setLayout(new BorderLayout());
		northPanel.add(component, BorderLayout.EAST);

		return northPanel;
	}

	public static JPanel createEmptyBorderPanel(JComponent component) {
		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(component, BorderLayout.CENTER);

		return panel;
	}

	public static JPanel createHorizontalPanel(JComponent... components) {
		JPanel buttonPanel = new JPanel();

		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		for (JComponent button : components) {
			buttonPanel.add(Box.createHorizontalStrut(5));
			buttonPanel.add(button);
		}

		buttonPanel.remove(0);

		return buttonPanel;
	}

	public static JPanel createOptionsPanel(String name,
			List<? extends JComponent> leftComponents,
			List<? extends JComponent> rightComponents) {
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

		JPanel outerPanel = new JPanel();

		outerPanel.setBorder(BorderFactory.createTitledBorder(name));
		outerPanel.setLayout(new BorderLayout());
		outerPanel.add(innerPanel, BorderLayout.CENTER);

		return outerPanel;
	}

	public static JPanel createCenterPanel(JComponent comp) {
		JPanel p = new JPanel();

		p.setLayout(new GridBagLayout());
		p.add(comp, new GridBagConstraints(0, 0, 1, 1, 0, 0,
				GridBagConstraints.LINE_START, GridBagConstraints.NONE,
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
			Component comp = table
					.getTableHeader()
					.getDefaultRenderer()
					.getTableCellRendererComponent(table, col.getHeaderValue(),
							false, false, 0, 0);
			int width = comp.getPreferredSize().width + 20;

			for (int r = 0; r < table.getRowCount(); r++) {
				renderer = table.getCellRenderer(r, c);
				comp = renderer.getTableCellRendererComponent(table,
						table.getValueAt(r, c), false, false, r, c);
				width = Math.max(width, comp.getPreferredSize().width);
			}

			width += 5;
			col.setPreferredWidth(Math.min(width, maxColumnWidth));
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
		c.setFont(new Font(c.getFont().getName(), c.getFont().getStyle(),
				fontSize));
	}
}
