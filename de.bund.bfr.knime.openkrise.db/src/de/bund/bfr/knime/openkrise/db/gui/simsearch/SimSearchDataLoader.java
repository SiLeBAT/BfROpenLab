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
import java.util.Collection;
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
  
  public static class ResultSetIsNullException extends Exception {
    private ResultSetIsNullException() {
      super("A ResultSet object returned by Stament:executeQuery is never null - rule violated;");
    }
  }
  private static final String ID_COLUMN_NAME = "ID";
  private static final int ID_COLUMN = 1;
  private static final Map<Integer, Class<?>> typeMap;
  //private static final Map<String, String> tableToLabelColumnMap;
  //private static final Map<String, SimSearch.SimSet.Type> tableToSimSetTypeMap;
  private static Map<String, ForeignField> foreignKeyMap;
  
  //private Map<SimSearch.SimSet.Type, Map<Integer, String>> foreignDataCacheMap;
  
  
//  public String[] columnNames;
//  public Class<?>[] columnClasses;
//  public Object[][] data;
 
  //public SimSearch.SimSet.Type[] columnSimSetTypes;
  
  private SimSearch.DataSource.DataSourceListener dataSourceListener;
  
  private static abstract class ForeignField {
    final List<String> labelColumns;
    final Function<List<String>, String> formatFunction;
        
    private ForeignField(String formatString, String[] labelColumns) throws Exception {
      this(new Function<List<String>, String>() {
        public String apply(List<String> arg) {
          return String.format(formatString, arg.toArray());
        }
      }, labelColumns);
    }
    
    private ForeignField(Function<List<String>, String> formatFunction, String[] labelColumns) throws Exception {
      if(labelColumns==null || labelColumns.length==0) throw(new Exception("labelColumns is null or empty."));
      this.labelColumns = new ArrayList<>(Arrays.asList(labelColumns));
      this.formatFunction = formatFunction;
    }
    
    abstract Map<Integer, Object> getResult(Collection<Integer> ids, SimSearchDataManipulationHandler dataManipulations, Statement statement) throws Exception ;      
    
    abstract Class<?> getResultType();  

    String getFormatedText(ResultSet resultSet) throws Exception {
      return this.formatFunction.apply(this.labelColumns.stream().map(columnName -> {
        try {
          
          return resultSet.getString(columnName);
        } catch (SQLException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
          return "";
        }
      }).collect(Collectors.toList()));
    }
  }
  
  private static class ForeignList extends ForeignField {
    private ChildTable childTable;
    
    ForeignList(ChildTable childTable, String[] labelColumns) throws Exception {
      super("%s", labelColumns);
      this.childTable = childTable;
    }
    
    ForeignList(ChildTable childTable, String formatString, String[] labelColumns) throws Exception {
      super(formatString, labelColumns);
      this.childTable = childTable;
    }
    
    ForeignList(ChildTable childTable, Function<List<String>, String> formatFunction, String[] labelColumns) throws Exception {
      super(formatFunction, labelColumns);
      this.childTable = childTable;
    }

    
    @Override
    Map<Integer, Object> getResult(Collection<Integer> ids,
        SimSearchDataManipulationHandler dataManipulations, Statement statement) throws Exception {
      String sql = "Select " + DBKernel.delimitL(this.childTable.getName())+"."+DBKernel.delimitL(this.childTable.columnName) + ", " + String.join(", ", this.labelColumns.stream().map(label -> DBKernel.delimitL(label)).collect(Collectors.toList())) + "\n" + 
          " FROM " + this.childTable.createSql(dataManipulations, "") + "\n" +
          " WHERE " + DBKernel.delimitL(this.childTable.getName())+"."+DBKernel.delimitL(this.childTable.columnName) + " IN (" + String.join(", ", ids.stream().map(id -> id.toString()).collect(Collectors.toList())) + ")" ;

      ResultSet resultSet = null;
      try {
        resultSet = statement.executeQuery(sql);
      } catch(Exception err) {
        throw(err);
      }
      if(resultSet==null) throw(new ResultSetIsNullException());

      Map<Integer, List<String>> result = new HashMap<>();
      while(resultSet.next()) {
        if(!result.containsKey(resultSet.getInt(1))) result.put(resultSet.getInt(1), new ArrayList<>());
        result.get(resultSet.getInt(1)).add(this.getFormatedText(resultSet));
      }
      //return result.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> (Object) entry.getValue()));
      return result.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> String.join("; ", entry.getValue())));
    }

    @Override
    Class<?> getResultType() { return String.class; }
  }
    
  private static class ForeignItem extends ForeignField {
    private final String tableName;
    private final String columnName;
    private ParentTable parentTable;
    
    ForeignItem(String tableName, String columnName, ParentTable parentTable, String[] labelColumns) throws Exception { this(tableName, columnName, parentTable, "%s", labelColumns); }
    
    ForeignItem(String tableName, String columnName, ParentTable parentTable, String formatString, String[] labelColumns) throws Exception {
      super(formatString, labelColumns);
      this.parentTable = parentTable;
      this.tableName = tableName;
      this.columnName = columnName;
    }
    
    ForeignItem(String tableName, String columnName, ParentTable parentTable, Function<List<String>, String> formatFunction, String[] labelColumns) throws Exception {
      super(formatFunction, labelColumns);
      this.parentTable = parentTable;
      this.tableName = tableName;
      this.columnName = columnName;
    }

    @Override
    Map<Integer, Object> getResult(Collection<Integer> ids,
        SimSearchDataManipulationHandler dataManipulations, Statement statement) throws Exception {
      String sql = "Select " + DBKernel.delimitL(this.tableName)+"."+ DBKernel.delimitL("ID") + ", " + String.join(", ", this.labelColumns.stream().map(label -> DBKernel.delimitL(label)).collect(Collectors.toList())) + 
          " FROM " + DBKernel.delimitL(tableName) + " INNER JOIN " + parentTable.createSql(dataManipulations, "ON " +  DBKernel.delimitL(tableName) + "." +  DBKernel.delimitL(columnName) + "=" + DBKernel.delimitL(parentTable.getName()) + "." +  DBKernel.delimitL("ID")) + "\n" +
          " WHERE " + DBKernel.delimitL(this.tableName)+"."+ DBKernel.delimitL("ID") + " IN (" + SimSearchDataLoader.idsToString(ids) + ")";
      
      ResultSet resultSet = statement.executeQuery(sql);
      if(resultSet==null) throw(new ResultSetIsNullException());
      
      Map<Integer, Object> result = new HashMap<>();
      while(resultSet.next()) {
        result.put(resultSet.getInt(1), (Object) this.getFormatedText(resultSet));
      }
      return result;
    }

    @Override
    Class<?> getResultType() { return String.class; }
        
  }  
  
  private static abstract class Table {
    private final String name;
    
    Table(String name) { this.name = name; }
    
    abstract String createSql(SimSearchDataManipulationHandler dataManipulations, String joinPredicate);
    
    String getName() { return this.name; }
  }
  
  private static class ParentTable extends Table {
    private String tableColumn;
    private ParentTable parent;
    
    private ParentTable(String name) { super(name); }
    
    private ParentTable(String name,  String tableColumn, ParentTable parent) {
      super(name);
      this.tableColumn = tableColumn;
      this.parent = parent;
    }

    @Override
    public String createSql(SimSearchDataManipulationHandler dataManipulations, String joinPredicate) {
      if(parent==null) {
        return DBKernel.delimitL(this.getName()) + " " + joinPredicate;
      } else {
        return DBKernel.delimitL(this.getName()) + " " + joinPredicate + "\nLEFT JOIN " + parent.createSql(dataManipulations, "ON " + DBKernel.delimitL(this.getName()) + "." + DBKernel.delimitL(tableColumn) + "=" + DBKernel.delimitL(parent.getName()) +"."+DBKernel.delimitL("ID"));
      }
    }
  }
  
  private static class ChildTable extends Table {
    private final String columnName;
    private String columnToParentTable;
    private ParentTable parentTable;
    private ChildTable childTable;
    
    private ChildTable(String name, String columnName) {
      super(name);
      this.columnName = columnName;
      this.childTable = null;
      this.columnToParentTable = null;
      this.parentTable = null;
    }
    
    private ChildTable(String name, String tableColumn, ChildTable childTable) {
      this(name, tableColumn);
      this.childTable = childTable;
    }
    
    private ChildTable(String name, String tableColumn, String columnToParentTable, ParentTable parentTable) {
      this(name, tableColumn);
      this.columnToParentTable = columnToParentTable;
      this.parentTable = parentTable;
    }

    @Override
    String createSql(SimSearchDataManipulationHandler dataManipulations, String joinPredicate) {
      if(parentTable!=null) {
        return DBKernel.delimitL(this.getName()) + " " + joinPredicate + "\nINNER JOIN " + parentTable.createSql(dataManipulations, "ON " + DBKernel.delimitL(this.getName()) + "." + DBKernel.delimitL(columnToParentTable) + "=" + DBKernel.delimitL(parentTable.getName()) +"." + DBKernel.delimitL("ID"));
      } else if (childTable!=null) {
        return DBKernel.delimitL(this.getName()) + " " + joinPredicate + "\nINNER JOIN " + childTable.createSql(dataManipulations, "ON " + DBKernel.delimitL(this.getName()) + "."+ DBKernel.delimitL("ID") + "=" + DBKernel.delimitL(childTable.getName()) +"."+DBKernel.delimitL(childTable.columnName));
      } else {
        return DBKernel.delimitL(this.getName()) + " " + joinPredicate;  
      }
    }
    
  }
  
  
  private static String convertToDate(String day, String month, String year) {
    if((day==null || day.isEmpty()) && (month==null || month.isEmpty()) && (year==null || year.isEmpty())) return "?";
    else return (day==null || day.isEmpty()?"?":day)+"."+(month==null || month.isEmpty()?"?":month)+"."+(year==null || year.isEmpty()?"?":year);
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
//	  Map<String, ForeignField> map = null;
//	  try {
//        map = createForeignKeyMap();
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
//	  foreignKeyMap = map;
  }
  
//  public SimSearchDataLoader(SimSearch.DataSource.DataSourceListener dataSourceListener) {
//    this.dataSourceListeners = new ArrayList<>();
//    this.dataSourceListeners.add(dataSourceListener);
//  }
  
//  private void informListeners(SimSearch.SimSet.Type simSetType, List<Integer> missingIds) {
//    this.dataSourceListeners.forEach(l -> l.missingIdsDetected(simSetType, missingIds));
//  }
  
  private static Map<String, ForeignField> createForeignKeyMap() throws Exception {
    MyDBI myDBi = DBKernel.myDBi;
    if(myDBi==null) return null;
    
    Map<String, ForeignField> map = new HashMap<>();
    // manuell assignments
    map.put(DBInfo.COLUMN.LOT_INGREDIENTS.getFullName(), new ForeignList(
        new ChildTable(DBInfo.TABLE.LOTLINK.getName(), DBInfo.COLUMN.LOTLINK_PRODUCT.getName(), 
            DBInfo.COLUMN.LOTLINK_INGREDIENT.getName(), new ParentTable(DBInfo.TABLE.DELIVERY.getName(), 
                DBInfo.COLUMN.DELIVERY_LOT.getName(), new ParentTable(DBInfo.TABLE.LOT.getName(), 
                    DBInfo.COLUMN.LOT_PRODUCT.getName(), new ParentTable(DBInfo.TABLE.PRODUCT.getName())))), 
        new Function<List<String>, String>() {
          public String apply(List<String> arg) {    
            return arg.get(0) + " [" + (arg.get(1)==null || arg.get(1).isEmpty()?"?":arg.get(1)) +"]";
          }
        },
        new String[] {DBInfo.COLUMN.PRODUCT_DESCRIPTION.getName(), DBInfo.COLUMN.LOT_NUMBER.getName()}));
    map.put(DBInfo.COLUMN.LOT_DELIVERIES.getFullName(), new ForeignList(
        new ChildTable(DBInfo.TABLE.DELIVERY.getName(), DBInfo.COLUMN.DELIVERY_LOT.getName(), 
            DBInfo.COLUMN.DELIVERY_RECIPIENT.getName(), new ParentTable(DBInfo.TABLE.STATION.getName())),
        new Function<List<String>, String>() {
          public String apply(List<String> arg) {
            return SimSearchDataLoader.convertToDate(arg.get(0), arg.get(1), arg.get(2)) + " -> "+ 
                SimSearchDataLoader.convertToDate(arg.get(3), arg.get(4), arg.get(5)) + " " +
                (arg.get(6)==null || arg.get(6).isEmpty()?"?":arg.get(6));
          }
        },
        new String[] {
            DBInfo.COLUMN.DELIVERY_DELIVEREDON_DAY.getName(),DBInfo.COLUMN.DELIVERY_DELIVEREDON_MONTH.getName(),DBInfo.COLUMN.DELIVERY_DELIVEREDON_YEAR.getName(),
            DBInfo.COLUMN.DELIVERY_ARRIVEDON_DAY.getName(),DBInfo.COLUMN.DELIVERY_ARRIVEDON_MONTH.getName(),DBInfo.COLUMN.DELIVERY_ARRIVEDON_YEAR.getName(),
            DBInfo.COLUMN.STATION_NAME.getName()
            }));
    
    Map<String, String> fkColumnToForeignTableMap = createFKColumnToForeignTableMap();
    Map<String, String[]> tableToLabelColumnMap = createTableToLabelColumnMap();
    Map<String, Map<String, String>> tableToChildTableMap = createTableToChildTableMap();
    
    for(DBInfo.TABLE table : Arrays.asList(DBInfo.TABLE.STATION, DBInfo.TABLE.PRODUCT, DBInfo.TABLE.LOT, DBInfo.TABLE.DELIVERY)) {
      
      MyTable myTable = myDBi.getTable(table.getName());
      if(myTable==null) throw(new Exception("Table " + table.getName() + " is provided in database interface."));
      
      for(MyTable foreignTable: myTable.getForeignFields()) {
        if(foreignTable!=null) {
          String foreignColumn = myTable.getForeignFieldName(foreignTable);
          String fullForeignColumn = myTable.getTablename() + "." + foreignColumn;
          
          if(map.containsKey(fullForeignColumn)) {
            // mapping already manually defined
            continue;
          }
          if(fkColumnToForeignTableMap.containsKey(fullForeignColumn)) {
            // this might be a parent, a grandparent, ....
            if(fkColumnToForeignTableMap.get(fullForeignColumn).equals(foreignTable.getTablename())) {
              // this is a simple parent relation
              map.put(fullForeignColumn, new ForeignItem(myTable.getTablename(), foreignColumn, new ParentTable(foreignTable.getTablename()), tableToLabelColumnMap.get(foreignTable.getTablename())));
            } else {
              // this might be a grandparent relation
            } 
          } else if(tableToChildTableMap.containsKey(myTable.getTablename()))  {
            Map<String, String> tableToColumnMap = tableToChildTableMap.get(myTable.getTablename());
            if(tableToColumnMap.containsKey(foreignTable.getTablename())) {
              // child table
              String[] labelColumns = tableToLabelColumnMap.get(foreignTable.getTablename());
              if(labelColumns==null || labelColumns.length==0) printWarning("No label columns defined for table " + foreignTable.getTablename() + ". Column " + fullForeignColumn + " cannot be mapped." );
              else map.put(fullForeignColumn, new ForeignList(new ChildTable(foreignTable.getTablename(), tableToColumnMap.get(foreignTable.getTablename())), labelColumns));
            } else if(tableToChildTableMap.containsKey(foreignTable.getTablename())) {
              // this might be a n to m relation
              Set<String> sharedChilds = Sets.intersection(tableToChildTableMap.get(foreignTable.getTablename()).keySet(), tableToColumnMap.keySet());
              if(sharedChilds.size()==1) {
                // n to m relation detected
                String sharedTable = sharedChilds.stream().collect(Collectors.toList()).get(0);
                String[] labelColumns = tableToLabelColumnMap.get(foreignTable.getTablename());
                if(labelColumns==null || labelColumns.length==0) printWarning("No label columns defined for table " + foreignTable.getTablename() + ". Column " + fullForeignColumn + " cannot be mapped." );
                else map.put(fullForeignColumn, new ForeignList(
                    new ChildTable(sharedTable, tableToColumnMap.get(sharedTable), tableToChildTableMap.get(foreignTable.getTablename()).get(sharedTable),new ParentTable(foreignTable.getTablename())),
                    labelColumns));
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
    //result.put(DBInfo.TABLE.DELIVERY.getName(), new String[] {DBInfo.COLUMN.DELIVERY_.getName()});
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
  
  public SimSearchDataLoader(SimSearch.SimSet simSet, SimSearchDataManipulationHandler dataManipulationHandler, SimSearch.DataSource.DataSourceListener dataSourceListener) {
    this.simSet = simSet;
    this.dataManipulationHandler = dataManipulationHandler;
    this.dataSourceListener = dataSourceListener;
    //this.dataSourceListeners.add(dataSourceListener);
    //this.foreignDataCacheMap = new HashMap<>();
    //for(SimSearch.SimSet.Type simSetType: SimSearch.SimSet.Type.class.getEnumConstants()) this.foreignDataCacheMap.put(simSetType, new HashMap<>());
  }
  
  public void loadData() throws Exception {
      if(SimSearchDataLoader.foreignKeyMap==null) SimSearchDataLoader.foreignKeyMap = SimSearchDataLoader.createForeignKeyMap();
	  switch(simSet.getType()) {
	  case STATION:
    	loadTableData(DBInfo.TABLE.STATION, null);
    	break;
	  case PRODUCT:
        loadTableData(DBInfo.TABLE.PRODUCT, null);
        break;
	  case LOT:
        loadTableData(DBInfo.TABLE.LOT, null);
        break;
	  case DELIVERY:
        loadTableData(DBInfo.TABLE.DELIVERY, null);
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
  
  private static String idsToString(Collection<Integer> ids) {
    return String.join(", ", ids.stream().map(id -> id.toString()).collect(Collectors.toList()));
  }
  
  private void loadTableData(DBInfo.TABLE table, List<DBInfo.COLUMN> childColumns) throws Exception {
     {
      MyDBI myDBI = DBKernel.myDBi;
      if(myDBI==null) throw(new Exception("DB interface is not available."));
      
      Connection con = DBKernel.getDBConnection();
      if(con==null) throw(new Exception("DB Connection is not available."));
    	  
      Map<Integer, Object[]> idToDataMap = new HashMap<>();
      List<Integer> ids = new ArrayList<>(simSet.getIdList()); 
      MyTable myTable = myDBI.getTable(table.getName());
      if(myTable==null) throw(new Exception("Cannot retrieve sql statement for table query."));
      
      
      String sql = myTable.getSelectSQL() + " where " + ID_COLUMN_NAME + " IN (" + idsToString(simSet.getIdList()) +  ")";
      if(sql==null) throw(new Exception("Cannot retrieve sql statement for table query."));
 
      Statement statement = con.createStatement();
      ResultSet resultSet = statement.executeQuery(sql);
      if(resultSet==null) throw(new ResultSetIsNullException());
      
      ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
      if(resultSetMetaData==null) throw(new Exception("Could not retrieve meta data."));
      
      this.columnCount = resultSetMetaData.getColumnCount();
      this.columnNames = new String[this.columnCount+1];
      this.columnNames[0] = "Status";
      
      this.columnClasses = new Class<?>[this.columnCount+1];
      this.columnClasses[0] = Integer.class;
      
      //this.columnSimSetTypes = new SimSearch.SimSet.Type[this.columnCount+1];
      
      for(int i=0; i<this.columnCount; ++i) {
        this.columnNames[i+1] = resultSetMetaData.getColumnName(i+1);
        if(!SimSearchDataLoader.typeMap.containsKey(resultSetMetaData.getColumnType(i+1))) throw(new SQLException("Unkown DB Type: " + resultSetMetaData.getColumnType(i+1)));
        this.columnClasses[i+1] = SimSearchDataLoader.typeMap.get(resultSetMetaData.getColumnType(i+1));
      }
      
      
      while (resultSet.next()) {
        Object[] data = new Object[this.columnCount+1];
        for(int column=0; column < this.columnCount; ++column) data[column+1] = this.columnClasses[column+1].cast(resultSet.getObject(column+1));
        idToDataMap.put((Integer) resultSet.getObject(ID_COLUMN_NAME), data);
      }
      resultSet.close();
      
      // replace foreign Fields
      for(int column=1; column<this.columnCount; ++column) {
        String fullColumnName = table.getName() + "." + this.columnNames[column];
        ForeignField foreignField = SimSearchDataLoader.foreignKeyMap.get(fullColumnName);
        
        if(foreignField!=null) {
          Map<Integer, Object> foreignObjects = foreignField.getResult(idToDataMap.keySet(), this.dataManipulationHandler, statement);
          for(Integer id: idToDataMap.keySet()) idToDataMap.get(id)[column] = foreignObjects.get(id);
          this.columnClasses[column] = foreignField.getResultType();
        }
      }
     
      this.data = new Object[idToDataMap.size()][]; 
      
      List<Integer> missingIds = new ArrayList<>(Sets.difference(new HashSet<>(simSet.getIdList()), idToDataMap.keySet()));
      //ids.removeAll(missingIds);
      if(this.dataSourceListener!=null && !missingIds.isEmpty()) this.dataSourceListener.missingIdsDetected(simSet.getType(), missingIds);
      
      int row = -1;
      for(Integer id: ids) {
        ++row;
        this.data[row] = idToDataMap.get(id);
      }
      
//      for(int column=1; column<this.columnCount+1; ++column) {
//        String fullColumnName = table.getName() + "." + this.columnNames[column];
//        ForeignField foreignField = foreignKeyMap.get(fullColumnName);
//        
//        if(foreignField!=null) {
//          List<String> labelColumns = new ArrayList<String>(Arrays.asList(foreignField.labelColumns));
//          for(int i=0; i<this.rowCount; ++i) {
//            sql = foreignField.createSql(data[i][ID_COLUMN], dataManipulations);
//            ResultSet resultSet = statement.executeQuery(sql);
//            if(resultSet==null) throw(new Exception("a ResultSet object is never null rule violated")); //definition according to api information
//            
//            if(foreignField.getResultType().equals(String.class)) {
//              // the result Set should contain exactly one result
//              while(resultSet.next()) data[i][ID_COLUMN] = foreignField.formatFunction.apply(labelColumns.stream().map(colName -> (String) resultSet.getObject(colName)).collect(Collectors.toList()));
//            }
//            else {
//              List<String> resultList= new ArrayList<>();
//              while(resultSet.next()) resultList.add(foreignField.formatFunction.apply(labelColumns.stream().map(colName -> (String) resultSet.getObject(colName)).collect(Collectors.toList())));
//              data[i][ID_COLUMN] = resultList;
//            }
//            resultSet.close();
//          }
//          this.columnClasses[column] = foreignField.getResultType();
//        }
//      }
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
  
  private static void printWarning(String text) {
    //final String RESET = "\033[0m";  // Text Reset
    //final String BLACK = "\033[0;30m";   // BLACK
    //final String RED = "\033[0;31m";     // RED
    
    System.err.println("Warning: " + text);
    //System.out.println(RED + text + RESET );
  }
}
