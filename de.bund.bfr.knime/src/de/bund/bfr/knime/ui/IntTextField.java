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
package de.bund.bfr.knime.ui;

public class IntTextField extends TypedTextField {

	private static final long serialVersionUID = 1L;

	private int minValue;
	private int maxValue;

	private Integer value;

	public IntTextField(boolean optional, int columns) {
		super(optional, columns);
		this.minValue = Integer.MIN_VALUE;
		this.maxValue = Integer.MAX_VALUE;
		textChanged();
	}

	public void setMinValue(int minValue) {
		this.minValue = minValue;
		textChanged();
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
		textChanged();
	}

	@Override
	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		getDocument().removeDocumentListener(this);
		setText(value != null ? value.toString() : "");
		getDocument().addDocumentListener(this);
		textChanged();
	}

	@Override
	protected void textChanged() {
		if (getText().trim().isEmpty()) {
			value = null;
			valueValid = isOptional();
		} else {
			try {
				value = Integer.parseInt(getText());
				valueValid = value >= minValue && value <= maxValue;
			} catch (NumberFormatException e) {
				value = null;
				valueValid = false;
			}
		}

		super.textChanged();
	}
}
