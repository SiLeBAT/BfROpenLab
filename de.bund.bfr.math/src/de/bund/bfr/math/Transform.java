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

	NO_TRANSFORM, SQRT_TRANSFORM, LOG_TRANSFORM, LOG10_TRANSFORM, EXP_TRANSFORM, EXP10_TRANSFORM, DIVX_TRANSFORM, DIVX2_TRANSFORM;

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
		case DIVX_TRANSFORM:
			return "1/x";
		case DIVX2_TRANSFORM:
			return "1/x^2";
		}
		
		return super.toString();
	}

	public static Double transform(Double value, Transform transform) {
		if (value == null) {
			return null;
		}

		switch (transform) {
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
		case DIVX_TRANSFORM:
			return 1 / value;
		case DIVX2_TRANSFORM:
			return 1 / value / value;
		}

		return null;
	}

	public static Double inverseTransform(Double value, Transform transform) {
		if (value == null) {
			return null;
		}

		switch (transform) {
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
		case DIVX_TRANSFORM:
			return 1 / value;
		case DIVX2_TRANSFORM:
			return 1 / Math.sqrt(value);
		}

		return null;
	}
}
