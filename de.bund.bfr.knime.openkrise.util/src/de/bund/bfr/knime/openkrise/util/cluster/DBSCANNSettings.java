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
package de.bund.bfr.knime.openkrise.util.cluster;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.google.common.collect.ImmutableList;

import de.bund.bfr.knime.NodeSettings;
import de.bund.bfr.knime.XmlConverter;
import de.bund.bfr.knime.gis.BackwardUtils;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.openkrise.util.Activator;

public class DBSCANNSettings extends NodeSettings {

	private static final XmlConverter SERIALIZER = new XmlConverter(Activator.class.getClassLoader());

	public static final String MODEL_DBSCAN = "DBSCAN";
	public static final String MODEL_K_MEANS = "k-means";
	public static final ImmutableList<String> MODEL_CHOICES = ImmutableList.of(MODEL_DBSCAN, MODEL_K_MEANS);

	private static final String CFG_MODEL = "chosenmodel";
	private static final String CFG_FILTER = "filter";
	private static final String CFG_MIN_POINTS = "minPts";
	private static final String CFG_MAX_DISTANCE = "eps";
	private static final String CFG_NUM_CLUSTERS = "clusters";

	private String model;
	private AndOrHighlightCondition filter;
	private int minPoints;
	private double maxDistance;
	private int numClusters;

	public DBSCANNSettings() {
		model = MODEL_DBSCAN;
		filter = null;
		minPoints = 2;
		maxDistance = 2.0;
		numClusters = 3;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			model = settings.getString(CFG_MODEL);
		} catch (InvalidSettingsException e) {
		}

		try {
			filter = (AndOrHighlightCondition) SERIALIZER
					.fromXml(BackwardUtils.toNewHighlightingFormat(settings.getString(CFG_FILTER)));
		} catch (InvalidSettingsException e) {
		}

		try {
			minPoints = settings.getInt(CFG_MIN_POINTS);
		} catch (InvalidSettingsException e) {
		}

		try {
			maxDistance = settings.getDouble(CFG_MAX_DISTANCE);
		} catch (InvalidSettingsException e) {
		}

		try {
			numClusters = settings.getInt(CFG_NUM_CLUSTERS);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addString(CFG_MODEL, model);
		settings.addString(CFG_FILTER, SERIALIZER.toXml(filter));
		settings.addInt(CFG_MIN_POINTS, minPoints);
		settings.addDouble(CFG_MAX_DISTANCE, maxDistance);
		settings.addInt(CFG_NUM_CLUSTERS, numClusters);
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public AndOrHighlightCondition getFilter() {
		return filter;
	}

	public void setFilter(AndOrHighlightCondition filter) {
		this.filter = filter;
	}

	public int getMinPoints() {
		return minPoints;
	}

	public void setMinPoints(int minPoints) {
		this.minPoints = minPoints;
	}

	public double getMaxDistance() {
		return maxDistance;
	}

	public void setMaxDistance(double maxDistance) {
		this.maxDistance = maxDistance;
	}

	public int getNumClusters() {
		return numClusters;
	}

	public void setNumClusters(int numClusters) {
		this.numClusters = numClusters;
	}

}
