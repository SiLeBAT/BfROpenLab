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
/**
 * 
 */
package de.bund.bfr.knime.openkrise.db;

/**
 * @author Weiser
 *
 */

public class Levenshtein {
    //****************************
    // Get minimum of three values
    //****************************

    private static int Minimum (int a, int b, int c) {
    int mi;

      mi = a;
      if (b < mi) {
        mi = b;
      }
      if (c < mi) {
        mi = c;
      }
      return mi;

    }

    //*****************************
    // Compute Levenshtein distance
    //*****************************

    public static int LD (String s, String t) {
    int d[][]; // matrix
    int n; // length of s
    int m; // length of t
    int i; // iterates through s
    int j; // iterates through t
    char s_i; // ith character of s
    char t_j; // jth character of t
    int cost; // cost

    if (s == null && t == null) return 0;
    else if (s == null || t == null) return 100000;
    
      // Step 1

      n = s.length ();
      m = t.length ();
      if (n == 0) {
        return m;
      }
      if (m == 0) {
        return n;
      }
      d = new int[n+1][m+1];

      // Step 2

      for (i = 0; i <= n; i++) {
        d[i][0] = i;
      }

      for (j = 0; j <= m; j++) {
        d[0][j] = j;
      }

      // Step 3

      for (i = 1; i <= n; i++) {

        s_i = s.charAt (i - 1);

        // Step 4

        for (j = 1; j <= m; j++) {

          t_j = t.charAt (j - 1);

          // Step 5

          if (s_i == t_j) {
            cost = 0;
          }
          else {
            cost = 1;
          }

          // Step 6

          d[i][j] = Minimum (d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1] + cost);

        }

      }

      // Step 7

      return d[n][m];

    }

}
