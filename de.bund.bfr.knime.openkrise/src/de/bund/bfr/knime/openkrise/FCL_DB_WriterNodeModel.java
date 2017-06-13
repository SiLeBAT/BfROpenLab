package de.bund.bfr.knime.openkrise;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat.Delivery;
import de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat.Lot;
import de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat.Product;
import de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat.Station;
import de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat.XlsLot;
import de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat.XlsProduct;
import de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat.XlsStruct;

/**
 * This is the model implementation of FCL_DB_Writer.
 * 
 *
 * @author BfR
 */
public class FCL_DB_WriterNodeModel extends NodeModel {
    
    /**
     * Constructor for the node model.
     */
    protected FCL_DB_WriterNodeModel() {
        super(3, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
		BufferedDataTable nodeTable = inData[0]; // Stations		
		int idIndex = nodeTable.getSpec().findColumnIndex("ID");
		int nameIndex = nodeTable.getSpec().findColumnIndex("Name");
		int addressIndex = nodeTable.getSpec().findColumnIndex("Address");
		int countryIndex = nodeTable.getSpec().findColumnIndex("Country");
		int tobIndex = nodeTable.getSpec().findColumnIndex("type of business");
		HashMap<String, Station> stationMap = new HashMap<>();
		for (DataRow row : nodeTable) {
			String id = IO.getCleanString(row.getCell(idIndex));
			if (id != null) {
				Station s = new Station();
				String cs = IO.getCleanString(row.getCell(nameIndex));
				String address = IO.getCleanString(row.getCell(addressIndex));
				s.setName(cs);
				s.setAddress(address);
				s.setCountry(IO.getCleanString(row.getCell(countryIndex)));
				s.setTypeOfBusiness(IO.getCleanString(row.getCell(tobIndex)));
				int sID = genDbId(""+cs+address);
				s.setId(""+sID);	
				
				stationMap.put(id, s);
			}
		}
		
		BufferedDataTable edgeTable = inData[1]; // Deliveries
		idIndex = edgeTable.getSpec().findColumnIndex("ID");
		int fromIndex = edgeTable.getSpec().findColumnIndex("from");
		int toIndex = edgeTable.getSpec().findColumnIndex("to");
		nameIndex = edgeTable.getSpec().findColumnIndex("Name");
		int eanIndex = edgeTable.getSpec().findColumnIndex("EAN");
		int lotIndex = edgeTable.getSpec().findColumnIndex("Lot Number");
		int mhdIndex = edgeTable.getSpec().findColumnIndex("BBD"); // MHD
		int ddIndex = edgeTable.getSpec().findColumnIndex("Date Delivery - Day");
		int dmIndex = edgeTable.getSpec().findColumnIndex("Date Delivery - Month");
		int dyIndex = edgeTable.getSpec().findColumnIndex("Date Delivery - Year");
		int amountIndex = edgeTable.getSpec().findColumnIndex("Amount");
		int commentIndex = edgeTable.getSpec().findColumnIndex("Comment");
		HashMap<String, Delivery> deliveryMap = new HashMap<>();
		for (DataRow row : edgeTable) {
			String id = IO.getCleanString(row.getCell(idIndex));
			if (id != null) {
				// Product
				String from = IO.getCleanString(row.getCell(fromIndex));
				String to = IO.getCleanString(row.getCell(toIndex));
				if (stationMap.containsKey(from) && stationMap.containsKey(to)) {
					String f2 = IO.getCleanString(row.getCell(nameIndex));
					String f3 = eanIndex < 0 ? null : IO.getCleanString(row.getCell(eanIndex));
					Product p = new Product();
					p.setStation(stationMap.get(from));
					p.setName(f2);
					p.addFlexibleField(XlsProduct.EAN("en"), f3);
					int pID = genDbId(""+(stationMap.get(from).getId() + f2 + f3));
					p.setId(pID);
					// Lot
					f2 = IO.getCleanString(row.getCell(lotIndex));
					f3 = mhdIndex < 0 ? null : IO.getCleanString(row.getCell(mhdIndex));
					Integer f4 = ddIndex < 0 ? null : IO.getInt(row.getCell(ddIndex));
					Integer f5 = dmIndex < 0 ? null : IO.getInt(row.getCell(dmIndex));
					Integer f6 = dyIndex < 0 ? null : IO.getInt(row.getCell(dyIndex));
					String f7 = amountIndex < 0 ? null : IO.getCleanString(row.getCell(amountIndex));
					String f8 = commentIndex < 0 ? null : IO.getCleanString(row.getCell(commentIndex));
					if (f2 == null && f3 == null) {
						if (f4 == null && f5 == null && f6 == null && f7 == null && stationMap.get(to).getName() == null) {
							;//exceptions.add(new Exception("You have no lot information at all in Row " + (i+1) + "."));
						}
						else if (f4 == null && f5 == null && f6 == null && f7 == null) {
							f2 = "[receiver: " + stationMap.get(to).getName() + "]";
						}
						else {
							f2 = "[delivery " + (f6==null ? "" : f6) + "" + (f5==null ? "" : f5) + "" + (f4==null ? "" : f4) + "" + (f7==null ? "" : "_"+f7) + "]";
						}
					}
					Lot lot = new Lot();
					lot.setProduct(p);
					lot.setNumber(f2);
					lot.addFlexibleField(XlsLot.MHD("en"), f3);
					int lID = genDbId(""+p.getId() + f2 + f3);
					lot.setId(lID);
					// Delivery
					Delivery d = new Delivery();
					d = new Delivery();
					d.setLot(lot);
					d.setArrivalDay(f4);
					d.setArrivalMonth(f5);
					d.setArrivalYear(f6);
					d.addFlexibleField("Amount", f7);
					d.setComment(f8);								
					d.setReceiver(stationMap.get(to));
					int  dID = genDbId(""+lot.getId()+f4+f5+f6+f7+f8+stationMap.get(to).getId());						
					d.setId(dID+"");
					
					deliveryMap.put(id, d);
				}
			}
		}
		for (Delivery d : deliveryMap.values()) {
			d.insertIntoDb(null);
		}

		BufferedDataTable d2dTable = inData[2]; // Relations
		fromIndex = d2dTable.getSpec().findColumnIndex("from");
		toIndex = d2dTable.getSpec().findColumnIndex("to");
		for (DataRow row : d2dTable) {
			String from = IO.getCleanString(row.getCell(fromIndex));
			String to = IO.getCleanString(row.getCell(toIndex));
			if (deliveryMap.containsKey(from) && deliveryMap.containsKey(to)) {
				deliveryMap.get(from).addTargetLotId(""+deliveryMap.get(to).getLot().getId());
				deliveryMap.get(to).getLot().getInDeliveries().add(deliveryMap.get(from).getId());
			}
		}
		
		for (Delivery d : deliveryMap.values()) {
			for (String lotId : d.getTargetLotIds()) {
				Delivery.insertD2D(null, d.getId(), lotId);				
			}
		}
		return null;
    }
	private int genDbId(String toCode) {
		return toCode.hashCode();
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
       return null;
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

