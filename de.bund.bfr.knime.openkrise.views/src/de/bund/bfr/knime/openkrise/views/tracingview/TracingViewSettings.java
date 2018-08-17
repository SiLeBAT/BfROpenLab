/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import javax.json.JsonValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Ordering;
import de.bund.bfr.jung.LabelPosition;
import de.bund.bfr.knime.NodeSettings;
import de.bund.bfr.knime.XmlConverter;
import de.bund.bfr.knime.gis.BackwardUtils;
import de.bund.bfr.knime.gis.GisType;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.util.ArrowHeadType;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.Tracing;
import de.bund.bfr.knime.openkrise.views.Activator;
import de.bund.bfr.knime.openkrise.views.canvas.ITracingCanvas;

public class TracingViewSettings extends NodeSettings {
	
	//private static Logger logger =  Logger.getLogger("de.bund.bfr");

	protected static final XmlConverter SERIALIZER = new XmlConverter(Activator.class.getClassLoader());

	private static final String CFG_SHOW_GIS = "ShowGis";
	private static final String CFG_GIS_TYPE = "GisType";
	private static final String CFG_EXPORT_AS_SVG = "ExportAsSvg";
	private static final String CFG_SKIP_EDGELESS_NODES = "SkipEdgelessNodes";
	private static final String CFG_SHOW_EDGES_IN_META_NODE = "ShowEdgesInMetaNode";
	private static final String CFG_JOIN_EDGES = "JoinEdges";
	private static final String CFG_HIDE_ARROW_HEAD = "HideArrowHead";
	private static final String CFG_ARROW_HEAD_IN_MIDDLE = "ArrowInMiddle";
	private static final String CFG_NODE_LABEL_POSITION = "NodeLabelPosition";
	private static final String CFG_SHOW_LEGEND = "GraphShowLegend";
	private static final String CFG_SELECTED_NODES = "GraphSelectedNodes";
	private static final String CFG_SELECTED_EDGES = "GraphSelectedEdges";
	private static final String CFG_CANVAS_SIZE = "GraphCanvasSize";
	private static final String CFG_NODE_HIGHLIGHT_CONDITIONS = "GraphNodeHighlightConditions";
	private static final String CFG_EDGE_HIGHLIGHT_CONDITIONS = "GraphEdgeHighlightConditions";
	private static final String CFG_COLLAPSED_NODES = "CollapsedNodes";
	private static final String CFG_LABEL = "Label";

	private static final String CFG_NODE_WEIGHTS = "CaseWeights";
	private static final String CFG_EDGE_WEIGHTS = "EdgeWeights";
	private static final String CFG_NODE_CROSS_CONTAMINATIONS = "CrossContaminations";
	private static final String CFG_EDGE_CROSS_CONTAMINATIONS = "EdgeCrossContaminations";
	private static final String CFG_NODE_KILL_CONTAMINATIONS = "NodeKillContaminations";
	private static final String CFG_EDGE_KILL_CONTAMINATIONS = "EdgeKillContaminations";
	private static final String CFG_OBSERVED_NODES = "Filter";
	private static final String CFG_OBSERVED_EDGES = "EdgeFilter";
	private static final String CFG_ENFORCE_TEMPORAL_ORDER = "EnforceTemporalOrder";
	private static final String CFG_SHOW_FORWARD = "ShowConnected";
	private static final String CFG_SHOW_DELIVERIES_WITHOUT_DATE = "ShowDeliveriesWithoutDate";
	private static final String CFG_SHOW_TO_DATE = "ShowToDate";
	
	

	private boolean showGis;
	private GisType gisType;
	private boolean exportAsSvg;
	private boolean skipEdgelessNodes;
	private boolean showEdgesInMetaNode;
	private boolean joinEdges;
	private boolean hideArrowHead;
	private boolean arrowHeadInMiddle;
	private LabelPosition nodeLabelPosition;
	private boolean showLegend;
	private Dimension canvasSize;
	private List<String> selectedNodes;
	private List<String> selectedEdges;
	private HighlightConditionList nodeHighlightConditions;
	private HighlightConditionList edgeHighlightConditions;
	private Map<String, Map<String, Point2D>> collapsedNodes;
	private String label;

	private Map<String, Double> nodeWeights;
	private Map<String, Double> edgeWeights;
	private Map<String, Boolean> nodeCrossContaminations;
	private Map<String, Boolean> edgeCrossContaminations;
	private Map<String, Boolean> nodeKillContaminations;
	private Map<String, Boolean> edgeKillContaminations;
	private Map<String, Boolean> observedNodes;
	private Map<String, Boolean> observedEdges;
	private boolean enforceTemporalOrder;
	private boolean showForward;
	private boolean showDeliveriesWithoutDate;
	private GregorianCalendar showToDate;

	private GraphSettings graphSettings;
	private GisSettings gisSettings;
	private ExplosionSettingsList gobjExplosionSettingsList;

	public TracingViewSettings() {
		showGis = false;
		gisType = GisType.SHAPEFILE;
		exportAsSvg = false;
		skipEdgelessNodes = false;
		showEdgesInMetaNode = false;
		joinEdges = false;
		hideArrowHead = false;
		arrowHeadInMiddle = false;
		nodeLabelPosition = LabelPosition.BOTTOM_RIGHT;
		showLegend = false;
		canvasSize = null;
		selectedNodes = new ArrayList<>();
		selectedEdges = new ArrayList<>();
		nodeHighlightConditions = new HighlightConditionList();
		edgeHighlightConditions = new HighlightConditionList();
		collapsedNodes = new LinkedHashMap<>();
		label = null;

		nodeWeights = new LinkedHashMap<>();
		edgeWeights = new LinkedHashMap<>();
		nodeCrossContaminations = new LinkedHashMap<>();
		edgeCrossContaminations = new LinkedHashMap<>();
		nodeKillContaminations = new LinkedHashMap<>();
		edgeKillContaminations = new LinkedHashMap<>();
		observedNodes = new LinkedHashMap<>();
		observedEdges = new LinkedHashMap<>();
		enforceTemporalOrder = true;
		showForward = false;
		showDeliveriesWithoutDate = true;
		showToDate = null;

		graphSettings = new GraphSettings();
		gisSettings = new GisSettings();
		this.gobjExplosionSettingsList = new ExplosionSettingsList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void loadSettings(NodeSettingsRO settings) {
		try {
			showGis = settings.getBoolean(CFG_SHOW_GIS);
		} catch (InvalidSettingsException e) {
		}

		try {
			gisType = GisType.valueOf(settings.getString(CFG_GIS_TYPE));
		} catch (InvalidSettingsException | IllegalArgumentException e) {
		}

		try {
			exportAsSvg = settings.getBoolean(CFG_EXPORT_AS_SVG);
		} catch (InvalidSettingsException e) {
		}

		try {
			skipEdgelessNodes = settings.getBoolean(CFG_SKIP_EDGELESS_NODES);
		} catch (InvalidSettingsException e) {
		}

		try {
			showEdgesInMetaNode = settings.getBoolean(CFG_SHOW_EDGES_IN_META_NODE);
		} catch (InvalidSettingsException e) {
		}

		try {
			joinEdges = settings.getBoolean(CFG_JOIN_EDGES);
		} catch (InvalidSettingsException e) {
		}

		try {
			hideArrowHead = settings.getBoolean(CFG_HIDE_ARROW_HEAD);
		} catch (InvalidSettingsException e) {
		}

		try {
			arrowHeadInMiddle = settings.getBoolean(CFG_ARROW_HEAD_IN_MIDDLE);
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeLabelPosition = LabelPosition.valueOf(settings.getString(CFG_NODE_LABEL_POSITION));
		} catch (InvalidSettingsException e) {
		}

		try {
			showLegend = settings.getBoolean(CFG_SHOW_LEGEND);
		} catch (InvalidSettingsException e) {
		}

		try {
			canvasSize = (Dimension) SERIALIZER.fromXml(settings.getString(CFG_CANVAS_SIZE));
		} catch (InvalidSettingsException e) {
		}

		try {
			selectedNodes = (List<String>) SERIALIZER.fromXml(settings.getString(CFG_SELECTED_NODES));
		} catch (InvalidSettingsException e) {
		}

		try {
			selectedEdges = (List<String>) SERIALIZER.fromXml(settings.getString(CFG_SELECTED_EDGES));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeHighlightConditions = (HighlightConditionList) SERIALIZER
					.fromXml(BackwardUtils.toNewHighlightingFormat(settings.getString(CFG_NODE_HIGHLIGHT_CONDITIONS)));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeHighlightConditions = (HighlightConditionList) SERIALIZER
					.fromXml(BackwardUtils.toNewHighlightingFormat(settings.getString(CFG_EDGE_HIGHLIGHT_CONDITIONS)));
		} catch (InvalidSettingsException e) {
		}

		try {
			collapsedNodes = (Map<String, Map<String, Point2D>>) SERIALIZER
					.fromXml(settings.getString(CFG_COLLAPSED_NODES));
		} catch (InvalidSettingsException e) {
		}

		try {
			label = settings.getString(CFG_LABEL);
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeWeights = (Map<String, Double>) SERIALIZER.fromXml(settings.getString(CFG_NODE_WEIGHTS));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeWeights = (Map<String, Double>) SERIALIZER.fromXml(settings.getString(CFG_EDGE_WEIGHTS));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeCrossContaminations = (Map<String, Boolean>) SERIALIZER
					.fromXml(settings.getString(CFG_NODE_CROSS_CONTAMINATIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeCrossContaminations = (Map<String, Boolean>) SERIALIZER
					.fromXml(settings.getString(CFG_EDGE_CROSS_CONTAMINATIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeKillContaminations = (Map<String, Boolean>) SERIALIZER
					.fromXml(settings.getString(CFG_NODE_KILL_CONTAMINATIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeKillContaminations = (Map<String, Boolean>) SERIALIZER
					.fromXml(settings.getString(CFG_EDGE_KILL_CONTAMINATIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			observedNodes = (Map<String, Boolean>) SERIALIZER.fromXml(settings.getString(CFG_OBSERVED_NODES));
		} catch (InvalidSettingsException e) {
		}

		try {
			observedEdges = (Map<String, Boolean>) SERIALIZER.fromXml(settings.getString(CFG_OBSERVED_EDGES));
		} catch (InvalidSettingsException e) {
		}

		try {
			enforceTemporalOrder = settings.getBoolean(CFG_ENFORCE_TEMPORAL_ORDER);
		} catch (InvalidSettingsException e) {
		}

		try {
			showForward = settings.getBoolean(CFG_SHOW_FORWARD);
		} catch (InvalidSettingsException e) {
		}

		try {
			showDeliveriesWithoutDate = settings.getBoolean(CFG_SHOW_DELIVERIES_WITHOUT_DATE);
		} catch (InvalidSettingsException e) {
		}

		try {
			showToDate = (GregorianCalendar) SERIALIZER.fromXml(settings.getString(CFG_SHOW_TO_DATE));
		} catch (InvalidSettingsException e) {
		}

		graphSettings.loadSettings(settings);
		gisSettings.loadSettings(settings);
		this.gobjExplosionSettingsList.loadSettings(settings);
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addBoolean(CFG_SHOW_GIS, showGis);
		settings.addString(CFG_GIS_TYPE, gisType.name());
		settings.addBoolean(CFG_EXPORT_AS_SVG, exportAsSvg);
		settings.addBoolean(CFG_SKIP_EDGELESS_NODES, skipEdgelessNodes);
		settings.addBoolean(CFG_SHOW_EDGES_IN_META_NODE, showEdgesInMetaNode);
		settings.addBoolean(CFG_JOIN_EDGES, joinEdges);
		settings.addBoolean(CFG_HIDE_ARROW_HEAD, hideArrowHead);
		settings.addBoolean(CFG_ARROW_HEAD_IN_MIDDLE, arrowHeadInMiddle);
		settings.addString(CFG_NODE_LABEL_POSITION, nodeLabelPosition.name());
		settings.addBoolean(CFG_SHOW_LEGEND, showLegend);
		settings.addString(CFG_CANVAS_SIZE, SERIALIZER.toXml(canvasSize));
		settings.addString(CFG_SELECTED_NODES, SERIALIZER.toXml(selectedNodes));
		settings.addString(CFG_SELECTED_EDGES, SERIALIZER.toXml(selectedEdges));
		settings.addString(CFG_NODE_HIGHLIGHT_CONDITIONS, SERIALIZER.toXml(nodeHighlightConditions));
		settings.addString(CFG_EDGE_HIGHLIGHT_CONDITIONS, SERIALIZER.toXml(edgeHighlightConditions));
		settings.addString(CFG_COLLAPSED_NODES, SERIALIZER.toXml(collapsedNodes));
		settings.addString(CFG_LABEL, label);

		settings.addString(CFG_NODE_WEIGHTS, SERIALIZER.toXml(nodeWeights));
		settings.addString(CFG_EDGE_WEIGHTS, SERIALIZER.toXml(edgeWeights));
		settings.addString(CFG_NODE_CROSS_CONTAMINATIONS, SERIALIZER.toXml(nodeCrossContaminations));
		settings.addString(CFG_EDGE_CROSS_CONTAMINATIONS, SERIALIZER.toXml(edgeCrossContaminations));
		settings.addString(CFG_NODE_KILL_CONTAMINATIONS, SERIALIZER.toXml(nodeKillContaminations));
		settings.addString(CFG_EDGE_KILL_CONTAMINATIONS, SERIALIZER.toXml(edgeKillContaminations));
		settings.addString(CFG_OBSERVED_NODES, SERIALIZER.toXml(observedNodes));
		settings.addString(CFG_OBSERVED_EDGES, SERIALIZER.toXml(observedEdges));
		settings.addBoolean(CFG_ENFORCE_TEMPORAL_ORDER, enforceTemporalOrder);
		settings.addBoolean(CFG_SHOW_FORWARD, showForward);
		settings.addBoolean(CFG_SHOW_DELIVERIES_WITHOUT_DATE, showDeliveriesWithoutDate);
		settings.addString(CFG_SHOW_TO_DATE, SERIALIZER.toXml(showToDate));

		graphSettings.saveSettings(settings);
		gisSettings.saveSettings(settings);
		this.gobjExplosionSettingsList.saveSettings(settings);
	}
	
	protected boolean isNodeSelected(String id) {
	  if(this.selectedNodes.contains(id)) return true;
	  for(Map<String, Point2D> childMap : this.collapsedNodes.values()) if(childMap.containsKey(id)) return true;
	  return false;
	}

	protected boolean isEdgeSelected(String id) {
      return this.selectedEdges.contains(id);
    }
	
	public void setFromCanvas(ITracingCanvas<?> canvas, boolean resized) {
		showLegend = canvas.getOptionsPanel().isShowLegend();
		joinEdges = canvas.getOptionsPanel().isJoinEdges();
		hideArrowHead = canvas.getOptionsPanel().getArrowHeadType() == ArrowHeadType.HIDE;
		arrowHeadInMiddle = canvas.getOptionsPanel().getArrowHeadType() == ArrowHeadType.IN_MIDDLE;
		nodeLabelPosition = canvas.getOptionsPanel().getNodeLabelPosition();
		skipEdgelessNodes = canvas.getOptionsPanel().isSkipEdgelessNodes();
		showEdgesInMetaNode = canvas.getOptionsPanel().isShowEdgesInMetaNode();
		label = canvas.getOptionsPanel().getLabel();

		setSelectedNodes(Ordering.natural().sortedCopy(canvas.getSelectedNodeIds()));
		setSelectedEdges(Ordering.natural().sortedCopy(canvas.getSelectedEdgeIds()));

		nodeHighlightConditions = canvas.getNodeHighlightConditions();
		edgeHighlightConditions = canvas.getEdgeHighlightConditions();
		
		if(gobjExplosionSettingsList.getActiveExplosionSettings()==null) {
			collapsedNodes = BackwardUtils.toOldCollapseFormat(canvas.getCollapsedNodes());
		}
		
		
		if (resized || canvasSize == null) {
			canvasSize = canvas.getCanvasSize();
		}

		nodeWeights = canvas.getNodeWeights();
		edgeWeights = canvas.getEdgeWeights();
		nodeCrossContaminations = canvas.getNodeCrossContaminations();
		edgeCrossContaminations = canvas.getEdgeCrossContaminations();
		nodeKillContaminations = canvas.getNodeKillContaminations();
		edgeKillContaminations = canvas.getEdgeKillContaminations();
		observedNodes = canvas.getObservedNodes();
		observedEdges = canvas.getObservedEdges();
		enforceTemporalOrder = canvas.isEnforceTemporalOrder();
		showForward = canvas.isShowForward();
		showDeliveriesWithoutDate = canvas.isShowDeliveriesWithoutDate();
		showToDate = canvas.getShowToDate();
	}

	public void setToCanvas(ITracingCanvas<?> canvas) {
		canvas.getOptionsPanel().setShowLegend(showLegend);
		canvas.getOptionsPanel().setJoinEdges(joinEdges);
		canvas.getOptionsPanel().setArrowHeadType(hideArrowHead ? ArrowHeadType.HIDE
				: (arrowHeadInMiddle ? ArrowHeadType.IN_MIDDLE : ArrowHeadType.AT_TARGET));
		canvas.getOptionsPanel().setNodeLabelPosition(nodeLabelPosition);
		canvas.getOptionsPanel().setLabel(label);
		canvas.getOptionsPanel().setSkipEdgelessNodes(skipEdgelessNodes);
		canvas.getOptionsPanel().setShowEdgesInMetaNode(showEdgesInMetaNode);
		
		canvas.setCollapsedNodes(BackwardUtils.toNewCollapseFormat(collapsedNodes));
		
		canvas.setNodeHighlightConditions(de.bund.bfr.knime.openkrise.BackwardUtils
				.renameColumns(nodeHighlightConditions, canvas.getNodeSchema().getMap().keySet()));
		canvas.setEdgeHighlightConditions(de.bund.bfr.knime.openkrise.BackwardUtils
				.renameColumns(edgeHighlightConditions, canvas.getEdgeSchema().getMap().keySet()));
		canvas.setSelectedNodeIds(new LinkedHashSet<>(this.getSelectedNodes()));
		canvas.setSelectedEdgeIds(new LinkedHashSet<>(this.getSelectedEdges()));

		if (canvasSize != null) {
			canvas.setCanvasSize(canvasSize);
		}

		canvas.setNodeWeights(nodeWeights);
		canvas.setEdgeWeights(edgeWeights);
		canvas.setNodeCrossContaminations(nodeCrossContaminations);
		canvas.setEdgeCrossContaminations(edgeCrossContaminations);
		canvas.setNodeKillContaminations(nodeKillContaminations);
		canvas.setEdgeKillContaminations(edgeKillContaminations);
		canvas.setObservedNodes(observedNodes);
		canvas.setObservedEdges(observedEdges);
		canvas.setEnforceTemporalOrder(enforceTemporalOrder);
		canvas.setShowForward(showForward);
		canvas.setShowDeliveriesWithoutDate(showDeliveriesWithoutDate);
		canvas.setShowToDate(showToDate);
	}

	public GraphSettings getGraphSettings() {
		return (this.gobjExplosionSettingsList.getActiveExplosionSettings()==null? 
				this.graphSettings:
				this.gobjExplosionSettingsList.getActiveExplosionSettings().getGraphSettings());
	}

	public GisSettings getGisSettings() {
		return (this.gobjExplosionSettingsList.getActiveExplosionSettings()==null? 
				this.gisSettings:
				this.gobjExplosionSettingsList.getActiveExplosionSettings().getGisSettings());
	}
	
	private List<String> getSelectedNodes() {
		return (this.gobjExplosionSettingsList.getActiveExplosionSettings()==null? 
				this.selectedNodes:
				this.gobjExplosionSettingsList.getActiveExplosionSettings().getSelectedNodes());
	}
	
	private List<String> getSelectedEdges() {
		return (this.gobjExplosionSettingsList.getActiveExplosionSettings()==null? 
				this.selectedEdges:
				this.gobjExplosionSettingsList.getActiveExplosionSettings().getSelectedEdges());
	}
	
	private void setSelectedNodes(List<String> selectedNodes) {
		if(this.gobjExplosionSettingsList.getActiveExplosionSettings()==null) {
			this.selectedNodes = selectedNodes;
		} else {
			this.gobjExplosionSettingsList.getActiveExplosionSettings().setSelectedNodes(selectedNodes);
		}
	}
	
	private void setSelectedEdges(List<String> selectedEdges) {
		if(this.gobjExplosionSettingsList.getActiveExplosionSettings()==null) {
			this.selectedEdges = selectedEdges;
		} else {
			this.gobjExplosionSettingsList.getActiveExplosionSettings().setSelectedEdges(selectedEdges);
		}
	}
	
	public ExplosionSettingsList getExplosionSettingsList() {
		return this.gobjExplosionSettingsList;
	}
	
	public boolean isShowGis() {
		return showGis;
	}

	public void setShowGis(boolean showGis) {
		this.showGis = showGis;
	}

	public GisType getGisType() {
		return gisType;
	}

	public void setGisType(GisType gisType) {
		this.gisType = gisType;
	}

	public boolean isExportAsSvg() {
		return exportAsSvg;
	}

	public void setExportAsSvg(boolean exportAsSvg) {
		this.exportAsSvg = exportAsSvg;
	}

	public void clearWeights() {
		nodeWeights.clear();
		edgeWeights.clear();
	}

	public void clearCrossContamination() {
		nodeCrossContaminations.clear();
		edgeCrossContaminations.clear();
	}

	public void clearKillContamination() {
		nodeKillContaminations.clear();
		edgeKillContaminations.clear();
	}

	public void clearObserved() {
		observedNodes.clear();
		observedEdges.clear();
	}
	
	public JsonValue toJson() throws JsonProcessingException {
//	  ObjectMapper mapper = new ObjectMapper();
//      SettingsJson obj = new SettingsJson();
      JsonConverter.JsonBuilder jsonBuilder = new JsonConverter.JsonBuilder();
      //this.saveSettings(obj);
      this.saveSettings(jsonBuilder);
      
      return jsonBuilder.build();
      //this.graphSettings.saveSettings(obj.settings.view.graph);
      //this.gisSettings.saveSettings(obj.settings.view.gis);
      
//      JsonValue tmp = null; 
//      try { tmp = JacksonConversions.getInstance().toJSR353(mapper.valueToTree(obj.settings));} 
//      catch(Exception e) {
//        try { tmp = JacksonConversions.getInstance().toJSR353(mapper.valueToTree(obj.settings.metaNodes));} 
//        catch(Exception e1) {
//          
//        }
//        tmp = null;
//        try { tmp = JacksonConversions.getInstance().toJSR353(mapper.valueToTree(obj.settings.tracing));} 
//        catch(Exception e1) {
//          
//        }
//        tmp = null;
//        try { tmp = JacksonConversions.getInstance().toJSR353(mapper.valueToTree(obj.settings.view));} 
//        catch(Exception e1) {
//          try { tmp = JacksonConversions.getInstance().toJSR353(mapper.valueToTree(obj.settings.view.edge));} 
//          catch(Exception e2) {
//            
//          }
//          tmp = null;
//          try { tmp = JacksonConversions.getInstance().toJSR353(mapper.valueToTree(obj.settings.view.node));} 
//          catch(Exception e2) {
//            
//          }
//          tmp = null;
//          try { tmp = JacksonConversions.getInstance().toJSR353(mapper.valueToTree(obj.settings.view.gis));} 
//          catch(Exception e2) {
//            try { tmp = JacksonConversions.getInstance().toJSR353(mapper.valueToTree(obj.settings.view.gis.transformation));} 
//            catch(Exception e3) {
//              
//            }
//            tmp = null;
//            try { tmp = JacksonConversions.getInstance().toJSR353(mapper.valueToTree(obj.settings.view.gis.edge));} 
//            catch(Exception e3) {
//              
//            }
//            tmp = null;
//            try { tmp = JacksonConversions.getInstance().toJSR353(mapper.valueToTree(obj.settings.view.gis.node));} 
//            catch(Exception e3) {
//              
//            }
//            tmp = null;
//            try { tmp = JacksonConversions.getInstance().toJSR353(mapper.valueToTree(obj.settings.view.gis.text));} 
//            catch(Exception e3) {
//              
//            }
//          }
//          tmp = null;
//          try { tmp = JacksonConversions.getInstance().toJSR353(mapper.valueToTree(obj.settings.view.graph));} 
//          catch(Exception e2) {
//            
//          }
//          tmp = null;
//          try { tmp = JacksonConversions.getInstance().toJSR353(mapper.valueToTree(obj.settings.view.explosions));} 
//          catch(Exception e2) {
//            
//          }
//        }
//      }
//      ObjectNode rootNode = mapper.valueToTree(obj);
      
     
      //StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
//      return JacksonConversions.getInstance().toJSR353(rootNode);
	}
	
	protected void saveSettings(JsonConverter.JsonBuilder jsonBuilder) {
//    obj.settings.collapsedNodes = this.collapsedNodes.entrySet().stream().map(entry -> new JsonFormat.Global.MetaNode(entry.getKey(), entry.getValue().keySet().stream().toArray(String[]::new))).toArray(JsonFormat.Global.MetaNode[]::new);
//    obj.settings.view
      //SettingsJson.TVSettings settings = new SettingsJson.TVSettings(); //   obj.settings;
      //obj.settings = settings;
     
      // general settings
	  jsonBuilder.setGlobalViewSettings(this.showLegend, this.exportAsSvg, this.showGis, this.gisType, this.label);
	  
//      settings.view = new SettingsJson.View();
//      settings.view.showLegend = this.showLegend;
//      settings.view.exportAsSvg = this.exportAsSvg;
//      settings.view.showGis = this.showGis;
//      settings.view.label = this.label;
	  jsonBuilder.setMetaNodes(this.collapsedNodes);
//      List<SettingsJson.MetaNode> metaNodeList = new ArrayList<>();
//      
//      for(Map.Entry<String, Map<String, Point2D>> entry: this.collapsedNodes.entrySet()) {
//        metaNodeList.add(new SettingsJson.MetaNode(entry.getKey(), (entry.getKey().startsWith("SC:")?"SimpleChain":null), entry.getValue().keySet().toArray(new String[0])));
//      }
//      settings.metaNodes = metaNodeList.toArray(new SettingsJson.MetaNode[0]);
      
      
      // tracing related settings
//      settings.tracing = new SettingsJson.Tracing(enforceTemporalOrder, 
//          edgeWeights, edgeCrossContaminations, edgeKillContaminations, observedEdges, 
//          nodeWeights, nodeCrossContaminations, nodeKillContaminations, observedNodes);
      jsonBuilder.setTracing(enforceTemporalOrder, 
          edgeWeights, edgeCrossContaminations, edgeKillContaminations, observedEdges, 
          nodeWeights, nodeCrossContaminations, nodeKillContaminations, observedNodes);
      
      //this.saveTracingSettings(settings);
      
//      settings.view.node = new SettingsJson.View.GlobalNodeSettings();
//      settings.view.node.setHighlighting(this.nodeHighlightConditions.getConditions());
//      settings.view.edge = new SettingsJson.View.GlobalEdgeSettings();
//      settings.view.edge.setHighlighting(this.edgeHighlightConditions.getConditions());
      
//      this.saveHighlightingSettings(settings);
      
      // edge related general settings
//      settings.view.edge.arrowInMiddle = this.arrowHeadInMiddle;
//      settings.view.edge.hideArrowHead = this.hideArrowHead;
//      settings.view.edge.joinEdges = this.joinEdges;
//      settings.view.edge.setDeliveryFilter(TracingColumns.DELIVERY_ARRIVAL, this.showToDate, this.showDeliveriesWithoutDate);
//    
//      settings.view.edge.showEdgesInMetanode = this.showEdgesInMetaNode;
//      settings.view.edge.showCrossContaminatedDeliveries = this.showForward;
//      settings.view.edge.selectedEdges = this.selectedEdges.toArray(new String[0]);
//      
//      // node related general settings
//      settings.view.node.skipEdgelessNodes = this.skipEdgelessNodes;
//      settings.view.node.labelPosition = this.nodeLabelPosition.toString();
//      settings.view.node.selectedNodes = this.selectedNodes.toArray(new String[0]);
      
      jsonBuilder.setGlobalNodeViewSettings(this.skipEdgelessNodes, this.nodeLabelPosition, this.selectedNodes.toArray(new String[0]),
          this.nodeHighlightConditions);
      
      jsonBuilder.setGlobalEdgeViewSettings(
          this.arrowHeadInMiddle, this.hideArrowHead, this.joinEdges, 
          TracingColumns.DELIVERY_ARRIVAL, this.showToDate, this.showDeliveriesWithoutDate,
          this.showEdgesInMetaNode, this.showForward, this.selectedEdges.toArray(new String[0]),
          this.edgeHighlightConditions);
      
      
      
      // gisView related Settings
//      settings.view.gis = new SettingsJson.View.GisSettings();
//      settings.view.gis.type = this.gisType.toString();
      this.gisSettings.saveSettings(jsonBuilder);
      
      // graphView related Settings
      //settings.view.graph = new SettingsJson.View.GraphSettings();
      this.graphSettings.saveSettings(jsonBuilder);
      jsonBuilder.setCollapsedPositionsInGraphSettings(this.collapsedNodes);
      
      // explosion view related settings
      this.gobjExplosionSettingsList.saveSettings(jsonBuilder);
    }
	
//	private void saveTracingSettings(SettingsJson.TVSettings settings) {
//	  settings.tracing.enforceTemporalOrder = this.enforeTemporalOrder;
//      List<JsonFormat.Tracing.Delivery> deliveryTracingList = new ArrayList<>();
//      
//      for(String id: edgeCrossContaminations.keySet()) 
//        deliveryTracingList.add(
//            new SettingsJson.Tracing.Delivery(id, edgeWeights.get(id), edgeCrossContaminations.get(id), edgeKillContaminations.get(id), observedEdges.get(id)));
//      
//      settings.tracing.deliveries = deliveryTracingList.toArray(new SettingsJson.Tracing.Delivery[0]);
//      
//      List<SettingsJson.Tracing.Node> nodeTracingList = new ArrayList<>();
//      
//      for(String id: nodeCrossContaminations.keySet()) 
//        nodeTracingList.add(
//            new SettingsJson.Tracing.Node(id, nodeWeights.get(id), nodeCrossContaminations.get(id), nodeKillContaminations.get(id), observedNodes.get(id)));
//      
//      settings.tracing.nodes = nodeTracingList.toArray(new JsonFormat.Tracing.Node[0]);
//	}
	
//	private void saveHighlightingSettings(SettingsJson.TVSettings settings) {
//	 
//      List<SettingsJson.View.GlobalNodeViewProps.NodeHighlightCondition> nodeHighlightConditionList = new ArrayList<>();
//     
//      for(HighlightCondition hLCondition: this.nodeHighlightConditions.getConditions()) {
//        // super(name, showInLegend, color, invisible, adjustThickness, label, valueCondition, logicalConditions);
//        Color color = hLCondition.getColor();
//        
//        nodeHighlightConditionList.add(
//            new SettingsJson.View.GlobalNodeViewProps.NodeHighlightCondition(
//                hLCondition.getName(), hLCondition.isShowInLegend(), 
//                (int []) (color==null?null:new int[] {color.getRed(), color.getGreen(), color.getBlue()}),
//                hLCondition.isInvisible(), hLCondition.isUseThickness(), hLCondition.getLabelProperty(), 
//                getValueCondition(hLCondition), getLogicalConditions(hLCondition),
//                hLCondition.getShape().toString()));
//      }
//      
//      settings.view.nodeProps.highlightConditions = nodeHighlightConditionList.toArray(new SettingsJson.View.GlobalNodeViewProps.NodeHighlightCondition[0]);
//      
//      List<SettingsJson.View.GlobalEdgeViewProps.EdgeHighlightCondition> edgeHighlightConditionList = new ArrayList<>();
//      
//      for(HighlightCondition hLCondition: this.edgeHighlightConditions.getConditions()) {
//        // super(name, showInLegend, color, invisible, adjustThickness, label, valueCondition, logicalConditions);
//        Color color = hLCondition.getColor();
//        
//        edgeHighlightConditionList.add(
//            new SettingsJson.View.GlobalEdgeViewProps.EdgeHighlightCondition(
//                hLCondition.getName(), hLCondition.isShowInLegend(), 
//                (int []) (color==null?null:new int[] {color.getRed(), color.getGreen(), color.getBlue()}),
//                hLCondition.isInvisible(), hLCondition.isUseThickness(), hLCondition.getLabelProperty(), 
//                getValueCondition(hLCondition), getLogicalConditions(hLCondition),
//                null));
//      }
//      
//      settings.view.edgeProps.highlightConditions = edgeHighlightConditionList.toArray(new JsonFormat.View.GlobalEdgeViewProps.EdgeHighlightCondition[0]);
//    }
      
//    private SettingsJson.View.ValueCondition getValueCondition(de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition hLCondition) {
//      
//      ValueHighlightCondition valueHighlightCondition = null; 
//      if(hLCondition instanceof LogicalValueHighlightCondition) {//AndOrHighlightCondition) {
//        valueHighlightCondition = ((LogicalValueHighlightCondition) hLCondition).getValueCondition();
//      } else if(hLCondition instanceof ValueHighlightCondition) {
//        valueHighlightCondition = (ValueHighlightCondition) hLCondition;
//      }
//      if(valueHighlightCondition!=null) return new SettingsJson.View.ValueCondition(valueHighlightCondition.getProperty(), valueHighlightCondition.getType().toString(), valueHighlightCondition.isZeroAsMinimum());
//      return null;
//    }
	
//    private SettingsJson.View.LogicalCondition[][] getLogicalConditions(de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition hLCondition) {
//      
//      AndOrHighlightCondition andOrHighlightCondition = null; 
//      if(hLCondition instanceof LogicalValueHighlightCondition) {//AndOrHighlightCondition) {
//        andOrHighlightCondition = ((LogicalValueHighlightCondition) hLCondition).getLogicalCondition();
//      } else if(hLCondition instanceof AndOrHighlightCondition) {
//        andOrHighlightCondition = (AndOrHighlightCondition) hLCondition;
//      }
//      if(andOrHighlightCondition!=null) {
//        int nOr = andOrHighlightCondition.getConditions().size();
//        SettingsJson.View.LogicalCondition[][] result = new SettingsJson.View.LogicalCondition[nOr][];
//        for(int iOr=0; iOr<nOr; ++iOr) {
//          int nAnd = andOrHighlightCondition.getConditions().get(iOr).size();
//          result[iOr] = new SettingsJson.View.LogicalCondition[nAnd];
//          for(int iAnd=0; iAnd<nAnd; ++iAnd) result[iOr][iAnd] = getLogicalCondition(andOrHighlightCondition.getConditions().get(iOr).get(iAnd));
//        }
//        return result;
//      }
//      return null;
//    }
    
//    private SettingsJson.View.LogicalCondition getLogicalCondition(LogicalHighlightCondition logicalCondition) {
//      return new SettingsJson.View.LogicalCondition(logicalCondition.getProperty(), logicalCondition.getType().toString(), logicalCondition.getValue());
//    }
	
	private static void setTracing(Tracing.TraceableUnit[] traceableUnits, Map<String, Double> weights, Map<String, Boolean> crossContaminations, Map<String, Boolean> killContaminations, Map<String, Boolean> observed) {
	  for(Tracing.TraceableUnit traceableUnit: traceableUnits) {
	    setTracingInfo(traceableUnit.id,traceableUnit.weight,weights);
	    setTracingInfo(traceableUnit.id,traceableUnit.crossContamination,crossContaminations);
	    setTracingInfo(traceableUnit.id,traceableUnit.killContamination,killContaminations);
	    setTracingInfo(traceableUnit.id,traceableUnit.observed,observed);
      }
	}
	
	private static <T> void setTracingInfo(String id, T value, Map<String, T> map) {
	  if(value!=null) map.put(id, value);
	}
	
//	private static void setEdgeHighlighting(HighlightConditionList target, GlobalEdgeSettings.EdgeHighlightCondition[] sources) {
//	  for(GlobalEdgeSettings.EdgeHighlightCondition source: sources) {
//	    HighlightCondition hLCondition 
//	    if(source.)
//	    HighlightCondition hLCondition = new HighlightCondition();
//	    target.getConditions().add(new HighlightCondition())
//	  }
//	}
	
	public void loadSettings(JsonFormat jsonFormat) throws JsonProcessingException, InvalidSettingsException {
//	  if(json != null) {
//  	    ObjectMapper mapper = new ObjectMapper();
//  	  
//  	    JsonNode rootNode = JacksonConversions.getInstance().toJackson(json);
//  	    JsonFormat jsonFormat = mapper.treeToValue(rootNode, JsonFormat.class);
  	    JsonFormat.TracingViewSettings settings = jsonFormat.settings;
  	    JsonFormat.Tracing tracing = jsonFormat.tracing;
  
  	    // ToDo: check nulls
  	    // metanode Settings
//  	    if(settings.metaNodes!=null) {
//  	      this.collapsedNodes = new LinkedHashMap<>();
//  	      for(JsonFormat.TracingViewSettings.MetaNode metaNode : settings.metaNodes) {
//  	        Map<String, Point2D> map = new LinkedHashMap<>();
//  	        for(String memberId : metaNode.members) {
//  	          if(this.)
//  	        }
//  	      }
//  	    }
        // general settings
  	    if(settings.view!=null) {
  	      if(settings.view.showLegend!=null) this.showLegend = settings.view.showLegend;
  	      if(settings.view.exportAsSvg!=null) this.exportAsSvg = settings.view.exportAsSvg;
          if(settings.view.showGis!=null)  this.showGis = settings.view.showGis;
          this.label = settings.view.label;
  	    }
          //this.collapsedNodes = null;
        
        // tracing related settings
  	    if(tracing!=null) {
  	      
  	      if(tracing.enforceTemporalOrder!=null) this.enforceTemporalOrder = tracing.enforceTemporalOrder;
  	      this.edgeCrossContaminations = new LinkedHashMap<>();
          this.edgeKillContaminations = new LinkedHashMap<>();
          this.edgeWeights = new LinkedHashMap<>();
          this.observedEdges = new LinkedHashMap<>();
          this.nodeCrossContaminations = new LinkedHashMap<>();
          this.nodeKillContaminations = new LinkedHashMap<>();
          this.nodeWeights = new LinkedHashMap<>();
          this.observedNodes = new LinkedHashMap<>();
        
          setTracing(tracing.nodes, nodeWeights, nodeCrossContaminations, nodeKillContaminations, observedNodes);
          setTracing(tracing.deliveries, edgeWeights, edgeCrossContaminations, edgeKillContaminations, observedEdges);
  	    }
        //settings.view.edge.highlightConditions
        //this.edgeHighlightConditions = new HighlightConditionList();
        JsonConverter.setHighlighting(this.edgeHighlightConditions, settings.view.edge.highlightConditions);
        JsonConverter.setHighlighting(this.nodeHighlightConditions, settings.view.node.highlightConditions);
        
        //this.nodeHighlightConditions = null;
        
        // edge related general settings
        if(settings.view.edge.arrowHeadInMiddle!=null) this.arrowHeadInMiddle = settings.view.edge.arrowHeadInMiddle;
        if(settings.view.edge.hideArrowHead!=null) this.hideArrowHead = settings.view.edge.hideArrowHead;
        if(settings.view.edge.joinEdges!=null) this.joinEdges = settings.view.edge.joinEdges;
        if(settings.view.edge.filter!=null && settings.view.edge.filter.dateFilter!=null) {
          // ToDO: consider date id
          if(settings.view.edge.filter.dateFilter.showDeliveriesWithoutDate!=null) this.showDeliveriesWithoutDate = settings.view.edge.filter.dateFilter.showDeliveriesWithoutDate;
          if(settings.view.edge.filter.dateFilter.toDate!=null) 
            this.showToDate = new GregorianCalendar(settings.view.edge.filter.dateFilter.toDate.year, settings.view.edge.filter.dateFilter.toDate.month, settings.view.edge.filter.dateFilter.toDate.day) ;
        }
        if(settings.view.edge.showEdgesInMetanode!=null) this.showEdgesInMetaNode = settings.view.edge.showEdgesInMetanode;
        if(settings.view.edge.showCrossContaminatedDeliveries!=null) this.showForward = settings.view.edge.showCrossContaminatedDeliveries;
        this.selectedEdges = new ArrayList<>(Arrays.asList(settings.view.edge.selectedEdges));
        
        // node related general settings
        if(settings.view.node.skipEdgelessNodes!=null) this.skipEdgelessNodes = settings.view.node.skipEdgelessNodes;
        if(settings.view.node.labelPosition!=null) this.nodeLabelPosition = JsonConverter.getLabelPosition(settings.view.node.labelPosition);
        
        this.selectedNodes = new ArrayList<>(Arrays.asList(settings.view.node.selectedNodes));
        
        // gisView related Settings
        if(settings.view.gisType!=null) this.gisType = JsonConverter.getGisType(settings.view.gisType);
        
        if(settings.view.gis!=null) this.gisSettings.loadSettings(settings.view.gis);
        if(settings.view.graph!=null) this.graphSettings.loadSettings(settings.view.graph);
   
        if(settings.view.explosions!=null) this.gobjExplosionSettingsList.loadSettings(settings.view);
        
        // meta nodes
        if(settings.metaNodes!=null) {
          
          this.collapsedNodes = new LinkedHashMap<>();
          Map<String, Point2D> globalMap = JsonConverter.getPositions(settings.view.graph.node.collapsedPositions);
          for(JsonFormat.TracingViewSettings.MetaNode metaNode : settings.metaNodes) {
            Map<String, Point2D> groupMap = new LinkedHashMap<>();
            for(String memberId : metaNode.members) groupMap.put(memberId, globalMap.get(memberId));
            this.collapsedNodes.put(metaNode.id, groupMap);
          }
        }
//	  }
	}
	
}
