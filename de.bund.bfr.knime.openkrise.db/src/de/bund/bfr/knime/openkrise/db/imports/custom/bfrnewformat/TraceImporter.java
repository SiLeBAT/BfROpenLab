/*******************************************************************************
 * Copyright (c) 2014-2025 German Federal Institute for Risk Assessment (BfR)
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

import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import java.util.stream.Collectors;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.filechooser.FileFilter;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jooq.tools.StringUtils;

import de.bund.bfr.knime.MemoryUtils;
import de.bund.bfr.knime.UserCancelException;
import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;
import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.imports.MyImporter;
import de.bund.bfr.knime.ui.EdtUtils;
import de.bund.bfr.knime.ui.IProgressMonitor;
import de.bund.bfr.knime.ui.ProgressMonitorParent;

public class TraceImporter extends FileFilter implements MyImporter {

	private MyDBI mydbi;
	
	private static final Pattern multiLinePattern = Pattern.compile("\\r\\n|\\r|\\n");
	
	private static final String LINE_BREAKS_REMOVED = "Line breaks in multiline cell were replaced by space";
	
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
	private DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
	
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
	
	private void checkStationsFirst(List<Exception> exceptions, Sheet businessSheet, IProgressMonitor taskMonitor) throws Exception {
		HashSet<String> stationIDs = new HashSet<>();
		int numRows = businessSheet.getLastRowNum() + 1;
		for (int i=1;i<numRows;i++) {
			Row row = businessSheet.getRow(i);
			if (row != null) {
				String id = getCellString(row.getCell(0));
				Cell nameCell = row.getCell(1);
				
				if (id == null && isCellEmpty(nameCell)) return;
				if (id == null) exceptions.add(new Exception("Station has no ID -> Row " + (i+1)));
				if (stationIDs.contains(id)) exceptions.add(new Exception("Station ID '" + id + "' is defined more than once -> Row " + (i+1)));

				stationIDs.add(id);
			}
			if (taskMonitor.isCanceled()) throw new UserCancelException();
		}
	}
	
	private void checkDeliveriesFirst(List<Exception> exceptions, Sheet deliverySheet, IProgressMonitor taskMonitor) throws Exception {
		HashSet<String> deliveryIDs = new HashSet<>();
		int numRows = deliverySheet.getLastRowNum() + 1;
		for (int i=2;i<numRows;i++) {
			Row row = deliverySheet.getRow(i);
			if (row != null) {
				String id = getCellString(row.getCell(0));
				String senderId = getCellString(row.getCell(1));
				
				if (id == null && senderId == null) return;
				if (id == null) exceptions.add(new Exception("Delivery has no ID -> Row " + (i+1)));
				if (deliveryIDs.contains(id)) exceptions.add(new Exception("Delivery ID '" + id + "' is defined more than once -> Row " + (i+1)));
				
				deliveryIDs.add(id);
			}
			if (taskMonitor.isCanceled()) throw new UserCancelException();
			taskMonitor.setProgress(i * 100 / numRows);
		}
		taskMonitor.setProgress(100);
	}
	
	private void checkTraceDeliveries(List<Exception> exceptions, Sheet deliverySheet, int borderRowBetweenTopAndBottom, boolean isForTracing, boolean isNewFormat_151105, IProgressMonitor taskMonitor) throws Exception {
		HashMap<String, HashSet<Row>> deliveryIDs = new HashMap<>();
		
		ProgressMonitorParent taskMonitorParent= new ProgressMonitorParent(taskMonitor, 2);
		IProgressMonitor subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
		
		int numRows = deliverySheet.getLastRowNum() + 1;
		for (int i=2;i<numRows;i++) {
			Row row = deliverySheet.getRow(i);
			if (row != null) {
				String cellString = getCellString(row.getCell(12)); // DeliveryID in DB
				if (
					cellString != null && 
					!cellString.isEmpty() && 
					!cellString.equals("DeliveryID in DB")
				) {
					if (!deliveryIDs.containsKey(cellString)) deliveryIDs.put(cellString, new HashSet<Row>());
					HashSet<Row> hs = deliveryIDs.get(cellString);
					hs.add(row);						
				}
				
				if (taskMonitor.isCanceled()) throw new UserCancelException();
				subTaskMonitor.setProgress(i * 100 / numRows);
			}			
		}
		
		subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
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
			if (taskMonitor.isCanceled()) throw new UserCancelException();
		}
		subTaskMonitor.setProgress(100);
	}
	
	private String getRowKey(Row row, int borderRowBetweenTopAndBottom, boolean isForTracing) throws Exception {
		boolean isProductsOut = row.getRowNum() < borderRowBetweenTopAndBottom && !isForTracing || isForTracing && row.getRowNum() > borderRowBetweenTopAndBottom;
		String key = "";
		for (int j=isProductsOut?0:1;j<row.getLastCellNum();j++) { // Start with Lot Number or after
			String cellString = getCellString(row.getCell(j));
			key += cellString;
			key += ";";
		}
		while(key.endsWith(";;")) {
			key = key.substring(0, key.length() - 1);
		}	
		return key;
	}
	
	private void loadLookupSheet(Sheet lookupSheet, IProgressMonitor taskMonitor) throws Exception {
		LookUp lu = new LookUp();
		int numRows = lookupSheet.getLastRowNum() + 1;
		for (int i=1;i<numRows;i++) {
			Row row = lookupSheet.getRow(i);
			if (row != null) {
				
				String sampling = getCellString(row.getCell(0));
				if (sampling != null) lu.addSampling(sampling);
				
				String typeOfBusiness = getCellString(row.getCell(1));
				if (typeOfBusiness != null) lu.addTypeOfBusiness(typeOfBusiness);
				
				String treatment = getCellString(row.getCell(2));
				if (treatment != null) lu.addTreatment(treatment);
					
				String unit = getCellString(row.getCell(3));
				if (unit != null) lu.addUnit(unit);
			}
			if (taskMonitor.isCanceled()) throw new UserCancelException();
			taskMonitor.setProgress(i * 100 / numRows);
		}
		lu.intoDb(mydbi);
		taskMonitor.setProgress(100);
	}
	
	private void sleep() { //int ms) {
//		try {
//			Thread.sleep(ms);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	private void debug(String msg) {
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
		String time = LocalTime.now().format(formatter);
		System.out.println(time + " " + msg);
	}
	
	private String formatDayNanos(long dayNanos) {
		long dayMs = dayNanos / 1000000;
		long hours = dayMs / (3600 * 1000);
		long mins = (dayMs / (60 * 1000)) % 60;
		long secs = (dayMs / 1000) % 60;
		long ms = dayMs % 1000;
		String result = "";
		if (secs == 0 && mins == 0 && hours == 0) return "" + ms;
		result = "." + StringUtils.leftPad("" + ms, 3, "0");
		if (mins == 0 && hours == 0) return "" + secs + result;
		result = ":" + StringUtils.leftPad("" + secs, 2, "0") + result;
		if (hours == 0) return "" + mins + result;
		return "" + hours + ":" + StringUtils.leftPad("" + mins, 2, "0") + result;
	}
	
	private List<Exception> importWorkbook(Workbook wb, String filename, final Window owner, IProgressMonitor taskMonitor)  throws Exception {

		List<Exception> exceptions = new ArrayList<>();
		
		Sheet stationSheet = wb.getSheet("Stations");
		if (stationSheet == null) return importSingleSheetWorkbook(wb, filename, owner, taskMonitor);
		
		Sheet deliverySheet = wb.getSheet("Deliveries");
		Sheet d2dSheet = wb.getSheet("Deliveries2Deliveries");
		Sheet transactionSheet = wb.getSheet("BackTracing");
		Sheet lookupSheet = wb.getSheet("LookUp");
		Sheet forwardSheet = wb.getSheet("Opt_ForwardTracing");
		Sheet forwardSheetNew = wb.getSheet("ForwardTracing_Opt");
		Sheet forSheet = wb.getSheet("ForTracing");
		Sheet fwdSheet = wb.getSheet("FwdTracing");
		if (forSheet == null) forSheet = fwdSheet;
		
		final boolean isForTracing = forSheet != null;
		if (isForTracing) transactionSheet = forSheet;
		
		if (stationSheet == null || transactionSheet == null && deliverySheet == null) {
			exceptions.add(new Exception("Wrong template format!"));
			return exceptions;
		}
		
		int requiredSubTaskMonitorCount = 1;
		if (deliverySheet != null) {
			requiredSubTaskMonitorCount = 4 + (d2dSheet != null ? 1 : 0) + (lookupSheet != null ? 1 : 0) + 2;
		} else {
			requiredSubTaskMonitorCount = 6 + (lookupSheet != null ? 1 : 0) + (!isForTracing ? 1 : 0);
		}
		ProgressMonitorParent taskMonitorParent = new ProgressMonitorParent(taskMonitor, requiredSubTaskMonitorCount);
		IProgressMonitor subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
				
		debug("TracingImporter.importWorkbook checking stations ...");
		
		checkStationsFirst(exceptions, stationSheet, subTaskMonitor);
		debug("TracingImporter.importWorkbook checking stations done.");
		subTaskMonitor.setProgress(100);
		sleep();
		
		if (deliverySheet != null) {
			debug("TracingImporter.importWorkbook checking deliveries ...");
			subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
			
			checkDeliveriesFirst(exceptions, deliverySheet, subTaskMonitor);
			
			debug("TracingImporter.importWorkbook checking deliveries done.");
			subTaskMonitor.setProgress(100);
			sleep();
			
			debug("TracingImporter.importWorkbook importing stations ...");
			subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
			
			// load all Stations
			HashMap<String, Station> stations = new HashMap<>();
			int numRows = stationSheet.getLastRowNum() + 1;
			Row titleRow = stationSheet.getRow(0);
			for (classRowIndex=1;classRowIndex<numRows;classRowIndex++) {
				Station s = getStation(titleRow, stationSheet.getRow(classRowIndex));
				if (s == null) break;
				if (stations.containsKey(s.getId())) exceptions.add(new Exception("Station defined twice -> Row " + (classRowIndex+1) + "; Station Id: '" + s.getId() + "'"));
				stations.put(s.getId(), s);
				subTaskMonitor.setProgress(classRowIndex * 100 / numRows);
			}
			debug("TracingImporter.importWorkbook importing stations done.");
			subTaskMonitor.setProgress(100);
			sleep();
			
			// load all Deliveries
			debug("TracingImporter.importWorkbook importing deliveries ...");
			subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
			
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
				subTaskMonitor.setProgress(classRowIndex * 100 / numRows);
			}
			debug("TracingImporter.importWorkbook importing deliveries done.");
			subTaskMonitor.setProgress(100);
			sleep();
			
			// load Recipes
			HashSet<D2D> recipes = new HashSet<>();
			if (d2dSheet != null) {
				debug("TracingImporter.importWorkbook importing d2ds ...");
				subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
				numRows = d2dSheet.getLastRowNum() + 1;
				titleRow = d2dSheet.getRow(0);
				for (classRowIndex=1;classRowIndex<numRows;classRowIndex++) {
					D2D dl = getD2D(exceptions, deliveries, titleRow, d2dSheet.getRow(classRowIndex), classRowIndex);
					if (dl == null) break;
					recipes.add(dl);
					if (taskMonitor.isCanceled()) throw new UserCancelException();
					subTaskMonitor.setProgress(classRowIndex * 100 / numRows);
				}
				debug("TracingImporter.importWorkbook importing d2ds done.");
				subTaskMonitor.setProgress(100);
				sleep();
			}

			MetaInfo mi = new MetaInfo();
			mi.setFilename(filename);
			
			if (lookupSheet != null) {
				debug("TracingImporter.importWorkbook looking up ...");
				subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
				loadLookupSheet(lookupSheet, subTaskMonitor);
				debug("TracingImporter.importWorkbook after lookup ...");
				subTaskMonitor.setProgress(100);
				sleep();
			}
			Integer miDbId = null;
			try {
				miDbId = mi.getID(mydbi);
			}
			catch (Exception e) {
				exceptions.add(e);
			}
			if (miDbId == null) exceptions.add(new Exception("Template already imported"));
			if (deliveries.size() == 0) {
				exceptions.add(new Exception("Template contains no deliveries"));
			}
			else {
				// Predefine DB IDs for Format_2017
				debug("TracingImporter.importWorkbook predefining ids ...");
				try {
					predefineIDs(deliveries.values());
				}
				catch (Exception e) {}
				
				debug("TracingImporter.importWorkbook processing CB 1 ...");
				subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
								
				int iDelivery = -1;
				long startTime = LocalTime.now().toNanoOfDay();
				long currentTime = startTime;
			
				for (Delivery d : deliveries.values()) {
					iDelivery++;
					try {
						d.getID(miDbId, false, mydbi);
					} 
					catch (Exception e) {
						exceptions.add(e);
					}
					//if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
					if (d.getExceptions().size() > 0) exceptions.addAll(d.getExceptions());
					if (taskMonitor.isCanceled()) throw new UserCancelException();

					if ((iDelivery % 1000) == 0) {
						long newCurrentTime = LocalTime.now().toNanoOfDay();
						debug(	
							"After DeliveryNo" + StringUtils.leftPad("" + (iDelivery + 1), 6) + ": " +
							"freespace: " + StringUtils.leftPad(MemoryUtils.getFormatedPresumableFreeMemory(), 14) + ", " +
							"consumed time: " + StringUtils.leftPad(formatDayNanos(newCurrentTime - startTime), 12)  + " ms, " +
							"lastDelta time: " + StringUtils.leftPad(formatDayNanos(newCurrentTime - currentTime), 12)  + " ms" //, " +
//							"ReqestCount: " + StringUtils.leftPad("" + (DBKernel.RequestCount - startRequestCount), 6) + ", " +
//							"avgRequestCount: " + ((DBKernel.RequestCount - startRequestCount) / (iDelivery + 1))
						);
						currentTime = newCurrentTime;
					}
					subTaskMonitor.setProgress(iDelivery * 100 / deliveries.size());
					MemoryUtils.checkMimimumRemainingMemory();
					checkUserCancel(taskMonitor);
				}
				debug("TracingImporter.importWorkbook processing CB 1 done.");
				subTaskMonitor.setProgress(100);
				sleep();
				
				debug("TracingImporter.importWorkbook processing CB 2 ...");
				subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
				
				HashMap<Delivery, HashSet<Integer>> ingredients = new HashMap<>(); 
				int recipeNo = 0;
				for (D2D dl : recipes) {
					recipeNo++;
					try {
						dl.getId(miDbId, mydbi);
					}
					catch (Exception e) {
						exceptions.add(e);
					}
					
					// collect data for checks if data is missing...
					Delivery d = dl.getTargetDelivery();
					if (!ingredients.containsKey(d)) ingredients.put(d, new HashSet<Integer>());
					HashSet<Integer> hd = ingredients.get(d);
					if (dl.getIngredient() != null) hd.add(dl.getIngredient().getDbId());
					subTaskMonitor.setProgress(recipeNo * 100 / recipes.size());
					checkUserCancel(taskMonitor);
				}
				debug("TracingImporter.importWorkbook processing CB 2 done.");
				subTaskMonitor.setProgress(100);
				sleep();
				
				debug("Freespace CP4: " + MemoryUtils.getFormatedPresumableFreeMemory());
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
		debug("TracingImporter.importWorkbook importing transactionsheet rows ...");
		subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
		
		if (forwardSheet != null) {
			// Station in focus
			{
				Cell stationInFocusCell = row.getCell(1);
				String cellString = getCellString(stationInFocusCell);
				
				if (cellString == null) exceptions.add(new Exception("Station in Focus is missing (cell " + getWbRelativeCellAddressString(stationInFocusCell) + ")."));
				
				sif = cellString == null ? null : getStation(exceptions, stationSheet, cellString, row);
			}
			
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
			{
				Cell stationInFocusCell = row.getCell(1);
				String cellString = getCellString(stationInFocusCell);
				
				if (cellString == null) exceptions.add(new Exception("Station in Focus is missing (cell " + getWbRelativeCellAddressString(stationInFocusCell) + ")."));
				
				sif = cellString == null ? null : getStation(exceptions, stationSheet, cellString, row);
			}
			
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
		debug("TracingImporter.importWorkbook importing transactionsheet rows done.");
		subTaskMonitor.setProgress(100);
		checkUserCancel(taskMonitor);
		sleep();
		
		System.out.println("TracingImporter.importWorkbook importing transactionsheet rows step 1 ...");
		subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
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
		debug("TracingImporter.importWorkbook importing transactionsheet rows step 2 done.");
		subTaskMonitor.setProgress(100);
		sleep();
		
		debug("TracingImporter.importWorkbook checking trace deliveries ...");
		subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
		checkTraceDeliveries(exceptions, transactionSheet, borderRowLotStart, isForTracing, isNewFormat_151105, subTaskMonitor);
		debug("TracingImporter.importWorkbook checking trace deliveries done.");
		subTaskMonitor.setProgress(100);
		sleep();

		debug("TracingImporter.importWorkbook importing transactionsheet rows step 3 ...");
		subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
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
		debug("TracingImporter.importWorkbook importing transactionsheet rows step 3 done.");
		subTaskMonitor.setProgress(100);
		sleep();
		
		// Opt_ForwardTracing
		HashSet<Delivery> forwDeliveries = new HashSet<>(); 
		if (!isForTracing) {
			debug("TracingImporter.importWorkbook importing transactionsheet rows step 4 ...");
			subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
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
			debug("TracingImporter.importWorkbook importing transactionsheet rows step 4 done.");
			subTaskMonitor.setProgress(100);
			checkUserCancel(taskMonitor);
			sleep();
		}
		
		if (lookupSheet != null) {
			debug("TracingImporter.importWorkbook iloading lookup sheet ...");
			subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
			loadLookupSheet(lookupSheet, subTaskMonitor);
			debug("TracingImporter.importWorkbook iloading lookup sheet done.");
			subTaskMonitor.setProgress(100);
			checkUserCancel(taskMonitor);
			sleep();
		}
		Integer miDbId = null;
		try {
			miDbId = mi.getID(mydbi);
		}
		catch (Exception e) {
			exceptions.add(e);
		}
		if (miDbId == null) exceptions.add(new Exception("File already imported"));
		
		debug("TracingImporter.importWorkbook inserting into db ...");
		subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
		if (isForTracing)
			try {
				insertForIntoDb(exceptions, miDbId, inDeliveries, outDeliveries);
			}
			catch (Exception e) {
				exceptions.add(e);
			}
		else
			try {
				insertIntoDb(exceptions, miDbId, inDeliveries, outDeliveries, forwDeliveries);
			}
			catch (Exception e) {
				exceptions.add(e);
			}
		debug("TracingImporter.importWorkbook inserting into db done.");
		subTaskMonitor.setProgress(100);
		sleep();
		return exceptions;
	}
	
	private String generateAddress(Station s) {
		String ad = s.getStreet()==null?"":s.getStreet();
		ad += (ad.isEmpty() ? "" : " ") + (s.getNumber()==null?"":s.getNumber());
		ad = ad.trim();
		ad += (ad.isEmpty() ? "" : ", ") + (s.getZip()==null?"":s.getZip());
		ad = ad.trim();
		ad += (ad.isEmpty() ? "" : " ") + (s.getCity()==null?"":s.getCity());
		ad = ad.trim();
		if (ad.endsWith(",")) ad.substring(0, ad.length() - 1).trim();
		if (ad.isEmpty()) ad = null;
		return ad;
	}
	
	private void predefineIDs(Collection<Delivery> deliveries) {
		for (Delivery delivery : deliveries) {
			Lot lot = delivery.getLot();
			Product product = lot.getProduct();
			Station station = product.getStation();	
			// ToDo: Why is station not checked for null
			
			station.setAddress(generateAddress(station));
			if (station.getDbId() == null) station.setDbId(genDbId(""+station.getName()+station.getAddress()));	
			if (product.getDbId() == null) product.setDbId(genDbId(""+station.getDbId()+product.getName()+product.getFlexible(XlsProduct.EAN("en"))));	
			
			String dd = delivery.getDepartureDay() == null || delivery.getDepartureMonth() == null || delivery.getDepartureYear() == null ? 
					delivery.getId() : 
					"" + delivery.getDepartureDay()+delivery.getDepartureMonth()+delivery.getDepartureYear();
			String ln = lot.getNumber() == null && lot.getFlexible(XlsLot.MHD("en")) == null ? 
					dd : 
					lot.getNumber()+lot.getFlexible(XlsLot.MHD("en"));
			
			if (lot.getDbId() == null) lot.setDbId(genDbId(""+product.getDbId() + ln));
			if (delivery.getUnitNumber() != null && delivery.getUnitUnit() != null) delivery.addFlexibleField("Amount", delivery.getUnitNumber() + " " + delivery.getUnitUnit());
			else if (delivery.getUnitNumber() != null) delivery.addFlexibleField("Amount", delivery.getUnitNumber()+"");
			
			Station receiver = delivery.getReceiver();
			// ToDo: Why is receiver not checked for null
			receiver.setAddress(generateAddress(receiver));
			if (receiver.getDbId() == null) receiver.setDbId(genDbId(""+receiver.getName()+receiver.getAddress()));					
			if (delivery.getDbId() == null) delivery.setDbId(genDbId(""+lot.getDbId() + delivery.getDepartureDay()+delivery.getDepartureMonth()+delivery.getDepartureYear()+delivery.getFlexible("Amount")+delivery.getComment()+receiver.getDbId()));
		}
	}

	@FunctionalInterface
	public interface ApplyStringFunction {

	    void applyString(String value);

	}
	
	@FunctionalInterface
	public interface ApplyIntFunction {

	    void applyInt(Integer value);

	}
	
	@FunctionalInterface
	public interface ApplyDoubleFunction {

	    void applyDouble(Double value);

	}
	
	@FunctionalInterface
	public interface AddFlexibleFieldFunction {
		void addFlexibleField(String key, String value);
	}
	
	
	
	private void applyCellString(Cell cell, ApplyStringFunction fun) throws Exception {
		String cellString = getCellString(cell);
		fun.applyString(cellString);
	}
	
	private void applyCellInt(Cell cell, ApplyIntFunction fun) {
		Integer cellInt = getCellInt(cell);
		fun.applyInt(cellInt);
	}
	
	private void applyCellDouble(Cell cell, ApplyDoubleFunction fun) {
		Double cellDouble = getCellDouble(cell);
		fun.applyDouble(cellDouble);
	}
	
	private String getLabelCellString(Cell cell) throws Exception {
		return getCellString(cell, false, false, false);
	}
	
	private String getCellString(Cell cell) throws Exception {
		return getCellString(cell, false, true, true);
	}
	
	private String getCellString(Cell cell, boolean checkIfDate) throws Exception {
		return getCellString(cell, checkIfDate, true, true);
	}
	
//	private String getCellString(Cell cell, boolean checkIfDate, boolean removeLinebreaks) {
//		return getCellString(cell, checkIfDate, removeLinebreaks, true);
//	}
	
	private String getCellString(Cell cell, boolean checkIfDate, boolean removeLinebreaks, boolean reportLineBreaks) throws Exception {
		if (cell == null) return null;
			
		CellType cellType = cell.getCellType();
		if (cellType == CellType.FORMULA) cellType = cell.getCachedFormulaResultType();
		if (cellType == CellType.BLANK) return null;
		if (cellType == CellType.ERROR) throwCellErrorException(cell);
			
		if (checkIfDate && cellType == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
			Date date = cell.getDateCellValue();
			return df.format(date);
		}
		if (cellType == CellType.NUMERIC) {
			// ToDo: Check whether this replacement is sufficient
			return NumberToTextConverter.toText(cell.getNumericCellValue());
		}
		if (cellType == CellType.BOOLEAN) {
			// ToDo: Check whether this replacement is sufficient
			return cell.getBooleanCellValue() ? "TRUE" : "FALSE";
		}
			
//				CellType tmpCellType = cell.getCellType();
//				Date tmpDate = null;
//				if (tmpCellType == CellType.NUMERIC) tmpDate = cell.getDateCellValue();
//				Boolean tmpBool = null;
//				if (tmpCellType == CellType.BOOLEAN) tmpBool = cell.getBooleanCellValue();
//				String tmpStr = null;
//				if (tmpCellType == CellType.STRING) tmpStr = cell.getStringCellValue();
//				Double tmpNum = null;
//				if (tmpCellType == CellType.NUMERIC) tmpNum = cell.getNumericCellValue();
//				
//				CellType tmpFormResType = null;
//				if (tmpCellType == CellType.FORMULA) tmpFormResType = cell.getCachedFormulaResultType();
//				if (tmpFormResType == CellType.STRING) tmpStr = cell.getStringCellValue();
//				if (tmpFormResType == CellType.BOOLEAN) tmpBool = cell.getBooleanCellValue();
//				if (tmpFormResType == CellType.NUMERIC) tmpNum = cell.getNumericCellValue();
				
		return getStringCellString(cell, removeLinebreaks, reportLineBreaks);
//				cell.setCellType(CellType.STRING);
//				String cellString = getStr(cell.getStringCellValue());
//				if (cellString != null && cellString.matches("\\r\n\\|\\r|\\n")) {
//					Set<String> oldWarns = warns.get(LINE_BREAKS_REMOVED);
//					if (oldWarns == null) oldWarns = new HashSet<>();
//					oldWarns.add("Sheet: " + cell.getSheet().getSheetName() + ", Cell: " + cell.getAddress().toString()); // cell.getAddress().toString());
//					cellString = getStr(cellString.replaceAll("\\r\n\\|\\r|\\n", " "));
//				};
//				
//				return cellString;				
//			}
//			// return null;
//		} 
//		return null;
//		catch(IllegalStateException ex) {
//			throw new IllegalStateException(ex.getMessage() + " (Cell: " + cell.getAddress().toString() + ")");
//		}
	}
	
	private String getStringCellString(Cell cell, boolean removeLinebreaks, boolean reportLineBreaks) {
		if (cell == null) return null;
		CellType cellType = cell.getCellType();
		if (cellType == CellType.FORMULA) cellType = cell.getCachedFormulaResultType();
		if (cellType != CellType.STRING) return null;		
			
		String cellString = getStr(cell.getStringCellValue());
		if (cellString != null && removeLinebreaks && cellString.matches("(.*(\\r|\\n).*)+")) {
			
			cellString = getStr(cellString.replaceAll("(\\s*(\\r\n\\|\\r|\\n)\\s*)+", " "));
			if (cellString != null && reportLineBreaks) {
				if (cell.getAddress().toString().equals("K5")) {
					Object tmp = 1;
				}
				String tmppCellAddress = cell.getAddress().toString();
				addWarning(LINE_BREAKS_REMOVED, getWbRelativeCellAddressString(cell));
				// Set<String> oldWarns = warns.get(LINE_BREAKS_REMOVED);
				// if (oldWarns == null) oldWarns = new HashSet<>();
				// oldWarns.add("Sheet: " + cell.getSheet().getSheetName() + ", Cell: " + cell.getAddress().toString()); // cell.getAddress().toString());
			}
		};
		
		return cellString;				
	}
	
//	private String isStringCellEmpty(Cell cell, boolean labelCell) {
//		return getStringCellString(cell, !labelCell, false);
//	}
	
	private String getWbRelativeCellAddressString(Cell cell) {
		return cell.getAddress().toString() + (cell.getSheet().getWorkbook().getNumberOfSheets() == 1 ? "" : " (sheet: " + cell.getSheet().getSheetName() + ")");
	}
	
	private Integer getCellInt(Cell cell) throws NumberFormatException {
		if (cell == null || cell.getCellType() == CellType.BLANK) return null;
		cell.setCellType(CellType.STRING); 
		String cellString = cell.getStringCellValue();
		try {
			return Integer.parseInt(cellString);
		} catch(NumberFormatException ex) {
			ex.printStackTrace();
			// throw new NumberFormatException("Value in cell " + getWbRelativeCellAddressString(cell) + " is expected be a integer.");
		}
		return null;
		
	}
	
	private Double getCellDouble(Cell cell) throws NumberFormatException {
		if (cell == null || cell.getCellType() == CellType.BLANK) return null;
		if (cell.getCellType() == CellType.NUMERIC) {
			return cell.getNumericCellValue();
		}
		cell.setCellType(CellType.STRING); 
		String cellString = cell.getStringCellValue();
		try {
			return Double.parseDouble(cellString);
		} catch(NumberFormatException ex) {
			throw ex;
			// throw new NumberFormatException("Value in cell " + getWbRelativeCellAddressString(cell) + " is expected to be a number.");
		}
		
	}
//	
//	private Double getCellDbl(Cell cell) {
//		
//	}
	
	private boolean rowEmpty(Row row) throws Exception {
		for (int i=0;i<row.getPhysicalNumberOfCells();i++) {
			// ToDo: check whether this is sufficient
			if (!isCellEmpty(row.getCell(i))) return false;
//			String cs = getCellString(row.getCell(i));
//			if (cs != null) return false;
		}
		return true;
	}
	
	private List<Exception> importSingleSheetWorkbook(Workbook wb, String filename, Window owner, IProgressMonitor taskMonitor) throws Exception {
		System.out.println("TracingImporter.importSingleSheetWorkbook entered ...");
		
		List<Exception> exceptions = new ArrayList<>();
		
		boolean backtracing = true;
		boolean isProduction = false;
		String lang = null;
		boolean isAllInOneTemplate = false; // All in One Template introduced for Fipronil

		Sheet sheet = wb.getSheet(XlsStruct.getBACK_SHEETNAME("de")); if (sheet != null) {lang = "de"; isProduction = false; backtracing = true;}
		if (sheet == null) {sheet = wb.getSheet(XlsStruct.getFWD_SHEETNAME("de")); if (sheet != null) {lang = "de"; isProduction = false; backtracing = false;}}
		if (sheet == null) {sheet = wb.getSheet(XlsStruct.getPROD_BACK_SHEETNAME("de")); if (sheet != null) {lang = "de"; isProduction = true; backtracing = true;}}
		if (sheet == null) {sheet = wb.getSheet(XlsStruct.getPROD_FWD_SHEETNAME("de")); if (sheet != null) {lang = "de"; isProduction = true; backtracing = false;}}
		if (sheet == null) {sheet = wb.getSheet(XlsStruct.getAiO_SHEETNAME("de")); if (sheet != null) {lang = "de"; isAllInOneTemplate = true; isProduction = false; backtracing = true;}}
		
		if (sheet == null) {sheet = wb.getSheet(XlsStruct.getBACK_SHEETNAME("en")); if (sheet != null) {lang = "en"; isProduction = false; backtracing = true;}}
		if (sheet == null) {sheet = wb.getSheet(XlsStruct.getFWD_SHEETNAME("en")); if (sheet != null) {lang = "en"; isProduction = false; backtracing = false;}}
		if (sheet == null) {sheet = wb.getSheet(XlsStruct.getPROD_BACK_SHEETNAME("en")); if (sheet != null) {lang = "en"; isProduction = true; backtracing = true;}}
		if (sheet == null) {sheet = wb.getSheet(XlsStruct.getPROD_FWD_SHEETNAME("en")); if (sheet != null) {lang = "en"; isProduction = true; backtracing = false;}}		
		if (sheet == null) {sheet = wb.getSheet(XlsStruct.getAiO_SHEETNAME("en")); if (sheet != null) {lang = "en"; isAllInOneTemplate = true; isProduction = false; backtracing = true;}}
		
		if (sheet == null) {sheet = wb.getSheet(XlsStruct.getBACK_SHEETNAME("es")); if (sheet != null) {lang = "es"; isProduction = false; backtracing = true;}}
		if (sheet == null) {sheet = wb.getSheet(XlsStruct.getFWD_SHEETNAME("es")); if (sheet != null) {lang = "es"; isProduction = false; backtracing = false;}}
		if (sheet == null) {sheet = wb.getSheet(XlsStruct.getPROD_BACK_SHEETNAME("es")); if (sheet != null) {lang = "es"; isProduction = true; backtracing = true;}}
		if (sheet == null) {sheet = wb.getSheet(XlsStruct.getPROD_FWD_SHEETNAME("es")); if (sheet != null) {lang = "es"; isProduction = true; backtracing = false;}}
				
		HashMap<Integer, Station> idToStationMap = new HashMap<>();
		HashMap<Integer, Product> idToProductMap = new HashMap<>();
		HashMap<Integer, Lot> idToLotMap = new HashMap<>();
		HashMap<Integer, Delivery> idToDeliveryMap = new HashMap<>();
		LinkedHashMap<String, Delivery> olddelsRow = new LinkedHashMap<>();
		LinkedHashMap<Integer, HashSet<Delivery>> olddelsLot = new LinkedHashMap<>();
		HashMap<String, Integer> lotNumberToLotId = new HashMap<>();
		HashSet<String> lotDoublettes = new HashSet<>();

		if (sheet != null) {
			// region sheet_exists
			Station focusStation = null;
			Row row;
			// String cs, address;
			// int sID;
			if (!isAllInOneTemplate) {
				focusStation = new Station();
				row = sheet.getRow(0);
				HashMap<String, Integer> hmS = TraceGenerator.getFirstRow(sheet);
				String cs = getCellString(row.getCell(hmS.get("name")));
				focusStation.setName(cs);
				String address = getCellString(row.getCell(hmS.get("address")));
				focusStation.setAddress(address);
				focusStation.setCountry(getCellString(row.getCell(hmS.get("country"))));
				if (!isProduction && hmS.get("tob") >= 0) {
					focusStation.setTypeOfBusiness(getCellString(row.getCell(hmS.get("tob"))));
				}
				focusStation.addFlexibleField(XlsStruct.getOUT_SOURCE_KEY("en"), XlsStruct.getOUT_SOURCE_VAL(lang) + " " + 1);
				int sID = genDbId(""+cs+address);
				focusStation.setId(""+sID);
				idToStationMap.put(sID, focusStation);				
			}
			
			int numRows = sheet.getLastRowNum() + 1;
			boolean doCollect = false;
			XlsStation xlsS = new XlsStation();
			XlsStation xlsSRecipient = new XlsStation();
			XlsProduct xlsP = new XlsProduct();
			XlsLot xlsL = new XlsLot();
			XlsDelivery xlsD = new XlsDelivery();
			XlsOther xlsO = new XlsOther();
			boolean doPreCollect = false;
			boolean chargenLinkTypeChecked = false;
			ChargenLinkType preferedChargenLinkType = null;
			
			for (int iRow=(isAllInOneTemplate?0:1);iRow<=numRows;iRow++) {
				checkUserCancel(taskMonitor);
				row = sheet.getRow(iRow);
				if (row != null) {
					if (!rowEmpty(row)) {
						String cellString__ = getCellString(row.getCell(0), false, false, false);
						if (cellString__ != null && !isAllInOneTemplate || isAllInOneTemplate && iRow > 1) doPreCollect = false; //  && cs.startsWith(XlsStruct.TOP_END_LINE)
						if (doCollect || doPreCollect) {
							//System.err.print(i+1);
							if (isAllInOneTemplate) {
								focusStation = null;
								String name = getCellString(row.getCell(xlsSRecipient.getNameCol()));
								String address = getCellString(row.getCell(xlsSRecipient.getAddressCol()));
								int sID = genDbId(""+name+address);
								if (idToStationMap.containsKey(sID)) {
									focusStation = idToStationMap.get(sID);
									focusStation.addFlexibleField(XlsStruct.getOUT_SOURCE_KEY("en"), focusStation.getFlexible(XlsStruct.getOUT_SOURCE_KEY(lang)) + "; " + XlsStruct.getOUT_SOURCE_VAL(lang) + " " + (iRow+1));
								}
								else {
									focusStation = new Station();
									focusStation.setName(name);
									focusStation.setAddress(address);
									if (xlsSRecipient.getCountryCol() >= 0) focusStation.setCountry(getCellString(row.getCell(xlsSRecipient.getCountryCol())));
									if (xlsSRecipient.getTobCol() >= 0) focusStation.setTypeOfBusiness(getCellString(row.getCell(xlsSRecipient.getTobCol())));
									focusStation.addFlexibleField(XlsStruct.getOUT_SOURCE_KEY("en"), XlsStruct.getOUT_SOURCE_VAL(lang) + " " + (iRow+1));
									focusStation.setId(""+sID);
									idToStationMap.put(sID, focusStation);
								}
								if (xlsSRecipient.getExtraVals().size() > 0) {
									for (int colnum : xlsSRecipient.getExtraVals().keySet()) {
										String val = getCellString(row.getCell(colnum));
										if (val != null) {
											focusStation.addFlexibleField(xlsSRecipient.getExtraVals().get(colnum), val);
										}
									}
								}
							}

							String name = getCellString(row.getCell(xlsS.getNameCol()));
							String address = getCellString(row.getCell(xlsS.getAddressCol()));
							int sID = genDbId(""+name+address);
							Station supplierS = null;
							if (idToStationMap.containsKey(sID)) {
								supplierS = idToStationMap.get(sID);
								supplierS.addFlexibleField(XlsStruct.getOUT_SOURCE_KEY("en"), supplierS.getFlexible(XlsStruct.getOUT_SOURCE_KEY(lang)) + "; " + XlsStruct.getOUT_SOURCE_VAL(lang) + " " + (iRow+1));
							}
							else {
								supplierS = new Station();
								supplierS.setName(name);
								supplierS.setAddress(address);
								if (xlsS.getCountryCol() >= 0) supplierS.setCountry(getCellString(row.getCell(xlsS.getCountryCol())));
								if (xlsS.getTobCol() >= 0) supplierS.setTypeOfBusiness(getCellString(row.getCell(xlsS.getTobCol())));
								supplierS.addFlexibleField(XlsStruct.getOUT_SOURCE_KEY("en"), XlsStruct.getOUT_SOURCE_VAL(lang) + " " + (iRow+1));
								supplierS.setId(""+sID);
								idToStationMap.put(sID, supplierS);
							}
							if (xlsS.getExtraVals().size() > 0) {
								for (int colnum : xlsS.getExtraVals().keySet()) {
									String val = getCellString(row.getCell(colnum));
									if (val != null) {
										supplierS.addFlexibleField(xlsS.getExtraVals().get(colnum), val);
									}
								}
							}
							
							String f2 = getCellString(row.getCell(xlsP.getNameCol()));
							String f3 = xlsP.getEanCol() < 0 ? null : getCellString(row.getCell(xlsP.getEanCol()));
							int pID = genDbId(""+(backtracing==doPreCollect?focusStation.getId():supplierS.getId()) + f2 + f3);
							//System.err.println(pID + " - " + f2 + " - " + f3 + " - " + focusS.getId() + " - " + supplierS.getId() + " - " + backtracing + " - " + doPreCollect);
							Product p = null;
							if (idToProductMap.containsKey(pID)) {
								p = idToProductMap.get(pID);
								p.addFlexibleField(XlsStruct.getOUT_SOURCE_KEY("en"), p.getFlexible(XlsStruct.getOUT_SOURCE_KEY(lang)) + "; " + XlsStruct.getOUT_SOURCE_VAL(lang) + " " + (iRow+1));
							}
							else {
								p = new Product();
								if (doPreCollect) {
									if (!backtracing) p.setStation(supplierS);
									else p.setStation(focusStation);
								}
								else {
									if (backtracing) p.setStation(supplierS);
									else p.setStation(focusStation);									
								}
								p.setName(f2);
								p.addFlexibleField(XlsProduct.EAN("en"), f3);
								p.addFlexibleField(XlsStruct.getOUT_SOURCE_KEY("en"), filename + " - " + XlsStruct.getOUT_SOURCE_VAL(lang) + " " + (iRow+1));
								p.setId(pID);
								idToProductMap.put(pID, p);
							}
							if (xlsP.getExtraVals().size() > 0) {
								for (int colnum : xlsP.getExtraVals().keySet()) {
									String val = getCellString(row.getCell(colnum));
									if (val != null) {
										p.addFlexibleField(xlsP.getExtraVals().get(colnum), val);
									}
								}
							}

							f2 = getCellString(row.getCell(xlsL.getLotCol()), true);
							f3 = getCellString(row.getCell(xlsL.getMhdCol()), true);
							Integer f4 = null, f5 = null, f6 = null;
							String f8 = "";
							if (isAllInOneTemplate) {
								String date = getCellString(row.getCell(xlsD.getDayCol()), true);
								if (date != null) {
								    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
								    Date d = null;
								    try {
										d = formatter.parse(date);
									} catch (ParseException e) {}
								    formatter = new SimpleDateFormat("dd.MM.yyyy");
								    try {
										d = formatter.parse(date);
									} catch (ParseException e) {}
								    formatter = new SimpleDateFormat("dd/MM/yyyy");
								    try {
										d = formatter.parse(date);
									} catch (ParseException e) {}
								    if (d != null) {
										Calendar calendar = new GregorianCalendar();
										calendar.setTime(d);
										f4 = calendar.get(Calendar.DAY_OF_MONTH);
										f5 = calendar.get(Calendar.MONTH) + 1;
										f6 = calendar.get(Calendar.YEAR);
								    }
								    else {
								    	System.err.println("date not recognized: " + date);
								    	f8 += "Delivery date: " + date + "?!?\n";
								    }
								}
							}
							else {
								if (xlsD.getDayCol()<0) { 
									if (xlsD.getDeliveryDateCol()<0)  {
										throw new Exception("Delivery date columns not found.");
									} else {
										throw new Exception("Delivery date day column not found.");
									}
								}
								f4 = getInt(getCellString(row.getCell(xlsD.getDayCol())));
								if (xlsD.getMonthCol()<0) { throw new Exception("Delivery date month column not found."); }
								f5 = getInt(getCellString(row.getCell(xlsD.getMonthCol())));
								if (xlsD.getYearCol()<0) { throw new Exception("Delivery date year column not found."); }
								f6 = getInt(getCellString(row.getCell(xlsD.getYearCol())));
							}
							String f7 = xlsD.getAmountCol() >= 0 ? getCellString(row.getCell(xlsD.getAmountCol())) : null;
							if (xlsD.getCommentCol() >= 0) {
								String val = getCellString(row.getCell(xlsD.getCommentCol()));
								if (val != null) f8 += val + "\n";
							}
							if (xlsO.getCommentCol() >= 0) {
								String val = getCellString(row.getCell(xlsO.getCommentCol()));
								if (val != null) f8 += val + "\n";
							}
							f8 = f8.trim();
							if (f8.isEmpty()) f8 = null;
							if (isAllInOneTemplate) {
								f2 = getCellString(row.getCell(6)); // Erzeugercode(s)
							}
							if (isAllInOneTemplate && f2 == null && f3 == null) {
								f2 = "L." + iRow + ".";
							}
							int lID;
							if (f2 != null) {
								lID = genDbId(""+p.getId() + f2);
							}
							else if (f3 != null) {
								lID = genDbId(""+p.getId() + f3);
							}
							else {
								if (f4 == null && f5 == null && f6 == null && f7 == null && supplierS.getName() == null) {
									exceptions.add(new Exception("You have no lot information at all in Row " + (iRow+1) + "."));
								}
								else if (f4 == null && f5 == null && f6 == null && f7 == null) {
									if (isProduction && backtracing && doPreCollect) f2 = "[receiver: " + supplierS.getName() + "]";
									if (isProduction && backtracing && !doPreCollect) f2 = "[receiver: " + focusStation.getName() + "]";
									if (isProduction && !backtracing && doPreCollect) f2 = "[receiver: " + focusStation.getName() + "]";
									if (isProduction && !backtracing && !doPreCollect) f2 = "[receiver: " + supplierS.getName() + "]";
									if (!isProduction && backtracing) f2 = "[receiver: " + focusStation.getName() + "]";
									if (!isProduction && !backtracing) f2 = "[receiver: " + supplierS.getName() + "]";
								}
								else {
									f2 = "[delivery " + (f6==null ? "" : f6) + "" + (f5==null ? "" : f5) + "" + (f4==null ? "" : f4) + "" + (f7==null ? "" : "_"+f7) + "]";
								}
								lID = genDbId(""+p.getId() + f2);
							}
							Lot lot = null;
							if (idToLotMap.containsKey(lID)) {
								lot = idToLotMap.get(lID);
								lot.addFlexibleField(XlsStruct.getOUT_SOURCE_KEY("en"), lot.getFlexible(XlsStruct.getOUT_SOURCE_KEY(lang)) + "; " + XlsStruct.getOUT_SOURCE_VAL(lang) + " " + (iRow+1));
							}
							else {
								lot = new Lot();
								lot.setProduct(p);
								lot.setNumber(f2);
								lot.addFlexibleField(XlsLot.MHD("en"), f3);
								lot.addFlexibleField(XlsStruct.getOUT_SOURCE_KEY("en"), filename + " - " + XlsStruct.getOUT_SOURCE_VAL(lang) + " " + (iRow+1));
								lot.setId(lID);
								idToLotMap.put(lID, lot);
							}
							if (xlsL.getExtraVals().size() > 0) {
								for (int colnum : xlsL.getExtraVals().keySet()) {
									String val = getCellString(row.getCell(colnum));
									if (val != null) {
										lot.addFlexibleField(xlsL.getExtraVals().get(colnum), val);
									}
								}
							}

							int  dID = genDbId(""+lot.getId()+f4+f5+f6+f7+f8+(doPreCollect==backtracing?supplierS.getId():focusStation.getId()));						
							Delivery d = null;
							if (doPreCollect) {
								d = new Delivery();
								d.setLot(lot);
								d.setArrivalDay(f4);
								d.setArrivalMonth(f5);
								d.setArrivalYear(f6);
								if (f7 != null) d.addFlexibleField("Amount", f7);
								d.setComment(f8);
								if (doPreCollect) {
									if (!backtracing) d.setReceiver(focusStation);
									else d.setReceiver(supplierS);									
								}
								else {
									if (!backtracing) d.setReceiver(supplierS);
									else d.setReceiver(focusStation);
								}
								d.addFlexibleField(XlsStruct.getOUT_SOURCE_KEY("en"), filename + " - " + XlsStruct.getOUT_SOURCE_VAL(lang) + " " + (iRow+1));
								d.setId(dID+"");
								olddelsRow.put((iRow+1)+"", d);
								if (!olddelsLot.containsKey(d.getLot().getId())) olddelsLot.put(d.getLot().getId(), new HashSet<Delivery>());
								olddelsLot.get(d.getLot().getId()).add(d);
								if (lotNumberToLotId.containsKey(d.getLot().getNumber())) {
									if (lotNumberToLotId.get(d.getLot().getNumber()).intValue() != d.getLot().getId().intValue()) {
										lotDoublettes.add(d.getLot().getNumber());
									}
								}
								lotNumberToLotId.put(d.getLot().getNumber(), d.getLot().getId());
							}
							else {
								if (idToDeliveryMap.containsKey(dID)) {
									System.err.println(iRow + "->" + focusStation.getName() + " -> " + idToDeliveryMap.get(dID).getReceiver().getName());
									d = idToDeliveryMap.get(dID);
									d.addFlexibleField(XlsStruct.getOUT_SOURCE_KEY("en"), d.getFlexible(XlsStruct.getOUT_SOURCE_KEY(lang)) + "; " + XlsStruct.getOUT_SOURCE_VAL(lang) + " " + (iRow+1));
								}
								else {
									d = new Delivery();
									d.setLot(lot);
									d.setArrivalDay(f4);
									d.setArrivalMonth(f5);
									d.setArrivalYear(f6);
									d.addFlexibleField("Amount", f7);
									d.setComment(f8);								
									if (backtracing) d.setReceiver(focusStation);
									else d.setReceiver(supplierS);
									d.addFlexibleField(XlsStruct.getOUT_SOURCE_KEY("en"), filename + " - " + XlsStruct.getOUT_SOURCE_VAL(lang) + " " + (iRow+1));
									d.setId(dID+"");
									if (idToDeliveryMap.put(dID, d) != null) {
										System.err.println("did doppelt???");
									};
								}
								if (xlsD.getExtraVals().size() > 0) {
									for (int colnum : xlsD.getExtraVals().keySet()) {
										String val = getCellString(row.getCell(colnum));
										if (val != null) {
											d.addFlexibleField(xlsD.getExtraVals().get(colnum), val);
										}
									}
								}
								if (xlsO.getExtraVals().size() > 0) {
									for (int colnum : xlsO.getExtraVals().keySet()) {
										String val = getCellString(row.getCell(colnum));
										if (val != null) {
											d.addFlexibleField(xlsO.getExtraVals().get(colnum), val);
										}
									}
								}
							}
							
// extra fields in das template mit integrieren!!!

// Verhalten ändern bei Generator "1": all Infos according to this station listen?
// stationsspezifisches traceback jetzt nur noch für missing ingredients!!! Nicht mehr allumfassendes edit für die station möglich!!! Bei den Tutorials berücksichtigen!!!!
// wie soll ab jetzt eine station editiert werden können?
// Betriebsart, etc. in die Station properties mit rein
// alter und neuer Import kann nicht "vermischt" werden... Entweder durchweg alte  Tenplates oder die neuen -> Serial ist ein großes Problem!  ----  oder ist das jetzt schon gefixt und klappt? durch automatisches Serial = ID???
// DAtei AllInOne-Import muss noch geändert werden!!!!!! Bitte die ID entsprechend des neuen Imports durch den Hash erzeugen!!!!! Sonst wird die weitere Generierung von Templates nach einem allinone import zu unnötigen Dopplungen bei den Stationen und Deliveries etc führen!!!!!
							//System.err.println(i + " -> " + lID + " -> " + dID);
							if (xlsD.getChargenLinkCol() >= 0) {
								if (!chargenLinkTypeChecked) {
									preferedChargenLinkType = detectPreferredChargenLinkType(
										sheet, xlsD.getChargenLinkCol(), iRow, numRows, olddelsRow, lotNumberToLotId, filename
									);
									
									chargenLinkTypeChecked = true;
								}
								String key = getCellString(row.getCell(xlsD.getChargenLinkCol()));
								if (key != null) {
									// if (chargenLinkType != ChargenLinkType.lineNo && lotNumberToLotId.containsKey(key)) {
									if (
										lotNumberToLotId.containsKey(key) && 
										(preferedChargenLinkType != ChargenLinkType.lineNo || !olddelsRow.containsKey(key))
									) {
										if (lotDoublettes.contains(key)) {
											exceptions.add(new Exception("[" + (iRow+1) + "] Unclear to which lot the ingredients should be connected - same Lot number (" + key + ") is used for different products.\nTry to make use of the Line Number as connection key."));											
										}

										HashSet<Delivery> odhs = olddelsLot.get(lotNumberToLotId.get(key));
										if (odhs != null) {
											for (Delivery od : odhs) {
												if (od != null) {
													if (backtracing) d.addTargetLotId(od.getLot().getId()+"");
													else d.getLot().getInDeliveries().add(od.getId());											
												}
											}
										}
									}
									//else if (chargenLinkType != ChargenLinkType.lotNo && olddelsRow.containsKey(key)) {
									else if (olddelsRow.containsKey(key)) {
										// To check:
										// the handling of rowNo references amd lotNo reference is to different
										// for lotNo references all deliveries of the corresponding lot are used 
										// but for a rowNo only the corresponding delivery
										Delivery od = olddelsRow.get(key);
										if (od != null) {
											
											if (backtracing) d.addTargetLotId(od.getLot().getId()+"");
											else d.getLot().getInDeliveries().add(od.getId());											
										}
									}
									else {
										exceptions.add(new Exception("[" + (iRow+1) + "] Value " + key + " in column " + xlsD.getChargenLinkCol() + " does not match a lot number or a line number."));
									}
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
						else if (
								isAllInOneTemplate && iRow == 0 ||
								backtracing && cellString__ != null && (cellString__.trim().startsWith(XlsStruct.getBACK_NEW_DATA_START(lang)) || cellString__.trim().startsWith(XlsStruct.getPROD_NEW_DATA_START(lang))) ||
								!backtracing && cellString__ != null && (cellString__.trim().startsWith(XlsStruct.getFWD_NEW_DATA_START(lang)) || cellString__.trim().startsWith(XlsStruct.getPROD_NEW_DATA_START(lang))) ||
								isProduction && iRow==3) {
							xlsS = new XlsStation();
							xlsSRecipient = new XlsStation();
							xlsP = new XlsProduct();
							xlsL = new XlsLot();
							xlsD = new XlsDelivery();
							xlsO = new XlsOther();
							// Header for Entities
							row = sheet.getRow(iRow);
							for (int iCell=0;iCell<row.getLastCellNum();iCell++) {
								String str = getLabelCellString(row.getCell(iCell));
								if (str != null) {
									str = str.trim();
									boolean isRecipient = str.equalsIgnoreCase(XlsStation.BLOCK_RECIPIENT(lang));
									if ((isRecipient || str.equalsIgnoreCase(XlsStation.BLOCK_SUPPLIER(lang)))) {
										if (isAllInOneTemplate && isRecipient) xlsSRecipient.setStartCol(iCell);
										else xlsS.setStartCol(iCell);
										iCell++;										
										while(true) {
											String string = getCellString(row.getCell(iCell));
											System.err.println(iCell + "-" + string);
											if (string != null && !string.trim().isEmpty()) break;
											if (iCell >= row.getLastCellNum()) break;
											iCell++;
										} 
										iCell--;
										if (isAllInOneTemplate && isRecipient) xlsSRecipient.setEndCol(iCell);
										else xlsS.setEndCol(iCell);
										
									}
									else if ((str.equalsIgnoreCase(XlsProduct.BLOCK_INGREDIENT(lang)) || str.equalsIgnoreCase(XlsProduct.BLOCK_PRODUCT(lang)))) {
										xlsP.setStartCol(iCell); iCell++;
										while(true) {
											String string = getCellString(row.getCell(iCell));
											System.err.println(iCell + "-" + string);
											if (string != null && !string.trim().isEmpty()) break;
											if (iCell >= row.getLastCellNum()) break;
											iCell++;
										} 
										iCell--; xlsP.setEndCol(iCell);
									}
									else if (str.equalsIgnoreCase(XlsLot.BLOCK(lang))) {
										xlsL.setStartCol(iCell); iCell++;
										while(true) {
											String string = getCellString(row.getCell(iCell));
											System.err.println(iCell + "-" + string);
											if (string != null && !string.trim().isEmpty()) break;
											if (iCell >= row.getLastCellNum()) break;
											iCell++;
										} 
										iCell--; xlsL.setEndCol(iCell);
									}
									else if (str.equalsIgnoreCase(XlsDelivery.BLOCK(lang))) {
										xlsD.setStartCol(iCell); iCell++;
										while(true) {
											String string = getCellString(row.getCell(iCell));
											System.err.println(iCell + "-" + string);
											if (string != null && !string.trim().isEmpty()) break;
											if (iCell >= row.getLastCellNum()) break;
											iCell++;
										} 
										iCell--; xlsD.setEndCol(iCell);
									}
									else if (str.equalsIgnoreCase(XlsOther.BLOCK(lang))) {
										xlsO.setStartCol(iCell); iCell++;
										while(true) {
											String string = getCellString(row.getCell(iCell));
											System.err.println(iCell + "-" + string);
											if (string != null && !string.trim().isEmpty()) break;
											if (iCell >= row.getLastCellNum()) break;
											iCell++;
										} 
										iCell--; xlsO.setEndCol(iCell);
									}
									else if (str.equalsIgnoreCase(XlsDelivery.COMMENT(lang))) {
										xlsD.setCommentCol(iCell);
									}
								}
							}
							// Fields for Entities
							iRow++;
							row = sheet.getRow(iRow);
							for (int ii=xlsS.getStartCol();ii<=xlsS.getEndCol();ii++) {
								String str = getLabelCellString(row.getCell(ii));
								xlsS.addField(str, ii, lang);
							}
							if (isAllInOneTemplate) {
								for (int ii=xlsSRecipient.getStartCol();ii<=xlsSRecipient.getEndCol();ii++) {
									String str = getCellString(row.getCell(ii));
									xlsSRecipient.addField(str, ii, lang);
								}								
							}
							for (int ii=xlsP.getStartCol();ii<=xlsP.getEndCol();ii++) {
								String str = getLabelCellString(row.getCell(ii));
								xlsP.addField(str, ii, lang);
							}
							for (int ii=xlsL.getStartCol();ii<=xlsL.getEndCol();ii++) {
								String str = getLabelCellString(row.getCell(ii));
								xlsL.addField(str, ii, lang);
							}
							for (int ii=xlsD.getStartCol();ii<=xlsD.getEndCol();ii++) {
								String str = getLabelCellString(row.getCell(ii));
								xlsD.addField(str, ii, lang);
							}
							if (xlsO.getStartCol() >= 0 && xlsO.getEndCol() >= 0) {
								for (int ii=xlsO.getStartCol();ii<=xlsO.getEndCol();ii++) {
									String str = getLabelCellString(row.getCell(ii));
									xlsO.addField(str, ii, lang);
								}								
							}
							
							if (isProduction && iRow==4) {
								xlsD.setChargenLinkCol(-1);
								doPreCollect = true;
							}
							else if (cellString__.trim().startsWith(XlsStruct.getPROD_NEW_DATA_START(lang))) { // "Zeilennummer"
								xlsD.setChargenLinkCol(0);
								doCollect = true;
							}
							else { // Simple Start Template or AiO
								xlsD.setChargenLinkCol(-1);
								doCollect = true;
							}
							if (!isAllInOneTemplate) iRow++;
							continue;
						}
					}
				}
				// if (taskMonitor.isCanceled()) throw new UserCancelException();
				taskMonitor.setProgress((int)((iRow + 1) / (double)numRows * 100));
			}
		}
		else {
			exceptions.add(new Exception("Wrong template format!"));
			return exceptions;
		}
			
		try {
			if (idToDeliveryMap.size() > 0) {
				for (Station s: idToStationMap.values()) {
					s.addFlexibleField(XlsStruct.getOUT_SOURCE_KEY("en"), filename + ": " + s.getFlexible(XlsStruct.getOUT_SOURCE_KEY(lang)));
				}
				for (Delivery d : olddelsRow.values()) {
					//System.err.println(d.getId() + "\t" + d.getLot().getNumber() + "\t" + d.getLot().getProduct().getName() + "\t" + d.getLot().getProduct().getId());
					d.insertIntoDb(mydbi);
					//if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
					if (d.getExceptions().size() > 0) exceptions.addAll(d.getExceptions());
					checkUserCancel(taskMonitor);
				}
				for (Delivery d : idToDeliveryMap.values()) {
					d.insertIntoDb(mydbi);
					//if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
					if (d.getExceptions().size() > 0) exceptions.addAll(d.getExceptions());
					checkUserCancel(taskMonitor);
				}	
				
				MetaInfo mi = new MetaInfo();
				mi.setFilename(filename);		
				Integer miDbId = null;
				try {
					miDbId = mi.getID(mydbi);
				}
				catch (Exception e) {
					exceptions.add(e);
				}
				if (miDbId == null) exceptions.add(new Exception("File already imported"));				
			}
			else {
				exceptions.add(new Exception("No new delivery data found. Nothing imported!"));	
			}
		}

		catch (UserCancelException e) {
			throw e;
		}
		catch (Exception e) {
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

	private boolean isBlockEnd(Row row, int numCols2Check, String nextBlockIdentifier) throws Exception {
		if (row == null) return true;
		for (int j=0;j<numCols2Check;j++) {

			String cellString = getCellString(row.getCell(j)); 
			if (cellString == null) continue;
			if (j == 0 && nextBlockIdentifier != null && cellString.equals(nextBlockIdentifier)) return true;
			if (!cellString.isEmpty()) return false;
		}
		return true;
	}
	
	private int getNextBlockRowIndex(Sheet transactionSheet, int rowIndex, String nextBlockIdentifier) throws Exception {
		int numRows = transactionSheet.getLastRowNum() + 1;
		for (;rowIndex < numRows;rowIndex++) {
			Row row = transactionSheet.getRow(rowIndex);
			String cellString = getCellString(row.getCell(0));
			if (cellString == null) continue;
			if (cellString.equals(nextBlockIdentifier)) return rowIndex;
		}
		// ToDo: Verify Really?
		return -100;
	}
	
	private static MetaInfo getMetaInfo(List<Exception> exceptions, Row row, Row rowBefore) {
		if (row == null) return null;
		boolean hasPartedDate = true;
		if (rowBefore != null) {
			Cell cell = rowBefore.getCell(1);
			if (cell != null && cell.getCellType() != CellType.BLANK) {
				cell.setCellType(CellType.STRING);
				if (getStr(cell.getStringCellValue()).equals("Reporting Date")) {
					cell = rowBefore.getCell(3);
					if (cell == null || cell.getCellType() == CellType.BLANK || getStr(cell.getStringCellValue()).trim().isEmpty()) {
						hasPartedDate = false;
					}
				}
			}
		}
		MetaInfo result = new MetaInfo();
		Cell cell = row.getCell(0); if (cell != null && cell.getCellType() != CellType.BLANK) {cell.setCellType(CellType.STRING); result.setReporter(getStr(cell.getStringCellValue()));}
		if (hasPartedDate) {
			cell = row.getCell(1);
			if (cell != null && cell.getCellType() != CellType.BLANK) {				
				cell.setCellType(CellType.STRING); result.setDateDay(getInt(cell.getStringCellValue()));
			}
			cell = row.getCell(2);
			if (cell != null && cell.getCellType() != CellType.BLANK) {
				cell.setCellType(CellType.STRING); result.setDateMonth(getInt(cell.getStringCellValue()));
			}
			cell = row.getCell(3);
			if (cell != null && cell.getCellType() != CellType.BLANK) {
				cell.setCellType(CellType.STRING); result.setDateYear(getInt(cell.getStringCellValue()));
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
			cell = row.getCell(4); if (cell != null && cell.getCellType() != CellType.BLANK) {cell.setCellType(CellType.STRING); result.setRemarks(getStr(cell.getStringCellValue()));}			
		}
		else {
			cell = row.getCell(1);
			if (cell != null && cell.getCellType() != CellType.BLANK) {
				cell.setCellType(CellType.STRING);
				result.setDate(getStr(cell.getStringCellValue()));
			}
			cell = row.getCell(2); if (cell != null && cell.getCellType() != CellType.BLANK) {cell.setCellType(CellType.STRING); result.setRemarks(getStr(cell.getStringCellValue()));}			
		}
		return result;
	}
	
	private Station getStation(List<Exception> exceptions, Sheet businessSheet, String lookup, Row srcrow) throws Exception {
		Station station = null;
		int numRows = businessSheet.getLastRowNum() + 1;
		for (int i=0;i<numRows;i++) {
			Row row = businessSheet.getRow(i);
			if (row != null) {
				String cellString = getCellString(row.getCell(0));
				if (cellString != null && cellString.equals(lookup)) {
					station = getStation(businessSheet.getRow(0), row);
					break;
				}
			}
		}
		if (station == null) {
			StackTraceElement[] tmpTrace = Thread.currentThread().getStackTrace();
			System.err.println(tmpTrace.toString());
			exceptions.add(new Exception("Station '" + lookup + "' is not correctly defined in Row " + (srcrow.getRowNum() + 1)));
		}
		return station;
	}
	
	private Station getStation(Row titleRow, Row row) throws Exception {
		if (row == null) return null;
		Station station = new Station();
		{
			String id = getCellString(row.getCell(0));
			if (id != null) station.setId(id);
			else return null;
		}
		
		applyCellString(row.getCell(1), x -> station.setName(x));
		applyCellString(row.getCell(2), x -> station.setStreet(x));
		applyCellString(row.getCell(3), x -> station.setNumber(x));
		applyCellString(row.getCell(4), x -> station.setZip(x));
		applyCellString(row.getCell(5), x -> station.setCity(x));
		applyCellString(row.getCell(6), x -> station.setDistrict(x));
		applyCellString(row.getCell(7), x -> station.setState(x));
		applyCellString(row.getCell(8), x -> station.setCountry(x));
		applyCellString(row.getCell(9), x -> station.setTypeOfBusiness(x));

		// Further flexible cells
		addFlexibleFields(titleRow, row, 10, 19, station);
		return station;
	}
	
	private D2D getD2D(List<Exception> exceptions, HashMap<String, Delivery> deliveries, Row titleRow, Row row, int rowNum) throws Exception {
		if (row == null) return null;
		D2D d2d = new D2D();
		{
			String deliveryId = getCellString(row.getCell(0));
			if (deliveryId == null) return null;
			Delivery delivery = deliveries.get(deliveryId);
			if (delivery == null) exceptions.add(new Exception("Delivery ID in sheet Deliveries2Deliveries not defined in deliveries sheet: '" + deliveryId + "'; -> Row " + (rowNum+1)));
			d2d.setIngredient(delivery);
		}
		{
			String deliveryId = getCellString(row.getCell(1));
			if (deliveryId == null) return null;
			Delivery delivery = deliveries.get(deliveryId);
			if (delivery == null) exceptions.add(new Exception("Delivery ID in sheet Deliveries2Deliveries not defined in deliveries sheet: '" + deliveryId + "'; -> Row " + (rowNum+1)));
			d2d.setTargetDelivery(delivery);
		}
		if (d2d.getIngredient() != null && d2d.getTargetDelivery() != null) {
			if (!d2d.getIngredient().getReceiver().getId().equals(d2d.getTargetDelivery().getLot().getProduct().getStation().getId())) {
				exceptions.add(new Exception("Recipient does not match Supplier; in sheet Deliveries2Deliveries: '" + d2d.getIngredient().getId() + "' -> '" + d2d.getTargetDelivery().getId() + "'; -> Row " + (rowNum+1)));
			}
		}
		// Please continue here 
		// Further flexible cells

		addFlexibleFields(titleRow, row, 2, 9, d2d);
		return d2d;
	}
	
	private void addFlexibleFields(Row titleRow, Row row, int fromIndex, int toIndex, IFlexibleFieldContainer flexibleFieldContainer) throws Exception {
		for (int i = fromIndex;i <= toIndex; i++) {
			String titleCellString = getCellString(titleRow.getCell(i));
			if (titleCellString != null) {
				String rowCellString = getCellString(row.getCell(i));
				if (rowCellString != null) flexibleFieldContainer.addFlexibleField(titleCellString, rowCellString);
			}
		}
	}
	
	class DateParts {
		public Integer day;
		public Integer month;
		public Integer year;
		
		public DateParts(Integer day, Integer month, Integer year) {
			this.day = day;
			this.month = month;
			this.year = year;
		}
	}
	
//	private DateParts getDatePartsFromCells(Row row, int startIndex) {
//		return getDatePartsFromCells(row.getCell(startIndex), row.getCell(startIndex + 1), row.getCell(startIndex + 2));
//	}
	
	private void addWarning(String key, String source) {
		Set<String> sources = warns.get(key);
		if (sources == null) {
			sources = new LinkedHashSet<>();
			warns.put(key, sources);
		}
		sources.add(source);
	}
	
	private boolean isLeapYear(int year) {
		return ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0);
	}
	
	private boolean isValidDay(Integer day, Integer month, Integer year) {
		if (day == null || day < 1 || day > 31) return false;
		if (month == null) return true;
		
		List<Integer> monthsWith31Days = Arrays.asList(1, 3, 5, 7, 8, 10, 12);
		
		if (day == 31 && monthsWith31Days.contains(day)) return false;
		if (day == 30 && month == 2) return false;
		if (year == null) return true;
		
		if (day == 29 && !isLeapYear(year)) return false;
		
		return false;
	}
//	
	private DateParts getDatePartsFromCells(Cell dayCell, Cell monthCell, Cell yearCell) {
		if (true) return new DateParts(getCellInt(dayCell), getCellInt(monthCell), getCellInt(yearCell));
		Integer day = null;
		Integer month = null;
		Integer year = null; 
		boolean yearError = false;
		boolean monthError = false;
		boolean dayError = false;
		
		try { 
			year = getCellInt(yearCell); 
		}
		catch(NumberFormatException ex) {
			yearError = true;
			addWarning("Invalid year.", getWbRelativeCellAddressString(yearCell));
		}
		try { 
			month = getCellInt(monthCell); 
		}
		catch(NumberFormatException ex) { 
			monthError = true;
			addWarning("Invalid month (number).", getWbRelativeCellAddressString(monthCell));
		}
		try { day = getCellInt(dayCell); }
		catch(NumberFormatException ex) { 
			addWarning("Invalid day.", getWbRelativeCellAddressString(dayCell));
		}
		// checkNumberRanges
		if (year != null && (year < 1 || year > 9999)) {
			year = null;
			yearError = true;
			addWarning("Invalid year.", getWbRelativeCellAddressString(yearCell));
		}
		if (month != null && (month < 1 || month > 12)) {
			month = null;
			monthError = true;
			addWarning("Invalid month.", getWbRelativeCellAddressString(monthCell));
		}
		if (day != null && !isValidDay(day, month, year)) {
			addWarning("Invalid day.", getWbRelativeCellAddressString(dayCell));
		}
		
		if (
				!dayError && !monthError && !yearError && // no Error
				(day != null || month != null || year != null) // some part is given
		) {
			Cell missingCell = year == null ? yearCell : (month == null ? monthCell : dayCell);
			addWarning("Incomplete date.", getWbRelativeCellAddressString(missingCell));
		}
		return new DateParts(day, month, year);
	}
	
	// private applyDatePartsFromCells(Cell dayCell, Cell monthCell, Cell yearCell, ApplyIntFunction applyDayFun, ApplyIntFunction applyMonthFun, ApplyInt)
	private void applyDepartureFromCells(Delivery delivery, Cell dayCell, Cell monthCell, Cell yearCell) {
		DateParts dateParts = getDatePartsFromCells(dayCell, monthCell, yearCell);
		delivery.setDepartureDay(dateParts.day);
		delivery.setDepartureMonth(dateParts.month);
		delivery.setDepartureYear(dateParts.year);
	}
	
	private void applyArrivalFromCells(Delivery delivery, Cell dayCell, Cell monthCell, Cell yearCell) {
		DateParts dateParts = getDatePartsFromCells(dayCell, monthCell, yearCell);
		delivery.setArrivalDay(dateParts.day);
		delivery.setArrivalMonth(dateParts.month);
		delivery.setArrivalYear(dateParts.year);
	}
	
	private Delivery getForwardDelivery(List<Exception> exceptions, Sheet stationSheet, HashMap<String, Lot> lots, Row titleRow, Row row, boolean isNewFormat_151105) throws Exception {
		if (row == null) return null;
		Lot lot = null;
		{
			String lotString = getCellString(row.getCell(0));
			if (lotString != null) lot = lots.get(lotString);
		}
		if (lot == null) return null;
		Delivery result = new Delivery();
		result.setLot(lot);
		
		applyDepartureFromCells(result, row.getCell(1), row.getCell(2), row.getCell(3));
		
		int startCol = 4;
		if (isNewFormat_151105) {
			applyArrivalFromCells(result, row.getCell(4), row.getCell(5), row.getCell(6));
			startCol = 7;
		}
		
		applyCellDouble(row.getCell(startCol), (x) -> result.setUnitNumber(x));
		
		applyCellString(row.getCell(startCol+1), x -> result.setUnitUnit(x));
		
		{
			String receiverId = getCellString(row.getCell(startCol+2));
			if (receiverId != null) {
				Station receiver = getStation(exceptions, stationSheet, receiverId, row);
				if (receiver == null) exceptions.add(new Exception("Recipient station '" + receiverId + "' not correctly defined / not known in Forward Tracing sheet"));
				result.setReceiver(receiver);
			}
			else exceptions.add(new Exception("No Recipient Station defined in Forward Tracing sheet"));
		}
		
		result.setId(getNewSerial(lot, result));
		result.setNewlyGeneratedID(true);
		
		// Further flexible cells

		addFlexibleFields(titleRow, row, startCol + 4, startCol + 21 - 1, result);
		return result;
	}
	
//	private String getStr(Cell cell) {
//		if (cell == null || cell.getCellType() == CellType.BLANK) return null;
//		cell.setCellType(CellType.STRING);
//		String s = getStr(cell.getStringCellValue());
//		return s;
//	}
	
//	private String getStr(Cell cell) {
//		if (cell == null || cell.getCellType() == CellType.BLANK) return null;
//		cell.setCellType(CellType.STRING);
//		String s = getStr(cell.getStringCellValue());
//		return s;
//	}

	private Delivery getMultiOutDelivery(List<Exception> exceptions, HashMap<String, Station> stations, Row titleRow, Row row, HashMap<String,String> definedLots,int rowNum, String filename, boolean ignoreMissingLotnumbers) throws Exception {
		if (row == null) return null;
		Delivery result = new Delivery();

		// ToDO: Verify why is id not required?
		applyCellString(row.getCell(0), x -> result.setId(x));
		
		Product p = new Product();
		{
			String stationId = getCellString(row.getCell(1));
			// ToDo: Verify: Why is no exception thrown?
			if (stationId == null) return null;
			Station station = stations.get(stationId);
			if (station == null) exceptions.add(new Exception("Station ID in Deliveries not defined in stations sheet: '" + stationId + "'; -> Row " + (rowNum+1)));
			p.setStation(station);
		}
		
		applyCellString( row.getCell(2), x -> p.setName(x));
		Lot l = new Lot();
		l.setProduct(p);
		
		{
			String lotNo = getCellString(row.getCell(3));
			if (lotNo != null) {l.setNumber(lotNo);}
			else if (!ignoreMissingLotnumbers) {exceptions.add(new Exception("Please, do always provide a lot number as this is most helpful! -> Row " + (rowNum+1) + " in '" + filename + "'\n"));}
		}
		
		applyCellDouble(row.getCell(4), (x) -> l.setUnitNumber(x));
		applyCellString(row.getCell(5), (x) -> l.setUnitUnit(x));
		
		String lotId = (p.getStation() == null) ? "_" + p.getName() + "_" + l.getNumber() : p.getStation().getId() + "_" + p.getName() + "_" + l.getNumber();
		String lotInfo = l.getUnitNumber() + "_" + l.getUnitUnit();
		if (definedLots.containsKey(lotId)) {
			if (!definedLots.get(lotId).equals(lotInfo)) exceptions.add(new Exception("Lot has different quantities -> Lot number: '" + l.getNumber() + "'; -> Row " + (rowNum+1)));
		}
		else definedLots.put(lotId, lotInfo);

		result.setLot(l);
		
		applyDepartureFromCells(result, row.getCell(6), row.getCell(7), row.getCell(8));
		applyArrivalFromCells(result, row.getCell(9), row.getCell(10), row.getCell(11));
		
		applyCellDouble(row.getCell(12), x -> result.setUnitNumber(x));
		applyCellString(row.getCell(13), (x) -> result.setUnitUnit(x));
		
		{
			String receiverId = getCellString(row.getCell(14));
			if (receiverId != null) {
				Station receiver = stations.get(receiverId);
				if (receiver == null) {
					exceptions.add(new Exception("Recipient ID in sheet Deliveries not defined in stations sheet: '" + receiverId + "'; -> Row " + (rowNum+1)));
				}
				result.setReceiver(receiver);
			}
			else {
				// ToDo: Verify: Why is the missing receiver not thrown?
				if (result.getId() == null) return null;
				else exceptions.add(new Exception("Recipient ID in sheet Deliveries not defined; -> Row " + (rowNum+1)));
			}
		}
		
		// Further flexible cells
		UnaryOperator<String> normalizeStringFun = (String x) -> x.toLowerCase();
		
		Set<String> knownLotFields = Stream.of(new String[] {
				"Production date",
				"Best before date",
				"Treatment of product during production",
				"Sampling"
		}).map(normalizeStringFun).collect(Collectors.toSet());
				
		for (int i=15;i<25;i++) {
			String fieldKey = getCellString(titleRow.getCell(i));
			if (fieldKey != null) {
				String fieldValue = getCellString(row.getCell(i));
				if (fieldValue != null) {
					if (knownLotFields.contains(normalizeStringFun.apply(fieldKey))) l.addFlexibleField(fieldKey, fieldValue);
					else result.addFlexibleField(fieldKey, fieldValue);
				}
			}
		}
		
		return result;
	}
	
	private void throwCellErrorException(Cell cell) throws Exception {
		throw new Exception("Cell " + getWbRelativeCellAddressString(cell) + " contains an error.");
	}
	
	private boolean isCellEmpty(Cell cell) throws Exception {
		if (cell == null) return true;
		CellType cellType = cell.getCellType();
		
		if (cellType == CellType.FORMULA) cellType = cell.getCachedFormulaResultType();
		if (cellType == CellType.BLANK) return true;
		if (cellType == CellType.ERROR) throwCellErrorException(cell);
		if (cellType != CellType.STRING) return false;
		
		return getStringCellString(cell, true, false) == null;
	}
	
	private Delivery getDelivery(List<Exception> exceptions, Sheet businessSheet, Station sif, Row row, boolean outbound, Row titleRow, String filename, boolean isForTracing, HashMap<String, Lot> outLots, HashMap<String, Delivery> existingDeliveries, boolean ignoreMissingLotnumbers, boolean isNewFormat_151105) throws Exception {
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
		
		Delivery delivery = new Delivery();
		String lotDelNumber = getCellString(cell);
		if (lotDelNumber != null) {
			if (isNewFormat_151105) {
				delivery.setId(lotDelNumber);
			}
			else {
				if (!isForTracing && outbound) {delivery.setId(lotDelNumber);}
				if (isForTracing && !outbound) {delivery.setId(lotDelNumber);}
				if (!isForTracing && !outbound) {delivery.addTargetLotId(lotDelNumber);}
			}
		}
		Lot l;
		if (isForTracing && outbound) {
			String cellString = getCellString(row.getCell(0));
			// ToDo: check (BfR_Format_Fortrace.xlsx) (first 2 columns are empty)
			// Why is this done here (in the code above the appropriate lot no is already checked)
			if (cellString != null) lotDelNumber = cellString;
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
					lotNumber = getCellString(row.getCell(0));
					if (lotNumber == null && !ignoreMissingLotnumbers) {exceptions.add(new Exception("Please, do always provide a lot number as this is most helpful! -> Row " + (row.getRowNum()+1) + " in '" + filename + "'\n"));}					
					l.setNumber(lotNumber);
					if (outLots.containsKey(lotNumber)) {
						l = outLots.get(lotNumber);
					}
				}				
				else {
					lotNumber = getCellString(row.getCell(0));
					if (lotNumber != null) delivery.addTargetLotId(lotNumber);

					applyCellString(row.getCell(1), (x) -> p.setName(x));
					lotNumber = getCellString(row.getCell(2));
					if (lotNumber == null && !ignoreMissingLotnumbers) {exceptions.add(new Exception("Please, do always provide a lot number as this is most helpful! -> Row " + (row.getRowNum()+1) + " in '" + filename + "'\n"));}
					l.setNumber(lotNumber);
					if (lotNumber == null && p.getName() == null) {
						exceptions.add(new Exception("Lot number undefined in Row number " + (classRowIndex + 1)));
					}
				}
			}
			else {
				applyCellString(row.getCell(0), (x) -> p.setName(x));
				lotNumber = getCellString(row.getCell(1));
				if (lotNumber == null && !ignoreMissingLotnumbers) {exceptions.add(new Exception("Please, do always provide a lot number as this is most helpful! -> Row " + (row.getRowNum()+1) + " in '" + filename + "'\n"));}
				l.setNumber(lotNumber);
				if (lotNumber == null && p.getName() == null) {
					exceptions.add(new Exception("Lot number and product name undefined in Row number " + (classRowIndex + 1)));
				}
			}
		}
		
		delivery.setLot(l);
		if (!outbound) delivery.setReceiver(sif);
		int startCol = isNewFormat_151105 ? 3 : 2;
		
		applyDepartureFromCells(delivery, row.getCell(startCol), row.getCell(startCol + 1), row.getCell(startCol + 2));
		applyArrivalFromCells(delivery, row.getCell(startCol+3), row.getCell(startCol+4), row.getCell(startCol+5));
		applyCellDouble(row.getCell(startCol+6), x -> delivery.setUnitNumber(x));
		applyCellString(row.getCell(startCol+7), (x) -> delivery.setUnitUnit(x));
					
		// ToDo: Please verify return of null, what happens here
		{
			String stationId = getCellString(row.getCell(startCol+8));
			if (stationId == null) return null;
			Station station = getStation(exceptions, businessSheet, stationId, row);
			// ToDo: Please verify return of null, what happens here
			if (outbound) delivery.setReceiver(station);
			else {
				l.getProduct().setStation(station); 
				l.setNumber(l.getNumber());
			}
		}

		if (!isForTracing && !outbound || isForTracing && outbound) {
			if (!isNewFormat_151105 || delivery.getId() == null) {
				delivery.setId(getNewSerial(l, delivery));
				delivery.setNewlyGeneratedID(true);
			}
			if (existingDeliveries != null && existingDeliveries.containsKey(delivery.getId())) {
				delivery.getTargetLotIds().addAll(existingDeliveries.get(delivery.getId()).getTargetLotIds());
			}
		}
		
		// Further flexible cells
		addFlexibleFields(titleRow, row, 13, 19, delivery);
		
		return delivery;
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
				try {
					result = (int) Double.parseDouble(val);
				}
				catch (Exception e2) {}
			}
		}
		return result;
	}
	
//	private Double getDbl(String val) {
//		Double result = null;
//		if (!val.trim().isEmpty()) result = Double.parseDouble(val.trim());
//		return result;
//	}
	
//	private Double getDbl(String val) {
//		Double result = null;
//		if (!val.trim().isEmpty()) result = Double.parseDouble(val.trim());
//		return result;
//	}

	private static String getStr(String val) {
		if (val == null) return null;
		if (val.trim().isEmpty()) return null;
		return val.trim();
	}
	
	private boolean fillLot(List<Exception> exceptions, Row row, Station sif, HashMap<String, Lot> outLots, Row titleRow, HashMap<String, Delivery> outDeliveries, int rowIndex, boolean isNewFormat_151105) throws Exception {
		Lot lot = null;
	
		{
			String lotNumber = getCellString(row.getCell(0));
			// ToDo: Why is the lotNumber not checked for null Verify Code Change
			lot = outLots.get(lotNumber);
			if (lot == null) {
				if (outDeliveries != null) {
					lot = new Lot(); 
					lot.setNumber(lotNumber); 
					outLots.put(lot.getNumber(), lot);
				}
				else return false;
			}
		}

		{
			Double unitNumber = getCellDouble(row.getCell(1));
			if (unitNumber != null) {
				if (lot.getUnitNumber() == null) lot.setUnitNumber(unitNumber);
				else if (lot.getUnitNumber().doubleValue() != unitNumber) {
					exceptions.add(new Exception("Lot information defines same lot number with different quantities -> Row " + rowIndex));
				}
			}
		}
		
		{
			String unitUnit = getCellString(row.getCell(2));
			if (unitUnit != null) {
				if (lot.getUnitUnit() == null) lot.setUnitUnit(unitUnit);
				else if (!lot.getUnitUnit().equals(unitUnit)) {
					exceptions.add(new Exception("Lot information defines same lot number with different units -> Row " + rowIndex));
				}
			}
		}
		
		if (isNewFormat_151105 || outDeliveries != null) {

			String productName = getCellString(row.getCell(3));
			if (productName != null) {
				if (lot.getProduct() == null) {
					Product p = new Product(); p.setName(productName); lot.setProduct(p); p.setStation(sif);
				}
				else if (lot.getProduct().getName() == null) {
					lot.getProduct().setName(productName);
				}
				else if (!lot.getProduct().getName().equals(productName)) {
					exceptions.add(new Exception("Lot information defines same lot number with different product names -> Row " + rowIndex));
				}
			}
			if (!isNewFormat_151105) {
				String deliveryId = getCellString(row.getCell(4));
				// ToDo: Verify: Why is missing deliveryId not thrown
				if (deliveryId != null) {
					Delivery d = outDeliveries.get(deliveryId); 
					// ToDo: Verify: Why is missing delivery not thrown
					if (d == null) return false; 
					d.addTargetLotId(lot.getNumber());
				}
			}
		}
		
		// Further flexible cells
		addFlexibleFields(titleRow, row, 12, 19, lot);
		return true;
	}
	
	private Map<String, String> getChargenLinks(Sheet sheet, int colIndex, int rowStartIndex, int rowEndIndex) throws Exception {
		Map<String, String> links = new HashMap<>();
		for (int iR = rowStartIndex; iR <= rowEndIndex; iR++) {
			Row row = sheet.getRow(iR);
			if (row != null) {
				Cell cell = row.getCell(colIndex);
				if (cell != null) {
					String text = getCellString(cell, false);
					if (text != null && !links.containsKey(text)) {
						links.put(text, cell.getAddress().toString());
					}
				}
			}
		}
		return links;
	}
	
	private String getFormatedCellAddressesString(String[] addresses) {
		final int maxCount = 5;
		String[] filteredAddresses = ArrayUtils.subarray(addresses, 0, maxCount);
		return String.join(", ", filteredAddresses) + (filteredAddresses.length < addresses.length ? ", ..." : "");
	}
	
	private ChargenLinkType detectPreferredChargenLinkType(
			Sheet sheet, int colIndex, int rowStartIndex, int rowEndIndex, 
			LinkedHashMap<String, Delivery> rowNoToReferableDeliveryMap,
			HashMap<String, Integer> lotNoToLotIdMap,
			String filepath
	) throws Exception {
		// collect chargenRefs 
		Map<String, String> chargenLinksToCellAddressMap = getChargenLinks(sheet, colIndex, rowStartIndex, rowEndIndex);
		
		Set<String> chargenLinks = chargenLinksToCellAddressMap.keySet();
		Set<String> ambiguousLinks = chargenLinks.stream()
			.filter(ref -> 
				lotNoToLotIdMap.containsKey(ref) && 
				rowNoToReferableDeliveryMap.containsKey(ref) && 
				!ref.equals(rowNoToReferableDeliveryMap.get(ref).getLot().getNumber())
			).collect(Collectors.toSet());
		long lotNoMatchCount = chargenLinks.stream().filter(ref -> lotNoToLotIdMap.containsKey(ref)).count();
		long lineNoMatchCount = chargenLinks.stream().filter(ref -> rowNoToReferableDeliveryMap.containsKey(ref)).count();
				
		if (!ambiguousLinks.isEmpty()) {
			long misMatchCount = chargenLinks.stream().filter(ref -> !lotNoToLotIdMap.containsKey(ref) && !rowNoToReferableDeliveryMap.containsKey(ref)).count();
			
			
			String[] options = {
				"Lot Number", //+ XlsLot.NUMBER(lang),
                "Line Number", 
                "Cancel"
	        };
			String fileame = "";
			
			String cellAddresses = getFormatedCellAddressesString(ambiguousLinks.stream().map(ref -> chargenLinksToCellAddressMap.get(ref)).toArray(String[]::new));
			long unambiguousLotNoMatchCount = lotNoMatchCount - ambiguousLinks.size();
			long unambiguousRowNoMatchCount = lineNoMatchCount - ambiguousLinks.size();
			
			String msg = "<html>" + 
					"Sheet '" + StringEscapeUtils.escapeHtml4(sheet.getSheetName()) + "' " +
					"in file '" + StringEscapeUtils.escapeHtml4(new File(filepath).getName()) + "'<br>" + 
					"contains " + ambiguousLinks.size() + " ambiguous lot reference(s) in cell(s) " + cellAddresses + ".<br>" + 
					unambiguousLotNoMatchCount + " of the " + (chargenLinks.size() - ambiguousLinks.size()) + " unambiguous lot references match lot numbers " + 
					"and " + unambiguousRowNoMatchCount + " match line numbers. " +
					(misMatchCount == 0 ? "" : ("<br>" + misMatchCount + " references have no match at all. ")) + 
					"<br><br>Are the ambiguous references refering to lot numbers or to line numbers?" +
					"</html>";
			
			// System.err.println(msg);
			
			int answer = EdtUtils.askQuestionInEdt(
				DBKernel.mainFrame,
				msg,
				"Choose reference type",
				JOptionPane.YES_NO_CANCEL_OPTION,
				null,
				options,
				options[lotNoMatchCount > lineNoMatchCount ? 0 : 1]
			);
			if (answer == 0) return ChargenLinkType.lotNo;
			else if (answer == 1) return ChargenLinkType.lineNo;
			throw new UserCancelException();
			
		}
		return null;
	}
	
	enum ChargenLinkType {
		lotNo, lineNo
	}
	
	private void checkUserCancel(IProgressMonitor taskMonitor) throws UserCancelException {
		if (taskMonitor.isCanceled()) throw new UserCancelException();
	}
	
	private boolean importResult = false;
	
	@Override
	/**
	 * @deprecated This method is deprecated due to refactoring
	 * Use {@link #importFile()} instead
	 */
	@Deprecated  
	public boolean doImport(String filename, JProgressBar progress, boolean showResults) {
		return false;
	}
	
	public boolean importFile(final String filename, Window owner, IProgressMonitor taskMonitor) throws UserCancelException {
		debug("TracingImporter.importFile entered ... (" + filename + ")");
		debug("Before loading stream freespace: " + MemoryUtils.getFormatedPresumableFreeMemory());
		importResult = false;
		
		InputStream is = null;
	
		taskMonitor.setProgress(0);
		try {
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

			debug("Loading wb ... (freespace: " + MemoryUtils.getFormatedPresumableFreeMemory() + ")");
			XSSFWorkbook wb = new XSSFWorkbook(is);
			debug("Loading wb done (freespace: " + MemoryUtils.getFormatedPresumableFreeMemory() + ").");
			MemoryUtils.checkMimimumRemainingMemory();
			
			Station.reset(); Lot.reset(); Delivery.reset();
			warns = new HashMap<>();
								
			List<Exception> exceptions = importWorkbook(wb, filename, owner, taskMonitor);
			
			if (exceptions != null && exceptions.size() > 0) {
	
				importResult = false;
				if (existsDBKernel()) {
					warns.putAll(de.bund.bfr.knime.openkrise.common.DeliveryUtils.getWarnings(DBKernel.getDBConnection()));
				}
				else if (mydbi != null) {
					warns.putAll(de.bund.bfr.knime.openkrise.common.DeliveryUtils.getWarnings(mydbi.getConn()));
				}
				doWarns(filename);
				
				boolean somethingIn = false;
				logMessages += "<h1 id=\"error\">Error in file '" + filename + "'</h1><ul>";
				for (Exception e : exceptions) {
					if (e.getMessage() != null) {
						logMessages += "<li>" + e.getMessage() + "</li>";
						MyLogger.handleException(e);	
						somethingIn = true;
					}
				}
				if (!somethingIn) logMessages += "<li>some undefined problems occurred - contact the support team</li>";
				logMessages += "</ul>";
				
//				try {
//					is.close();
//				} catch (IOException e1) {}
				
			}
			else {
				importResult = true;
				if (existsDBKernel()) {
					warns.putAll(de.bund.bfr.knime.openkrise.common.DeliveryUtils.getWarnings(DBKernel.getDBConnection()));
				}
				else if (mydbi != null) {
					warns.putAll(de.bund.bfr.knime.openkrise.common.DeliveryUtils.getWarnings(mydbi.getConn()));
				}
				doWarns(filename);
			}
		} catch (OutOfMemoryError | UserCancelException e) {
			throw e;
		} catch (Exception e) {
			importResult = false;
			
			logMessages += "<h1 id=\"error\">'" + filename + "'</h1><ul><li>" + e.getMessage() + "</li></ul>";
			MyLogger.handleException(e);
			
		}
		finally {
			if (is != null) {
				try {
					is.close();
				} 
				catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		debug("Importing - Fin");
		
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
				Set<String> oldWarns = warnsBeforeImport.get(key);
				
				if (warns.get(key) != null && !warns.get(key).isEmpty()) {
					newLogs += "<ul>";
					for (String w : warns.get(key)) {
						if (oldWarns == null || !oldWarns.contains(w)) {
							if (logWarnings.indexOf("<li>" + w + "</li>") < 0) newLogs += "<li>" + w + "</li>";							
						}
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
