/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.network.analyzer;

import org.knime.core.data.DataType;
import org.knime.network.core.analyzer.Analyzer;
import org.knime.network.core.analyzer.AnalyzerType;
import org.knime.network.core.api.GraphObjectType;
import org.knime.network.core.api.PersistentObject;

public class ClosenessAnalyzerType implements AnalyzerType<PersistentObject> {

	private static final String ID = "Closeness";
	private static final String NAME = "Closeness Centrality (BfR)";
	private static final ClosenessAnalyzer ANALYZER = new ClosenessAnalyzer();

	@Override
	public String[] getColumnNames() {
		return ANALYZER.getColumnNames();
	}

	@Override
	public DataType[] getDataTypes() {
		return ANALYZER.getDataTypes();
	}

	@Override
	public String[] getSummaryColumnNames() {
		return ANALYZER.getSummaryColumnNames();
	}

	@Override
	public DataType[] getSummaryDataTypes() {
		return ANALYZER.getSummaryDataTypes();
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getToolTip() {
		return NAME;
	}

	@Override
	public boolean getDefaultSelected() {
		return false;
	}

	@Override
	public Analyzer<PersistentObject> getAnalyzer() {
		return ANALYZER.createInstance();
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public GraphObjectType getTargetType() {
		return GraphObjectType.NODE;
	}

}
