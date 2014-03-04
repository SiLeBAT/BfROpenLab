package de.bund.bfr.knime.gis.shapeview;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "ShapeView" Node.
 * 
 * 
 * @author Christian Thoens
 */
public class ShapeViewNodeFactory extends NodeFactory<ShapeViewNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ShapeViewNodeModel createNodeModel() {
		return new ShapeViewNodeModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNrNodeViews() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<ShapeViewNodeModel> createNodeView(final int viewIndex,
			final ShapeViewNodeModel nodeModel) {
		return new ShapeViewNodeView(nodeModel);
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
		return new ShapeViewNodeDialog();
	}

}
