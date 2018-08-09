package de.bund.bfr.knime.openkrise.util.json;

public class JsonFormat {

  private static String CURRENT_VERSION = "1.0.0";

  public String version = CURRENT_VERSION;

  public Data data;

  public Tracing tracing;
  
  public TracingViewSettings settings;

  public static class Tracing{
    private static String CURRENT_VERSION = "1.0.0";

    public String version = CURRENT_VERSION;
    
    public Boolean enforceTemporalOrder;
    public TraceableUnit[] nodes;
    public TraceableUnit[] deliveries;

    public static class TraceableUnit {
      public String id;
      public Double weight;
      public Boolean crossContamination;
      public Boolean killContamination;
      public Boolean observed;
    }
  }

  public static class TracingViewSettings {
    private static String CURRENT_VERSION = "1.0.0";

    public String version = CURRENT_VERSION;

    public MetaNode[] metaNodes;

    public View view;

    public static class MetaNode{
      public String id;
      public String type;
      public String[] members;
    }

    public static class View{
      public boolean showGis;
      public String gisType;
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
      }

      public static class NodePosition {
        public String id;
        public XYPair position;
      }

      public static class Transformation {
        public XYPair scale; 
        public XYPair translation; 
      }

      public static class ValueCondition {
        public String propertyName;
        public String valueType;
        public Boolean useZeroAsMinimum;
      }

      public static class LogicalCondition {
        public String propertyName;
        public String operationType;
        public String value;
      }

      public static class HighlightCondition {
        public String name;
        public Boolean showInLegend;
        public int[] color;
        public Boolean invisible;
        public Boolean adjustThickness;
        public String label;
        public ValueCondition valueCondition;
        public LogicalCondition[][] logicalConditions;
      }

      public static class GlobalEdgeSettings{
        public boolean joinEdges;
        public boolean showEdgesInMetanode;
        public boolean hideArrowHead;
        public boolean arrowHeadInMiddle;
        public String[] selectedEdges;
        public String[] invisibleEdges;
        public EdgeHighlightCondition[] highlightConditions;
        public boolean showCrossContaminatedDeliveries;
        public Filter filter; 

        public static class Filter {
          public String[] invisibleEdges;
          public DeliveryToDateFilter dateFilter;

          public static class DeliveryToDateFilter {
            public String dateId;
            public Date toDate;
            public Boolean showDeliveriesWithoutDate;
          }
        }

        public static class EdgeHighlightCondition extends HighlightCondition {
          public String linePattern;
        }
      }


      public static class GlobalNodeSettings{
        public boolean skipEdgelessNodes;
        public String labelPosition;
        public String[] selectedNodes;
        public String[] invisibleNodes;
        public NodeHighlightCondition[] highlightConditions;  

        public static class NodeHighlightCondition extends HighlightCondition{
          public String shape;
        }
      }

      public static class SharedViewSettings{
        public Transformation transformation;
        public NodeSettings node;
        public EdgeSettings edge;
        public TextSettings text;

        public static class NodeSettings{
          public int minSize;
          public Integer maxSize;
        }

        public static class EdgeSettings{
          public int minWidth;
          public Integer maxWidth;
        }

        public static class TextSettings{
          public int fontSize;
          public boolean fontBold;
        }
      }

      public static class GraphSettings extends SharedViewSettings{
        public static class NodeSettings extends SharedViewSettings.NodeSettings {
          public NodePosition[] positions;
          public NodePosition[] collapsedPositions;
        }

        public NodeSettings node;
      }

      public static class GisSettings extends SharedViewSettings {
        public int borderAlpha;
        public NodeSettings node;

        public static class NodeSettings extends SharedViewSettings.NodeSettings {
          public boolean avoidOverlay;
        }
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


  public static class Date{
    public Integer year;
    public Integer month;
    public Integer day;
  }

  public static class Data {
    private static final String CURRENT_VERSION = "1.0.0";

    public String version = CURRENT_VERSION;

    public ColumnSpec[] stationColumns;
    public Property[][] stations;
    public ColumnSpec[] deliveryColumns;
    public Property[][] deliveries;
    public DeliveryRelation[] deliveryRelations;

    public static class ColumnSpec {
      public String id;
      public String type;
    }

    //    public static class Station {
    //      public String id;
    //      public Property[] properties;
    //    }
    //    
    //    public static class Delivery {
    //      public String ID;
    //      public Property[] properties;
    //    }

    public static class Property {
      public String id;
      public Object value;
    }
    
    public static class DeliveryRelation {
      public String fromId;
      public String toId;
    }

  }
}
