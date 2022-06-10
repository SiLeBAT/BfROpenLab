/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.ui;

import com.google.common.base.Strings;

public class StringTextField extends TypedTextField {

	private static final long serialVersionUID = 1L;

	private String value;

	public StringTextField(boolean optional, int columns) {
		super(optional, columns);
		textChanged();
	}

	@Override
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		setTextWithoutListener(Strings.nullToEmpty(value));
		textChanged();
	}

	@Override
	protected void textChanged() {
		value = Strings.emptyToNull(getText().trim());
		valueValid = value != null || isOptional();
		super.textChanged();
	}
}
