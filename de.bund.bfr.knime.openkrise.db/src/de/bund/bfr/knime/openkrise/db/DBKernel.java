/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.zip.CRC32;

import javax.swing.JFrame;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import de.bund.bfr.knime.openkrise.db.gui.InfoBox;
import de.bund.bfr.knime.openkrise.db.gui.Login;
import de.bund.bfr.knime.openkrise.db.gui.MainFrame;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren.MyStringFilter;

/**
 * @author Armin
 * 
 */
public class DBKernel {

	/**
	 * @param args
	 */

	private static HashMap<String, String> adminU = new HashMap<>();
	private static HashMap<String, String> adminP = new HashMap<>();
	private static LinkedHashMap<Object, LinkedHashMap<Object, String>> filledHashtables = new LinkedHashMap<>();

	private static Connection localConn = null;
	private static String m_Username = "";
	private static String m_Password = "";

	final static String HSH_PATH = System.getProperty("user.home") + System.getProperty("file.separator") + ".localHSH" + System.getProperty("file.separator") + "BfR"
			+ System.getProperty("file.separator");
	public static String HSHDB_PATH = HSH_PATH + "DBs" + System.getProperty("file.separator");

	public static boolean dontLog = false;

	public static MyPreferences prefs = new MyPreferences();

	public static MyDBI myDBi = null;
	public static MainFrame mainFrame = null;

	public static boolean passFalse = false;
	public static boolean openingDBGUI = false;

	public static boolean isServerConnection = false;
	public static boolean isKNIME = false;

	public static String softwareVersion = "1.8.7"; // "1.8.7";
	public static boolean debug = true;

	public static String getTempSA(String dbPath) {
		if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) return DBKernel.myDBi.getSA();
		// String sa = DBKernel.prefs.get("DBADMINUSER" +
		// getCRC32(dbPath),"00");
		// if (sa.equals("00")) {
		if (!adminU.containsKey(dbPath)) getUP(dbPath);
		return adminU.get(dbPath);
	}

	private static String getTempSAPass(String dbPath) {
		if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) return DBKernel.myDBi.getSAP();
		// String pass = DBKernel.prefs.get("DBADMINPASS" +
		// getCRC32(dbPath),"00");
		// if (pass.equals("00")) {
		if (isServerConnection) return "de6!§5ddy";
		if (!adminP.containsKey(dbPath)) getUP(dbPath);
		return adminP.get(dbPath);
	}

	private static String getDefaultSA() {
		return getDefaultSA(false);
	}

	private static String getDefaultSAPass() {
		return getDefaultSAPass(false);
	}

	private static String getDefaultSA(boolean other) {
		String sa = "";
		// if (debug) return "SA";
		if (other) sa = "defad";
		else sa = "SA";
		return sa;
	}

	private static String getDefaultSAPass(boolean other) {
		String pass = "";
		// if (debug) return "";
		if (other) pass = "de6!§5ddy";
		else pass = "";
		return pass;
	}

	public static void removeAdminInfo(String dbPath) {
		if (adminU.containsKey(dbPath)) adminU.remove(dbPath);
		if (adminP.containsKey(dbPath)) adminP.remove(dbPath);
	}

	public static String getLanguage() {
		return "en";
	}

	public static boolean getUP(String dbPath) {
		boolean result = false;
		DBKernel.closeDBConnections(false);

		String sa = getDefaultSA();
		String pass = getDefaultSAPass();
		Connection conn = null;
		try {
			conn = getDBConnection(dbPath, sa, pass, false, true);
		} catch (Exception e) {
		}
		if (conn != null && !isAdmin(conn, sa)) {
			try {
				conn.close();
			} catch (Exception e) {
			}
			conn = null;
		}
		if (conn == null) {
			sa = getDefaultSA(true);
			try {
				conn = getDBConnection(dbPath, sa, pass, false, true);
			} catch (Exception e) {
			}
			if (conn != null && !isAdmin(conn, sa)) {
				try {
					conn.close();
				} catch (Exception e) {
				}
				conn = null;
			}
		}
		if (conn == null) {
			pass = getDefaultSAPass(true);
			try {
				conn = getDBConnection(dbPath, sa, pass, false, true);
			} catch (Exception e) {
			}
			if (conn != null && !isAdmin(conn, sa)) {
				try {
					conn.close();
				} catch (Exception e) {
				}
				conn = null;
			}
		}
		if (conn == null) {
			sa = getDefaultSA(false);
			try {
				conn = getDBConnection(dbPath, sa, pass, false, true);
			} catch (Exception e) {
			}
			if (conn != null && !isAdmin(conn, sa)) {
				try {
					conn.close();
				} catch (Exception e) {
				}
				conn = null;
			}
		}

		if (conn == null) System.err.println("Admin not found...");
		else {
			result = true;
			adminU.put(dbPath, sa);
			adminP.put(dbPath, pass);
			// System.err.println("pass combi is: " + sa + "\t" + pass);
		}

		try {
			DBKernel.closeDBConnections(false);
			DBKernel.getDBConnection(true);
			if (DBKernel.mainFrame.getMyList() != null && DBKernel.mainFrame.getMyList().getMyDBTable() != null) {
				DBKernel.mainFrame.getMyList().getMyDBTable().setConnection(DBKernel.getDBConnection(true));
			}
		} catch (Exception e) {
		}

		return result;
	}

	public static Integer getLastInsertedID(final PreparedStatement psmt) {
		if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) return DBKernel.myDBi.getLastInsertedID(psmt);
		Integer lastInsertedID = null;
		try {
			ResultSet rs = psmt.getGeneratedKeys();
			if (rs.next()) {
				lastInsertedID = rs.getInt(1);
			} else {
				System.err.println("getGeneratedKeys failed!\n" + psmt);
			}
			rs.close();
		} catch (Exception e) {
		}
		return lastInsertedID;
	}

	static String getPassword() {
		if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) return DBKernel.myDBi.getDbPassword();
		return m_Password;
	}

	public static String getUsername() {
		if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) return DBKernel.myDBi.getDbUsername();
		String username = DBKernel.m_Username;
		try { // im Servermodus muss ich schon abchecken, welcher User
				// eingeloggt ist!
			Connection lconn = getDefaultConnection();
			if (lconn == null) {
				username = DBKernel.m_Username; // lokale Variante
			} else {
				// System.out.println(lconn.getMetaData());
				username = lconn.getMetaData().getUserName(); // Server (hoffe
																// ich klappt
																// immer ...?!?)
			}
		} catch (SQLException e) {
			// MyLogger.handleException(e);
		}
		return username;
	}

	public static void setForeignNullAt(final String tableName, final String fieldName, final Object id) {
		try {
			Statement anfrage = getDBConnection().createStatement();
			String sql = "UPDATE " + delimitL(tableName) + " SET " + delimitL(fieldName) + " = NULL WHERE " + delimitL("ID") + " = " + id;
			anfrage.execute(sql);
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
	}

	public static boolean deleteBLOB(final String tableName, final String fieldName, final int id) {
		String sql = "DELETE FROM " + delimitL("DateiSpeicher") + " WHERE " + delimitL("TabellenID") + "=" + id + " AND" + delimitL("Tabelle") + "='" + tableName + "' AND "
				+ delimitL("Feld") + "='" + fieldName + "'";
		return sendRequest(sql, false);
	}

	public static boolean insertBLOB(final String tableName, final String fieldName, final File fl, final int id) {
		boolean result = false;
		try {
			if (fl.exists()) {
				String sql = "INSERT INTO " + delimitL("DateiSpeicher") + " (" + delimitL("Zeitstempel") + "," + delimitL("Tabelle") + "," + delimitL("Feld") + ","
						+ delimitL("TabellenID") + "," + delimitL("Dateiname") + "," + delimitL("Dateigroesse") + "," + delimitL("Datei") + ")" + " VALUES (?,?,?,?,?,?,?);";

				PreparedStatement psmt = getDBConnection().prepareStatement(sql);
				psmt.clearParameters();
				psmt.setTimestamp(1, new Timestamp(new Date().getTime()));
				psmt.setString(2, tableName);
				psmt.setString(3, fieldName);
				psmt.setInt(4, id);
				psmt.setString(5, fl.getName());
				psmt.setInt(6, (int) fl.length());
				FileInputStream fis = new FileInputStream(fl);
				psmt.setBinaryStream(7, fis, (int) fl.length());
				result = (psmt.executeUpdate() > 0);
				psmt.close();
				fis.close();
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static boolean insertBLOB(final String tableName, final String fieldName, final String content, final String filename, final int id) {
		boolean result = false;
		try {
			String sql = "INSERT INTO " + delimitL("DateiSpeicher") + " (" + delimitL("Zeitstempel") + "," + delimitL("Tabelle") + "," + delimitL("Feld") + ","
					+ delimitL("TabellenID") + "," + delimitL("Dateiname") + "," + delimitL("Dateigroesse") + "," + delimitL("Datei") + ")" + " VALUES (?,?,?,?,?,?,?);";

			PreparedStatement psmt = getDBConnection().prepareStatement(sql);
			psmt.clearParameters();
			psmt.setTimestamp(1, new Timestamp(new Date().getTime()));
			psmt.setString(2, tableName);
			psmt.setString(3, fieldName);
			psmt.setInt(4, id);
			psmt.setString(5, filename);
			byte[] b = content.getBytes();
			psmt.setInt(6, b.length);
			InputStream bais = new ByteArrayInputStream(b);
			psmt.setBinaryStream(7, bais, b.length);
			result = (psmt.executeUpdate() > 0);
			psmt.close();
			bais.close();
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static List<Integer> getLastChangeLogEntries(final String tablename, int fromID) {
		List<Integer> result = new ArrayList<>();
		String sql = "SELECT " + delimitL("TabellenID") + " FROM " + delimitL("ChangeLog") + " WHERE " + delimitL("Tabelle") + " = '" + tablename + "' AND " + delimitL("ID")
				+ " >= " + fromID;
		ResultSet rs = getResultSet(sql, false);
		try {
			if (rs != null && rs.first()) {
				do {
					result.add(rs.getInt(1));
				} while (rs.next());
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static LinkedHashMap<String, Timestamp> getFirstUserFromChangeLog(final String tablename, final Integer tableID) {
		LinkedHashMap<String, Timestamp> result = new LinkedHashMap<>();
		String sql = "SELECT " + delimitL("Username") + "," + delimitL("Zeitstempel") + " FROM " + delimitL("ChangeLog") + " WHERE " + delimitL("Tabelle") + " = '" + tablename
				+ "' AND " + delimitL("TabellenID") + " = " + tableID + " ORDER BY " + delimitL("Zeitstempel") + " ASC";
		ResultSet rs = getResultSet(sql, false);
		try {
			if (rs != null && rs.first()) {
				result.put(rs.getString(1), rs.getTimestamp(2));
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static LinkedHashMap<Integer, Vector<String>> getUsersFromChangeLog(final String tablename, final Integer tableID) {
		return getUsersFromChangeLog(tablename, tableID, null);
	}

	public static LinkedHashMap<Integer, Vector<String>> getUsersFromChangeLog(final String tablename, final String username) {
		return getUsersFromChangeLog(tablename, null, username);
	}

	private static LinkedHashMap<Integer, Vector<String>> getUsersFromChangeLog(final String tablename, final Integer tableID, final String username) {
		return getUsersFromChangeLog(null, tablename, tableID, username, false);
	}

	private static LinkedHashMap<Integer, Vector<String>> getUsersFromChangeLog(final Statement anfrage, final String tablename, final Integer tableID, final String username,
			final boolean showDeletedAsWell) {
		LinkedHashMap<Integer, Vector<String>> result = new LinkedHashMap<>();
		Vector<String> entries = new Vector<>();
		String sql = "SELECT " + delimitL("TabellenID") + "," + delimitL("Username") + "," + delimitL("Zeitstempel") + "," + delimitL(tablename) + "." + delimitL("ID") + " AS "
				+ delimitL("ID") + "," + delimitL("ChangeLog") + "." + delimitL("ID") + "," + delimitL("Alteintrag") + "," + delimitL(tablename) + ".*" + " FROM "
				+ delimitL("ChangeLog") + " LEFT JOIN " + delimitL(tablename) + " ON " + delimitL("ChangeLog") + "." + delimitL("TabellenID") + "=" + delimitL(tablename) + "."
				+ delimitL("ID") + " WHERE " + delimitL("ChangeLog") + "." + delimitL("Tabelle") + " = '" + tablename + "'"
				+ (tableID != null ? " AND " + delimitL("ChangeLog") + "." + delimitL("TabellenID") + " = " + tableID : "")
				+ (username != null ? " AND " + delimitL("ChangeLog") + "." + delimitL("Username") + " = '" + username + "'" : "") + " ORDER BY " + delimitL("ChangeLog") + "."
				+ delimitL("ID") + " ASC"; // Zeitstempel DESC
		ResultSet rs = anfrage == null ? getResultSet(sql, false) : getResultSet(anfrage, sql, false);
		try {
			if (rs != null && rs.first()) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
				String actualRow = "";
				for (int j = 8; j <= rs.getMetaData().getColumnCount(); j++) {
					actualRow += "\t" + rs.getString(j);
				}
				do {
					if (showDeletedAsWell || rs.getObject("ID") != null) { // wurde die ID in der Zwischenzeit gelöscht? Dann muss sie auch nicht gelistet werden!
						Integer id = rs.getInt("TabellenID");
						if (result.containsKey(id)) {
							entries = result.get(id);
						} else {
							entries = new Vector<>();
						}
						String newEntry = rs.getString("Username") + "\t" + sdf.format(rs.getTimestamp("Zeitstempel"));
						Object o = rs.getObject("Alteintrag");
						if (o != null && o instanceof Object[]) {
							Object[] oo = (Object[]) o;
							String ae = "";
							for (int i = 1; i < oo.length; i++) {
								ae += "\t" + oo[i];
							}
							if (entries.size() > 0) {
								String oldEntry = entries.get(entries.size() - 1);
								entries.remove(entries.size() - 1);
								int oe = oldEntry.indexOf("\n\t");
								if (oldEntry.startsWith("Unknown\n\t")) {
									oe = oldEntry.indexOf("\n\t", oe + 1);
								}
								if (oe > 0) {
									oldEntry = oldEntry.substring(0, oe) + "\n" + ae;
								} else {
									oldEntry = oldEntry + "\n" + ae;
								}
								entries.add(oldEntry);
							} else { // kann passieren, wenn erster Eintrag von SA, z.B. bei Katalogen
								entries.add("Unknown\n\t" + ae.substring(1));
							}
						}
						entries.add(newEntry + "\n" + actualRow);
						result.put(id, entries);
					} else {
						// System.err.println(rs.getInt("TabellenID") + " wurde bereits gelöscht!");
					}
				} while (rs.next());
				rs.close();
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static String delimitL(final String name) {
		String newName = name.replace("\"", "\"\"");
		return "\"" + newName + "\"";
	}

	public static boolean closeDBConnections(final boolean kompakt) {
		if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) return DBKernel.myDBi.closeDBConnections(kompakt);
		boolean result = true;
		try {
			if (localConn != null && !localConn.isClosed()) {
				if (!DBKernel.isServerConnection) {
					try {
						if (kompakt && !isAdmin()) { // kompakt ist nur beim Programm schliessen true
							closeDBConnections(false);
							try {
								localConn = getDefaultAdminConn(HSHDB_PATH, false, true);
							} catch (Exception e) {
								e.printStackTrace();
							}
							if (localConn == null) {
								getUP(HSHDB_PATH);
								if (localConn != null) localConn.close();
								try {
									localConn = getDefaultAdminConn(HSHDB_PATH, false, true);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
						Statement stmt = localConn.createStatement();
						MyLogger.handleMessage("vor SHUTDOWN");
						stmt.execute("SHUTDOWN"); // Hier kanns es eine Exception geben, weil nur der Admin SHUTDOWN machen darf!
						//XmlLoader.save2File(HSHDB_PATH + "DB.xml", myDBi);
					} catch (SQLException e) {
						result = false;
						if (kompakt) e.printStackTrace();
					}
				}
				MyLogger.handleMessage("vor close");
				localConn.close();
				MyLogger.handleMessage("vor gc");
				System.gc();
				System.runFinalization();
				try {
					if (mainFrame != null && mainFrame.getMyList() != null && mainFrame.getMyList().getMyDBTable() != null
							&& mainFrame.getMyList().getMyDBTable().getActualTable() != null) {
						DBKernel.prefs.put("LAST_SELECTED_TABLE", mainFrame.getMyList().getMyDBTable().getActualTable().getTablename());

						DBKernel.prefs.put("LAST_MainFrame_FULL", DBKernel.mainFrame.getExtendedState() == JFrame.MAXIMIZED_BOTH ? "TRUE" : "FALSE");

						// in order to be able to save the dimension and position of the NORMAL window we have to do the following
						if (DBKernel.mainFrame.isVisible() && DBKernel.mainFrame.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
							DBKernel.mainFrame.setVisible(true);
							DBKernel.mainFrame.setExtendedState(JFrame.NORMAL);
							DBKernel.mainFrame.setExtendedState(JFrame.ICONIFIED);
						}

						DBKernel.prefs.put("LAST_MainFrame_WIDTH", DBKernel.mainFrame.getWidth() + "");
						DBKernel.prefs.put("LAST_MainFrame_HEIGHT", DBKernel.mainFrame.getHeight() + "");
						DBKernel.prefs.put("LAST_MainFrame_X", DBKernel.mainFrame.getX() + "");
						DBKernel.prefs.put("LAST_MainFrame_Y", DBKernel.mainFrame.getY() + "");

						DBKernel.prefs.prefsFlush();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			result = false;
			MyLogger.handleException(e);
		}
		return result;
	}

	public static void getPaper(final int tableID, final String tablename, final String feldname, final int blobID) {
		try {
			ResultSet rs = getResultSet(
					"SELECT "
							+ DBKernel.delimitL("Dateiname")
							+ ","
							+ DBKernel.delimitL("Datei")
							+ " FROM "
							+ delimitL("DateiSpeicher")
							+ (blobID > 0 ? " WHERE " + DBKernel.delimitL("ID") + "=" + blobID : " WHERE " + DBKernel.delimitL("Tabelle") + "='" + tablename + "' AND "
									+ DBKernel.delimitL("Feld") + "='" + feldname + "' AND " + DBKernel.delimitL("TabellenID") + "=" + tableID + " " + " ORDER BY "
									+ delimitL("ID") + " DESC"), true);
			if (rs.first()) {
				do {
					try {
						final String filename = rs.getString("Dateiname");
						final byte[] b = rs.getBytes("Datei");
						if (b != null) {
							Runnable runnable = new Runnable() {
								@Override
								public void run() {
									try {
										String tmpFolder = System.getProperty("java.io.tmpdir");
										String pathname = "";
										if (tmpFolder != null && tmpFolder.length() > 0) {
											FileOutputStream out = null;
											try {
												if (!tmpFolder.endsWith(System.getProperty("file.separator"))) {
													tmpFolder += System.getProperty("file.separator");
												}
												pathname = tmpFolder + filename;
												out = new FileOutputStream(pathname);
												out.write(b); // totalBytes
											} finally {
												if (out != null) {
													out.close();
												}
											}
											if (pathname.length() > 0) {
												Runtime.getRuntime().exec(new String[] { "rundll32", "url.dll,FileProtocolHandler", new File(pathname).getAbsolutePath() });
											}
										}
									} catch (Exception e) {
										MyLogger.handleException(e);
									}
								}
							};
							Thread thread = new Thread(runnable);
							thread.start();
						} else {
							MyLogger.handleMessage("InputStream = null\tfeldname = " + feldname + "\ttableID = " + tableID + "\tfilename = " + filename);
						}
					} catch (Exception e) {
						MyLogger.handleException(e);
					}
					break; // nur das zuletzt abgespeicherte soll geöffnet werden!
				} while (rs.next());
			}
		} catch (SQLException e) {
			MyLogger.handleException(e);
		}
	}

	private static Connection getDefaultConnection() {
		Connection result = null;
		String connStr = "jdbc:default:connection";
		try {
			result = DriverManager.getConnection(connStr);
		} catch (Exception e) {
			// MyLogger.handleException(e);
		}
		return result;
	}

	private static Connection getDBConnection(boolean suppressWarnings) throws Exception {
		return getDBConnection(HSHDB_PATH, DBKernel.m_Username, DBKernel.m_Password, false, suppressWarnings);
	}

	public static Connection getDBConnection() throws Exception {
		if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) return DBKernel.myDBi.getConn();
		return getDBConnection(false);
	}

	// Still to look at... myDBI... KNIME...
	public static Connection getDBConnection(final String username, final String password) throws Exception {
		if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) return DBKernel.myDBi.getConn();
		DBKernel.m_Username = username;
		DBKernel.m_Password = password;
		return getDBConnection(HSHDB_PATH, username, password, false);
	}

	/*
	 * // Still to look at... myDBI... KNIME... public static void
	 * setLocalConn(final Connection conn, String path, String username, String
	 * password) { localConn = conn; DBKernel.HSHDB_PATH = path;
	 * DBKernel.m_Username = username; DBKernel.m_Password = password; }
	 */
	// Still to look at... myDBI... KNIME...
	public static Connection getLocalConn(boolean autoUpdate) {
		if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) return DBKernel.myDBi.getConn();
		try {
			if ((localConn == null || localConn.isClosed()) && isKNIME) {
				localConn = getInternalKNIMEDB_LoadGui(autoUpdate);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return localConn;
	}

	public static void setCaller4Trigger(String tableName, Callable<Void> caller4Trigger) {
		if (DBKernel.myDBi != null) {
			MyTable myT = DBKernel.myDBi.getTable(tableName);
			if (myT != null) myT.setCaller4Trigger(caller4Trigger);			
		}
	}

	// Still to look at... myDBI... newConn...
	// newConn wird nur von MergeDBs benötigt
	static Connection getDBConnection(final String dbPath, final String theUsername, final String thePassword, final boolean newConn) throws Exception {
		return getDBConnection(dbPath, theUsername, thePassword, newConn, false);
	}

	private static Connection getDBConnection(final String dbPath, final String theUsername, final String thePassword, final boolean newConn, final boolean suppressWarnings)
			throws Exception {
		if (newConn) {
			return getNewConnection(theUsername, thePassword, dbPath, suppressWarnings);
		} else if (localConn == null || localConn.isClosed()) {
			localConn = getNewConnection(theUsername, thePassword, dbPath, suppressWarnings);
		}
		return localConn;
	}

	private static Connection getDefaultAdminConn(final String dbPath, final boolean newConn, final boolean suppressWarnings) throws Exception {
		Connection result = getDBConnection(dbPath, getTempSA(dbPath), getTempSAPass(dbPath), newConn, suppressWarnings);
		return result;
	}

	// Still to look at... myDBI... newConn...
	// newConn wird nur von MergeDBs und Bfrdb benötigt
	static Connection getDefaultAdminConn(final String dbPath, final boolean newConn) throws Exception {
		Connection result = getDBConnection(dbPath, getTempSA(dbPath), getTempSAPass(dbPath), newConn);
		return result;
	}

	public static Connection getDefaultAdminConn() throws Exception {
		if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) return DBKernel.myDBi.getConn(true);
		return getDefaultAdminConn(DBKernel.HSHDB_PATH, false);
	}

	private static Connection getNewConnection(final String dbUsername, final String dbPassword, final String path, final boolean suppressWarnings) throws Exception {
		// Sicherheitshalber erstmal alles wieder auf Read/Write Access setzen!
		DBKernel.prefs.putBoolean("FC_LAB_SETTINGS_DB_RO", false);
		DBKernel.prefs.prefsFlush();
		if (isServerConnection) {
			return getNewServerConnection(dbUsername, dbPassword, path, suppressWarnings);
		} else {
			return getNewLocalConnection(dbUsername, dbPassword, path + "DB", suppressWarnings);
		}
	}

	private static Connection getNewServerConnection(final String dbUsername, final String dbPassword, final String serverPath, final boolean suppressWarnings) throws Exception {
		// serverPath = "192.168.212.54/silebat";
		Connection result = null;
		passFalse = false;
		Class.forName("org.hsqldb.jdbc.JDBCDriver").getDeclaredConstructor().newInstance();
		String connStr = "jdbc:hsqldb:hsql://" + serverPath;
		try {
			result = DriverManager.getConnection(connStr, dbUsername, dbPassword);
			result.setReadOnly(DBKernel.isReadOnly());
		} catch (SQLException e) {
			/*
# invalid authorization specification
4000=28000 invalid authorization specification

# HSQLDB invalid authorization specification
4001=28501 invalid authorization specification - not found
4002=28502 invalid authorization specification - system identifier
4003=28503 invalid authorization specification - already exists
			 */
			passFalse = e.getSQLState().startsWith("28");//e.getMessage().startsWith("invalid authorization specification");
			if (!suppressWarnings) MyLogger.handleException(e);
		}
		return result;
	}

	public static boolean isHsqlServer(String checkURL) {
		boolean result = false; // checkURL.startsWith("192") ||
								// checkURL.startsWith("localhost");
		String host = "";
		try {
			if (!checkURL.startsWith("http")) {
				checkURL = "http://" + checkURL;
			}
			URL url = new URL(checkURL); // "192.168.212.54/silebat"
			host = url.getHost();
			if (!host.isEmpty()) {
				InetSocketAddress isa = new InetSocketAddress(host, 9001);// new
																			// URL(checkURL).openConnection();
				result = !isa.isUnresolved();
			}
		} catch (MalformedURLException e) {
			// e.printStackTrace();
		}
		return result;
	}

	private static Connection getNewLocalConnection(final String dbUsername, final String dbPassword, final String dbFile, final boolean suppressWarnings) throws Exception {
		if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) return DBKernel.myDBi.getConn();
		// startHsqldbServer("c:/tmp/DB", "DB");
		Connection result = null;
		passFalse = false;
		Class.forName("org.hsqldb.jdbc.JDBCDriver").getDeclaredConstructor().newInstance();
		// System.out.println(dbFile);
		String connStr = "jdbc:hsqldb:file:" + dbFile;// + ";hsqldb.write_delay=false;";
		try {
			result = DriverManager.getConnection(connStr, dbUsername, dbPassword);
			result.setReadOnly(DBKernel.isReadOnly());
		} catch (SQLException e) {
			passFalse = e.getSQLState().startsWith("28");//e.getMessage().startsWith("invalid authorization specification");
			// MyLogger.handleMessage(e.getMessage());
			if (Math.abs(e.getErrorCode()) == 451) { //e.getMessage().startsWith("Database lock acquisition failure:")) {
				Frame[] fs = Frame.getFrames();
				if (fs != null && fs.length > 0) {
					InfoBox ib = new InfoBox(fs[0], "Die Datenbank wird zur Zeit von\neinem anderen Benutzer verwendet!", true, new Dimension(300, 150), null, true);
					ib.setVisible(true);
				}
			} else {
				if (!suppressWarnings) MyLogger.handleException(e);
			}
		}
		return result;
	}

	public static Integer getMaxID(final String tablename) {
		Integer result = null;
		String sql = "SELECT TOP 1 " + delimitL("ID") + " FROM " + delimitL(tablename) + " ORDER BY " + delimitL("ID") + " DESC";
		ResultSet rs = getResultSet(sql, false);
		try {
			if (rs != null && rs.first()) {
				result = rs.getInt(1);
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static Integer getID(final String tablename, final String[] feldname, final String[] feldVal) {
		Integer result = null;
		String sql = "SELECT " + delimitL("ID") + " FROM " + delimitL(tablename) + " WHERE ";
		String where = " ";
		for (int i = 0; i < feldname.length; i++) {
			if (i < feldVal.length) {
				if (!where.trim().isEmpty()) where += " AND ";
				where += delimitL(feldname[i]);
				if (feldVal[i] == null) {
					where += " IS NULL";
				} else {
					where += " = '" + feldVal[i].replace("'", "''") + "'";
				}
			}
		}
		ResultSet rs = getResultSet(sql + where, true);
		try {
			if (rs != null && rs.last()) {
				result = rs.getInt(1);
				if (rs.getRow() > 1) {
					System.err.println("Attention! Entry occurs " + rs.getRow() + "x in table " + tablename + ", please check: '" + where + "'!!!");
				}
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static Integer getID(final String tablename, final String feldname, final String feldVal) {
		Integer result = null;
		String sql = "SELECT " + delimitL("ID") + " FROM " + delimitL(tablename) + " WHERE " + delimitL(feldname);
		if (feldVal == null) sql += " IS NULL";
		else sql += " = '" + feldVal.replace("'", "''") + "'";
		ResultSet rs = getResultSet(sql, true);
		try {
			if (rs != null && rs.last()) {
				result = rs.getInt(1);
				if (rs.getRow() > 1) {
					System.err.println("Attention! Entry " + feldVal + " occurs " + rs.getRow() + "x in column " + feldname + " of table " + tablename + ", please check!!!");
				}
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static Integer getLastID(final String tablename) {
		Integer result = null;
		String sql = "SELECT MAX(" + delimitL("ID") + ") FROM " + delimitL(tablename);
		ResultSet rs = getResultSet(sql, true);
		try {
			if (rs != null && rs.last()) {
				result = rs.getInt(1);
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static Object getValue(final String tablename, final String feldname, final String feldVal, final String desiredColumn) {
		return getValue(null, tablename, feldname, feldVal, desiredColumn);
	}

	public static Object getValue(Connection conn, final String tablename, final String feldname, final String feldVal, final String desiredColumn) {
		return getValue(conn, tablename, new String[] { feldname }, new String[] { feldVal }, desiredColumn, true);
	}

	public static Object getValue(Connection conn, final String tablename, final String[] feldname, final String[] feldVal, final String desiredColumn) {
		return getValue(conn, tablename, feldname, feldVal, desiredColumn, false);
	}

	private static Object getValue(Connection conn, final String tablename, final String[] feldname, final String[] feldVal, final String desiredColumn, boolean suppressWarnings) {
		Object result = null;
		String sql = "SELECT " + delimitL(desiredColumn) + " FROM " + delimitL(tablename) + " WHERE ";
		String where = " ";
		for (int i = 0; i < feldname.length; i++) {
			if (i < feldVal.length) {
				if (!where.trim().isEmpty()) where += " AND ";
				where += delimitL(feldname[i]);
				if (feldVal[i] == null) where += " IS NULL";
				else where += " = '" + feldVal[i].replace("'", "''") + "'";
			}
		}
		ResultSet rs = getResultSet(conn, sql + where, true);
		try {
			if (rs != null && rs.last()) { // && rs.getRow() == 1
				result = rs.getObject(1);
				if (!suppressWarnings && rs.getRow() > 1) {
					System.err.println("Attention! '" + where + "' results in " + rs.getRow() + " entries in table " + tablename + ", please check (getValue)!!!");
				}
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static boolean isDouble(final String textValue) {
		boolean result = true;
		try {
			// System.out.println(textValue);
			if (textValue.equals("-")) {
				return true;
			}
			Double.parseDouble(textValue);
		} catch (NumberFormatException e) {
			result = false;
		}
		return result;
	}

	public static boolean hasID(final String tablename, final int id) {
		boolean result = false;
		ResultSet rs = getResultSet("SELECT " + delimitL("ID") + " FROM " + delimitL(tablename) + " WHERE " + delimitL("ID") + "=" + id, true);
		try {
			if (rs != null && rs.last() && rs.getRow() == 1) {
				result = true;
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	private static String handleField(final Object id, final MyTable[] foreignFields, final String[] mnTable, final int i, final boolean goDeeper, final String startDelim,
			final String delimiter, final String endDelim, HashSet<MyTable> alreadyUsed) {
		String result = "";
		if (id == null) {
			;
		} else if (foreignFields != null && i > 1 && foreignFields.length > i - 2 && foreignFields[i - 2] != null) {
			if (goDeeper) {
				LinkedHashMap<Object, String> hashBox = fillHashtable(foreignFields[i - 2], startDelim, delimiter, endDelim,
						goDeeper && !alreadyUsed.contains(foreignFields[i - 2]), false, alreadyUsed); // " | " " ; "
				if (hashBox != null && hashBox.get(id) != null) {
					String ssttrr = hashBox.get(id).toString();
					result = ssttrr.trim().length() == 0 ? "" : ssttrr; // ft +
																		// ":\n"
																		// +
				} else if (mnTable != null && i > 1 && i - 2 < mnTable.length && mnTable[i - 2] != null && mnTable[i - 2].length() > 0) {
					result = "";
					// System.err.println("isMN..." + ft);
				} else {
					System.err.println("hashBox überprüfen...\t" + id);
					result = "";// ft + ": leer\n";
				}
			} else {
				String ft = foreignFields[i - 2].getTablename();
				result = ft + "-ID: " + id + "\n";
			}
		} else {
			result = (id instanceof Double ? DBKernel.getDoubleStr(id) : id.toString());
		}
		if (result.length() > 0) {
			if (mnTable != null && i > 1 && i - 2 < mnTable.length && mnTable[i - 2] != null && mnTable[i - 2].length() > 0) { // MN-Tabellen,
																																// wie
																																// z.B.
																																// INT
																																// oder
																																// DBL
																																// sollten
																																// hier
																																// unsichtbar
																																// bleiben!
			} else {
				//result += (newRow ? "\n" : ""); // rs.getMetaData().getColumnName(i)
				// + ": " +
			}
		} else result = "?";
		return result;
	}

	public static void refreshHashTables() {
		if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) DBKernel.myDBi.refreshHashTables();
		filledHashtables.clear();
	}

	public static LinkedHashMap<Object, String> fillHashtable(final MyTable theTable, final String startDelim, final String delimiter, final String endDelim, final boolean goDeeper) {
		return fillHashtable(theTable, startDelim, delimiter, endDelim, goDeeper, false);
	}

	public static LinkedHashMap<Object, String> fillHashtable(final MyTable theTable, final String startDelim, final String delimiter, final String endDelim,
			final boolean goDeeper, final boolean forceUpdate) {
		return fillHashtable(theTable, startDelim, delimiter, endDelim, goDeeper, forceUpdate, null);
	}

	private static LinkedHashMap<Object, String> fillHashtable(final MyTable theTable, final String startDelim, final String delimiter, final String endDelim,
			final boolean goDeeper, final boolean forceUpdate, HashSet<MyTable> alreadyUsed) {
		if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) return DBKernel.myDBi.fillHashtable(theTable, startDelim, delimiter, endDelim, goDeeper, forceUpdate,
				alreadyUsed);
		if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) {
			return myDBi.fillHashtable(theTable, startDelim, delimiter, endDelim, goDeeper, forceUpdate, alreadyUsed);
		}
		if (theTable == null) {
			return null;
		}
		String foreignTable = theTable.getTablename();
		if (forceUpdate && filledHashtables.containsKey(foreignTable)) {
			filledHashtables.remove(foreignTable);
		}
		if (filledHashtables.containsKey(foreignTable)) {
			return filledHashtables.get(foreignTable);
		}

		LinkedHashMap<Object, String> h = new LinkedHashMap<>();
		String selectSQL = theTable.getSelectSQL();
		String sql = selectSQL;
		ResultSet rs = getResultSet(sql, true);
		String value;
		int i;
		Object o = null;
		Object val = null;
		try {
			if (rs != null && rs.first()) {
				MyTable[] foreignFields = theTable.getForeignFields();
				String[] mnTable = theTable.getMNTable();
				if (alreadyUsed == null) alreadyUsed = new HashSet<>();
				alreadyUsed.add(theTable);
				boolean isdkz = theTable.getTablename().equals("DoubleKennzahlen");
				do {
					value = "";
					String valueBkp = "";
					boolean hasSymbols = false;
					if (theTable.getFields2ViewInGui() != null) {
						for (String s : theTable.getFields2ViewInGui()) {
							Integer fi = theTable.getFieldIndex(s);
							if (fi == null) {
								value += s;
								hasSymbols = true;
							} else {
								if (!value.isEmpty() && !hasSymbols) value += "\t";
								for (i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
									if (rs.getMetaData().getColumnName(i).equals(s)) {
										String tVal = handleField(rs.getObject(i), foreignFields, mnTable, i, goDeeper, startDelim, delimiter, endDelim, alreadyUsed);
										if (isdkz && tVal.equals("?") && s.equals("Exponent")) value = valueBkp;
										else value += tVal;
										break;
									}
								}
								if (isdkz && s.equals("Wert")) valueBkp = value;
							}
						}
					} else {
						for (i = 2; i <= rs.getMetaData().getColumnCount(); i++) {
							String v = handleField(rs.getObject(i), foreignFields, mnTable, i, goDeeper, startDelim, delimiter, endDelim, alreadyUsed);
							if (!v.isEmpty()) {
								v += "\t";
								String cn = rs.getMetaData().getColumnName(i);
								value += cn + ": " + v;
							}
						}
					}
					/*
					 * if (foreignTable.equals("DoubleKennzahlen") &&
					 * value.isEmpty()) { value = "..."; }
					 */
					o = rs.getObject(1);
					val = value;
					if (theTable.getTablename().equals("DoubleKennzahlen")) {
						h.put(Double.valueOf((Integer) rs.getObject(1)), value);
					} else {
						h.put(rs.getObject(1), value);
					}
				} while (rs.next());
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
			MyLogger.handleMessage(theTable.getTablename() + "\t" + o + "\t" + val + "\t" + selectSQL);
		}
		if (!filledHashtables.containsKey(foreignTable)) {
			filledHashtables.put(foreignTable, h);
		}
		return h;
	}

	public static String getDoubleStr(final Object dbl) {
		String result = null;
		if (dbl == null) {
			return null;
		}
		NumberFormat f = NumberFormat.getInstance(Locale.US);
		f.setGroupingUsed(false);
		try {
			result = f.format(dbl);
		}
		catch (Exception e) {
			System.err.println(e.getMessage() + " -> " + dbl);
		}
		return result;
	}

	public static boolean kzIsString(final String kennzahl) {
		return kennzahl.equals("Verteilung") || kennzahl.equals("Funktion (Zeit)") || kennzahl.equals("x") || kennzahl.equals("Funktion (x)");
	}

	public static boolean kzIsBoolean(final String kennzahl) {
		return kennzahl.endsWith("_g") || kennzahl.equals("Undefiniert (n.d.)");
	}

	public static Object insertDBL(final String tablename, final String fieldname, final Integer tableID, Object kzID, String kz, Object value) {
		try {
			if (kzID == null) {
				kzID = DBKernel.getValue(tablename, "ID", tableID + "", fieldname);
				if (kzID == null) {
					PreparedStatement psmt = DBKernel.getDBConnection().prepareStatement(
							"INSERT INTO " + DBKernel.delimitL("DoubleKennzahlen") + " (" + DBKernel.delimitL("Wert") + ") VALUES (NULL)", Statement.RETURN_GENERATED_KEYS);
					if (psmt.executeUpdate() > 0) {
						kzID = DBKernel.getLastInsertedID(psmt);
						DBKernel.sendRequest("UPDATE " + DBKernel.delimitL(tablename) + " SET " + DBKernel.delimitL(fieldname) + "=" + kzID + " WHERE " + DBKernel.delimitL("ID")
								+ "=" + tableID, false);
					}
				}
			}
			if (kzID == null) {
				System.err.println("eeeeeSHIIETEW...");
			} else { // UPDATE
				if (kz.indexOf("(?)") >= 0) {
					kz = kz.replace("(?)", "(x)");
				}
				if (value == null) {
					value = "NULL";
				}
				if (DBKernel.kzIsString(kz)) {
					DBKernel.sendRequest(
							"UPDATE " + DBKernel.delimitL("DoubleKennzahlen") + " SET " + DBKernel.delimitL(kz) + "='" + value + "'" + " WHERE " + DBKernel.delimitL("ID") + "="
									+ kzID, false);
				} else if (DBKernel.kzIsBoolean(kz)) {
					DBKernel.sendRequest(
							"UPDATE " + DBKernel.delimitL("DoubleKennzahlen") + " SET " + DBKernel.delimitL(kz) + "=" + value + "" + " WHERE " + DBKernel.delimitL("ID") + "="
									+ kzID, false);
				} else {
					DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("DoubleKennzahlen") + " SET " + DBKernel.delimitL(kz) + "=" + value + " WHERE " + DBKernel.delimitL("ID")
							+ "=" + kzID, false);
				}
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return kzID;
	}

	public static ResultSet getResultSet(final String sql, final boolean suppressWarnings) {
		if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) return DBKernel.myDBi.getResultSet(sql, suppressWarnings);
		ResultSet ergebnis = null;
		try {
			getDBConnection();
			Statement anfrage = localConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ergebnis = anfrage.executeQuery(sql);
			ergebnis.first();
		} catch (Exception e) {
			if (!suppressWarnings) {
				MyLogger.handleMessage(sql);
				MyLogger.handleException(e);
			}
		}
		return ergebnis;
	}

	public static ResultSet getResultSet(final Connection conn, final String sql, final boolean suppressWarnings) {
		if (conn == null) {
			return getResultSet(sql, suppressWarnings);
		} else {
			try {
				return getResultSet(conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY), sql, suppressWarnings);
			} catch (SQLException e) {
				// e.printStackTrace();
			}
		}
		return null;
	}

	static ResultSet getResultSet(final Statement anfrage, final String sql, final boolean suppressWarnings) {
		ResultSet ergebnis = null;
		try {
			ergebnis = anfrage.executeQuery(sql);
			ergebnis.first();
		} catch (Exception e) {
			if (!suppressWarnings) {
				MyLogger.handleMessage(sql);
				MyLogger.handleException(e);
			}
		}
		return ergebnis;
	}

	public static boolean sendRequest(final String sql, final boolean suppressWarnings) {
		return sendRequest(sql, suppressWarnings, false);
	}

	public static boolean sendRequest(final String sql, final boolean suppressWarnings, final boolean fetchAdminInCase) {
		if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) return DBKernel.myDBi.sendRequest(sql, suppressWarnings, fetchAdminInCase);
		try {
			Connection conn = getDBConnection();
			return sendRequest(conn, sql, suppressWarnings, fetchAdminInCase);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean sendRequest(Connection conn, final String sql, final boolean suppressWarnings, final boolean fetchAdminInCase) {
		boolean result = false;
		boolean adminGathered = false;
		try {
			if (conn == null || conn.isClosed()) conn = getDBConnection();
			if (fetchAdminInCase && !DBKernel.isAdmin()) {
				DBKernel.closeDBConnections(false);
				conn = DBKernel.getDefaultAdminConn();
				adminGathered = true;
			}
			Statement anfrage = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			anfrage.execute(sql);
			result = true;
		} catch (Exception e) {
			if (!suppressWarnings) {
				//if (!DBKernel.isKNIME || (!e.getMessage().equals("The table data is read only") && !e.getMessage().equals("invalid transaction state: read-only SQL-transaction"))) MyLogger.handleMessage(sql);
				if (!DBKernel.isKNIME || (e instanceof SQLException && Math.abs(((SQLException)e).getErrorCode()) != 451 && Math.abs(((SQLException)e).getErrorCode()) != 3706)) MyLogger.handleMessage(sql);
				MyLogger.handleException(e);
			}
		}
		if (adminGathered) {
			DBKernel.closeDBConnections(false);
			try {
				conn = DBKernel.getDBConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static Integer sendRequestGetAffectedRowNumber(Connection conn, final String sql, final boolean suppressWarnings, final boolean fetchAdminInCase) {
		Integer result = null;
		boolean adminGathered = false;
		try {
			if (fetchAdminInCase && !DBKernel.isAdmin()) {
				DBKernel.closeDBConnections(false);
				conn = DBKernel.getDefaultAdminConn();
				adminGathered = true;
			}
			Statement anfrage = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			result = anfrage.executeUpdate(sql);
		} catch (Exception e) {
			if (!suppressWarnings) {
				//if (!DBKernel.isKNIME || (!e.getMessage().equals("The table data is read only") && !e.getMessage().equals("invalid transaction state: read-only SQL-transaction"))) MyLogger.handleMessage(sql);
				if (!DBKernel.isKNIME || (e instanceof SQLException && Math.abs(((SQLException)e).getErrorCode()) != 451 && Math.abs(((SQLException)e).getErrorCode()) != 3706)) MyLogger.handleMessage(sql);
				MyLogger.handleException(e);
			}
		}
		if (adminGathered) {
			DBKernel.closeDBConnections(false);
			try {
				DBKernel.getDBConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	static String sendRequestGetErr(final String sql) {
		String result = "";
		try {
			Connection conn = getDBConnection();
			Statement anfrage = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			anfrage.execute(sql);
		} catch (Exception e) {
			result = e.getMessage();
			MyLogger.handleException(e);
		}
		return result;
	}

	public static boolean showHierarchic(final String tableName) {
		return tableName.equals("Matrices") || tableName.equals("Methoden") || tableName.equals("Agenzien") || tableName.equals("Methodiken");
	}

	static int countUsers(boolean adminsOnly) {
		Connection conn = null;
		try {
			conn = getDBConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return countUsers(conn, adminsOnly);
	}

	private static int countUsers(Connection conn, boolean adminsOnly) {
		int result = -1;
		ResultSet rs = getResultSet(conn, "SELECT COUNT(*) FROM " + delimitL("Users") + " WHERE " + (adminsOnly ? delimitL("Zugriffsrecht") + " = " + Users.ADMIN + " AND " : "")
				+ delimitL("Username") + " IS NOT NULL", true);
		try {
			if (rs != null && rs.first()) {
				result = rs.getInt(1);
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
			result = -1;
		}
		// System.out.println(result);
		return result;
	}

	public static int getRowCount(final String tableName, final String where) {
		Connection conn = null;
		try {
			conn = getDBConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getRowCount(conn, tableName, where);
	}

	public static int getRowCount(Connection conn, final String tableName, final String where) {
		int result = 0;
		String sql = "SELECT COUNT(*) FROM " + DBKernel.delimitL(tableName) + (where != null && where.trim().length() > 0 ? " " + where : "");
		ResultSet rs = DBKernel.getResultSet(conn, sql, true);
		try {
			if (rs != null && rs.first()) {
				result = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean isAdmin() {
		if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) return DBKernel.myDBi.isAdmin();
		String un = getUsername();
		return isAdmin(null, un);
	}

	private static boolean isAdmin(Connection conn, String un) { // nur der Admin kann überhaupt die Users Tabelle abfragen, daher ist ein Wert <> -1 ein Zeichen für Adminrechte, das kann auch defad sein
		if (conn == null) {
			if (un.equals(getTempSA(HSHDB_PATH))) {
				return true;
			}
		}
		boolean result = false;
		ResultSet rs = getResultSet(conn, "SELECT COUNT(*) FROM " + delimitL("Users") + " WHERE " + delimitL("Zugriffsrecht") + " = " + Users.ADMIN + " AND "
				+ delimitL("Username") + " = '" + un + "'", true);
		try {
			if (rs != null && rs.first()) {
				result = (rs.getInt(1) > (conn == null ? 0 : -1));
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static String getCodesName(final String tablename) {
		int index = tablename.indexOf("_");
		if (index >= 0) {
			return "Codes" + tablename.substring(index);
		} else {
			return "Codes_" + tablename;
		}
	}

	public static boolean DBFilesDa(String path) {
		boolean result = false;
		if (!path.endsWith(System.getProperty("file.separator"))) path += System.getProperty("file.separator");
		File f = new File(path + "DB.script");
		if (!f.exists()) {
			f = new File(path + "DB.data");
		}
		result = f.exists();
		return result;
	}

	public static long getLastCache(Connection conn, String tablename) {
		long result = 0;
		ResultSet rs = getResultSet(conn, "SELECT " + delimitL("Wert") + " FROM " + delimitL("Infotabelle") + " WHERE " + delimitL("Parameter") + " = 'lastCache_" + tablename
				+ "'", true);
		try {
			if (rs != null && rs.first()) {
				String strVal = rs.getString(1);
				result = Long.parseLong(strVal);
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static long getLastRelevantChange(Connection conn, String[] relevantTables) {
		long result = 0;
		if (relevantTables.length > 0) {
			String where = delimitL("Tabelle") + " = '" + relevantTables[0] + "'";
			for (int i = 1; i < relevantTables.length; i++) {
				where += " OR " + delimitL("Tabelle") + " = '" + relevantTables[i] + "'";
			}
			String sql = "SELECT TOP 1 " + delimitL("Zeitstempel") + " FROM " + delimitL("ChangeLog") + " WHERE " + where + " ORDER BY " + delimitL("Zeitstempel") + " DESC";
			ResultSet rs = getResultSet(conn, sql, true);
			try {
				if (rs != null && rs.first()) {
					result = rs.getTimestamp(1).getTime();
				}
			} catch (Exception e) {
				MyLogger.handleException(e);
			}
		}
		return result;
	}

	public static void setLastCache(Connection conn, String tablename, long newCacheTime) {
		try {
			boolean ro = conn.isReadOnly();
			if (ro) conn.setReadOnly(false);
			if (!sendRequest(conn, "INSERT INTO \"Infotabelle\" (\"Parameter\",\"Wert\") VALUES ('lastCache_" + tablename + "','" + newCacheTime + "')", true, false)) {
				sendRequest(conn, "UPDATE \"Infotabelle\" SET \"Wert\" = '" + newCacheTime + "' WHERE \"Parameter\" = 'lastCache_" + tablename + "'", false, false);
			}
			if (ro) conn.setReadOnly(ro);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static String getDBVersionFromDB() {
		return getDBVersionFromDB(null);
	}

	public static String getDBVersionFromDB(Connection conn) {
		String result = null;
		ResultSet rs = getResultSet(conn, "SELECT " + delimitL("Wert") + " FROM " + delimitL("Infotabelle") + " WHERE " + delimitL("Parameter") + " = 'DBVersion'", true);
		try {
			if (rs != null && rs.first()) {
				result = rs.getString(1);
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static void setDBVersion(final String dbVersion) {
		setDBVersion(null, dbVersion);
	}

	public static void setDBVersion(Connection conn, final String dbVersion) {
		if (!DBKernel.sendRequest(conn, "INSERT INTO \"Infotabelle\" (\"Parameter\",\"Wert\") VALUES ('DBVersion','" + dbVersion + "')", true, false)) {
			DBKernel.sendRequest(conn,
					"UPDATE " + DBKernel.delimitL("Infotabelle") + " SET " + DBKernel.delimitL("Wert") + " = '" + dbVersion + "'" + " WHERE " + DBKernel.delimitL("Parameter")
							+ " = 'DBVersion'", false, false);
		}
	}

	public static long getFileSize(final String filename) {
		File file = new File(filename);
		if (file == null || !file.exists() || !file.isFile()) {
			System.out.println("File doesn\'t exist");
			return -1;
		}
		return file.length();
	}

	static void grantDefaults(final String tableName) {
		DBKernel.sendRequest("GRANT SELECT ON TABLE " + DBKernel.delimitL(tableName) + " TO " + DBKernel.delimitL("PUBLIC"), false);
		if (tableName.startsWith("Codes_")) {
			DBKernel.sendRequest("GRANT SELECT ON TABLE " + DBKernel.delimitL(tableName) + " TO " + DBKernel.delimitL("WRITE_ACCESS"), false);
		} else {
			DBKernel.sendRequest("GRANT SELECT, INSERT, UPDATE ON TABLE " + DBKernel.delimitL(tableName) + " TO " + DBKernel.delimitL("WRITE_ACCESS"), false);
		}
		DBKernel.sendRequest("GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE " + DBKernel.delimitL(tableName) + " TO " + DBKernel.delimitL("SUPER_WRITE_ACCESS"), false);
	}

	// Still to look at... myDBI...KNIME...Backup...
	public static void openDBGUI() {
		if (!openingDBGUI) {
			openingDBGUI = true;
			final Connection connection = getLocalConn(true);
			try {
				connection.setReadOnly(DBKernel.isReadOnly());
				if (DBKernel.mainFrame != null && DBKernel.mainFrame.getMyList() != null && DBKernel.mainFrame.getMyList().getMyDBTable() != null) {
					DBKernel.mainFrame.getMyList().getMyDBTable().setConnection(connection);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			StartApp.go(connection);
			openingDBGUI = false;
		}
		if (DBKernel.mainFrame != null) {
			if (DBKernel.mainFrame.isVisible()) {
				/*
				java.awt.EventQueue.invokeLater(new Runnable() {
				    @Override
				    public void run() {
						  DBKernel.mainFrame.setAlwaysOnTop(true);
						  DBKernel.mainFrame.toFront();
						  DBKernel.mainFrame.requestFocus();
						  DBKernel.mainFrame.setAlwaysOnTop(false);
				    }
				});
				*/
				//DBKernel.mainFrame.setAlwaysOnTop(true);
				DBKernel.mainFrame.toFront();
				DBKernel.mainFrame.requestFocus();
				//DBKernel.mainFrame.setAlwaysOnTop(false);
			  }
		}
	}

	public static String getInternalDefaultDBPath() {
		return ResourcesPlugin.getWorkspace().getRoot().getLocation().toString().replace("/", System.getProperty("file.separator")) + System.getProperty("file.separator")
				+ (".fclabDB") + System.getProperty("file.separator");
	}

	// Still to look at... myDBI...KNIME...
	private static Connection getInternalKNIMEDB_LoadGui(boolean autoUpdate) {
		Connection result = null;
		//try {
		String internalPath = DBKernel.prefs.get("FC_LAB_SETTINGS_DB_PATH", DBKernel.prefs.get("PMM_LAB_SETTINGS_DB_PATH", getInternalDefaultDBPath()));
		CRC32 crc32 = new CRC32();
		crc32.update(internalPath.getBytes());
		long crc32Out = crc32.getValue();
		String username = DBKernel.prefs.get("FC_LAB_SETTINGS_DB_USERNAME" + crc32Out, DBKernel.prefs.get("PMM_LAB_SETTINGS_DB_USERNAME" + crc32Out, "SA"));
		String password = DBKernel.prefs.get("FC_LAB_SETTINGS_DB_PASSWORD" + crc32Out, DBKernel.prefs.get("PMM_LAB_SETTINGS_DB_PASSWORD" + crc32Out, ""));

		try {
			new Login(internalPath, username, password, DBKernel.isReadOnly(), autoUpdate);
		}
		catch (Exception he) { //HeadlessException
			he.printStackTrace();
			DBKernel.m_Username = username;
			DBKernel.m_Password = password;
			boolean noDBThere = !DBKernel.isServerConnection && !DBKernel.DBFilesDa(DBKernel.HSHDB_PATH);
			if (noDBThere) {
				File temp = DBKernel.getCopyOfInternalDB();
				if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) {
					if (BackupMyDBI.doRestore(null, temp, true, false)) {
						DBKernel.myDBi.addUserInCaseNotThere(username, password);
					}
				} else {
					if (!Backup.doRestore(null, temp, true)) { // Passwort hat sich verändert innerhalb der 2 beteiligten Datenbanken...
					}
				}
			}
			DBKernel.myDBi = new MyDBTablesNew();
		} 
		
		/*
		 * DBKernel.isServerConnection = DBKernel.isHsqlServer(internalPath); if
		 * (DBKernel.isServerConnection) { HSHDB_PATH = internalPath; try { //
		 * DBKernel.getNewServerConnection(login, pw, filename); result =
		 * DBKernel.getDBConnection(username, password); createGui(result); }
		 * catch (Exception e) { e.printStackTrace(); } } else { File
		 * incFileInternalDBFolder = new File(internalPath); if
		 * (!incFileInternalDBFolder.exists()) { if
		 * (!incFileInternalDBFolder.mkdirs()) { System.err.println(
		 * "Creation of folder for internal database not succeeded."); return
		 * null;// throw new IllegalStateException(
		 * "Creation of folder for internal database not succeeded.",
		 * null);//return null; } } if (incFileInternalDBFolder.list() == null)
		 * { System.err.println(
		 * "Creation of folderlist for internal database not succeeded.");
		 * return null;// throw new IllegalStateException(
		 * "Creation of folderlist for internal database not succeeded.",
		 * null);//return null; } // folder is empty? Create database! String[]
		 * fl = incFileInternalDBFolder.list(); boolean folderEmpty = (fl.length
		 * == 0); if (!folderEmpty) { folderEmpty = true; for (String f : fl) {
		 * if (f.startsWith("DB.")) { folderEmpty = false; break; } } } if
		 * (folderEmpty) { // Get the bundle this class belongs to. Bundle
		 * bundle = FrameworkUtil.getBundle(DBKernel.class); URL incURLfirstDB =
		 * bundle.getResource("de/bund/bfr/knime/openkrise/db/res/firstDB.tar.gz"); if
		 * (incURLfirstDB == null) { // incURLInternalDBFolder == null || return
		 * null; } File incFilefirstDB = new
		 * File(FileLocator.toFileURL(incURLfirstDB).getPath()); try {
		 * org.hsqldb.lib.tar.DbBackupMain.main(new String[] { "--extract",
		 * incFilefirstDB.getAbsolutePath(),
		 * incFileInternalDBFolder.getAbsolutePath() }); JOptionPane pane = new
		 * JOptionPane("Internal database created in folder '" +
		 * incFileInternalDBFolder.getAbsolutePath() + "'",
		 * JOptionPane.INFORMATION_MESSAGE); JDialog dialog =
		 * pane.createDialog("Internal database created");
		 * dialog.setAlwaysOnTop(true); dialog.setVisible(true); } catch
		 * (Exception e) { throw new
		 * IllegalStateException("Creation of internal database not succeeded.",
		 * e); } }
		 * 
		 * try { HSHDB_PATH = internalPath; String un =
		 * DBKernel.prefs.get("PMM_LAB_SETTINGS_DB_USERNAME", null); String pw =
		 * DBKernel.prefs.get("PMM_LAB_SETTINGS_DB_PASSWORD", null); result =
		 * getDBConnection(un != null ? un : getTempSA(HSHDB_PATH), pw != null ?
		 * pw : getTempSAPass(HSHDB_PATH));
		 * 
		 * createGui(result); if (autoUpdate) { checkUpdate(); } else { Thread
		 * queryThread = new Thread() { public void run() { try { checkUpdate();
		 * } catch (Exception e) { e.printStackTrace(); } } };
		 * queryThread.start(); } } catch (Exception e) { e.printStackTrace(); }
		 * } // DBKernel.saveUP2PrefsTEMP(HSHDB_PATH);
		 * DBKernel.getTempSA(HSHDB_PATH); } catch (IOException e) { throw new
		 * IllegalStateException
		 * ("Cannot locate necessary internal database path.", e); }
		 */
		try {
			result = DBKernel.getDBConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/*
	 * private static void checkUpdate() throws Exception { // UpdateChecker
	 * String dbVersion = DBKernel.getDBVersionFromDB(); if
	 * (!DBKernel.isServerConnection && (dbVersion == null ||
	 * !dbVersion.equals(DBKernel.softwareVersion))) { boolean dl =
	 * MainKernel.dontLog; MainKernel.dontLog = true; boolean isAdmin =
	 * DBKernel.isAdmin(); if (!isAdmin) { DBKernel.closeDBConnections(false);
	 * DBKernel.getDefaultAdminConn(); }
	 * 
	 * if (DBKernel.getDBVersionFromDB().equals("1.7.0")) {
	 * UpdateChecker.check4Updates_170_171(); DBKernel.setDBVersion("1.7.1"); }
	 * if (DBKernel.getDBVersionFromDB().equals("1.7.1")) {
	 * UpdateChecker.check4Updates_171_172(); DBKernel.setDBVersion("1.7.2"); }
	 * if (DBKernel.getDBVersionFromDB().equals("1.7.2")) {
	 * UpdateChecker.check4Updates_172_173(); DBKernel.setDBVersion("1.7.3"); }
	 * if (DBKernel.getDBVersionFromDB().equals("1.7.3")) {
	 * UpdateChecker.check4Updates_173_174(); DBKernel.setDBVersion("1.7.4"); }
	 * if (DBKernel.getDBVersionFromDB().equals("1.7.4")) {
	 * UpdateChecker.check4Updates_174_175(); DBKernel.setDBVersion("1.7.5"); }
	 * if (DBKernel.getDBVersionFromDB().equals("1.7.5")) {
	 * UpdateChecker.check4Updates_175_176(); DBKernel.setDBVersion("1.7.6"); }
	 * if (DBKernel.getDBVersionFromDB().equals("1.7.6")) {
	 * UpdateChecker.check4Updates_176_177(); DBKernel.setDBVersion("1.7.7"); }
	 * if (DBKernel.getDBVersionFromDB().equals("1.7.7")) {
	 * UpdateChecker.check4Updates_177_178(); DBKernel.setDBVersion("1.7.8"); }
	 * if (DBKernel.getDBVersionFromDB().equals("1.7.8")) {
	 * UpdateChecker.check4Updates_178_179(); DBKernel.setDBVersion("1.7.9"); }
	 * DBKernel.sendRequest("DROP TABLE " + DBKernel.delimitL("CACHE_TS") +
	 * " IF EXISTS", false, true); DBKernel.sendRequest("DROP TABLE " +
	 * DBKernel.delimitL("CACHE_selectEstModel") + " IF EXISTS", false, true);
	 * DBKernel.sendRequest("DROP TABLE " +
	 * DBKernel.delimitL("CACHE_selectEstModel1") + " IF EXISTS", false, true);
	 * DBKernel.sendRequest("DROP TABLE " +
	 * DBKernel.delimitL("CACHE_selectEstModel2") + " IF EXISTS", false, true);
	 * 
	 * if (!isAdmin) { DBKernel.closeDBConnections(false);
	 * DBKernel.getDBConnection(); if (DBKernel.mainFrame.getMyList() != null &&
	 * DBKernel.mainFrame.getMyList().getMyDBTable() != null) {
	 * DBKernel.mainFrame
	 * .getMyList().getMyDBTable().setConnection(DBKernel.getDBConnection()); }
	 * } MainKernel.dontLog = dl;
	 * 
	 * } }
	 * 
	 * public static void createGui(Connection conn) { //
	 * MyDBTables.loadMyTables(); DBKernel.myDBi = new MyDBTablesNew(); try { if
	 * ((DBKernel.mainFrame == null || DBKernel.mainFrame.getMyList() == null)
	 * && conn != null) { // Login login = new Login(); MyDBTable myDB = new
	 * MyDBTable(); myDB.initConn(conn); MyDBTree myDBTree = new MyDBTree();
	 * MyList myList = new MyList(myDB, myDBTree);
	 * 
	 * if (myList != null && myList.getMyDBTable() != null) { if
	 * (myDB.getConnection() == null || myDB.getConnection().isClosed()) {
	 * myList.getMyDBTable().setConnection(DBKernel.getDBConnection()); } }
	 * myList.addAllTables(); // login.loadMyTables(myList, null);
	 * 
	 * MainFrame mf = new MainFrame(myList); DBKernel.mainFrame = mf;
	 * myList.setSelection(DBKernel.prefs.get("LAST_SELECTED_TABLE",
	 * "Versuchsbedingungen")); try { boolean full =
	 * Boolean.parseBoolean(DBKernel.prefs.get("LAST_MainFrame_FULL", "FALSE"));
	 * 
	 * int w = Integer.parseInt(DBKernel.prefs.get("LAST_MainFrame_WIDTH",
	 * "1020")); int h =
	 * Integer.parseInt(DBKernel.prefs.get("LAST_MainFrame_HEIGHT", "700")); int
	 * x = Integer.parseInt(DBKernel.prefs.get("LAST_MainFrame_X", "0")); int y
	 * = Integer.parseInt(DBKernel.prefs.get("LAST_MainFrame_Y", "0"));
	 * mf.setPreferredSize(new Dimension(w, h)); mf.setBounds(x, y, w, h);
	 * 
	 * mf.pack(); mf.setLocationRelativeTo(null); if (full)
	 * mf.setExtendedState(JFrame.MAXIMIZED_BOTH); } catch (Exception e) { } } }
	 * catch (Exception he) { he.printStackTrace(); } // HeadlessException }
	 */
	public static String[] getItemListMisc(Connection conn) {
		HashSet<String> hs = new HashSet<>();
		try {
			ResultSet rs = null;
			String sql = "SELECT " + DBKernel.delimitL("Parameter") + " FROM " + DBKernel.delimitL("SonstigeParameter");
			rs = DBKernel.getResultSet(conn, sql, false);
			if (rs != null && rs.first()) {
				do {
					if (rs.getObject("Parameter") != null) hs.add(rs.getString("Parameter"));
				} while (rs.next());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hs.toArray(new String[] {});
	}

	public static boolean mergeIDs(Connection conn, final String tableName, int oldID, int newID) {
		ResultSet rs = null;
		String sql = "SELECT FKTABLE_NAME, FKCOLUMN_NAME FROM INFORMATION_SCHEMA.SYSTEM_CROSSREFERENCE " + " WHERE PKTABLE_NAME = '" + tableName + "'";
		try {
			rs = DBKernel.getResultSet(conn, sql, false);
			if (rs != null && rs.first()) {
				do {
					String fkt = rs.getObject("FKTABLE_NAME") != null ? rs.getString("FKTABLE_NAME") : "";
					String fkc = rs.getObject("FKCOLUMN_NAME") != null ? rs.getString("FKCOLUMN_NAME") : "";
					// System.err.println(tableName + " wird in " + fkt + "->" +
					// fkc + " referenziert");
					if (!DBKernel.sendRequest(conn, "UPDATE " + DBKernel.delimitL(fkt) + " SET " + DBKernel.delimitL(fkc) + "=" + newID + " WHERE " + DBKernel.delimitL(fkc) + "="
							+ oldID, false, false)) return false;
				} while (rs.next());
				if (DBKernel.sendRequest(conn, "DELETE FROM " + DBKernel.delimitL(tableName) + " WHERE " + DBKernel.delimitL("ID") + "=" + oldID, false, false)) {
					return true;
				}
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return false;
	}
	
	public static boolean mergeIDs(Statement statement, final String tableName, int oldID, int newID) throws SQLException {
      if(statement==null || statement.isClosed()) return false;
      
	  String sql = "SELECT FKTABLE_NAME, FKCOLUMN_NAME FROM INFORMATION_SCHEMA.SYSTEM_CROSSREFERENCE " + " WHERE PKTABLE_NAME = '" + tableName + "'";
	  ResultSet resultSet = statement.executeQuery(sql);

	  if (resultSet == null) return false;
	  
	    
	  while(resultSet.next()) {
	    String fkt = resultSet.getObject("FKTABLE_NAME") != null ? resultSet.getString("FKTABLE_NAME") : "";
	    String fkc = resultSet.getObject("FKCOLUMN_NAME") != null ? resultSet.getString("FKCOLUMN_NAME") : "";

	    sql = "UPDATE " + DBKernel.delimitL(fkt) + " SET " + DBKernel.delimitL(fkc) + "=" + newID + " WHERE " + DBKernel.delimitL(fkc) + "="
	        + oldID;
	    statement.execute(sql);
	  } 

	  sql = "DELETE FROM " + DBKernel.delimitL(tableName) + " WHERE " + DBKernel.delimitL("ID") + "=" + oldID;
	  statement.execute(sql);
	  return true;
	}

	public static int getUsagecountOfID(final String tableName, int id) {
		int result = 0;
		ResultSet rs = DBKernel.getResultSet("SELECT FKTABLE_NAME, FKCOLUMN_NAME FROM INFORMATION_SCHEMA.SYSTEM_CROSSREFERENCE " + " WHERE PKTABLE_NAME = '" + tableName + "'",
				false);
		try {
			if (rs != null && rs.first()) {
				do {
					String fkt = rs.getObject("FKTABLE_NAME") != null ? rs.getString("FKTABLE_NAME") : "";
					String fkc = rs.getObject("FKCOLUMN_NAME") != null ? rs.getString("FKCOLUMN_NAME") : "";
					// System.err.println(tableName + " wird in " + fkt + "->" +
					// fkc + " referenziert");
					ResultSet rs2 = DBKernel.getResultSet("SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL(fkt) + " WHERE " + DBKernel.delimitL(fkc) + "=" + id,
							false);
					if (rs2 != null && rs2.last()) {
						result += rs2.getRow();
					}
				} while (rs.next());
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static List<String> getUsageListOfID(final String tableName, int id) {
		List<String> result = new ArrayList<>();
		ResultSet rs = DBKernel.getResultSet("SELECT FKTABLE_NAME, FKCOLUMN_NAME FROM INFORMATION_SCHEMA.SYSTEM_CROSSREFERENCE " + " WHERE PKTABLE_NAME = '" + tableName + "'",
				false);
		try {
			if (rs != null && rs.first()) {
				do {
					String fkt = rs.getObject("FKTABLE_NAME") != null ? rs.getString("FKTABLE_NAME") : "";
					String fkc = rs.getObject("FKCOLUMN_NAME") != null ? rs.getString("FKCOLUMN_NAME") : "";
					// System.err.println(tableName + " wird in " + fkt + "->" +
					// fkc + " referenziert");
					ResultSet rs2 = DBKernel.getResultSet("SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL(fkt) + " WHERE " + DBKernel.delimitL(fkc) + "=" + id,
							false);
					if (rs2 != null && rs2.first()) {
						do {
							result.add(fkt + ": " + rs2.getInt("ID"));
						} while (rs.next());
					}
				} while (rs.next());
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static File getCopyOfInternalDB() {
		File temp = null;
		try {
			Bundle bundle = null;
			try {
				bundle = FrameworkUtil.getBundle(DBKernel.class);
			}
			catch (Exception e) {}
			if (bundle != null) {
				URL incURLfirstDB = bundle.getResource("de/bund/bfr/knime/openkrise/db/res/firstDB.tar.gz");
				if (incURLfirstDB == null) {
					return null;
				}
				temp = new File(FileLocator.toFileURL(incURLfirstDB).getPath());
			} else {
				temp = File.createTempFile("firstDB", ".tar.gz");
				InputStream in = DBKernel.class.getResourceAsStream("/de/bund/bfr/knime/openkrise/db/res/firstDB.tar.gz");
				BufferedInputStream bufIn = new BufferedInputStream(in);
				BufferedOutputStream bufOut = null;
				try {
					bufOut = new BufferedOutputStream(new FileOutputStream(temp));
				} catch (FileNotFoundException e1) {
					MyLogger.handleException(e1);
				}

				byte[] inByte = new byte[4096];
				int count = -1;
				try {
					while ((count = bufIn.read(inByte)) != -1) {
						bufOut.write(inByte, 0, count);
					}
				} catch (IOException e) {
					MyLogger.handleException(e);
				}

				try {
					bufOut.close();
				} catch (IOException e) {
					MyLogger.handleException(e);
				}
				try {
					bufIn.close();
				} catch (IOException e) {
					MyLogger.handleException(e);
				}
			}
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		return temp;
	}

	public static Integer openPrimModelDBWindow(Component parent, Integer id) {
		MyTable myT = DBKernel.myDBi.getTable("Modellkatalog");
		MyStringFilter mf = new MyStringFilter(myT, "Level", "1");
		Object newVal = DBKernel.mainFrame.openNewWindow(myT, id, "Modellkatalog", null, null, null, null, true, mf, parent);

		if (newVal instanceof Integer) {
			return (Integer) newVal;
		} else {
			return null;
		}
	}

	public static Integer openSecModelDBWindow(Component parent, Integer id) {
		MyTable myT = DBKernel.myDBi.getTable("Modellkatalog");
		MyStringFilter mf = new MyStringFilter(myT, "Level", "2");
		Object newVal = DBKernel.mainFrame.openNewWindow(myT, id, "Modellkatalog", null, null, null, null, true, mf, parent);

		if (newVal instanceof Integer) {
			return (Integer) newVal;
		} else {
			return null;
		}
	}

	public static Integer openMiscDBWindow(Component parent, Integer id) {
		MyTable myT = DBKernel.myDBi.getTable("SonstigeParameter");
		Object newVal = mainFrame.openNewWindow(myT, id, "SonstigeParameter", null, null, null, null, true, null, parent);

		if (newVal instanceof Integer) {
			return (Integer) newVal;
		} else {
			return null;
		}
	}

	public static Integer openAgentDBWindow(Component parent, Integer id) {
		MyTable myT = DBKernel.myDBi.getTable("Agenzien");
		Object newVal = mainFrame.openNewWindow(myT, id, "Agenzien", null, null, null, null, true, null, parent);

		if (newVal instanceof Integer) {
			return (Integer) newVal;
		} else {
			return null;
		}
	}

	public static Integer openMatrixDBWindow(Component parent, Integer id) {
		MyTable myT = DBKernel.myDBi.getTable("Matrices");
		Object newVal = mainFrame.openNewWindow(myT, id, "Matrices", null, null, null, null, true, null, parent);

		if (newVal instanceof Integer) {
			return (Integer) newVal;
		} else {
			return null;
		}
	}

	public static Integer openLiteratureDBWindow(Component parent, Integer id) {
		MyTable myT = DBKernel.myDBi.getTable("Literatur");
		Object newVal = mainFrame.openNewWindow(myT, id, "Literatur", null, null, null, null, true, null, parent);

		if (newVal instanceof Integer) {
			return (Integer) newVal;
		} else {
			return null;
		}
	}

	public static String getLocalDBUUID() {
		try {
			return getDBUUID(getLocalConn(true), true);
		} catch (SQLException e) {
			return null;
		}
	}

	private static String getDBUUID(Connection conn, boolean tryOnceAgain) throws SQLException {
		String result = null;
		ResultSet rs = getResultSet(conn, "SELECT \"Wert\" FROM \"Infotabelle\" WHERE \"Parameter\" = 'DBuuid'", false);
		if (rs != null && rs.first()) {
			result = rs.getString(1);
		}
		if (tryOnceAgain && result == null) {
			setDBUUID(conn, UUID.randomUUID().toString());
			result = getDBUUID(conn, false);
		}
		return result;
	}

	public static boolean isReadOnly() {
		return DBKernel.isKNIME && DBKernel.prefs.getBoolean("FC_LAB_SETTINGS_DB_RO", DBKernel.prefs.getBoolean("PMM_LAB_SETTINGS_DB_RO", false)) || !DBKernel.isKNIME && DBKernel.prefs.getBoolean("DB_READONLY", true);
	}

	private static void setDBUUID(Connection conn, final String uuid) throws SQLException {
		conn.setReadOnly(false);
		//sendRequest(conn, "INSERT INTO \"Infotabelle\" (\"Parameter\",\"Wert\") VALUES ('DBuuid','" + uuid + "')", false, false);
		if (!sendRequest(conn, "INSERT INTO \"Infotabelle\" (\"Parameter\",\"Wert\") VALUES ('DBuuid','" + uuid + "')", true, false)) {
			sendRequest(conn, "UPDATE \"Infotabelle\" SET \"Wert\" = '" + uuid + "' WHERE \"Parameter\" = 'DBuuid'", false, false);
		}
		conn.setReadOnly(DBKernel.isReadOnly());
	}

	public static void getKnownIDs4PMM(Connection conn, HashMap<Integer, Integer> foreignDbIds, String tablename, String rowuuid) {
		String sql = "SELECT " + DBKernel.delimitL("TableID") + "," + DBKernel.delimitL("SourceID") + " FROM " + DBKernel.delimitL("DataSource") + " WHERE ";
		sql += DBKernel.delimitL("Table") + "=" + "'" + tablename + "' AND";
		sql += DBKernel.delimitL("SourceDBUUID") + "=" + "'" + rowuuid + "';";

		ResultSet rs = DBKernel.getResultSet(conn, sql, true);
		try {
			if (rs != null && rs.first()) {
				do {
					if (rs.getObject("SourceID") != null && rs.getObject("TableID") != null) {
						foreignDbIds.put(rs.getInt("SourceID"), rs.getInt("TableID"));
					}
				} while (rs.next());
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
	}

	private static void setKnownIDs4PMM(Connection conn, HashMap<Integer, Integer> foreignDbIds, String tablename, String rowuuid) {
		for (Integer sID : foreignDbIds.keySet()) {
			Object id = DBKernel.getValue(conn, "DataSource", new String[] { "Table", "SourceDBUUID", "SourceID" }, new String[] { tablename, rowuuid, sID + "" }, "TableID");
			if (id == null) {
				String sql = "INSERT INTO " + DBKernel.delimitL("DataSource") + " (" + DBKernel.delimitL("Table") + "," + DBKernel.delimitL("TableID") + ","
						+ DBKernel.delimitL("SourceDBUUID") + "," + DBKernel.delimitL("SourceID") + ") VALUES ('" + tablename + "'," + foreignDbIds.get(sID) + ",'" + rowuuid
						+ "'," + sID + ");";
				DBKernel.sendRequest(conn, sql, true, false);
			}
		}
	}
	public static void setKnownIDs4PMM(Connection conn, HashMap<String, HashMap<String, HashMap<Integer, Integer>>> foreignDbIds) {
		for (String rowuuid : foreignDbIds.keySet()) {
			HashMap<String, HashMap<Integer, Integer>> hm = foreignDbIds.get(rowuuid);
			for (String tableName : hm.keySet()) {
				DBKernel.setKnownIDs4PMM(conn, hm.get(tableName), tableName, rowuuid);
			}
		}
	}
}
