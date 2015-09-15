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
