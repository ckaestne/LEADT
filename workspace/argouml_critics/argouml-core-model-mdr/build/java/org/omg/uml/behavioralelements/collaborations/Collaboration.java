package org.omg.uml.behavioralelements.collaborations;

/**
 * Collaboration object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface Collaboration extends org.omg.uml.foundation.core.GeneralizableElement, org.omg.uml.foundation.core.Namespace {
    /**
     * Returns the value of reference interaction.
     * @return Value of reference interaction. Element type: {@link org.omg.uml.behavioralelements.collaborations.Interaction}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.collaborations.Interaction> getInteraction();
    /**
     * Returns the value of reference representedClassifier.
     * @return Value of reference representedClassifier.
     */
    public org.omg.uml.foundation.core.Classifier getRepresentedClassifier();
    /**
     * Sets the value of reference representedClassifier. See {@link #getRepresentedClassifier} 
     * for description on the reference.
     * @param newValue New value to be set.
     */
    public void setRepresentedClassifier(org.omg.uml.foundation.core.Classifier newValue);
    /**
     * Returns the value of reference representedOperation.
     * @return Value of reference representedOperation.
     */
    public org.omg.uml.foundation.core.Operation getRepresentedOperation();
    /**
     * Sets the value of reference representedOperation. See {@link #getRepresentedOperation} 
     * for description on the reference.
     * @param newValue New value to be set.
     */
    public void setRepresentedOperation(org.omg.uml.foundation.core.Operation newValue);
    /**
     * Returns the value of reference constrainingElement.
     * @return Value of reference constrainingElement. Element type: {@link org.omg.uml.foundation.core.ModelElement}
     */
    public java.util.Collection<org.omg.uml.foundation.core.ModelElement> getConstrainingElement();
    /**
     * Returns the value of reference usedCollaboration.
     * @return Value of reference usedCollaboration. Element type: {@link org.omg.uml.behavioralelements.collaborations.Collaboration}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.collaborations.Collaboration> getUsedCollaboration();
}
