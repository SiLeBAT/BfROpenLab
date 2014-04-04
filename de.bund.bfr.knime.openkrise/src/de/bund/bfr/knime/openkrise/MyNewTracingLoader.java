package de.bund.bfr.knime.openkrise;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

import org.hsh.bfr.db.DBKernel;
import org.hsh.bfr.db.Hsqldbiface;

import de.bund.bfr.knime.openkrise.MyDelivery;
import de.bund.bfr.knime.openkrise.MyNewTracing;

public class MyNewTracingLoader {

	private static HashMap<Integer, MyDelivery> allDeliveries;
	private static HashMap<Integer, Double> caseStations = null;
	private static HashSet<Integer> ccStations = null;
	private static double caseSum = 0;

	public static MyNewTracing getNewTracingModel(Hsqldbiface db, boolean assumeCrossContamination, boolean enforceTemporalOrder) {
		// Zeroly: get all cases
		caseStations = new HashMap<Integer, Double>();
		ccStations = new HashSet<Integer>();
		//ccDeliveries = new HashSet<Integer>();
		String sql = "SELECT " + DBKernel.delimitL("ID") + "," + DBKernel.delimitL("CasePriority") + " FROM " + DBKernel.delimitL("Station") + " WHERE " + DBKernel.delimitL("CasePriority") + " > 0";
		try {
			ResultSet rs = db.pushQuery(sql);
			caseSum = 0;
			while (rs.next()) {
				double cp = rs.getDouble("CasePriority");
				if (cp < 0) cp = 0;
				if (cp > 1) cp = 1;
				caseStations.put(rs.getInt("ID"), cp);
				caseSum += cp;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		allDeliveries = new HashMap<Integer, MyDelivery>();		
		// Firstly: get all deliveries
		sql = "SELECT " + DBKernel.delimitL("ID") + "," + DBKernel.delimitL("Empfänger") + "," + DBKernel.delimitL("Station") + "," + DBKernel.delimitL("dd_day") + "," + DBKernel.delimitL("dd_month") + "," + DBKernel.delimitL("dd_year") +
				" FROM " + DBKernel.delimitL("Lieferungen") +
    			" LEFT JOIN " + DBKernel.delimitL("Chargen") +
    			" ON " + DBKernel.delimitL("Lieferungen") + "." + DBKernel.delimitL("Charge") + "=" + DBKernel.delimitL("Chargen") + "." + DBKernel.delimitL("ID") +
    			" LEFT JOIN " + DBKernel.delimitL("Produktkatalog") +
    			" ON " + DBKernel.delimitL("Chargen") + "." + DBKernel.delimitL("Artikel") + "=" + DBKernel.delimitL("Produktkatalog") + "." + DBKernel.delimitL("ID");
		try {
			ResultSet rs = db.pushQuery(sql);
			while (rs.next()) {
				MyDelivery md = new MyDelivery(rs.getInt("ID"), rs.getInt("Station"), rs.getInt("Empfänger"), (Integer) rs.getObject("dd_day"), (Integer) rs.getObject("dd_month"), (Integer) rs.getObject("dd_year"));
				allDeliveries.put(rs.getInt("ID"), md);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Secondly: prev/next deliveries
		if (assumeCrossContamination) {
			assumeCC(db, enforceTemporalOrder);
		}
		else {
			sql = "SELECT " + DBKernel.delimitL("ZutatLieferungen") + "." + DBKernel.delimitL("ID") + "," + DBKernel.delimitL("ProduktLieferungen") + "." + DBKernel.delimitL("ID") +
					" FROM " + DBKernel.delimitL("ChargenVerbindungen") +
					" LEFT JOIN " + DBKernel.delimitL("Lieferungen") + " AS " + DBKernel.delimitL("ZutatLieferungen") +
					" ON " + DBKernel.delimitL("ZutatLieferungen") + "." + DBKernel.delimitL("ID") + "=" + DBKernel.delimitL("ChargenVerbindungen") + "." + DBKernel.delimitL("Zutat") +
					" LEFT JOIN " + DBKernel.delimitL("Lieferungen") + " AS " + DBKernel.delimitL("ProduktLieferungen") +
					" ON " + DBKernel.delimitL("ProduktLieferungen") + "." + DBKernel.delimitL("Charge") + "=" + DBKernel.delimitL("ChargenVerbindungen") + "." + DBKernel.delimitL("Produkt");
			try {
				ResultSet rs = db.pushQuery(sql);
				while (rs.next()) {
					MyDelivery mdZ = allDeliveries.get(rs.getInt(1));
					MyDelivery mdP = allDeliveries.get(rs.getInt(2));
					if (mdZ != null) mdZ.addNext(mdP.getId());
					if (mdP != null) mdP.addPrevious(mdZ.getId());
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		MyNewTracing mnt = new MyNewTracing(allDeliveries, caseStations, ccStations, caseSum);
		return mnt;
	}
	private static void assumeCC(Hsqldbiface db, boolean enforceTemporalOrder) {
		try {
			db.pushUpdate("DROP TABLE KRISECCTMP IF EXISTS");
			String sql = "CREATE TEMPORARY TABLE KRISECCTMP AS (" +
					"SELECT " + DBKernel.delimitL("Lieferungen") + "." + DBKernel.delimitL("ID") + "," +
					DBKernel.delimitL("Produktkatalog") + "." + DBKernel.delimitL("Station") + "," +
					DBKernel.delimitL("Lieferungen") + "." + DBKernel.delimitL("dd_day") + "," +
					DBKernel.delimitL("Lieferungen") + "." + DBKernel.delimitL("dd_month") + "," +
					DBKernel.delimitL("Lieferungen") + "." + DBKernel.delimitL("dd_year") +
					" FROM " + DBKernel.delimitL("Lieferungen") +
	    			" LEFT JOIN " + DBKernel.delimitL("Chargen") +
	    			" ON " + DBKernel.delimitL("Lieferungen") + "." + DBKernel.delimitL("Charge") + "=" + DBKernel.delimitL("Chargen") + "." + DBKernel.delimitL("ID") +
	    			" LEFT JOIN " + DBKernel.delimitL("Produktkatalog") +
	    			" ON " + DBKernel.delimitL("Chargen") + "." + DBKernel.delimitL("Artikel") + "=" + DBKernel.delimitL("Produktkatalog") + "." + DBKernel.delimitL("ID") +
	    			") WITH DATA";
			db.pushUpdate(sql);
		}
		catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		for (Integer id : allDeliveries.keySet()) {
			MyDelivery md = allDeliveries.get(id);
			String sql = "SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL("Lieferungen") +
					" WHERE " + DBKernel.delimitL("Empfänger") + "=" + md.getSupplierID() +
					(!enforceTemporalOrder || md.getDeliveryYear() == null ? "" : 
						" AND " +
							"(" + DBKernel.delimitL("dd_year") + " IS NULL" +
							" OR " + DBKernel.delimitL("dd_year") + " <" + md.getDeliveryYear() + ")"
							
							+ " OR " + DBKernel.delimitL("dd_year") + " =" + md.getDeliveryYear() +
							" AND (" + (md.getDeliveryMonth() == null ? "TRUE" : "FALSE") +
							" OR " + DBKernel.delimitL("dd_month") + " IS NULL" +
							" OR " + DBKernel.delimitL("dd_month") + "<" + (md.getDeliveryMonth() == null ? 13 : md.getDeliveryMonth()) + ")"
							
							+ " OR " + DBKernel.delimitL("dd_month") + " =" + md.getDeliveryMonth() +
							" AND (" + (md.getDeliveryDay() == null ? "TRUE" : "FALSE") +
							" OR " + DBKernel.delimitL("dd_day") + " IS NULL" +
							" OR " + DBKernel.delimitL("dd_day") + "<=" + (md.getDeliveryDay() == null ? 32 : md.getDeliveryDay()) + ")");
			
			try {
				ResultSet rs = db.pushQuery(sql);
				while (rs.next()) {
					MyDelivery md1 = allDeliveries.get(rs.getInt(1));
					md.addPrevious(md1.getId());
					md1.addNext(md.getId());
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}

			sql = "SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL("KRISECCTMP") +
	    			" WHERE " + DBKernel.delimitL("Station") + "=" + md.getRecipientID() +
	    			(!enforceTemporalOrder || md.getDeliveryYear() == null ? "" : 
						" AND " +
						"(" + DBKernel.delimitL("dd_year") + " IS NULL" +
						" OR " + DBKernel.delimitL("dd_year") + " >" + md.getDeliveryYear() + ")"
						
						+ " OR " + DBKernel.delimitL("dd_year") + " =" + md.getDeliveryYear() +
						" AND (" + (md.getDeliveryMonth() == null ? "TRUE" : "FALSE") +
						" OR " + DBKernel.delimitL("dd_month") + " IS NULL" +
						" OR " + DBKernel.delimitL("dd_month") + ">" + (md.getDeliveryMonth() == null ? -1 : md.getDeliveryMonth()) + ")"
						
						+ " OR " + DBKernel.delimitL("dd_month") + " =" + md.getDeliveryMonth() +
						" AND (" + (md.getDeliveryDay() == null ? "TRUE" : "FALSE") +
						" OR " + DBKernel.delimitL("dd_day") + " IS NULL" +
						" OR " + DBKernel.delimitL("dd_day") + ">=" + (md.getDeliveryDay() == null ? -1 : md.getDeliveryDay()) + ")");
					
			try {
				ResultSet rs = db.pushQuery(sql);
				while (rs.next()) {
					MyDelivery md1 = allDeliveries.get(rs.getInt(1));
					if (md1 != null) md1.addPrevious(md.getId());
					if (md != null) md.addNext(md1.getId());
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}		
	}
}
