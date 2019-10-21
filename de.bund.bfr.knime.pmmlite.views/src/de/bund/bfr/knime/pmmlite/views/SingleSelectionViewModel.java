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

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.port.PortObject;

import de.bund.bfr.knime.chart.ChartUtils;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.views.chart.ChartCreator;

public abstract class SingleSelectionViewModel extends ViewModel {

	protected SingleSelectionViewModel() {
		super(false);
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		ViewReader reader = createReader((PmmPortObject) inObjects[0]);
		ChartCreator creator = new ChartCreator(reader.getPlotables(), reader.getLegend());
		String id = ((SingleSelectionViewSettings) set).getSelectedID();

		if (reader.getPlotables().containsKey(id)) {
			set.setToPlotable(reader.getPlotables().get(id));
			set.setToChartCreator(creator);
		}

		return new PortObject[] { ChartUtils.getImage(creator.getChart(id), set.isExportAsSvg(), set.getExportWidth(),
				set.getExportHeight()) };
	}

	@Override
	protected ViewSettings createSettings() {
		return new SingleSelectionViewSettings();
	}
}
