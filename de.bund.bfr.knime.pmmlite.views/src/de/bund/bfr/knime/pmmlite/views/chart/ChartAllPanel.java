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
package de.bund.bfr.knime.pmmlite.views.chart;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;

public class ChartAllPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JSplitPane splitPane;

	public ChartAllPanel(ChartCreator chartCreator, ChartSelectionPanel selectionPanel, ChartConfigPanel configPanel) {
		configPanel.setOwner(this);

		JPanel upperPanel = new JPanel();

		upperPanel.setLayout(new BorderLayout());
		upperPanel.add(chartCreator, BorderLayout.CENTER);
		upperPanel.add(new JScrollPane(configPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.EAST);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperPanel, selectionPanel);
		splitPane.setResizeWeight(0.5);

		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
	}

	public ChartAllPanel(ChartCreator chartCreator, ChartSelectionPanel selectionPanel, ChartConfigPanel configPanel,
			ChartSamplePanel samplePanel) {
		configPanel.setOwner(this);

		JPanel upperPanel = new JPanel();

		upperPanel.setLayout(new BorderLayout());
		upperPanel.add(chartCreator, BorderLayout.CENTER);
		upperPanel.add(new JScrollPane(configPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.EAST);

		JPanel bottomPanel = new JPanel();

		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(selectionPanel, BorderLayout.CENTER);
		bottomPanel.add(samplePanel, BorderLayout.EAST);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperPanel, bottomPanel);
		splitPane.setResizeWeight(0.5);

		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
	}
}
