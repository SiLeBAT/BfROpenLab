/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Armin A. Weiser (BfR)
 * Christian Thoens (BfR)
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
package de.bund.bfr.knime.openkrise.util.cluster;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import de.bund.bfr.knime.gis.geocode.GeocodingNodeModel;
import de.bund.bfr.knime.openkrise.TracingColumns;

/**
 * This is the model implementation of DBSCAN.
 * 
 * 
 * @author BfR
 */
public class DBSCANNodeModel extends NodeModel {

	protected static final String DBSCAN = "DBSCAN";
	protected static final String K_MEANS = "k-means";

	protected static final String CFG_MINPTS = "minPts";
	protected static final String CFG_EPS = "eps";
	protected static final String CFG_CHOSENMODEL = "chosenmodel";
	protected static final String CFG_CLUSTERS = "clusters";

	protected static final int DEFAULT_MINPTS = 2;
	protected static final double DEFAULT_EPS = 2.0;
	protected static final String DEFAULT_CHOSENMODEL = DBSCAN;
	protected static final int DEFAULT_CLUSTERS = 3;

	private SettingsModelInteger m_minPts;
	private SettingsModelDouble m_eps;
	private SettingsModelString m_chosenModel;
	private SettingsModelInteger m_clusters;

	/**
	 * Constructor for the node model.
	 */
	public DBSCANNodeModel() {
		super(1, 1);
		m_minPts = new SettingsModelInteger(CFG_MINPTS, DEFAULT_MINPTS);
		m_eps = new SettingsModelDouble(CFG_EPS, DEFAULT_EPS);
		m_chosenModel = new SettingsModelString(CFG_CHOSENMODEL,
				DEFAULT_CHOSENMODEL);
		m_clusters = new SettingsModelInteger(CFG_CLUSTERS, DEFAULT_CLUSTERS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		BufferedDataTable data = inData[0];
		BufferedDataContainer buf = exec.createDataContainer(createSpec(data
				.getSpec()));
		int latIndex = data.getSpec().findColumnIndex(
				GeocodingNodeModel.LATITUDE_COLUMN);
		int lonIndex = data.getSpec().findColumnIndex(
				GeocodingNodeModel.LONGITUDE_COLUMN);

		if (latIndex == -1) {
			throw new Exception(GeocodingNodeModel.LATITUDE_COLUMN
					+ " column missing");
		}

		if (lonIndex == -1) {
			throw new Exception(GeocodingNodeModel.LONGITUDE_COLUMN
					+ " column missing");
		}

		Map<Integer, DoublePoint> idp = new LinkedHashMap<>();
		List<DoublePoint> points = new ArrayList<>();
		int rowNumber = 0;

		for (DataRow row : data) {
			exec.checkCanceled();

			if (!row.getCell(latIndex).isMissing()
					&& !row.getCell(lonIndex).isMissing()) {
				DoubleCell latCell = (DoubleCell) row.getCell(latIndex);
				DoubleCell lonCell = (DoubleCell) row.getCell(lonIndex);
				double[] d = new double[2];

				d[0] = Math.toRadians(latCell.getDoubleValue());
				d[1] = Math.toRadians(lonCell.getDoubleValue());

				DoublePoint dp = new DoublePoint(d);

				idp.put(rowNumber, dp);
				points.add(dp);
			}

			rowNumber++;
		}
		List<? extends Cluster<DoublePoint>> cluster = null;

		if (m_chosenModel.getStringValue().equals(DBSCAN)) {
			cluster = dbScan(points);
		} else if (m_chosenModel.getStringValue().equals(K_MEANS)) {
			cluster = kMeans(points);
		}

		rowNumber = 0;

		for (DataRow row : data) {
			exec.checkCanceled();

			int n = data.getSpec().getNumColumns() + 1;
			DataCell[] cells = new DataCell[n];

			for (int i = 0; i < row.getNumCells(); i++) {
				cells[i] = row.getCell(i);
			}

			cells[n - 1] = DataType.getMissingCell();

			for (int i = 0; i < cluster.size(); i++) {
				if (cluster.get(i).getPoints().contains(idp.get(rowNumber))) {
					cells[n - 1] = new IntCell(i);
					break;
				}
			}

			buf.addRowToTable(new DefaultRow(String.valueOf(rowNumber), cells));
			rowNumber++;
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
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		return new DataTableSpec[] { createSpec(inSpecs[0]) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_eps.saveSettingsTo(settings);
		m_minPts.saveSettingsTo(settings);
		m_chosenModel.saveSettingsTo(settings);
		m_clusters.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_eps.loadSettingsFrom(settings);
		m_minPts.loadSettingsFrom(settings);
		m_chosenModel.loadSettingsFrom(settings);
		m_clusters.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_eps.validateSettings(settings);
		m_minPts.validateSettings(settings);
		m_chosenModel.validateSettings(settings);
		m_clusters.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

	private DataTableSpec createSpec(DataTableSpec inSpec) {
		DataColumnSpec[] spec = new DataColumnSpec[inSpec.getNumColumns() + 1];

		for (int i = 0; i < inSpec.getNumColumns(); i++) {
			spec[i] = inSpec.getColumnSpec(i);
		}

		spec[inSpec.getNumColumns()] = new DataColumnSpecCreator(
				TracingColumns.CLUSTER_ID, IntCell.TYPE).createSpec();

		return new DataTableSpec(spec);
	}

	private List<? extends Cluster<DoublePoint>> kMeans(List<DoublePoint> points) {
		KMeansPlusPlusClusterer<DoublePoint> km = new KMeansPlusPlusClusterer<>(
				m_clusters.getIntValue(), -1, new HaversineDistance());
		MultiKMeansPlusPlusClusterer<DoublePoint> mkm = new MultiKMeansPlusPlusClusterer<>(
				km, 5);

		return mkm.cluster(points);
	}

	private List<? extends Cluster<DoublePoint>> dbScan(List<DoublePoint> points) {
		DBSCANClusterer<DoublePoint> dbscan = new DBSCANClusterer<>(
				m_eps.getDoubleValue(), m_minPts.getIntValue(),
				new HaversineDistance());

		return dbscan.cluster(points);
	}

	private static class HaversineDistance implements DistanceMeasure {

		private static final long serialVersionUID = 1L;
		private static final double AVERAGE_RADIUS_OF_EARTH = 6372.8;

		@Override
		public double compute(double[] p1, double[] p2) {
			double d2LatSin = Math.sin((p2[0] - p1[0]) / 2);
			double d2LonSin = Math.sin((p2[1] - p1[1]) / 2);

			double a = d2LatSin * d2LatSin + Math.cos(p1[0]) * Math.cos(p2[0])
					* d2LonSin * d2LonSin;

			return 2 * AVERAGE_RADIUS_OF_EARTH * Math.asin(Math.sqrt(a));
		}

	}
}
