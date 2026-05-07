package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class KeyGenerator {
	
	private Map<Object, String> keyCacheMap = new HashMap<>();
	
	private boolean useMSTVariant = true;
	
	KeyGenerator(boolean forMultiSheetTemplateImport) {
		this.useMSTVariant = forMultiSheetTemplateImport;
	}
	
	void resetCache() {
		this.keyCacheMap = new HashMap<>();
	}
	
	private static String concat(String a, String b) {
		return (a == null ? "" : a) + (b == null ? "" : b);
	}
	
	private static String createKey(String... args) {
		if (args == null) return null;
		args = Arrays.asList(args).stream().map(x -> x == null ? "" : x.replaceAll(";", "\\\\;")).toArray(String[]::new);
		return String.join(";;", args);
	}

	static String createStationKey(String name, String address) {
		return "S:" + createKey(new String[] {name, address});
	}	
	
	String createStationKey(Station station) {
		String key = keyCacheMap.get(station);
		if (key != null) return key;
		key = createStationKey(station.getName(), station.getAddress());
		keyCacheMap.put(station, key);
		return key;
	}	
	
	static String createProductKey(String supplierStationKey, String name, String ean) {
		return "P:" + createKey(new String[] {"S:" + supplierStationKey, name, ean});
	}
	
	String createProductKey(Product product) {
		String key = keyCacheMap.get(product);
		if (key != null) return key;
		key = createProductKey(createStationKey(product.getStation()), product.getName(), product.getFlexible(XlsProduct.EAN("en")));
		keyCacheMap.put(product, key);
		return key;
	}
	
	// generate lotkey for multi sheet templates
	static String createMSTLotKey(String productKey, String lotNumber, String mhd) {
		return "L:" + createKey(new String[] {productKey, concat("LN:", lotNumber), concat("MHD:",mhd)});
	}
	
	// generate lotkey for single sheet templates
	static String createSSTLotKey(String productKey, String lotNumber, String mhd) {
		if (lotNumber != null) return "L:" + createKey(new String[] {productKey, concat("LN:", lotNumber)});
		if (mhd != null) return "L:" + createKey(new String[] {productKey, concat("Mhd:", mhd)});
		return "L:" + createKey(new String[] {productKey});
	}
	
	static String createMSTDeliveryLotKey(String productKey, Integer deliveryYear, Integer deliveryMonth, Integer deliveryDay, String userId) {
		if (deliveryYear == null || deliveryMonth == null || deliveryDay == null) 
			return "L:" + createKey(productKey, concat("UID:", userId));
		return "L:" + createKey(productKey, concat("DD:", dateToString(deliveryYear, deliveryMonth, deliveryDay)));
	}
	
	static String createSSTDeliveryLotKey(String productKey, Integer arrivalYear, Integer arrivalMonth, Integer arrivalDay, String deliveryAmount, String receiverKey ) {
		if (arrivalYear == null && arrivalMonth == null && arrivalDay == null && deliveryAmount == null) 
			return "L:" + createKey(productKey, "R:" + receiverKey);
		else 
			return "L:" + createKey(productKey, concat("AD:", dateToString(arrivalYear, arrivalMonth, arrivalDay)), concat("DA:", deliveryAmount));
	}
	
	String createMSTDeliveryLotKey(Delivery delivery) {
		Lot lot = delivery.getLot();
		
		String lotKey = keyCacheMap.get(lot);
		if (lotKey != null) return lotKey;
		
		String mhd = lot.getFlexible(XlsLot.MHD("en"));
		String productKey = createProductKey(lot.getProduct());
		
		if (lot.getNumber() != null || mhd != null) 
			lotKey = createMSTLotKey(productKey, lot.getNumber(), mhd);
		else
			lotKey = createMSTDeliveryLotKey(productKey, delivery.getDepartureYear(), delivery.getDepartureMonth(), delivery.getDepartureDay(), delivery.getId());
		
		keyCacheMap.put(lot, lotKey);
		return lotKey;
	}
	
	String createSSTDeliveryLotKey(Delivery delivery) {
		Lot lot = delivery.getLot();
		
		String lotKey = keyCacheMap.get(lot);
		if (lotKey != null) return lotKey;
		
		String mhd = lot.getFlexible(XlsLot.MHD("en"));
		String productKey = createProductKey(lot.getProduct());
		
		if (lot.getNumber() != null || mhd != null) 
			lotKey = createSSTLotKey(productKey, lot.getNumber(), mhd);
		else 
			lotKey = createSSTDeliveryLotKey(
				productKey, 
				delivery.getArrivalYear(), delivery.getArrivalMonth(), delivery.getArrivalDay(), 
				delivery.getFlexible("Amount"), 
				createStationKey(delivery.getReceiver())
			);
		keyCacheMap.put(lot, lotKey);
		return lotKey;
	}
	
	
	private static String dateToString(Integer year, Integer month, Integer day) {
		if (year == null && month == null && day == null) return null;
		return (year == null ? "?" : year) + "-" + (month == null ? "?" : month) + "-" + (day == null ? "?" : day);
	}
	
	private static String createDeliveryKey(String lotKey, String date, String amount, String comment, String receiverStationKey) {
		return "D:" + createKey(lotKey, date, concat("A:", amount), concat("C:", comment), "R:" + receiverStationKey);
	}
	
	static String createMSTDeliveryKey(String lotKey, Integer deliveryYear, Integer deliveryMonth, Integer deliveryDay, String amount, String comment, String receiverKey) {
		return createDeliveryKey(lotKey, concat("DD:", dateToString(deliveryYear, deliveryMonth, deliveryDay)), amount, comment, receiverKey);
	}
	
	static String createSSTDeliveryKey(String lotKey, Integer arrivalYear, Integer arrivalMonth, Integer arrivalDay, String amount, String comment, String receiverKey) {
		return createDeliveryKey(lotKey, concat("AD:", dateToString(arrivalYear, arrivalMonth, arrivalDay)), amount, comment, receiverKey);
	}
	
	String createDeliveryKey(Delivery delivery) {
		String key = keyCacheMap.get(delivery);
		if (key != null) return key;
		
		if (useMSTVariant)
			key = createMSTDeliveryKey(
				createMSTDeliveryLotKey(delivery), 
				delivery.getDepartureYear(), 
				delivery.getDepartureMonth(), 
				delivery.getDepartureDay(), 
				delivery.getFlexible("Amount"),
				delivery.getComment(),
				createStationKey(delivery.getReceiver())
			);
		else
			key = createSSTDeliveryKey(
				createMSTDeliveryLotKey(delivery), 
				delivery.getArrivalYear(), 
				delivery.getArrivalMonth(), 
				delivery.getArrivalDay(), 
				delivery.getFlexible("Amount"),
				delivery.getComment(),
				createStationKey(delivery.getReceiver())
			);
		
		keyCacheMap.put(delivery, key);
		return key;
	}
}
