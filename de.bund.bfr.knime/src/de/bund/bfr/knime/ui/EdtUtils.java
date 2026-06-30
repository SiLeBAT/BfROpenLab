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

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class EdtUtils {
	static class AnswerWrapper {
		int answer = -1;
	}
	public static int askQuestionInEdt(Component parentComponent, String message, String title, int optionType, Icon icon, String[] options, String initialValue) {
		final AnswerWrapper answerWrapper = new AnswerWrapper();
		
		Runnable runnable = new Runnable(){
		    public void run()
		    {
		    	int n = JOptionPane.showOptionDialog(
		    		parentComponent,
	    			message,
	    			title,
	    			optionType,
	    			JOptionPane.QUESTION_MESSAGE,
	    			null,
	    			options,
	    			initialValue);
		    	answerWrapper.answer =  n;
		    }
		};
		if (SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(runnable);
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
				return -1;
			}
		}
		
		return answerWrapper.answer;
	}
}
