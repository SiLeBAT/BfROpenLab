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
	
	private ArrayList<ExplosionSettings> gobjExplosionSettingsList;
	private Stack<ExplosionSettings> gobjActiveExplosionSettingsList;
	
    protected ExplosionSettingsList() {
		this.gobjExplosionSettingsList = new ArrayList<ExplosionSettings>();
		this.gobjActiveExplosionSettingsList = new Stack<ExplosionSettings>();
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
		});
	}
	
	
	
	private static int getIndex(Pattern pattern, String text) {
  	  Matcher matcher = pattern.matcher(text);
  	  return (matcher.find()? Integer.parseInt(matcher.group(1)):-1);
    }

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		// TODO Auto-generated method stub
		for(int i=this.gobjExplosionSettingsList.size()-1; i>=0; i--) {
			this.gobjExplosionSettingsList.get(i).saveSettings(settings, this.getElementPrefix(i+1));
		}
	}
		
	public ExplosionSettings getExplosionSettings(String strKey, Set<String> containedNodes) {
		//List<ExplosionSettings> oESL = this.gobjExplosionSettingsList.stream().filter(eS -> eS.getContainedNodes().equals(containedNodes)).collect(Collectors.toList());
		List<ExplosionSettings> oESL = this.gobjExplosionSettingsList.stream().filter(eS -> eS.getKey().equals(containedNodes)).collect(Collectors.toList());
		
		return (oESL.size()==0?null:oESL.get(0));
	}
	
	public ExplosionSettings getExplosionSettings(Set<String> containedNodesIds) {
		
		if(containedNodesIds == null || containedNodesIds.isEmpty()) return null;
		
		List<ExplosionSettings> oESL = this.gobjExplosionSettingsList.stream().filter(eS -> eS.getContainedNodesIds().equals(containedNodesIds)).collect(Collectors.toList());
		
		return (oESL.size()==0?null:oESL.get(0));
	}
	
	protected ExplosionSettings getActiveExplosionSettings() { return (this.gobjActiveExplosionSettingsList.isEmpty()?null:this.gobjActiveExplosionSettingsList.peek());}
	
	public boolean setActiveExplosionSettings(ExplosionSettings objES, boolean bolActive) {
		boolean wasActive = this.gobjActiveExplosionSettingsList.remove(objES);
			
		if(bolActive && objES!=null) {
			if(!this.gobjExplosionSettingsList.contains(objES)) return false;
			this.gobjActiveExplosionSettingsList.push(objES);
			if(!wasActive) {
				objES.setSelectedNodes(new ArrayList<>());
				objES.setSelectedEdges(new ArrayList<>());
			}
		}
		return true;
	}
	
	public boolean setActiveExplosionSettings(ExplosionSettings objActivateES, ExplosionSettings objDeactivateES) {
		this.gobjActiveExplosionSettingsList.remove(objDeactivateES);
		boolean wasActive = false;
		if(objActivateES!=null) wasActive = this.gobjActiveExplosionSettingsList.remove(objActivateES);
		
		if(objActivateES!=null) {
			if(!this.gobjExplosionSettingsList.contains(objActivateES)) return false;
			this.gobjActiveExplosionSettingsList.push(objActivateES);
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
			this.gobjExplosionSettingsList.add(objES);
		}
		
		return (this.setActiveExplosionSettings(objES,true)?objES:null);
	}
	
	public void clearActiveExplosionSettings() {
	  if(this.gobjActiveExplosionSettingsList != null) this.gobjActiveExplosionSettingsList.clear();	
	}
	
	public ExplosionSettings setActiveExplosionSettings(Set<String> containedNodes, boolean bolActive) {
		if(containedNodes==null || containedNodes.isEmpty()) {
			this.gobjActiveExplosionSettingsList.clear();
		} else {
			ExplosionSettings objES = this.getExplosionSettings(containedNodes);
			if(objES!=null && this.setActiveExplosionSettings(objES,bolActive)) return objES;
		}
		return null;
	}

}
