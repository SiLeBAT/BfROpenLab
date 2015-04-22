package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

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

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;
import de.bund.bfr.knime.openkrise.db.imports.MyImporter;

public class BackTraceImporter extends FileFilter implements MyImporter {

	private String logMessages = "";
	private int classRowIndex = -1;
	
	public String getLogMessages() {
		return logMessages;
	}
	public void doImport(Workbook wb, String filename) throws Exception {
		Sheet businessSheet = wb.getSheet("Stations");
		
		Sheet inSheet = wb.getSheet("Deliveries2Lot");
		Sheet lotSheet = wb.getSheet("Lot");
		Sheet outSheet = wb.getSheet("Deliveries");
		
		if (inSheet != null && lotSheet != null && outSheet != null) {
			// load all Stations
			HashMap<String, Station> stations = new HashMap<>();
			int numRows = businessSheet.getLastRowNum() + 1;
			Row titleRow = businessSheet.getRow(0);
			for (classRowIndex=1;classRowIndex<numRows;classRowIndex++) {
				Station s = getStation(titleRow, businessSheet.getRow(classRowIndex));
				if (s == null) break;
				stations.put(s.getLookup(), s);
			}
			// load all Lots
			HashMap<String, Lot> lots = new HashMap<>();
			numRows = lotSheet.getLastRowNum() + 1;
			titleRow = lotSheet.getRow(0);
			for (classRowIndex=2;classRowIndex<numRows;classRowIndex++) {
				Lot l = getMultiLot(stations, titleRow, lotSheet.getRow(classRowIndex));
				if (l == null) break;
				lots.put(l.getLookup(), l);
			}
			// load all Deliveries
			HashMap<String, Delivery> deliveries = new HashMap<>();
			numRows = outSheet.getLastRowNum() + 1;
			titleRow = outSheet.getRow(0);
			for (classRowIndex=2;classRowIndex<numRows;classRowIndex++) {
				Delivery d = getMultiOutDelivery(stations, lots, titleRow, outSheet.getRow(classRowIndex));
				if (d == null) break;
				deliveries.put(d.getLookup(), d);
			}
			
			// load Recipes
			HashSet<DeliveryLot> recipes = new HashSet<>();
			numRows = inSheet.getLastRowNum() + 1;
			titleRow = inSheet.getRow(0);
			for (classRowIndex=1;classRowIndex<numRows;classRowIndex++) {
				DeliveryLot dl = getDeliveryLot(lots, deliveries, titleRow, inSheet.getRow(classRowIndex));
				if (dl == null) break;
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
		Sheet transactionSheet = wb.getSheet("BackTracing");

		Row row = transactionSheet.getRow(0);
		
		// Station in focus
		Station sif = getStation(businessSheet, row.getCell(1).getStringCellValue());
		
		// Delivery(s) Outbound
		classRowIndex = 5;
		HashMap<String, Delivery> outDeliveries = new HashMap<>(); 
		HashMap<String, Lot> outLots = new HashMap<>(); 
		Row titleRow = transactionSheet.getRow(classRowIndex - 2);
		for (;;classRowIndex++) {
			row = transactionSheet.getRow(classRowIndex);
			if (row == null) continue;
			if (isBlockEnd(row, 13, "Reporter Information")) break;
			Delivery d = getDelivery(businessSheet, sif, row, true, titleRow);
			outDeliveries.put(d.getId(), d);
			outLots.put(d.getLot().getNumber(), d.getLot());
		}
		
		// Metadata on Reporter
		classRowIndex = getNextBlockRowIndex(transactionSheet, classRowIndex, "Reporter Information") + 2;
		row = transactionSheet.getRow(classRowIndex);
		MetaInfo mi = getMetaInfo(row);
		mi.setFilename(filename);
		
		// Lot(s)
		classRowIndex = getNextBlockRowIndex(transactionSheet, classRowIndex, "Lot Information") + 3;
		titleRow = transactionSheet.getRow(classRowIndex - 2);
		for (;;classRowIndex++) {
			row = transactionSheet.getRow(classRowIndex);
			if (row == null) continue;
			if (isBlockEnd(row, 13, "Ingredients for Lot(s)")) break;
			if (!fillLot(row, outLots, titleRow)) throw new Exception("Lot number unknown in Row number " + (classRowIndex + 1));
		}
		
		// Deliveries/Recipe Inbound
		classRowIndex = getNextBlockRowIndex(transactionSheet, classRowIndex, "Ingredients for Lot(s)") + 3;
		HashSet<Delivery> inDeliveries = new HashSet<>(); 
		int numRows = transactionSheet.getLastRowNum() + 1;
		titleRow = transactionSheet.getRow(classRowIndex - 2);
		for (;classRowIndex < numRows;classRowIndex++) {
			row = transactionSheet.getRow(classRowIndex);
			if (row == null) continue;
			if (isBlockEnd(row, 13, null)) break;
			Delivery d = getDelivery(businessSheet, sif, row, false, titleRow);
			if (d.getTargetLotId() == null) throw new Exception("Lot number unknown in Row number " + (classRowIndex + 1));
			inDeliveries.add(d);
		}
		
		// what are the insertRessources (new BfR-Format -> then SupplyChain-Reader has to look for other IDcolumns, i.e. Serial)
		// requests erzeugen
		// forward tracing importieren
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
		Cell cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setReporter(getStr(cell.getStringCellValue()));}
		cell = row.getCell(1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDate(getStr(cell.getStringCellValue()));}
		cell = row.getCell(2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setRemarks(getStr(cell.getStringCellValue()));}
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
		Cell cell = row.getCell(0);
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING);
			String id = getStr(cell.getStringCellValue());
			if (id == null) return null;
			result.setId(id);
		}
		else return null;
		cell = row.getCell(1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setName(getStr(cell.getStringCellValue()));}
		cell = row.getCell(2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setStreet(getStr(cell.getStringCellValue()));}
		cell = row.getCell(3); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setNumber(getStr(cell.getStringCellValue()));}
		cell = row.getCell(4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setZip(getStr(cell.getStringCellValue()));}
		cell = row.getCell(5); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setCity(getStr(cell.getStringCellValue()));}
		cell = row.getCell(6); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDistrict(getStr(cell.getStringCellValue()));}
		cell = row.getCell(7); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setState(getStr(cell.getStringCellValue()));}
		cell = row.getCell(8); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setCountry(getStr(cell.getStringCellValue()));}
		cell = row.getCell(9); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setTypeOfBusiness(getStr(cell.getStringCellValue()));}
		cell = row.getCell(10); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setLookup(getStr(cell.getStringCellValue()));}
		
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
		Cell cell = row.getCell(0);
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING);
			Delivery d = deliveries.get(getStr(cell.getStringCellValue()));
			if (d == null) return null;
			result.setIngredient(d);
		}
		else {
			return null;
		}
		cell = row.getCell(1);
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING);
			Lot l = lots.get(getStr(cell.getStringCellValue()));
			if (l == null) return null;
			result.setTargetLot(l);
		}
		else {
			return null;
		}
		
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
		Cell cell = row.getCell(0);
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING); 
			Lot l = lots.get(getStr(cell.getStringCellValue()));
			if (l == null) return null;
			result.setLot(l);
		}
		else {
			return null;
		}
		cell = row.getCell(1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureDay(getInt(cell.getStringCellValue()));}
		cell = row.getCell(2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureMonth(getInt(cell.getStringCellValue()));}
		cell = row.getCell(3); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureYear(getInt(cell.getStringCellValue()));}
		cell = row.getCell(4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setArrivalDay(getInt(cell.getStringCellValue()));}
		cell = row.getCell(5); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setArrivalMonth(getInt(cell.getStringCellValue()));}
		cell = row.getCell(6); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setArrivalYear(getInt(cell.getStringCellValue()));}
		cell = row.getCell(7); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setUnitNumber(getDbl(cell.getStringCellValue()));}
		cell = row.getCell(8); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setUnitUnit(getStr(cell.getStringCellValue()));}
		cell = row.getCell(9);
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING); 
			Station s = stations.get(getStr(cell.getStringCellValue()));
			if (s == null) return null;
			result.setReceiver(s);
		}
		else {
			return null;
		}
		cell = row.getCell(10); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setLookup(getStr(cell.getStringCellValue()));}
		
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
		Cell cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); p.setName(getStr(cell.getStringCellValue()));}
		cell = row.getCell(1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); p.setNumber(getStr(cell.getStringCellValue()));}
		Lot l = new Lot();
		l.setProduct(p);
		cell = row.getCell(2); if (cell != null) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setNumber(getStr(cell.getStringCellValue()));}
		Delivery result = new Delivery();
		result.setLot(l);
		if (!outbound) result.setReceiver(sif);
		cell = row.getCell(3); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureDay(getInt(cell.getStringCellValue()));}
		cell = row.getCell(4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureMonth(getInt(cell.getStringCellValue()));}
		cell = row.getCell(5); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureYear(getInt(cell.getStringCellValue()));}
		cell = row.getCell(6); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setArrivalDay(getInt(cell.getStringCellValue()));}
		cell = row.getCell(7); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setArrivalMonth(getInt(cell.getStringCellValue()));}
		cell = row.getCell(8); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setArrivalYear(getInt(cell.getStringCellValue()));}
		cell = row.getCell(9); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setUnitNumber(getDbl(cell.getStringCellValue()));}
		cell = row.getCell(10); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setUnitUnit(getStr(cell.getStringCellValue()));}
		cell = row.getCell(11); 
		if (outbound && cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setReceiver(getStation(businessSheet, cell.getStringCellValue()));}
		if (!outbound && cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); p.setStation(getStation(businessSheet, cell.getStringCellValue()));}
		cell = row.getCell(12);
		if (outbound && cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setId(getStr(cell.getStringCellValue()));}
		if (!outbound && cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setTargetLotId(getStr(cell.getStringCellValue()));}
		
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
		Cell cell = row.getCell(0);
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING); 
			Station s = stations.get(getStr(cell.getStringCellValue()));
			if (s == null) return null;
			p.setStation(s);
		}
		else {
			return null;
		}
		cell = row.getCell(1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); p.setName(getStr(cell.getStringCellValue()));}
		cell = row.getCell(2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); p.setNumber(getStr(cell.getStringCellValue()));}
		Lot l = new Lot();
		l.setProduct(p);
		cell = row.getCell(3); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setNumber(getStr(cell.getStringCellValue()));}
		cell = row.getCell(4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setUnitNumber(getDbl(cell.getStringCellValue()));}
		cell = row.getCell(5); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setUnitUnit(getStr(cell.getStringCellValue()));}
		cell = row.getCell(6); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setProductionDay(getInt(cell.getStringCellValue()));}
		cell = row.getCell(7); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setProductionMonth(getInt(cell.getStringCellValue()));}
		cell = row.getCell(8); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setProductionYear(getInt(cell.getStringCellValue()));}
		cell = row.getCell(9); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setExpiryDay(getInt(cell.getStringCellValue()));}
		cell = row.getCell(10); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setExpiryMonth(getInt(cell.getStringCellValue()));}
		cell = row.getCell(11); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setExpiryYear(getInt(cell.getStringCellValue()));}
		cell = row.getCell(12); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.getProduct().setTreatment(getStr(cell.getStringCellValue()));}
		cell = row.getCell(13); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setSampling(getStr(cell.getStringCellValue()));}
		cell = row.getCell(14); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setLookup(getStr(cell.getStringCellValue()));}
		
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
	private Integer getInt(String val) {
		Integer result = null;
		if (!val.trim().isEmpty()) result = Integer.parseInt(val);
		return result;
	}
	private Double getDbl(String val) {
		Double result = null;
		if (!val.trim().isEmpty()) result = Double.parseDouble(val.trim());
		return result;
	}
	private String getStr(String val) {
		if (val.trim().isEmpty()) return null;
		return val;
	}
	private boolean fillLot(Row row, HashMap<String, Lot> outLots, Row titleRow) {
		Lot l = null;
		Cell cell = row.getCell(12); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l = outLots.get(getStr(cell.getStringCellValue()));}
		if (l == null) return false;
		//cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setNumber(getStr(cell.getStringCellValue()));}
		cell = row.getCell(1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setUnitNumber(getDbl(cell.getStringCellValue()));}
		cell = row.getCell(2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setUnitUnit(getStr(cell.getStringCellValue()));}
		cell = row.getCell(3); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setProductionDay(getInt(cell.getStringCellValue()));}
		cell = row.getCell(4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setProductionMonth(getInt(cell.getStringCellValue()));}
		cell = row.getCell(5); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setProductionYear(getInt(cell.getStringCellValue()));}
		cell = row.getCell(6); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setExpiryDay(getInt(cell.getStringCellValue()));}
		cell = row.getCell(7); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setExpiryMonth(getInt(cell.getStringCellValue()));}
		cell = row.getCell(8); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setExpiryYear(getInt(cell.getStringCellValue()));}
		cell = row.getCell(9); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.getProduct().setTreatment(getStr(cell.getStringCellValue()));}
		cell = row.getCell(11); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setSampling(getStr(cell.getStringCellValue()));}
		
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
					} else if (filename.startsWith("/de/bund/bfr/knime/openkrise/db/")) {
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
					logMessages += "\nUnable to import file '" + filename + "'.\nWrong file format?\nLast row analyzed: " + (classRowIndex+1) + "\nImporter says: \n" + e.toString() + "\n" + getST(e, true) + "\n\n";
					MyLogger.handleException(e);
				}
				System.err.println("Importing - Fin");
				logMessages += "Importing - Fin" + "\n\n";
				
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
