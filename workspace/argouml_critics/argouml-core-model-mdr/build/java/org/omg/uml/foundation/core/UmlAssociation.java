package org.omg.uml.foundation.core;

/**
 * Association object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface UmlAssociation extends org.omg.uml.foundation.core.GeneralizableElement, org.omg.uml.foundation.core.Relationship {
    /**
     * Returns the value of reference connection.
     * @return Value of reference connection. Element type: {@link org.omg.uml.foundation.core.AssociationEnd}
     */
    public java.util.List<org.omg.uml.foundation.core.AssociationEnd> getConnection();
}
