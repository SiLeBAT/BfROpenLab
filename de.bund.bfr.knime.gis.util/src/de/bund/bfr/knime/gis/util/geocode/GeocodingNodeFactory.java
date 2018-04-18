/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.gis.util.geocode;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import de.bund.bfr.knime.gis.geocode.GeocodingNodeDialog;
import de.bund.bfr.knime.gis.geocode.GeocodingNodeModel;

/**
 * <code>NodeFactory</code> for the "Geocoding" Node.
 * 
 * 
 * @author Christian Thoens
 */
public class GeocodingNodeFactory extends NodeFactory<GeocodingNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GeocodingNodeModel createNodeModel() {
		return new GeocodingNodeModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNrNodeViews() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<GeocodingNodeModel> createNodeView(final int viewIndex, final GeocodingNodeModel nodeModel) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new GeocodingNodeDialog();
	}

}
