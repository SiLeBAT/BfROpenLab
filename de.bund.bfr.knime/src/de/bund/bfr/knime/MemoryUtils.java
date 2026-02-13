/*******************************************************************************
 * Copyright (c) 2014-2025 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime;

public class MemoryUtils {
	
	// private static double MIN_FREE_MEMORY_QUOTA = 0.05; //  5%
	private static long MIN_FREE_MEMORY_ABS = 20 * 1024 * 1024; //  20 MB
	
	public static long getPresumableFreeMemory() {
		Runtime runtime = Runtime.getRuntime();
		long allocatedMemory = runtime.totalMemory() - runtime.freeMemory();
		long presumableFreeMemory = runtime.maxMemory() - allocatedMemory;
		return presumableFreeMemory;
	}
	
	public static String formatMemoryAmount(long memoryAmount) {
		String result = "" + memoryAmount;
		String revResult = new StringBuilder(result).reverse().toString();
	    revResult = String.join(".", revResult.split("(?<=^(\\d{3,3})+)"));
		result = new StringBuilder(revResult).reverse().toString();
		return result;
	}
	
	public static String getFormatedPresumableFreeMemory() {
		long presumableFreeMemory = getPresumableFreeMemory();
		return formatMemoryAmount(presumableFreeMemory);
	}
	
	public static void checkMimimumRemainingMemory() {
		long freeMemory = getPresumableFreeMemory();
		if (freeMemory < MIN_FREE_MEMORY_ABS) throw ExceptionUtils.createNewOutOfJavaHeapSpaceError();
	}
	
	public static void printPresumableFreeMemory() {
		System.out.println("Freespace: " + getFormatedPresumableFreeMemory());
	}
}
