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
package de.bund.bfr.knime.pmmlite.io.combasereader;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class CombaseReaderSettings {

	private static final String CFG_FILE_NAME = "FileName";
	private static final String CFG_TREAT_NOT_DETECTED_AS_ZERO = "TreatNotDetectedAsZero";

	private static final boolean DEFAULT_TREAT_NOT_DETECTED_AS_ZERO = false;

	private String fileName;
	private boolean treatNotDetectedAsZero;

	public CombaseReaderSettings() {
		fileName = null;
		treatNotDetectedAsZero = DEFAULT_TREAT_NOT_DETECTED_AS_ZERO;
	}

	public void load(NodeSettingsRO settings) {
		try {
			fileName = settings.getString(CFG_FILE_NAME);
		} catch (InvalidSettingsException e) {
		}

		try {
			treatNotDetectedAsZero = settings.getBoolean(CFG_TREAT_NOT_DETECTED_AS_ZERO);
		} catch (InvalidSettingsException e) {
		}
	}

	public void save(NodeSettingsWO settings) {
		settings.addString(CFG_FILE_NAME, fileName);
		settings.addBoolean(CFG_TREAT_NOT_DETECTED_AS_ZERO, treatNotDetectedAsZero);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean isTreatNotDetectedAsZero() {
		return treatNotDetectedAsZero;
	}

	public void setTreatNotDetectedAsZero(boolean treatNotDetectedAsZero) {
		this.treatNotDetectedAsZero = treatNotDetectedAsZero;
	}
}
