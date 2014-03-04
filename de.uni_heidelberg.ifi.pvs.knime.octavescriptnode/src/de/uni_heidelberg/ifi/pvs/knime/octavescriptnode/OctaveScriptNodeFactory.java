/*
 * OctaveScriptNode - A KNIME node that runs Octave scripts
 * Copyright (C) 2011 Andre-Patrick Bubel (pvs@andre-bubel.de) and
 *                    Parallel and Distributed Systems Group (PVS),
 *                    University of Heidelberg, Germany
 * Website: http://pvs.ifi.uni-heidelberg.de/
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
 */
package de.uni_heidelberg.ifi.pvs.knime.octavescriptnode;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "OctaveScriptNode" Node.
 * 
 * @author André-Patrick Bubel <code@andre-bubel.de>
 * 
 */
public class OctaveScriptNodeFactory extends NodeFactory<OctaveScriptNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new OctaveScriptNodeDialog();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OctaveScriptNodeModel createNodeModel() {
		return new OctaveScriptNodeModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<OctaveScriptNodeModel> createNodeView(final int viewIndex,
			final OctaveScriptNodeModel nodeModel) {
		return new OctaveScriptNodeView(nodeModel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNrNodeViews() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}

}
