package de.bund.bfr.knime.openkrise.util.json;

import javax.json.JsonValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.json.JSONValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NotConfigurableException;
import com.google.common.collect.Iterables;

public class Utils {
  
  public static JsonValue extractJsonValueFromBufferedDataTable(BufferedDataTable table) throws NotConfigurableException {
    if(table != null) {
      DataRow row = Iterables.getFirst(table, null);
      DataCell cell = row.getCell(table.getSpec().findColumnIndex(JsonConstants.JSON_COLUMN));

      if (cell.isMissing()) {
          throw new NotConfigurableException("Cell in " + JsonConstants.JSON_COLUMN + " is missing");
      }

      return ((JSONValue) cell).getJsonValue();
    
    }
    return null;
  }
  
}
