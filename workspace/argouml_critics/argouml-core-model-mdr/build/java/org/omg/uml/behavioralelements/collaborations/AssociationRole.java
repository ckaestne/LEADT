package org.omg.uml.behavioralelements.collaborations;

/**
 * AssociationRole object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface AssociationRole extends org.omg.uml.foundation.core.UmlAssociation {
    /**
     * Returns the value of attribute multiplicity.
     * @return Value of attribute multiplicity.
     */
    public org.omg.uml.foundation.datatypes.Multiplicity getMultiplicity();
    /**
     * Sets the value of multiplicity attribute. See {@link #getMultiplicity} 
     * for description on the attribute.
     * @param newValue New value to be set.
     */
    public void setMultiplicity(org.omg.uml.foundation.datatypes.Multiplicity newValue);
    /**
     * Returns the value of reference base.
     * @return Value of reference base.
     */
    public org.omg.uml.foundation.core.UmlAssociation getBase();
    /**
     * Sets the value of reference base. See {@link #getBase} for description 
     * on the reference.
     * @param newValue New value to be set.
     */
    public void setBase(org.omg.uml.foundation.core.UmlAssociation newValue);
    /**
     * Returns the value of reference message.
     * @return Value of reference message. Element type: {@link org.omg.uml.behavioralelements.collaborations.Message}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.collaborations.Message> getMessage();
    /**
     * Returns the value of reference conformingLink.
     * @return Value of reference conformingLink. Element type: {@link org.omg.uml.behavioralelements.commonbehavior.Link}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.commonbehavior.Link> getConformingLink();
}
