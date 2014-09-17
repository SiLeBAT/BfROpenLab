/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Armin A. Weiser (BfR)
 * Christian Thoens (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
 ******************************************************************************/
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
		
		allNextIDs = new HashSet<>();
		allPreviousIDs = new HashSet<>();
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

	public void setDeliveryDay(Integer deliveryDay) {
		this.deliveryDay = deliveryDay;
	}

	public void setDeliveryMonth(Integer deliveryMonth) {
		this.deliveryMonth = deliveryMonth;
	}

	public void setDeliveryYear(Integer deliveryYear) {
		this.deliveryYear = deliveryYear;
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
	@Override
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
