/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db.gui.simsearch;

import java.util.Random;

public class RandomDummy {
	private static final Random rnd = new Random();
	private static final char[] SALTCHARS = ((String) "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcdefghijklmnopqrstuvwxyz").toCharArray();
	
	public static String getRandomText(int length) {
		 //StringBuffer salt = new StringBuffer();
		char[] res = new char[length];
		for(int i=0; i<length; i++) res[i] = SALTCHARS[rnd.nextInt(SALTCHARS.length)];
	    return res.toString();
	}
	
	public static String[] getRandomTexts(int count, int length) {
	  String[] randomText = new String[count];
	  for(int i=0; i<count; ++i) randomText[i] = getRandomText(length);
	  return randomText;
	}
	
	public static String manipulateText(String text, int number) {
		StringBuffer sb = new StringBuffer(text);
		for(int i=0; i<number; i++) {
			int k = rnd.nextInt(sb.length());
			if(rnd.nextFloat()<0.2) {
				//insert
				sb.insert(rnd.nextInt(sb.length()), SALTCHARS[rnd.nextInt(SALTCHARS.length)]);
			} else {
				//mismatch
				sb.setCharAt(rnd.nextInt(sb.length()), SALTCHARS[rnd.nextInt(SALTCHARS.length)]);
			}
		}
		return sb.toString();
	}

}
