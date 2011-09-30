package org.omg.uml.behavioralelements.collaborations;

/**
 * AssociationEndRole object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface AssociationEndRole extends org.omg.uml.foundation.core.AssociationEnd {
    /**
     * Returns the value of attribute collaborationMultiplicity.
     * @return Value of attribute collaborationMultiplicity.
     */
    public org.omg.uml.foundation.datatypes.Multiplicity getCollaborationMultiplicity();
    /**
     * Sets the value of collaborationMultiplicity attribute. See {@link #getCollaborationMultiplicity} 
     * for description on the attribute.
     * @param newValue New value to be set.
     */
    public void setCollaborationMultiplicity(org.omg.uml.foundation.datatypes.Multiplicity newValue);
    /**
     * Returns the value of reference base.
     * @return Value of reference base.
     */
    public org.omg.uml.foundation.core.AssociationEnd getBase();
    /**
     * Sets the value of reference base. See {@link #getBase} for description 
     * on the reference.
     * @param newValue New value to be set.
     */
    public void setBase(org.omg.uml.foundation.core.AssociationEnd newValue);
    /**
     * Returns the value of reference availableQualifier.
     * @return Value of reference availableQualifier. Element type: {@link org.omg.uml.foundation.core.Attribute}
     */
    public java.util.Collection<org.omg.uml.foundation.core.Attribute> getAvailableQualifier();
}
