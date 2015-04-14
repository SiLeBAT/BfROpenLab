/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General  License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General  License for more details.
 *
 * You should have received a copy of the GNU General  License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.gis.views.canvas;

public interface CanvasListener {

	void nodeSelectionChanged(Canvas<?> source);

	void edgeSelectionChanged(Canvas<?> source);

	void nodeHighlightingChanged(Canvas<?> source);

	void edgeHighlightingChanged(Canvas<?> source);

	void edgeJoinChanged(Canvas<?> source);

	void skipEdgelessChanged(Canvas<?> source);

	void showEdgesInMetaNodeChanged(Canvas<?> source);

	void collapsedNodesChanged(Canvas<?> source);
}
