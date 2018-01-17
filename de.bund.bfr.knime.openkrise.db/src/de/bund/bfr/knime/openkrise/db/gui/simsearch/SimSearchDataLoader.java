package de.bund.bfr.knime.openkrise.db.gui.simsearch;

import java.sql.Array;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.common.collect.Sets;
import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;
import de.bund.bfr.knime.openkrise.db.MyTable;
import de.bund.bfr.knime.openkrise.db.gui.simsearch.SimSearch.SimSet;

public class SimSearchDataLoader {
  private static final Map<Integer, Class<?>> typeMap;
  
  public String[] columnNames;
  public Class<?>[] columnClasses;
  public Object[][] data;
  public int rowCount;
  public int columnCount;
  
  static {
	  typeMap = new HashMap<>();
	  Arrays.asList(Types.VARCHAR,Types.NVARCHAR,Types.BLOB, Types.CHAR, Types.LONGNVARCHAR, Types.LONGVARCHAR, Types.NCHAR).forEach(i -> typeMap.put(i, String.class));
	  Arrays.asList(Types.BIGINT, Types.INTEGER, Types.SMALLINT, Types.TINYINT).forEach(i -> typeMap.put(i, Integer.class));
	  Arrays.asList(Types.DECIMAL, Types.DOUBLE, Types.FLOAT, Types.NUMERIC, Types.REAL).forEach(i -> typeMap.put(i, Double.class));
	  Arrays.asList(Types.DATE).forEach(i -> typeMap.put(i, Date.class));
  }
  private SimSearch.SimSet simSet;
  private SimSearchDataManipulationHandler dataManipulationHandler;
  private PreparedStatement preparedStatementLoadTableData;
  
  public SimSearchDataLoader(SimSearch.SimSet simSet, SimSearchDataManipulationHandler dataManipulationHandler) {
    this.simSet = simSet;
    this.dataManipulationHandler = dataManipulationHandler;
  }
  
  public void loadData() throws Exception {
      
	  switch(simSet.getType()) {
	  case STATION:
    	loadTableData(DBInfo.TABLE.STATION, simSet.getIdList(), null);
    	break;
      default:
        throw(new SQLException("Unkown SimSet Type: " + simSet.getType()));
    }
  }
  
  private void loadTableData(DBInfo.TABLE table, List<Integer> idList, List<DBInfo.COLUMN> childColumns) throws Exception {
     {
      MyDBI myDBI = DBKernel.myDBi;
      Connection con = DBKernel.getDBConnection();
      MyTable myTable = null;

      //Connection con = (myDBI==null?null:myDBI.getConn(true));
      if (myDBI!=null && con != null) {
        Map<Integer, Object[]> idToDataMap = new HashMap<>();
        List<Integer> ids = new ArrayList<>(idList); 
        //if(preparedStatementLoadTableData==null) 
        //this.preparedStatementLoadTableData = con.prepareStatement("Select * from " + table.getName() + " where ID IN (?)") ;
        //this.preparedStatementLoadTableData = con.prepareStatement(myDBI.getTable(table.getName()).getSelectSQL() + " where "  + DBKernel.delimitL(table.getPrimaryKey().getName()) + " IN (?)");
        //this.preparedStatementLoadTableData = con.prepareStatement(myDBI.getTable(table.getName()).getSelectSQL() + " where "  + DBKernel.delimitL(table.getPrimaryKey().getName()) + " IN (" , ")");
        //				  ResultSet.TYPE_SCROLL_INSENSITIVE, 
        //				  ResultSet.CONCUR_READ_ONLY);
        //preparedStatementLoadTableData.
        //Array array = con.createArrayOf("INTEGER", idList.toArray()); //      new Object[]{"1", "2","3"});
        //preparedStatementLoadTableData.set
        //preparedStatementLoadTableData.setArray(1, array);
        String sql = myDBI.getTable(table.getName()).getSelectSQL() + " where "  + DBKernel.delimitL(table.getPrimaryKey().getName()) + " IN (" + String.join(", ", idList.stream().map(e->e.toString()).collect(Collectors.toList())) +  ")";
        //ResultSet rs = preparedStatementLoadTableData.executeQuery();
        
        ResultSet rs = DBKernel.getResultSet(sql, false);
        //rs.last();
        //this.rowCount = rs.getRow();
        //rs.beforeFirst();
        //this.rowCount = rs.
        ResultSetMetaData rsmd = rs.getMetaData();
        this.columnCount = rsmd.getColumnCount();
        this.columnNames = new String[this.columnCount+1];
        this.columnNames[0] = "Status";
        
        this.columnClasses = new Class<?>[this.columnCount+1];
        this.columnClasses[0] = Integer.class;
        
        for(int i=0; i<this.columnCount; ++i) {
          this.columnNames[i+1] = rsmd.getColumnName(i+1);
          if(!this.typeMap.containsKey(rsmd.getColumnType(i+1))) throw(new SQLException("Unkown DB Type: " + rsmd.getColumnType(i+1)));
          this.columnClasses[i+1] = this.typeMap.get(rsmd.getColumnType(i+1));
        }
        //this.data = new Object[simSet.getIdList().size()][]; //this.columnCount+1];
        //int row = -1;
        if (rs != null && rs.first()) {
          do {
            //++row;
            Object[] data = new Object[this.columnCount+1];
            //for(int column=0; column < this.columnCount; ++column) this.data[row][column+1] = this.columnClasses[column+1].cast(rs.getObject(column+1));
            for(int column=0; column < this.columnCount; ++column) data[column+1] = this.columnClasses[column+1].cast(rs.getObject(column+1));
            idToDataMap.put((Integer) rs.getObject(table.getPrimaryKey().getName()), data);
          } while(rs.next());
          rs.close();
        }
        this.data = new Object[idToDataMap.size()][]; //this.columnCount+1];
        
        Set<Integer> missingIds = Sets.difference(new HashSet<>(idList), idToDataMap.keySet());
        ids.removeAll(missingIds);
        
        int row = -1;
        for(Integer id: ids) {
          ++row;
          this.data[row] = idToDataMap.get(id);
        }
      } else throw(new Exception("DBConnection was not available."));
    }
  }
}
