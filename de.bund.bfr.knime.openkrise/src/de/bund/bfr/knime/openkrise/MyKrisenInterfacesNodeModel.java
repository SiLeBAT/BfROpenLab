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

import static de.bund.bfr.knime.openkrise.generated.public_.Tables.CHARGEN;
import static de.bund.bfr.knime.openkrise.generated.public_.Tables.CHARGENVERBINDUNGEN;
import static de.bund.bfr.knime.openkrise.generated.public_.Tables.EXTRAFIELDS;
import static de.bund.bfr.knime.openkrise.generated.public_.Tables.LIEFERUNGEN;
import static de.bund.bfr.knime.openkrise.generated.public_.Tables.PRODUKTKATALOG;
import static de.bund.bfr.knime.openkrise.generated.public_.Tables.STATION;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.Select;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
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

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.openkrise.db.DBKernel;

/**
 * This is the model implementation of MyKrisenInterfaces.
 * 
 * 
 * @author draaw
 */
public class MyKrisenInterfacesNodeModel extends NodeModel {

	static final String PARAM_FILENAME = "filename";
	static final String PARAM_OVERRIDE = "override";
	static final String PARAM_ANONYMIZE = "anonymize";

	private String filename;
	private boolean override;
	private boolean doAnonymize;

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
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
		Connection conn = override
				? createLocalConnection("SA", "", KnimeUtils.getFile(filename + "/DB").getAbsolutePath())
				: DBKernel.getLocalConn(true);
		boolean useSerialAsID = !doAnonymize && serialPossible(conn);
		Map<Integer, String> stationIds = new LinkedHashMap<>();
		Map<Integer, String> deliveryIds = new LinkedHashMap<>();

		for (Record r : DSL.using(conn, SQLDialect.HSQLDB).select().from(STATION)) {
			stationIds.put(r.getValue(STATION.ID),
					useSerialAsID ? r.getValue(STATION.SERIAL) : r.getValue(STATION.ID).toString());
		}

		for (Record r : DSL.using(conn, SQLDialect.HSQLDB).select().from(LIEFERUNGEN)) {
			deliveryIds.put(r.getValue(LIEFERUNGEN.ID),
					useSerialAsID ? r.getValue(LIEFERUNGEN.SERIAL) : r.getValue(LIEFERUNGEN.ID).toString());
		}

		Map<String, Delivery> allDeliveries = getNewTracingModel(conn, stationIds, deliveryIds);
		boolean warningsThere = false;

		// check for temporal inconsistencies
		for (Delivery d : allDeliveries.values()) {
			for (String nextId : d.getAllNextIds()) {
				Delivery next = allDeliveries.get(nextId);

				if (!d.isBefore(next)) {
					setWarningMessage("Dates correct?? In: " + d.getId() + " ("
							+ formatDate(d.getArrivalDay(), d.getArrivalMonth(), d.getArrivalYear()) + ") vs. Out: "
							+ next.getId() + " ("
							+ formatDate(next.getDepartureDay(), next.getDepartureMonth(), next.getDepartureYear())
							+ ")");
					warningsThere = true;
				}
			}
		}

		// check for inconsistencies regarding the amounts
		String sql = "select GROUP_CONCAT(\"id1\") AS \"ids_in\", " + "sum(\"Amount_In\") AS \"Amount_In\", "
				+ "min(\"Amount_Out\") AS \"Amount_Out\", "
				+ "min(\"id2\") as \"ids_out\" from (SELECT min(\"L1\".\"Serial\") AS \"id1\","
				+ "GROUP_CONCAT(\"L2\".\"Serial\") AS \"id2\",min(\"L1\".\"Unitmenge\") AS \"Amount_In\",sum(\"L2\".\"Unitmenge\") AS \"Amount_Out\" FROM \"Lieferungen\" AS \"L1\" LEFT JOIN \"ChargenVerbindungen\" ON \"L1\".\"ID\"=\"ChargenVerbindungen\".\"Zutat\" LEFT JOIN \"Lieferungen\" AS \"L2\" ON \"L2\".\"Charge\"=\"ChargenVerbindungen\".\"Produkt\" WHERE \"ChargenVerbindungen\".\"ID\" IS NOT NULL GROUP BY \"L1\".\"ID\") GROUP BY \"id2\"";
		ResultSet rsp = DBKernel.getResultSet(conn, sql, false);
		if (rsp != null && rsp.first()) {
			do {
				if (rsp.getObject("Amount_In") != null && rsp.getObject("Amount_Out") != null) {
					double in = rsp.getDouble("Amount_In");
					double out = rsp.getDouble("Amount_Out");
					if (in > out * 2 || out > in * 2) { // 1.1
						setWarningMessage("Amounts correct?? In: " + rsp.getString("ids_in") + " (" + in + ") vs. Out: "
								+ rsp.getString("ids_out") + " (" + out + ")");
						warningsThere = true;
					}
				}
			} while (rsp.next());
		}
		// numPU, typePU
		sql = "SELECT GROUP_CONCAT(\"id1\") AS \"ids_in\",SUM(\"Amount_In\") AS \"Amount_In\",MIN(\"Amount_Out\") AS \"Amount_Out\",MIN(\"id2\") AS \"ids_out\",\"Type_In\",\"Type_Out\" FROM "
				+ " (SELECT MIN(\"L1\".\"Serial\") AS \"id1\",GROUP_CONCAT(\"L2\".\"Serial\") AS \"id2\",MIN(\"L1\".\"numPU\") AS \"Amount_In\",\"L1\".\"typePU\" AS \"Type_In\",SUM(\"L2\".\"numPU\") AS \"Amount_Out\",\"L2\".\"typePU\" AS \"Type_Out\" FROM "
				+ " \"Lieferungen\" AS \"L1\" LEFT JOIN \"ChargenVerbindungen\" ON \"L1\".\"ID\"=\"ChargenVerbindungen\".\"Zutat\" LEFT JOIN \"Lieferungen\" AS \"L2\" ON \"L2\".\"Charge\"=\"ChargenVerbindungen\".\"Produkt\" "
				+ " WHERE \"ChargenVerbindungen\".\"ID\" IS NOT NULL AND \"L1\".\"typePU\" = \"L2\".\"typePU\" GROUP BY \"L1\".\"ID\",\"L1\".\"typePU\",\"L2\".\"typePU\") "
				+ " WHERE \"Type_In\" = \"Type_Out\" " + " GROUP BY \"id2\",\"Type_In\",\"Type_Out\"";
		rsp = DBKernel.getResultSet(conn, sql, false);
		if (rsp != null && rsp.first()) {
			do {
				if (rsp.getObject("Amount_In") != null && rsp.getObject("Amount_Out") != null) {
					double in = rsp.getDouble("Amount_In");
					double out = rsp.getDouble("Amount_Out");
					if (in > out * 2 || out > in * 2) { // 1.1
						setWarningMessage("Amounts correct?? In: " + rsp.getString("ids_in") + " (" + in + ") vs. Out: "
								+ rsp.getString("ids_out") + " (" + out + ")");
						warningsThere = true;
					}
				}
			} while (rsp.next());
		}

		if (warningsThere) {
			setWarningMessage("Look into the console - there are plausibility issues...");
		}

		// Stations
		DataTableSpec stationSpec = getStationSpec(conn);
		BufferedDataContainer stationContainer = exec.createDataContainer(stationSpec);
		int stationIndex = 0;
		boolean bvlFormat = false;

		for (Record r : DSL.using(conn, SQLDialect.HSQLDB).select().from(STATION)) {
			String id = stationIds.get(r.getValue(STATION.ID));
			String district = clean(r.getValue(STATION.DISTRICT));
			String state = clean(r.getValue(STATION.BUNDESLAND));
			String country = clean(r.getValue(STATION.LAND));
			String zip = clean(r.getValue(STATION.PLZ));

			// TODO: Remove BVL-Format stuff. Corrupt databases should not be
			// fixed here
			if (stationIndex == 0 && state != null && (state.equals("Altenburger Land") || state.equals("Wesel"))) {
				bvlFormat = true;
			}

			if (bvlFormat) {
				district = state;
				state = country;

				if (zip != null && zip.length() == 4) {
					country = "BE";
				} else {
					country = "DE";
				}
			}

			String company = r.getValue(STATION.NAME) == null || doAnonymize
					? getISO3166_2(country, state) + "#" + r.getValue(STATION.ID) : clean(r.getValue(STATION.NAME));
			DataCell[] cells = new DataCell[stationSpec.getNumColumns()];

			fillCell(stationSpec, cells, TracingColumns.ID, createCell(id));
			fillCell(stationSpec, cells, TracingColumns.STATION_NODE, createCell(company));
			fillCell(stationSpec, cells, TracingColumns.STATION_NAME, createCell(company));
			fillCell(stationSpec, cells, TracingColumns.STATION_STREET,
					doAnonymize ? DataType.getMissingCell() : createCell(r.getValue(STATION.STRASSE)));
			fillCell(stationSpec, cells, TracingColumns.STATION_HOUSENO,
					doAnonymize ? DataType.getMissingCell() : createCell(r.getValue(STATION.HAUSNUMMER)));
			fillCell(stationSpec, cells, TracingColumns.STATION_ZIP, createCell(zip));
			fillCell(stationSpec, cells, TracingColumns.STATION_CITY,
					doAnonymize ? DataType.getMissingCell() : createCell(r.getValue(STATION.ORT)));
			fillCell(stationSpec, cells, TracingColumns.STATION_DISTRICT,
					doAnonymize ? DataType.getMissingCell() : createCell(district));
			fillCell(stationSpec, cells, TracingColumns.STATION_STATE,
					doAnonymize ? DataType.getMissingCell() : createCell(state));
			fillCell(stationSpec, cells, TracingColumns.STATION_COUNTRY,
					doAnonymize ? DataType.getMissingCell() : createCell(country));
			fillCell(stationSpec, cells, TracingColumns.STATION_VAT,
					doAnonymize ? DataType.getMissingCell() : createCell(r.getValue(STATION.VATNUMBER)));
			fillCell(stationSpec, cells, TracingColumns.STATION_TOB, createCell(r.getValue(STATION.BETRIEBSART)));
			fillCell(stationSpec, cells, TracingColumns.STATION_NUMCASES, createCell(r.getValue(STATION.ANZAHLFAELLE)));
			fillCell(stationSpec, cells, TracingColumns.STATION_DATESTART, createCell(r.getValue(STATION.DATUMBEGINN)));
			fillCell(stationSpec, cells, TracingColumns.STATION_DATEPEAK,
					createCell(r.getValue(STATION.DATUMHOEHEPUNKT)));
			fillCell(stationSpec, cells, TracingColumns.STATION_DATEEND, createCell(r.getValue(STATION.DATUMENDE)));
			fillCell(stationSpec, cells, TracingColumns.STATION_SERIAL, createCell(r.getValue(STATION.SERIAL)));
			fillCell(stationSpec, cells, TracingColumns.STATION_SIMPLESUPPLIER,
					isSimpleSupplier(allDeliveries, id) ? BooleanCell.TRUE : BooleanCell.FALSE);
			fillCell(stationSpec, cells, TracingColumns.STATION_DEADSTART,
					isStationStart(allDeliveries, id) ? BooleanCell.TRUE : BooleanCell.FALSE);
			fillCell(stationSpec, cells, TracingColumns.STATION_DEADEND,
					isStationEnd(allDeliveries, id) ? BooleanCell.TRUE : BooleanCell.FALSE);
			fillCell(stationSpec, cells, TracingColumns.FILESOURCES, createCell(r.getValue(STATION.IMPORTSOURCES)));
			fillCell(stationSpec, cells, TracingColumns.STATION_COUNTY,
					doAnonymize ? DataType.getMissingCell() : createCell(district));

			// Extras
			for (String extraCol : stationSpec.getColumnNames()) {
				if (extraCol.startsWith("_")) {
					String attribute = extraCol.substring(1);
					Result<Record1<String>> result = DSL.using(conn, SQLDialect.HSQLDB).select(EXTRAFIELDS.VALUE)
							.from(EXTRAFIELDS)
							.where(EXTRAFIELDS.TABLENAME.equal(STATION.getName()),
									EXTRAFIELDS.ID.equal(r.getValue(STATION.ID)),
									EXTRAFIELDS.ATTRIBUTE.equal(attribute))
							.fetch();

					if (!result.isEmpty()) {
						fillCell(stationSpec, cells, extraCol, createCell(result.get(0).value1()));
					} else {
						fillCell(stationSpec, cells, extraCol, DataType.getMissingCell());
					}
				}
			}

			stationContainer.addRowToTable(new DefaultRow("Row" + stationIndex++, cells));
			exec.checkCanceled();
		}

		stationContainer.close();

		// Deliveries
		DataTableSpec deliverySpec = getDeliverySpec(conn);
		BufferedDataContainer deliveryContainer = exec.createDataContainer(deliverySpec);
		int deliveryIndex = 0;

		for (Record r : DSL.using(conn, SQLDialect.HSQLDB).select().from(LIEFERUNGEN).leftOuterJoin(CHARGEN)
				.on(LIEFERUNGEN.CHARGE.equal(CHARGEN.ID)).leftOuterJoin(PRODUKTKATALOG)
				.on(CHARGEN.ARTIKEL.equal(PRODUKTKATALOG.ID)).orderBy(PRODUKTKATALOG.ID)) {
			String id = deliveryIds.get(r.getValue(LIEFERUNGEN.ID));
			String fromId = stationIds.get(r.getValue(PRODUKTKATALOG.STATION));
			String toId = stationIds.get(r.getValue(LIEFERUNGEN.EMPFÄNGER));
			DataCell[] cells = new DataCell[deliverySpec.getNumColumns()];

			fillCell(deliverySpec, cells, TracingColumns.ID, createCell(id));
			fillCell(deliverySpec, cells, TracingColumns.FROM, createCell(fromId));
			fillCell(deliverySpec, cells, TracingColumns.TO, createCell(toId));

			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_ITEMNAME,
					createCell(r.getValue(PRODUKTKATALOG.BEZEICHNUNG)));
			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_ITEMNUM,
					doAnonymize ? DataType.getMissingCell() : createCell(r.getValue(PRODUKTKATALOG.ARTIKELNUMMER)));
			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_DEPARTURE,
					createCell(formatDate(r.getValue(LIEFERUNGEN.DD_DAY), r.getValue(LIEFERUNGEN.DD_MONTH),
							r.getValue(LIEFERUNGEN.DD_YEAR))));
			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_ARRIVAL,
					createCell(formatDate(r.getValue(LIEFERUNGEN.AD_DAY), r.getValue(LIEFERUNGEN.AD_MONTH),
							r.getValue(LIEFERUNGEN.AD_YEAR))));
			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_SERIAL, createCell(r.getValue(LIEFERUNGEN.SERIAL)));
			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_PROCESSING,
					createCell(r.getValue(PRODUKTKATALOG.PROZESSIERUNG)));
			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_USAGE,
					createCell(r.getValue(PRODUKTKATALOG.INTENDEDUSE)));
			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_LOTNUM,
					doAnonymize ? DataType.getMissingCell() : createCell(r.getValue(CHARGEN.CHARGENNR)));
			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_DATEEXP,
					createCell(formatDate(r.getValue(CHARGEN.MHD_DAY), r.getValue(CHARGEN.MHD_MONTH),
							r.getValue(CHARGEN.MHD_YEAR))));
			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_DATEMANU, createCell(
					formatDate(r.getValue(CHARGEN.PD_DAY), r.getValue(CHARGEN.PD_MONTH), r.getValue(CHARGEN.PD_YEAR))));
			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_AMOUNT,
					createCell(getAmountInKg(r.getValue(LIEFERUNGEN.UNITMENGE), r.getValue(LIEFERUNGEN.UNITEINHEIT))));
			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_NUM_PU, createCell(r.getValue(LIEFERUNGEN.NUMPU)));
			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_TYPE_PU, createCell(r.getValue(LIEFERUNGEN.TYPEPU)));
			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_ENDCHAIN,
					createCell(r.getValue(LIEFERUNGEN.ENDCHAIN)));
			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_ORIGIN,
					createCell(r.getValue(CHARGEN.ORIGINCOUNTRY)));
			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_ENDCHAINWHY,
					createCell(r.getValue(LIEFERUNGEN.EXPLANATION_ENDCHAIN)));
			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_REMARKS,
					createCell(r.getValue(LIEFERUNGEN.CONTACT_QUESTIONS_REMARKS)));
			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_FURTHERTB,
					createCell(r.getValue(LIEFERUNGEN.FURTHER_TRACEBACK)));
			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_MICROSAMPLE,
					createCell(r.getValue(CHARGEN.MICROBIOSAMPLE)));
			fillCell(deliverySpec, cells, TracingColumns.FILESOURCES,
					createCell(r.getValue(LIEFERUNGEN.IMPORTSOURCES)));
			fillCell(deliverySpec, cells, TracingColumns.DELIVERY_CHARGENUM,
					doAnonymize ? DataType.getMissingCell() : createCell(r.getValue(CHARGEN.CHARGENNR)));

			// Extras
			for (String extraCol : deliverySpec.getColumnNames()) {
				if (extraCol.startsWith("_")) {
					String attribute = extraCol.substring(1);
					int index = attribute.indexOf(".");
					String tn = attribute.substring(0, index);
					String fn = attribute.substring(index + 1);

					Result<Record1<String>> result = DSL.using(conn, SQLDialect.HSQLDB).select(EXTRAFIELDS.VALUE)
							.from(EXTRAFIELDS).where(EXTRAFIELDS.TABLENAME.equal(tn),
									EXTRAFIELDS.ID.equal(r.getValue(LIEFERUNGEN.ID)), EXTRAFIELDS.ATTRIBUTE.equal(fn))
							.fetch();

					if (!result.isEmpty()) {
						fillCell(deliverySpec, cells, extraCol, createCell(result.get(0).value1()));
					} else {
						fillCell(deliverySpec, cells, extraCol, DataType.getMissingCell());
					}
				}
			}

			DataRow outputRow = new DefaultRow("Row" + deliveryIndex++, cells);

			deliveryContainer.addRowToTable(outputRow);
			exec.checkCanceled();
		}

		deliveryContainer.close();

		BufferedDataContainer deliveryConnectionsContainer = exec.createDataContainer(
				new DataTableSpec(new DataColumnSpecCreator(TracingColumns.ID, StringCell.TYPE).createSpec(),
						new DataColumnSpecCreator(TracingColumns.NEXT, StringCell.TYPE).createSpec()));
		int deliveryConnectionsIndex = 0;

		for (Delivery delivery : allDeliveries.values()) {
			for (String next : delivery.getAllNextIds()) {
				deliveryConnectionsContainer.addRowToTable(new DefaultRow(deliveryConnectionsIndex++ + "",
						createCell(delivery.getId()), createCell(next)));
			}
		}

		deliveryConnectionsContainer.close();

		return new BufferedDataTable[] { stationContainer.getTable(), deliveryContainer.getTable(),
				deliveryConnectionsContainer.getTable() };
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
		return new DataTableSpec[] { null, null, null };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		settings.addString(PARAM_FILENAME, filename);
		settings.addBoolean(PARAM_OVERRIDE, override);
		settings.addBoolean(PARAM_ANONYMIZE, doAnonymize);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		filename = settings.getString(PARAM_FILENAME);
		override = settings.getBoolean(PARAM_OVERRIDE);
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
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	private static void fillCell(DataTableSpec spec, DataCell[] cells, String columnname, DataCell value) {
		int index = spec.findColumnIndex(columnname);
		if (index >= 0)
			cells[index] = value;
	}

	private static DataCell createCell(String s) {
		return s != null ? new StringCell(clean(s)) : DataType.getMissingCell();
	}

	private static DataCell createCell(Date d) {
		return d != null ? new StringCell(d.toString()) : DataType.getMissingCell();
	}

	private static DataCell createCell(Integer i) {
		return i != null ? new IntCell(i) : DataType.getMissingCell();
	}

	private static DataCell createCell(Double d) {
		return d != null ? new DoubleCell(d) : DataType.getMissingCell();
	}

	private static String clean(String s) {
		if (s == null || s.equalsIgnoreCase("null")) {
			return null;
		}

		return s.replace("\n", "|").replaceAll("\\p{C}", "").replace("\u00A0", "").replace("\t", " ").trim();
	}

	private static String getISO3166_2(String country, String state) {
		for (String code : Locale.getISOCountries()) {
			if (new Locale("", code).getDisplayCountry(Locale.ENGLISH).equals(country)) {
				return code;
			}
		}

		if (state != null && state.length() >= 2) {
			return state.substring(0, 2);
		}

		return "NN";
	}

	private static String formatDate(Integer day, Integer month, Integer year) {
		if (year == null)
			return null;

		String thisYear = new SimpleDateFormat("yyyy").format(new Date());

		if (year.toString().length() == 2)
			year = year > Integer.parseInt(thisYear.substring(2)) ? 1900 : 2000 + year;

		if (month == null) {
			return year.toString();
		} else if (day == null) {
			return year + "-" + new DecimalFormat("00").format(month);
		}

		return year + "-" + new DecimalFormat("00").format(month) + "-" + new DecimalFormat("00").format(day);
	}

	private static Double getAmountInKg(Double u3, String bu3) {
		if (u3 != null && bu3 != null) {
			if (bu3.equalsIgnoreCase("t")) {
				return u3 * 1000.0;
			} else if (bu3.equalsIgnoreCase("kg")) {
				return u3;
			} else if (bu3.equalsIgnoreCase("g")) {
				return u3 / 1000.0;
			}
		}

		return null;
	}

	private static DataTableSpec getStationSpec(Connection conn) {
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

		if (containsValues(conn, STATION.VATNUMBER))
			columns.add(new DataColumnSpecCreator(TracingColumns.STATION_VAT, StringCell.TYPE).createSpec());
		if (containsValues(conn, STATION.BETRIEBSART))
			columns.add(new DataColumnSpecCreator(TracingColumns.STATION_TOB, StringCell.TYPE).createSpec());
		if (containsValues(conn, STATION.ANZAHLFAELLE))
			columns.add(new DataColumnSpecCreator(TracingColumns.STATION_NUMCASES, IntCell.TYPE).createSpec());
		if (containsValues(conn, STATION.DATUMBEGINN))
			columns.add(new DataColumnSpecCreator(TracingColumns.STATION_DATESTART, StringCell.TYPE).createSpec());
		if (containsValues(conn, STATION.DATUMHOEHEPUNKT))
			columns.add(new DataColumnSpecCreator(TracingColumns.STATION_DATEPEAK, StringCell.TYPE).createSpec());
		if (containsValues(conn, STATION.DATUMENDE))
			columns.add(new DataColumnSpecCreator(TracingColumns.STATION_DATEEND, StringCell.TYPE).createSpec());
		if (containsValues(conn, STATION.IMPORTSOURCES))
			columns.add(new DataColumnSpecCreator(TracingColumns.FILESOURCES, StringCell.TYPE).createSpec());

		// due to backward compatibility:
		columns.add(new DataColumnSpecCreator(TracingColumns.STATION_COUNTY, StringCell.TYPE).createSpec());

		// ExtraFields
		for (Record1<String> r : DSL.using(conn, SQLDialect.HSQLDB).selectDistinct(EXTRAFIELDS.ATTRIBUTE)
				.from(EXTRAFIELDS).where(EXTRAFIELDS.TABLENAME.equal(STATION.getName()))) {
			columns.add(new DataColumnSpecCreator("_" + r.value1(), StringCell.TYPE).createSpec());
		}

		return new DataTableSpec(columns.toArray(new DataColumnSpec[0]));
	}

	private static DataTableSpec getDeliverySpec(Connection conn) {
		List<DataColumnSpec> columns = new ArrayList<>();
		columns.add(new DataColumnSpecCreator(TracingColumns.ID, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.FROM, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.TO, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_ITEMNUM, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_ITEMNAME, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_DEPARTURE, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_ARRIVAL, StringCell.TYPE).createSpec());
		columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_SERIAL, StringCell.TYPE).createSpec());

		if (containsValues(conn, PRODUKTKATALOG.PROZESSIERUNG))
			columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_PROCESSING, StringCell.TYPE).createSpec());
		if (containsValues(conn, PRODUKTKATALOG.INTENDEDUSE))
			columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_USAGE, StringCell.TYPE).createSpec());
		if (containsValues(conn, CHARGEN.CHARGENNR))
			columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_LOTNUM, StringCell.TYPE).createSpec());
		if (containsValues(conn, CHARGEN.MHD_DAY, CHARGEN.MHD_MONTH, CHARGEN.MHD_YEAR))
			columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_DATEEXP, StringCell.TYPE).createSpec());
		if (containsValues(conn, CHARGEN.PD_DAY, CHARGEN.PD_MONTH, CHARGEN.PD_YEAR))
			columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_DATEMANU, StringCell.TYPE).createSpec());
		if (containsValues(conn, LIEFERUNGEN.UNITMENGE))
			columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_AMOUNT, DoubleCell.TYPE).createSpec());

		if (containsValues(conn, LIEFERUNGEN.NUMPU)) {
			columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_NUM_PU, DoubleCell.TYPE).createSpec());
			if (containsValues(conn, LIEFERUNGEN.TYPEPU))
				columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_TYPE_PU, StringCell.TYPE).createSpec());
		}

		if (containsValues(conn, CHARGEN.ORIGINCOUNTRY))
			columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_ORIGIN, StringCell.TYPE).createSpec());
		if (containsValues(conn, LIEFERUNGEN.ENDCHAIN))
			columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_ENDCHAIN, StringCell.TYPE).createSpec());
		if (containsValues(conn, LIEFERUNGEN.EXPLANATION_ENDCHAIN))
			columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_ENDCHAINWHY, StringCell.TYPE).createSpec());
		if (containsValues(conn, LIEFERUNGEN.CONTACT_QUESTIONS_REMARKS))
			columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_REMARKS, StringCell.TYPE).createSpec());
		if (containsValues(conn, LIEFERUNGEN.FURTHER_TRACEBACK))
			columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_FURTHERTB, StringCell.TYPE).createSpec());
		if (containsValues(conn, CHARGEN.MICROBIOSAMPLE))
			columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_MICROSAMPLE, StringCell.TYPE).createSpec());
		if (containsValues(conn, LIEFERUNGEN.IMPORTSOURCES))
			columns.add(new DataColumnSpecCreator(TracingColumns.FILESOURCES, StringCell.TYPE).createSpec());
		if (containsValues(conn, CHARGEN.CHARGENNR))
			columns.add(new DataColumnSpecCreator(TracingColumns.DELIVERY_CHARGENUM, StringCell.TYPE).createSpec());

		// ExtraFields
		for (Record2<String, String> r : DSL.using(conn, SQLDialect.HSQLDB)
				.selectDistinct(EXTRAFIELDS.TABLENAME, EXTRAFIELDS.ATTRIBUTE).from(EXTRAFIELDS)
				.where(EXTRAFIELDS.TABLENAME.equal(PRODUKTKATALOG.getName()))
				.or(EXTRAFIELDS.TABLENAME.equal(CHARGEN.getName()))
				.or(EXTRAFIELDS.TABLENAME.equal(LIEFERUNGEN.getName()))) {
			columns.add(new DataColumnSpecCreator("_" + r.value1() + "." + r.value2(), StringCell.TYPE).createSpec());
		}

		return new DataTableSpec(columns.toArray(new DataColumnSpec[0]));
	}

	private static boolean containsValues(Connection conn, TableField<?, ?>... fields) {
		for (TableField<?, ?> field : fields) {
			for (Record1<?> r : DSL.using(conn, SQLDialect.HSQLDB).selectDistinct(field).from(field.getTable())) {
				if (r.value1() != null) {
					return true;
				}
			}
		}

		return false;
	}

	private static Connection createLocalConnection(String dbUsername, String dbPassword, String dbFile)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		Class.forName("org.hsqldb.jdbc.JDBCDriver").newInstance();

		String connStr = "jdbc:hsqldb:file:" + dbFile;
		Connection result = DriverManager.getConnection(connStr, dbUsername, dbPassword);

		result.setReadOnly(true);

		return result;
	}

	private static boolean isStationStart(Map<String, Delivery> deliveries, String id) {
		for (Delivery d : deliveries.values()) {
			if (d.getRecipientId().equals(id)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isSimpleSupplier(Map<String, Delivery> deliveries, String id) {
		if (isStationStart(deliveries, id)) {
			String recId = null;
			for (Delivery d : deliveries.values()) {
				if (d.getSupplierId().equals(id)) {
					if (recId == null)
						recId = d.getRecipientId();
					else if (!recId.equals(d.getRecipientId()))
						return false;
				}
			}
			return true;
		}
		return false;
	}

	private static boolean isStationEnd(Map<String, Delivery> deliveries, String id) {
		for (Delivery d : deliveries.values()) {
			if (d.getSupplierId().equals(id)) {
				return false;
			}
		}

		return true;
	}

	private static Map<String, Delivery> getNewTracingModel(Connection conn, Map<Integer, String> stationIds,
			Map<Integer, String> deliveryIds) {
		Map<String, Delivery> allDeliveries = new LinkedHashMap<>();

		Select<Record> deliverySelect = DSL.using(conn, SQLDialect.HSQLDB).select().from(LIEFERUNGEN)
				.leftOuterJoin(CHARGEN).on(LIEFERUNGEN.CHARGE.equal(CHARGEN.ID)).leftOuterJoin(PRODUKTKATALOG)
				.on(CHARGEN.ARTIKEL.equal(PRODUKTKATALOG.ID));

		for (Record r : deliverySelect) {
			Integer id = r.getValue(LIEFERUNGEN.ID);
			Integer from = r.getValue(PRODUKTKATALOG.STATION);
			Integer to = r.getValue(LIEFERUNGEN.EMPFÄNGER);
			String lotNumber = r.getValue(CHARGEN.CHARGENNR) != null ? r.getValue(CHARGEN.CHARGENNR)
					: r.getValue(CHARGEN.ID).toString();
			Double amountInKg = getAmountInKg(r.getValue(LIEFERUNGEN.UNITMENGE), r.getValue(LIEFERUNGEN.UNITEINHEIT));

			if (id != null && from != null && to != null) {
				Delivery d = new Delivery(deliveryIds.get(id), stationIds.get(from), stationIds.get(to),
						r.getValue(LIEFERUNGEN.DD_DAY), r.getValue(LIEFERUNGEN.DD_MONTH),
						r.getValue(LIEFERUNGEN.DD_YEAR), r.getValue(LIEFERUNGEN.AD_DAY),
						r.getValue(LIEFERUNGEN.AD_MONTH), r.getValue(LIEFERUNGEN.AD_YEAR), lotNumber,
						r.getValue(LIEFERUNGEN.NUMPU), r.getValue(LIEFERUNGEN.TYPEPU), amountInKg);

				allDeliveries.put(d.getId(), d);
			}
		}

		Select<Record> deliveryToDeliverySelect = DSL.using(conn, SQLDialect.HSQLDB).select().from(CHARGENVERBINDUNGEN)
				.leftOuterJoin(LIEFERUNGEN).on(CHARGENVERBINDUNGEN.PRODUKT.equal(LIEFERUNGEN.CHARGE));

		for (Record r : deliveryToDeliverySelect) {
			Delivery from = allDeliveries.get(deliveryIds.get(r.getValue(CHARGENVERBINDUNGEN.ZUTAT)));
			Delivery to = allDeliveries.get(deliveryIds.get(r.getValue(LIEFERUNGEN.ID)));

			if (from != null && to != null) {
				from.getAllNextIds().add(to.getId());
				to.getAllPreviousIds().add(from.getId());
			}
		}

		return allDeliveries;
	}

	private static boolean serialPossible(Connection conn) {
		Set<String> stationSerials = new LinkedHashSet<>();

		for (Record1<String> r : DSL.using(conn, SQLDialect.HSQLDB).select(STATION.SERIAL).from(STATION)) {
			if (r.value1() == null || !stationSerials.add(r.value1())) {
				return false;
			}
		}

		Set<String> deliverySerials = new LinkedHashSet<>();
		boolean alwaysKg = true;

		for (Record2<String, String> r : DSL.using(conn, SQLDialect.HSQLDB)
				.select(LIEFERUNGEN.SERIAL, LIEFERUNGEN.UNITEINHEIT).from(LIEFERUNGEN)) {
			if (r.value1() == null || !deliverySerials.add(r.value1())) {
				return false;
			}

			if (!"kg".equals(r.value2())) {
				alwaysKg = false;
			}
		}

		if (alwaysKg) {
			// beim EFSA Importer wurde immer kg eingetragen, später beim
			// bfrnewimporter wurde nur noch "numPU" und "typePU" benutzt
			// und UnitEinheit müsste immer NULL sein, daher ist das ein
			// sehr gutes Indiz dafür, dass wir es mit alten Daten zu tun
			// haben
			return false;
		}

		return true;
	}
}
