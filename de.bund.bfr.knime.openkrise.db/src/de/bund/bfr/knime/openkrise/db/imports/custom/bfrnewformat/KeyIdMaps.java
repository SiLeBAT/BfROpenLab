package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

public class KeyIdMaps {
	private KeyIdMap stationKeyIdMap_ = new KeyIdMap();
	private KeyIdMap productKeyIdMap_ = new KeyIdMap();
	private KeyIdMap lotKeyIdMap_ = new KeyIdMap();
	private KeyIdMap deliveryKeyIdMap_ = new KeyIdMap();
	
	KeyIdMap getStationKeyIdMap() {
		return stationKeyIdMap_;
	}
	
	KeyIdMap getProductKeyIdMap() {
		return productKeyIdMap_;
	}
	
	KeyIdMap getLotKeyIdMap() {
		return lotKeyIdMap_;
	}
	
	KeyIdMap getDeliveryKeyIdMap() {
		return deliveryKeyIdMap_;
	}
}
