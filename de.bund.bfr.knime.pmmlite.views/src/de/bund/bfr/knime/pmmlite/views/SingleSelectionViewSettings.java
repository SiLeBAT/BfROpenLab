/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.views;

import java.util.Arrays;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.pmmlite.views.chart.ChartSelectionPanel;

public class SingleSelectionViewSettings extends SingleDimensionViewSettings {

	private static final String CFG_SELECTED_ID = "SelectedID";

	protected String selectedID;

	public SingleSelectionViewSettings() {
		selectedID = null;
	}

	@Override
	public void load(NodeSettingsRO settings) {
		super.load(settings);

		try {
			selectedID = settings.getString(CFG_SELECTED_ID);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void save(NodeSettingsWO settings) throws InvalidSettingsException {
		super.save(settings);
		settings.addString(CFG_SELECTED_ID, selectedID);
	}

	@Override
	public void setFromSelectionPanel(ChartSelectionPanel selectionPanel) {
		super.setFromSelectionPanel(selectionPanel);

		if (!selectionPanel.getSelectedIDs().isEmpty()) {
			selectedID = selectionPanel.getSelectedIDs().get(0);
		} else {
			selectedID = null;
		}
	}

	@Override
	public void setToSelectionPanel(ChartSelectionPanel selectionPanel) {
		super.setToSelectionPanel(selectionPanel);

		if (getSelectedID() != null) {
			selectionPanel.setSelectedIDs(Arrays.asList(selectedID));
		}
	}

	public String getSelectedID() {
		return selectedID;
	}

	public void setSelectedID(String selectedID) {
		this.selectedID = selectedID;
	}
}
