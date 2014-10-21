/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
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
package de.bund.bfr.math;

public enum Transform {

	NO_TRANSFORM, SQRT_TRANSFORM, LOG_TRANSFORM, LOG10_TRANSFORM, EXP_TRANSFORM, EXP10_TRANSFORM, KELVIN_TO_CELSIUS;

	@Override
	public String toString() {
		switch (this) {
		case NO_TRANSFORM:
			return "";
		case SQRT_TRANSFORM:
			return "sqrt";
		case LOG_TRANSFORM:
			return "log";
		case LOG10_TRANSFORM:
			return "log10";
		case EXP_TRANSFORM:
			return "exp";
		case EXP10_TRANSFORM:
			return "10^x";
		case KELVIN_TO_CELSIUS:
			return "K->C°";
		}

		return super.toString();
	}

	public Double to(Double value) {
		if (value == null) {
			return null;
		}

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
		}

		return null;
	}

	public Double from(Double value) {
		if (value == null) {
			return null;
		}

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
		}

		return null;
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
		}

		return null;
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
		}

		return null;
	}

	public String getName(String attr) {
		if (this != Transform.NO_TRANSFORM) {
			return this + "(" + attr + ")";
		}

		return attr;
	}
}
