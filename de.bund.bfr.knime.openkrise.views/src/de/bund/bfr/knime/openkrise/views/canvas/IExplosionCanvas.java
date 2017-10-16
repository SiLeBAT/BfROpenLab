package de.bund.bfr.knime.openkrise.views.canvas;

import java.util.Set;


public interface IExplosionCanvas<V> {
	
  void addExplosionListener(ExplosionListener listener);

  void removeExplosionListener(ExplosionListener listener);
  
  public Set<V> getBoundaryNodes();
  
}
