package org.omg.uml.behavioralelements.statemachines;

/**
 * ChangeEvent object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface ChangeEvent extends org.omg.uml.behavioralelements.statemachines.Event {
    /**
     * Returns the value of attribute changeExpression.
     * @return Value of attribute changeExpression.
     */
    public org.omg.uml.foundation.datatypes.BooleanExpression getChangeExpression();
    /**
     * Sets the value of changeExpression attribute. See {@link #getChangeExpression} 
     * for description on the attribute.
     * @param newValue New value to be set.
     */
    public void setChangeExpression(org.omg.uml.foundation.datatypes.BooleanExpression newValue);
}
