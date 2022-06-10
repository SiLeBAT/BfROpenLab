/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;

public class MetaInfo {

	public String getReporter() {
		return reporter;
	}
	public void setReporter(String reporter) {
		this.reporter = reporter;
	}
	public String getDate() {
		return strDate;
	}
	public void setDate(String strDate) {
		this.strDate = strDate;
	}
	public Integer getDateDay() {
		return dateDay;
	}
	public void setDateDay(Integer dateDay) {
		this.dateDay = dateDay;
	}
	public Integer getDateMonth() {
		return dateMonth;
	}
	public void setDateMonth(Integer dateMonth) {
		this.dateMonth = dateMonth;
	}
	public Integer getDateYear() {
		return dateYear;
	}
	public void setDateYear(Integer dateYear) {
		this.dateYear = dateYear;
	}
	public long getDateInMillis() {
		if (dateDay != null && dateMonth != null && dateYear != null) {
			Calendar c = Calendar.getInstance();
			c.set(dateYear, dateMonth - 1, dateDay);  
			return c.getTimeInMillis();
		}
		else if (strDate != null && !strDate.isEmpty()) {
			  List<SimpleDateFormat> knownPatterns = new ArrayList<SimpleDateFormat>();
			  knownPatterns.add(new SimpleDateFormat("dd.MM.yyyy"));
			  knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd"));

			  String str_date = this.getDate();
			  if (str_date != null) {
				  str_date = str_date.trim();
				  for (DateFormat formatter : knownPatterns) {
				      try {
						Date d = formatter.parse(str_date);
						if (d != null) return d.getTime();	
				      } catch (ParseException pe) {
				          // Loop on
				      }
				  }
			  }
		}
		return 0L;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	private String reporter;
	private String strDate;
	private Integer dateDay;
	private Integer dateMonth;
	private Integer dateYear;
	private String remarks;
	private String filename;
	
	
	private String logMessages = "";
	
	public String getLogMessages() {
		return logMessages;
	}
	public Integer getID(MyDBI mydbi) throws Exception {
		Integer result = null;
		String s1 = MyDBI.delimitL("filename");
		String s2 = "'" + filename + "'";
		if (reporter != null && !reporter.isEmpty()) {
			s1 += "," + MyDBI.delimitL("reporter");
			s2 += ",'" + reporter + "'";
		}
		if (dateDay != null && dateMonth != null && dateYear != null) {
			s1 += "," + MyDBI.delimitL("date");
			s2 += ",'" + dateDay + "." + dateMonth + "." + dateYear + "'";
		}
		else if (strDate != null && !strDate.isEmpty()) {
			s1 += "," + MyDBI.delimitL("date");
			s2 += ",'" + strDate + "'";
		}
		if (remarks != null && !remarks.isEmpty()) {
			s1 += "," + MyDBI.delimitL("remarks");
			s2 += ",'" + remarks + "'";
		}
		try {
			String sql = "INSERT INTO " + MyDBI.delimitL("ImportMetadata") + " (" + s1 + ") VALUES (" + s2 + ")";
			@SuppressWarnings("resource")
			Connection conn = (mydbi != null ? mydbi.getConn() : DBKernel.getDBConnection());
			PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			if (ps.executeUpdate() > 0) {
				result = (mydbi != null ? mydbi.getLastInsertedID(ps) : DBKernel.getLastInsertedID(ps));
			}
		}
		catch (Exception e) {System.err.println(e.getMessage());}

		return result;
	}
}
