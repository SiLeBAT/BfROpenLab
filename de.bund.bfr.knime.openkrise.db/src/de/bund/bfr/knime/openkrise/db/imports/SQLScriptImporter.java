/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db.imports;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JProgressBar;

import org.hsqldb.cmdline.SqlFile;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyLogger;

/**
 * @author Weiser
 *
 */
public class SQLScriptImporter implements MyImporter {

	private String delimiter;
	private boolean doSqlTool;
	
	public SQLScriptImporter() {
		this(false);
	}
	private SQLScriptImporter(boolean doSqlTool) {
		this.doSqlTool = doSqlTool;
		this.delimiter = ";";
	}
	public SQLScriptImporter(String delimiter) {
		this.delimiter = delimiter;
	}
	
	public boolean doImport(final String filename, final JProgressBar progress, final boolean showResults) {
	  	Runnable runnable = new Runnable() {
	        @SuppressWarnings("resource")
			public void run() {
	  		    try {
	        		if (progress != null) {
	        			progress.setVisible(true);
			            progress.setStringPainted(true);
			            progress.setString("Importiere SQL Script...");
	        			progress.setMinimum(0);
	        			progress.setMaximum(1);
	        			progress.setValue(0);
	        		}
	          		InputStream is = null;
			    	if (filename.startsWith("http://")) {
			    		URL url = new URL(filename);
			    		URLConnection uc = url.openConnection();
			    		is = uc.getInputStream();
			    	}
			    	else if (filename.startsWith("/de/bund/bfr/knime/openkrise/db/res/")) {
							is = getClass().getResourceAsStream(filename);		    		
			    	}
			    	else {
			    		is = new FileInputStream(filename);
			    	}

			    	if (doSqlTool) {
			    		Reader reader = new InputStreamReader(is, "UTF-8");
	  		            SqlFile sqlFile = new SqlFile(reader, filename, System.out, null, false, null);
	  		            sqlFile.setConnection(DBKernel.getDBConnection());
	  		            sqlFile.execute();	  		    		
	  		    	}
	  		    	else {
				    	Scanner scanner;
	    		        scanner = new Scanner(is, "UTF-8").useDelimiter(delimiter);
	        		    while(scanner.hasNext()) {
	        		        String rawStatement = scanner.next();
	        		        if (rawStatement.trim().length() > 0) {
	        		        	if (rawStatement.contains("\\u")) {
	        		        		//System.err.println(rawStatement + "\n" + process(rawStatement));
	        		        		rawStatement = process(rawStatement);
	        		        	}
	        		        	DBKernel.sendRequest(rawStatement + delimiter, false);
	        		        }
	        		    }
	  		    	}
			    	
	        		if (progress != null) {
	    				progress.setVisible(false);
	    			}
	  				else {
	  					MyLogger.handleMessage("SQL Script Importer - Fin");
	  				}
	    			if (showResults) {
	    				//   				
	    			}
	    			else {
	    				MyLogger.handleMessage("SQL Script Importer (" + filename + "): Fin!");
	    			}
	  		    }
			    catch (Exception e) {MyLogger.handleException(e);}
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

	    return true;
	}
	private final Pattern pattern = Pattern.compile("(\\\\\\\\)|\\\\u([0-9a-fA-F]{4})");  
	private String process(String input) {
			StringBuffer sb = new StringBuffer(input.length());  
			Matcher m = pattern.matcher(input);  
			while (m.find()) {  
				String replacement;  
				if (m.group(1) != null) {  
					// found two backslashes in source  
					replacement = "\\\\"; // represents one backslash in result  
				}
				else {  
					// group 2 must be non-null; found a unicode escape  
					String hexStr = m.group(2);  
					int n = Integer.parseInt(hexStr, 16); // parse as hexadecimal  
					replacement = String.valueOf((char) n);  
				}  
				m.appendReplacement(sb, replacement);  
			}  
			m.appendTail(sb);  
			return sb.toString();  
	}
}
