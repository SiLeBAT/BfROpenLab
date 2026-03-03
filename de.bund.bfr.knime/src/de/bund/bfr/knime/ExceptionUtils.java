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

import java.lang.OutOfMemoryError;

public class ExceptionUtils {
	private final static String ERR_MSG_OUT_OF_JAVA_HEAP_SPACE = "Java heap space";
	private final static String SQL_EXCEPTION_OUT_OF_MEMORY_MSG = "java.lang.OutOfMemoryError: Java heap space";
	
	public static OutOfMemoryError createNewOutOfJavaHeapSpaceError() {
		return new OutOfMemoryError(ERR_MSG_OUT_OF_JAVA_HEAP_SPACE);
	}

	public static void rethrowOutOfMemoryException(Throwable ex) {
		if (ex instanceof OutOfMemoryError) {
			ex.printStackTrace();
			throw (OutOfMemoryError)ex;
		} 
		else if (ex.getMessage().equals(SQL_EXCEPTION_OUT_OF_MEMORY_MSG)) {
			ex.printStackTrace();
			throw createNewOutOfJavaHeapSpaceError();
		}
	}
	
	public static boolean isOutOfJavaHeapSpaceError(Throwable throwable) {
		return throwable instanceof OutOfMemoryError && throwable.getMessage().equals(ERR_MSG_OUT_OF_JAVA_HEAP_SPACE);
	}
}