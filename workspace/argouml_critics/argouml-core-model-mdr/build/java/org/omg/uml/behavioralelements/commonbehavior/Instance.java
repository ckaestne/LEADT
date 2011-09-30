package org.omg.uml.behavioralelements.commonbehavior;

/**
 * Instance object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface Instance extends org.omg.uml.foundation.core.ModelElement {
    /**
     * Returns the value of reference classifier.
     * @return Value of reference classifier. Element type: {@link org.omg.uml.foundation.core.Classifier}
     */
    public java.util.Collection<org.omg.uml.foundation.core.Classifier> getClassifier();
    /**
     * Returns the value of reference linkEnd.
     * @return Value of reference linkEnd. Element type: {@link org.omg.uml.behavioralelements.commonbehavior.LinkEnd}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.commonbehavior.LinkEnd> getLinkEnd();
    /**
     * Returns the value of reference slot.
     * @return Value of reference slot. Element type: {@link org.omg.uml.behavioralelements.commonbehavior.AttributeLink}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.commonbehavior.AttributeLink> getSlot();
    /**
     * Returns the value of reference componentInstance.
     * @return Value of reference componentInstance.
     */
    public org.omg.uml.behavioralelements.commonbehavior.ComponentInstance getComponentInstance();
    /**
     * Sets the value of reference componentInstance. See {@link #getComponentInstance} 
     * for description on the reference.
     * @param newValue New value to be set.
     */
    public void setComponentInstance(org.omg.uml.behavioralelements.commonbehavior.ComponentInstance newValue);
    /**
     * Returns the value of reference ownedInstance.
     * @return Value of reference ownedInstance. Element type: {@link org.omg.uml.behavioralelements.commonbehavior.Instance}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.commonbehavior.Instance> getOwnedInstance();
    /**
     * Returns the value of reference ownedLink.
     * @return Value of reference ownedLink. Element type: {@link org.omg.uml.behavioralelements.commonbehavior.Link}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.commonbehavior.Link> getOwnedLink();
}
