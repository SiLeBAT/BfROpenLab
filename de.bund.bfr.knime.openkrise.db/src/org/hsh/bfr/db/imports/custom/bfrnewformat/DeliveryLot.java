package org.hsh.bfr.db.imports.custom.bfrnewformat;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hsh.bfr.db.DBKernel;

public class DeliveryLot {

	public static void getId(Integer dbId, Integer dbPId, Integer miDbId) throws SQLException {
		String sql = "SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL("ChargenVerbindungen") +
				" WHERE " + DBKernel.delimitL("Zutat") + "=" + dbId + " AND " + DBKernel.delimitL("Produkt") + "=" + dbPId;
		ResultSet rs = DBKernel.getResultSet(sql, false);
		Integer result = null;
		if (rs != null && rs.first()) {
			result = rs.getInt(1);
		}
		
		if (result != null) {
			DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("ChargenVerbindungen") + " SET " + DBKernel.delimitL("ImportSources") + "=CASEWHEN(INSTR(';" + miDbId + ";'," + DBKernel.delimitL("ImportSources") + ")=0,CONCAT(" + DBKernel.delimitL("ImportSources") + ", '" + miDbId + ";'), " + DBKernel.delimitL("ImportSources") + ") WHERE " + DBKernel.delimitL("ID") + "=" + result, false);
		}
		else {
			sql = "INSERT INTO " + DBKernel.delimitL("ChargenVerbindungen") +
					" (" + DBKernel.delimitL("Zutat") + "," + DBKernel.delimitL("Produkt") + "," + DBKernel.delimitL("ImportSources") +
					") VALUES (" + dbId + "," + dbPId + ",';" + miDbId + ";')";
			DBKernel.sendRequest(sql, false);
		}		
	}
}
