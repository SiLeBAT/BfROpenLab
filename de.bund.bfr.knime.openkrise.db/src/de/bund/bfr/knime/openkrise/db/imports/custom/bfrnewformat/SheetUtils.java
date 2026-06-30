package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class SheetUtils {
	
	static class InvalidCellValueException extends Exception {
		private static final long serialVersionUID = -991760986495292191L;
		public String address;
		public Integer rowNum;
		public String sheet;

		InvalidCellValueException(String msg, Cell cell) {
			this(msg, cell.getAddress().toString(), cell.getSheet().getSheetName(), cell.getRow().getRowNum() + 1);
		}
		
		InvalidCellValueException(String msg, Row row) {
			this(msg, null, row.getSheet().getSheetName(), row.getRowNum() + 1);
		}
		
		InvalidCellValueException(String msg, Sheet sheet) {
			this(msg, null, sheet.getSheetName(), null);
		}
		
		InvalidCellValueException(String msg, Sheet sheet, int rowIndex) {
			this(msg, null, sheet.getSheetName(), rowIndex + 1);
		}
		
		InvalidCellValueException(String msg, Row row, int columnIndex) {
			this(msg, getCellAddress(columnIndex, row.getRowNum()), row.getSheet().getSheetName(), row.getRowNum() + 1);
		}
		
		InvalidCellValueException(String msg, String address, String sheet, Integer rowNum) {
			super(msg);
			this.address = address;
			this.sheet = sheet;
			this.rowNum = rowNum;
		}
	}
	
	static String convertIndexToLetterCode(int zeroBasedIndex) {
		final int LETTER_ALPHABET_LENGTH = 26;
		final int LETTER_ALPHABET_START_CODE = 65;
		
		int remainingIndex = zeroBasedIndex + 1;
	    List<Integer> codes = Arrays.asList(new Integer[] {(remainingIndex - 1) % LETTER_ALPHABET_LENGTH});
	    remainingIndex = (remainingIndex - codes.get(0) - 1) / LETTER_ALPHABET_LENGTH;
	    while (remainingIndex > 0) {
	        codes.add((remainingIndex - 1) % LETTER_ALPHABET_LENGTH);
	        remainingIndex =
	            (remainingIndex - codes.get(codes.size() - 1) - 1) /
	            LETTER_ALPHABET_LENGTH;
	    }
	    
	    List<Character> chars = codes.stream().map(code -> (char)(code + LETTER_ALPHABET_START_CODE)).toList();
	    Collections.reverse(chars);
	    return chars.stream().map(String::valueOf).collect(Collectors.joining());
	}
	
	static String getCellAddress(int columnIndex, int rowIndex) {
		return convertIndexToLetterCode(columnIndex) + (rowIndex + 1);
	}
	
	static String getColumnAddress(int zeroBasedColumnIndex) {
		return convertIndexToLetterCode(zeroBasedColumnIndex);
	}
	
	static CellType getEffectiveCellType(Cell cell) throws InvalidCellValueException {
		if (cell == null) return null;
		
		CellType cellType = cell.getCellType();
		
		if (cellType == CellType.FORMULA) cellType = cell.getCachedFormulaResultType();
		if (cellType == CellType.ERROR) throwCellErrorException(cell);
		
		return cellType;
	}
	
	static void throwCellErrorException(Cell cell) throws InvalidCellValueException {
		throw new InvalidCellValueException("Cell Error", cell);
	}
	
	static boolean isCellEmpty(Cell cell) {
		if (cell == null) return true;
		CellType cellType = cell.getCellType();
		
		if (cellType == CellType.FORMULA) cellType = cell.getCachedFormulaResultType();
		if (cellType == CellType.BLANK) return true;
		
		if (cellType == CellType.STRING) {
			String cellString = cell.getStringCellValue();
			return cellString == null || cellString.isBlank();
		}
		
		return false;
	}
}
