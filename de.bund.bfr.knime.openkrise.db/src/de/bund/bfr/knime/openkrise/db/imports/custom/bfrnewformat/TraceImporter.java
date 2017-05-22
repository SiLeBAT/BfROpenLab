/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JProgressBar;
import javax.swing.filechooser.FileFilter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.gisgraphy.addressparser.Address;
import com.gisgraphy.addressparser.StructuredAddressQuery;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;
import de.bund.bfr.knime.openkrise.db.MyLogger;
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
	private Map<String, Set<String>> warnsBeforeImport = new HashMap<>();
	private DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
	
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
				if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) exceptions.add(new Exception("Station has no ID -> Row " + (i+1)));
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
				if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) exceptions.add(new Exception("Delivery has no ID -> Row " + (i+1)));
				cell.setCellType(Cell.CELL_TYPE_STRING);
				String val = cell.getStringCellValue();
				if (deliveryIDs.contains(val)) exceptions.add(new Exception("Delivery ID '" + val + "' is defined more than once -> Row " + (i+1)));
				deliveryIDs.add(val);
			}
		}
	}
	private void checkTraceDeliveries(List<Exception> exceptions, Sheet deliverySheet, int borderRowBetweenTopAndBottom, boolean isForTracing, boolean isNewFormat_151105) {
		HashMap<String, HashSet<Row>> deliveryIDs = new HashMap<>();
		int numRows = deliverySheet.getLastRowNum() + 1;
		for (int i=2;i<numRows;i++) {
			Row row = deliverySheet.getRow(i);
			if (row != null) {
				Cell cellM = row.getCell(12); // DeliveryID in DB
				//if ((cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) && (cell1 == null || cell1.getCellType() == Cell.CELL_TYPE_BLANK)) return;
				if (isCellEmpty(cellM)) {
					//exceptions.add(new Exception("Delivery has no ID -> Row " + (i+1)));
				}
				else {
					cellM.setCellType(Cell.CELL_TYPE_STRING);
					String val = cellM.getStringCellValue().trim();
					if (val.isEmpty() || val.equals("DeliveryID in DB")) {
						
					}
					else {
						if (!deliveryIDs.containsKey(val)) deliveryIDs.put(val, new HashSet<Row>());
						HashSet<Row> hs = deliveryIDs.get(val);
						hs.add(row);						
					}
				}
				
				/*
				String key = getRowKey(row, borderRowBetweenTopAndBottom, isForTracing);
					if (!duplicateRows.containsKey(key)) duplicateRows.put(key, new HashSet<Row>());
					HashSet<Row> hs = duplicateRows.get(key);
					hs.add(row);
					*/
			}			
		}
		for (String val : deliveryIDs.keySet()) {
			HashSet<Row> hs = deliveryIDs.get(val);
			if (hs.size() > 1) {
				String rows = "", key = null;
				boolean different = false;
				for (Row tmp : hs) {
					if (isNewFormat_151105 || tmp.getRowNum() < borderRowBetweenTopAndBottom) {
						String tkey = getRowKey(tmp, borderRowBetweenTopAndBottom, isForTracing);
						if (key == null) key = tkey;
						else if (!key.equals(tkey)) different = true;
						rows += ";" + (tmp.getRowNum()+1); 
					}
				}
				if (different) exceptions.add(new Exception("Delivery ID '" + val + "' is defined more than once -> Rows: " + rows.substring(1) + ". If you have copy/pasted a new row, please clear the cell for the DeliveryID of the new Row in Column 'M' (expand it firstly to be able to see it)."));
			}
		}
		/*
		for (String val : duplicateRows.keySet()) {
			HashSet<Integer> hs = duplicateRows.get(val);
			if (hs.size() > 1) {
				String rows = "";
				for (Integer tmp : hs) {
					rows += ";" + tmp; 
				}
				exceptions.add(new Exception("Rows are identical, but Delivery IDs are different -> Rows: " + rows.substring(1) + ". You may want to give them the same ID in Column 'M'."));
			}
		}
		*/
	}
	private String getRowKey(Row row, int borderRowBetweenTopAndBottom, boolean isForTracing) {
		boolean isProductsOut = row.getRowNum() < borderRowBetweenTopAndBottom && !isForTracing || isForTracing && row.getRowNum() > borderRowBetweenTopAndBottom;
		String key = "";
		for (int j=isProductsOut?0:1;j<row.getLastCellNum();j++) { // Start with Lot Number or after
			Cell cell = row.getCell(j);
			if (!isCellEmpty(cell)) {
				cell.setCellType(Cell.CELL_TYPE_STRING);
				key += cell.getStringCellValue().trim();
			}
			key += ";";
		}
		while(key.endsWith(";;")) {
			key = key.substring(0, key.length() - 1);
		}	
		return key;
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
		if (stationSheet == null) return doTheSimpleImport(wb, filename);
		Sheet deliverySheet = wb.getSheet("Deliveries");
		Sheet d2dSheet = wb.getSheet("Deliveries2Deliveries");
		Sheet transactionSheet = wb.getSheet("BackTracing");
		Sheet lookupSheet = wb.getSheet("LookUp");
		Sheet forwardSheet = wb.getSheet("Opt_ForwardTracing");
		Sheet forwardSheetNew = wb.getSheet("ForwardTracing_Opt");
		Sheet forSheet = wb.getSheet("ForTracing");
		Sheet fwdSheet = wb.getSheet("FwdTracing");
		if (forSheet == null) forSheet = fwdSheet;
		
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
				if (stations.containsKey(s.getId())) exceptions.add(new Exception("Station defined twice -> Row " + (classRowIndex+1) + "; Station Id: '" + s.getId() + "'"));
				stations.put(s.getId(), s);
			}
			// load all Deliveries
			HashMap<String, Delivery> deliveries = new HashMap<>();
			numRows = deliverySheet.getLastRowNum() + 1;
			titleRow = deliverySheet.getRow(0);
			HashMap<String,String> definedLots = new HashMap<>();
			HashMap<String, Integer> deliveryRows = new HashMap<>();
			for (classRowIndex=2;classRowIndex<numRows;classRowIndex++) {
				Delivery d = getMultiOutDelivery(exceptions, stations, titleRow, deliverySheet.getRow(classRowIndex), definedLots, classRowIndex, filename, d2dSheet != null);
				if (d == null) break;
				if (deliveries.containsKey(d.getId())) exceptions.add(new Exception("Delivery defined twice -> in Row " + (classRowIndex+1) + " and in Row " + deliveryRows.get(d.getId()) + "; Delivery Id: '" + d.getId() + "'"));
				else deliveryRows.put(d.getId(), classRowIndex+1);
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
				//if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
				if (d.getExceptions().size() > 0) exceptions.addAll(d.getExceptions());
				
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
				if (dl.getIngredient() != null) hd.add(dl.getIngredient().getDbId());
			}

			return exceptions;
		}
		int borderRowLotStart = 0;
		
		Row row = transactionSheet.getRow(0);
		Row titleRow;
		Cell cell;
		HashMap<String, Delivery> outDeliveries = new HashMap<>(); 
		HashMap<String, Lot> outLots = new HashMap<>(); 
		Station sif;
		MetaInfo mi;
				
		boolean isNewFormat_151105 = false;
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
				Delivery d = getDelivery(exceptions, stationSheet, sif, row, true, titleRow, filename, false, null, outDeliveries, false, isNewFormat_151105);
				if (d == null) continue;
				outDeliveries.put(d.getId(), d);
				outLots.put(d.getLot().getNumber(), d.getLot());
			}
			
			// Metadata on Reporter
			classRowIndex = getNextBlockRowIndex(transactionSheet, classRowIndex, "Reporter Information") + 2;
			row = transactionSheet.getRow(classRowIndex);
			mi = getMetaInfo(exceptions, row, transactionSheet.getRow(classRowIndex-1));
			mi.setFilename(filename);
		}
		else { // Reporter shifted to the top
			// Metadata on Reporter
			classRowIndex = getNextBlockRowIndex(transactionSheet, 0, "Reporter Information") + 2;
			row = transactionSheet.getRow(classRowIndex);
			mi = getMetaInfo(exceptions, row, transactionSheet.getRow(classRowIndex-1));
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
			cell = titleRow.getCell(0);
			isNewFormat_151105 = cell.getStringCellValue().equals("Product Lot Number");
			for (;;classRowIndex++) {
				row = transactionSheet.getRow(classRowIndex);
				if (row == null) continue;
				if (isBlockEnd(row, 13, "Lot Information")) break;
				Delivery d = getDelivery(exceptions, stationSheet, sif, row, !isForTracing, titleRow, filename, isForTracing, outLots, outDeliveries, false, isNewFormat_151105);
				if (d == null) continue;
				outDeliveries.put(d.getId(), d);
				if (!isForTracing) outLots.put(d.getLot().getNumber(), d.getLot());
			}			
		}
		
		String label = "Ingredients In for Lot(s)";
		if (isForTracing) label = "Products Out";
		// Lot(s)
		classRowIndex = getNextBlockRowIndex(transactionSheet, classRowIndex, "Lot Information") + 3;
		borderRowLotStart = classRowIndex;
		titleRow = transactionSheet.getRow(classRowIndex - 2);
		for (;;classRowIndex++) {
			row = transactionSheet.getRow(classRowIndex);
			if (row == null) continue;
			if (isBlockEnd(row, 13, label)) break;
			if (!fillLot(exceptions, row, sif, outLots, titleRow, isForTracing ? outDeliveries : null, classRowIndex + 1, isNewFormat_151105)) {
				exceptions.add(new Exception("Lot number unknown in Row number " + (classRowIndex + 1)));
			}
		}
		
		checkTraceDeliveries(exceptions, transactionSheet, borderRowLotStart, isForTracing, isNewFormat_151105);

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
			Delivery d = getDelivery(exceptions, stationSheet, sif, row, isForTracing, titleRow, filename, isForTracing, outLots, inDeliveries, false, isNewFormat_151105);
			if (d == null) continue;
			if (!isForTracing && d.getTargetLotIds().size() == 0) exceptions.add(new Exception("Lot number unknown in Row number " + (classRowIndex + 1)));
			inDeliveries.put(d.getId(), d);
			hasIngredients = true;
		}
		if (!hasIngredients) {
			warns.put("No " + (isForTracing ? "Products Out" : "ingredients") + " defined...", null);
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
				Delivery d = getForwardDelivery(exceptions, stationSheet, outLots, titleRow, forwardSheet.getRow(classRowIndex), isNewFormat_151105);
				if (d == null) continue;
				forwDeliveries.add(d);
			}
		}
		
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
				insertForIntoDb(exceptions, miDbId, inDeliveries, outDeliveries);
			} catch (Exception e) {
				exceptions.add(e);
			}
		else
			try {
				insertIntoDb(exceptions, miDbId, inDeliveries, outDeliveries, forwDeliveries);
			} catch (Exception e) {
				exceptions.add(e);
			}
		
		return exceptions;
	}
	private String getCellString(Cell cell) {
		return getCellString(cell, false);
	}
	private String getCellString(Cell cell, boolean checkIfDate) {
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {			
			if (checkIfDate && cell.getCellType() != Cell.CELL_TYPE_STRING && DateUtil.isCellDateFormatted(cell)) {
				Date date = cell.getDateCellValue();
				return df.format(date);
			}
			else {
				cell.setCellType(Cell.CELL_TYPE_STRING);
				return getStr(cell.getStringCellValue());				
			}
		}
		return null;
	}
	private List<Exception> doTheSimpleImport(Workbook wb, String filename) { //  throws Exception
		DBKernel.sendRequest("ALTER TABLE " + MyDBI.delimitL("ExtraFields") + " ALTER COLUMN " + MyDBI.delimitL("value") + " VARCHAR(32768)", false, true);
		DBKernel.sendRequest("ALTER TABLE " + MyDBI.delimitL("Station") + " ADD COLUMN " + MyDBI.delimitL("Adresse") + " VARCHAR(32768)", true, true);
		List<Exception> exceptions = new ArrayList<>();
		
		boolean backtracing = true;
		boolean isProduction = false;
		Sheet sheet = wb.getSheet("Rückverfolgung");
		if (sheet == null) {sheet = wb.getSheet("Herstellung - Rückverfolgung"); isProduction = true;}
		if (sheet == null) {
			backtracing = false;
			sheet = wb.getSheet("Vorwärtsverfolgung");
		}
		if (sheet == null) {sheet = wb.getSheet("Herstellung - Vorwärtsverfolgun"); isProduction = true;}
				
		HashMap<Integer, Station> stations = new HashMap<>();
		HashMap<Integer, Product> products = new HashMap<>();
		HashMap<Integer, Lot> lots = new HashMap<>();
		HashMap<Integer, Delivery> dels = new HashMap<>();
		HashMap<String, Delivery> olddels = new HashMap<>();
		HashMap<String, Delivery> olddelsLot = new HashMap<>();

		int NAME = -1, ADDRESS = -1, ADDRESS_COUNTRY = -1, PRODUCTNAME = -1, EAN = -1, CHARGE = -1, MHD = -1, DAY = -1, MONTH = -1,
				YEAR = -1, AMOUNT = -1, COMMENT = -1, CHARGENLINK = -1, TOB = -1;
		if (sheet != null) {
			Station focusS = new Station();
			Row row = sheet.getRow(0);
			String cs = getCellString(row.getCell(1));
			focusS.setName(cs);
			String address = getCellString(row.getCell(2));
			focusS.setAddress(address);
			focusS.setCountry(getCellString(row.getCell(6))); // oder 3 ... ???
			focusS.addFlexibleField("Quelle", "Zeile 1");
			int sID = genDbId(""+cs+address);
			focusS.setId(""+sID);
			stations.put(sID, focusS);
			
			int numRows = sheet.getLastRowNum() + 1;
			boolean doCollect = false;
			boolean doPreCollect = false;
			for (int i=1;i<numRows;i++) {
				row = sheet.getRow(i);
				if (row != null) {
					cs = getCellString(row.getCell(0));
					String cs1 = getCellString(row.getCell(1));
					if (cs != null || cs1 != null) {
						if (cs != null && cs.startsWith("Information zum Ausfüllen")) doPreCollect = false;
						if (doCollect || doPreCollect) {
							//System.err.print(i+1);

							String name = getCellString(row.getCell(NAME));
							address = getCellString(row.getCell(ADDRESS));
							sID = genDbId(""+name+address);
							Station supplierS = null;
							if (stations.containsKey(sID)) {
								supplierS = stations.get(sID);
								supplierS.addFlexibleField("Quelle", supplierS.getFlexible("Quelle") + "; Zeile " + (i+1));
							}
							else {
								supplierS = new Station();
								supplierS.setName(name);
								supplierS.setAddress(address);
								if (ADDRESS_COUNTRY >= 0) supplierS.setCountry(getCellString(row.getCell(ADDRESS_COUNTRY)));
								if (TOB >= 0) supplierS.setTypeOfBusiness(getCellString(row.getCell(TOB)));
								supplierS.addFlexibleField("Quelle", "Zeile " + (i+1));
								supplierS.setId(""+sID);
								stations.put(sID, supplierS);
							}
							
							String f2 = getCellString(row.getCell(PRODUCTNAME));
							String f3 = getCellString(row.getCell(EAN));
							int pID = genDbId(""+(backtracing==doPreCollect?focusS.getId():supplierS.getId()) + f2 + f3);
							Product p = null;
							if (products.containsKey(pID)) {
								p = products.get(pID);
								p.addFlexibleField("Quelle", p.getFlexible("Quelle") + "; Zeile " + (i+1));
							}
							else {
								p = new Product();
								if (doPreCollect) {
									if (!backtracing) p.setStation(supplierS);
									else p.setStation(focusS);
								}
								else {
									if (backtracing) p.setStation(supplierS);
									else p.setStation(focusS);									
								}
								p.setName(f2);
								p.addFlexibleField("EAN", f3);
								p.addFlexibleField("Quelle", filename + " - Zeile " + (i+1));
								p.setId(pID);
								products.put(pID, p);
							}

							f2 = getCellString(row.getCell(CHARGE));
							f3 = getCellString(row.getCell(MHD), true);
							int lID = genDbId(""+p.getId() + f2 + f3);
							Lot lot = null;
							if (lots.containsKey(lID)) {
								lot = lots.get(lID);
								lot.addFlexibleField("Quelle", lot.getFlexible("Quelle") + "; Zeile " + (i+1));
							}
							else {
								lot = new Lot();
								lot.setProduct(p);
								lot.setNumber(f2);
								lot.addFlexibleField("MHD", f3);
								lot.addFlexibleField("Quelle", filename + " - Zeile " + (i+1));
								lot.setId(lID);
								lots.put(lID, lot);
							}

							Integer f4 = getInt(getCellString(row.getCell(DAY)));
							Integer f5 = getInt(getCellString(row.getCell(MONTH)));
							Integer f6 = getInt(getCellString(row.getCell(YEAR)));
							String f7 = getCellString(row.getCell(AMOUNT));
							String f8 = getCellString(row.getCell(COMMENT));
							int  dID = genDbId(""+lot.getId()+f4+f5+f6+f7+f8+(doPreCollect==backtracing?supplierS.getId():focusS.getId()));						
							Delivery d = null;
							if (doPreCollect) {
								d = new Delivery();
								d.setLot(lot);
								d.setArrivalDay(f4);
								d.setArrivalMonth(f5);
								d.setArrivalYear(f6);
								d.addFlexibleField("Amount", f7);
								d.setComment(f8);
								if (doPreCollect) {
									if (!backtracing) d.setReceiver(focusS);
									else d.setReceiver(supplierS);									
								}
								else {
									if (!backtracing) d.setReceiver(supplierS);
									else d.setReceiver(focusS);
								}
								d.addFlexibleField("Quelle", filename + " - Zeile " + (i+1));
								d.setId(dID+"");
								olddels.put((i+1)+"", d);
								olddelsLot.put(d.getLot().getNumber(), d);
							}
							else {
								if (dels.containsKey(dID)) {
									d = dels.get(dID);
									d.addFlexibleField("Quelle", d.getFlexible("Quelle") + "; Zeile " + (i+1));
								}
								else {
									d = new Delivery();
									d.setLot(lot);
									d.setArrivalDay(f4);
									d.setArrivalMonth(f5);
									d.setArrivalYear(f6);
									d.addFlexibleField("Amount", f7);
									d.setComment(f8);								
									if (backtracing) d.setReceiver(focusS);
									else d.setReceiver(supplierS);
									d.addFlexibleField("Quelle", filename + " - Zeile " + (i+1));
									d.setId(dID+"");
									dels.put(dID, d);
								}
							}
							
							if (CHARGENLINK >= 0) {
								String rownum = getCellString(row.getCell(CHARGENLINK));
								// hier nochmal überlegen, ob nicht besser bei leerer Zelle einfach importiert werden soll - ohne Verknüpfung
								// nicht vergessen: verenglischen
								if (!olddelsLot.containsKey(rownum) && !olddels.containsKey(rownum)) {
									exceptions.add(new Exception("Zeilennummer/Chargennummer in Feld A" + (i+1) + " ist ungültig!"));
								}
								else {
									Delivery od = olddels.get(rownum);
									if (od == null) od = olddelsLot.get(rownum);
									System.err.println(od.getLot().getNumber() + ": " + od.getLot().getProduct().getStation().getId() + " - " + od.getLot().getProduct().getId() + " - " + od.getLot().getId() + " - " + od.getId());
									if (backtracing) d.addTargetLotId(od.getLot().getId()+"");
									else d.getLot().getInDeliveries().add(od.getId());
								}
							}
							/*
							Address a = new Address();
							a.setBlock("Hessenweg 1, 12343 Gese");
							a.getStreetName();
							StructuredAddressQuery saq = new StructuredAddressQuery(a, "DE");
							
							saq.getAddress();
							//com.gisgraphy.ser
							saq.getStructuredAddress().getStreetName();
							*/
							
						}
						else if (backtracing && cs != null && (cs.trim().startsWith("Lieferant") || cs.trim().startsWith("Zeilennummer")) ||
								!backtracing && cs != null && (cs.trim().startsWith("Empfänger") || cs.trim().startsWith("Zeilennummer")) ||
								isProduction && i==3) {
							i++;
							row = sheet.getRow(i);
							if (isProduction && i==4) {
								CHARGENLINK = -1; NAME = 9; ADDRESS = 10; PRODUCTNAME = 1; EAN = 2; CHARGE = 3; MHD = 4; DAY = 5; MONTH = 6; YEAR = 7; AMOUNT = 8;							
								String ls = getCellString(row.getCell(11));
								if (ls != null && ls.equals("Land")) { // Betriebsart hier auch rein etc etc
									ADDRESS_COUNTRY = 11; COMMENT = 12;								
								}
								else {
									ADDRESS_COUNTRY = -1; COMMENT = 11;																
								}								
								doPreCollect = true;
							}
							else if (cs.trim().startsWith("Zeilennummer")) {
								CHARGENLINK = 0; NAME = 9; ADDRESS = 10; PRODUCTNAME = 1; EAN = 2; CHARGE = 3; MHD = 4; DAY = 5; MONTH = 6; YEAR = 7; AMOUNT = 8;							
								String ls = getCellString(row.getCell(11));
								if (ls != null && ls.equals("Land")) {
									ADDRESS_COUNTRY = 11; COMMENT = 12;								
								}
								else {
									ADDRESS_COUNTRY = -1; COMMENT = 11;																
								}
								doCollect = true;
							}
							else {
								NAME = 0; ADDRESS = 1; 
								String ls = getCellString(row.getCell(2));
								if (ls != null && ls.equals("Land")) {
									ADDRESS_COUNTRY = 2; PRODUCTNAME = 3; EAN = 4; CHARGE = 5;
									MHD = 6; DAY = 7; MONTH = 8; YEAR = 9; AMOUNT = 10; COMMENT = 11;								
								}
								else {
									ADDRESS_COUNTRY = -1; PRODUCTNAME = 2; EAN = 3; CHARGE = 4;
									MHD = 5; DAY = 6; MONTH = 7; YEAR = 8; AMOUNT = 9; COMMENT = 10;																
								}
								doCollect = true;
							}
							continue;
						}
					}
				}
			}			
		}
		else {
			exceptions.add(new Exception("Wrong template format!"));
			return exceptions;
		}
			
		try {
			for (Station s: stations.values()) {
				s.addFlexibleField("Quelle", filename + ": " + s.getFlexible("Quelle"));
			}
			for (Delivery d : dels.values()) {
				d.insertIntoDb(mydbi);
				//if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
				if (d.getExceptions().size() > 0) exceptions.addAll(d.getExceptions());
			}
		} catch (Exception e) {
			exceptions.add(e);
		}

		return exceptions;
	}
	private int genDbId(String toCode) {
		return toCode.hashCode();
	}
	private void insertForIntoDb(List<Exception> exceptions, Integer miDbId, HashMap<String, Delivery> outDeliveries, HashMap<String, Delivery> inDeliveries) throws Exception {
		HashMap<String, Integer> lotDbNumber = new HashMap<>();
		for (Delivery d : outDeliveries.values()) {
			d.getID(miDbId, !d.isNewlyGeneratedID(), mydbi);
			//if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
			if (d.getExceptions().size() > 0) exceptions.addAll(d.getExceptions());
			lotDbNumber.put(d.getLot().getNumber(), d.getLot().getDbId());
		}
		for (Delivery d : inDeliveries.values()) {
			Integer dbId = d.getID(miDbId, !d.isNewlyGeneratedID(), mydbi);
			//if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
			if (d.getExceptions().size() > 0) exceptions.addAll(d.getExceptions());
			for (String targetLotId : d.getTargetLotIds()) {
				if (lotDbNumber.containsKey(targetLotId)) {
					new D2D().getId(dbId, lotDbNumber.get(targetLotId), miDbId, mydbi);
				}
			}
		}
	}
	private void insertIntoDb(List<Exception> exceptions, Integer miDbId, HashMap<String, Delivery> inDeliveries, HashMap<String, Delivery> outDeliveries, HashSet<Delivery> forwDeliveries) throws Exception {
		HashMap<String, Lot> lotDbNumber = new HashMap<>();
		for (Delivery d : outDeliveries.values()) {
			d.getID(miDbId, !d.isNewlyGeneratedID(), mydbi);
			//if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
			if (d.getExceptions().size() > 0) exceptions.addAll(d.getExceptions());
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
			Integer dbId = d.getID(miDbId, !d.isNewlyGeneratedID(), mydbi);
			//if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
			if (d.getExceptions().size() > 0) exceptions.addAll(d.getExceptions());
			for (String targetLotId : d.getTargetLotIds()) {
				if (lotDbNumber.containsKey(targetLotId)) {
					new D2D().getId(dbId, lotDbNumber.get(targetLotId).getDbId(), miDbId, mydbi);
				}
			}
		}
		for (Delivery d : forwDeliveries) {
			d.getID(miDbId, false, mydbi);
			//if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
			if (d.getExceptions().size() > 0) exceptions.addAll(d.getExceptions());
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
	
	private static MetaInfo getMetaInfo(List<Exception> exceptions, Row row, Row rowBefore) {
		if (row == null) return null;
		boolean hasPartedDate = true;
		if (rowBefore != null) {
			Cell cell = rowBefore.getCell(1);
			if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
				cell.setCellType(Cell.CELL_TYPE_STRING);
				if (getStr(cell.getStringCellValue()).equals("Reporting Date")) {
					cell = rowBefore.getCell(3);
					if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || getStr(cell.getStringCellValue()).trim().isEmpty()) {
						hasPartedDate = false;
					}
				}
			}
		}
		MetaInfo result = new MetaInfo();
		Cell cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setReporter(getStr(cell.getStringCellValue()));}
		if (hasPartedDate) {
			cell = row.getCell(1);
			if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {				
				cell.setCellType(Cell.CELL_TYPE_STRING); result.setDateDay(getInt(cell.getStringCellValue()));
			}
			cell = row.getCell(2);
			if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
				cell.setCellType(Cell.CELL_TYPE_STRING); result.setDateMonth(getInt(cell.getStringCellValue()));
			}
			cell = row.getCell(3);
			if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
				cell.setCellType(Cell.CELL_TYPE_STRING); result.setDateYear(getInt(cell.getStringCellValue()));
			}
			/*
			if (result.getDateDay() == null) {
				if (exceptions != null) exceptions.add(new Exception("Reporting date is not defined correctly. This is mandatory! The Day is missing"));
			}
			if (result.getDateMonth() == null) {
				if (exceptions != null) exceptions.add(new Exception("Reporting date is not defined correctly. This is mandatory! The Month is missing"));
			}
			if (result.getDateYear() == null) {
				if (exceptions != null) exceptions.add(new Exception("Reporting date is not defined correctly. This is mandatory! The Year is missing"));
			}
			*/
			cell = row.getCell(4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setRemarks(getStr(cell.getStringCellValue()));}			
		}
		else {
			cell = row.getCell(1);
			if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
				cell.setCellType(Cell.CELL_TYPE_STRING);
				result.setDate(getStr(cell.getStringCellValue()));
			}
			cell = row.getCell(2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setRemarks(getStr(cell.getStringCellValue()));}			
		}
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
	private Delivery getForwardDelivery(List<Exception> exceptions, Sheet stationSheet, HashMap<String, Lot> lots, Row titleRow, Row row, boolean isNewFormat_151105) {
		if (row == null) return null;
		Lot l = null;
		Cell cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l=lots.get(getStr(cell.getStringCellValue()));}
		if (l == null) return null;
		Delivery result = new Delivery();
		result.setLot(l);
		cell = row.getCell(1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureDay(getInt(cell.getStringCellValue()));}
		cell = row.getCell(2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureMonth(getInt(cell.getStringCellValue()));}
		cell = row.getCell(3); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureYear(getInt(cell.getStringCellValue()));}
		int startCol = 4;
		if (isNewFormat_151105) {
			cell = row.getCell(4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setArrivalDay(getInt(cell.getStringCellValue()));}
			cell = row.getCell(5); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setArrivalMonth(getInt(cell.getStringCellValue()));}
			cell = row.getCell(6); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setArrivalYear(getInt(cell.getStringCellValue()));}
			startCol = 7;
		}
		cell = row.getCell(startCol); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setUnitNumber(getDbl(cell.getStringCellValue()));}
		cell = row.getCell(startCol+1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setUnitUnit(getStr(cell.getStringCellValue()));}
		cell = row.getCell(startCol+2);
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
		result.setNewlyGeneratedID(true);
		
		// Further flexible cells
		for (int i=startCol+4;i<startCol+21;i++) {
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
		else if (!ignoreMissingLotnumbers) {exceptions.add(new Exception("Please, do always provide a lot number as this is most helpful! -> Row " + (rowNum+1) + " in '" + filename + "'\n"));}
		cell = row.getCell(4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setUnitNumber(getDbl(cell.getStringCellValue()));}
		cell = row.getCell(5); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setUnitUnit(getStr(cell.getStringCellValue()));}
		String lotId = (p.getStation() == null) ? "_" + p.getName() + "_" + l.getNumber() : p.getStation().getId() + "_" + p.getName() + "_" + l.getNumber();
		String lotInfo = l.getUnitNumber() + "_" + l.getUnitUnit();
		if (definedLots.containsKey(lotId)) {
			if (!definedLots.get(lotId).equals(lotInfo)) exceptions.add(new Exception("Lot has different quantities -> Lot number: '" + l.getNumber() + "'; -> Row " + (rowNum+1)));
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
		LinkedHashSet<String> le0 = new LinkedHashSet<>();
		le0.add("Production date".toLowerCase());
		le0.add("Best before date".toLowerCase());
		le0.add("Treatment of product during production".toLowerCase());
		le0.add("Sampling".toLowerCase());
		for (int i=15;i<25;i++) {
			Cell tCell = titleRow.getCell(i);
			if (tCell != null && tCell.getCellType() != Cell.CELL_TYPE_BLANK) {
				tCell.setCellType(Cell.CELL_TYPE_STRING);
				String field = tCell.getStringCellValue();
				cell = row.getCell(i);
				if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					if (le0.contains(field.toLowerCase())) l.addFlexibleField(field, cell.getStringCellValue());
					else result.addFlexibleField(field, cell.getStringCellValue());
				}			
			}
		}
		return result;
	}
	private boolean isCellEmpty(Cell cell) {
		return cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || (cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getStringCellValue().isEmpty());
	}
	private Delivery getDelivery(List<Exception> exceptions, Sheet businessSheet, Station sif, Row row, boolean outbound, Row titleRow, String filename, boolean isForTracing, HashMap<String, Lot> outLots, HashMap<String, Delivery> existingDeliveries, boolean ignoreMissingLotnumbers, boolean isNewFormat_151105) {
		Cell cell;
		if (isNewFormat_151105) {
			cell = row.getCell(0);
			if (isCellEmpty(cell)) {
				Cell cell10 = row.getCell(10); 
				if (!isForTracing || !isCellEmpty(cell10)) {
					exceptions.add(new Exception("It is essential to choose the associated Lot number ('Lot Number of " + (isForTracing ? "" : " \"") + "Product" + (isForTracing ? "" : " Out\"") + "') to the delivery in Row number " + (classRowIndex + 1)));
				}
				return null;
			}
			cell = row.getCell(12);
		}
		else {
			cell = row.getCell(12);
			if (isCellEmpty(cell)) {
				Cell cell10 = row.getCell(10); 
				Cell cell0 = row.getCell(0); 
				if ((!isForTracing && !isCellEmpty(cell0)) || !isCellEmpty(cell10)) {
					exceptions.add(new Exception("It is essential to choose the associated Lot number ('Lot Number of " + (isForTracing ? "" : " \"") + "Product" + (isForTracing ? "" : " Out\"") + "') to the delivery in Row number " + (classRowIndex + 1)));
				}
				return null;
			}
		}
		Delivery result = new Delivery();
		String lotDelNumber = null;
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING); lotDelNumber = getStr(cell.getStringCellValue());
			//if (isForTracing && outbound) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setTargetLotId(getStr(cell.getStringCellValue()));}
			if (isNewFormat_151105) {
				result.setId(lotDelNumber);
			}
			else {
				if (!isForTracing && outbound) {result.setId(lotDelNumber);}
				if (isForTracing && !outbound) {result.setId(lotDelNumber);}
				if (!isForTracing && !outbound) {result.addTargetLotId(lotDelNumber);}
			}
		}
		Lot l;
		if (isForTracing && outbound) {
			cell = row.getCell(0);
			if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); lotDelNumber = getStr(cell.getStringCellValue());}
			if (lotDelNumber == null && !ignoreMissingLotnumbers) {exceptions.add(new Exception("Please, do always provide a lot number as this is most helpful! -> Row " + (row.getRowNum()+1) + " in '" + filename + "'\n"));}					
			l = outLots.get(lotDelNumber);
		}
		else {
			Product p = new Product();
			if (outbound) p.setStation(sif);
			l = new Lot();
			l.setProduct(p);
			String lotNumber = null;
			if (isNewFormat_151105) {
				if (outbound) {
					cell = row.getCell(0);
					if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); lotNumber = getStr(cell.getStringCellValue());}
					if (lotNumber == null && !ignoreMissingLotnumbers) {exceptions.add(new Exception("Please, do always provide a lot number as this is most helpful! -> Row " + (row.getRowNum()+1) + " in '" + filename + "'\n"));}					
					l.setNumber(lotNumber);
					if (outLots.containsKey(lotNumber)) {
						l = outLots.get(lotNumber);
					}
				}				
				else {
					cell = row.getCell(0);
					if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); lotNumber = getStr(cell.getStringCellValue());}
					if (lotNumber != null) result.addTargetLotId(lotNumber);

					cell = row.getCell(1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); p.setName(getStr(cell.getStringCellValue()));}				
					cell = row.getCell(2);
					lotNumber = null;
					if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); lotNumber = getStr(cell.getStringCellValue());}
					if (lotNumber == null && !ignoreMissingLotnumbers) {exceptions.add(new Exception("Please, do always provide a lot number as this is most helpful! -> Row " + (row.getRowNum()+1) + " in '" + filename + "'\n"));}
					l.setNumber(lotNumber);
					if (lotNumber == null && p.getName() == null) {
						exceptions.add(new Exception("Lot number undefined in Row number " + (classRowIndex + 1)));
					}
				}
			}
			else {
				cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); p.setName(getStr(cell.getStringCellValue()));}				
				cell = row.getCell(1);
				if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); lotNumber = getStr(cell.getStringCellValue());}
				else if (!ignoreMissingLotnumbers) {exceptions.add(new Exception("Please, do always provide a lot number as this is most helpful! -> Row " + (row.getRowNum()+1) + " in '" + filename + "'\n"));}
				l.setNumber(lotNumber);
				if (lotNumber == null && p.getName() == null) {
					exceptions.add(new Exception("Lot number and product name undefined in Row number " + (classRowIndex + 1)));
				}
			}
		}
		//cell = row.getCell(1); if (cell != null) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setNumber(getStr(cell.getStringCellValue()));}
		result.setLot(l);
		if (!outbound) result.setReceiver(sif);
		int startCol = isNewFormat_151105 ? 3 : 2;
		cell = row.getCell(startCol); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureDay(getInt(cell.getStringCellValue()));}
		cell = row.getCell(startCol+1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureMonth(getInt(cell.getStringCellValue()));}
		cell = row.getCell(startCol+2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setDepartureYear(getInt(cell.getStringCellValue()));}
		cell = row.getCell(startCol+3); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setArrivalDay(getInt(cell.getStringCellValue()));}
		cell = row.getCell(startCol+4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setArrivalMonth(getInt(cell.getStringCellValue()));}
		cell = row.getCell(startCol+5); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setArrivalYear(getInt(cell.getStringCellValue()));}
		cell = row.getCell(startCol+6); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setUnitNumber(getDbl(cell.getStringCellValue()));}
		cell = row.getCell(startCol+7); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setUnitUnit(getStr(cell.getStringCellValue()));}
		cell = row.getCell(startCol+8); 
		if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || (cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getStringCellValue().isEmpty())) return null;
		if (outbound && cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setReceiver(getStation(exceptions, businessSheet, cell.getStringCellValue(), row));}
		if (!outbound && cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.getProduct().setStation(getStation(exceptions, businessSheet, cell.getStringCellValue(), row)); l.setNumber(l.getNumber());}

		if (!isForTracing && !outbound || isForTracing && outbound) {
			if (!isNewFormat_151105 || result.getId() == null) {
				result.setId(getNewSerial(l, result));
				result.setNewlyGeneratedID(true);
			}
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
		String newSerial = (l.getProduct() != null && l.getProduct().getStation() != null ? l.getProduct().getStation().getId() + ";" + l.getProduct().getName() : "null") + ";" + l.getNumber() + ";" +
				d.getDepartureDay() + ";" + d.getDepartureMonth() + ";" + d.getDepartureYear() + ";" +
				d.getArrivalDay() + ";" + d.getArrivalMonth() + ";" + d.getArrivalYear() + ";" +
				d.getUnitNumber() + ";" + d.getUnitUnit() + ";" + d.getReceiver().getId();
		return newSerial;
	}
	private static Integer getInt(String val) {
		Integer result = null;
		if (val != null && !val.trim().isEmpty()) {
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
		if (val == null) return null;
		if (val.trim().isEmpty()) return null;
		return val;
	}
	private boolean fillLot(List<Exception> exceptions, Row row, Station sif, HashMap<String, Lot> outLots, Row titleRow, HashMap<String, Delivery> outDeliveries, int rowIndex, boolean isNewFormat_151105) {
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
				exceptions.add(new Exception("Lot information defines same lot number with different quantities -> Row " + rowIndex));
			}
		}
		cell = row.getCell(2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING);
			String str = getStr(cell.getStringCellValue());
			if (l.getUnitUnit() == null) l.setUnitUnit(str);
			else if (!l.getUnitUnit().equals(str)) {
				exceptions.add(new Exception("Lot information defines same lot number with different units -> Row " + rowIndex));
			}
		}
		if (isNewFormat_151105 || outDeliveries != null) {
			cell = row.getCell(3); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
				cell.setCellType(Cell.CELL_TYPE_STRING);
				String pr = getStr(cell.getStringCellValue());
				if (l.getProduct() == null) {
					Product p = new Product(); p.setName(pr); l.setProduct(p); p.setStation(sif);
				}
				else if (l.getProduct().getName() == null) {
					l.getProduct().setName(pr);
				}
				else if (!l.getProduct().getName().equals(pr)) {
					exceptions.add(new Exception("Lot information defines same lot number with different product names -> Row " + rowIndex));
				}
			}
			if (!isNewFormat_151105) {
				cell = row.getCell(4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); Delivery d = outDeliveries.get(getStr(cell.getStringCellValue())); if (d == null) return false; d.addTargetLotId(l.getNumber());}
			}
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

	private boolean importResult = false;
	@Override
	public boolean doImport(final String filename, final JProgressBar progress, boolean showResults) {
		importResult = false;
		Runnable runnable = new Runnable() {
			public void run() {
				System.err.println("Importing " + filename);
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

					// warnsBeforeImport erkennen
					warnsBeforeImport = new HashMap<>();
					if (existsDBKernel()) warnsBeforeImport.putAll(de.bund.bfr.knime.openkrise.common.DeliveryUtils.getWarnings(DBKernel.getDBConnection()));
					else if (mydbi != null) warnsBeforeImport.putAll(de.bund.bfr.knime.openkrise.common.DeliveryUtils.getWarnings(mydbi.getConn()));


					XSSFWorkbook wb = new XSSFWorkbook(is);

					Station.reset(); Lot.reset(); Delivery.reset();
					warns = new HashMap<>();
					//if (existsDBKernel()) DBKernel.sendRequest("SET AUTOCOMMIT FALSE", false);
					
					List<Exception> exceptions = doTheImport(wb, filename);
					//List<Exception> exceptions = doTheSimpleImport(wb, filename);
					
					if (exceptions != null && exceptions.size() > 0) {
						importResult = false;
						if (existsDBKernel()) {
							//DBKernel.sendRequest("ROLLBACK", false);
							//DBKernel.sendRequest("SET AUTOCOMMIT TRUE", false);
							warns.putAll(de.bund.bfr.knime.openkrise.common.DeliveryUtils.getWarnings(DBKernel.getDBConnection()));
						}
						else if (mydbi != null) {
							warns.putAll(de.bund.bfr.knime.openkrise.common.DeliveryUtils.getWarnings(mydbi.getConn()));
						}
						doWarns(filename);
						
						logMessages += "<h1 id=\"error\">Error in file '" + filename + "'</h1><ul>";
						for (Exception e : exceptions) {
							logMessages += "<li>" + e.getMessage() + "</li>";
							MyLogger.handleException(e);
						}
						logMessages += "</ul>";
						if (progress != null) progress.setVisible(false);
						try {
							is.close();
						} catch (IOException e1) {}
					}
					else {
						importResult = true;
						if (existsDBKernel()) {
							/*
							if (exceptions != null) DBKernel.sendRequest("COMMIT", false);
							DBKernel.sendRequest("SET AUTOCOMMIT TRUE", false);
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
							*/
							warns.putAll(de.bund.bfr.knime.openkrise.common.DeliveryUtils.getWarnings(DBKernel.getDBConnection()));
						}
						else if (mydbi != null) {
							warns.putAll(de.bund.bfr.knime.openkrise.common.DeliveryUtils.getWarnings(mydbi.getConn()));
						}
						doWarns(filename);
						is.close();
					}
				} catch (Exception e) {
					importResult = false;
					if (existsDBKernel()) {
						//DBKernel.sendRequest("ROLLBACK", false);
						//DBKernel.sendRequest("SET AUTOCOMMIT TRUE", false);
						//if (progress != null) progress.setVisible(false);
					}
					logMessages += "<h1 id=\"error\">'" + filename + "'</h1><ul><li>" + e.getMessage() + "</li></ul>";
					MyLogger.handleException(e);
					try {
						is.close();
					} catch (IOException e1) {}
				}
				System.err.println("Importing - Fin");
				//logMessages += "Importing - Fin" + "\n";
				
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			logMessages += "<h1 id=\"error\">'" + filename + "' (Maybe wrong file format)</h1><ul><li>" + e.getMessage() + "</li></ul>";			
			MyLogger.handleException(e);
		}
		return importResult;
	}
	private void doWarns(String filename) {
		if (warns.size() > 0) {
			String newFileLogs = "";
			if (filename != null) {
				newFileLogs += "<h1 id=\"warning\">Warnings for import file '" + filename + "'<h1>";
			}
			for (String key : warns.keySet()) {
				String newLogs = "<h2>" + key + "</h2>";
				//logWarnings += "<h2>" + key + "</h2>";
				Set<String> oldWarns = warnsBeforeImport.get(key);
				if (warns.get(key) != null && !warns.get(key).isEmpty()) {
					newLogs += "<ul>";
					for (String w : warns.get(key)) {
						if (oldWarns == null || !oldWarns.contains(w)) {
							if (logWarnings.indexOf("<li>" + w + "</li>") < 0) newLogs += "<li>" + w + "</li>";							
						}
						//newLogs += "<li>" + w + "</li>";
					}
					newLogs += "</ul>";
				}
				if (newLogs.length() > ("<h2>" + key + "</h2><ul></ul>").length()) {
					newFileLogs += newLogs;					
				}
			}		
			if (newFileLogs.length() > ("<h1 id=\"warning\">Warnings for import file '" + filename + "'<h1>").length()) {
				logWarnings += newFileLogs;
			}
		}
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
			  Date date = wb.getProperties().getCoreProperties().getCreated();
			  if (date != null) result = date.getTime();
			  if (result < new GregorianCalendar(2012,1,1,0,0,0).getTime().getTime()) {
					Sheet transactionSheet = wb.getSheet("BackTracing");
					Sheet forSheet = wb.getSheet("ForTracing");
					Sheet fwdSheet = wb.getSheet("FwdTracing");
					if (forSheet == null) forSheet = fwdSheet;
					
					boolean isForTracing = forSheet != null;
					if (isForTracing) transactionSheet = forSheet;
					
					if (transactionSheet != null) {
						Row row = transactionSheet.getRow(2);
						MetaInfo mi = getMetaInfo(exceptions, row, transactionSheet.getRow(1));
						result = mi.getDateInMillis();
					}
			  }
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
		  return result;
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
