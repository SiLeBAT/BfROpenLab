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
/*
 * Created by JFormDesigner on Wed Mar 30 15:56:05 CEST 2011
 */

package org.hsh.bfr.db.gui.dbtable.editoren;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.hsh.bfr.db.DBKernel;
import org.hsh.bfr.db.MyLogger;
import org.hsh.bfr.db.gui.InfoBox;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.lsmp.djep.djep.DJep;
import org.lsmp.djep.djep.diffRules.MacroDiffRules;
import org.lsmp.djep.xjep.MacroFunction;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Armin Weiser
 */
class MyChartDialog extends JDialog {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private XYDataset dataset;
    private XYSeries series2;
    private String Datenpunkte = "";
    private String origDatenpunkte;
    
	MyChartDialog(JDialog owner, String Datenpunkte, String xAxis, String yAxis) {
		super(owner);
		this.Datenpunkte = Datenpunkte;
		origDatenpunkte = Datenpunkte;
		initComponents();

		series2 = new XYSeries("Datenpunkte");
		fillTable(xAxis, yAxis);
		dataset = createDataset();
		final JFreeChart chart = createChart(dataset, xAxis, yAxis);
        final ChartPanel chartPanel = new ChartPanel(chart);
        panel1.remove(button1);
		panel1.add(chartPanel, new CellConstraints().xywh(1, 1, 1, 1, CellConstraints.FILL, CellConstraints.FILL));
		this.setTitle(xAxis + "-" + yAxis + "-Profil");

		table1.getModel().addTableModelListener(
				new TableModelListener() {
					public void tableChanged(TableModelEvent e) {
				        dataset = createDataset();
					}
				}
		);
	}

	public String getDatenpunkte() {
		return Datenpunkte;
	}
    public static void main(final String[] args) {

    	JDialog f = new JDialog();
        final MyChartDialog demo = new MyChartDialog(f, null, "Temperatur", "Zeit");
        demo.pack();
        demo.setVisible(true);

    }

	private void button2ActionPerformed(ActionEvent e) {
		dispose();	
	}

	private void button3ActionPerformed(ActionEvent e) {
		Datenpunkte = origDatenpunkte;
		dispose();	
	}

	private void button4ActionPerformed(ActionEvent e) {
		String hilfe = "In der linken Spalte der Tabelle können einzelne Punkte für die x-Achse angegeben werden.\n" +
			"In der rechten Spalte die zugehörigen Werte.\n" +
			"Es ist möglich für die x-Achse Intervalle einzugeben mit '-' als Trennzeichen (z.B. 3-5).\n" +
			"Außerdem ist es möglich verschiedene Einheiten für Zeiten anzugeben, z.B. 3d 13h 13m 5s\n" +
			"Für die y-Achse können einfache Zahlen angegeben werden oder auch Funktionen (mit x als Variable),\n" +
			"z.B. x^2, 2*x, exp(x), log10(x), ln(x)";
		if (DBKernel.getLanguage().equals("en")) {
			hilfe = "In this table values for time (x-axis) and conditions (e.g. temperature; y-axis) can be entered.\n" +
					"Define time intervals using ‘-‘ (e.g. 3-5).\n" +
					"Assign time units using d (day), h (hour), m (minute) or s (second), e.g. 3d 13h 13m 5s.\n" +
					"Values on the y-axis may be numerical data or functions with x as variable, e.g. x^2, 2*x, exp(x), log10(x), ln(x).\n" +
					"Please note that it is necessary to use ‘*’ to indicate that x is multiplied.";
		}
		InfoBox ib = new InfoBox(this, hilfe, true, new Dimension(750, 300), null, true);
		ib.setVisible(true);    				  										        			
	}

    private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		splitPane1 = new JSplitPane();
		scrollPane1 = new JScrollPane();
		table1 = new JTable();
		panel1 = new JPanel();
		button1 = new JButton();
		panel2 = new JPanel();
		button2 = new JButton();
		button3 = new JButton();
		button4 = new JButton();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.setLayout(new FormLayout(
			"default:grow",
			"2*(default, $lgap), default"));

		//======== splitPane1 ========
		{
			splitPane1.setDividerLocation(200);

			//======== scrollPane1 ========
			{
				scrollPane1.setPreferredSize(new Dimension(200, 423));

				//---- table1 ----
				table1.setModel(new DefaultTableModel(
					new Object[][] {
						{"3.0", "5"},
						{null, null},
						{null, null},
						{null, null},
						{null, null},
						{null, null},
						{null, null},
						{null, null},
						{null, null},
						{null, null},
						{null, null},
						{null, null},
						{null, null},
						{null, null},
						{null, null},
						{null, null},
						{null, null},
						{null, null},
						{null, null},
						{null, null},
					},
					new String[] {
						"Zeit (h)", "Temperatur"
					}
				) {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;
					Class<?>[] columnTypes = new Class<?>[] {
						String.class, String.class
					};
					@Override
					public Class<?> getColumnClass(int columnIndex) {
						return columnTypes[columnIndex];
					}
				});
				{
					TableColumnModel cm = table1.getColumnModel();
					cm.getColumn(0).setPreferredWidth(100);
				}
				scrollPane1.setViewportView(table1);
			}
			splitPane1.setLeftComponent(scrollPane1);

			//======== panel1 ========
			{
				panel1.setLayout(new FormLayout(
					"default:grow",
					"fill:default:grow"));

				//---- button1 ----
				button1.setText("text");
				panel1.add(button1, cc.xy(1, 1));
			}
			splitPane1.setRightComponent(panel1);
		}
		contentPane.add(splitPane1, cc.xy(1, 1));

		//======== panel2 ========
		{
			panel2.setLayout(new FormLayout(
				"3*(default:grow, $lcgap), default:grow",
				"default"));
			((FormLayout)panel2.getLayout()).setColumnGroups(new int[][] {{1, 3, 5, 7}});

			//---- button2 ----
			button2.setText("OK");
			button2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button2ActionPerformed(e);
				}
			});
			panel2.add(button2, cc.xy(3, 1));

			//---- button3 ----
			button3.setText("Abbrechen");
			button3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button3ActionPerformed(e);
				}
			});
			panel2.add(button3, cc.xy(5, 1));

			//---- button4 ----
			button4.setText("Hilfe");
			button4.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button4ActionPerformed(e);
				}
			});
			panel2.add(button4, cc.xy(7, 1));
		}
		contentPane.add(panel2, cc.xy(1, 5));
		setSize(555, 390);
		setLocationRelativeTo(null);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JSplitPane splitPane1;
	private JScrollPane scrollPane1;
	private JTable table1;
	private JPanel panel1;
	private JButton button1;
	private JPanel panel2;
	private JButton button2;
	private JButton button3;
	private JButton button4;
	// JFormDesigner - End of variables declaration  //GEN-END:variables


	private void fillTable(String xAxis, String yAxis) {
		table1.getColumnModel().getColumn(0).setHeaderValue(xAxis);
		table1.getColumnModel().getColumn(1).setHeaderValue(yAxis);
		int row = 0;
        for (int i=0;i<table1.getModel().getRowCount();i++) {
        	table1.getModel().setValueAt(null, i, 0);
        	table1.getModel().setValueAt(null, i, 1);
        }
        if (Datenpunkte != null) {
    		StringTokenizer tok = new StringTokenizer(Datenpunkte, "\n");
    		while (tok.hasMoreTokens()) {
    			String vals = tok.nextToken();
    			int index = vals.indexOf("\t");
    			if (index > 0) {
        			table1.getModel().setValueAt(vals.substring(0, index), row, 0);
        			table1.getModel().setValueAt(vals.substring(index+1), row, 1);    				
    			}
    			else {
    				MyLogger.handleMessage("java.lang.StringIndexOutOfBoundsException - fillTable - index <= 0: " + vals + "\n" + Datenpunkte);
    			}
    			row++;
    			if (row == table1.getModel().getRowCount()) break;
    		}        	
        }
	}
    /**
     * Creates a sample dataset.
     * 
     * @return a sample dataset.
     */
    private XYDataset createDataset() {
        
    	series2.clear();
    	Datenpunkte = "";
        for (int i=0;i<table1.getModel().getRowCount();i++) {
        	Object o1 = table1.getModel().getValueAt(i, 0);
        	Object o2 = table1.getModel().getValueAt(i, 1);
        	if (o1 == null || o2 == null || o1.toString().trim().length() == 0 || o2.toString().trim().length() == 0) {
        		break;
        	}
        	else {
        		String interval = o1.toString().trim();
        		String function = o2.toString().trim();
        		Datenpunkte += "\n" + interval + "\t" + function;
        		Double tVal = getDouble(interval); 
        		Double val = getDouble(function); 
        		if (val != null && tVal != null) series2.add(tVal, val);   
        		else {
        			try {
						parseString(series2, function, interval); // z.B. sin(x)+2*cos(3*x)
					} catch (ParseException e) {
						e.printStackTrace();
					} 
        		}
        	}
        }
        if (Datenpunkte.length() > 0) Datenpunkte = Datenpunkte.substring(1);

        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series2);
                
        return dataset;
        
    }
    private Double getDouble(String val) {
    	Double result = null;
    	try {
    		if (val.indexOf("d") < 0  && val.indexOf("D") < 0) result = Double.parseDouble(val.trim());
    		else Double.parseDouble("wsd");
    	}
    	catch (Exception e) {
    		// es ist erlaubt d: Tage, h: Stunden, m: Minuten, s: Sekunden dranzuschreiben
    		try {
        		Double res = 0.0;
        		String tVal = val.trim();
        		if (tVal.length() > 0) {
            		int index = tVal.indexOf("d");  
            		if (index >= 0) {res += Double.parseDouble(tVal.substring(0, index).trim()) * 24; tVal = tVal.substring(index+1).trim();}   
            		index = tVal.indexOf("h");  
            		if (index >= 0) {res += Double.parseDouble(tVal.substring(0, index).trim()); tVal = tVal.substring(index+1).trim();}   
            		index = tVal.indexOf("m");  
            		if (index >= 0) {res += Double.parseDouble(tVal.substring(0, index).trim()) / 60; tVal = tVal.substring(index+1).trim();}   
            		index = tVal.indexOf("s");  
            		if (index >= 0) {res += Double.parseDouble(tVal.substring(0, index).trim()) / 60 / 60; tVal = tVal.substring(index+1).trim();}        			
        		}
        		if (tVal.length() == 0) result = res;
    		}
        	catch (Exception e1) {}
    	}
    	return result;
    }
    private void parseString(XYSeries series, String function, String interval) throws ParseException {
    	DJep parser = createParser();
    	
    	parser.addVariable("x", 0.0);
    	
        Node f = parser.parse(function);        
        
        int index = interval.indexOf("-", interval.charAt(0) == '-' ? 1 : 0);
        if (index > 0) {
        	Double min = getDouble(interval.substring(0,index));
        	Double max = getDouble(interval.substring(index + 1));
        	if (min != null && max != null) {
        		int numPoints = 50;
                double iv = (max - min) / numPoints;
                for (int i=0;i<numPoints+1;i++) {
                	double t = min+i*iv;
                	
                	parser.setVarValue("x", t);
                	
                	Object number = parser.evaluate(f);
                	
                	if (number instanceof Double) {
                		series.add(t, (Double) number);
                	}                	  
                }
        	}
        }
     }
    private static DJep createParser() {
		DJep parser = new DJep();

		parser.setAllowAssignment(true);
		parser.setAllowUndeclared(true);
		parser.setImplicitMul(true);
		parser.addStandardFunctions();
		parser.addStandardDiffRules();
		parser.removeVariable("x");

		try {
			parser.addFunction("log10", new MacroFunction("log10", 1,
					"ln(x)/ln(10)", parser));
			parser.addDiffRule(new MacroDiffRules(parser, "log10",
					"1/(x*ln(10))"));			
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return parser;
	}
    /**
     * Creates a chart.
     * 
     * @param dataset  the data for the chart.
     * 
     * @return a chart.
     */
    private JFreeChart createChart(final XYDataset dataset, String xAxis, String yAxis) {
        
        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
        		"",      // chart title
            xAxis,                      // x axis label
            yAxis,                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            false,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

//        final StandardLegend legend = (StandardLegend) chart.getLegend();
  //      legend.setDisplaySeriesShapes(true);
        
        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        //plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, false);
        plot.setRenderer(renderer);

        // change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        // OPTIONAL CUSTOMISATION COMPLETED.
                
        return chart;
        
    }
}
