/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.openkrise.util.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonFormat {

  public String version;

  public Data data;

  public Tracing tracing;
  
  public TracingViewSettings settings;

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Tracing{
    
    public String version;
    
    public Boolean enforceTemporalOrder;
    public TraceableUnit[] nodes;
    public TraceableUnit[] deliveries;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TraceableUnit {
      public String id;
      public Double weight;
      public Boolean crossContamination;
      public Boolean killContamination;
      public Boolean observed;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TracingViewSettings {
    
    public String version; 

    public MetaNode[] metaNodes;

    public View view;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetaNode{
      public String id;
      public String type;
      public String[] members;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class View{
      public Boolean showGis;
      public String gisType;
      public Boolean showLegend;
      public Boolean exportAsSvg;
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

      @JsonIgnoreProperties(ignoreUnknown = true)
      public static class NodePosition {
        public String id;
        public XYPair position;
      }

      @JsonIgnoreProperties(ignoreUnknown = true)
      public static class Transformation {
        public XYPair scale; 
        public XYPair translation; 
      }

      @JsonIgnoreProperties(ignoreUnknown = true)
      public static class ValueCondition {
        public String propertyName;
        public String valueType;
        public Boolean useZeroAsMinimum;
      }

      @JsonIgnoreProperties(ignoreUnknown = true)
      public static class LogicalCondition {
        public String propertyName;
        public String operationType;
        public String value;
      }

      @JsonIgnoreProperties(ignoreUnknown = true)
      public static class HighlightCondition {
        public String name;
        public Boolean showInLegend;
        public int[] color;
        public Boolean invisible;
        public Boolean adjustThickness;
        public String labelProperty;
        public ValueCondition valueCondition;
        public LogicalCondition[][] logicalConditions;
      }

      @JsonIgnoreProperties(ignoreUnknown = true)
      public static class GlobalEdgeSettings{
        public Boolean joinEdges;
        public Boolean showEdgesInMetanode;
        public Boolean hideArrowHead;
        public Boolean arrowHeadInMiddle;
        public String[] selectedEdges;
        public String[] invisibleEdges;
        public EdgeHighlightCondition[] highlightConditions;
        public Boolean showCrossContaminatedDeliveries;
        public Filter filter; 

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Filter {
          public String[] invisibleEdges;
          public DeliveryToDateFilter dateFilter;

          @JsonIgnoreProperties(ignoreUnknown = true)
          public static class DeliveryToDateFilter {
            public String dateId;
            public Date toDate;
            public Boolean showDeliveriesWithoutDate;
          }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class EdgeHighlightCondition extends HighlightCondition {
          public String linePattern;
        }
      }

      @JsonIgnoreProperties(ignoreUnknown = true)
      public static class GlobalNodeSettings{
        public Boolean skipEdgelessNodes;
        public String labelPosition;
        public String[] selectedNodes;
        public String[] invisibleNodes;
        public NodeHighlightCondition[] highlightConditions;  

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class NodeHighlightCondition extends HighlightCondition{
          public String shape;
        }
      }

      @JsonIgnoreProperties(ignoreUnknown = true)
      public static class SharedViewSettings{
        public Transformation transformation;
        public NodeSettings node;
        public EdgeSettings edge;
        public TextSettings text;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class NodeSettings{
          public int minSize;
          public Integer maxSize;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class EdgeSettings{
          public int minWidth;
          public Integer maxWidth;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class TextSettings{
          public int fontSize;
          public boolean fontBold;
        }
      }

      @JsonIgnoreProperties(ignoreUnknown = true)
      public static class GraphSettings extends SharedViewSettings{
    	@JsonIgnoreProperties(ignoreUnknown = true)
        public static class NodeSettings extends SharedViewSettings.NodeSettings {
          public NodePosition[] positions;
          public NodePosition[] collapsedPositions;
        }

        public NodeSettings node;
      }

      @JsonIgnoreProperties(ignoreUnknown = true)
      public static class GisSettings extends SharedViewSettings {
        public int borderAlpha;
        public NodeSettings node;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class NodeSettings extends SharedViewSettings.NodeSettings {
          public boolean avoidOverlay;
        }
      }

      @JsonIgnoreProperties(ignoreUnknown = true)
      public static class ExplosionSettings{
        public String id;
        public ExplosionGraphSettings graph;
        public GisSettings gis;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ExplosionGraphSettings extends GraphSettings{
          public double[] boundaryParams;
        }
      }
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Date{
    public Integer year;
    public Integer month;
    public Integer day;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Data {

    public String version;

    public Table stations;
    public Table deliveries;
    public Table deliveryRelations;
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeliveryRelation {
      public String fromId;
      public String toId;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Table {
      public ColumnProperty[] columnProperties;
      public ItemProperty[][] data;
      
      @JsonIgnoreProperties(ignoreUnknown = true)
      public static class ItemProperty {
        public String id;
        public Object value;
      }
      
      @JsonIgnoreProperties(ignoreUnknown = true)
      public static class ColumnProperty {
        public String id;
        public String type;
      }

    }
  }
}
