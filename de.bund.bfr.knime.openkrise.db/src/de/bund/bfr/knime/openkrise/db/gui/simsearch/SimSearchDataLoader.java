package de.bund.bfr.knime.openkrise.db.gui.simsearch;

import java.sql.Array;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  
  private void loadData() throws SQLException {
	  switch(simSet.getType()) {
	  case STATION:
    	loadTableData(DBInfo.TABLE.STATION, simSet.getIDList(), null);
    }
  }
  
  private void loadStationData() {
	  
  }
  
  private void loadTableData(DBInfo.TABLE table, List<String> idList, List<DBInfo.COLUMN> childColumns) throws SQLException {
	  MyDBI myDBI = DBKernel.myDBi;
	  Connection con = (myDBI==null?null:myDBI.getConn());
	  if (con != null) {
		  //if(preparedStatementLoadTableData==null) 
		  //this.preparedStatementLoadTableData = con.prepareStatement("Select * from " + table.getName() + " where ID IN (?)") ;
		  this.preparedStatementLoadTableData = con.prepareStatement(myDBI.getTable(table.getName()).getSelectSQL() + " where ID IN (?)");
//				  ResultSet.TYPE_SCROLL_INSENSITIVE, 
//				  ResultSet.CONCUR_READ_ONLY);
		  //preparedStatementLoadTableData.
		  Array array = con.createArrayOf("VARCHAR", idList.toArray()); //      new Object[]{"1", "2","3"});
		  preparedStatementLoadTableData.setArray(1, array);
		  ResultSet rs = preparedStatementLoadTableData.executeQuery();
		  //rs.last();
		  //this.rowCount = rs.getRow();
		  //rs.beforeFirst();
		  //this.rowCount = rs.
		  ResultSetMetaData rsmd = rs.getMetaData();
		  this.columnCount = rsmd.getColumnCount();
		  this.columnNames = new String[this.columnCount];
		  this.columnClasses = new Class<?>[this.columnCount];
		  for(int i=0; i<this.columnCount; ++i) {
			  this.columnNames[i] = rsmd.getColumnName(i);
			  if(!this.typeMap.containsKey(rsmd.getColumnType(i))) throw(new SQLException("sdsdksdkj"));
			  this.columnClasses[i] = this.typeMap.get(rsmd.getColumnType(i));
		  }
		  this.data = new Object[simSet.getIDList().size()][this.columnCount];
		  int row = -1;
		  while(rs.next()) {
			  ++row;
			  for(int column=0; column < this.columnCount; ++column) this.data[row][column] = this.columnClasses[column].cast(rs.getObject(column));
		  }
		  rs.close();
		  
	  }
  }
}
