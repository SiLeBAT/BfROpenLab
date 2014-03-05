package de.bund.bfr.knime.openkrise;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.ClassLoaderReference;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class MyNewTracing {

	private HashMap<Integer, MyDelivery> allDeliveriesOrig;
	private HashMap<Integer, MyDelivery> allDeliveries;
	private HashMap<Integer, HashSet<MyDelivery>> allIncoming;
	private HashMap<Integer, HashSet<MyDelivery>> allOutgoing;
	private HashMap<Integer, Double> caseStations = null;
	private HashSet<Integer> ccStations = null;
	//private HashSet<Integer> ccDeliveries = null;
	private LinkedHashMap<Integer, HashSet<Integer>> sortedStations = null;
	private LinkedHashMap<Integer, HashSet<Integer>> sortedDeliveries = null;
	private double caseSum = 0;
	private boolean enforceTemporalOrder = false;
	
	public MyNewTracing(HashMap<Integer, MyDelivery> allDeliveries, HashMap<Integer, Double> caseStations, HashSet<Integer> ccStations, double caseSum) {
		this.allDeliveries = allDeliveries;
		this.caseStations = caseStations;
		this.ccStations = ccStations;
		this.caseSum = caseSum;
		calcRecsSuppls();
	}
	private void calcRecsSuppls() {
		if (allIncoming == null || allOutgoing == null) {
			allIncoming = new HashMap<Integer, HashSet<MyDelivery>>();
			allOutgoing = new HashMap<Integer, HashSet<MyDelivery>>();
			for (MyDelivery d : allDeliveries.values()) {
				if (!allOutgoing.containsKey(d.getSupplierID())) allOutgoing.put(d.getSupplierID(), new HashSet<MyDelivery>());
				allOutgoing.get(d.getSupplierID()).add(d);
				if (!allIncoming.containsKey(d.getRecipientID())) allIncoming.put(d.getRecipientID(), new HashSet<MyDelivery>());
				allIncoming.get(d.getRecipientID()).add(d);
			}
		}
	}
	
	public boolean isStationStart(int id) {
		boolean result = true;
		for (Integer key : allDeliveries.keySet()) {
			if (allDeliveries.get(key).getRecipientID() == id) {
				result = false;
				break;
			}
		}
		return result;
	}
	public boolean isStationEnd(int id) {
		boolean result = true;
		for (Integer key : allDeliveries.keySet()) {
			if (allDeliveries.get(key).getSupplierID() == id) {
				result = false;
				break;
			}
		}
		return result;
	}
	public Double getStationScore(int id) {
		if (sortedStations == null) getScores(true);
		if (caseSum > 0 && sortedStations.get(id) != null) {
			double sum = 0;
			for (Integer key : sortedStations.get(id)) {
				if (caseStations.get(key) != null) {
					sum += caseStations.get(key);
				}
			}
			return sum / caseSum;
		}
		else return -1.0;
	}
	public Double getDeliveryScore(int id) {
		if (sortedDeliveries == null) getScores(false);
		if (caseSum > 0 && sortedDeliveries.get(id) != null) {
			double sum = 0;
			for (Integer key : sortedDeliveries.get(id)) {
				sum += caseStations.get(key);
			}
			return sum / caseSum;
//			return ((double) sortedDeliveries.get(id).size()) / caseStations.size();
		}
		else return -1.0;
	}
	@SuppressWarnings("unchecked")
	public LinkedHashMap<Integer, HashSet<Integer>> getScores(boolean stations) {
		// getForwardStationsWithCases counts for each delivery. But: it might be the case that a station delivers into "different" directions (deliveries), and all of them have cases!!!
		// Therefore, we sum here based on the suppliers (supplierSum), not on the deliveries!!!
		HashMap<Integer, HashSet<Integer>> supplierSum = new HashMap<Integer, HashSet<Integer>>(); 
		HashMap<Integer, HashSet<Integer>> deliverySum = new HashMap<Integer, HashSet<Integer>>(); 
		for (MyDelivery md : allDeliveries.values()) {
			if (supplierSum.containsKey(md.getSupplierID())) {
				HashSet<Integer> hi = (HashSet<Integer>) supplierSum.get(md.getSupplierID());
				for (Integer i : getForwardStationsWithCases(md)) {
					hi.add(i);
				}
				supplierSum.put(md.getSupplierID(), hi);
			}
			else {
				supplierSum.put(md.getSupplierID(), (HashSet<Integer>) getForwardStationsWithCases(md).clone());
			}

			deliverySum.put(md.getId(), (HashSet<Integer>) getForwardStationsWithCases(md).clone());
		}
		
		sortedStations = sortByValues(supplierSum);
		sortedDeliveries = sortByValues(deliverySum);		
		/*
        for (Integer key : sortedStations.keySet()) {
        	HashSet<Integer> hi = sortedStations.get(key);
            System.err.println("SupplierID: " + key + ":\t" + hi.size() + (caseStations != null ? " / " + caseStations.size() : "") + "\t" + hi.toString());
        }     
        for (Integer key : sortedDeliveries.keySet()) {
        	HashSet<Integer> hi = sortedDeliveries.get(key);
            System.err.println("DeliveryID: " + key + ":\t" + hi.size() + (caseStations != null ? " / " + caseStations.size() : "") + "\t" + hi.toString());
        }             
        */
        return stations ? sortedStations : sortedDeliveries;
	}
	private LinkedHashMap<Integer, HashSet<Integer>> sortByValues(HashMap<Integer, HashSet<Integer>> map){
        List<Map.Entry<Integer, HashSet<Integer>>> entries = new LinkedList<Map.Entry<Integer, HashSet<Integer>>>(map.entrySet());
      
        Collections.sort(entries, new Comparator<Map.Entry<Integer, HashSet<Integer>>>() {

            @Override
            public int compare(Entry<Integer, HashSet<Integer>> o1, Entry<Integer, HashSet<Integer>> o2) {
                return o2.getValue().size() - o1.getValue().size();
            }
        });
      
        //LinkedHashMap will keep the keys in the order they are inserted
        //which is currently sorted on natural ordering
        LinkedHashMap<Integer, HashSet<Integer>> sortedMap = new LinkedHashMap<Integer, HashSet<Integer>>();
      
        for(Map.Entry<Integer, HashSet<Integer>> entry: entries){
            sortedMap.put(entry.getKey(), entry.getValue());
        }
      
        return sortedMap;
    }
	public Set<Integer> getForwardStations(int stationID) {
		Set<Integer> stations = new LinkedHashSet<Integer>();
		
		for (MyDelivery md : getOutgoingDeliveries(stationID)) {
			stations.addAll(getForwardStations(md));
		}
		
		return stations;
	}
	public Set<Integer> getBackwardStations(int stationID) {
		Set<Integer> stations = new LinkedHashSet<Integer>();
		
		for (MyDelivery md : getIncomingDeliveries(stationID)) {
			stations.addAll(getBackwardStations(md));
		}
		
		return stations;
	}
	public Set<Integer> getForwardDeliveries(int stationID) {
		Set<Integer> deliveries = new LinkedHashSet<Integer>();
		
		for (MyDelivery md : getOutgoingDeliveries(stationID)) {
			deliveries.addAll(getForwardDeliveries(md));
		}
		
		return deliveries;
	}
	public Set<Integer> getBackwardDeliveries(int stationID) {
		Set<Integer> deliveries = new LinkedHashSet<Integer>();
		
		for (MyDelivery md : getIncomingDeliveries(stationID)) {
			deliveries.addAll(getBackwardDeliveries(md));
		}
		
		return deliveries;
	}
	private Set<MyDelivery> getOutgoingDeliveries(int stationID) {
		Set<MyDelivery> outgoing = new LinkedHashSet<MyDelivery>();
		
		for (MyDelivery md : allDeliveries.values()) {
			if (md.getSupplierID() == stationID) {
				outgoing.add(md);
			}
		}
		
		return outgoing;
	}
	private Set<MyDelivery> getIncomingDeliveries(int stationID) {
		Set<MyDelivery> incoming = new LinkedHashSet<MyDelivery>();
		
		for (MyDelivery md : allDeliveries.values()) {
			if (md.getRecipientID() == stationID) {
				incoming.add(md);
			}
		}
		
		return incoming;
	}

	public static XStream getXStream() {
		XStream xstream = new XStream(null, new XppDriver(),new ClassLoaderReference(MyNewTracing.class.getClassLoader())); // new DomDriver()
//		xstream.setClassLoader(Activator.class.getClassLoader());
		//xstream.alias("mynewtracing", MyNewTracing.class);
		xstream.omitField(MyNewTracing.class, "allDeliveriesOrig");
		xstream.omitField(MyNewTracing.class, "caseStations");
		xstream.omitField(MyNewTracing.class, "caseSum");
		xstream.omitField(MyNewTracing.class, "ccStations");
		xstream.omitField(MyNewTracing.class, "enforceTemporalOrder");
		xstream.omitField(MyNewTracing.class, "sortedStations");
		xstream.omitField(MyNewTracing.class, "sortedDeliveries");
		xstream.omitField(MyNewTracing.class, "allIncoming");
		xstream.omitField(MyNewTracing.class, "allOutgoing");
		
		xstream.omitField(MyDelivery.class, "forwardStationsWithCases");
		xstream.omitField(MyDelivery.class, "backwardStationsWithCases");
		xstream.omitField(MyDelivery.class, "forwardStations");
		xstream.omitField(MyDelivery.class, "backwardStations");
		xstream.omitField(MyDelivery.class, "forwardDeliveries");
		xstream.omitField(MyDelivery.class, "backwardDeliveries");

		return xstream;
	}
	public void fillDeliveries(boolean enforceTemporalOrder) {
		this.enforceTemporalOrder = enforceTemporalOrder;
		calcRecsSuppls();
		for (MyDelivery md : allDeliveries.values()) {
			md.resetStatusVariables();
		}
	}
	public void setCase(int stationID, double priority) {
		if (caseStations == null) caseStations = new HashMap<Integer, Double>();
		if (priority < 0) priority = 0;
		else if (priority > 1) priority = 1;
		if (caseStations.containsKey(stationID)) {			
			caseSum = caseSum - caseStations.get(stationID) + priority;
			caseStations.put(stationID, priority);
		}
		else {
			caseSum = caseSum + priority;
			caseStations.put(stationID, priority);
		}
		if (priority == 0) caseStations.remove(stationID);
		sortedStations = null;
		sortedDeliveries = null;
	}
	public void setCrossContamination(int stationID, boolean possible) {
		if (ccStations == null) ccStations = new HashSet<Integer>();
		if (possible) ccStations.add(stationID);
		else if (ccStations.contains(stationID)) ccStations.remove(stationID);  
		sortedStations = null;
		sortedDeliveries = null;
	}
	public void mergeStations(HashSet<Integer> toBeMerged) {
		mergeStations(toBeMerged, -1);
	}
	public void mergeStations(HashSet<Integer> toBeMerged, Integer mergedStationID) {
		getClone();
		allDeliveries = new HashMap<Integer, MyDelivery>();
		for (Integer key : allDeliveriesOrig.keySet()) {
			MyDelivery md = allDeliveriesOrig.get(key);
			if (toBeMerged != null) {
				if (!toBeMerged.contains(md.getRecipientID()) && !toBeMerged.contains(md.getSupplierID())) {
				}
				else {
					if (toBeMerged.contains(md.getSupplierID())) md.setSupplierID(mergedStationID);
					if (toBeMerged.contains(md.getRecipientID())) md.setRecipientID(mergedStationID);
				}				
			}
			allDeliveries.put(key, md);							
		}
		sortedStations = null;
		sortedDeliveries = null;
		fillDeliveries(enforceTemporalOrder);
	}
	public void resetMergedStations() {
		mergeStations(null);
	}
	public HashMap<Integer, MyDelivery> getClone() {
		if (allDeliveriesOrig == null) {
			allDeliveriesOrig = new HashMap<Integer, MyDelivery>();
			for (Integer key : allDeliveries.keySet()) {
				MyDelivery md = allDeliveries.get(key);
				MyDelivery mdNew = new MyDelivery(md.getId(), md.getSupplierID(), md.getRecipientID(), md.getDeliveryDate());
				for (Integer next : md.getAllNextIDs()) {
					mdNew.addNext(next);
				}
				for (Integer previous : md.getAllPreviousIDs()) {
					mdNew.addPrevious(previous);
				}
				allDeliveriesOrig.put(key, mdNew);
			}
		}
		return allDeliveriesOrig;
	}
	/*
	public void setCrossContaminationDelivery(int deliveryID, boolean possible) {
		if (ccDeliveries == null) ccDeliveries = new HashSet<Integer>();
		if (possible) ccDeliveries.add(deliveryID);
		else if (ccDeliveries.contains(deliveryID)) ccDeliveries.remove(deliveryID);  
		sortedStations = null;
		sortedDeliveries = null;
	}
	*/
	private void searchFBCases(MyDelivery md, HashSet<Integer> stemmingStationsWithCases,boolean includeStationWithoutCases,HashSet<Integer> stemmingDeliveries) {
		if (caseStations == null) caseStations = new HashMap<Integer, Double>();
		if (stemmingDeliveries.contains(md.getId())) return;
		else stemmingDeliveries.add(md.getId());
		if (includeStationWithoutCases || caseStations.containsKey(md.getSupplierID())) stemmingStationsWithCases.add(md.getSupplierID());		
		HashSet<Integer> n = md.getAllPreviousIDs();
		for (Integer d : n) {
			searchFBCases(allDeliveries.get(d), stemmingStationsWithCases,includeStationWithoutCases,stemmingDeliveries);
		}
		// check individual cross contamination
		if (allDeliveries != null) {
			if (ccStations != null && ccStations.contains(md.getSupplierID())) {
				//for (MyDelivery d : allDeliveries.values()) {
				HashSet<MyDelivery> mdl = allIncoming.get(md.getSupplierID()); 
				if (mdl != null) {
					for (MyDelivery d : mdl) {
						if (
								//d.getRecipientID() == md.getSupplierID() &&
								(!enforceTemporalOrder ||
								(is1Newer(md, d)))) {
										//md.getDeliveryDateAsSeconds() >= d.getDeliveryDateAsSeconds()))) {
							searchFBCases(d, stemmingStationsWithCases,includeStationWithoutCases,stemmingDeliveries);
						}
					}
				}
			}			
		}
	}
	private boolean is1Newer(MyDelivery md1, MyDelivery md2) { // e.g. Jan 2012 vs. 18.Jan 2012 - be generous
		Calendar cal1 = md1.getDeliveryDate();
		Calendar cal2 = md2.getDeliveryDate();
		if (cal1 == null || cal2 == null) return true;
		if (cal1.get(Calendar.HOUR_OF_DAY) == 12) { // day unknown, means: any day in month possible... this is specified in LieferkettenImporterEFSA
			Calendar cal = (Calendar) cal1.clone();
			if (cal.get(Calendar.MONTH) == 0) { // if January, any month in year is possible...
				cal.set(Calendar.MONTH, 11); // MONTH is 0-based
			}
			cal.set(Calendar.DAY_OF_MONTH, 31); // DAY_OF_MONTH is 1-based, should be no problem for e.g. february, we are generous...
			return cal.getTimeInMillis() >= cal2.getTimeInMillis();
		}
		else {
			return md1.getDeliveryDateAsMillis() >= md2.getDeliveryDateAsMillis();
		}
	}
	private void searchFFCases(MyDelivery md, HashSet<Integer> headingStationsWithCases,boolean includeStationWithoutCases,HashSet<Integer> headingDeliveries) {
		if (caseStations == null) caseStations = new HashMap<Integer, Double>();
		if (headingDeliveries.contains(md.getId())) return;
		else headingDeliveries.add(md.getId());
		//System.err.println(md);
		if (includeStationWithoutCases || caseStations.containsKey(md.getRecipientID())) headingStationsWithCases.add(md.getRecipientID());		
		HashSet<Integer> n = md.getAllNextIDs();
		for (Integer d : n) {
			searchFFCases(allDeliveries.get(d), headingStationsWithCases,includeStationWithoutCases,headingDeliveries);
		}
		// check individual cross contamination
		if (allDeliveries != null) {
			if (ccStations != null && ccStations.contains(md.getRecipientID())) {
				//for (MyDelivery d : allDeliveries.values()) {
				HashSet<MyDelivery> mdl = allOutgoing.get(md.getRecipientID()); 
				if (mdl != null) {
					for (MyDelivery d : mdl) {
						if (
								//d.getSupplierID() == md.getRecipientID() &&
								(!enforceTemporalOrder ||
								(is1Newer(d, md)))) {
										//md.getDeliveryDateAsSeconds() <= d.getDeliveryDateAsSeconds()))) {
							searchFFCases(d, headingStationsWithCases,includeStationWithoutCases,headingDeliveries);
						}
					}
				}
			}			
		}
	}
	private HashSet<Integer> getForwardDeliveries(MyDelivery md) {
		HashSet<Integer> forwardDeliveries = md.getForwardDeliveries();
		if (forwardDeliveries == null) {
			forwardDeliveries = new HashSet<Integer>();			
			searchFFCases(md, new HashSet<Integer>(),true,forwardDeliveries);
			md.setForwardDeliveries(forwardDeliveries);
		}
		return forwardDeliveries;
	}
	private HashSet<Integer> getBackwardDeliveries(MyDelivery md) {
		HashSet<Integer> backwardDeliveries = md.getBackwardDeliveries();
		if (backwardDeliveries == null) {
			backwardDeliveries = new HashSet<Integer>();			
			searchFBCases(md, new HashSet<Integer>(),true,backwardDeliveries);
			md.setBackwardDeliveries(backwardDeliveries);
		}
		return backwardDeliveries;
	}
	private HashSet<Integer> getForwardStations(MyDelivery md) {
		HashSet<Integer> forwardStations = md.getForwardStations();
		if (forwardStations == null) {
			forwardStations = new HashSet<Integer>();			
			searchFFCases(md, forwardStations,true,new HashSet<Integer>());
			md.setForwardStations(forwardStations);
		}
		return forwardStations;
	}
	private HashSet<Integer> getBackwardStations(MyDelivery md) {
		HashSet<Integer> backwardStations = md.getBackwardStations();
		if (backwardStations == null) {
			backwardStations = new HashSet<Integer>();			
			searchFBCases(md, backwardStations,true,new HashSet<Integer>());
			md.setBackwardStations(backwardStations);
		}
		return backwardStations;
	}
	
	private HashSet<Integer> getForwardStationsWithCases(MyDelivery md) {
		HashSet<Integer> forwardStationsWithCases = md.getForwardStationsWithCases();
		if (forwardStationsWithCases == null) {
			forwardStationsWithCases = new HashSet<Integer>();			
			searchFFCases(md, forwardStationsWithCases,false,new HashSet<Integer>());
			md.setForwardStationsWithCases(forwardStationsWithCases);
		}
		return forwardStationsWithCases;
	}
}
