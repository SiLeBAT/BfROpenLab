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
	private String supplierId, recipientId;
	private Integer departureDay;
	private Integer departureMonth;
	private Integer departureYear;
	private Integer arrivalDay;
	private Integer arrivalMonth;
	private Integer arrivalYear;
	private String lotNumber;
	private Double amount;
	private String unit;
	private Double amountInKg;

	private Set<String> allNextIds;
	private Set<String> allPreviousIds;

	public Delivery(String id, String supplierId, String recipientId, Integer departureDay, Integer departureMonth,
			Integer departureYear, Integer arrivalDay, Integer arrivalMonth, Integer arrivalYear, String lotNumber,
			Double amount, String unit, Double amountInKg) {
		this.id = id;
		this.supplierId = supplierId;
		this.recipientId = recipientId;
		this.departureDay = departureDay;
		this.departureMonth = departureMonth;
		this.departureYear = departureYear;
		this.arrivalDay = arrivalDay;
		this.arrivalMonth = arrivalMonth;
		this.arrivalYear = arrivalYear;
		this.lotNumber = lotNumber;
		this.amount = amount;
		this.unit = unit;
		this.amountInKg = amountInKg;

		allNextIds = new LinkedHashSet<>();
		allPreviousIds = new LinkedHashSet<>();

		// if one of the dates is missing, just use the other date
		if (this.arrivalYear == null) {
			this.arrivalDay = this.departureDay;
			this.arrivalMonth = this.departureMonth;
			this.arrivalYear = this.departureYear;
		} else if (this.departureYear == null) {
			this.departureDay = this.arrivalDay;
			this.departureMonth = this.arrivalMonth;
			this.departureYear = this.arrivalYear;
		}
	}

	public Delivery(String id, String supplierId, String recipientId, Integer departureDay, Integer departureMonth,
			Integer departureYear, Integer arrivalDay, Integer arrivalMonth, Integer arrivalYear) {
		this(id, supplierId, recipientId, departureDay, departureMonth, departureYear, arrivalDay, arrivalMonth,
				arrivalYear, null, null, null, null);
	}

	public String getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}

	public String getRecipientId() {
		return recipientId;
	}

	public void setRecipientId(String recipientId) {
		this.recipientId = recipientId;
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

	public String getLotNumber() {
		return lotNumber;
	}

	public Double getAmount() {
		return amount;
	}

	public String getUnit() {
		return unit;
	}

	public Double getAmountInKg() {
		return amountInKg;
	}

	public Set<String> getAllNextIds() {
		return allNextIds;
	}

	public Set<String> getAllPreviousIds() {
		return allPreviousIds;
	}

	public Delivery copy() {
		Delivery copy = new Delivery(id, supplierId, recipientId, departureDay, departureMonth, departureYear,
				arrivalDay, arrivalMonth, arrivalYear, lotNumber, amount, unit, amountInKg);

		copy.allNextIds = new LinkedHashSet<>(allNextIds);
		copy.allPreviousIds = new LinkedHashSet<>(allPreviousIds);

		return copy;
	}

	// e.g. Jan 2012 vs. 18.Jan 2012 - be generous
	public boolean isBefore(Delivery next) {
		if (arrivalYear == null || next.departureYear == null) {
			return true;
		} else if (next.departureYear > arrivalYear) {
			return true;
		} else if (next.departureYear < arrivalYear) {
			return false;
		}

		if (arrivalMonth == null || next.departureMonth == null) {
			return true;
		} else if (next.departureMonth > arrivalMonth) {
			return true;
		} else if (next.departureMonth < arrivalMonth) {
			return false;
		}

		if (arrivalDay == null || next.departureDay == null) {
			return true;
		} else if (next.departureDay >= arrivalDay) {
			return true;
		}

		return false;
	}
}
