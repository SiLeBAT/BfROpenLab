/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
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
package de.bund.bfr.knime.gis.views.canvas.highlighting;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.bund.bfr.knime.gis.views.canvas.element.Element;

public class AndOrHighlightCondition implements HighlightCondition,
		Serializable {

	public static final String AND_TYPE = "And";
	public static final String OR_TYPE = "Or";
	public static final String[] TYPES = { AND_TYPE, OR_TYPE };

	private static final long serialVersionUID = 1L;

	private List<List<LogicalHighlightCondition>> conditions;
	private Color color;
	private boolean invisible;
	private boolean useThickness;
	private String labelProperty;

	public AndOrHighlightCondition() {
		this(null, null, false, false, null);
	}

	public AndOrHighlightCondition(
			List<List<LogicalHighlightCondition>> conditions, Color color,
			boolean invisible, boolean useThickness, String labelProperty) {
		setConditions(conditions);
		setColor(color);
		setInvisible(invisible);
		setUseThickness(useThickness);
		setLabelProperty(labelProperty);
	}

	public List<List<LogicalHighlightCondition>> getConditions() {
		return conditions;
	}

	public void setConditions(List<List<LogicalHighlightCondition>> conditions) {
		this.conditions = conditions;
	}

	@Override
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public boolean isInvisible() {
		return invisible;
	}

	public void setInvisible(boolean invisible) {
		this.invisible = invisible;
	}

	@Override
	public boolean isUseThickness() {
		return useThickness;
	}

	public void setUseThickness(boolean useThickness) {
		this.useThickness = useThickness;
	}

	@Override
	public String getLabelProperty() {
		return labelProperty;
	}

	public void setLabelProperty(String labelProperty) {
		this.labelProperty = labelProperty;
	}

	@Override
	public <T extends Element> Map<T, Double> getValues(Collection<T> elements) {
		List<List<Map<T, Double>>> valuesList = new ArrayList<List<Map<T, Double>>>();

		for (List<LogicalHighlightCondition> andLists : conditions) {
			List<Map<T, Double>> v = new ArrayList<Map<T, Double>>();

			for (LogicalHighlightCondition condition : andLists) {
				v.add(condition.getValues(elements));
			}

			valuesList.add(v);
		}

		Map<T, Double> returnValues = new LinkedHashMap<T, Double>();

		for (T element : elements) {
			boolean allFalse = true;

			for (List<Map<T, Double>> andValues : valuesList) {
				boolean allTrue = true;

				for (Map<T, Double> values : andValues) {
					if (values.get(element) != 1.0) {
						allTrue = false;
						break;
					}
				}

				if (allTrue) {
					allFalse = false;
					break;
				}
			}

			if (allFalse) {
				returnValues.put(element, 0.0);
			} else {
				returnValues.put(element, 1.0);
			}
		}

		return returnValues;
	}

	@Override
	public String toString() {
		return "Logical Condition";
	}

}
