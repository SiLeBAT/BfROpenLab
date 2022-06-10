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

import java.awt.Color;
import java.util.stream.Stream;

import javax.swing.JTextField;
import javax.swing.event.DocumentListener;

import de.bund.bfr.knime.UI;

public abstract class TypedTextField extends JTextField implements TextInput {

	private static final long serialVersionUID = 1L;

	private static final Color INVALID_COLOR = Color.RED;
	private static final Color BACKGROUND_COLOR;
	private static final Color FOREGROUND_COLOR;
	private static final Color NOT_ENABLED_BACKGROUND_COLOR;
	private static final Color NOT_ENABLED_FOREGROUND_COLOR;

	static {
		JTextField field = new JTextField();

		BACKGROUND_COLOR = field.getBackground();
		FOREGROUND_COLOR = field.getForeground();

		field.setEnabled(false);

		NOT_ENABLED_BACKGROUND_COLOR = field.getBackground();
		NOT_ENABLED_FOREGROUND_COLOR = field.getForeground();
	}

	private DocumentListener documentListener;
	private boolean optional;
	protected boolean valueValid;

	public TypedTextField(boolean optional, int columns) {
		super(columns);

		this.optional = optional;
		valueValid = true;
		getDocument().addDocumentListener(documentListener = UI.newDocumentActionListener(e -> textChanged()));
	}

	@Override
	public abstract Object getValue();

	@Override
	public boolean isOptional() {
		return optional;
	}

	@Override
	public boolean isValueValid() {
		return valueValid;
	}

	@Override
	public void addTextListener(TextListener listener) {
		listenerList.add(TextListener.class, listener);
	}

	@Override
	public void removeTextListener(TextListener listener) {
		listenerList.remove(TextListener.class, listener);
	}

	@Override
	public Color getForeground() {
		if (isEnabled()) {
			return !valueValid ? INVALID_COLOR : FOREGROUND_COLOR;
		} else {
			return NOT_ENABLED_FOREGROUND_COLOR;
		}
	}

	@Override
	public Color getBackground() {
		if (isEnabled()) {
			boolean hasText = getDocument() != null && !getText().trim().isEmpty();

			return !valueValid && !hasText ? INVALID_COLOR : BACKGROUND_COLOR;
		} else {
			return NOT_ENABLED_BACKGROUND_COLOR;
		}
	}

	protected void textChanged() {
		Stream.of(getListeners(TextListener.class)).forEach(l -> l.textChanged(this));
	}

	protected void setTextWithoutListener(String text) {
		getDocument().removeDocumentListener(documentListener);
		setText(text);
		getDocument().addDocumentListener(documentListener);
	}
}
