/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.views.tracingview;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View;
import de.bund.bfr.knime.openkrise.views.canvas.ExplosionTracingGraphCanvas;

public class ExplosionGraphSettings extends GraphSettings {
  
	private static final String CFG_BOUNDARY_PARAMS = "GraphBoundaryParameters";

	private double[] boundaryParams;

	public ExplosionGraphSettings() {
		super();
	}

	public ExplosionGraphSettings(GraphSettings set) {
		this(set, false);
	}
	
	private ExplosionGraphSettings(GraphSettings set, boolean applyTransformAndPositions) {
		super(set, applyTransformAndPositions);
	}

	public ExplosionGraphSettings copy() {
		ExplosionGraphSettings copy = new ExplosionGraphSettings(this, true);
		copy.boundaryParams = null;
		if (boundaryParams != null) {
			copy.boundaryParams = new double[boundaryParams.length];
			System.arraycopy(boundaryParams, 0, copy.boundaryParams, 0, boundaryParams.length);
		}
		return copy;
	}
   
	/*
	 * loads graph settings 
	 * (prefix is non empty for explosion graph settings)
	 */
	protected void loadSettings(NodeSettingsRO settings, String prefix) {
		try {
			boundaryParams = settings.getDoubleArray(prefix + CFG_BOUNDARY_PARAMS);
		} catch (InvalidSettingsException e) {
		}
	}


	public void setFromCanvas(GraphCanvas canvas) {
		super.setFromCanvas(canvas);
		this.boundaryParams = ((ExplosionTracingGraphCanvas) canvas).getBoundaryParams();  
	}

	public void setToCanvas(GraphCanvas canvas) {
		((ExplosionTracingGraphCanvas) canvas).setBoundaryParams(boundaryParams);
		super.setToCanvas(canvas);
	}

	/*
	 * saves the graph settings
	 * (prefix is non empty for explosion graph settings)
	 */
	public void saveSettings(NodeSettingsWO settings, String prefix) {
		super.saveSettings(settings, prefix);
		settings.addDoubleArray(prefix + CFG_BOUNDARY_PARAMS, boundaryParams);
	}

	public void saveSettings(JsonConverter.JsonBuilder jsonBuilder, int index) {
		super.saveSettings(jsonBuilder, index);
		jsonBuilder.setExplosionGraphBoundary(index, this.boundaryParams);
	}

	public void loadSettings(View.ExplosionSettings.ExplosionGraphSettings graphView) {
		super.loadSettings(graphView);
		this.boundaryParams = graphView.boundaryParams;
	}
}
