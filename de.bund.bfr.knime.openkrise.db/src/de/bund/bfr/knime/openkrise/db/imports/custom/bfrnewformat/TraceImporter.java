package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JProgressBar;
import javax.swing.filechooser.FileFilter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;
import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.MyDBTable;
import de.bund.bfr.knime.openkrise.db.imports.MyImporter;

public class TraceImporter extends FileFilter implements MyImporter {

	private MyDBI mydbi;
	
	public TraceImporter() {
		this.mydbi = null;
	}
	public TraceImporter(MyDBI mydbi) {
		this.mydbi = mydbi;
	}
	
	private String logMessages = "";
	private String logWarnings = "";
	private Map<String, Set<String>> warns = new HashMap<>();
	
	public Map<String, Set<String>> getLastWarnings() {
		return warns;
	}
	public String getLogWarnings() {
		return logWarnings;
	}
	private int classRowIndex = -1;
	
	public String getLogMessages() {
		return logMessages;
	}
	private void checkStationsFirst(List<Exception> exceptions, Sheet businessSheet) {
		HashSet<String> stationIDs = new HashSet<>();
		int numRows = businessSheet.getLastRowNum() + 1;
		for (int i=1;i<numRows;i++) {
			Row row = businessSheet.getRow(i);
			if (row != null) {
				Cell cell = row.getCell(0); // ID
				Cell cell1 = row.getCell(1); // Name
				if ((cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) && (cell1 == null || cell1.getCellType() == Cell.CELL_TYPE_BLANK)) return;
				if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) exceptions.add(new Exception("Station has no ID??? -> Row " + (i+1)));
				cell.setCellType(Cell.CELL_TYPE_STRING);
				String val = cell.getStringCellValue();
				if (stationIDs.contains(val)) exceptions.add(new Exception("Station ID '" + val + "' is defined more than once -> Row " + (i+1)));
				stationIDs.add(val);
			}
		}
	}
	private void checkDeliveriesFirst(List<Exception> exceptions, Sheet deliverySheet) {
		HashSet<String> deliveryIDs = new HashSet<>();
		int numRows = deliverySheet.getLastRowNum() + 1;
		for (int i=2;i<numRows;i++) {
			Row row = deliverySheet.getRow(i);
			if (row != null) {
				Cell cell = row.getCell(0); // ID
				Cell cell1 = row.getCell(1); // Station
				if ((cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) && (cell1 == null || cell1.getCellType() == Cell.CELL_TYPE_BLANK)) return;
				if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) exceptions.add(new Exception("Delivery has no ID??? -> Row " + (i+1)));
				cell.setCellType(Cell.CELL_TYPE_STRING);
				String val = cell.getStringCellValue();
				if (deliveryIDs.contains(val)) exceptions.add(new Exception("Delivery ID '" + val + "' is defined more than once -> Row " + (i+1)));
				deliveryIDs.add(val);
			}
		}
	}
	private void loadLookupSheet(Sheet lookupSheet) {
		LookUp lu = new LookUp();
		int numRows = lookupSheet.getLastRowNum() + 1;
		for (int i=1;i<numRows;i++) {
			Row row = lookupSheet.getRow(i);
			if (row != null) {
				Cell cell = row.getCell(0); // Sampling
				if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					lu.addSampling(cell.getStringCellValue());
				}
				cell = row.getCell(1); // TypeOfBusiness
				if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					lu.addTypeOfBusiness(cell.getStringCellValue());
				}
				cell = row.getCell(2); // Treatment
				if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					lu.addTreatment(cell.getStringCellValue());
				}
				cell = row.getCell(3); // Units
				if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					lu.addUnit(cell.getStringCellValue());
				}
			}
		}
		lu.intoDb(mydbi);
	}
	private List<Exception> doTheImport(Workbook wb, String filename) { //  throws Exception
		List<Exception> exceptions = new ArrayList<>();
		
		Sheet stationSheet = wb.getSheet("Stations");
		Sheet deliverySheet = wb.getSheet("Deliveries");
		Sheet d2dSheet = wb.getSheet("Deliveries2Deliveries");
		Sheet transactionSheet = wb.getSheet("BackTracing");
		Sheet lookupSheet = wb.getSheet("LookUp");
		Sheet forwardSheet = wb.getSheet("Opt_ForwardTracing");
		Sheet forwardSheetNew = wb.getSheet("ForwardTracing_Opt");
		Sheet forSheet = wb.getSheet("ForTracing");
		
		boolean isForTracing = forSheet != null;
		if (isForTracing) transactionSheet = forSheet;
		
		if (stationSheet == null || transactionSheet == null && deliverySheet == null) {
			exceptions.add(new Exception("Wrong template format!"));
			return exceptions;
		}

		checkStationsFirst(exceptions, stationSheet);
		
		if (deliverySheet != null) {
			checkDeliveriesFirst(exceptions, deliverySheet);
			// load all Stations
			HashMap<String, Station> stations = new HashMap<>();
			int numRows = stationSheet.getLastRowNum() + 1;
			Row titleRow = stationSheet.getRow(0);
			for (classRowIndex=1;classRowIndex<numRows;classRowIndex++) {
				Station s = getStation(titleRow, stationSheet.getRow(classRowIndex));
				if (s == null) break;
				if (stations.containsKey(s.getId())) exceptions.add(new Exception("Station defined twice??? -> Row " + (classRowIndex+1) + "; Station Id: '" + s.getId() + "'"));
				stations.put(s.getId(), s);
			}
			// load all Deliveries
			HashMap<String, Delivery> deliveries = new HashMap<>();
			numRows = deliverySheet.getLastRowNum() + 1;
			titleRow = deliverySheet.getRow(0);
			HashMap<String,String> definedLots = new HashMap<>();
			for (classRowIndex=2;classRowIndex<numRows;classRowIndex++) {
				Delivery d = getMultiOutDelivery(exceptions, stations, titleRow, deliverySheet.getRow(classRowIndex), definedLots, classRowIndex, filename, d2dSheet != null);
				if (d == null) break;
				if (deliveries.containsKey(d.getId())) exceptions.add(new Exception("Delivery defined twice??? -> Row " + (classRowIndex+1) + "; Delivery Id: '" + d.getId() + "'"));
				deliveries.put(d.getId(), d);
			}
			
			// load Recipes
			HashSet<D2D> recipes = new HashSet<>();
			if (d2dSheet != null) {
				numRows = d2dSheet.getLastRowNum() + 1;
				titleRow = d2dSheet.getRow(0);
				for (classRowIndex=1;classRowIndex<numRows;classRowIndex++) {
					D2D dl = getD2D(exceptions, deliveries, titleRow, d2dSheet.getRow(classRowIndex), classRowIndex);
					if (dl == null) break;
					recipes.add(dl);
				}
			}

			MetaInfo mi = new MetaInfo();
			mi.setFilename(filename);
			
			//DBKernel.sendRequest("SET AUTOCOMMIT FALSE", false);
			if (lookupSheet != null) loadLookupSheet(lookupSheet);
			Integer miDbId = null;
			try {
				miDbId = mi.getID(mydbi);
			} catch (Exception e) {
				exceptions.add(e);
			}
			if (miDbId == null) exceptions.add(new Exception("File already imported"));
			for (Delivery d : deliveries.values()) {
				try {
					d.getID(miDbId, false, mydbi);
				} catch (Exception e) {
					exceptions.add(e);
				}
				if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
			}
			
			HashMap<Delivery, HashSet<Integer>> ingredients = new HashMap<>(); 
			for (D2D dl : recipes) {
				try {
					dl.getId(miDbId, mydbi);
				} catch (Exception e) {
					exceptions.add(e);
				}
				
				// collect data for checks if data is missing...
				Delivery d = dl.getTargetDelivery();
				if (!ingredients.containsKey(d)) ingredients.put(d, new HashSet<Integer>());
				HashSet<Integer> hd = ingredients.get(d);
				hd.add(dl.getIngredient().getDbId());
			}

			return exceptions;
		}
		
		Row row = transactionSheet.getRow(0);
		Row titleRow;
		Cell cell;
		HashMap<String, Delivery> outDeliveries = new HashMap<>(); 
		HashMap<String, Lot> outLots = new HashMap<>(); 
		Station sif;
		MetaInfo mi;
				
		if (forwardSheet != null) {
			// Station in focus
			cell = row.getCell(1);
			if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) exceptions.add(new Exception("Station in Focus not defined"));
			cell.setCellType(Cell.CELL_TYPE_STRING);
			sif = getStation(exceptions, stationSheet, cell.getStringCellValue(), row);
			
			// Delivery(s) Outbound
			classRowIndex = 5;
			titleRow = transactionSheet.getRow(classRowIndex - 2);
			for (;;classRowIndex++) {
				row = transactionSheet.getRow(classRowIndex);
				if (row == null) continue;
				if (isBlockEnd(row, 13, "Reporter Information")) break;
				Delivery d = getDelivery(exceptions, stationSheet, sif, row, true, titleRow, filename, false, null, outDeliveries, false);
				if (d == null) continue;
				outDeliveries.put(d.getId(), d);
				outLots.put(d.getLot().getNumber(), d.getLot());
			}
			
			// Metadata on Reporter
			classRowIndex = getNextBlockRowIndex(transactionSheet, classRowIndex, "Reporter Information") + 2;
			row = transactionSheet.getRow(classRowIndex);
			mi = getMetaInfo(exceptions, row);
			mi.setFilename(filename);
		}
		else { // Reporter shifted to the top
			// Metadata on Reporter
			classRowIndex = getNextBlockRowIndex(transactionSheet, 0, "Reporter Information") + 2;
			row = transactionSheet.getRow(classRowIndex);
			mi = getMetaInfo(exceptions, row);
			mi.setFilename(filename);

			// Station in focus
			classRowIndex = getNextBlockRowIndex(transactionSheet, classRowIndex, "Station in Focus:");
			row = transactionSheet.getRow(classRowIndex);
			cell = row.getCell(1);
			if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) exceptions.add(new Exception("Station in Focus not defined"));
			cell.setCellType(Cell.CELL_TYPE_STRING);
			sif = getStation(exceptions, stationSheet, cell.getStringCellValue(), row);
			
			String label = "Products Out";
			if (isForTracing) label = "Ingredients In for Lot(s)";
			// Delivery(s) Outbound
			classRowIndex = getNextBlockRowIndex(transactionSheet, classRowIndex, label) + 3;
			titleRow = transactionSheet.getRow(classRowIndex - 2);
			for (;;classRowIndex++) {
				row = transactionSheet.getRow(classRowIndex);
				if (row == null) continue;
				if (isBlockEnd(row, 13, "Lot Information")) break;
				Delivery d = getDelivery(exceptions, stationSheet, sif, row, !isForTracing, titleRow, filename, isForTracing, outLots, outDeliveries, false);
				if (d == null) continue;
				outDeliveries.put(d.getId(), d);
				if (!isForTracing) outLots.put(d.getLot().getNumber(), d.getLot());
			}			
		}
		
		String label = "Ingredients In for Lot(s)";
		if (isForTracing) label = "Products Out";
		// Lot(s)
		classRowIndex = getNextBlockRowIndex(transactionSheet, classRowIndex, "Lot Information") + 3;
		titleRow = transactionSheet.getRow(classRowIndex - 2);
		for (;;classRowIndex++) {
			row = transactionSheet.getRow(classRowIndex);
			if (row == null) continue;
			if (isBlockEnd(row, 13, label)) break;
			if (!fillLot(exceptions, row, sif, outLots, titleRow, isForTracing ? outDeliveries : null, classRowIndex + 1)) {
				exceptions.add(new Exception("Lot number unknown in Row number " + (classRowIndex + 1)));
			}
		}
		
		// Deliveries/Recipe Inbound
		boolean hasIngredients = false;
		label = "Ingredients for Lot(s)";
		if (isForTracing) label = "Products Out";
		classRowIndex = getNextBlockRowIndex(transactionSheet, classRowIndex, label) + 3;
		HashMap<String, Delivery> inDeliveries = new HashMap<>(); 
		int numRows = transactionSheet.getLastRowNum() + 1;
		titleRow = transactionSheet.getRow(classRowIndex - 2);
		for (;classRowIndex < numRows;classRowIndex++) {
			row = transactionSheet.getRow(classRowIndex);
			if (row == null) continue;
			if (isBlockEnd(row, 13, null)) break;
			Delivery d = getDelivery(exceptions, stationSheet, sif, row, isForTracing, titleRow, filename, isForTracing, outLots, inDeliveries, false);
			if (d == null) continue;
			if (!isForTracing && d.getTargetLotIds().size() == 0) exceptions.add(new Exception("Lot number unknown in Row number " + (classRowIndex + 1)));
			inDeliveries.put(d.getId(), d);
			hasIngredients = true;
		}
		if (!hasIngredients) {
			logWarnings += "No " + (isForTracing ? "Products Out" : "ingredients") + " defined...\n";
		}
		
		// Opt_ForwardTracing
		HashSet<Delivery> forwDeliveries = new HashSet<>(); 
		if (!isForTracing) {
			if (forwardSheet == null) forwardSheet = forwardSheetNew;
			numRows = forwardSheet.getLastRowNum() + 1;
			titleRow = forwardSheet.getRow(0);
			for (classRowIndex=2;classRowIndex < numRows;classRowIndex++) {
				row = transactionSheet.getRow(classRowIndex);
				if (row == null) continue;
				Delivery d = getForwardDelivery(exceptions, stationSheet, outLots, titleRow, forwardSheet.getRow(classRowIndex));
				if (d == null) continue;
				forwDeliveries.add(d);
			}
		}
		
		// what are the insertRessources (new BfR-Format -> then SupplyChain-Reader has to look for other IDcolumns, i.e. Serial)
		// forward tracing importieren
		// welche Reihenfolge to insert? Was, wenn beim Import was schiefgeht?
		//DBKernel.sendRequest("SET AUTOCOMMIT FALSE", false);
		if (lookupSheet != null) loadLookupSheet(lookupSheet);
		Integer miDbId = null;
		try {
			miDbId = mi.getID(mydbi);
		} catch (Exception e) {
			exceptions.add(e);
		}
		if (miDbId == null) exceptions.add(new Exception("File already imported"));
		if (isForTracing)
			try {
				insertForIntoDb(miDbId, inDeliveries, outDeliveries);
			} catch (Exception e) {
				exceptions.add(e);
			}
		else
			try {
				insertIntoDb(miDbId, inDeliveries, outDeliveries, forwDeliveries);
			} catch (Exception e) {
				exceptions.add(e);
			}
		//DBKernel.sendRequest("COMMIT", false);
		//DBKernel.sendRequest("SET AUTOCOMMIT TRUE", false);
		
		return exceptions;
	}
	private void insertForIntoDb(Integer miDbId, HashMap<String, Delivery> outDeliveries, HashMap<String, Delivery> inDeliveries) throws Exception {
		HashMap<String, Integer> lotDbNumber = new HashMap<>();
		for (Delivery d : outDeliveries.values()) {
			d.getID(miDbId, false, mydbi);
			if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
			lotDbNumber.put(d.getLot().getNumber(), d.getLot().getDbId());
		}
		for (Delivery d : inDeliveries.values()) {
			Integer dbId = d.getID(miDbId, true, mydbi);
			if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
			for (String targetLotId : d.getTargetLotIds()) {
				if (lotDbNumber.containsKey(targetLotId)) {
					new D2D().getId(dbId, lotDbNumber.get(targetLotId), miDbId, mydbi);
				}
			}
		}
	}
	private void insertIntoDb(Integer miDbId, HashMap<String, Delivery> inDeliveries, HashMap<String, Delivery> outDeliveries, HashSet<Delivery> forwDeliveries) throws Exception {
		HashMap<String, Lot> lotDbNumber = new HashMap<>();
		for (Delivery d : outDeliveries.values()) {
			d.getID(miDbId, true, mydbi);
			if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
			if (lotDbNumber.containsKey(d.getLot().getNumber()) && lotDbNumber.get(d.getLot().getNumber()).getDbId().intValue() != d.getLot().getDbId()) {
				Lot ol = lotDbNumber.get(d.getLot().getNumber());
				if (d.getLot().getDbId() != null && d.getLot().getProduct() != null && d.getLot().getProduct().getName() != null &&
						ol.getProduct() != null && d.getLot().getProduct().getName().equals(ol.getProduct().getName())) {
					d.mergeLot(d.getLot().getDbId(), ol.getDbId(), mydbi);
				}
				/*
				else {
					throw new Exception("Lot Numbers of different lots are the same in 'Products Out'!");
				}
				*/
			}
			else lotDbNumber.put(d.getLot().getNumber(), d.getLot());
		}
		for (Delivery d : inDeliveries.values()) {
			Integer dbId = d.getID(miDbId, false, mydbi);
			if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
			for (String targetLotId : d.getTargetLotIds()) {
				if (lotDbNumber.containsKey(targetLotId)) {
					new D2D().getId(dbId, lotDbNumber.get(targetLotId).getDbId(), miDbId, mydbi);
				}
			}
		}
		for (Delivery d : forwDeliveries) {
			d.getID(miDbId, false, mydbi);
			if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
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
	
	private static MetaInfo getMetaInfo(List<Exception> exceptions, Row row) {
		if (row == null) return null;
		MetaInfo result = new MetaInfo();
		Cell cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setReporter(getStr(cell.getStringCellValue()));}
		cell = row.getCell(1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING);
			result.setDate(getStr(cell.getStringCellValue()));
			long millis = getMs(result);
			if (millis == 0) {
				if (exceptions != null) exceptions.add(new Exception("Reporting date not defined or in wrong format. This is mandatory! Supported formats are at the moment 'yyyy-MM-dd' and 'dd.MM.yyyy', e.g. 2015-12-11 or 12.11.2015!"));
			}
		}
		cell = row.getCell(2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setRemarks(getStr(cell.getStringCellValue()));}
		return result;
	}
	private Station getStation(List<Exception> exceptions, Sheet businessSheet, String lookup, Row srcrow) {
		Station result = null;
		int numRows = businessSheet.getLastRowNum() + 1;
		for (int i=0;i<numRows;i++) {
			Row row = businessSheet.getRow(i);
			if (row != null) {
				Cell cell = row.getCell(0);
				if (cell.getStringCellValue().equals(lookup)) {
					result = getStation(businessSheet.getRow(0), row);
					break;
				}
			}
		}
		if (result == null) exceptions.add(new Exception("Station '" + lookup + "' is not correctly defined in Row " + (srcrow.getRowNum() + 1)));
		return result;
	}
	private Station getStation(Row titleRow, Row row) {
		if (row == null) return null;
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
//		cell = row.getCell(10); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setLookup(getStr(cell.getStringCellValue()));}
		
		// Further flexible cells
		for (int ii=10;ii<20;ii++) {
			Cell tCell = titleRow.getCell(ii);
			if (tCell != null && tCell.getCellType() != Cell.CELL_TYPE_BLANK) {
				tCell.setCellType(Cell.CELL_TYPE_STRING);
				cell = row.getCell(ii); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.addFlexibleField(tCell.getStringCellValue(), cell.getStringCellValue());}			
			}
		}
		return result;
	}
	private D2D getD2D(List<Exception> exceptions, HashMap<String, Delivery> deliveries, Row titleRow, Row row, int rowNum) {
		if (row == null) return null;
		D2D result = new D2D();
		Cell cell = row.getCell(0);
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING);
			String did = getStr(cell.getStringCellValue());
			Delivery d = deliveries.get(did);
			if (d == null) exceptions.add(new Exception("Delivery ID in sheet Deliveries2Deliveries not defined in deliveries sheet: '" + did + "'; -> Row " + (rowNum+1)));
			result.setIngredient(d);
		}
		else {
			return null;
		}
		cell = row.getCell(1);
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING);
			String did = getStr(cell.getStringCellValue());
			Delivery d = deliveries.get(did);
			if (d == null) exceptions.add(new Exception("Delivery ID in sheet Deliveries2Deliveries not defined in deliveries sheet: '" + did + "'; -> Row " + (rowNum+1)));
			result.setTargetDelivery(d);
		}
		else {
			return null;
		}
		if (result.getIngredient() != null && result.getTargetDelivery() != null) {
			if (!result.getIngredient().getReceiver().getId().equals(result.getTargetDelivery().getLot().getProduct().getStation().getId())) {
				exceptions.add(new Exception("Recipient does not match Supplier; in sheet Deliveries2Deliveries: '" + result.getIngredient().getId() + "' -> '" + result.getTargetDelivery().getId() + "'; -> Row " + (rowNum+1)));
			}
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
	private Delivery getForwardDelivery(List<Exception> exceptions, Sheet stationSheet, HashMap<String, Lot> lots, Row titleRow, Row row) {
		if (row == null) return null;
		Lot l = null;
		Cell cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l=lots.get(getStr(cell.getStringCellValue()));}
		if (l == null) return null;
		Delivery result = new Delivery();
		result.setLot(l);
		cell = row.getCell(1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureDay(getInt(cell.getStringCellValue()));}
		cell = row.getCell(2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureMonth(getInt(cell.getStringCellValue()));}
		cell = row.getCell(3); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureYear(getInt(cell.getStringCellValue()));}
		cell = row.getCell(4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setUnitNumber(getDbl(cell.getStringCellValue()));}
		cell = row.getCell(5); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setUnitUnit(getStr(cell.getStringCellValue()));}
		cell = row.getCell(6);
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING); 
			String ss = getStr(cell.getStringCellValue());
			Station s = getStation(exceptions, stationSheet, ss, row);
			if (s == null) exceptions.add(new Exception("Recipient station '" + ss + "' not correclty defined / not known in Forward Tracing sheet"));
			result.setReceiver(s);
		}
		else {
			exceptions.add(new Exception("No Recipient Station defined in Forward Tracing sheet"));
		}
		result.setId(getNewSerial(l, result));
		
		// Further flexible cells
		for (int i=8;i<25;i++) {
			Cell tCell = titleRow.getCell(i);
			if (tCell != null && tCell.getCellType() != Cell.CELL_TYPE_BLANK) {
				tCell.setCellType(Cell.CELL_TYPE_STRING);
				cell = row.getCell(i); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.addFlexibleField(tCell.getStringCellValue(), cell.getStringCellValue());}			
			}
		}
		return result;
	}
	private String getStr(Cell cell) {
		if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) return null;
		cell.setCellType(Cell.CELL_TYPE_STRING);
		String s = getStr(cell.getStringCellValue());
		return s;
	}
	private Delivery getMultiOutDelivery(List<Exception> exceptions, HashMap<String, Station> stations, Row titleRow, Row row, HashMap<String,String> definedLots,int rowNum, String filename, boolean ignoreMissingLotnumbers) {
		if (row == null) return null;
		Delivery result = new Delivery();
		Cell cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setId(getStr(cell.getStringCellValue()));}
		Product p = new Product();
		cell = row.getCell(1);
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING); 
			String sid = getStr(cell.getStringCellValue());
			Station s = stations.get(sid);
			if (s == null) exceptions.add(new Exception("Station ID in Deliveries not defined in stations sheet: '" + sid + "'; -> Row " + (rowNum+1)));
			p.setStation(s);
		}
		else {
			return null;
		}
		cell = row.getCell(2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); p.setName(getStr(cell.getStringCellValue()));}
		Lot l = new Lot();
		l.setProduct(p);
		cell = row.getCell(3);
		String str = getStr(cell);
		if (str != null) {l.setNumber(str);}
		else if (!ignoreMissingLotnumbers) {exceptions.add(new Exception("Please, do always provide a lot number as this is most helpful! -> Row " + (rowNum+1) + " in '" + filename + "'\n\n"));}
		cell = row.getCell(4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setUnitNumber(getDbl(cell.getStringCellValue()));}
		cell = row.getCell(5); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setUnitUnit(getStr(cell.getStringCellValue()));}
		String lotId = p.getStation().getId() + "_" + p.getName() + "_" + l.getNumber();
		String lotInfo = l.getUnitNumber() + "_" + l.getUnitUnit();
		if (definedLots.containsKey(lotId)) {
			if (!definedLots.get(lotId).equals(lotInfo)) exceptions.add(new Exception("Lot has different quantities??? -> Lot number: '" + l.getNumber() + "'; -> Row " + (rowNum+1)));
		}
		else definedLots.put(lotId, lotInfo);

		result.setLot(l);
		cell = row.getCell(6); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureDay(getInt(cell.getStringCellValue()));}
		cell = row.getCell(7); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureMonth(getInt(cell.getStringCellValue()));}
		cell = row.getCell(8); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureYear(getInt(cell.getStringCellValue()));}
		cell = row.getCell(9); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setArrivalDay(getInt(cell.getStringCellValue()));}
		cell = row.getCell(10); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setArrivalMonth(getInt(cell.getStringCellValue()));}
		cell = row.getCell(11); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setArrivalYear(getInt(cell.getStringCellValue()));}
		cell = row.getCell(12); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setUnitNumber(getDbl(cell.getStringCellValue()));}
		cell = row.getCell(13); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setUnitUnit(getStr(cell.getStringCellValue()));}
		cell = row.getCell(14);
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING); 
			String sid = getStr(cell.getStringCellValue());
			Station s = stations.get(sid);
			if (s == null) exceptions.add(new Exception("Recipient ID in sheet Deliveries not defined in stations sheet: '" + sid + "'; -> Row " + (rowNum+1)));
			result.setReceiver(s);
		}
		else {
			if (result.getId() == null) return null;
			else exceptions.add(new Exception("Recipient ID in sheet Deliveries not defined; -> Row " + (rowNum+1)));
		}
		
		// Further flexible cells
		for (int i=15;i<25;i++) {
			Cell tCell = titleRow.getCell(i);
			if (tCell != null && tCell.getCellType() != Cell.CELL_TYPE_BLANK) {
				tCell.setCellType(Cell.CELL_TYPE_STRING);
				cell = row.getCell(i); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.addFlexibleField(tCell.getStringCellValue(), cell.getStringCellValue());}			
			}
		}
		return result;
	}
	private boolean isCellEmpty(Cell cell) {
		return cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || (cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getStringCellValue().isEmpty());
	}
	private Delivery getDelivery(List<Exception> exceptions, Sheet businessSheet, Station sif, Row row, boolean outbound, Row titleRow, String filename, boolean isForTracing, HashMap<String, Lot> outLots, HashMap<String, Delivery> existingDeliveries, boolean ignoreMissingLotnumbers) {
		Cell cell = row.getCell(12);
		if (isCellEmpty(cell)) {
			Cell cell10 = row.getCell(10); 
			Cell cell0 = row.getCell(0); 
			if ((!isForTracing && !isCellEmpty(cell0)) || !isCellEmpty(cell10)) {
				exceptions.add(new Exception("It is essential to choose the associated Lot number ('Lot Number of " + (isForTracing ? "" : " \"") + "Product" + (isForTracing ? "" : " Out\"") + "') to the delivery in Row number " + (classRowIndex + 1)));
			}
			return null;
		}
		Delivery result = new Delivery();
		String lotDelNumber = null;
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING); lotDelNumber = getStr(cell.getStringCellValue());
			//if (isForTracing && outbound) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setTargetLotId(getStr(cell.getStringCellValue()));}
			if (!isForTracing && outbound) {result.setId(lotDelNumber);}
			if (isForTracing && !outbound) {result.setId(lotDelNumber);}
			if (!isForTracing && !outbound) {result.addTargetLotId(lotDelNumber);}
		}
		Lot l;
		if (isForTracing && outbound) {
			l = outLots.get(lotDelNumber);
		}
		else {
			Product p = new Product();
			if (outbound) p.setStation(sif);
			cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); p.setName(getStr(cell.getStringCellValue()));}
			l = new Lot();
			l.setProduct(p);
			String lotNumber = null;
			cell = row.getCell(1);
			if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); lotNumber = getStr(cell.getStringCellValue());}
			else if (!ignoreMissingLotnumbers) {exceptions.add(new Exception("Please, do always provide a lot number as this is most helpful! -> Row " + (row.getRowNum()+1) + " in '" + filename + "'\n\n"));}
			l.setNumber(lotNumber);
			if (lotNumber == null && p.getName() == null) {
				exceptions.add(new Exception("Lot number and product name undefined in Row number " + (classRowIndex + 1)));
			}
		}
		//cell = row.getCell(1); if (cell != null) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setNumber(getStr(cell.getStringCellValue()));}
		result.setLot(l);
		if (!outbound) result.setReceiver(sif);
		cell = row.getCell(2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureDay(getInt(cell.getStringCellValue()));}
		cell = row.getCell(3); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureMonth(getInt(cell.getStringCellValue()));}
		cell = row.getCell(4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureYear(getInt(cell.getStringCellValue()));}
		cell = row.getCell(5); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setArrivalDay(getInt(cell.getStringCellValue()));}
		cell = row.getCell(6); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setArrivalMonth(getInt(cell.getStringCellValue()));}
		cell = row.getCell(7); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setArrivalYear(getInt(cell.getStringCellValue()));}
		cell = row.getCell(8); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setUnitNumber(getDbl(cell.getStringCellValue()));}
		cell = row.getCell(9); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setUnitUnit(getStr(cell.getStringCellValue()));}
		cell = row.getCell(10); 
		if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || (cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getStringCellValue().isEmpty())) return null;
		if (outbound && cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setReceiver(getStation(exceptions, businessSheet, cell.getStringCellValue(), row));}
		if (!outbound && cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.getProduct().setStation(getStation(exceptions, businessSheet, cell.getStringCellValue(), row)); l.setNumber(l.getNumber());}

		if (!isForTracing && !outbound || isForTracing && outbound) {
			result.setId(getNewSerial(l, result));
			if (existingDeliveries != null && existingDeliveries.containsKey(result.getId())) {
				result.getTargetLotIds().addAll(existingDeliveries.get(result.getId()).getTargetLotIds());
			}
		}
		
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
	private String getNewSerial(Lot l, Delivery d) {
		String newSerial = (l.getProduct() != null ? l.getProduct().getName() : "null") + ";" + l.getNumber() + ";" +
				d.getDepartureDay() + ";" + d.getDepartureMonth() + ";" + d.getDepartureYear() + ";" +
				d.getArrivalDay() + ";" + d.getArrivalMonth() + ";" + d.getArrivalYear() + ";" +
				d.getUnitNumber() + ";" + d.getUnitUnit() + ";" + d.getReceiver().getId();
		return newSerial;
	}
	private Integer getInt(String val) {
		Integer result = null;
		if (!val.trim().isEmpty()) {
			try {
				result = Integer.parseInt(val);
			}
			catch (Exception e) {
				result = (int) Double.parseDouble(val);
			}
		}
		return result;
	}
	private Double getDbl(String val) {
		Double result = null;
		if (!val.trim().isEmpty()) result = Double.parseDouble(val.trim());
		return result;
	}
	private static String getStr(String val) {
		if (val.trim().isEmpty()) return null;
		return val;
	}
	private boolean fillLot(List<Exception> exceptions, Row row, Station sif, HashMap<String, Lot> outLots, Row titleRow, HashMap<String, Delivery> outDeliveries, int rowIndex) {
		Lot l = null;
		String lotNumber = null;
		Cell cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); lotNumber = getStr(cell.getStringCellValue());}	
		l = outLots.get(lotNumber);
		if (l == null) {
			if (outDeliveries != null) {l = new Lot(); l.setNumber(lotNumber); outLots.put(l.getNumber(), l);}
			else return false;
		}
		cell = row.getCell(1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING);
			Double dbl = getDbl(cell.getStringCellValue());
			if (l.getUnitNumber() == null) l.setUnitNumber(dbl);
			else if (l.getUnitNumber().doubleValue() != dbl) {
				exceptions.add(new Exception("Lot information defines same lot number with different quantities??? -> Row " + rowIndex));
			}
		}
		cell = row.getCell(2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING);
			String str = getStr(cell.getStringCellValue());
			if (l.getUnitUnit() == null) l.setUnitUnit(str);
			else if (!l.getUnitUnit().equals(str)) {
				exceptions.add(new Exception("Lot information defines same lot number with different units??? -> Row " + rowIndex));
			}
		}
		if (outDeliveries != null) {
			cell = row.getCell(3); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
				cell.setCellType(Cell.CELL_TYPE_STRING);
				String pr = getStr(cell.getStringCellValue());
				if (l.getProduct() == null) {
					Product p = new Product(); p.setName(pr); l.setProduct(p); p.setStation(sif);
				}
				else if (!l.getProduct().getName().equals(pr)) {
					exceptions.add(new Exception("Lot information defines same lot number with different product names??? -> Row " + rowIndex));
				}
			}
			cell = row.getCell(4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); Delivery d = outDeliveries.get(getStr(cell.getStringCellValue())); if (d == null) return false; d.addTargetLotId(l.getNumber());}
		}
		
		// Further flexible cells
		for (int i=12;i<20;i++) {
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
		if (getTrace)  result = e.toString() + "\n" + result;
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
				InputStream is = null;
				try {
					if (progress != null) {
						progress.setVisible(true);
						progress.setStringPainted(true);
						progress.setString("Importiere Lieferketten Datei...");
						progress.setMinimum(0);
					}

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

					Station.reset(); Lot.reset(); Delivery.reset();
					if (existsDBKernel()) DBKernel.sendRequest("SET AUTOCOMMIT FALSE", false);
					List<Exception> exceptions = doTheImport(wb, filename);
					
					warns = new HashMap<>();
					if (exceptions != null && exceptions.size() > 0) {
						if (existsDBKernel()) {
							warns = de.bund.bfr.knime.openkrise.common.DeliveryUtils.getWarnings(DBKernel.getDBConnection());
							DBKernel.sendRequest("ROLLBACK", false);
							DBKernel.sendRequest("SET AUTOCOMMIT TRUE", false);
						}
						else if (mydbi != null) {
							warns = de.bund.bfr.knime.openkrise.common.DeliveryUtils.getWarnings(mydbi.getConn());
						}
						for (String key : warns.keySet()) {
							logWarnings += "\n" + key + ":\n";
							for (String w : warns.get(key)) {
								logWarnings += w + "\n";
							}
						}
						logMessages += "\nUnable to import file '" + filename + "'.\nImporter says: \n";
						for (Exception e : exceptions) {
							logMessages += getST(e, false);
							MyLogger.handleException(e);
						}
						logMessages += "\n\n";
						if (progress != null) progress.setVisible(false);
						try {
							is.close();
						} catch (IOException e1) {}
					}
					else {
						if (existsDBKernel()) {
							if (exceptions != null) DBKernel.sendRequest("COMMIT", false);
							DBKernel.sendRequest("SET AUTOCOMMIT TRUE", false);
							DBKernel.myDBi.getTable("Station").doMNs();
							DBKernel.myDBi.getTable("Produktkatalog").doMNs();
							DBKernel.myDBi.getTable("Chargen").doMNs();
							DBKernel.myDBi.getTable("Lieferungen").doMNs();
							warns = de.bund.bfr.knime.openkrise.common.DeliveryUtils.getWarnings(DBKernel.getDBConnection());
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
						}
						else if (mydbi != null) {
							warns = de.bund.bfr.knime.openkrise.common.DeliveryUtils.getWarnings(mydbi.getConn());
						}
						for (String key : warns.keySet()) {
							logWarnings += "\n" + key + ":\n";
							for (String w : warns.get(key)) {
								logWarnings += w + "\n";
							}
						}
						is.close();
					}
				} catch (Exception e) {
					if (existsDBKernel()) {
						DBKernel.sendRequest("ROLLBACK", false);
						DBKernel.sendRequest("SET AUTOCOMMIT TRUE", false);
					}
					logMessages += "\nUnable to import file '" + filename + "'.\nImporter says: \n" + getST(e, false) + "\n\n";
					MyLogger.handleException(e);
					if (progress != null) progress.setVisible(false);
					try {
						is.close();
					} catch (IOException e1) {}
				}
				System.err.println("Importing - Fin");
				//logMessages += "Importing - Fin" + "\n\n";
				
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			logMessages += "\nUnable to run thread for '" + filename + "'.\nWrong file format?\nImporter says: \n" + getST(e, false) + "\n\n";
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
		return "Supply Chain Importer - BfR-formats (*.xlsx)";
	}
	
	  public static Long getMillis(List<Exception> exceptions, String filename) {
		  Long result = 0L;//System.currentTimeMillis();
	
		  try (InputStream is = filename.startsWith("http://") ? new URL(filename).openConnection().getInputStream() : new FileInputStream(filename);
						XSSFWorkbook wb = new XSSFWorkbook(is)) {
				Sheet transactionSheet = wb.getSheet("BackTracing");
				Sheet forSheet = wb.getSheet("ForTracing");
				
				boolean isForTracing = forSheet != null;
				if (isForTracing) transactionSheet = forSheet;
				
				if (transactionSheet != null) {
					Row row = transactionSheet.getRow(2);
					MetaInfo mi = getMetaInfo(exceptions, row);
					result = getMs(mi) ;
				}
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
		  return result;
	  }	

	private static long getMs(MetaInfo mi) {
		  List<SimpleDateFormat> knownPatterns = new ArrayList<SimpleDateFormat>();
		  knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd"));
		  knownPatterns.add(new SimpleDateFormat("dd.MM.yyyy"));

		  String str_date = mi.getDate();
		  if (str_date != null) {
			  str_date = str_date.trim();
			  for (DateFormat formatter : knownPatterns) {
			      try {
					Date d = formatter.parse(str_date);
					if (d != null) return d.getTime();	
			      } catch (ParseException pe) {
			          // Loop on
			      }
			  }
		  }
		return 0L;
	  }
		private boolean existsDBKernel() {
			boolean result = true;
			try {
			 Class.forName("de.bund.bfr.knime.openkrise.db.DBKernel");
			} catch( ClassNotFoundException e ) {
				result = false;
			}
			return result;
		}
}
