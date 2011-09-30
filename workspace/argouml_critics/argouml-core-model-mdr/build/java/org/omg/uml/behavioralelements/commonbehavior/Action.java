package org.omg.uml.behavioralelements.commonbehavior;

/**
 * Action object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface Action extends org.omg.uml.foundation.core.ModelElement {
    /**
     * Returns the value of attribute recurrence.
     * @return Value of attribute recurrence.
     */
    public org.omg.uml.foundation.datatypes.IterationExpression getRecurrence();
    /**
     * Sets the value of recurrence attribute. See {@link #getRecurrence} for 
     * description on the attribute.
     * @param newValue New value to be set.
     */
    public void setRecurrence(org.omg.uml.foundation.datatypes.IterationExpression newValue);
    /**
     * Returns the value of attribute target.
     * @return Value of attribute target.
     */
    public org.omg.uml.foundation.datatypes.ObjectSetExpression getTarget();
    /**
     * Sets the value of target attribute. See {@link #getTarget} for description 
     * on the attribute.
     * @param newValue New value to be set.
     */
    public void setTarget(org.omg.uml.foundation.datatypes.ObjectSetExpression newValue);
    /**
     * Returns the value of attribute isAsynchronous.
     * @return Value of attribute isAsynchronous.
     */
    public boolean isAsynchronous();
    /**
     * Sets the value of isAsynchronous attribute. See {@link #isAsynchronous} 
     * for description on the attribute.
     * @param newValue New value to be set.
     */
    public void setAsynchronous(boolean newValue);
    /**
     * Returns the value of attribute script.
     * @return Value of attribute script.
     */
    public org.omg.uml.foundation.datatypes.ActionExpression getScript();
    /**
     * Sets the value of script attribute. See {@link #getScript} for description 
     * on the attribute.
     * @param newValue New value to be set.
     */
    public void setScript(org.omg.uml.foundation.datatypes.ActionExpression newValue);
    /**
     * Returns the value of reference actualArgument.
     * @return Value of reference actualArgument. Element type: {@link org.omg.uml.behavioralelements.commonbehavior.Argument}
     */
    public java.util.List<org.omg.uml.behavioralelements.commonbehavior.Argument> getActualArgument();
    /**
     * Returns the value of reference actionSequence.
     * @return Value of reference actionSequence.
     */
    public org.omg.uml.behavioralelements.commonbehavior.ActionSequence getActionSequence();
    /**
     * Sets the value of reference actionSequence. See {@link #getActionSequence} 
     * for description on the reference.
     * @param newValue New value to be set.
     */
    public void setActionSequence(org.omg.uml.behavioralelements.commonbehavior.ActionSequence newValue);
}
