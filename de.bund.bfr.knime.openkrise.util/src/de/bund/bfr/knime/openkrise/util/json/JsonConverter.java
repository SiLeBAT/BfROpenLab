package de.bund.bfr.knime.openkrise.util.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.json.JsonValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.DataTypeRegistry;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.def.*;
import org.knime.core.data.def.BooleanCell.BooleanCellFactory;
import org.knime.core.data.json.JacksonConversions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.util.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.util.NodePropertySchema;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.common.Delivery;
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
      .put(BooleanCell.class, (obj) -> (BooleanCell) BooleanCellFactory.create((boolean) obj))
      .put(DoubleCell.class, (obj) -> new DoubleCell((Double) obj))
      .put(IntCell.class, (obj) -> new IntCell((Integer) obj))
      .put(LongCell.class, (obj) -> new LongCell((Long) obj))
      .put(StringCell.class, (obj) -> new StringCell((String) obj))
      .build();
  
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
  
  public static Class<? extends DataCell> convertToDataCellClass(String type) {
    Optional<Class<? extends DataCell>> optionalDataCellClass = DataTypeRegistry.getInstance().getCellClass(type);
    if(optionalDataCellClass.isPresent()) return optionalDataCellClass.get();
    return null;
  }
  
  public static DataCell createDataCell(Object value, Class<? extends DataCell> dataCellClass) {
    Function<Object, DataCell> fun = DATACELLCLASS_TO_CREATEDATACELL_FUN_MAP.get(dataCellClass);
    if(fun!=null) return fun.apply(value);
    return null;
  }
  
//  public static DataCell convertToDataCell(Object value, DataType type) {
//    
//  }
  
  public static class JsonBuilder {
    private JsonFormat json;
    
    public JsonBuilder() {
      json = new JsonFormat();
    }
    
    private static JsonFormat.Data.ColumnSpec createColumnSpec(String id, String type) {
      JsonFormat.Data.ColumnSpec columnSpec = new JsonFormat.Data.ColumnSpec();
      columnSpec.id = id;
      columnSpec.type = type;
      return columnSpec;
    }
    
    private static JsonFormat.Data.Property createProperty(String id, Object value) {
      JsonFormat.Data.Property property = new JsonFormat.Data.Property();
      property.id = id;
      property.value = value;
      return property;
    }
    
    private static JsonFormat.Data.DeliveryRelation createDeliveryRelation(String fromId, String toId) {
      JsonFormat.Data.DeliveryRelation deliveryRelation = new JsonFormat.Data.DeliveryRelation();
      deliveryRelation.fromId = fromId;
      deliveryRelation.toId = toId;
      return deliveryRelation;
    }
    
    public void setData(NodePropertySchema nodeSchema, EdgePropertySchema edgeSchema, 
        Map<String, GraphNode> nodes, List<Edge<GraphNode>> edges, Map<String, Delivery> deliveries) {
      json.data = new JsonFormat.Data();
      //json.data.deliveryColumns = new JsonFormat.Data.ColumnSpec[edgeSchema.getMap().size()];
      json.data.deliveryColumns = edgeSchema.getMap().entrySet().stream().map(entry -> createColumnSpec(entry.getKey(),entry.getValue().getName())).collect(Collectors.toList()).toArray(new JsonFormat.Data.ColumnSpec[0]);
      json.data.stationColumns = nodeSchema.getMap().entrySet().stream().map(entry -> createColumnSpec(entry.getKey(),entry.getValue().getName())).collect(Collectors.toList()).toArray(new JsonFormat.Data.ColumnSpec[0]);
      json.data.stations = new JsonFormat.Data.Property[nodes.size()][];
      
      List<JsonFormat.Data.DeliveryRelation> deliveryRelationList = new ArrayList<>();
      
      int i = 0;
      for(GraphNode node : nodes.values()) {
        
        List<JsonFormat.Data.Property> propertyList = new ArrayList<>();
        
        propertyList.add(createProperty(TracingColumns.ID,node.getId()));
        for(Map.Entry<String, Object> entry : node.getProperties().entrySet()) if(entry.getValue()!=null) propertyList.add(createProperty(entry.getKey(),entry.getValue()));
        json.data.stations[i++] = propertyList.toArray(new JsonFormat.Data.Property[0]);
      }
      
      i = 0;
      for(Edge<GraphNode> edge : edges) {
        
        List<JsonFormat.Data.Property> propertyList = new ArrayList<>();
        
        propertyList.add(createProperty(TracingColumns.ID,edge.getId()));
        for(Map.Entry<String, Object> entry : edge.getProperties().entrySet()) if(entry.getValue()!=null) propertyList.add(createProperty(entry.getKey(),entry.getValue()));
        json.data.deliveries[i++] = propertyList.toArray(new JsonFormat.Data.Property[0]);
        
        for (String next : deliveries.get(edge.getId()).getAllNextIds()) deliveryRelationList.add(createDeliveryRelation(edge.getId(), next));
          
      }
      json.data.deliveryRelations = deliveryRelationList.toArray(new JsonFormat.Data.DeliveryRelation[0]);
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
    
    protected void merge(JsonFormat jsonWithSettings) {
      if(jsonWithSettings.tracing!=null) json.tracing = jsonWithSettings.tracing;
      json.settings = jsonWithSettings.settings;
    }
    
    public JsonValue build() throws JsonProcessingException {
      
      return convertToJson(this.json);
    }
  }
  
  public static JsonFormat convertFromJson(JsonValue json) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
  
    JsonNode rootNode = JacksonConversions.getInstance().toJackson(json);
    return mapper.treeToValue(rootNode, JsonFormat.class);
  }
  
  public static JsonValue convertToJson(JsonFormat json) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    //SettingsJson obj = new SettingsJson();
   
    ObjectNode rootNode = mapper.valueToTree(json);
    
   
    //StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    return JacksonConversions.getInstance().toJSR353(rootNode);
  }
}
