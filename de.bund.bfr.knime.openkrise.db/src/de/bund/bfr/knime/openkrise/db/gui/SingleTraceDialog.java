package de.bund.bfr.knime.openkrise.db.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.ui.AutoSuggestField;

public class SingleTraceDialog extends JDialog implements ActionListener {

	public static void main(String[] args)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Business b1 = new Business();

		b1.name = "Bäcker Müller";

		Business b2 = new Business();

		b2.name = "Bäcker Meier";

		Business b3 = new Business();

		b3.name = "Megashop";

		JFrame frame = new JFrame("Test");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 200);
		frame.setVisible(true);

		SingleTraceDialog dialog = new SingleTraceDialog(frame, Arrays.asList(b1, b2, b3), true);

		dialog.setVisible(true);
	}

	private static final long serialVersionUID = 1L;

	private boolean approved;
	private Business selected;

	private AutoSuggestField nameField;

	private JButton okButton;
	private JButton cancelButton;

	public SingleTraceDialog(Component parent, List<Business> businesses, boolean isForward) {
		super(SwingUtilities.getWindowAncestor(parent), "Business for " + (isForward ? "Forward" : "Back") + " Tracing",
				DEFAULT_MODALITY_TYPE);
		approved = false;
		selected = null;

		nameField = new AutoSuggestField(20);

		try {
			nameField.setPossibleValues(getPossibleValues(businesses, Business.class.getField("name")));
		} catch (NoSuchFieldException | SecurityException e) {
		}

		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		setLayout(new BorderLayout());
		add(UI.createHorizontalPanel(
				new JLabel("Select Business Types for " + (isForward ? "Forward tracing" : "Backtracing"))),
				BorderLayout.NORTH);
		add(UI.createWestPanel(UI.createHorizontalPanel(nameField)), BorderLayout.CENTER);
		add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)), BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(parent);
		setResizable(false);
	}

	public boolean isApproved() {
		return approved;
	}

	public Business getSelected() {
		return selected;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			approved = true;
			selected = null;
			dispose();
		} else if (e.getSource() == cancelButton) {
			dispose();
		}
	}

	private static Set<String> getPossibleValues(List<Business> businesses, Field f) {
		Set<String> values = new LinkedHashSet<>();

		for (Business b : businesses) {
			try {
				Object value = f.get(b);

				if (value instanceof String) {
					values.add((String) value);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
			}
		}

		return values;
	}

	private static List<Business> filterBusiness(List<Business> businesses, String type, String country, String state,
			String district, String city, String zipCode) {
		List<Business> result = new ArrayList<>();

		for (Business b : businesses) {
			if ((type == null || type.equals(b.type)) && (country == null || country.equals(b.country))
					&& (state == null || state.equals(b.state)) && (district == null || district.equals(b.district))
					&& (city == null || city.equals(b.city)) && (zipCode == null || zipCode.equals(b.zipCode))) {
				result.add(b);
			}
		}

		return result;
	}

	private static class Business {

		public String id;
		public String name;

		public String type;
		public String country;
		public String state;
		public String district;
		public String city;
		public String zipCode;
	}
}
