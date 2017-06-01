/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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

import java.lang.reflect.Method;

import com.google.common.collect.ImmutableMap;

public class Station {

	public static final ImmutableMap<String, Method> PROPERTIES = new ImmutableMap.Builder<String, Method>()
			.put("ID", getMethod("getId")).put("Name", getMethod("getName")).put("Business Type", getMethod("getType"))
			.put("Zip Code", getMethod("getZipCode")).put("City", getMethod("getCity"))
			.put("District", getMethod("getDistrict")).put("State", getMethod("getState"))
			.put("Country", getMethod("getCountry")).put("Address", getMethod("getAddress")).put("ID2017", getMethod("getId2017")).build();

	private static Method getMethod(String name) {
		try {
			return Station.class.getMethod(name);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String id;
	private String id2017;
	private String name;

	private String type;
	private String country;
	private String state;
	private String district;
	private String city;
	private String zipCode;
	private String address;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Station(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getId2017() {
		return id2017;
	}

	public void setId2017(String id2017) {
		this.id2017 = id2017;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	@Override
	public String toString() {
		return "Station [id=" + id + ", name=" + name + ", type=" + type + ", country=" + country + ", state=" + state
				+ ", district=" + district + ", city=" + city + ", zipCode=" + zipCode + "]";
	}
}
