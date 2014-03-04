/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
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
package de.bund.bfr.knime.openkrise.views.tracingparameters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import com.thoughtworks.xstream.XStream;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.openkrise.MyNewTracing;
import de.bund.bfr.knime.openkrise.views.TracingConstants;
import de.bund.bfr.knime.openkrise.views.TracingUtilities;

/**
 * This is the model implementation of TracingVisualizer.
 * 
 * 
 * @author Christian Thoens
 */
public class TracingParametersNodeModel extends NodeModel {

	private TracingParametersSettings set;

	/**
	 * Constructor for the node model.
	 */
	protected TracingParametersNodeModel() {
		super(3, 2);
		set = new TracingParametersSettings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		BufferedDataTable nodeTable = inData[0];
		BufferedDataTable edgeTable = inData[1];
		BufferedDataTable dataTable = inData[2];
		MyNewTracing tracing = getTracing(dataTable);

		for (int id : set.getCaseWeights().keySet()) {
			Double value = set.getCaseWeights().get(id);

			if (value != null) {
				tracing.setCase(id, value);
			}
		}

		for (int id : set.getCrossContaminations().keySet()) {
			Boolean value = set.getCrossContaminations().get(id);

			if (value != null) {
				tracing.setCrossContamination(id, value);
			}
		}

		tracing.fillDeliveries(set.isEnforeTemporalOrder());

		Set<Integer> filterNodes = new LinkedHashSet<Integer>();
		Set<Integer> backwardNodes = new LinkedHashSet<Integer>();
		Set<Integer> forwardNodes = new LinkedHashSet<Integer>();
		Set<Integer> backwardEdges = new LinkedHashSet<Integer>();
		Set<Integer> forwardEdges = new LinkedHashSet<Integer>();
		Set<Integer> simpleSuppliers = TracingUtilities.getSimpleSuppliers(
				nodeTable, TracingConstants.ID_COLUMN, edgeTable,
				TracingConstants.FROM_COLUMN, TracingConstants.TO_COLUMN);

		for (int id : set.getFilter().keySet()) {
			Boolean value = set.getFilter().get(id);

			if (value != null && value == true) {
				filterNodes.add(id);
				backwardNodes.addAll(tracing.getBackwardStations(id));
				forwardNodes.addAll(tracing.getForwardStations(id));
				backwardEdges.addAll(tracing.getBackwardDeliveries(id));
				forwardEdges.addAll(tracing.getForwardDeliveries(id));
			}
		}

		int index = 0;
		DataTableSpec nodeInSpec = nodeTable.getSpec();
		DataTableSpec nodeOutSpec = createNodeOutSpec(nodeInSpec);
		BufferedDataContainer nodeContainer = exec
				.createDataContainer(nodeOutSpec);

		for (DataRow row : nodeTable) {
			int id = IO.getInt(row.getCell(nodeInSpec
					.findColumnIndex(TracingConstants.ID_COLUMN)));
			DataCell[] cells = new DataCell[nodeOutSpec.getNumColumns()];

			for (DataColumnSpec column : nodeInSpec) {
				cells[nodeOutSpec.findColumnIndex(column.getName())] = row
						.getCell(nodeInSpec.findColumnIndex(column.getName()));
			}

			cells[nodeOutSpec
					.findColumnIndex(TracingConstants.CASE_WEIGHT_COLUMN)] = IO
					.createCell(set.getCaseWeights().get(id));
			cells[nodeOutSpec
					.findColumnIndex(TracingConstants.CROSS_CONTAMINATION_COLUMN)] = IO
					.createCell(set.getCrossContaminations().get(id));
			cells[nodeOutSpec.findColumnIndex(TracingConstants.SCORE_COLUMN)] = IO
					.createCell(tracing.getStationScore(id));
			cells[nodeOutSpec.findColumnIndex(TracingConstants.FILTER_COLUMN)] = IO
					.createCell(filterNodes.contains(id));
			cells[nodeOutSpec.findColumnIndex(TracingConstants.BACKWARD_COLUMN)] = IO
					.createCell(backwardNodes.contains(id));
			cells[nodeOutSpec.findColumnIndex(TracingConstants.FORWARD_COLUMN)] = IO
					.createCell(forwardNodes.contains(id));
			cells[nodeOutSpec
					.findColumnIndex(TracingConstants.SIMPLE_SUPPLIER_COLUMN)] = IO
					.createCell(simpleSuppliers.contains(id));

			nodeContainer.addRowToTable(new DefaultRow(row.getKey(), cells));
			exec.checkCanceled();
			exec.setProgress((double) index
					/ (double) (nodeTable.getRowCount() + edgeTable
							.getRowCount()));
			index++;
		}

		nodeContainer.close();

		DataTableSpec edgeInSpec = edgeTable.getSpec();
		DataTableSpec edgeOutSpec = createEdgeOutSpec(edgeInSpec);
		BufferedDataContainer edgeContainer = exec
				.createDataContainer(edgeOutSpec);

		for (DataRow row : edgeTable) {
			int id = IO.getInt(row.getCell(edgeInSpec
					.findColumnIndex(TracingConstants.ID_COLUMN)));
			DataCell[] cells = new DataCell[edgeOutSpec.getNumColumns()];

			for (DataColumnSpec column : edgeInSpec) {
				cells[edgeOutSpec.findColumnIndex(column.getName())] = row
						.getCell(edgeInSpec.findColumnIndex(column.getName()));
			}

			cells[edgeOutSpec.findColumnIndex(TracingConstants.SCORE_COLUMN)] = IO
					.createCell(tracing.getDeliveryScore(id));
			cells[edgeOutSpec.findColumnIndex(TracingConstants.BACKWARD_COLUMN)] = IO
					.createCell(backwardEdges.contains(id));
			cells[edgeOutSpec.findColumnIndex(TracingConstants.FORWARD_COLUMN)] = IO
					.createCell(forwardEdges.contains(id));

			edgeContainer.addRowToTable(new DefaultRow(row.getKey(), cells));
			exec.checkCanceled();
			exec.setProgress((double) index
					/ (double) (nodeTable.getRowCount() + edgeTable
							.getRowCount()));
			index++;
		}

		edgeContainer.close();

		return new BufferedDataTable[] { nodeContainer.getTable(),
				edgeContainer.getTable() };
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
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		DataTableSpec nodeSpec = inSpecs[0];
		DataTableSpec edgeSpec = inSpecs[1];

		return new DataTableSpec[] { createNodeOutSpec(nodeSpec),
				createEdgeOutSpec(edgeSpec) };
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
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		set.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
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

	protected static MyNewTracing getTracing(BufferedDataTable dataTable)
			throws NotConfigurableException {
		if (dataTable.getRowCount() == 0) {
			throw new NotConfigurableException("Tracing Table is empty");
		}

		DataRow row = null;

		for (DataRow r : dataTable) {
			row = r;
			break;
		}

		DataCell cell = row.getCell(0);
		String xml = ((StringValue) cell).getStringValue();
		XStream xstream = MyNewTracing.getXStream();

		return (MyNewTracing) xstream.fromXML(xml);
	}

	private static DataTableSpec createNodeOutSpec(DataTableSpec nodeSpec) {
		List<DataColumnSpec> newNodeSpec = new ArrayList<DataColumnSpec>();

		for (DataColumnSpec column : nodeSpec) {
			newNodeSpec.add(column);
		}

		newNodeSpec.add(new DataColumnSpecCreator(
				TracingConstants.CASE_WEIGHT_COLUMN, DoubleCell.TYPE)
				.createSpec());
		newNodeSpec.add(new DataColumnSpecCreator(
				TracingConstants.CROSS_CONTAMINATION_COLUMN, BooleanCell.TYPE)
				.createSpec());
		newNodeSpec.add(new DataColumnSpecCreator(
				TracingConstants.SCORE_COLUMN, DoubleCell.TYPE).createSpec());
		newNodeSpec.add(new DataColumnSpecCreator(
				TracingConstants.FILTER_COLUMN, BooleanCell.TYPE).createSpec());
		newNodeSpec.add(new DataColumnSpecCreator(
				TracingConstants.BACKWARD_COLUMN, BooleanCell.TYPE)
				.createSpec());
		newNodeSpec
				.add(new DataColumnSpecCreator(TracingConstants.FORWARD_COLUMN,
						BooleanCell.TYPE).createSpec());
		newNodeSpec.add(new DataColumnSpecCreator(
				TracingConstants.SIMPLE_SUPPLIER_COLUMN, BooleanCell.TYPE)
				.createSpec());

		return new DataTableSpec(newNodeSpec.toArray(new DataColumnSpec[0]));
	}

	private static DataTableSpec createEdgeOutSpec(DataTableSpec edgeSpec) {
		List<DataColumnSpec> newEdgeSpec = new ArrayList<DataColumnSpec>();

		for (DataColumnSpec column : edgeSpec) {
			newEdgeSpec.add(column);
		}

		newEdgeSpec.add(new DataColumnSpecCreator(
				TracingConstants.SCORE_COLUMN, DoubleCell.TYPE).createSpec());
		newEdgeSpec.add(new DataColumnSpecCreator(
				TracingConstants.BACKWARD_COLUMN, BooleanCell.TYPE)
				.createSpec());
		newEdgeSpec
				.add(new DataColumnSpecCreator(TracingConstants.FORWARD_COLUMN,
						BooleanCell.TYPE).createSpec());

		return new DataTableSpec(newEdgeSpec.toArray(new DataColumnSpec[0]));
	}

}
