package org.omg.uml.foundation.datatypes;

/**
 * Multiplicity object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface Multiplicity extends javax.jmi.reflect.RefObject {
    /**
     * Returns the value of reference range.
     * @return Value of reference range. Element type: {@link org.omg.uml.foundation.datatypes.MultiplicityRange}
     */
    public java.util.Collection<org.omg.uml.foundation.datatypes.MultiplicityRange> getRange();
}
