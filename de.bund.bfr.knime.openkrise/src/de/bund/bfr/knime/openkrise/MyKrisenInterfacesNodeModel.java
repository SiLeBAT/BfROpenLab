/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.openkrise;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.openkrise.db.DBKernel;

/**
 * This is the model implementation of MyKrisenInterfaces.
 * 
 * 
 * @author draaw
 */
public class MyKrisenInterfacesNodeModel extends NodeModel {

	static final String PARAM_FILENAME = "filename";
	static final String PARAM_LOGIN = "login";
	static final String PARAM_PASSWD = "passwd";
	static final String PARAM_OVERRIDE = "override";

	static final String PARAM_ANONYMIZE = "anonymize";
	static final String PARAM_RANDOM = "randomgenerator";
	static final String PARAM_RANDOMNODES = "randomgeneratornodes";
	static final String PARAM_RANDOMLINKING = "randomgeneratorlinking";

	private String filename;
	private String login;
	private String passwd;
	private boolean override;

	private boolean doAnonymize;
	private boolean randomGen;
	private int randomGenNodes, randomGenLinking;

	/**
	 * Constructor for the node model.
	 */
	protected MyKrisenInterfacesNodeModel() {
		super(0, 3);
	}

	private Connection getLocalConn() {		
		return DBKernel.getLocalConn(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {

		BufferedDataContainer output33Nodes = null;
		BufferedDataContainer output33Links = null;
		BufferedDataContainer deliveryDelivery = exec.createDataContainer(getDataModelSpec());

		if (randomGen) {
			RandomNetworkGenerator rng = new RandomNetworkGenerator(randomGenNodes, randomGenLinking);
			output33Nodes = exec.createDataContainer(getSpec33Nodes(null));
			output33Links = exec.createDataContainer(getSpec33Links(null));
			rng.getNodes(output33Nodes);
			rng.getLinks(output33Links);
			rng.getDeliveryDelivery(deliveryDelivery);
		} else {
			Connection conn = override ? getNewLocalConnection(login, passwd, filename) : getLocalConn();
			output33Nodes = exec.createDataContainer(getSpec33Nodes(conn));
			output33Links = exec.createDataContainer(getSpec33Links(conn));
			//if (doAnonymize) doAnonymizeHard(conn);

			String warningMessage = "Starting Plausibility Checks...";
			System.err.println(warningMessage);
			boolean warningsThere = false;
			// Date_In <= Date_Out???
			String sql = "SELECT \"ChargenVerbindungen\".\"ID\" AS \"ID\", \"L1\".\"Serial\" AS \"ID_In\", \"L2\".\"Serial\" AS \"ID_Out\", \"L1\".\"dd_day\" AS \"Day_In\",\"L2\".\"dd_day\" AS \"Day_Out\", \"L1\".\"dd_month\" AS \"Month_In\",\"L2\".\"dd_month\" AS \"Month_Out\", \"L1\".\"dd_year\" AS \"Year_In\",\"L2\".\"dd_year\" AS \"Year_Out\" FROM \"Lieferungen\" AS \"L1\" LEFT JOIN \"ChargenVerbindungen\" ON \"L1\".\"ID\"=\"ChargenVerbindungen\".\"Zutat\" LEFT JOIN \"Lieferungen\" AS \"L2\" ON \"L2\".\"Charge\"=\"ChargenVerbindungen\".\"Produkt\" WHERE \"ChargenVerbindungen\".\"ID\" IS NOT NULL AND (\"L2\".\"dd_year\" < \"L1\".\"dd_year\" OR \"L2\".\"dd_year\" = \"L1\".\"dd_year\" AND \"L2\".\"dd_month\" < \"L1\".\"dd_month\" OR \"L2\".\"dd_year\" = \"L1\".\"dd_year\" AND \"L2\".\"dd_month\" = \"L1\".\"dd_month\" AND \"L2\".\"dd_day\" < \"L1\".\"dd_day\")";
			ResultSet rsp = DBKernel.getResultSet(conn, sql, false);
			if (rsp != null && rsp.first()) {
				do {
					warningMessage = "Dates correct?? In: " + rsp.getString("ID_In") + " (" + rsp.getInt("Day_In") + "." + rsp.getInt("Month_In") + "." + rsp.getInt("Year_In")
							+ ") vs. Out: " + rsp.getString("ID_Out") + " (" + rsp.getInt("Day_Out") + "." + rsp.getInt("Month_Out") + "." + rsp.getInt("Year_Out") + ")";
					System.err.println(warningMessage);
					this.setWarningMessage(warningMessage);
					warningsThere = true;
				} while (rsp.next());
			}
			// Sum(In) <=> Sum(Out)???
			sql = "select GROUP_CONCAT(\"id1\") AS \"ids_in\",sum(\"Amount_In\") AS \"Amount_In\",min(\"Amount_Out\") AS \"Amount_Out\",min(\"id2\") as \"ids_out\" from (SELECT min(\"L1\".\"Serial\") AS \"id1\",GROUP_CONCAT(\"L2\".\"Serial\") AS \"id2\",min(\"L1\".\"Unitmenge\") AS \"Amount_In\",sum(\"L2\".\"Unitmenge\") AS \"Amount_Out\" FROM \"Lieferungen\" AS \"L1\" LEFT JOIN \"ChargenVerbindungen\" ON \"L1\".\"ID\"=\"ChargenVerbindungen\".\"Zutat\" LEFT JOIN \"Lieferungen\" AS \"L2\" ON \"L2\".\"Charge\"=\"ChargenVerbindungen\".\"Produkt\" WHERE \"ChargenVerbindungen\".\"ID\" IS NOT NULL GROUP BY \"L1\".\"ID\") GROUP BY \"id2\"";
			rsp = DBKernel.getResultSet(conn, sql, false);
			if (rsp != null && rsp.first()) {
				do {
					if (rsp.getObject("Amount_In") != null && rsp.getObject("Amount_Out") != null) {
						double in = rsp.getDouble("Amount_In");
						double out = rsp.getDouble("Amount_Out");
						if (in > out * 2 || out > in * 2) { // 1.1
							warningMessage = "Amounts correct?? In: " + rsp.getString("ids_in") + " (" + in + ") vs. Out: " + rsp.getString("ids_out") + " (" + out + ")";
							System.err.println(warningMessage);
							this.setWarningMessage(warningMessage);
							warningsThere = true;
						}
					}
				} while (rsp.next());
			}
			// numPU, typePU
			sql = "SELECT GROUP_CONCAT(\"id1\") AS \"ids_in\",SUM(\"Amount_In\") AS \"Amount_In\",MIN(\"Amount_Out\") AS \"Amount_Out\",MIN(\"id2\") AS \"ids_out\",\"Type_In\",\"Type_Out\" FROM " +

			" (SELECT MIN(\"L1\".\"Serial\") AS \"id1\",GROUP_CONCAT(\"L2\".\"Serial\") AS \"id2\",MIN(\"L1\".\"numPU\") AS \"Amount_In\",\"L1\".\"typePU\" AS \"Type_In\",SUM(\"L2\".\"numPU\") AS \"Amount_Out\",\"L2\".\"typePU\" AS \"Type_Out\" FROM " +
			" \"Lieferungen\" AS \"L1\" LEFT JOIN \"ChargenVerbindungen\" ON \"L1\".\"ID\"=\"ChargenVerbindungen\".\"Zutat\" LEFT JOIN \"Lieferungen\" AS \"L2\" ON \"L2\".\"Charge\"=\"ChargenVerbindungen\".\"Produkt\" " +
			" WHERE \"ChargenVerbindungen\".\"ID\" IS NOT NULL AND \"L1\".\"typePU\" = \"L2\".\"typePU\" GROUP BY \"L1\".\"ID\",\"L1\".\"typePU\",\"L2\".\"typePU\") " +

			" WHERE \"Type_In\" = \"Type_Out\" " +
			" GROUP BY \"id2\",\"Type_In\",\"Type_Out\"";
			//System.err.println(sql);
			rsp = DBKernel.getResultSet(conn, sql, false);
			if (rsp != null && rsp.first()) {
				do {
					if (rsp.getObject("Amount_In") != null && rsp.getObject("Amount_Out") != null) {
						double in = rsp.getDouble("Amount_In");
						double out = rsp.getDouble("Amount_Out");
						if (in > out * 2 || out > in * 2) { // 1.1
							warningMessage = "Amounts correct?? In: " + rsp.getString("ids_in") + " (" + in + ") vs. Out: " + rsp.getString("ids_out") + " (" + out + ")";
							System.err.println(warningMessage);
							this.setWarningMessage(warningMessage);
							warningsThere = true;
						}
					}
				} while (rsp.next());
			}
			if (warningsThere) this.setWarningMessage("Look into the console - there are plausibility issues...");
			System.err.println("Plausibility Checks - Fin!");

			System.err.println("Starting Tracing...");
			Map<String, Delivery> allDeliveries = MyNewTracingLoader.getNewTracingModel(DBKernel.myDBi, conn);			

			boolean useSerialAsID = MyNewTracingLoader.serialPossible(conn);
			HashMap<String, String> hmStationIDs = new HashMap<>();
			HashMap<String, String> hmDeliveryIDs = new HashMap<>();
			System.err.println("Starting Nodes33...");
			ResultSet rs = DBKernel.getResultSet(conn, "SELECT * FROM " + DBKernel.delimitL("Station"), false);
			if (rs != null && rs.first()) {
				int rowNumber = 0;
				boolean isBVLFormat = false;
				DataTableSpec spec = output33Nodes.getTableSpec();
				do {
					String sID = rs.getString("ID");
					String stationID = rs.getObject(useSerialAsID ? "Serial" : "ID").toString();
					if (useSerialAsID) hmStationIDs.put(sID, stationID);
					String district = null;
					String bll = clean(rs.getString("Bundesland"));
					if (rowNumber == 0 && bll != null && (bll.equals("Altenburger Land") || bll.equals("Wesel"))) isBVLFormat = true;
					//if (!antiArticle || !checkCompanyReceivedArticle(stationID, articleFilterList) || !checkCase(stationID)) {
					String country = clean(rs.getString("Land"));//getBL(clean(rs.getString("Land"), 3);
					String zip = clean(rs.getString("PLZ"));
					//Integer cp = rs.getObject("CasePriority") == null ? null : rs.getInt("CasePriority");
					if (isBVLFormat) {
						district = bll;
						bll = country;
						if (zip != null && zip.length() == 4) country = "BE";
						else country = "DE";
					}
					String bl = getBL(bll);
					String company = (rs.getObject("Name") == null || doAnonymize) ? getAnonymizedStation(bl, sID.hashCode(), country)
							: clean(rs.getString("Name")); // bl + stationID + "(" + country + ")"
					//if (rs.getObject("Land") != null && clean(rs.getString("Land").equals("Serbia")) toBeMerged.add(stationID);
					//id2Code.put(stationID, company);
					RowKey key = RowKey.createRowKey(rowNumber);
					DataCell[] cells = new DataCell[spec.getNumColumns()];

					fillCell(spec, cells, TracingColumns.ID, new StringCell(stationID));

					fillCell(spec, cells, TracingColumns.STATION_NODE, new StringCell(company));
					fillCell(spec, cells, TracingColumns.STATION_NAME, new StringCell(company));
					fillCell(spec, cells, TracingColumns.STATION_STREET, doAnonymize ? DataType.getMissingCell() : getDataStringCell(rs, "Strasse"));
					fillCell(spec, cells, TracingColumns.STATION_HOUSENO, doAnonymize ? DataType.getMissingCell() : getDataStringCell(rs, "Hausnummer"));
					fillCell(spec, cells, TracingColumns.STATION_ZIP, zip == null ? DataType.getMissingCell() : new StringCell(zip));
					fillCell(spec, cells, TracingColumns.STATION_CITY, doAnonymize ? DataType.getMissingCell() : getDataStringCell(rs, "Ort"));
					fillCell(spec, cells, TracingColumns.STATION_DISTRICT, doAnonymize || district == null ? DataType.getMissingCell() : new StringCell(district));
					fillCell(spec, cells, TracingColumns.STATION_STATE, doAnonymize || bll == null ? DataType.getMissingCell() : new StringCell(bll));
					fillCell(spec, cells, TracingColumns.STATION_COUNTRY, doAnonymize || country == null ? DataType.getMissingCell() : new StringCell(country));

					fillCell(spec, cells, TracingColumns.STATION_VAT, doAnonymize ? DataType.getMissingCell() : getDataStringCell(rs, "VATnumber"));
					fillCell(spec, cells, TracingColumns.STATION_TOB, getDataStringCell(rs, "Betriebsart"));
					fillCell(spec, cells, TracingColumns.STATION_NUMCASES, getDataIntCell(rs, "AnzahlFaelle"));
					fillCell(spec, cells, TracingColumns.STATION_DATESTART, getDataStringCell(rs, "DatumBeginn"));
					fillCell(spec, cells, TracingColumns.STATION_DATEPEAK, getDataStringCell(rs, "DatumHoehepunkt"));
					fillCell(spec, cells, TracingColumns.STATION_DATEEND, getDataStringCell(rs, "DatumEnde"));
					fillCell(spec, cells, TracingColumns.STATION_SERIAL, getDataStringCell(rs, "Serial"));
					fillCell(spec, cells, TracingColumns.STATION_SIMPLESUPPLIER, isSimpleSupplier(allDeliveries, sID) ? BooleanCell.TRUE : BooleanCell.FALSE);
					fillCell(spec, cells, TracingColumns.STATION_DEADSTART, isStationStart(allDeliveries, sID) ? BooleanCell.TRUE : BooleanCell.FALSE);
					fillCell(spec, cells, TracingColumns.STATION_DEADEND, isStationEnd(allDeliveries, sID) ? BooleanCell.TRUE : BooleanCell.FALSE);

					fillCell(spec, cells, TracingColumns.FILESOURCES, getDataStringCell(rs, "ImportSources"));
					
					fillCell(spec, cells, TracingColumns.STATION_COUNTY, doAnonymize || bll == null ? DataType.getMissingCell() : new StringCell(bll));


					// Extras
					for (String extraCol : spec.getColumnNames()) {
						if (extraCol.startsWith("_")) {
							String attribute = extraCol.substring(1);
							ResultSet rs2 = DBKernel.getResultSet(conn, "SELECT " +	DBKernel.delimitL("value") + " FROM " + DBKernel.delimitL("ExtraFields") +
									" WHERE " +	DBKernel.delimitL("tablename") + "='Station' AND " +
									DBKernel.delimitL("id") + "=" + sID + " AND " +	DBKernel.delimitL("attribute") + "='" + attribute + "'", false);
							if (rs2 != null && rs2.first()) {
								fillCell(spec, cells, extraCol, getDataStringCell(rs2, "value"));
							}
							else {
								fillCell(spec, cells, extraCol, DataType.getMissingCell());
							}
						}
					}

					DataRow outputRow = new DefaultRow(key, cells);

					output33Nodes.addRowToTable(outputRow);
					exec.checkCanceled();

					rowNumber++;
				} while (rs.next());
			}
			rs.close();

			//mnt.mergeStations(toBeMerged);
			//System.err.println(mnt.getStationScore(-1));

			System.err.println("Starting Links33...");
			// Alle Lieferungen -> Links33
			rs = DBKernel.getResultSet(
					conn,
					"SELECT * FROM " + DBKernel.delimitL("Lieferungen") + " LEFT JOIN " + DBKernel.delimitL("Chargen") + " ON " + DBKernel.delimitL("Lieferungen") + "."
							+ DBKernel.delimitL("Charge") + "=" + DBKernel.delimitL("Chargen") + "." + DBKernel.delimitL("ID") + " LEFT JOIN "
							+ DBKernel.delimitL("Produktkatalog") + " ON " + DBKernel.delimitL("Chargen") + "." + DBKernel.delimitL("Artikel") + "="
							+ DBKernel.delimitL("Produktkatalog") + "." + DBKernel.delimitL("ID") + " ORDER BY " + DBKernel.delimitL("Produktkatalog") + "."
							+ DBKernel.delimitL("ID"), false);
			if (rs != null && rs.first()) {
				int rowNumber = 0;
				DataTableSpec spec = output33Links.getTableSpec();
				do {
					String lID = rs.getObject("Lieferungen.ID").toString();
					String lieferID = rs.getObject(useSerialAsID ? "Lieferungen.Serial" : lID).toString();
					String id1 = rs.getObject("Produktkatalog.Station").toString();
					String id2 = rs.getObject("Lieferungen.Empfänger").toString();
					if (useSerialAsID) {
						hmDeliveryIDs.put(lID, lieferID);
						id1 = hmStationIDs.get(id1);
						id2 = hmStationIDs.get(id2);
					}
					String from = id1;
					String to = id2;
					RowKey key = RowKey.createRowKey(rowNumber);
					DataCell[] cells = new DataCell[spec.getNumColumns()];

					fillCell(spec, cells, TracingColumns.ID, new StringCell(lieferID));
					fillCell(spec, cells, TracingColumns.FROM, new StringCell(from));
					fillCell(spec, cells, TracingColumns.TO, new StringCell(to));

					fillCell(spec, cells, TracingColumns.DELIVERY_ITEMNAME, getDataStringCell(rs, "Bezeichnung"));
					fillCell(spec, cells, TracingColumns.DELIVERY_ITEMNUM, doAnonymize ? DataType.getMissingCell() : getDataStringCell(rs, "Artikelnummer"));
					String dd = sdfFormat(clean(rs.getString("Lieferungen.dd_day")), clean(rs.getString("Lieferungen.dd_month")), clean(rs.getString("Lieferungen.dd_year")));
					fillCell(spec, cells, TracingColumns.DELIVERY_DATE, dd == null ? DataType.getMissingCell() : new StringCell(dd));
					fillCell(spec, cells, TracingColumns.DELIVERY_SERIAL, getDataStringCell(rs, "Lieferungen.Serial"));

					fillCell(spec, cells, TracingColumns.DELIVERY_PROCESSING, getDataStringCell(rs, "Prozessierung"));
					fillCell(spec, cells, TracingColumns.DELIVERY_USAGE, getDataStringCell(rs, "IntendedUse"));
					fillCell(spec, cells, TracingColumns.DELIVERY_LOTNUM, doAnonymize ? DataType.getMissingCell() : getDataStringCell(rs, "ChargenNr"));
					String mhd = sdfFormat(clean(rs.getString("MHD_day")), clean(rs.getString("MHD_month")), clean(rs.getString("MHD_year")));
					fillCell(spec, cells, TracingColumns.DELIVERY_DATEEXP, mhd == null ? DataType.getMissingCell() : new StringCell(mhd));
					String pd = sdfFormat(clean(rs.getString("pd_day")), clean(rs.getString("pd_month")), clean(rs.getString("pd_year")));
					fillCell(spec, cells, TracingColumns.DELIVERY_DATEMANU, pd == null ? DataType.getMissingCell() : new StringCell(pd));
					Double menge = calcMenge(rs.getObject("Unitmenge"), rs.getObject("UnitEinheit"));
					fillCell(spec, cells, TracingColumns.DELIVERY_AMOUNT, menge == null ? DataType.getMissingCell() : new DoubleCell(menge / 1000.0));
					fillCell(spec, cells, TracingColumns.DELIVERY_NUM_PU, getDataDoubleCell(rs, "Lieferungen.numPU"));
					fillCell(spec, cells, TracingColumns.DELIVERY_TYPE_PU, getDataStringCell(rs, "Lieferungen.typePU"));
					fillCell(spec, cells, TracingColumns.DELIVERY_ENDCHAIN, getDataStringCell(rs, "Lieferungen.EndChain"));
					fillCell(spec, cells, TracingColumns.DELIVERY_ORIGIN, getDataStringCell(rs, "Chargen.OriginCountry"));
					fillCell(spec, cells, TracingColumns.DELIVERY_ENDCHAIN, getDataStringCell(rs, "Lieferungen.EndChain"));
					fillCell(spec, cells, TracingColumns.DELIVERY_ENDCHAINWHY, getDataStringCell(rs, "Lieferungen.Explanation_EndChain"));
					fillCell(spec, cells, TracingColumns.DELIVERY_REMARKS, getDataStringCell(rs, "Lieferungen.Contact_Questions_Remarks"));
					fillCell(spec, cells, TracingColumns.DELIVERY_FURTHERTB, getDataStringCell(rs, "Lieferungen.Further_Traceback"));
					fillCell(spec, cells, TracingColumns.DELIVERY_MICROSAMPLE, getDataStringCell(rs, "Chargen.MicrobioSample"));

					fillCell(spec, cells, TracingColumns.FILESOURCES, getDataStringCell(rs, "Lieferungen.ImportSources"));

					fillCell(spec, cells, TracingColumns.DELIVERY_CHARGENUM, doAnonymize ? DataType.getMissingCell() : getDataStringCell(rs, "ChargenNr"));

					// Extras
					for (String extraCol : spec.getColumnNames()) {
						if (extraCol.startsWith("_")) {
							String attribute = extraCol.substring(1);
							int index = attribute.indexOf(".");
							String tn = attribute.substring(0, index);
							String fn = attribute.substring(index + 1);
							ResultSet rs2 = DBKernel.getResultSet(conn, "SELECT " +	DBKernel.delimitL("value") + " FROM " + DBKernel.delimitL("ExtraFields") +
									" WHERE " +	DBKernel.delimitL("tablename") + "='" + tn + "' AND " +
									DBKernel.delimitL("id") + "=" + lID + " AND " +	DBKernel.delimitL("attribute") + "='" + fn + "'", false);
							if (rs2 != null && rs2.first()) {
								fillCell(spec, cells, extraCol, getDataStringCell(rs2, "value"));
							}
							else {
								fillCell(spec, cells, extraCol, DataType.getMissingCell());
							}
						}
					}

					DataRow outputRow = new DefaultRow(key, cells);

					output33Links.addRowToTable(outputRow);
					rowNumber++;
					exec.checkCanceled();
				} while (rs.next());
			}
			rs.close();

			int i = 0;

			for (Delivery delivery : allDeliveries.values()) {
				for (String next : delivery.getAllNextIDs()) {
					if (useSerialAsID) deliveryDelivery.addRowToTable(new DefaultRow(i + "", IO.createCell(hmDeliveryIDs.get(delivery.getId())), IO.createCell(hmDeliveryIDs.get(next))));
					else deliveryDelivery.addRowToTable(new DefaultRow(i + "", IO.createCell(delivery.getId()), IO.createCell(next)));
					i++;
				}
			}
		}

		output33Nodes.close();
		output33Links.close();
		deliveryDelivery.close();
		//getDataModel(buf.getTable());

		System.err.println("Fin!");
		return new BufferedDataTable[] { output33Nodes.getTable(), output33Links.getTable(), deliveryDelivery.getTable() }; // outputWordle.getTable(), outputBurow.getTable(), outputBurowNew.getTable(),
	}

	private DataCell getDataStringCell(ResultSet rs, String columnname) throws SQLException {
		DataCell result = DataType.getMissingCell();
		try {
			if (rs.getObject(columnname) != null) result = new StringCell(clean(rs.getString(columnname)));
		}
		catch (Exception e) {}
		return result;
	}

	private DataCell getDataIntCell(ResultSet rs, String columnname) throws SQLException {
		return rs.getObject(columnname) == null ? DataType.getMissingCell() : new IntCell(rs.getInt(columnname));
	}

	private DataCell getDataDoubleCell(ResultSet rs, String columnname) throws SQLException {
		return rs.getObject(columnname) == null ? DataType.getMissingCell() : new DoubleCell(rs.getDouble(columnname));
	}

	private void fillCell(DataTableSpec spec, DataCell[] cells, String columnname, DataCell value) {
		int index = spec.findColumnIndex(columnname);
		if (index >= 0) cells[index] = value;
	}

	private String clean(String s) {
		if (s == null || s.equalsIgnoreCase("null")) {
			return null;
		}

		return s.replace("\n", "|").replaceAll("\\p{C}", "").replace("\u00A0", "").replace("\t", " ").trim();
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
		if (year == null) {
			return null;
		} else if (month == null) {
			return year;
		} else if (day == null) {
			return year + "-" + month;
		}
		return year + "-" + month + "-" + day; // day + "." + month + "." + 
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
		DataColumnSpec[] spec = new DataColumnSpec[2];
		spec[0] = new DataColumnSpecCreator(TracingColumns.ID, StringCell.TYPE).createSpec();
		spec[1] = new DataColumnSpecCreator(TracingColumns.NEXT, StringCell.TYPE).createSpec();
		return new DataTableSpec(spec);
	}

	private DataTableSpec getSpec33Nodes(Connection conn) {
		List<DataColumnSpec> columns = new ArrayList<>();
		columns.add(new DataColumnSpecCreator(TracingColumns.ID, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.STATION_NODE, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.STATION_NAME, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.STATION_STREET, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.STATION_HOUSENO, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.STATION_ZIP, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.STATION_CITY, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.STATION_DISTRICT, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.STATION_STATE, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.STATION_COUNTRY, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.STATION_SERIAL, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.STATION_SIMPLESUPPLIER, BooleanCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.STATION_DEADSTART, BooleanCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.STATION_DEADEND, BooleanCell.TYPE).createSpec());

		if (containsValues(conn, "Station", "VATnumber")) columns.add(new DataColumnSpecCreator(TracingColumns.STATION_VAT, StringCell.TYPE).createSpec());
		if (containsValues(conn, "Station", "Betriebsart")) columns.add(new DataColumnSpecCreator(TracingColumns.STATION_TOB, StringCell.TYPE).createSpec());
		if (containsValues(conn, "Station", "AnzahlFaelle")) columns.add(new DataColumnSpecCreator(TracingColumns.STATION_NUMCASES, IntCell.TYPE).createSpec());
		if (containsValues(conn, "Station", "DatumBeginn")) columns.add(new DataColumnSpecCreator(TracingColumns.STATION_DATESTART, StringCell.TYPE).createSpec());
		if (containsValues(conn, "Station", "DatumHoehepunkt")) columns.add(new DataColumnSpecCreator(TracingColumns.STATION_DATEPEAK, StringCell.TYPE).createSpec());
		if (containsValues(conn, "Station", "DatumEnde")) columns.add(new DataColumnSpecCreator(TracingColumns.STATION_DATEEND, StringCell.TYPE).createSpec());
		if (containsValues(conn, "Station", "ImportSources")) columns.add(new DataColumnSpecCreator(TracingColumns.FILESOURCES, StringCell.TYPE).createSpec());

		// due to backward compatibility:
		columns.add(new DataColumnSpecCreator(TracingColumns.STATION_COUNTY, StringCell.TYPE).createSpec());

		// ExtraFields
		try {
			ResultSet rs = DBKernel.getResultSet(conn, "SELECT DISTINCT(" +	DBKernel.delimitL("attribute") + ") FROM " + DBKernel.delimitL("ExtraFields") +
					" WHERE " +	DBKernel.delimitL("tablename") + "='Station'"
						, false);
			if (rs != null && rs.first()) {
				do {
					columns.add(new DataColumnSpecCreator("_" + rs.getString(1), StringCell.TYPE).createSpec());
				} while (rs.next());
			}
		}
		catch (Exception e) {e.printStackTrace();} 
		
		return new DataTableSpec(columns.toArray(new DataColumnSpec[0]));
	}

	private DataTableSpec getSpec33Links(Connection conn) {
		List<DataColumnSpec> columns = new ArrayList<>();
		columns.add(new DataColumnSpecCreator(TracingColumns.ID, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.FROM, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.TO, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_ITEMNUM, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_ITEMNAME, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_DATE, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_SERIAL, StringCell.TYPE).createSpec());

		if (containsValues(conn, "Produktkatalog", "Prozessierung")) columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_PROCESSING, StringCell.TYPE).createSpec());
		if (containsValues(conn, "Produktkatalog", "IntendedUse")) columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_USAGE, StringCell.TYPE).createSpec());
		if (containsValues(conn, "Chargen", "ChargenNr")) columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_LOTNUM, StringCell.TYPE).createSpec());
		if (containsValues(conn, "Chargen", new String[] { "MHD_day", "MHD_month", "MHD_year" })) columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_DATEEXP,
				StringCell.TYPE).createSpec());
		if (containsValues(conn, "Chargen", new String[] { "pd_day", "pd_month", "pd_year" })) columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_DATEMANU,
				StringCell.TYPE).createSpec());
		if (containsValues(conn, "Lieferungen", "Unitmenge")) columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_AMOUNT, DoubleCell.TYPE).createSpec());
		boolean acv = containsValues(conn, "Lieferungen", "numPU"); 
		if (acv) columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_NUM_PU, DoubleCell.TYPE).createSpec());
		if (acv && containsValues(conn, "Lieferungen", "typePU")) columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_TYPE_PU, StringCell.TYPE).createSpec());
		if (containsValues(conn, "Chargen", "OriginCountry")) columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_ORIGIN, StringCell.TYPE).createSpec());
		if (containsValues(conn, "Lieferungen", "EndChain")) columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_ENDCHAIN, StringCell.TYPE).createSpec());
		if (containsValues(conn, "Lieferungen", "Explanation_EndChain")) columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_ENDCHAINWHY, StringCell.TYPE).createSpec());
		if (containsValues(conn, "Lieferungen", "Contact_Questions_Remarks")) columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_REMARKS, StringCell.TYPE).createSpec());
		if (containsValues(conn, "Lieferungen", "Further_Traceback")) columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_FURTHERTB, StringCell.TYPE).createSpec());
		if (containsValues(conn, "Chargen", "MicrobioSample")) columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_MICROSAMPLE, StringCell.TYPE).createSpec());
		if (containsValues(conn, "Lieferungen", "ImportSources")) columns.add(new DataColumnSpecCreator(TracingColumns.FILESOURCES, StringCell.TYPE).createSpec());

		if (containsValues(conn, "Chargen", "ChargenNr")) columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_CHARGENUM, StringCell.TYPE).createSpec());

		// ExtraFields
		try {
			ResultSet rs = DBKernel.getResultSet(conn, "SELECT DISTINCT(CONCAT(" +	DBKernel.delimitL("tablename") + ",'.'," +	DBKernel.delimitL("attribute") + ")) FROM " + DBKernel.delimitL("ExtraFields") +
					" WHERE " +	DBKernel.delimitL("tablename") + "='Produktkatalog'" +
					" OR " +	DBKernel.delimitL("tablename") + "='Chargen'" +
					" OR " +	DBKernel.delimitL("tablename") + "='Lieferungen'"
						, false);
			if (rs != null && rs.first()) {
				do {
					columns.add(new DataColumnSpecCreator("_" + rs.getString(1), StringCell.TYPE).createSpec());
				} while (rs.next());
			}
		}
		catch (Exception e) {e.printStackTrace();} 
		
		return new DataTableSpec(columns.toArray(new DataColumnSpec[0]));
	}

	private boolean containsValues(Connection conn, String tablename, String columnname) {
		return containsValues(conn, tablename, new String[] { columnname });
	}

	private boolean containsValues(Connection conn, String tablename, String[] columnnames) {
		boolean result = false;
		if (conn != null) {
			try {
				String count = "COUNT(DISTINCT " + DBKernel.delimitL(columnnames[0]) + ")";
				for (int i = 1; i < columnnames.length; i++) {
					count += "+COUNT(DISTINCT " + DBKernel.delimitL(columnnames[i]) + ")";
				}
				ResultSet rs = DBKernel.getResultSet(conn, "SELECT (" + count + ") FROM " + DBKernel.delimitL(tablename), false);
				if (rs != null && rs.first() && rs.getInt(1) > 0) result = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
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
//		Connection conn = null;
//		if (!randomGen) {
//			if (override) {
//				try {
//					conn = getNewLocalConnection(login, passwd, filename);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			} else conn = getLocalConn();
//		}
		return new DataTableSpec[] { null, null, null }; // getSpec33Nodes(conn), getSpec33Links(conn), getDataModelSpec()
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

		settings.addBoolean(PARAM_RANDOM, randomGen);
		settings.addInt(PARAM_RANDOMNODES, randomGenNodes);
		settings.addInt(PARAM_RANDOMLINKING, randomGenLinking);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {

		filename = settings.getString(PARAM_FILENAME);
		login = settings.getString(PARAM_LOGIN);
		passwd = settings.getString(PARAM_PASSWD);
		override = settings.getBoolean(PARAM_OVERRIDE);

		doAnonymize = settings.getBoolean(PARAM_ANONYMIZE, false);

		randomGen = settings.getBoolean(PARAM_RANDOM, false);
		randomGenNodes = settings.getInt(PARAM_RANDOMNODES, 150);
		randomGenLinking = settings.getInt(PARAM_RANDOMLINKING, 3);
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

	private static boolean isStationStart(Map<String, Delivery> deliveries, String id) {
		for (Delivery d : deliveries.values()) {
			if (d.getRecipientID().equals(id)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isSimpleSupplier(Map<String, Delivery> deliveries, String id) {
		if (isStationStart(deliveries, id)) {
			String recId = null;
			for (Delivery d : deliveries.values()) {
				if (d.getSupplierID().equals(id)) {
					if (recId == null)
						recId = d.getRecipientID();
					else if (!recId.equals(d.getRecipientID()))
						return false;
				}
			}
			return true;
		}
		return false;
	}

	private static boolean isStationEnd(Map<String, Delivery> deliveries, String id) {
		for (Delivery d : deliveries.values()) {
			if (d.getSupplierID().equals(id)) {
				return false;
			}
		}

		return true;
	}
}
