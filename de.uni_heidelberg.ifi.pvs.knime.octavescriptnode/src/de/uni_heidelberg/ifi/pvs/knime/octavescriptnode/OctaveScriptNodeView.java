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

import javax.swing.JEditorPane;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "OctaveScriptNode" Node.
 * 
 * @author André-Patrick Bubel <code@andre-bubel.de>
 * 
 */
public class OctaveScriptNodeView extends NodeView<OctaveScriptNodeModel> {

	private JEditorPane octaveOutputPane;

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel
	 *            The model (class: {@link OctaveScriptNodeModel})
	 */
	protected OctaveScriptNodeView(final OctaveScriptNodeModel nodeModel) {
		super(nodeModel);

		octaveOutputPane = new JEditorPane();
		this.setComponent(octaveOutputPane);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void modelChanged() {

		// retrieve the new model from your nodemodel and
		// update the view.
		OctaveScriptNodeModel nodeModel = getNodeModel();
		assert nodeModel != null;

		// be aware of a possibly not executed nodeModel! The data you retrieve
		// from your nodemodel could be null, emtpy, or invalid in any kind.
		octaveOutputPane.setText(nodeModel.getLatestOctaveOutput());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onClose() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onOpen() {
	}

}
