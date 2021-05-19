/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.util.json;

import javax.json.JsonValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.json.JSONCell;
import org.knime.core.data.json.JSONCellFactory;
import org.knime.core.data.json.JSONValue;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NotConfigurableException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Iterables;

public class Utils {
  
  public static JsonValue extractJsonValueFromBufferedDataTable(BufferedDataTable table) throws NotConfigurableException {
    return extractJsonValueFromBufferedDataTable(table, JsonConstants.JSON_COLUMN);
  }
  
  public static JsonValue extractJsonValueFromBufferedDataTable(BufferedDataTable table, String columnName) throws NotConfigurableException {
    if(table != null) {
      int columnIndex = table.getSpec().findColumnIndex(columnName);
      if(columnIndex<0) throw new NotConfigurableException("Column " + columnName + " is missing.");
      
      DataRow row = Iterables.getFirst(table, null);
      if(row==null) throw new NotConfigurableException("No cells in column " + columnName + ".");
      
      DataCell cell = row.getCell(columnIndex);

      if (cell.isMissing()) {
          throw new NotConfigurableException("Cell in column " + columnName + " is missing.");
      }

      return ((JSONValue) cell).getJsonValue();
    
    }
    return null;
  }
  
  protected static DataTableSpec createJSONTableSpec() {
    return  new DataTableSpec(new DataColumnSpecCreator(JsonConstants.JSON_COLUMN, JSONCell.TYPE).createSpec());
  }
  
  protected static BufferedDataTable convertJsonValueToTable(JsonValue json, ExecutionContext exec) throws JsonProcessingException {
    
    BufferedDataContainer container = exec.createDataContainer(Utils.createJSONTableSpec());
    
    if(json!=null) container.addRowToTable(new DefaultRow(RowKey.createRowKey(0L),
        JSONCellFactory.create(json)));
    
    container.close();

    return container.getTable();
  }
  
}
