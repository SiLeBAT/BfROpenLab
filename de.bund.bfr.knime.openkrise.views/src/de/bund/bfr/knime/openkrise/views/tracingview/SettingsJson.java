package de.bund.bfr.knime.openkrise.views.tracingview;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition;

public class SettingsJson {
  private final String FORMAT_VERSION = "1.0.0";
  public String version = FORMAT_VERSION;
  public FCLData data; 
  public TVSettings settings; 
  
  public static class FCLData{
    public Stations stations; 
    public Deliveries deliveries;
    public DeliveriesToDeliveries deliveriesToDeliveries;
    
    public static class Stations{

    }
    public static class Deliveries{

    }
    public static class DeliveriesToDeliveries{

    }
  }
  
  public static class TVSettings{
    public MetaNode[] metaNodes;
    public Tracing tracing;
    public View view;
  }
  
  public static class MetaNode{
    public String id;
    public String type;
    public String[] members;
    public MetaNode() {};
    public MetaNode(String id, String type, String[] members) {
      this.id = id;
      this.type = type;
      this.members = members;
    }
  }
  
  public static class Tracing{
    public boolean enforceTemporalOrder;
    public Node[] nodes;
    public Delivery[] deliveries;
        
    private static class Item {
      public String id;
      public Double weight;
      public Boolean crossContamination;
      public Boolean killContamination;
      public Boolean observed;
      
      public Item() {};
      public Item(String id, Double weight, Boolean crossContamination, Boolean killContamination, Boolean observed) {
        this.id = id;
        this.weight = weight;
        this.crossContamination = crossContamination;
        this.killContamination = killContamination;
        this.observed = observed;
      }
    }
    
    public static class Node extends Item{
      public Node() {super(); };
      public Node(String id, Double weight, Boolean crossContamination, Boolean killContamination,
        Boolean observed) { 
        super(id,weight,crossContamination,killContamination,observed); 
      }
      
//      public ComputedNodeProps computedProps;
    }
    
    public static class Delivery extends Item {
      public Delivery() {super(); };
      public Delivery(String id, Double weight, Boolean crossContamination, Boolean killContamination,
        Boolean observed) { 
        super(id,weight,crossContamination,killContamination,observed); 
      }
      
//      public ComputedEdgeProps computedProps;
    }

//    public static class ComputedProps{
//      public double score;
//      public double normalizedScore;
//      public double positiveScore;
//      public double negativeScore;
//      public boolean isOnBackwardTrace;
//      public boolean isOnForwardTrace;
//    }
    
//    public static class ComputedNodeProps extends ComputedProps{
//      public double maxLotScore;
//    }
//    
//    public static class ComputedEdgeProps extends ComputedProps{
//      public double lotScore;
//    }
    
    public Tracing(boolean enforceTemporalOrder, 
        Map<String,Double> edgeWeights, Map<String, Boolean> edgeCrossContaminations, Map<String, Boolean> edgeKillContaminations, Map<String, Boolean> observedEdges, 
        Map<String,Double> nodeWeights, Map<String, Boolean> nodeCrossContaminations, Map<String, Boolean> nodeKillContaminations, Map<String, Boolean> observedNodes) {

      this.enforceTemporalOrder = enforceTemporalOrder;
      List<Delivery> deliveryTracingList = new ArrayList<>();

      for(String id: edgeCrossContaminations.keySet()) 
        deliveryTracingList.add(
            new Delivery(id, edgeWeights.get(id), edgeCrossContaminations.get(id), edgeKillContaminations.get(id), observedEdges.get(id)));

      this.deliveries = deliveryTracingList.toArray(new Delivery[0]);

      List<Node> nodeTracingList = new ArrayList<>();

      for(String id: nodeCrossContaminations.keySet()) 
        nodeTracingList.add(
            new Node(id, nodeWeights.get(id), nodeCrossContaminations.get(id), nodeKillContaminations.get(id), observedNodes.get(id)));

      this.nodes = nodeTracingList.toArray(new Node[0]);
    }
  }
  
  public static class Date{
    public Integer year;
    public Integer month;
    public Integer day;
    
    public Date() {}
    public Date(Integer year, Integer month, Integer day) {
      this.year = year;
      this.month = month;
      this.day = day;
    }
    public Date(Calendar date) {
      this(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
    }
  }
  
  public static class View{
    public boolean showGis;
    public boolean showLegend;
    public boolean exportAsSvg;
    public String label;
    public GlobalEdgeSettings edge;
    public GlobalNodeSettings node;
    public GraphSettings graph;
    public GisSettings gis;  
    public ExplosionSettings[] explosions;  
    
    public static class XYPair {

      public Double x;
      public Double y;

      public XYPair() {}

      public XYPair(double x, double y) {
        this.x = Double.isNaN(x)?null:x;
        this.y = Double.isNaN(y)?null:y;
      }

      private void setX(double x) { this.x = Double.isNaN(x)?null:x; }
      private void setY(double y) { this.y = Double.isNaN(y)?null:y; }
      
      public XYPair(Point2D p) {
        if(p!=null) {
          this.setX(p.getX());
          this.setY(p.getY());
        }
      }
    }

    public static class NodePosition {
      public String id;
      public XYPair position;
      public NodePosition() {}

      protected NodePosition(String id, Point2D point) {
        this.id = id;
        if(point!=null)  this.position = new XYPair(point.getX(), point.getY());
      }
      
      public static NodePosition[] convertPositions(Map<String, Point2D> positions) {
        if(positions==null) return null;
        return positions.entrySet().stream().map(entry -> new NodePosition(entry.getKey(), entry.getValue())).collect(Collectors.toList()).toArray(new NodePosition[0]);
      }
      
      public static NodePosition[] convertPositions(Collection<Map<String, Point2D>> positions) {
        List<NodePosition> positionList = new ArrayList<>();
        positions.forEach(p -> positionList.addAll(Arrays.asList(convertPositions(p))));            
        return positionList.toArray(new NodePosition[0]);
      }
    }

    public static class Transformation {
      public XYPair scale = new XYPair();
      public XYPair translation = new XYPair();

      public Transformation() {}
      public Transformation(double scaleX, double scaleY, double translationX, double translationY) {
        this.scale = new XYPair(scaleX, scaleY);
        this.translation = new XYPair(translationX,translationY);
      }
    }

    public static class ValueCondition {
      public String propertyName;
      public String valueType;
      public Boolean useZeroAsMinimum;

      public ValueCondition() {};
      public ValueCondition(String propertyName, String valueType, Boolean useZeroAsMinimum) {
        this.propertyName = propertyName;
        this.valueType = valueType;
        this.useZeroAsMinimum = useZeroAsMinimum;
      }
      
      private static SettingsJson.View.ValueCondition getValueCondition(de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition hLCondition) {
        
        ValueHighlightCondition valueHighlightCondition = null; 
        if(hLCondition instanceof LogicalValueHighlightCondition) {//AndOrHighlightCondition) {
          valueHighlightCondition = ((LogicalValueHighlightCondition) hLCondition).getValueCondition();
        } else if(hLCondition instanceof ValueHighlightCondition) {
          valueHighlightCondition = (ValueHighlightCondition) hLCondition;
        }
        if(valueHighlightCondition!=null) return new SettingsJson.View.ValueCondition(valueHighlightCondition.getProperty(), valueHighlightCondition.getType().toString(), valueHighlightCondition.isZeroAsMinimum());
        return null;
      }
    }

    public static class LogicalCondition {
      public String propertyName;
      public String operationType;
      public String value;

      public LogicalCondition() {};
      public LogicalCondition(String propertyName, String operationType, String value) {
        this.propertyName = propertyName;
        this.operationType = operationType;
        this.value = value;
      }
      
      private static SettingsJson.View.LogicalCondition getLogicalCondition(LogicalHighlightCondition logicalCondition) {
        return new SettingsJson.View.LogicalCondition(logicalCondition.getProperty(), logicalCondition.getType().toString(), logicalCondition.getValue());
      }
      
      private static SettingsJson.View.LogicalCondition[][] getLogicalConditions(de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition hLCondition) {
        
        AndOrHighlightCondition andOrHighlightCondition = null; 
        if(hLCondition instanceof LogicalValueHighlightCondition) {//AndOrHighlightCondition) {
          andOrHighlightCondition = ((LogicalValueHighlightCondition) hLCondition).getLogicalCondition();
        } else if(hLCondition instanceof AndOrHighlightCondition) {
          andOrHighlightCondition = (AndOrHighlightCondition) hLCondition;
        }
        if(andOrHighlightCondition!=null) {
          int nOr = andOrHighlightCondition.getConditions().size();
          SettingsJson.View.LogicalCondition[][] result = new SettingsJson.View.LogicalCondition[nOr][];
          for(int iOr=0; iOr<nOr; ++iOr) {
            int nAnd = andOrHighlightCondition.getConditions().get(iOr).size();
            result[iOr] = new SettingsJson.View.LogicalCondition[nAnd];
            for(int iAnd=0; iAnd<nAnd; ++iAnd) result[iOr][iAnd] = getLogicalCondition(andOrHighlightCondition.getConditions().get(iOr).get(iAnd));
          }
          return result;
        }
        return null;
      }
    }

    private static class HighlightCondition {
      public String name;
      public Boolean showInLegend;
      public int[] color;
      public Boolean invisible;
      public Boolean adjustThickness;
      public String label;
      public ValueCondition valueCondition;
      public LogicalCondition[][] logicalConditions;
      
      private HighlightCondition() {}
      protected HighlightCondition(String name, Boolean showInLegend, int[] color, Boolean invisible, Boolean adjustThickness, String label, ValueCondition valueCondition, LogicalCondition[][] logicalConditions) {
        this.name = name;
        this.showInLegend = showInLegend;
        this.color = color;
        this.invisible = invisible;
        this.adjustThickness = adjustThickness;
        this.label = label;
        this.valueCondition = valueCondition;
        this.logicalConditions = logicalConditions;
      }
    }

    public static class GlobalEdgeSettings{
      public boolean joinEdges;
      public boolean showEdgesInMetanode;
      public boolean hideArrowHead;
      public boolean arrowInMiddle;
      public String[] selectedEdges;
      public String[] invisibleEdges;
      public EdgeHighlightCondition[] highlightConditions;
      public boolean showCrossContaminatedDeliveries;
      public Filter filter; 
      //public DeliveryFilter deliveryFilter;
      
      public static class Filter {
        public String[] invisibleEdges;
        public DeliveryToDateFilter dateFilter;
            
        public static class DeliveryToDateFilter {
          public String dateId;
          public Date toDate;
          public Boolean showDeliveriesWithoutDate;
        }
      }
      
      public void setDeliveryFilter(String dateId, Calendar date, boolean showDeliveriesWithoutDate) {
        this.filter = new Filter();
        this.filter.dateFilter = new Filter.DeliveryToDateFilter();
        this.filter.dateFilter.dateId = dateId;
        this.filter.dateFilter.toDate = date==null?null:new Date(date);
        this.filter.dateFilter.showDeliveriesWithoutDate = showDeliveriesWithoutDate;
      }
      
      public static class EdgeHighlightCondition extends HighlightCondition {
        public EdgeHighlightCondition() {};
        public EdgeHighlightCondition(String name, Boolean showInLegend, int[] color, Boolean invisible, Boolean adjustThickness, String label, ValueCondition valueCondition, LogicalCondition[][] logicalConditions, String linePattern) {
          super(name, showInLegend, color, invisible, adjustThickness, label, valueCondition, logicalConditions);
          this.linePattern = linePattern;
        };
        public String linePattern;
      }
      
      public void setHighlighting(List<de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition> highlightConditions) {
        
        List<EdgeHighlightCondition> edgeHighlightConditionList = new ArrayList<>();
        
        for(de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition hLCondition: highlightConditions) {
          // super(name, showInLegend, color, invisible, adjustThickness, label, valueCondition, logicalConditions);
          Color color = hLCondition.getColor();
          
          edgeHighlightConditionList.add(
              new EdgeHighlightCondition(
                  hLCondition.getName(), hLCondition.isShowInLegend(), 
                  (int []) (color==null?null:new int[] {color.getRed(), color.getGreen(), color.getBlue()}),
                  hLCondition.isInvisible(), hLCondition.isUseThickness(), hLCondition.getLabelProperty(), 
                  ValueCondition.getValueCondition(hLCondition), LogicalCondition.getLogicalConditions(hLCondition),
                  null));
        }
        
        this.highlightConditions = edgeHighlightConditionList.toArray(new EdgeHighlightCondition[0]);
      }
      
    }
    

    public static class GlobalNodeSettings{
      public boolean skipEdgelessNodes;
      public String labelPosition;
      public String[] selectedNodes;
      public String[] invisibleNodes;
      public NodeHighlightCondition[] highlightConditions;  
      
      public static class NodeHighlightCondition extends HighlightCondition{
        public NodeHighlightCondition() {};
        public NodeHighlightCondition(String name, Boolean showInLegend, int[] color, Boolean invisible, Boolean adjustThickness, String label, ValueCondition valueCondition, LogicalCondition[][] logicalConditions, String shape) {
          super(name, showInLegend, color, invisible, adjustThickness, label, valueCondition, logicalConditions);
          this.shape = shape;
        };
        public String shape;
      }
      
      public void setHighlighting(List<de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition> highlightConditions) {

        List<NodeHighlightCondition> nodeHighlightConditionList = new ArrayList<>();

        for(de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition hLCondition: highlightConditions) {
          // super(name, showInLegend, color, invisible, adjustThickness, label, valueCondition, logicalConditions);
          Color color = hLCondition.getColor();

          nodeHighlightConditionList.add(
              new NodeHighlightCondition(
                  hLCondition.getName(), hLCondition.isShowInLegend(), 
                  (int []) (color==null?null:new int[] {color.getRed(), color.getGreen(), color.getBlue()}),
                  hLCondition.isInvisible(), hLCondition.isUseThickness(), hLCondition.getLabelProperty(), 
                  ValueCondition.getValueCondition(hLCondition), LogicalCondition.getLogicalConditions(hLCondition),
                  hLCondition.getShape()==null?null:hLCondition.getShape().toString()));
        }

        this.highlightConditions = nodeHighlightConditionList.toArray(new NodeHighlightCondition[0]);
      }
    }
   
    private static class SharedViewSettings{
      public Transformation transformation;
      public NodeSettings node;
      public EdgeSettings edge;
      public TextSettings text;
      
      public static class NodeSettings{
        public int minSize;
        public Integer maxSize;
        
        public NodeSettings() {};
        public NodeSettings(int minSize, Integer maxSize) {
          this.minSize = minSize;
          this.maxSize = maxSize;
        }
      }
      
      public static class EdgeSettings{
        public int minWidth;
        public Integer maxWidth;
        
        public EdgeSettings() {};
        public EdgeSettings(int minWidth, Integer maxWidth) {
          this.minWidth = minWidth;
          this.maxWidth = maxWidth;
        }
      }
      
      public static class TextSettings{
        public int fontSize;
        public boolean fontBold;
        
        public TextSettings() {}
        public TextSettings(int fontSize, boolean fontBold) {
          this.fontSize = fontSize;
          this.fontBold = fontBold;
        }
      }

      public void setTransformation(double scaleX, double scaleY, double translationX, double translationY) {
        if(!Arrays.asList(scaleX, scaleY, translationX, translationY).stream().anyMatch((v) -> Double.isNaN(v))) {
          this.transformation = new Transformation(scaleX, scaleY, translationX, translationX);
        }
      }
      
      public void setEdgeSettings(int minWidth, Integer maxWidth) {
        this.edge = new EdgeSettings(minWidth, maxWidth);
      }
      
      public void setNodeSettings(int minSize, Integer maxSize) {
        this.node = new NodeSettings(minSize, maxSize);
      }
      
      public void setTextSettings(int fontSize, boolean fontBold) {
        this.text = new TextSettings(fontSize, fontBold);
      }
    }
    
    public static class GraphSettings extends SharedViewSettings{
      public static class NodeSettings extends SharedViewSettings.NodeSettings {
        public NodePosition[] positions;
        public NodePosition[] collapsedPositions;
        
        public NodeSettings() {super(); }
        public NodeSettings(int minSize, Integer maxSize) {super(minSize, maxSize); }
        
        public void setCollapsedPositions(Collection<Map<String, Point2D>> collapsedPositions) {
          this.collapsedPositions = NodePosition.convertPositions(collapsedPositions);
        }
      }
      
      public NodeSettings node;
      
      public void setNodeSettings(int minSize, Integer maxSize, Map<String, Point2D> positions) {
        this.node = new NodeSettings(minSize, maxSize);
        this.node.positions = NodePosition.convertPositions(positions);
      }
     
    }
    
    public static class GisSettings extends SharedViewSettings {
      public boolean avoidOverlay;
      public int borderAlpha;
      public String type;
    }
    
    public static class ExplosionSettings{
     public String id;
     public ExplosionGraphSettings graphSettings;
     public GisSettings gisSettings;
     
     public static class ExplosionGraphSettings extends GraphSettings{
       public double[] boundaryParams;
     }
    }
  }
  
}
