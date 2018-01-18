package de.bund.bfr.knime.openkrise.db.gui.simsearch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import de.bund.bfr.knime.openkrise.db.DBKernel;

public class SimSearchDataSource extends SimSearch.DataSource{
  private SimSearch.Settings settings;
  
  protected SimSearchDataSource(SimSearch.DataSource.DataSourceListener listener) {
    super(listener);
  }
  
  @Override
  public void findSimilarities(SimSearch.Settings settings) throws Exception {
    if(this.addLDFunctionToDB(settings)) {
      if(!this.getSearchStopped() && settings.isChecked(SimSearch.SimSet.Type.STATION)) findSimilarStations(settings);
      if(!this.getSearchStopped() && settings.isChecked(SimSearch.SimSet.Type.PRODUCT)) findSimilarProducts(settings);
      if(!this.getSearchStopped() && settings.isChecked(SimSearch.SimSet.Type.LOT)) findSimilarLots(settings);
      if(!this.getSearchStopped() && settings.isChecked(SimSearch.SimSet.Type.DELIVERY)) findSimilarDeliveries(settings);
      this.removeLDFunctionFromDB();
    }
  }
    
  private boolean addLDFunctionToDB(SimSearch.Settings settings) {
    return DBKernel.sendRequest(
        "CREATE FUNCTION LD(x VARCHAR(255), y VARCHAR(255))\n" +
        (settings.getUseLevenshtein() ? "RETURNS INT\n" : "RETURNS DOUBLE\n") + 
        "NO SQL\n" +
        "LANGUAGE JAVA\n" +
        "PARAMETER STYLE JAVA\n" +
        (settings.getUseLevenshtein() ? "EXTERNAL NAME 'CLASSPATH:de.bund.bfr.knime.openkrise.db.Levenshtein.LD'" :
        "EXTERNAL NAME 'CLASSPATH:de.bund.bfr.knime.openkrise.db.StringSimilarity.diceCoefficientOptimized'") // diceCoefficientOptimized compareStringsStrikeAMatch
        , false, true) &&
        DBKernel.sendRequest(
                "GRANT EXECUTE ON FUNCTION LD TO " +
        DBKernel.delimitL("WRITE_ACCESS") + "," + DBKernel.delimitL("SUPER_WRITE_ACCESS"),
        false, true); 
  }
  
  private void removeLDFunctionFromDB() {
    DBKernel.sendRequest("DROP FUNCTION IF EXISTS LD", false, true);
  }
  
  private class SimCheck {
    private final String columnName;
    private final int maxScore;
    
    private SimCheck(DBInfo.COLUMN column, int maxScore) {
      this.columnName = column.getName();
      this.maxScore = maxScore;
    }
  }
  
  private void findSimilarStations(SimSearch.Settings settings) throws Exception {
    List<SimCheck> simCheckList = new ArrayList<>(Arrays.asList(new SimCheck(DBInfo.COLUMN.STATION_NAME, settings.getTreshold(SimSearch.Settings.Attribute.StationName))));
    
    if(settings.getUseAllInOneAddress()) simCheckList.add(new SimCheck(DBInfo.COLUMN.STATION_ADDRESS, settings.getTreshold(SimSearch.Settings.Attribute.StationAddress)));
    else simCheckList.addAll(Arrays.asList(
              new SimCheck(DBInfo.COLUMN.STATION_ZIP, settings.getTreshold(SimSearch.Settings.Attribute.StationZip)),
              new SimCheck(DBInfo.COLUMN.STATION_STREET, settings.getTreshold(SimSearch.Settings.Attribute.StationStreet)),
              new SimCheck(DBInfo.COLUMN.STATION_HOUSENUMBER, settings.getTreshold(SimSearch.Settings.Attribute.StationHousenumber)),
              new SimCheck(DBInfo.COLUMN.STATION_CITY, settings.getTreshold(SimSearch.Settings.Attribute.StationCity))));
    
    this.findSimilaritiesInTable(
          DBInfo.TABLE.STATION, 
          simCheckList,
          null, null, null, SimSearch.SimSet.Type.STATION, settings);
  }
  
  private void findSimilarProducts(SimSearch.Settings settings) throws Exception {
    this.findSimilaritiesInTable(
          DBInfo.TABLE.PRODUCT,
          Arrays.asList(
              new SimCheck(DBInfo.COLUMN.PRODUCT_STATION, settings.getTreshold(SimSearch.Settings.Attribute.ProductStation)),
              new SimCheck(DBInfo.COLUMN.PRODUCT_DESCRIPTION, settings.getTreshold(SimSearch.Settings.Attribute.ProductDescription))),
          null, null, null, SimSearch.SimSet.Type.PRODUCT, settings);
  }
  
  private void findSimilarLots(SimSearch.Settings settings) throws Exception {
    this.findSimilaritiesInTable(
          DBInfo.TABLE.LOT,
          Arrays.asList(
              new SimCheck(DBInfo.COLUMN.LOT_PRODUCT, settings.getTreshold(SimSearch.Settings.Attribute.LotProduct)),
              new SimCheck(DBInfo.COLUMN.LOT_NUMBER, settings.getTreshold(SimSearch.Settings.Attribute.LotNumber))),
          null, null, null, SimSearch.SimSet.Type.LOT, settings);
  }
  
  private void findSimilarDeliveries(SimSearch.Settings settings) throws Exception {
    List<SimCheck> simCheckList = new ArrayList<>(Arrays.asList(
        new SimCheck(DBInfo.COLUMN.DELIVERY_LOT, settings.getTreshold(SimSearch.Settings.Attribute.DeliveryLot)),
        new SimCheck(DBInfo.COLUMN.DELIVERY_RECIPIENT, settings.getTreshold(SimSearch.Settings.Attribute.DeliveryRecipient))));
    
    
    if(settings.getUseArrivedDate()) simCheckList.addAll(Arrays.asList(
        new SimCheck(DBInfo.COLUMN.DELIVERY_ARIVEDON_DAY, settings.getTreshold(SimSearch.Settings.Attribute.DeliveryDate)),
        new SimCheck(DBInfo.COLUMN.DELIVERY_ARIVEDON_MONTH, settings.getTreshold(SimSearch.Settings.Attribute.DeliveryDate)),
        new SimCheck(DBInfo.COLUMN.DELIVERY_ARIVEDON_YEAR, settings.getTreshold(SimSearch.Settings.Attribute.DeliveryDate))));
    else simCheckList.addAll(Arrays.asList(
        new SimCheck(DBInfo.COLUMN.DELIVERY_DELIVEREDON_DAY, settings.getTreshold(SimSearch.Settings.Attribute.DeliveryDate)),
        new SimCheck(DBInfo.COLUMN.DELIVERY_DELIVEREDON_MONTH, settings.getTreshold(SimSearch.Settings.Attribute.DeliveryDate)),
        new SimCheck(DBInfo.COLUMN.DELIVERY_DELIVEREDON_YEAR, settings.getTreshold(SimSearch.Settings.Attribute.DeliveryDate))));
    
    this.findSimilaritiesInTable(
        DBInfo.TABLE.DELIVERY, 
        simCheckList,
        null, null, null, SimSearch.SimSet.Type.DELIVERY, settings);
  }
  
  private void findSimilaritiesInTable(DBInfo.TABLE table, List<SimCheck> simCheckList,
      String otherTable, String otherTableField, String[] otherTableDesires, SimSearch.SimSet.Type simSetType, SimSearch.Settings settings) throws Exception {
      
      int simCheckCount = simCheckList.size();
      
      StringBuilder sqlSb = new StringBuilder("SELECT " + DBKernel.delimitL("ID"));
      //String sql = "SELECT " + DBKernel.delimitL("ID");
      for(SimCheck simCheck : simCheckList) sqlSb.append("," + DBKernel.delimitL(simCheck.columnName));
      //for (int i=0;i<fieldnames.length;i++) sql += "," + DBKernel.delimitL(fieldnames[i]);
      sqlSb.append(" FROM " + DBKernel.delimitL(table.getName()));
      //sql += " FROM " + DBKernel.delimitL(tablename);
      ResultSet rs = DBKernel.getResultSet(sqlSb.toString(), false);
      //ResultSet rs = DBKernel.getResultSet(sql, false);
      if (rs != null && rs.first()) {
          do {
            if(this.getSearchStopped()) return;
            
            String[] resRowFirst = new String[simCheckCount + 1 + (otherTableDesires == null ? 0 : otherTableDesires.length + 1)];
              //String[] resRowFirst = new String[fieldnames.length + 1 + (otherTableDesires == null ? 0 : otherTableDesires.length + 1)];

              // Firstly - fieldnames
              int id = rs.getInt("ID");
              List<Integer> idList = new ArrayList<>(); //Arrays.asList(id);
              idList.add(id);
              resRowFirst[0] = id+"";
              String result = ""+id;
              Object[] fieldVals = new Object[simCheckCount];
              //Object[] fieldVals = new Object[fieldnames.length];
              boolean go4Row = false;
              for (int i=0;i<simCheckCount;i++) {
              //for (int i=0;i<fieldnames.length;i++) {
                  fieldVals[i] = rs.getObject(simCheckList.get(i).columnName);
                  //fieldVals[i] = rs.getObject(fieldnames[i]);
                  if (fieldVals[i] != null) fieldVals[i] = fieldVals[i].toString().replace("'", "''");
                  result += "\t" + fieldVals[i];
                  resRowFirst[i+1] = fieldVals[i]+"";
                  if (fieldVals[i] != null && !fieldVals[i].toString().trim().isEmpty()) go4Row = true;
              }
              if (!go4Row) continue;
              
              // Firstly - otherTableDesires
              if (otherTable != null) {
                  result += " (" + otherTable + ": ";
                  sqlSb = new StringBuilder("SELECT " + DBKernel.delimitL("ID"));
                  //sql = "SELECT " + DBKernel.delimitL("ID");
                  for (int i=0;i<otherTableDesires.length;i++) sqlSb.append("," + DBKernel.delimitL(otherTableDesires[i]));
                  //for (int i=0;i<otherTableDesires.length;i++) sql += "," + DBKernel.delimitL(otherTableDesires[i]);
                  sqlSb.append(" FROM " + DBKernel.delimitL(otherTable) + " WHERE " + DBKernel.delimitL(otherTableField) + "=" + id);
                  //sql += " FROM " + DBKernel.delimitL(otherTable) + " WHERE " + DBKernel.delimitL(otherTableField) + "=" + id;
                  ResultSet rs3 = DBKernel.getResultSet(sqlSb.toString(), false);
                  //ResultSet rs3 = DBKernel.getResultSet(sql, false);
                  if (rs3 != null && rs3.first()) {
                      do {
                          result += rs3.getInt("ID");
                          if (resRowFirst[simCheckCount+1] == null || resRowFirst[simCheckCount+1].isEmpty()) resRowFirst[simCheckCount+1] = rs3.getInt("ID")+"";
                          //if (resRowFirst[fieldnames.length+1] == null || resRowFirst[fieldnames.length+1].isEmpty()) resRowFirst[fieldnames.length+1] = rs3.getInt("ID")+"";
                          else resRowFirst[simCheckCount+1] += "," + rs3.getInt("ID")+"";
                          //else resRowFirst[fieldnames.length+1] += "," + rs3.getInt("ID")+"";
                          for (int i=0;i<otherTableDesires.length;i++) {
                              result += "\t" + rs3.getString(otherTableDesires[i]);
                              if (resRowFirst[simCheckCount+2+i] == null || resRowFirst[simCheckCount+2+i].isEmpty()) resRowFirst[simCheckCount+2+i] = rs3.getString(otherTableDesires[i]);
                              //if (resRowFirst[fieldnames.length+2+i] == null || resRowFirst[fieldnames.length+2+i].isEmpty()) resRowFirst[fieldnames.length+2+i] = rs3.getString(otherTableDesires[i]);
                              else resRowFirst[simCheckCount+2+i] += "," + rs3.getString(otherTableDesires[i]);
                              //else resRowFirst[fieldnames.length+2+i] += "," + rs3.getString(otherTableDesires[i]);
                          }
                      } while(rs3.next());
                  }
                  result += ")";
              }
              
              result += "\n";
              
              sqlSb = new StringBuilder("SELECT " + DBKernel.delimitL("ID"));
              //sql = "SELECT " + DBKernel.delimitL("ID");
              for(SimCheck simCheck : simCheckList) sqlSb.append("," + DBKernel.delimitL(simCheck.columnName));
              //for (int i=0;i<fieldnames.length;i++) sql += "," + DBKernel.delimitL(fieldnames[i]);
              for (int i=0;i<simCheckCount;i++) {
              //for (int i=0;i<fieldnames.length;i++) {
                if (settings.getUseLevenshtein()) {
                  //if (useLevenshtein) {
                      sqlSb.append((fieldVals[i] == null) ? ",0 AS SCORE" + i : "," + DBKernel.delimitL("LD") + "(" + (fieldVals[i] == null ? "NULL" : "'" + fieldVals[i].toString().toUpperCase() + "'") + ",UCASE(CAST(" + DBKernel.delimitL(simCheckList.get(i).columnName) + " AS VARCHAR(255))))" + " AS SCORE" + i);
                      //sql += (fieldVals[i] == null) ? ",0 AS SCORE" + i : "," + DBKernel.delimitL("LD") + "(" + (fieldVals[i] == null ? "NULL" : "'" + fieldVals[i].toString().toUpperCase() + "'") + ",UCASE(CAST(" + DBKernel.delimitL(fieldnames[i]) + " AS VARCHAR(255))))" + " AS SCORE" + i;
                  }
                  else {
                    if(simCheckList.get(i).maxScore>0) {
                      //if (maxScores[i] > 0) {
                      sqlSb.append((fieldVals[i] == null) ? ",1 AS SCORE" + i : "," + DBKernel.delimitL("LD") + "(" + (fieldVals[i] == null ? "NULL" : "'" + fieldVals[i].toString().toUpperCase() + "'") + ",UCASE(CAST(" + DBKernel.delimitL(simCheckList.get(i).columnName) + " AS VARCHAR(255))))" + " AS SCORE" + i);
                          //sql += (fieldVals[i] == null) ? ",1 AS SCORE" + i : "," + DBKernel.delimitL("LD") + "(" + (fieldVals[i] == null ? "NULL" : "'" + fieldVals[i].toString().toUpperCase() + "'") + ",UCASE(CAST(" + DBKernel.delimitL(fieldnames[i]) + " AS VARCHAR(255))))" + " AS SCORE" + i;                                                    
                      }
                  }
              }
              sqlSb.append(" FROM " + DBKernel.delimitL(table.getName()) + " WHERE " + DBKernel.delimitL("ID") + " > " + id);
              //sql += " FROM " + DBKernel.delimitL(tablename) + " WHERE " + DBKernel.delimitL("ID") + " > " + id;
              for (int i=0;i<simCheckCount;i++) {
              //for (int i=0;i<fieldnames.length;i++) {
                if(settings.getUseLevenshtein()) {
                  //if (useLevenshtein) {
                  sqlSb.append((fieldVals[i] == null) ? " AND TRUE" : " AND " + DBKernel.delimitL("LD") + "(" + (fieldVals[i] == null ? "NULL" : "'" + fieldVals[i].toString().toUpperCase() + "'") + ",UCASE(CAST(" + DBKernel.delimitL(simCheckList.get(i).columnName) + " AS VARCHAR(255)))) <= " + simCheckList.get(i).maxScore);
                      //sql += (fieldVals[i] == null) ? " AND TRUE" : " AND " + DBKernel.delimitL("LD") + "(" + (fieldVals[i] == null ? "NULL" : "'" + fieldVals[i].toString().toUpperCase() + "'") + ",UCASE(CAST(" + DBKernel.delimitL(fieldnames[i]) + " AS VARCHAR(255)))) <= " + maxScores[i];
                  }
                  else {
                    if (simCheckList.get(i).maxScore>0) {
                      //if (maxScores[i] > 0) {
                      sqlSb.append((fieldVals[i] == null) ? " AND TRUE" : " AND " + DBKernel.delimitL("LD") + "(" + (fieldVals[i] == null ? "NULL" : "'" + fieldVals[i].toString().toUpperCase() + "'") + ",UCASE(CAST(" + DBKernel.delimitL(simCheckList.get(i).columnName) + " AS VARCHAR(255)))) >= " + (simCheckList.get(i).maxScore / 100.0));
                          //sql += (fieldVals[i] == null) ? " AND TRUE" : " AND " + DBKernel.delimitL("LD") + "(" + (fieldVals[i] == null ? "NULL" : "'" + fieldVals[i].toString().toUpperCase() + "'") + ",UCASE(CAST(" + DBKernel.delimitL(fieldnames[i]) + " AS VARCHAR(255)))) >= " + (maxScores[i] / 100.0);
                          //sql += (gentle && fieldVals[i] == null) ? " AND TRUE" : " AND SCORE" + i + " >= 0.7";
                      }
                  }
              }
              //sql += " ORDER BY SCORE ASC";
              
              ResultSet rs2 = DBKernel.getResultSet(sqlSb.toString(), false);
              //ResultSet rs2 = DBKernel.getResultSet(sql, false);
              if (rs2 != null && rs2.first()) {
                  LinkedHashSet<String[]> resSetOther = new LinkedHashSet<>(); 
                  do {
                      
                      String[] resRowOther = new String[simCheckCount + 1 + (otherTableDesires == null ? 0 : otherTableDesires.length + 1)];
                      //String[] resRowOther = new String[fieldnames.length + 1 + (otherTableDesires == null ? 0 : otherTableDesires.length + 1)];

                      // Match - fieldnames
                      result += rs2.getInt("ID");
                      resRowOther[0] = rs2.getInt("ID")+"";
                      idList.add(rs2.getInt("ID"));
                                          
                      for (int i=0;i<simCheckCount;i++) {
                      //for (int i=0;i<fieldnames.length;i++) {
                        result += "\t" + rs2.getString(simCheckList.get(i).columnName);
                          //result += "\t" + rs2.getString(fieldnames[i]);
                        resRowOther[i+1] = rs2.getString(simCheckList.get(i).columnName);
                          //resRowOther[i+1] = rs2.getString(fieldnames[i]);
                      }
                      for (int i=0;i<simCheckCount;i++) {
                      //for (int i=0;i<fieldnames.length;i++) {
                        if (settings.getUseLevenshtein() || simCheckList.get(i).maxScore > 0) result += "\t" + rs2.getDouble("SCORE" + i);
                          //if (useLevenshtein || maxScores[i] > 0) result += "\t" + rs2.getDouble("SCORE" + i);
                      }
                      
                      // Match - otherTableDesires
                      if (otherTable != null) {
                          result += " (" + otherTable + ": ";
                          sqlSb = new StringBuilder("SELECT " + DBKernel.delimitL("ID"));
                          //sql = "SELECT " + DBKernel.delimitL("ID");
                          for (int i=0;i<otherTableDesires.length;i++) sqlSb.append("," + DBKernel.delimitL(otherTableDesires[i]));
                          //for (int i=0;i<otherTableDesires.length;i++) sql += "," + DBKernel.delimitL(otherTableDesires[i]);
                          sqlSb.append(" FROM " + DBKernel.delimitL(otherTable) + " WHERE " + DBKernel.delimitL(otherTableField) + "=" + rs2.getInt("ID"));
                          //sql += " FROM " + DBKernel.delimitL(otherTable) + " WHERE " + DBKernel.delimitL(otherTableField) + "=" + rs2.getInt("ID");
                          ResultSet rs3 = DBKernel.getResultSet(sqlSb.toString(), false);
                          //ResultSet rs3 = DBKernel.getResultSet(sql, false);
                          if (rs3 != null && rs3.first()) {
                              do {
                                  result += rs3.getInt("ID");
                                  //idList.add(rs3.getInt("ID"));
                                  if (resRowOther[simCheckCount+1] == null || resRowOther[simCheckCount+1].isEmpty()) resRowOther[simCheckCount+1] = rs3.getInt("ID")+"";
                                  //if (resRowOther[fieldnames.length+1] == null || resRowOther[fieldnames.length+1].isEmpty()) resRowOther[fieldnames.length+1] = rs3.getInt("ID")+"";
                                  else resRowOther[simCheckCount+1] += "," + rs3.getInt("ID")+"";
                                  //else resRowOther[fieldnames.length+1] += "," + rs3.getInt("ID")+"";
                                  for (int i=0;i<otherTableDesires.length;i++) {
                                      result += "\t" + rs3.getString(otherTableDesires[i]);
                                      if (resRowOther[simCheckCount+2+i] == null || resRowOther[simCheckCount+2+i].isEmpty()) resRowOther[simCheckCount+2+i] = rs3.getString(otherTableDesires[i]);
                                      //if (resRowOther[fieldnames.length+2+i] == null || resRowOther[fieldnames.length+2+i].isEmpty()) resRowOther[fieldnames.length+2+i] = rs3.getString(otherTableDesires[i]);
                                      else resRowOther[simCheckCount+2+i] += "," + rs3.getString(otherTableDesires[i]);
                                      //else resRowOther[fieldnames.length+2+i] += "," + rs3.getString(otherTableDesires[i]);
                                  }
                              } while(rs3.next());
                          }
                          result += ")";
                      }
                      
                      resSetOther.add(resRowOther);
                      
                      result += "\n";
                  } while(rs2.next());
                  //ldResult.put(resRowFirst, resSetOther);
                  if(idList.size()<2) throw(new SQLException("Implementation error."));
                  //if (ldResult.size() == 0) System.err.println(result);
                  this.newSimilarityMatchFound(simSetType, idList);
              }
          } while(rs.next());
      }       
      //return ldResult;
    }
      

  @Override
  public SimSearchDataLoader getDataLoader() {
    // TODO Auto-generated method stub
    return null;
  }

}
