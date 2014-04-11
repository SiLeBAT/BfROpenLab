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

	private MyHashSet<Integer> forwardDeliveries;
	private MyHashSet<Integer> backwardDeliveries;

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
	public MyHashSet<Integer> getForwardDeliveries() {
		return forwardDeliveries;
	}
	public void setForwardDeliveries(MyHashSet<Integer> forwardDeliveries) {
		this.forwardDeliveries = forwardDeliveries;
	}
	public MyHashSet<Integer> getBackwardDeliveries() {
		return backwardDeliveries;
	}
	public void setBackwardDeliveries(MyHashSet<Integer> backwardDeliveries) {
		this.backwardDeliveries = backwardDeliveries;
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
		forwardDeliveries = null;
		backwardDeliveries = null;
	}
	public MyDelivery clone() {
		MyDelivery md = this;
		MyDelivery mdNew = new MyDelivery(md.getId(), md.getSupplierID(), md.getRecipientID(), md.getDeliveryDay(), md.getDeliveryMonth(), md.getDeliveryYear());
		//mdNew.getAllNextIDs().addAll(md.getAllNextIDs());
		//mdNew.getAllPreviousIDs().addAll(md.getAllPreviousIDs());
		for (Integer next : md.getAllNextIDs()) {
			mdNew.addNext(next);
		}
		for (Integer previous : md.getAllPreviousIDs()) {
			mdNew.addPrevious(previous);
		}
		return mdNew;
	}
}
