package de.bund.bfr.knime.openkrise.db.gui.simsearch;

import java.io.Console;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Sets;
import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;
import de.bund.bfr.knime.openkrise.db.MyTable;
import de.bund.bfr.knime.openkrise.db.gui.simsearch.SimSearch.SimSet;

public class SimSearchDataLoader extends SimSearch.DataSource.DataLoader{
  private static final String ID_COLUMN_NAME = "ID";
  private static final int ID_COLUMN = 1;
  private static final Map<Integer, Class<?>> typeMap;
  //private static final Map<String, String> tableToLabelColumnMap;
  //private static final Map<String, SimSearch.SimSet.Type> tableToSimSetTypeMap;
  private static final Map<String, ForeignField> foreignKeyMap;
  
  private Map<SimSearch.SimSet.Type, Map<Integer, String>> foreignDataCacheMap;
  
  
  public String[] columnNames;
  public Class<?>[] columnClasses;
  public Object[][] data;
  public int rowCount;
  public int columnCount;
  public SimSearch.SimSet.Type[] columnSimSetTypes;
  
  private static class ForeignField {
    private String[] labelColumns;
    private Function<List<String>, String> formatFunction;
    private ParentTable parentTable;
    private ChildTable childTable;
    private Class<?> resultType;
    
    ForeignField(ParentTable parentTable, String[] labelColumns) {
      this("%s", labelColumns);
      this.parentTable = parentTable;
      this.resultType = String.class;
    }
    
    ForeignField(ParentTable parentTable, String formatString, String[] labelColumns) {
      this(formatString, labelColumns);
      this.parentTable = parentTable;
      this.resultType = String.class;
    }
    
    ForeignField(ParentTable parentTable, Function<List<String>, String> formatFunction, String[] labelColumns) {
      this(formatFunction, labelColumns);
      this.parentTable = parentTable;
      this.resultType = String.class;
    }
    
    
    ForeignField(ChildTable childTable, String[] labelColumns) {
      this("%s", labelColumns);
      this.childTable = childTable;
      this.resultType = List.class;
    }
    
    ForeignField(ChildTable childTable, String formatString, String[] labelColumns) {
      this(formatString, labelColumns);
      this.childTable = childTable;
      this.resultType = List.class;
    }
    
    ForeignField(ChildTable childTable, Function<List<String>, String> formatFunction, String[] labelColumns) {
      this(formatFunction, labelColumns);
      this.childTable = childTable;
      this.resultType = List.class;
    }
    
//    private ForeignField(String formatString, String labelColumn) {
//      this.labelColumns = new String[] {labelColumn};
//      this.formatFunction = new Function<String[], String>() {
//        public String apply(String[] arg) {
//          return String.format(formatString, (Object[]) arg);
//        }
//      };
//    }
    
    private ForeignField(String formatString, String[] labelColumns) {
      this.labelColumns = labelColumns;
      this.formatFunction = new Function<List<String>, String>() {
        public String apply(List<String> arg) {
          return String.format(formatString, arg.toArray());
        }
      };
    }
    
    private ForeignField(Function<List<String>, String> formatFunction, String[] labelColumns) {
      this.labelColumns = labelColumns;
      this.formatFunction = formatFunction;
    }
    
    private String createSql(int id, SimSearchDataManipulationHandler dataManipulations) {
      StringBuilder sb = new StringBuilder("Select " + String.join(", " + this.labelColumns.  DBKernel.delimitL(this.labelColumns)));
      if(parentTable!=null) {
       
      }
      return null;
    }
    
    private Class<?> getResultType() { return this.resultType; }
  }
  
  private static class ParentTable {
    
    private String tableName;
    private String tableColumn;
    private ParentTable parent;
    
    private ParentTable(String tableName) {
      this.tableName = tableName;
    }
    
    private ParentTable(String tableName,  String tableColumn, ParentTable parent) {
      this.tableName = tableName;
      this.tableColumn = tableColumn;
      this.parent = parent;
    }
  }
  
  private static class ChildTable {
    private String tableName;
    private String tableColumn;
    private String columnToParentTable;
    private ParentTable parentTable;
    private ChildTable childTable;
    
    private ChildTable(String tableName, String tableColumn) {
      this.tableName = tableName;
      this.tableColumn = tableColumn;
    }
    
    private ChildTable(String tableName, String tableColumn, ChildTable child) {
      this(tableName, tableColumn);
      this.childTable = childTable;
    }
    
    private ChildTable(String tableName, String tableColumn, String columnToParentTable, ParentTable parentTable) {
      this(tableName, tableColumn);
      this.columnToParentTable = columnToParentTable;
      this.parentTable = parentTable;
    }
  }
  
  static {
	  typeMap = new HashMap<>();
	  Arrays.asList(Types.VARCHAR,Types.NVARCHAR,Types.BLOB, Types.CHAR, Types.LONGNVARCHAR, Types.LONGVARCHAR, Types.NCHAR).forEach(i -> typeMap.put(i, String.class));
	  Arrays.asList(Types.BIGINT, Types.INTEGER, Types.SMALLINT, Types.TINYINT).forEach(i -> typeMap.put(i, Integer.class));
	  Arrays.asList(Types.DECIMAL, Types.DOUBLE, Types.FLOAT, Types.NUMERIC, Types.REAL).forEach(i -> typeMap.put(i, Double.class));
	  Arrays.asList(Types.DATE).forEach(i -> typeMap.put(i, Date.class));
	  
	  
	  
	  //tableToLabelColumnMap.put(DBInfo.TABLE.LOT.getName(), DBInfo.COLUMN.LOT_.getName());
	  //tableToLabelColumnMap.put(DBInfo.TABLE.STATION.getName(), DBInfo.COLUMN.STATION_NAME.getName());
	  
	  // create ForeignMaps
	  //foreignKeyMap.put(DBInfo.COLUMN.STATION_AGENTS.getName(), value
	  Map<String, ForeignField> map = null;
	  try {
        map = createForeignKeyMap();
      } catch (Exception e) {
        e.printStackTrace();
      }
	  foreignKeyMap = map;
  }
  
  private static Map<String, ForeignField> createForeignKeyMap() throws Exception {
    MyDBI myDBI = DBKernel.myDBi;
    if(myDBI==null) return null;
    
    Map<String, String> fkColumnToForeignTableMap = createFKColumnToForeignTableMap();
    Map<String, String[]> tableToLabelColumnMap = createTableToLabelColumnMap();
    Map<String, Map<String, String>> tableToChildTableMap = createTableToChildTableMap();
    Map<String, ForeignField> map = new HashMap<>();
    for(DBInfo.TABLE table : Arrays.asList(DBInfo.TABLE.STATION, DBInfo.TABLE.PRODUCT, DBInfo.TABLE.LOT, DBInfo.TABLE.DELIVERY)) {
      
      MyTable myTable = DBKernel.myDBi.getTable(table.getName());
      if(myTable==null) return null;
      
      for(MyTable foreignTable: myTable.getForeignFields()) {
        if(foreignTable!=null) {
          String foreignColumn = myTable.getForeignFieldName(foreignTable);
          String fullForeignColumn = myTable.getTablename() + "." + foreignColumn;
          
          if(fkColumnToForeignTableMap.containsKey(fullForeignColumn)) {
            // this might be a parent, a grandparent, ....
            if(fkColumnToForeignTableMap.get(fullForeignColumn).equals(foreignTable.getTablename())) {
              // this is a simple parent relation
              map.put(fullForeignColumn, new ForeignField(new ParentTable(foreignTable.getTablename()), tableToLabelColumnMap.get(foreignTable.getTablename())));
            } else {
              // this might be a grandparent relation
            } 
          } else if(tableToChildTableMap.containsKey(myTable.getTablename()))  {
            Map<String, String> tableToColumnMap = tableToChildTableMap.get(myTable.getTablename());
            if(tableToColumnMap.containsKey(foreignTable.getTablename())) {
              // child table
              map.put(fullForeignColumn, new ForeignField(new ChildTable(foreignTable.getTablename(), tableToColumnMap.get(foreignTable.getTablename())), tableToLabelColumnMap.get(foreignTable.getTablename())));
            } else if(tableToChildTableMap.containsKey(foreignTable.getTablename())) {
              // this might be a n to m relation
              Set<String> sharedChilds = Sets.intersection(tableToChildTableMap.get(foreignTable.getTablename()).keySet(), tableToColumnMap.keySet());
              if(sharedChilds.size()==1) {
                // n to m relation detected
                String sharedTable = sharedChilds.stream().collect(Collectors.toList()).get(0);
                map.put(fullForeignColumn, new ForeignField(
                    new ChildTable(sharedTable, tableToColumnMap.get(sharedTable), tableToChildTableMap.get(foreignTable.getTablename()).get(sharedTable),new ParentTable(foreignTable.getTablename())),
                    tableToLabelColumnMap.get(foreignTable.getTablename())));
              } else {
                // unknown relation
              }
            } else {
              // unknown relation
            }
          } else {
            // unknown relation
          }
        }
      }
    }
    return map;
  }
  
  private static Map<String, String> createFKColumnToForeignTableMap() throws Exception {
    Connection con = DBKernel.getDBConnection();
    if(con==null) return null;
    DatabaseMetaData  dbMetaData = con.getMetaData();
    if(dbMetaData==null) return null;
    MyDBI myDBi = DBKernel.myDBi;
    if(myDBi==null) return null;
    
    Map<String, String> result = new HashMap<>();
        
    for(DBInfo.TABLE table : Arrays.asList(DBInfo.TABLE.STATION, DBInfo.TABLE.PRODUCT, DBInfo.TABLE.LOT, DBInfo.TABLE.DELIVERY)) {
      ResultSet resultSet = dbMetaData.getImportedKeys(null, null, table.getName());
      if(resultSet!=null) {
        while(resultSet.next()) result.put(table.getName() + "." + resultSet.getString("FKCOLUMN_NAME"), resultSet.getString("PKTABLE_NAME"));
        resultSet.close();
      }
    }
    
    return result;
  }
  
  private static Map<String, String[]> createTableToLabelColumnMap() {
    Map<String, String[]> result = new HashMap<>();
    
    result.put(DBInfo.TABLE.STATION.getName(), new String[] {DBInfo.COLUMN.STATION_NAME.getName()});
    result.put(DBInfo.TABLE.PRODUCT.getName(), new String[] {DBInfo.COLUMN.PRODUCT_DESCRIPTION.getName()});
    result.put(DBInfo.TABLE.MATRIX.getName(), new String[] {DBInfo.COLUMN.MATRIX_NAME.getName()});
    result.put(DBInfo.TABLE.LOT.getName(), new String[] {DBInfo.COLUMN.LOT_NUMBER.getName()});
    result.put(DBInfo.TABLE.AGENT.getName(), new String[] {DBInfo.COLUMN.AGENT_NAME.getName()});
    
//    result = new HashMap<>();
//    result.put(DBInfo.TABLE.STATION.getName(), new String[] {SimSearch.SimSet.Type.STATION});
//    result.put(DBInfo.TABLE.PRODUCT.getName(), new String[] {SimSearch.SimSet.Type.PRODUCT});
    return result;
  }
  
  private static Map<String, Map<String, String>> createTableToChildTableMap() throws Exception {
    Connection con = DBKernel.getDBConnection();
    if(con==null) return null;
    DatabaseMetaData  dbMetaData = con.getMetaData();
    if(dbMetaData==null) return null;
    MyDBI myDBi = DBKernel.myDBi;
    if(myDBi==null) return null;
    
    Map<String, Map<String, String>> result = new HashMap<>();
    Set<String> tables = new HashSet<>(Arrays.asList(DBInfo.TABLE.STATION, DBInfo.TABLE.PRODUCT, DBInfo.TABLE.LOT, DBInfo.TABLE.DELIVERY).stream().map(t -> t.getName()).collect(Collectors.toList()));
    
    for(DBInfo.TABLE table : Arrays.asList(DBInfo.TABLE.STATION, DBInfo.TABLE.PRODUCT, DBInfo.TABLE.LOT, DBInfo.TABLE.DELIVERY)) {
      
      MyTable myTable = myDBi.getTable(table.getName());
      if(myTable==null) return null;
      
      for(MyTable foreignTable: myTable.getForeignFields()) if(foreignTable!=null) tables.add(foreignTable.getTablename());
      
    }
          
    for(String tableName : tables) {
      ResultSet resultSet = dbMetaData.getExportedKeys(null, null, tableName);
      if(resultSet!=null) {
        Set<String> childTablesWithNonUniqueFKs = new HashSet<>();
        while(resultSet.next()) {
          if(!result.containsKey(tableName)) result.put(tableName,  new HashMap<>());
          if(result.get(tableName).containsKey(resultSet.getString("FKTABLE_NAME"))) childTablesWithNonUniqueFKs.add(resultSet.getString("FKTABLE_NAME"));
          else result.get(tableName).put(resultSet.getString("FKTABLE_NAME"), resultSet.getString("FKCOLUMN_NAME")); 
        }
        resultSet.close();
      }
    }
    
    return result;
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
  
  private void loadTableData(DBInfo.TABLE table, List<Integer> idList, List<DBInfo.COLUMN> childColumns, SimSearchDataManipulationHandler dataManipulations) throws Exception {
     {
      MyDBI myDBI = DBKernel.myDBi;
      if(myDBI==null) throw(new Exception("DB interface is not available."));
      
      Connection con = DBKernel.getDBConnection();
      if(con==null) throw(new Exception("DB Connection is not available."));
    	  
      Map<Integer, Object[]> idToDataMap = new HashMap<>();
      List<Integer> ids = new ArrayList<>(idList); 
      MyTable myTable = myDBI.getTable(table.getName());
      if(myTable==null) throw(new Exception("Cannot retrieve sql statement for table query."));
      
      
      String sql = myTable.getSelectSQL() + " where " + ID_COLUMN_NAME + " IN (" + String.join(", ", idList.stream().map(e->e.toString()).collect(Collectors.toList())) +  ")";
      if(sql==null) throw(new Exception("Cannot retrieve sql statement for table query."));
 
        Statement statement = con.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        if(resultSet==null) throw(new Exception("a ResultSet object is never null rule violated"));
      
      ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
      if(resultSetMetaData==null) throw(new Exception("Could not retrieve meta data."));
      
      this.columnCount = resultSetMetaData.getColumnCount();
      this.columnNames = new String[this.columnCount+1];
      this.columnNames[0] = "Status";
      
      this.columnClasses = new Class<?>[this.columnCount+1];
      this.columnClasses[0] = Integer.class;
      
      this.columnSimSetTypes = new SimSearch.SimSet.Type[this.columnCount+1];
      
      for(int i=0; i<this.columnCount; ++i) {
        this.columnNames[i+1] = resultSetMetaData.getColumnName(i+1);
        if(!this.typeMap.containsKey(resultSetMetaData.getColumnType(i+1))) throw(new SQLException("Unkown DB Type: " + resultSetMetaData.getColumnType(i+1)));
        this.columnClasses[i+1] = this.typeMap.get(resultSetMetaData.getColumnType(i+1));
      }
      
      
      while (resultSet.next()) {
        Object[] data = new Object[this.columnCount+1];
        for(int column=0; column < this.columnCount; ++column) data[column+1] = this.columnClasses[column+1].cast(rs.getObject(column+1));
        idToDataMap.put((Integer) resultSet.getObject(ID_COLUMN_NAME), data);
      }
      resultSet.close();
     
      this.data = new Object[idToDataMap.size()][]; //this.columnCount+1];
      
      Set<Integer> missingIds = Sets.difference(new HashSet<>(idList), idToDataMap.keySet());
      ids.removeAll(missingIds);
      
      int row = -1;
      for(Integer id: ids) {
        ++row;
        this.data[row] = idToDataMap.get(id);
      }
      
      for(int column=1; column<this.columnCount+1; ++column) {
        String fullColumnName = table.getName() + "." + this.columnNames[column];
        ForeignField foreignField = foreignKeyMap.get(fullColumnName);
        
        if(foreignField!=null) {
          List<String> labelColumns = new ArrayList<String>(Arrays.asList(foreignField.labelColumns));
          for(int i=0; i<this.rowCount; ++i) {
            sql = foreignField.createSql(data[i][ID_COLUMN], dataManipulations);
            ResultSet resultSet = statement.executeQuery(sql);
            if(resultSet==null) throw(new Exception("a ResultSet object is never null rule violated")); //definition according to api information
            
            if(foreignField.getResultType().equals(String.class)) {
              // the result Set should contain exactly one result
              while(resultSet.next()) data[i][ID_COLUMN] = foreignField.formatFunction.apply(labelColumns.stream().map(colName -> (String) resultSet.getObject(colName)).collect(Collectors.toList()));
            }
            else {
              List<String> resultList= new ArrayList<>();
              while(resultSet.next()) resultList.add(foreignField.formatFunction.apply(labelColumns.stream().map(colName -> (String) resultSet.getObject(colName)).collect(Collectors.toList())));
              data[i][ID_COLUMN] = resultList;
            }
            resultSet.close();
          }
          this.columnClasses[column] = foreignField.getResultType();
        }
      }
    }
  }
  
//  private void cacheForeignData(Map<Integer, String> foreignDataMap, SimSearch.SimSet.Type simSetType) {
//	  for(Entry<Integer, String> entry: foreignDataMap.entrySet()) this.foreignDataCacheMap.get(simSetType).put(entry.getKey(), entry.getValue());
//  }
  
//  private Map<Integer, String> loadForeignData(Set<Integer> ids, String tableName) throws Exception {
//	  Connection con = DBKernel.getDBConnection();
//	  if(con==null) throw(new Exception("No database connection available."));
//	  
//	  final String RESET = "\033[0m";  // Text Reset
//
//	    // Regular Colors
//	  final String BLACK = "\033[0;30m";   // BLACK
//	  final String RED = "\033[0;31m";     // RED
//	  
//	  if(SimSearchDataLoader.tableToLabelColumnMap.get(tableName)==null) {
//		  MyTable tmp = DBKernel.myDBi.getTable(tableName);
//		  String[] tmp2 = tmp.getFieldNames();
//		  System.out.println(RED + "LabelColumn for table " + tableName + " is missing. " + RESET + "");
//	  }
//	  Map<Integer, String> result = new HashMap<>();
//	  Statement statement = con.createStatement();
//	  String sql = "SELECT ID, " + DBKernel.delimitL(SimSearchDataLoader.tableToLabelColumnMap.get(tableName)) + " " + 
//			  "FROM " + DBKernel.delimitL(tableName) + " WHERE ID IN (" + String.join(", ", ids.stream().map(id -> id.toString()).collect(Collectors.toList())) + ")";
//	  ResultSet resultSet = statement.executeQuery(sql);
//	  
//	  if(resultSet!=null) {
//		  
//		  while(resultSet.next()) result.put(resultSet.getInt(1), resultSet.getString(2));
//			  
//		  resultSet.close();
//	  }
//	  statement.close();
//	  
//	  return result;
//  }
}
