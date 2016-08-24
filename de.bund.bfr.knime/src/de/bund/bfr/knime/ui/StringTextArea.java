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
package de.bund.bfr.knime.ui;

import java.awt.Color;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.event.DocumentListener;

import com.google.common.base.Strings;

import de.bund.bfr.knime.UI;

public class StringTextArea extends JTextArea implements TextInput {

	private static final long serialVersionUID = 1L;

	private static final Color INVALID_COLOR = Color.RED;
	private static final Color BACKGROUND_COLOR;
	private static final Color FOREGROUND_COLOR;
	private static final Color NOT_ENABLED_BACKGROUND_COLOR;
	private static final Color NOT_ENABLED_FOREGROUND_COLOR;

	static {
		JTextArea area = new JTextArea();

		BACKGROUND_COLOR = area.getBackground();
		FOREGROUND_COLOR = area.getForeground();

		area.setEnabled(false);

		NOT_ENABLED_BACKGROUND_COLOR = area.getBackground();
		NOT_ENABLED_FOREGROUND_COLOR = area.getForeground();
	}

	private DocumentListener documentListener;
	private boolean optional;
	private boolean valueValid;
	private String value;

	public StringTextArea(boolean optional, int rows, int columns) {
		super(null, null, rows, columns);
		this.optional = optional;
		setLineWrap(true);
		setBorder(BorderFactory.createLoweredBevelBorder());
		getDocument().addDocumentListener(documentListener = UI.newDocumentActionListener(e -> textChanged()));
		textChanged();
	}

	@Override
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		getDocument().removeDocumentListener(documentListener);
		setText(Strings.nullToEmpty(value));
		getDocument().addDocumentListener(documentListener);
		textChanged();
	}

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

	private void textChanged() {
		value = Strings.emptyToNull(getText().trim());
		valueValid = value != null || optional;
		Stream.of(getListeners(TextListener.class)).forEach(l -> l.textChanged(this));
	}
}
