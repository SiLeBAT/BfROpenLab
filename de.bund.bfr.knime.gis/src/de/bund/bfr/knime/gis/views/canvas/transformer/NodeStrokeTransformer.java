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
package de.bund.bfr.knime.gis.views.canvas.transformer;

import java.awt.BasicStroke;
import java.awt.Stroke;

import org.apache.commons.collections15.Transformer;

import de.bund.bfr.knime.gis.views.canvas.element.Node;

public class NodeStrokeTransformer<V extends Node> implements Transformer<V, Stroke> {

	private String metaNodeProperty;

	public NodeStrokeTransformer(String metaNodeProperty) {
		this.metaNodeProperty = metaNodeProperty;
	}

	@Override
	public Stroke transform(V node) {
		Boolean isMetaNode = (Boolean) node.getProperties().get(metaNodeProperty);

		if (isMetaNode != null && isMetaNode) {
			return new BasicStroke(4.0f);
		}

		return new BasicStroke(1.0f);
	}

}
