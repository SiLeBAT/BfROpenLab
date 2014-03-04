package de.bund.bfr.knime.gis.shapeproperties;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "GetShapeCenter" Node.
 * 
 * 
 * @author Christian Thoens
 */
public class ShapePropertiesNodeFactory extends
		NodeFactory<ShapePropertiesNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ShapePropertiesNodeModel createNodeModel() {
		return new ShapePropertiesNodeModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNrNodeViews() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<ShapePropertiesNodeModel> createNodeView(
			final int viewIndex, final ShapePropertiesNodeModel nodeModel) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new ShapePropertiesNodeDialog();
	}

}
