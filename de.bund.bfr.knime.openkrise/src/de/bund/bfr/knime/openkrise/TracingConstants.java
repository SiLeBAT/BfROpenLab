/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
 ******************************************************************************/
package de.bund.bfr.knime.openkrise;

public interface TracingConstants {

	public static final String ID_COLUMN = "ID";
	public static final String FROM_COLUMN = "from";
	public static final String TO_COLUMN = "to";
	public static final String NEXT_COLUMN = "Next";

	public static final String OLD_WEIGHT_COLUMN = "CaseWeight";
	public static final String WEIGHT_COLUMN = "Weight";
	public static final String CROSS_CONTAMINATION_COLUMN = "CrossContamination";
	public static final String SCORE_COLUMN = "Score";

	public static final String OLD_OBSERVED_COLUMN = "Filter";
	public static final String OBSERVED_COLUMN = "Observed";
	public static final String BACKWARD_COLUMN = "Backward";
	public static final String FORWARD_COLUMN = "Forward";

	public static final String CLUSTERABLE_COLUMN = "Clusterable";
	public static final String CLUSTER_ID_COLUMN = "ClusterID";

	public static final String DELIVERY_DATE = "Date Delivery";

	public static final String NODE_NAME = "Station";
	public static final String EDGE_NAME = "Delivery";
	public static final String NODES_NAME = "Stations";
	public static final String EDGES_NAME = "Deliveries";

}
