package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.util.LinkedHashSet;

import de.bund.bfr.knime.openkrise.db.DBKernel;

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
	
	public void intoDb() {
		for (String s : lSampling) {
			int numIdsPresent = DBKernel.getRowCount("LookUps", " WHERE " + DBKernel.delimitL("type") + "='Sampling' AND UCASE(" + DBKernel.delimitL("value") + ")='" + s.toUpperCase() + "'");
			if (numIdsPresent == 0) {
				String sql = "INSERT INTO " + DBKernel.delimitL("LookUps") + " (" + DBKernel.delimitL("type") + "," + DBKernel.delimitL("value") + ") VALUES ('Sampling','" + s + "')";
				DBKernel.sendRequest(sql, false);
			}
		}
		for (String s : lTypeOfBusiness) {
			int numIdsPresent = DBKernel.getRowCount("LookUps", " WHERE " + DBKernel.delimitL("type") + "='TypeOfBusiness' AND UCASE(" + DBKernel.delimitL("value") + ")='" + s.toUpperCase() + "'");
			if (numIdsPresent == 0) {
				String sql = "INSERT INTO " + DBKernel.delimitL("LookUps") + " (" + DBKernel.delimitL("type") + "," + DBKernel.delimitL("value") + ") VALUES ('TypeOfBusiness','" + s + "')";
				DBKernel.sendRequest(sql, false);
			}
		}
		for (String s : lTreatment) {
			int numIdsPresent = DBKernel.getRowCount("LookUps", " WHERE " + DBKernel.delimitL("type") + "='Treatment' AND UCASE(" + DBKernel.delimitL("value") + ")='" + s.toUpperCase() + "'");
			if (numIdsPresent == 0) {
				String sql = "INSERT INTO " + DBKernel.delimitL("LookUps") + " (" + DBKernel.delimitL("type") + "," + DBKernel.delimitL("value") + ") VALUES ('Treatment','" + s + "')";
				DBKernel.sendRequest(sql, false);
			}
		}
		for (String s : lUnit) {
			int numIdsPresent = DBKernel.getRowCount("LookUps", " WHERE " + DBKernel.delimitL("type") + "='Units' AND UCASE(" + DBKernel.delimitL("value") + ")='" + s.toUpperCase() + "'");
			if (numIdsPresent == 0) {
				String sql = "INSERT INTO " + DBKernel.delimitL("LookUps") + " (" + DBKernel.delimitL("type") + "," + DBKernel.delimitL("value") + ") VALUES ('Units','" + s + "')";
				DBKernel.sendRequest(sql, false);
			}
		}
	}
}
