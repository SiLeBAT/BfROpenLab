/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Armin A. Weiser (BfR)
 * Christian Thoens (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
package de.bund.bfr.knime.openkrise.util.cluster;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "DBSCAN" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author BfR
 */
public class DBSCANNodeDialog extends DefaultNodeSettingsPane implements
		ChangeListener {

	private DialogComponent algorithmComp;
	private DialogComponent minPointsComp;
	private DialogComponent maxDistComp;
	private DialogComponent clustersComp;

	/**
	 * New pane for configuring the DBSCAN node.
	 */
	public DBSCANNodeDialog() {
		algorithmComp = new DialogComponentStringSelection(
				new SettingsModelString(DBSCANNodeModel.CFG_CHOSENMODEL,
						DBSCANNodeModel.DEFAULT_CHOSENMODEL),
				"Algorithm",
				new String[] { DBSCANNodeModel.DBSCAN, DBSCANNodeModel.K_MEANS });
		algorithmComp.getModel().addChangeListener(this);
		minPointsComp = new DialogComponentNumber(new SettingsModelInteger(
				DBSCANNodeModel.CFG_MINPTS, DBSCANNodeModel.DEFAULT_MINPTS),
				"Min Number of Points per Cluster", 1);
		maxDistComp = new DialogComponentNumber(new SettingsModelDouble(
				DBSCANNodeModel.CFG_EPS, DBSCANNodeModel.DEFAULT_EPS),
				"Max Neighborhood Distance (km)", 0.5);
		clustersComp = new DialogComponentNumber(
				new SettingsModelInteger(DBSCANNodeModel.CFG_CLUSTERS,
						DBSCANNodeModel.DEFAULT_CLUSTERS),
				"Number of Clusters", 1);

		addChangeListeners();
		addDialogComponent(algorithmComp);
		addDialogComponent(minPointsComp);
		addDialogComponent(maxDistComp);
		addDialogComponent(clustersComp);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		boolean isDBSCAN = ((SettingsModelString) algorithmComp.getModel())
				.getStringValue().equals(DBSCANNodeModel.DBSCAN);

		removeChangeListeners();
		minPointsComp.getModel().setEnabled(isDBSCAN);
		maxDistComp.getModel().setEnabled(isDBSCAN);
		clustersComp.getModel().setEnabled(!isDBSCAN);
		addChangeListeners();
	}

	private void addChangeListeners() {
		minPointsComp.getModel().addChangeListener(this);
		maxDistComp.getModel().addChangeListener(this);
		clustersComp.getModel().addChangeListener(this);
	}

	private void removeChangeListeners() {
		minPointsComp.getModel().removeChangeListener(this);
		maxDistComp.getModel().removeChangeListener(this);
		clustersComp.getModel().removeChangeListener(this);
	}
}
