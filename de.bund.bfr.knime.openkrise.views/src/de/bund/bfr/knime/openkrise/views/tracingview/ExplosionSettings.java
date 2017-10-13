package de.bund.bfr.knime.openkrise.views.tracingview;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.IGisCanvas;

public class ExplosionSettings {
	
	private final static String CFG_KEY = "ExplosionKey";
	private final static String CFG_NODE_SUBSET = "ExplosionNodeSubset";
	
	private String metaNodeId;
	private Set<String> containedNodesIds;
	private List<String> selectedNodes;
	private List<String> selectedEdges;
	private GisSettings gisSettings;
	private GraphSettings graphSettings;
	
	public ExplosionSettings() {
		this.gisSettings = new GisSettings();
		this.graphSettings = new GraphSettings();
		this.selectedNodes = new ArrayList<>();
		this.selectedEdges = new ArrayList<>();
	}
	
	public ExplosionSettings(String metaNodeId, Set<String> containedNodesIds) {
		this();
		this.metaNodeId = metaNodeId;
		this.containedNodesIds = containedNodesIds;
	}

	public GraphSettings getGraphSettings() { return this.graphSettings; }
	public GisSettings getGisSettings() { return this.gisSettings; }
	public String getKey() { return this.metaNodeId; }
	public Set<String> getContainedNodesIds() {	return this.containedNodesIds; }
	
	public void loadSettings(NodeSettingsRO settings, String prefix) {
		// TODO Auto-generated method stub
		try {
			this.metaNodeId = settings.getString(prefix + CFG_KEY);
		} catch (InvalidSettingsException e) {
		}
		try {
			this.containedNodesIds = (Set<String>) GraphSettings.SERIALIZER.fromXml(settings.getString(prefix + CFG_NODE_SUBSET));
		} catch (InvalidSettingsException e) {
		}
		this.gisSettings.loadSettings(settings, prefix);
		this.graphSettings.loadSettings(settings, prefix);
	}

	public void saveSettings(NodeSettingsWO settings, String prefix) {
		// TODO Auto-generated method stub
		settings.addString(prefix + CFG_KEY, this.metaNodeId);
		settings.addString(prefix + CFG_NODE_SUBSET, GraphSettings.SERIALIZER.toXml(this.containedNodesIds));
		this.gisSettings.saveSettings(settings, prefix);
		this.graphSettings.saveSettings(settings, prefix);
	}
	
//	public void setToCanvas(IDetailCanvas canvas) {
//		canvas.setKey(this.gstrKey);
//		canvas.setNodeSubset(this.gobjNodeSubset);
//		if(canvas instanceof GraphCanvas) {
//		  this.gobjGraphSettings.setToCanvas((GraphCanvas) canvas);
//		} else if(canvas instanceof IGisCanvas) {
//			this.gobjGisSettings.setToCanvas((IGisCanvas) canvas);
//		}  
//	}
//	
//	public void setFromCanvas(IDetailCanvas canvas) {
//		this.gstrKey = canvas.getKey();
//		this.gobjNodeSubset = canvas.getNodeSubset();
//		
//		if(canvas instanceof GraphCanvas) {
//		  this.gobjGraphSettings.setFromCanvas((GraphCanvas) canvas);
//		} else if(canvas instanceof IGisCanvas) {
//			this.gobjGisSettings.setFromCanvas((IGisCanvas) canvas);
//		}  
//	}
	
	protected List<String> getSelectedNodes() { return this.selectedNodes; }
	
	protected List<String> getSelectedEdges() { return this.selectedEdges; }
	
	protected void setSelectedNodes(List<String> selectedNodes) { this.selectedNodes = selectedNodes; }
	
	protected void setSelectedEdges(List<String> selectedEdges) { this.selectedEdges = selectedEdges; }

}