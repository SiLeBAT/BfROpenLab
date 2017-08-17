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
package de.bund.bfr.knime.ui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class DoubleTextField extends TypedTextField implements FocusListener {

	private static final long serialVersionUID = 1L;

	private double minValue;
	private double maxValue;

	private Double value;

	public DoubleTextField(boolean optional, int columns) {
		super(optional, columns);
		this.minValue = Double.NEGATIVE_INFINITY;
		this.maxValue = Double.POSITIVE_INFINITY;
		addFocusListener(this);
		textChanged();
		formatText();
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
		textChanged();
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
		textChanged();
	}

	@Override
	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		setTextWithoutListener(value != null ? value.toString() : "");
		textChanged();
		formatText();
		setCaretPosition(0);
	}

	@Override
	public void focusGained(FocusEvent e) {
	}

	@Override
	public void focusLost(FocusEvent e) {
		formatText();
	}

	protected void formatText() {
		setTextWithoutListener(getText().replace(",", "."));
	}

	@Override
	protected void textChanged() {
		if (getText().trim().isEmpty()) {
			value = null;
			valueValid = isOptional();
		} else {
			try {
				value = Double.parseDouble(getText());
				valueValid = value >= minValue && value <= maxValue;
			} catch (NumberFormatException e) {
				value = null;
				valueValid = false;
			}
		}

		super.textChanged();
	}
}
