package de.bund.bfr.knime.openkrise.views.tracingview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.NodeSettings;

public class ExplosionSettingsList extends NodeSettings {
	
	private final static String CFG_PREFIX = "CFG_EXPLOSION";
	
	private ArrayList<ExplosionSettings> explosionSettingsList;
	private Stack<ExplosionSettings> activeExplosionSettingsList;
	
    protected ExplosionSettingsList() {
		this.explosionSettingsList = new ArrayList<ExplosionSettings>();
		this.activeExplosionSettingsList = new Stack<ExplosionSettings>();
	}
	
	private static String getElementPrefix(int index) {
		return CFG_PREFIX + "_" + index + "_";
	}
	
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		// TODO Auto-generated method stub
		Pattern pattern = Pattern.compile("^" + CFG_PREFIX +  "_([0-9]+)_");
		Set<Integer> indices = settings.keySet().stream().mapToInt(s -> getIndex(pattern, s)).filter(i -> (i>=0)).boxed().collect(Collectors.toSet());

		indices.forEach(i -> {
			ExplosionSettings eS = new ExplosionSettings();
			eS.loadSettings(settings, getElementPrefix(i));
			this.explosionSettingsList.add(eS);
		});
		
		this.activeExplosionSettingsList.clear();
	}
	
	
	
	private static int getIndex(Pattern pattern, String text) {
  	  Matcher matcher = pattern.matcher(text);
  	  return (matcher.find()? Integer.parseInt(matcher.group(1)):-1);
    }

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		// TODO Auto-generated method stub
		for(int i=this.explosionSettingsList.size()-1; i>=0; i--) {
			this.explosionSettingsList.get(i).saveSettings(settings, this.getElementPrefix(i+1));
		}
	}
		
	public ExplosionSettings getExplosionSettings(String strKey, Set<String> containedNodes) {
		//List<ExplosionSettings> oESL = this.gobjExplosionSettingsList.stream().filter(eS -> eS.getContainedNodes().equals(containedNodes)).collect(Collectors.toList());
		List<ExplosionSettings> oESL = this.explosionSettingsList.stream().filter(eS -> eS.getKey().equals(strKey)).collect(Collectors.toList());
		
		return (oESL.size()==0?null:oESL.get(0));
	}
	
	public ExplosionSettings getExplosionSettings(Set<String> containedNodesIds) {
		
		if(containedNodesIds == null || containedNodesIds.isEmpty()) return null;
		
		List<ExplosionSettings> oESL = this.explosionSettingsList.stream().filter(eS -> eS.getContainedNodesIds().equals(containedNodesIds)).collect(Collectors.toList());
		
		return (oESL.size()==0?null:oESL.get(0));
	}
	
	protected ExplosionSettings getActiveExplosionSettings() { return (this.activeExplosionSettingsList.isEmpty()?null:this.activeExplosionSettingsList.peek());}
	
	public boolean setActiveExplosionSettings(ExplosionSettings objES, boolean bolActive) {
		boolean wasActive = this.activeExplosionSettingsList.remove(objES);
			
		if(bolActive && objES!=null) {
			if(!this.explosionSettingsList.contains(objES)) return false;
			this.activeExplosionSettingsList.push(objES);
			if(!wasActive) {
				objES.setSelectedNodes(new ArrayList<>());
				objES.setSelectedEdges(new ArrayList<>());
			}
		}
		return true;
	}
	
	public boolean setActiveExplosionSettings(ExplosionSettings objActivateES, ExplosionSettings objDeactivateES) {
		this.activeExplosionSettingsList.remove(objDeactivateES);
		boolean wasActive = false;
		if(objActivateES!=null) wasActive = this.activeExplosionSettingsList.remove(objActivateES);
		
		if(objActivateES!=null) {
			if(!this.explosionSettingsList.contains(objActivateES)) return false;
			this.activeExplosionSettingsList.push(objActivateES);
			if(!wasActive) {
				objActivateES.setSelectedNodes(new ArrayList<>());
				objActivateES.setSelectedEdges(new ArrayList<>());
			}
		}
		return true;
	}

	public ExplosionSettings setActiveExplosionSettings(String strKey, Set<String> containedNodesIds) {
		ExplosionSettings objES = this.getExplosionSettings(strKey, containedNodesIds);
		
		if(objES==null) {
			objES=new ExplosionSettings(strKey, containedNodesIds);
			this.explosionSettingsList.add(objES);
		}
		
		return (this.setActiveExplosionSettings(objES,true)?objES:null);
	}
	
	public void clearActiveExplosionSettings() {
	  if(this.activeExplosionSettingsList != null) this.activeExplosionSettingsList.clear();	
	}
	
	public ExplosionSettings setActiveExplosionSettings(Set<String> containedNodes, boolean bolActive) {
		if(containedNodes==null || containedNodes.isEmpty()) {
			this.activeExplosionSettingsList.clear();
		} else {
			ExplosionSettings objES = this.getExplosionSettings(containedNodes);
			if(objES!=null && this.setActiveExplosionSettings(objES,bolActive)) return objES;
		}
		return null;
	}

}
