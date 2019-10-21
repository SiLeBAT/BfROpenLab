/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.util.shapefilereader;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import de.bund.bfr.knime.gis.shapefilereader.ShapefileReaderNodeDialog;
import de.bund.bfr.knime.gis.shapefilereader.ShapefileReaderNodeModel;

/**
 * <code>NodeFactory</code> for the "ShapefileReader" Node.
 * 
 * 
 * @author Christian Thoens
 */
public class ShapefileReaderNodeFactory extends NodeFactory<ShapefileReaderNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ShapefileReaderNodeModel createNodeModel() {
		return new ShapefileReaderNodeModel();
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
	public NodeView<ShapefileReaderNodeModel> createNodeView(final int viewIndex,
			final ShapefileReaderNodeModel nodeModel) {
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
		return new ShapefileReaderNodeDialog();
	}

}
