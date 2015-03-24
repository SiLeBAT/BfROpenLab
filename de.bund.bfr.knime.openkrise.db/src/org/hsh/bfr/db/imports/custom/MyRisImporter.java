/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
/**
 * 
 */
package org.hsh.bfr.db.imports.custom;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.JProgressBar;
import javax.swing.filechooser.FileFilter;

import org.hsh.bfr.db.DBKernel;
import org.hsh.bfr.db.MyLogger;
import org.hsh.bfr.db.gui.InfoBox;
import org.hsh.bfr.db.gui.dbtable.MyDBTable;
import org.hsh.bfr.db.imports.MyImporter;

import net.sf.jabref.AuthorList;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.BibtexEntryType;
import net.sf.jabref.BibtexFields;

/**
 * @author Armin
 *
 */


/**
 * Imports a Biblioscape Tag File. The format is described on
 * http://www.biblioscape.com/manual_bsp/Biblioscape_Tag_File.htm Several
 * Biblioscape field types are ignored. Others are only included in the BibTeX
 * field "comment".
 */
public class MyRisImporter extends FileFilter implements MyImporter {
	public boolean accept(File f) {
	  if (f.isDirectory()) return true;

	  String extension = getExtension(f);
	  if (extension.equals("txt") || extension.equals("ris")) return true; 
	  return false;
	}
	  
	public String getDescription() {
	    return "RIS Datei (*.txt; *.ris)";
	}

	private String getExtension(File f) {
	  String s = f.getName();
	  int i = s.lastIndexOf('.');
	  if (i > 0 &&  i < s.length() - 1) return s.substring(i+1).toLowerCase();
	  return "";
	}
		private InputStream getIn(String filename) {
			InputStream in = null;
			try {
		    	if (filename.startsWith("http://")) {
		    		URL url = new URL(filename);
		    		URLConnection uc = url.openConnection();
		    		in = uc.getInputStream();
		    	}
		    	else if (filename.startsWith("/org/hsh/bfr/db/res/")) {
						in = getClass().getResourceAsStream(filename);		    		
		    	}
		    	else {
		    		in = new FileInputStream(filename);
		    	}	
			}
			catch (Exception e) {MyLogger.handleException(e);}
	    	return in;
		}
	public String doImport(final String filename, final JProgressBar progress, final boolean showResults) {
		//filename = "C:/Users/Armin/Documents/private/freelance/BfR/Data/100711/RIS-ExportReferenceManagerTest.txt";
  	Runnable runnable = new Runnable() {
      public void run() {
		try {
      		if (progress != null) {
      			progress.setVisible(true);
      			progress.setStringPainted(true);
      			progress.setString("Importiere RIS Datei...");
      			progress.setMinimum(0);
      			progress.setMaximum(1);
      			progress.setValue(0);						
      		}

      		String log = "";
      		int numFailed = 0, numSuccess = 0, numPapers = 0, numPapersFailed = 0, numRisses = 0;
      		Vector<String> duplicates = new Vector<>();
      		File f = new File(filename);
      		InputStream in = null;
			//InputStream in = new FileInputStream(f);
      		in = getIn(filename);
					if (isRecognizedFormat(in)) { // isRecognizedFormat(in)
						in.close();
						in = getIn(filename); //new FileInputStream(f);
						LinkedHashMap<BibtexEntry, String> risses = importEntries(in);
						if (progress != null) {
							progress.setMaximum(risses.size());
						}
				    try {
					    String sql = "INSERT INTO " + DBKernel.delimitL("Literatur") +
		      			" (" + DBKernel.delimitL("Erstautor") + ", " + DBKernel.delimitL("Jahr") + ", " +
		      			DBKernel.delimitL("Titel") + ", " +	DBKernel.delimitL("Abstract") + ", " +	DBKernel.delimitL("Journal") + ", " +
		      			DBKernel.delimitL("Volume") + ", " + DBKernel.delimitL("Issue") + ", " + DBKernel.delimitL("Seite") +
		      			", " + DBKernel.delimitL("Webseite") + ", " + DBKernel.delimitL("Literaturtyp") +
		      			") VALUES (?,?,?,?,?,?,?,?,?,?)";
					    PreparedStatement psmt = DBKernel.getDBConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
					    int lfd=0;
							for (BibtexEntry bibitem : risses.keySet()) {
								if (progress != null) {lfd++;progress.setValue(lfd);}
							    psmt.clearParameters();
							    psmt.setString(1, bibitem.getField("erstautor"));
							    int year = 0;
							    /*
							    if (bibitem.getField("title") != null) System.err.println("title: " + bibitem.getField("title").length() + "\t" + bibitem.getField("title"));
							    if (bibitem.getField("journal") != null) System.err.println("journal: " + bibitem.getField("journal").length() + "\t" + bibitem.getField("journal"));
							    if (bibitem.getField("volume") != null) System.err.println("volume: " + bibitem.getField("volume").length() + "\t" + bibitem.getField("volume"));
							    if (bibitem.getField("number") != null) System.err.println("number: " + bibitem.getField("number").length() + "\t" + bibitem.getField("number"));
							    if (bibitem.getField("url") != null) System.err.println("url: " + bibitem.getField("url").length() + "\t" + bibitem.getField("url"));
							    if (bibitem.getField("abstract") != null) System.err.println("abstract: " + bibitem.getField("abstract").length());
							    */
							    if (bibitem.getField("year") != null) {year = Integer.parseInt(bibitem.getField("year")); psmt.setInt(2, year);}
							    else psmt.setNull(2, java.sql.Types.INTEGER);
							    if (bibitem.getField("title") != null) psmt.setString(3, bibitem.getField("title"));
							    else psmt.setNull(3, java.sql.Types.VARCHAR);
							    if (bibitem.getField("abstract") != null) psmt.setString(4, bibitem.getField("abstract"));
							    else psmt.setNull(4, java.sql.Types.VARCHAR);
							    if (bibitem.getField("journal") != null) psmt.setString(5, bibitem.getField("journal"));
							    else psmt.setNull(5, java.sql.Types.VARCHAR);
							    if (bibitem.getField("volume") != null) psmt.setString(6, bibitem.getField("volume"));
							    else psmt.setNull(6, java.sql.Types.VARCHAR);
							    if (bibitem.getField("number") != null) psmt.setString(7, bibitem.getField("number"));
							    else psmt.setNull(7, java.sql.Types.VARCHAR);
							    if (bibitem.getField("startPage") != null && DBKernel.isDouble(bibitem.getField("startPage"))) psmt.setInt(8, Integer.parseInt(bibitem.getField("startPage")));
							    else psmt.setNull(8, java.sql.Types.INTEGER);
							    if (bibitem.getField("url") != null) psmt.setString(9, bibitem.getField("url"));
							    else psmt.setNull(9, java.sql.Types.VARCHAR);
							    /*
							    lt.put(new Integer(1), "Paper");
							    lt.put(new Integer(2), "SOP");
							    lt.put(new Integer(3), "LA");
							    lt.put(new Integer(4), "Handbuch");
							    lt.put(new Integer(5), "Laborbuch");
							    lt.put(new Integer(6), "Buch");
							    */
							    if (bibitem.getType() == BibtexEntryType.ARTICLE) psmt.setInt(10, 1);
							    else if (bibitem.getType() == BibtexEntryType.BOOK) psmt.setInt(10, 6);
							    else if (bibitem.getType() == BibtexEntryType.BOOKLET) psmt.setInt(10, 6);
							    else if (bibitem.getType() == BibtexEntryType.INBOOK) psmt.setInt(10, 6);
							    else if (bibitem.getType() == BibtexEntryType.MANUAL) psmt.setInt(10, 4);
							    else psmt.setNull(10, java.sql.Types.INTEGER);
							    
							    int psexup = 0;
							    try {psexup = psmt.executeUpdate();}
							    catch (SQLException e1) {
							    	if (e1.getErrorCode() == -104) duplicates.add(bibitem.getField("erstautor") + " " + bibitem.getField("year"));
							    	else {System.err.println(bibitem.getField("erstautor") + " " + bibitem.getField("year")); e1.printStackTrace();}
							    } //e1.printStackTrace(); 
							    if (psexup > 0) {
							    	numSuccess++;
								    // Get lastInsertedID
								    int lastInsertedID = 0;
							    	ResultSet rs = psmt.getGeneratedKeys();
							      if (rs.next()) {
							      	lastInsertedID = rs.getInt(1);
							      }
							      else {
							      	MyLogger.handleMessage("getGeneratedKeys failed!");
							      }
							      rs.close();
	
							      if (lastInsertedID > 0) {
								      // Wir wollen aber dennoch die gesamte RIS Datei abspeichern - zur Sicherheit
									    DBKernel.insertBLOB("Literatur", "ID", risses.get(bibitem), bibitem.getField("erstautor") + "_" + year + ".ris", lastInsertedID);
									    numRisses++;
	
									    // Das Paper wird natürlich auch abgespeichert, falls verfügbar
									    if (bibitem.getField("fulltext") != null || bibitem.getField("pdf") != null) {
									    	String strURL = (bibitem.getField("pdf") != null) ? bibitem.getField("pdf") : bibitem.getField("fulltext");
									    	URL myUrl = getMyURL(strURL);
									    	if (strURL != null) {
											      MyLogger.handleMessage(myUrl.getProtocol() + "\t" + myUrl.getHost() + ":" + myUrl.getPath());
											      if (myUrl.getProtocol().equals("file")) {
											      	String newFile;
											      	if (myUrl.getPath().length() == 0) {
											      		newFile = f.getParentFile().getAbsolutePath() + System.getProperty("file.separator") + myUrl.getHost();
											      		MyLogger.handleMessage("Wasn das eigentlich fürn komischer Fall????\t" + newFile + "\t" + myUrl);
											      	}
											      	else if (myUrl.getHost().length() == 0) {
											      		newFile = myUrl.getPath();
											      	}
											      	else {
											      		newFile = myUrl.getHost() + ":" + myUrl.getPath();									      		
											      	}
											      	//System.out.println(newFile + "\t" + urlToFile(myUrl).exists());
												      File fl = new File(newFile);
												      if (fl.exists()) { // Hier dauerts evtl. sehr lange, wenn das Netzlaufwerk nicht verfügbar ist
												      	DBKernel.insertBLOB("Literatur", "Paper", fl, lastInsertedID);
												      	DBKernel.getDBConnection().createStatement().execute("UPDATE " + DBKernel.delimitL("Literatur") + " SET " + DBKernel.delimitL("Paper") + "='" + fl.getName() + "' WHERE " + DBKernel.delimitL("ID") + "=" + lastInsertedID);
												      	numPapers++;
												      }
												      else {
												      	numPapersFailed++;
												      	MyLogger.handleMessage("File for Import not found!");
												      }
											      }
									    	}
									    	else {
									    		MyLogger.handleMessage("Wasn das fürn URL Format??? " + strURL);
									    	}
									    }
							      }
								    
							    }
							    else {
							    	numFailed++;
							    }
						    
							}
					    psmt.close();
					    //fis.close();
					  }
				    catch (Exception e) {
				    	MyLogger.handleException(e);
				    }
				}
				else {
					log = "Unbekanntes Format! RIS? Oder Autor nicht angegeben??\n";
				}
				in.close();

    			if (progress != null) {
    				progress.setVisible(false);
  	  			// Refreshen:
    				MyDBTable myDB = DBKernel.mainFrame.getMyList().getMyDBTable();
    				if (myDB.getActualTable() != null) {
	    				String tablename = myDB.getActualTable().getTablename();
	    				if (tablename.equals("Literatur")) {
	    					myDB.setTable(myDB.getActualTable());
	    				}
    				}
    				
    			}
        		log += numSuccess + " erfolgreiche Importe.\n";
        		log += numFailed + " fehlgeschlagene Importe.\n";
        		log += numPapers + " erfolgreich importierte Datei(en).";
        		if (numPapersFailed > 0) log += "\n" + numPapersFailed + " nicht gefundene Datei(en).";
        		if (numRisses < numSuccess) log += "\n" + (numSuccess - numRisses) + " fehlgeschlagene Importe der RIS Datei.";
        		if (duplicates.size() > 0) {
        			log += "\nSchon drin in DB:\n\n";
	        		for (int iii=0;iii<duplicates.size();iii++) {
	        			log += duplicates.get(iii) + "\n";
	        		}
        		}
    			if (showResults) {
    				InfoBox ib = new InfoBox(log, true, new Dimension(400, 300), null);
    				ib.setVisible(true);    				
    			}
    			else {
    				System.out.println("MyRisImporter (" + filename + "):\n" + log);
    			}
			}
			catch (Exception e) {
				MyLogger.handleException(e);
			}
      }
	  };
	  Thread thread = new Thread(runnable);
	  thread.start();
    try {
			thread.join();
		}
    catch (InterruptedException e) {
    	MyLogger.handleException(e);
		}
    return "";
	}
	
	/**
	 * Imports a Biblioscape Tag File. The format is described on
	 * http://www.biblioscape.com/manual_bsp/Biblioscape_Tag_File.htm Several
	 * Biblioscape field types are ignored. Others are only included in the BibTeX
	 * field "comment".
	 */

	/**
	 * Return the name of this import format.
	 */
	public String getFormatName() {
		return "RIS";
	}
	
	/*
	 *  (non-Javadoc)
	 * @see net.sf.jabref.imports.ImportFormat#getCLIId()
	 */
	public String getCLIId() {
	  return "ris";
	}
	
	/**
	 * Check whether the source is in the correct format for this importer.
	 */
	private boolean isRecognizedFormat(InputStream stream) throws IOException {
	
		// Our strategy is to look for the "AU  - *" line.
		BufferedReader in = new BufferedReader(getReaderDefaultEncoding(stream));
		Pattern pat1 = Pattern.compile("AU  - .*"),
		    pat2 = Pattern.compile("A1  - .*"),
		    pat3 = Pattern.compile("A2  - .*");
		
		
		String str;
		while ((str = in.readLine()) != null){
		    if (pat1.matcher(str).find() || pat2.matcher(str).find() || pat3.matcher(str).find())
		        return true;
		}
		
		return false;
	}
	
	/**
	 * Parse the entries in the source, and return a List of BibtexEntry
	 * objects.
	 */
	private LinkedHashMap<BibtexEntry, String> importEntries(InputStream stream) throws IOException {
		LinkedHashMap<BibtexEntry, String> bibitems = new LinkedHashMap<>();
		StringBuffer sb = new StringBuffer();
		BufferedReader in = new BufferedReader(getReaderDefaultEncoding(stream));
		String str;
		while ((str = in.readLine()) != null) {
			if (str.length() > 0) { // Komischerweise sind hier alle zwei Zeilen empty... Also weg damit, wird ja eh nicht benötigt
		    sb.append(str);
		    sb.append("\n");
			}
		}
		String[] entries = sb.toString().split("ER  -");
		
		for (int i = 0; i < entries.length; i++) {
		
		    if (entries[i].trim().length() == 0) {
		        continue;
		    }
		    
        String type = "", author = "", editor = "", startPage = "", endPage = "", comment = "";
        String erstautor = "";
        HashMap<String, String> hm = new HashMap<>();
		
		
		    String[] fields = entries[i].split("\n");
		
		    for (int j = 0; j < fields.length; j++) {
	        StringBuffer current = new StringBuffer(fields[j]);
	        boolean done = false;
	        while (!done && (j < fields.length-1)) {
	            if ((fields[j+1].length() >= 6) && !fields[j+1].substring(2, 6).equals("  - ")) {
	                if ((current.length() > 0)
	                        && !Character.isWhitespace(current.charAt(current.length()-1))
	                        && !Character.isWhitespace(fields[j+1].charAt(0)))
	                    current.append(' ');
	                current.append(fields[j+1]);
	                j++;
	            } else
	                done = true;
	        }
	        String entry = current.toString();
		    if (entry.length() < 6) {
		    	continue;
		    }
		    else {
	        String lab = entry.substring(0, 2);
	        String val = entry.substring(6).trim();
	        if (lab.equals("TY")) {
		        if (val.equals("BOOK")) type = "book";
		        else if (val.equals("JOUR") || val.equals("MGZN")) type = "article";
		        else if (val.equals("THES")) type = "phdthesis";
		        else if (val.equals("UNPB")) type = "unpublished";
		        else if (val.equals("RPRT")) type = "techreport";
		        else if (val.equals("CONF")) type = "inproceedings";
		        else if (val.equals("CHAP")) type = "incollection";//"inbook";		
		        else type = "other";
	        }
	        else if (lab.equals("T1") || lab.equals("TI")) hm.put("title", val);//Title
	        // =
	        // val;
	        else if (lab.equals("T2") || lab.equals("T3") || lab.equals("BT")) {
	            hm.put("booktitle", val);
	        }
	        else if (lab.equals("AU") || lab.equals("A1")) {
	            if (author.equals("")) // don't add " and " for the first author
	                author = val;
	            else author += " and " + val;
	            if (lab.equals("A1") && erstautor.length() == 0) erstautor = val;
	        }
		      else if (lab.equals("A2")) {
		            if (editor.equals("")) // don't add " and " for the first editor
		                editor = val;
		            else editor += " and " + val;
		      }
		      else if (lab.equals("JA") || lab.equals("JF") || lab.equals("JO")) {
	          if (type.equals("inproceedings"))
	          	hm.put("booktitle", val);
	          else
	          	hm.put("journal", val);
		       }		
		       else if (lab.equals("SP"))
		      	 startPage = val;
		       else if (lab.equals("PB")) {
		      	 if (type.equals("phdthesis"))
		      		 hm.put("school", val);
		         else
		           hm.put("publisher", val);
		       }
		       else if (lab.equals("AD") || lab.equals("CY"))
		         hm.put("address", val);
		       else if (lab.equals("EP"))
		      	 endPage = val;
		       else if (lab.equals("SN"))
		         hm.put("issn", val);
		       else if (lab.equals("VL"))
		      	 hm.put("volume", val);
		       else if (lab.equals("IS"))
		      	 hm.put("number", val);
		       else if (lab.equals("L1"))
		      	 hm.put("pdf", val);
		       else if (lab.equals("L2"))
		      	 hm.put("fulltext", val);
		       else if (lab.equals("N2") || lab.equals("AB")) {
		      	 String oldAb = hm.get("abstract");
		         if (oldAb == null)
		           hm.put("abstract", val);
		         else
		        	 hm.put("abstract", oldAb+"\n"+val);
		       }		
		       else if (lab.equals("UR")) hm.put("url", val);
		       else if ((lab.equals("Y1") || lab.equals("PY")) && val.length() >= 4) {
		      	 String[] parts = val.split("/");
					   hm.put("year", parts[0]);
					   if ((parts.length > 1) && (parts[1].length() > 0)) {
					        try {
					            int month = Integer.parseInt(parts[1]);
					            if ((month > 0) && (month <= 12)) {
					                //System.out.println(Globals.MONTHS[month-1]);
					                hm.put("month", "#"+MONTHS[month-1]+"#");
					            }
					        } catch (NumberFormatException ex) {
					            // The month part is unparseable, so we ignore it.
					        }
					   }
					 }
		       else if (lab.equals("KW")) {
		      	 if (!hm.containsKey("keywords")) hm.put("keywords", val);
		         else {
		            String kw = hm.get("keywords");
		            hm.put("keywords", kw + ", " + val);
		         }
		       }
		       else if (lab.equals("U1") || lab.equals("U2") || lab.equals("N1")) {
		          if (comment.length() > 0)
		              comment = comment+"\n";
		          comment = comment+val;
		       }
		       // Added ID import 2005.12.01, Morten Alver:
		       else if (lab.equals("ID"))
		      	 hm.put("refid", val);
		    	 }
		    }
		    // fix authors
		    if (author.length() > 0) {
		    	int index = author.indexOf(" and ");
		        if (erstautor.trim().length() == 0) erstautor = index < 0 ? author : author.substring(0, index);		        	
		        author = AuthorList.fixAuthor_lastNameFirst(author);
		        hm.put("author", author);
		    }
		    if (erstautor.length() > 0) {
		    	erstautor = AuthorList.fixAuthor_lastNameFirst(erstautor);
			    hm.put("erstautor", erstautor);
	    }
		    if (editor.length() > 0) {
		        editor = AuthorList.fixAuthor_lastNameFirst(editor);
		        hm.put("editor", editor);
		    }
		    if (comment.length() > 0) {
		        hm.put("comment", comment);
		    }
		
		    hm.put("startPage", startPage);
		    hm.put("pages", startPage + "--" + endPage);
		    BibtexEntry b = new BibtexEntry(BibtexFields.DEFAULT_BIBTEXENTRY_ID, getEntryType(type)); // id assumes an existing database so don't
		
		    // Remove empty fields:
		    ArrayList<Object> toRemove = new ArrayList<>();
		    for (Iterator<String> it = hm.keySet().iterator(); it.hasNext();) {
		        Object key = it.next();
		        String content = hm.get(key);
		        if ((content == null) || (content.trim().length() == 0))
		            toRemove.add(key);
		    }
		    for (Iterator<Object> iterator = toRemove.iterator(); iterator.hasNext();) {
		        hm.remove(iterator.next());
		
		    }
		    // create one here
		    b.setField(hm);
		
		    bibitems.put(b, entries[i]);		    	
		}
		
		return bibitems;
	}	
	    
	private String[] MONTHS = new String[] { "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec" };

	private Reader getReaderDefaultEncoding(InputStream in) throws IOException {
	  InputStreamReader reader;
	  String encoding = System.getProperty("file.encoding");
	  if (encoding.equalsIgnoreCase("Cp1252")) encoding = "Cp850"; // Cp1252 bekommt die Umlaute nicht so gut hin. ReferenceManager scheint in Cp850 abzuspeichern (OEM vs. ANSI)
	  else MyLogger.handleMessage("Was gibts denn noch so für Encodings????? Anderes OS?");
	  //System.out.println(encoding);
	  reader = new InputStreamReader(in, encoding); // "Cp850" oder System.getProperty("file.encoding")
	
	  return reader;
	}
	private BibtexEntryType getEntryType(String type) {
		// decide which entryType object to return
	  Object o = BibtexEntryType.ALL_TYPES.get(type);
	  if (o != null) {
	          return (BibtexEntryType) o;
	  } else {
	          return BibtexEntryType.OTHER;
	  }
	  /*
	   * if(type.equals("article")) return BibtexEntryType.ARTICLE; else
	   * if(type.equals("book")) return BibtexEntryType.BOOK; else
	   * if(type.equals("inproceedings")) return
	   * BibtexEntryType.INPROCEEDINGS;
	   */
	}
	/*
	private File urlToFile(URL url) { 
        File file = null; 
        try { 
          file = new File(url.toURI()); 
        }
        catch(Exception e) { 
          file = new File(url.getPath()); 
        } 
        MyLogger.handleMessage(file.toString()); 
        return file; 
    } 
	*/
	private URL getMyURL(String strURL) {
		URL myUrl = null;
    	try {
		      myUrl = new URL(strURL);									    		
	  	}
	  	catch (Exception e) {
	  		if (!strURL.startsWith("file://")) myUrl = getMyURL("file://" + strURL);
	  	}
	  	return myUrl;
	}

/*
 * 
 * aus http://en.wikipedia.org/wiki/RIS_(file_format)
 * 
TY  - Type of reference (must be the first tag)
ID  - Reference ID (not imported to reference software)
T1  - Primary title
TI  - Book title
CT  - Title of unpublished reference
A1  - Primary author
A2  - Secondary author (each name on separate line)
AU  - Author (syntax. Last name, First name, Suffix)
Y1  - Primary date
PY  - Publication year (YYYY/MM/DD)
N1  - Notes 
KW  - Keywords (each keyword must be on separate line preceded KW -)
RP  - Reprint status (IN FILE, NOT IN FILE, ON REQUEST (MM/DD/YY))
SP  - Start page number
EP  - Ending page number
JF  - Periodical full name
JO  - Periodical standard abbreviation
JA  - Periodical in which article was published
J1  - Periodical name - User abbreviation 1
J2  - Periodical name - User abbreviation 2
VL  - Volume number
IS  - Issue number
T2  - Title secondary
CY  - City of Publication
PB  - Publisher
U1  - User definable 1
U5  - User definable 5
T3  - Title series
N2  - Abstract
SN  - ISSN/ISBN (e.g. ISSN XXXX-XXXX)
AV  - Availability
M1  - Misc. 1
M3  - Misc. 3
AD  - Address
UR  - Web/URL
L1  - Link to PDF
L2  - Link to Full-text
L3  - Related records
L4  - Images
ER  - End of Reference (must be the last tag)
 * 
 * 
 */
}







