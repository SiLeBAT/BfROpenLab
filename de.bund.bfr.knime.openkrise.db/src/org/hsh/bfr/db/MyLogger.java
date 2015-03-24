/*******************************************************************************
 * PMM-Lab � 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab � 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * Christian Th�ns (BfR)
 * Matthias Filter (BfR)
 * Armin A. Weiser (BfR)
 * Alexander Falenski (BfR)
 * J�rgen Brandt (BfR)
 * Annemarie K�sbohrer (BfR)
 * Bernd Appel (BfR)
 * 
 * PMM-Lab is a project under development. Contributions are welcome.
 * 
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
 ******************************************************************************/
/**
 * 
 */
package org.hsh.bfr.db;

/**
 * @author Armin
 *
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
		if (isKNIME && e.getMessage() != null && (e.getMessage().equals("The table data is read only") || e.getMessage().equals("invalid transaction state: read-only SQL-transaction"))) {
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
