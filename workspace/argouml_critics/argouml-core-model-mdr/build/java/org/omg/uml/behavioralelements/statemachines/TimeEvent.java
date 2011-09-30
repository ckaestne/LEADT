package org.omg.uml.behavioralelements.statemachines;

/**
 * TimeEvent object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface TimeEvent extends org.omg.uml.behavioralelements.statemachines.Event {
    /**
     * Returns the value of attribute when.
     * @return Value of attribute when.
     */
    public org.omg.uml.foundation.datatypes.TimeExpression getWhen();
    /**
     * Sets the value of when attribute. See {@link #getWhen} for description 
     * on the attribute.
     * @param newValue New value to be set.
     */
    public void setWhen(org.omg.uml.foundation.datatypes.TimeExpression newValue);
}
