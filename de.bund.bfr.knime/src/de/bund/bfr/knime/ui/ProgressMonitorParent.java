/*******************************************************************************
 * Copyright (c) 2014-2026 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.ui;

import java.util.ArrayList;
import java.util.List;

public class ProgressMonitorParent {
	private IProgressMonitor hookedProgressMonitor;
	
	private List<IProgressMonitor> childMonitors = new ArrayList<>();
	private double shareSum = 0;
	private int currentSubMonitorIndex = -1;
	
	public ProgressMonitorParent(IProgressMonitor hookedProgressMonitor, int childMonitorCount) {
		this.hookedProgressMonitor = hookedProgressMonitor;
		this.addSubMonitors(childMonitorCount);
	}
	
    private void addSubMonitor(double progressShare) {
    	this.childMonitors.add(new IProgressMonitor() {

    		private final double shareOffset = shareSum;
    		private final double share = progressShare;
    		
			@Override
			public void setMessage(String message) {
				// do nothing
			}

			@Override
			public void setProgress(int progress) {
				int parentProgress = (int)Math.max(0, Math.min(100, (shareOffset + share * progress / 100.0) / shareSum * 100));
				hookedProgressMonitor.setProgress(parentProgress);
			}

			@Override
			public boolean isCanceled() {
				return hookedProgressMonitor.isCanceled();
			}
    		
    	});
    	shareSum += progressShare;
    }
    
    private void addSubMonitors(int count) {
    	for (int i = 1; i <= count; i++) addSubMonitor(100);
    }
    
    public IProgressMonitor getNextSubTaskMonitor() {
    	if (currentSubMonitorIndex >= childMonitors.size() - 1) {
    		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        	StackTraceElement item = trace[2];
        	System.err.println(
        		"Trying to use monitor " + (currentSubMonitorIndex + 2) + " of " + childMonitors.size() + " " + item.toString()
        	);
        	return null;
    	} else {
    		++currentSubMonitorIndex;
    		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
    		StackTraceElement item = trace[2];
    		System.out.println(
    			"Using monitor " + (currentSubMonitorIndex + 1) + " of " + childMonitors.size() + " " + item.toString()
    		);
    	}
    	return childMonitors.get(currentSubMonitorIndex);
    }
}
