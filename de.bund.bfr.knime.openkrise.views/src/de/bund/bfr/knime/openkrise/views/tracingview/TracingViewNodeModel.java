/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.json.JsonValue;
import org.knime.base.data.xml.SvgCell;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.image.png.PNGImageContent;
import org.knime.core.data.json.JSONCell;
import org.knime.core.data.json.JSONCellFactory;
import org.knime.core.data.json.JSONValue;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Iterables;
import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.NoInternalsNodeModel;
import de.bund.bfr.knime.gis.GisType;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.TracingUtils;
import de.bund.bfr.knime.openkrise.util.json.JsonConstants;
import de.bund.bfr.knime.openkrise.views.canvas.ITracingGisCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.TracingGraphCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.TracingOsmCanvas;

/**
 * This is the model implementation of TracingVisualizer.
 * 
 * 
 * @author Christian Thoens
 */
public class TracingViewNodeModel extends NoInternalsNodeModel {

	private TracingViewSettings set;
	
	private int count;
	private int maxCount;

	/**
	 * Constructor for the node model.
	 */
	protected TracingViewNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE,
				BufferedDataTable.TYPE_OPTIONAL, BufferedDataTable.TYPE_OPTIONAL },
				new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE, ImagePortObject.TYPE,
						ImagePortObject.TYPE, ImagePortObject.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE });
		set = new TracingViewSettings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
	    // required inputs
		BufferedDataTable nodeInTable = (BufferedDataTable) inObjects[0];
		BufferedDataTable edgeInTable = (BufferedDataTable) inObjects[1];
		BufferedDataTable tracingInTable = (BufferedDataTable) inObjects[2];
		// optional inputs
		
		BufferedDataTable shapeInTable = (BufferedDataTable) inObjects[3];
		BufferedDataTable configurationInTable = (BufferedDataTable) inObjects[4];
		
		// ToDo:
		// if configurationTable is specified update node settings
		if(configurationInTable != null) {
		    DataRow row = Iterables.getFirst(configurationInTable, null);
	        DataCell cell = row.getCell(configurationInTable.getSpec().findColumnIndex(JsonConstants.JSON_COLUMN));

	        if (cell.isMissing()) {
	            throw new Exception("Cell in " + JsonConstants.JSON_COLUMN + " is missing");
	        }

	        //JsonObject json = (JsonObject) ((JSONValue) cell).getJsonValue();
	        
	      //ToDo validate configuration
	      set.loadSettings(((JSONValue) cell).getJsonValue());
		  
		}
		
		GisType originalGisType = set.getGisType();

		if (set.getGisType() == GisType.SHAPEFILE && shapeInTable == null) {
			set.setGisType(GisType.MAPNIK);
		}

		TracingViewCanvasCreator creator = new TracingViewCanvasCreator(nodeInTable, edgeInTable, tracingInTable, shapeInTable,
				set);
		TracingGraphCanvas graphCanvas = creator.createGraphCanvas();
		ImagePortObject graphImage = CanvasUtils.getImage(set.isExportAsSvg(), graphCanvas);
		ImagePortObject gisImage;
		ImagePortObject combinedImage;

		if (creator.hasGisCoordinates()) {
			ITracingGisCanvas<?> gisCanvas = creator.createGisCanvas();

			if (gisCanvas instanceof TracingOsmCanvas) {
				((TracingOsmCanvas) gisCanvas).loadAllTiles();
			}

			gisImage = CanvasUtils.getImage(set.isExportAsSvg(), gisCanvas);
			combinedImage = CanvasUtils.getImage(set.isExportAsSvg(), graphCanvas, gisCanvas);
		} else {
			gisImage = CanvasUtils.getImage(set.isExportAsSvg());
			combinedImage = CanvasUtils.getImage(set.isExportAsSvg());
		}

		set.setGisType(originalGisType);

		creator.getSkippedDeliveryRows().forEach((key,
				value) -> setWarningMessage("Deliveries Table: Row " + key.getString() + " skipped (" + value + ")"));
		creator.getSkippedDeliveryRelationRows().forEach((key, value) -> setWarningMessage(
				"Deliveries Relations Table: Row " + key.getString() + " skipped (" + value + ")"));
		creator.getSkippedShapeRows().forEach(
				(key, value) -> setWarningMessage("Shapes Table: Row " + key.getString() + " skipped (" + value + ")"));

		graphCanvas.getOptionsPanel().setJoinEdges(false);

		count = 0;
		maxCount = graphCanvas.getNodes().size() + graphCanvas.getEdges().size();

		BufferedDataTable nodeOutTable = createTable(graphCanvas.getNodes(), graphCanvas.getNodeSchema().getMap(),
				createNodeOutSpec(nodeInTable.getSpec()), exec);
		BufferedDataTable edgeOutTable = createTable(graphCanvas.getEdges(), graphCanvas.getEdgeSchema().getMap(),
				createEdgeOutSpec(edgeInTable.getSpec()), exec);
		
		BufferedDataTable configurationOutTable = createConfigurationTable(exec);
		
		DataCell configurationJsonCell = createConfigurationJSONCell();
		
		BufferedDataTable aggDataOutTable = createAggregatedDataTable(exec, graphImage, gisImage, combinedImage, configurationJsonCell);
		
		return new PortObject[] { nodeOutTable, edgeOutTable, graphImage, gisImage, combinedImage, configurationOutTable, aggDataOutTable};
	}
	
	private BufferedDataTable createAggregatedDataTable(ExecutionContext exec, ImagePortObject graphImage, ImagePortObject gisImage, ImagePortObject combinedImage, DataCell configurationJsonCell ) {
	  
//	  List<DataColumnSpec> newNodeSpec = new ArrayList<>();
           
      DataCell graphImageCell = graphImage.toDataCell();
      DataCell gisImageCell = gisImage.toDataCell();
      DataCell combinedImageCell = combinedImage.toDataCell();
      
//      newNodeSpec.add(new DataColumnSpecCreator("Graph Image", graphImageCell.getType()).createSpec());
//      newNodeSpec.add(new DataColumnSpecCreator("GIS Image", gisImageCell.getType()).createSpec());
//      newNodeSpec.add(new DataColumnSpecCreator("Combined Image", combinedImageCell.getType()).createSpec());
//      newNodeSpec.add(new DataColumnSpecCreator("Configuration", configurationJsonCell.getType()).createSpec());
      
      DataTableSpec spec = createAggDataTableOutSpec(); //new DataTableSpec(newNodeSpec.toArray(new DataColumnSpec[0]));
      
      BufferedDataContainer nodeContainer = exec.createDataContainer(spec);
     
      nodeContainer.addRowToTable(new DefaultRow("0", new DataCell[] {graphImageCell, gisImageCell, combinedImageCell, configurationJsonCell}));
     
      nodeContainer.close();

      return nodeContainer.getTable();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		DataTableSpec nodeSpec = (DataTableSpec) inSpecs[0];
		DataTableSpec edgeSpec = (DataTableSpec) inSpecs[1];
		
		return new PortObjectSpec[] { createNodeOutSpec(nodeSpec), createEdgeOutSpec(edgeSpec),  
				CanvasUtils.getImageSpec(set.isExportAsSvg()), CanvasUtils.getImageSpec(set.isExportAsSvg()),
				CanvasUtils.getImageSpec(set.isExportAsSvg()), 
				createConfigurationOutSpec(), createAggDataTableOutSpec() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		set.saveSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		set.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
	}

	private BufferedDataTable createTable(Collection<? extends Element> elements, Map<String, Class<?>> propertyTypes,
			DataTableSpec spec, ExecutionContext exec) throws CanceledExecutionException {
		BufferedDataContainer nodeContainer = exec.createDataContainer(spec);
		int index = 0;

		int isSelectedIndex = spec.findColumnIndex(TracingColumns.IS_SELECTED);
		
		for (Element node : elements) {
			DataCell[] cells = new DataCell[spec.getNumColumns()];

			for (int i = 0; i < cells.length; i++) {
				cells[i] = DataType.getMissingCell();
			}

			propertyTypes.forEach((property, type) -> {
				int column = spec.findColumnIndex(property);

				if (column != -1) {
				    
					if (type == String.class) {
						cells[column] = IO.createCell((String) node.getProperties().get(property));
					} else if (type == Integer.class) {
						cells[column] = IO.createCell((Integer) node.getProperties().get(property));
					} else if (type == Double.class) {
						cells[column] = IO.createCell((Double) node.getProperties().get(property));
					} else if (type == Boolean.class) {
						cells[column] = IO.createCell((Boolean) node.getProperties().get(property));
					}
				  }

			});
			
			if(isSelectedIndex>=0) cells[isSelectedIndex] =  IO.createCell((Boolean) (node instanceof Node ? set.isNodeSelected(node.getId()):set.isEdgeSelected(node.getId())));

			nodeContainer.addRowToTable(new DefaultRow(index++ + "", cells));
			exec.checkCanceled();
			exec.setProgress((double) count / (double) maxCount);
			count++;
		}

		nodeContainer.close();

		return nodeContainer.getTable();
	}
	
	private DataTableSpec createAggDataTableOutSpec() {
	  
	  DataType imageType = set.isExportAsSvg()?SvgCell.TYPE:PNGImageContent.TYPE;
	  
	  return new DataTableSpec(new DataColumnSpec[] {
	      new DataColumnSpecCreator("Graph Image", imageType).createSpec(),
	      new DataColumnSpecCreator("GIS Image", imageType).createSpec(),
	      new DataColumnSpecCreator("Combined Image", imageType).createSpec(),
	      new DataColumnSpecCreator("Configuration", JSONCell.TYPE).createSpec()
	  });
	}
	
    private BufferedDataTable createConfigurationTable(ExecutionContext exec) throws InvalidSettingsException {
      
      BufferedDataContainer container = exec.createDataContainer(createConfigurationOutSpec());
            
      //JsonValue json = set.toJson();    
      
//      container.addRowToTable(new DefaultRow(RowKey.createRowKey(0L),
//          JSONCellFactory.create(json)));

      try {
        container.addRowToTable(new DefaultRow(RowKey.createRowKey(0L),
            createConfigurationJSONCell()));
      } catch (JsonProcessingException e) {
        throw(new InvalidSettingsException(e));
      }
      container.close();

      return container.getTable();
    }
    
    private DataCell createConfigurationJSONCell() throws JsonProcessingException {
      
      JsonValue json = set.toJson();    
      
      return JSONCellFactory.create(json);
    }

	private static DataTableSpec createNodeOutSpec(DataTableSpec nodeSpec) throws InvalidSettingsException {
		List<DataColumnSpec> newNodeSpec = new ArrayList<>();
		Map<String, DataType> columns = new LinkedHashMap<>();

		for (DataColumnSpec column : nodeSpec) {
			if (column.getName().equals(TracingColumns.ID)) {
				column = new DataColumnSpecCreator(column.getName(), StringCell.TYPE).createSpec();
			}

			column = TracingUtils.toCompatibleColumn(column);
			newNodeSpec.add(column);
			columns.put(column.getName(), column.getType());
		}

		for (String columnName : TracingColumns.STATION_IN_OUT_COLUMNS) {
			DataType type = TracingColumns.IN_OUT_COLUMN_TYPES.get(columnName);
			DataType oldType = columns.get(columnName);

			if (oldType == null) {
				newNodeSpec.add(new DataColumnSpecCreator(columnName, type).createSpec());
			} else if (!oldType.equals(type)) {
				throw new InvalidSettingsException("Type of column \"" + columnName + "\" must be \"" + type + "\"");
			}
		}
		
		for (String columnName : TracingColumns.STATION_OUTPORTONLY_COLUMNS) {
          DataType type = TracingColumns.IN_OUT_COLUMN_TYPES.get(columnName);
          DataType oldType = columns.get(columnName);

          if (oldType == null) {
              newNodeSpec.add(new DataColumnSpecCreator(columnName, type).createSpec());
          } else if (!oldType.equals(type)) {
              throw new InvalidSettingsException("Type of column \"" + columnName + "\" must be \"" + type + "\"");
          }
      }

		return new DataTableSpec(newNodeSpec.toArray(new DataColumnSpec[0]));
	}

	private static DataTableSpec createEdgeOutSpec(DataTableSpec edgeSpec) throws InvalidSettingsException {
		List<DataColumnSpec> newEdgeSpec = new ArrayList<>();
		Map<String, DataType> columns = new LinkedHashMap<>();

		for (DataColumnSpec column : edgeSpec) {
			if (column.getName().equals(TracingColumns.ID) || column.getName().equals(TracingColumns.FROM)
					|| column.getName().equals(TracingColumns.TO)) {
				column = new DataColumnSpecCreator(column.getName(), StringCell.TYPE).createSpec();
			}

			column = TracingUtils.toCompatibleColumn(column);
			newEdgeSpec.add(column);
			columns.put(column.getName(), column.getType());
		}

		for (String columnName : TracingColumns.DELIVERY_IN_OUT_COLUMNS) {
			DataType type = TracingColumns.IN_OUT_COLUMN_TYPES.get(columnName);
			DataType oldType = columns.get(columnName);

			if (oldType == null) {
				newEdgeSpec.add(new DataColumnSpecCreator(columnName, type).createSpec());
			} else if (!oldType.equals(type)) {
				throw new InvalidSettingsException("Type of column \"" + columnName + "\" must be \"" + type + "\"");
			}
		}
		
		for (String columnName : TracingColumns.DELIVERY_OUTPORTONLY_COLUMNS) {
          DataType type = TracingColumns.IN_OUT_COLUMN_TYPES.get(columnName);
          DataType oldType = columns.get(columnName);

          if (oldType == null) {
              newEdgeSpec.add(new DataColumnSpecCreator(columnName, type).createSpec());
          } else if (!oldType.equals(type)) {
              throw new InvalidSettingsException("Type of column \"" + columnName + "\" must be \"" + type + "\"");
          }
      }

		return new DataTableSpec(newEdgeSpec.toArray(new DataColumnSpec[0]));
	}
	
	private DataTableSpec createConfigurationOutSpec() {
	  return new DataTableSpec(new DataColumnSpecCreator(JsonConstants.JSON_COLUMN, JSONCell.TYPE).createSpec());
	}
}
