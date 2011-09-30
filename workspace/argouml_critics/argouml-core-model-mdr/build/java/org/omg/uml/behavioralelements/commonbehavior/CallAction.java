package org.omg.uml.behavioralelements.commonbehavior;

/**
 * CallAction object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface CallAction extends org.omg.uml.behavioralelements.commonbehavior.Action {
    /**
     * Returns the value of reference operation.
     * @return Value of reference operation.
     */
    public org.omg.uml.foundation.core.Operation getOperation();
    /**
     * Sets the value of reference operation. See {@link #getOperation} for description 
     * on the reference.
     * @param newValue New value to be set.
     */
    public void setOperation(org.omg.uml.foundation.core.Operation newValue);
}
