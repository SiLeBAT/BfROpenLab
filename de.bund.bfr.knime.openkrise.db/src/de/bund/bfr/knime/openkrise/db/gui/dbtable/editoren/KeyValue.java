/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db.gui.dbtable.editoren;

/**
 * @author Armin
 *
 */
class KeyValue {

  private Object key;
  private Object value;

  KeyValue () {
  }

  KeyValue (Object key, Object value) {
      this.key=key;
      this.value=value;
  }

  public Object getKey() {
      return this.key;
  }
  public void setKey(Object key) {
      this.key=key;
  }

  public Object getValue() {
      return this.value;
  }
  public void setValue(String value) {
      this.value=value;
  }

  @Override
  public String toString() {
      return this.value == null ? "" : this.value.toString();
  }

}
