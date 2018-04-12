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
package de.bund.bfr.knime.gis.geocode;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import org.geotools.filter.visitor.FixBBOXFilterVisitor;
import org.knime.base.node.preproc.joiner.UseSingleRowKeyFactory;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import com.google.common.collect.Lists;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.ui.ColumnComboBox;

/**
 * <code>NodeDialog</code> for the "Geocoding" Node.
 * 
 * @author Christian Thoens
 */
public class GeocodingNodeDialog extends NodeDialogPane {

	private GeocodingSettings set;

	private JComboBox<GeocodingSettings.Provider> providerBox;
	private JTextField delayField;
	private JComboBox<GeocodingSettings.Multiple> multipleBox;

	private ColumnComboBox addressBox;
	private ColumnComboBox streetBox;
	private ColumnComboBox cityBox;
	private ColumnComboBox zipBox;
	private ColumnComboBox countryCodeBox;
	private JTextField serverField;
	private JRadioButton singleLineAddressRadioButton; // for MapQuest
	private JRadioButton fiveBoxAddressRadioButton;    // for MapQuest
	//private ButtonGroup addressButtonGroup;

	private JPanel panel;

	/**
	 * New pane for configuring the Geocoding node.
	 */
	public GeocodingNodeDialog() {
		set = new GeocodingSettings();
		providerBox = new JComboBox<>(GeocodingSettings.Provider.values());
		providerBox.addActionListener(e -> updatePanel());
		delayField = new JTextField();
		multipleBox = new JComboBox<>(GeocodingSettings.Multiple.values());

		addressBox = new ColumnComboBox(false);
		streetBox = new ColumnComboBox(true);
		cityBox = new ColumnComboBox(true);
		zipBox = new ColumnComboBox(true);
		countryCodeBox = new ColumnComboBox(false);
		serverField = new JTextField();
		singleLineAddressRadioButton = new JRadioButton("Use single-line address");
		fiveBoxAddressRadioButton = new JRadioButton("Use box address");
		ButtonGroup buttonGroup = new ButtonGroup();
	    buttonGroup.add(singleLineAddressRadioButton);
	    buttonGroup.add(fiveBoxAddressRadioButton);

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		addTab("Options", UI.createNorthPanel(panel));
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
		addressBox.removeAllColumns();
		streetBox.removeAllColumns();
		cityBox.removeAllColumns();
		zipBox.removeAllColumns();
		countryCodeBox.removeAllColumns();

		for (DataColumnSpec column : IO.getColumns(specs[0], StringCell.TYPE)) {
			addressBox.addColumn(column);
			streetBox.addColumn(column);
			cityBox.addColumn(column);
			zipBox.addColumn(column);
			countryCodeBox.addColumn(column);
		}

		set.loadSettings(settings);

		providerBox.setSelectedItem(set.getServiceProvider());
		delayField.setText(set.getRequestDelay() + "");
		multipleBox.setSelectedItem(set.getMultipleResults());

		addressBox.setSelectedColumnName(set.getAddressColumn());
		streetBox.setSelectedColumnName(set.getStreetColumn());
		cityBox.setSelectedColumnName(set.getCityColumn());
		zipBox.setSelectedColumnName(set.getZipColumn());
		countryCodeBox.setSelectedColumnName(set.getCountryCodeColumn());
		singleLineAddressRadioButton.setSelected(set.getUseSingleLineAddress());
		fiveBoxAddressRadioButton.setSelected(!set.getUseSingleLineAddress());
		
		
		if (set.getServiceProvider() == GeocodingSettings.Provider.GISGRAPHY) {
			serverField.setText(set.getGisgraphyServer() != null ? set.getGisgraphyServer() : "");
		}
		else if (set.getServiceProvider() == GeocodingSettings.Provider.PHOTON) {
			serverField.setText(set.getPhotonServer() != null ? set.getPhotonServer() : "");
		}
		else {
			serverField.setText("");
		}

		updatePanel();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		if (delayField.getText().trim().isEmpty()) {
			throw new InvalidSettingsException("No Request Delay specified");
		}

		try {
			Integer.parseInt(delayField.getText());
		} catch (NumberFormatException e) {
			throw new InvalidSettingsException("Request Delay invalid");
		}
		
		
		//set.setAddressColumn(addressBox.getSelectedColumnName());
		set.setServiceProvider((GeocodingSettings.Provider) providerBox.getSelectedItem());
		set.setRequestDelay(Integer.parseInt(delayField.getText()));
        set.setMultipleResults((GeocodingSettings.Multiple) multipleBox.getSelectedItem());
        
		if (set.getServiceProvider() == GeocodingSettings.Provider.MAPQUEST) {
		  set.setUseSingleLineAddress(this.singleLineAddressRadioButton.isSelected());
		  if(set.getUseSingleLineAddress()) {
		    set.setAddressColumn(addressBox.getSelectedColumnName());
		  } else {
		    set.setStreetColumn(streetBox.getSelectedColumnName());
            set.setCityColumn(cityBox.getSelectedColumnName());
            set.setZipColumn(zipBox.getSelectedColumnName());
            set.setCountryCodeColumn(countryCodeBox.getSelectedColumnName());
		  }
		} else {
		  set.setAddressColumn(addressBox.getSelectedColumnName());
		}
		
		if (set.getServiceProvider() == GeocodingSettings.Provider.GISGRAPHY) {
			if (serverField.getText().trim().isEmpty()) {
				throw new InvalidSettingsException("No Server specified");
			}

			set.setCountryCodeColumn(countryCodeBox.getSelectedColumnName());
			set.setGisgraphyServer(serverField.getText().trim());
		} else if (set.getServiceProvider() == GeocodingSettings.Provider.PHOTON) {
			if (serverField.getText().trim().isEmpty()) {
				throw new InvalidSettingsException("No Server specified");
			}

			set.setPhotonServer(serverField.getText().trim());
		} else if (set.getServiceProvider() == GeocodingSettings.Provider.MAPQUEST) {
//			set.setStreetColumn(streetBox.getSelectedColumnName());
//			set.setCityColumn(cityBox.getSelectedColumnName());
//			set.setZipColumn(zipBox.getSelectedColumnName());
		}

		set.saveSettings(settings);
	}

	private void updatePanel() {
		List<JLabel> addressLabels = Lists.newArrayList(new JLabel("Address:"));
		List<ColumnComboBox> addressBoxes = Lists.newArrayList(addressBox);
		List<JLabel> otherLabels = Lists.newArrayList(new JLabel("Delay between Request (ms):"),
				new JLabel("When multiple Results:"));
		List<Component> otherFields = Lists.newArrayList(delayField, multipleBox);

		if (providerBox.getSelectedItem() == GeocodingSettings.Provider.GISGRAPHY) {
			addressLabels.add(new JLabel("Country Code:"));
			addressBoxes.add(countryCodeBox);
			otherLabels.add(0, new JLabel("Server Address:"));
			otherFields.add(0, serverField);
		} else if (providerBox.getSelectedItem() == GeocodingSettings.Provider.PHOTON) {
			otherLabels.add(0, new JLabel("Server Address:"));
			otherFields.add(0, serverField);
		} else if (providerBox.getSelectedItem() == GeocodingSettings.Provider.MAPQUEST) {
			addressLabels.add(new JLabel("Street:")); addressBoxes.add(streetBox);
			addressLabels.add(new JLabel("City:")); addressBoxes.add(cityBox);
			addressLabels.add(new JLabel("Zip:")); addressBoxes.add(zipBox);
			addressLabels.add(new JLabel("Country Code:")); addressBoxes.add(countryCodeBox);
		}

		panel.removeAll();
		panel.add(UI.createOptionsPanel("Provider", Arrays.asList(new JLabel("Service Provider")),
				Arrays.asList(providerBox)));
		
		//panel.add(UI.createOptionsPanel("Addresses", addressLabels, addressBoxes));
		if (providerBox.getSelectedItem() == GeocodingSettings.Provider.MAPQUEST) createMapQuestAddressPanel( addressLabels, addressBoxes);
		else panel.add(UI.createOptionsPanel("Addresses", addressLabels, addressBoxes));
		
		panel.add(UI.createOptionsPanel("Other Options", otherLabels, otherFields));
		panel.revalidate();
	}
	
	private void createMapQuestAddressPanel(List<? extends Component> leftComponents, List<? extends Component> rightComponents) {
	  GridBagLayout gridbag = new GridBagLayout();
	  GridBagConstraints c = new GridBagConstraints();
	  
	  JPanel innerPanel = new JPanel();
	  innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.PAGE_AXIS));
	  
	//setFont(new Font("Helvetica", Font.PLAIN, 14));
      innerPanel.setLayout(gridbag);
     
      c.gridwidth = GridBagConstraints.REMAINDER; //end of row
      //c.gridwidth = 1; // next component doesn't take up a whole row), you'll see the same instance variable reset to its default value:

      c.fill = GridBagConstraints.BOTH;
      c.weightx = 1.0;
      gridbag.setConstraints(singleLineAddressRadioButton, c);
      innerPanel.add(singleLineAddressRadioButton);
      
      c.weightx = 0.0;
      c.gridwidth = 1; 
      Component emptySpace = Box.createHorizontalStrut(10);
      gridbag.setConstraints(emptySpace, c);
      innerPanel.add(emptySpace);
      
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.weightx = 1.0;
      JPanel singleLinePanel = UI.createOptionsPanel(null, Arrays.asList(leftComponents.get(0)), Arrays.asList(rightComponents.get(0)));
      gridbag.setConstraints(singleLinePanel, c);
      innerPanel.add(singleLinePanel);
      
      leftComponents.remove(0);
      rightComponents.remove(0);
      
      //c.fill = GridBagConstraints.BOTH;
      c.weightx = 1.0;
      gridbag.setConstraints(fiveBoxAddressRadioButton, c);
      innerPanel.add(fiveBoxAddressRadioButton);
      
      c.weightx = 0.0;
      c.gridwidth = 1; 
      emptySpace = Box.createHorizontalStrut(10);
      gridbag.setConstraints(emptySpace, c);
      innerPanel.add(emptySpace);
      
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.weightx = 1.0;
      JPanel fiveBoxPanel = UI.createOptionsPanel(null, leftComponents, rightComponents);
      gridbag.setConstraints(fiveBoxPanel, c);
      innerPanel.add(fiveBoxPanel);
      
//      makebutton("Button1", gridbag, c);
//      makebutton("Button2", gridbag, c);
//      makebutton("Button3", gridbag, c);
//
//      c.gridwidth = GridBagConstraints.REMAINDER; //end of row
//      makebutton("Button4", gridbag, c);
//
//      c.weightx = 0.0;                   //reset to the default
//      makebutton("Button5", gridbag, c); //another row
//
//      c.gridwidth = GridBagConstraints.RELATIVE; //next to last in row
//      makebutton("Button6", gridbag, c);
//
//      c.gridwidth = GridBagConstraints.REMAINDER; //end of row
//      makebutton("Button7", gridbag, c);
//
//      c.gridwidth = 1;                      //reset to the default
//      c.gridheight = 2;
//      c.weighty = 1.0;
//      makebutton("Button8", gridbag, c);
//
//      c.weighty = 0.0;                   //reset to the default
//      c.gridwidth = GridBagConstraints.REMAINDER; //end of row
//      c.gridheight = 1;                   //reset to the default
//      makebutton("Button9", gridbag, c);
//      makebutton("Button10", gridbag, c);
//	  
//	  
//	  
//	  
//	  
//	  
//	  
//	    innerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//	    innerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);     //setHorizontalAlignment(SwingConstants.LEFT)
//	    //innerPanel.setLayout(new BorderLayout(5, 5));
//	    singleLineAddressRadioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
//	    singleLineAddressRadioButton.setHorizontalAlignment(SwingConstants.LEFT); 
//	    innerPanel.add(singleLineAddressRadioButton); //leftPanel, BorderLayout.CENTER);
//	    singleLineAddressRadioButton.setHorizontalAlignment(SwingConstants.LEFT); 
//	    singleLineAddressRadioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
//	    
//	    innerPanel.add(UI.createOptionsPanel(null, Arrays.asList(leftComponents.get(0)), Arrays.asList(rightComponents.get(0))));
//	    
//	    leftComponents.remove(0);
//	    rightComponents.remove(0);
//	      
//	    innerPanel.add(fiveBoxAddressRadioButton);
//	    fiveBoxAddressRadioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
//	    innerPanel.add(UI.createOptionsPanel(null, leftComponents, rightComponents));
	    
	  JPanel outerPanel = new JPanel();

	  outerPanel.setBorder(BorderFactory.createTitledBorder("Addresses"));
	  outerPanel.setLayout(new BorderLayout());
	  outerPanel.add(innerPanel, BorderLayout.CENTER);
//	  singleLineAddressRadioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
	  //return outerPanel;
	  
	  
	  
	  
//	  List<Component> singleLineComponents = new ArrayList<>();
//	  List<Component> boxComponents = new ArrayList<>();
//	  
//	  singleLineComponents.add(singleLineAddressRadioButton);
//	  singleLineComponents.add(createOptionsPanel(null, Arrays.asList(leftComponents.get(0),null,null), Arrays.asList(rightComponents.get(0),null,null), BorderLayout.CENTER));
//	  
//	  leftComponents.remove(0);
//	  rightComponents.remove(0);
//	  
//	  boxComponents.add(fiveBoxAddressRadioButton);
//	  boxComponents.add(UI.createOptionsPanel(null, leftComponents, rightComponents));
	  
	  //JPanel panel = UI.createOptionsPanel("Addresses", leftComponents, rightComponents);
	  
//	  JPanel panel2 = new JPanel();
//	  panel2.add(singleLineAddressRadioButton);
//	  panel2.add(fiveBoxAddressRadioButton);
//	  panel.add(panel2,0);
	  this.panel.add(outerPanel);
//	  singleLineAddressRadioButton.setHorizontalAlignment(SwingConstants.LEFT); 
//	  singleLineAddressRadioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
	}
	
//	public static JPanel createOptionsPanel(String name, List<? extends Component> leftComponents,
//        List<? extends Component> rightComponents) {
//    int n = leftComponents.size();
//
//    JPanel leftPanel = new JPanel();
//    JPanel rightPanel = new JPanel();
//
//    leftPanel.setLayout(new GridLayout(n, 1, 5, 5));
//    rightPanel.setLayout(new GridLayout(n, 1, 5, 5));
//
//    for (int i = 0; i < n; i++) {
//        if(leftComponents.get(i)!=null) leftPanel.add(leftComponents.get(i), 0, i);
//        if(rightComponents.get(i)!=null) rightPanel.add(rightComponents.get(i), 0, i);
//    }
//
//    JPanel innerPanel = new JPanel();
//
//    innerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//    innerPanel.setLayout(new BorderLayout(5, 5));
//    innerPanel.add(leftPanel, BorderLayout.CENTER);
//    innerPanel.add(rightPanel, constraints);
//
//    if (name == null) {
//        return innerPanel;
//    }
//
//    JPanel outerPanel = new JPanel();
//
//    outerPanel.setBorder(BorderFactory.createTitledBorder(name));
//    outerPanel.setLayout(new BorderLayout());
//    outerPanel.add(innerPanel, BorderLayout.CENTER);
//
//    return outerPanel;
//}
}
