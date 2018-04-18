/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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

import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.CHARGEN;
import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.CHARGENVERBINDUNGEN;
import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.EXTRAFIELDS;
import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.LIEFERUNGEN;
import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.PRODUKTKATALOG;
import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.STATION;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.InvalidPathException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
import org.jooq.SelectJoinStep;
import org.jooq.TableField;
import org.jooq.impl.DSL;
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
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.NoInternalsNodeModel;
import de.bund.bfr.knime.openkrise.common.Delivery;
import de.bund.bfr.knime.openkrise.common.DeliveryUtils;
import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;
import de.bund.bfr.knime.openkrise.db.MyDBTablesNew;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This is the model implementation of MyKrisenInterfaces.
 * 
 * 
 * @author draaw
 */
public class MyKrisenInterfacesNodeModel extends NoInternalsNodeModel {

	private static final ImmutableMap<String, TableField<?, Integer>> ID_COLUMNS = ImmutableMap.of(STATION.getName(),
			STATION.ID, LIEFERUNGEN.getName(), LIEFERUNGEN.ID, CHARGEN.getName(), CHARGEN.ID, PRODUKTKATALOG.getName(),
			PRODUKTKATALOG.ID);

	private MyKrisenInterfacesSettings set;
	
	private HashMap<String, String> extraFieldS;
	private HashMap<String, String> extraFieldD;

	/**
	 * Constructor for the node model.
	 */
	protected MyKrisenInterfacesNodeModel() {
		super(0, 3);
		set = new MyKrisenInterfacesSettings();
		extraFieldS = new HashMap<>();
		extraFieldD = new HashMap<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
		try (Connection conn = set.isUseExternalDb()
				? createLocalConnection(KnimeUtils.getFile(removeNameOfDB(set.getDbPath())).getAbsolutePath())
				: DBKernel.getLocalConn(true)) {
			boolean isFormat2017 = !DeliveryUtils.hasOnlyPositiveIDs(conn); // check if format is earlier than Format2017. Starting with 2017 negative IDs are more than probable. Now, please ignore SerialIDs!!!!!
			boolean useSerialAsID = !set.isAnonymize() && DeliveryUtils.hasUniqueSerials(conn) && !isFormat2017;
			Map<Integer, String> stationIds = DeliveryUtils.getStationIds(conn, useSerialAsID);
			Map<Integer, String> deliveryIds = DeliveryUtils.getDeliveryIds(conn, useSerialAsID);
			SetMultimap<String, String> warnings = LinkedHashMultimap.create();

			List<Delivery> deliveries = DeliveryUtils.getDeliveries(conn, stationIds, deliveryIds, warnings);
			BufferedDataTable stationTable = getStationTable(conn, stationIds, deliveries, exec, useSerialAsID, isFormat2017);
			BufferedDataTable deliveryTable = getDeliveryTable(conn, stationIds, deliveryIds, exec, useSerialAsID, isFormat2017);
			BufferedDataTable deliveryConnectionsTable = getDeliveryConnectionsTable(deliveries, deliveryTable, exec);

			if (!warnings.isEmpty()) {
				for (Map.Entry<String, Set<String>> entry : Multimaps.asMap(warnings).entrySet()) {
					setWarningMessage(entry.getKey() + ":");

					for (String w : entry.getValue()) {
						setWarningMessage("\t" + w);
					}
				}

				setWarningMessage("Look into the console - there are plausibility issues...");
			}

			return new BufferedDataTable[] { stationTable, deliveryTable, deliveryConnectionsTable };
		}
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
		set.saveSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		set.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
	}

	protected static String removeNameOfDB(String path) throws InvalidPathException, MalformedURLException {
		if (path == null) {
			return null;
		}

		if ((path.endsWith("\\DB") || path.endsWith("/DB")) && KnimeUtils.getFile(path + ".properties").exists()) {
			return path.substring(0, path.length() - 3);
		}

		return path;
	}

	private DataTableSpec getStationSpec(Connection conn, boolean useSerialAsId, boolean isFormat2017) {
		List<DataColumnSpec> columns = new ArrayList<>();

		addSpec(columns, TracingColumns.ID, StringCell.TYPE);

		addSpecIf(set.isLotBased(), columns, TracingColumns.LOT_NUMBER, StringCell.TYPE);
		addSpecIf(set.isLotBased(), columns, TracingColumns.NAME, StringCell.TYPE);
		addSpecIf(set.isLotBased(), columns, TracingColumns.STATION_ID, StringCell.TYPE);
		if (!isFormat2017) addSpecIf(!useSerialAsId, columns, BackwardUtils.STATION_SERIAL, StringCell.TYPE);
		addSpecIf(!set.isLotBased(), columns, TracingColumns.NAME, StringCell.TYPE);
		addSpecIf(set.isLotBased(), columns, TracingColumns.STATION_NAME, StringCell.TYPE);
		if (!isFormat2017) {
			addSpecIf(set.isEnsureBackwardCompatibility() || hasValues(conn, STATION.STRASSE), columns,
					TracingColumns.STATION_STREET, StringCell.TYPE);
			addSpecIf(set.isEnsureBackwardCompatibility() || hasValues(conn, STATION.HAUSNUMMER), columns,
					TracingColumns.STATION_HOUSENO, StringCell.TYPE);
			addSpecIf(set.isEnsureBackwardCompatibility() || hasValues(conn, STATION.PLZ), columns,
					TracingColumns.STATION_ZIP, StringCell.TYPE);
			addSpecIf(set.isEnsureBackwardCompatibility() || hasValues(conn, STATION.ORT), columns,
					TracingColumns.STATION_CITY, StringCell.TYPE);
			addSpecIf(set.isEnsureBackwardCompatibility() || hasValues(conn, STATION.DISTRICT), columns,
					TracingColumns.STATION_DISTRICT, StringCell.TYPE);
			addSpecIf(set.isEnsureBackwardCompatibility() || hasValues(conn, STATION.BUNDESLAND), columns,
					TracingColumns.STATION_STATE, StringCell.TYPE);
		}

		addSpecIf(hasValues(conn, STATION.ADRESSE), columns, TracingColumns.ADDRESS, StringCell.TYPE);
		addSpec(columns, TracingColumns.STATION_COUNTRY, StringCell.TYPE);

		addSpecIf(hasValues(conn, STATION.BETRIEBSART), columns, TracingColumns.STATION_TOB, StringCell.TYPE);

		addSpecIf(!set.isLotBased(), columns, TracingColumns.STATION_SIMPLESUPPLIER, BooleanCell.TYPE);
		addSpecIf(!set.isLotBased(), columns, TracingColumns.STATION_DEADSTART, BooleanCell.TYPE);
		addSpecIf(!set.isLotBased(), columns, TracingColumns.STATION_DEADEND, BooleanCell.TYPE);

		addSpecIf(set.isLotBased() && hasValues(conn, PRODUKTKATALOG.ARTIKELNUMMER), columns,
				TracingColumns.PRODUCT_NUMBER, StringCell.TYPE);
		addSpecIf(hasValues(conn, STATION.VATNUMBER), columns, BackwardUtils.STATION_VAT, StringCell.TYPE);
		addSpecIf(hasValues(conn, STATION.ANZAHLFAELLE), columns, BackwardUtils.STATION_NUMCASES, IntCell.TYPE);
		addSpecIf(hasValues(conn, STATION.DATUMBEGINN), columns, BackwardUtils.STATION_DATESTART, StringCell.TYPE);
		addSpecIf(hasValues(conn, STATION.DATUMHOEHEPUNKT), columns, BackwardUtils.STATION_DATEPEAK, StringCell.TYPE);
		addSpecIf(hasValues(conn, STATION.DATUMENDE), columns, BackwardUtils.STATION_DATEEND, StringCell.TYPE);
		if (!isFormat2017) addSpecIf(hasValues(conn, STATION.IMPORTSOURCES), columns, TracingColumns.FILESOURCES, StringCell.TYPE);

		addSpecIf(set.isEnsureBackwardCompatibility(), columns, BackwardUtils.STATION_NODE, StringCell.TYPE);
		addSpecIf(set.isEnsureBackwardCompatibility(), columns, BackwardUtils.STATION_COUNTY, StringCell.TYPE);

		if (!set.isLotBased()) {
			for (Record1<String> r : DSL.using(conn, SQLDialect.HSQLDB).selectDistinct(EXTRAFIELDS.ATTRIBUTE)
					.from(EXTRAFIELDS).where(EXTRAFIELDS.TABLENAME.equal(STATION.getName()))) {
				String betterName = "_" + r.value1();
				if (isFormat2017) {
					if (betterName.equals("_Source")) {betterName = "Source"; extraFieldS.put("Source", "_Source");}
					else addSpec(columns, betterName, StringCell.TYPE);
				}
				else {
					addSpec(columns, betterName, StringCell.TYPE);
				}
			}
		} else {
			for (Record2<String, String> r : DSL.using(conn, SQLDialect.HSQLDB)
					.selectDistinct(EXTRAFIELDS.TABLENAME, EXTRAFIELDS.ATTRIBUTE).from(EXTRAFIELDS)
					.where(EXTRAFIELDS.TABLENAME.equal(STATION.getName()))
					.or(EXTRAFIELDS.TABLENAME.equal(CHARGEN.getName()))
					.or(EXTRAFIELDS.TABLENAME.equal(PRODUKTKATALOG.getName()))) {
				addSpec(columns, "_" + r.value1() + "." + r.value2(), StringCell.TYPE);
			}
		}

		return new DataTableSpec(columns.toArray(new DataColumnSpec[0]));
	}

	private DataTableSpec getDeliverySpec(Connection conn, boolean useSerialAsId, boolean isFormat2017) {
		List<DataColumnSpec> columns = new ArrayList<>();

		addSpec(columns, TracingColumns.ID, StringCell.TYPE);
		addSpecIf(set.isLotBased(), columns, TracingColumns.DELIVERY_ID, StringCell.TYPE);
		if (!isFormat2017) addSpecIf(!useSerialAsId, columns, BackwardUtils.DELIVERY_SERIAL, StringCell.TYPE);

		addSpec(columns, TracingColumns.FROM, StringCell.TYPE);
		addSpec(columns, TracingColumns.TO, StringCell.TYPE);
		addSpec(columns, TracingColumns.NAME, StringCell.TYPE);

		addSpec(columns, TracingColumns.LOT_ID, StringCell.TYPE);

		addSpecIf(set.isEnsureBackwardCompatibility() || !set.isLotBased(), columns, TracingColumns.LOT_NUMBER,
				StringCell.TYPE);
		addSpec(columns, TracingColumns.DELIVERY_DEPARTURE, StringCell.TYPE);
		addSpec(columns, TracingColumns.DELIVERY_ARRIVAL, StringCell.TYPE);

		addSpecIf(
				hasValues(conn, PRODUKTKATALOG.ARTIKELNUMMER)
						&& (set.isEnsureBackwardCompatibility() || !set.isLotBased()),
				columns, TracingColumns.PRODUCT_NUMBER, StringCell.TYPE);
		addSpecIf(hasValues(conn, PRODUKTKATALOG.PROZESSIERUNG), columns, BackwardUtils.DELIVERY_PROCESSING,
				StringCell.TYPE);
		addSpecIf(hasValues(conn, PRODUKTKATALOG.INTENDEDUSE), columns, BackwardUtils.DELIVERY_USAGE, StringCell.TYPE);
		addSpecIf(hasValues(conn, CHARGEN.MHD_DAY, CHARGEN.MHD_MONTH, CHARGEN.MHD_YEAR), columns,
				BackwardUtils.DELIVERY_DATEEXP, StringCell.TYPE);
		addSpecIf(hasValues(conn, CHARGEN.PD_DAY, CHARGEN.PD_MONTH, CHARGEN.PD_YEAR), columns,
				BackwardUtils.DELIVERY_DATEMANU, StringCell.TYPE);
		if (!isFormat2017) addSpecIf(hasValues(conn, LIEFERUNGEN.UNITMENGE), columns, TracingColumns.DELIVERY_AMOUNT, DoubleCell.TYPE);

		if (!isFormat2017 && hasValues(conn, LIEFERUNGEN.NUMPU)) {
			addSpec(columns, TracingColumns.DELIVERY_NUM_PU, DoubleCell.TYPE);
			addSpecIf(hasValues(conn, LIEFERUNGEN.TYPEPU), columns, TracingColumns.DELIVERY_TYPE_PU, StringCell.TYPE);
		}

		addSpecIf(hasValues(conn, CHARGEN.ORIGINCOUNTRY), columns, BackwardUtils.DELIVERY_ORIGIN, StringCell.TYPE);
		addSpecIf(hasValues(conn, LIEFERUNGEN.ENDCHAIN), columns, BackwardUtils.DELIVERY_ENDCHAIN, StringCell.TYPE);
		addSpecIf(hasValues(conn, LIEFERUNGEN.EXPLANATION_ENDCHAIN), columns, BackwardUtils.DELIVERY_ENDCHAINWHY,
				StringCell.TYPE);
		addSpecIf(hasValues(conn, LIEFERUNGEN.CONTACT_QUESTIONS_REMARKS), columns, BackwardUtils.DELIVERY_REMARKS,
				StringCell.TYPE);
		addSpecIf(hasValues(conn, LIEFERUNGEN.KOMMENTAR), columns, BackwardUtils.DELIVERY_COMMENTS,
				StringCell.TYPE);
		addSpecIf(hasValues(conn, LIEFERUNGEN.FURTHER_TRACEBACK), columns, BackwardUtils.DELIVERY_FURTHERTB,
				StringCell.TYPE);
		addSpecIf(hasValues(conn, CHARGEN.MICROBIOSAMPLE), columns, BackwardUtils.DELIVERY_MICROSAMPLE,
				StringCell.TYPE);
		if (!isFormat2017) addSpecIf(hasValues(conn, LIEFERUNGEN.IMPORTSOURCES), columns, TracingColumns.FILESOURCES, StringCell.TYPE);

		addSpecIf(set.isEnsureBackwardCompatibility(), columns, BackwardUtils.DELIVERY_CHARGENUM, StringCell.TYPE);

		if (!set.isLotBased()) {
			for (Record2<String, String> r : DSL.using(conn, SQLDialect.HSQLDB)
					.selectDistinct(EXTRAFIELDS.TABLENAME, EXTRAFIELDS.ATTRIBUTE).from(EXTRAFIELDS)
					.where(EXTRAFIELDS.TABLENAME.equal(LIEFERUNGEN.getName()))
					.or(EXTRAFIELDS.TABLENAME.equal(CHARGEN.getName()))
					.or(EXTRAFIELDS.TABLENAME.equal(PRODUKTKATALOG.getName()))) {
				String origName = "_" + r.value1() + "." + r.value2();
				if (isFormat2017) {
					String betterName = origName;
					if (betterName.equals("_Chargen.BestBefore")) {betterName = "BestBefore"; extraFieldD.put("BestBefore", "_Chargen.BestBefore"); addSpec(columns, betterName, StringCell.TYPE);}
					else if (betterName.equals("_Lieferungen.Amount")) {betterName = "Amount"; extraFieldD.put("Amount", "_Lieferungen.Amount"); addSpec(columns, betterName, StringCell.TYPE);}
					else if (betterName.equals("_Produktkatalog.Source")) {betterName = "Product_Source"; extraFieldD.put("Product_Source", "_Produktkatalog.Source");}
					else if (betterName.equals("_Chargen.Source")) {betterName = "Lot_Source"; extraFieldD.put("Lot_Source", "_Chargen.Source");}
					else if (betterName.equals("_Lieferungen.Source")) {betterName = "Deliveries_Source"; extraFieldD.put("Deliveries_Source", "_Lieferungen.Source");}
					else addSpec(columns, betterName, StringCell.TYPE);
				}
				else {
					addSpec(columns, origName, StringCell.TYPE);
				}
			}
		} else {
			for (Record1<String> r : DSL.using(conn, SQLDialect.HSQLDB).selectDistinct(EXTRAFIELDS.ATTRIBUTE)
					.from(EXTRAFIELDS).where(EXTRAFIELDS.TABLENAME.equal(LIEFERUNGEN.getName()))) {
				addSpec(columns, "_" + r.value1(), StringCell.TYPE);
			}
		}

		return new DataTableSpec(columns.toArray(new DataColumnSpec[0]));
	}

	private BufferedDataTable getStationTable(Connection conn, Map<Integer, String> stationIds,
			Collection<Delivery> deliveries, ExecutionContext exec, boolean useSerialAsId, boolean isFormat2017)
			throws CanceledExecutionException {
		SetMultimap<String, String> deliversTo = LinkedHashMultimap.create();
		SetMultimap<String, String> receivesFrom = LinkedHashMultimap.create();

		for (Delivery d : deliveries) {
			deliversTo.put(d.getSupplierId(), d.getRecipientId());
			receivesFrom.put(d.getRecipientId(), d.getSupplierId());
		}

		DataTableSpec spec = getStationSpec(conn, useSerialAsId, isFormat2017);
		BufferedDataContainer container = exec.createDataContainer(spec);
		long index = 0;
		SelectJoinStep<Record> select = DSL.using(conn, SQLDialect.HSQLDB).select().from(STATION);

		if (set.isLotBased()) {
			select = select.join(PRODUKTKATALOG).on(STATION.ID.equal(PRODUKTKATALOG.STATION)).join(CHARGEN)
					.on(PRODUKTKATALOG.ID.equal(CHARGEN.ARTIKEL));
		}

		for (Record r : select) {
			String stationId = stationIds.get(r.getValue(STATION.ID));
			String state = clean(r.getValue(STATION.BUNDESLAND));
			String country = clean(r.getValue(STATION.LAND));
			String name = r.getValue(STATION.NAME) == null || set.isAnonymize()
					? getISO3166_2(country, state) + "#" + r.getValue(STATION.ID) : clean(r.getValue(STATION.NAME));

			DataCell[] cells = new DataCell[spec.getNumColumns()];

			fillCell(spec, cells, TracingColumns.ID,
					!set.isLotBased() ? createCell(stationId) : createCell(String.valueOf(r.getValue(CHARGEN.ID))));
			fillCell(spec, cells, TracingColumns.STATION_ID, createCell(stationId));
			fillCell(spec, cells, BackwardUtils.STATION_NODE, createCell(name));
			fillCell(spec, cells, TracingColumns.NAME,
					!set.isLotBased() ? createCell(name) : createCell(r.getValue(PRODUKTKATALOG.BEZEICHNUNG)));
			fillCell(spec, cells, TracingColumns.STATION_NAME, createCell(name));
			fillCell(spec, cells, TracingColumns.STATION_STREET,
					set.isAnonymize() ? DataType.getMissingCell() : createCell(r.getValue(STATION.STRASSE)));
			fillCell(spec, cells, TracingColumns.STATION_HOUSENO,
					set.isAnonymize() ? DataType.getMissingCell() : createCell(r.getValue(STATION.HAUSNUMMER)));
			fillCell(spec, cells, TracingColumns.STATION_ZIP, createCell(r.getValue(STATION.PLZ)));
			fillCell(spec, cells, TracingColumns.STATION_CITY,
					set.isAnonymize() ? DataType.getMissingCell() : createCell(r.getValue(STATION.ORT)));
			fillCell(spec, cells, TracingColumns.STATION_DISTRICT,
					set.isAnonymize() ? DataType.getMissingCell() : createCell(r.getValue(STATION.DISTRICT)));
			fillCell(spec, cells, TracingColumns.STATION_STATE,
					set.isAnonymize() ? DataType.getMissingCell() : createCell(state));
			fillCell(spec, cells, TracingColumns.ADDRESS,
					set.isAnonymize() ? DataType.getMissingCell() : createCell(r.getValue(STATION.ADRESSE)));
			fillCell(spec, cells, TracingColumns.STATION_COUNTRY,
					set.isAnonymize() ? DataType.getMissingCell() : createCell(country));
			fillCell(spec, cells, BackwardUtils.STATION_VAT,
					set.isAnonymize() ? DataType.getMissingCell() : createCell(r.getValue(STATION.VATNUMBER)));
			fillCell(spec, cells, TracingColumns.STATION_TOB, createCell(r.getValue(STATION.BETRIEBSART)));
			fillCell(spec, cells, BackwardUtils.STATION_NUMCASES, createCell(r.getValue(STATION.ANZAHLFAELLE)));
			fillCell(spec, cells, BackwardUtils.STATION_DATESTART, createCell(r.getValue(STATION.DATUMBEGINN)));
			fillCell(spec, cells, BackwardUtils.STATION_DATEPEAK, createCell(r.getValue(STATION.DATUMHOEHEPUNKT)));
			fillCell(spec, cells, BackwardUtils.STATION_DATEEND, createCell(r.getValue(STATION.DATUMENDE)));
			fillCell(spec, cells, BackwardUtils.STATION_SERIAL, createCell(r.getValue(STATION.SERIAL)));
			fillCell(spec, cells, TracingColumns.STATION_SIMPLESUPPLIER,
					!receivesFrom.containsKey(stationId) && deliversTo.get(stationId).size() == 1 ? BooleanCell.TRUE
							: BooleanCell.FALSE);
			fillCell(spec, cells, TracingColumns.STATION_DEADSTART,
					!receivesFrom.containsKey(stationId) ? BooleanCell.TRUE : BooleanCell.FALSE);
			fillCell(spec, cells, TracingColumns.STATION_DEADEND,
					!deliversTo.containsKey(stationId) ? BooleanCell.TRUE : BooleanCell.FALSE);
			fillCell(spec, cells, TracingColumns.FILESOURCES, createCell(r.getValue(STATION.IMPORTSOURCES)));
			fillCell(spec, cells, BackwardUtils.STATION_COUNTY,
					set.isAnonymize() ? DataType.getMissingCell() : createCell(r.getValue(STATION.DISTRICT)));

			if (set.isLotBased()) {
				fillCell(spec, cells, TracingColumns.LOT_NUMBER, createCell(r.getValue(CHARGEN.CHARGENNR)));
				fillCell(spec, cells, TracingColumns.PRODUCT_NUMBER,
						createCell(r.getValue(PRODUKTKATALOG.ARTIKELNUMMER)));
			}

			for (String column : spec.getColumnNames()) {
				if (column.startsWith("_") || extraFieldS.containsKey(column) && extraFieldS.get(column).startsWith("_")) {
					Result<Record1<String>> result;

					if (!set.isLotBased()) {
						String c = extraFieldS.containsKey(column) ? extraFieldS.get(column) : column;
						String attribute = c.substring(1);

						result = DSL.using(conn, SQLDialect.HSQLDB).select(EXTRAFIELDS.VALUE).from(EXTRAFIELDS)
								.where(EXTRAFIELDS.TABLENAME.equal(STATION.getName()),
										EXTRAFIELDS.ID.equal(r.getValue(STATION.ID)),
										EXTRAFIELDS.ATTRIBUTE.equal(attribute))
								.fetch();
					} else {
						String table = column.substring(1, column.indexOf("."));
						String attribute = column.substring(column.indexOf(".") + 1);

						result = DSL.using(conn, SQLDialect.HSQLDB).select(EXTRAFIELDS.VALUE).from(EXTRAFIELDS)
								.where(EXTRAFIELDS.TABLENAME.equal(table),
										EXTRAFIELDS.ID.equal(r.getValue(ID_COLUMNS.get(table))),
										EXTRAFIELDS.ATTRIBUTE.equal(attribute))
								.fetch();
					}

					fillCell(spec, cells, column,
							!result.isEmpty() ? createCell(result.get(0).value1()) : DataType.getMissingCell());
				}
			}

			container.addRowToTable(new DefaultRow(RowKey.createRowKey(index++), cells));
			exec.checkCanceled();
		}

		container.close();

		return container.getTable();
	}

	private BufferedDataTable getDeliveryTable(Connection conn, Map<Integer, String> stationIds,
			Map<Integer, String> deliveryIds, ExecutionContext exec, boolean useSerialAsId, boolean isFormat2017)
			throws CanceledExecutionException {
		DataTableSpec spec = getDeliverySpec(conn, useSerialAsId, isFormat2017);
		BufferedDataContainer container = exec.createDataContainer(spec);
		int index = 0;
		Set<String> ids = new LinkedHashSet<>();
		SelectJoinStep<Record> select = DSL.using(conn, SQLDialect.HSQLDB).select().from(LIEFERUNGEN)
				.leftOuterJoin(CHARGEN).on(LIEFERUNGEN.CHARGE.equal(CHARGEN.ID)).leftOuterJoin(PRODUKTKATALOG)
				.on(CHARGEN.ARTIKEL.equal(PRODUKTKATALOG.ID));

		if (set.isLotBased()) {
			select = select.join(CHARGENVERBINDUNGEN).on(LIEFERUNGEN.ID.equal(CHARGENVERBINDUNGEN.ZUTAT));
		}

		for (Record r : select.orderBy(PRODUKTKATALOG.ID)) {
			String deliveryId = deliveryIds.get(r.getValue(LIEFERUNGEN.ID));
			String lotId = r.getValue(CHARGEN.ID) + "";
			String fromId = !set.isLotBased() ? stationIds.get(r.getValue(PRODUKTKATALOG.STATION))
					: String.valueOf(r.getValue(CHARGEN.ID));
			String toId = !set.isLotBased() ? stationIds.get(r.getValue(LIEFERUNGEN.EMPFÄNGER))
					: String.valueOf(r.getValue(CHARGENVERBINDUNGEN.PRODUKT));
			String id = !set.isLotBased() ? deliveryId : deliveryId + "-" + toId;

			if (!ids.add(id)) {
				continue;
			}

			DataCell[] cells = new DataCell[spec.getNumColumns()];

			fillCell(spec, cells, TracingColumns.ID, createCell(id));
			fillCell(spec, cells, TracingColumns.FROM, createCell(fromId));
			fillCell(spec, cells, TracingColumns.TO, createCell(toId));
			fillCell(spec, cells, TracingColumns.LOT_ID, createCell(lotId));
			fillCell(spec, cells, TracingColumns.DELIVERY_ID, createCell(deliveryId));
			fillCell(spec, cells, TracingColumns.NAME, createCell(r.getValue(PRODUKTKATALOG.BEZEICHNUNG)));
			fillCell(spec, cells, TracingColumns.PRODUCT_NUMBER, set.isAnonymize() ? DataType.getMissingCell()
					: createCell(r.getValue(PRODUKTKATALOG.ARTIKELNUMMER)));
			fillCell(spec, cells, TracingColumns.DELIVERY_DEPARTURE,
					createCell(DeliveryUtils.formatDate(r.getValue(LIEFERUNGEN.DD_DAY),
							r.getValue(LIEFERUNGEN.DD_MONTH), r.getValue(LIEFERUNGEN.DD_YEAR))));
			fillCell(spec, cells, TracingColumns.DELIVERY_ARRIVAL,
					createCell(DeliveryUtils.formatDate(r.getValue(LIEFERUNGEN.AD_DAY),
							r.getValue(LIEFERUNGEN.AD_MONTH), r.getValue(LIEFERUNGEN.AD_YEAR))));
			fillCell(spec, cells, BackwardUtils.DELIVERY_SERIAL, createCell(r.getValue(LIEFERUNGEN.SERIAL)));
			fillCell(spec, cells, BackwardUtils.DELIVERY_PROCESSING,
					createCell(r.getValue(PRODUKTKATALOG.PROZESSIERUNG)));
			fillCell(spec, cells, BackwardUtils.DELIVERY_USAGE, createCell(r.getValue(PRODUKTKATALOG.INTENDEDUSE)));
			fillCell(spec, cells, TracingColumns.LOT_NUMBER,
					set.isAnonymize() ? DataType.getMissingCell() : createCell(r.getValue(CHARGEN.CHARGENNR)));
			fillCell(spec, cells, BackwardUtils.DELIVERY_DATEEXP,
					createCell(DeliveryUtils.formatDate(r.getValue(CHARGEN.MHD_DAY), r.getValue(CHARGEN.MHD_MONTH),
							r.getValue(CHARGEN.MHD_YEAR))));
			fillCell(spec, cells, BackwardUtils.DELIVERY_DATEMANU,
					createCell(DeliveryUtils.formatDate(r.getValue(CHARGEN.PD_DAY), r.getValue(CHARGEN.PD_MONTH),
							r.getValue(CHARGEN.PD_YEAR))));
			fillCell(spec, cells, TracingColumns.DELIVERY_AMOUNT, createCell(DeliveryUtils
					.getAmountInKg(r.getValue(LIEFERUNGEN.UNITMENGE), r.getValue(LIEFERUNGEN.UNITEINHEIT))));
			fillCell(spec, cells, TracingColumns.DELIVERY_NUM_PU, createCell(r.getValue(LIEFERUNGEN.NUMPU)));
			fillCell(spec, cells, TracingColumns.DELIVERY_TYPE_PU, createCell(r.getValue(LIEFERUNGEN.TYPEPU)));
			fillCell(spec, cells, BackwardUtils.DELIVERY_ENDCHAIN, createCell(r.getValue(LIEFERUNGEN.ENDCHAIN)));
			fillCell(spec, cells, BackwardUtils.DELIVERY_ORIGIN, createCell(r.getValue(CHARGEN.ORIGINCOUNTRY)));
			fillCell(spec, cells, BackwardUtils.DELIVERY_ENDCHAINWHY,
					createCell(r.getValue(LIEFERUNGEN.EXPLANATION_ENDCHAIN)));
			fillCell(spec, cells, BackwardUtils.DELIVERY_REMARKS,
					createCell(r.getValue(LIEFERUNGEN.CONTACT_QUESTIONS_REMARKS)));
			fillCell(spec, cells, BackwardUtils.DELIVERY_COMMENTS,
					createCell(r.getValue(LIEFERUNGEN.KOMMENTAR)));
			fillCell(spec, cells, BackwardUtils.DELIVERY_FURTHERTB,
					createCell(r.getValue(LIEFERUNGEN.FURTHER_TRACEBACK)));
			fillCell(spec, cells, BackwardUtils.DELIVERY_MICROSAMPLE, createCell(r.getValue(CHARGEN.MICROBIOSAMPLE)));
			fillCell(spec, cells, TracingColumns.FILESOURCES, createCell(r.getValue(LIEFERUNGEN.IMPORTSOURCES)));
			fillCell(spec, cells, BackwardUtils.DELIVERY_CHARGENUM,
					set.isAnonymize() ? DataType.getMissingCell() : createCell(r.getValue(CHARGEN.CHARGENNR)));

			for (String column : spec.getColumnNames()) {
				if (column.startsWith("_") || extraFieldD.containsKey(column) && extraFieldD.get(column).startsWith("_")) {
					Result<Record1<String>> result;

					if (!set.isLotBased()) {
						String c = extraFieldD.containsKey(column) ? extraFieldD.get(column) : column;
						String table = c.substring(1, c.indexOf("."));
						String attribute = c.substring(c.indexOf(".") + 1);

						result = DSL.using(conn, SQLDialect.HSQLDB).select(EXTRAFIELDS.VALUE).from(EXTRAFIELDS)
								.where(EXTRAFIELDS.TABLENAME.equal(table),
										EXTRAFIELDS.ID.equal(r.getValue(ID_COLUMNS.get(table))),
										EXTRAFIELDS.ATTRIBUTE.equal(attribute))
								.fetch();

					} else {
						String attribute = column.substring(1);

						result = DSL.using(conn, SQLDialect.HSQLDB).select(EXTRAFIELDS.VALUE).from(EXTRAFIELDS)
								.where(EXTRAFIELDS.TABLENAME.equal(LIEFERUNGEN.getName()),
										EXTRAFIELDS.ID.equal(r.getValue(LIEFERUNGEN.ID)),
										EXTRAFIELDS.ATTRIBUTE.equal(attribute))
								.fetch();
					}

					fillCell(spec, cells, column,
							!result.isEmpty() ? createCell(result.get(0).value1()) : DataType.getMissingCell());
				}
			}

			DataRow outputRow = new DefaultRow("Row" + index++, cells);

			container.addRowToTable(outputRow);
			exec.checkCanceled();
		}

		container.close();

		return container.getTable();
	}

	private BufferedDataTable getDeliveryConnectionsTable(List<Delivery> deliveries, BufferedDataTable deliveryTable,
			ExecutionContext exec) throws CanceledExecutionException {
		BufferedDataContainer container = exec.createDataContainer(
				new DataTableSpec(new DataColumnSpecCreator(TracingColumns.FROM, StringCell.TYPE).createSpec(),
						new DataColumnSpecCreator(TracingColumns.TO, StringCell.TYPE).createSpec()));
		int index = 0;

		if (!set.isLotBased()) {
			for (Delivery delivery : deliveries) {
				for (String next : delivery.getAllNextIds()) {
					container.addRowToTable(
							new DefaultRow(index++ + "", createCell(delivery.getId()), createCell(next)));
					exec.checkCanceled();
				}
			}
		} else {
			ListMultimap<String, String> incoming = ArrayListMultimap.create();
			ListMultimap<String, String> outgoing = ArrayListMultimap.create();

			for (DataRow row : deliveryTable) {
				incoming.put(IO.getString(row.getCell(deliveryTable.getSpec().findColumnIndex(TracingColumns.TO))),
						IO.getString(row.getCell(deliveryTable.getSpec().findColumnIndex(TracingColumns.ID))));
				outgoing.put(IO.getString(row.getCell(deliveryTable.getSpec().findColumnIndex(TracingColumns.FROM))),
						IO.getString(row.getCell(deliveryTable.getSpec().findColumnIndex(TracingColumns.ID))));
			}

			for (String lot : Sets.intersection(incoming.keySet(), outgoing.keySet())) {
				for (String in : incoming.get(lot)) {
					for (String out : outgoing.get(lot)) {
						container.addRowToTable(new DefaultRow(index++ + "", createCell(in), createCell(out)));
						exec.checkCanceled();
					}
				}
			}
		}

		container.close();

		return container.getTable();
	}

	private static void fillCell(DataTableSpec spec, DataCell[] cells, String columnname, DataCell value) {
		int index = spec.findColumnIndex(columnname);

		if (index >= 0) {
			cells[index] = value;
		}
	}

	private static void addSpec(Collection<DataColumnSpec> specs, String name, DataType type) {
		specs.add(new DataColumnSpecCreator(name, type).createSpec());
	}

	private static void addSpecIf(boolean condition, Collection<DataColumnSpec> specs, String name, DataType type) {
		if (condition) {
			addSpec(specs, name, type);
		}
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

	private static boolean hasValues(Connection conn, TableField<?, ?>... fields) {
		for (TableField<?, ?> field : fields) {
			for (Record1<?> r : DSL.using(conn, SQLDialect.HSQLDB).selectDistinct(field).from(field.getTable())) {
				if (r.value1() != null) {
					return true;
				}
			}
		}

		return false;
	}

	@SuppressFBWarnings(value = "Dm")
	private static Connection createLocalConnection(String dbFolder) throws SQLException {
		MyDBI db = new MyDBTablesNew();

		db.establishNewConnection("SA", "", dbFolder + File.separator, false);
		db.setDbUsername("SA");
		db.updateCheck("");

		Connection result = db.getConn();

		result.setReadOnly(true);

		return result;
	}
}
