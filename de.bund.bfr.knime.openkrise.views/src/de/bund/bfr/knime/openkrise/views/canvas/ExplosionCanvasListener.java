package de.bund.bfr.knime.openkrise.views.canvas;

import de.bund.bfr.knime.gis.views.canvas.CanvasListener;

public interface ExplosionCanvasListener extends CanvasListener {
	
	void closeExplosionViewRequested(IExplosionCanvas<?> source);

}
