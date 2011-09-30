package org.omg.uml.behavioralelements.statemachines;

/**
 * StateMachine object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface StateMachine extends org.omg.uml.foundation.core.ModelElement {
    /**
     * Returns the value of reference context.
     * @return Value of reference context.
     */
    public org.omg.uml.foundation.core.ModelElement getContext();
    /**
     * Sets the value of reference context. See {@link #getContext} for description 
     * on the reference.
     * @param newValue New value to be set.
     */
    public void setContext(org.omg.uml.foundation.core.ModelElement newValue);
    /**
     * Returns the value of reference top.
     * @return Value of reference top.
     */
    public org.omg.uml.behavioralelements.statemachines.State getTop();
    /**
     * Sets the value of reference top. See {@link #getTop} for description on 
     * the reference.
     * @param newValue New value to be set.
     */
    public void setTop(org.omg.uml.behavioralelements.statemachines.State newValue);
    /**
     * Returns the value of reference transitions.
     * @return Value of reference transitions. Element type: {@link org.omg.uml.behavioralelements.statemachines.Transition}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.statemachines.Transition> getTransitions();
    /**
     * Returns the value of reference submachineState.
     * @return Value of reference submachineState. Element type: {@link org.omg.uml.behavioralelements.statemachines.SubmachineState}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.statemachines.SubmachineState> getSubmachineState();
}
