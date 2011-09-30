package org.omg.uml.behavioralelements.collaborations;

/**
 * CollaborationInstanceSet object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface CollaborationInstanceSet extends org.omg.uml.foundation.core.ModelElement {
    /**
     * Returns the value of reference interactionInstanceSet.
     * @return Value of reference interactionInstanceSet. Element type: {@link 
     * org.omg.uml.behavioralelements.collaborations.InteractionInstanceSet}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.collaborations.InteractionInstanceSet> getInteractionInstanceSet();
    /**
     * Returns the value of reference collaboration.
     * @return Value of reference collaboration.
     */
    public org.omg.uml.behavioralelements.collaborations.Collaboration getCollaboration();
    /**
     * Sets the value of reference collaboration. See {@link #getCollaboration} 
     * for description on the reference.
     * @param newValue New value to be set.
     */
    public void setCollaboration(org.omg.uml.behavioralelements.collaborations.Collaboration newValue);
    /**
     * Returns the value of reference participatingInstance.
     * @return Value of reference participatingInstance. Element type: {@link 
     * org.omg.uml.behavioralelements.commonbehavior.Instance}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.commonbehavior.Instance> getParticipatingInstance();
    /**
     * Returns the value of reference participatingLink.
     * @return Value of reference participatingLink. Element type: {@link org.omg.uml.behavioralelements.commonbehavior.Link}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.commonbehavior.Link> getParticipatingLink();
    /**
     * Returns the value of reference constrainingElement.
     * @return Value of reference constrainingElement. Element type: {@link org.omg.uml.foundation.core.ModelElement}
     */
    public java.util.Collection<org.omg.uml.foundation.core.ModelElement> getConstrainingElement();
}
