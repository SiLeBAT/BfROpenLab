package de.bund.bfr.knime.openkrise.util.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.json.JsonValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataTypeRegistry;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.*;
import org.knime.core.data.def.BooleanCell.BooleanCellFactory;
import org.knime.core.data.json.JacksonConversions;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.Tracing;

public class JsonConverter {
//  private static final ImmutableMap<String, DataType> NAME_TO_DATATYPE_MAP = new ImmutableMap.Builder<String, DataType>()
//      .put(StringCell.TYPE.getName(), StringCell.TYPE)
//      .put(LongCell.TYPE.getName(), LongCell.TYPE)
//      .put(IntCell.TYPE.getName(), IntCell.TYPE)
//      .put(BooleanCell.TYPE.getName(), BooleanCell.TYPE)
//      .put(LongCell.TYPE.getName(), LongCell.TYPE)
//      .put(LongCell.TYPE.getName(), LongCell.TYPE)
//      .put(LongCell.TYPE.getName(), LongCell.TYPE)
//      .put(LongCell.TYPE.getName(), LongCell.TYPE)
//      .build();
  
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
  
  
//  private static IntCell createIntCell(Object value) {
//    int castedValue = 0;
//    try {
//      castedValue = ((Number) value).intValue();
//    } catch(Exception e) {
//      e.printStackTrace();
//    }
//    return new IntCell(castedValue);
//  }
//  
//  private static boolean getBooleanValue(DataCell cell) {
//    boolean result=false;
//    try {
//      result = ((BooleanCell) cell).getBooleanValue();
//    } catch(Exception e) {
//      e.printStackTrace();
//    }
//    return result;
//  }
//  
//  private static int getIntValue(DataCell cell) {
//    int result = 0;
//    try {
//      result = ((IntCell) cell).getIntValue();
//    } catch(Exception e) {
//      e.printStackTrace();
//    }
//    return result;
//  }
//  
//  private static long getLongValue(DataCell cell) {
//    long result = 0;
//    try {
//      result = ((LongCell) cell).getLongValue();
//    } catch(Exception e) {
//      e.printStackTrace();
//    }
//    return result;
//  }
//  {
//    Map<Class<? extends DataCell>, Function<Object,DataCell>> dataCellFunMap = new ImmutableMap.Builder<Class<? extends DataCell>, Function<Object,DataCell>>()
//        .put(BooleanCell.class, (obj) -> (BooleanCell) BooleanCellFactory.create((boolean) obj))
//        .put(DoubleCell.class, (obj) -> new DoubleCell((Double) obj))
//        .put(IntCell.class, (obj) -> new IntCell((Integer) obj))
//        .put(LongCell.class, (obj) -> new LongCell((Long) obj))
//        .put(StringCell.class, (obj) -> new StringCell((String) obj))
//        .build();
//    
//    Optional<Class<? extends DataCell>> tmp = DataTypeRegistry.getInstance().getCellClass("ldkf");
//  }
  
  //private static final ImmutableMap<String, DataType> NAME_TO_DATACELL_FUN_MAP = new ImmutableMap.Builder<String, DataType>().put(StringCell.TYPE.getName(), StringCell.TYPE).build();
//  private static final ImmutableMap<DataType, DataCell> DATATYPE_TO_DATACELL = new ImmutableMap.Builder<DataType, DataCell>().build();
  
  public static DataType convertToDataType(String type) {
    Optional<Class<? extends DataCell>> optionalDataCellClass = DataTypeRegistry.getInstance().getCellClass(type);
    if(optionalDataCellClass.isPresent()) return DataType.getType(optionalDataCellClass.get());
    return null;
  }
  
//  public static Class<? extends DataCell> convertToDataCellClass(String type) {
//    Optional<Class<? extends DataCell>> optionalDataCellClass = DataTypeRegistry.getInstance().getCellClass(type);
//    if(optionalDataCellClass.isPresent()) return optionalDataCellClass.get();
//    return null;
//  }
  
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
  
  public static BufferedDataTable createDataTable(JsonFormat.Data.Table dataTable, ExecutionContext exec) {
    Map<String, DataType> dataColumns = new LinkedHashMap<>();
    
    for(JsonFormat.Data.Table.ColumnProperty columnProperty : dataTable.columnProperties) dataColumns.put(columnProperty.id, JsonConverter.convertToDataType(columnProperty.type));
    DataTableSpec tableSpec = toTableSpec(dataColumns);
    BufferedDataContainer dataContainer = exec.createDataContainer(tableSpec);
    
    long rowIndex = 0;
    for (JsonFormat.Data.Table.ItemProperty[] properties : dataTable.data) {
      DataCell[] cells = new DataCell[tableSpec.getNumColumns()];

      Arrays.fill(cells, DataType.getMissingCell());
      
      for(JsonFormat.Data.Table.ItemProperty property : properties) {
        int columnIndex = tableSpec.findColumnIndex(property.id);
        
        if(property.value!=null) cells[columnIndex] = JsonConverter.createDataCell(property.value, tableSpec.getColumnSpec(columnIndex).getType().getCellClass());
      }
      
      dataContainer.addRowToTable(new DefaultRow( RowKey.createRowKey(rowIndex++),cells));
    }
    
    dataContainer.close();
    return dataContainer.getTable();
  }
  
//  public static DataCell convertToDataCell(Object value, DataType type) {
//    
//  }
  
  public static class JsonBuilder {
    private JsonFormat json;
    
    public JsonBuilder() {
      json = new JsonFormat();
    }
    
//    private static JsonFormat.Data.Table.ColumnProperty createColumnSpec(String id, String type) {
//      JsonFormat.Data.Table.ColumnProperty columnSpec = new JsonFormat.Data.Table.ColumnProperty();
//      columnSpec.id = id;
//      columnSpec.type = type;
//      return columnSpec;
//    }
//    
//    private static JsonFormat.Data.Property createProperty(String id, Object value) {
//      JsonFormat.Data.Property property = new JsonFormat.Data.Property();
//      property.id = id;
//      property.value = value;
//      return property;
//    }
//    
//    private static JsonFormat.Data.DeliveryRelation createDeliveryRelation(String fromId, String toId) {
//      JsonFormat.Data.DeliveryRelation deliveryRelation = new JsonFormat.Data.DeliveryRelation();
//      deliveryRelation.fromId = fromId;
//      deliveryRelation.toId = toId;
//      return deliveryRelation;
//    }
    
//    public void setData(NodePropertySchema nodeSchema, EdgePropertySchema edgeSchema, 
//        Map<String, GraphNode> nodes, List<Edge<GraphNode>> edges, Map<String, Delivery> deliveries) {
//      json.data = new JsonFormat.Data();
//      //json.data.deliveryColumns = new JsonFormat.Data.ColumnSpec[edgeSchema.getMap().size()];
//      json.data.deliveryColumns = edgeSchema.getMap().entrySet().stream().map(entry -> createColumnSpec(entry.getKey(),entry.getValue().getName())).collect(Collectors.toList()).toArray(new JsonFormat.Data.ColumnSpec[0]);
//      json.data.stationColumns = nodeSchema.getMap().entrySet().stream().map(entry -> createColumnSpec(entry.getKey(),entry.getValue().getName())).collect(Collectors.toList()).toArray(new JsonFormat.Data.ColumnSpec[0]);
//      json.data.stations = new JsonFormat.Data.Property[nodes.size()][];
//      
//      List<JsonFormat.Data.DeliveryRelation> deliveryRelationList = new ArrayList<>();
//      
//      int i = 0;
//      for(GraphNode node : nodes.values()) {
//        
//        List<JsonFormat.Data.Property> propertyList = new ArrayList<>();
//        
//        propertyList.add(createProperty(TracingColumns.ID,node.getId()));
//        for(Map.Entry<String, Object> entry : node.getProperties().entrySet()) if(entry.getValue()!=null) propertyList.add(createProperty(entry.getKey(),entry.getValue()));
//        json.data.stations[i++] = propertyList.toArray(new JsonFormat.Data.Property[0]);
//      }
//      
//      i = 0;
//      for(Edge<GraphNode> edge : edges) {
//        
//        List<JsonFormat.Data.Property> propertyList = new ArrayList<>();
//        
//        propertyList.add(createProperty(TracingColumns.ID,edge.getId()));
//        for(Map.Entry<String, Object> entry : edge.getProperties().entrySet()) if(entry.getValue()!=null) propertyList.add(createProperty(entry.getKey(),entry.getValue()));
//        json.data.deliveries[i++] = propertyList.toArray(new JsonFormat.Data.Property[0]);
//        
//        for (String next : deliveries.get(edge.getId()).getAllNextIds()) deliveryRelationList.add(createDeliveryRelation(edge.getId(), next));
//          
//      }
//      json.data.deliveryRelations = deliveryRelationList.toArray(new JsonFormat.Data.DeliveryRelation[0]);
//    }
    
    private static JsonFormat.Data.Table.ColumnProperty[] convertFromDataTableSpecs(DataTableSpec tableSpec) {
      
      JsonFormat.Data.Table.ColumnProperty[] columnSpecs = new JsonFormat.Data.Table.ColumnProperty[tableSpec.getNumColumns()];
      //int[] tmp = IntStream.iterate(0, i -> i + 1).limit(table.getSpec().getNumColumns()).mapToObj(i -> table.getSpec().getColumnSpec(i)).map(spec -> );
      for(int i=0; i<columnSpecs.length; ++i) {
        DataColumnSpec columnSpec = tableSpec.getColumnSpec(i);
        columnSpecs[i] = new JsonFormat.Data.Table.ColumnProperty();
        columnSpecs[i].id = columnSpec.getName();
        columnSpecs[i].type = columnSpec.getType().getCellClass().getName();
      }
      return columnSpecs;
    }
    
    private static JsonFormat.Data.Table.ItemProperty createItemProperty(String id, Object value) {
      JsonFormat.Data.Table.ItemProperty property = new JsonFormat.Data.Table.ItemProperty();
      property.id = id;
      property.value = value;
      return property;
    }
    
    private static JsonFormat.Data.Table.ItemProperty[][] convertToProperties(BufferedDataTable table) {
      
      JsonFormat.Data.Table.ItemProperty[][] properties = new JsonFormat.Data.Table.ItemProperty[(int) table.size()][];
      DataTableSpec spec = table.getSpec();
      
      int iRow = 0;
      for(DataRow row : table) {
        
        List<JsonFormat.Data.Table.ItemProperty> propertyList = new ArrayList<>();
        int iCol = 0;
        
        for(DataCell cell : row) {
          if(!cell.isMissing() && DATACELLCLASS_TO_GETVALUE_FUN_MAP.containsKey(cell.getClass())) {

            propertyList.add(createItemProperty(
                spec.getColumnSpec(iCol).getName(),
                DATACELLCLASS_TO_GETVALUE_FUN_MAP.get(cell.getClass()).apply(cell)));
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
      //json.data.deliveryColumns = new JsonFormat.Data.ColumnSpec[edgeSchema.getMap().size()];
//      json.data.stationColumns = convertToColumnSpecs(stationTable);
//      json.data.deliveryColumns = convertToColumnSpecs(deliveryTable);
      json.data.stations = convertToTable(stationTable);
      json.data.deliveries = convertToTable(deliveryTable);
      json.data.deliveryRelations = convertToTable(deliveryRelationTable);
      
//      json.data.stations = convertDataTableToProperties(stationTable);
//      json.data.deliveries = convertDataTableToProperties(stationTable);
      
//      List<JsonFormat.Data.DeliveryRelation> deliveryRelationList = new ArrayList<>();
//      for(DataRow row: deliveryRelationTable) deliveryRelationList.add(createDeliveryRelation(row.getCell(0).toString(), row.getCell(1).toString()));
//      json.data.deliveryRelations = deliveryRelationList.toArray(new JsonFormat.Data.DeliveryRelation[0]);
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
    //SettingsJson obj = new SettingsJson();
   
    ObjectNode rootNode = mapper.valueToTree(json);
    
   
    //StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    return JacksonConversions.getInstance().toJSR353(rootNode);
  }
}
