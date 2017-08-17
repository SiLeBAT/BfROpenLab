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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * This is the model implementation of Station2LotConverter.
 * 
 *
 * @author BfR
 */
public class Station2LotConverterNodeModel extends NodeModel {
    
    /**
     * Constructor for the node model.
     */
    protected Station2LotConverterNodeModel() {
        super(3, 3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
    	BufferedDataTable stationData = inData[0];
    	BufferedDataTable deliveryData = inData[1];
    	BufferedDataTable d2dData = inData[2];
    	List<String> lotParams = Lot.lotParams;

		Map<String, Lot> lotMap = new HashMap<>();
		Map<String, Lot> lotMapRev = new HashMap<>();
		Map<String, DataRow> deliveries = new HashMap<>();
		Map<String, String> newD2D = new HashMap<>();
		
		for (DataRow row : deliveryData) {
			Lot lot = new Lot();
			DataCell cell = row.getCell(deliveryData.getSpec().findColumnIndex("ID"));
			String id = getString(cell);
			lot.addDeliveryId(id);
			
			String key = "";
			for (String param : lotParams) {
				cell = row.getCell(deliveryData.getSpec().findColumnIndex(param));
				String s = getString(cell);
				lot.setParam(param, s);
				key += s + ";:;";
			}
			lot.setLotId(key);

			if (!lotMap.containsKey(key)) {
				lotMap.put(key, lot);
				lotMapRev.put(id, lot);
			}
			else {
				lotMapRev.put(id, lotMap.get(key));
			}
			deliveries.put(id, row);
		}
		for (DataRow row : d2dData) {
			DataCell from = row.getCell(d2dData.getSpec().findColumnIndex("ID"));
			DataCell to = row.getCell(d2dData.getSpec().findColumnIndex("Next"));
			String df = getString(from);
			String dt = getString(to);
			Lot f = lotMapRev.get(df);
			f.addToDelivery(lotMapRev.get(dt), df);
		}

		//out1 -> Lot
		DataTableSpec outLotSpec = getLotSpec(lotParams, stationData.getDataTableSpec());
		BufferedDataContainer lotContainer = exec.createDataContainer(outLotSpec);
		for (String key : lotMap.keySet()) {
			Lot lot = lotMap.get(key);
			DataCell[] cells = new DataCell[outLotSpec.getNumColumns()];
			int lfd = 0;
			for (String param : lotParams) {
				cells[lfd] = lot.getCell(param);
				lfd++;
			}

			String from = lot.getStationId();
			for (DataRow row : stationData) {
				DataCell cellId = row.getCell(stationData.getSpec().findColumnIndex("ID"));
				String id = getString(cellId);
				if (id.equals(from)) {
					for (DataCell cell : row) {
						cells[lfd] = cell;
						lfd++;
					}
					break;
				}
			}
			lotContainer.addRowToTable(new DefaultRow(RowKey.createRowKey(lotContainer.size()), cells));
			exec.checkCanceled();
		}				
		lotContainer.close();

		//out2 -> Delivery
		DataTableSpec outDeliverySpec = getDeliverySpec(lotParams, deliveryData.getDataTableSpec());
		BufferedDataContainer deliveryContainer = exec.createDataContainer(outDeliverySpec);
		for (Lot lotfrom : lotMap.values()) {
			for (Lot lotto : lotfrom.getTos().keySet()) {
				for (String deliveryIdFrom : lotfrom.getTos().get(lotto)) {
					DataRow row = deliveries.get(deliveryIdFrom);
					int lfd = 0;
					DataCell[] cells = new DataCell[outDeliverySpec.getNumColumns()];
					for (String col : outDeliverySpec.getColumnNames()) {
						if (col.equals("from")) {
							DataCell cell = new StringCell(lotfrom.getLotId());
							cells[lfd] = cell;							
						}
						else if (col.equals("to")) {
							DataCell cell = new StringCell(lotto.getLotId());
							cells[lfd] = cell;							
						}
						else if (col.equals("ID")) {
							String newDelId = deliveryContainer.size()+"";
							String oldDelID = getString(row.getCell(deliveryData.getSpec().findColumnIndex("ID")));
							newD2D.put(oldDelID, newDelId);
							DataCell cell = new StringCell(newDelId);
							cells[lfd] = cell;							
						}
						else if (col.equals("Delivery ID")) {
							DataCell cell = row.getCell(deliveryData.getSpec().findColumnIndex("ID"));
							cells[lfd] = cell;							
						}
						else {
							DataCell cell = row.getCell(deliveryData.getSpec().findColumnIndex(col));
							cells[lfd] = cell;
						}
						lfd++;
					}
					deliveryContainer.addRowToTable(new DefaultRow(RowKey.createRowKey(deliveryContainer.size()), cells));
					exec.checkCanceled();
				}
			}
		}
		deliveryContainer.close();

		//out3 -> D2D
		DataTableSpec outD2DSpec = d2dData.getDataTableSpec();
		BufferedDataContainer d2dContainer = exec.createDataContainer(outD2DSpec);
		for (DataRow row : d2dData) {
			String d1 = getString(row.getCell(d2dData.getSpec().findColumnIndex("ID")));
			String d2 = getString(row.getCell(d2dData.getSpec().findColumnIndex("Next")));
			if (newD2D.containsKey(d1) && newD2D.containsKey(d2)) {
				DataCell[] cells = new DataCell[2];
				cells[0] = new StringCell(newD2D.get(d1));
				cells[1] = new StringCell(newD2D.get(d2));
				d2dContainer.addRowToTable(new DefaultRow(RowKey.createRowKey(d2dContainer.size()), cells));
			}
			else {
				System.err.println("d1: " + d1 + " -> " + newD2D.get(d1));
				System.err.println("d2: " + d2 + " -> " + newD2D.get(d2));
			}
			exec.checkCanceled();
		}
		d2dContainer.close();

		return new BufferedDataTable[]{lotContainer.getTable(),deliveryContainer.getTable(),d2dContainer.getTable()};
    }
    private String getString(DataCell cell) {
    	return cell instanceof StringValue ? ((StringValue) cell).getStringValue() : null;
    }
    private DataTableSpec getDeliverySpec(List<String> lotParams, DataTableSpec inDeliverySpec) {
    	List<DataColumnSpec> columns = new ArrayList<>();
    	for (DataColumnSpec dcs : inDeliverySpec) {
    		if (!dcs.getName().equals("from") && lotParams.contains(dcs.getName())) ;
    		else if (dcs.getName().equals("ID")) {
    			columns.add(new DataColumnSpecCreator("ID", StringCell.TYPE).createSpec());
    			columns.add(new DataColumnSpecCreator("Delivery ID", StringCell.TYPE).createSpec());
    		}
    		else columns.add(new DataColumnSpecCreator(dcs.getName(), dcs.getType()).createSpec());
    	}
    	return new DataTableSpec(columns.toArray(new DataColumnSpec[0]));
    }
    private DataTableSpec getLotSpec(List<String> lotParams, DataTableSpec inStationsSpec) {
    	List<DataColumnSpec> columns = new ArrayList<>();
    	int lfd = 0;
    	for (String param : lotParams) {
    		if (lfd == 0) columns.add(new DataColumnSpecCreator("ID", StringCell.TYPE).createSpec());
    		else columns.add(new DataColumnSpecCreator(param, StringCell.TYPE).createSpec());
    		lfd++;
    	}
    	for (DataColumnSpec dcs : inStationsSpec) {
   		 	// ID -> Station ID; Name -> Station Name; 
    		if (dcs.getName().equals("ID")) columns.add(new DataColumnSpecCreator("Station ID", dcs.getType()).createSpec());
    		else if (dcs.getName().equals("Name")) columns.add(new DataColumnSpecCreator("Station Name", dcs.getType()).createSpec());
    		else columns.add(new DataColumnSpecCreator(dcs.getName(), dcs.getType()).createSpec());
    	}
    	return new DataTableSpec(columns.toArray(new DataColumnSpec[0]));
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

        // TODO: generated method stub
        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
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

}

