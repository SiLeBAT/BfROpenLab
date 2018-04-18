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
package de.bund.bfr.knime.pmmlite.views;

import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeView;

public abstract class ViewFactory extends NodeFactory<NodeModel> {

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<NodeModel> createNodeView(int viewIndex, NodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}
}
