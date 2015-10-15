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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.clustering.MultiKMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
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
		BufferedDataTable nodeTable = inData[0];

		int idIndex = nodeTable.getSpec().findColumnIndex(TracingColumns.ID);
		int latIndex = nodeTable.getSpec().findColumnIndex(GeocodingNodeModel.LATITUDE_COLUMN);
		int lonIndex = nodeTable.getSpec().findColumnIndex(GeocodingNodeModel.LONGITUDE_COLUMN);

		if (idIndex == -1) {
			throw new Exception(TracingColumns.ID + " colum missing");
		}

		if (latIndex == -1) {
			throw new Exception(GeocodingNodeModel.LATITUDE_COLUMN + " column missing");
		}

		if (lonIndex == -1) {
			throw new Exception(GeocodingNodeModel.LONGITUDE_COLUMN + " column missing");
		}

		NodePropertySchema nodeSchema = new NodePropertySchema(TracingUtils.getTableColumns(nodeTable.getSpec()),
				TracingColumns.ID);
		Map<String, GraphNode> nodes = TracingUtils.readGraphNodes(nodeTable, nodeSchema);
		Set<String> filteredOut = new LinkedHashSet<>();

		if (set.getFilter() != null) {
			Map<GraphNode, Double> filterResult = set.getFilter().getValues(nodes.values());

			for (Map.Entry<GraphNode, Double> entry : filterResult.entrySet()) {
				if (entry.getValue() == 0.0) {
					filteredOut.add(entry.getKey().getId());
				}
			}
		}

		BufferedDataContainer buf = exec.createDataContainer(createSpec(nodeTable.getSpec()));

		Map<RowKey, DoublePoint> idp = new LinkedHashMap<>();
		List<DoublePoint> points = new ArrayList<>();

		for (DataRow row : nodeTable) {
			String id = IO.getToCleanString(row.getCell(idIndex));
			Double lat = IO.getDouble(row.getCell(latIndex));
			Double lon = IO.getDouble(row.getCell(lonIndex));

			if (id == null || lat == null || lon == null || filteredOut.contains(id)) {
				continue;
			}

			DoublePoint dp = new DoublePoint(new double[] { Math.toRadians(lat), Math.toRadians(lon) });

			idp.put(row.getKey(), dp);
			points.add(dp);
			exec.checkCanceled();
		}

		List<? extends Cluster<DoublePoint>> cluster = null;

		if (set.getModel().equals(DBSCANNSettings.MODEL_DBSCAN)) {
			cluster = dbScan(points);
		} else if (set.getModel().equals(DBSCANNSettings.MODEL_K_MEANS)) {
			cluster = kMeans(points);
		}

		int index = 0;

		for (DataRow row : nodeTable) {
			int n = nodeTable.getSpec().getNumColumns() + 1;
			DataCell[] cells = new DataCell[n];

			for (int i = 0; i < row.getNumCells(); i++) {
				cells[i] = row.getCell(i);
			}

			cells[n - 1] = DataType.getMissingCell();

			for (int i = 0; i < cluster.size(); i++) {
				if (cluster.get(i).getPoints().contains(idp.get(row.getKey()))) {
					cells[n - 1] = new IntCell(i);
					break;
				}
			}

			buf.addRowToTable(new DefaultRow(String.valueOf(index++), cells));
			exec.checkCanceled();
		}

		buf.close();

		return new BufferedDataTable[] { buf.getTable() };
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

	private DataTableSpec createSpec(DataTableSpec inSpec) {
		DataColumnSpec[] spec = new DataColumnSpec[inSpec.getNumColumns() + 1];

		for (int i = 0; i < inSpec.getNumColumns(); i++) {
			spec[i] = inSpec.getColumnSpec(i);
		}

		spec[inSpec.getNumColumns()] = new DataColumnSpecCreator(TracingColumns.CLUSTER_ID, IntCell.TYPE).createSpec();

		return new DataTableSpec(spec);
	}

	private List<? extends Cluster<DoublePoint>> dbScan(List<DoublePoint> points) {
		DBSCANClusterer<DoublePoint> dbscan = new DBSCANClusterer<>(set.getMaxDistance(), set.getMinPoints(),
				new HaversineDistance());

		return dbscan.cluster(points);
	}

	private List<? extends Cluster<DoublePoint>> kMeans(List<DoublePoint> points) {
		KMeansPlusPlusClusterer<DoublePoint> km = new KMeansPlusPlusClusterer<>(set.getNumClusters(), -1,
				new HaversineDistance());
		MultiKMeansPlusPlusClusterer<DoublePoint> mkm = new MultiKMeansPlusPlusClusterer<>(km, 5);

		return mkm.cluster(points);
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
