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
package de.bund.bfr.knime.openkrise.util.cluster;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.clustering.MultiKMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.gis.geocode.GeocodingNodeModel;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.TracingUtils;

/**
 * This is the model implementation of DBSCAN.
 * 
 * 
 * @author BfR
 */
public class DBSCANNodeModel extends NodeModel {

	private DBSCANNSettings set;

	/**
	 * Constructor for the node model.
	 */
	public DBSCANNodeModel() {
		super(1, 1);
		set = new DBSCANNSettings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
		BufferedDataTable table = inData[0];
		DataTableSpec spec = table.getSpec();

		KnimeUtils.assertColumnNotMissing(spec, TracingColumns.ID);
		KnimeUtils.assertColumnNotMissing(spec, GeocodingNodeModel.LATITUDE_COLUMN);
		KnimeUtils.assertColumnNotMissing(spec, GeocodingNodeModel.LONGITUDE_COLUMN);

		NodePropertySchema nodeSchema = new NodePropertySchema(TracingUtils.getTableColumns(table.getSpec()),
				TracingColumns.ID);
		Collection<GraphNode> nodes = TracingUtils.readGraphNodes(table, nodeSchema).values();
		Set<String> filteredOut;

		if (set.getFilter() != null) {
			filteredOut = set.getFilter().getValues(nodes).entrySet().stream().filter(e -> e.getValue() == 0.0)
					.map(e -> e.getKey().getId()).collect(Collectors.toSet());
		} else {
			filteredOut = new LinkedHashSet<>();
		}

		List<ClusterableRow> clusterableRows = new ArrayList<>();

		for (DataRow row : table) {
			String id = IO.getToCleanString(row.getCell(spec.findColumnIndex(TracingColumns.ID)));
			Double lat = IO.getDouble(row.getCell(spec.findColumnIndex(GeocodingNodeModel.LATITUDE_COLUMN)));
			Double lon = IO.getDouble(row.getCell(spec.findColumnIndex(GeocodingNodeModel.LONGITUDE_COLUMN)));

			if (id == null || lat == null || lon == null || filteredOut.contains(id)) {
				continue;
			}

			clusterableRows.add(new ClusterableRow(row.getKey(), Math.toRadians(lat), Math.toRadians(lon)));
		}

		List<? extends Cluster<ClusterableRow>> clusters;

		if (set.getModel().equals(DBSCANNSettings.MODEL_DBSCAN)) {
			clusters = new DBSCANClusterer<ClusterableRow>(set.getMaxDistance(), set.getMinPoints(),
					new HaversineDistance()).cluster(clusterableRows);
		} else if (set.getModel().equals(DBSCANNSettings.MODEL_K_MEANS)) {
			clusters = new MultiKMeansPlusPlusClusterer<ClusterableRow>(
					new KMeansPlusPlusClusterer<>(set.getNumClusters(), -1, new HaversineDistance()), 5)
							.cluster(clusterableRows);
		} else {
			throw new InvalidSettingsException(set.getModel());
		}

		Map<RowKey, Integer> clusterIds = new LinkedHashMap<>();

		for (int i = 0; i < clusters.size(); i++) {
			for (ClusterableRow r : clusters.get(i).getPoints()) {
				clusterIds.put(r.getKey(), i);
			}
		}

		DataTableSpec outSpec = createSpec(spec);
		BufferedDataContainer container = exec.createDataContainer(outSpec);

		for (DataRow row : table) {
			DataCell[] cells = new DataCell[outSpec.getNumColumns()];

			for (String column : spec.getColumnNames()) {
				cells[outSpec.findColumnIndex(column)] = row.getCell(spec.findColumnIndex(column));
			}

			cells[outSpec.findColumnIndex(TracingColumns.CLUSTER_ID)] = IO.createCell(clusterIds.get(row.getKey()));
			container.addRowToTable(new DefaultRow(row.getKey(), cells));
			exec.checkCanceled();
		}

		container.close();

		return new BufferedDataTable[] { container.getTable() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
		return new DataTableSpec[] { createSpec(inSpecs[0]) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		set.saveSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		set.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	private static DataTableSpec createSpec(DataTableSpec inSpec) throws InvalidSettingsException {
		List<DataColumnSpec> columns = new ArrayList<>();

		for (DataColumnSpec column : inSpec) {
			if (column.getName().equals(TracingColumns.CLUSTER_ID)) {
				throw new InvalidSettingsException(
						"Column name \"" + column.getName() + "\" not allowed in input table.");
			}

			columns.add(column);
		}

		columns.add(new DataColumnSpecCreator(TracingColumns.CLUSTER_ID, IntCell.TYPE).createSpec());

		return new DataTableSpec(columns.toArray(new DataColumnSpec[0]));
	}

	private static class ClusterableRow implements Clusterable {

		private RowKey key;
		private double[] point;

		public ClusterableRow(RowKey key, double latitude, double longitude) {
			this.key = key;
			point = new double[] { latitude, longitude };
		}

		public RowKey getKey() {
			return key;
		}

		@Override
		public double[] getPoint() {
			return point;
		}
	}

	private static class HaversineDistance implements DistanceMeasure {

		private static final long serialVersionUID = 1L;
		private static final double AVERAGE_RADIUS_OF_EARTH = 6372.8;

		@Override
		public double compute(double[] p1, double[] p2) {
			double d2LatSin = Math.sin((p2[0] - p1[0]) / 2);
			double d2LonSin = Math.sin((p2[1] - p1[1]) / 2);

			double a = d2LatSin * d2LatSin + Math.cos(p1[0]) * Math.cos(p2[0]) * d2LonSin * d2LonSin;

			return 2 * AVERAGE_RADIUS_OF_EARTH * Math.asin(Math.sqrt(a));
		}

	}
}
