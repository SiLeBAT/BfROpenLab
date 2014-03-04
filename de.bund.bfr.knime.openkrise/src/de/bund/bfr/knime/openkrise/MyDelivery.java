package de.bund.bfr.knime.openkrise;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

public class MyDelivery {

	private int id;
	private int supplierID, recipientID;
	private Calendar deliveryDate;
	private Long deliveryDateAsMillis;
	private HashMap<Integer, Double> caseStations;
	private HashSet<Integer> ccStations;
	//private HashSet<Integer> ccDeliveries;
	private HashMap<Integer, MyDelivery> allDeliveries;
	private HashMap<Integer, HashSet<MyDelivery>> allIncoming;
	private HashMap<Integer, HashSet<MyDelivery>> allOutgoing;
	
	private HashSet<MyDelivery> allNext;
	private HashSet<MyDelivery> allPrevious;
	private HashSet<Integer> allNextIDs;
	private HashSet<Integer> allPreviousIDs;
	private HashSet<Integer> forwardStationsWithCases;
	private HashSet<Integer> backwardStationsWithCases;
	private HashSet<Integer> forwardStations;
	private HashSet<Integer> backwardStations;
	private HashSet<MyDelivery> forwardDeliveries;
	private HashSet<MyDelivery> backwardDeliveries;
	
	private boolean enforceTemporalOrder = true;
	
	public MyDelivery(int id, int supplierID, int recipientID, HashMap<Integer, Double> caseStations, Calendar deliveryDate, HashSet<Integer> ccStations) {
		this.id = id;
		this.supplierID = supplierID;
		this.recipientID = recipientID;
		this.caseStations = caseStations;
		this.ccStations = ccStations;
		this.deliveryDate = deliveryDate;
		deliveryDateAsMillis = (deliveryDate != null) ? deliveryDate.getTimeInMillis() : 0;
		
		forwardStationsWithCases = null;
		backwardStationsWithCases = null;
		forwardStations = null;
		backwardStations = null;
		forwardDeliveries = null;
		backwardDeliveries = null;
		allNext = new HashSet<MyDelivery>();
		allPrevious = new HashSet<MyDelivery>();
		allNextIDs = new HashSet<Integer>();
		allPreviousIDs = new HashSet<Integer>();
	}
	public void setSupplierID(int supplierID) {
		this.supplierID = supplierID;
	}
	public void setRecipientID(int recipientID) {
		this.recipientID = recipientID;
	}
	public void setCaseStations(HashMap<Integer, Double> caseStations) {
		this.caseStations = caseStations;
	}
	public void setCCStations(HashSet<Integer> ccStations) {
		this.ccStations = ccStations;
	}
	public void fillPrevsNexts(HashMap<Integer, MyDelivery> allDeliveries, HashMap<Integer, HashSet<MyDelivery>> allIncoming, HashMap<Integer, HashSet<MyDelivery>> allOutgoing) {
		this.allIncoming = allIncoming;
		this.allOutgoing = allOutgoing;
		this.allDeliveries = allDeliveries;
		allNext = new HashSet<MyDelivery>();
		allPrevious = new HashSet<MyDelivery>();
		for (Integer id : allNextIDs) {
			allNext.add(allDeliveries.get(id));
		}
		for (Integer id : allPreviousIDs) {
			allPrevious.add(allDeliveries.get(id));
		}
	}
	public void resetStatusVariables(boolean enforceTemporalOrder) {
		this.enforceTemporalOrder = enforceTemporalOrder;
		forwardStationsWithCases = null;
		backwardStationsWithCases = null;
		forwardStations = null;
		backwardStations = null;
		forwardDeliveries = null;
		backwardDeliveries = null;
	}
	
	public int getSupplierID() {
		return supplierID;
	}

	public int getRecipientID() {
		return recipientID;
	}
	public int getId() {
		return id;
	}
	public Calendar getDeliveryDate() {
		return deliveryDate;
	}
	public Long getDeliveryDateAsMillis() {
		return deliveryDateAsMillis;
	}

	public HashSet<MyDelivery> getNexts() {
		return allNext;
	}
	public void addNext(MyDelivery next) {
		if (next == null) System.err.println("next = null...");
		else {
			allNext.add(next);
			allNextIDs.add(next.getId());
		}
	}
	public HashSet<MyDelivery> getPreviouss() {
		return allPrevious;
	}
	public void addPrevious(MyDelivery previous) {
		if (previous == null) System.err.println("previous = null...");
		else {
			allPrevious.add(previous);
			allPreviousIDs.add(previous.getId());
		}
	}
	
	
	public HashSet<MyDelivery> getForwardDeliveries() {
		if (forwardDeliveries == null) {
			forwardDeliveries = new HashSet<MyDelivery>();			
			searchFFCases(this, new HashSet<Integer>(),true,forwardDeliveries);
		}
		return forwardDeliveries;
	}
	public HashSet<MyDelivery> getBackwardDeliveries() {
		if (backwardDeliveries == null) {
			backwardDeliveries = new HashSet<MyDelivery>();			
			searchFBCases(this, new HashSet<Integer>(),true,backwardDeliveries);
		}
		return backwardDeliveries;
	}
	public HashSet<Integer> getForwardStations() {
		if (forwardStations == null) {
			forwardStations = new HashSet<Integer>();			
			searchFFCases(this, forwardStations,true,new HashSet<MyDelivery>());
		}
		return forwardStations;
	}
	public HashSet<Integer> getBackwardStations() {
		if (backwardStations == null) {
			backwardStations = new HashSet<Integer>();			
			searchFBCases(this, backwardStations,true,new HashSet<MyDelivery>());
		}
		return backwardStations;
	}
	
	public HashSet<Integer> getForwardStationsWithCases() {
		if (forwardStationsWithCases == null) {
			forwardStationsWithCases = new HashSet<Integer>();			
			searchFFCases(this, forwardStationsWithCases,false,new HashSet<MyDelivery>());
		}
		return forwardStationsWithCases;
	}
	public HashSet<Integer> getBackwardStationsWithCases() {
		if (backwardStationsWithCases == null) {
			backwardStationsWithCases = new HashSet<Integer>();			
			searchFBCases(this, backwardStationsWithCases,false,new HashSet<MyDelivery>());
		}
		return backwardStationsWithCases;
	}
	private void searchFBCases(MyDelivery md, HashSet<Integer> stemmingStationsWithCases,boolean includeStationWithoutCases,HashSet<MyDelivery> alreadySeen) {
		if (caseStations == null) caseStations = new HashMap<Integer, Double>();
		if (md == null || alreadySeen.contains(md)) return;
		else alreadySeen.add(md);
		if (includeStationWithoutCases || caseStations.containsKey(md.getSupplierID())) stemmingStationsWithCases.add(md.getSupplierID());		
		HashSet<MyDelivery> n = md.getPreviouss();
		for (MyDelivery d : n) {
			searchFBCases(d, stemmingStationsWithCases,includeStationWithoutCases,alreadySeen);
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
							searchFBCases(d, stemmingStationsWithCases,includeStationWithoutCases,alreadySeen);
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
	private void searchFFCases(MyDelivery md, HashSet<Integer> headingStationsWithCases,boolean includeStationWithoutCases,HashSet<MyDelivery> alreadySeen) {
		if (caseStations == null) caseStations = new HashMap<Integer, Double>();
		if (md == null || alreadySeen.contains(md)) return;
		else alreadySeen.add(md);
		//System.err.println(md);
		if (includeStationWithoutCases || caseStations.containsKey(md.getRecipientID())) headingStationsWithCases.add(md.getRecipientID());		
		HashSet<MyDelivery> n = md.getNexts();
		for (MyDelivery d : n) {
			searchFFCases(d, headingStationsWithCases,includeStationWithoutCases,alreadySeen);
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
							searchFFCases(d, headingStationsWithCases,includeStationWithoutCases,alreadySeen);
						}
					}
				}
			}			
		}
	}
}
