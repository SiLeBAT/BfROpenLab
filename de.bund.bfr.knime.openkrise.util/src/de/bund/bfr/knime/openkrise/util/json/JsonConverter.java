package de.bund.bfr.knime.openkrise.util.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.json.JsonValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.*;
import org.knime.core.data.def.BooleanCell.BooleanCellFactory;
import org.knime.core.data.json.JacksonConversions;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.Tracing;

public class JsonConverter {
  
  private static final ImmutableMap<Class<? extends DataCell>, Function<Object,DataCell>> DATACELLCLASS_TO_CREATEDATACELL_FUN_MAP = new ImmutableMap.Builder<Class<? extends DataCell>, Function<Object,DataCell>>()
      .put(BooleanCell.class, (value) -> (BooleanCell) BooleanCellFactory.create((boolean) value))
      .put(DoubleCell.class, (value) -> new DoubleCell(((Number) value).doubleValue()))
      .put(IntCell.class, (value) -> new IntCell(((Number) value).intValue()))  //new IntCell((Integer) obj))
      .put(LongCell.class, (value) -> new LongCell(((Number) value).longValue()))
      .put(StringCell.class, (value) -> new StringCell((String) value))
      .build();
  private static final ImmutableMap<Class<? extends DataCell>, Function<DataCell, Object>> DATACELLCLASS_TO_GETVALUE_FUN_MAP = new ImmutableMap.Builder<Class<? extends DataCell>, Function<DataCell,Object>>()
      .put(BooleanCell.class, (cell) -> ((BooleanCell) cell).getBooleanValue())
      .put(DoubleCell.class, (cell) -> ((DoubleCell) cell).getDoubleValue())
      .put(IntCell.class, (cell) -> ((IntCell) cell).getIntValue())
      .put(LongCell.class, (cell) -> ((LongCell) cell).getLongValue())
      .put(StringCell.class, (cell) -> ((StringCell) cell).getStringValue())
      .build();
  
  private static final ImmutableMap<String, Function<Object, Object>> TYPESTRING_TO_VALUECONVERTER_FUN_MAP = new ImmutableMap.Builder<String, Function<Object,Object>>()
      .put("string", (value) -> (String) value)
      .put("boolean", (value) -> (Boolean) value)
      .put("int", (value) -> ((Number) value).intValue())
      .put("long", (value) ->((Number) value).longValue())
      .put("double", (value) ->((Number) value).doubleValue())
      .build();
  
  private static final ImmutableMap<String, Class<? extends DataCell>> TYPESTRING_TO_CELLCLASS_MAP = new ImmutableMap.Builder<String, Class<? extends DataCell>>()
      .put("string", StringCell.class)
      .put("boolean", BooleanCell.class)
      .put("double", DoubleCell.class)
      .put("int", IntCell.class)
      .put("long", LongCell.class)
      .build();
  
  // create a reverse map of TYPESTRING_TO_CELLCLASS_MAP
  private static final ImmutableMap<Class<? extends DataCell>, String> CELLCLASS_TO_TYPESTRING_MAP = ImmutableMap.copyOf(TYPESTRING_TO_CELLCLASS_MAP.entrySet().stream()
      .collect(Collectors.toMap(Entry::getValue, c -> c.getKey()))); 
  
  private static DataCell convertValueToDataCell(Object value, Class<? extends DataCell> cellClass) {
    Function<Object, DataCell> fun = DATACELLCLASS_TO_CREATEDATACELL_FUN_MAP.get(cellClass);
    return fun.apply(value);
  }
  
  private static Object convertDataCellToValue(DataCell cell) {
    Function<DataCell,Object> fun = DATACELLCLASS_TO_GETVALUE_FUN_MAP.get(cell.getClass());
    if(fun!=null) return fun.apply(cell);
    // DataCell is not supported, switch to string
    return cell.toString();
  }

  public static DataType convertTypeStringToDataType(String typeString) throws InvalidSettingsException {
    if(Strings.isNullOrEmpty(typeString)) throw(new InvalidSettingsException("Column type is missing."));
   
    Class<? extends DataCell> cellClass = TYPESTRING_TO_CELLCLASS_MAP.get(typeString);
    if(cellClass==null) throw(new InvalidSettingsException("Column type '" + typeString + "' is unkown."));
    
    return DataType.getType(cellClass);
  }
    
  public static DataCell createDataCell(Object value, Class<? extends DataCell> dataCellClass) {
    Function<Object, DataCell> fun = DATACELLCLASS_TO_CREATEDATACELL_FUN_MAP.get(dataCellClass);
    try {
      if(fun!=null) return fun.apply(value);
    } catch(Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  
  private static DataTableSpec toTableSpec(Map<String, DataType> columns) {
    List<DataColumnSpec> columnSpecs = new ArrayList<>();

    columns.forEach((name, type) -> columnSpecs.add(new DataColumnSpecCreator(name, type).createSpec()));

    return new DataTableSpec(columnSpecs.toArray(new DataColumnSpec[0]));
  }
  
  public static BufferedDataTable createDataTable(JsonFormat.Data.Table dataTable, ExecutionContext exec) throws InvalidSettingsException {
    Map<String, DataType> dataColumns = new LinkedHashMap<>();
    
    for(JsonFormat.Data.Table.ColumnProperty columnProperty : dataTable.columnProperties) 
      dataColumns.put(columnProperty.id, JsonConverter.convertTypeStringToDataType(columnProperty.type));
    
    DataTableSpec tableSpec = toTableSpec(dataColumns);
    BufferedDataContainer dataContainer = exec.createDataContainer(tableSpec);
    
    long rowIndex = 0;
    for (JsonFormat.Data.Table.ItemProperty[] properties : dataTable.data) {
      DataCell[] cells = new DataCell[tableSpec.getNumColumns()];

      Arrays.fill(cells, DataType.getMissingCell());
      
      for(JsonFormat.Data.Table.ItemProperty property : properties) {
        int columnIndex = tableSpec.findColumnIndex(property.id);
        
        if(property.value!=null) cells[columnIndex] = convertValueToDataCell(property.value, tableSpec.getColumnSpec(columnIndex).getType().getCellClass()) ;
      }
      
      dataContainer.addRowToTable(new DefaultRow( RowKey.createRowKey(rowIndex++),cells));
    }
    
    dataContainer.close();
    return dataContainer.getTable();
  }
  
  private static String convertCellClassToTypeString(Class<? extends DataCell> cellClass) {
    String typeString = CELLCLASS_TO_TYPESTRING_MAP.get(cellClass);
    if(typeString!=null) return typeString;
    return "string";
  }
  
  public static class JsonBuilder {
    private JsonFormat json;
    private final static String CURRENT_VERSION = "1.0.0";
    
    public JsonBuilder() {
      json = new JsonFormat();
      json.version = CURRENT_VERSION;
    }
    
    protected JsonFormat getJson() {
      return json;
    }
    
    // knime to json
    private static JsonFormat.Data.Table.ColumnProperty[] convertFromDataTableSpecs(DataTableSpec tableSpec) {
      
      JsonFormat.Data.Table.ColumnProperty[] columnSpecs = new JsonFormat.Data.Table.ColumnProperty[tableSpec.getNumColumns()];
      
      for(int i=0; i<columnSpecs.length; ++i) {
        DataColumnSpec columnSpec = tableSpec.getColumnSpec(i);
        columnSpecs[i] = new JsonFormat.Data.Table.ColumnProperty();
        columnSpecs[i].id = columnSpec.getName();
        columnSpecs[i].type = convertCellClassToTypeString(columnSpec.getType().getCellClass());
      }
      return columnSpecs;
    }
    
    private static JsonFormat.Data.Table.ItemProperty createItemProperty(String id, Object value) {
      JsonFormat.Data.Table.ItemProperty property = new JsonFormat.Data.Table.ItemProperty();
      property.id = id;
      property.value = value;
      return property;
    }
    
    // Knime to json
    private static JsonFormat.Data.Table.ItemProperty[][] convertToProperties(BufferedDataTable table) {
      
      JsonFormat.Data.Table.ItemProperty[][] properties = new JsonFormat.Data.Table.ItemProperty[(int) table.size()][];
      DataTableSpec spec = table.getSpec();
      
      int iRow = 0;
      for(DataRow row : table) {
        
        List<JsonFormat.Data.Table.ItemProperty> propertyList = new ArrayList<>();
        int iCol = 0;
        
        for(DataCell cell : row) {
          if(!cell.isMissing()) { 

            propertyList.add(createItemProperty(
                spec.getColumnSpec(iCol).getName(),
                convertDataCellToValue(cell))); 
          }
          ++iCol;
        }
        properties[iRow++] = propertyList.toArray(new JsonFormat.Data.Table.ItemProperty[0]);
      }
        
      return properties;
    }
    
    private static JsonFormat.Data.Table convertToTable(BufferedDataTable knimeTable) {
      JsonFormat.Data.Table jsonTable  = new JsonFormat.Data.Table();
      jsonTable.columnProperties = convertFromDataTableSpecs(knimeTable.getSpec());
      jsonTable.data = convertToProperties(knimeTable);
      return jsonTable;
    }
    
    public void setData(BufferedDataTable stationTable, BufferedDataTable deliveryTable, BufferedDataTable deliveryRelationTable) {
      json.data = new JsonFormat.Data();
      json.data.version = CURRENT_VERSION;
      
      json.data.stations = convertToTable(stationTable);
      json.data.deliveries = convertToTable(deliveryTable);
      json.data.deliveryRelations = convertToTable(deliveryRelationTable);
      
    }
    
    private static Tracing.TraceableUnit createTraceableUnit(String id, Double weight, Boolean crossContamination, Boolean killContamination,
        Boolean observed) { 
      Tracing.TraceableUnit unit = new Tracing.TraceableUnit();
      unit.id = id;
      unit.crossContamination = crossContamination;
      unit.killContamination = killContamination;
      unit.observed = observed;
      return unit;
    }
    
    protected void setTracing(Map<String, GraphNode> nodes, List<Edge<GraphNode>> edges) {
      json.tracing = new JsonFormat.Tracing();
      json.tracing.version = CURRENT_VERSION;
      
      json.tracing.nodes = nodes.values().stream().map(node -> createTraceableUnit(
          node.getId(), 
          (Double) node.getProperties().get(TracingColumns.WEIGHT), 
          (Boolean) node.getProperties().get(TracingColumns.CROSS_CONTAMINATION), 
          (Boolean) node.getProperties().get(TracingColumns.KILL_CONTAMINATION), 
          (Boolean) node.getProperties().get(TracingColumns.OBSERVED))).collect(Collectors.toList()).toArray(new Tracing.TraceableUnit[0]);
      json.tracing.deliveries = edges.stream().map(edge -> createTraceableUnit(
          edge.getId(), 
          (Double) edge.getProperties().get(TracingColumns.WEIGHT), 
          (Boolean) edge.getProperties().get(TracingColumns.CROSS_CONTAMINATION), 
          (Boolean) edge.getProperties().get(TracingColumns.KILL_CONTAMINATION), 
          (Boolean) edge.getProperties().get(TracingColumns.OBSERVED))).collect(Collectors.toList()).toArray(new Tracing.TraceableUnit[0]);
    }
    
    protected void setTracing(BufferedDataTable stationTable, BufferedDataTable edgeTable) {
      // check
      List<String> idList = Arrays.asList(TracingColumns.ID, TracingColumns.WEIGHT, TracingColumns.CROSS_CONTAMINATION, TracingColumns.KILL_CONTAMINATION, TracingColumns.OBSERVED);
      if(!idList.stream().allMatch(id -> stationTable.getSpec().containsName(id))) return;
      if(!idList.stream().allMatch(id -> edgeTable.getSpec().containsName(id))) return;
      
      json.tracing = new JsonFormat.Tracing();
      
      List<Tracing.TraceableUnit> itemList = new ArrayList<>();
      
      Map<String, Integer> idToIndexMap = idList.stream().collect(Collectors.toMap(id -> id, id -> stationTable.getSpec().findColumnIndex(id))); //  map(id -> stationTable.getSpec().findColumnIndex(columnName))
            
      for(DataRow row : stationTable) 
        itemList.add(createTraceableUnit(
            ((StringCell) row.getCell(idToIndexMap.get(TracingColumns.ID))).getStringValue(), 
            ((DoubleCell) row.getCell(idToIndexMap.get(TracingColumns.WEIGHT))).getDoubleValue(),
            ((BooleanCell) row.getCell(idToIndexMap.get(TracingColumns.CROSS_CONTAMINATION))).getBooleanValue(),
            ((BooleanCell) row.getCell(idToIndexMap.get(TracingColumns.KILL_CONTAMINATION))).getBooleanValue(),
            ((BooleanCell) row.getCell(idToIndexMap.get(TracingColumns.OBSERVED))).getBooleanValue()));  
      
      json.tracing.nodes =  itemList.toArray(new Tracing.TraceableUnit[0]);
      
      itemList = new ArrayList<>();
      idToIndexMap = idList.stream().collect(Collectors.toMap(id -> id, id -> edgeTable.getSpec().findColumnIndex(id)));
      for(DataRow row : edgeTable) 
        itemList.add(createTraceableUnit(
            ((StringCell) row.getCell(idToIndexMap.get(TracingColumns.ID))).getStringValue(), 
            ((DoubleCell) row.getCell(idToIndexMap.get(TracingColumns.WEIGHT))).getDoubleValue(),
            ((BooleanCell) row.getCell(idToIndexMap.get(TracingColumns.CROSS_CONTAMINATION))).getBooleanValue(),
            ((BooleanCell) row.getCell(idToIndexMap.get(TracingColumns.KILL_CONTAMINATION))).getBooleanValue(),
            ((BooleanCell) row.getCell(idToIndexMap.get(TracingColumns.OBSERVED))).getBooleanValue()));  
      
      json.tracing.deliveries =  itemList.toArray(new Tracing.TraceableUnit[0]);
    }
    
    
    
    protected void merge(JsonFormat jsonWithSettings) {
      if(jsonWithSettings.tracing!=null) json.tracing = jsonWithSettings.tracing;
      json.settings = jsonWithSettings.settings;
    }
    
    public JsonValue build() throws JsonProcessingException {
      
      return convertToJsonValue(this.json);
    }
  }
  
  public static JsonFormat convertFromJson(JsonValue json) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
  
    JsonNode rootNode = JacksonConversions.getInstance().toJackson(json);
    return mapper.treeToValue(rootNode, JsonFormat.class);
  }
  
  public static JsonValue convertToJsonValue(JsonFormat json) throws JsonProcessingException {
    
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode rootNode = mapper.valueToTree(json);
    
    return JacksonConversions.getInstance().toJSR353(rootNode);
  }
  
  public static boolean hasDataChanged(JsonFormat.Data data, BufferedDataTable stationTable, BufferedDataTable deliveryTable, BufferedDataTable deliveryRelationTable) throws InvalidSettingsException {
    JsonBuilder jsonBuilder = new JsonBuilder();
    jsonBuilder.setData(stationTable, deliveryTable, deliveryRelationTable);
    return         
        !(areTablesEqual(data.stations, jsonBuilder.json.data.stations) &&
        areTablesEqual(data.deliveries, jsonBuilder.json.data.deliveries) && 
        areTablesEqual(data.deliveryRelations, jsonBuilder.json.data.deliveryRelations));
  }
  
  private static boolean areTablesEqual(JsonFormat.Data.Table table1, JsonFormat.Data.Table table2) throws InvalidSettingsException {
    Map<String, String> colMap1 = convertTableColumnsToMap(table1.columnProperties);
    Map<String, String> colMap2 = convertTableColumnsToMap(table2.columnProperties);
    if(!colMap1.equals(colMap2)) return false;
    Set<Map<String, Object>> dataSet1 = convertTableDataToSet(table1, colMap1);
    Set<Map<String, Object>> dataSet2 = convertTableDataToSet(table2, colMap1);
    return dataSet1.equals(dataSet2);
    
  }
  
  private static Set<Map<String, Object>> convertTableDataToSet(JsonFormat.Data.Table table, Map<String, String> columnTypeMap) throws InvalidSettingsException {
    Set<Map<String,Object>> result = new HashSet<>();
    for(JsonFormat.Data.Table.ItemProperty[] properties : table.data) {
      Map<String,Object> map = new HashMap<>();
      for(JsonFormat.Data.Table.ItemProperty property : properties) map.put(property.id, convertValue(property.value, columnTypeMap.get(property.id)));
      result.add(map);
    }
    return result;
  }
  
  private static Object convertValue(Object value, String toType) throws InvalidSettingsException {
    if(Strings.isNullOrEmpty(toType)) throw(new InvalidSettingsException("A datatype is missing."));
    
    Function<Object,Object> converterFun = TYPESTRING_TO_VALUECONVERTER_FUN_MAP.get(toType);
    if(converterFun==null) throw(new InvalidSettingsException("The datatype '" + toType + "' is unknown."));
    
    try {
      return converterFun.apply(value);
    } catch(Exception ex) {
      throw(new InvalidSettingsException("A value could not be converted to type '" + toType + "' (" + ex.getMessage() + ")."));
    }
  }
  
  private static Map<String, String> convertTableColumnsToMap(JsonFormat.Data.Table.ColumnProperty[] columnProperties) {
    Map<String,String> result = new HashMap<>();
    for(JsonFormat.Data.Table.ColumnProperty property : columnProperties) { result.put(property.id, property.type); }
    return result;
  }
  
}
