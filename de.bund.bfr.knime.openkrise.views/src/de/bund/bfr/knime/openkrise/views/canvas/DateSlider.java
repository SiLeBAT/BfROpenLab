/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EventListener;
import java.util.GregorianCalendar;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class DateSlider extends JPanel {

	private static final long serialVersionUID = 1L;

	private GregorianCalendar date;

	private JSlider slider;
	private JLabel dateLabel;
	private JCheckBox withoutDateBox;

	private boolean mouseDown;

	public DateSlider(GregorianCalendar from, GregorianCalendar to) {
		int max = getDifferenceInDays(from, to);

		mouseDown = false;
		date = from;
		slider = new JSlider(0, max, max);
		slider.addChangeListener(e -> {
			updateDateLabel();

			if (!mouseDown) {
				configChanged();
			}
		});
		slider.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					mouseDown = true;
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					mouseDown = false;
				}

				configChanged();
			}
		});
		dateLabel = new JLabel();
		updateDateLabel();
		withoutDateBox = new JCheckBox("Omit Deliveries without Date", false);
		withoutDateBox.addItemListener(e -> configChanged());

		JPanel leftPanel = new JPanel();

		leftPanel.setLayout(new BorderLayout(10, 0));
		leftPanel.add(withoutDateBox, BorderLayout.WEST);
		leftPanel.add(dateLabel, BorderLayout.CENTER);

		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());
		add(slider, BorderLayout.CENTER);
		add(leftPanel, BorderLayout.WEST);
	}

	public void addDateListener(DateListener listener) {
		listenerList.add(DateListener.class, listener);
	}

	public void removeDateListener(DateListener listener) {
		listenerList.remove(DateListener.class, listener);
	}

	public GregorianCalendar getShowToDate() {
		return slider.getValue() != slider.getMaximum() ? addDaysTo(date, slider.getValue()) : null;
	}

	public void setShowToDate(GregorianCalendar showToDate) {
		slider.setValue(showToDate != null ? getDifferenceInDays(date, showToDate) : slider.getMaximum());
		configChanged();
	}

	public boolean isShowDeliveriesWithoutDate() {
		return !withoutDateBox.isSelected();
	}

	public void setShowDeliveriesWithoutDate(boolean showDeliveriesWithoutDate) {
		withoutDateBox.setSelected(!showDeliveriesWithoutDate);
	}

	private void updateDateLabel() {
		dateLabel.setText("Show Deliveries until: "
				+ new SimpleDateFormat("yyyy-MM-dd").format(addDaysTo(date, slider.getValue()).getTime()));
	}

	private void configChanged() {
		Stream.of(getListeners(DateListener.class)).forEach(l -> l.configChanged(this));
	}

	private static GregorianCalendar addDaysTo(GregorianCalendar c, int days) {
		GregorianCalendar newC = new GregorianCalendar(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
				c.get(Calendar.DAY_OF_MONTH));

		newC.add(Calendar.DAY_OF_MONTH, days);

		return newC;
	}

	private static int getDifferenceInDays(GregorianCalendar c1, GregorianCalendar c2) {
		if (c2.before(c1)) {
			return 0;
		}

		GregorianCalendar c = new GregorianCalendar(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH),
				c1.get(Calendar.DAY_OF_MONTH));

		for (int i = 0;; i++) {
			if (equal(c, c2)) {
				return i;
			}

			c.add(Calendar.DAY_OF_MONTH, 1);
		}
	}

	private static boolean equal(GregorianCalendar c1, GregorianCalendar c2) {
		return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
				&& c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
	}

	public static interface DateListener extends EventListener {

		void configChanged(DateSlider source);
	}
}
