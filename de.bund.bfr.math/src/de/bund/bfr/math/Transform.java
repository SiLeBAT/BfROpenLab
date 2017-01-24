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
	NO_TRANSFORM("") {
		@Override
		public double to(double value) {
			return value;
		}

		@Override
		public double from(double value) {
			return value;
		}

		@Override
		public String to(String term) {
			return "(" + term + ")";
		}

		@Override
		public String from(String term) {
			return "(" + term + ")";
		}
	},
	SQRT_TRANSFORM("sqrt") {
		@Override
		public double to(double value) {
			return Math.sqrt(value);
		}

		@Override
		public double from(double value) {
			return value * value;
		}

		@Override
		public String to(String term) {
			return "sqrt(" + term + ")";
		}

		@Override
		public String from(String term) {
			return "(" + term + ")^2";
		}
	},
	LOG_TRANSFORM("log") {
		@Override
		public double to(double value) {
			return Math.log(value);
		}

		@Override
		public double from(double value) {
			return Math.exp(value);
		}

		@Override
		public String to(String term) {
			return "log(" + term + ")";
		}

		@Override
		public String from(String term) {
			return "exp(" + term + ")";
		}
	},
	LOG10_TRANSFORM("log10") {
		@Override
		public double to(double value) {
			return Math.log10(value);
		}

		@Override
		public double from(double value) {
			return Math.pow(10.0, value);
		}

		@Override
		public String to(String term) {
			return "log10(" + term + ")";
		}

		@Override
		public String from(String term) {
			return "10^(" + term + ")";
		}
	},
	EXP_TRANSFORM("exp") {
		@Override
		public double to(double value) {
			return Math.exp(value);
		}

		@Override
		public double from(double value) {
			return Math.log(value);
		}

		@Override
		public String to(String term) {
			return "exp(" + term + ")";
		}

		@Override
		public String from(String term) {
			return "log(" + term + ")";
		}
	},
	EXP10_TRANSFORM("10^x") {
		@Override
		public double to(double value) {
			return Math.pow(10.0, value);
		}

		@Override
		public double from(double value) {
			return Math.log10(value);
		}

		@Override
		public String to(String term) {
			return "10^(" + term + ")";
		}

		@Override
		public String from(String term) {
			return "log10(" + term + ")";
		}
	},
	KELVIN_TO_CELSIUS("K->C°") {
		@Override
		public double to(double value) {
			return value - 273.15;
		}

		@Override
		public double from(double value) {
			return value + 273.15;
		}

		@Override
		public String to(String term) {
			return "(" + term + "-273.15)";
		}

		@Override
		public String from(String term) {
			return "(" + term + "+273.15)";
		}
	};

	private String name;

	private Transform(String name) {
		this.name = name;
	}

	public abstract double to(double value);

	public abstract double from(double value);

	public abstract String to(String term);

	public abstract String from(String term);

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
