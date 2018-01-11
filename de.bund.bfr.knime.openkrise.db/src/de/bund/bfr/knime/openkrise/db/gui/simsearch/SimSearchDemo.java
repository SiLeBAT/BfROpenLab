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
package de.bund.bfr.knime.openkrise.db.gui.simsearch;

import java.awt.Dimension;
import java.awt.EventQueue;

public class SimSearchDemo {
	public static void main(String... args) {
	    EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
              TestFrame frame = new TestFrame();
              frame.setVisible(true);
            	//SimSearchJFrame frame = new SimSearchJFrame();
            	//frame.setPreferredSize(new Dimension(1200,800));
                //frame.setVisible(true);
                //frame.startSearch();
            }
        });
	  }
}
