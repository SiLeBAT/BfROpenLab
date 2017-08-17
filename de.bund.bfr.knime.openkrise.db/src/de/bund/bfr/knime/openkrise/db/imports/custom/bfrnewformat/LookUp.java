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
package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.util.LinkedHashSet;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;

public class LookUp {

	LinkedHashSet<String> lSampling = new LinkedHashSet<>();
	LinkedHashSet<String> lTypeOfBusiness = new LinkedHashSet<>();
	LinkedHashSet<String> lTreatment = new LinkedHashSet<>();
	LinkedHashSet<String> lUnit = new LinkedHashSet<>();
	
	public void addSampling(String sampling) {
		if (sampling != null && !sampling.isEmpty()) lSampling.add(sampling);
	}
	public void addTypeOfBusiness(String typeOfBusiness) {
		if (typeOfBusiness != null && !typeOfBusiness.isEmpty()) lTypeOfBusiness.add(typeOfBusiness);
	}
	public void addTreatment(String treatment) {
		if (treatment != null && !treatment.isEmpty()) lTreatment.add(treatment);
	}
	public void addUnit(String unit) {
		if (unit != null && !unit.isEmpty()) lUnit.add(unit);
	}
	
	public void intoDb(MyDBI mydbi) {
		for (String s : lSampling) {
			String where = " WHERE " + MyDBI.delimitL("type") + "='Sampling' AND UCASE(" + MyDBI.delimitL("value") + ")='" + s.toUpperCase() + "'";
			int numIdsPresent = (mydbi != null ? mydbi.getRowCount("LookUps", where) : DBKernel.getRowCount("LookUps", where));
			if (numIdsPresent == 0) {
				String sql = "INSERT INTO " + MyDBI.delimitL("LookUps") + " (" + MyDBI.delimitL("type") + "," + MyDBI.delimitL("value") + ") VALUES ('Sampling','" + s + "')";
				if (mydbi != null) mydbi.sendRequest(sql, false, false);
				else DBKernel.sendRequest(sql, false);
			}
		}
		for (String s : lTypeOfBusiness) {
			String where = " WHERE " + MyDBI.delimitL("type") + "='TypeOfBusiness' AND UCASE(" + MyDBI.delimitL("value") + ")='" + s.toUpperCase() + "'";
			int numIdsPresent = (mydbi != null ? mydbi.getRowCount("LookUps", where) : DBKernel.getRowCount("LookUps", where));
			if (numIdsPresent == 0) {
				String sql = "INSERT INTO " + MyDBI.delimitL("LookUps") + " (" + MyDBI.delimitL("type") + "," + MyDBI.delimitL("value") + ") VALUES ('TypeOfBusiness','" + s + "')";
				if (mydbi != null) mydbi.sendRequest(sql, false, false);
				else DBKernel.sendRequest(sql, false);
			}
		}
		for (String s : lTreatment) {
			String where = " WHERE " + MyDBI.delimitL("type") + "='Treatment' AND UCASE(" + MyDBI.delimitL("value") + ")='" + s.toUpperCase() + "'";
			int numIdsPresent = (mydbi != null ? mydbi.getRowCount("LookUps", where) : DBKernel.getRowCount("LookUps", where));
			if (numIdsPresent == 0) {
				String sql = "INSERT INTO " + MyDBI.delimitL("LookUps") + " (" + MyDBI.delimitL("type") + "," + MyDBI.delimitL("value") + ") VALUES ('Treatment','" + s + "')";
				if (mydbi != null) mydbi.sendRequest(sql, false, false);
				else DBKernel.sendRequest(sql, false);
			}
		}
		for (String s : lUnit) {
			String where = " WHERE " + MyDBI.delimitL("type") + "='Units' AND UCASE(" + MyDBI.delimitL("value") + ")='" + s.toUpperCase() + "'";
			int numIdsPresent = (mydbi != null ? mydbi.getRowCount("LookUps", where) : DBKernel.getRowCount("LookUps", where));
			if (numIdsPresent == 0) {
				String sql = "INSERT INTO " + MyDBI.delimitL("LookUps") + " (" + MyDBI.delimitL("type") + "," + MyDBI.delimitL("value") + ") VALUES ('Units','" + s + "')";
				if (mydbi != null) mydbi.sendRequest(sql, false, false);
				else DBKernel.sendRequest(sql, false);
			}
		}
	}
}
