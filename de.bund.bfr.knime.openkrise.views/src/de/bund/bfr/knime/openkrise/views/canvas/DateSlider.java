/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.EventListener;
import java.util.GregorianCalendar;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import de.bund.bfr.knime.UI;

public class DateSlider extends JPanel {

	private static final long serialVersionUID = 1L;

	private GregorianCalendar date;

	private JSlider slider;
	private JLabel dateLabel;
	private JCheckBox withoutDateBox;

	public DateSlider(GregorianCalendar from, GregorianCalendar to) {
		int max = getDifferenceInDays(from, to);

		date = from;
		slider = new JSlider(0, max, max);
		slider.addChangeListener(e -> updateDateLabel());
		slider.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				Arrays.asList(listenerList.getListeners(DateListener.class))
						.forEach(l -> l.configChanged(DateSlider.this));
			}
		});
		dateLabel = new JLabel();
		updateDateLabel();
		withoutDateBox = new JCheckBox("Show Deliveries without Date", true);
		withoutDateBox.addItemListener(e -> Arrays.asList(listenerList.getListeners(DateListener.class))
				.forEach(l -> l.configChanged(DateSlider.this)));

		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());
		add(slider, BorderLayout.CENTER);
		add(UI.createHorizontalPanel(withoutDateBox, dateLabel), BorderLayout.WEST);
	}

	public void addDateListener(DateListener listener) {
		listenerList.add(DateListener.class, listener);
	}

	public void removeDateListener(DateListener listener) {
		listenerList.remove(DateListener.class, listener);
	}

	public GregorianCalendar getDate() {
		return addDaysTo(date, slider.getValue());
	}

	public boolean isShowDeliveriesWithoutDate() {
		return withoutDateBox.isSelected();
	}

	private void updateDateLabel() {
		dateLabel.setText(new SimpleDateFormat("yyyy-MM-dd").format(addDaysTo(date, slider.getValue()).getTime()));
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
