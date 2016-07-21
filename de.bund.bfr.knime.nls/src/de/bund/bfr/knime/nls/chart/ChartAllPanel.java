/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.nls.chart;

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;

public class ChartAllPanel extends JPanel implements ComponentListener {

	private static final long serialVersionUID = 1L;

	private JSplitPane splitPane;
	private boolean adjusted;

	public ChartAllPanel(ChartCreator chartCreator, ChartSelectionPanel selectionPanel, ChartConfigPanel configPanel) {
		adjusted = false;

		JPanel upperPanel = new JPanel();

		upperPanel.setLayout(new BorderLayout());
		upperPanel.add(chartCreator, BorderLayout.CENTER);
		upperPanel.add(new JScrollPane(configPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.EAST);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperPanel, selectionPanel);
		splitPane.addComponentListener(this);

		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
	}

	public int getDividerLocation() {
		return splitPane.getDividerLocation();
	}

	public void setDividerLocation(int location) {
		splitPane.setDividerLocation(location);
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		JSplitPane pane = (JSplitPane) e.getComponent();

		if (!adjusted && pane.getWidth() > 0 && pane.getHeight() > 0) {
			pane.setDividerLocation(0.5);
			adjusted = true;
		}
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}
}
