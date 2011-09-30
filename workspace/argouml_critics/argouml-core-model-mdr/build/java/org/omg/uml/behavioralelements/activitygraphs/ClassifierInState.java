package org.omg.uml.behavioralelements.activitygraphs;

/**
 * ClassifierInState object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface ClassifierInState extends org.omg.uml.foundation.core.Classifier {
    /**
     * Returns the value of reference type.
     * @return Value of reference type.
     */
    public org.omg.uml.foundation.core.Classifier getType();
    /**
     * Sets the value of reference type. See {@link #getType} for description 
     * on the reference.
     * @param newValue New value to be set.
     */
    public void setType(org.omg.uml.foundation.core.Classifier newValue);
    /**
     * Returns the value of reference inState.
     * @return Value of reference inState. Element type: {@link org.omg.uml.behavioralelements.statemachines.State}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.statemachines.State> getInState();
}
