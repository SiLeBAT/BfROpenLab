package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class IdGenerator {
	private KeyIdMaps keyIdMaps;
	private KeyGenerator keyGenerator;
	private Map<Station, Integer> station2IdCache = new HashMap<>();
	private Map<Product, Integer> product2IdCache = new HashMap<>();
	private Map<Lot, Integer> lot2IdCache = new HashMap<>();
	
	private boolean useMSTVariant = true;
	
	IdGenerator(Connection connection, boolean forMultiSheetTemplateImport) {
		this.useMSTVariant = forMultiSheetTemplateImport;
		keyIdMaps = KeyIdMapsReader.readKeyIdMaps(connection, forMultiSheetTemplateImport);
		keyGenerator = new KeyGenerator(forMultiSheetTemplateImport);
	}
	
	KeyGenerator getKeyGenerator() {
		return keyGenerator;
	}
	
	boolean isForMultiSheetTemplateImport() {
		return useMSTVariant;
	}
	
	void resetCache() {
		station2IdCache = new HashMap<>();
		product2IdCache = new HashMap<>();
		lot2IdCache = new HashMap<>();
		keyGenerator.resetCache();
	}
	
	Integer getSafeStationId(Station station) {
		Integer id = station2IdCache.get(station);
		if (id != null) return id;
		
		id = getSafeId(keyGenerator.createStationKey(station), createStationHashBase(station), keyIdMaps.getStationKeyIdMap());
		
		station2IdCache.put(station, id);
		return id;
	}
	
	Integer getSafeProductId(Product product) {
		Integer id = product2IdCache.get(product);
		if (id != null) return id;
		
		id = getSafeId(keyGenerator.createProductKey(product), createProductHashBase(product), keyIdMaps.getProductKeyIdMap());
		
		product2IdCache.put(product,  id);
		return id;
	}
	
	Integer getSafeDeliveryLotId(Delivery delivery) {
		Integer id = lot2IdCache.get(delivery.getLot());
		if (id != null) return id;
		
		id = useMSTVariant ? getSafeMSTDeliveryLotId(delivery) : getSafeSSTDeliveryLotId(delivery);
		
		lot2IdCache.put(delivery.getLot(),  id);
		return id;
	}
	
	Integer getSafeDeliveryId(Delivery delivery) {
		return useMSTVariant ? getSafeMSTDeliveryId(delivery) : getSafeSSTDeliveryId(delivery);
	}
	
	private Integer getSafeId(String key, String hashBase, KeyIdMap keyIdMap) {
		int id = hashBase.hashCode();
		// System.out.println("GetSafeId for hashbase: " + hashBase);
		
		// int tmp = id;
		if (keyIdMap.hasKey(key)) {
			id = keyIdMap.getId(key, id);
			// if (id != tmp) System.out.println("Key is not mapped to the preferred id (Key:" + key + ", expId: " + tmp + ", obsId: " + id + ")");
			// return id;
		} else {
			int i = 1;
			while (keyIdMap.hasId(id)) id = (hashBase + ++i).hashCode();
			// if (id != tmp) System.out.println("Key is not mapped to the preferred id (Key:" + key + ", expId: " + tmp + ", obsId: " + id + ", i: " + i + ")");
			keyIdMap.add(key, id);
			// return id;
		}
		// System.out.println("GetSafeId for hashbase: " + hashBase + "(id: " + id +")");
		return id;
	}
	
	private Integer getSafeMSTDeliveryLotId(Delivery delivery) {
		return getSafeId(keyGenerator.createMSTDeliveryLotKey(delivery), createMSTDeliveryLotHashBase(delivery), keyIdMaps.getLotKeyIdMap());
	}
	
	private Integer getSafeSSTDeliveryLotId(Delivery delivery) {
		return getSafeId(keyGenerator.createSSTDeliveryLotKey(delivery), createSSTDeliveryLotHashBase(delivery), keyIdMaps.getLotKeyIdMap());
	}
	
	private Integer getSafeMSTDeliveryId(Delivery delivery) {
		return getSafeId(keyGenerator.createDeliveryKey(delivery), createMSTDeliveryHashBase(delivery), keyIdMaps.getDeliveryKeyIdMap());
	}
	
	private Integer getSafeSSTDeliveryId(Delivery delivery) {
		return getSafeId(keyGenerator.createDeliveryKey(delivery), createSSTDeliveryHashBase(delivery), keyIdMaps.getDeliveryKeyIdMap());
	}
	
	private String createStationHashBase(Station station) {
		return "" + station.getName() + station.getAddress();
	}
	
	private String createProductHashBase(Product product) {
		return "" + getSafeStationId(product.getStation()) + product.getName() + product.getFlexible(XlsProduct.EAN("en"));
	}
	
	private String createMSTDeliveryLotHashBase(Delivery delivery) {
		Lot lot = delivery.getLot();
		String mhd = lot.getFlexible(XlsLot.MHD("en"));
		
		String productId = "" + lot.getProduct().getDbId();
		// String productId = "" + getSafeProductId(lot.getProduct());
		if (lot.getNumber() != null || mhd != null) 
			return productId + lot.getNumber() + mhd;
		else if (delivery.getDepartureDay() == null || delivery.getDepartureMonth() == null || delivery.getDepartureYear() == null) 
			return productId + delivery.getId();
		else
			return productId + delivery.getDepartureDay()+delivery.getDepartureMonth()+delivery.getDepartureYear();
	}
	
	private String createSSTDeliveryLotHashBase(Delivery delivery) {
		Lot lot = delivery.getLot();
		// String productId = "" + getSafeProductId(lot.getProduct());
		String productId = "" + lot.getProduct().getId();
		if (lot.getNumber() != null) 
			return productId + lot.getNumber();
		
		String mhd = lot.getFlexible(XlsLot.MHD("en"));
		if (mhd != null) 
			return productId + mhd;
		
		String amount = delivery.getFlexible("Amount");
		if (delivery.getArrivalDay() == null && delivery.getArrivalDay() == null && delivery.getArrivalDay() == null && amount == null)
			return productId + "[receiver: " + delivery.getReceiver().getName() + "]";
		else  
			return productId + "[delivery " + 
				(delivery.getArrivalYear() == null ? "" : delivery.getArrivalYear()) + 
				(delivery.getArrivalMonth() == null ? "" : delivery.getArrivalMonth()) + 
				(delivery.getArrivalDay() == null ? "" : delivery.getArrivalDay()) + 
				(amount == null ? "" : "_" + amount) + "]";
	}
	
	private String createMSTDeliveryHashBase(Delivery delivery) {
		return "" + delivery.getLot().getDbId() + 
				delivery.getDepartureDay() + delivery.getDepartureMonth() + delivery.getDepartureYear() +
				delivery.getFlexible("Amount") + delivery.getComment() + delivery.getReceiver().getDbId();
	}
	
	private String createSSTDeliveryHashBase(Delivery delivery) {
		return "" + delivery.getLot().getId() + 
			delivery.getArrivalDay() + delivery.getArrivalMonth() + delivery.getArrivalYear() +
			delivery.getFlexible("Amount") + delivery.getComment() + delivery.getReceiver().getId();
	}
}
