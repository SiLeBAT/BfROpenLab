/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;

import org.hsqldb.server.Server;

/**
 * @author Armin
 *
 */

public class MainKernel {

	static boolean dontLog = false;
	private static boolean isServer = false;
	private static Server s = null;
	
	private static String dbFolder = "/opt/hsqldb/data/";
	private static String bkpFolder = "/opt/hsqldb/backup/";
	private static String logFolder = "/var/log/hsqldb/";
	/*
	private static String dbFolder = "C:/Dokumente und Einstellungen/Weiser/Desktop/";
	private static String bkpFolder = "C:/Dokumente und Einstellungen/Weiser/.localHSH/BfR/LOGs/";
	private static String logFolder = "C:/Dokumente und Einstellungen/Weiser/.localHSH/BfR/LOGs/";
	*/
	private static String[][] dbDefs = new String[][] {
		//{"krise_145","krise_145","SA","de6!§5ddy"},
		{"silebat_DB","silebat_DB","defad","de6!§5ddy"},
		{"silebat_DB_test","silebat_DB_test","defad","de6!§5ddy"}//,
	};
	
	public static void main(final String[] args) { // Servervariante
		isServer = true;
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("start")) {
				MyLogger.setup(logFolder + System.currentTimeMillis() + ".log");
				s = new Server();
				//s.setDaemon(true);
				for (int i=0;i<dbDefs.length;i++) {
					s.setDatabaseName(i, dbDefs[i][0]);
					s.setDatabasePath(i, dbFolder + dbDefs[i][1] + "/DB");					
				}
				s.start();
				s.setNoSystemExit(false);
				MyLogger.handleMessage("Server connected!");
				new Wecker(24).start();
			}
			else if (args[0].equalsIgnoreCase("stop")) {
				shutdownServer();
			}
			else {
				Server.main(args);
			}
		}
    }
	private static void shutdownServer() {
		for (int i=0;i<dbDefs.length;i++) {
    	    try {
    	    	Connection conn = getDefaultAdminConn(i);
    	    	//defragDB(conn);// wir machen hier lieber 'CHECKPOINT DEFRAG', weil 'CHECKPOINT DEFRAG' im Gegensatz zu SHUTDOWN COMPACT bisher noch keine outofmemory Exception geworfen hat
            	MainKernel.sendRequest("SHUTDOWN", false, conn);
    	    }
    	    catch (Exception e) {
    	    	MyLogger.handleException(e);
    	    	System.exit(1);
    	    }
		}
		System.exit(0);		
	}
	/*
	private static void defragDB(final Connection conn) {
    	MyLogger.handleMessage("start CHECKPOINT DEFRAG!");
    	MainKernel.sendRequest("CHECKPOINT DEFRAG", false, conn);
    	MyLogger.handleMessage("fin CHECKPOINT DEFRAG!");		
	}
	*/
	static void dbBackup() {
		for (int i=0;i<dbDefs.length;i++) {
    	    try {
    	    	Connection conn = getDefaultAdminConn(i);
    	    	//defragDB(conn);
	    		Calendar cal = Calendar.getInstance();
	    		cal.setTime(new Date());
	    		int day = cal.get(Calendar.DAY_OF_MONTH);
    	    	String backupFile = bkpFolder + dbDefs[i][0] + "_" + day + ".tar.gz"; // System.currentTimeMillis()		
    	    	File f = new File(backupFile);
    	    	if (f.exists()) {
    	    		f.delete();
    	    		System.gc();
    	    	}
            	MainKernel.sendRequest("BACKUP DATABASE TO '" + backupFile + "' BLOCKING", false, conn);
    	    	System.gc();
    	    }
    	    catch (Exception e) {
    	    	MyLogger.handleException(e);
    	    }
		}
      }
	private static Connection getDefaultAdminConn(final int index) throws Exception {
	    Connection result = null;
	    Class.forName("org.hsqldb.jdbc.JDBCDriver").newInstance();
	    String connStr = "jdbc:hsqldb:hsql://localhost/" + dbDefs[index][0];
	    try {
	    	result = DriverManager.getConnection(connStr, dbDefs[index][2], dbDefs[index][3]);	    			
	    }
	    catch(Exception e) {
	    	MyLogger.handleException(e);
	    }
	    return result;
	}

	static boolean isServer() {
		return isServer;
	}
	/*
	private static Integer getNextChangeLogID(final Connection conn) {
		Integer result = null;
	    try {
	    	Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	    	ResultSet rs = stmt.executeQuery("SELECT MAX(" + delimitL("ID") + ") FROM " + delimitL("ChangeLog"));
	    	if (rs != null && rs.first()) {
		    	result = rs.getInt(1) + 1;
		    	rs.close();
	    	}
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    }
		return result;
	}
	*/
	/*
	private static Integer callIdentity(final Connection conn) {
		Integer result = null;
	    try {
	    	Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	    	ResultSet rs = stmt.executeQuery("CALL IDENTITY()");
	    	if (rs != null && rs.first()) {
		    	result = rs.getInt(1);
		    	rs.close();
	    	}
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    }
		return result;
	}
	*/
	static String getUsername() {
		  	String username = "";
			try {
				Connection lconn = getDefaultConnection();
				if (lconn != null) {
					username = lconn.getMetaData().getUserName();
				}
			}
			catch (SQLException e) {
				MyLogger.handleException(e);
			} 
		  	return username;
		  }
	  private static Connection getDefaultConnection() {
		  Connection result = null;
		    String connStr = "jdbc:default:connection";
		    try {
		    	result = DriverManager.getConnection(connStr);
		    }
		    catch(Exception e) {
		    	MyLogger.handleException(e);
		    }
		    return result;
	  }
	  static String delimitL(final String name) {
		    String newName = name.replace("\"", "\"\"");
		    return "\"" + newName + "\"";
		  }
	  static boolean sendRequest(final String sql, final boolean suppressWarnings) {
		  return sendRequest(sql, suppressWarnings, null);
	  }
	  private static boolean sendRequest(final String sql, final boolean suppressWarnings, Connection conn) {
		  boolean result = false;
		    try {
		    	if (conn == null) {
					conn = getDefaultConnection();
				}
		      Statement anfrage = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		      anfrage.execute(sql);
		      result = true;
		    }
		    catch (Exception e) {
		      if (!suppressWarnings) {
		    	  MyLogger.handleMessage(sql);
		        MyLogger.handleException(e);
		      }
		    }
		    return result;
		  }
}
