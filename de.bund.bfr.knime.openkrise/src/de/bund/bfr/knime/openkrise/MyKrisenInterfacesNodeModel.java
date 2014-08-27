/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Armin A. Weiser (BfR)
 * Christian Thoens (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
package de.bund.bfr.knime.openkrise;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.hsh.bfr.db.DBKernel;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.data.xml.XMLCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.thoughtworks.xstream.XStream;

import de.bund.bfr.knime.openkrise.MyNewTracing;

/**
 * This is the model implementation of MyKrisenInterfaces.
 * 
 * 
 * @author draaw
 */
public class MyKrisenInterfacesNodeModel extends NodeModel {

	
	static final String PARAM_FILENAME = "filename"; static final String
	PARAM_LOGIN = "login"; static final String PARAM_PASSWD = "passwd";
	static final String PARAM_OVERRIDE = "override";
	 
	static final String PARAM_ANONYMIZE = "anonymize";
	
	private String filename; private String login; private String passwd;
	private boolean override;
	
	private boolean doAnonymize;

	private boolean isDE = false;

	/**
	 * Constructor for the node model.
	 */
	protected MyKrisenInterfacesNodeModel() {
		super(0, 3);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {
	
		Connection conn = override ? getNewLocalConnection(login, passwd, filename) : null;
		//if (doAnonymize) doAnonymizeHard(conn);
	

		System.err.println("Starting Plausibility Checks...");
		// Date_In <= Date_Out???
		String sql = "SELECT \"ChargenVerbindungen\".\"ID\" AS \"ID\", \"L1\".\"ID\" AS \"ID_In\", \"L2\".\"ID\" AS \"ID_Out\", \"L1\".\"dd_day\" AS \"Day_In\",\"L2\".\"dd_day\" AS \"Day_Out\", \"L1\".\"dd_month\" AS \"Month_In\",\"L2\".\"dd_month\" AS \"Month_Out\", \"L1\".\"dd_year\" AS \"Year_In\",\"L2\".\"dd_year\" AS \"Year_Out\" FROM \"Lieferungen\" AS \"L1\" LEFT JOIN \"ChargenVerbindungen\" ON \"L1\".\"ID\"=\"ChargenVerbindungen\".\"Zutat\" LEFT JOIN \"Lieferungen\" AS \"L2\" ON \"L2\".\"Charge\"=\"ChargenVerbindungen\".\"Produkt\" WHERE \"ChargenVerbindungen\".\"ID\" IS NOT NULL AND (\"L2\".\"dd_year\" < \"L1\".\"dd_year\" OR \"L2\".\"dd_year\" = \"L1\".\"dd_year\" AND \"L2\".\"dd_month\" < \"L1\".\"dd_month\" OR \"L2\".\"dd_year\" = \"L1\".\"dd_year\" AND \"L2\".\"dd_month\" = \"L1\".\"dd_month\" AND \"L2\".\"dd_day\" < \"L1\".\"dd_day\")";
		ResultSet rsp = DBKernel.getResultSet(conn, sql, false);
		if (rsp != null && rsp.first()) {
			do {
				System.err.println("Dates correct?? In: " + rsp.getInt("ID_In") + " (" + rsp.getInt("Day_In") + "." + rsp.getInt("Month_In") + "." + rsp.getInt("Year_In")
						+ ") vs. Out: " + rsp.getInt("ID_Out") + " (" + rsp.getInt("Day_Out") + "." + rsp.getInt("Month_Out") + "." + rsp.getInt("Year_Out") + ")");
			} while (rsp.next());
		}
		// Sum(In) <=> Sum(Out)???
		sql = "select GROUP_CONCAT(\"id1\") AS \"ids_in\",sum(\"Amount_In\") AS \"Amount_In\",min(\"Amount_Out\") AS \"Amount_Out\",min(\"id2\") as \"ids_out\" from (SELECT min(\"L1\".\"ID\") AS \"id1\",GROUP_CONCAT(\"L2\".\"ID\") AS \"id2\",min(\"L1\".\"Unitmenge\") AS \"Amount_In\",sum(\"L2\".\"Unitmenge\") AS \"Amount_Out\" FROM \"Lieferungen\" AS \"L1\" LEFT JOIN \"ChargenVerbindungen\" ON \"L1\".\"ID\"=\"ChargenVerbindungen\".\"Zutat\" LEFT JOIN \"Lieferungen\" AS \"L2\" ON \"L2\".\"Charge\"=\"ChargenVerbindungen\".\"Produkt\" WHERE \"ChargenVerbindungen\".\"ID\" IS NOT NULL GROUP BY \"L1\".\"ID\") GROUP BY \"id2\"";
		rsp = DBKernel.getResultSet(conn, sql, false);
		if (rsp != null && rsp.first()) {
			do {
				if (rsp.getObject("Amount_In") != null && rsp.getObject("Amount_Out") != null) {
					double in = rsp.getDouble("Amount_In");
					double out = rsp.getDouble("Amount_Out");
					if (in > out * 1.1 || out > in * 1.1) {
						System.err.println("Amounts correct?? In: " + rsp.getString("ids_in") + " (" + in + " kg) vs. Out: " + rsp.getString("ids_out") + " (" + out + ")");
					}
				}
			} while (rsp.next());
		}

		System.err.println("Starting Tracing...");
		MyNewTracing mnt = MyNewTracingLoader.getNewTracingModel(DBKernel.myDBi, conn);

		System.err.println("Starting Nodes33...");
		//HashSet<Integer> toBeMerged = new HashSet<Integer>();
		//LinkedHashMap<Integer, String> id2Code = new LinkedHashMap<Integer, String>();
		// Alle Stationen -> Nodes33
		BufferedDataContainer output33Nodes = exec.createDataContainer(getSpec33Nodes());
		ResultSet rs = DBKernel.getResultSet(conn, "SELECT * FROM " + DBKernel.delimitL("Station"), false);
		if (rs != null && rs.first()) {
			int rowNumber = 0;
			do {
				int stationID = rs.getInt("ID");
				//if (!antiArticle || !checkCompanyReceivedArticle(stationID, articleFilterList) || !checkCase(stationID)) {
				String bl = getBL(clean(rs.getString("Bundesland")));
				//Integer cp = rs.getObject("CasePriority") == null ? null : rs.getInt("CasePriority");
				String country = clean(rs.getString("Land"));//getBL(clean(rs.getString("Land"), 3);
				String company = (rs.getObject("Name") == null || (doAnonymize && stationID < 100000)) ? getAnonymizedStation(bl, stationID, country) : clean(rs.getString("Name")); // bl + stationID + "(" + country + ")"
				//if (rs.getObject("Land") != null && clean(rs.getString("Land").equals("Serbia")) toBeMerged.add(stationID);
				//id2Code.put(stationID, company);
				RowKey key = RowKey.createRowKey(rowNumber);
				DataCell[] cells = new DataCell[19];
				cells[0] = new IntCell(stationID);
				cells[1] = new StringCell(company);
				//cells[2] = new StringCell("square"); // circle, square, triangle
				//cells[3] = new DoubleCell(1.5);
				//cells[4] = new StringCell("yellow"); // red, yellow
				cells[2] = (doAnonymize || rs.getObject("Strasse") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("Strasse")));
				cells[3] = (doAnonymize || rs.getObject("Hausnummer") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("Hausnummer")));
				cells[4] = (rs.getObject("PLZ") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("PLZ")));
				cells[5] = (doAnonymize || rs.getObject("Ort") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("Ort")));
				cells[6] = DataType.getMissingCell();
				cells[7] = (doAnonymize || rs.getObject("Bundesland") == null || clean(rs.getString("Bundesland")).equals("NULL")) ? DataType.getMissingCell() : new StringCell(
						clean(rs.getString("Bundesland")));
				cells[8] = (doAnonymize || rs.getObject("Land") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("Land")));
				cells[9] = (doAnonymize || rs.getObject("VATnumber") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("VATnumber")));
				cells[10] = (rs.getObject("Betriebsart") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("Betriebsart")));

				cells[11] = (rs.getObject("AnzahlFaelle") == null) ? DataType.getMissingCell() : new IntCell(rs.getInt("AnzahlFaelle")); // DataType.getMissingCell()
				cells[12] = (rs.getObject("DatumBeginn") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("DatumBeginn")));
				cells[13] = (rs.getObject("DatumHoehepunkt") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("DatumHoehepunkt")));
				cells[14] = (rs.getObject("DatumEnde") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("DatumEnde")));

				cells[15] = (rs.getObject("Serial") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("Serial")));
				//if (cp != null) cells[14] = new StringCell(""+cp.intValue());
				cells[16] = mnt.isSimpleSupplier(stationID) ? BooleanCell.TRUE : BooleanCell.FALSE;
	            cells[17] = mnt.isStationStart(stationID) ? BooleanCell.TRUE : BooleanCell.FALSE;
	            cells[18] = mnt.isStationEnd(stationID) ? BooleanCell.TRUE : BooleanCell.FALSE;

				DataRow outputRow = new DefaultRow(key, cells);

				output33Nodes.addRowToTable(outputRow);
				//}
				exec.checkCanceled();
				//exec.setProgress(rowNumber / 10000, "Adding row " + rowNumber);

				rowNumber++;
			} while (rs.next());
		}
		output33Nodes.close();
		rs.close();

		//mnt.mergeStations(toBeMerged);
		//System.err.println(mnt.getStationScore(-1));

		System.err.println("Starting Links33...");
		// Alle Lieferungen -> Links33
		BufferedDataContainer output33Links = exec.createDataContainer(getSpec33Links());
		rs = DBKernel.getResultSet(conn, "SELECT * FROM " + DBKernel.delimitL("Lieferungen") + " LEFT JOIN " + DBKernel.delimitL("Chargen") + " ON " + DBKernel.delimitL("Lieferungen")
				+ "." + DBKernel.delimitL("Charge") + "=" + DBKernel.delimitL("Chargen") + "." + DBKernel.delimitL("ID") + " LEFT JOIN " + DBKernel.delimitL("Produktkatalog")
				+ " ON " + DBKernel.delimitL("Chargen") + "." + DBKernel.delimitL("Artikel") + "=" + DBKernel.delimitL("Produktkatalog") + "." + DBKernel.delimitL("ID")
				+ " ORDER BY " + DBKernel.delimitL("Produktkatalog") + "." + DBKernel.delimitL("ID"), false);
		if (rs != null && rs.first()) {
			int rowNumber = 0;
			do {
				int lieferID = rs.getInt("Lieferungen.ID");
				int id1 = rs.getInt("Produktkatalog.Station");
				int id2 = rs.getInt("Lieferungen.Empfänger");
				//if (id2Code.containsKey(id1) && id2Code.containsKey(id2)) {
				int from = id1;//id2Code.get(id1);
				int to = id2;//id2Code.get(id2);
				RowKey key = RowKey.createRowKey(rowNumber);
				DataCell[] cells = new DataCell[20];
				cells[0] = new IntCell(lieferID);
				cells[1] = new IntCell(from);
				cells[2] = new IntCell(to);
				//cells[3] = new StringCell("black"); // black
				cells[3] = (doAnonymize || rs.getObject("Artikelnummer") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("Artikelnummer")));
				cells[4] = (rs.getObject("Bezeichnung") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("Bezeichnung")));
				cells[5] = (rs.getObject("Prozessierung") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("Prozessierung")));
				cells[6] = (rs.getObject("IntendedUse") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("IntendedUse")));
				cells[7] = (doAnonymize || rs.getObject("ChargenNr") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("ChargenNr")));
				String mhd = sdfFormat(clean(rs.getString("MHD_day")), clean(rs.getString("MHD_month")), clean(rs.getString("MHD_year")));
				cells[8] = (mhd == null) ? DataType.getMissingCell() : new StringCell(mhd);
				String pd = sdfFormat(clean(rs.getString("pd_day")), clean(rs.getString("pd_month")), clean(rs.getString("pd_year")));
				cells[9] = (pd == null) ? DataType.getMissingCell() : new StringCell(pd);
				String dd = sdfFormat(clean(rs.getString("Lieferungen.dd_day")), clean(rs.getString("Lieferungen.dd_month")), clean(rs.getString("Lieferungen.dd_year")));
				cells[10] = (dd == null) ? DataType.getMissingCell() : new StringCell(dd);
				Double menge = calcMenge(rs.getObject("Unitmenge"), rs.getObject("UnitEinheit"));
				cells[11] = menge == null ? DataType.getMissingCell() : new DoubleCell(menge / 1000.0); // Menge [kg]
				cells[12] = new StringCell("Row" + rowNumber);

				cells[13] = (rs.getObject("Lieferungen.Serial") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("Lieferungen.Serial")));
				cells[14] = (rs.getObject("Chargen.OriginCountry") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("Chargen.OriginCountry")));

				cells[15] = (rs.getObject("Lieferungen.EndChain") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("Lieferungen.EndChain")));
				cells[16] = (rs.getObject("Lieferungen.Explanation_EndChain") == null) ? DataType.getMissingCell() : new StringCell(
						clean(rs.getString("Lieferungen.Explanation_EndChain")));
				cells[17] = (rs.getObject("Lieferungen.Contact_Questions_Remarks") == null) ? DataType.getMissingCell() : new StringCell(
						clean(rs.getString("Lieferungen.Contact_Questions_Remarks")));
				cells[18] = (rs.getObject("Lieferungen.Further_Traceback") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("Lieferungen.Further_Traceback")));
				cells[19] = (rs.getObject("Chargen.MicrobioSample") == null) ? DataType.getMissingCell() : new StringCell(clean(rs.getString("Chargen.MicrobioSample")));

				DataRow outputRow = new DefaultRow(key, cells);

				output33Links.addRowToTable(outputRow);
				rowNumber++;
				//}
				exec.checkCanceled();
				//exec.setProgress(rowNumber / (double)inData[0].getRowCount(), "Adding row " + rowNumber);
			} while (rs.next());
		}
		output33Links.close();
		rs.close();

		BufferedDataContainer buf = exec.createDataContainer(getDataModelSpec());
		buf.addRowToTable(new DefaultRow("0", XMLCellFactory.create(getDataModel(mnt))));
		buf.close();
		//getDataModel(buf.getTable());

		System.err.println("Fin!");
		return new BufferedDataTable[] { output33Nodes.getTable(), output33Links.getTable(), buf.getTable() }; // outputWordle.getTable(), outputBurow.getTable(), outputBurowNew.getTable(),
	}

	private String clean(String s) {
		if (s == null) {
			return null;
		}

		return s.replaceAll("\\p{C}", "").replace("\u00A0", "")
				.replace("\t", " ").replace("\n", " ").trim();
	}
	private String getISO3166_2(String country, String bl) {
		Locale locale = Locale.ENGLISH;//Locale.GERMAN;
		for (String code : Locale.getISOCountries()) {
			if (new Locale("", code).getDisplayCountry(locale).equals(country)) {
				return code;
			}
		}
		if (bl != null && bl.length() > 1) return getBL(bl);
		return "N.N";
	}

	private String getAnonymizedStation(String bl, int stationID, String country) {
		return getISO3166_2(country, bl) + "#" + stationID;//bl + stationID + "(" + country + ")";
	}

	private String sdfFormat(String day, String month, String year) {
		if ((day == null || day.trim().isEmpty()) && (month == null || month.trim().isEmpty()) && (year == null || year.trim().isEmpty())) return null;
		String thisYear = new SimpleDateFormat("yyyy").format(new Date());
		if (year != null) year = year.trim();
		if (month != null) month = month.trim();
		if (day != null) day = day.trim();
		if (year != null && year.length() == 2) year = Integer.parseInt(year) > Integer.parseInt(thisYear.substring(2)) ? "19" : "20" + year;
		if (month != null && month.length() == 1) month = "0" + month;
		if (day != null && day.length() == 1) day = "0" + day;
		return year + "-" + month + "-" + day; // day + "." + month + "." + 
	}

	private String getDataModel(MyNewTracing mnt) {
		XStream xstream = MyNewTracing.getXStream();
		String xml = xstream.toXML(mnt);
		//System.err.println(xml);
		System.err.println(xml.length());
		return xml;
	}

	private Double calcMenge(Object u3, Object bu3) {
		Double result = null;
		if (u3 != null && bu3 != null) {
			Double u3d = (Double) u3;
			String bu3s = bu3.toString();
			if (bu3s.equalsIgnoreCase("t")) result = u3d * 1000000;
			else if (bu3s.equalsIgnoreCase("kg")) result = u3d * 1000;
			else result = u3d; // if (bu3s.equalsIgnoreCase("g"))
		}
		return result;
	}

	private DataTableSpec getDataModelSpec() {
		DataColumnSpec[] spec = new DataColumnSpec[1];
		spec[0] = new DataColumnSpecCreator("DataModel", XMLCell.TYPE).createSpec();
		return new DataTableSpec(spec);
	}

	/*
	 * private DataTableSpec getSpecWordle() { DataColumnSpec[] spec = new
	 * DataColumnSpec[2]; spec[0] = new DataColumnSpecCreator("Words",
	 * StringCell.TYPE).createSpec(); spec[1] = new
	 * DataColumnSpecCreator("Weight", IntCell.TYPE).createSpec(); return new
	 * DataTableSpec(spec); }
	 */
	private DataTableSpec getSpec33Nodes() {
		DataColumnSpec[] spec = new DataColumnSpec[19];
		spec[0] = new DataColumnSpecCreator(TracingConstants.ID_COLUMN, IntCell.TYPE).createSpec();
		spec[1] = new DataColumnSpecCreator("node", StringCell.TYPE).createSpec();
		spec[2] = new DataColumnSpecCreator("Street", StringCell.TYPE).createSpec();
		spec[3] = new DataColumnSpecCreator("HouseNumber", StringCell.TYPE).createSpec();
		spec[4] = new DataColumnSpecCreator(isDE ? "PLZ" : "ZIP", StringCell.TYPE).createSpec();
		spec[5] = new DataColumnSpecCreator(isDE ? "Ort" : "City", StringCell.TYPE).createSpec();
		spec[6] = new DataColumnSpecCreator(isDE ? "Landkreis" : "District", StringCell.TYPE).createSpec();
		spec[7] = new DataColumnSpecCreator(isDE ? "Bundesland" : "County", StringCell.TYPE).createSpec();
		spec[8] = new DataColumnSpecCreator(isDE ? "Land" : "Country", StringCell.TYPE).createSpec();
		spec[9] = new DataColumnSpecCreator("VAT", StringCell.TYPE).createSpec();
		spec[10] = new DataColumnSpecCreator(isDE ? "Betriebsart" : "type of business", StringCell.TYPE).createSpec();
		spec[11] = new DataColumnSpecCreator(isDE ? "NumFaelle" : "Number Cases", IntCell.TYPE).createSpec();
		spec[12] = new DataColumnSpecCreator(isDE ? "DatumBeginn" : "Date start", StringCell.TYPE).createSpec();
		spec[13] = new DataColumnSpecCreator(isDE ? "DatumHoehepunkt" : "Date peak", StringCell.TYPE).createSpec();
		spec[14] = new DataColumnSpecCreator(isDE ? "DatumEnde" : "Date end", StringCell.TYPE).createSpec();
		spec[15] = new DataColumnSpecCreator("Serial", StringCell.TYPE).createSpec();
	    spec[16] = new DataColumnSpecCreator("SimpleSupplier", BooleanCell.TYPE).createSpec();
	    spec[17] = new DataColumnSpecCreator("DeadStart", BooleanCell.TYPE).createSpec();
	    spec[18] = new DataColumnSpecCreator("DeadEnd", BooleanCell.TYPE).createSpec();
		return new DataTableSpec(spec);
	}

	private DataTableSpec getSpec33Links() {
		DataColumnSpec[] spec = new DataColumnSpec[20];
		spec[0] = new DataColumnSpecCreator(TracingConstants.ID_COLUMN, IntCell.TYPE).createSpec();
		spec[1] = new DataColumnSpecCreator(TracingConstants.FROM_COLUMN, IntCell.TYPE).createSpec();
		spec[2] = new DataColumnSpecCreator(TracingConstants.TO_COLUMN, IntCell.TYPE).createSpec();
		spec[3] = new DataColumnSpecCreator(isDE ? "Artikelnummer" : "Item Number", StringCell.TYPE).createSpec();
		spec[4] = new DataColumnSpecCreator(isDE ? "Bezeichnung" : "Name", StringCell.TYPE).createSpec();
		spec[5] = new DataColumnSpecCreator(isDE ? "Prozessierung" : "Processing", StringCell.TYPE).createSpec();
		spec[6] = new DataColumnSpecCreator("IntendedUse", StringCell.TYPE).createSpec();
		spec[7] = new DataColumnSpecCreator(isDE ? "ChargenNr" : "Charge Number", StringCell.TYPE).createSpec();
		spec[8] = new DataColumnSpecCreator(isDE ? "MHD" : "Date Expiration", StringCell.TYPE).createSpec();
		spec[9] = new DataColumnSpecCreator(isDE ? "Herstellungsdatum" : "Date Manufactoring", StringCell.TYPE).createSpec();
		spec[10] = new DataColumnSpecCreator(isDE ? "Lieferdatum" : "Date Delivery", StringCell.TYPE).createSpec();
		spec[11] = new DataColumnSpecCreator(isDE ? "Menge [kg]" : "Amount [kg]", DoubleCell.TYPE).createSpec();
		spec[12] = new DataColumnSpecCreator("EdgeID", StringCell.TYPE).createSpec();
		spec[13] = new DataColumnSpecCreator("Serial", StringCell.TYPE).createSpec();
		spec[14] = new DataColumnSpecCreator("OriginCountry", StringCell.TYPE).createSpec();
		spec[15] = new DataColumnSpecCreator("EndChain", StringCell.TYPE).createSpec();
		spec[16] = new DataColumnSpecCreator("ExplanationEndChain", StringCell.TYPE).createSpec();
		spec[17] = new DataColumnSpecCreator("Contact_Questions_Remarks", StringCell.TYPE).createSpec();
		spec[18] = new DataColumnSpecCreator("FurtherTB", StringCell.TYPE).createSpec();
		spec[19] = new DataColumnSpecCreator("MicroSample", StringCell.TYPE).createSpec();
		return new DataTableSpec(spec);
	}

	private String getBL(String bl) {
		return getBL(bl, 2);
	}

	private String getBL(String bl, int numCharsMax) {
		String result = bl;
		if (result == null || result.trim().isEmpty() || result.trim().equalsIgnoreCase("null")) result = "NN";
		if (result.length() > numCharsMax) {
			result = result.substring(0, numCharsMax);
		}
		return result;
	}

	@SuppressWarnings("unused")
	private void doAnonymizeHard(Connection conn) {
		String sql = "SELECT * FROM " + DBKernel.delimitL("Station");
		ResultSet rs = DBKernel.getResultSet(conn, sql, false);
		try {
			if (rs != null && rs.first()) {
				do {
					String bl = getBL(clean(rs.getString("Bundesland")));
					String country = clean(rs.getString("Land"));//getBL(clean(rs.getString("Land"), 3);
					int stationID = rs.getInt("ID");
					String anonStr = getAnonymizedStation(bl, stationID, country);
					sql = "UPDATE " + DBKernel.delimitL("Station") + " SET " + DBKernel.delimitL("Name") + "='" + anonStr + "', " + DBKernel.delimitL("Strasse") + "=NULL, "
							+ DBKernel.delimitL("Hausnummer") + "=NULL, " + DBKernel.delimitL("Ort") + "=NULL WHERE " + DBKernel.delimitL("ID") + "=" + rs.getInt("ID");
					DBKernel.sendRequest(conn, sql, false, false);
					sql = "UPDATE " + DBKernel.delimitL("Station") + " SET " + DBKernel.delimitL("Betriebsnummer") + "=NULL WHERE " + DBKernel.delimitL("ID") + "="
							+ rs.getInt("ID");
					DBKernel.sendRequest(conn, sql, false, false);
				} while (rs.next());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private static Connection getNewLocalConnection(final String dbUsername, final String dbPassword, final String dbFile) throws Exception {
		Connection result = null;
		Class.forName("org.hsqldb.jdbc.JDBCDriver").newInstance();
		String connStr = "jdbc:hsqldb:file:" + dbFile;
		try {
			result = DriverManager.getConnection(connStr, dbUsername, dbPassword);
			result.setReadOnly(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
		//DBKernel.convertEHEC2NewDB("Samen");
		// evtl. auch: else if (DBKernel.isKrise) { ... nochmal auskommentieren
		//DBKernel.convertEHEC2NewDB("Cluster");
		return new DataTableSpec[] { getSpec33Nodes(), getSpec33Links(), getDataModelSpec() }; // getSpecBurow(), null, getSpecWordle(),
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
	
		settings.addString(PARAM_FILENAME, filename);
		settings.addString(PARAM_LOGIN, login);
		settings.addString(PARAM_PASSWD, passwd);
		settings.addBoolean(PARAM_OVERRIDE, override);
		
		settings.addBoolean(PARAM_ANONYMIZE, doAnonymize);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
	
		filename = settings.getString(PARAM_FILENAME); login =
		settings.getString(PARAM_LOGIN); passwd =
		settings.getString(PARAM_PASSWD); override =
		settings.getBoolean(PARAM_OVERRIDE);
		
		doAnonymize = settings.getBoolean(PARAM_ANONYMIZE, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
	}

}
