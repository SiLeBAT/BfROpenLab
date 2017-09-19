package de.bund.bfr.knime.openkrise.views.tracingview;

import java.util.Set;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.IGisCanvas;

public class ExplosionSettings {
	
	private final static String CFG_KEY = "ExplosionKey";
	private final static String CFG_NODE_SUBSET = "ExplosionNodeSubset";
	
	private String gstrKey;
	private Set<String> gobjNodeSubset;
	private GisSettings gobjGisSettings;
	private GraphSettings gobjGraphSettings;
	
	public ExplosionSettings() {
		this.gobjGisSettings = new GisSettings();
		this.gobjGraphSettings = new GraphSettings();
	}
	
	public ExplosionSettings(String strKey, Set<String> objNodeSet) {
		this.gobjGisSettings = new GisSettings();
		this.gobjGraphSettings = new GraphSettings();
		this.gstrKey = strKey;
		this.gobjNodeSubset = objNodeSet;
	}

	public GraphSettings getGraphSettings() { return this.gobjGraphSettings; }
	public GisSettings getGisSettings() { return this.gobjGisSettings; }
	public String getKey() { return this.gstrKey; }
	public Set<String> getContainedNodes() { 
		return this.gobjNodeSubset; 
	}
	
	public void loadSettings(NodeSettingsRO settings, String prefix) {
		// TODO Auto-generated method stub
		try {
			this.gstrKey = settings.getString(prefix + CFG_KEY);
		} catch (InvalidSettingsException e) {
		}
		try {
			this.gobjNodeSubset = (Set<String>) GraphSettings.SERIALIZER.fromXml(settings.getString(prefix + CFG_NODE_SUBSET));
		} catch (InvalidSettingsException e) {
		}
		this.gobjGisSettings.loadSettings(settings, prefix);
		this.gobjGraphSettings.loadSettings(settings, prefix);
	}

	public void saveSettings(NodeSettingsWO settings, String prefix) {
		// TODO Auto-generated method stub
		settings.addString(prefix + CFG_KEY, this.gstrKey);
		settings.addString(prefix + CFG_NODE_SUBSET, GraphSettings.SERIALIZER.toXml(this.gobjNodeSubset));
		this.gobjGisSettings.saveSettings(settings, prefix);
		this.gobjGraphSettings.saveSettings(settings, prefix);
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


}