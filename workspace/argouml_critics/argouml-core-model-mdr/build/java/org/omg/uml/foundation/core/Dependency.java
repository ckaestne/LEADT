package org.omg.uml.foundation.core;

/**
 * Dependency object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface Dependency extends org.omg.uml.foundation.core.Relationship {
    /**
     * Returns the value of reference client.
     * @return Value of reference client. Element type: {@link org.omg.uml.foundation.core.ModelElement}
     */
    public java.util.Collection<org.omg.uml.foundation.core.ModelElement> getClient();
    /**
     * Returns the value of reference supplier.
     * @return Value of reference supplier. Element type: {@link org.omg.uml.foundation.core.ModelElement}
     */
    public java.util.Collection<org.omg.uml.foundation.core.ModelElement> getSupplier();
}
