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
package org.apache.poi;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.extractor.HPSFPropertiesExtractor;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Common Parent for OLE2 based Text Extractors
 *  of POI Documents, such as .doc, .xls
 * You will typically find the implementation of
 *  a given format's text extractor under
 *  org.apache.poi.[format].extractor .
 * @see org.apache.poi.hssf.extractor.ExcelExtractor
 * @see org.apache.poi.hslf.extractor.PowerPointExtractor
 * @see org.apache.poi.hdgf.extractor.VisioTextExtractor
 * @see org.apache.poi.hwpf.extractor.WordExtractor
 */
public abstract class POIOLE2TextExtractor extends POITextExtractor {
	/**
	 * Creates a new text extractor for the given document
	 * 
	 * @param document The POIDocument to use in this extractor.
	 */
	public POIOLE2TextExtractor(POIDocument document) {
		super(document);
	}

	/**
	 * Returns the document information metadata for the document
	 * 
     * @return The Document Summary Information or null 
     *      if it could not be read for this document.
	 */
	public DocumentSummaryInformation getDocSummaryInformation() {
		return document.getDocumentSummaryInformation();
	}
	/**
	 * Returns the summary information metadata for the document.
	 * 
     * @return The Summary information for the document or null
     *      if it could not be read for this document.
	 */
	public SummaryInformation getSummaryInformation() {
		return document.getSummaryInformation();
	}

	/**
	 * Returns an HPSF powered text extractor for the
	 *  document properties metadata, such as title and author.
	 *  
	 * @return an instance of POIExtractor that can extract meta-data.
	 */
	public POITextExtractor getMetadataTextExtractor() {
		return new HPSFPropertiesExtractor(this);
	}

	/**
	 * Return the underlying DirectoryEntry of this document.
	 *
	 * @return the DirectoryEntry that is associated with the POIDocument of this extractor.
	 */
    public DirectoryEntry getRoot()
    {
        return document.directory;
    }

    /**
     * Return the underlying POIFS FileSystem of this document.
     * 
     * @return the POIFSFileSystem that is associated with the POIDocument of this extractor. 
     *
     * @deprecated Use {@link #getRoot()} instead
     */
    @Deprecated
    public POIFSFileSystem getFileSystem()
    {
        return document.directory.getFileSystem();
    }
}
