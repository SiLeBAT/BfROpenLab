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
package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition;
import de.bund.bfr.knime.openkrise.TracingColumns;

public class DefaultHighlighting {

	private DefaultHighlighting() {
	}

	public static HighlightConditionList createNodeHighlighting() {
		List<HighlightCondition> conditions = new ArrayList<>();

		conditions.add(createOutbreakCondition());
		conditions.add(createObservedCondition(Color.GREEN));
		conditions.add(createForwardCondition());
		conditions.add(createBackwardCondition());
		conditions.add(createCrossContaminationCondition());
		conditions.add(createCommonLinkCondition());
		conditions.add(createScoreCondition());

		return new HighlightConditionList(conditions, false);
	}

	public static HighlightConditionList createEdgeHighlighting() {
		List<HighlightCondition> conditions = new ArrayList<>();

		conditions.add(createOutbreakCondition());
		conditions.add(createObservedCondition(Color.BLUE));
		conditions.add(createForwardCondition());
		conditions.add(createBackwardCondition());

		return new HighlightConditionList(conditions, false);
	}

	private static HighlightCondition createOutbreakCondition() {
		LogicalHighlightCondition weight = new LogicalHighlightCondition(TracingColumns.WEIGHT,
				LogicalHighlightCondition.Type.GREATER, "0");

		return new AndOrHighlightCondition(weight, "Outbreak", true, Color.RED, false, false, null, null);
	}

	private static HighlightCondition createObservedCondition(Color color) {
		LogicalHighlightCondition observed = new LogicalHighlightCondition(TracingColumns.OBSERVED,
				LogicalHighlightCondition.Type.EQUAL, "1");

		return new AndOrHighlightCondition(observed, "Observed", true, color, false, false, null, null);
	}

	private static HighlightCondition createForwardCondition() {
		LogicalHighlightCondition forward = new LogicalHighlightCondition(TracingColumns.FORWARD,
				LogicalHighlightCondition.Type.EQUAL, "1");

		return new AndOrHighlightCondition(forward, "Forward Trace", true, Color.ORANGE, false, false, null, null);
	}

	private static HighlightCondition createBackwardCondition() {
		LogicalHighlightCondition backward = new LogicalHighlightCondition(TracingColumns.BACKWARD,
				LogicalHighlightCondition.Type.EQUAL, "1");

		return new AndOrHighlightCondition(backward, "Backward Trace", true, Color.MAGENTA, false, false, null, null);
	}

	private static HighlightCondition createCrossContaminationCondition() {
		LogicalHighlightCondition crossContamination = new LogicalHighlightCondition(TracingColumns.CROSS_CONTAMINATION,
				LogicalHighlightCondition.Type.EQUAL, "1");

		return new AndOrHighlightCondition(crossContamination, "Cross Contamination", true, Color.BLACK, false, false,
				null, null);
	}

	private static HighlightCondition createCommonLinkCondition() {
		LogicalHighlightCondition commonLink = new LogicalHighlightCondition(TracingColumns.SCORE,
				LogicalHighlightCondition.Type.EQUAL, "1");

		return new AndOrHighlightCondition(commonLink, "Common Link", true, Color.YELLOW, false, false, null, null);
	}

	private static HighlightCondition createScoreCondition() {
		return new ValueHighlightCondition(TracingColumns.SCORE, ValueHighlightCondition.Type.VALUE, true, "Score",
				false, null, false, true, null, null);
	}
}
