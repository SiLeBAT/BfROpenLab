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

import java.util.regex.Pattern;
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
// import de.bund.bfr.knime.PerformanceUtils;
import de.bund.bfr.knime.UserCancelException;
import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;
import de.bund.bfr.knime.openkrise.db.MyLogger;
import de.bund.bfr.knime.openkrise.db.imports.MyImporter;
import de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat.ReportUtils.WarningSource;
import de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat.SheetUtils.InvalidCellValueException;
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
	private Map<String, List<WarningSource>> fileRelatedWarns = new HashMap<>();
	private Map<String, Set<String>> warns = new HashMap<>();
	private Map<String, Set<String>> warnsBeforeImport = new HashMap<>();
	private DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
	
	public String getLogWarnings() {
		return logWarnings;
	}
	
	private int classRowIndex = -1;
	
	public String getLogMessages() {
		return logMessages;
	}
	
	private static InvalidCellValueException createDuplicateIdException(String attributeLabel, String value, Cell cell) {
		return createDuplicateIdException(attributeLabel, value, cell.getRow(), cell.getColumnIndex());
	}
	
	private static InvalidCellValueException createDuplicateIdException(String attributeLabel, String value, Row row, int colIndex) {
		return new InvalidCellValueException("Duplicate " + attributeLabel + " '" + value + "' (ID was already defined)",  row, colIndex);
	}
	
	private static InvalidCellValueException createInvalidValueException(String attributeLabel, String hint, Cell cell) {
		return createInvalidValueException(attributeLabel, hint, cell.getRow(), cell.getColumnIndex());
	}
	
	private static InvalidCellValueException createInvalidValueException(String attributeLabel, String hint, Row row, int colIndex) {
		String msg = "Invalid " + attributeLabel;
		if (hint != null && !hint.isEmpty()) msg += " (" + hint + ")";
		return new InvalidCellValueException(msg,  row, colIndex);
	}
	
	private void checkStationsFirst(List<Exception> exceptions, Sheet businessSheet, IProgressMonitor taskMonitor) throws Exception {
		HashSet<String> stationIDs = new HashSet<>();
		int numRows = businessSheet.getLastRowNum() + 1;
		for (int i=1;i<numRows;i++) {
			Row row = businessSheet.getRow(i);
			if (row != null) {
				Cell idCell = row.getCell(0);
				Cell nameCell = row.getCell(1);
				if (SheetUtils.isCellEmpty(idCell) && SheetUtils.isCellEmpty(nameCell)) return;
				
				String id = getRequiredCellString(row, 0, StationsSheet.COL_LABEL_STATION_ID, exceptions);
				
				if (id == null) continue;
				// if (id == null && isCellEmpty(nameCell)) return;
				// if (id == null) exceptions.add(new Exception("Station has no ID -> Row " + (i+1)));
				// if (stationIDs.contains(id)) exceptions.add(new Exception("Station ID '" + id + "' is defined more than once -> Row " + (i+1)));
				if (stationIDs.contains(id)) exceptions.add(createDuplicateIdException(StationsSheet.COL_LABEL_STATION_ID, id, idCell));

				stationIDs.add(id);
			}
			taskMonitor.setProgress(i * 100 / numRows);
			if (taskMonitor.isCanceled()) throw new UserCancelException();
		}
		taskMonitor.setProgress(100);
	}
	
	private HashMap<String, Station> collectStations(List<Exception> exceptions, Sheet stationSheet, IProgressMonitor taskMonitor) throws UserCancelException {
		debugEnter();
		HashMap<String, Station> stations = new HashMap<>();
		HashMap<String, Integer> id2RowNum = new HashMap<>();
		final int numRows = stationSheet.getLastRowNum() + 1;
		Row titleRow = stationSheet.getRow(0);
		
		for (int rowIndex=1; rowIndex < numRows; rowIndex++) {
			final Row row = stationSheet.getRow(rowIndex);
			if (row == null || rowEmpty(row)) break;
			final int rowNum = rowIndex + 1;
			final int nE = exceptions.size();
			
			Station station = new Station();
			
			station.setId(getRequiredCellString(row, StationsSheet.COL_STATION_ID, StationsSheet.COL_LABEL_STATION_ID, exceptions));
			if (station.getId() != null) {
				if (stations.containsKey(station.getId())) {
					// exceptions.add(new Exception("Station defined twice -> Row " + rowNum + "; Station Id: '" + station.getId() + "'"));
					// exceptions.add(new InvalidCellValueException("Duplicate Station ID '" + station.getId() + "' (ID was already defined in row " + id2RowNum.get(station.getId()) + ")", row, StationsSheet.COL_STATION_ID));
					exceptions.add(createDuplicateIdException(StationsSheet.COL_LABEL_STATION_ID, station.getId(), row, StationsSheet.COL_STATION_ID));
					stations.put(station.getId(), null);
				}
				else {
					id2RowNum.put(station.getId(), rowNum);
					// boolean isStationValid = nE == exceptions.size();
					stations.put(station.getId(), station);
				}
			}

			
			station.setName(getCellString(row.getCell(StationsSheet.COL_NAME), exceptions));
			station.setStreet(getCellString(row.getCell(StationsSheet.COL_STREET), exceptions));
			station.setNumber(getCellString(row.getCell(StationsSheet.COL_STREET_NO), exceptions));
			station.setZip(getCellString(row.getCell(StationsSheet.COL_ZIP), exceptions));
			station.setCity(getCellString(row.getCell(StationsSheet.COL_CITY), exceptions));
			station.setDistrict(getCellString(row.getCell(StationsSheet.COL_DISTRICT), exceptions));
			station.setState(getCellString(row.getCell(StationsSheet.COL_STATE), exceptions));
			station.setCountry(getCellString(row.getCell(StationsSheet.COL_COUNTRY), exceptions));
			station.setTypeOfBusiness(getCellString(row.getCell(StationsSheet.COL_TOB), exceptions));
			
			// Further flexible cells
			addFlexibleFields(titleRow, row, 10, 19, station, exceptions);
		
//			if (station.getId() != null) {
//				if (stations.containsKey(station.getId())) {
//					// exceptions.add(new Exception("Station defined twice -> Row " + rowNum + "; Station Id: '" + station.getId() + "'"));
//					exceptions.add(new InvalidCellValueException("Duplicate Station ID '" + station.getId() + "' (ID was already defined in row " + rowNum + ")", row, StationsSheet.COL_STATION_ID));
//					stations.put(station.getId(), null);
//				}
//				else {
//					id2RowNum.put(station.getId(), rowNum);
//					boolean isStationValid = nE == exceptions.size();
//					stations.put(station.getId(), isStationValid ? station : null);
//				}
//			}
			
			if (station.getId() != null) { 
				if (exceptions.size() > nE) stations.put(station.getId(), null);
			}
			checkUserCancel(taskMonitor);
			taskMonitor.setProgress(rowNum * 100 / numRows);
		}
		
		taskMonitor.setProgress(100);
		debugLeaving();
		return stations;
	}
	
	private HashMap<String, Delivery> collectDeliveries(List<Exception> exceptions, Sheet deliverySheet, HashMap<String, Station> stations, String stationDefSource, boolean ignoreMissingLotnumbers, IProgressMonitor taskMonitor) throws UserCancelException {
		debugEnter();
		final HashMap<String,String> definedLots = new HashMap<>();
		final HashMap<String, Integer> lotId2RowNum = new HashMap<>(); 
		final HashMap<String, Integer> deliveryRows = new HashMap<>();
		
		final HashMap<String, Delivery> deliveries = new HashMap<>();
		final int numRows = deliverySheet.getLastRowNum() + 1;
		final Row titleRow = deliverySheet.getRow(0);
		
		for (int rowIndex=2; rowIndex < numRows; rowIndex++) {
			final Row row = deliverySheet.getRow(rowIndex);
			if (row == null || rowEmpty(row)) break;
			final int rowNum = rowIndex + 1;
			final int nE = exceptions.size();
			boolean isInvalid = false;
			
			Delivery delivery = new Delivery();
			
			// ToDO: Verify why is id not required?
			{
				delivery.setId(getRequiredCellString(row, DeliveriesSheet.COL_DELIVERY_ID, DeliveriesSheet.COL_LABEL_DELIVERY_ID, exceptions));
				if (delivery.getId() != null) {
					if (deliveries.containsKey(delivery.getId())) {
						// exceptions.add(new Exception("Delivery defined twice -> in Row " + rowNum + " and in Row " + deliveryRows.get(delivery.getId()) + "; Delivery Id: '" + delivery.getId() + "'"));
						exceptions.add(createDuplicateIdException(DeliveriesSheet.COL_LABEL_DELIVERY_ID, delivery.getId(), row, DeliveriesSheet.COL_DELIVERY_ID));
						deliveries.put(delivery.getId(), null);
					}
					else {
						deliveryRows.put(delivery.getId(), rowNum);
						deliveries.put(delivery.getId(), delivery);
					}
				}
			}
			
			
			Product p = new Product();
			{
				String senderId = getRequiredCellString(row, DeliveriesSheet.COL_SENDER_ID, "Sender ID", exceptions);
//				if (stationId == null) return null;
				if (senderId != null) {
					Station sender = stations.get(senderId);
					// if (sender == null && !stations.containsKey(senderId)) exceptions.add(new Exception("Station ID in Deliveries not defined in stations sheet: '" + senderId + "'; -> Row " + (rowNum)));
					if (sender == null) {
						if (!stations.containsKey(senderId)) exceptions.add(new InvalidCellValueException("Unknown Sender ID '" + senderId + "' (ID has to be listed in " + stationDefSource + ")", row, DeliveriesSheet.COL_SENDER_ID));
						isInvalid = true;
					} 
					else p.setStation(sender);
				}
			}
			
			p.setName(getCellString(row.getCell(DeliveriesSheet.COL_PRODUCT_NAME), exceptions));
			
			Lot lot = new Lot();
			lot.setProduct(p);
			
			{
				Cell lotNoCell = row.getCell(DeliveriesSheet.COL_LOT_NO);
				String lotNo = getCellString(lotNoCell, exceptions);
				if (lotNo != null) lot.setNumber(lotNo);
				// else if (!ignoreMissingLotnumbers) {exceptions.add(new Exception("Please, do always provide a lot number as this is most helpful! -> Row " + (rowNum) + " in '" + filename + "'\n"));}
				else if (!ignoreMissingLotnumbers) exceptions.add(new InvalidCellValueException("Please, do always provide a lot number as this is most helpful.", row, DeliveriesSheet.COL_LOT_NO)); // -> Row " + (rowNum) + " in '" + filename + "'\n"));}
			}
			
			{
				AmountParts amountParts = getAmountPartsFromCells(
						row, 
						DeliveriesSheet.COL_LOT_SIZE_Q,
						DeliveriesSheet.COL_LOT_SIZE_U,
						"Lot Size",
						exceptions
				);
				lot.setUnitNumber(amountParts.quantity);
				lot.setUnitUnit(amountParts.unit);
			}
			
			{
				String lotId = (p.getStation() == null) ? "_" + p.getName() + "_" + lot.getNumber() : p.getStation().getId() + "_" + p.getName() + "_" + lot.getNumber();
				String lotInfo = lot.getUnitNumber() + "_" + lot.getUnitUnit();
				if (definedLots.containsKey(lotId)) {
					// ToDo: verify change (verify lot size comparison)
					// if (!definedLots.get(lotId).equals(lotInfo)) exceptions.add(new Exception("Lot has different quantities -> Lot number: '" + lot.getNumber() + "'; -> Row " + (rowNum+1)));
					if (!definedLots.get(lotId).equals(lotInfo)) exceptions.add(new InvalidCellValueException("Lot Size is different (from size in row " + lotId2RowNum.get(lotId) + ")", row));
				}
				else {
					definedLots.put(lotId, lotInfo);
					lotId2RowNum.put(lotId, rowNum);
				}
			}

			delivery.setLot(lot);
			
			applyDepartureFromCells(delivery, row, 6, 7, 8, exceptions);
			applyArrivalFromCells(delivery, row, 9, 10, 11, exceptions);
			
			{
				AmountParts amountParts = getAmountPartsFromCells(
						row,
						DeliveriesSheet.COL_UNIT_Q,
						DeliveriesSheet.COL_UNIT_U,
						"Delivery Size",
						exceptions
				);
				delivery.setUnitNumber(amountParts.quantity);
				delivery.setUnitUnit(amountParts.unit);
			}
			
			{
				// String receiverId = getCellString(row.getCell(14));
				String receiverId = getRequiredCellString(row, DeliveriesSheet.COL_RECIPIENT, "Recipient ID", exceptions);
				if (receiverId != null) {
					Station receiver = stations.get(receiverId);
					if (receiver == null) {
						if (!stations.containsKey(receiverId)) {
							// exceptions.add(new Exception("Recipient ID in sheet Deliveries not defined in stations sheet: '" + receiverId + "'; -> Row " + rowNum));
							// exceptions.add(new InvalidCellValueException("Unknown Recipient ID (ID has to be defined in " +  stationDefSource + ")", row, DeliveriesSheet.COL_RECIPIENT));
							exceptions.add(new InvalidCellValueException("Unknown Recipient ID '" + receiverId + "' (ID has to be listed in " +  stationDefSource + ")", row, DeliveriesSheet.COL_RECIPIENT));
						}
						isInvalid = true;
					}
					else delivery.setReceiver(receiver);
				}
//				else {
//					// ToDo: Verify: Why is the missing receiver not thrown?
//					if (result.getId() == null) return null;
//					else exceptions.add(new Exception("Recipient ID in sheet Deliveries not defined; -> Row " + (rowNum+1)));
//				}
			}
			
			// Further flexible cells
					
			for (int i=15;i<25;i++) {
				String fieldKey = getCellString(titleRow.getCell(i), exceptions);
				if (fieldKey != null) {
					String fieldValue = getCellString(row.getCell(i), exceptions);
					if (fieldValue != null) {
						if (DeliveriesSheet.KNOWN_LOT_FIELDS_LC.contains(fieldKey.toLowerCase())) lot.addFlexibleField(fieldKey, fieldValue);
						else delivery.addFlexibleField(fieldKey, fieldValue);
					}
				}
			}
			
			if (delivery.getId() != null) { 
				isInvalid = isInvalid || exceptions.size() > nE;
	
				if (isInvalid) deliveries.put(delivery.getId(), null);
			}
			taskMonitor.setProgress(rowNum * 100 / numRows);
			checkUserCancel(taskMonitor);
		}
		taskMonitor.setProgress(100);
		debugLeaving();
		return deliveries;
	}
	
	private void checkTraceDeliveries(List<Exception> exceptions, Sheet deliverySheet, int borderRowBetweenTopAndBottom, boolean isForTracing, boolean isNewFormat_151105, IProgressMonitor taskMonitor) throws Exception {
		HashMap<String, HashSet<Row>> deliveryIDs = new HashMap<>();
		
		ProgressMonitorParent taskMonitorParent= new ProgressMonitorParent(taskMonitor, 2);
		IProgressMonitor subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
		
		int numRows = deliverySheet.getLastRowNum() + 1;
		for (int i=2;i<numRows;i++) {
			Row row = deliverySheet.getRow(i);
			if (row != null) {
				// String cellString = getCellString(row.getCell(12)); // DeliveryID in DB
				String cellString = getCellString(row.getCell(12), exceptions); // DeliveryID in DB
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
						String tkey = getRowKey(exceptions, tmp, borderRowBetweenTopAndBottom, isForTracing);
						if (key == null) key = tkey;
						else if (!key.equals(tkey)) different = true;
						rows += ";" + (tmp.getRowNum()+1); 
					}
				}
				// if (different) exceptions.add(new Exception("Delivery ID '" + val + "' is defined more than once -> Rows: " + rows.substring(1) + ". If you have copy/pasted a new row, please clear the cell for the DeliveryID of the new Row in Column 'M' (expand it firstly to be able to see it)."));
				if (different) exceptions.add(new InvalidCellValueException("Delivery ID '" + val + "' is defined more than once -> Rows: " + rows.substring(1) + ". If you have copy/pasted a new row, please clear the cell for the DeliveryID of the new Row in Column 'M' (expand it firstly to be able to see it).", deliverySheet));
			}
			if (taskMonitor.isCanceled()) throw new UserCancelException();
		}
		subTaskMonitor.setProgress(100);
	}
	
	private String getRowKey(List<Exception> exceptions, Row row, int borderRowBetweenTopAndBottom, boolean isForTracing) throws Exception {
		boolean isProductsOut = row.getRowNum() < borderRowBetweenTopAndBottom && !isForTracing || isForTracing && row.getRowNum() > borderRowBetweenTopAndBottom;
		String key = "";
		for (int j=isProductsOut?0:1;j<row.getLastCellNum();j++) { // Start with Lot Number or after
			String cellString = getCellString(row.getCell(j), exceptions);
			key += cellString;
			key += ";";
		}
		while(key.endsWith(";;")) {
			key = key.substring(0, key.length() - 1);
		}	
		return key;
	}
	
	private void loadLookupSheet(List<Exception> exceptions, Sheet lookupSheet, IProgressMonitor taskMonitor) throws Exception {
		LookUp lu = new LookUp();
		int numRows = lookupSheet.getLastRowNum() + 1;
		for (int i=1;i<numRows;i++) {
			Row row = lookupSheet.getRow(i);
			if (row != null) {
				
				String sampling = getCellString(row.getCell(0), exceptions);
				if (sampling != null) lu.addSampling(sampling);
				
				String typeOfBusiness = getCellString(row.getCell(1), exceptions);
				if (typeOfBusiness != null) lu.addTypeOfBusiness(typeOfBusiness);
				
				String treatment = getCellString(row.getCell(2), exceptions);
				if (treatment != null) lu.addTreatment(treatment);
					
				String unit = getCellString(row.getCell(3), exceptions);
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
//		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
//		String time = LocalTime.now().format(formatter);
//		System.out.println(time + " " + msg);
	}
	
	private void debugL(String msg) {
//		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
//		for (int i = 1; i < trace.length; i++) {
//			if (
//					!trace[i].getClassName().endsWith(".Thread") &&
//					!trace[i].getMethodName().startsWith("debug")
//			) {
//				debug(trace[i].getClassName().replaceFirst("^([A-Za-z\\d_]+\\.)*", "") + "."+ trace[i].getMethodName() + " " + msg + "  " + trace[i].toString());
//				return;
//			}
//		}
//		debug(msg);
	}
	
	private void debugEnter() {
		debugL("entered ...");
	}
	
	private void debugLeaving() {
		debugL("leaving ...");
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
			requiredSubTaskMonitorCount = 2 + (d2dSheet != null ? 1 : 0) + (lookupSheet != null ? 1 : 0) + 2;
		} else {
			requiredSubTaskMonitorCount = 6 + (lookupSheet != null ? 1 : 0) + (!isForTracing ? 1 : 0);
		}
		ProgressMonitorParent taskMonitorParent = new ProgressMonitorParent(taskMonitor, requiredSubTaskMonitorCount);
		IProgressMonitor subTaskMonitor;
		
		if (deliverySheet == null) {
			subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
			debug("TracingImporter.importWorkbook checking stations ...");
		
			checkStationsFirst(exceptions, stationSheet, subTaskMonitor);
			debug("TracingImporter.importWorkbook checking stations done.");
			subTaskMonitor.setProgress(100);
			sleep();
		}
		
		if (deliverySheet != null) {
			// load all Stations
			subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
			HashMap<String, Station> stations = collectStations(exceptions, stationSheet, subTaskMonitor);
			sleep();
			
			// load all Deliveries
			subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();			
			// HashMap<String, Delivery> deliveries = collectDeliveries(exceptions, deliverySheet, stations, "sheet '" + stationSheet.getSheetName() + "'", d2dSheet != null, subTaskMonitor);
			HashMap<String, Delivery> deliveries = collectDeliveries(exceptions, deliverySheet, stations, "sheet '" + stationSheet.getSheetName() + "' in column " + SheetUtils.getColumnAddress(StationsSheet.COL_STATION_ID), d2dSheet != null, subTaskMonitor);
			sleep();
			
			// load Recipes
			HashSet<D2D> recipes = new HashSet<>();
			if (d2dSheet != null) {
				subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
				recipes = collectD2Ds(exceptions, d2dSheet, deliveries, "sheet '" + deliverySheet.getSheetName() + "' in column " + SheetUtils.getColumnAddress(DeliveriesSheet.COL_DELIVERY_ID), subTaskMonitor);
				sleep();
			}

			MetaInfo mi = new MetaInfo();
			mi.setFilename(filename);
			
			if (lookupSheet != null) {
				debug("TracingImporter.importWorkbook looking up ...");
				subTaskMonitor = taskMonitorParent.getNextSubTaskMonitor();
				loadLookupSheet(exceptions, lookupSheet, subTaskMonitor);
				debug("TracingImporter.importWorkbook after lookup ...");
				subTaskMonitor.setProgress(100);
				sleep();
			}
			
			if (exceptions.size() == 0) {
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
				//Cell stationInFocusCell = row.getCell(1);
				//String cellString = getCellString(row.getCell(1));
				String cellString = getRequiredCellString(row, 1, "Station in Focus", exceptions); //stationInFocusCell);
				
				// if (cellString == null) exceptions.add(new Exception("Station in Focus is missing (cell " + getWbRelativeCellAddressString(stationInFocusCell) + ")."));
				
				sif = cellString == null ? null : getStation(exceptions, stationSheet, cellString, row);
			}
			
			// Delivery(s) Outbound
			classRowIndex = 5;
			titleRow = transactionSheet.getRow(classRowIndex - 2);
			for (;;classRowIndex++) {
				row = transactionSheet.getRow(classRowIndex);
				if (row == null) continue;
				if (isBlockEnd(exceptions, row, 13, "Reporter Information")) break;
				Delivery d = getDelivery(exceptions, stationSheet, sif, row, true, titleRow, filename, false, null, outDeliveries, false, isNewFormat_151105);
				if (d == null) continue;
				outDeliveries.put(d.getId(), d);
				outLots.put(d.getLot().getNumber(), d.getLot());
			}
			
			// Metadata on Reporter
			classRowIndex = getNextBlockRowIndex(exceptions, transactionSheet, classRowIndex, "Reporter Information") + 2;
			row = transactionSheet.getRow(classRowIndex);
			mi = getMetaInfo(exceptions, row, transactionSheet.getRow(classRowIndex-1));
			mi.setFilename(filename);	
		}
		else { // Reporter shifted to the top
			// Metadata on Reporter
			classRowIndex = getNextBlockRowIndex(exceptions, transactionSheet, 0, "Reporter Information") + 2;
			row = transactionSheet.getRow(classRowIndex);
			mi = getMetaInfo(exceptions, row, transactionSheet.getRow(classRowIndex-1));
			mi.setFilename(filename);

			// Station in focus
			classRowIndex = getNextBlockRowIndex(exceptions, transactionSheet, classRowIndex, "Station in Focus:");
			row = transactionSheet.getRow(classRowIndex);
			{
//				Cell stationInFocusCell = row.getCell(1);
//				String cellString = getCellString(stationInFocusCell);
				String cellString = getRequiredCellString(row, 1, "Station in Focus", exceptions);
				
				// if (cellString == null) exceptions.add(new Exception("Station in Focus is missing (cell " + getWbRelativeCellAddressString(stationInFocusCell) + ")."));
				
				sif = cellString == null ? null : getStation(exceptions, stationSheet, cellString, row);
			}
			
			String label = "Products Out";
			if (isForTracing) label = "Ingredients In for Lot(s)";
			// Delivery(s) Outbound
			classRowIndex = getNextBlockRowIndex(exceptions, transactionSheet, classRowIndex, label) + 3;
			titleRow = transactionSheet.getRow(classRowIndex - 2);
			cell = titleRow.getCell(0);
			isNewFormat_151105 = cell.getStringCellValue().equals("Product Lot Number");
			for (;;classRowIndex++) {
				row = transactionSheet.getRow(classRowIndex);
				if (row == null) continue;
				if (isBlockEnd(exceptions, row, 13, "Lot Information")) break;
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
		classRowIndex = getNextBlockRowIndex(exceptions, transactionSheet, classRowIndex, "Lot Information") + 3;
		borderRowLotStart = classRowIndex;
		titleRow = transactionSheet.getRow(classRowIndex - 2);
		for (;;classRowIndex++) {
			row = transactionSheet.getRow(classRowIndex);
			if (row == null) continue;
			if (isBlockEnd(exceptions, row, 13, label)) break;
			if (!fillLot(exceptions, row, sif, outLots, titleRow, isForTracing ? outDeliveries : null, classRowIndex + 1, isNewFormat_151105)) {
				// exceptions.add(new Exception("Lot number unknown in Row number " + (classRowIndex + 1)));
				exceptions.add(new InvalidCellValueException("Unknown Lot Number", row)); //Lot number unknown in Row number " + (classRowIndex + 1)));
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
		classRowIndex = getNextBlockRowIndex(exceptions, transactionSheet, classRowIndex, label) + 3;
		HashMap<String, Delivery> inDeliveries = new HashMap<>(); 
		int numRows = transactionSheet.getLastRowNum() + 1;
		titleRow = transactionSheet.getRow(classRowIndex - 2);
		for (;classRowIndex < numRows;classRowIndex++) {
			row = transactionSheet.getRow(classRowIndex);
			if (row == null) continue;
			if (isBlockEnd(exceptions, row, 13, null)) break;
			Delivery d = getDelivery(exceptions, stationSheet, sif, row, isForTracing, titleRow, filename, isForTracing, outLots, inDeliveries, false, isNewFormat_151105);
			if (d == null) continue;
			// if (!isForTracing && d.getTargetLotIds().size() == 0) exceptions.add(new Exception("Lot number unknown in Row number " + (classRowIndex + 1)));
			if (!isForTracing && d.getTargetLotIds().size() == 0) exceptions.add(new InvalidCellValueException("Unknown Lot Number", row));
			inDeliveries.put(d.getId(), d);
			hasIngredients = true;
		}
		if (!hasIngredients) {
			// warns.put("No " + (isForTracing ? "Products Out" : "ingredients") + " defined...", null);
			addFileRelatedWarning("No " + (isForTracing ? "Products Out" : "ingredients") + " defined...", transactionSheet);
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
			loadLookupSheet(exceptions, lookupSheet, subTaskMonitor);
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
	
//	private boolean applyCellString__(Cell cell, ApplyStringFunction fun, List<Exception> exceptions) throws InvalidCellValueException {
//		try {
//			applyCellString__(cell, fun);
//			return true;
//		} catch(InvalidCellValueException ex) {
//			if (exceptions == null) throw ex;
//			exceptions.add(ex);
//		}
//		return false;
//	}
	
//	private void applyCellString__(Cell cell, ApplyStringFunction fun) throws InvalidCellValueException {
//		String cellString = getCellString(cell);
//		fun.applyString(cellString);
//	}
	
	private String getLabelCellString(Cell cell) throws Exception {
		return getCellString(cell, false, false, false);
	}
	
	private String getLabelCellString(Cell cell, List<Exception> exceptions) {
		try {
			return getLabelCellString(cell);
		} catch(Exception ex) {
			exceptions.add(ex);
		}
		return null;
	}
	
	private String getCellString(Cell cell, List<Exception> exceptions) {
		try {
			return getCellString(cell);
		} catch (InvalidCellValueException ex) {
			exceptions.add(ex);
		}
		return null;
	}
	
	private String getRequiredCellString(Row row, int colIndex, String valueLabel, List<Exception> exceptions) {
		try {
			String text = getCellString(row.getCell(colIndex));
			if (text == null) exceptions.add(new InvalidCellValueException("Missing " + valueLabel, row, colIndex));
			return text;
		} catch (InvalidCellValueException ex) {
			exceptions.add(ex);
		}
		return null;
	}
	
	private String getCellString(Cell cell) throws InvalidCellValueException {
		return getCellString(cell, false, true, true);
	}
	
	private String getCellString(Cell cell, boolean checkIfDate) throws InvalidCellValueException {
		return getCellString(cell, checkIfDate, true, true);
	}
	
	private String getCellString(Cell cell, boolean checkIfDate, boolean removeLinebreaks, boolean reportLineBreaks) throws InvalidCellValueException {
		CellType cellType = SheetUtils.getEffectiveCellType(cell);
		if (cellType == null) return null;
		if (cellType == CellType.BLANK) return null;
			
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
							
		return getStringCellString(cell, removeLinebreaks, reportLineBreaks);
	}
	
	private String getStringCellString(Cell cell, boolean removeLinebreaks, boolean reportLineBreaks) {	
			
		String cellString = trim(cell.getStringCellValue());
		if (cellString == null) return null;
		
		if (removeLinebreaks && cellString.matches("(.*(\\r|\\n).*)+")) {
			
			cellString = trim(cellString.replaceAll("(\\s*(\\r\n\\|\\r|\\n)\\s*)+", " "));
			if (cellString == null) return null;
			
			if (reportLineBreaks) {
				addFileRelatedWarning(LINE_BREAKS_REMOVED, cell);
			}
		};
		
		return cellString;				
	}
		
	private Integer getCellInt(Cell cell, String valueLabel) throws InvalidCellValueException {
		return getCellInt(cell, valueLabel, null);
	}
	
	private Integer getCellInt(Cell cell, String valueLabel, List<Exception> exceptions) throws InvalidCellValueException {
		CellType cellType = SheetUtils.getEffectiveCellType(cell);
		//if (cell == null || cell.getCellType() == CellType.BLANK) return null;
		if (cellType == null) return null;
		if (cellType == CellType.BLANK) return null;
		if (cellType == CellType.NUMERIC) return (int)cell.getNumericCellValue();
		if (cellType == CellType.STRING) {
			String cellString = getStringCellString(cell, true, false);
			if (cellString == null) return null;
			try {
				return Integer.parseInt(cellString);
			} catch(NumberFormatException ex) {
				ex.printStackTrace();
				// throw new NumberFormatException("Value in cell " + getWbRelativeCellAddressString(cell) + " is expected be a integer.");
			}
		}
		if (valueLabel == null) valueLabel = "integer value";
		InvalidCellValueException exception = createInvalidValueException(valueLabel, "value has to be an integer", cell);
		if (exceptions == null) throw exception;
		exceptions.add(exception);
		return null;
	}
	
//	private Double getCellDouble_(Cell cell, String valueLabel, List<Exception> exceptions) throws InvalidCellValueException {
//		CellType cellType = SheetUtils.getEffectiveCellType(cell);
//		
//		if (cellType == null) return null;
//		if (cellType == CellType.BLANK) return null;
//		if (cellType == CellType.NUMERIC) return cell.getNumericCellValue();
//		
//		if (cellType == CellType.STRING) {
//			String cellString = cell.getStringCellValue();
//			if (cellString == null) return null; 
//			try {
//				return Double.parseDouble(cellString.trim());
//			} catch(NumberFormatException ex) {
//				// String msg = "Invalid " + (valueLabel == null ? "value" : valueLabel) +  " ('" + cellString + "') in cell " + getWbRelativeCellAddressString(cell) + ". Value has to be a number.";
//				InvalidCellValueException exception = new InvalidCellValueException("Invalid " + valueLabel, cell);
//				if (exceptions != null) exceptions.add(exception);
//				else throw exception;	
//			}
//		}
//		return null;
//	}
	
	private Double getCellDouble(Cell cell, String valueLabel, List<Exception> exceptions) {
		try {
			CellType cellType = SheetUtils.getEffectiveCellType(cell);
			
			if (cellType == null) return null;
			if (cellType == CellType.BLANK) return null;
			if (cellType == CellType.NUMERIC) return cell.getNumericCellValue();
			
			if (cellType == CellType.STRING) {
				String cellString = cell.getStringCellValue();
				if (cellString == null) return null; 
				try {
					return Double.parseDouble(cellString.trim());
				} catch(NumberFormatException ex) {
					// InvalidCellValueException exception = new InvalidCellValueException("Invalid " + valueLabel, cell);
					InvalidCellValueException exception = createInvalidValueException(valueLabel, "value has to be a number", cell);
					exceptions.add(exception);	
				}
			}
		} catch(InvalidCellValueException ex) {
			exceptions.add(ex);
		}
		return null;
	}
	
	private boolean rowEmpty(Row row) {
		int firstCellIndex = row.getFirstCellNum(); // returns zero based cell index
		if (firstCellIndex < 0) return true;
		
		int lastCellNum = row.getLastCellNum(); // returns one based cell num
		
		for (int i = firstCellIndex; i < lastCellNum; i++) {
			if (!SheetUtils.isCellEmpty(row.getCell(i))) return false;
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
		
		String[] expectedTables;
		if (isAllInOneTemplate) expectedTables = new String[] {"deliveries"};
		else if (backtracing) expectedTables = new String[] {"outgoing deliveries", "incoming deliveries"};
		else expectedTables = new String[] {"incoming deliveries", "outgoing deliveries"};
		if (!isProduction && !isAllInOneTemplate) expectedTables = new String[] {expectedTables[1]};
		

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
				String cs = getCellString(row.getCell(hmS.get("name")), exceptions);
				focusStation.setName(cs);
				String address = getCellString(row.getCell(hmS.get("address")), exceptions);
				focusStation.setAddress(address);
				focusStation.setCountry(getCellString(row.getCell(hmS.get("country")), exceptions));
				if (!isProduction && hmS.get("tob") >= 0) {
					focusStation.setTypeOfBusiness(getCellString(row.getCell(hmS.get("tob")), exceptions));
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
			int startedTableCount = 0;
			boolean headerFound = false;
			boolean doPreCollect = false;
			boolean chargenLinkTypeChecked = false;
			ChargenLinkType preferedChargenLinkType = null;
			
			for (int iRow=(isAllInOneTemplate?0:1);iRow<=numRows;iRow++) {
				checkUserCancel(taskMonitor);
				row = sheet.getRow(iRow);
				if (row != null) {
					if (!rowEmpty(row)) {
						// ToDo: verify change
						final String cell0Label = getLabelCellString(row.getCell(0), exceptions);
						if (cell0Label != null && !isAllInOneTemplate || isAllInOneTemplate && iRow > 1) doPreCollect = false; //  && cs.startsWith(XlsStruct.TOP_END_LINE)
						
						if (doCollect || doPreCollect) {
							//System.err.print(i+1);
							if (isAllInOneTemplate) {
								if (!headerFound) {
									exceptions.add(new InvalidCellValueException("Delivery Table Header not found", sheet));
									return exceptions;
								}

								focusStation = null;
								String name = getCellString(row.getCell(xlsSRecipient.getNameCol()), exceptions);
								String address = getCellString(row.getCell(xlsSRecipient.getAddressCol()), exceptions);
								int sID = genDbId(""+name+address);
								if (idToStationMap.containsKey(sID)) {
									focusStation = idToStationMap.get(sID);
									focusStation.addFlexibleField(XlsStruct.getOUT_SOURCE_KEY("en"), focusStation.getFlexible(XlsStruct.getOUT_SOURCE_KEY(lang)) + "; " + XlsStruct.getOUT_SOURCE_VAL(lang) + " " + (iRow+1));
								}
								else {
									focusStation = new Station();
									focusStation.setName(name);
									focusStation.setAddress(address);
									if (xlsSRecipient.getCountryCol() >= 0) focusStation.setCountry(getCellString(row.getCell(xlsSRecipient.getCountryCol()), exceptions));
									if (xlsSRecipient.getTobCol() >= 0) focusStation.setTypeOfBusiness(getCellString(row.getCell(xlsSRecipient.getTobCol()), exceptions));
									focusStation.addFlexibleField(XlsStruct.getOUT_SOURCE_KEY("en"), XlsStruct.getOUT_SOURCE_VAL(lang) + " " + (iRow+1));
									focusStation.setId(""+sID);
									idToStationMap.put(sID, focusStation);
								}
								if (xlsSRecipient.getExtraVals().size() > 0) {
									for (int colnum : xlsSRecipient.getExtraVals().keySet()) {
										String val = getCellString(row.getCell(colnum), exceptions);
										if (val != null) {
											focusStation.addFlexibleField(xlsSRecipient.getExtraVals().get(colnum), val);
										}
									}
								}
							}
			
							if (!headerFound) {
//								String table;
//								if (isAllInOneTemplate) table = "deliveries";
//								else if (backtracing == (startedTableCount == 0)) table = "incoming deliveries";
//								else table = "outgoing deliveries";
										
								exceptions.add(new InvalidCellValueException("Table header for " + expectedTables[startedTableCount] + " not found", sheet));
								return exceptions;
							}
				
							String name = getCellString(row.getCell(xlsS.getNameCol()), exceptions);
							String address = getCellString(row.getCell(xlsS.getAddressCol()), exceptions);
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
								if (xlsS.getCountryCol() >= 0) supplierS.setCountry(getCellString(row.getCell(xlsS.getCountryCol()), exceptions));
								if (xlsS.getTobCol() >= 0) supplierS.setTypeOfBusiness(getCellString(row.getCell(xlsS.getTobCol()), exceptions));
								supplierS.addFlexibleField(XlsStruct.getOUT_SOURCE_KEY("en"), XlsStruct.getOUT_SOURCE_VAL(lang) + " " + (iRow+1));
								supplierS.setId(""+sID);
								idToStationMap.put(sID, supplierS);
							}
							if (xlsS.getExtraVals().size() > 0) {
								for (int colnum : xlsS.getExtraVals().keySet()) {
									String val = getCellString(row.getCell(colnum), exceptions);
									if (val != null) {
										supplierS.addFlexibleField(xlsS.getExtraVals().get(colnum), val);
									}
								}
							}
							
							String f2 = getCellString(row.getCell(xlsP.getNameCol()), exceptions);
							String f3 = xlsP.getEanCol() < 0 ? null : getCellString(row.getCell(xlsP.getEanCol()), exceptions);
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
									String val = getCellString(row.getCell(colnum), exceptions);
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
//								if (xlsD.getDayCol()<0) { 
//									if (xlsD.getDeliveryDateCol()<0)  {
//										throw new Exception("Delivery date columns not found.");
//									} else {
//										throw new Exception("Delivery date day column not found.");
//									}
//								}
//								f4 = getInt(getCellString(row.getCell(xlsD.getDayCol())));
//								if (xlsD.getMonthCol()<0) { throw new Exception("Delivery date month column not found."); }
//								f5 = getInt(getCellString(row.getCell(xlsD.getMonthCol())));
//								if (xlsD.getYearCol()<0) { throw new Exception("Delivery date year column not found."); }
//								f6 = getInt(getCellString(row.getCell(xlsD.getYearCol())));
								DateParts dateParts = getDatePartsFromCells(row, xlsD.getDayCol(), xlsD.getMonthCol(), xlsD.getYearCol(), "Delivery Date", exceptions);
								f4 = dateParts.day;
								f5 = dateParts.month;
								f6 = dateParts.year;
							}
							String f7 = xlsD.getAmountCol() >= 0 ? getCellString(row.getCell(xlsD.getAmountCol()), exceptions) : null;
							if (xlsD.getCommentCol() >= 0) {
								String val = getCellString(row.getCell(xlsD.getCommentCol()), exceptions);
								if (val != null) f8 += val + "\n";
							}
							if (xlsO.getCommentCol() >= 0) {
								String val = getCellString(row.getCell(xlsO.getCommentCol()), exceptions);
								if (val != null) f8 += val + "\n";
							}
							f8 = f8.trim();
							if (f8.isEmpty()) f8 = null;
							if (isAllInOneTemplate) {
								f2 = getCellString(row.getCell(6), exceptions); // Erzeugercode(s)
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
									// exceptions.add(new Exception("You have no lot information at all in Row " + (iRow+1) + "."));
									exceptions.add(new InvalidCellValueException("You have no lot information at all", row));
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
									String val = getCellString(row.getCell(colnum), exceptions);
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
										String val = getCellString(row.getCell(colnum), exceptions);
										if (val != null) {
											d.addFlexibleField(xlsD.getExtraVals().get(colnum), val);
										}
									}
								}
								if (xlsO.getExtraVals().size() > 0) {
									for (int colnum : xlsO.getExtraVals().keySet()) {
										String val = getCellString(row.getCell(colnum), exceptions);
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
								Cell chargenLinkCell = row.getCell(xlsD.getChargenLinkCol());
								String key = getCellString(chargenLinkCell, exceptions);
								if (key != null) {
									// if (chargenLinkType != ChargenLinkType.lineNo && lotNumberToLotId.containsKey(key)) {
									if (
										lotNumberToLotId.containsKey(key) && 
										(preferedChargenLinkType != ChargenLinkType.lineNo || !olddelsRow.containsKey(key))
									) {
										if (lotDoublettes.contains(key)) {
											// ToDo: Verify whether this message is also delivered in case of forward tracing
											// exceptions.add(new Exception("[" + (iRow+1) + "] Unclear to which lot the ingredients should be connected - same Lot number (" + key + ") is used for different products.\nTry to make use of the Line Number as connection key."));
											exceptions.add(new InvalidCellValueException("Unclear to which lot the ingredients should be connected to. The Lot number '" + key + "' is used for different products.\nTry to make use of the Line Number as connection key.", chargenLinkCell));
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
										// exceptions.add(new Exception("[" + (iRow+1) + "] Value " + key + " in column " + xlsD.getChargenLinkCol() + " does not match a lot number or a line number."));
										// exceptions.add(new InvalidCellValueException("Value '" + key + "' in cell " + chargenLinkCell.getAddress().toString() + " does not match a lot number or a line number.", row.getSheet()));
										if (backtracing) exceptions.add(new InvalidCellValueException("Unknown outgoing goods reference (value does not match a lot number or a line number from outgoing goods table)", row, xlsD.getChargenLinkCol()));
										else exceptions.add(new InvalidCellValueException("Unknown incoming goods reference (value does not match a lot number or a line number from incoming goods table)", row, xlsD.getChargenLinkCol()));
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
//							backtracing && cellLabel != null && (cellLabel.trim().startsWith(XlsStruct.getBACK_NEW_DATA_START(lang)) || cellLabel.trim().startsWith(XlsStruct.getPROD_NEW_DATA_START(lang))) ||
							backtracing && cell0Label != null && (
								cell0Label.trim().startsWith(XlsStruct.getBACK_NEW_DATA_START(lang)) && startedTableCount == 0 || 
								cell0Label.trim().startsWith(XlsStruct.getPROD_NEW_DATA_START(lang)) && startedTableCount == 1 && isProduction
							) ||
//							!backtracing && cellLabel != null && (cellLabel.trim().startsWith(XlsStruct.getFWD_NEW_DATA_START(lang)) || cellLabel.trim().startsWith(XlsStruct.getPROD_NEW_DATA_START(lang))) ||
							!backtracing && cell0Label != null && (
								cell0Label.trim().startsWith(XlsStruct.getFWD_NEW_DATA_START(lang)) && startedTableCount == 0 || 
								cell0Label.trim().startsWith(XlsStruct.getPROD_NEW_DATA_START(lang)) && startedTableCount == 1 && isProduction
							) ||
							isProduction && iRow==3
						) {
							xlsS = new XlsStation();
							xlsSRecipient = new XlsStation();
							xlsP = new XlsProduct();
							xlsL = new XlsLot();
							xlsD = new XlsDelivery();
							xlsO = new XlsOther();
							// headerFound = true;
							// Header for Entities
							row = sheet.getRow(iRow);
							for (int iCell=0;iCell<row.getLastCellNum();iCell++) {
								String str = getLabelCellString(row.getCell(iCell), exceptions);
								if (str != null) {
									str = str.trim();
									boolean isRecipient = str.equalsIgnoreCase(XlsStation.BLOCK_RECIPIENT(lang));
									if ((isRecipient || str.equalsIgnoreCase(XlsStation.BLOCK_SUPPLIER(lang)))) {
										if (isAllInOneTemplate && isRecipient) xlsSRecipient.setStartCol(iCell);
										else xlsS.setStartCol(iCell);
										iCell++;										
										while(true) {
											String string = getLabelCellString(row.getCell(iCell), exceptions);
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
											String string = getLabelCellString(row.getCell(iCell), exceptions);
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
											String string = getLabelCellString(row.getCell(iCell), exceptions);
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
											String string = getLabelCellString(row.getCell(iCell), exceptions);
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
											String string = getLabelCellString(row.getCell(iCell), exceptions);
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
							if (xlsS.getStartCol() >= 0) {
								for (int ii=xlsS.getStartCol();ii<=xlsS.getEndCol();ii++) {
									String str = getLabelCellString(row.getCell(ii));
									xlsS.addField(str, ii, lang);
								}
							}
							if (isAllInOneTemplate && xlsSRecipient.getStartCol() >= 0) {
								for (int ii=xlsSRecipient.getStartCol();ii<=xlsSRecipient.getEndCol();ii++) {
									String str = getLabelCellString(row.getCell(ii), exceptions);
									xlsSRecipient.addField(str, ii, lang);
								}								
							}
							if (xlsP.getStartCol() >= 0) {
								for (int ii=xlsP.getStartCol();ii<=xlsP.getEndCol();ii++) {
									String str = getLabelCellString(row.getCell(ii), exceptions);
									xlsP.addField(str, ii, lang);
								}
							}
							if (xlsL.getStartCol() >= 0) {
								for (int ii=xlsL.getStartCol();ii<=xlsL.getEndCol();ii++) {
									String str = getLabelCellString(row.getCell(ii), exceptions);
									xlsL.addField(str, ii, lang);
								}
							}
							if (xlsD.getStartCol() >= 0) {
								for (int ii=xlsD.getStartCol();ii<=xlsD.getEndCol();ii++) {
									String str = getLabelCellString(row.getCell(ii), exceptions);
									xlsD.addField(str, ii, lang);
								}
							}
							if (xlsO.getStartCol() >= 0 && xlsO.getEndCol() >= 0) {
								for (int ii=xlsO.getStartCol();ii<=xlsO.getEndCol();ii++) {
									String str = getLabelCellString(row.getCell(ii), exceptions);
									xlsO.addField(str, ii, lang);
								}								
							}
							
							if (isProduction && iRow==4) {
								xlsD.setChargenLinkCol(-1);
								doPreCollect = true;
							}
							else if (cell0Label != null && cell0Label.trim().startsWith(XlsStruct.getPROD_NEW_DATA_START(lang))) { // "Zeilennummer"
								xlsD.setChargenLinkCol(0);
								doCollect = true;
							}
							else { // Simple Start Template or AiO
								xlsD.setChargenLinkCol(-1);
								doCollect = true;
							}
							if (!isAllInOneTemplate) iRow++;
							
							{
								// header validation
								List<Exception> headerExceptions = new ArrayList<>();
								
								String stationType;
								if (isAllInOneTemplate) stationType = "Supplier";
								else if (backtracing == (startedTableCount == 0)) stationType = "Recipient";
								else stationType = "Supplier";
								
								if (xlsS.getNameCol() < 0) headerExceptions.add(new InvalidCellValueException("Missing " + stationType + " Name Column", sheet));
								if (xlsS.getAddressCol() < 0) headerExceptions.add(new InvalidCellValueException("Missing " + stationType + " Address Column", sheet));
								if (xlsP.getNameCol() < 0) headerExceptions.add(new InvalidCellValueException("Missing Product Name Column", sheet));
								if (xlsL.getLotCol() < 0) headerExceptions.add(new InvalidCellValueException("Missing Lot Number Column", sheet));
								if (xlsL.getMhdCol() < 0) headerExceptions.add(new InvalidCellValueException("Missing Mhd Column", sheet));
								if (xlsD.getDayCol() < 0) headerExceptions.add(new InvalidCellValueException("Missing Delivery Date Day Column", sheet));
								if (xlsD.getMonthCol() < 0) headerExceptions.add(new InvalidCellValueException("Missing Delivery Date Month Column", sheet));
								if (xlsD.getYearCol() < 0) headerExceptions.add(new InvalidCellValueException("Missing Delivery Date Year Column", sheet));
								
								if (isAllInOneTemplate) {
									if (xlsSRecipient.getNameCol() < 0) headerExceptions.add(new InvalidCellValueException("Missing Recipient Name Column", sheet));
									if (xlsSRecipient.getAddressCol() < 0) headerExceptions.add(new InvalidCellValueException("Missing Recpient Address Column", sheet));	
								}
								
								if (!headerExceptions.isEmpty()) {
//									String table;
//									if (isAllInOneTemplate) table = "deliveries";
//									else if (backtracing == (startedTableCount == 0)) table = "incoming deliveries";
//									else table = "outgoing deliveries";
											
									exceptions.add(new InvalidCellValueException("Invalid table header for " + expectedTables[startedTableCount], sheet));
									exceptions.addAll(headerExceptions);
									return exceptions;
								}
								headerFound = true;
								startedTableCount++;
							}
							continue;
						}
					}
				}
				if (taskMonitor.isCanceled()) throw new UserCancelException();
				// taskMonitor.setProgress((int)((iRow + 1) / (double)numRows * 100));
				taskMonitor.setProgress(100 * iRow / numRows);
			}
			{
				// final int expectedTableCount = isAllInOneTemplate || !isProduction ? 1 : 2;
				if (startedTableCount < expectedTables.length) {
//					String table;
//					if (isAllInOneTemplate) table = "deliveries";
//					else if (backtracing == (startedTableCount == 0)) table = "incoming deliveries";
//					else table = "outgoing deliveries";
							
					exceptions.add(new InvalidCellValueException("Missing table for " + expectedTables[startedTableCount], sheet));
					return exceptions;
				}
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
			e.printStackTrace();
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

	private boolean isBlockEnd(List<Exception> exceptions, Row row, int numCols2Check, String nextBlockIdentifier) throws Exception {
		if (row == null) return true;
		for (int j=0;j<numCols2Check;j++) {

			String cellString = getCellString(row.getCell(j), exceptions); 
			if (cellString == null) continue;
			if (j == 0 && nextBlockIdentifier != null && cellString.equals(nextBlockIdentifier)) return true;
			if (!cellString.isEmpty()) return false;
		}
		return true;
	}
	
	private int getNextBlockRowIndex(List<Exception> exceptions, Sheet transactionSheet, int rowIndex, String nextBlockIdentifier) throws Exception {
		int numRows = transactionSheet.getLastRowNum() + 1;
		for (;rowIndex < numRows;rowIndex++) {
			Row row = transactionSheet.getRow(rowIndex);
			String cellString = getCellString(row.getCell(0), exceptions);
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
				String cellString = getCellString(row.getCell(0), exceptions);
				if (cellString != null && cellString.equals(lookup)) {
					station = getStation(businessSheet.getRow(0), row, exceptions);
					break;
				}
			}
		}
		if (station == null) {
			exceptions.add(new InvalidCellValueException("Station '" + lookup + "' is not correctly defined", srcrow));
		}
		return station;
	}
	
	private Station getStation(Row titleRow, Row row, List<Exception> exceptions) {
		if (row == null) return null;
		Station station = new Station();
		{
			String id = getRequiredCellString(row, StationsSheet.COL_STATION_ID, StationsSheet.COL_LABEL_STATION_ID, exceptions);
			if (id != null) station.setId(id);
			else return null;
		}
		
		station.setName(getCellString(row.getCell(StationsSheet.COL_NAME), exceptions));
		station.setStreet(getCellString(row.getCell(StationsSheet.COL_STREET), exceptions));
		station.setNumber(getCellString(row.getCell(StationsSheet.COL_STREET_NO), exceptions));
		station.setZip(getCellString(row.getCell(StationsSheet.COL_ZIP), exceptions));
		station.setCity(getCellString(row.getCell(StationsSheet.COL_CITY), exceptions));
		station.setDistrict(getCellString(row.getCell(StationsSheet.COL_DISTRICT), exceptions));
		station.setState(getCellString(row.getCell(StationsSheet.COL_STATE), exceptions));
		station.setCountry(getCellString(row.getCell(StationsSheet.COL_COUNTRY), exceptions));
		station.setTypeOfBusiness(getCellString(row.getCell(StationsSheet.COL_TOB), exceptions));

		// Further flexible cells
		addFlexibleFields(titleRow, row, 10, 19, station, exceptions);
		return station;
	}
	
	private HashSet<D2D> collectD2Ds(List<Exception> exceptions, Sheet d2dSheet, HashMap<String, Delivery> deliveries, String deliveryDefSource, IProgressMonitor taskMonitor) throws UserCancelException {
		debugEnter();
		final HashSet<D2D> recipes = new HashSet<>();
		
		final int numRows = d2dSheet.getLastRowNum() + 1;
		final Row titleRow = d2dSheet.getRow(0);
		for (int rowIndex=1; rowIndex < numRows; rowIndex++) {
			final Row row = d2dSheet.getRow(rowIndex);
			if (row == null || rowEmpty(row)) break;
			final int rowNum = rowIndex + 1;
			final int nE = exceptions.size();
			boolean isInvalid = false;
			
			final D2D d2d = new D2D();
			{
				String fromDeliveryId = getRequiredCellString(row, D2DSheet.COL_FROM_ID, "Delivery ID", exceptions);
				
				if (fromDeliveryId != null) {
					Delivery fromDelivery = deliveries.get(fromDeliveryId);
					if (fromDelivery == null) {
						if (!deliveries.containsKey(fromDeliveryId)) {
							// exceptions.add(new Exception("Delivery ID in sheet Deliveries2Deliveries not defined in deliveries sheet: '" + fromDeliveryId + "'; -> Row " + (rowNum)));
							// exceptions.add(new InvalidCellValueException("Unknown From Delivery ID (ID has to be defined in " + deliveryDefSource + ")", row, D2DSheet.COL_FROM_ID));
							exceptions.add(new InvalidCellValueException("Unknown Delivery ID (ID has to be listed in " + deliveryDefSource + ")", row, D2DSheet.COL_FROM_ID));
						}
						isInvalid = true;
					} 
					else d2d.setIngredient(fromDelivery);
				}
			}
			
			{
				String toDeliveryId = getRequiredCellString(row, D2DSheet.COL_TO_ID, "Delivery ID", exceptions);
				if (toDeliveryId != null) {
					Delivery toDelivery = deliveries.get(toDeliveryId);
					if (toDelivery == null) {
						if (!deliveries.containsKey(toDeliveryId)) { 
							// exceptions.add(new Exception("Delivery ID in sheet Deliveries2Deliveries not defined in deliveries sheet: '" + toDeliveryId + "'; -> Row " + (rowNum)));
							exceptions.add(new InvalidCellValueException("Unknown Delivery ID (ID has to be listed in " + deliveryDefSource + ")", row, D2DSheet.COL_TO_ID));
						}
						isInvalid = true;
					} 
					else d2d.setTargetDelivery(toDelivery);
				}
			}
			
			if (d2d.getIngredient() != null && d2d.getTargetDelivery() != null) {
				Station fromDeliveryReceiver = d2d.getIngredient().getReceiver();
				Station toDeliverySender = d2d.getTargetDelivery().getLot().getProduct().getStation();
				// String fromDeliveryReceiverId = d2d.getIngredient().getReceiver() == null ? null : 
				//if (!d2d.getIngredient().getReceiver().getId().equals(d2d.getTargetDelivery().getLot().getProduct().getStation().getId())) {
				if (fromDeliveryReceiver != null && toDeliverySender != null && fromDeliveryReceiver != toDeliverySender) {
					// exceptions.add(new Exception("Recipient does not match Supplier; in sheet Deliveries2Deliveries: '" + d2d.getIngredient().getId() + "' -> '" + d2d.getTargetDelivery().getId() + "'; -> Row " + (rowNum+1)));
					// exceptions.add(new InvalidCellValueException("Invalid Delivery to Delivery assignment. Recipient does not match supplier", row));
					exceptions.add(new InvalidCellValueException(
						"Invalid Delivery to Delivery assignment. " + 
						"Recipient of \"" + D2DSheet.COL_LABEL_FROM_ID + "\" does not match Sender of \"" + D2DSheet.COL_LABEL_TO_ID + "\"", 
						row
					));
				}
			}
			// Please continue here 
			// Further flexible cells

			addFlexibleFields(titleRow, row, 2, 9, d2d, exceptions);
			if (!isInvalid && exceptions.size() == nE) {
				recipes.add(d2d);
			}
//			return d2d;
//			if (dl == null) break;
//			recipes.add(dl);
			checkUserCancel(taskMonitor);
			taskMonitor.setProgress(rowNum * 100 / numRows);
		}
		// debug("importing d2ds done.");
		taskMonitor.setProgress(100);
		// sleep();
		debugLeaving();
		return recipes;
	}
	
	private void addFlexibleFields(Row titleRow, Row row, int fromIndex, int toIndex, IFlexibleFieldContainer flexibleFieldContainer, List<Exception> exceptions) {
		for (int i = fromIndex;i <= toIndex; i++) {
			String titleCellString = getCellString(titleRow.getCell(i), exceptions);
			if (titleCellString != null) {
				String rowCellString = getCellString(row.getCell(i), exceptions);
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
	
	class AmountParts {
		public Double quantity;
		public String unit;
		
		public AmountParts(Double quantity, String unit) {
			this.quantity = quantity;
			this.unit = unit;
		}
	}
	
//	private void addWarning(String key, String source) {
//		Set<String> sources = warns.get(key);
//		if (sources == null) {
//			sources = new LinkedHashSet<>();
//			warns.put(key, sources);
//		}
//		sources.add(source);
//	}
	
	private void addFileRelatedWarning(String warning, Cell cell) {
		List<WarningSource> sources = fileRelatedWarns.get(warning);
		if (sources == null) {
			sources = new ArrayList<>();
			fileRelatedWarns.put(warning, sources);
		}
		sources.add(new WarningSource(cell));
	}
	
	private void addFileRelatedWarning(String warning, Sheet sheet) {
		List<WarningSource> sources = fileRelatedWarns.get(warning);
		if (sources == null) {
			sources = new ArrayList<>();
			fileRelatedWarns.put(warning, sources);
		}
		sources.add(new WarningSource(sheet));
	}
	
	private boolean isLeapYear(int year) {
		return ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0);
	}
	
	private boolean isValidDay(Integer day, Integer month, Integer year) {
		if (day == null || day < 1 || day > 31) return false;
		if (month == null) return true;
		
		List<Integer> monthsWith31Days = Arrays.asList(1, 3, 5, 7, 8, 10, 12);
		
		if (day == 31 && monthsWith31Days.contains(day)) return true;
		return month != 2 || (day != 30 && (day != 29 || year == null || !isLeapYear(year))); 
	}
//	
	private DateParts getDatePartsFromCells(Row row, int dayColIndex, int monthColIndex, int yearColIndex, String valueLabel, List<Exception> exceptions) {
		Integer day = null;
		Integer month = null;
		Integer year = null; 
		Exception yearEx = null;
		Exception monthEx = null;
		Exception dayEx = null;
		
		try { 
			// year = getCellInt(row.getCell(yearColIndex), valueLabel); 
			year = getCellInt(row.getCell(yearColIndex), valueLabel + " Year"); 
		}
		catch(NumberFormatException | InvalidCellValueException ex) {
			yearEx = ex;
		}
		try { 
			// month = getCellInt(row.getCell(monthColIndex), valueLabel);
			month = getCellInt(row.getCell(monthColIndex), valueLabel + " Month");
		}
		catch(NumberFormatException | InvalidCellValueException ex) { 
			monthEx = ex;
		}
		try { 
			// day = getCellInt(row.getCell(dayColIndex), valueLabel);
			day = getCellInt(row.getCell(dayColIndex), valueLabel + " Day");
		}
		catch(NumberFormatException | InvalidCellValueException ex) { 
			dayEx = ex;
		}
		// checkNumberRanges
		if (year != null && (year < 1 || year > 9999)) {
			year = null;
			yearEx = createInvalidValueException(valueLabel + " Year", null, row, yearColIndex);
		}
		if (month != null && (month < 1 || month > 12)) {
			month = null;
			monthEx = createInvalidValueException(valueLabel + " Month", null, row, monthColIndex);
		}
		if (day != null && !isValidDay(day, month, year)) {
			day = null;
			dayEx = createInvalidValueException(valueLabel + " Day", null, row, dayColIndex);
		}
		
		for (Exception ex : new Exception[] {dayEx, monthEx, yearEx}) if (ex != null) exceptions.add(ex);
				
		return new DateParts(day, month, year);
	}
	
	private AmountParts getAmountPartsFromCells(Row row, int quantityColIndex, int unitColIndex, String valueLabel, List<Exception> exceptions) {
		Double quantity = null;
		String unit = null;
		
		unit = getCellString(row.getCell(unitColIndex), exceptions);
		quantity = getCellDouble(row.getCell(quantityColIndex), valueLabel + " Quantity", exceptions);
//		try {
//			quantity = getCellDouble(row.getCell(quantityColIndex), valueLabel + " Quantity", null);
//			if (unit != null && quantity == null) exceptions.add(new InvalidCellValueException("Missing " + valueLabel + " Quantity", row, quantityColIndex));
//		} catch(InvalidCellValueException ex) {
//			exceptions.add(ex);
//		}
		return new AmountParts(quantity, unit);
	}
	
	private void applyDepartureFromCells(Delivery delivery, Row row, int dayColIndex, int monthColIndex, int yearColIndex, List<Exception> exceptions) {
		DateParts dateParts = getDatePartsFromCells(row, dayColIndex, monthColIndex, yearColIndex, "Delivery Date", exceptions);
		delivery.setDepartureDay(dateParts.day);
		delivery.setDepartureMonth(dateParts.month);
		delivery.setDepartureYear(dateParts.year);
	}
	
	private void applyArrivalFromCells(Delivery delivery, Row row, int dayColIndex, int monthColIndex, int yearColIndex, List<Exception> exceptions) {
		DateParts dateParts = getDatePartsFromCells(row, dayColIndex, monthColIndex, yearColIndex, "Delivery Arrival Date", exceptions);
		delivery.setArrivalDay(dateParts.day);
		delivery.setArrivalMonth(dateParts.month);
		delivery.setArrivalYear(dateParts.year);
	}
	
	private Delivery getForwardDelivery(List<Exception> exceptions, Sheet stationSheet, HashMap<String, Lot> lots, Row titleRow, Row row, boolean isNewFormat_151105) throws Exception {
		if (row == null) return null;
		Lot lot = null;
		{
			String lotString = getCellString(row.getCell(0), exceptions);
			if (lotString != null) lot = lots.get(lotString);
		}
		if (lot == null) return null;
		Delivery result = new Delivery();
		result.setLot(lot);
		
		applyDepartureFromCells(result, row, 1, 2, 3, exceptions);
		
		int startCol = 4;
		if (isNewFormat_151105) {
			applyArrivalFromCells(result, row, 4, 5, 6, exceptions);
			startCol = 7;
		}
		
//		{
//			Cell unitNumberCell = row.getCell(startCol);
//			Double unitNumber = getCellDouble(unitNumberCell, "Delivery Size Quantity", exceptions);
//			if (unitNumber != null) result.setUnitNumber(unitNumber);
//			// applyCellDouble(row.getCell(startCol), (x) -> result.setUnitNumber(x));
//		}
//		applyCellString(row.getCell(startCol+1), x -> result.setUnitUnit(x));
		{
			AmountParts amountParts = getAmountPartsFromCells(row, startCol, startCol + 1, "Delivery Size", exceptions);
			if (amountParts.quantity != null) result.setUnitNumber(amountParts.quantity);
			if (amountParts.unit != null) result.setUnitUnit(amountParts.unit);
		}
		
		{
//			String receiverId = getCellString(row.getCell(startCol+2));
//			if (receiverId != null) {
//				Station receiver = getStation(exceptions, stationSheet, receiverId, row);
//				// if (receiver == null) exceptions.add(new Exception("Recipient station '" + receiverId + "' not correctly defined / not known in Forward Tracing sheet"));
//				if (receiver == null) exceptions.add(new Exception("Recipient station '" + receiverId + "' not correctly defined / not known in Forward Tracing sheet"));
//				result.setReceiver(receiver);
//			}
//			else {
//				exceptions.add(new Exception("No Recipient Station defined in Forward Tracing sheet"));
//			}
			
			String receiverId = getRequiredCellString(row, startCol + 2, "Recipient Id", exceptions);
			if (receiverId != null) {
				Station receiver = getStation(exceptions, stationSheet, receiverId, row);
				// if (receiver == null) exceptions.add(new Exception("Recipient station '" + receiverId + "' not correctly defined / not known in Forward Tracing sheet"));
				if (receiver == null) exceptions.add(new InvalidCellValueException("Recipient station '" + receiverId + "' not correctly defined / not known", row, startCol + 2)); // Recipient station '" + receiverId + "' not correctly defined / not known in Forward Tracing sheet"));
				result.setReceiver(receiver);
			}
//			else {
//				exceptions.add(new Exception("No Recipient Station defined in Forward Tracing sheet"));
//			}
		}
		
		result.setId(getNewSerial(lot, result));
		result.setNewlyGeneratedID(true);
		
		// Further flexible cells

		addFlexibleFields(titleRow, row, startCol + 4, startCol + 21 - 1, result, exceptions);
		return result;
	}

//	private Delivery getMultiOutDelivery(List<Exception> exceptions, HashMap<String, Station> stations, Row titleRow, Row row, HashMap<String,String> definedLots,int rowNum, String filename, boolean ignoreMissingLotnumbers) throws Exception {
//		if (row == null) return null;
//		Delivery result = new Delivery();
//		
//		if (row.getRowNum() == 66) {
//			rowNum = rowNum + 0;
//		}
//
//		// ToDO: Verify why is id not required?
//		try {applyCellString(row.getCell(0), x -> result.setId(x));} catch(Exception ex) {}
//		
//		
//		Product p = new Product();
//		{
//			String stationId = getCellString(row.getCell(1), exceptions);
//			// ToDo: Verify: Why is no exception thrown?
//			if (stationId == null) return null;
//			Station station = stations.get(stationId);
//			if (station == null) exceptions.add(new Exception("Station ID in Deliveries not defined in stations sheet: '" + stationId + "'; -> Row " + (rowNum+1)));
//			p.setStation(station);
//		}
//		
//		applyCellString(row.getCell(2), x -> p.setName(x), exceptions);
//		Lot l = new Lot();
//		l.setProduct(p);
//		
//		{
//			String lotNo = getCellString(row.getCell(3));
//			if (lotNo != null) {l.setNumber(lotNo);}
//			// else if (!ignoreMissingLotnumbers) {exceptions.add(new Exception("Please, do always provide a lot number as this is most helpful! -> Row " + (rowNum+1) + " in '" + filename + "'\n"));}
//			else if (!ignoreMissingLotnumbers) {exceptions.add(new InvalidCellValueException("Please, do always provide a lot number as this is most helpful!", row));}
//		}
//		
////		{
////			Cell lotUnitNumberCell = row.getCell(4);
////			Double lotUnitNumber = getCellDouble(lotUnitNumberCell, "Lot Size Quantity", exceptions);
////			if (lotUnitNumber != null) l.setUnitNumber(lotUnitNumber);
////			// applyCellDouble(row.getCell(4), (x) -> l.setUnitNumber(x));
////			AmountParts amountParts = getAmountPartsFromCells(row, 4, 5, "Lot Size", exceptions);
////			
////			
////		}
////		applyCellString(row.getCell(5), (x) -> l.setUnitUnit(x));
//		
//		{
//			AmountParts amountParts = getAmountPartsFromCells(row, 4, 5, "Lot Size", exceptions);
//			l.setUnitNumber(amountParts.quantity);
//			l.setUnitUnit(amountParts.unit);
//		}
//		
//		String lotId = (p.getStation() == null) ? "_" + p.getName() + "_" + l.getNumber() : p.getStation().getId() + "_" + p.getName() + "_" + l.getNumber();
//		String lotInfo = l.getUnitNumber() + "_" + l.getUnitUnit();
//		if (definedLots.containsKey(lotId)) {
//			if (!definedLots.get(lotId).equals(lotInfo)) exceptions.add(new Exception("Lot has different quantities -> Lot number: '" + l.getNumber() + "'; -> Row " + (rowNum+1)));
//		}
//		else definedLots.put(lotId, lotInfo);
//
//		result.setLot(l);
//		
//		applyDepartureFromCells(result, row, 6, 7, 8, exceptions);
//		applyArrivalFromCells(result, row, 9, 10, 11, exceptions);
//		
//		{
//			AmountParts amountParts = getAmountPartsFromCells(row, 12, 13, "Delivery Size", exceptions);
//			
//			result.setUnitNumber(amountParts.quantity);
//			result.setUnitUnit(amountParts.unit);
//		}
//		// applyAmountParts(result, row, 12, 13, "unit", exceptions);
//		
//		//applyCellDouble(row.getCell(12), x -> result.setUnitNumber(x));
////		{
////			Cell unitNumberCell = row.getCell(12);
////			Double unitNumber = getCellDouble(unitNumberCell, "unit quantity", exceptions);
////			if (unitNumber != null) result.setUnitNumber(unitNumber);
////		}
////		applyCellString(row.getCell(13), (x) -> result.setUnitUnit(x));
//		
//		{
//			String receiverId = getCellString(row.getCell(14));
//			if (receiverId != null) {
//				Station receiver = stations.get(receiverId);
//				if (receiver == null) {
//					exceptions.add(new Exception("Recipient ID in sheet Deliveries not defined in stations sheet: '" + receiverId + "'; -> Row " + (rowNum+1)));
//				}
//				result.setReceiver(receiver);
//			}
//			else {
//				// ToDo: Verify: Why is the missing receiver not thrown?
//				if (result.getId() == null) return null;
//				else exceptions.add(new Exception("Recipient ID in sheet Deliveries not defined; -> Row " + (rowNum+1)));
//			}
//		}
//		
//		// Further flexible cells
//		UnaryOperator<String> normalizeStringFun = (String x) -> x.toLowerCase();
//		
//		Set<String> knownLotFields = Stream.of(new String[] {
//				"Production date",
//				"Best before date",
//				"Treatment of product during production",
//				"Sampling"
//		}).map(normalizeStringFun).collect(Collectors.toSet());
//				
//		for (int i=15;i<25;i++) {
//			String fieldKey = getCellString(titleRow.getCell(i));
//			if (fieldKey != null) {
//				String fieldValue = getCellString(row.getCell(i));
//				if (fieldValue != null) {
//					if (knownLotFields.contains(normalizeStringFun.apply(fieldKey))) l.addFlexibleField(fieldKey, fieldValue);
//					else result.addFlexibleField(fieldKey, fieldValue);
//				}
//			}
//		}
//		
//		return result;
//	}
	
	private Delivery getDelivery(List<Exception> exceptions, Sheet businessSheet, Station sif, Row row, boolean outbound, Row titleRow, String filename, boolean isForTracing, HashMap<String, Lot> outLots, HashMap<String, Delivery> existingDeliveries, boolean ignoreMissingLotnumbers, boolean isNewFormat_151105) throws Exception {
		Cell cell;
		if (sif == null) {
			sif = null;
		}
		if (isNewFormat_151105) {
			cell = row.getCell(0);
			if (SheetUtils.isCellEmpty(cell)) {
				Cell cell10 = row.getCell(10); 
				// ToDo: check change
				if (!isForTracing || !SheetUtils.isCellEmpty(cell10)) {
					// exceptions.add(new Exception("It is essential to choose the associated Lot number ('Lot Number of " + (isForTracing ? "" : " \"") + "Product" + (isForTracing ? "" : " Out\"") + "') to the delivery in Row number " + (classRowIndex + 1)));
					exceptions.add(new InvalidCellValueException("It is essential to choose the associated Lot number ('Lot Number of " + (isForTracing ? "" : " \"") + "Product" + (isForTracing ? "" : " Out\"") + "') to the delivery", row));
				}
				return null;
			}
			cell = row.getCell(12);
		}
		else {
			cell = row.getCell(12);
			if (SheetUtils.isCellEmpty(cell)) {
				Cell cell10 = row.getCell(10); 
				Cell cell0 = row.getCell(0); 
				// ToDo: check change
				if ((!isForTracing && !SheetUtils.isCellEmpty(cell0)) || !SheetUtils.isCellEmpty(cell10)) {
					// exceptions.add(new Exception("It is essential to choose the associated Lot number ('Lot Number of " + (isForTracing ? "" : " \"") + "Product" + (isForTracing ? "" : " Out\"") + "') to the delivery in Row number " + (classRowIndex + 1)));
					exceptions.add(new InvalidCellValueException("It is essential to choose the associated Lot number ('Lot Number of " + (isForTracing ? "" : " \"") + "Product" + (isForTracing ? "" : " Out\"") + "') to the delivery", row));
				}
				return null;
			}
		}
		
		Delivery delivery = new Delivery();
		String lotDelNumber = getCellString(cell, exceptions);
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
			String cellString = getCellString(row.getCell(0), exceptions);
			// ToDo: check (BfR_Format_Fortrace.xlsx) (first 2 columns are empty)
			// Why is this done here (in the code above the appropriate lot no is already checked)
			if (cellString != null) lotDelNumber = cellString;
			// if (lotDelNumber == null && !ignoreMissingLotnumbers) {exceptions.add(new Exception("Please, do always provide a lot number as this is most helpful! -> Row " + (row.getRowNum()+1) + " in '" + filename + "'\n"));}
			if (lotDelNumber == null && !ignoreMissingLotnumbers) {exceptions.add(new InvalidCellValueException("Please, do always provide a lot number as this is most helpful!", row));}
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
					lotNumber = getCellString(row.getCell(0), exceptions);
					// if (lotNumber == null && !ignoreMissingLotnumbers) {exceptions.add(new Exception("Please, do always provide a lot number as this is most helpful! -> Row " + (row.getRowNum()+1) + " in '" + filename + "'\n"));}
					if (lotNumber == null && !ignoreMissingLotnumbers) {exceptions.add(new InvalidCellValueException("Please, do always provide a lot number as this is most helpful!", row)); }
					l.setNumber(lotNumber);
					if (outLots.containsKey(lotNumber)) {
						l = outLots.get(lotNumber);
					}
				}				
				else {
					// ToDo: What is going on here?
					lotNumber = getCellString(row.getCell(0), exceptions);
					if (lotNumber != null) delivery.addTargetLotId(lotNumber);

					// applyCellString(row.getCell(1), (x) -> p.setName(x));
					p.setName(getCellString(row.getCell(1), exceptions));
					lotNumber = getCellString(row.getCell(2), exceptions);
//					if (lotNumber == null && !ignoreMissingLotnumbers) {exceptions.add(new Exception("Please, do always provide a lot number as this is most helpful! -> Row " + (row.getRowNum()+1) + " in '" + filename + "'\n"));}
					if (lotNumber == null && !ignoreMissingLotnumbers) {exceptions.add(new InvalidCellValueException("Please, do always provide a lot number as this is most helpful!", row));}
					l.setNumber(lotNumber);
					if (lotNumber == null && p.getName() == null) {
						// exceptions.add(new Exception("Lot number undefined in Row number " + (classRowIndex + 1)));
						exceptions.add(new InvalidCellValueException("Lot Number is missing", row));
					}
				}
			}
			else {
				// applyCellString(row.getCell(0), (x) -> p.setName(x));
				p.setName(getCellString(row.getCell(0), exceptions));
				lotNumber = getCellString(row.getCell(1), exceptions);
				// if (lotNumber == null && !ignoreMissingLotnumbers) {exceptions.add(new Exception("Please, do always provide a lot number as this is most helpful! -> Row " + (row.getRowNum()+1) + " in '" + filename + "'\n"));}
				if (lotNumber == null && !ignoreMissingLotnumbers) {exceptions.add(new InvalidCellValueException("Please, do always provide a lot number as this is most helpful!", row));}
				l.setNumber(lotNumber);
				if (lotNumber == null && p.getName() == null) {
					// exceptions.add(new Exception("Lot number and product name undefined in Row number " + (classRowIndex + 1)));
					exceptions.add(new InvalidCellValueException("Lot number and product name undefined", row));
				}
			}
		}
		
		delivery.setLot(l);
		if (!outbound) delivery.setReceiver(sif);
		int startCol = isNewFormat_151105 ? 3 : 2;
		
		applyDepartureFromCells(delivery, row, startCol, startCol + 1, startCol + 2, exceptions);
		applyArrivalFromCells(delivery, row, startCol + 3, startCol + 4, startCol+ 5, exceptions);
//		{
//			Cell unitNumberCell = row.getCell(startCol+6);
//			Double unitNumber = getCellDouble(unitNumberCell, "unit quantity", exceptions);
//			if (unitNumber != null) delivery.setUnitNumber(unitNumber);
//			// applyCellDouble(unitNumberCell, x -> delivery.setUnitNumber(x));
//		}
//		applyCellString(row.getCell(startCol+7), (x) -> delivery.setUnitUnit(x));
		{
			AmountParts amountParts = getAmountPartsFromCells(row, startCol + 6, startCol + 7, "Delivery Size", exceptions);
			if (amountParts.quantity != null) delivery.setUnitNumber(amountParts.quantity);
			if (amountParts.unit != null) delivery.setUnitUnit(amountParts.unit);
		}
					
		// ToDo: Please verify return of null, what happens here
		{
			String stationId = getCellString(row.getCell(startCol+8), exceptions);
			if (stationId == null) {
				return null;
			}
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
		addFlexibleFields(titleRow, row, 13, 19, delivery, exceptions);
		
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

	private static String getStr(String val) {
		if (val == null) return null;
		if (val.trim().isEmpty()) return null;
		return val.trim();
	}
	
	private static String trim(String text) {
		if (text == null) return null;
		text = text.trim();
		if (text.isEmpty()) return null;
		return text;
	}
	
	private boolean fillLot(List<Exception> exceptions, Row row, Station sif, HashMap<String, Lot> outLots, Row titleRow, HashMap<String, Delivery> outDeliveries, int rowIndex, boolean isNewFormat_151105) throws Exception {
		Lot lot = null;
	
		{
			String lotNumber = getCellString(row.getCell(0), exceptions);
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

//		{
//			Cell unitNumberCell = row.getCell(1);
//			Double unitNumber = getCellDouble(unitNumberCell, "lot size quantity", exceptions);
//			if (unitNumber != null) {
//				if (lot.getUnitNumber() == null) lot.setUnitNumber(unitNumber);
//				else if (lot.getUnitNumber().doubleValue() != unitNumber) {
//					exceptions.add(new Exception("Lot information defines same lot number with different quantities -> Row " + rowIndex));
//				}
//			}
//		}
//		
//		{
//			String unitUnit = getCellString(row.getCell(2));
//			if (unitUnit != null) {
//				if (lot.getUnitUnit() == null) lot.setUnitUnit(unitUnit);
//				else if (!lot.getUnitUnit().equals(unitUnit)) {
//					exceptions.add(new Exception("Lot information defines same lot number with different units -> Row " + rowIndex));
//				}
//			}
//		}
		
		{
			AmountParts amountParts = getAmountPartsFromCells(row, 1, 2, "Lot Size", exceptions);
			if (amountParts.quantity != null) {
				if (lot.getUnitNumber() == null) lot.setUnitNumber(amountParts.quantity);
				else if (lot.getUnitNumber().doubleValue() != amountParts.quantity.doubleValue()) {
					exceptions.add(new InvalidCellValueException("Lot information defines same lot number with different quantities -> Row " + rowIndex, row, 1));
				}
			}
			if (amountParts.unit != null) {
				if (lot.getUnitUnit() == null) lot.setUnitUnit(amountParts.unit);
				else if (!lot.getUnitUnit().equals(amountParts.unit)) {
					exceptions.add(new InvalidCellValueException("Lot information defines same lot number with different units -> Row " + rowIndex, row, 2));
				}
			}
		}
		
		if (isNewFormat_151105 || outDeliveries != null) {

			String productName = getCellString(row.getCell(3), exceptions);
			if (productName != null) {
				if (lot.getProduct() == null) {
					Product p = new Product(); p.setName(productName); lot.setProduct(p); p.setStation(sif);
				}
				else if (lot.getProduct().getName() == null) {
					lot.getProduct().setName(productName);
				}
				else if (!lot.getProduct().getName().equals(productName)) {
					// exceptions.add(new Exception("Lot information defines same lot number with different product names -> Row " + rowIndex));
					exceptions.add(new InvalidCellValueException("Lot information defines same lot number with different product names -> Row " + rowIndex, row.getSheet()));
				}
			}
			if (!isNewFormat_151105) {
				String deliveryId = getCellString(row.getCell(4), exceptions);
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
		addFlexibleFields(titleRow, row, 12, 19, lot, exceptions);
		return true;
	}
	
	private LinkedHashMap<String, String> getChargenLinks(Sheet sheet, int colIndex, int rowStartIndex, int rowEndIndex) throws Exception {
		LinkedHashMap<String, String> links = new LinkedHashMap<>();
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
	
	private static String getFormatedCellAddressesString(String[] addresses) {
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
		LinkedHashMap<String, String> chargenLinksToCellAddressMap = getChargenLinks(sheet, colIndex, rowStartIndex, rowEndIndex);
		
		Set<String> chargenLinks = chargenLinksToCellAddressMap.keySet();

		LinkedHashSet<String> ambiguousLinks = new LinkedHashSet<>();
		chargenLinks.stream()
			.filter(ref -> 
				lotNoToLotIdMap.containsKey(ref) && 
				rowNoToReferableDeliveryMap.containsKey(ref) && 
				!ref.equals(rowNoToReferableDeliveryMap.get(ref).getLot().getNumber())
			).forEach(ref -> ambiguousLinks.add(ref));
				
		if (!ambiguousLinks.isEmpty()) {
			long matchCount = chargenLinks.stream().filter(ref -> lotNoToLotIdMap.containsKey(ref) || rowNoToReferableDeliveryMap.containsKey(ref)).count();
			long lotNoMatchCount = chargenLinks.stream().filter(ref -> lotNoToLotIdMap.containsKey(ref)).count();
			long lineNoMatchCount = chargenLinks.stream().filter(ref -> rowNoToReferableDeliveryMap.containsKey(ref)).count();
			
			
			String[] options = {
				"Lot Numbers", //+ XlsLot.NUMBER(lang),
                "Line Numbers", 
                "Cancel"
	        };
			
			String[] cellAddresses = ambiguousLinks.stream().map(ref -> chargenLinksToCellAddressMap.get(ref)).toArray(String[]::new);
			String formatedCellAddressesString = getFormatedCellAddressesString(cellAddresses);
			long unambiguousLotNoMatchCount = lotNoMatchCount - ambiguousLinks.size();
			long unambiguousRowNoMatchCount = lineNoMatchCount - ambiguousLinks.size();
			long unambiguousMatchCount = matchCount - ambiguousLinks.size();
			
			String msg = "<html>" + 
					"Sheet '" + StringEscapeUtils.escapeHtml4(sheet.getSheetName()) + "' " +
					"in file '" + StringEscapeUtils.escapeHtml4(new File(filepath).getName()) + "'<br>" + 
					"contains " + ambiguousLinks.size() + " ambiguous lot reference(s) in cell(s) " + formatedCellAddressesString + ".<br><br>" + 
					unambiguousMatchCount + " lot reference(s) can be clearly assigned:<br>" +
					unambiguousLotNoMatchCount + " match lot numbers.<br>" +
					unambiguousRowNoMatchCount + " match line numbers.<br><br>" +
					"Please have a look at this file in your spreadsheet software.<br>" + 
					"Are the ambiguous references referring to lot numbers or to line numbers?" +
					"</html>";
			
			int answer = EdtUtils.askQuestionInEdt(
				DBKernel.mainFrame,
				msg,
				"Choose lot reference type",
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
		
//		PerformanceUtils.packageName = "de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat";
//		PerformanceUtils.reset();
//		PerformanceUtils.start(PerformanceUtils.getMethodPath());
		// PerformanceUtils.clearEnterings();
		
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
			fileRelatedWarns = new LinkedHashMap<>();
			
			// addWarning("Some test warning", "CellXY"); // for test purposes
								
			List<Exception> exceptions = importWorkbook(wb, filename, owner, taskMonitor);
//			if (exceptions == null) exceptions = new ArrayList<>(); // for temp test
//			if (exceptions.size() == 0) exceptions.add(new Exception("Dummy Error.")); // for temp test
			
			if (exceptions != null && exceptions.size() > 0) {
				importResult = false;
				logMessages += ReportUtils.exceptions2Html(exceptions, filename);
			}
			else {
				importResult = true;
			}
			if (existsDBKernel()) {
				warns.putAll(de.bund.bfr.knime.openkrise.common.DeliveryUtils.getWarnings(DBKernel.getDBConnection()));
			}
			else if (mydbi != null) {
				warns.putAll(de.bund.bfr.knime.openkrise.common.DeliveryUtils.getWarnings(mydbi.getConn()));
			}
			doWarns(filename);
			
		} catch (OutOfMemoryError | UserCancelException e) {
//			if (e instanceof UserCancelException) {
//				PerformanceUtils.stop(PerformanceUtils.getMethodPath());
//				PerformanceUtils.print();
//			}
			throw e;
//		} catch (PerformanceUtils.StopError e) {
//			importResult = false;
		} catch (Exception e) {
			importResult = false;
			// ToDo: remove
			e.printStackTrace();
			logMessages += "<h1 id=\"error\">'" + filename + "'</h1><ul><li>" + e.getMessage() + "</li></ul>";
			MyLogger.handleException(e);
			
		}
		finally {
			Delivery.reset();
			// PerformanceUtils.printEnterings();
//			PerformanceUtils.stop(PerformanceUtils.getMethodPath());
//			PerformanceUtils.print();
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
		String html = ReportUtils.warns2Html(filename, fileRelatedWarns, warns, warnsBeforeImport);
		if (html != null && !html.isEmpty()) logWarnings += html;
//		if (warns.size() > 0) {
//			String newFileLogs = "";
//			if (filename != null) {
//				newFileLogs += "<h1 id=\"warning\">Warnings for import file '" + filename + "'<h1>";
//				
//			}
//			for (String key : warns.keySet()) {
//				String newLogs = "<h2>" + key + "</h2>";
//				Set<String> oldWarns = warnsBeforeImport.get(key);
//				
//				if (warns.get(key) != null && !warns.get(key).isEmpty()) {
//					newLogs += "<ul>";
//					for (String w : warns.get(key)) {
//						if (oldWarns == null || !oldWarns.contains(w)) {
//							if (logWarnings.indexOf("<li>" + w + "</li>") < 0) newLogs += "<li>" + w + "</li>";							
//						}
//					}
//					newLogs += "</ul>";
//				}
//				if (newLogs.length() > ("<h2>" + key + "</h2><ul></ul>").length()) {
//					newFileLogs += newLogs;					
//				}
//			}		
//			if (newFileLogs.length() > ("<h1 id=\"warning\">Warnings for import file '" + filename + "'<h1>").length()) {
//				logWarnings += newFileLogs;
//			}
//		}
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
