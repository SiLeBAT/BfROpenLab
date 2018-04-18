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
package de.bund.bfr.knime.pmmlite.views;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.image.ImagePortObject;

import de.bund.bfr.knime.chart.ChartUtils;
import de.bund.bfr.knime.pmmlite.core.PmmUtils;
import de.bund.bfr.knime.pmmlite.core.common.Identifiable;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.views.chart.ChartCreator;

public abstract class MultiSelectionViewModel extends ViewModel {

	/**
	 * Constructor for the node model.
	 */
	protected MultiSelectionViewModel(boolean isSelectionNode) {
		super(isSelectionNode);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		PmmPortObject input = (PmmPortObject) inObjects[0];
		ViewReader reader = createReader(input);
		List<String> ids = ((MultiSelectionViewSettings) set).getSelectedIDs();
		ChartCreator creator = new ChartCreator(reader.getPlotables(), reader.getLegend());

		set.setToChartCreator(creator);
		ids.stream().map(id -> reader.getPlotables().get(id)).filter(Objects::nonNull)
				.forEach(p -> set.setToPlotable(p));

		ImagePortObject image = ChartUtils.getImage(creator.getChart(ids), set.isExportAsSvg(), set.getExportWidth(),
				set.getExportHeight());

		return isSelectionNode ? new PortObject[] { PmmPortObject.createListObject(
				PmmUtils.getById(input.getData(Identifiable.class), new LinkedHashSet<>(ids)),
				getCompatibleSpecs().get(0)), image } : new PortObject[] { image };
	}

	@Override
	protected ViewSettings createSettings() {
		return new MultiSelectionViewSettings();
	}
}
