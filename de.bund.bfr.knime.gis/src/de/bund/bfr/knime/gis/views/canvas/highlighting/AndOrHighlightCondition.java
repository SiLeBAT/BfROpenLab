/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.gis.views.canvas.highlighting;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.bund.bfr.knime.gis.views.canvas.element.Element;

public class AndOrHighlightCondition implements HighlightCondition, Serializable {

	private static final long serialVersionUID = 1L;

	private List<List<LogicalHighlightCondition>> conditions;
	private String name;
	private boolean showInLegend;
	private Color color;
	private boolean invisible;
	private boolean useThickness;
	private String labelProperty;

	public AndOrHighlightCondition() {
		this(new LogicalHighlightCondition(), null, true, null, false, false, null);
	}

	public AndOrHighlightCondition(LogicalHighlightCondition condition, String name, boolean showInLegend, Color color,
			boolean invisible, boolean useThickness, String labelProperty) {
		this(new ArrayList<>(Arrays.asList(new ArrayList<>(Arrays.asList(condition)))), name, showInLegend, color,
				invisible, useThickness, labelProperty);
	}

	public AndOrHighlightCondition(List<List<LogicalHighlightCondition>> conditions, String name, boolean showInLegend,
			Color color, boolean invisible, boolean useThickness, String labelProperty) {
		setConditions(conditions);
		setName(name);
		setShowInLegend(showInLegend);
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
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean isShowInLegend() {
		return showInLegend;
	}

	public void setShowInLegend(boolean showInLegend) {
		this.showInLegend = showInLegend;
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

	public int getConditionCount() {
		return conditions.stream().mapToInt(c -> c.size()).sum();
	}

	@Override
	public <T extends Element> Map<T, Double> getValues(Collection<? extends T> elements) {
		List<List<Map<? extends T, Double>>> valuesList = new ArrayList<>();

		for (List<LogicalHighlightCondition> andLists : conditions) {
			valuesList.add(
					andLists.stream().map(c -> c.getValues(elements)).collect(Collectors.toCollection(ArrayList::new)));
		}

		Map<T, Double> returnValues = new LinkedHashMap<>();

		for (T element : elements) {
			boolean allFalse = true;

			for (List<Map<? extends T, Double>> andValues : valuesList) {
				if (andValues.stream().allMatch(values -> values.get(element) == 1.0)) {
					allFalse = false;
					break;
				}
			}

			returnValues.put(element, allFalse ? 0.0 : 1.0);
		}

		return returnValues;
	}

	@Override
	public Point2D getValueRange(Collection<? extends Element> elements) {
		return new Point2D.Double(0.0, 1.0);
	}

	@Override
	public AndOrHighlightCondition copy() {
		List<List<LogicalHighlightCondition>> conditionsCopy = new ArrayList<>();

		for (List<LogicalHighlightCondition> list : conditions) {
			conditionsCopy.add(
					list.stream().map(c -> new LogicalHighlightCondition(c.getProperty(), c.getType(), c.getValue()))
							.collect(Collectors.toCollection(ArrayList::new)));
		}

		return new AndOrHighlightCondition(conditionsCopy, name, showInLegend, color, invisible, useThickness,
				labelProperty);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + ((conditions == null) ? 0 : conditions.hashCode());
		result = prime * result + (invisible ? 1231 : 1237);
		result = prime * result + ((labelProperty == null) ? 0 : labelProperty.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (showInLegend ? 1231 : 1237);
		result = prime * result + (useThickness ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AndOrHighlightCondition other = (AndOrHighlightCondition) obj;
		if (color == null) {
			if (other.color != null)
				return false;
		} else if (!color.equals(other.color))
			return false;
		if (conditions == null) {
			if (other.conditions != null)
				return false;
		} else if (!conditions.equals(other.conditions))
			return false;
		if (invisible != other.invisible)
			return false;
		if (labelProperty == null) {
			if (other.labelProperty != null)
				return false;
		} else if (!labelProperty.equals(other.labelProperty))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (showInLegend != other.showInLegend)
			return false;
		if (useThickness != other.useThickness)
			return false;
		return true;
	}
}
