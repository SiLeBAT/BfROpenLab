/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.math;

public enum Transform {
	NO_TRANSFORM(""), SQRT_TRANSFORM("sqrt"), LOG_TRANSFORM("log"), LOG10_TRANSFORM("log10"), EXP_TRANSFORM(
			"exp"), EXP10_TRANSFORM("10^x"), KELVIN_TO_CELSIUS("K->C°");

	private String name;

	private Transform(String name) {
		this.name = name;
	}

	public double to(double value) {
		switch (this) {
		case NO_TRANSFORM:
			return value;
		case SQRT_TRANSFORM:
			return Math.sqrt(value);
		case LOG_TRANSFORM:
			return Math.log(value);
		case LOG10_TRANSFORM:
			return Math.log10(value);
		case EXP_TRANSFORM:
			return Math.exp(value);
		case EXP10_TRANSFORM:
			return Math.pow(10.0, value);
		case KELVIN_TO_CELSIUS:
			return value - 273.15;
		default:
			throw new RuntimeException("Unknown Transform: " + this);
		}
	}

	public double from(double value) {
		switch (this) {
		case NO_TRANSFORM:
			return value;
		case SQRT_TRANSFORM:
			return value * value;
		case LOG_TRANSFORM:
			return Math.exp(value);
		case LOG10_TRANSFORM:
			return Math.pow(10.0, value);
		case EXP_TRANSFORM:
			return Math.log(value);
		case EXP10_TRANSFORM:
			return Math.log10(value);
		case KELVIN_TO_CELSIUS:
			return value + 273.15;
		default:
			throw new RuntimeException("Unknown Transform: " + this);
		}
	}

	public String to(String term) {
		if (term == null) {
			return null;
		}

		switch (this) {
		case NO_TRANSFORM:
			return "(" + term + ")";
		case SQRT_TRANSFORM:
			return "sqrt(" + term + ")";
		case LOG_TRANSFORM:
			return "log(" + term + ")";
		case LOG10_TRANSFORM:
			return "log10(" + term + ")";
		case EXP_TRANSFORM:
			return "exp(" + term + ")";
		case EXP10_TRANSFORM:
			return "10^(" + term + ")";
		case KELVIN_TO_CELSIUS:
			return "(" + term + "-273.15)";
		default:
			throw new RuntimeException("Unknown Transform: " + this);
		}
	}

	public String from(String term) {
		if (term == null) {
			return null;
		}

		switch (this) {
		case NO_TRANSFORM:
			return "(" + term + ")";
		case SQRT_TRANSFORM:
			return "(" + term + ")^2";
		case LOG_TRANSFORM:
			return "exp(" + term + ")";
		case LOG10_TRANSFORM:
			return "10^(" + term + ")";
		case EXP_TRANSFORM:
			return "log(" + term + ")";
		case EXP10_TRANSFORM:
			return "log10(" + term + ")";
		case KELVIN_TO_CELSIUS:
			return "(" + term + "+273.15)";
		default:
			throw new RuntimeException("Unknown Transform: " + this);
		}
	}

	public String getName() {
		return name;
	}

	public String getName(String attr) {
		if (this != NO_TRANSFORM) {
			return name + "(" + attr + ")";
		}

		return attr;
	}

	public static Transform fromName(String name) {
		for (Transform f : values()) {
			if (f.name.equals(name)) {
				return f;
			}
		}

		return NO_TRANSFORM;
	}

	@Override
	public String toString() {
		return name;
	}
}
