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
package de.bund.bfr.knime.openkrise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;

public class Lot {
	public static List<String> lotParams = new ArrayList<String>(){/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
		add("from");
		add("Name");
		add("Lot Number");
		add("Item Number");
		add("Date Expiration");
	}};

	private String lotId;
	private Map<String, Set<String>> deliveryIds2Ids;
	private String stationId;
	private String lotnumber;
	private String name;
	private String itemnumber;
	private String dateexpiration;
	private Map<Lot, Set<String>> tos;

	public Lot() {
		tos = new HashMap<>();
		deliveryIds2Ids = new HashMap<>();
	}

	public void setParam(String param, String value) {
		if (param.equals("from")) setStationId(value);
		else if (param.equals("Name")) setName(value);
		else if (param.equals("Lot Number")) setLotnumber(value);
		else if (param.equals("Item Number")) setItemnumber(value);
		else if (param.equals("Date Expiration")) setDateexpiration(value);
	}
	public DataCell getCell(String param) {
		if (lotId != null && param.equals("from")) return new StringCell(lotId);
		else if (name != null && param.equals("Name")) return new StringCell(name);
		else if (lotnumber != null && param.equals("Lot Number")) return new StringCell(lotnumber);
		else if (itemnumber != null && param.equals("Item Number")) return new StringCell(itemnumber);
		else if (dateexpiration != null && param.equals("Date Expiration")) return new StringCell(dateexpiration);
		return DataType.getMissingCell();
	}
	
	public Map<Lot, Set<String>> getTos() {
		return tos;
	}

	public void addTo(Lot to) {
		if (!tos.containsKey(to)) tos.put(to, new HashSet<>());
	}
	public void addToDelivery(Lot to, String deliveryId) {
		if (!tos.containsKey(to)) tos.put(to, new HashSet<>());
		tos.get(to).add(deliveryId);
	}

	public String getLotId() {
		return lotId;
	}

	public void setLotId(String lotId) {
		this.lotId = lotId;
	}

	public Map<String, Set<String>> getDeliveryIds2Ids() {
		return deliveryIds2Ids;
	}

	public void addDeliveryId(String deliveryId_from) {
		if (!deliveryIds2Ids.containsKey(deliveryId_from)) deliveryIds2Ids.put(deliveryId_from, new HashSet<>());
	}
	public void addDeliveryId2Id(String deliveryId_from, String deliveryId_to) {
		addDeliveryId(deliveryId_from);
		deliveryIds2Ids.get(deliveryId_from).add(deliveryId_to);
	}

	public String getStationId() {
		return stationId;
	}

	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	public String getLotnumber() {
		return lotnumber;
	}

	public void setLotnumber(String lotnumber) {
		this.lotnumber = lotnumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getItemnumber() {
		return itemnumber;
	}

	public void setItemnumber(String itemnumber) {
		this.itemnumber = itemnumber;
	}

	public String getDateexpiration() {
		return dateexpiration;
	}

	public void setDateexpiration(String dateexpiration) {
		this.dateexpiration = dateexpiration;
	}
}
