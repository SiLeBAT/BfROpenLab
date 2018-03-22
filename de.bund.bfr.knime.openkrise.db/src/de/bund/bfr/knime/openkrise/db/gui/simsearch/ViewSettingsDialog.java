package de.bund.bfr.knime.openkrise.db.gui.simsearch;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

public class ViewSettingsDialog extends JDialog {
	
	//private SimSearchTable.ViewSettings table;
	
	protected ViewSettingsDialog(Frame owner, SimSearchTable.ViewSettings viewSettings) {
		super(owner);
		//this.table = table;
		initComponents();
	}
	
	private void initComponents() {
		JPanel mainPanel = new JPanel();
		JPanel fontPanel = new JPanel();
		fontPanel.setBorder(BorderFactory.createTitledBorder("Font"));
		fontPanel.setLayout(new FlowLayout());
		fontPanel.add(new JLabel("Name:"));
		fontPanel.add(new FontChooser());
		//fontPanel.add(new FontSizeChooser());
		mainPanel.add(fontPanel);
		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
	}
	
	public static class FontChooser extends JComboBox<Font> {
		 
	    public FontChooser(final Component... components) {
	 
	        final Font[] fonts = GraphicsEnvironment
	                .getLocalGraphicsEnvironment()
	                .getAllFonts();
	 
	        Arrays.sort(fonts, new Comparator<Font>() {
	            @Override
	            public int compare(Font f1, Font f2) {
	                return f1.getName().compareTo(f2.getName());
	            }
	        });
	 
	        for (Font font : fonts) {
	            if (font.canDisplayUpTo(font.getName()) == -1) {
	                addItem(font);
	            }
	        }
	 
	        addItemListener(new ItemListener() {
	            @Override
	            public void itemStateChanged(ItemEvent e) {
	                final Font font = (Font) e.getItem();
	                for (Component comp : components) {
	                    setFontPreserveSize(comp, font);
	                }
	            }
	        });
	         
	        setRenderer(new FontCellRenderer());
	    }
	     
	    private static class FontCellRenderer 
	            implements ListCellRenderer<Font> {
	         
	        protected DefaultListCellRenderer renderer = 
	                new DefaultListCellRenderer();
	         
	        public Component getListCellRendererComponent(
	                JList<? extends Font> list, Font font, int index, 
	                boolean isSelected, boolean cellHasFocus) {
	             
	            final Component result = renderer.getListCellRendererComponent(
	                    list, font.getName(), index, isSelected, cellHasFocus);
	             
	            setFontPreserveSize(result, font);
	            return result;
	        }
	    }
	 
	    private static void setFontPreserveSize(final Component comp, Font font) {
	        final float size = comp.getFont().getSize();
	        comp.setFont(font.deriveFont(size));
	    }
	}
	
	
}
