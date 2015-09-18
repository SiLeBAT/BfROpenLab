/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.gis.views.canvas;

public interface CanvasListener {

	void transformChanged(ICanvas<?> source);

	void selectionChanged(ICanvas<?> source);

	void nodeSelectionChanged(ICanvas<?> source);

	void edgeSelectionChanged(ICanvas<?> source);

	void nodeHighlightingChanged(ICanvas<?> source);

	void edgeHighlightingChanged(ICanvas<?> source);

	void layoutProcessStarted(ICanvas<?> source);

	void layoutProcessFinished(ICanvas<?> source);

	void nodePositionsChanged(ICanvas<?> source);

	void edgeJoinChanged(ICanvas<?> source);

	void skipEdgelessChanged(ICanvas<?> source);

	void showEdgesInMetaNodeChanged(ICanvas<?> source);

	void arrowInMiddleChanged(ICanvas<?> source);

	void collapsedNodesChanged(ICanvas<?> source);

	void collapsedNodesAndPickingChanged(ICanvas<?> source);

	void nodeSizeChanged(ICanvas<?> source);

	void edgeThicknessChanged(ICanvas<?> source);

	void fontChanged(ICanvas<?> source);

	void borderAlphaChanged(ICanvas<?> source);

	void avoidOverlayChanged(ICanvas<?> source);
}
