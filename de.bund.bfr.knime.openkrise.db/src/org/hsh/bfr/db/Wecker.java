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
 * @author Weiser
 *
 */
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


class Wecker {

    private final Timer timer = new Timer();
    private final double hours;

    Wecker(double hours) {
      this.hours = hours;
    }

    
    void start() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int diesestunde = cal.get(Calendar.HOUR_OF_DAY);
		int delayStunden = (27 - diesestunde); // nachts um ca. 3 Uhr bitte alle Backups machen!
		MyLogger.handleMessage("Stunden bis zum ersten Backup: " + delayStunden);
		timer.schedule(new TimerTask() {
	        public void run() {
	        	/*
	    		Calendar cal = Calendar.getInstance();
	    		cal.setTime(new Date());
	    		int now = cal.get(Calendar.HOUR_OF_DAY);
	        	if (now > 7 && now < 20) MainKernel.dbBackup();
	        	*/
	        	MainKernel.dbBackup();
	        }
    	},
    	(long) (delayStunden * 60 * 60 * 1000), // delayStunden 0
    	(long) (hours * 60 * 60 * 1000)); // hours 1
    }
}
