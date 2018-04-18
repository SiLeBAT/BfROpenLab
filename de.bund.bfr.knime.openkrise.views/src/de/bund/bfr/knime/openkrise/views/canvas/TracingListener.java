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
package de.bund.bfr.knime.openkrise.views.canvas;

import java.util.EventListener;

public interface TracingListener extends EventListener {

	void nodePropertiesChanged(ITracingCanvas<?> source);

	void edgePropertiesChanged(ITracingCanvas<?> source);

	void nodeWeightsChanged(ITracingCanvas<?> source);

	void edgeWeightsChanged(ITracingCanvas<?> source);

	void nodeCrossContaminationsChanged(ITracingCanvas<?> source);

	void edgeCrossContaminationsChanged(ITracingCanvas<?> source);

	void nodeKillContaminationsChanged(ITracingCanvas<?> source);

	void edgeKillContaminationsChanged(ITracingCanvas<?> source);

	void observedNodesChanged(ITracingCanvas<?> source);

	void observedEdgesChanged(ITracingCanvas<?> source);

	void enforceTemporalOrderChanged(ITracingCanvas<?> source);

	void showForwardChanged(ITracingCanvas<?> source);

	void dateSettingsChanged(ITracingCanvas<?> source);
}
