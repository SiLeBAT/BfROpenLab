/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.openkrise;

import java.util.LinkedHashSet;
import java.util.Set;

public class Delivery {

	private String id;
	private String supplierID, recipientID;
	private Integer departureDay;
	private Integer departureMonth;
	private Integer departureYear;
	private Integer arrivalDay;
	private Integer arrivalMonth;
	private Integer arrivalYear;

	private Set<String> allNextIDs;
	private Set<String> allPreviousIDs;

	public Delivery(String id, String supplierID, String recipientID, Integer departureDay,
			Integer departureMonth, Integer departureYear, Integer arrivalDay,
			Integer arrivalMonth, Integer arrivalYear) {
		this.id = id;
		this.supplierID = supplierID;
		this.recipientID = recipientID;
		this.departureDay = departureDay;
		this.departureMonth = departureMonth;
		this.departureYear = departureYear;
		this.arrivalDay = arrivalDay;
		this.arrivalMonth = arrivalMonth;
		this.arrivalYear = arrivalYear;

		allNextIDs = new LinkedHashSet<>();
		allPreviousIDs = new LinkedHashSet<>();
	}

	public Set<String> getAllNextIDs() {
		return allNextIDs;
	}

	public Set<String> getAllPreviousIDs() {
		return allPreviousIDs;
	}

	public void setSupplierID(String supplierID) {
		this.supplierID = supplierID;
	}

	public void setRecipientID(String recipientID) {
		this.recipientID = recipientID;
	}

	public String getSupplierID() {
		return supplierID;
	}

	public String getRecipientID() {
		return recipientID;
	}

	public String getId() {
		return id;
	}

	public Integer getDepartureDay() {
		return departureDay;
	}

	public Integer getDepartureMonth() {
		return departureMonth;
	}

	public Integer getDepartureYear() {
		return departureYear;
	}

	public Integer getArrivalDay() {
		return arrivalDay;
	}

	public Integer getArrivalMonth() {
		return arrivalMonth;
	}

	public Integer getArrivalYear() {
		return arrivalYear;
	}

	public Delivery copy() {
		Delivery copy = new Delivery(id, supplierID, recipientID, departureDay, departureMonth,
				departureYear, arrivalDay, arrivalMonth, arrivalYear);

		copy.allNextIDs = new LinkedHashSet<>(allNextIDs);
		copy.allPreviousIDs = new LinkedHashSet<>(allPreviousIDs);

		return copy;
	}
}
