package de.bund.bfr.knime.gis.shapeview;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.property.ShapeFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortType;

/**
 * This is the model implementation of ShapeView.
 * 
 * 
 * @author Christian Thoens
 */
public class ShapeViewNodeModel extends NodeModel {

	private static final String CFG_MARKERS_FILE = "markers";

	static final SettingsModelString createLatColModel() {
		return new SettingsModelString("latitude_column", "");
	}

	static final SettingsModelString createLonColModel() {
		return new SettingsModelString("longitude_column", "");
	}

	static final SettingsModelColumnFilter2 createHoverInfoCols() {
		return new SettingsModelColumnFilter2("hover_info_columns");
	}

	private final SettingsModelString m_smLatCol = createLatColModel();

	private final SettingsModelString m_smLonCol = createLonColModel();

	private SettingsModelColumnFilter2 m_smHoverInfoCols = createHoverInfoCols();

	private Map<RowKey, KnimeMapMarker2> m_mapMarkers;

	protected ShapeViewNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE_OPTIONAL },
				new PortType[] {});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		checkInputColumns(inSpecs[0]);
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		int[] latlon = checkInputColumns(inData[0] != null ? inData[0]
				.getDataTableSpec() : null);

		// create map markers
		m_mapMarkers = new HashMap<RowKey, KnimeMapMarker2>();
		if (latlon[0] != -1 && latlon[1] != -1) {
			int[] hoverInfoColIndices;
			if (inData[0] != null) {
				String[] hoverInfoCols = m_smHoverInfoCols.applyTo(
						inData[0].getDataTableSpec()).getIncludes();
				hoverInfoColIndices = new int[hoverInfoCols.length];
				for (int i = 0; i < hoverInfoColIndices.length; i++) {
					hoverInfoColIndices[i] = inData[0].getDataTableSpec()
							.findColumnIndex(hoverInfoCols[i]);
					if (hoverInfoColIndices[i] == -1) {
						setWarningMessage("Column " + hoverInfoCols[i]
								+ " for hover info doesn't exist.");
					}
				}
			} else {
				hoverInfoColIndices = new int[0];
			}

			exec.setProgress("Create map markers");
			int rowCount = inData[0].getRowCount();
			int rowIdx = 0;
			StringBuilder sb = new StringBuilder();
			for (DataRow row : inData[0]) {
				if (row.getCell(latlon[0]).isMissing()
						|| row.getCell(latlon[1]).isMissing()) {
					setWarningMessage("Missing values in marker coordinates encountered. These markers have been skipped!");
					continue;
				}
				double lat = ((DoubleValue) row.getCell(latlon[0]))
						.getDoubleValue();
				double lon = ((DoubleValue) row.getCell(latlon[1]))
						.getDoubleValue();
				Color c = inData[0].getDataTableSpec().getRowColor(row)
						.getColor(false, false);
				ShapeFactory.Shape s = inData[0].getDataTableSpec()
						.getRowShape(row);
				double size = inData[0].getDataTableSpec()
						.getRowSizeFactor(row);
				sb.setLength(0);
				for (int i = 0; i < hoverInfoColIndices.length; i++) {
					if (hoverInfoColIndices[i] != -1) {
						sb.append(inData[0].getDataTableSpec()
								.getColumnSpec(hoverInfoColIndices[i])
								.getName());
						sb.append("=");
						sb.append(row.getCell(hoverInfoColIndices[i])
								.toString());
						sb.append(";");
					}
				}
				m_mapMarkers.put(row.getKey(), new KnimeMapMarker2(lat, lon, c,
						s, size, sb.toString(), row.getKey().toString()));
				exec.checkCanceled();
				exec.setProgress((double) rowIdx++ / rowCount);
			}

			// hiliting
			for (RowKey rk : getInHiLiteHandler(0).getHiLitKeys()) {
				m_mapMarkers.get(rk).setHilited(true);
			}
		}
		return null;
	}

	/*
	 * Checks the correctness of the input columns and returns the column
	 * indices [lat,lon]
	 */
	private int[] checkInputColumns(final DataTableSpec inSpec)
			throws InvalidSettingsException {
		int[] latlon = new int[] { -1, -1 };
		if (inSpec != null) {
			latlon[0] = inSpec.findColumnIndex(m_smLatCol.getStringValue());
			latlon[1] = inSpec.findColumnIndex(m_smLonCol.getStringValue());
			if (latlon[0] == -1 || latlon[1] == -1) {
				throw new InvalidSettingsException(
						"Latitude and/or Longitude columns for map markers do not exist.");
			} else if ((latlon[0] != -1 && !inSpec.getColumnSpec(latlon[0])
					.getType().isCompatible(DoubleValue.class))
					|| (latlon[1] != -1 && !inSpec.getColumnSpec(latlon[1])
							.getType().isCompatible(DoubleValue.class))) {
				throw new InvalidSettingsException(
						"Selected columns are not numeric!");
			}
		}
		return latlon;
	}

	/**
	 * @return mapping from a row key to the map markers to be over-layed
	 */
	Map<RowKey, KnimeMapMarker2> getMapMarkers() {
		return m_mapMarkers;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File nodeInternDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// load map markers
		final File settingsFile = new File(nodeInternDir, CFG_MARKERS_FILE);
		FileInputStream fileIn = new FileInputStream(settingsFile);
		BufferedInputStream bufIn = new BufferedInputStream(fileIn);
		ObjectInputStream objectIn = new ObjectInputStream(bufIn);
		int size = objectIn.readInt();
		m_mapMarkers = new HashMap<RowKey, KnimeMapMarker2>(size);
		for (int i = 0; i < size; i++) {
			RowKey rk = new RowKey(objectIn.readUTF());
			Object marker = null;
			try {
				marker = objectIn.readObject();
			} catch (ClassNotFoundException e) {
				NodeLogger.getLogger(this.getClass()).warn(
						"Problems loading internals.", e);
			}

			m_mapMarkers.put(rk, (KnimeMapMarker2) marker);
		}
		objectIn.close();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File nodeInternDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// save map markers
		final File settingsFile = new File(nodeInternDir, CFG_MARKERS_FILE);
		FileOutputStream fileOut = new FileOutputStream(settingsFile);
		BufferedOutputStream bufOut = new BufferedOutputStream(fileOut);
		ObjectOutputStream objectOut = new ObjectOutputStream(bufOut);
		objectOut.writeInt(m_mapMarkers.size());
		for (Entry<RowKey, KnimeMapMarker2> marker : m_mapMarkers.entrySet()) {
			objectOut.writeUTF(marker.getKey().toString());
			objectOut.writeObject(marker.getValue());
		}
		bufOut.flush();
		objectOut.close();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_smLatCol.saveSettingsTo(settings);
		m_smLonCol.saveSettingsTo(settings);
		m_smHoverInfoCols.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_smLatCol.validateSettings(settings);
		m_smLonCol.validateSettings(settings);
		m_smHoverInfoCols.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_smLatCol.loadSettingsFrom(settings);
		m_smLonCol.loadSettingsFrom(settings);
		m_smHoverInfoCols.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {

	}

}
