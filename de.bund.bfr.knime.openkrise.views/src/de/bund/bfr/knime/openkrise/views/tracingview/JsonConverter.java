package de.bund.bfr.knime.openkrise.views.tracingview;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.json.JsonValue;
import org.knime.core.node.InvalidSettingsException;
import com.fasterxml.jackson.core.JsonProcessingException;
import de.bund.bfr.jung.LabelPosition;
import de.bund.bfr.jung.NamedShape;
import de.bund.bfr.knime.gis.GisType;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.Tracing;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.MetaNode;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View.LogicalCondition;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View.SharedViewSettings.EdgeSettings;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View.SharedViewSettings.NodeSettings;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View.SharedViewSettings.TextSettings;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View.ValueCondition;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View.GlobalEdgeSettings.EdgeHighlightCondition;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View.GlobalEdgeSettings.Filter;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View.GlobalEdgeSettings.Filter.DeliveryToDateFilter;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View.GlobalNodeSettings.NodeHighlightCondition;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View.GisSettings;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View.GraphSettings;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View.ExplosionSettings;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View.ExplosionSettings.ExplosionGraphSettings;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View.Transformation;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View.XYPair;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View.NodePosition;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.Date;

public class JsonConverter {
  public static class JsonBuilder extends de.bund.bfr.knime.openkrise.util.json.JsonConverter.JsonBuilder {
    
    private static final String CURRENT_VERSION = "1.0.0";
    
    private JsonFormat json;
    private JsonFormat.TracingViewSettings settings;
    
    public JsonBuilder() {
      super();
      json = this.getJson();
      json.settings = new JsonFormat.TracingViewSettings();
      json.settings.version = CURRENT_VERSION;
      
      settings = json.settings;
    }
    
    
    private static Tracing.TraceableUnit createTraceableUnit(String id, Double weight, Boolean crossContamination, Boolean killContamination,
        Boolean observed) { 
      Tracing.TraceableUnit unit = new Tracing.TraceableUnit();
      unit.id = id;
      unit.weight = weight;
      unit.crossContamination = crossContamination;
      unit.killContamination = killContamination;
      unit.observed = observed;
      return unit;
    }
    
    public JsonBuilder setTracing(boolean enforceTemporalOrder, 
        Map<String,Double> edgeWeights, Map<String, Boolean> edgeCrossContaminations, Map<String, Boolean> edgeKillContaminations, Map<String, Boolean> observedEdges, 
        Map<String,Double> nodeWeights, Map<String, Boolean> nodeCrossContaminations, Map<String, Boolean> nodeKillContaminations, Map<String, Boolean> observedNodes) {
      
      json.tracing = new Tracing();
      json.tracing.version = CURRENT_VERSION;
      
      json.tracing.enforceTemporalOrder = enforceTemporalOrder;
      List<Tracing.TraceableUnit> tracingList = new ArrayList<>();

      for(String id: edgeCrossContaminations.keySet()) 
        tracingList.add(
            createTraceableUnit(id, edgeWeights.get(id), edgeCrossContaminations.get(id), edgeKillContaminations.get(id), observedEdges.get(id)));

      json.tracing.deliveries = tracingList.toArray(new Tracing.TraceableUnit[0]);

      tracingList = new ArrayList<>();

      for(String id: nodeCrossContaminations.keySet()) 
        tracingList.add(
            createTraceableUnit(id, nodeWeights.get(id), nodeCrossContaminations.get(id), nodeKillContaminations.get(id), observedNodes.get(id)));

      json.tracing.nodes = tracingList.toArray(tracingList.toArray(new Tracing.TraceableUnit[0]));
      return this;
    }
    
    private static MetaNode createMetaNode(String id, String type, String[] members) {
      MetaNode metaNode= new MetaNode();
      metaNode.id = id;
      metaNode.type = type;
      metaNode.members = members;
      return metaNode;
      
    }
    public JsonBuilder setMetaNodes(Map<String, Map<String, Point2D>> collapsedNodes) {
      List<MetaNode> metaNodeList = new ArrayList<>();
    
      for(Map.Entry<String, Map<String, Point2D>> entry: collapsedNodes.entrySet()) {
        metaNodeList.add(createMetaNode(entry.getKey(), (entry.getKey().startsWith("SC:")?"SimpleChain":null), entry.getValue().keySet().toArray(new String[0])));
      }
      settings.metaNodes = metaNodeList.toArray(new MetaNode[0]);
      return this;
    }
    
    public JsonBuilder setGlobalViewSettings(boolean showLegend, boolean exportAsSvg, boolean showGis, GisType gisType, String label) {
      if(settings.view==null) settings.view = new View();
      
      settings.view.showLegend = showLegend;
      settings.view.exportAsSvg = exportAsSvg;
      settings.view.showGis = showGis;
      settings.view.gisType = gisType==null?null:gisType.name();
      settings.view.label = label;
      return this;
    }
    
    private static ValueCondition getValueCondition(HighlightCondition hLCondition) {
      
      ValueHighlightCondition valueHighlightCondition = null; 
      if(hLCondition instanceof LogicalValueHighlightCondition) {//AndOrHighlightCondition) {
        valueHighlightCondition = ((LogicalValueHighlightCondition) hLCondition).getValueCondition();
      } else if(hLCondition instanceof ValueHighlightCondition) {
        valueHighlightCondition = (ValueHighlightCondition) hLCondition;
      }
      if(valueHighlightCondition!=null) {
        ValueCondition valueCondition = new ValueCondition();
        valueCondition.propertyName = valueHighlightCondition.getProperty();
        valueCondition.valueType = valueHighlightCondition.getType().name();
        valueCondition.useZeroAsMinimum = valueHighlightCondition.isZeroAsMinimum();
        return valueCondition;
      }
      return null;
    }
    
    private static LogicalCondition getLogicalCondition(LogicalHighlightCondition logicalHighlightCondition) {
      LogicalCondition logicalCondition = new LogicalCondition();
      logicalCondition.propertyName = logicalHighlightCondition.getProperty();
      logicalCondition.operationType = logicalHighlightCondition.getType().name(); 
      logicalCondition.value = logicalHighlightCondition.getValue();
      return logicalCondition;
    }
    
    private static LogicalCondition[][] getLogicalConditions(HighlightCondition hLCondition) {
      
      AndOrHighlightCondition andOrHighlightCondition = null; 
      if(hLCondition instanceof LogicalValueHighlightCondition) {
        andOrHighlightCondition = ((LogicalValueHighlightCondition) hLCondition).getLogicalCondition();
      } else if(hLCondition instanceof AndOrHighlightCondition) {
        andOrHighlightCondition = (AndOrHighlightCondition) hLCondition;
      }
      if(andOrHighlightCondition!=null) {
        int nOr = andOrHighlightCondition.getConditions().size();
        LogicalCondition[][] result = new LogicalCondition[nOr][];
        for(int iOr=0; iOr<nOr; ++iOr) {
          int nAnd = andOrHighlightCondition.getConditions().get(iOr).size();
          result[iOr] = new LogicalCondition[nAnd];
          for(int iAnd=0; iAnd<nAnd; ++iAnd) result[iOr][iAnd] = getLogicalCondition(andOrHighlightCondition.getConditions().get(iOr).get(iAnd));
        }
        return result;
      }
      return null;
    }
    
    private void initHighlightCondition(View.HighlightCondition condition, String name, Boolean showInLegend, int[] color, Boolean invisible, Boolean adjustThickness, String label, ValueCondition valueCondition, LogicalCondition[][] logicalConditions) {
      condition.name = name;
      condition.showInLegend = showInLegend;
      condition.color = color;
      condition.invisible = invisible;
      condition.adjustThickness = adjustThickness;
      condition.labelProperty = label;
      condition.valueCondition = valueCondition;
      condition.logicalConditions = logicalConditions;
    }
    
    private EdgeHighlightCondition createEdgeHighlightCondition(String name, Boolean showInLegend, int[] color, Boolean invisible, Boolean adjustThickness, String label, ValueCondition valueCondition, LogicalCondition[][] logicalConditions, String linePattern) {
      EdgeHighlightCondition condition = new EdgeHighlightCondition();
      initHighlightCondition(condition, name, showInLegend, color, invisible, adjustThickness, label, valueCondition, logicalConditions);
      condition.linePattern = linePattern;
      return condition;
    }
    
    private EdgeHighlightCondition[] createEdgeHighlightings(HighlightConditionList highlightConditions) {
      
      List<EdgeHighlightCondition> edgeHighlightConditionList = new ArrayList<>();
      
      for(HighlightCondition hLCondition: highlightConditions.getConditions()) {
        Color color = hLCondition.getColor();
        
        edgeHighlightConditionList.add(
            createEdgeHighlightCondition(
                hLCondition.getName(), hLCondition.isShowInLegend(), 
                (int []) (color==null?null:new int[] {color.getRed(), color.getGreen(), color.getBlue()}),
                hLCondition.isInvisible(), hLCondition.isUseThickness(), hLCondition.getLabelProperty(), 
                getValueCondition(hLCondition), getLogicalConditions(hLCondition),
                null));
      }
      
      return edgeHighlightConditionList.toArray(new EdgeHighlightCondition[0]);
    }
    
    private NodeHighlightCondition createNodeHighlightCondition(String name, Boolean showInLegend, int[] color, Boolean invisible, Boolean adjustThickness, String label, ValueCondition valueCondition, LogicalCondition[][] logicalConditions, NamedShape shape) {
      
      NodeHighlightCondition condition = new NodeHighlightCondition();
      initHighlightCondition(condition, name, showInLegend, color, invisible, adjustThickness, label, valueCondition, logicalConditions);
      
      condition.shape = shape==null?null:shape.name();
      return condition;
    }
    
  private NodeHighlightCondition[] createNodeHighlightings(HighlightConditionList highlightConditions) {
      
      List<NodeHighlightCondition> nodeHighlightConditionList = new ArrayList<>();
      
      for(HighlightCondition hLCondition: highlightConditions.getConditions()) {
        Color color = hLCondition.getColor();
        
        nodeHighlightConditionList.add(
            createNodeHighlightCondition(
                hLCondition.getName(), hLCondition.isShowInLegend(), 
                (int []) (color==null?null:new int[] {color.getRed(), color.getGreen(), color.getBlue()}),
                hLCondition.isInvisible(), hLCondition.isUseThickness(), hLCondition.getLabelProperty(), 
                getValueCondition(hLCondition), getLogicalConditions(hLCondition),
                hLCondition.getShape()));
      }
      
      return nodeHighlightConditionList.toArray(new NodeHighlightCondition[0]);
    }
    
    
    public JsonBuilder setGlobalNodeViewSettings(boolean skipEdgelessNodes, LabelPosition nodeLabelPosition, String[] selectedNodes, HighlightConditionList nodeHighlightConditions) {
      
      if(settings.view==null) settings.view = new View();
      if(settings.view.node==null) settings.view.node = new View.GlobalNodeSettings();
      settings.view.node.skipEdgelessNodes = skipEdgelessNodes;
      settings.view.node.labelPosition = nodeLabelPosition.name();
      settings.view.node.selectedNodes = selectedNodes;
      settings.view.node.highlightConditions = createNodeHighlightings(nodeHighlightConditions); 
      return this;
    }
    
    public JsonBuilder setGlobalEdgeViewSettings(boolean arrowHeadInMiddle, boolean hideArrowHead, boolean joinEdges, 
        String deliveryDateFilterColumn, Calendar deliveryDateFilterDate, boolean showDeliveriesWithoutDate,
        boolean showEdgesInMetaNode, boolean showCrossContaminatedDeliveries, String[] selectedEdges,
        HighlightConditionList edgeHighlightConditions) {
      
      if(settings.view==null) settings.view = new View();
      if(settings.view.edge==null) settings.view.edge = new View.GlobalEdgeSettings();
      settings.view.edge.arrowHeadInMiddle = arrowHeadInMiddle;
      settings.view.edge.hideArrowHead = hideArrowHead;
      settings.view.edge.joinEdges = joinEdges;
      settings.view.edge.filter = new Filter();
      settings.view.edge.filter.dateFilter = new DeliveryToDateFilter();
      settings.view.edge.filter.dateFilter.dateId = deliveryDateFilterColumn;
      if(deliveryDateFilterDate!=null) {
        settings.view.edge.filter.dateFilter.toDate = new Date();
        settings.view.edge.filter.dateFilter.toDate.year = deliveryDateFilterDate.get(Calendar.YEAR);
        settings.view.edge.filter.dateFilter.toDate.month = deliveryDateFilterDate.get(Calendar.MONTH);
        settings.view.edge.filter.dateFilter.toDate.day = deliveryDateFilterDate.get(Calendar.DAY_OF_MONTH);
      }
      settings.view.edge.filter.dateFilter.showDeliveriesWithoutDate = showDeliveriesWithoutDate;
      
      settings.view.edge.showEdgesInMetanode = showEdgesInMetaNode;
      settings.view.edge.showCrossContaminatedDeliveries = showCrossContaminatedDeliveries;
      settings.view.edge.selectedEdges = selectedEdges;
      settings.view.edge.highlightConditions = createEdgeHighlightings(edgeHighlightConditions);
 
      return this;
    }
    
    public JsonBuilder setGraphView() {
      return this;
    }
    
    private static XYPair createXYPair(double x, double y) {
      XYPair xyPair = new XYPair();
      xyPair.x = x;
      xyPair.y = y;
      return xyPair;
    }
    
    private static Transformation createTransformation(double scaleX, double scaleY, double translationX, double translationY) {
      Transformation transformation = new Transformation();
      transformation.scale = createXYPair(scaleX, scaleY);
      transformation.translation = createXYPair(translationX, translationY);
      return transformation;
    }
    
    private static void initNodeSettings(NodeSettings settings, int minSize, Integer maxSize) {
      settings.minSize = minSize;
      settings.maxSize = maxSize;
    }
    
    private static EdgeSettings createEdgeSettings(int minWidth, Integer maxWidth) {
      EdgeSettings edgeSettings = new EdgeSettings();
      edgeSettings.minWidth = minWidth;
      edgeSettings.maxWidth = maxWidth;
      return edgeSettings;
    }
    
    private static TextSettings createTextSettings(int fontSize, boolean fontBold) {
      TextSettings textSettings = new TextSettings();
      textSettings.fontSize = fontSize;
      textSettings.fontBold = fontBold;
      return textSettings;
    }
    
    public JsonBuilder setGisSettings(double scaleX, double scaleY, double translationX, double translationY,
        int minEdgeThickness, Integer maxEdgeThickness, int fontSize, boolean fontBold, int minNodeSize, Integer maxNodeSize, boolean avoidOverlay, int borderAlpha) {
      
      if(settings.view==null) settings.view = new View();
      settings.view.gis = new View.GisSettings();
      initGisSettings(settings.view.gis, scaleX,  scaleY,  translationX,  translationY,
           minEdgeThickness,  maxEdgeThickness,  fontSize,  fontBold,  minNodeSize,  maxNodeSize,  avoidOverlay,  borderAlpha);
      return this;
    }
    
    private static NodePosition createNodePosition(String id, Point2D p) {
      NodePosition nodePosition = new NodePosition();
      nodePosition.id = id;
      if(p!=null) nodePosition.position = createXYPair(p.getX(),p.getY());
      return nodePosition;
    }
    
    public static NodePosition[] convertNodePositions(Map<String, Point2D> positions) {
      if(positions==null) return null;
      return positions.entrySet().stream().map(entry -> createNodePosition(entry.getKey(), entry.getValue())).collect(Collectors.toList()).toArray(new NodePosition[0]);
    }
    
    public static NodePosition[] convertCollapsedPositions(Map<String, Map<String, Point2D>> positions) {
      List<NodePosition> positionList = new ArrayList<>();
      positions.values().forEach(p -> positionList.addAll(Arrays.asList(convertNodePositions(p))));            
      return positionList.toArray(new NodePosition[0]);
    }
    
    public JsonBuilder setGraphSettings(double scaleX, double scaleY, double translationX, double translationY,
        int minEdgeThickness, Integer maxEdgeThickness, int fontSize, boolean fontBold, int minNodeSize, Integer maxNodeSize, Map<String, Point2D> nodePositions) {
      
      if(settings.view==null) settings.view = new View();
      settings.view.graph = new View.GraphSettings();
      
      initGraphSettings(settings.view.graph, scaleX,  scaleY,  translationX,  translationY,
          minEdgeThickness,  maxEdgeThickness,  fontSize,  fontBold,  minNodeSize,  maxNodeSize, nodePositions);
      
      return this;
    }
    
    public JsonBuilder setCollapsedPositionsInGraphSettings(Map<String, Map<String, Point2D>> collapsedPositions) {
      settings.view.graph.node.collapsedPositions = convertCollapsedPositions(collapsedPositions);
      return this;
    }
    
    public JsonBuilder setExplosionCount(int n) {
      settings.view.explosions = new ExplosionSettings[n];
      return this;
    }
    
    public JsonBuilder setExplosionId(int index, String id) {
      settings.view.explosions[index] = new ExplosionSettings();
      settings.view.explosions[index].id = id;
      return this;
    }
   
    private static void initGraphSettings(GraphSettings graphSettings, double scaleX, double scaleY, double translationX, double translationY,
        int minEdgeThickness, Integer maxEdgeThickness, int fontSize, boolean fontBold, int minNodeSize, Integer maxNodeSize, Map<String, Point2D> nodePositions) {
      if(!Arrays.asList(scaleX, scaleY, translationX, translationY).stream().anyMatch((v) -> Double.isNaN(v))) {
        graphSettings.transformation = createTransformation(scaleX, scaleY, translationX, translationX);
      }
     
      graphSettings.node = new GraphSettings.NodeSettings();
      initNodeSettings(graphSettings.node, minNodeSize, maxNodeSize);
      graphSettings.edge = createEdgeSettings(minEdgeThickness,maxEdgeThickness);
      graphSettings.text = createTextSettings(fontSize, fontBold);
      graphSettings.node.positions = convertNodePositions(nodePositions);
    }
    
    private static void initGisSettings(GisSettings gisSettings, double scaleX, double scaleY, double translationX, double translationY,
        int minEdgeThickness, Integer maxEdgeThickness, int fontSize, boolean fontBold, int minNodeSize, Integer maxNodeSize, boolean avoidOverlay, int borderAlpha) {
      
      if(!Arrays.asList(scaleX, scaleY, translationX, translationY).stream().anyMatch((v) -> Double.isNaN(v))) {
        gisSettings.transformation = createTransformation(scaleX, scaleY, translationX, translationX);
      }
      gisSettings.borderAlpha = borderAlpha;
      gisSettings.node = new GisSettings.NodeSettings();
      initNodeSettings(gisSettings.node, minNodeSize, maxNodeSize);
      gisSettings.node.avoidOverlay = avoidOverlay;
      gisSettings.edge = createEdgeSettings(minEdgeThickness,maxEdgeThickness);
      gisSettings.text = createTextSettings(fontSize, fontBold);
    }
    
    
    public JsonBuilder setExplosionGraphSettings(int index, double scaleX, double scaleY, double translationX, double translationY,
        int minEdgeThickness, Integer maxEdgeThickness, int fontSize, boolean fontBold, int minNodeSize, Integer maxNodeSize, Map<String, Point2D> nodePositions) {
      
      settings.view.explosions[index].graph = new ExplosionGraphSettings();
      initGraphSettings(settings.view.explosions[index].graph, scaleX,  scaleY,  translationX,  translationY,
         minEdgeThickness,  maxEdgeThickness,  fontSize,  fontBold,  minNodeSize,  maxNodeSize, nodePositions);
      return this;
    }
    
    public JsonBuilder setExplosionGraphBoundary(int index, double[] boundaryParams) {
      settings.view.explosions[index].graph.boundaryParams = boundaryParams;
      return this;
    }
    
    public JsonBuilder setExplosionGisSettings(int index, double scaleX, double scaleY, double translationX, double translationY,
        int minEdgeThickness, Integer maxEdgeThickness, int fontSize, boolean fontBold, int minNodeSize, Integer maxNodeSize, boolean avoidOverlay, int borderAlpha) {
      
      settings.view.explosions[index].gis = new GisSettings();
      initGisSettings(settings.view.explosions[index].gis, scaleX,  scaleY,  translationX,  translationY,
         minEdgeThickness,  maxEdgeThickness,  fontSize,  fontBold,  minNodeSize,  maxNodeSize, avoidOverlay, borderAlpha);
      
      return this;
    }
    
    public JsonValue build() throws JsonProcessingException {
      return de.bund.bfr.knime.openkrise.util.json.JsonConverter.convertToJsonValue(this.json);
    }
  }
  
 
  private static NamedShape getShape(View.HighlightCondition source) throws InvalidSettingsException {
    if(source instanceof View.GlobalNodeSettings.NodeHighlightCondition) { 
      String shape = ((View.GlobalNodeSettings.NodeHighlightCondition) source).shape;
      if (shape!=null) {
        try {
          return NamedShape.valueOf(shape);
        } catch(IllegalArgumentException e) {
          throw(new InvalidSettingsException("The shape '" + shape + "' of a highlighting condition in JSON data is unkown."));
        }
      }
    }
    return null;
  }
  
  private static LogicalHighlightCondition.Type getTypeOfLogicalCondition(String type) throws InvalidSettingsException {
    if(type==null) throw(new InvalidSettingsException("The type of a logical condition in JSON data is missing."));
    try {
      return LogicalHighlightCondition.Type.valueOf(type);
    } catch(IllegalArgumentException e) {
      throw(new InvalidSettingsException("The logical condition type '" + type +"' in JSON data is unkown."));
    }
  }
  
  private static AndOrHighlightCondition createLogicalCondition(View.HighlightCondition source) throws InvalidSettingsException {
    List<List<LogicalHighlightCondition>> conditionList = new ArrayList<>();
    List<LogicalHighlightCondition> andList = null; //new ArrayList<>();
    for(View.LogicalCondition[] andConditions : source.logicalConditions) {
      andList = new ArrayList<>();
      for(View.LogicalCondition andCondition : andConditions) 
        andList.add(new LogicalHighlightCondition(andCondition.propertyName, getTypeOfLogicalCondition(andCondition.operationType),andCondition.value));
      conditionList.add(andList);
    }
    return new AndOrHighlightCondition(conditionList, source.name, source.showInLegend, getColor(source.color), source.invisible, source.adjustThickness,
        source.labelProperty, getShape(source));
  }
  
  private static Color getColor(int[] rgb) {
    if(rgb==null) return null;
    return new Color(rgb[0], rgb[1], rgb[2]);
  }
  
  private static ValueHighlightCondition.Type getTypeOfValueCondition(String type) throws InvalidSettingsException {
    if(type==null) throw(new InvalidSettingsException("The type of a value condition in JSON data is missing."));
    try {
      return ValueHighlightCondition.Type.valueOf(type);
    } catch(IllegalArgumentException e) {
      throw(new InvalidSettingsException("The value condition type '" + type +"' in JSON data is unkown."));
    }
  }
  
  protected static LabelPosition getLabelPosition(String labelPosition) throws InvalidSettingsException {
	  
    if(labelPosition==null) throw(new InvalidSettingsException("The labelPosition in JSON data is missing."));
    
    try {
    	
      return LabelPosition.valueOf(labelPosition);
      
    } catch(IllegalArgumentException e) {
    	
      throw(new InvalidSettingsException("The labelPosition '" + labelPosition +"' in JSON data is unkown."));
      
    }
  }
  
  protected static GisType getGisType(String gisType) throws InvalidSettingsException {
	  
    if(gisType==null) throw(new InvalidSettingsException("The gisType in JSON data is missing."));
    
    try {
    	
      return GisType.valueOf(gisType);
      
    } catch(IllegalArgumentException e) {
    	
      throw(new InvalidSettingsException("The gisType '" + gisType +"' in JSON data is unkown."));
      
    }
  }
  
  private static ValueHighlightCondition createValueCondition(View.HighlightCondition source) throws InvalidSettingsException {
    return new ValueHighlightCondition(source.valueCondition.propertyName,
        getTypeOfValueCondition(source.valueCondition.valueType), source.valueCondition.useZeroAsMinimum, source.name,
        source.showInLegend, getColor(source.color), source.invisible, source.adjustThickness, source.labelProperty, getShape(source));
  }
  
  private static HighlightCondition createLogicalValueHighlightCondition(View.HighlightCondition source) throws InvalidSettingsException {
    return new LogicalValueHighlightCondition(
        createValueCondition(source),
        createLogicalCondition(source));
  }
  
  private static Point2D createPoint2D(XYPair position) {
    if(position==null) return null;
    if(position.x==null || position.y ==null) return null;
    return new Point2D.Double(position.x, position.y);
  }
  
  protected static Map<String, Point2D> getPositions(NodePosition[] nodePositions) {
    Map<String, Point2D> result = new LinkedHashMap<>();
    for(NodePosition nodePosition: nodePositions) result.put(nodePosition.id, createPoint2D(nodePosition.position));
    return result;
  }
  
  protected static <T extends View.HighlightCondition> void setHighlighting(HighlightConditionList target, T[] sources) throws InvalidSettingsException {
    target.getConditions().clear();
    for(T source: sources) 
      target.getConditions().add(source.valueCondition==null ? createLogicalCondition(source) : (source.logicalConditions==null? createValueCondition(source) : createLogicalValueHighlightCondition(source))); 
  }
}
