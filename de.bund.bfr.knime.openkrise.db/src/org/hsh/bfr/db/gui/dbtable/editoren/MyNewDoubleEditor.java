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
 * Created by JFormDesigner on Fri Feb 11 14:27:53 CET 2011
 */

package org.hsh.bfr.db.gui.dbtable.editoren;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.hsh.bfr.db.DBKernel;
import org.hsh.bfr.db.MyLogger;
import org.hsh.bfr.db.MyTable;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Armin Weiser
 */
public class MyNewDoubleEditor extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean inited = false;
	private Object oldValue;
	private String spaltenName;
	private Double newValue = null;
	private boolean savePressed = false;
	
	public MyNewDoubleEditor(final Object value, final String spaltenName, final int x, final int y, final char ch) {
		this.oldValue = value;
		this.spaltenName = spaltenName;
		savePressed = false;
		initComponents();
		this.setTitle(spaltenName);
		//int w = this.getPreferredSize().width;
		//int h = this.getPreferredSize().height;
		int w = this.getSize().width;
		int h = this.getSize().height;
		
		int newX = x - w / 2;
		int newY = y - h / 2;
		if (newX < 0) {
			newX = 0;
		}
		if (newY < 0) {
			newY = 0;
		}
	    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	    if (newX + w > screen.width) {
			newX = screen.width - w;
		}
	    if (newY + h > screen.height) {
			newY = screen.height - h;
		}
	  //System.out.println(newX + "\t" + newY + "\t" + x + "\t" + y + "\t" + w + "\t" + h + "\t" + this.getSize().width + "\t" + this.getSize().height);
		this.setLocation(newX, newY);
		this.setIconImage(new ImageIcon(getClass().getResource("/org/hsh/bfr/db/gui/res/Database.gif")).getImage()); //  SiLeBAT.gif
		readValues();
		if (DBKernel.isDouble(""+ch)) {
			textField1.setText(""+ch);
			inited = true;
		}
	}

	private void thisKeyTyped(final KeyEvent e) {
	  	char ch = e.getKeyChar();
	  	if (ch == KeyEvent.VK_ENTER) {
	  		saveNdispose();
		}
	  	else if (ch == KeyEvent.VK_ESCAPE) {
	  		dispose();	
	  	}
	  	else {
		  	if (e.getSource() instanceof JTextField) {
		  		JTextField tf = (JTextField) e.getSource();
		  		if (tf != null && tf != textField9) { // textField9 ist die Verteilung
		  			String text = tf.getText();
		  			int cp = tf.getCaretPosition();
		  			if (ch == ',') {
		  				ch = '.';
		  				e.setKeyChar('.');
		  			}
		  			text = text.substring(0, cp) + ch + text.substring(cp);
		  			if (!DBKernel.isDouble(text))
					 {
						e.consume();
						//System.out.println(tf.getCaretPosition() + "\t" + (tf == textField10) + "\t" + DBKernel.isDouble(text) + "\t" + text);	  			
					}
		  		}
		  	}	  		
	  	}
	}

	private void button1ActionPerformed(final ActionEvent e) {
		saveNdispose();
	}
	private void saveNdispose() {
		newValue = saveValues();	
		savePressed = true;
		dispose();	
	}
	public Double getNewValue() {
		return newValue;
	}
	public boolean savePressed() {
		return savePressed;
	}

	private void button2ActionPerformed(final ActionEvent e) {
		dispose();	
	}

	private void textField1FocusGained(final FocusEvent e) {
		if (inited) {
			textField1.select(0, 0);
			textField1.setCaretPosition(textField1.getText().length());
			inited = false;
		}
	}

	private void button10ActionPerformed(final ActionEvent e) {
		openCD(button10, "Zeit (h)");
	}

	private void button11ActionPerformed(final ActionEvent e) {
		openCD(button11, comboBox11.getSelectedItem().toString());
	}
	private void openCD(final JButton bt, final String xAxis) {
		
        MyChartDialog mcd = new MyChartDialog(this, bt.getToolTipText(), xAxis, spaltenName);
        mcd.setModal(true);
        //mcd.pack();
        mcd.setVisible(true);
        bt.setToolTipText(mcd.getDatenpunkte());
		
	}

	private void manageForeign(final JTextField tf, final Object id, final String foreignVal) {
		if (id == null) {
			tf.setText(null);
			tf.setToolTipText(null);
		}
		else {
			tf.setText(foreignVal);
			tf.setToolTipText(id.toString());
		}
	}
	private String getForeignVal(final String tablename, final int id, final String fields) {
		String result = "";
		String sql= "SELECT " + fields + " FROM " + DBKernel.delimitL(tablename) + " WHERE " + DBKernel.delimitL("ID") + "=" + id;
		ResultSet rs = DBKernel.getResultSet(sql, false);
		try {
			if (rs != null && rs.first()) {
				boolean isDbl = fields.startsWith(DBKernel.delimitL("Wert") + ",");
				for (int i=1;i<=rs.getMetaData().getColumnCount();i++) {
					if (rs.getObject(i) != null) {
						if (!result.isEmpty()) {
							result += "; ";
						}
						if (isDbl) {
							result += rs.getMetaData().getColumnName(i) + ": ";
						}
						if (rs.getMetaData().getColumnType(i) == java.sql.Types.DOUBLE) {
							result += DBKernel.getDoubleStr(rs.getDouble(i));
						} else {
							result += rs.getString(i);
						}
					}
				}
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	private Integer getInt(final String value) {
		Integer result = null;
		try {
			result = Integer.parseInt(value);
		}
		catch (Exception e) {}
		return result;
	}

	private void button13ActionPerformed(final ActionEvent e) {
		MyTable myFT = DBKernel.myDBi.getTable("Literatur"); // in case Reference Table is there
		if (myFT != null) {
			Object newVal = DBKernel.mainFrame.openNewWindow(myFT, getInt(textField13.getToolTipText()), "Literatur", null, "", null, this);
			//if (newVal != null) textField13.setToolTipText(newVal.toString());
			if (newVal != null) manageForeign(textField13, newVal, getForeignVal("Literatur", getInt(newVal == null ? null : newVal.toString()), DBKernel.delimitL("Erstautor") + "," + DBKernel.delimitL("Jahr")));
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label000 = new JLabel();
		label111 = new JLabel();
		label1 = new JLabel();
		textField1 = new JTextField();
		textField1_exp = new JTextField();
		checkBox1 = new JCheckBox();
		label2 = new JLabel();
		comboBox2 = new JComboBox<>();
		label3 = new JLabel();
		textField3 = new JTextField();
		checkBox3 = new JCheckBox();
		label4 = new JLabel();
		textField4 = new JTextField();
		textField4_exp = new JTextField();
		checkBox4 = new JCheckBox();
		label5 = new JLabel();
		textField5 = new JTextField();
		textField5_exp = new JTextField();
		checkBox5 = new JCheckBox();
		label6 = new JLabel();
		textField6 = new JTextField();
		textField6_exp = new JTextField();
		checkBox6 = new JCheckBox();
		label7 = new JLabel();
		textField7 = new JTextField();
		textField7_exp = new JTextField();
		checkBox7 = new JCheckBox();
		label8 = new JLabel();
		textField8 = new JTextField();
		textField8_exp = new JTextField();
		checkBox8 = new JCheckBox();
		label9 = new JLabel();
		textField9 = new JTextField();
		checkBox9 = new JCheckBox();
		label10 = new JLabel();
		button10 = new JButton();
		checkBox10 = new JCheckBox();
		label11 = new JLabel();
		button11 = new JButton();
		comboBox11 = new JComboBox<>();
		label12 = new JLabel();
		checkBox12 = new JCheckBox();
		label13 = new JLabel();
		textField13 = new JTextField();
		button13 = new JButton();
		panel1 = new JPanel();
		button1 = new JButton();
		button2 = new JButton();

		//======== this ========
		setAlwaysOnTop(true);
		setResizable(false);
		setModal(true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		Container contentPane = getContentPane();
		contentPane.setLayout(new FormLayout(
			"default, $lcgap, default:grow, $lcgap, default, $lcgap, center:default",
			"14*(default, $lgap), default"));

		//---- label000 ----
		label000.setText("Exponent");
		label000.setToolTipText("Exponent zur Basis 10, falls vorhanden\\nBeispiel 1.3*10^-4 : 1.3 wird in der Spalte 'Wert' eingetragen und -4 in dieser Spalte");
		contentPane.add(label000, CC.xy(5, 1));

		//---- label111 ----
		label111.setText("gesch\u00e4tzt");
		contentPane.add(label111, CC.xy(7, 1));

		//---- label1 ----
		label1.setText("Wert");
		contentPane.add(label1, CC.xy(1, 3));

		//---- textField1 ----
		textField1.setPreferredSize(new Dimension(80, 20));
		textField1.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		textField1.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				textField1FocusGained(e);
			}
		});
		contentPane.add(textField1, CC.xy(3, 3));

		//---- textField1_exp ----
		textField1_exp.setToolTipText("Exponent zur Basis 10, falls vorhanden\\nBeispiel 1.3*10^-4 : 1.3 wird in der Spalte 'Wert' eingetragen und -4 in dieser Spalte");
		textField1_exp.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(textField1_exp, CC.xy(5, 3));

		//---- checkBox1 ----
		checkBox1.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(checkBox1, CC.xy(7, 3));

		//---- label2 ----
		label2.setText("Wert_typ");
		contentPane.add(label2, CC.xy(1, 5));

		//---- comboBox2 ----
		comboBox2.setModel(new DefaultComboBoxModel<>(new String[] {
			"Einzelwert",
			"Mittelwert",
			"Median"
		}));
		comboBox2.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(comboBox2, CC.xy(3, 5));

		//---- label3 ----
		label3.setText("Wiederholungen");
		label3.setToolTipText("Anzahl der Wiederholungsmessungen/technischen Replikate f\u00fcr diesen Wert. \nACHTUNG: gemeint ist hier die Anzahl der Messungen insgesamt!!!");
		contentPane.add(label3, CC.xy(1, 7));

		//---- textField3 ----
		textField3.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(textField3, CC.xy(3, 7));

		//---- checkBox3 ----
		checkBox3.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(checkBox3, CC.xy(7, 7));

		//---- label4 ----
		label4.setText("Standardabweichung");
		label4.setToolTipText("Standardabweichung des gemessenen Wertes - Eintrag nur bei Mehrfachmessungen m\u00f6glich");
		contentPane.add(label4, CC.xy(1, 9));

		//---- textField4 ----
		textField4.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(textField4, CC.xy(3, 9));

		//---- textField4_exp ----
		textField4_exp.setToolTipText("Exponent zur Basis 10, falls vorhanden\\nBeispiel 1.3*10^-4 : 1.3 wird in der Spalte 'Wert' eingetragen und -4 in dieser Spalte");
		textField4_exp.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(textField4_exp, CC.xy(5, 9));

		//---- checkBox4 ----
		checkBox4.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(checkBox4, CC.xy(7, 9));

		//---- label5 ----
		label5.setText("Minimum");
		contentPane.add(label5, CC.xy(1, 11));

		//---- textField5 ----
		textField5.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(textField5, CC.xy(3, 11));

		//---- textField5_exp ----
		textField5_exp.setToolTipText("Exponent zur Basis 10, falls vorhanden\\nBeispiel 1.3*10^-4 : 1.3 wird in der Spalte 'Wert' eingetragen und -4 in dieser Spalte");
		textField5_exp.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(textField5_exp, CC.xy(5, 11));

		//---- checkBox5 ----
		checkBox5.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(checkBox5, CC.xy(7, 11));

		//---- label6 ----
		label6.setText("Maximum");
		contentPane.add(label6, CC.xy(1, 13));

		//---- textField6 ----
		textField6.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(textField6, CC.xy(3, 13));

		//---- textField6_exp ----
		textField6_exp.setToolTipText("Exponent zur Basis 10, falls vorhanden\\nBeispiel 1.3*10^-4 : 1.3 wird in der Spalte 'Wert' eingetragen und -4 in dieser Spalte");
		textField6_exp.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(textField6_exp, CC.xy(5, 13));

		//---- checkBox6 ----
		checkBox6.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(checkBox6, CC.xy(7, 13));

		//---- label7 ----
		label7.setText("LCL95");
		label7.setToolTipText("Untere Konfidenzgrenze des gemessenen Wertes (95%-KI) - Eintrag nur bei Mehrfachmessungen m\u00f6glich");
		contentPane.add(label7, CC.xy(1, 15));

		//---- textField7 ----
		textField7.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(textField7, CC.xy(3, 15));

		//---- textField7_exp ----
		textField7_exp.setToolTipText("Exponent zur Basis 10, falls vorhanden\\nBeispiel 1.3*10^-4 : 1.3 wird in der Spalte 'Wert' eingetragen und -4 in dieser Spalte");
		textField7_exp.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(textField7_exp, CC.xy(5, 15));

		//---- checkBox7 ----
		checkBox7.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(checkBox7, CC.xy(7, 15));

		//---- label8 ----
		label8.setText("UCL95");
		label8.setToolTipText("Obere Konfidenzgrenze des gemessenen Wertes (95%-KI) - Eintrag nur bei Mehrfachmessungen m\u00f6glich");
		contentPane.add(label8, CC.xy(1, 17));

		//---- textField8 ----
		textField8.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(textField8, CC.xy(3, 17));

		//---- textField8_exp ----
		textField8_exp.setToolTipText("Exponent zur Basis 10, falls vorhanden\\nBeispiel 1.3*10^-4 : 1.3 wird in der Spalte 'Wert' eingetragen und -4 in dieser Spalte");
		textField8_exp.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(textField8_exp, CC.xy(5, 17));

		//---- checkBox8 ----
		checkBox8.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(checkBox8, CC.xy(7, 17));

		//---- label9 ----
		label9.setText("Verteilung");
		label9.setToolTipText("Verteilung der Werte bei Mehrfachmessungen, z.B. Normalverteilung. Anzugeben ist die entsprechende Funktion in R, z.B. rnorm(n, mean = 0, sd = 1) ");
		contentPane.add(label9, CC.xy(1, 19));

		//---- textField9 ----
		textField9.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(textField9, CC.xy(3, 19));

		//---- checkBox9 ----
		checkBox9.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				thisKeyTyped(e);
			}
		});
		contentPane.add(checkBox9, CC.xy(7, 19));

		//---- label10 ----
		label10.setText("Funktion (Zeit)");
		label10.setToolTipText("\"Parameter\"/Zeit-Profil. Funktion des Parameters in Abh\u00e4ngigkeit von der Zeit.");
		contentPane.add(label10, CC.xy(1, 21));

		//---- button10 ----
		button10.setText("Starte Editor");
		button10.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button10ActionPerformed(e);
			}
		});
		contentPane.add(button10, CC.xy(3, 21));
		contentPane.add(checkBox10, CC.xy(7, 21));

		//---- label11 ----
		label11.setText("Funktion (x)");
		label11.setToolTipText("\"Parameter\"/?-Profil. Funktion des Parameters in Abh\u00e4ngigkeit des anzugebenden ?-Parameters.");
		contentPane.add(label11, CC.xy(1, 23));

		//---- button11 ----
		button11.setText("Starte Editor");
		button11.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button11ActionPerformed(e);
			}
		});
		contentPane.add(button11, CC.xy(3, 23));

		//---- comboBox11 ----
		comboBox11.setModel(new DefaultComboBoxModel<>(new String[] {
			"\u00b0C",
			"pH",
			"aw",
			"CO2",
			"Druck"
		}));
		contentPane.add(comboBox11, CC.xy(7, 23, CC.FILL, CC.DEFAULT));

		//---- label12 ----
		label12.setText("Undefiniert (n.d.)");
		contentPane.add(label12, CC.xy(1, 25));
		contentPane.add(checkBox12, CC.xy(3, 25));

		//---- label13 ----
		label13.setText("Referenz");
		contentPane.add(label13, CC.xy(1, 27));

		//---- textField13 ----
		textField13.setEditable(false);
		contentPane.add(textField13, CC.xywh(3, 27, 3, 1));

		//---- button13 ----
		button13.setText("insert");
		button13.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button13ActionPerformed(e);
			}
		});
		contentPane.add(button13, CC.xy(7, 27));

		//======== panel1 ========
		{
			panel1.setLayout(new FormLayout(
				"2*(default:grow, $lcgap), default:grow",
				"default"));
			((FormLayout)panel1.getLayout()).setColumnGroups(new int[][] {{1, 3, 5}});

			//---- button1 ----
			button1.setText("OK");
			button1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}
			});
			panel1.add(button1, CC.xy(3, 1));

			//---- button2 ----
			button2.setText("Abbrechen");
			button2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button2ActionPerformed(e);
				}
			});
			panel1.add(button2, CC.xy(5, 1));
		}
		contentPane.add(panel1, CC.xywh(1, 29, 7, 1));
		setSize(380, 440);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel label000;
	private JLabel label111;
	private JLabel label1;
	private JTextField textField1;
	private JTextField textField1_exp;
	private JCheckBox checkBox1;
	private JLabel label2;
	private JComboBox<String> comboBox2;
	private JLabel label3;
	private JTextField textField3;
	private JCheckBox checkBox3;
	private JLabel label4;
	private JTextField textField4;
	private JTextField textField4_exp;
	private JCheckBox checkBox4;
	private JLabel label5;
	private JTextField textField5;
	private JTextField textField5_exp;
	private JCheckBox checkBox5;
	private JLabel label6;
	private JTextField textField6;
	private JTextField textField6_exp;
	private JCheckBox checkBox6;
	private JLabel label7;
	private JTextField textField7;
	private JTextField textField7_exp;
	private JCheckBox checkBox7;
	private JLabel label8;
	private JTextField textField8;
	private JTextField textField8_exp;
	private JCheckBox checkBox8;
	private JLabel label9;
	private JTextField textField9;
	private JCheckBox checkBox9;
	private JLabel label10;
	private JButton button10;
	private JCheckBox checkBox10;
	private JLabel label11;
	private JButton button11;
	private JComboBox<String> comboBox11;
	private JLabel label12;
	private JCheckBox checkBox12;
	private JLabel label13;
	private JTextField textField13;
	private JButton button13;
	private JPanel panel1;
	private JButton button1;
	private JButton button2;
	// JFormDesigner - End of variables declaration  //GEN-END:variables


	private void readValues() {
		textField1.setText(""); textField1_exp.setText(""); checkBox1.setSelected(false);
		comboBox2.setSelectedIndex(0);
		textField3.setText(""); checkBox3.setSelected(false);
		textField4.setText(""); textField4_exp.setText(""); checkBox4.setSelected(false);
		textField5.setText(""); textField5_exp.setText(""); checkBox5.setSelected(false);
		textField6.setText(""); textField6_exp.setText(""); checkBox6.setSelected(false);
		textField7.setText(""); textField7_exp.setText(""); checkBox7.setSelected(false);
		textField8.setText(""); textField8_exp.setText(""); checkBox8.setSelected(false);
		textField9.setText(""); checkBox9.setSelected(false);
		button10.setToolTipText(""); checkBox10.setSelected(false);
		button11.setToolTipText(""); comboBox11.setSelectedIndex(0);
		checkBox12.setSelected(false);
		if (oldValue != null) {
			String sql = "SELECT * FROM " + DBKernel.delimitL("DoubleKennzahlen") + " WHERE " + DBKernel.delimitL("ID") + "=" + oldValue;			
			try {
				ResultSet rs = DBKernel.getResultSet(sql, false);
				if (rs != null && rs.first()) {		
					// "Einzelwert","Einzelwert_g","Wiederholungen","Wiederholungen_g","Mittelwert","Mittelwert_g","Standardabweichung","Standardabweichung_g","Median","Median_g","Minimum","Minimum_g","Maximum","Maximum_g","LCL95","LCL95_g","UCL95","UCL95_g","Verteilung","Verteilung_g",
					readValue(rs, label1, textField1, textField1_exp, checkBox1);
					Integer typ = rs.getInt("Wert_typ");
					if (typ != null && typ == 2) {
						comboBox2.setSelectedItem("Mittelwert");
					} else if (typ != null && typ == 3) {
						comboBox2.setSelectedItem("Median");
					} else {
						comboBox2.setSelectedItem("Einzelwert");
					}
					
						readValue(rs, label3, textField3, null, checkBox3);
						readValue(rs, label4, textField4, textField4_exp, checkBox4);
						readValue(rs, label5, textField5, textField5_exp, checkBox5);
						readValue(rs, label6, textField6, textField6_exp, checkBox6);
						readValue(rs, label7, textField7, textField7_exp, checkBox7);
						readValue(rs, label8, textField8, textField8_exp, checkBox8);
						readValue(rs, label9, textField9, null, checkBox9, true);
						// "Funktion (Zeit)","Funktion (Zeit)_g"
						try {
							String str = rs.getString(label10.getText());
							if (str != null) {
								button10.setToolTipText(str);
								boolean ass = rs.getBoolean(label10.getText() + "_g");
								checkBox10.setSelected(ass);					
							}
						}
						catch (Exception e) {MyLogger.handleException(e);}
						
						// "Funktion (x)","x","Funktion (x)_g"
						String str = rs.getString(label11.getText());
						if (str != null) {
							button11.setToolTipText(str);
							//boolean ass = rs.getBoolean(label11.getText() + "_g");
							String comboVal = rs.getString("x") == null ? "" : rs.getString("x");
							for (int i=0;i<comboBox11.getItemCount();i++) {
								if (comboBox11.getItemAt(i).equals(comboVal)) {
									comboBox11.setSelectedIndex(i);
									break;
								}
							}						
						}
						
						// Undefiniert (n.d.)
						try {
							boolean nd = rs.getBoolean(label12.getText());
							checkBox12.setSelected(nd);
						}
						catch (Exception e) {MyLogger.handleException(e);}
						
						manageForeign(textField13, rs.getObject("Referenz"), getForeignVal("Literatur", rs.getInt("Referenz"), DBKernel.delimitL("Erstautor") + "," + DBKernel.delimitL("Jahr")));
				}
			}
			catch (Exception e) {
				MyLogger.handleException(e);
			}
		}
	}
	private void readValue(final ResultSet rs, final JLabel jl, final JTextField tf, final JTextField tf_exp, final JCheckBox cb) {
		readValue(rs, jl, tf, tf_exp, cb, false);
	}
	private void readValue(final ResultSet rs, final JLabel jl, final JTextField tf, final JTextField tf_exp, final JCheckBox cb, final boolean isString) {
		try {
			String kz = jl.getText();
			String str = rs.getString(kz);
			if (str != null) {
				if (isString) {
					tf.setText(str);
				} else {
					tf.setText(DBKernel.getDoubleStr(rs.getDouble(kz)));
				}
				if (tf_exp != null) {
					str = rs.getString(kz.equals("Wert") ? "Exponent" : (kz + "_exp"));
					if (str != null) {
						tf_exp.setText(DBKernel.getDoubleStr(rs.getDouble(kz.equals("Wert") ? "Exponent" : (kz + "_exp"))));
					}
				}
				boolean ass = rs.getBoolean(kz + "_g");
				cb.setSelected(ass);					
			}
		}
		catch (Exception e) {MyLogger.handleException(e);}
	}

	private Double saveValues() {
		Double result = null;
		try {
			String sql = "";
			if (oldValue != null) {
						sql = "UPDATE " + DBKernel.delimitL("DoubleKennzahlen") +
		    		" SET " + DBKernel.delimitL("Wert") + "=?," + DBKernel.delimitL("Exponent") + "=?," + DBKernel.delimitL("Wert_g") + "=?," + DBKernel.delimitL("Wert_typ") + "=?," +
						DBKernel.delimitL("Wiederholungen") + "=?," + DBKernel.delimitL("Wiederholungen_g") + "=?, " + 
						DBKernel.delimitL("Standardabweichung") + "=?," + DBKernel.delimitL("Standardabweichung_exp") + "=?, " + DBKernel.delimitL("Standardabweichung_g") + "=?, " +
						DBKernel.delimitL("Minimum") + "=?, " + DBKernel.delimitL("Minimum_exp") + "=?, " + DBKernel.delimitL("Minimum_g") + "=?, " +
						DBKernel.delimitL("Maximum") + "=?," + DBKernel.delimitL("Maximum_exp") + "=?, " + DBKernel.delimitL("Maximum_g") + "=?, " +
						DBKernel.delimitL("LCL95") + "=?, " + DBKernel.delimitL("LCL95_exp") + "=?," + DBKernel.delimitL("LCL95_g") + "=?," +
						DBKernel.delimitL("UCL95") + "=?, " + DBKernel.delimitL("UCL95_exp") + "=?, " + DBKernel.delimitL("UCL95_g") + "=?, " +
						DBKernel.delimitL("Verteilung") + "=?," + DBKernel.delimitL("Verteilung_g") + "=?, " +
						DBKernel.delimitL("Funktion (Zeit)") + "=?, " + DBKernel.delimitL("Funktion (Zeit)_g") + "=?," +
						DBKernel.delimitL("Funktion (x)") + "=?, " + DBKernel.delimitL("x") + "=?, " + DBKernel.delimitL("Funktion (x)_g") + "=?," +
						DBKernel.delimitL("Undefiniert (n.d.)") + "=?, " + DBKernel.delimitL("Referenz") + "=?" +
						" WHERE " + DBKernel.delimitL("ID") + "=" + oldValue;
			}
			else {
				sql = "INSERT INTO " + DBKernel.delimitL("DoubleKennzahlen") +
    		" (" + DBKernel.delimitL("Wert") + "," + DBKernel.delimitL("Exponent") + "," + DBKernel.delimitL("Wert_g") + "," + DBKernel.delimitL("Wert_typ") + "," +
						DBKernel.delimitL("Wiederholungen") + "," + DBKernel.delimitL("Wiederholungen_g") + ", " +
						DBKernel.delimitL("Standardabweichung") + "," + DBKernel.delimitL("Standardabweichung_exp") + ", " + DBKernel.delimitL("Standardabweichung_g") + ", " +
						DBKernel.delimitL("Minimum") + ", " + DBKernel.delimitL("Minimum_exp") + ", " + DBKernel.delimitL("Minimum_g") + ", " +
						DBKernel.delimitL("Maximum") + "," + DBKernel.delimitL("Maximum_exp") + ", " + DBKernel.delimitL("Maximum_g") + ", " +
						DBKernel.delimitL("LCL95") + ", " + DBKernel.delimitL("LCL95_exp") + "," + DBKernel.delimitL("LCL95_g") + "," +
						DBKernel.delimitL("UCL95") + ", " + DBKernel.delimitL("UCL95_exp") + ", " + DBKernel.delimitL("UCL95_g") + ", " +
						DBKernel.delimitL("Verteilung") + "," + DBKernel.delimitL("Verteilung_g") + ", " +
						DBKernel.delimitL("Funktion (Zeit)") + ", " + DBKernel.delimitL("Funktion (Zeit)_g") + "," +
						DBKernel.delimitL("Funktion (x)") + ", " + DBKernel.delimitL("x") + ", " + DBKernel.delimitL("Funktion (x)_g") + "," +
						DBKernel.delimitL("Undefiniert (n.d.)") + ", " + DBKernel.delimitL("Referenz") +
				") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			}
      PreparedStatement ps = DBKernel.getDBConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			saveValue(ps, 1, textField1.getText(), textField1_exp, checkBox1);
			if (comboBox2.getSelectedItem().equals("Einzelwert")) {
				ps.setInt(4, 1);
			} else if (comboBox2.getSelectedItem().equals("Mittelwert")) {
				ps.setInt(4, 2);
			} else if (comboBox2.getSelectedItem().equals("Median")) {
				ps.setInt(4, 3);
			} else {
				ps.setNull(4, java.sql.Types.INTEGER);
			}
			saveValue(ps, 5, textField3.getText(), null, checkBox3);
			saveValue(ps, 7, textField4.getText(), textField4_exp, checkBox4);
			saveValue(ps, 10, textField5.getText(), textField5_exp, checkBox5);
			saveValue(ps, 13, textField6.getText(), textField6_exp, checkBox6);
			saveValue(ps, 16, textField7.getText(), textField7_exp, checkBox7);
			saveValue(ps, 19, textField8.getText(), textField8_exp, checkBox8);
			saveValue(ps, 22, textField9.getText(), null, checkBox9, true);

			if (button10.getToolTipText().trim().length() > 0) {
				ps.setString(24, button10.getToolTipText());
			} else {
				ps.setNull(24, java.sql.Types.VARCHAR);
			}
			if (checkBox10.isSelected()) {
				ps.setBoolean(25, true);
			} else {
				ps.setNull(25, java.sql.Types.BOOLEAN);
			}

			if (button11.getToolTipText().trim().length() > 0) {
				ps.setString(26, button11.getToolTipText());
				ps.setString(27, comboBox11.getSelectedItem().toString());
			}
			else {
				ps.setNull(26, java.sql.Types.VARCHAR);
				ps.setNull(27, java.sql.Types.VARCHAR);
			}
			ps.setNull(28, java.sql.Types.BOOLEAN); // Funktion (x)_g

			if (checkBox12.isSelected()) {
				ps.setBoolean(29, true);
			} else {
				ps.setNull(29, java.sql.Types.BOOLEAN);
			}

			Integer iVal = getInt(textField13.getToolTipText());
			if (iVal == null) {
				ps.setNull(30, java.sql.Types.INTEGER);
			} else {
				ps.setInt(30, iVal);
			}

			if (ps.executeUpdate() > 0 && oldValue == null) {// execute()
				result = new Double(DBKernel.getLastInsertedID(ps));
			} 
		}
		catch (Exception e) {MyLogger.handleException(e);}
		return result;
	}
	private void saveValue(final PreparedStatement ps, final int index, final String tfText, final JTextField tf_exp, final JCheckBox cb) {
		saveValue(ps, index, tfText, tf_exp, cb, false);
	}
	private void saveValue(final PreparedStatement ps, final int index, final String tfText, final JTextField tf_exp, final JCheckBox cb, final boolean isString) {
		try {
			boolean hasExponent = (tf_exp != null);
			if (tfText.trim().length() > 0) {
				if (isString) {
					ps.setString(index, tfText);
				}	
				else {
					try {
						double dbl = Double.parseDouble(tfText);
						ps.setDouble(index, dbl);
					}
					catch (Exception ed) {MyLogger.handleException(ed); ps.setNull(index, java.sql.Types.DOUBLE);}					
				}
			}
			else {
				if (isString) {
					ps.setNull(index, java.sql.Types.VARCHAR);
				} else {
					ps.setNull(index, java.sql.Types.DOUBLE);
				}
			}
			if (hasExponent) {
				if (tf_exp.getText().trim().length() > 0) {
					try {
						double dbl = Double.parseDouble(tf_exp.getText().trim());
						ps.setDouble(index+1, dbl);
					}
					catch (Exception ed) {MyLogger.handleException(ed); ps.setNull(index+1, java.sql.Types.DOUBLE);}										
				}
				else {
					ps.setNull(index+1, java.sql.Types.DOUBLE);
				}
			}
			if (cb.isSelected()) {
				ps.setBoolean(index+(hasExponent?2:1), true);
			} else {
				ps.setNull(index+(hasExponent?2:1), java.sql.Types.BOOLEAN);
			}
		}
		catch (Exception e) {MyLogger.handleException(e);}
	}
}
