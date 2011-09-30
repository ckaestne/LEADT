package org.omg.uml.foundation.core;

/**
 * Node object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface Node extends org.omg.uml.foundation.core.Classifier {
    /**
     * Returns the value of reference deployedComponent.
     * @return Value of reference deployedComponent. Element type: {@link org.omg.uml.foundation.core.Component}
     */
    public java.util.Collection<org.omg.uml.foundation.core.Component> getDeployedComponent();
}
