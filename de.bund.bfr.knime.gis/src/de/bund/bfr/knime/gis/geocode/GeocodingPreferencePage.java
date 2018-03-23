/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.gis.geocode;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.bund.bfr.knime.gis.Activator;

public class GeocodingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String MAPQUEST_KEY = "GeocodingMapQuestKey";
	public static final String BKG_UUID = "GeocodingBkgUuid";
	public static final String BING_KEY = "GeocodingBingKey";

	public GeocodingPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		addField(new TrimmingStringFieldEditor(MAPQUEST_KEY, "MapQuest API Key", getFieldEditorParent()));
		addField(new TrimmingStringFieldEditor(BKG_UUID, "\"Bundesamt für Kartographie und Geodäsie\" UUID",
				getFieldEditorParent()));
		addField(new TrimmingStringFieldEditor(BING_KEY, "Bing Maps Key", getFieldEditorParent()));
	}

	private static class TrimmingStringFieldEditor extends StringFieldEditor {

		public TrimmingStringFieldEditor(String name, String labelText, Composite parent) {
			super(name, labelText, UNLIMITED, VALIDATE_ON_FOCUS_LOST, parent);
		}

		@Override
		protected boolean doCheckState() {
			setStringValue(getStringValue().trim());

			return super.doCheckState();
		}
	}
}
