/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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

import java.awt.Font;
import java.sql.Connection;
import java.util.LinkedHashMap;

import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import com.jgoodies.looks.windows.WindowsLookAndFeel;

import de.bund.bfr.knime.openkrise.db.gui.Login;

/**
 * @author Armin
 *
 */

public class StartApp {

	public static void main(final String[] args) {
	    try {
	        //UIManager.setLookAndFeel(new com.jgoodies.looks.plastic.Plastic3DLookAndFeel()); // .plastic.Plastic3DLookAndFeel() .windows.WindowsLookAndFeel()
	        //UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel"); // PlasticXPLookAndFeel Plastic3DLookAndFeel
	        //UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
	        UIManager.setLookAndFeel(new WindowsLookAndFeel());
	        /*
	        UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
	        UIManager.put("Table.gridColor", Color.black);
	        UIManager.put("Table.selectionBackground", Color.white);
	        UIManager.put("Table.selectionForeground", Color.black);
	        UIManager.put("Table.focusCellBackground", Color.white);
	        UIManager.put("Table.focusCellForeground", Color.black);
	         */
	  		setUIFont(new javax.swing.plaf.FontUIResource("Tahoma",Font.PLAIN,13));
	      }
	      catch (Exception e) {MyLogger.handleException(e);}
	      System.setProperty("line.separator", "\n"); // Damit RDiff auch funktioniert, sonst haben wir einmal (unter Windows) "\r\n" und bei Linux nur "\n"

	      go(null);
	}
	static void go(final Connection conn) {
		if (!DBKernel.debug) {
	  		MyLogger.setup(DBKernel.HSH_PATH + "LOGs" + System.getProperty("file.separator") + "log_" + System.currentTimeMillis() + ".txt");
	  	}
		MyLogger.handleMessage(System.getProperty("java.version") + "\t" + (Runtime.getRuntime().maxMemory()/1024/1024)+ " MB"); // -Xms256m -Xmx1g
	      
	  	ToolTipManager ttm = null;
	  	ttm = ToolTipManager.sharedInstance();
	  	ttm.setInitialDelay(0);
	  	ttm.setDismissDelay(60000);
	  	
	  	if (conn == null) {
	  		Login login = new Login(true);
	  		login.setVisible(true);	    	  
	  	}
	  	else {
	  		/*
    	  	MyTable myT = DBKernel.myDBi.getTable("GeschaetzteModelle"); if (myT != null) myT.doMNs();
    	  	myT = DBKernel.myDBi.getTable("Modellkatalog"); if (myT != null) myT.doMNs();
    	  	myT = DBKernel.myDBi.getTable("Versuchsbedingungen"); if (myT != null) myT.doMNs();
    	  	*/
	  		// refresh MNs - just to be safe...
	  		LinkedHashMap<String, MyTable> at = DBKernel.myDBi.getAllTables();
	  		for (MyTable myT : at.values()) {
	  			myT.doMNs();
	  		}

    	  	DBKernel.mainFrame.getMyList().getMyDBTable().setTable();
    	  	
    	  	DBKernel.mainFrame.setTitle("internal DB    [in '" + DBKernel.HSHDB_PATH + "']");

    	  	DBKernel.mainFrame.toFront();	    
    	  	DBKernel.mainFrame.setVisible(true);
	  	}
	  	/*
	  	DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("Infotabelle") + " WHERE " + DBKernel.delimitL("Parameter") + " = 'DBuuid'", false);
	  	DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("ChangeLog"), false);
	  	*/
	}
	private static void setUIFont(final javax.swing.plaf.FontUIResource f){
	    //
	    // sets the default font for all Swing components.
	    // ex. 
	    //  setUIFont (new javax.swing.plaf.FontUIResource("Serif",Font.ITALIC,12));
	    //
	    java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
	    while (keys.hasMoreElements()) {
	      Object key = keys.nextElement();
	      Object value = UIManager.get (key);
	      if (value instanceof javax.swing.plaf.FontUIResource) {
			UIManager.put (key, f);
		}
	      }
	    }   
	/*
	private static void testFocus() {

		final Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				if (false) {
					timer.cancel();
				} else {
					// null is returned if none of the components in this application has the focus
					Component compFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

					// null is returned if none of the windows in this application has the focus
					Window windowFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
					
					System.out.println(windowFocusOwner + "\n" + compFocusOwner);					
				}
			}
		};
		//start des Timers:
		timer.scheduleAtFixedRate(task, 0, 1000);
		// wiederholt sich unendlich immer nach einer Sekunde (1000 Millisekunden)
	}
	*/
}
