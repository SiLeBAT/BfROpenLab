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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.helpers.XSSFXmlColumnPr;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.TableDocument;

/**
 * 
 * This class implements the Table Part (Open Office XML Part 4:
 * chapter 3.5.1)
 * 
 * This implementation works under the assumption that a table contains mappings to a subtree of an XML.
 * The root element of this subtree an occur multiple times (one for each row of the table). The child nodes
 * of the root element can be only attributes or element with maxOccurs=1 property set
 * 
 *
 * @author Roberto Manicardi
 */
public class XSSFTable extends POIXMLDocumentPart {

	private CTTable ctTable;
	private List<XSSFXmlColumnPr> xmlColumnPr;
	private CellReference startCellReference;
	private CellReference endCellReference;	
	private String commonXPath; 
	
	
	public XSSFTable() {
		super();
		ctTable = CTTable.Factory.newInstance();

	}

	public XSSFTable(PackagePart part, PackageRelationship rel)
			throws IOException {
		super(part, rel);
		readFrom(part.getInputStream());
	}

	public void readFrom(InputStream is) throws IOException {
		try {
			TableDocument doc = TableDocument.Factory.parse(is);
			ctTable = doc.getTable();
		} catch (XmlException e) {
			throw new IOException(e.getLocalizedMessage());
		}
	}
	
	public XSSFSheet getXSSFSheet(){
		return (XSSFSheet) getParent();
	}

	public void writeTo(OutputStream out) throws IOException {
        updateHeaders();

        TableDocument doc = TableDocument.Factory.newInstance();
		doc.setTable(ctTable);
		doc.save(out, DEFAULT_XML_OPTIONS);
	}

	@Override
	protected void commit() throws IOException {
		PackagePart part = getPackagePart();
		OutputStream out = part.getOutputStream();
		writeTo(out);
		out.close();
	}
	
	public CTTable getCTTable(){
		return ctTable;
	}
	
	/**
	 * Checks if this Table element contains even a single mapping to the map identified by id
	 * @param id the XSSFMap ID
	 * @return true if the Table element contain mappings
	 */
	public boolean mapsTo(long id){
		boolean maps =false;
		
		List<XSSFXmlColumnPr> pointers = getXmlColumnPrs();
		
		for(XSSFXmlColumnPr pointer: pointers){
			if(pointer.getMapId()==id){
				maps=true;
				break;
			}
		}
		
		return maps;
	}

	
	/**
	 * 
	 * Calculates the xpath of the root element for the table. This will be the common part
	 * of all the mapping's xpaths
	 * 
	 * @return the xpath of the table's root element
	 */
    @SuppressWarnings("deprecation")
	public String getCommonXpath() {
		
		if(commonXPath == null){
		
		String[] commonTokens ={};
		
		for(CTTableColumn column :ctTable.getTableColumns().getTableColumnArray()){
			if(column.getXmlColumnPr()!=null){
				String xpath = column.getXmlColumnPr().getXpath();
				String[] tokens =  xpath.split("/");
				if(commonTokens.length==0){
					commonTokens = tokens;
					
				}else{
					int maxLenght = commonTokens.length>tokens.length? tokens.length:commonTokens.length;
					for(int i =0; i<maxLenght;i++){
						if(!commonTokens[i].equals(tokens[i])){
						 List<String> subCommonTokens = Arrays.asList(commonTokens).subList(0, i);
						 
						 String[] container = {};
						 
						 commonTokens = subCommonTokens.toArray(container);
						 break;
						 
						 
						}
					}
				}
				
			}
		}
		
		
		commonXPath ="";
		
		for(int i = 1 ; i< commonTokens.length;i++){
			commonXPath +="/"+commonTokens[i];
		
		}
		}
		
		return commonXPath;
	}

	
    @SuppressWarnings("deprecation")
	public List<XSSFXmlColumnPr> getXmlColumnPrs() {
		
		if(xmlColumnPr==null){
			xmlColumnPr = new ArrayList<XSSFXmlColumnPr>();
			for (CTTableColumn column:ctTable.getTableColumns().getTableColumnArray()){
				if (column.getXmlColumnPr()!=null){
					XSSFXmlColumnPr columnPr = new XSSFXmlColumnPr(this,column,column.getXmlColumnPr());
					xmlColumnPr.add(columnPr);
				}
			}
		}
		return xmlColumnPr;
	}
	
	/**
	 * @return the name of the Table, if set
	 */
	public String getName() {
	   return ctTable.getName();
	}
	
	/**
	 * Changes the name of the Table
	 */
	public void setName(String name) {
	   if(name == null) {
	      ctTable.unsetName();
	      return;
	   }
	   ctTable.setName(name);
	}

   /**
    * @return the display name of the Table, if set
    */
   public String getDisplayName() {
      return ctTable.getDisplayName();
   }

   /**
    * Changes the display name of the Table
    */
   public void setDisplayName(String name) {
      ctTable.setDisplayName(name);
   }

	/**
	 * @return  the number of mapped table columns (see Open Office XML Part 4: chapter 3.5.1.4)
	 */
	public long getNumerOfMappedColumns(){
		return ctTable.getTableColumns().getCount();
	}
	
	
	/**
	 * @return The reference for the cell in the top-left part of the table
	 * (see Open Office XML Part 4: chapter 3.5.1.2, attribute ref) 
	 *
	 */
	public CellReference getStartCellReference() {
		
		if(startCellReference==null){			
				String ref = ctTable.getRef();
                if(ref != null) {
                    String[] boundaries = ref.split(":");
                    String from = boundaries[0];
                    startCellReference = new CellReference(from);
                }
		}
		return startCellReference;
	}
	
	/**
	 * @return The reference for the cell in the bottom-right part of the table
	 * (see Open Office XML Part 4: chapter 3.5.1.2, attribute ref)
	 *
	 */
	public CellReference getEndCellReference() {
		
		if(endCellReference==null){
			
				String ref = ctTable.getRef();
				String[] boundaries = ref.split(":");
				String from = boundaries[1];
				endCellReference = new CellReference(from);
		}
		return endCellReference;
	}
	
	
	/**
	 *  @return the total number of rows in the selection. (Note: in this version autofiltering is ignored)
	 *
	 */
	public int getRowCount(){
		
		
		CellReference from = getStartCellReference();
		CellReference to = getEndCellReference();
		
		int rowCount = -1;
		if (from!=null && to!=null){
		 rowCount = to.getRow()-from.getRow();
		}
		return rowCount;
	}

    /**
     * Synchronize table headers with cell values in the parent sheet.
     * Headers <em>must</em> be in sync, otherwise Excel will display a
     * "Found unreadable content" message on startup.
     */
    @SuppressWarnings("deprecation")
    public void updateHeaders(){
        XSSFSheet sheet = (XSSFSheet)getParent();
        CellReference ref = getStartCellReference();
        if(ref == null) return;

        int headerRow = ref.getRow();
        int firstHeaderColumn = ref.getCol();
        XSSFRow row = sheet.getRow(headerRow);

        if (row != null && row.getCTRow().validate()) {
            int cellnum = firstHeaderColumn;
            for (CTTableColumn col : getCTTable().getTableColumns().getTableColumnArray()) {
                XSSFCell cell = row.getCell(cellnum);
                if (cell != null) {
                    col.setName(cell.getStringCellValue());
                }
                cellnum++;
            }
        }
    }
}
