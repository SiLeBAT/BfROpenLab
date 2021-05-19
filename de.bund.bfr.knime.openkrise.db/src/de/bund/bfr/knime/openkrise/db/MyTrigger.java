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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.hsqldb.Trigger;

/**
 * @author Armin
 *
 */

public class MyTrigger implements Trigger {
		
	public static long triggerFired = System.currentTimeMillis();

	@Override
	public void fire(final int triggerType, final String triggerName, final String tableName, final Object rowBefore[], final Object rowAfter[]) {
        try {
        	if (triggerType == Trigger.INSERT_BEFORE_ROW || triggerType == Trigger.UPDATE_BEFORE_ROW || triggerType == Trigger.DELETE_BEFORE_ROW) {
        		if (tableName.equals("Users")) {
        			if (triggerType == Trigger.INSERT_BEFORE_ROW) {
	          			//System.out.println(rowBefore + "\t" + rowAfter[4]);
	          			if (rowAfter[4] == null) {
							rowAfter[4] = new Integer(Users.READ_ONLY);
						}        				
        			}
        			else if (triggerType == Trigger.UPDATE_BEFORE_ROW) {
        				// Achtung: Aktiver User darf die Kennung nicht ändern. Daher: Änderung nicht zulassen!!!
	        	  		if (rowBefore != null && rowBefore[1] != null) {
	        	  			String un = MainKernel.getUsername(); 
	        	  			if (un.length() == 0) {
								MyLogger.handleMessage("ServerKernel.getUsername()...WWEWEW: " + rowBefore[1].toString());
							}
		    	  			if (un.equals(rowBefore[1].toString())) { // klappt das denn auch mit ServerKernel???
		        	  				if (rowAfter[1] == null || !rowAfter[1].toString().equals(rowBefore[1].toString())) {
		        	  					rowAfter[1] = rowBefore[1].toString();
		        	  				}
		        	  			}
		        	  	}
	        	  		/*
        				// Achtung: es sollte immer mindestens ein Admin vorhanden sein. Daher: Änderung nicht zulassen!!!
        				if (rowBefore != null && rowBefore[4] != null) {
        					int oldAccRight = ((Integer) rowBefore[4]).intValue();
        					if (oldAccRight == Users.ADMIN) {
        						int newAccRight = (rowAfter == null || rowAfter[4] == null) ? -1 : ((Integer) rowAfter[4]).intValue();
        						if (newAccRight != oldAccRight) {
	          						if (DBKernel.countUsers(true) == 1) {
			                  			//System.out.println(DBKernel.countUsers(true));     
			                  			rowAfter[4] = new Integer(Users.ADMIN);  
	          						}        							
        						}
        					}
        				}
        				*/
        				// Achtung: Usernamen die leer sind werden nicht zugelassen
        				if (rowAfter != null && (rowAfter[1] == null || rowAfter[1].toString().length() == 0)) {
        					if (rowBefore[1] == null) {
								rowAfter[1] = rowBefore[1];
							} else {
								rowAfter[1] = rowBefore[1].toString();
							}
        				}
        				// Userrechte sollten auch nicht leer sein 
        				if (rowAfter != null && rowAfter[4] == null) {
        					int oldAccRight = (rowBefore == null || rowBefore[4] == null) ? Users.READ_ONLY : ((Integer) rowBefore[4]).intValue();
        					rowAfter[4] = new Integer(oldAccRight);
        				}
        			}
        		}
        		else if (tableName.equals("ProzessWorkflow") && triggerType == Trigger.UPDATE_BEFORE_ROW) { // XML sollte nicht gelöscht werden dürfen!
        			if (rowAfter != null && (rowAfter[9] == null)) { // XML
    					rowAfter[9] = rowBefore[9];
        			}
			    }
        	}        		
        	else if (triggerType == Trigger.INSERT_AFTER_ROW || triggerType == Trigger.UPDATE_AFTER_ROW || triggerType == Trigger.DELETE_AFTER_ROW) {
          	
	          	if (tableName.equals("Users")) {
	          		if (triggerType == Trigger.DELETE_AFTER_ROW) {
	          			deleteUser(rowBefore);          			
	          		}
	          		else if (triggerType == Trigger.INSERT_AFTER_ROW) {
	          			changeUser(rowBefore, rowAfter);         
	          		}
	          		else {
	            		changeUser(rowBefore, rowAfter);          			
	          		}
	          	}

	          	if (!MainKernel.dontLog) {
	          		if (!insertIntoChangeLog(tableName, rowBefore, rowAfter, false)) {
	          			MyLogger.handleMessage("Something went wrong in MyTrigger...." + tableName + "\t" + rowBefore + "\t" + rowAfter);
	          		}
	          	}
	          	
        	}
        	
        	if (DBKernel.myDBi != null) {
        		MyTable myT = DBKernel.myDBi.getTable(tableName);
            	if (myT != null && myT.getCaller4Trigger() != null) {
        			  try {myT.getCaller4Trigger().call();}
        			  catch (Exception e) {e.printStackTrace();}
            	}
        	}
        	
        }
        catch (Exception e) {
        	MyLogger.handleException(e);
        }
  }

  private void changeUser(final Object oldUser[], final Object newUser[]) {
		if (newUser != null && newUser[1] != null && newUser[1].toString().length() > 0) {
			String newUsername = newUser[1].toString();
			int newAccRight = Users.READ_ONLY;
			if (newUser[4] != null && newUser[4] instanceof Integer) {
				newAccRight = ((Integer) newUser[4]).intValue();
			}
		  	// 1. komplette Neudefinition
		  	if (oldUser == null || oldUser[1] == null) {
		  		createUser(newUsername, newAccRight);
		  	}
		  	else {
		  		int oldAccRight = (oldUser[4] == null) ? Users.READ_ONLY : ((Integer) oldUser[4]).intValue();
		  		String oldUsername = oldUser[1].toString();
			  	// 2. Username hat sich geändert
		  		if (oldUsername.length() > 0 && !oldUsername.equals(newUsername)) {
			  		if (createUser(newUsername, newAccRight)) {
						deleteUser(oldUser);
					}	  			
		  		}
			  	// 3. Access Rights haben sich geändert
			  	else if (oldAccRight != newAccRight) {
			  		if (removeAccRight(newUsername, oldAccRight)) {
						createAccRight(newUsername, newAccRight);
					}
			  	}	  	
		  	}
		}
  }
  private boolean removeAccRight(final String username, final int oldAccRight) {
  	boolean success = true;
  	//System.out.println("removeAccRight\t" + username + "\t" + oldAccRight + "\t");
  	if (oldAccRight == Users.ADMIN) {
		success = MainKernel.sendRequest("REVOKE " + MainKernel.delimitL("DBA") + " FROM " + MainKernel.delimitL(username) + " RESTRICT", false);
	} else if (oldAccRight == Users.SUPER_WRITE_ACCESS) {
		success = MainKernel.sendRequest("REVOKE " + MainKernel.delimitL("SUPER_WRITE_ACCESS") + " FROM " + MainKernel.delimitL(username) + " RESTRICT", false);
	} else if (oldAccRight == Users.WRITE_ACCESS) {
		success = MainKernel.sendRequest("REVOKE " + MainKernel.delimitL("WRITE_ACCESS") + " FROM " + MainKernel.delimitL(username) + " RESTRICT", false);
	}			
  	return success;
  }
  private boolean createAccRight(final String username, final int newAccRight) {
  	boolean success = true;
  	//System.out.println("createAccRight\t" + username + "\t" + newAccRight + "\t");
  	if (newAccRight == Users.ADMIN) {
		success = MainKernel.sendRequest("GRANT " + MainKernel.delimitL("DBA") + " TO " + MainKernel.delimitL(username), false);
	} else if (newAccRight == Users.SUPER_WRITE_ACCESS) {
		success = MainKernel.sendRequest("GRANT " + MainKernel.delimitL("SUPER_WRITE_ACCESS") + " TO " + MainKernel.delimitL(username), false);
	} else if (newAccRight == Users.WRITE_ACCESS) {
		success = MainKernel.sendRequest("GRANT " + MainKernel.delimitL("WRITE_ACCESS") + " TO " + MainKernel.delimitL(username), false);
	}			
  	return success;
  }
  private boolean createUser(final String username, final int accRight) {
		boolean success = MainKernel.sendRequest("CREATE USER " + MainKernel.delimitL(username) + " PASSWORD ''", true); // + (accRight == Users.ADMIN ? " ADMIN" : "") " + MD5.encode("", "UTF-8") + "
  	//System.out.println("createUser\t" + username + "\t" + accRight + "\t");
		if (success) {
			success = createAccRight(username, accRight);
		}
  	return success;
  }
  private boolean deleteUser(final Object oldUser[]) {
		if (oldUser != null && oldUser[1] != null && oldUser[1].toString().length() > 0) {
			String username = oldUser[1].toString();
	  	//System.out.println("deleteUser\t" + username);
  		return MainKernel.sendRequest("DROP USER " + MainKernel.delimitL(username), false);
  	}
		return false;
  }

  
	private boolean insertIntoChangeLog(final String tablename, final Object[] rowBefore, final Object[] rowAfter, final boolean suppressWarnings) {
		if (MainKernel.dontLog || DBKernel.dontLog) return true;
		else {
			boolean diff = different(rowBefore, rowAfter);
			if (!diff) return true;
			boolean result = false;
			try {
		    	Connection conn = getDefaultConnection();
		    	String username = getUsername(conn);
				PreparedStatement ps = conn.prepareStatement("INSERT INTO " + MainKernel.delimitL("ChangeLog") + " (" + MainKernel.delimitL("ID") + ", "
						+ MainKernel.delimitL("Zeitstempel") + ", " + MainKernel.delimitL("Username") + ", " + MainKernel.delimitL("Tabelle") + ", "
						+ MainKernel.delimitL("TabellenID") + ", " + MainKernel.delimitL("Alteintrag") + ") VALUES (NEXT VALUE FOR "
						+ MainKernel.delimitL("ChangeLogSEQ") + ", ?, ?, ?, ?, ?)");

				ps.setTimestamp(1, new Timestamp(new Date().getTime()));
				ps.setString(2, username);
				ps.setString(3, tablename);
				int tableID;
				if (rowBefore != null && rowBefore.length > 0 && rowBefore[0] != null && rowBefore[0] instanceof Integer) {
					tableID = (Integer) rowBefore[0];
				}
				else if (rowAfter != null && rowAfter.length > 0 && rowAfter[0] != null && rowAfter[0] instanceof Integer) {
					tableID = (Integer) rowAfter[0];
				}
				else {
					tableID = -1;
				}
				ps.setInt(4, tableID);
				check4SerializationProblems(rowBefore);
				ps.setObject(5, rowBefore);
				triggerFired = System.currentTimeMillis();
				ps.execute();
				result = true;
			}
			catch (Exception e) {
				if (!suppressWarnings) {
					MyLogger.handleMessage(tablename + ": " + eintragAlt2String(rowBefore) + "\t" + eintragAlt2String(rowAfter));
					MyLogger.handleException(e, true);
				}
			}
			return result;
		}
	}

	private void check4SerializationProblems(final Object[] rowBefore) {
		if (rowBefore == null) {
			return;
		}
		for (int i = 0; i < rowBefore.length; i++) {
			if (rowBefore[i] instanceof org.hsqldb.types.TimestampData) {
				rowBefore[i] = ((org.hsqldb.types.TimestampData) rowBefore[i])
						.getSeconds();
				// Long d = (Long) rowBefore[i];
				// System.err.println(d + "\t" + rowBefore[i]);
			}
		}
	}

	private String eintragAlt2String(final Object[] eintragAlt) {
		if (eintragAlt == null) {
			return null;
		}
		String result = eintragAlt[0].toString();
		for (int i = 1; i < eintragAlt.length; i++) {
			result += "," + eintragAlt[i];
		}
		return result;
	}
	private boolean different(final Object[] rowBefore,
			final Object[] rowAfter) {
		if (rowBefore == null && rowAfter == null) {
			return false;
		}
		if (rowBefore == null && rowAfter != null || rowBefore != null
				&& rowAfter == null) {
			return true;
		}
		if (rowBefore.equals(rowAfter)) {
			return false;
		}
		for (int i = 0; i < rowBefore.length; i++) {
			if (rowBefore[i] == null && rowAfter[i] == null) {
				;
			} else if (rowBefore[i] == null && rowAfter[i] != null
					|| rowAfter[i] == null && rowBefore[i] != null
					|| !rowBefore[i].toString().equals(rowAfter[i].toString())) {
				return true;
			}
		}
		return false;
	}
  	private Connection getDefaultConnection() {
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
	private String getUsername(Connection conn) {
	  	String username = "";
		try {
			if (conn != null) {
				username = conn.getMetaData().getUserName();
			}
		}
		catch (SQLException e) {
			MyLogger.handleException(e);
		} 
	  	return username;
	}
}
