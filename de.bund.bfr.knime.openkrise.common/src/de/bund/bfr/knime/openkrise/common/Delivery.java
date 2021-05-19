/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.common;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class Delivery {

	public static class Builder {

		private String id;
		private String supplierId;
		private String recipientId;

		private ImmutableSet<String> allPreviousIds;
		private ImmutableSet<String> allNextIds;

		private Integer departureDay;
		private Integer departureMonth;
		private Integer departureYear;

		private Integer arrivalDay;
		private Integer arrivalMonth;
		private Integer arrivalYear;

		private String lotId;
		private String lot;

		private Double amount;
		private String unit;
		private Double amountInKg;

		public Builder(String id, String supplierId, String recipientId) {
			this.id = id;
			this.supplierId = supplierId;
			this.recipientId = recipientId;

			allPreviousIds = ImmutableSet.of();
			allNextIds = ImmutableSet.of();

		}

		public Builder connectedDeliveries(Set<String> allPreviousIds, Set<String> allNextIds) {
			this.allPreviousIds = ImmutableSet.copyOf(allPreviousIds);
			this.allNextIds = ImmutableSet.copyOf(allNextIds);
			return this;
		}

		public Builder departure(Integer year, Integer month, Integer day) {
			departureYear = year;
			departureMonth = year != null ? month : null;
			departureDay = (year != null && month != null) ? day : null;
			return this;
		}

		public Builder arrival(Integer year, Integer month, Integer day) {
			arrivalYear = year;
			arrivalMonth = year != null ? month : null;
			arrivalDay = (year != null && month != null) ? day : null;
			return this;
		}

		public Builder lotId(String lotId) {
			this.lotId = lotId;
			return this;
		}

		public Builder lot(String lot) {
			this.lot = lot;
			return this;
		}

		public Builder amount(Double amount, String unit, Double amountInKg) {
			this.amount = unit != null ? amount : null;
			this.unit = amount != null ? unit : null;
			this.amountInKg = amountInKg;
			return this;
		}

		public Delivery build() {
			// if one of the dates is missing, just use the other date
			if (arrivalYear == null) {
				arrivalDay = departureDay;
				arrivalMonth = departureMonth;
				arrivalYear = departureYear;
			} else if (departureYear == null) {
				departureDay = arrivalDay;
				departureMonth = arrivalMonth;
				departureYear = arrivalYear;
			}

			return new Delivery(this);
		}
	}

	private Builder builder;

	private Delivery(Builder builder) {
		this.builder = builder;
	}

	public String getSupplierId() {
		return builder.supplierId;
	}

	public String getRecipientId() {
		return builder.recipientId;
	}

	public String getId() {
		return builder.id;
	}

	public Integer getDepartureDay() {
		return builder.departureDay;
	}

	public Integer getDepartureMonth() {
		return builder.departureMonth;
	}

	public Integer getDepartureYear() {
		return builder.departureYear;
	}

	public Integer getArrivalDay() {
		return builder.arrivalDay;
	}

	public Integer getArrivalMonth() {
		return builder.arrivalMonth;
	}

	public Integer getArrivalYear() {
		return builder.arrivalYear;
	}

	public String getLotId() {
		return builder.lotId;
	}

	public String getLot() {
		return builder.lot;
	}

	public Double getAmount() {
		return builder.amount;
	}

	public String getUnit() {
		return builder.unit;
	}

	public Double getAmountInKg() {
		return builder.amountInKg;
	}

	public ImmutableSet<String> getAllNextIds() {
		return builder.allNextIds;
	}

	public ImmutableSet<String> getAllPreviousIds() {
		return builder.allPreviousIds;
	}

	public boolean isBefore(Delivery next) {
		if (builder.arrivalYear == null || next.builder.departureYear == null) {
			return true;
		} else if (next.builder.departureYear > builder.arrivalYear) {
			return true;
		} else if (next.builder.departureYear < builder.arrivalYear) {
			return false;
		}

		if (builder.arrivalMonth == null || next.builder.departureMonth == null) {
			return true;
		} else if (next.builder.departureMonth > builder.arrivalMonth) {
			return true;
		} else if (next.builder.departureMonth < builder.arrivalMonth) {
			return false;
		}

		if (builder.arrivalDay == null || next.builder.departureDay == null) {
			return true;
		} else if (next.builder.departureDay >= builder.arrivalDay) {
			return true;
		}

		return false;
	}
}
