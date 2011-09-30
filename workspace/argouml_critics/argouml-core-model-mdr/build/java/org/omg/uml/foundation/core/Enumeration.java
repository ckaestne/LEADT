package org.omg.uml.foundation.core;

/**
 * Enumeration object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface Enumeration extends org.omg.uml.foundation.core.DataType {
    /**
     * Returns the value of reference literal.
     * @return Value of reference literal. Element type: {@link org.omg.uml.foundation.core.EnumerationLiteral}
     */
    public java.util.List<org.omg.uml.foundation.core.EnumerationLiteral> getLiteral();
}
