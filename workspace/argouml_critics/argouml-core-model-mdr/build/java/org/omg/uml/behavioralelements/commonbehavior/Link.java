package org.omg.uml.behavioralelements.commonbehavior;

/**
 * Link object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface Link extends org.omg.uml.foundation.core.ModelElement {
    /**
     * Returns the value of reference association.
     * @return Value of reference association.
     */
    public org.omg.uml.foundation.core.UmlAssociation getAssociation();
    /**
     * Sets the value of reference association. See {@link #getAssociation} for 
     * description on the reference.
     * @param newValue New value to be set.
     */
    public void setAssociation(org.omg.uml.foundation.core.UmlAssociation newValue);
    /**
     * Returns the value of reference connection.
     * @return Value of reference connection. Element type: {@link org.omg.uml.behavioralelements.commonbehavior.LinkEnd}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.commonbehavior.LinkEnd> getConnection();
}
