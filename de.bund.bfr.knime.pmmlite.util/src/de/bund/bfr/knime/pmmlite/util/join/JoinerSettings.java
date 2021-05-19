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
package de.bund.bfr.knime.pmmlite.util.join;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class JoinerSettings {

	public static enum JoinType {
		PRIMARY_JOIN("Primary Join"), SECONDARY_JOIN("Secondary Join"), TERTIARY_JOIN("Tertiary Join"), FORMULA_JOIN(
				"Formula Join");

		private String name;

		private JoinType(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private static final String CFG_JOIN_TYPE = "JoinType";
	private static final String CFG_ASSIGNMENTS = "Assignments";

	private JoinType joinType;
	private String assignments;

	public JoinerSettings() {
		joinType = null;
		assignments = null;
	}

	public void loadSettings(NodeSettingsRO settings) {
		try {
			joinType = JoinType.valueOf(settings.getString(CFG_JOIN_TYPE));
		} catch (NullPointerException e) {
			joinType = null;
		} catch (InvalidSettingsException e) {
		}

		try {
			assignments = settings.getString(CFG_ASSIGNMENTS);
		} catch (InvalidSettingsException e) {
		}
	}

	public void saveSettings(NodeSettingsWO settings) {
		settings.addString(CFG_JOIN_TYPE, joinType != null ? joinType.name() : null);
		settings.addString(CFG_ASSIGNMENTS, assignments);
	}

	public JoinType getJoinType() {
		return joinType;
	}

	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}

	public String getAssignments() {
		return assignments;
	}

	public void setAssignments(String assignments) {
		this.assignments = assignments;
	}
}
