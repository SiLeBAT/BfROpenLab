package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.STATION;
import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.EXTRAFIELDS;
import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.PRODUKTKATALOG;
import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.CHARGEN;
import static de.bund.bfr.knime.openkrise.db.generated.public_.Tables.LIEFERUNGEN;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;

public class KeyIdMapsReader {
	
	private static String getMhdDate(Record record) {
		if (record == null) return null;
		Integer year = record.getValue(CHARGEN.MHD_YEAR);
		Integer month = record.getValue(CHARGEN.MHD_MONTH);
		Integer day = record.getValue(CHARGEN.MHD_DAY);
		if (year == null || month == null || day == null) return null;
		return "" + year + "-" + month + "-" + day;
	}
	
	private static void readStationKeyIdMap(Connection connection, KeyIdMap stationKeyIdMap) {
		SelectJoinStep<Record> select = DSL.using(connection, SQLDialect.HSQLDB).select().from(STATION);
		
		for (Record r : select) {
			Integer stationId = r.getValue(STATION.ID);
			String name = r.getValue(STATION.NAME);
			String addresse = r.getValue(STATION.ADRESSE);
			String key = KeyGenerator.createStationKey(name, addresse);
			stationKeyIdMap.add(key, stationId);
		}
	}
	
	private static void readProductKeyIdMap(Connection connection, KeyIdMaps keyIdMaps) {
		KeyIdMap stationKeyIdMap = keyIdMaps.getStationKeyIdMap();
		KeyIdMap productKeyIdMap = keyIdMaps.getProductKeyIdMap();
		
		Map<Integer, String> id2EanMap = loadIdToExtraFieldMap(connection, PRODUKTKATALOG.getName(), Product.EXTRA_FIELD_EAN);
		
		SelectJoinStep<Record> select = DSL.using(connection, SQLDialect.HSQLDB).select().from(PRODUKTKATALOG);
		
		for (Record r : select) {
			Integer productId = r.getValue(PRODUKTKATALOG.ID);
			Integer stationId = r.getValue(PRODUKTKATALOG.STATION);
			String name = r.getValue(PRODUKTKATALOG.BEZEICHNUNG);
			String ean = id2EanMap.get(productId);

			String key = KeyGenerator.createProductKey(stationKeyIdMap.getKey(stationId), name,	ean);
			
			productKeyIdMap.add(key, productId);
		}
	}
	
	private static void readMSTLotAndDeliveryKeyIdMap(Connection connection, KeyIdMaps keyIdMaps) {
		KeyIdMap stationKeyIdMap = keyIdMaps.getStationKeyIdMap();
		KeyIdMap lotKeyIdMap = keyIdMaps.getLotKeyIdMap();
		KeyIdMap deliveryKeyIdMap = keyIdMaps.getDeliveryKeyIdMap();
		KeyIdMap productKeyIdMap = keyIdMaps.getProductKeyIdMap();
		Map<Integer, String> id2AmountMap = loadIdToExtraFieldMap(connection, LIEFERUNGEN.getName(), Delivery.EXTRA_FIELD_AMOUNT);
		
		SelectJoinStep<Record> select = DSL.using(connection, SQLDialect.HSQLDB)
				.select().from(CHARGEN)
				.leftOuterJoin(LIEFERUNGEN).on(CHARGEN.ID.equal(LIEFERUNGEN.CHARGE));
		
		for (Record r : select) {
			Integer lotId = r.getValue(CHARGEN.ID);
			Integer productId = r.getValue(CHARGEN.ARTIKEL);
			Integer deliveryId = r.getValue(LIEFERUNGEN.ID);
			String lotNumber = r.getValue(CHARGEN.CHARGENNR);
			String mhd = getMhdDate(r);
			
			String lotKey;
			if (lotNumber != null || mhd != null || deliveryId == null) {
				lotKey = lotKeyIdMap.getKey(lotId);
				if (lotKey == null) {
					lotKey = KeyGenerator.createMSTLotKey(keyIdMaps.getProductKeyIdMap().getKey(productId), lotNumber, mhd);
					lotKeyIdMap.add(lotKey, lotId);
				}
			}
			else {
				lotKey = KeyGenerator.createMSTDeliveryLotKey(
					productKeyIdMap.getKey(productId), 
					r.getValue(LIEFERUNGEN.DD_YEAR),
					r.getValue(LIEFERUNGEN.DD_MONTH),
					r.getValue(LIEFERUNGEN.DD_DAY),
					r.getValue(LIEFERUNGEN.SERIAL)
				);
				lotKeyIdMap.add(lotKey, lotId);
			}		
			
			if (deliveryId != null) {				
				String deliveryKey = KeyGenerator.createMSTDeliveryKey(
					lotKey, 
					r.getValue(LIEFERUNGEN.DD_YEAR), 
					r.getValue(LIEFERUNGEN.DD_MONTH),
					r.getValue(LIEFERUNGEN.DD_DAY),
					id2AmountMap.get(deliveryId), 
					r.getValue(LIEFERUNGEN.KOMMENTAR), 
					stationKeyIdMap.getKey(r.getValue(LIEFERUNGEN.EMPFÄNGER))
				);
				deliveryKeyIdMap.add(deliveryKey, deliveryId);
			}
		}
	}
	
	private static void readSSTLotAndDeliveryKeyIdMap(Connection connection, KeyIdMaps keyIdMaps) {
		KeyIdMap stationKeyIdMap = keyIdMaps.getStationKeyIdMap();
		KeyIdMap lotKeyIdMap = keyIdMaps.getLotKeyIdMap();
		KeyIdMap deliveryKeyIdMap = keyIdMaps.getDeliveryKeyIdMap();
		KeyIdMap productKeyIdMap = keyIdMaps.getProductKeyIdMap();
		
		Map<Integer, String> id2AmountMap = loadIdToExtraFieldMap(connection, LIEFERUNGEN.getName(), Delivery.EXTRA_FIELD_AMOUNT);
		
		SelectJoinStep<Record> select = DSL.using(connection, SQLDialect.HSQLDB)
				.select().from(CHARGEN)
				.leftOuterJoin(LIEFERUNGEN).on(CHARGEN.ID.equal(LIEFERUNGEN.CHARGE));
		
		for (Record r : select) {
			Integer lotId = r.getValue(CHARGEN.ID);
			Integer productId = r.getValue(CHARGEN.ARTIKEL);
			Integer deliveryId = r.getValue(LIEFERUNGEN.ID);
			String lotNumber = r.getValue(CHARGEN.CHARGENNR);
			String mhd = getMhdDate(r);
					
			String lotKey;
			String amount = null;
			String receiverKey = null;
			if (deliveryId != null) {
				amount = id2AmountMap.get(deliveryId); // ToDo: getDeliveryAmount(connection, deliveryId);
				receiverKey = stationKeyIdMap.getKey(r.getValue(LIEFERUNGEN.EMPFÄNGER));
			}
			
			if (lotNumber != null || mhd != null || deliveryId == null) {
				lotKey = lotKeyIdMap.getKey(lotId);
				if (lotKey == null) {
					lotKey = KeyGenerator.createSSTLotKey(keyIdMaps.getProductKeyIdMap().getKey(productId), lotNumber, mhd);
					lotKeyIdMap.add(lotKey, lotId);
				}
			} else {
				lotKey = KeyGenerator.createSSTDeliveryLotKey(
					productKeyIdMap.getKey(productId), 
					r.getValue(LIEFERUNGEN.AD_YEAR),
					r.getValue(LIEFERUNGEN.AD_MONTH),
					r.getValue(LIEFERUNGEN.AD_DAY),
					amount,
					receiverKey
				);
				lotKeyIdMap.add(lotKey, lotId);
			}
			
			
			if (deliveryId != null) {
				String deliveryKey = KeyGenerator.createSSTDeliveryKey(
					lotKey, 
					r.getValue(LIEFERUNGEN.AD_YEAR), 
					r.getValue(LIEFERUNGEN.AD_MONTH),
					r.getValue(LIEFERUNGEN.AD_DAY),
					amount, 
					r.getValue(LIEFERUNGEN.KOMMENTAR), 
					receiverKey
				);
				deliveryKeyIdMap.add(deliveryKey, deliveryId);
			}
		}
	}
	
	private static Map<Integer, String> loadIdToExtraFieldMap(Connection connection, String tableName, String attributeName) {
		Map<Integer, String> id2ValueMap = new HashMap<>();
		
		Result<Record> select = DSL.using(connection, SQLDialect.HSQLDB).select().from(EXTRAFIELDS)
				.where(
					EXTRAFIELDS.TABLENAME.equal(tableName),
					EXTRAFIELDS.ATTRIBUTE.equal(attributeName)
				).fetch();
		
		if (select != null) for (Record r : select) id2ValueMap.put(r.getValue(EXTRAFIELDS.ID), r.getValue(EXTRAFIELDS.VALUE));
		
		return id2ValueMap;
	}
	
	static KeyIdMaps readKeyIdMaps(Connection connection, boolean forMulitSheetTemplate) {
		// System.out.println(java.time.LocalTime.now().toString() + "  Reading of KeyIDMapping from DB started ...");
		KeyIdMaps keyIdMaps = new KeyIdMaps();
		readStationKeyIdMap(connection, keyIdMaps.getStationKeyIdMap());
		readProductKeyIdMap(connection, keyIdMaps);
		if (forMulitSheetTemplate) 	
			readMSTLotAndDeliveryKeyIdMap(connection, keyIdMaps);
		else
			readSSTLotAndDeliveryKeyIdMap(connection, keyIdMaps);
		
		// System.out.println(java.time.LocalTime.now().toString() + "  Reading of KeyIDMapping from DB finished.");
		return keyIdMaps;
	}
}
