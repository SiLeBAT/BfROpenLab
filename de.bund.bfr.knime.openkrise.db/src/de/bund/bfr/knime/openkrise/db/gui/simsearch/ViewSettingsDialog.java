/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

public class ViewSettingsDialog extends JDialog {

	private static final long serialVersionUID = 6828266449840240905L;

	private SimSearchTable.ViewSettings viewSettings;

	protected ViewSettingsDialog(Frame owner, SimSearchTable.ViewSettings viewSettings) {
		super(owner);

		this.viewSettings = viewSettings;
		initComponents();
	}

	private void initComponents() {
		JPanel mainPanel = new JPanel();
		JPanel fontPanel = new JPanel();
		fontPanel.setBorder(BorderFactory.createTitledBorder("Font"));
		fontPanel.setLayout(new FlowLayout());
		fontPanel.add(new JLabel("Name:"));
		fontPanel.add(new FontChooser(viewSettings));
		mainPanel.add(fontPanel);
		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
	}

	public static class FontChooser extends JComboBox<Font> {


		private static final long serialVersionUID = 9215197736341580206L;

		public FontChooser(SimSearchTable.ViewSettings viewSettings, final Component... components) {

			Font currentFont = viewSettings.getFont();
			Font monospacedFont = new Font("Monospaced", Font.PLAIN, currentFont.getSize());
			FontMetrics fm = getFontMetrics(monospacedFont);
			int fontHeight = fm.getHeight();

			final Font[] fonts = GraphicsEnvironment
					.getLocalGraphicsEnvironment()
					.getAllFonts();

			Arrays.sort(fonts, new Comparator<Font>() {
				@Override
				public int compare(Font f1, Font f2) {
					return f1.getName().compareTo(f2.getName());
				}
			});

			for (Font font : fonts) {
				if (font.canDisplayUpTo(font.getName()) == -1) {
					Font tmp = new Font(font.getName(), Font.PLAIN, viewSettings.getFont().getSize());
					fm = getFontMetrics(tmp);
					if(fontHeight==fm.getHeight()) addItem(tmp); //font);
				}
			}

			addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					final Font font = (Font) e.getItem();
					for (Component comp : components) {
						setFontPreserveSize(comp, font);
					}
				}
			});

			setRenderer(new FontCellRenderer());
		}

		private static class FontCellRenderer 
		implements ListCellRenderer<Font> {

			protected DefaultListCellRenderer renderer = 
					new DefaultListCellRenderer();

			public Component getListCellRendererComponent(
					JList<? extends Font> list, Font font, int index, 
					boolean isSelected, boolean cellHasFocus) {

				final Component result = renderer.getListCellRendererComponent(
						list, font.getName(), index, isSelected, cellHasFocus);

				setFontPreserveSize(result, font);
				return result;
			}
		}

		private static void setFontPreserveSize(final Component comp, Font font) {
			final float size = comp.getFont().getSize();
			comp.setFont(font.deriveFont(size));
		}
	}


}
