package de.bund.bfr.knime.openkrise;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "MyKrisenInterfaces" Node.
 * 
 *
 * @author draaw
 */
public class MyKrisenInterfacesNodeFactory 
        extends NodeFactory<MyKrisenInterfacesNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public MyKrisenInterfacesNodeModel createNodeModel() {
        return new MyKrisenInterfacesNodeModel();
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
    public NodeView<MyKrisenInterfacesNodeModel> createNodeView(final int viewIndex,
            final MyKrisenInterfacesNodeModel nodeModel) {
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
        return new MyKrisenInterfacesNodeDialog();
    }

}

