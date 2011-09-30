package org.omg.uml.behavioralelements.collaborations;

/**
 * InteractionInstanceSet object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface InteractionInstanceSet extends org.omg.uml.foundation.core.ModelElement {
    /**
     * Returns the value of reference context.
     * @return Value of reference context.
     */
    public org.omg.uml.behavioralelements.collaborations.CollaborationInstanceSet getContext();
    /**
     * Sets the value of reference context. See {@link #getContext} for description 
     * on the reference.
     * @param newValue New value to be set.
     */
    public void setContext(org.omg.uml.behavioralelements.collaborations.CollaborationInstanceSet newValue);
    /**
     * Returns the value of reference interaction.
     * @return Value of reference interaction.
     */
    public org.omg.uml.behavioralelements.collaborations.Interaction getInteraction();
    /**
     * Sets the value of reference interaction. See {@link #getInteraction} for 
     * description on the reference.
     * @param newValue New value to be set.
     */
    public void setInteraction(org.omg.uml.behavioralelements.collaborations.Interaction newValue);
    /**
     * Returns the value of reference participatingStimulus.
     * @return Value of reference participatingStimulus. Element type: {@link 
     * org.omg.uml.behavioralelements.commonbehavior.Stimulus}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.commonbehavior.Stimulus> getParticipatingStimulus();
}
