package de.bund.bfr.knime.openkrise.db.gui.simsearch;

import java.io.Console;
import java.sql.Array;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Sets;
import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;
import de.bund.bfr.knime.openkrise.db.MyTable;
import de.bund.bfr.knime.openkrise.db.gui.simsearch.SimSearch.SimSet;

public class SimSearchDataLoader extends SimSearch.DataSource.DataLoader{
  private static final String ID_COLUMN_NAME = "ID";
  private static final Map<Integer, Class<?>> typeMap;
  private static final Map<String, String> tableToLabelColumnMap;
  private static final Map<String, SimSearch.SimSet.Type> tableToSimSetTypeMap;
  
  private Map<SimSearch.SimSet.Type, Map<Integer, String>> foreignDataCacheMap;
  
  public String[] columnNames;
  public Class<?>[] columnClasses;
  public Object[][] data;
  public int rowCount;
  public int columnCount;
  public SimSearch.SimSet.Type[] columnSimSetTypes;
  
  static {
	  typeMap = new HashMap<>();
	  Arrays.asList(Types.VARCHAR,Types.NVARCHAR,Types.BLOB, Types.CHAR, Types.LONGNVARCHAR, Types.LONGVARCHAR, Types.NCHAR).forEach(i -> typeMap.put(i, String.class));
	  Arrays.asList(Types.BIGINT, Types.INTEGER, Types.SMALLINT, Types.TINYINT).forEach(i -> typeMap.put(i, Integer.class));
	  Arrays.asList(Types.DECIMAL, Types.DOUBLE, Types.FLOAT, Types.NUMERIC, Types.REAL).forEach(i -> typeMap.put(i, Double.class));
	  Arrays.asList(Types.DATE).forEach(i -> typeMap.put(i, Date.class));
	  
	  tableToLabelColumnMap = new HashMap<>();
	  tableToLabelColumnMap.put(DBInfo.TABLE.STATION.getName(), DBInfo.COLUMN.STATION_NAME.getName());
	  tableToLabelColumnMap.put(DBInfo.TABLE.PRODUCT.getName(), DBInfo.COLUMN.PRODUCT_DESCRIPTION.getName());
	  tableToLabelColumnMap.put(DBInfo.TABLE.MATRIX.getName(), DBInfo.COLUMN.MATRIX_NAME.getName());
	  tableToLabelColumnMap.put(DBInfo.TABLE.LOT.getName(), DBInfo.COLUMN.LOT_NUMBER.getName());
	  tableToLabelColumnMap.put(DBInfo.TABLE.AGENT.getName(), DBInfo.COLUMN.AGENT_NAME.getName());
	  
	  tableToSimSetTypeMap = new HashMap<>();
	  tableToSimSetTypeMap.put(DBInfo.TABLE.STATION.getName(), SimSearch.SimSet.Type.STATION);
	  tableToSimSetTypeMap.put(DBInfo.TABLE.PRODUCT.getName(), SimSearch.SimSet.Type.PRODUCT);
	  
	  //tableToLabelColumnMap.put(DBInfo.TABLE.LOT.getName(), DBInfo.COLUMN.LOT_.getName());
	  //tableToLabelColumnMap.put(DBInfo.TABLE.STATION.getName(), DBInfo.COLUMN.STATION_NAME.getName());
  }
  private SimSearch.SimSet simSet;
  private SimSearchDataManipulationHandler dataManipulationHandler;
  private PreparedStatement preparedStatementLoadTableData;
  
  public SimSearchDataLoader(SimSearch.SimSet simSet, SimSearchDataManipulationHandler dataManipulationHandler) {
    this.simSet = simSet;
    this.dataManipulationHandler = dataManipulationHandler;
    this.foreignDataCacheMap = new HashMap<>();
    for(SimSearch.SimSet.Type simSetType: SimSearch.SimSet.Type.class.getEnumConstants()) this.foreignDataCacheMap.put(simSetType, new HashMap<>());
  }
  
  public void loadData() throws Exception {
      
	  switch(simSet.getType()) {
	  case STATION:
    	loadTableData(DBInfo.TABLE.STATION, simSet.getIdList(), null);
    	break;
	  case PRODUCT:
        loadTableData(DBInfo.TABLE.PRODUCT, simSet.getIdList(), null);
        break;
	  case LOT:
        loadTableData(DBInfo.TABLE.LOT, simSet.getIdList(), null);
        break;
	  case DELIVERY:
        loadTableData(DBInfo.TABLE.DELIVERY, simSet.getIdList(), null);
        break;
      default:
        throw(new SQLException("Unkown SimSet Type: " + simSet.getType()));
    }
  }
  
  private Map<String, String> loadForeignFields(DBInfo.TABLE table) {
	  Map<String, String> result = new HashMap<>();
	  MyDBI myDBI = DBKernel.myDBi;
      MyTable myTable = myDBI.getTable(table.getName());
      MyTable[] foreignTables = myTable.getForeignFields();
      for(int i=0; i<foreignTables.length; ++i) if(foreignTables[i]!=null) result.put(myTable.getForeignFieldName(foreignTables[i]), foreignTables[i].getTablename());
      //String tmp2 = myTable.getForeignFieldName(tmp[0]);
      return result;
  }
  
  private void loadTableData(DBInfo.TABLE table, List<Integer> idList, List<DBInfo.COLUMN> childColumns) throws Exception {
     {
      MyDBI myDBI = DBKernel.myDBi;
      Connection con = DBKernel.getDBConnection();
      MyTable myTable = myDBI.getTable(table.getName());
      
      
      MyTable[] tmp = myTable.getForeignFields();
      String tmp2 = myTable.getForeignFieldName(tmp[0]);

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
        String sql = myDBI.getTable(table.getName()).getSelectSQL() + " where " + ID_COLUMN_NAME + " IN (" + String.join(", ", idList.stream().map(e->e.toString()).collect(Collectors.toList())) +  ")";
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
        
        this.columnSimSetTypes = new SimSearch.SimSet.Type[this.columnCount+1];
        
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
            idToDataMap.put((Integer) rs.getObject(ID_COLUMN_NAME), data);
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
        
        Map<String, String> foreignFieldsMap = loadForeignFields(table);
        for(Entry<String, String> foreignField: foreignFieldsMap.entrySet()) {
        	int columnIndex = ArrayUtils.indexOf(this.columnNames, foreignField.getKey());
        	if(columnIndex>0) {
        		Set<Integer> foreignIds = new HashSet<>();
        		for(int i = 0; i<this.data.length; ++i) if(this.data[i][columnIndex]!=null) foreignIds.add((Integer) this.data[i][columnIndex]);
        		Map<Integer, String> foreignDataMap = loadForeignData(foreignIds, foreignField.getValue());
        		SimSearch.SimSet.Type simSetType = tableToSimSetTypeMap.get(foreignField.getValue());
        		if(simSetType!=null) {
        			// sensitive field
        			this.cacheForeignData(foreignDataMap, simSetType);
        			for(int i = 0; i<this.data.length; ++i) if(this.data[i][columnIndex]!=null) this.data[i][columnIndex] = this.createForeignField((Integer) this.data[i][columnIndex], foreignDataMap.get(this.data[i][columnIndex]));
        			this.columnClasses[columnIndex] = SimSearch.DataSource.ForeignField.class;
        			this.columnSimSetTypes[columnIndex] = simSetType;
        		} else {
        			// simple foreignField
        			for(int i = 0; i<this.data.length; ++i) if(this.data[i][columnIndex]!=null) this.data[i][columnIndex] = foreignDataMap.get(this.data[i][columnIndex]);
        			this.columnClasses[columnIndex] = String.class;
        		}
        		
        	}
        }
      } else throw(new Exception("DBConnection was not available."));
    }
  }
  
  private void cacheForeignData(Map<Integer, String> foreignDataMap, SimSearch.SimSet.Type simSetType) {
	  for(Entry<Integer, String> entry: foreignDataMap.entrySet()) this.foreignDataCacheMap.get(simSetType).put(entry.getKey(), entry.getValue());
  }
  
  private Map<Integer, String> loadForeignData(Set<Integer> ids, String tableName) throws Exception {
	  Connection con = DBKernel.getDBConnection();
	  if(con==null) throw(new Exception("No database connection available."));
	  
	  final String RESET = "\033[0m";  // Text Reset

	    // Regular Colors
	  final String BLACK = "\033[0;30m";   // BLACK
	  final String RED = "\033[0;31m";     // RED
	  
	  if(SimSearchDataLoader.tableToLabelColumnMap.get(tableName)==null) {
		  MyTable tmp = DBKernel.myDBi.getTable(tableName);
		  String[] tmp2 = tmp.getFieldNames();
		  System.out.println(RED + "LabelColumn for table " + tableName + " is missing. " + RESET + "");
	  }
	  Map<Integer, String> result = new HashMap<>();
	  Statement statement = con.createStatement();
	  String sql = "SELECT ID, " + DBKernel.delimitL(SimSearchDataLoader.tableToLabelColumnMap.get(tableName)) + " " + 
			  "FROM " + DBKernel.delimitL(tableName) + " WHERE ID IN (" + String.join(", ", ids.stream().map(id -> id.toString()).collect(Collectors.toList())) + ")";
	  ResultSet resultSet = statement.executeQuery(sql);
	  
	  if(resultSet!=null) {
		  
		  while(resultSet.next()) result.put(resultSet.getInt(1), resultSet.getString(2));
			  
		  resultSet.close();
	  }
	  statement.close();
	  
	  return result;
  }
}
