package de.bund.bfr.knime.openkrise;

import java.util.Set;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.gis.views.canvas.dialogs.PropertySelectionBox;
import de.bund.bfr.knime.gis.views.canvas.dialogs.PropertySelector;
import de.bund.bfr.knime.gis.views.canvas.dialogs.PropertySelectorCreator;

public class TracingPropertySelectorCreator implements PropertySelectorCreator {

	@Override
	public PropertySelector createSelector(Set<String> properties) {
		return new PropertySelectionBox(KnimeUtils.OBJECT_ORDERING.sortedCopy(properties));
	}
}
