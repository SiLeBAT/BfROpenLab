/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.NodeSettings;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View;

/*
 * class contains the settings for all explosion views
 */
public class ExplosionSettingsList extends NodeSettings {
	
	private final static String CFG_PREFIX = "CFG_EXPLOSION";
	
	private ArrayList<ExplosionSettings> explosionSettingsList; // list of explosion settings
	private Stack<ExplosionSettings> activeExplosionSettingsList; // stack of active explosion settings
	
    protected ExplosionSettingsList() {
		this.explosionSettingsList = new ArrayList<ExplosionSettings>();
		this.activeExplosionSettingsList = new Stack<ExplosionSettings>();
	}
	
	private static String getElementPrefix(int index) {
		return CFG_PREFIX + "_" + index + "_";
	}
	
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		// determine the indices of setting objects by checking for a certain prefix structure
	    explosionSettingsList.clear();
		Pattern pattern = Pattern.compile("^" + CFG_PREFIX +  "_([0-9]+)_");
		Set<Integer> indices = settings.keySet().stream().mapToInt(s -> getIndex(pattern, s)).filter(i -> (i>=0)).boxed().collect(Collectors.toSet());
		
		// for each found prefix do
		indices.forEach(i -> {
			ExplosionSettings eS = new ExplosionSettings();
			eS.loadSettings(settings, getElementPrefix(i));
			this.explosionSettingsList.add(eS);
		});
		
		this.activeExplosionSettingsList.clear();
	}
	
	/*
	 * return the index out of the text if it matches the pattern otherwise -1
	 */
	private static int getIndex(Pattern pattern, String text) {
  	  Matcher matcher = pattern.matcher(text);
  	  return (matcher.find()? Integer.parseInt(matcher.group(1)):-1);
    }

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		for(int i=this.explosionSettingsList.size()-1; i>=0; i--) {
			this.explosionSettingsList.get(i).saveSettings(settings, ExplosionSettingsList.getElementPrefix(i+1));
		}
	}
	
	public void saveSettings(JsonConverter.JsonBuilder jsonBuilder) {
      int n = this.explosionSettingsList.size();
      jsonBuilder.setExplosionCount(n);
      for(int i=0; i<n; ++i) {
          //settings.explosions[i] = new SettingsJson.View.ExplosionSettings();
          this.explosionSettingsList.get(i).saveSettings(jsonBuilder, i);
      }
    }
	
	public void loadSettings(View settings) {
      int n = settings.explosions.length;
      for(int i=0; i<n; ++i) {
        ExplosionSettings eS = new ExplosionSettings();
        eS.loadSettings(settings.explosions[i]);
        this.explosionSettingsList.add(eS);
      }
    }
	
	
	/*
	 * returns an explosion setting with the specified key if it exists otherwise null
	 */
	public ExplosionSettings getExplosionSettings(String key, Set<String> containedNodes) {
		List<ExplosionSettings> oESL = this.explosionSettingsList.stream().filter(eS -> eS.getKey().equals(key)).collect(Collectors.toList());
		
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

	public ExplosionSettings setActiveExplosionSettings(String key, Set<String> containedNodesIds, TracingViewSettings set) {
		ExplosionSettings objES = this.getExplosionSettings(key, containedNodesIds);
		
		if(objES==null) {
			objES=new ExplosionSettings(key, containedNodesIds, set);
			this.explosionSettingsList.add(objES);
		}
		
		return (this.setActiveExplosionSettings(objES,true)?objES:null);
	}
	
	public void clearActiveExplosionSettings() {
	  if(this.activeExplosionSettingsList != null) this.activeExplosionSettingsList.clear();	
	}

}
