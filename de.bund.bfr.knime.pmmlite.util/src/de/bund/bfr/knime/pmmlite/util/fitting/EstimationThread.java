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
package de.bund.bfr.knime.pmmlite.util.fitting;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.sbml.jsbml.text.parser.ParseException;

import de.bund.bfr.knime.pmmlite.core.UnitException;
import de.bund.bfr.knime.pmmlite.core.models.Model;

public abstract class EstimationThread<T extends Model> implements Runnable {

	protected T dataModel;

	protected Map<String, Double> minStartValues;
	protected Map<String, Double> maxStartValues;

	protected boolean enforceLimits;
	protected int nParameterSpace;
	protected int nLevenberg;
	protected boolean stopWhenSuccessful;

	private AtomicInteger finishedThreads;

	public EstimationThread(T dataModel, Map<String, Double> minStartValues, Map<String, Double> maxStartValues,
			boolean enforceLimits, int nParameterSpace, int nLevenberg, boolean stopWhenSuccessful,
			AtomicInteger finishedThreads) {
		this.dataModel = dataModel;
		this.minStartValues = minStartValues != null ? minStartValues : Collections.emptyMap();
		this.maxStartValues = maxStartValues != null ? maxStartValues : Collections.emptyMap();
		this.enforceLimits = enforceLimits;
		this.nParameterSpace = nParameterSpace;
		this.nLevenberg = nLevenberg;
		this.stopWhenSuccessful = stopWhenSuccessful;
		this.finishedThreads = finishedThreads;
	}

	@Override
	public void run() {
		try {
			estimate();
			finishedThreads.incrementAndGet();
		} catch (ParseException | UnitException e) {
			e.printStackTrace();
		}
	}

	protected abstract void estimate() throws ParseException, UnitException;
}
