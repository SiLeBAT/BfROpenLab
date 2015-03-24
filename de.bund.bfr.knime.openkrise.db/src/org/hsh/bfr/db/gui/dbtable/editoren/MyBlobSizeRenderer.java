/*******************************************************************************
 * PMM-Lab � 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab � 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * Christian Th�ns (BfR)
 * Matthias Filter (BfR)
 * Armin A. Weiser (BfR)
 * Alexander Falenski (BfR)
 * J�rgen Brandt (BfR)
 * Annemarie K�sbohrer (BfR)
 * Bernd Appel (BfR)
 * 
 * PMM-Lab is a project under development. Contributions are welcome.
 * 
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
 ******************************************************************************/
/**
 * 
 */
package org.hsh.bfr.db.gui.dbtable.editoren;

import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.JLabel;

import quick.dbtable.CellComponent;

/**
 * @author Armin
 *
 */
public class MyBlobSizeRenderer extends JLabel implements CellComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DecimalFormat df = new DecimalFormat("0.00");

	public MyBlobSizeRenderer() {
		this.setHorizontalAlignment(JLabel.CENTER);
		this.setVerticalAlignment(JLabel.TOP);
	}
	public void setValue(Object value) { 
		if (value == null) this.setText(""); 
		else if (value instanceof Integer) {
			int size = (Integer) value;
			if (size < 1024) this.setText(size + " B");
			else if (size < 1024*1024) this.setText(df.format(size / 1024.0) + " kB");
			else this.setText(df.format(size / 1024.0 / 1024) + " MB");
		}
		else this.setText(value.toString()); 
	}

	public void addActionListener(ActionListener arg0) {
	}

	public JComponent getComponent() {
		return this;
	}

	public Object getValue() {
		return null;
	}

}