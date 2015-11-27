/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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

import java.io.Serializable;
import java.util.Objects;

public final class Pair<T, M> implements Serializable {

	private static final long serialVersionUID = 1L;

	private final T first;
	private final M second;

	public Pair(final T first, final M second) {
		this.first = first;
		this.second = second;
	}

	public T getFirst() {
		return first;
	}

	public M getSecond() {
		return second;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof Pair)) {
			return false;
		}

		Pair<?, ?> p = (Pair<?, ?>) o;

		return Objects.equals(first, p.first) && Objects.equals(second, p.second);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(first) ^ Objects.hashCode(second);
	}

	@Override
	public String toString() {
		return "<" + first + ";" + second + ">";
	}
}