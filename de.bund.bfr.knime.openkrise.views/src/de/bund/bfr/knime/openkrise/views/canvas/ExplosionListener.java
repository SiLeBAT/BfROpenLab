package de.bund.bfr.knime.openkrise.views.canvas;

import java.util.EventListener;

public interface ExplosionListener extends EventListener{
	
	void closeExplosionViewRequested(IExplosionCanvas<?> source);

}
