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

/**
 * @author Armin
 *
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MyLogger {

	private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.S");
	public static boolean isKNIME = false;

	static void setup(String path) {
		try {
			File file = new File(path);
			file.getParentFile().mkdirs();
			FileOutputStream fos = new FileOutputStream(file);
			System.setOut(new PrintStream(fos));
			System.setErr(new PrintStream(fos));
		} catch (IOException e) {
			handleException(e);
		}
	}

	public static void handleException(Exception e) {
		handleException(e, false);
	}

	static void handleException(Exception e, boolean forceMessage) {
		if (isKNIME && e != null && (e instanceof SQLException && Math.abs(((SQLException)e).getErrorCode()) == 451 && Math.abs(((SQLException)e).getErrorCode()) == 3706)) {
		//if (isKNIME && e.getMessage() != null && (e.getMessage().equals("The table data is read only") || e.getMessage().equals("invalid transaction state: read-only SQL-transaction"))) {
			;
		} else {
			Calendar c1 = Calendar.getInstance();
			System.err.println("Datum: " + sdf.format(c1.getTime()));
			e.printStackTrace();
			System.err.println("\n" + e.getMessage());
			if (!MainKernel.isServer()) {
				checkOOM(e.getMessage());
				if (forceMessage) {
					//InfoBox ib = new InfoBox("Bitte mal bei Armin melden!\n(Tel.: 030-18412 2118, E-Mail: armin.weiser@bfr.bund.de)\n" + e.getMessage(), true, new Dimension(750, 300), null, false);
					//ib.setVisible(true);    				  										        												
				}
			}
		}
	}

	public static void handleMessage(String message) {
		Calendar c1 = Calendar.getInstance();
		System.out.println("Datum: " + sdf.format(c1.getTime()));
		System.out.println("\t" + message + "\n");
		if (!MainKernel.isServer()) checkOOM(message);
	}

	private static void checkOOM(String msg) {

		if (msg != null) {
			if (msg.indexOf("emory") >= 0) {
				//InfoBox ib = new InfoBox("OutOfMemory!!! Bitte mal bei Armin melden!\n(Tel.: 030-18412 2118, E-Mail: armin.weiser@bfr.bund.de)", true, new Dimension(750, 300), null, false);
				//ib.setVisible(true);    				  										        								
			} else if (msg.indexOf("logSevereEvent") >= 0) {
				//InfoBox ib = new InfoBox("logSevereEvent!!! Bitte mal bei Armin melden!\n(Tel.: 030-18412 2118, E-Mail: armin.weiser@bfr.bund.de)", true, new Dimension(750, 300), null, false);
				//ib.setVisible(true);    				  										        								
			}
		}

	}
}
