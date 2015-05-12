package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
	private String logWarnings = "";
	
	public String getLogWarnings() {
		return logWarnings;
	}
	private int classRowIndex = -1;
	
	public String getLogMessages() {
		return logMessages;
	}
	private void checkStationsFirst(Sheet businessSheet) throws Exception {
		HashSet<String> stationIDs = new HashSet<>();
		int numRows = businessSheet.getLastRowNum() + 1;
		for (int i=1;i<numRows;i++) {
			Row row = businessSheet.getRow(i);
			if (row != null) {
				Cell cell = row.getCell(0); // ID
				Cell cell1 = row.getCell(1); // Name
				if ((cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) && (cell1 == null || cell1.getCellType() == Cell.CELL_TYPE_BLANK)) return;
				if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) throw new Exception("Station has no ID??? -> Row " + (i+1));
				cell.setCellType(Cell.CELL_TYPE_STRING);
				String val = cell.getStringCellValue();
				if (stationIDs.contains(val)) throw new Exception("Station ID '" + val + "' is defined more than once -> Row " + (i+1));
				stationIDs.add(val);
			}
		}
	}
	private void checkDeliveriesFirst(Sheet deliverySheet) throws Exception {
		HashSet<String> deliveryIDs = new HashSet<>();
		int numRows = deliverySheet.getLastRowNum() + 1;
		for (int i=2;i<numRows;i++) {
			Row row = deliverySheet.getRow(i);
			if (row != null) {
				Cell cell = row.getCell(0); // ID
				Cell cell1 = row.getCell(1); // Station
				if ((cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) && (cell1 == null || cell1.getCellType() == Cell.CELL_TYPE_BLANK)) return;
				if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) throw new Exception("Delivery has no ID??? -> Row " + (i+1));
				cell.setCellType(Cell.CELL_TYPE_STRING);
				String val = cell.getStringCellValue();
				if (deliveryIDs.contains(val)) throw new Exception("Delivery ID '" + val + "' is defined more than once -> Row " + (i+1));
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
		lu.intoDb();
	}
	public boolean doTheImport(Workbook wb, String filename) throws Exception {
		Sheet stationSheet = wb.getSheet("Stations");
		Sheet deliverySheet = wb.getSheet("Deliveries");
		Sheet d2dSheet = wb.getSheet("Deliveries2Deliveries");
		Sheet transactionSheet = wb.getSheet("BackTracing");
		Sheet lookupSheet = wb.getSheet("LookUp");
		
		if (stationSheet == null) return false;
		if (transactionSheet == null && (deliverySheet == null || d2dSheet == null)) return false;

		checkStationsFirst(stationSheet);
			
		if (d2dSheet != null && deliverySheet != null) {
			checkDeliveriesFirst(deliverySheet);
			// load all Stations
			HashMap<String, Station> stations = new HashMap<>();
			int numRows = stationSheet.getLastRowNum() + 1;
			Row titleRow = stationSheet.getRow(0);
			for (classRowIndex=1;classRowIndex<numRows;classRowIndex++) {
				Station s = getStation(titleRow, stationSheet.getRow(classRowIndex));
				if (s == null) break;
				if (stations.containsKey(s.getId())) throw new Exception("Station defined twice??? -> Row " + (classRowIndex+1) + "; Station Id: '" + s.getId() + "'");
				stations.put(s.getId(), s);
			}
			// load all Deliveries
			HashMap<String, Delivery> deliveries = new HashMap<>();
			numRows = deliverySheet.getLastRowNum() + 1;
			titleRow = deliverySheet.getRow(0);
			HashMap<String,String> definedLots = new HashMap<>();
			for (classRowIndex=2;classRowIndex<numRows;classRowIndex++) {
				Delivery d = getMultiOutDelivery(stations, titleRow, deliverySheet.getRow(classRowIndex), definedLots, classRowIndex, filename);
				if (d == null) break;
				if (deliveries.containsKey(d.getId())) throw new Exception("Delivery defined twice??? -> Row " + (classRowIndex+1) + "; Delivery Id: '" + d.getId() + "'");
				deliveries.put(d.getId(), d);
			}
			
			// load Recipes
			HashSet<D2D> recipes = new HashSet<>();
			numRows = d2dSheet.getLastRowNum() + 1;
			titleRow = d2dSheet.getRow(0);
			for (classRowIndex=1;classRowIndex<numRows;classRowIndex++) {
				D2D dl = getD2D(deliveries, titleRow, d2dSheet.getRow(classRowIndex), classRowIndex);
				if (dl == null) break;
				recipes.add(dl);
			}

			MetaInfo mi = new MetaInfo();
			mi.setFilename(filename);

			DBKernel.sendRequest("SET AUTOCOMMIT FALSE", false);
			if (lookupSheet != null) loadLookupSheet(lookupSheet);
			Integer miDbId = mi.getID();
			if (miDbId == null) throw new Exception("File already imported");
			for (Delivery d : deliveries.values()) {
				d.getID(miDbId, false);
				if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
				//if (!d.getLogWarnings().isEmpty()) logWarnings += d.getLogWarnings() + "\n";
			}
			
			HashMap<Delivery, HashSet<Integer>> ingredients = new HashMap<>(); 
			for (D2D dl : recipes) {
				dl.getId(miDbId);
				
				// collect data for checks if data is missing...
				Delivery d = dl.getTargetDelivery();
				if (!ingredients.containsKey(d)) ingredients.put(d, new HashSet<Integer>());
				HashSet<Integer> hd = ingredients.get(d);
				hd.add(dl.getIngredient().getDbId());
			}

			DBKernel.sendRequest("COMMIT", false);
			DBKernel.sendRequest("SET AUTOCOMMIT TRUE", false);
			/*
			HashMap<Integer, String> hm = new HashMap<>(); 
			for (Delivery d : deliveries.values()) { // ingredients.keySet()
				HashSet<Delivery> ingredsDB = D2D.getIngredients(d.getLot().getDbId());
				HashSet<Integer> ingredsExcel; 
				if (ingredients.containsKey(d)) ingredsExcel = ingredients.get(d);
				else ingredsExcel = new HashSet<Integer>(); 
				for (Delivery dId : ingredsDB) {
					if (!ingredsExcel.contains(dId.getDbId().intValue())) {
						if (!hm.containsKey(d.getLot().getDbId())) {
							String w = "All ingredients of lot number '" + d.getLot().getNumber() + "' of product '" + (d.getLot().getProduct() == null ? "..." : d.getLot().getProduct().getName()) + "':\n";
							for (Delivery dId2 : ingredsDB) {
								w += "Station: '" + dId2.getComment() + "' ,Product: '" + dId2.getId() + "' , Lot: '" + dId2.getTargetLotId() + "'\n";
							}
							w = w.substring(0, w.length() - 1) + "\nMissing delivery2delivery:\n";
							hm.put(d.getLot().getDbId(), w);
						}
						hm.put(d.getLot().getDbId(), hm.get(d.getLot().getDbId()) + "'" + dId.getUnitUnit() + "' -> '" + d.getId() + "'\n");
/*
						logWarnings += "There is an ingredient (" + (dId.getId() == null ? "?" : dId.getId()) + ", Lot: " + dId.getTargetLotId() + ") undefined for lot '" + d.getLot().getNumber() + "', which is defined for that lot elsewhere...\n"+
								"Maybe it is a typo in lot typing? Or did you forget to define that ingredient?\n"+
								"Anyway, we assumed to integrate all ingredients to that lot.\n"+
								"If that is different from what you intended, please correct the Excel sheet, reset database and try to import your data again!\n"+
								"-> Delivery ID: '" + d.getId() + "' in '" + filename + "'\n\n";
								*//*
					}
				}
			}
			if (hm.size() > 0) {
				logWarnings += "In '" + filename + "':\n";
				for (Integer ln : hm.keySet()) {
					logWarnings += hm.get(ln) + "\n";
				}
			}
*/
			return true;
		}
		
		Row row = transactionSheet.getRow(0);
		
		// Station in focus
		Cell cell = row.getCell(1);
		if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) throw new Exception("Station in Focus not defined");
		cell.setCellType(Cell.CELL_TYPE_STRING);
		Station sif = getStation(stationSheet, cell.getStringCellValue(), row);
		
		// Delivery(s) Outbound
		classRowIndex = 5;
		HashMap<String, Delivery> outDeliveries = new HashMap<>(); 
		HashMap<String, Lot> outLots = new HashMap<>(); 
		Row titleRow = transactionSheet.getRow(classRowIndex - 2);
		for (;;classRowIndex++) {
			row = transactionSheet.getRow(classRowIndex);
			if (row == null) continue;
			if (isBlockEnd(row, 13, "Reporter Information")) break;
			Delivery d = getDelivery(stationSheet, sif, row, true, titleRow, filename);
			if (d == null) continue;
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
			Delivery d = getDelivery(stationSheet, sif, row, false, titleRow, filename);
			if (d == null) continue;
			if (d.getTargetLotId() == null) throw new Exception("Lot number unknown in Row number " + (classRowIndex + 1));
			inDeliveries.add(d);
		}
		
		// Opt_ForwardTracing
		Sheet forwardSheet = wb.getSheet("Opt_ForwardTracing");
		HashSet<Delivery> forwDeliveries = new HashSet<>(); 
		numRows = forwardSheet.getLastRowNum() + 1;
		titleRow = forwardSheet.getRow(0);
		for (classRowIndex=2;classRowIndex < numRows;classRowIndex++) {
			row = transactionSheet.getRow(classRowIndex);
			if (row == null) continue;
			Delivery d = getForwardDelivery(stationSheet, outLots, titleRow, forwardSheet.getRow(classRowIndex));
			if (d == null) continue;
			forwDeliveries.add(d);
		}
		
		// what are the insertRessources (new BfR-Format -> then SupplyChain-Reader has to look for other IDcolumns, i.e. Serial)
		// forward tracing importieren
		// welche Reihenfolge to insert? Was, wenn beim Import was schiefgeht?
		DBKernel.sendRequest("SET AUTOCOMMIT FALSE", false);
		if (lookupSheet != null) loadLookupSheet(lookupSheet);
		Integer miDbId = mi.getID();
		if (miDbId == null) throw new Exception("File already imported");
		insertIntoDb(miDbId, inDeliveries, outDeliveries, forwDeliveries);
		DBKernel.sendRequest("COMMIT", false);
		DBKernel.sendRequest("SET AUTOCOMMIT TRUE", false);
		
		return true;
	}
	private void insertIntoDb(Integer miDbId, HashSet<Delivery> inDeliveries, HashMap<String, Delivery> outDeliveries, HashSet<Delivery> forwDeliveries) throws Exception {
		HashMap<String, Integer> lotDbNumber = new HashMap<>();
		for (Delivery d : outDeliveries.values()) {
			d.getID(miDbId, true);
			if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
			//if (!d.getLogWarnings().isEmpty()) logWarnings += d.getLogWarnings() + "\n";
			if (lotDbNumber.containsKey(d.getLot().getNumber()) && lotDbNumber.get(d.getLot().getNumber()).intValue() != d.getLot().getDbId()) throw new Exception("Lot Numbers of different lots are the same in 'Products Out'!");
			else lotDbNumber.put(d.getLot().getNumber(), d.getLot().getDbId());
		}
		for (Delivery d : inDeliveries) {
			Integer dbId = d.getID(miDbId, false);
			if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
			if (!d.getLogWarnings().isEmpty()) logWarnings += d.getLogWarnings() + "\n";
			if (d.getTargetLotId() != null && lotDbNumber.containsKey(d.getTargetLotId())) {
				new D2D().getId(dbId, lotDbNumber.get(d.getTargetLotId()), miDbId);
			}
		}
		for (Delivery d : forwDeliveries) {
			d.getID(miDbId, false);
			if (!d.getLogMessages().isEmpty()) logMessages += d.getLogMessages() + "\n";
			//if (!d.getLogWarnings().isEmpty()) logWarnings += d.getLogWarnings() + "\n";
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
	private Station getStation(Sheet businessSheet, String lookup, Row srcrow) throws Exception {
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
		if (result == null) throw new Exception("Station '" + lookup + "' is not correctly defined in Row " + (srcrow.getRowNum() + 1));
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
	private D2D getD2D(HashMap<String, Delivery> deliveries, Row titleRow, Row row, int rowNum) throws Exception {
		if (row == null) return null;
		D2D result = new D2D();
		Cell cell = row.getCell(0);
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING);
			String did = getStr(cell.getStringCellValue());
			Delivery d = deliveries.get(did);
			if (d == null) throw new Exception("Delivery ID in sheet Deliveries2Deliveries not defined in deliveries sheet: '" + did + "'; -> Row " + (rowNum+1));
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
			if (d == null) throw new Exception("Delivery ID in sheet Deliveries2Deliveries not defined in deliveries sheet: '" + did + "'; -> Row " + (rowNum+1));
			result.setTargetDelivery(d);
		}
		else {
			return null;
		}
		if (!result.getIngredient().getReceiver().getId().equals(result.getTargetDelivery().getLot().getProduct().getStation().getId())) {
			throw new Exception("Recipient does not match Supplier; in sheet Deliveries2Deliveries: '" + result.getIngredient().getId() + "' -> '" + result.getTargetDelivery().getId() + "'; -> Row " + (rowNum+1));
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
	private Delivery getForwardDelivery(Sheet stationSheet, HashMap<String, Lot> lots, Row titleRow, Row row) throws Exception {
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
			Station s = getStation(stationSheet, ss, row);
			if (s == null) throw new Exception("Recipient station '" + ss + "' not correclty defined / not known in Forward Tracing sheet");
			result.setReceiver(s);
		}
		else {
			throw new Exception("No Recipient Station defined in Forward Tracing sheet");
		}
		
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
	private Delivery getMultiOutDelivery(HashMap<String, Station> stations, Row titleRow, Row row, HashMap<String,String> definedLots,int rowNum, String filename) throws Exception {
		if (row == null) return null;
		Delivery result = new Delivery();
		Cell cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setId(getStr(cell.getStringCellValue()));}
		Product p = new Product();
		cell = row.getCell(1);
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING); 
			String sid = getStr(cell.getStringCellValue());
			Station s = stations.get(sid);
			if (s == null) throw new Exception("Station ID in Deliveries not defined in stations sheet: '" + sid + "'; -> Row " + (rowNum+1));
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
		else {logWarnings += "Please, do always provide a lot number as this is most helpful! -> Row " + (rowNum+1) + " in '" + filename + "'\n\n";}
		cell = row.getCell(4); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setUnitNumber(getDbl(cell.getStringCellValue()));}
		cell = row.getCell(5); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setUnitUnit(getStr(cell.getStringCellValue()));}
		String lotId = p.getStation().getId() + "_" + p.getName() + "_" + l.getNumber();
		String lotInfo = l.getUnitNumber() + "_" + l.getUnitUnit();
		if (definedLots.containsKey(lotId)) {
			if (!definedLots.get(lotId).equals(lotInfo)) throw new Exception("Lot has different quantities??? -> Lot number: '" + l.getNumber() + "'; -> Row " + (rowNum+1));
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
			if (s == null) throw new Exception("Recipient ID in sheet Deliveries not defined in stations sheet: '" + sid + "'; -> Row " + (rowNum+1));
			result.setReceiver(s);
		}
		else {
			if (result.getId() == null) return null;
			else throw new Exception("Recipient ID in sheet Deliveries not defined; -> Row " + (rowNum+1));
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
	private Delivery getDelivery(Sheet businessSheet, Station sif, Row row, boolean outbound, Row titleRow, String filename) throws Exception {
		Product p = new Product();
		if (outbound) p.setStation(sif);
		Cell cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); p.setName(getStr(cell.getStringCellValue()));}
		Lot l = new Lot();
		l.setProduct(p);
		cell = row.getCell(1);
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setNumber(getStr(cell.getStringCellValue()));}
		else {logWarnings += "Please, do always provide a lot number as this is most helpful! -> Row " + (row.getRowNum()+1) + " in '" + filename + "'\n\n";}
		//cell = row.getCell(1); if (cell != null) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setNumber(getStr(cell.getStringCellValue()));}
		Delivery result = new Delivery();
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
		if (outbound && cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); result.setReceiver(getStation(businessSheet, cell.getStringCellValue(), row));}
		if (!outbound && cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); p.setStation(getStation(businessSheet, cell.getStringCellValue(), row));}
		cell = row.getCell(12);
		if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || (cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getStringCellValue().isEmpty())) return null;
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
	private String getStr(String val) {
		if (val.trim().isEmpty()) return null;
		return val;
	}
	private boolean fillLot(Row row, HashMap<String, Lot> outLots, Row titleRow) {
		Lot l = null;
		Cell cell = row.getCell(0); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l = outLots.get(getStr(cell.getStringCellValue()));}
		if (l == null) return false;
		cell = row.getCell(1); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setUnitNumber(getDbl(cell.getStringCellValue()));}
		cell = row.getCell(2); if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {cell.setCellType(Cell.CELL_TYPE_STRING); l.setUnitUnit(getStr(cell.getStringCellValue()));}
		
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

					doTheImport(wb, filename);

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
					is.close();
				} catch (Exception e) {
					DBKernel.sendRequest("ROLLBACK", false);
					DBKernel.sendRequest("SET AUTOCOMMIT TRUE", false);
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
		return "Neue BfR-Lieferketten Datei (*.xlsx)";
	}
}
