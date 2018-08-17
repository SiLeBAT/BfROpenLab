package de.bund.bfr.knime.openkrise.views.tracingview;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View;
import de.bund.bfr.knime.openkrise.views.canvas.ExplosionTracingGraphCanvas;

public class ExplosionGraphSettings extends GraphSettings {
  
  private static final String CFG_BOUNDARY_PARAMS = "GraphBoundaryParameters";
  
  private double[] boundaryParams;
  
  public ExplosionGraphSettings() {
    super();
  }
  
  public ExplosionGraphSettings(GraphSettings set) {
    super(set);
  }
   
  /*
   * loads graph settings 
   * (prefix is non empty for explosion graph settings)
   */
  protected void loadSettings(NodeSettingsRO settings, String prefix) {
      try {
          boundaryParams = settings.getDoubleArray(prefix + CFG_BOUNDARY_PARAMS);
      } catch (InvalidSettingsException e) {
      }
  }


  public void setFromCanvas(GraphCanvas canvas) {
    super.setFromCanvas(canvas);
    this.boundaryParams = ((ExplosionTracingGraphCanvas) canvas).getBoundaryParams();  
  }

  public void setToCanvas(GraphCanvas canvas) {
    ((ExplosionTracingGraphCanvas) canvas).setBoundaryParams(boundaryParams);
    super.setToCanvas(canvas);
  }
  
  /*
   * saves the graph settings
   * (prefix is non empty for explosion graph settings)
   */
  public void saveSettings(NodeSettingsWO settings, String prefix) {
    super.saveSettings(settings, prefix);
    settings.addDoubleArray(prefix + CFG_BOUNDARY_PARAMS, boundaryParams);
  }
  
  public void saveSettings(JsonConverter.JsonBuilder jsonBuilder, int index) {
    super.saveSettings(jsonBuilder, index);
    jsonBuilder.setExplosionGraphBoundary(index, this.boundaryParams);
  }
  
  public void loadSettings(View.ExplosionSettings.ExplosionGraphSettings graphView) {
    super.loadSettings(graphView);
    this.boundaryParams = graphView.boundaryParams;
  }
}
