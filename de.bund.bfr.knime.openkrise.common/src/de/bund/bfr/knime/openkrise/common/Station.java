package de.bund.bfr.knime.openkrise.common;

import java.lang.reflect.Method;

import com.google.common.collect.ImmutableMap;

public class Station {

	public static final ImmutableMap<String, Method> PROPERTIES = new ImmutableMap.Builder<String, Method>()
			.put("ID", getMethod("getId")).put("Name", getMethod("getName")).put("Business Type", getMethod("getType"))
			.put("Zip Code", getMethod("getZipCode")).put("City", getMethod("getCity"))
			.put("District", getMethod("getDistrict")).put("State", getMethod("getState"))
			.put("Country", getMethod("getCountry")).build();

	private static Method getMethod(String name) {
		try {
			return Station.class.getMethod(name);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String id;
	private String name;

	private String type;
	private String country;
	private String state;
	private String district;
	private String city;
	private String zipCode;

	public Station(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
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
