package org.omg.uml.behavioralelements.statemachines;

/**
 * SubmachineState object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface SubmachineState extends org.omg.uml.behavioralelements.statemachines.CompositeState {
    /**
     * Returns the value of reference submachine.
     * @return Value of reference submachine.
     */
    public org.omg.uml.behavioralelements.statemachines.StateMachine getSubmachine();
    /**
     * Sets the value of reference submachine. See {@link #getSubmachine} for 
     * description on the reference.
     * @param newValue New value to be set.
     */
    public void setSubmachine(org.omg.uml.behavioralelements.statemachines.StateMachine newValue);
}
