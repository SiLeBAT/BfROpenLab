package de.bund.bfr.knime.sbml.reader;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "SbmlReader" Node.
 * 
 * 
 * @author Christian Thoens
 */
public class SbmlReaderNodeFactory extends NodeFactory<SbmlReaderNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SbmlReaderNodeModel createNodeModel() {
		return new SbmlReaderNodeModel();
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
	public NodeView<SbmlReaderNodeModel> createNodeView(final int viewIndex,
			final SbmlReaderNodeModel nodeModel) {
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
		return new SbmlReaderNodeDialog();
	}

}
