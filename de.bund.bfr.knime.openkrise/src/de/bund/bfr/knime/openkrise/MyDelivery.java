package de.bund.bfr.knime.openkrise;

import java.util.HashSet;

public class MyDelivery {

	private int id;
	private int supplierID, recipientID;
	private Integer deliveryDay;
	private Integer deliveryMonth;
	private Integer deliveryYear;
	
	private HashSet<Integer> allNextIDs;
	private HashSet<Integer> allPreviousIDs;

	private HashSet<Integer> forwardStationsWithCases;
	private HashSet<Integer> backwardStationsWithCases;
	private HashSet<Integer> forwardStations;
	private HashSet<Integer> backwardStations;
	private HashSet<Integer> forwardDeliveries;
	private HashSet<Integer> backwardDeliveries;
	
	public MyDelivery(int id, int supplierID, int recipientID, Integer deliveryDay, Integer deliveryMonth, Integer deliveryYear) {
		this.id = id;
		this.supplierID = supplierID;
		this.recipientID = recipientID;
		this.deliveryDay = deliveryDay;
		this.deliveryMonth = deliveryMonth;
		this.deliveryYear = deliveryYear;
		
		allNextIDs = new HashSet<Integer>();
		allPreviousIDs = new HashSet<Integer>();
	}

	public HashSet<Integer> getAllNextIDs() {
		return allNextIDs;
	}
	public HashSet<Integer> getAllPreviousIDs() {
		return allPreviousIDs;
	}
	public HashSet<Integer> getForwardStations() {
		return forwardStations;
	}
	public void setForwardStations(HashSet<Integer> forwardStations) {
		this.forwardStations = forwardStations;
	}
	public HashSet<Integer> getBackwardStations() {
		return backwardStations;
	}
	public void setBackwardStations(HashSet<Integer> backwardStations) {
		this.backwardStations = backwardStations;
	}
	public HashSet<Integer> getForwardDeliveries() {
		return forwardDeliveries;
	}
	public void setForwardDeliveries(HashSet<Integer> forwardDeliveries) {
		this.forwardDeliveries = forwardDeliveries;
	}
	public HashSet<Integer> getBackwardDeliveries() {
		return backwardDeliveries;
	}
	public void setBackwardDeliveries(HashSet<Integer> backwardDeliveries) {
		this.backwardDeliveries = backwardDeliveries;
	}
	public HashSet<Integer> getBackwardStationsWithCases() {
		return backwardStationsWithCases;
	}
	public void setBackwardStationsWithCases(
			HashSet<Integer> backwardStationsWithCases) {
		this.backwardStationsWithCases = backwardStationsWithCases;
	}
	public HashSet<Integer> getForwardStationsWithCases() {
		return forwardStationsWithCases;
	}
	public void setForwardStationsWithCases(
			HashSet<Integer> forwardStationsWithCases) {
		this.forwardStationsWithCases = forwardStationsWithCases;
	}
	public void setSupplierID(int supplierID) {
		this.supplierID = supplierID;
	}
	public void setRecipientID(int recipientID) {
		this.recipientID = recipientID;
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
	public Integer getDeliveryDay() {
		return deliveryDay;
	}
	public Integer getDeliveryMonth() {
		return deliveryMonth;
	}
	public Integer getDeliveryYear() {
		return deliveryYear;
	}

	public void removeNext(Integer nextID) {
		if (nextID == null) System.err.println("next = null...");
		else {
			allNextIDs.remove(nextID);
		}
	}
	public void addNext(Integer nextID) {
		if (nextID == null) System.err.println("next = null...");
		else {
			allNextIDs.add(nextID);
		}
	}
	public void removePrevious(Integer previousID) {
		if (previousID == null) System.err.println("previous = null...");
		else {
			allPreviousIDs.remove(previousID);
		}
	}
	public void addPrevious(Integer previousID) {
		if (previousID == null) System.err.println("previous = null...");
		else {
			allPreviousIDs.add(previousID);
		}
	}
		
	public void resetStatusVariables() {
		forwardStationsWithCases = null;
		backwardStationsWithCases = null;
		forwardStations = null;
		backwardStations = null;
		forwardDeliveries = null;
		backwardDeliveries = null;
	}
}
