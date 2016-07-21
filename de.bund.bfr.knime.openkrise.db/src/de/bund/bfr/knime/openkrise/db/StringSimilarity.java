/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StringSimilarity {
	 
	// https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Dice's_coefficient
	
	//Note that this implementation is case-sensitive!
	public static double diceCoefficient(String s1, String s2)
	{
		s1 = s1.toUpperCase();
		s2 = s2.toUpperCase();
		
		Set<String> nx = new HashSet<String>();
		Set<String> ny = new HashSet<String>();
	 
		for (int i=0; i < s1.length()-1; i++) {
			char x1 = s1.charAt(i);
			char x2 = s1.charAt(i+1);
			String tmp = "" + x1 + x2;
			nx.add(tmp);
		}
		for (int j=0; j < s2.length()-1; j++) {
			char y1 = s2.charAt(j);
			char y2 = s2.charAt(j+1);
			String tmp = "" + y1 + y2;
			ny.add(tmp);
		}
	 
		Set<String> intersection = new HashSet<String>(nx);
		intersection.retainAll(ny);
		double totcombigrams = intersection.size();
	 
		return (2*totcombigrams) / (nx.size()+ny.size());
	}
	 
	/**
	 * Here's an optimized version of the dice coefficient calculation. It takes
	 * advantage of the fact that a bigram of 2 chars can be stored in 1 int, and
	 * applies a matching algorithm of O(n*log(n)) instead of O(n*n).
	 * 
	 * <p>Note that, at the time of writing, this implementation differs from the
	 * other implementations on this page. Where the other algorithms incorrectly
	 * store the generated bigrams in a set (discarding duplicates), this
	 * implementation actually treats multiple occurrences of a bigram as unique.
	 * The correctness of this behavior is most easily seen when getting the
	 * similarity between "GG" and "GGGGGGGG", which should obviously not be 1.
	 * 
	 * @param s The first string
	 * @param t The second String
	 * @return The dice coefficient between the two input strings. Returns 0 if one
	 *         or both of the strings are {@code null}. Also returns 0 if one or both
	 *         of the strings contain less than 2 characters and are not equal.
	 * @author Jelle Fresen
	 */
	public static double diceCoefficientOptimized(String s, String t)
	{
		// Verifying the input:
		if (s == null || t == null)
			return 0;
		// Quick check to catch identical objects:
		if (s == t)
			return 1;
	        // avoid exception for single character searches
	        if (s.length() < 2 || t.length() < 2)
	            return 0;
	 
			s = s.toUpperCase();
			t = t.toUpperCase();

			// Create the bigrams for string s:
		final int n = s.length()-1;
		final int[] sPairs = new int[n];
		for (int i = 0; i <= n; i++)
			if (i == 0)
				sPairs[i] = s.charAt(i) << 16;
			else if (i == n)
				sPairs[i-1] |= s.charAt(i);
			else
				sPairs[i] = (sPairs[i-1] |= s.charAt(i)) << 16;
	 
		// Create the bigrams for string t:
		final int m = t.length()-1;
		final int[] tPairs = new int[m];
		for (int i = 0; i <= m; i++)
			if (i == 0)
				tPairs[i] = t.charAt(i) << 16;
			else if (i == m)
				tPairs[i-1] |= t.charAt(i);
			else
				tPairs[i] = (tPairs[i-1] |= t.charAt(i)) << 16;
	 
		// Sort the bigram lists:
		Arrays.sort(sPairs);
		Arrays.sort(tPairs);
	 
		// Count the matches:
		int matches = 0, i = 0, j = 0;
		while (i < n && j < m)
		{
			if (sPairs[i] == tPairs[j])
			{
				matches += 2;
				i++;
				j++;
			}
			else if (sPairs[i] < tPairs[j])
				i++;
			else
				j++;
		}
		return (double)matches/(n+m);
	}
	
	
	// http://stackoverflow.com/questions/653157/a-better-similarity-ranking-algorithm-for-variable-length-strings
	// http://www.catalysoft.com/articles/StrikeAMatch.html
	
	/** @return an array of adjacent letter pairs contained in the input string */
	   private static String[] letterPairs(String str) {
	       int numPairs = str.length()-1;
	       String[] pairs = new String[numPairs];
	       for (int i=0; i<numPairs; i++) {
	           pairs[i] = str.substring(i,i+2);
	       }
	       return pairs;
	   }
	   
	   /** @return an ArrayList of 2-character Strings. */
	   private static ArrayList<String> wordLetterPairs(String str) {
	       ArrayList<String> allPairs = new ArrayList<>();
	       // Tokenize the string and put the tokens/words into an array
	       String[] words = str.split("\\s");
	       // For each word
	       for (int w=0; w < words.length; w++) {
	    	   if (words[w].length() > 0) {
		           // Find the pairs of characters
		           String[] pairsInWord = letterPairs(words[w]);
		           for (int p=0; p < pairsInWord.length; p++) {
		               allPairs.add(pairsInWord[p]);
		           }
	    	   }
	       }
	       return allPairs;
	   }
	   
	   /** @return lexical similarity value in the range [0,1] */
	   public static double compareStringsStrikeAMatch(String str1, String str2) {
			// Verifying the input:
		   if (str1 == null || str2 == null || str1.length() < 2 || str2.length() < 2) return 0;
	       ArrayList<String> pairs1 = wordLetterPairs(str1.toUpperCase());
	       ArrayList<String> pairs2 = wordLetterPairs(str2.toUpperCase());
	       int intersection = 0;
	       int union = pairs1.size() + pairs2.size();
	       for (int i=0; i<pairs1.size(); i++) {
	           Object pair1=pairs1.get(i);
	           for(int j=0; j<pairs2.size(); j++) {
	               Object pair2=pairs2.get(j);
	               if (pair1.equals(pair2)) {
	                   intersection++;
	                   pairs2.remove(j);
	                   break;
	               }
	           }
	       }
	       return (2.0*intersection)/union;
	   }
}
