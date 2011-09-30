package org.omg.uml.foundation.core;

/**
 * Flow object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface Flow extends org.omg.uml.foundation.core.Relationship {
    /**
     * Returns the value of reference target.
     * @return Value of reference target. Element type: {@link org.omg.uml.foundation.core.ModelElement}
     */
    public java.util.Collection<org.omg.uml.foundation.core.ModelElement> getTarget();
    /**
     * Returns the value of reference source.
     * @return Value of reference source. Element type: {@link org.omg.uml.foundation.core.ModelElement}
     */
    public java.util.Collection<org.omg.uml.foundation.core.ModelElement> getSource();
}
