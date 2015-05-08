/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.xssf.usermodel;

import java.util.List;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationName;
import org.apache.poi.ss.formula.EvaluationSheet;
import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.SheetIdentifier;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.ptg.Area3DPxg;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.NameXPtg;
import org.apache.poi.ss.formula.ptg.NameXPxg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPxg;
import org.apache.poi.ss.formula.udf.IndexedUDFFinder;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.model.ExternalLinksTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDefinedName;

/**
 * Internal POI use only
 */
public final class XSSFEvaluationWorkbook implements FormulaRenderingWorkbook, EvaluationWorkbook, FormulaParsingWorkbook {
	private final XSSFWorkbook _uBook;

	public static XSSFEvaluationWorkbook create(XSSFWorkbook book) {
		if (book == null) {
			return null;
		}
		return new XSSFEvaluationWorkbook(book);
	}

	private XSSFEvaluationWorkbook(XSSFWorkbook book) {
		_uBook = book;
	}

	private int convertFromExternalSheetIndex(int externSheetIndex) {
		return externSheetIndex;
	}
	/**
	 * XSSF doesn't use external sheet indexes, so when asked treat
	 * it just as a local index
	 */
	public int convertFromExternSheetIndex(int externSheetIndex) {
		return externSheetIndex;
	}
	/**
	 * @return  the external sheet index of the sheet with the given internal
	 * index. Used by some of the more obscure formula and named range things.
	 * Fairly easy on XSSF (we think...) since the internal and external
	 * indices are the same
	 */
	private int convertToExternalSheetIndex(int sheetIndex) {
		return sheetIndex;
	}

    public int getExternalSheetIndex(String sheetName) {
        int sheetIndex = _uBook.getSheetIndex(sheetName);
        return convertToExternalSheetIndex(sheetIndex);
    }

    private int resolveBookIndex(String bookName) {
        // Strip the [] wrapper, if still present
        if (bookName.startsWith("[") && bookName.endsWith("]")) {
            bookName = bookName.substring(1, bookName.length()-2);
        }

        // Is it already in numeric form?
        try {
            return Integer.parseInt(bookName);
        } catch (NumberFormatException e) {}

        // Look up an External Link Table for this name
        List<ExternalLinksTable> tables = _uBook.getExternalLinksTable();
        int index = findExternalLinkIndex(bookName, tables);
        if (index != -1) return index;
        
        // Is it an absolute file reference?
        if (bookName.startsWith("'file:///") && bookName.endsWith("'")) {
            String relBookName = bookName.substring(bookName.lastIndexOf('/')+1);
            relBookName = relBookName.substring(0, relBookName.length()-1); // Trailing '
            
            // Try with this name
            index = findExternalLinkIndex(relBookName, tables);
            if (index != -1) return index;
            
            // If we get here, it's got no associated proper links yet
            // So, add the missing reference and return
            // Note - this is really rather nasty...
            ExternalLinksTable fakeLinkTable = new FakeExternalLinksTable(relBookName);
            tables.add(fakeLinkTable);
            return tables.size(); // 1 based results, 0 = current workbook
        }
        
        // Not properly referenced
        throw new RuntimeException("Book not linked for filename " + bookName);
    }
    private int findExternalLinkIndex(String bookName, List<ExternalLinksTable> tables) {
        for (int i=0; i<tables.size(); i++) {
            if (tables.get(i).getLinkedFileName().equals(bookName)) {
                return i+1; // 1 based results, 0 = current workbook
            }
        }
        return -1;
    }
    private static class FakeExternalLinksTable extends ExternalLinksTable {
        private final String fileName;
        private FakeExternalLinksTable(String fileName) {
            this.fileName = fileName;
        }
        public String getLinkedFileName() {
            return fileName;
        }
    }

	public EvaluationName getName(String name, int sheetIndex) {
		for (int i = 0; i < _uBook.getNumberOfNames(); i++) {
			XSSFName nm = _uBook.getNameAt(i);
			String nameText = nm.getNameName();
			int nameSheetindex = nm.getSheetIndex();
			if (name.equalsIgnoreCase(nameText) && 
			       (nameSheetindex == -1 || nameSheetindex == sheetIndex)) {
				return new Name(_uBook.getNameAt(i), i, this);
			}
		}
		return sheetIndex == -1 ? null : getName(name, -1);
	}

	public int getSheetIndex(EvaluationSheet evalSheet) {
		XSSFSheet sheet = ((XSSFEvaluationSheet)evalSheet).getXSSFSheet();
		return _uBook.getSheetIndex(sheet);
	}

	public String getSheetName(int sheetIndex) {
		return _uBook.getSheetName(sheetIndex);
	}
	
	public ExternalName getExternalName(int externSheetIndex, int externNameIndex) {
        throw new IllegalStateException("HSSF-style external references are not supported for XSSF");
	}

	public ExternalName getExternalName(String nameName, String sheetName, int externalWorkbookNumber) {
        if (externalWorkbookNumber > 0) {
            // External reference - reference is 1 based, link table is 0 based
            int linkNumber = externalWorkbookNumber - 1;
            ExternalLinksTable linkTable = _uBook.getExternalLinksTable().get(linkNumber);
            
            for (org.apache.poi.ss.usermodel.Name name : linkTable.getDefinedNames()) {
                if (name.getNameName().equals(nameName)) {
                    // HSSF returns one sheet higher than normal, and various bits
                    //  of the code assume that. So, make us match that behaviour!
                    int nameSheetIndex = name.getSheetIndex() + 1;
                    
                    // TODO Return a more specialised form of this, see bug #56752
                    // Should include the cached values, for in case that book isn't available
                    // Should support XSSF stuff lookups
                    return new ExternalName(nameName, -1, nameSheetIndex);
                }
            }
            throw new IllegalArgumentException("Name '"+nameName+"' not found in " +
                                               "reference to " + linkTable.getLinkedFileName());
        } else {
            // Internal reference
            int nameIdx = _uBook.getNameIndex(nameName);
            return new ExternalName(nameName, nameIdx, 0);  // TODO Is this right?
        }
	    
    }

    public NameXPxg getNameXPtg(String name, SheetIdentifier sheet) {
	    // First, try to find it as a User Defined Function
        IndexedUDFFinder udfFinder = (IndexedUDFFinder)getUDFFinder();
        FreeRefFunction func = udfFinder.findFunction(name);
        if (func != null) {
            return new NameXPxg(null, name);
        }
        
        // Otherwise, try it as a named range
        if (sheet == null) {
            if (_uBook.getNameIndex(name) > -1) {
                return new NameXPxg(null, name);
            }
            return null;
        }
        if (sheet._sheetIdentifier == null) {
            // Workbook + Named Range only
            int bookIndex = resolveBookIndex(sheet._bookName);
            return new NameXPxg(bookIndex, null, name);
        }

        // Use the sheetname and process
        String sheetName = sheet._sheetIdentifier.getName();
        
        if (sheet._bookName != null) {
            int bookIndex = resolveBookIndex(sheet._bookName);
            return new NameXPxg(bookIndex, sheetName, name);
        } else {
            return new NameXPxg(sheetName, name);
        }
	}
    public Ptg get3DReferencePtg(CellReference cell, SheetIdentifier sheet) {
        if (sheet._bookName != null) {
            int bookIndex = resolveBookIndex(sheet._bookName);
            return new Ref3DPxg(bookIndex, sheet, cell);
        } else {
            return new Ref3DPxg(sheet, cell);
        }
    }
    public Ptg get3DReferencePtg(AreaReference area, SheetIdentifier sheet) {
        if (sheet._bookName != null) {
            int bookIndex = resolveBookIndex(sheet._bookName);
            return new Area3DPxg(bookIndex, sheet, area);
        } else {
            return new Area3DPxg(sheet, area);
        }
    }

    public String resolveNameXText(NameXPtg n) {
        int idx = n.getNameIndex();
        String name = null;
        
        // First, try to find it as a User Defined Function
        IndexedUDFFinder udfFinder = (IndexedUDFFinder)getUDFFinder();
        name = udfFinder.getFunctionName(idx);
        if (name != null) return name;
        
        // Otherwise, try it as a named range
        XSSFName xname = _uBook.getNameAt(idx);
        if (xname != null) {
            name = xname.getNameName();
        }
        
        return name;
    }

	public EvaluationSheet getSheet(int sheetIndex) {
		return new XSSFEvaluationSheet(_uBook.getSheetAt(sheetIndex));
	}

	public ExternalSheet getExternalSheet(int externSheetIndex) {
	    throw new IllegalStateException("HSSF-style external references are not supported for XSSF");
	}
	public ExternalSheet getExternalSheet(String firstSheetName, String lastSheetName, int externalWorkbookNumber) {
	    String workbookName;
	    if (externalWorkbookNumber > 0) {
	        // External reference - reference is 1 based, link table is 0 based
	        int linkNumber = externalWorkbookNumber - 1;
	        ExternalLinksTable linkTable = _uBook.getExternalLinksTable().get(linkNumber);
	        workbookName = linkTable.getLinkedFileName();
	    } else {
	        // Internal reference
	        workbookName = null;
	    }
	    
	    if (lastSheetName == null || firstSheetName.equals(lastSheetName)) {
	        return new ExternalSheet(workbookName, firstSheetName);
	    } else {
	        return new ExternalSheetRange(workbookName, firstSheetName, lastSheetName);
	    }
    }

    public int getExternalSheetIndex(String workbookName, String sheetName) {
		throw new RuntimeException("not implemented yet");
	}
	public int getSheetIndex(String sheetName) {
		return _uBook.getSheetIndex(sheetName);
	}

	public String getSheetFirstNameByExternSheet(int externSheetIndex) {
		int sheetIndex = convertFromExternalSheetIndex(externSheetIndex);
		return _uBook.getSheetName(sheetIndex);
	}
    public String getSheetLastNameByExternSheet(int externSheetIndex) {
        // XSSF does multi-sheet references differently, so this is the same as the first
        return getSheetFirstNameByExternSheet(externSheetIndex);
    }

	public String getNameText(NamePtg namePtg) {
		return _uBook.getNameAt(namePtg.getIndex()).getNameName();
	}
	public EvaluationName getName(NamePtg namePtg) {
		int ix = namePtg.getIndex();
		return new Name(_uBook.getNameAt(ix), ix, this);
	}
	public Ptg[] getFormulaTokens(EvaluationCell evalCell) {
		XSSFCell cell = ((XSSFEvaluationCell)evalCell).getXSSFCell();
		XSSFEvaluationWorkbook frBook = XSSFEvaluationWorkbook.create(_uBook);
		return FormulaParser.parse(cell.getCellFormula(), frBook, FormulaType.CELL, _uBook.getSheetIndex(cell.getSheet()));
	}
	
    public UDFFinder getUDFFinder(){
        return _uBook.getUDFFinder();
    }

	private static final class Name implements EvaluationName {

		private final XSSFName _nameRecord;
		private final int _index;
		private final FormulaParsingWorkbook _fpBook;

		public Name(XSSFName name, int index, FormulaParsingWorkbook fpBook) {
			_nameRecord = name;
			_index = index;
			_fpBook = fpBook;
		}

		public Ptg[] getNameDefinition() {

			return FormulaParser.parse(_nameRecord.getRefersToFormula(), _fpBook, FormulaType.NAMEDRANGE, _nameRecord.getSheetIndex());
		}

		public String getNameText() {
			return _nameRecord.getNameName();
		}

		public boolean hasFormula() {
			// TODO - no idea if this is right
			CTDefinedName ctn = _nameRecord.getCTName();
			String strVal = ctn.getStringValue();
			return !ctn.getFunction() && strVal != null && strVal.length() > 0;
		}

		public boolean isFunctionName() {
			return _nameRecord.isFunctionName();
		}

		public boolean isRange() {
			return hasFormula(); // TODO - is this right?
		}
		public NamePtg createPtg() {
			return new NamePtg(_index);
		}
	}

	public SpreadsheetVersion getSpreadsheetVersion(){
		return SpreadsheetVersion.EXCEL2007;
	}
}
