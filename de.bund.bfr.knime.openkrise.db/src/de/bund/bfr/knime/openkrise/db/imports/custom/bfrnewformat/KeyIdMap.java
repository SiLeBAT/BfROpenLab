package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class KeyIdMap {
	private Map<String, Integer> key2IdMap = new HashMap<>();          // for one id per key
	private Map<Integer, String> id2KeyMap = new HashMap<>();          // for some key per id
	private Map<String, LinkedHashSet<Integer>> key2IdsMap = new HashMap<>();    // for multiple ids per key 
	
	// a key can be mapped to several ids, but in most cases there should only be one id
	// an id should be mapped to one key, but in the lot cases which have no lot specific info
	// the lot keys are derived from the deliveries and an lot id can be assigned to many keys
	
	void add(String key, Integer id) {
		if (key == null || id == null) return;
		
		// if for this key only one id exists the key id relation ship is stored in map key2IdMap
		// if several ids exist for the key the key id relationships are stored in map key2IdsMap (and the key in key2IdMap is mapped to null (as a flag))
		if (key2IdMap.containsKey(key)) {
			LinkedHashSet<Integer> ids = key2IdsMap.get(key);
			if (ids == null) {
				// only one id was mapped to this key so far
				Integer mappedId = key2IdMap.get(key);
				if (id.equals(mappedId)) return;
				// System.out.println("Key id mapping is not unique for key " + key);
				ids = new LinkedHashSet<>(Arrays.asList(mappedId));
				key2IdsMap.put(key, ids);
				key2IdMap.put(key, null);
			}
			ids.add(id);
		}
		else key2IdMap.put(key, id);
		
//		if (id2KeyMap.containsKey(id)) {
//			if (allowOnlyOneKeyPerId) {
//				return;
//			}
//			Set<String> keys = id2AltKeysMap.get(id);
//			if (keys == null) {
//				String mappedKey = id2KeyMap.get(id);
//				if (key.equals(mappedKey)) return;
//				// System.out.println("Id key mapping is not unique for id " + id + " (initial Key: " +  mappedKey + ")");
//				keys = new HashSet<>();
//				id2AltKeysMap.put(id,  keys);
//			}
//			keys.add(key);
//		}
//		else id2KeyMap.put(id, key);
		
		if (!id2KeyMap.containsKey(id)) id2KeyMap.put(id, key);
		
		// System.out.println("Key To ID Mapping added (key: " + key + ", id: " + id + ")");
	}
	
	String getKey(Integer id) {
		return id2KeyMap.get(id);
	}
	
	Integer getId(String key, Integer preferredId) {
		if (key2IdMap.containsKey(key)) {
			Integer id = key2IdMap.get(key);
			// if (id != null && !id.equals(preferredId)) System.out.println("Key is not mapped to the preferred id (Key:" + key + ", expId: " + preferredId + ", obsId: " + id + ")");
			if (id != null) return id;
			Set<Integer> ids = key2IdsMap.get(key);
			if (ids.contains(preferredId)) return preferredId;
			// System.out.println("Key has a non default id. (Key: " + key + ", DefId: " + preferredId + ", NewId: " + ids.toArray(Integer[]::new)[0] + ")");
			return ids.toArray(Integer[]::new)[0];
		}
		return null;
	}
	
	boolean hasKey(String key) {
		return key2IdMap.containsKey(key);
	}
	
	boolean hasId(Integer id) {
		return id2KeyMap.containsKey(id);
	}
}
