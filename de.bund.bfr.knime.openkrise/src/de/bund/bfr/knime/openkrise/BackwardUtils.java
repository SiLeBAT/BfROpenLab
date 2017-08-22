/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition;

public class BackwardUtils {

	public static final String STATION_NODE = "node";
	public static final String STATION_VAT = "VAT";
	public static final String STATION_NUMCASES = "Number Cases";
	public static final String STATION_DATESTART = "Date start";
	public static final String STATION_DATEPEAK = "Date peak";
	public static final String STATION_DATEEND = "Date end";
	public static final String STATION_SERIAL = "Serial";
	public static final String STATION_COUNTY = "County";

	public static final String DELIVERY_CHARGENUM = "Charge Number";
	public static final String DELIVERY_ORIGIN = "OriginCountry";
	public static final String DELIVERY_ENDCHAIN = "EndChain";
	public static final String DELIVERY_ENDCHAINWHY = "ExplanationEndChain";
	public static final String DELIVERY_REMARKS = "Contact_Questions_Remarks";
	public static final String DELIVERY_COMMENTS = "Comments";
	public static final String DELIVERY_FURTHERTB = "FurtherTB";
	public static final String DELIVERY_MICROSAMPLE = "MicroSample";
	public static final String DELIVERY_PROCESSING = "Processing";
	public static final String DELIVERY_USAGE = "IntendedUse";
	public static final String DELIVERY_DATEEXP = "Date Expiration";
	public static final String DELIVERY_DATEMANU = "Date Manufactoring";
	public static final String DELIVERY_SERIAL = "Serial";

	public static final String OLD_WEIGHT = "CaseWeight";
	public static final String OLD_OBSERVED = "Filter";

	public static final String OLD_FROM = "ID";
	public static final String OLD_TO = "Next";

	private BackwardUtils() {
	}

	public static HighlightConditionList renameColumns(HighlightConditionList list, Set<String> columns) {
		List<HighlightCondition> conditions = new ArrayList<>();

		for (HighlightCondition c : list.getConditions()) {
			if (c instanceof AndOrHighlightCondition) {
				conditions.add(renameColumn((AndOrHighlightCondition) c, columns));
			} else if (c instanceof ValueHighlightCondition) {
				conditions.add(renameColumn((ValueHighlightCondition) c, columns));
			} else if (c instanceof LogicalValueHighlightCondition) {
				conditions.add(new LogicalValueHighlightCondition(
						renameColumn(((LogicalValueHighlightCondition) c).getValueCondition(), columns),
						renameColumn(((LogicalValueHighlightCondition) c).getLogicalCondition(), columns)));
			}
		}

		return new HighlightConditionList(conditions, list.isPrioritizeColors());
	}

	private static AndOrHighlightCondition renameColumn(AndOrHighlightCondition c, Set<String> columns) {
		List<List<LogicalHighlightCondition>> newConditions = new ArrayList<>();

		for (List<LogicalHighlightCondition> andList : c.getConditions()) {
			List<LogicalHighlightCondition> newAndList = new ArrayList<>();

			for (LogicalHighlightCondition l : andList) {
				newAndList.add(new LogicalHighlightCondition(renameColumn(l.getProperty(), columns), l.getType(),
						l.getValue()));
			}

			newConditions.add(newAndList);
		}

		return new AndOrHighlightCondition(newConditions, c.getName(), c.isShowInLegend(), c.getColor(),
				c.isInvisible(), c.isUseThickness(), (renameColumn(c.getLabelProperty(), columns)), c.getShape());
	}

	private static ValueHighlightCondition renameColumn(ValueHighlightCondition c, Set<String> columns) {
		return new ValueHighlightCondition(renameColumn(c.getProperty(), columns), c.getType(), c.isZeroAsMinimum(),
				c.getName(), c.isShowInLegend(), c.getColor(), c.isInvisible(), c.isUseThickness(),
				renameColumn(c.getLabelProperty(), columns), c.getShape());
	}

	private static String renameColumn(String column, Set<String> columns) {
		if (column == null) {
			return null;
		}

		if (column.equals(OLD_WEIGHT) && !columns.contains(OLD_WEIGHT)) {
			return TracingColumns.WEIGHT;
		}

		if (column.equals(OLD_OBSERVED) && !columns.contains(OLD_OBSERVED)) {
			return TracingColumns.OBSERVED;
		}

		return column;
	}
}
