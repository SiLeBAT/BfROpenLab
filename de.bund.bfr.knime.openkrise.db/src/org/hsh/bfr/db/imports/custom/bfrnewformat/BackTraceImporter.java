package org.hsh.bfr.db.imports.custom.bfrnewformat;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JProgressBar;
import javax.swing.filechooser.FileFilter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hsh.bfr.db.DBKernel;
import org.hsh.bfr.db.MyLogger;
import org.hsh.bfr.db.gui.dbtable.MyDBTable;
import org.hsh.bfr.db.imports.MyImporter;

public class BackTraceImporter extends FileFilter implements MyImporter {

	private String logMessages = "";
	
	public String getLogMessages() {
		return logMessages;
	}
	public void doImport(Workbook wb, String filename) throws Exception {
		Sheet businessSheet = wb.getSheet("Stations");

		Sheet transactionSheet = wb.getSheet("BackTracing");
		
		Sheet inSheet = wb.getSheet("IncomingDeliveries");
		Sheet lotSheet = wb.getSheet("Lot");
		Sheet outSheet = wb.getSheet("OutgoingDeliveries");
		
		if (inSheet != null && lotSheet != null && outSheet != null) {
			// load all Stations
			HashMap<String, Station> stations = new HashMap<>();
			int numRows = businessSheet.getLastRowNum() + 1;
			Row titleRow = businessSheet.getRow(0);
			for (int i=1;i<numRows;i++) {
				Station s = getStation(titleRow, businessSheet.getRow(i));
				stations.put(s.getLookup(), s);
			}
			// load all Lots
			HashMap<String, Lot> lots = new HashMap<>();
			numRows = lotSheet.getLastRowNum() + 1;
			titleRow = lotSheet.getRow(0);
			for (int i=1;i<numRows;i++) {
				Lot l = getMultiLot(stations, titleRow, lotSheet.getRow(i));
				lots.put(l.getLookup(), l);
			}
			// load all Deliveries
			HashMap<String, Delivery> deliveries = new HashMap<>();
			numRows = outSheet.getLastRowNum() + 1;
			titleRow = outSheet.getRow(0);
			for (int i=1;i<numRows;i++) {
				Delivery d = getMultiOutDelivery(stations, lots, titleRow, outSheet.getRow(i));
				deliveries.put(d.getLookup(), d);
			}
			
			// load Recipes
			HashSet<DeliveryLot> recipes = new HashSet<>();
			numRows = inSheet.getLastRowNum() + 1;
			titleRow = inSheet.getRow(0);
			for (int i=1;i<numRows;i++) {
				DeliveryLot dl = getDeliveryLot(lots, deliveries, titleRow, inSheet.getRow(i));
				recipes.add(dl);
			}

			MetaInfo mi = new MetaInfo();
			mi.setFilename(filename);

			DBKernel.sendRequest("SET AUTOCOMMIT FALSE", false);
			Integer miDbId = mi.getID();
			if (miDbId == null) throw new Exception("File already imported");
			for (Delivery d : deliveries.values()) {
				d.getID(miDbId);
				if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
			}
			for (DeliveryLot dl : recipes) {
				dl.getId(miDbId);
			}

			DBKernel.sendRequest("COMMIT", false);
			DBKernel.sendRequest("SET AUTOCOMMIT TRUE", false);
			
			return;
		}
		Row row = transactionSheet.getRow(0);
		
		// Station in focus
		Station sif = getStation(businessSheet, row.getCell(1).getStringCellValue());
		
		// Delivery(s) Outbound
		int rowIndex = 5;
		HashMap<String, Delivery> outDeliveries = new HashMap<>(); 
		Row titleRow = transactionSheet.getRow(rowIndex - 2);
		for (;;rowIndex++) {
			row = transactionSheet.getRow(rowIndex);
			if (row == null) continue;
			if (isBlockEnd(row, 13, "Reporter Information")) break;
			Delivery d = getDelivery(businessSheet, sif, row, true, titleRow);
			outDeliveries.put(d.getId(), d);
		}
		
		// Metadata on Reporter
		rowIndex = getNextBlockRowIndex(transactionSheet, rowIndex, "Reporter Information") + 2;
		row = transactionSheet.getRow(rowIndex);
		MetaInfo mi = getMetaInfo(row);
		mi.setFilename(filename);
		
		// Lot(s)
		rowIndex = getNextBlockRowIndex(transactionSheet, rowIndex, "Lot Information") + 3;
		titleRow = transactionSheet.getRow(rowIndex - 2);
		for (;;rowIndex++) {
			row = transactionSheet.getRow(rowIndex);
			if (row == null) continue;
			if (isBlockEnd(row, 13, "Ingredients for Lot(s)")) break;
			if (!fillLot(row, outDeliveries, titleRow)) throw new Exception("DeliveryID unknown in Row number " + (rowIndex + 1));
		}
		
		// Deliveries/Recipe Inbound
		rowIndex = getNextBlockRowIndex(transactionSheet, rowIndex, "Ingredients for Lot(s)") + 3;
		HashSet<Delivery> inDeliveries = new HashSet<>(); 
		int numRows = transactionSheet.getLastRowNum() + 1;
		titleRow = transactionSheet.getRow(rowIndex - 2);
		for (;rowIndex < numRows;rowIndex++) {
			row = transactionSheet.getRow(rowIndex);
			if (row == null) continue;
			if (isBlockEnd(row, 13, null)) break;
			Delivery d = getDelivery(businessSheet, sif, row, false, titleRow);
			if (d.getTargetLotId() == null) throw new Exception("LotID unknown in Row number " + (rowIndex + 1));
			inDeliveries.add(d);
		}
		
		// what are the insertRessources (new BfR-Format -> then SupplyChain-Reader has to look for other IDcolumns, i.e. Serial)
		// welche Reihenfolge to insert? Was, wenn beim Import was schiefgeht?
		DBKernel.sendRequest("SET AUTOCOMMIT FALSE", false);
		Integer miDbId = mi.getID();
		if (miDbId == null) throw new Exception("File already imported");
		insertIntoDb(miDbId, inDeliveries, outDeliveries);
		DBKernel.sendRequest("COMMIT", false);
		DBKernel.sendRequest("SET AUTOCOMMIT TRUE", false);
	}
	private void insertIntoDb(Integer miDbId, HashSet<Delivery> inDeliveries, HashMap<String, Delivery> outDeliveries) throws Exception {
		HashMap<String, Integer> lotDbNumber = new HashMap<>();
		for (Delivery d : outDeliveries.values()) {
			d.getID(miDbId);
			if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
			lotDbNumber.put(d.getLot().getNumber(), d.getLot().getDbId());
		}
		for (Delivery d : inDeliveries) {
			Integer dbId = d.getID(miDbId);
			if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
			if (d.getTargetLotId() != null && lotDbNumber.containsKey(d.getTargetLotId())) {
				new DeliveryLot().getId(dbId, lotDbNumber.get(d.getTargetLotId()), miDbId);
			}
		}
	}
	private boolean isBlockEnd(Row row, int numCols2Check, String nextBlockIdentifier) {
		if (row == null) return true;
		for (int j=0;j<numCols2Check;j++) {
			Cell cell = row.getCell(j);
			if (cell == null) continue;
			cell.setCellType(Cell.CELL_TYPE_STRING);
			String s = cell.getStringCellValue().trim(); 
			if (j == 0 && nextBlockIdentifier != null && s.equals(nextBlockIdentifier)) return true;
			if (!s.isEmpty()) return false;
		}
		return true;
	}
	private int getNextBlockRowIndex(Sheet transactionSheet, int rowIndex, String nextBlockIdentifier) {
		int numRows = transactionSheet.getLastRowNum() + 1;
		for (;rowIndex < numRows;rowIndex++) {
			Row row = transactionSheet.getRow(rowIndex);
			if (row == null) continue;
			Cell cell = row.getCell(0);
			if (cell == null) continue;
			cell.setCellType(Cell.CELL_TYPE_STRING);
			String s = cell.getStringCellValue().trim(); 
			if (s.equals(nextBlockIdentifier)) return rowIndex;
		}
		return -100;
	}
	
	private MetaInfo getMetaInfo(Row row) {
		if (row == null) return null;
		MetaInfo result = new MetaInfo();
		Cell cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setReporter(cell.getStringCellValue());}
		cell = row.getCell(1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDate(cell.getStringCellValue());}
		cell = row.getCell(2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setRemarks(cell.getStringCellValue());}
		return result;
	}
	private Station getStation(Sheet businessSheet, String lookup) {
		Station result = null;
		int numRows = businessSheet.getLastRowNum() + 1;
		for (int i=0;i<numRows;i++) {
			Row row = businessSheet.getRow(i);
			Cell cell = row.getCell(10);
			if (cell.getStringCellValue().equals(lookup)) {
				result = getStation(businessSheet.getRow(0), row);
				break;
			}
		}
		return result;
	}
	private Station getStation(Row titleRow, Row row) {
		Station result = new Station();
		Cell cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setId(cell.getStringCellValue());}
		cell = row.getCell(1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setName(cell.getStringCellValue());}
		cell = row.getCell(2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setStreet(cell.getStringCellValue());}
		cell = row.getCell(3); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setNumber(cell.getStringCellValue());}
		cell = row.getCell(4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setZip(cell.getStringCellValue());}
		cell = row.getCell(5); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setCity(cell.getStringCellValue());}
		cell = row.getCell(6); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDistrict(cell.getStringCellValue());}
		cell = row.getCell(7); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setState(cell.getStringCellValue());}
		cell = row.getCell(8); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setCountry(cell.getStringCellValue());}
		cell = row.getCell(9); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setTypeOfBusiness(cell.getStringCellValue());}
		cell = row.getCell(10); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setLookup(cell.getStringCellValue());}
		
		// Further flexible cells
		for (int ii=11;ii<20;ii++) {
			Cell tCell = titleRow.getCell(ii);
			if (tCell != null && tCell.getCellType() != Cell.CELL_TYPE_BLANK) {
				tCell.setCellType(Cell.CELL_TYPE_STRING);
				cell = row.getCell(ii); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.addFlexibleField(tCell.getStringCellValue(), cell.getStringCellValue());}			
			}
		}
		return result;
	}
	private DeliveryLot getDeliveryLot(HashMap<String, Lot> lots, HashMap<String, Delivery> deliveries, Row titleRow, Row row) {
		DeliveryLot result = new DeliveryLot();
		Cell cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setIngredient(deliveries.get(cell.getStringCellValue()));}
		cell = row.getCell(1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setTargetLot(lots.get(cell.getStringCellValue()));}
		
		// Further flexible cells
		for (int i=2;i<10;i++) {
			Cell tCell = titleRow.getCell(i);
			if (tCell != null && tCell.getCellType() != Cell.CELL_TYPE_BLANK) {
				tCell.setCellType(Cell.CELL_TYPE_STRING);
				cell = row.getCell(i); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.addFlexibleField(tCell.getStringCellValue(), cell.getStringCellValue());}			
			}
		}
		return result;
	}
	private Delivery getMultiOutDelivery(HashMap<String, Station> stations, HashMap<String, Lot> lots, Row titleRow, Row row) {
		Delivery result = new Delivery();
		Cell cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setLot(lots.get(cell.getStringCellValue()));}
		cell = row.getCell(1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {result.setDepartureDay((int) cell.getNumericCellValue());}
		cell = row.getCell(2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {result.setDepartureMonth((int) cell.getNumericCellValue());}
		cell = row.getCell(3); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {result.setDepartureYear((int) cell.getNumericCellValue());}
		cell = row.getCell(4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {result.setArrivalDay((int) cell.getNumericCellValue());}
		cell = row.getCell(5); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {result.setArrivalMonth((int) cell.getNumericCellValue());}
		cell = row.getCell(6); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {result.setArrivalYear((int) cell.getNumericCellValue());}
		cell = row.getCell(7); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {result.setUnitNumber(cell.getNumericCellValue());}
		cell = row.getCell(8); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setUnitUnit(cell.getStringCellValue());}
		cell = row.getCell(9);  if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setReceiver(stations.get(cell.getStringCellValue()));}
		cell = row.getCell(10); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setLookup(cell.getStringCellValue());}
		
		// Further flexible cells
		for (int i=11;i<20;i++) {
			Cell tCell = titleRow.getCell(i);
			if (tCell != null && tCell.getCellType() != Cell.CELL_TYPE_BLANK) {
				tCell.setCellType(Cell.CELL_TYPE_STRING);
				cell = row.getCell(i); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.addFlexibleField(tCell.getStringCellValue(), cell.getStringCellValue());}			
			}
		}
		return result;
	}
	private Delivery getDelivery(Sheet businessSheet, Station sif, Row row, boolean outbound, Row titleRow) {
		Product p = new Product();
		if (outbound) p.setStation(sif);
		Cell cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); p.setName(cell.getStringCellValue());}
		cell = row.getCell(1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); p.setNumber(cell.getStringCellValue());}
		Lot l = new Lot();
		l.setProduct(p);
		cell = row.getCell(2); if (cell != null) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setNumber(cell.getStringCellValue());}
		Delivery result = new Delivery();
		result.setLot(l);
		if (!outbound) result.setReceiver(sif);
		cell = row.getCell(3); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {result.setDepartureDay((int) cell.getNumericCellValue());}
		cell = row.getCell(4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {result.setDepartureMonth((int) cell.getNumericCellValue());}
		cell = row.getCell(5); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {result.setDepartureYear((int) cell.getNumericCellValue());}
		cell = row.getCell(6); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {result.setArrivalDay((int) cell.getNumericCellValue());}
		cell = row.getCell(7); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {result.setArrivalMonth((int) cell.getNumericCellValue());}
		cell = row.getCell(8); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {result.setArrivalYear((int) cell.getNumericCellValue());}
		cell = row.getCell(9); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {result.setUnitNumber(cell.getNumericCellValue());}
		cell = row.getCell(10); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setUnitUnit(cell.getStringCellValue());}
		cell = row.getCell(11); 
		if (outbound && cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setReceiver(getStation(businessSheet, cell.getStringCellValue()));}
		if (!outbound && cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); p.setStation(getStation(businessSheet, cell.getStringCellValue()));}
		cell = row.getCell(12);
		if (outbound && cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setId(cell.getStringCellValue());}
		if (!outbound && cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setTargetLotId(cell.getStringCellValue());}
		
		// Further flexible cells
		for (int i=13;i<20;i++) {
			Cell tCell = titleRow.getCell(i);
			if (tCell != null && tCell.getCellType() != Cell.CELL_TYPE_BLANK) {
				tCell.setCellType(Cell.CELL_TYPE_STRING);
				cell = row.getCell(i); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.addFlexibleField(tCell.getStringCellValue(), cell.getStringCellValue());}			
			}
		}
		return result;
	}
	private Lot getMultiLot(HashMap<String, Station> stations, Row titleRow, Row row) {
		Product p = new Product();
		Cell cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); p.setStation(stations.get(cell.getStringCellValue()));}
		cell = row.getCell(1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); p.setName(cell.getStringCellValue());}
		cell = row.getCell(2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); p.setNumber(cell.getStringCellValue());}
		Lot l = new Lot();
		cell = row.getCell(3); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setNumber(cell.getStringCellValue());}
		cell = row.getCell(4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {l.setUnitNumber(cell.getNumericCellValue());}
		cell = row.getCell(5); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setUnitUnit(cell.getStringCellValue());}
		cell = row.getCell(6); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {l.setProductionDay((int) cell.getNumericCellValue());}
		cell = row.getCell(7); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {l.setProductionMonth((int) cell.getNumericCellValue());}
		cell = row.getCell(8); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {l.setProductionYear((int) cell.getNumericCellValue());}
		cell = row.getCell(9); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {l.setExpiryDay((int) cell.getNumericCellValue());}
		cell = row.getCell(10); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {l.setExpiryMonth((int) cell.getNumericCellValue());}
		cell = row.getCell(11); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {l.setExpiryYear((int) cell.getNumericCellValue());}
		cell = row.getCell(12); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.getProduct().setTreatment(cell.getStringCellValue());}
		cell = row.getCell(13); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setSampling(cell.getStringCellValue());}
		cell = row.getCell(14); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setLookup(cell.getStringCellValue());}
		
		// Further flexible cells
		for (int i=15;i<25;i++) {
			Cell tCell = titleRow.getCell(i);
			if (tCell != null && tCell.getCellType() != Cell.CELL_TYPE_BLANK) {
				tCell.setCellType(Cell.CELL_TYPE_STRING);
				cell = row.getCell(i); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.addFlexibleField(tCell.getStringCellValue(), cell.getStringCellValue());}			
			}
		}
		return l;
	}
	private boolean fillLot(Row row, HashMap<String, Delivery> deliveries, Row titleRow) {
		Delivery d = null;
		Cell cell = row.getCell(12); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); d = deliveries.get(cell.getStringCellValue());}
		if (d == null) return false;
		Lot l = d.getLot();
		cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setNumber(cell.getStringCellValue());}
		cell = row.getCell(1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {l.setUnitNumber(cell.getNumericCellValue());}
		cell = row.getCell(2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setUnitUnit(cell.getStringCellValue());}
		cell = row.getCell(3); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {l.setProductionDay((int) cell.getNumericCellValue());}
		cell = row.getCell(4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {l.setProductionMonth((int) cell.getNumericCellValue());}
		cell = row.getCell(5); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {l.setProductionYear((int) cell.getNumericCellValue());}
		cell = row.getCell(6); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {l.setExpiryDay((int) cell.getNumericCellValue());}
		cell = row.getCell(7); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {l.setExpiryMonth((int) cell.getNumericCellValue());}
		cell = row.getCell(8); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {l.setExpiryYear((int) cell.getNumericCellValue());}
		cell = row.getCell(9); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.getProduct().setTreatment(cell.getStringCellValue());}
		cell = row.getCell(11); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setSampling(cell.getStringCellValue());}
		
		// Further flexible cells
		for (int i=13;i<20;i++) {
			Cell tCell = titleRow.getCell(i);
			if (tCell != null && tCell.getCellType() != Cell.CELL_TYPE_BLANK) {
				tCell.setCellType(Cell.CELL_TYPE_STRING);
				cell = row.getCell(i); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.addFlexibleField(tCell.getStringCellValue(), cell.getStringCellValue());}			
			}
		}
		return true;
	}
	
	private String getST(Exception e, boolean getTrace) {
		String result = e.getMessage() + "\n";
		if (getTrace) {
			StackTraceElement[] ste = e.getStackTrace();
			if (ste != null) {
				for (StackTraceElement stres : ste) {
					result += stres + "\n";
				}
			}
		}
		return result;
	}

	@Override
	public String doImport(final String filename, final JProgressBar progress, boolean showResults) {
		Runnable runnable = new Runnable() {
			public void run() {
				System.err.println("Importing " + filename);
				logMessages += "Importing " + filename + "\n";
				try {
					if (progress != null) {
						progress.setVisible(true);
						progress.setStringPainted(true);
						progress.setString("Importiere Lieferketten Datei...");
						progress.setMinimum(0);
					}

					InputStream is = null;
					if (filename.startsWith("http://")) {
						URL url = new URL(filename);
						URLConnection uc = url.openConnection();
						is = uc.getInputStream();
					} else if (filename.startsWith("/org/hsh/bfr/db/res/")) {
						is = getClass().getResourceAsStream(filename);
					} else {
						is = new FileInputStream(filename);
					}

					XSSFWorkbook wb = new XSSFWorkbook(is);

					doImport(wb, filename);

					DBKernel.myDBi.getTable("Station").doMNs();
					DBKernel.myDBi.getTable("Produktkatalog").doMNs();
					DBKernel.myDBi.getTable("Chargen").doMNs();
					DBKernel.myDBi.getTable("Lieferungen").doMNs();
					if (progress != null) {
						// Refreshen:
						MyDBTable myDB = DBKernel.mainFrame.getMyList().getMyDBTable();
						if (myDB.getActualTable() != null) {
							String actTablename = myDB.getActualTable().getTablename();
							if (actTablename.equals("Produktkatalog") || actTablename.equals("Lieferungen") || actTablename.equals("Station") || actTablename.equals("Chargen")) {
								myDB.setTable(myDB.getActualTable());
							}
						}
						progress.setVisible(false);
					}

				} catch (Exception e) {
					DBKernel.sendRequest("ROLLBACK", false);
					DBKernel.sendRequest("SET AUTOCOMMIT TRUE", false);
					logMessages += "\nUnable to import file '" + filename + "'.\nWrong file format?\nImporter says: \n" + e.toString() + "\n" + getST(e, true) + "\n\n";
					MyLogger.handleException(e);
				}
				System.err.println("Importing - Fin");
				logMessages += "Importing - Fin" + "\n\n";
				
				BackTraceGenerator btg = new BackTraceGenerator();
				btg.save("");
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			logMessages += "\nUnable to run thread for '" + filename + "'.\nWrong file format?\nImporter says: \n" + e.toString() + "\n" + getST(e, true) + "\n\n";
			MyLogger.handleException(e);
		}
		return null;
	}

	private String getExtension(File f) {
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 && i < s.length() - 1) return s.substring(i + 1).toLowerCase();
		return "";
	}
	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) return true;

		String extension = getExtension(f);
		if ((extension.equals("xlsx"))) return true;
		return false;
	}

	@Override
	public String getDescription() {
		return "Very new BfR-Lieferketten Datei (*.xlsx)";
	}
}
